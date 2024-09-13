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
import br.unicamp.cst.representation.idea.Idea;
import static codelets.learner.OldLearnerCodelet.calculateMean;
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

public class ActionExecCodelet extends Codelet 

{

	private int time_graph;
	private static final float CRASH_TRESHOLD = 0.28f;
	
	private static final int MAX_ACTION_NUMBER = 500;
	
	private static final int MAX_EXPERIMENTS_NUMBER = 100;
	
	private QLearningL ql;
    


    private Idea motivationMO;
    private MemoryObject motorActionMO;
    private MemoryObject neckMotorMO;
    private MemoryObject headMotorMO;
    private MemoryObject desFC, desFD, desFR;
    private List<String> actionsList;
  
    private OutsideCommunication oc;
    private final int timeWindow;
    private final int sensorDimension;
    
    private float vel = 2f,angle_step=0.1f;
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
   
    private int action_number, action_index;
    private int experiment_number;
    private int stage, fovea;
    
    private String mode;
    private Random gerador = new Random();

    
    private float yawPos = 0f, headPos = 0f;   
    private boolean crashed = false;
    private boolean debug = false, sdebug = false;
    private int aux_crash = 0;
    private ArrayList<String> executedActions  = new ArrayList<>();
    private ArrayList<String> allActionsList;
    private ArrayList<Float> lastLine, lastRed, lastGreen, lastBlue, lastDist;
    private List winnersList, colorReadings, redReadings, greenReadings, blueReadings, distReadings, battReadings;
    private List saliencyMap;
    private int curiosity_lv, red_c, green_c, blue_c;

	public ActionExecCodelet (OutsideCommunication outc, String mode, int tWindow, int sensDimn) {
		
		super();
		time_graph = 0;
		sensorDimension = sensDimn;
		
		experiment_number = 1;
                
                // allActions: am0: focus; am1: neck left; am2: neck right; am3: head up; am4: head down; 
                // am5: fovea 0; am6: fovea 1; am7: fovea 2; am8: fovea 3; am9: fovea 4; 
                // am10: neck tofocus; am11: head tofocus; am12: neck awayfocus; am13: head awayfocus
                // aa0: focus td color; aa1: focus td depth; aa2: focus td region.
		allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", "am13", "aa0", "aa1", "aa2", "am14", "am15", "am16"));
		// States are 0 1 2 ... 5^256-1
		
                oc = outc;
                yawPos = oc.NeckYaw_m.getSpeed();
                headPos = oc.HeadPitch_m.getSpeed();                
                this.stage = this.oc.vision.getStage();
                
		this.mode = mode;
		
		timeWindow = tWindow;
                curiosity_lv = 0;
                red_c = 0;
                green_c = 0;
                blue_c =0;
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
                MemoryContainer MC = (MemoryContainer) this.getInput("MOTIVATION");
                    motivationMO = (Idea) MC.getI();
                MO = (MemoryObject) this.getInput("BATTERY_BUFFER");
                battReadings = (List) MO.getI();
                
                MO = (MemoryObject) this.getInput("VISION_COLOR_FM");
                colorReadings = (List) MO.getI();
/*                MO = (MemoryObject) this.getInput("VISION_GREEN_FM");
                greenReadings = (List) MO.getI();
                MO = (MemoryObject) this.getInput("VISION_BLUE_FM");
                blueReadings = (List) MO.getI();*/
                MO = (MemoryObject) this.getInput("DEPTH_FM");
                distReadings = (List) MO.getI();
                
                MO = (MemoryObject) this.getInput("ACTIONS");
                actionsList = (List) MO.getI();
                
                motorActionMO = (MemoryObject) this.getOutput("MOTOR");
                neckMotorMO = (MemoryObject) this.getOutput("NECK_YAW");
                headMotorMO = (MemoryObject) this.getOutput("HEAD_PITCH");
                
                desFC = (MemoryObject) this.getOutput("DESFEAT_C");
                desFD = (MemoryObject) this.getOutput("DESFEAT_D");
                desFR = (MemoryObject) this.getOutput("DESFEAT_R");
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
        
        if(actionsList.size()<1 || winnersList.size()<1 || battReadings.size()<1){
            if(debug){
                System.out.println("ACT_EXEC----- actionsList.size():"+actionsList.size());
            
                System.out.println("ACT_EXEC----- winnersList.size():"+winnersList.size());
                System.out.println("ACT_EXEC----- battReadings.size():"+battReadings.size());
            }
            return;
        }
        String actionToTake = actionsList.get(actionsList.size() - 1);
        if(sdebug) System.out.println("ACT_EXEC -----  Exp: "+ experiment_number +" ----- Act: "+ actionToTake+" ----- N_act: "+action_number+" Curiosity_lv: "+curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c);
        
        Winner lastWinner = (Winner) winnersList.get(winnersList.size() - 1);
        winnerIndex = lastWinner.featureJ;
        String state = getStateFromSalMap();
        action_number += 1;
        if(!executedActions.contains(actionToTake)) executedActions.add(actionToTake);
            
            // AM1 - Move neck to left
            if (actionToTake.equals("am1")) {
                    yawPos = yawPos-angle_step;
                    neckMotorMO.setI(yawPos);

            }
            
            // AM2 - Move neck to right
            else if (actionToTake.equals("am2")) {
                    yawPos = yawPos+angle_step;
                    neckMotorMO.setI(yawPos);

            }
            
            // AM3 - Move head up
            else if (actionToTake.equals("am3")) {
                    headPos = headPos-angle_step;
                    headMotorMO.setI(headPos);

            }
            
            // AM4 - Move head down
            else if (actionToTake.equals("am4")) {
                    headPos = headPos+angle_step;
                    headMotorMO.setI(headPos);

            }
            
            // AM5 - Fovea 0
            else if (actionToTake.equals("am5")) {
                fovea = 0;

            }
            
            // AM6 - Fovea 1
            else if (actionToTake.equals("am6")) {
                fovea = 1;

            }
            
            // AM7 - Fovea 2
            else if (actionToTake.equals("am7")) {
                fovea = 2;

            }
            
            // AM8 - Fovea 3
            else if (actionToTake.equals("am8")) {
                fovea = 3;

            }
            
            // AM9 - Fovea 4
            else if (actionToTake.equals("am9")) {
                fovea = 4;

            }
            
            // AM10 - Neck to focus
            // just Stage 3
             else if (actionToTake.equals("am10") && this.stage == 3) {
                if(fovea == 0 || fovea == 2){
                    yawPos = yawPos-angle_step;
                    neckMotorMO.setI(yawPos);
                }
                else if(fovea == 1 || fovea == 3){
                    yawPos = yawPos+angle_step;
                    neckMotorMO.setI(yawPos);
                }
             }
             
             // AM11 - Head to focus
             else if (actionToTake.equals("am11") && this.stage == 3) {
                if(fovea == 0 || fovea == 2){
                    yawPos = yawPos+angle_step;
                    neckMotorMO.setI(yawPos);
                }
                else if(fovea == 1 || fovea == 3){
                    yawPos = yawPos-angle_step;
                    neckMotorMO.setI(yawPos);
                }
             }
             
             // AM12 - Neck away focus
             else if (actionToTake.equals("am12") && this.stage == 3) {
                if(fovea == 3 || fovea == 2){
                    headPos = headPos-angle_step;
                    headMotorMO.setI(headPos);
                }
                else if(fovea == 1 || fovea == 0){
                    headPos = headPos+angle_step;
                    headMotorMO.setI(headPos);
                }
             }
             
             // AM13 - Head away focus
             else if (actionToTake.equals("am13") && this.stage == 3) {
                if(fovea == 3 || fovea == 2){
                    headPos = headPos+angle_step;
                    headMotorMO.setI(headPos);
                }
                else if(fovea == 1 || fovea == 0){
                    headPos = headPos-angle_step;
                    headMotorMO.setI(headPos);
                }
             }
             
             // AM14 - Get red 
             
             else if (actionToTake.equals("am14") && this.stage == 3) {
                 if(calculateMean(lastRed)>0.005){
                     red_c += 1;
                     curiosity_lv += 2;
                    try {
                        oc.set_object_back(0);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ActionExecCodelet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if(debug) System.out.println("GOT RED");
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        Thread.currentThread().interrupt();
                    }
                    oc.reset_positions();
                 }
                }


             // Get green
             else if (actionToTake.equals("am15") && this.stage == 3) {
                 if(calculateMean(lastGreen)>0.005){  
                     green_c += 1;
                     curiosity_lv += 1;
                 try {
                        oc.set_object_back(1);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ActionExecCodelet.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    oc.battery.setCharge(true);
                    if(debug) System.out.println("GOT GREEN");
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        Thread.currentThread().interrupt();
                    }
                    oc.battery.setCharge(false);
                    oc.reset_positions();
                 }

             }
             
             // get blue
             else if (actionToTake.equals("am16") && this.stage == 3) {
                    if(calculateMean(lastBlue)>0.005){ 
                    blue_c += 1;    
                    try {
                        oc.set_object_back(2);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ActionExecCodelet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    oc.battery.setCharge(true);
                    if(debug) System.out.println("GOT BLUE");

                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        Thread.currentThread().interrupt();
                    }
                    oc.battery.setCharge(false);
                    oc.reset_positions();
                    }
             }

             // attentional actions

            // AA0 - Define desired color
            else if (actionToTake.equals("aa0") && this.stage == 3) {
                List desired_feat_color = (List) desFC.getI();        
                if(desired_feat_color.size() == timeWindow){
                    desired_feat_color.remove(0);
                }
                desired_feat_color.add(new ArrayList<>());
                ArrayList<Float> desired_feat_color_t = (ArrayList<Float>) desired_feat_color.get(desired_feat_color.size()-1);
                if(calculateMean(lastRed)>calculateMean(lastBlue) && calculateMean(lastRed)>calculateMean(lastGreen)){
                    desired_feat_color_t.add((float) 255.0);
                    desired_feat_color_t.add((float) 0.0);
                    desired_feat_color_t.add((float) 0.0);
                }
                if(calculateMean(lastGreen)>calculateMean(lastBlue) && calculateMean(lastGreen)>calculateMean(lastRed)){
                    desired_feat_color_t.add((float) 0.0);
                    desired_feat_color_t.add((float) 255.0);
                    desired_feat_color_t.add((float) 0.0);
                }    
                if(calculateMean(lastBlue)>calculateMean(lastGreen) && calculateMean(lastBlue)>calculateMean(lastRed)){
                    desired_feat_color_t.add((float) 0.0);
                    desired_feat_color_t.add((float) 0.0);
                    desired_feat_color_t.add((float) 255.0);
                }
            } 

            // AA1 - Define desired distance
            else if (actionToTake.equals("aa1") && this.stage == 3) {
                   List desired_feat_dist = (List) desFD.getI();        
                if(desired_feat_dist.size() == timeWindow){
                    desired_feat_dist.remove(0);
                }
                desired_feat_dist.add((float) 0.0);
              }
            
            // AA2 - Define desired region
            else if (actionToTake.equals("aa2") && this.stage == 3) {
                  List desired_feat_reg = (List) desFR.getI();        
                if(desired_feat_reg.size() == timeWindow){
                    desired_feat_reg.remove(0);
                }
                desired_feat_reg.add(new ArrayList<>());
                ArrayList<Integer> desired_feat_reg_t = (ArrayList<Integer>) desired_feat_reg.get(desired_feat_reg.size()-1);
                desired_feat_reg_t.add(8);
                desired_feat_reg_t.add(8);
                
            }
            

            check_stop_experiment();
            printToFile("object_count.txt");
    } 

        // Discretization
	// Normalize and transform a salience map into one state
		// Normalized values between 0 and 1 can be mapped into 0, 1, 2, 3 or 4
		// Them this values are computed into one respective state
		public String getStateFromSalMap() {
			
			
                        ArrayList<Float> mean_lastLine = new ArrayList<>();
                        for(int i=0; i<16;i++){
                            mean_lastLine.add(0f);
                        }
                        redReadings = (List) colorReadings.get(0);
                        greenReadings = (List) colorReadings.get(1);
                        blueReadings = (List) colorReadings.get(2);
                        
			// Getting just the last entry (current sal map)
			lastLine = (ArrayList<Float>) saliencyMap.get(saliencyMap.size() -1);
                        lastRed = (ArrayList<Float>) redReadings.get(redReadings.size() -1);
                        lastGreen = (ArrayList<Float>) greenReadings.get(greenReadings.size() -1);
                        lastBlue = (ArrayList<Float>) blueReadings.get(blueReadings.size() -1);

                        lastDist = (ArrayList<Float>) distReadings.get(distReadings.size() -1);

                        //if (calculateMean(lastRed)<0.01 && calculateMean(lastGreen)<0.01 && calculateMean(lastBlue)<0.01) aux_crash += 1;
                        if (Collections.max(lastLine) == 0) aux_crash += 1;
                        else aux_crash = 0; 
                        
                        if(action_number > 5 && aux_crash> 5 ){
                            crashed = true;
                        }
                        
                        int indexRed = -1;
                        int indexGreen = -1;
                        int indexBlue = -1;                        
                        int indexDist = -1;

                        if (this.stage == 3) {
                            
                            if (lastRed.indexOf(Collections.max(lastRed))>-1) indexRed = lastRed.indexOf(Collections.max(lastRed));
                            if (lastGreen.indexOf(Collections.max(lastGreen))>-1) indexGreen = lastGreen.indexOf(Collections.max(lastGreen));
                            if (lastBlue.indexOf(Collections.max(lastBlue))>-1) indexBlue = lastBlue.indexOf(Collections.max(lastBlue));
                                                        
                            if (lastDist.indexOf(Collections.max(lastDist))>-1) indexDist = lastDist.indexOf(Collections.max(lastDist));   
                        }
                        
                        if(debug){
                            System.out.println("~~~~~~ BEGIN ACTION EXEC ~~~~~");
                            System.out.println("lastRed: "+calculateMean(lastRed));
                            System.out.println("indexRed: "+indexRed);
                            System.out.println("lastGreen: "+calculateMean(lastGreen));
                            System.out.println("indexGreen: "+indexGreen);

                            System.out.println("lastBlue: "+calculateMean(lastBlue));
                            System.out.println("indexBlue: "+indexBlue);
                            System.out.println("~~~~~~ END ACTION EXEC ~~~~~");
                        }
                    if (Collections.max(lastLine) > 0){
                        ArrayList<Float> MeanValue = new ArrayList<>();
                        for(int n = 0;n<4;n++){
                        int ni = (int) (n*4);
                        int no = (int) (4+n*4);
                        for(int m = 0;m<4;m++){    
                            int mi = (int) (m*4);
                            int mo = (int) (4+m*4);
                            for (int y = ni; y < no; y++) {

                                for (int x = mi; x < mo; x++) {
                                    int i = (y*16+x);
                                    if(i == indexRed && indexRed != -1) winnerRed = n*4+m;
                                    if(i == indexGreen && indexGreen != -1) winnerGreen = n*4+m;
                                    if(i == indexBlue && indexBlue != -1) winnerBlue = n*4+m;
                                    if(i == indexDist && indexDist != -1) winnerDist = n*4+m;
                                    if(i == winnerIndex) winnerFovea = n*4+m;

                                    float Fvalue_r = (float) lastLine.get(i);                         
                                    MeanValue.add(Fvalue_r);

                                }
                            }
                            float correct_mean_r = Collections.max(MeanValue);
                            
                            mean_lastLine.set(n*4+m, correct_mean_r);
                            MeanValue.clear();
                            
                            }
                        }
                        if(debug){
                            System.out.println("winnerRed: "+winnerRed);
                            System.out.println("winnerGreen: "+winnerGreen);
                            System.out.println("winnerBlue: "+winnerBlue);
                        }
                    }
			// For normalizing readings between 0 and 1 before transforming to state 
			Float max = Collections.max(mean_lastLine);
			Float min = Collections.min(mean_lastLine);		
			// System.out.println("mean_lastLine len: "+mean_lastLine.size()+" max: "+max+ " min: "+min);
			Integer discreteVal = 0;
			Integer stateVal = 0;
			for (int i=0; i < 16; i++) {
				// Normalizing value
				Float normVal; 
                                if(max>0) normVal = (mean_lastLine.get(i)-min)/(max-min);
                                else normVal = 0f;
				// Getting discrete value
				if (normVal <= 0.5) {
					discreteVal = 0;
				}
				else if (normVal > 0.5) {
					discreteVal = 1;
				}
				
				// Getting state from discrete value
				stateVal += (int) Math.pow(2, i)*discreteVal;
			}
			return stateVal.toString();
		}
        
	
	
	
	public void check_stop_experiment() {

            if(yawPos>1.4f || yawPos<-1.4f || headPos>0.6f || headPos<-0.4f ){
                crashed = true;
            }

            MemoryObject battery_lv = (MemoryObject) battReadings.get(battReadings.size()-1);
            int battery_lvint = (int)battery_lv.getI();

            if (mode.equals("learning") && ((action_number >= MAX_ACTION_NUMBER ) || crashed || battery_lvint==0) ){

                oc.shuffle_positions();
                oc.reset_positions();

                curiosity_lv = 0;
                red_c = 0;
                green_c = 0;
                blue_c = 0;
                System.out.println("Max number of actions or crashed. Exp: "+ experiment_number +" ----- N_act: "+action_number+" Curiosity_lv: "+curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c);


                aux_crash = 0;
                headMotorMO.setI(0f);
                neckMotorMO.setI(0f);
                yawPos = 0f;
                headPos = 0f;
                experiment_number++;
                //experiment_number = printToFile(global_reward, "rewards.txt", experiment_number, false, action_number);
//                        stringOutput.clear();
//                       stringOutput.add("rewards.txt");
                oc.vision.setExp(experiment_number);
                action_number = 0;
                oc.reset_battery();
                executedActions.clear();


                //oc.marta_position.resetData();
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            } else if (mode.equals("exploring") && (action_number >= MAX_ACTION_NUMBER ) || crashed || battery_lvint==0) {
                System.out.println("Max number of actions or crashed. Exp: "+ experiment_number +" ----- N_act: "+action_number+" Curiosity_lv: "+curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c);
                aux_crash = 0;
                oc.shuffle_positions();
                oc.reset_positions();

                headMotorMO.setI(0f);
                neckMotorMO.setI(0f);
                yawPos = 0f;
                headPos = 0f;
                //experiment_number = printToFile(global_reward, "rewards.txt", experiment_number, false, action_number);
                experiment_number++;
                //stringOutput.clear();
                //stringOutput.add("rewards.txt");
                oc.vision.setExp(experiment_number);
                oc.reset_battery();
                action_number = 0;
                executedActions.clear();
                if (experiment_number > MAX_EXPERIMENTS_NUMBER) {

                    System.exit(0);
                }
            }
	}
	
            
	private void printToFile(String filename){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
        LocalDateTime now = LocalDateTime.now();
        
        if ( experiment_number < MAX_EXPERIMENTS_NUMBER) {
            MemoryObject battery_lv = (MemoryObject) battReadings.get(battReadings.size()-1);
            int battery_lvint = (int)battery_lv.getI();
	        try(FileWriter fw = new FileWriter("profile/"+filename,true);
	            BufferedWriter bw = new BufferedWriter(fw);
	            PrintWriter out = new PrintWriter(bw))
	        {
	            out.println(dtf.format(now)+" Exp:"+experiment_number+" Nact:"+action_number+" Battery:"+battery_lvint+" Curiosity_lv:"+curiosity_lv+" Red:"+red_c+" Green:"+green_c+" Blue:"+blue_c);
	            
	            out.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
        }
      

	}
		

}
