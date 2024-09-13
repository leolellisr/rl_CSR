import argparse
import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2
import statistics
debug = False
from matplotlib.ticker import PercentFormatter


#width_bars = 1.0
#width_space = 2.0


# 1 Q-Table
file1 = "../results/1QTable/profile/rewards.txt"


# 2 Q-Tables
file2 = "../results/2QTables/profile/rewards.txt"
output_folder = "../results/"
m_i = False

def get_data_drives(file):
    rewards = []
    exps = []
    actions = []


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
                if(len(col)>7): col = col[1:]
                print(col)
                if m_i:
                    rewards.append(int(col[1])+300)
                    actions.append(int(col[4])+250)
                    
                else: 

                    rewards.append(float(col[1]))
                    actions.append(int(col[4]))
                    if debug: print(col)
                    
                    
                
                exps.append(i)

            i+=1
    return [rewards, exps, actions] # len 3

def get_data_2qt(file):
    rewards = []
    exps = []
    actions = []
    
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
                if(len(col)>7): col = col[1:]
                print(col)
                if m_i:
                    rewards.append(int(col[1])+300)
                    actions.append(int(col[4])+250)
                   

                else: 

                    rewards.append(float(col[1]))
                    actions.append(int(col[4]))
                    if debug: print(col)
                    
                    
                
                exps.append(i)

            i+=1
    return [rewards, exps, actions] # len 3
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
    "[","]","Exp number:", "Action num: ", "Battery:", "reward: ",
    "Curiosity_lv: ", "Red: ", "Green: ", "Blue: ","action:","mot_value: ",
    "r_imp: ","g_imp: ","b_imp: ", "hug_drive: ", "cur_drive: ", "QTables:", 
    "Exp:", "Nact:", "Type:"
]

remove_strings_from_file(file1, strings_to_remove)
remove_strings_from_file(file2, strings_to_remove)

## Get data
results1 = get_data_drives(file1)
results2 = get_data_drives(file2)

# rewards, exps, actions, batery, curiosity, r, g, b
print(f"num Exps: {len(results1[1])}")

print(f"Mean rewards 1: {statistics.mean(results1[0])}. Stdv: +- {statistics.stdev(results1[0])} ")
print(f"Mean rewards 2: {statistics.mean(results2[0])}. Stdv: +- {statistics.stdev(results2[0])} ")
if(statistics.stdev(results2[0]) != 0): print(f"Mean rewards 1/2: {statistics.mean(results1[0])/statistics.mean(results2[0])}. Stdv: +- {statistics.stdev(results1[0])/statistics.stdev(results2[0])} ")

print(f"Mean actions 1: {statistics.mean(results1[2])}. Stdv: +- {statistics.stdev(results1[2])} ")
print(f"Mean actions 2: {statistics.mean(results2[2])}. Stdv: +- {statistics.stdev(results2[2])} ")
if(statistics.stdev(results2[2]) != 0): print(f"Mean actions 1/2: {statistics.mean(results1[2])/statistics.mean(results2[2])}. Stdv: +- {statistics.stdev(results1[2])/statistics.stdev(results2[2])} ")



def get_mean_n_std(step, results):
    mean_rewards = []
    mean_actions = []
    dv_rewards = []
    dv_actions = []
    exps_s = []
    mean_rewards.append(0)
    mean_actions.append(0)
    dv_rewards.append(0)
    dv_actions.append(0)
    exps_s.append(0)
# rewards, exps, actions, batery, curiosity, r, g, b
    for st in range(step):        
        n0 = int(st*len(results[0])/step)
        n1 = int(n0+len(results[0])/step)
        exps_s.append(n1)
        am_rewards = []
        am_actions = [] 
        max_rewards = 0
        max_exp_rew = 0
        max_actions = 0
        max_exp_act = 0
        # rewards, exps, actions, batery, curiosity, r, g, b
        if(debug): print(f"n0: {n0} n1: {n1}")
        for n in range(n0,n1):
            if(debug): print(n)
            am_rewards.append(results[0][n])
            if results[0][n] > max_rewards: 
                max_rewards = results[0][n]
                max_exp_rew = results[1][n]
            if results[2][n] > max_actions: 
                max_actions = results[2][n]
                max_exp_act = results[1][n]
            
            am_actions.append(results[2][n])
            
        mean_rewards.append(int(statistics.mean(am_rewards)))
        mean_actions.append(int(statistics.mean(am_actions)))
        dv_rewards.append(int(statistics.stdev(am_rewards)))
        dv_actions.append(int(statistics.stdev(am_actions)))
        
    print(f"Max. reward: {max_rewards} Exp: {max_exp_rew}")
    print(f"Max. actions: {max_actions} Exp: {max_exp_act}")
    
    print(f"len exps: {len(exps_s)}")
    return [mean_rewards, dv_rewards, mean_actions, dv_actions, exps_s]
#print(exps_s)

print("1 Q-Table")
plots1 = get_mean_n_std(10, results1)

print("2 Q-Tables")
plots2 = get_mean_n_std(10, results2)

plt.rcParams['font.size'] = '32'

def plot_graphs_mean_dv(title, mean1, dv1, exp, mean2, dv2, max_ticks, step_ticks):
    
    Y_ticks = [i for i in range(0,max_ticks, step_ticks)]
    Y_ticks_act = [i for i in range(0,max_ticks, step_ticks)]

    plt.figure(figsize=(25,20))

    fig, ax1 = plt.subplots(figsize=(25, 20))
    ax1.set_ylim([0, max_ticks])
    color = 'tab:blue'
    ax1.set_xlabel('Experiment')
    
    ax1.set_yticks(Y_ticks_act)
    ax1.set_xticks(exp)
    ax1.tick_params(axis='y') # , labelcolor=color
    ax1.set_ylabel(title)  # we already handled the x-label with ax1
    ax1.plot(exp, mean1, '^b:', label="1 Q-Table") #color=color
    plt.fill_between(exp,np.array(mean1)-np.array(dv1)/2,np.array(mean1)+np.array(dv1)/2,alpha=.1, color=color)

    color = 'tab:red'


    ax1.set_ylabel(title) # , color=color
    ax1.plot(exp, mean2, 'sr-', label="2 Q-Tables") #color=color
    plt.fill_between(exp,np.array(mean2)-np.array(dv2)/2,np.array(mean2)+np.array(dv2)/2,alpha=.1, color=color)
    plt.legend(loc="upper left")
    plt.savefig(output_folder+title+'.pdf')  

def plot_graphs(title, mean1, exp, mean2, max_ticks, step_ticks):
    
    Y_ticks = [i for i in range(0,max_ticks, step_ticks)]
    Y_ticks_act = [i for i in range(0,max_ticks, step_ticks)]

    plt.figure(figsize=(25,20))

    fig, ax1 = plt.subplots(figsize=(25, 20))
    ax1.set_ylim([0, max_ticks])
    color = 'tab:blue'
    ax1.set_xlabel('Experiment')
    
    ax1.set_yticks(Y_ticks_act)
    ax1.tick_params(axis='y') # , labelcolor=color
    ax1.set_ylabel(title)  # we already handled the x-label with ax1
        
    if(len(exp)<90): 
        ax1.set_xticks(exp)
        ax1.plot(exp, mean1, '^b:', label="Impulses") #color=color
    else: ax1.plot(exp, mean1, '^b:', label="Impulses") #color=color
    

    color = 'tab:red'

    #ax2.set_yticks(Y_ticks)
    ax1.set_ylabel(title) # , color=color
    if(len(exp)<90): ax1.plot(exp, mean2, 'sr-', label="Drives") #color=color
    else:
        ax1.plot(exp, mean2, 'sr:', label="Drives") #color=color
        
    plt.legend(loc="upper left")

    plt.savefig(output_folder+title+'.pdf')  
    #plt.show()



## Main 
    

# X Axis for Means     
exp1 = [0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100]

cut2 = -9
y_rewards = 6000
ticks_rewards = 200
y_actions = 140
ticks_actions = 10

# mean_rewards, dv_rewards, mean_actions, dv_actions, mean_bat, 
# dv_bat, mean_cur, dv_cur, mean_r, dv_r, mean_g, 
# dv_g, mean_b, dv_b, exps_s

if m_i: 
    plots2[4][2] = 85
    plots2[4][3] = 100
    plots2[4][4] = 90
    plots2[4][5] = 80

plot_graphs_mean_dv("Rewards", plots1[0], plots1[1], exp1, plots2[0], plots2[1], y_rewards, ticks_rewards)
plot_graphs_mean_dv("Number of Actions Performed", plots1[2], plots1[3], exp1, plots2[2], plots2[3], y_actions, ticks_actions)


def replace(results):
    aux = []
    for element in results:
        element = element.replace('\n', '')
        aux.append(float(element))
    return aux



print("Finished")
