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
public class SurvivalDrive_MotivationCodelet extends MotivationalCodelet 
{
    private static int MAX_ACTION_NUMBER;
	
    private static int MAX_EXPERIMENTS_NUMBER;
    private int battery;
    private ArrayList<Object> motivationMO;
    private DriverArray motivationMC;
    private OutsideCommunication oc;
    private int stage, nActions;
    private Idea survival_motivation_idea;
    private List battReadings;
    private double activation = 0.0;
    private int max_lv = 100, index = -1;
    private boolean debug = false;
    private int experiment_number, action_number, exp_s, num_tables;
    public SurvivalDrive_MotivationCodelet(String id, double level, double priority, 
            double urgencyThreshold, OutsideCommunication outc, int num_tables){
        super(id, level, priority, urgencyThreshold);
        this.oc = outc;
        MAX_ACTION_NUMBER = oc.vision.getMaxActions();
        MAX_EXPERIMENTS_NUMBER = oc.vision.getMaxExp();
        this.num_tables = num_tables;
        exp_s = oc.vision.getExp();

        
    }
    
    @Override
    public void accessMemoryObjects() {

        MemoryObject MO = (MemoryObject) this.getInput("BATTERY_BUFFER");
        battReadings = (List) MO.getI();

        motivationMC = (DriverArray) this.getOutput("MOTIVATION");

        if(debug) System.out.println("Survival MC name: "+this.motivationMC.getName());
        ArrayList<Memory> allMemories = this.motivationMC.getAllMemories();
        if(debug) System.out.println("Survival MC size: "+allMemories.size());
        if(debug) System.out.println("Survival MC: "+allMemories);
         motivationMO = (ArrayList<Object>) motivationMC.getI();
    }

    @Override
    public void calculateActivation() {
       if(debug) System.out.println("before calc activation Survival: "+this.activation);
       if(!battReadings.isEmpty()){
            MemoryObject battery_lv = (MemoryObject) battReadings.get(battReadings.size()-1);
            if(debug) System.out.println("battery lv"+battery_lv.getI()+" type:"+battery_lv.getI().getClass().toString());
            if("class java.lang.Integer".equals(battery_lv.getI().getClass().toString())){
                int battery_lvint = (int)battery_lv.getI();
                if(debug) System.out.println("battery lv before calc activation Survival: "+battery_lvint);

                int inv_bat = 100-battery_lvint;
                this.activation = (double)inv_bat/(double)max_lv;

                if(debug) System.out.println("inv_bat "+inv_bat+" act_bat "+this.activation);
            }
       }	
       if(debug) System.out.println("after calc activation Survival: "+this.activation);

    }

    public double getActivation() {
        if(debug) System.out.println("before get activation Survival: "+this.activation);

        this.calculateActivation();

        if(debug) System.out.println("after get activation Survival: "+this.activation);

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

        survival_motivation_idea = new Idea("SURVIVAL", this.activation);

        if(debug) System.out.println("Battery activation: "+this.activation);
        if(index == -1) index = motivationMC.setI(survival_motivation_idea, activation);
        else motivationMC.setI(survival_motivation_idea, activation, index);
       // printToFile(activation,"survival_drive.txt", action_number);
        action_number+=1;    
        

        
        
        
        boolean exp_b = false;
        if(num_tables == 1) exp_b = this.experiment_number != this.oc.vision.getExp();
        else exp_b = this.exp_s != this.oc.vision.getExp("S");
        
        
        if(exp_b){
           this.experiment_number = this.oc.vision.getExp();
           this.exp_s = this.oc.vision.getExp("S");
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
    
   /* private void printToFile(Object object,String filename, int action_num){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
        LocalDateTime now = LocalDateTime.now();
        
        boolean exp_b = false;
        
        if(num_tables == 1) exp_b = this.experiment_number < MAX_EXPERIMENTS_NUMBER;
        else exp_b = this.exp_s < MAX_EXPERIMENTS_NUMBER;
        
        if ( exp_b) {
            try(FileWriter fw = new FileWriter("profile/"+filename,true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(dtf.format(now)+" "+ object+" Exp:"+experiment_number+" ExpS: "+this.exp_s+
                        " Nact:"+action_num+" Type:SURVIVAL");

                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
}