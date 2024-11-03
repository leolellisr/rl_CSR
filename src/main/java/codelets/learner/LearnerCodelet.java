package codelets.learner;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.learning.QLearning;
import br.unicamp.cst.representation.idea.Idea;
import codelets.motivation.DriverArray;
import coppelia.remoteApi;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import outsideCommunication.OutsideCommunication;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * @author L. L. Rossi (leolellisr)
 * Obs: This class represents the implementations present in the proposed scheme for: 
 * DiscretizationCodelet; adaptation; accommodation and assimilation. 
 * Procedural Memory is represented by QTable.
 */

public class LearnerCodelet extends Codelet 

{

    private int time_graph;


    private static int MAX_ACTION_NUMBER;

    private static int MAX_EXPERIMENTS_NUMBER;
    private QLearningSQL ql;
    

    private List saliencyMap;
    private List statesList;
    private ArrayList<Object> motivationMO;
    private List<String> actionsList;
    private List<QLearningSQL> qTableList;
    private List<Double>  rewardsList;
    private OutsideCommunication oc;
    private final int timeWindow;
    
    
    private double global_reward;
    private int action_number, num_tables;
    private int experiment_number,exp_s, exp_c;;
    private int stage;
    private String mode;
    private boolean debug = false;
    private ArrayList<String> allActionsList;
    private remoteApi vrep;
    private final int clientID;
    private String output, motivation, nameMotivation, motivationType, lastAction = "am0";
    private  boolean end_all;
    
    private final int numDcValues = 21;      // Dc has 21 values (0.0, 0.05, ..., 1.0)
    private final int numDsValues = 21;      // Ds has 21 values (same as Dc)
    private final int numSalValues = 65536;  // Sal has 2^16 values

    private List<Integer> allStatesList = new ArrayList<>();
        
    //private int past_exp;
    //private Idea ideaMotivation;
    public LearnerCodelet (remoteApi vrep, int clientid, OutsideCommunication outc, int tWindow, String mode, String motivation,  String motivationType,  String output, int num_tables) {
        super();
        this.vrep=vrep;

        time_graph = 0;

        action_number = 0;

        
        clientID = clientid;
        this.num_tables = num_tables;
        this.output = output;
        this.motivationType = motivationType;
        this.motivation = motivation;
        // allActions: am0: focus; am1: neck left; am2: neck right; am3: head up; am4: head down; 
        // am5: fovea 0; am6: fovea 1; am7: fovea 2; am8: fovea 3; am9: fovea 4; 
        // am10: neck tofocus; am11: head tofocus; am12: neck awayfocus; am13: head awayfocus
        // aa0: focus td color; aa1: focus td depth; aa2: focus td region.
        allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", "am13", "aa0", "aa1", "aa2", "am14", "am15", "am16"));
        // States are 0 1 2 ... 5^256-1
     //   ArrayList<String> allStatesList = new ArrayList<>(Arrays.asList(IntStream.rangeClosed(0, (int)Math.pow(2, 16)-1).mapToObj(String::valueOf).toArray(String[]::new)));
        double[] dcValues = DoubleStream.iterate(0.0, n -> n <= 1.0, n -> n + 0.2).toArray(); // 21 values from 0 to 1.0
        double[] dsValues = DoubleStream.iterate(0.0, n -> n <= 1.0, n -> n + 0.2).toArray(); // Same as Dc range
        int salMax = (int)Math.pow(2, 16); // Sal has 65536 values (0 to 65535)

        
       int numStates; 
        
        
        // QLearning initialization
        
        
        
        if(num_tables==2) {
            ql = new QLearningSQL("QTable_"+motivationType+".db");
            ql.setFilename("QTable_"+motivationType+".db");
        }else{
            ql = new QLearningSQL("Qtable.db");
            ql.setFilename("Qtable.db");
        }
        ql.setAlpha((double) 0.9);
        ql.setActionsList(allActionsList);
        oc = outc;               
        experiment_number = oc.vision.getEpoch();
        this.stage = this.oc.vision.getStage();
        MAX_ACTION_NUMBER = oc.vision.getMaxActions();
        MAX_EXPERIMENTS_NUMBER = oc.vision.getMaxEpochs();
        // learning mode ---> build Qtable from scratch
        if (mode.equals("learning") && this.stage == 1) {
        // Initialize QTable to 0
        
        if(num_tables==1){
            // Directly calculate and set Q-values for each state-action pair on-the-fly
            for (double dc : dcValues) {
                int dcIndex = (int) (dc * 5); // Map Dc to [0, 20]
                for (double ds : dsValues) {
                    int dsIndex = (int) (ds * 5); // Map Ds to [0, 20]
                    for (int sal = 0; sal < salMax; sal++) {
                        // Calculate the unique state index
                        int stateIndex = (dcIndex * 6 * 65536) + (dsIndex * 65536) + sal;
                        // Set Q-values for each action in the current state
                        for (String action : allActionsList) {
                            ql.setQ(0, stateIndex, action); // Initialize Q-table entry
                        }
                    }
                }
            }
            ql.storeQ();
        }else{
            // Directly calculate and set Q-values for each state-action pair on-the-fly
            
                for (double ds : dsValues) {
                    int dsIndex = (int) (ds * 5); // Map Ds to [0, 20]
                    for (int sal = 0; sal < salMax; sal++) {
                        // Calculate the unique state index
                        int stateIndex = (dsIndex * 65536) + sal;
                        // Set Q-values for each action in the current state
                        for (String action : allActionsList) {
                            ql.setQ(0, stateIndex, action); // Initialize Q-table entry
                        }
                    }
                }
            
            ql.storeQ();
        
        }
        
        } else if (mode.equals("learning") && this.stage > 1){
            try {
                    ql.recoverQ();
                }
            catch (Exception e) {
                    System.out.println("ERROR LOADING QTABLE");
                    System.exit(1);
                }
        }

        // exploring mode ---> reloads Qtable 
        else {
            try {
                ql.recoverQ();
            }
            catch (Exception e) {
                System.out.println("ERROR LOADING QTABLE");
                System.exit(1);
            }
        }


        timeWindow = tWindow;
        this.mode = mode;
    }

    // This method is used in every Codelet to capture input, broadcast 
    // and output MemoryObjects which shall be used in the proc() method. 
    // This abstract method must be implemented by the user. 
    // Here, the user must get the inputs and outputs it needs to perform proc.
    @Override
    public void accessMemoryObjects() {

        MemoryObject MO;
        MO = (MemoryObject) this.getInput("SALIENCY_MAP");
        saliencyMap = (List) MO.getI();

        if(this.motivation.equals("drives")){
            DriverArray MC = (DriverArray) this.getInput("MOTIVATION");
            motivationMO = (ArrayList<Object>) MC.getI();
        }               

        if(num_tables==2){ 
            if(motivationType.equals("SURVIVAL")){
                MO = (MemoryObject) this.getInput("SUR_REWARDS");
                rewardsList = (List) MO.getI();
            }else{
                MO = (MemoryObject) this.getInput("CUR_REWARDS");
                rewardsList = (List) MO.getI();
            }
        }else if(num_tables==1){
                MO = (MemoryObject) this.getInput("REWARDS");
                rewardsList = (List) MO.getI();
            }
        MO = (MemoryObject) this.getInput("ACTIONS");
        actionsList = (List) MO.getI();

        MO = (MemoryObject) this.getInput("STATES");
        statesList = (List) MO.getI();

        MO = (MemoryObject) this.getOutput(output);
        qTableList = (List) MO.getI();


    }

    // This abstract method must be implemented by the user. 
    // Here, the user must calculate the activation of the codelet
    // before it does what it is supposed to do in proc();

    @Override
    public void calculateActivation() {
            // TODO Auto-generated method stub

    }

    public static Object getLast(List list) {
        if (list.isEmpty()) {
                return list.get(list.size()-1);
        }
        return null;
    }

    // Main Codelet function, to be implemented in each subclass.
    @Override
    public void proc() {

        /*try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }       */
        if(motivationMO == null){
             if(debug) System.out.println("Learner - motivationMO null");

        return;
    }
       /* Idea curI = (Idea) motivationMO.get(0);
        Idea surI = (Idea) motivationMO.get(1);
        if(curI == null || surI == null){
             if(debug) System.out.println("Learner - curI || surI  null");

        return;
    }*/
        boolean curB =  oc.vision.getFValues(3) > oc.vision.getFValues(1);
        
        String motivationName;
        motivationName = "";
        if(curB){
            motivationName = "CURIOSITY";
        }
        else{
            motivationName = "SURVIVAL";
        }
                
              
        if( mode.equals("learning") && this.oc.vision.endEpochR()){
            /*System.out.println(" LEARNER ----- QTables:"+num_tables+" Exp: "+ experiment_number + 
                    " ----- Nact: "+action_number+ " ----- Rew: "+global_reward+
                    " -- exp_c: "+this.exp_c+" -- exp_s: "+this.exp_s+" CurD:"+Collections.max((List) curI.getValue())+" SurD:"+(double) surI.getValue());
           */
            String lastState;
            try{
            lastState = (String) statesList.get(statesList.size() - 1);
               
            float rw;
                if(num_tables==1) rw = oc.vision.getFValues(0)+oc.vision.getFValues(6);
                if(oc.vision.gettype().equals("c")) rw = oc.vision.getFValues(6);
                else rw = oc.vision.getFValues(0);
                ql.update(lastState, oc.vision.getLastAction(), rw);
            ql.storeQ();
            }catch(Exception e){
                    // no state
                    } 
            
            if(oc.vision.getIValues(1)==1){
                 end_all = oc.vision.getIValues(1) > oc.vision.getMaxEpochs();
             }else{
                 
                 end_all = oc.vision.getIValues(2) > oc.vision.getMaxEpochs()&&
                     oc.vision.getIValues(3) > oc.vision.getMaxEpochs();
             }
            
            if (end_all) {
                lastState = (String) statesList.get(statesList.size() - 1);
                float rw;
                if(num_tables==1) rw = oc.vision.getFValues(0)+oc.vision.getFValues(6);
                if(oc.vision.gettype().equals("c")) rw = oc.vision.getFValues(6);
                else rw = oc.vision.getFValues(0);
                ql.update(lastState, oc.vision.getLastAction(), rw);
                ql.storeQ();
                //vrep.simxPauseCommunication(clientID, true);
                //vrep.simxStopSimulation(clientID, vrep.simx_opmode_oneshot_wait);
                //System.exit(0);
            }
            if (!end_all) ql.setB(0.95-(0.95*experiment_number/MAX_EXPERIMENTS_NUMBER));
            experiment_number = (int) oc.vision.getIValues(1);
            exp_s = (int) oc.vision.getIValues(3);
            exp_c = (int) oc.vision.getIValues(2);
            
          /*  try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }*/
        } 
        
        
        
        if(!motivationType.equals(motivationName) && num_tables==2){
                 if(debug) System.out.println("Learner - motivationType diff from motivationMO");
            return;
        }
                
                
        if(qTableList.size() == timeWindow){
                qTableList.remove(0);
            }
        qTableList.add(ql);
        // Use the Random class to generate a random index
        Random random = new Random();

        String state = "-1";

        if (!end_all && !saliencyMap.isEmpty() &&  mode.equals("learning")) {

            if (!statesList.isEmpty() && !rewardsList.isEmpty() && !actionsList.isEmpty() && mode.equals("learning")) {

                // Find reward of the current state, given previous  winner 
                global_reward = (double) rewardsList.get(rewardsList.size() - 1);

                // Gets last action taken
                lastAction = actionsList.get(actionsList.size() - 1);

                // Gets last state that was in
                String lastState = (String) statesList.get(statesList.size() - 1);

                // Updates QLearning table // Adaptation
                ql.update(lastState, lastAction, global_reward);
                
               // action_number += 1;
                
            }
        }
    }


		
}
