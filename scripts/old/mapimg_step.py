# Colormaps: https://learnopencv.com/applycolormap-for-pseudocoloring-in-opencv-c-python/

import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2

debug = False
debugmap = False

res = 256
slices = 16
new_res_1_2 = (res/slices)

def map_data(mode, file, goal_id_v, goal_id_img):
    with open(file,"r") as f: # Open file
        name = file.split('.')[0] 
        data = f.readlines()
        
        for line in data: 
            col = line.split(' ') 
            id = col[0]
            id_array = id.split('_')
            if debug: print(f"Array len: {len(id_array)}")
            if len(id_array) == 7:
                last_id = id_array[6]
                if last_id == goal_id_v:
                    if(debug): print("Name: "+name)
                    new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)
                    col = new_line.split(' ')    
                    col_sem_id = col[1:]
                    col_sem_id_np_st = np.array(col_sem_id)
                    col_sem_id_np_fl = col_sem_id_np_st.astype(np.float)
                    if name == "attMap": 
                        #col_sem_id_np_fl = col_sem_id_np_fl / np.sqrt(np.sum(col_sem_id_np_fl**2))
                        col_sem_id_np_fl = [ x-1 for x in col_sem_id_np_fl]
                        #print(col_sem_id_np_fl)
                        col_sem_id_np_max = max(col_sem_id_np_fl)                
                        if(col_sem_id_np_max>0): col_sem_id_np_fl = col_sem_id_np_fl/col_sem_id_np_max
                    break
        if(debug):
            print(col_sem_id_np_fl)
            print(f"len  line / number of col: {len(col_sem_id_np_fl)}")

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
                            if(debug):
                                if(col_sem_id_np_fl[n*slices+m]>0): print(f"Value col_sem_id: {col_sem_id_np_fl[n*slices+m]}")
                                if(fm_array[y*res+x]>0): print(f"fm_array: {fm_array[y*res+x]}")
                            
            if(debug): print(f"len  Array: {len(fm_array)}")


        with os.scandir('data/') as entries:
            for entry in entries:
                last_id_a = entry.name.split('_')
                if debug: print(len(last_id_a))
                if len(last_id_a) == 8:
                    last_id = last_id_a[6]
                    if last_id == goal_id_img:
                        img_path = "data/"+entry.name
                        img = cv2.imread(img_path)
                        if debugmap: print(img.shape)

                        # find max to normalize
                        #col_sem_id_np_max = max(col_sem_id_np_fl)
                        if mode == "sensor" and name == "depth": fm_array = fm_array*255
                        elif mode == "fm": fm_array = fm_array*255
                        # Resize the map
                        #fm_array = cv2.resize(fm_array, (img.shape[0], img.shape[1]))
                        fm_array = fm_array.astype(np.uint8)
                        fm_array = fm_array.reshape((res,res,1))
                        if debugmap: print(fm_array.shape)
                        # Overlay the map on the image
                        fm_array = cv2.applyColorMap(fm_array, cv2.COLORMAP_JET)
                        cv2.imshow('colormap'+name, fm_array)
                        cv2.waitKey(0)
                        result = cv2.addWeighted(img, 0.6, fm_array, 0.4, 0)
                        cv2.imshow('img'+name, result)
                        cv2.waitKey(0)
                        
                        #fig, ax = plt.subplots()
                        #ax.imshow(result)
                        if(debug): print("ploted")
                        break
    f.close()
    if(debug): print("fim")



#img step = txt_vs_step /4,81
txt_vs_step = "371"
txt_step = "87"
img_step = "87"

map_data("sensor", "vision_red.txt", txt_vs_step, img_step)
#map_data("sensor", "vision_green.txt", txt_step, img_step)
#map_data("sensor", "vision_blue.txt", txt_step, img_step)
map_data("sensor", "depth.txt", txt_step, img_step)
map_data("fm", "vision_red_FM.txt", txt_step, img_step)
#map_data("fm", "vision_green_FM.txt", txt_step, img_step)
#map_data("fm", "vision_blue_FM.txt", txt_step, img_step)
map_data("fm", "depth_FM.txt", txt_step, img_step)
map_data("fm","vision_top_red_FM.txt", txt_step, img_step)
#map_data("fm", "vision_top_green_FM.txt", txt_step, img_step)
#map_data("fm", "vision_top_blue_FM.txt", txt_step, img_step)
map_data("fm", "depth_top_FM.txt", txt_step, img_step)

#CFM
map_data("fm", "cfm.txt", "394", img_step)

#salmap
map_data("fm", "salmap.txt", "435", img_step)