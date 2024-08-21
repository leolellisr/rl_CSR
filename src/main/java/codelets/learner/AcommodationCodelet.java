/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codelets.learner;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;

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
public class AcommodationCodelet extends Codelet 
{
    private List states, rewards, actions, proceduralList;
    private MemoryContainer proceduralMemoryMO;
    private OutsideCommunication oc;
    private int stage, nActions;
    public AcommodationCodelet(OutsideCommunication outc){
        super();
        this.oc = outc;
        this.stage = this.oc.vision.getStage();
    }
    
    @Override
	public void accessMemoryObjects() {
		
		MemoryObject MO;
                MO = (MemoryObject) this.getInput("STATES");
                states = (List) MO.getI();
                MO = (MemoryObject) this.getInput("REWARDS");
                rewards = (List) MO.getI();
                MO = (MemoryObject) this.getInput("ACTIONS");
                actions = (List) MO.getI();
                
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
                if (!states.isEmpty() && !actions.isEmpty() && !rewards.isEmpty() && !proceduralMemoryMO.getAllMemories().isEmpty()){
                Object state = (Object) states.get(states.size() - 1);
                int action = (int) actions.get(actions.size() - 1);
                int reward = (int) rewards.get(rewards.size() - 1);
                boolean verify_memory = verify_if_memory_exists(state.toString());
                    if(verify_memory){
                        ArrayList<Integer> info = null;
                        for(Memory memory : proceduralMemoryMO.getAllMemories()) {
                            if(memory.getName().equalsIgnoreCase(state.toString())){
                                info = (ArrayList<Integer>) memory.getI();
                                if(info.size() != nActions){
                                    for(int i=0; i<nActions-info.size();i++){
                                        info.add(0);
                                    }
                                }
                                info.set(action, reward);
                                break;
                            }
                        }
                        proceduralMemoryMO.setI(info, 0, state.toString());
                    }
                }
        }
        
         public boolean verify_if_memory_exists(String name_m){
            boolean exists = false;
            //if(!proceduralMemoryMO.getAllMemories().isEmpty()){
                for(Memory memory : proceduralMemoryMO.getAllMemories()) {
                    if(memory.getName().equalsIgnoreCase(name_m)){
                        exists = true;
                        break;
                    }
                }
            //}
            return exists;
        }
}
