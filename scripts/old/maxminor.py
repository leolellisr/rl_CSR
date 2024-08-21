
import re
import numpy as np
import matplotlib.pyplot as plt
import os
goal_id = '27'
#VISION DATA
with open("vision.txt","r") as f: # Open file
    data = f.readlines()
    for line in data: 
        col = line.split(' ') 
        id = col[0]
        last_id_a = id.split('_')
        last_id = last_id_a[6]
        if last_id == goal_id:
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
            if last_id == goal_id:
                img_path = "data/"+entry.name

print("fim vision")
  
#VISION FM
with open("visionFM.txt","r") as f: # Open file
    data = f.readlines()
    data = data[3:]
    aux = 0
    max_value = 0.0
    min_value = 0.0
    line0 = "0 "
    line1 = "1 "
    line2 = "2 "
    ind = 0
    for line in data: 
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        col = new_line.split(' ') 
        col_sem_id = col[1:]
        col_sem_id_np_st = np.array(col_sem_id)
        col_sem_id_np_fl = col_sem_id_np_st.astype(np.float)
        max_data = max(col_sem_id_np_fl)
        min_data = min(col_sem_id_np_fl)
        if aux == 0:
            max_value = max_data
            min_value = min_data
            aux+=1
        else:
            if(max_data > max_value): max_value = max_data
            if(min_data < min_value): min_value = min_data


    for col_n in col_sem_id_np_fl: 
        line0 = line0+str(ind)+" "
        line1 = line1+str(min_value)+" "
        line2 = line2+str(max_value)+" "
        ind = ind+1

with open("visionFM.txt","w") as f: # Open file
    f.write(line0+"\n")
    
with open("visionFM.txt","a") as f: # Open file
   f.write(line1+"\n")
   f.write(line2+"\n")
   for line in data:
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        f.write(new_line)

print("fim vision FM")

#VISION RED
with open("vision_red.txt","r") as f: # Open file
    data = f.readlines()
    data = data[3:]
    aux = 0
    max_value = 0.0
    min_value = 0.0
    line0 = "0 "
    line1 = "1 "
    line2 = "2 "
    ind = 0
    for line in data: 
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        col = new_line.split(' ') 
        col_sem_id = col[1:]
        col_sem_id_np_st = np.array(col_sem_id)
        col_sem_id_np_fl = col_sem_id_np_st.astype(np.float)
        max_data = max(col_sem_id_np_fl)
        min_data = min(col_sem_id_np_fl)
        if aux == 0:
            max_value = max_data
            min_value = min_data
            aux+=1
        else:
            if(max_data > max_value): max_value = max_data
            if(min_data < min_value): min_value = min_data


    for col_n in col_sem_id_np_fl: 
        line0 = line0+str(ind)+" "
        line1 = line1+str(min_value)+" "
        line2 = line2+str(max_value)+" "
        ind = ind+1

with open("vision_red.txt","w") as f: # Open file
    f.write(line0+"\n")
    
with open("vision_red.txt","a") as f: # Open file
   f.write(line1+"\n")
   f.write(line2+"\n")
   for line in data:
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        f.write(new_line)
print("fim vision Red")  

#VISION green
with open("vision_green.txt","r") as f: # Open file
    data = f.readlines()
    data = data[3:]
    aux = 0
    max_value = 0.0
    min_value = 0.0
    line0 = "0 "
    line1 = "1 "
    line2 = "2 "
    ind = 0
    for line in data: 
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        col = new_line.split(' ') 
        col_sem_id = col[1:]
        col_sem_id_np_st = np.array(col_sem_id)
        col_sem_id_np_fl = col_sem_id_np_st.astype(np.float)
        max_data = max(col_sem_id_np_fl)
        min_data = min(col_sem_id_np_fl)
        if aux == 0:
            max_value = max_data
            min_value = min_data
            aux+=1
        else:
            if(max_data > max_value): max_value = max_data
            if(min_data < min_value): min_value = min_data


    for col_n in col_sem_id_np_fl: 
        line0 = line0+str(ind)+" "
        line1 = line1+str(min_value)+" "
        line2 = line2+str(max_value)+" "
        ind = ind+1

with open("vision_green.txt","w") as f: # Open file
    f.write(line0+"\n")
    
with open("vision_green.txt","a") as f: # Open file
   f.write(line1+"\n")
   f.write(line2+"\n")
   for line in data:
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        f.write(new_line)
print("fim vision green")   

#VISION blue
with open("vision_blue.txt","r") as f: # Open file
    data = f.readlines()
    data = data[3:]
    aux = 0
    max_value = 0.0
    min_value = 0.0
    line0 = "0 "
    line1 = "1 "
    line2 = "2 "
    ind = 0
    for line in data: 
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        col = new_line.split(' ') 
        col_sem_id = col[1:]
        col_sem_id_np_st = np.array(col_sem_id)
        col_sem_id_np_fl = col_sem_id_np_st.astype(np.float)
        max_data = max(col_sem_id_np_fl)
        min_data = min(col_sem_id_np_fl)
        if aux == 0:
            max_value = max_data
            min_value = min_data
            aux+=1
        else:
            if(max_data > max_value): max_value = max_data
            if(min_data < min_value): min_value = min_data


    for col_n in col_sem_id_np_fl: 
        line0 = line0+str(ind)+" "
        line1 = line1+str(min_value)+" "
        line2 = line2+str(max_value)+" "
        ind = ind+1

with open("vision_blue.txt","w") as f: # Open file
    f.write(line0+"\n")
    
with open("vision_blue.txt","a") as f: # Open file
   f.write(line1+"\n")
   f.write(line2+"\n")
   for line in data:
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        f.write(new_line)
print("fim vision blue")   

#VISION RED FM
with open("vision_red_FM.txt","r") as f: # Open file
    data = f.readlines()
    data = data[3:]
    aux = 0
    max_value = 0.0
    min_value = 0.0
    line0 = "0 "
    line1 = "1 "
    line2 = "2 "
    ind = 0
    for line in data: 
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        col = new_line.split(' ') 
        col_sem_id = col[1:]
        col_sem_id_np_st = np.array(col_sem_id)
        col_sem_id_np_fl = col_sem_id_np_st.astype(np.float)
        max_data = max(col_sem_id_np_fl)
        min_data = min(col_sem_id_np_fl)
        if aux == 0:
            max_value = max_data
            min_value = min_data
            aux+=1
        else:
            if(max_data > max_value): max_value = max_data
            if(min_data < min_value): min_value = min_data


    for col_n in col_sem_id_np_fl: 
        line0 = line0+str(ind)+" "
        line1 = line1+str(min_value)+" "
        line2 = line2+str(max_value)+" "
        ind = ind+1

with open("vision_red_FM.txt","w") as f: # Open file
    f.write(line0+"\n")
    
with open("vision_red_FM.txt","a") as f: # Open file
   f.write(line1+"\n")
   f.write(line2+"\n")
   for line in data:
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        f.write(new_line)
print("fim vision Red")       

#VISION GREEN FM
with open("vision_green_FM.txt","r") as f: # Open file
    data = f.readlines()
    data = data[3:]
    aux = 0
    max_value = 0.0
    min_value = 0.0
    line0 = "0 "
    line1 = "1 "
    line2 = "2 "
    ind = 0
    for line in data: 
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        col = new_line.split(' ') 
        col_sem_id = col[1:]
        col_sem_id_np_st = np.array(col_sem_id)
        col_sem_id_np_fl = col_sem_id_np_st.astype(np.float)
        max_data = max(col_sem_id_np_fl)
        min_data = min(col_sem_id_np_fl)
        if aux == 0:
            max_value = max_data
            min_value = min_data
            aux+=1
        else:
            if(max_data > max_value): max_value = max_data
            if(min_data < min_value): min_value = min_data


    for col_n in col_sem_id_np_fl: 
        line0 = line0+str(ind)+" "
        line1 = line1+str(min_value)+" "
        line2 = line2+str(max_value)+" "
        ind = ind+1

with open("vision_green_FM.txt","w") as f: # Open file
    f.write(line0+"\n")
    
with open("vision_green_FM.txt","a") as f: # Open file
   f.write(line1+"\n")
   f.write(line2+"\n")
   for line in data:
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        f.write(new_line)
print("fim vision Green")       

#VISION BLUE FM
with open("vision_blue_FM.txt","r") as f: # Open file
    data = f.readlines()
    data = data[3:]
    aux = 0
    max_value = 0.0
    min_value = 0.0
    line0 = "0 "
    line1 = "1 "
    line2 = "2 "
    ind = 0
    for line in data: 
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        col = new_line.split(' ') 
        col_sem_id = col[1:]
        col_sem_id_np_st = np.array(col_sem_id)
        col_sem_id_np_fl = col_sem_id_np_st.astype(np.float)
        max_data = max(col_sem_id_np_fl)
        min_data = min(col_sem_id_np_fl)
        if aux == 0:
            max_value = max_data
            min_value = min_data
            aux+=1
        else:
            if(max_data > max_value): max_value = max_data
            if(min_data < min_value): min_value = min_data


    for col_n in col_sem_id_np_fl: 
        line0 = line0+str(ind)+" "
        line1 = line1+str(min_value)+" "
        line2 = line2+str(max_value)+" "
        ind = ind+1

with open("vision_blue_FM.txt","w") as f: # Open file
    f.write(line0+"\n")
    
with open("vision_blue_FM.txt","a") as f: # Open file
   f.write(line1+"\n")
   f.write(line2+"\n")
   for line in data:
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        f.write(new_line)
print("fim vision Blue")  

#CFM
with open("CFM.txt","r") as f: # Open file
    data = f.readlines()
    data = data[3:]
    aux = 0
    max_value = 0.0
    min_value = 0.0
    line0 = "0 "
    line1 = "1 "
    line2 = "2 "
    ind = 0
    for line in data: 
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        col = new_line.split(' ') 
        col_sem_id = col[1:]
        col_sem_id_np_st = np.array(col_sem_id)
        col_sem_id_np_fl = col_sem_id_np_st.astype(np.float)
        max_data = max(col_sem_id_np_fl)
        min_data = min(col_sem_id_np_fl)
        if aux == 0:
            max_value = max_data
            min_value = min_data
            aux+=1
        else:
            if(max_data > max_value): max_value = max_data
            if(min_data < min_value): min_value = min_data


    for col_n in col_sem_id_np_fl: 
        line0 = line0+str(ind)+" "
        line1 = line1+str(min_value)+" "
        line2 = line2+str(max_value)+" "
        ind = ind+1

with open("CFM.txt","w") as f: # Open file
    f.write(line0+"\n")
    
with open("CFM.txt","a") as f: # Open file
   f.write(line1+"\n")
   f.write(line2+"\n")
   for line in data:
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        f.write(new_line)
print("fim CFM")  

#attMap
with open("attMap.txt","r") as f: # Open file
    data = f.readlines()
    data = data[3:]
    aux = 0
    max_value = 0.0
    min_value = 0.0
    line0 = "0 "
    line1 = "1 "
    line2 = "2 "
    ind = 0
    for line in data: 
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        col = new_line.split(' ') 
        col_sem_id = col[1:]
        col_sem_id_np_st = np.array(col_sem_id)
        col_sem_id_np_fl = col_sem_id_np_st.astype(np.float)
        max_data = max(col_sem_id_np_fl)
        min_data = min(col_sem_id_np_fl)
        if aux == 0:
            max_value = max_data
            min_value = min_data
            aux+=1
        else:
            if(max_data > max_value): max_value = max_data
            if(min_data < min_value): min_value = min_data


    for col_n in col_sem_id_np_fl: 
        line0 = line0+str(ind)+" "
        line1 = line1+str(min_value)+" "
        line2 = line2+str(max_value)+" "
        ind = ind+1

with open("attMap.txt","w") as f: # Open file
    f.write(line0+"\n")
    
with open("attMap.txt","a") as f: # Open file
   f.write(line1+"\n")
   f.write(line2+"\n")
   ite = 3
   for line in data:
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        col = new_line.split(' ') 
        line_sem_id = col[1:]
        bl_line = str(ite)
        for colu in line_sem_id:
            bl_line = bl_line+" "+str(colu)

        f.write(bl_line)
        ite = ite+1
print("fim attMap")
#salMap
with open("salMap.txt","r") as f: # Open file
    data = f.readlines()
    data = data[3:]
    aux = 0
    max_value = 0.0
    min_value = 0.0
    line0 = "0 "
    line1 = "1 "
    line2 = "2 "
    ind = 0
    for line in data: 
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        col = new_line.split(' ') 
        col_sem_id = col[1:]
        col_sem_id_np_st = np.array(col_sem_id)
        col_sem_id_np_fl = col_sem_id_np_st.astype(np.float)
        max_data = max(col_sem_id_np_fl)
        min_data = min(col_sem_id_np_fl)
        if aux == 0:
            max_value = max_data
            min_value = min_data
            aux+=1
        else:
            if(max_data > max_value): max_value = max_data
            if(min_data < min_value): min_value = min_data


    for col_n in col_sem_id_np_fl: 
        line0 = line0+str(ind)+" "
        line1 = line1+str(min_value)+" "
        line2 = line2+str(max_value)+" "
        ind = ind+1

with open("salMap.txt","w") as f: # Open file
    f.write(line0+"\n")
    
with open("salMap.txt","a") as f: # Open file
   f.write(line1+"\n")
   f.write(line2+"\n")
   for line in data:
        new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)    
        f.write(new_line)         

print("fim de código")
