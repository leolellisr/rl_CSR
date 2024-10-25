import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2
import statistics
import pandas as pd
import pingouin as pg

debug = False
cfile1 = "../results/1QTable/profile/nrewards.txt"
sfile1 = "../results/1QTable/profile/nrewards.txt"

cfile2 = "../results/2QTables/profile/ncur_rewards.txt"
sfile2 = "../results/2QTables/profile/nsur_rewards.txt"

output_folder = "../results/"

n_exps = 106
## Remove strings 

def remove_strings_from_file(file_name, strings_to_remove):
    try:
        with open(file_name, 'r') as file:
            lines = file.readlines()

        # Open a new file for writing
        with open(file_name, 'w') as f:
            # Iterate through each line in the input file
            for line in lines:
                # Split the line by whitespace and keep only the elements from index 1 onwards
                elements = line.split(' ')[1:]
                # Join the elements back into a string and write to the output file
                f.write(' '.join(elements) + '\n')
                
        with open(file_name, 'w') as file:
            for line in lines:
                for string_to_remove in strings_to_remove:
                    line = line.replace(string_to_remove, '')
                    line = line.replace(',', '\n')
                file.write(line)
        
        print("Strings removed successfully!")
    except FileNotFoundError:
        print(f"File '{file_name}' not found.")

## Clean Data

# List of strings to remove
strings_to_remove = [
    "[","]","Exp number:", "Action num: ", "Battery: ","Battery:", "reward: ",
    "Curiosity_lv: ", "Red: ", "Green: ", "Blue: ","action:","mot_value: ",
    "r_imp: ","g_imp: ","b_imp: ", "hug_drive: ", "cur_drive: ", "QTables:", 
    "Exp:", "Nact:", "Type:", "ExpS: ", "ExpC: ", "ExpC:", "cur_a:", "sur_a:","exp_c:","exp_s:","dSurV:","SurV:","dCurV:","CurV:"
]

def replace(results):
    aux = []
    for element in results:
        for i in range(len(element)):
            if(type(element[i])== str): element[i] = element[i].replace('\n', '')
        aux.append(element)
    return aux


def get_data_drives(file,ind):
    # Dicionário para mapear os valores únicos do array aos seus índices
    drives = []
    exps = []
    actions = []

    for i in range(0,n_exps+1):
        exp_i = []
        drives.append(exp_i)

    with open(file,"r") as f: # Open file
        #name = file.split('.')[0] 
        #last_name = name.split('/')[2] 
        data = f.readlines()
        aux = 0
        i = 0
        for line in data:
            if(aux == 0):
                aux+=1
            else:
                     
                col = line.split(' ') 
                #print(col)
                #print(int(col[3]))

                if int(col[3]) < n_exps:
                    #if(len(col)>5): col = col[1:]
                    #print("Exp:"+col[3])
                    actions.append(float(col[6])+float(col[7])) 
                    try:
                        drives[int(col[6])].append(float(col[ind]))
                        if debug: print(col)
                               
                    except:
                        print('fim exp')
            

            

            i+=1
        #print('len act:'+str(len(actions)))
        for j, drive in enumerate(drives):
            if len(drive)==0: drives[j] = 0
            else: drives[j] = np.mean(drive)   
            exps.append(j)        
        print("Max actions:"+str(max(actions)))
        dv = int(statistics.stdev(drives))
    return [exps, drives,dv] # len 2

def get_data_drives2q(file, ind):
    # Dicionário para mapear os valores únicos do array aos seus índices
    drives = []
    exps = []
    actions = []
    for i in range(0,n_exps+1):
        exp_i = []
        drives.append(exp_i)

    with open(file,"r") as f: # Open file
        #name = file.split('.')[0] 
        #last_name = name.split('/')[2] 
        data = f.readlines()
        aux = 0
        i = 0
        for line in data:
            if(aux == 0):
                aux+=1
            else:
                     
                col = line.split(' ') 
                #print(col)
                #print(int(col[3]))

                if int(col[4]) < n_exps and int(col[5]) < n_exps:
                    #if(len(col)>5): col = col[1:]
                    #print("Exp:"+col[3])
                    try:
                        drives[int(col[6])].append(float(col[ind]))
                        if debug: print(col)
                        actions.append(float(col[6])+float(col[7]))        
                    except:
                        print('fim exp')
            

            i+=1
        for j, drive in enumerate(drives):
            if len(drive)==0: drives[j] = 0
            else: drives[j] = np.mean(drive)   
            exps.append(j)        

        print("Max actions:"+str(max(actions)))
        dv = int(statistics.stdev(drives))

    return [exps, drives, dv] # len 2

plt.rcParams['font.size'] = '42'

def plot_graphs(title, mean1, exp, mean2, max_ticks, step_ticks):
    
    Y_ticks = [i/10 for i in range(-max_ticks,max_ticks, step_ticks)]
    Y_ticks_act = [i/10 for i in range(-1,max_ticks+2, step_ticks)]
    exp = [expi+1 for expi in exp]
    plt.figure(figsize=(60,20))

    fig, ax1 = plt.subplots(figsize=(60, 20))
    ax1.set_ylim([0, max_ticks/10])
    color = 'tab:blue'
    ax1.set_xlabel('Action')
    
    ax1.set_yticks(Y_ticks_act)
    ax1.tick_params(axis='y') # , labelcolor=color
    ax1.set_ylabel(title)  # we already handled the x-label with ax1
        
    if(len(exp)<90): 
        ax1.set_xticks(exp)
        print(len(exp))
        print(len(mean1))
        ax1.plot(exp, mean1, '^b:', label="Curiosity") #color=color
    else: ax1.plot(exp, mean1, '^b:', label="Curiosity") #color=color
    
    dv1 = float(statistics.stdev(mean1))
    plt.fill_between(exp,np.array(mean1)-np.array(dv1)/2,np.array(mean1)+np.array(dv1)/2,alpha=.1, color=color)

    color = 'tab:red'

    #ax2.set_yticks(Y_ticks)
    ax1.set_ylabel(title) # , color=color
    if(len(exp)<90): ax1.plot(exp, mean2, 'sr-', label="Survival") #color=color
    else:
        ax1.plot(exp, mean2, 'sr:', label="Survival") #color=color
    dv2 = float(statistics.stdev(mean2))
    plt.fill_between(exp,np.array(mean2)-np.array(dv2)/2,np.array(mean2)+np.array(dv2)/2,alpha=.1, color=color)
    
    plt.legend(loc="upper left")

    plt.savefig(output_folder+title+'.pdf')  
    #plt.show()


def plot_graphs_stress(title, mean1, exp, mean2, max_ticks, step_ticks,dv1,dv2):
    
    Y_ticks = [i/10 for i in range(-max_ticks,max_ticks, step_ticks)]
    Y_ticks_act = [i/10 for i in range(-1,max_ticks+2, step_ticks)]
    exp = [expi+1 for expi in exp]
    plt.figure(figsize=(60,20))

    fig, ax1 = plt.subplots(figsize=(60, 20))
    ax1.set_ylim([0, max_ticks/10])
    color = 'tab:blue'
    ax1.set_xlabel('Action')
    
    ax1.set_yticks(Y_ticks_act)
    ax1.tick_params(axis='y') # , labelcolor=color
    ax1.set_ylabel(title)  # we already handled the x-label with ax1
        
    if(len(exp)<90): 
        ax1.set_xticks(exp)
        print(len(exp))
        print(len(mean1))
        ax1.plot(exp, mean1, '^b:', label="1QTable") #color=color
    else: ax1.plot(exp, mean1, '^b:', label="1QTable") #color=color
    
    plt.fill_between(exp,np.array(mean1)-np.array(dv1)/2,np.array(mean1)+np.array(dv1)/2,alpha=.1, color=color)

    color = 'tab:red'

    #ax2.set_yticks(Y_ticks)
    ax1.set_ylabel(title) # , color=color
    if(len(exp)<90): ax1.plot(exp, mean2, 'sr-', label="2QTables") #color=color
    else:
        ax1.plot(exp, mean2, 'sr:', label="2QTables") #color=color

    plt.fill_between(exp,np.array(mean2)-np.array(dv2)/2,np.array(mean2)+np.array(dv2)/2,alpha=.1, color=color)

    plt.legend(loc="upper left")

    plt.savefig(output_folder+title+'.pdf')  
    #plt.show()



remove_strings_from_file(cfile1, strings_to_remove)
remove_strings_from_file(sfile1, strings_to_remove)

remove_strings_from_file(cfile2, strings_to_remove)
remove_strings_from_file(sfile2, strings_to_remove)



# Main
## Get data
results1s = get_data_drives(sfile1,9)
results1c = get_data_drives(cfile1,11)
results2s = get_data_drives2q(sfile2,9)
results2c = get_data_drives2q(cfile2,11)

max_ticks=10
step_ticks=1


try:
    plot_graphs("Activation - 1-QTable", results1c[1][:-1], results1c[0][:-1], results1s[1][:-1], max_ticks, step_ticks)
except:
    if(len(results1c[1])<len(results1s[1])):
        cut = len(results1s[1])-len(results1c[1])+1
        plot_graphs("Activation - 1-QTable", results1c[1][:-1], results1c[0][:-1], results1s[1][:-cut], max_ticks, step_ticks)
    elif (len(results1c[0])>len(results1s[1])):
        cut = len(results1c[0])-len(results1s[1])+1
        plot_graphs("Activation - 1-QTable", results1c[1][:-cut], results1c[0][:-cut], results1s[1][:-1], max_ticks, step_ticks)

try:
    plot_graphs("Activation - 2-QTables", results2c[1][:-1], results2c[0][:-1], results2s[1][:-1], max_ticks, step_ticks)
except:
    if(len(results2c[0])<len(results2s[1])):
        cut = len(results2s[1])-len(results2c[1])+1
        plot_graphs("Activation - 2-QTables", results2c[1][:-1], results2c[0][:-1], results2s[1][:-cut], max_ticks, step_ticks)
    elif (len(results2c[0])>len(results2s[1])):
        cut = len(results2c[1])-len(results2s[1])+1
        plot_graphs("Activation - 2-QTables", results2c[1][:-cut], results2c[0][:-cut], results2s[1][:-1], max_ticks, step_ticks)


try:
    stress_a1 = []
    
    for rc,rs in zip(results1c[1][:-1],results1s[1][:-1]):
        stress = rc+rs
        stress_a1.append(stress)
except:
    print("error")
dv1 = float(statistics.stdev(stress_a1))
try:
    stress_a2 = []
    for rc,rs in zip(results2c[1][:-1],results2s[1][:-1]):
        stress = rc+rs
        stress_a2.append(stress)
except:
    print("error")
print(max(stress_a1))
print(max(stress_a2))
dv2=float(statistics.stdev(stress_a2))

plot_graphs_stress("Stress", stress_a1, results2c[0][:-1], stress_a2, 18, step_ticks, dv1,dv2)    

v = np.array((stress_a1,stress_a2))


df = pd.DataFrame()
df[["1QTable", "2QTables"]] = v.T

rma = pg.rm_anova(df)
print(rma)