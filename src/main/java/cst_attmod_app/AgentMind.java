/*
 * /*******************************************************************************
 *  * Copyright (c) 2012  DCA-FEEC-UNICAMP
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the GNU Lesser Public License v3
 *  * which accompanies this distribution, and is available at
 *  * http://www.gnu.org/licenses/lgpl.html
 *  * 
 *  * Contributors:
 *  *     K. Raizer, A. L. O. Paraense, R. R. Gudwin - initial API and implementation
 *  ******************************************************************************/
 
package cst_attmod_app;


import attention.DecisionMaking;
import attention.SalMap;
import attention.Winner;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.Idea;
import sensory.SensorBufferCodelet;
import codelets.learner.AcommodationCodelet;
import codelets.learner.AssimilationCodelet;
import codelets.motor.MotorCodelet;
import codelets.sensors.Sensor_Vision;
import codelets.sensors.Sensor_Depth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import outsideCommunication.OutsideCommunication;
import codelets.learner.LearnerCodelet;
import codelets.sensors.CFM;
/*import codelets.sensors.Sensor_ColorRed;
import codelets.sensors.Sensor_ColorGreen;
import codelets.sensors.Sensor_ColorBlue;*/
import codelets.sensors.BU_FM_Color;
/*import codelets.sensors.BU_FM_ColorGreen;
import codelets.sensors.BU_FM_ColorBlue;*/
import codelets.sensors.BU_FM_Depth;
import codelets.sensors.TD_FM_Color;
import codelets.sensors.TD_FM_Depth;
import codelets.motivation.CuriosityDrive_MotivationCodelet;
import codelets.motivation.HungerDrive_MotivationCodelet;
import codelets.sensors.Sensor_Battery;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import codelets.support.*;

/**
 *
 * @author L. L. Rossi (leolellisr)
 */
public class AgentMind extends Mind {
    public static final int Resolution = 256;
    public static final int Buffersize = 100;
    public static final int Visiondimension = Resolution*Resolution*3;
    public static final int Vision_image_dimension = Resolution*Resolution;
    public static final int Sensor_dimension = 256;
    public static final boolean debug = true;
    private int index_hunger, index_curiosity;
        private String stringOutputac = "", stringOutputre = "";

    public AgentMind(OutsideCommunication oc, String mode, String motivation) throws IOException{
        
        super();
        //System.out.println("AgentMind");
        //////////////////////////////////////////////
        //Declare Memory Objects
        //////////////////////////////////////////////
        
        // Motivation
        // MemoryObject curiosity_motivationMO = null, curiosity_activationMO  = null, hunger_motivationMO  = null;
        MemoryContainer motivationMC = new MemoryContainer("MOTIVATION");

        //Motor - Neck
        MemoryObject motorActionMO;
        MemoryObject Neck_Yaw, Head_Pitch;
        motorActionMO = createMemoryObject("MOTOR", "");
        Neck_Yaw = createMemoryObject("NECK_YAW", new Float(0));
        Head_Pitch = createMemoryObject("HEAD_PITCH", new Float(0));
        
        ArrayList<Memory> motorMOs = new ArrayList<>();
        motorMOs.add(motorActionMO);
        motorMOs.add(Neck_Yaw);
        motorMOs.add(Head_Pitch);
        
        //Sensors
        
        // Battery
        List battery_data = Collections.synchronizedList(new ArrayList<Float>(1));
        MemoryObject battery_read = createMemoryObject("BATTERY", battery_data);
        
        //Vision Sensor
        List vision_data = Collections.synchronizedList(new ArrayList<Float>(Visiondimension));
        MemoryObject vision_read = createMemoryObject("VISION", vision_data);
        
        //Vision Sensor
        List depth_data = Collections.synchronizedList(new ArrayList<Float>(Vision_image_dimension));
        MemoryObject depth_read = createMemoryObject("DEPTH", depth_data);
        
        //Sensor Buffers
        List battery_buffer_list = Collections.synchronizedList(new ArrayList<Memory>(Buffersize));
        MemoryObject battery_bufferMO = createMemoryObject("BATTERY_BUFFER",battery_buffer_list);
        

        //Vision buffer
        List vision_buffer_list = Collections.synchronizedList(new ArrayList<Memory>(Buffersize));
        MemoryObject vision_bufferMO = createMemoryObject("VISION_BUFFER",vision_buffer_list);
        
               
        //Sensor Buffers
        //Vision buffer
        List depth_buffer_list = Collections.synchronizedList(new ArrayList<Memory>(Buffersize));
        MemoryObject depth_bufferMO = createMemoryObject("DEPTH_BUFFER", depth_buffer_list);
        
        
                //Depth FM
        List depthFM = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        MemoryObject depth_fmMO = createMemoryObject("DEPTH_FM", depthFM);
        
        
        
      // Color data 
        
 /* not used
      //Vision RED data
        List visionRed = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        MemoryObject vision_redMO = createMemoryObject("VISION_RED", visionRed);
        
        //Vision GREEN data
        List visionGreen = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        MemoryObject vision_greenMO = createMemoryObject("VISION_GREEN", visionGreen);
        
        //Vision BLUE data
        List visionBlue = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        MemoryObject vision_blueMO = createMemoryObject("VISION_BLUE", visionBlue);
          
        */
        
        //Color bottom-up Feature Maps

        //Vision color FM
        List visionColorFM = Collections.synchronizedList(new ArrayList<ArrayList<ArrayList<Float>>>());
        ArrayList vision_redFM = new ArrayList<>(); //= (List) vision_FM.get(0); // Get red data
        ArrayList vision_greenFM= new ArrayList<>(); // = (List) vision_FM.get(1); // Get green data
        ArrayList vision_blueFM = new ArrayList<>(); // = (List) vision_FM.get(2); // Get blue data
        visionColorFM.add(vision_redFM);
        visionColorFM.add(vision_greenFM);
        visionColorFM.add(vision_blueFM);
        MemoryObject vision_color_fmMO = createMemoryObject("VISION_COLOR_FM", visionColorFM);
        
/*        //Vision GREEN FM
        List visionGreenFM = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        MemoryObject vision_green_fmMO = createMemoryObject("VISION_GREEN_FM", visionGreenFM);
        
        //Vision BLUE FM
        List visionBlueFM = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        MemoryObject vision_blue_fmMO = createMemoryObject("VISION_BLUE_FM", visionBlueFM);
  */      

        //Top-down FMs
        //Depth FM
        List depthtopFM = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        MemoryObject depth_top_fmMO = createMemoryObject("DEPTH_TOP_FM", depthtopFM);
        
        //Color top-down Feature Maps

        //Vision Color
        List visionColortopFM = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        MemoryObject vision_color_top_fmMO = createMemoryObject("VISION_COLOR_TOP_FM", visionColortopFM);
        
        //Vision region
        List visionRegiontopFM = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        MemoryObject vision_region_top_fmMO = createMemoryObject("REGION_TOP_FM", visionRegiontopFM);
        
        //Variable:  type_winner       
        List typeWinner= Collections.synchronizedList(new ArrayList<Float>());
        MemoryObject type_fmMO = createMemoryObject("TYPE", typeWinner);
        

        //
        // Weight list
        
        List weights = Collections.synchronizedList(new ArrayList<Float>(7));
        weights.add(1.0f);
        weights.add(1.0f);
        weights.add(1.0f);
        weights.add(1.0f);
        weights.add(1.0f);
    
        MemoryObject weightsMO = createMemoryObject("FM_WEIGHTS",weights);
        
        //Combined Feat data
        List CombFM = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());        
        MemoryObject combFMMO = createMemoryObject("COMB_FM",CombFM);
        
        
        //ATTENTIONAL MAP
        List att_mapList = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());        
        MemoryObject attMapMO = createMemoryObject("ATTENTIONAL_MAP",att_mapList);
        
        //WINNERS
        List winnersList = Collections.synchronizedList(new ArrayList<Winner>());
        MemoryObject winnersMO = createMemoryObject("WINNERS",winnersList);
        
        //SALIENCY MAP
        List saliencyMap = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        MemoryObject salMapMO = createMemoryObject("SALIENCY_MAP", saliencyMap);
                
        //ACTIONS 
        List actionsList = Collections.synchronizedList(new ArrayList<String>());
        MemoryObject actionsMO = createMemoryObject("ACTIONS", actionsList);
        
        //STATES 
        List statesList = Collections.synchronizedList(new ArrayList<String>());
        MemoryObject statesMO = createMemoryObject("STATES", statesList);

        //REWARDS
        List rewardsList = Collections.synchronizedList(new ArrayList<Integer>());
        MemoryObject rewardsMO = createMemoryObject("REWARDS", rewardsList);
        
        // PROCEDURAL MEMORY
        //List proceduralMemory = Collections.synchronizedList(new ArrayList<HashMap<Observation, ArrayList<Integer>>>());
        MemoryContainer proceduralMO = createMemoryContainer("PROCEDURAL");
        
        // SAVER MOs
        //stringOutput.add("rewards.txt");
        stringOutputre = "time_graph Exp_number Action_num Battery Curiosity_lv Red Green Blue reward";
                        
        MemoryObject reward_saverMO = createMemoryObject("REWARDS_STRING_OUTPUT", stringOutputre);
        
        // stringOutput.clear();
        stringOutputac = "time_graph Exp_number Action_num Battery Curiosity_lv Red Green Blue action";

        MemoryObject action_saverMO = createMemoryObject("ACTION_STRING_OUTPUT", stringOutputac);
        //stringOutput.add("actions.txt");
         
//        
//        
//        ////////////////////////////////////////////
//        //Codelets
//        ////////////////////////////////////////////
////        
        //Motor - Neck
        Codelet motors = new MotorCodelet(oc.HeadPitch_m, oc.NeckYaw_m);
        motors.addInputs(motorMOs);
        insertCodelet(motors);
        
        //Vision Sensor
        Codelet visions = new Sensor_Vision(oc.vision);
        visions.addOutput(vision_read);
        insertCodelet(visions);

                //Battery Sensor
        Codelet battery_c = new Sensor_Battery(oc.battery);
        battery_c.addOutput(battery_read);
        insertCodelet(battery_c);

        //Depth Sensor
        Codelet depths = new Sensor_Depth(oc.depth, oc.vision);
        //visions.addInput(stage_fmMO);
        depths.addOutput(depth_read);
        insertCodelet(depths);
        
        //Sensor Buffers
        //Vision data
        Codelet vision_buffer = new SensorBufferCodelet("VISION", "VISION_BUFFER", Buffersize);
        vision_buffer.addInput(vision_read);
        vision_buffer.addOutput(vision_bufferMO);
        insertCodelet(vision_buffer);

        //Battery data
        Codelet battery_buffer = new SensorBufferCodelet("BATTERY", "BATTERY_BUFFER", Buffersize);
        battery_buffer.addInput(battery_read);
        battery_buffer.addOutput(battery_bufferMO);
        insertCodelet(battery_buffer);
        
        //Depth data
        Codelet depth_buffer = new SensorBufferCodelet("DEPTH", "DEPTH_BUFFER", Buffersize);
        depth_buffer.addInput(depth_read);
        depth_buffer.addOutput(depth_bufferMO);
        insertCodelet(depth_buffer);
        
        //Buffers list
        ArrayList<String> sensbuff_names_vision = new ArrayList<>();
        sensbuff_names_vision.add("VISION_BUFFER");
        sensbuff_names_vision.add("DEPTH_BUFFER");
        sensbuff_names_vision.add("BATTERY_BUFFER");
       
        
/*        //Red buffer
        Codelet vision_red_c = new Sensor_ColorRed(oc.vision, sensbuff_names_vision.size(), sensbuff_names_vision, "VISION_RED",Buffersize,Vision_image_dimension);
        vision_red_c.addInput(vision_bufferMO);
        vision_red_c.addOutput(vision_redMO);
        insertCodelet(vision_red_c);

        //Green buffer
        Codelet vision_green_c = new Sensor_ColorGreen(oc.vision, sensbuff_names_vision.size(), sensbuff_names_vision, "VISION_GREEN",Buffersize,Vision_image_dimension);
        vision_green_c.addInput(vision_bufferMO);
        vision_green_c.addOutput(vision_greenMO);
        insertCodelet(vision_green_c);
        
        //Blue buffer
        Codelet vision_blue_c = new Sensor_ColorBlue(oc.vision, sensbuff_names_vision.size(), sensbuff_names_vision, "VISION_BLUE",Buffersize,Vision_image_dimension);
        vision_blue_c.addInput(vision_bufferMO);
        vision_blue_c.addOutput(vision_blueMO);
        insertCodelet(vision_blue_c);
  */      
        //Feature Maps bottom-up
        //Red FM
        Codelet vision_color_fm_c = new BU_FM_Color(oc.vision, sensbuff_names_vision.size(),sensbuff_names_vision,"VISION_COLOR_FM",Buffersize,Sensor_dimension);
        vision_color_fm_c.addInput(vision_bufferMO);
        vision_color_fm_c.addOutput(vision_color_fmMO);
        insertCodelet(vision_color_fm_c);

/*        //Green FM
        Codelet vision_green_fm_c = new BU_FM_ColorGreen(oc.vision, sensbuff_names_vision.size(), sensbuff_names_vision, "VISION_GREEN_FM",Buffersize,Sensor_dimension);
        vision_green_fm_c.addInput(vision_bufferMO);
        vision_green_fm_c.addOutput(vision_green_fmMO);
        insertCodelet(vision_green_fm_c);
        
        //Blue FM
        Codelet vision_blue_fm_c = new BU_FM_ColorBlue(oc.vision, sensbuff_names_vision.size(), sensbuff_names_vision, "VISION_BLUE_FM",Buffersize,Sensor_dimension);
        vision_blue_fm_c.addInput(vision_bufferMO);
        vision_blue_fm_c.addOutput(vision_blue_fmMO);
        insertCodelet(vision_blue_fm_c);
  */      
                
        //Depth FM
        Codelet depth_fm_c = new BU_FM_Depth(oc.vision, sensbuff_names_vision.size(),sensbuff_names_vision,"DEPTH_FM",Buffersize,Sensor_dimension);
        depth_fm_c.addInput(depth_bufferMO);
        depth_fm_c.addOutput(depth_fmMO);
        insertCodelet(depth_fm_c);
        
     // TOP DOWN
        Codelet vision_color_top_fm_c = new TD_FM_Color(oc.vision, sensbuff_names_vision.size(), sensbuff_names_vision, "VISION_COLOR_TOP_FM",Buffersize,Sensor_dimension);
        vision_color_top_fm_c.addInput(vision_bufferMO);
        vision_color_top_fm_c.addInput(winnersMO);
        vision_color_top_fm_c.addOutput(vision_color_top_fmMO);
        insertCodelet(vision_color_top_fm_c);
        
                
        //Depth FM
        Codelet depth_top_fm_c = new TD_FM_Depth(oc.vision, sensbuff_names_vision.size(),sensbuff_names_vision,"DEPTH_TOP_FM",Buffersize,Sensor_dimension);
        depth_top_fm_c.addInput(winnersMO);
        depth_top_fm_c.addInput(depth_bufferMO);
        depth_top_fm_c.addOutput(depth_top_fmMO);
        depth_top_fm_c.addOutput(vision_region_top_fmMO);
        insertCodelet(depth_top_fm_c);
     
        
        ArrayList<String> FMnames = new ArrayList<>();
        FMnames.add("VISION_COLOR_FM");
//        FMnames.add("VISION_GREEN_FM");
//        FMnames.add("VISION_BLUE_FM");
        FMnames.add("DEPTH_FM");
        FMnames.add("VISION_COLOR_TOP_FM");
        FMnames.add("DEPTH_TOP_FM");
        FMnames.add("REGION_TOP_FM");
        
        //CFM
        Codelet comb_fm_c = new CFM(oc.vision, FMnames.size(), FMnames,Buffersize,Sensor_dimension);
        comb_fm_c.addInput(vision_color_fmMO);
        comb_fm_c.addInput(depth_fmMO);
        comb_fm_c.addInput(vision_color_top_fmMO);
        comb_fm_c.addInput(depth_top_fmMO);
        comb_fm_c.addInput(vision_region_top_fmMO);
        comb_fm_c.addInput(weightsMO);
        comb_fm_c.addOutput(combFMMO);
        comb_fm_c.addOutput(type_fmMO);
        insertCodelet(comb_fm_c);
        
        //SALIENCY MAP CODELET
        Codelet sal_map_cod = new SalMap(oc.vision, "SALIENCY_MAP", "COMB_FM", "ATTENTIONAL_MAP", Buffersize, Sensor_dimension);
        sal_map_cod.addInput(combFMMO);
        sal_map_cod.addInput(attMapMO);
        sal_map_cod.addOutput(salMapMO);
        insertCodelet(sal_map_cod);
        
        //DECISION MAKING CODELET
        Codelet dec_mak_cod = new DecisionMaking(oc.vision, "WINNERS", "ATTENTIONAL_MAP", "SALIENCY_MAP", Buffersize, Sensor_dimension);
        dec_mak_cod.addInput(salMapMO);
        dec_mak_cod.addInput(type_fmMO);
        dec_mak_cod.addOutput(winnersMO);
        dec_mak_cod.addOutput(attMapMO);
        insertCodelet(dec_mak_cod);
        
        //LEARNER CODELET
        Codelet learner_cod = new LearnerCodelet(oc, Buffersize, Sensor_dimension, mode, motivation);
        learner_cod.addInput(salMapMO);
        learner_cod.addInput(winnersMO);
        learner_cod.addInput(vision_color_fmMO);
        learner_cod.addInput(depth_fmMO);
        learner_cod.addInput(vision_color_top_fmMO);
        learner_cod.addInput(depth_top_fmMO);
        learner_cod.addInput(proceduralMO);
        
        if(motivation.equals("drives")){
            learner_cod.addInput(motivationMC);
//            learner_cod.addInput(hunger_motivationMO);
          }
        
        learner_cod.addInput(battery_bufferMO);
        learner_cod.addOutputs(motorMOs);
        learner_cod.addOutput(actionsMO);
        learner_cod.addOutput(statesMO);
        learner_cod.addOutput(rewardsMO);
        learner_cod.addOutput(action_saverMO);
        learner_cod.addOutput(reward_saverMO);        
        insertCodelet(learner_cod);
        
        
        // Assimilation
        Codelet assimilation_cod = new AssimilationCodelet(oc);
        assimilation_cod.addInput(actionsMO);
        assimilation_cod.addInput(statesMO);
        assimilation_cod.addInput(rewardsMO);
        assimilation_cod.addOutput(proceduralMO);
        insertCodelet(assimilation_cod);
        
        // Acommodation
        Codelet acommodation_cod = new AcommodationCodelet(oc);
        acommodation_cod.addInput(actionsMO);
        acommodation_cod.addInput(statesMO);
        acommodation_cod.addInput(rewardsMO);
        acommodation_cod.addOutput(proceduralMO);
        insertCodelet(acommodation_cod);
        
        if(motivation.equals("drives")){
            // Motivation
            Codelet curiosity_motivation_cod = new CuriosityDrive_MotivationCodelet("Curiosity_Motivation", 0.0, 0.0, 0.0, oc);
            curiosity_motivation_cod.addInput(actionsMO);
            curiosity_motivation_cod.addInput(rewardsMO);
            curiosity_motivation_cod.addOutput(motivationMC);
            insertCodelet(curiosity_motivation_cod);

            Codelet hunger_motivation_cod = new HungerDrive_MotivationCodelet("Hunger_Motivation", 0.0, 0.0, 0.0, oc);
            hunger_motivation_cod.addInput(battery_bufferMO);
            hunger_motivation_cod.addOutput(motivationMC);
            insertCodelet(hunger_motivation_cod);
        }
        
        // SAVER CODELETS
        
        Codelet action_saver = new saverCodelet(50, "actions.txt");
        action_saver.addInput(action_saverMO);
        insertCodelet(action_saver);
        
        Codelet reward_saver = new saverCodelet(10, "rewards.txt");
        reward_saver.addInput(reward_saverMO);
        insertCodelet(reward_saver);
        
        ///////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////
        
        // NOTE Sets the time interval between the readings
        // sets a time step for running the codelets to avoid heating too much your machine
        for (Codelet c : this.getCodeRack().getAllCodelets())
            c.setTimeStep(200);
	
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        
     
	// Start Cognitive Cycle
	start(); 
	
    }
}
