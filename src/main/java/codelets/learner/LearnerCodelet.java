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
    private int stage,convergenceCounter=0;
    private String mode;
    private boolean debug = false;
    private ArrayList<String> allActionsList;
    private remoteApi vrep;
    private final int clientID;
    private String output, motivation, nameMotivation, motivationType, lastAction = "am0";
    private  boolean end_all;
    
    
    private final int numSalValues = 65536;  // Sal has 2^16 values

    private final double Q_CHANGE_THRESHOLD = 0.001;
    private final int CONVERGENCE_EPOCHS = 100;
    private List<Integer> allStatesList = new ArrayList<>();
    private long seed;
    //private int past_exp;
    //private Idea ideaMotivation;
    public LearnerCodelet (remoteApi vrep, int clientid, OutsideCommunication outc, int tWindow, 
            String mode, String motivation,  String motivationType,  String output, int num_tables, 
            long seed) {
        super();
        this.vrep=vrep;

        time_graph = 0;

        action_number = 0;
        this.seed = seed;
        this.oc = outc;
        clientID = clientid;
        this.num_tables = num_tables;
        this.output = output;
        this.motivationType = motivationType;
        this.motivation = motivation;
        // allActions: am0: focus; am1: neck left; am2: neck right; am3: head up; am4: head down; 
        // am5: fovea 0; am6: fovea 1; am7: fovea 2; am8: fovea 3; am9: fovea 4; 
        // am10: neck tofocus; am11: head tofocus; am12: neck awayfocus; am13: head awayfocus
        // aa0: focus td color; aa1: focus td depth; aa2: focus td region.
        allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", "am13", "aa0", "aa1", "aa2", "am14", "am15", "am16")); //
        // States are 0 1 2 ... 5^256-1
     //   ArrayList<String> allStatesList = new ArrayList<>(Arrays.asList(IntStream.rangeClosed(0, (int)Math.pow(2, 16)-1).mapToObj(String::valueOf).toArray(String[]::new)));
        double[] dcValues = DoubleStream.iterate(0.0, n -> n <= 1.0, n -> n + 0.2).toArray(); // 21 values from 0 to 1.0
        double[] dsValues = DoubleStream.iterate(0.0, n -> n <= 1.0, n -> n + 0.2).toArray(); // Same as Dc range
        int salMax = (int)Math.pow(2, 16); // Sal has 65536 values (0 to 65535)
        int batteryMax =21;
        
       int numStates; 
        experiment_number = oc.vision.getEpoch();
        this.stage = this.oc.vision.getStage();
        
        // QLearning initialization
        if(num_tables==2) {
            ql = new QLearningSQL("QTable_"+motivationType+".db",allActionsList,this.seed);
            ql.setFilename("QTable_"+motivationType+".db");
        }else{
            ql = new QLearningSQL("Qtable.db",allActionsList,this.seed);
            ql.setFilename("Qtable.db");
        }
         ql.setAlpha((double) 0.9);
         //ql.setGamma((double) 0.99);
        ql.setE(0.95);
        
        if (mode.equals("learning") && this.stage == 3 && oc.vision.getIValues(1) == 1) {
            System.out.println("init Learner");
            // Initialize QTable to 0 with bulk insertion
           /* int totalStates;
            if (num_tables == 1) {
                totalStates = 65536*21*21*21; // State - Battery - Dc - Ds
            } else {
                totalStates = 65536*21*21; // State - Battery - D 
            }
            ql.initializeQTableBulk(totalStates, allActionsList );*/
        
        } else if (mode.equals("learning") && oc.vision.getIValues(1) > 1 ){
            
            try {
                    ql.recoverQ();
                }
            catch (Exception e) {
                    System.out.println("ERROR LOADING QTABLE");
                    System.exit(1);
                }
        }

        // exploring mode ---> reloads Qtable 
        else if (mode.equals("exploring")) {
            try {
                ql.recoverQ();
                
                ql.setE(0);
            }
            catch (Exception e) {
                System.out.println("ERROR LOADING QTABLE");
                System.exit(1);
            }
        }

if(debug) System.out.println("init learner");
        timeWindow = tWindow;
        this.mode = mode;
        
        MAX_ACTION_NUMBER = oc.vision.getMaxActions();
        MAX_EXPERIMENTS_NUMBER = oc.vision.getMaxEpochs();
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

    
    @Override
    public void proc() {
        if (mode.equals("learning") && oc.vision.endEpochR()) {
            double totalDeltaQ = 0.0;
            int updatesCount = 0;

            try {
                int lastState = (int) statesList.get(statesList.size() - 1);
                float reward = num_tables == 1 ? oc.vision.getFValues(0) 
                                               : (motivationType.equals("c") ? oc.vision.getFValues(6) 
                                                                                  : oc.vision.getFValues(0));

                // Update Q-values and track Q-value changes
                totalDeltaQ += ql.update(lastState, oc.vision.getLastAction(), reward);
                updatesCount++;

                ql.storeQ();
            } catch (Exception e) {
                System.out.println("No state to update: " + e.getMessage());
            }

            // Calculate average Q-value change per update in this epoch
            double avgDeltaQ = updatesCount > 0 ? totalDeltaQ / updatesCount : 0;

            // Log the average change for monitoring
            if(debug) System.out.println("Average Q-value change for epoch " + experiment_number + ": " + avgDeltaQ);

            // Early stopping condition
            if (avgDeltaQ < Q_CHANGE_THRESHOLD && experiment_number > 100) {
                convergenceCounter++;
            } else {
                convergenceCounter = 0; // Reset if significant Q-value changes occur
            }

            // Stop if Q-values have converged for several consecutive epochs
            if (convergenceCounter >= CONVERGENCE_EPOCHS) {
                end_all = true; // Set end flag to true to stop training
                System.out.println("Convergence reached. Training stopped.");
            }

            if (end_all) {
                ql.storeQ(); // Final save of Q-table
                System.exit(0);
            }
        }
        
        if(qTableList.size() == timeWindow){
                qTableList.remove(0);
            }
        qTableList.add(ql);
        
    }




		
}
