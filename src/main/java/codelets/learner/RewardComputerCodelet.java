package codelets.learner;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.Dictionary;

import attention.Winner;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.learning.QLearning;
import br.unicamp.cst.representation.idea.Idea;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import outsideCommunication.OutsideCommunication;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * @author L. L. Rossi (leolellisr)
 * Obs: This class represents the implementations present in the proposed scheme for: 
 * DiscretizationCodelet; adaptation; accommodation and assimilation. 
 * Procedural Memory is represented by QTable.
 */

public class RewardComputerCodelet extends Codelet 

{

	private int time_graph;
	
	private static final int MAX_ACTION_NUMBER = 500;
	
	private static final int MAX_EXPERIMENTS_NUMBER = 100;
	
	private QLearning ql;
    

    private List winnersList, battReadings;
    private List saliencyMap, curiosityMot, curiosityAct;
    private Idea motivationMO;
    private MemoryObject rewardMO, reward_stringMO, action_stringMO;
    private List<String> actionsList;
    
    private OutsideCommunication oc;
    private final int timeWindow;
    private final int sensorDimension;
    
    private float vel = 2f,angle_step;
    
    private int global_reward;
    private int curiosity_lv, red_c, green_c, blue_c;
    private int action_number, action_index;
    private int experiment_number;
    private int stage;
    int fovea; 
    private String mode;
    private Random gerador = new Random();
    private Integer winnerIndex;
    private Integer winnerFovea = -1, winnerGreen = -1, winnerBlue = -1, winnerRed = -1, winnerDist = -1;
    private int[] posLeft = {0, 4, 8, 12};
    private int[] posRight = {3, 7, 11, 15};
    private int[] posUp = {12, 13, 14, 15};
    private int[] posDown = {0, 1, 2, 3};
    private int[] posCenter = {5, 6, 9, 10};
    
    private int[] fovea0 = {0, 1, 4, 5};
    private int[] fovea1 = {2, 3, 6, 7};
    private int[] fovea2 = {8, 9, 12, 13};
    private int[] fovea3 = {10, 11, 14, 15};
    
    private float yawPos = 0f, headPos = 0f;   
    private boolean crashed = false;
    private boolean debug = false;
    private int aux_crash = 0, battery_lvint;
    private ArrayList<String> allActionsList;
    private ArrayList<Integer> curiosity_motivationIntensity;
    private ArrayList<Float> lastLine, lastRed, lastGreen, lastBlue, lastDist;
    private String motivationType, motivation, nameMotivation, stringOutput = "";
    private float  mot_value=0, hug_drive=0, cur_drive=0, r_imp=0, g_imp=0, b_imp=0;
    //private Idea ideaMotivation;
	public RewardComputerCodelet (OutsideCommunication outc, int tWindow, int sensDim, String mode, String motivation, String motivationType) {
		
		super();
		time_graph = 0;
		
		global_reward = 0;
		
		action_number = 0;
		
		experiment_number = 1;
                
                curiosity_lv = 0;
                red_c = 0;
                green_c = 0;
                blue_c =0;
                this.motivationType = motivationType;
                this.motivation = motivation;
                // allActions: am0: focus; am1: neck left; am2: neck right; am3: head up; am4: head down; 
                // am5: fovea 0; am6: fovea 1; am7: fovea 2; am8: fovea 3; am9: fovea 4; 
                // am10: neck tofocus; am11: head tofocus; am12: neck awayfocus; am13: head awayfocus
                // aa0: focus td color; aa1: focus td depth; aa2: focus td region.
		allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", "am13", "aa0", "aa1", "aa2", "am14", "am15", "am16"));
                if(this.motivation.equals("drives")) curiosity_motivationIntensity  = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
		
               
                yawPos = oc.NeckYaw_m.getSpeed();
                headPos = oc.HeadPitch_m.getSpeed();                
                this.stage = this.oc.vision.getStage();
                
				timeWindow = tWindow;
        sensorDimension = sensDim;
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
                MO = (MemoryObject) this.getInput("WINNERS");
                winnersList = (List) MO.getI();
                
              
                
                MO = (MemoryObject) this.getInput("BATTERY_BUFFER");
                battReadings = (List) MO.getI();
                
                if(this.motivation.equals("drives")){
                    MemoryContainer MC = (MemoryContainer) this.getInput("MOTIVATION");
                    motivationMO = (Idea) MC.getI();
                }
                rewardMO = (MemoryObject) this.getOutput("REWARDS");
                reward_stringMO = (MemoryObject) this.getOutput("REWARDS_STRING_OUTPUT");
                //action_stringMO = (MemoryObject) this.getInput("ACTION_STRING_OUTPUT");
                
                        
                MO = (MemoryObject) this.getInput("ACTIONS");
                actionsList = (List) MO.getI();



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
            crashed = false;
            yawPos = oc.NeckYaw_m.getSpeed();
            headPos = oc.HeadPitch_m.getSpeed(); 
                //System.out.println("yawPos: "+yawPos+" headPos: "+headPos);
            try {
            Thread.sleep(50);
            } catch (Exception e) {
            Thread.currentThread().interrupt();
            }       
            
            if(!motivationType.equals(motivationMO.getName())){
                return;
            }

                // Use the Random class to generate a random index
            Random random = new Random();
                        
            MemoryObject battery_lv = (MemoryObject) battReadings.get(battReadings.size()-1);
            if(debug) System.out.println("battery_lv: "+battery_lv);
                battery_lvint = (int)battery_lv.getI();
		System.out.println("Reward computer Exp: "+ experiment_number + " num action: "+action_number+ " Reward: "+global_reward+" Battery: "+battery_lvint+" Curiosity_lv: "+curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c);
		if (!saliencyMap.isEmpty() && !winnersList.isEmpty() && !actionsList.isEmpty()) {
			
                    Winner lastWinner = (Winner) winnersList.get(winnersList.size() - 1);
                    winnerIndex = lastWinner.featureJ;
                        
				// Find reward of the current state, given previous  winner 
                                
                    if(yawPos>1.4f || yawPos<-1.4f || headPos>0.6f || headPos<-0.4f ){
                            crashed = true;
                    }
                    if(crashed || battery_lvint == 0){
                            global_reward -= 10;
                    }


                    Double reward = 1d;
                    global_reward += reward;
                                
				// Gets last action taken
                    String lastAction = actionsList.get(actionsList.size() - 1);
                                
		
                            // Motivation

                    int i = 0;
                    double max_action = 0.0;
                    ArrayList<Integer> max_list = new ArrayList<Integer>();
                    if(this.motivation.equals("drives")){                        
                        nameMotivation = motivationMO.getName();
                        if(nameMotivation.equals("CURIOSITY") && motivationType.equals("CURIOSITY")){
                            ArrayList<Double> valueMotivation = (ArrayList<Double>) motivationMO.getValue();

                            for(double action : valueMotivation){
                                if(action > max_action){
                                    max_action = action;
                                } 
                            }
                                
                            for(double action : valueMotivation){
                                if(action == max_action){
                                    max_list.add(i);
                                }
                                i += 1;
                            }
                                // Retrieve the random element from the ArrayList
                            action_index = max_list.get(random.nextInt(max_list.size()));
                            cur_drive = (float) max_action;
                            global_reward += 100-100*cur_drive;
                        } else if(motivationType.equals("SURVIVAL")) {
                                double valueMotivation = (double) motivationMO.getValue();
                                hug_drive = (float) valueMotivation;
                                global_reward += 100-100*hug_drive;
                        }                                
                            
                            
                        mot_value = cur_drive + hug_drive;
                           

                    }
                       
                     int winner =   getStateFromSalMap();
                        
			
                    System.out.println("Exp: "+ experiment_number + " Action: "+lastAction + " num action: "+action_number+ " Winner: "+winnerIndex+ " WinnerFovea: "+winnerFovea);
                    if (lastAction.equals("am1")) {
                        yawPos = yawPos-angle_step;
                                 //neckMotorMO.setI(yawPos);
                        if(winnerFovea !=-1 && IntStream.of(posLeft).anyMatch(x -> x == winnerFovea) && stage > 1){
                            global_reward += 1;
                        }
                    }

                    else if (lastAction.equals("am2")) {
                        yawPos = yawPos+angle_step;
                                 //neckMotorMO.setI(yawPos);
                        if(winnerFovea !=-1 && IntStream.of(posRight).anyMatch(x -> x == winnerFovea)){
                            global_reward += 1;
                            }
                    }
                    else if (lastAction.equals("am3")) {
                            headPos = headPos-angle_step;
                             // headMotorMO.setI(headPos);
                            if(winnerFovea !=-1 && IntStream.of(posUp).anyMatch(x -> x == winnerFovea)) {
                                global_reward += 1;
                            } 
                    }
                    else if (lastAction.equals("am4")) {
                            headPos = headPos+angle_step;
                            //headMotorMO.setI(headPos);
                            if(winnerFovea !=-1 && IntStream.of(posDown).anyMatch(x -> x == winnerFovea)){
                                global_reward += 1;
                            } 
                    }
                    else if (lastAction.equals("am5")) {
                        fovea = 0;
                        if(winnerFovea !=-1 && IntStream.of(fovea0).anyMatch(x -> x == winnerFovea)){
                                global_reward += 1;
                            } 
                    }
                    else if (lastAction.equals("am6")) {
                        fovea = 1;
                        if(winnerFovea !=-1 && IntStream.of(fovea1).anyMatch(x -> x == winnerFovea)){
                                global_reward += 1;
                            } 
                    }
                    else if (lastAction.equals("am7")) {
                        fovea = 2;
                        if(winnerFovea !=-1 && IntStream.of(fovea2).anyMatch(x -> x == winnerFovea)){
                                global_reward += 1;
                            } 
                    }
                    else if (lastAction.equals("am8")) {
                        fovea = 3;
                        if(winnerFovea !=-1 && IntStream.of(fovea3).anyMatch(x -> x == winnerFovea)){
                                global_reward += 1;
                            } 
                    }
                    else if (lastAction.equals("am9")) {
                        fovea = 4;
                        if(winnerFovea !=-1 && IntStream.of(posCenter).anyMatch(x -> x == winnerFovea)){
                                global_reward += 1;
                            } 
                    }

                    // just Stage 3
                     else if (lastAction.equals("am10") && this.stage == 3) {
                        if(fovea == 0 || fovea == 2){
                            yawPos = yawPos-angle_step;
                           //  neckMotorMO.setI(yawPos);
                        }
                        else if(fovea == 1 || fovea == 3){
                            yawPos = yawPos+angle_step;
                           //  neckMotorMO.setI(yawPos);
                        }
                     }
                     else if (lastAction.equals("am11") && this.stage == 3) {
                        if(fovea == 0 || fovea == 2){
                            yawPos = yawPos+angle_step;
                           //  neckMotorMO.setI(yawPos);
                        }
                        else if(fovea == 1 || fovea == 3){
                            yawPos = yawPos-angle_step;
                           //  neckMotorMO.setI(yawPos);
                        }
                     }
                     else if (lastAction.equals("am12") && this.stage == 3) {
                        if(fovea == 3 || fovea == 2){
                            headPos = headPos-angle_step;
                           //  headMotorMO.setI(headPos);
                        }
                        else if(fovea == 1 || fovea == 0){
                            headPos = headPos+angle_step;
                           //  headMotorMO.setI(headPos);
                        }
                     }
                     else if (lastAction.equals("am13") && this.stage == 3) {
                        if(fovea == 3 || fovea == 2){
                            headPos = headPos+angle_step;
                           //  headMotorMO.setI(headPos);
                        }
                        else if(fovea == 1 || fovea == 0){
                            headPos = headPos-angle_step;
                           // headMotorMO.setI(headPos);
                        }
                     }

                     else if (lastAction.equals("am14") && this.stage == 3) {
                        if(calculateMean(lastRed)>0.01 && calculateMean(lastGreen)<0.015 && calculateMean(lastBlue)<0.015){

                            global_reward += 1;
                            red_c += 1;
                            curiosity_lv += 2;

                        }
                        else {
                            global_reward -= 1;
                        }
                     }

                     else if (lastAction.equals("am15") && this.stage == 3) {
                        if(calculateMean(lastGreen)>0.01 && calculateMean(lastRed)<0.015 && calculateMean(lastBlue)<0.015){

                            curiosity_lv += 1;
                            green_c += 1;

                        }
                        else {
                            global_reward -= 1;
                        }
                     }

                     else if (lastAction.equals("am16") && this.stage == 3) {
                        if(calculateMean(lastBlue)>0.01 && calculateMean(lastGreen)<0.015 && calculateMean(lastRed)<0.015){


                            global_reward += 1;
                            blue_c += 1;

                        }
                        else {
                            global_reward -= 1;
                        }
                     }

                     // attentional actions

                    
		}
                List rewardsList = (List) rewardMO.getI();        
        
                if(rewardsList.size() == timeWindow){
                    rewardsList.remove(0);
                } 
                
                rewardsList.add(global_reward);
//		time_graph = printToFile(state, "states.txt", time_graph, true, action_number);
//                printToFile(actionsList.get(actionsList.size() - 1), "actions.txt", time_graph, true, action_number);
                //stringOutput.clear();
                //stringOutput.add("actions.txt");
                if(mot_value==0) mot_value = 1;
                 if(this.motivation.equals("drives")){  
                    stringOutput = time_graph+" Exp number:"+experiment_number+" Action num: "+action_number+ " Battery: "+battery_lvint+" Curiosity_lv: "+curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c+"action: "+actionsList.get(actionsList.size() - 1)+" mot_value: "+mot_value+" hug_drive: "+(float) (hug_drive/mot_value*100)+" cur_drive: "+(float) (cur_drive/mot_value*100);
                } else if(this.motivation.equals("impulses")){
                    stringOutput = time_graph+" Exp number:"+experiment_number+" Action num: "+action_number+ " Battery: "+battery_lvint+" Curiosity_lv: "+curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c+"action: "+actionsList.get(actionsList.size() - 1)+" mot_value: "+mot_value+" r_imp: "+(float) (r_imp/mot_value*100)+" g_imp: "+(float) (g_imp/mot_value*100)+" b_imp: "+(float) (b_imp/mot_value*100);
                }
                
                 if(stringOutput==null) System.out.println("stringOutput==null");
                 if(action_stringMO==null) System.out.println("action_stringMO==null");
                 
                 if(stringOutput!=null && action_stringMO != null)                action_stringMO.setI(stringOutput);
		
		
	
                
        }
        
	
	
	
	public void check_stop_experiment(String mode) {

		if (action_number >= MAX_ACTION_NUMBER  || crashed || battery_lvint==0 ){
		
            aux_crash = 0;
            yawPos = 0f;
            headPos = 0f;
            experiment_number++;
                    //experiment_number = printToFile(global_reward, "rewards.txt", experiment_number, false, action_number);
//                        stringOutput.clear();
//                       stringOutput.add("rewards.txt");
           if(mot_value==0) mot_value = 1;
           if(this.motivation.equals("drives")){  
                stringOutput = time_graph+" Exp number:"+experiment_number+" Action num: "+action_number+ " Battery: "+battery_lvint+" Curiosity_lv: "+curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c+" reward: "+global_reward+" mot_value: "+mot_value+" hug_drive: "+(float) (hug_drive/mot_value*100)+" cur_drive: "+(float) (cur_drive/mot_value*100);
            } else if(this.motivation.equals("impulses")){
                stringOutput = time_graph+" Exp number:"+experiment_number+" Action num: "+action_number+ " Battery: "+battery_lvint+" Curiosity_lv: "+curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c+" reward: "+global_reward+" mot_value: "+mot_value+" r_imp: "+(float) (r_imp/mot_value*100)+" g_imp: "+(float) (g_imp/mot_value*100)+" b_imp: "+(float) (b_imp/mot_value*100);
            }
            if(stringOutput!=null && reward_stringMO != null)   reward_stringMO.setI(stringOutput);
            action_number = 0;
            global_reward = 0;
            curiosity_lv = 0;
            red_c = 0;
            green_c = 0;
            blue_c = 0;
			
                       
                      
                }	
		
                
	}
	
        // Discretization
	// Normalize and transform a salience map into one state
		// Normalized values between 0 and 1 can be mapped into 0, 1, 2, 3 or 4
		// Them this values are computed into one respective state
public Integer getStateFromSalMap() {


    // Getting just the last entry (current sal map)
    lastLine = (ArrayList<Float>) saliencyMap.get(saliencyMap.size() -1);

    //if (calculateMean(lastRed)<0.01 && calculateMean(lastGreen)<0.01 && calculateMean(lastBlue)<0.01) aux_crash += 1;
    if (Collections.max(lastLine) == 0) aux_crash += 1;
    else aux_crash = 0; 

    if(action_number > 5 && aux_crash> 5 ){
        crashed = true;
    }


    if (Collections.max(lastLine) > 0){
      for(int n = 0;n<4;n++){
        int ni = (int) (n*4);
        int no = (int) (4+n*4);
        for(int m = 0;m<4;m++){    
            int mi = (int) (m*4);
            int mo = (int) (4+m*4);
            for (int y = ni; y < no; y++) {

                for (int x = mi; x < mo; x++) {
                    int i = (y*16+x);
                    if(i == winnerIndex) winnerFovea = n*4+m;


                }
            }

                            
        }
      }

    }
			
			return winnerFovea;
}
		
	
        public static float calculateMean(ArrayList<Float> list) {
            if (list.isEmpty()) {
                return 0; // Return 0 if the list is empty or handle it as required
            }

            float sum = 0;
            for (float value : list) {
                sum += value;
            }

            return sum / list.size();
        }
            
	private int printToFile(Object object,String filename, int counter, boolean check, int action_num){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
        LocalDateTime now = LocalDateTime.now();
        
        if (!check || experiment_number < MAX_EXPERIMENTS_NUMBER) {
            MemoryObject battery_lv = (MemoryObject) battReadings.get(battReadings.size()-1);
            int battery_lvint = (int)battery_lv.getI();
	        try(FileWriter fw = new FileWriter("profile/"+filename,true);
	            BufferedWriter bw = new BufferedWriter(fw);
	            PrintWriter out = new PrintWriter(bw))
	        {
	            out.println(dtf.format(now)+"_"+counter+" "+ object+" Exp number:"+experiment_number+" Action num: "+action_num+ " Battery: "+battery_lvint+" Curiosity_lv: "+curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c);
	            
	            out.close();
	            return ++counter;
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
        }
      
		return counter;

	}
		

}
