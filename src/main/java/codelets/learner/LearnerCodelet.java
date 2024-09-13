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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import outsideCommunication.OutsideCommunication;
import java.util.Random;

/**
 * @author L. L. Rossi (leolellisr)
 * Obs: This class represents the implementations present in the proposed scheme for: 
 * DiscretizationCodelet; adaptation; accommodation and assimilation. 
 * Procedural Memory is represented by QTable.
 */

public class LearnerCodelet extends Codelet 

{

	private int time_graph;
	
	
	private static final int MAX_EXPERIMENTS_NUMBER = 100;
	
	private QLearningL ql;
    

    private List saliencyMap;
    private List statesList;
    private Idea motivationMO;
    private List<String> actionsList;
    private List<QLearningL> qTableList;
    private List<Double>  rewardsList;
    private OutsideCommunication oc;
    private final int timeWindow;
    
    
    private double global_reward;
    private int action_number, num_tables;
    private int experiment_number;
    private int stage;
    private String mode;
    private boolean debug = false;
    private ArrayList<String> allActionsList;
    private String output, motivation, nameMotivation, motivationType, lastAction = "am0";
    //private int past_exp;
    //private Idea ideaMotivation;
	public LearnerCodelet (OutsideCommunication outc, int tWindow, String mode, String motivation,  String motivationType,  String output, int num_tables) {
		
		super();
		time_graph = 0;
		
		action_number = 0;
		
		experiment_number = 1;
                
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
		ArrayList<String> allStatesList = new ArrayList<>(Arrays.asList(IntStream.rangeClosed(0, (int)Math.pow(2, 16)-1).mapToObj(String::valueOf).toArray(String[]::new)));
		
                // QLearning initialization
		ql = new QLearningL();
                ql.setAlpha((double) 0.9);
		ql.setActionsList(allActionsList);
                if(num_tables==2) ql.setFilename("QTable_"+motivationType+".txt");
                oc = outc;               
                this.stage = this.oc.vision.getStage();
                
		// learning mode ---> build Qtable from scratch
		if (mode.equals("learning") && this.stage == 1) {
		// Initialize QTable to 0
			for (int i=0; i < allStatesList.size(); i ++) {
                            for (int j=0; j < allActionsList.size(); j++) {
                                    ql.setQ(0, allStatesList.get(i), allActionsList.get(j));
                            }
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
                    MemoryContainer MC = (MemoryContainer) this.getInput("MOTIVATION");
                    motivationMO = (Idea) MC.getI();
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

                //System.out.println("yawPos: "+yawPos+" headPos: "+headPos);
		try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }       
                if(motivationMO == null){
                     if(debug) System.out.println("Learner - motivationMO null");
            
                return;
            }
                
                if(!motivationType.equals(motivationMO.getName()) && num_tables==2){
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

        if (!saliencyMap.isEmpty() ) {



            if (!statesList.isEmpty() && !rewardsList.isEmpty() && !actionsList.isEmpty() && mode.equals("learning")) {

                // Find reward of the current state, given previous  winner 
                global_reward = (double) rewardsList.get(rewardsList.size() - 1);

                // Gets last action taken
                lastAction = actionsList.get(actionsList.size() - 1);

                // Gets last state that was in
                String lastState = (String) statesList.get(statesList.size() - 1);

                // Updates QLearning table // Adaptation
                ql.update(lastState, lastAction, global_reward);
                
                action_number += 1;
                
                }

                // Select best action to take



        }
        
        if(this.experiment_number != this.oc.vision.getExp()){
            System.out.println(" LEARNER ----- QTables:"+num_tables+" Exp: "+ experiment_number + " ----- Nact: "+action_number+ " ----- Rew: "+global_reward);
        
            //past_exp = experiment_number+5;
            this.experiment_number = this.oc.vision.getExp();
            action_number=0;
            
            //if (experiment_number> past_exp) 
            ql.storeQ();
            if (experiment_number > MAX_EXPERIMENTS_NUMBER) {
                ql.storeQ();
                System.exit(0);
            }
            ql.setB(0.95-(0.95*experiment_number/MAX_EXPERIMENTS_NUMBER));
        }
        
	}
	
	
	
	
            
    private int printToFile(Object object,String filename, int counter, boolean check, int action_num){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
        LocalDateTime now = LocalDateTime.now();
        
        if (!check || experiment_number < MAX_EXPERIMENTS_NUMBER) {
	        try(FileWriter fw = new FileWriter("profile/"+filename,true);
	            BufferedWriter bw = new BufferedWriter(fw);
	            PrintWriter out = new PrintWriter(bw))
	        {
	            out.println(dtf.format(now)+"_"+counter+" "+ object+" Exp number:"+experiment_number+" Action num: "+action_num);
	            
	            out.close();
	            return ++counter;
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
        }
      
		return counter;
    }
		
}
