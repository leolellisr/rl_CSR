
import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2
goal_id_img = '11'
goal_id_v = '44'
res = 256
#VISION DATAq
with open("vision_red.txt","r") as f: # Open file
    data = f.readlines()
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
                col_sem_id_np_fl = col_sem_id_np_fl*255
                # Resize the map
                #col_sem_id_np_fl = cv2.resize(col_sem_id_np_fl, (img.shape[0], img.shape[1]))
                col_sem_id_np_fl = col_sem_id_np_fl.astype(np.uint8)
                col_sem_id_np_fl = col_sem_id_np_fl.reshape((res,res,1))
                print(col_sem_id_np_fl.shape)
                # Overlay the map on the image
                col_sem_id_np_fl = cv2.applyColorMap(col_sem_id_np_fl, cv2.COLORMAP_HOT)
                cv2.imshow('col_sem_id_np_fl', col_sem_id_np_fl)
                cv2.waitKey(0)
                result = cv2.addWeighted(img, 0.6, col_sem_id_np_fl, 0.4, 0)
                cv2.imshow('img', result)
                cv2.waitKey(0)
                
                #fig, ax = plt.subplots()
                #ax.imshow(result)
                print("ploted")
                break
print("fim vision")