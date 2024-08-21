
import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2
goal_id_img = '11'
goal_id_v = '44'
res = 256
slices = 16
debug = False


new_res_1_2 = (res/slices)


def map_data_fm(data):
    fm_array = np.zeros(res*res)
    for line in data: 
        col = line.split(' ') 
        id = col[0]
        last_id_a = id.split('_')
        last_id = last_id_a[6]
        if last_id == goal_id_v:
            new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)
            col = new_line.split(' ')    
            col_sem_id = col[1:]
            col_sem_id_np_st = np.array(col_sem_id)
            col_sem_id_np_fl = col_sem_id_np_st.astype(np.float)
            break
    if(debug):
        print(col_sem_id_np_fl)
        print(f"len  col_sem_id_np_fl: {len(col_sem_id_np_fl)}")
    
    for n in range(slices):
        ni = int(n*new_res_1_2)
        no = int(new_res_1_2+n*new_res_1_2)
            
        for m in range(slices):    
            mi = int(m*new_res_1_2)
            mo = int(new_res_1_2+m*new_res_1_2)
            
            for y in range(ni, no):
                for x in range(mi, mo):
                    
                    if(debug): print(f" n: {n} m: {m} y: {y} x: {x} n*slices+m: {n*slices+m}")
    
                    fm_array[y*res+x] = col_sem_id_np_fl[n*slices+m]
                    if(debug):
                        if(col_sem_id_np_fl[n*slices+m]>0): print(f"Value col_sem_id: {col_sem_id_np_fl[n*slices+m]}")
                        if(fm_array[y*res+x]>0): print(f"fm_array: {fm_array[y*res+x]}")
                    
    if(debug): print(f"len  fm_array: {len(fm_array)}")
    
    with os.scandir('data/') as entries:
        for entry in entries:
            last_id_a = entry.name.split('_')
            last_id = last_id_a[6]
            if last_id == goal_id_img:
                img_path = "data/"+entry.name
                img = cv2.imread(img_path)
                print(img.shape)

                # find max to normalize
                #col_sem_id_np_max = max(col_sem_id_np_fl)
                
                fm_array = fm_array*255
                fm_array = fm_array.astype(np.uint8)
                fm_array = fm_array.reshape((res,res,1))
                print(fm_array.shape)
                
                # Overlay the map on the image
                fm_array = cv2.applyColorMap(fm_array, cv2.COLORMAP_HOT)
                cv2.imshow('col_sem_id_np_fl', fm_array)
                cv2.waitKey(0)
                result = cv2.addWeighted(img, 0.6, fm_array, 0.4, 0)
                cv2.imshow('img', result)
                cv2.waitKey(0)

                if(debug): print("ploted")
                break
if(debug): print("fim visionFM")

#VISION FM
with open("vision_red_FM.txt","r") as f: # Open file
    data = f.readlines()
    map_data_fm(data)