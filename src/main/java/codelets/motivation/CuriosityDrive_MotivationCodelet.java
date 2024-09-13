/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codelets.motivation;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import br.unicamp.cst.representation.idea.Idea;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
public class CuriosityDrive_MotivationCodelet extends MotivationalCodelet 
{   private Idea curiosity_motivation_id = new Idea("CURIOSITY", null);;
    private List actions, rewards;
    private MemoryObject motivationMO, activationMO;
    private OutsideCommunication oc;
    private int stage, nActions;
    private ArrayList<String> allActionsList;
    private double activation;
    private boolean debug = false;
    private int index = -1;
    private MemoryContainer motivationMC;
    private ArrayList<Double> curiosity_motivation_list;
    private int experiment_number, action_number;
    private static final int MAX_EXPERIMENTS_NUMBER = 100;    
    public CuriosityDrive_MotivationCodelet(String id, double level, double priority, double urgencyThreshold, OutsideCommunication outc){
        super(id, level, priority, urgencyThreshold);
        this.oc = outc;
        this.stage = this.oc.vision.getStage();
        this.activation = 0.0;
    }
    
    @Override
	public void accessMemoryObjects() {
		
		MemoryObject MO;
                MO = (MemoryObject) this.getInput("ACTIONS");
                actions = (List) MO.getI();
                
                MO = (MemoryObject) this.getInput("CUR_REWARDS");
                rewards = (List) MO.getI();
                
                motivationMC = (MemoryContainer) this.getOutput("MOTIVATION");

                if(debug) System.out.println("Curiosity MC name: "+this.motivationMC.getName());
                ArrayList<Memory> allMemories = this.motivationMC.getAllMemories();
                if(debug) System.out.println("Curiosity MC size: "+allMemories.size());
                if(debug) System.out.println("Curiosity MC: "+allMemories);
                

//                activationMO = (MemoryObject) this.getOutput("CURIOSITY_ACTIVATION");
//                curiosity_activationList = (List) activationMO.getI();
        }
        
        @Override
	public void calculateActivation() {
            if(actions.isEmpty()) this.activation = (double) 1.0;
		
	}

	public double getActivation() {
            this.calculateActivation();
            return this.activation;
		
	}
        
       // Main Codelet function, to be implemented in each subclass.
	@Override
	public void proc() {
            getActivation();
            if(stage == 1 || stage == 2){
                nActions = 10;
		allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", "am13", "aa0", "aa1", "aa2", "am14", "am15", "am16"));
            }else if(stage == 3){
                nActions = 20;
		allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", "am13", "aa0", "aa1", "aa2", "am14", "am15", "am16"));
            }
		try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
               
            if(curiosity_motivation_id.getValue() == null && stage > 1){
                curiosity_motivation_list = new ArrayList<>(Collections.nCopies(nActions, 0.0));

            } else if (stage > 1){
                curiosity_motivation_list = (ArrayList<Double>) curiosity_motivation_id.getValue();
                for(String action : allActionsList){
                    if(!actions.contains(action)){
                        if(curiosity_motivation_list.get(allActionsList.indexOf(action)) == 0 || curiosity_motivation_list.get(allActionsList.indexOf(action)) < 0) curiosity_motivation_list.set(allActionsList.indexOf(action), 1.0);
                        else curiosity_motivation_list.set(allActionsList.indexOf(action), curiosity_motivation_list.get(allActionsList.indexOf(action))-0.1);
                    }else if(curiosity_motivation_list.get(allActionsList.indexOf(action)) > 0.1) {
                        curiosity_motivation_list.set(allActionsList.indexOf(action),curiosity_motivation_list.get(allActionsList.indexOf(action))-0.1);
                    }else if(curiosity_motivation_list.get(allActionsList.indexOf(action)) < 0.1 || curiosity_motivation_list.get(allActionsList.indexOf(action)) == 0.1) {
                        curiosity_motivation_list.set(allActionsList.indexOf(action), 0.0);
                    }
                }
            }



            curiosity_motivation_id.setValue(curiosity_motivation_list);
            if(actions.isEmpty()) activation = (double) 1.0;
            else activation = (double) Collections.max(curiosity_motivation_list);
            if(debug) System.out.println("curiosity_motivation_list: "+curiosity_motivation_list);
            if(index == -1) index = motivationMC.setI(curiosity_motivation_id, activation);
            else motivationMC.setI(curiosity_motivation_id, activation, index);
            printToFile(activation,"curiosity_drive.txt", action_number);
            action_number+=1;    
            if(this.experiment_number != this.oc.vision.getExp()){
                this.experiment_number = this.oc.vision.getExp();
                action_number=0;
            }
        }

    @Override
    public double calculateSimpleActivation(List<Memory> list) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public double calculateSecundaryDriveActivation(List<Memory> list, List<Drive> list1) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
        private void printToFile(Object object,String filename, int action_num){
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
    LocalDateTime now = LocalDateTime.now();

    if ( experiment_number < MAX_EXPERIMENTS_NUMBER) {
            try(FileWriter fw = new FileWriter("profile/"+filename,true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(dtf.format(now)+" "+ object+" Exp:"+experiment_number+" Nact:"+action_num+" Type:CURIOSITY");

                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    }
}
