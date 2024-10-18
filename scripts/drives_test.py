import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2
import statistics
import pandas as pd
debug = False
cfile1 = "../results/1QTable/profile/nrewards.txt"
sfile1 = "../results/1QTable/profile/nrewards.txt"

cfile2 = "../results/2QTables/profile/ncur_rewards.txt"
sfile2 = "../results/2QTables/profile/nsur_rewards.txt"

output_folder = "../results/"

n_exps = 50
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
                print(col)
                print(int(col[3]))

                if int(col[3]) < n_exps:
                    #if(len(col)>5): col = col[1:]
                    print("Exp:"+col[3])
                    actions.append(int(col[6])+int(col[7])) 
                    try:
                        drives[int(col[3])].append(float(col[ind]))
                        if debug: print(col)
                               
                    except:
                        print('fim exp')
            

            i+=1
        print('len act:'+str(len(actions)))
        for j, drive in enumerate(drives):
            if len(drive)==0: drives[j] = 0
            else: drives[j] = np.mean(drive)   
            exps.append(j)        
            print("Max actions:"+str(max(actions)))
    return [exps, drives] # len 2

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
                print(col)
                print(int(col[3]))

                if int(col[3]) < n_exps:
                    #if(len(col)>5): col = col[1:]
                    print("Exp:"+col[3])
                    try:
                        drives[int(col[3])].append(float(col[ind]))
                        if debug: print(col)
                        actions.append(int(col[6])+int(col[7]))        
                    except:
                        print('fim exp')
            

            i+=1
        for j, drive in enumerate(drives):
            if len(drive)==0: drives[j] = 0
            else: drives[j] = np.mean(drive)   
            exps.append(j)        
            print("Max actions:"+str(max(actions)))
    return [exps, drives] # len 2
def plot_graphs(title, mean1, exp, mean2, max_ticks, step_ticks):
    
    Y_ticks = [i/10 for i in range(-max_ticks,max_ticks, step_ticks)]
    Y_ticks_act = [i/10 for i in range(-1,max_ticks+2, step_ticks)]
    exp = [expi+1 for expi in exp]
    plt.figure(figsize=(25,20))

    fig, ax1 = plt.subplots(figsize=(25, 20))
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
    

    color = 'tab:red'

    #ax2.set_yticks(Y_ticks)
    ax1.set_ylabel(title) # , color=color
    if(len(exp)<90): ax1.plot(exp, mean2, 'sr-', label="Survival") #color=color
    else:
        ax1.plot(exp, mean2, 'sr:', label="Survival") #color=color
        
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
    plot_graphs("1QTable", results1c[1], results1c[0], results1s[1], max_ticks, step_ticks)
except:
    if(len(results1c[0])<len(results1s[1])):
        cut = len(results1s[1])-len(results1c[0])
        plot_graphs("1QTable", results1c[1], results1c[0], results1s[1][:-cut], max_ticks, step_ticks)
    elif (len(results1c[0])>len(results1s[1])):
        cut = len(results1c[0])-len(results1s[1])
        plot_graphs("1QTable", results1c[1][:-cut], results1c[0][:-cut], results1s[1], max_ticks, step_ticks)

try:
    plot_graphs("2QTables", results2c[1], results2c[0], results2s[1], max_ticks, step_ticks)
except:
    if(len(results2c[0])<len(results2s[1])):
        cut = len(results2s[1])-len(results2c[0])
        plot_graphs("2QTables", results2c[1], results2c[0], results2s[1][:-cut], max_ticks, step_ticks)
    elif (len(results2c[0])>len(results2s[1])):
        cut = len(results2c[0])-len(results2s[1])
        plot_graphs("2QTables", results2c[1][:-cut], results2c[0][:-cut], results2s[1], max_ticks, step_ticks)


