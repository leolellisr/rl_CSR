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
    private List<String> allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", "am13", "aa0", "aa1", "aa2", "am14", "am15", "am16"));
    private Idea motivationMO;
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
                    MemoryContainer MC = (MemoryContainer) this.getInput("MOTIVATION");
                    motivationMO = (Idea) MC.getI();
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
		try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
                if(motivationMO == null){
                  if(debug) System.out.println("Rewardcomputer motivationMO is null");
                return;
            }
                
                if (!states.isEmpty() && !actions.isEmpty()  ){
                Object state = (Object) states.get(states.size() - 1);
                String action = (String) actions.get(actions.size() - 1);
                int action_n = allActionsList.indexOf(action);
                double reward = 0;
                if(this.num_tables == 2){
                    if(motivationMO.getName().equals("CURIOSITY") && !crewards.isEmpty()) reward = (double) crewards.get(crewards.size() - 1);
                    else if(motivationMO.getName().equals("SURVIVAL") && !srewards.isEmpty()) reward = (double) srewards.get(srewards.size() - 1);
                } else if(this.num_tables == 1 && !rewards.isEmpty()){
                    reward = (double) rewards.get(rewards.size() - 1); 
                }
                boolean verify_memory = verify_if_memory_exists(state.toString());
                    if(!verify_memory){
                        MemoryObject newProcedure = new MemoryObject();
                        newProcedure.setName(state.toString());
                        ArrayList<Integer> info = new ArrayList<>(Collections.nCopies(nActions, 0));
                        info.set(action_n, (int) reward);
                        newProcedure.setI(info);                    
                        proceduralMemoryMO.add(newProcedure);
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
}
