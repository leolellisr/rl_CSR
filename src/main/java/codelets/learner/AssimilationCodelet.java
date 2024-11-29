/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codelets.learner;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;
import static codelets.learner.AcommodationCodelet.calculateMean;
import codelets.motivation.DriverArray;

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
import java.util.HashMap;
import java.util.Map;

import outsideCommunication.OutsideCommunication;

/**
 *
 * @author leolellisr
 */
public class AssimilationCodelet extends Codelet 
{
    private List states, crewards, srewards, rewards, actions, proceduralList;
    private MemoryContainer proceduralMemoryMO;
    private OutsideCommunication oc;
    private int stage, nActions, num_tables;
    private String motivation;
    private List<String> allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", "am13", "aa0",  "am14", "am15", "am16")); //"aa1", "aa2",
    private ArrayList<Object> motivationMO;
    private boolean debug = false;
    
    public AssimilationCodelet(OutsideCommunication outc, String motivation, int num_tables){
        super();
        this.oc = outc;
        this.stage = this.oc.vision.getStage();
        this.num_tables = num_tables;
        this.motivation = motivation;
    }
    
    @Override
    public void accessMemoryObjects() {

        MemoryObject MO;
        MO = (MemoryObject) this.getInput("STATES");
        states = (List) MO.getI();
        if(this.num_tables == 2){
            MO = (MemoryObject) this.getInput("CUR_REWARDS");
            crewards = (List) MO.getI();
            MO = (MemoryObject) this.getInput("SUR_REWARDS");
            srewards = (List) MO.getI();
        }else if(this.num_tables == 1){
            MO = (MemoryObject) this.getInput("REWARDS");
            rewards = (List) MO.getI();
        }
        MO = (MemoryObject) this.getInput("ACTIONS");
        actions = (List) MO.getI();
        if(this.motivation.equals("drives")){
            DriverArray MC = (DriverArray) this.getInput("MOTIVATION");
            motivationMO = (ArrayList<Object>) MC.getI();
        }
        proceduralMemoryMO = (MemoryContainer) this.getOutput("PROCEDURAL");
        //proceduralList = (List) proceduralMemoryMO.getI();
    }
        
        @Override
	public void calculateActivation() {
		// TODO Auto-generated method stub
		
	}
        
       // Main Codelet function, to be implemented in each subclass.
	@Override
	public void proc() {
	
        if(stage == 1 || stage == 2){
            nActions = 10;
        }else if(stage == 3){
            nActions = 17;
        }
	/*try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }*/
         if(motivationMO == null){
              if(debug) System.out.println("Rewardcomputer motivationMO is null");
            return;
        }
                
        if (!states.isEmpty() && !actions.isEmpty()  ){
            Object state = (Object) states.get(states.size() - 1);
            String action = (String) actions.get(actions.size() - 1);
            int action_n = allActionsList.indexOf(action);
            if(action_n > -1){
            double reward = 0;
            Idea curI = (Idea) motivationMO.get(0);
                Idea surI = (Idea) motivationMO.get(1);
                String nameMotivation;
                boolean curB;
try{
                curB = (double) Collections.max((List) curI.getValue()) > (double) surI.getValue();
}
        catch(Exception e){
        curB = false;
        }

                if(curB){
                    nameMotivation = "CURIOSITY";
                }
                else{
                    nameMotivation = "SURVIVAL";
                }
                
            if(this.num_tables == 2){
                if(nameMotivation.equals("CURIOSITY") && !crewards.isEmpty()) reward = (double) crewards.get(crewards.size() - 1);
                else if(nameMotivation.equals("SURVIVAL") && !srewards.isEmpty()) reward = (double) srewards.get(srewards.size() - 1);
            } else if(this.num_tables == 1 && !rewards.isEmpty()){
                reward = (double) rewards.get(rewards.size() - 1); 
            }
            double activation;
            ArrayList<Double> activation_a;
            if(nameMotivation.equals("CURIOSITY")) {
                activation_a = (ArrayList<Double>) curI.getValue();
                activation  = calculateMean(activation_a);
            }
            else activation  = (double) surI.getValue();
            boolean verify_memory = verify_if_memory_exists(state.toString());
                if(!verify_memory){
                    MemoryObject newProcedure = new MemoryObject();
                    newProcedure.setName(state.toString());
                    ArrayList<Integer> info = new ArrayList<>(Collections.nCopies(nActions, 0));
                    if(info.size()>action_n){  
                        info.set(action_n, (int) reward);
                        newProcedure.setI(info);                    
                        int i = proceduralMemoryMO.add(newProcedure);
                        proceduralMemoryMO.setEvaluation(activation, i);
                    }
                }
            }
        }
        }
        
        public boolean verify_if_memory_exists(String name){
            boolean exists = false;
            if(!proceduralMemoryMO.getAllMemories().isEmpty()){
                for(Memory memory : proceduralMemoryMO.getAllMemories()) {
                    if(memory.getName().equalsIgnoreCase(name)){
                        exists = true;
                        break;
                    }
                }
            }
            return exists;
        }
        
        
        public static double calculateMean(ArrayList<Double> list) {
            if (list.isEmpty()) {
                return 0; // Return 0 if the list is empty or handle it as required
            }

            double sum = 0;
            for (double value : list) {
                sum += value;
            }

            return sum / list.size();
        }
            
}
