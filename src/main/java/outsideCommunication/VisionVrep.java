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
 
package outsideCommunication;

import CommunicationInterface.SensorI;
import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.IntWA;

import coppelia.IntW;
import coppelia.remoteApi;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
 import java.util.List;   
import java.util.Collections; 

import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;   

import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;    

/**
 *
 * @author L. M. Berto
 * @author L. L. Rossi (leolellisr)
 */
public class VisionVrep implements SensorI{
    private final IntW vision_handles;
    private final remoteApi vrep;
    private final int clientID; 
    private  int time_graph;
    private List<Float> vision_data;   
    private int stage, num_exp, num_exp_s, num_exp_c;    
    private final int res = 256;
    private final int max_time_graph=100;
    private static final int MAX_ACTION_NUMBER = 500;
	private boolean debug = false;
    private int max_epochs;
    public VisionVrep(remoteApi vrep, int clientid, IntW vision_handles, int max_epochs) {
        this.time_graph = 0;
        vision_data = Collections.synchronizedList(new ArrayList<>(res*res*3));
        this.vrep = vrep;
        this.stage =3;
       this.num_exp = 194;
        this.num_exp_c = 1;
        this.num_exp_s = 1;
        
        this.vision_handles = vision_handles;
        clientID = clientid;
        this.max_epochs = max_epochs;
        for (int i = 0; i < res*res*3; i++) {
            vision_data.add(0f);
        }    
    }
    
    private void setResizedColorData(char[] pixels_red, char[] pixels_green, char[] pixels_blue, int f){
	float MeanValue_r = 0;
        float MeanValue_g = 0;
        float MeanValue_b = 0;
        for(int n = 0;n<res/f;n++){
            int ni = (int) (n*f);
            int no = (int) (f+n*f);
            for(int m = 0;m<res/f;m++){    
                int mi = (int) (m*f);
                int mo = (int) (f+m*f);
                for (int y = ni; y < no; y++) {
                    for (int x = mi; x < mo; x++) {
                        float Fvalue_r = (float) pixels_red[y*res+x];     
                        MeanValue_r += Fvalue_r;
                        float Fvalue_g = (float) pixels_green[y*res+x];     
                        MeanValue_g += Fvalue_g;
                        float Fvalue_b = (float) pixels_blue[y*res+x];     
                        MeanValue_b += Fvalue_b;
                    }
                }
                float correct_mean_r = MeanValue_r/(f*f);
                float correct_mean_g = MeanValue_g/(f*f);
                float correct_mean_b = MeanValue_b/(f*f);
                for (int y = ni; y < no; y++) {
                    for (int x = mi; x < mo; x++) {
                        vision_data.set(3*(y*res+x), correct_mean_r);
                        vision_data.set(3*(y*res+x)+1, correct_mean_g);
                        vision_data.set(3*(y*res+x)+2, correct_mean_b);
                    }
                }
                        //System.out.println("Mean_r: "+ correct_mean_r + " Mean_g: "+ correct_mean_g +" Mean_b: "+ correct_mean_b +" ni: "+ni+" no: "+no+" mi: "+mi+" mo: "+mo);
                MeanValue_r = 0;
                MeanValue_g = 0;
                MeanValue_b = 0;
            }
        }         
    }
    @Override
    public boolean endExp(){
        FloatWA position = new FloatWA(3);
	vrep.simxGetObjectPosition(clientID, vision_handles.getValue(), -1, position,
        vrep.simx_opmode_streaming);
		
//	printToFile(position.getArray()[2], "positions.txt");
        if(debug) System.out.println("Marta on exp "+this.getExp()+" with z = "+position.getArray()[2]);        
        if (this.getExp() > 1 && (position.getArray()[2] < 0.35 || position.getArray()[0] > 0.2)) {
            System.out.println("Marta crashed on exp "+this.getExp()+" with z = "+position.getArray()[2]);
                            
            vrep.simxPauseCommunication(clientID, true);
            vrep.simxStopSimulation(clientID, vrep.simx_opmode_oneshot_wait);
            try {
			Thread.sleep(20);
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		}
            vrep.simxPauseCommunication(clientID, false);
            vrep.simxStartSimulation(clientID, remoteApi.simx_opmode_oneshot_wait);
            try {
			Thread.sleep(200);
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		}  
            
        }
        return this.getExp() > 1 && (position.getArray()[2] < 0.35 || position.getArray()[0] > 0.2);
    }
    
    @Override
    public int getMaxActions() {
            
        return  MAX_ACTION_NUMBER;
    }
    
    @Override
    public int getMaxExp() {
            
        return  this.max_epochs;
    }
    
    @Override
    public int getExp() {
        return this.num_exp;    
    }
    
    public int getExp(String s) {
        if(s.equals("C"))  return this.num_exp_c;    
        else if(s.equals("S")) return this.num_exp_s;
        return 0;
    }
    
    @Override
    public void setExp(int newExp) {
       this.num_exp = newExp;    
    }
    
    public void setExp(int newExp, String s) {
       if(s.equals("C")) this.num_exp_c = newExp;
       else if(s.equals("S")) this.num_exp_s = newExp;
    }
    
    
    @Override
    public int getStage() {
        return this.stage;    
    }
    
    @Override
    public void setStage(int newstage) {
       this.stage = newstage;    
    }
    
    @Override
    public Object getData() {
       try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }

        char temp_RGB[];                            //char Array to get RGB data of Vision Sensor
        
        CharWA image_RGB = new CharWA(res*res*3);           //CharWA that returns RGB data of Vision Sensor
        IntWA resolution = new IntWA(2);            //Array to get resolution of Vision Sensor
        
        long startTime = System.currentTimeMillis();
        
        int ret_RGB = vrep.simxGetVisionSensorImage(clientID, vision_handles.getValue(), resolution, image_RGB, 0, vrep.simx_opmode_streaming); 
        
        while (System.currentTimeMillis()-startTime < 2000)
        {
            ret_RGB = vrep.simxGetVisionSensorImage(clientID, vision_handles.getValue(), resolution, image_RGB, 0, remoteApi.simx_opmode_buffer);
            if (ret_RGB == remoteApi.simx_return_ok  || ret_RGB == remoteApi.simx_return_novalue_flag){
                
                int count_aux = 0; 
                temp_RGB = image_RGB.getArray();
                char[] pixels_red = new char[res*res];
                char[] pixels_green = new char[res*res];
                char[] pixels_blue = new char[res*res];
                
                
                for(int y =0; y < res; y++){  
                    for(int x =0; x < res; x++){  
                        char pixel_red = temp_RGB[3*(y*res+x)];
                        char pixel_green = temp_RGB[3*(y*res+x)+1];
                        char pixel_blue = temp_RGB[3*(y*res+x)+2];
                        pixels_red[count_aux]=pixel_red;
                        pixels_green[count_aux]=pixel_green;
                        pixels_blue[count_aux]=pixel_blue;
                        count_aux += 1;
                    } 
                }
                if(stage==3){
                    int pixel_len = 3;
                    int cont_pix = 0;
                    for(int i =0; i < res*res; i++){
                        vision_data.set(cont_pix, (float)pixels_red[i]);
                        vision_data.set(cont_pix+1, (float)pixels_green[i]);
                        vision_data.set(cont_pix+2, (float)pixels_blue[i]);
                        cont_pix += pixel_len;
                         
                    }
                }
                
                if(stage==2) setResizedColorData(pixels_red, pixels_green, pixels_blue, 2);
                if(stage==1) setResizedColorData(pixels_red, pixels_green, pixels_blue, 4);
                
            } else{
                int count_aux = 0; 
                for(int y =0; y < res; y++){  
                    for(int x =0; x < res; x++){  
                        vision_data.set(count_aux, new Float(0));
                        vision_data.set(count_aux+1, new Float(0));
                        vision_data.set(count_aux+2, new Float(0));
                        count_aux += 3;
                    }
                }
            }

        
        
        }
        
        // SYNC

       // printToFile(vision_data);        
        return  vision_data;
    }
    
   /* private void printToFile(Object object){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
        LocalDateTime now = LocalDateTime.now();  
        try(FileWriter fw = new FileWriter("profile/vision.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)){
            out.println(dtf.format(now)+"_"+time_graph+" "+ object);
            time_graph++;
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        IntWA resolution = new IntWA(2);            //Array to get resolution of Vision Sensor
        int ret;                                        //Return of RemoteAPI
        CharWA image = new CharWA(res*res);              //CharWA that returns GRAYSCALED data of Vision Sensor
        char temp[];                                //char Array to get GRAYSCALED data of Vision Sensor
        
        
        ret = vrep.simxGetVisionSensorImage(clientID, vision_handles.getValue(), resolution, image, 1, remoteApi.simx_opmode_buffer);
        temp = image.getArray();
        if(ret == remoteApi.simx_return_ok  || ret == remoteApi.simx_return_novalue_flag){          
            byte[] byteMama = new byte[temp.length];
            for(int bma=0;bma<temp.length;bma++){
                byteMama[bma]= (byte) temp[bma];
            }
            BufferedImage convertedGrayscale = new BufferedImage(res, res, BufferedImage.TYPE_BYTE_GRAY);
            convertedGrayscale.getRaster().setDataElements(0, 0, res, res, byteMama);
            String outputimage = "data/"+dtf.format(now)+"_"+this.num_exp+"_"+time_graph+".jpg";
            try{
                ImageIO.write(convertedGrayscale, "jpg", new File(outputimage) );
            }
            catch(Exception e){
                String erro = e.toString();
                System.out.println(erro);
            }
        }
    }
*/
	@Override
	public void resetData() {
		// TODO Auto-generated method stub
		
	}

    @Override
    public int getAux() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
