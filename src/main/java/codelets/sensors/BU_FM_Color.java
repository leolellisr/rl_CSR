/*
 * /*******************************************************************************
 *  * Copyright (c) 2012  DCA-FEEC-UNICAMP
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the GNU Lesser Public License v3
 *  * which accompanies this vision_redribution, and is available at
 *  * http://www.gnu.org/licenses/lgpl.html
 *  * 
 *  * Contributors:
 *  *     K. Raizer, A. L. O. Paraense, R. R. Gudwin - initial API and implementation
 *  ******************************************************************************/
 
package codelets.sensors;

import CommunicationInterface.SensorI;
import br.unicamp.cst.core.entities.MemoryObject;
import sensory.FeatMapCodelet;
//import codelets.motor.Lock;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
//import static java.lang.Math.abs;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author L. M. Berto
 * @author L. L. Rossi (leolellisr)
 */
public class BU_FM_Color extends FeatMapCodelet {
    private final float mr = 255;                     //Max Value for VisionSensor
    private final int max_time_graph=100;
    private final int res = 256;                     //Resolution of VisionSensor
    private int time_graph;
    private final int slices = 16, print_step;                    //Slices in each coordinate (x & y) 
    private SensorI vision;
    private ArrayList<Float> vision_redFM_t;
    private ArrayList<Float> vision_greenFM_t;
    private ArrayList<Float> vision_blueFM_t;
    private boolean debug = false;
    public BU_FM_Color(SensorI vision, int nsensors, ArrayList<String> sens_names, String featmapname,
            int timeWin, int mapDim, int print_step) {
        super(nsensors, sens_names, featmapname,timeWin,mapDim);
        this.time_graph = 0;
        this.vision = vision;
        this.print_step=print_step;
    }

    private ArrayList<Float>  set_resize_Image(float mean_all, ArrayList<Float> visionData_Array){
        
        Float Fvalue;
        float MeanValue = 0;
        ArrayList<Float> vision_mean = new ArrayList<>();
         //Converts res*res image to res/slices*res/slices sensors
        float new_res = (res/slices)*(res/slices);
        float new_res_1_2 = (res/slices);
        
        for(int n = 0;n<slices;n++){
            int ni = (int) (n*new_res_1_2);
            int no = (int) (new_res_1_2+n*new_res_1_2);
            for(int m = 0;m<slices;m++){    
                int mi = (int) (m*new_res_1_2);
                int mo = (int) (new_res_1_2+m*new_res_1_2);
                for (int y = ni; y < no; y++) {
                    for (int x = mi; x < mo; x++) {
                        Fvalue = visionData_Array.get(y*res+x);                         
                        MeanValue += Fvalue;
                        
                    }
                }
                float correct_mean = MeanValue/new_res - mean_all;
                if(correct_mean/mr>1) vision_mean.add(new Float(1));
                else if(correct_mean/mr<0.001) vision_mean.add(new Float(0));
                else vision_mean.add(correct_mean/mr);     
                
                MeanValue = 0;
                
            }
        }
        return vision_mean;
    }
    
    @Override
    public void calculateActivation() {
        
    }

    @Override
    public void proc() {
        try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        
        MemoryObject vision_bufferMO = (MemoryObject) sensor_buffers.get(0);        //Gets vision Data
        
        List visionData_buffer;
        visionData_buffer = (List) vision_bufferMO.getI();
        
        List vision_FM = (List) featureMap.getI();  
        if(debug) System.out.println("vision_FM begin: "+vision_FM.size());

        
        List vision_redFM = (List) vision_FM.get(0); // Get red data
        List vision_greenFM = (List) vision_FM.get(1); // Get green data
        List vision_blueFM = (List) vision_FM.get(2); // Get blue data
        
        if(vision_redFM.size() == timeWindow) vision_redFM.remove(0);
        if(vision_greenFM.size() == timeWindow) vision_greenFM.remove(0);
        if(vision_blueFM.size() == timeWindow) vision_blueFM.remove(0);
        
        
        vision_redFM.add(new ArrayList<>());
        vision_greenFM.add(new ArrayList<>());
        vision_blueFM.add(new ArrayList<>());
        
        int t = vision_redFM.size()-1;

        vision_redFM_t = (ArrayList<Float>) vision_redFM.get(t);
        vision_greenFM_t = (ArrayList<Float>) vision_greenFM.get(t);
        vision_blueFM_t = (ArrayList<Float>) vision_blueFM.get(t);
        
        for (int j = 0; j < mapDimension; j++) {
            vision_redFM_t.add(new Float(0));
            vision_greenFM_t.add(new Float(0));
            vision_blueFM_t.add(new Float(0));
        }
        
        MemoryObject visionDataMO;
        
        if(visionData_buffer.size() < 1){
            return;
        }

        visionDataMO = (MemoryObject)visionData_buffer.get(visionData_buffer.size()-1);

        List visionData;

        visionData = (List) visionDataMO.getI();
        
        Float Fvalue;
        
        ArrayList<Float> visionRedData_Array = new ArrayList<>();
        ArrayList<Float> visionGreenData_Array = new ArrayList<>();
        ArrayList<Float> visionBlueData_Array = new ArrayList<>();
        for (int j = 0; j < res*res; j++) {
            visionRedData_Array.add(new Float(0));
            visionGreenData_Array.add(new Float(0));
            visionBlueData_Array.add(new Float(0));
        }
        
        int pixel_len = 3;
        int count_3 = 0;
        for (int j = 0; j+pixel_len < visionData.size(); j+= pixel_len) {
           
            Fvalue = (Float) visionData.get(j);               //Gets Red VisionData
            visionRedData_Array.set(count_3, Fvalue);        //Red data
            Fvalue = (Float) visionData.get(j+1);           //Gets Green VisionData
            visionGreenData_Array.set(count_3, Fvalue);        //Green data
            Fvalue = (Float) visionData.get(j+2);               //Gets Blue VisionData
            visionBlueData_Array.set(count_3, Fvalue);        //Blue data
            count_3 += 1;
        }
        //System.out.println("Vision r size:"+visionData_Array.size());
/*        printToFile(visionRedData_Array, "vision_red.txt");
        printToFile(visionGreenData_Array, "vision_green.txt");
        printToFile(visionBlueData_Array, "vision_blue.txt");
  */      
        // get mean all elements
        float sumR = 0, sumG = 0, sumB = 0;
        for (float value : visionRedData_Array) {
            sumR += value;
        }
        for (float value : visionGreenData_Array) {
            sumG += value;
        }
        for (float value : visionBlueData_Array) {
            sumB += value;
        }
        
        float mean_all_R = sumR / visionRedData_Array.size();
        float mean_all_G = sumG / visionGreenData_Array.size();
        float mean_all_B = sumB / visionBlueData_Array.size();
        
        ArrayList<Float> vision_mean_red = set_resize_Image(mean_all_R, visionRedData_Array);
        ArrayList<Float> vision_mean_green = set_resize_Image(mean_all_G, visionGreenData_Array);
        ArrayList<Float> vision_mean_blue = set_resize_Image(mean_all_B, visionBlueData_Array);
        
        for (int j = 0; j < vision_mean_red.size(); j++) {
           
            vision_redFM_t.set(j, vision_mean_red.get(j));
        }   
//         printToFile(vision_redFM_t, "vision_red_FM.txt");
         
        for (int j = 0; j < vision_mean_green.size(); j++) {
           
            vision_greenFM_t.set(j, vision_mean_green.get(j));
        }
   //      printToFile(vision_greenFM_t, "vision_green_FM.txt");
         
        for (int j = 0; j < vision_mean_blue.size(); j++) {
           
            vision_blueFM_t.set(j, vision_mean_blue.get(j));
        }
     //   printToFile(vision_blueFM_t, "vision_blue_FM.txt");
       
       vision_redFM.set(t, vision_redFM_t);
       vision_greenFM.set(t, vision_greenFM_t);
       vision_blueFM.set(t, vision_blueFM_t);
       
       vision_FM.set(0,vision_redFM);
       vision_FM.set(1,vision_greenFM);
       vision_FM.set(2,vision_blueFM);
       
        featureMap.setI(vision_FM);
        if(debug) System.out.println("vision_FM end: "+vision_FM.size());
    }
    
    /*private void printToFile(ArrayList<Float> arr, String title){
        if(this.vision.getExp() == 1 || this.vision.getExp()%print_step == 0){
             //if(time_graph%2 == 0 ){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
            LocalDateTime now = LocalDateTime.now(); 
            try(FileWriter fw = new FileWriter("profile/"+title, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(dtf.format(now)+"_"+this.vision.getExp()+"_"+time_graph+" "+ arr);
                time_graph++;
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
}
    

