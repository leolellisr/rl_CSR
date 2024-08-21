# Colormaps: https://learnopencv.com/applycolormap-for-pseudocoloring-in-opencv-c-python/

import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2

debug = False
debugmap = False
saving_mode = True
printing_mode = False

res = 256
slices = 16
new_res_1_2 = (res/slices)

def map_data(mode, file, goal_time, goal_timeimg, img):
    with open(file,"r") as f: # Open file
        name = file.split('.')[0] 
        last_name = name.split('/')[2] 
        data = f.readlines()
        
        for line in data: 
            col = line.split(' ') 
            id = col[0]
            id_array = id.split('_')
            goal_array = goal_time.split('_')
            if debug and debugmap: print(f"Array len: {len(id_array)}")
            if len(id_array) == 7 and goal_array[0] == id_array[0] and goal_array[1] == id_array[1] and goal_array[2] == id_array[2] and goal_array[3] == id_array[3] and goal_array[4] == id_array[4] and goal_array[5] == id_array[5]:                    
                new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)
                col = new_line.split(' ')    
                col_sem_id = col[1:]
                col_sem_id_np_st = np.array(col_sem_id)
                col_sem_id_np_fl = col_sem_id_np_st.astype(np.float)
                if last_name == "attMap": 
                    #col_sem_id_np_fl = col_sem_id_np_fl / np.sqrt(np.sum(col_sem_id_np_fl**2))
                    col_sem_id_np_fl = [ x-1 for x in col_sem_id_np_fl]
                        #print(col_sem_id_np_fl)
                    col_sem_id_np_max = max(col_sem_id_np_fl)                
                    if(col_sem_id_np_max>0): col_sem_id_np_fl = col_sem_id_np_fl/col_sem_id_np_max
                break
                if(debug): print(f"got array {last_name} array len: {len(col_sem_id_np_fl)}")

            #print(col_sem_id_np_fl)

        if mode == "sensor": fm_array = col_sem_id_np_fl
        elif mode == "fm":
            fm_array = np.zeros(res*res)
            for n in range(slices):
                ni = int(n*new_res_1_2)
                no = int(new_res_1_2+n*new_res_1_2)
                for m in range(slices):    
                    mi = int(m*new_res_1_2)
                    mo = int(new_res_1_2+m*new_res_1_2)                   
                    for y in range(ni, no):
                        for x in range(mi, mo):                           
                            if(debug and debugmap ): print(f" n: {n} m: {m} y: {y} x: {x} n*slices+m: {n*slices+m}")           
                            fm_array[y*res+x] = col_sem_id_np_fl[n*slices+m]
                            if(debug and debugmap):
                                if(col_sem_id_np_fl[n*slices+m]>0): print(f"Value col_sem_id: {col_sem_id_np_fl[n*slices+m]}")
                                if(fm_array[y*res+x]>0): print(f"fm_array: {fm_array[y*res+x]}")                           
            if(debug and debugmap ): print(f"len  Array: {len(fm_array)}")

    f.close()
    
                    # find max to normalize
                        #col_sem_id_np_max = max(col_sem_id_np_fl)
    if mode == "sensor" and last_name == "depth": fm_array = fm_array*255
    elif mode == "fm": fm_array = fm_array*255
                        # Resize the map
                        #fm_array = cv2.resize(fm_array, (img.shape[0], img.shape[1]))
    fm_array = fm_array.astype(np.uint8)
    fm_array = fm_array.reshape((res,res,1))
    if debugmap: print(f"img shape: {fm_array.shape}")
                        # Overlay the map on the image
    fm_array = cv2.applyColorMap(fm_array, cv2.COLORMAP_JET)
    if printing_mode: 
        cv2.imshow('colormap'+last_name, fm_array)
        cv2.waitKey(0)
        print("printed colormap"+last_name)

    result = cv2.addWeighted(img, 0.6, fm_array, 0.4, 0)
    fliped = cv2.flip(result, 0)            

    if mode == "sensor" and last_name != "depth":   
        img_path_leg = "results/legend_vis.jpg"
    elif mode == "sensor" and last_name == "depth": 
        img_path_leg = "results/legend_dep.jpg"
    elif mode == "fm": 
        img_path_leg = "results/legend_fm.jpg"

    img_leg = cv2.imread(img_path_leg)   
    final_img = np.concatenate((fliped, img_leg), axis=1)     
    if saving_mode: cv2.imwrite('results/'+goal_time+'_'+last_name+'.jpg', final_img) 

    if printing_mode:
        cv2.imshow('result_'+last_name, fliped)
        cv2.waitKey(0)

    if debug: print("ploted"+last_name)


#img step = txt_vs_step /4,81
goal_time_img = "2021_03_07_22_15_55_56"
goal_time_red = "2021_03_07_22_15_55_56"
goal_time_green = "2021_03_07_22_16_01_260"
goal_time_blue = "2021_03_07_22_15_55_56"
goal_time_depth = "2021_03_07_22_15_43_57"
goal_time_att =  "2021_03_07_22_15_55_56"

 
aux_img = 0
with os.scandir('data/') as entries:
    for entry in entries:
        
        id_array = entry.name.split('_')
            #if debug: print(f"len name img: {len(id_array)}")
        if len(id_array) == 7:
                    # last_id = last_id_a[6]
            goal_array = goal_time_img.split('_')
            if goal_array[0] == id_array[0] and goal_array[1] == id_array[1] and goal_array[2] == id_array[2] and goal_array[3] == id_array[3] and goal_array[4] == id_array[4] and goal_array[5] == id_array[5]:
                img_path = "data/"+entry.name
                img = cv2.imread(img_path)
                aux_img += 1
                if debugmap: print(f"img shape: {img.shape}")
                break
if aux_img > 0:
    map_data("sensor", "results/txt_last_exp/vision_red.txt", goal_time_red, goal_time_img, img)
    map_data("sensor", "results/txt_last_exp/vision_green.txt", goal_time_green, goal_time_img, img)
    map_data("sensor", "results/txt_last_exp/vision_blue.txt", goal_time_blue, goal_time_img, img)
    map_data("sensor", "results/txt_last_exp/depth.txt", goal_time_depth, goal_time_img, img)
    map_data("fm", "results/txt_last_exp/vision_red_FM.txt", goal_time_red, goal_time_img, img)
    map_data("fm", "results/txt_last_exp/vision_green_FM.txt", goal_time_green, goal_time_img, img)
    map_data("fm", "results/txt_last_exp/vision_blue_FM.txt", goal_time_blue, goal_time_img, img)
    map_data("fm", "results/txt_last_exp/depth_FM.txt", goal_time_depth, goal_time_img, img)
    map_data("fm","results/txt_last_exp/vision_top_red_FM.txt", goal_time_red, goal_time_img, img)
    map_data("fm", "results/txt_last_exp/vision_top_green_FM.txt",goal_time_green, goal_time_img, img)
    map_data("fm", "results/txt_last_exp/vision_top_blue_FM.txt",goal_time_blue, goal_time_img, img)
    map_data("fm", "results/txt_last_exp/depth_top_FM.txt", goal_time_depth, goal_time_img, img)

#CFM
    map_data("fm", "results/txt_last_exp/cfm.txt", goal_time_green, goal_time_img, img)

#salmap
    map_data("fm", "results/txt_last_exp/salmap.txt", goal_time_green, goal_time_img, img)

#att map: só tem instantes impares
    map_data("fm", "results/txt_last_exp/attMap.txt", goal_time_att, goal_time_img, img)
print("fim")