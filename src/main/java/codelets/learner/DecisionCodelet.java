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

public class DecisionCodelet extends Codelet 

{

	private int time_graph;
	private static final float CRASH_TRESHOLD = 0.28f;
	
	private static final int MAX_ACTION_NUMBER = 500;
	
	private static final int MAX_EXPERIMENTS_NUMBER = 100;
	
	private QLearningL ql;
    


    private Idea motivationMO;
    private MemoryObject motorActionMO, reward_stringMO, action_stringMO;
    private MemoryObject neckMotorMO;
    private MemoryObject headMotorMO;
    private List<String> actionsList, allStatesList;
    private List<QLearningL> qTableList, qTableSList, qTableCList;
    private List<Double>  rewardList, rewardSList, rewardCList;
    private OutsideCommunication oc;
    private final int timeWindow;
    private final int sensorDimension;
    private List saliencyMap;
    private float vel = 2f,angle_step;
    
    private int curiosity_lv, red_c, green_c, blue_c;
    private int action_index;
    private int experiment_number;
    private int stage, action_number=0;
    int fovea; 
    private String mode;
    private Random gerador = new Random();

    
    private float yawPos = 0f, headPos = 0f;   
    private boolean crashed = false;
    private boolean debug = false;
    private int num_tables, aux_crash = 0;
    private ArrayList<String> executedActions  = new ArrayList<>();
    private ArrayList<String> allActionsList;
    private Map<String, ArrayList<Integer>> proceduralMemory = new HashMap<String, ArrayList<Integer>>();
    private String output, motivation, stringOutput = "";
    private ArrayList<Float> lastLine;
	public DecisionCodelet (OutsideCommunication outc, int tWindow, int sensDim, String mode, String motivation, int num_tables) {
		
		super();
		time_graph = 0;
				
		experiment_number = 1;
                this.num_tables = num_tables;
                this.motivation = motivation;
                // allActions: am0: focus; am1: neck left; am2: neck right; am3: head up; am4: head down; 
                // am5: fovea 0; am6: fovea 1; am7: fovea 2; am8: fovea 3; am9: fovea 4; 
                // am10: neck tofocus; am11: head tofocus; am12: neck awayfocus; am13: head awayfocus
                // aa0: focus td color; aa1: focus td depth; aa2: focus td region.
		allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", "am13", "aa0", "aa1", "aa2", "am14", "am15", "am16"));
		// States are 0 1 2 ... 5^256-1
		ArrayList<String> allStatesList = new ArrayList<>(Arrays.asList(IntStream.rangeClosed(0, (int)Math.pow(2, 16)-1).mapToObj(String::valueOf).toArray(String[]::new)));
		
                oc = outc;
                         
                this.stage = this.oc.vision.getStage();
                
		
		angle_step = 0.1f;
		
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
                if(this.motivation.equals("drives")){
                    MemoryContainer MC = (MemoryContainer) this.getInput("MOTIVATION");
                    motivationMO = (Idea) MC.getI();
                }               
                
                if(num_tables==2){
                        
                    MO = (MemoryObject) this.getInput("SUR_REWARDS");
                    rewardSList = (List) MO.getI();
                    MO = (MemoryObject) this.getInput("QTABLES");
                    qTableSList = (List) MO.getI();

                    MO = (MemoryObject) this.getInput("CUR_REWARDS");
                    rewardCList = (List) MO.getI();
                    MO = (MemoryObject) this.getInput("QTABLEC");
                    qTableCList = (List) MO.getI();
                }
                else if(num_tables == 1){
                    MO = (MemoryObject) this.getInput("REWARDS");
                    rewardList = (List) MO.getI();
                    MO = (MemoryObject) this.getInput("QTABLE");
                    qTableList = (List) MO.getI();
                }
                MO = (MemoryObject) this.getOutput("STATES");
                allStatesList = (List) MO.getI();

                MO = (MemoryObject) this.getOutput("ACTIONS");
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
                //System.out.println("yawPos: "+yawPos+" headPos: "+headPos);
		try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }       
        QLearningL ql = null;
        
        if(motivationMO == null){
            System.out.println("DECISION -----  motivationMO is null");
                return;
            }
        
        if(this.num_tables == 2 && motivationMO.getName().equals("SURVIVAL")){
            if(qTableSList.isEmpty()){
                return;
            }
            ql = qTableSList.get(qTableSList.size()-1);

        }else if(this.num_tables == 2){
            if(qTableCList.isEmpty()){
                return;
            }
            ql = qTableCList.get(qTableCList.size()-1);

        }else if(this.num_tables == 1){
            if(qTableList.isEmpty()){
                return;
            }
            ql = qTableList.get(qTableList.size()-1);
        }
        
        // Use the Random class to generate a random index
       
        if(ql==null){
            return;
        }

        
        String state = "-1";
        state = getStateFromSalMap();
        String actionToTake = ql.getAction(state);

                // Select best action to take

        
        if(actionsList.size() == timeWindow){
                    actionsList.remove(0);
        } 
                
        actionsList.add(actionToTake);
        
        if(allStatesList.size() == timeWindow){
                    allStatesList.remove(0);
        } 
                
        allStatesList.add(state);
        action_number += 1;
        printToFile(actionToTake,"actions.txt", action_number);
        
        if(this.experiment_number != this.oc.vision.getExp()){
            this.experiment_number = this.oc.vision.getExp();
            action_number=0;
        }
        System.out.println("DECISION ----- Exp: "+ experiment_number + " ----- N act: "+action_number+" ----- Act: "+actionToTake+" ----- Type: "+motivationMO.getName());
	}
	
	

	
        // Discretization
	// Normalize and transform a salience map into one state
		// Normalized values between 0 and 1 can be mapped into 0, 1, 2, 3 or 4
		// Them this values are computed into one respective state
    public String getStateFromSalMap() {
        ArrayList<Float> mean_lastLine = new ArrayList<>();
        for(int i=0; i<16;i++) mean_lastLine.add(0f);
        
/*                        redReadings = (List) colorReadings.get(0);
                        greenReadings = (List) colorReadings.get(1);
                        blueReadings = (List) colorReadings.get(2);
                        */
			// Getting just the last entry (current sal map)
			lastLine = (ArrayList<Float>) saliencyMap.get(saliencyMap.size() -1);
/*                        lastRed = (ArrayList<Float>) redReadings.get(redReadings.size() -1);
                        lastGreen = (ArrayList<Float>) greenReadings.get(greenReadings.size() -1);
                        lastBlue = (ArrayList<Float>) blueReadings.get(blueReadings.size() -1);

                        lastDist = (ArrayList<Float>) distReadings.get(distReadings.size() -1);
*/
        try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        } 
                        
        //if (calculateMean(lastRed)<0.01 && calculateMean(lastGreen)<0.01 && calculateMean(lastBlue)<0.01) aux_crash += 1;
        if (Collections.max(lastLine) == 0) aux_crash += 1;
        else aux_crash = 0; 

        if(action_number > 5 && aux_crash> 5 ){
            crashed = true;
        }

        /*int indexRed = -1;
        int indexGreen = -1;
        int indexBlue = -1;                        
        int indexDist = -1;
*/
        if (this.stage == 3) {

/*                          if (lastRed.indexOf(Collections.max(lastRed))>-1) indexRed = lastRed.indexOf(Collections.max(lastRed));
            if (lastGreen.indexOf(Collections.max(lastGreen))>-1) indexGreen = lastGreen.indexOf(Collections.max(lastGreen));
            if (lastBlue.indexOf(Collections.max(lastBlue))>-1) indexBlue = lastBlue.indexOf(Collections.max(lastBlue));

            if (lastDist.indexOf(Collections.max(lastDist))>-1) indexDist = lastDist.indexOf(Collections.max(lastDist));   
*/ 
            }

        if(debug){
/*                        System.out.println("lastRed: "+calculateMean(lastRed));
            System.out.println("indexRed: "+indexRed);
            System.out.println("lastGreen: "+calculateMean(lastGreen));
            System.out.println("indexGreen: "+indexGreen);

            System.out.println("lastBlue: "+calculateMean(lastBlue));
            System.out.println("indexBlue: "+indexBlue);*/
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
                        /*if(i == indexRed && indexRed != -1) winnerRed = n*4+m;
                        if(i == indexGreen && indexGreen != -1) winnerGreen = n*4+m;
                        if(i == indexBlue && indexBlue != -1) winnerBlue = n*4+m;
                        if(i == indexDist && indexDist != -1) winnerDist = n*4+m;*/

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
                /*System.out.println("winnerRed: "+winnerRed);
                System.out.println("winnerGreen: "+winnerGreen);
                System.out.println("winnerBlue: "+winnerBlue);*/
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

    private void printToFile(Object object,String filename, int action_num){
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
    LocalDateTime now = LocalDateTime.now();

    if ( experiment_number < MAX_EXPERIMENTS_NUMBER) {
            try(FileWriter fw = new FileWriter("profile/"+filename,true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(dtf.format(now)+" "+ object+" Exp:"+experiment_number+" Act num: "+action_num+" ----- Type: "+motivationMO.getName());

                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    }


}
