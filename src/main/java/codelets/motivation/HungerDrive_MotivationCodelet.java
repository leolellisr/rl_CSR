/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codelets.motivation;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
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
public class HungerDrive_MotivationCodelet extends MotivationalCodelet 
{
    private int battery;
    private MemoryObject motivationMO;
    private MemoryContainer motivationMC;
    private OutsideCommunication oc;
    private int stage, nActions;
    private Idea hunger_motivation_idea;
    private List battReadings;
    private double activation = 0.0;
    private int max_lv = 100, index = -1;
    private boolean debug = false;
    public HungerDrive_MotivationCodelet(String id, double level, double priority, double urgencyThreshold, OutsideCommunication outc){
        super(id, level, priority, urgencyThreshold);
        this.oc = outc;
    }
    
    @Override
	public void accessMemoryObjects() {
		
		MemoryObject MO = (MemoryObject) this.getInput("BATTERY_BUFFER");
                battReadings = (List) MO.getI();
                
                motivationMC = (MemoryContainer) this.getOutput("MOTIVATION");

                if(debug) System.out.println("Hunger MC name: "+this.motivationMC.getName());
                ArrayList<Memory> allMemories = this.motivationMC.getAllMemories();
                if(debug) System.out.println("Hunger MC size: "+allMemories.size());
                if(debug) System.out.println("Hunger MC: "+allMemories);
                // motivationMO = (MemoryObject) motivationMC.getI(index);
                        }
        
        @Override
	public void calculateActivation() {
           if(debug) System.out.println("before calc activation hunger: "+this.activation);
           if(!battReadings.isEmpty()){
                MemoryObject battery_lv = (MemoryObject) battReadings.get(battReadings.size()-1);
                if(debug) System.out.println("battery lv"+battery_lv.getI()+" type:"+battery_lv.getI().getClass().toString());
                if("class java.lang.Integer".equals(battery_lv.getI().getClass().toString())){
                    int battery_lvint = (int)battery_lv.getI();
                    if(debug) System.out.println("battery lv before calc activation hunger: "+battery_lvint);

                    int inv_bat = 100-battery_lvint;
                    this.activation = (double)inv_bat/(double)max_lv;

                    if(debug) System.out.println("inv_bat "+inv_bat+" act_bat "+this.activation);
                }
           }	
           if(debug) System.out.println("after calc activation hunger: "+this.activation);
           
	}

	public double getActivation() {
            if(debug) System.out.println("before get activation hunger: "+this.activation);
        
            this.calculateActivation();

            if(debug) System.out.println("after get activation hunger: "+this.activation);

            return this.activation;
		
	}
        
       // Main Codelet function, to be implemented in each subclass.
	@Override
	public void proc() {
	
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }

            if(!battReadings.isEmpty()) this.activation = getActivation();

            hunger_motivation_idea = new Idea("SURVIVAL", this.activation);

            if(debug) System.out.println("Battery activation: "+this.activation);
            if(index == -1) index = motivationMC.setI(hunger_motivation_idea, activation);
            else motivationMC.setI(hunger_motivation_idea, activation, index);
        }

    @Override
    public double calculateSimpleActivation(List<Memory> list) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public double calculateSecundaryDriveActivation(List<Memory> list, List<Drive> list1) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
