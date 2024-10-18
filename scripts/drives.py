import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2
import statistics
import pandas as pd
debug = False
cfile1 = "../results/1QTable/profile/curiosity_drive.txt"
sfile1 = "../results/1QTable/profile/survival_drive.txt"

cfile2 = "../results/2QTables/profile/curiosity_drive.txt"
sfile2 = "../results/2QTables/profile/survival_drive.txt"

output_folder = "../results/"

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
    "[","]","Exp number:", "Action num: ", "Battery: ", "reward: ",
    "Curiosity_lv: ", "Red: ", "Green: ", "Blue: ","action:","mot_value: ",
    "r_imp: ","g_imp: ","b_imp: ", "hug_drive: ", "cur_drive: ", "QTables:", 
    "Exp:", "Nact:", "Type:", "Exp:", "Nact:", "Type:", "ExpS: ", "ExpC: ", "ExpC:"
]

def replace(results):
    aux = []
    for element in results:
        for i in range(len(element)):
            if(type(element[i])== str): element[i] = element[i].replace('\n', '')
        aux.append(element)
    return aux


def get_data_drives(file,exp):
    n_action = []
    exps = []
    drives = []

    # Dicionário para mapear os valores únicos do array aos seus índices
   

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
                if(len(col)>5): col = col[1:]
                #print(col[1])
                if(int(col[2])==exp):
                    drives.append(float(col[1]))
                    n_action.append(int(col[3]))
                    if debug: print(col)
                        
                    exps.append(i)

                    i+=1
    return [n_action, exps, drives] # len 3

def get_data_drives2q(file,exp):
    n_action = []
    exps = []
    drives = []

    # Dicionário para mapear os valores únicos do array aos seus índices
   

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
                if(len(col)>6): col = col[1:]
               
                if(int(col[3])==exp):
                    drives.append(float(col[1]))
                    n_action.append(int(col[4]))
                    if debug: print(col)
                        
                    exps.append(i)

                    i+=1
    return [n_action, exps, drives] # len 3

def plot_graphs(title, mean1, exp, mean2, max_ticks, step_ticks):
    
    Y_ticks = [i/10 for i in range(-max_ticks,max_ticks, step_ticks)]
    Y_ticks_act = [i/10 for i in range(0,max_ticks, step_ticks)]

    plt.figure(figsize=(25,20))

    fig, ax1 = plt.subplots(figsize=(25, 20))
    ax1.set_ylim([0, max_ticks/10])
    color = 'tab:blue'
    ax1.set_xlabel('Experiment')
    
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



print_all = False   
# Main
## Get data
if print_all:
    for exp in range(0,100):
        results1s = get_data_drives(sfile1,exp)
        results1c = get_data_drives(cfile1,exp)
        results2s = get_data_drives2q(sfile2,exp)
        results2c = get_data_drives2q(cfile2,exp)


        try:
            plot_graphs("1QTable"+str(exp), results1c[2], results1c[1], results1s[2], 10, 1)
        except:
            if(len(results1c[1])<len(results1s[2])):
                cut = len(results1s[2])-len(results1c[1])
                plot_graphs("1QTable"+str(exp), results1c[2], results1c[1], results1s[2][:-cut], 10, 1)
            elif (len(results1c[1])>len(results1s[2])):
                cut = len(results1c[1])-len(results1s[2])
                plot_graphs("1QTable"+str(exp), results1c[2][:-cut], results1c[1][:-cut], results1s[2], 10, 1)

        try:
            plot_graphs("2QTables"+str(exp), results2c[2], results2c[1], results2s[2], 10, 1)
        except:
            if(len(results2c[1])<len(results2s[2])):
                cut = len(results2s[2])-len(results2c[1])
                plot_graphs("2QTables"+str(exp), results2c[2], results2c[1], results2s[2][:-cut], 10, 1)
            elif (len(results2c[1])>len(results2s[2])):
                cut = len(results2c[1])-len(results2s[2])
                plot_graphs("2QTables"+str(exp), results2c[2][:-cut], results2c[1][:-cut], results2s[2], 10, 1)


else:
    exp = 97
    results1s = get_data_drives(sfile1,exp)
    results1c = get_data_drives(cfile1,exp)



    try:
        
        plot_graphs("1QTable"+str(exp), results1c[2], results1c[1], results1s[2], 10, 1)
    except:
        if(len(results1c[1])<len(results1s[2])):
            cut = len(results1s[2])-len(results1c[1])
            plot_graphs("1QTable"+str(exp), results1c[2], results1c[1], results1s[2][:-cut], 10, 1)
        elif (len(results1c[1])>len(results1s[2])):
            cut = len(results1c[1])-len(results1s[2])
            plot_graphs("1QTable"+str(exp), results1c[2][:-cut], results1c[1][:-cut], results1s[2], 10, 1)
    exp = 91
    results2s = get_data_drives2q(sfile2,exp)
    results2c = get_data_drives2q(cfile2,exp)
    try:
        plot_graphs("2QTables"+str(exp), results2c[2], results2c[1], results2s[2], 10, 1)
    except:
        if(len(results2c[1])<len(results2s[2])):
            cut = len(results2s[2])-len(results2c[1])
            plot_graphs("2QTables"+str(exp), results2c[2], results2c[1], results2s[2][:-cut], 10, 1)
        elif (len(results2c[1])>len(results2s[2])):
            cut = len(results2c[1])-len(results2s[2])
            plot_graphs("2QTables"+str(exp), results2c[2][:-cut], results2c[1][:-cut], results2s[2], 10, 1)



