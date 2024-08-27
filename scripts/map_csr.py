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


# Impulses
file1 = "../backup/2nd pack/Train/Impulses/txt/rewards.txt"


# Drives
file2 = "../backup/2nd pack/Train/Drives/txt/rewards.txt"
output_folder = "../results/"
m_i = False

def get_data_drives(file):
    rewards = []
    exps = []
    actions = []
    battery = []
    curiosity = []
    r_vl = []
    g_vl = []
    b_vl =[]
    mot = []
    mot_hung = []
    mot_cur = []

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
                if(len(col)>12): col = col[1:]
                print(col)
                if m_i:
                    rewards.append(int(col[8])+300)
                    actions.append(int(col[2])+250)
                    battery.append(100-int(col[3]))
                    curiosity.append(int(col[4]))
                    r_vl.append(int(col[5]))
                    g_vl.append(int(col[6]))
                    b_vl.append(int(col[7]))
                    mot.append(col[9])
                    mot_hung.append(col[10])
                    mot_cur.append(col[11])
                else: 

                    rewards.append(int(col[8]))
                    actions.append(int(col[2]))
                    if debug: print(col)
                    battery.append(100-int(col[3]))
                    curiosity.append(int(col[4]))
                    r_vl.append(int(col[5]))
                    g_vl.append(int(col[6]))
                    b_vl.append(int(col[7]))
                    mot.append(col[9])
                    mot_hung.append(col[10])
                    mot_cur.append(col[11])
                    
                
                exps.append(i)

            i+=1
    return [rewards, exps, actions, battery, curiosity, r_vl, g_vl, b_vl, mot, mot_hung, mot_cur] # len 11

def get_data_impulses(file):
    rewards = []
    exps = []
    actions = []
    battery = []
    curiosity = []
    r_vl = []
    g_vl = []
    b_vl =[]
    mot = []
    mot_r=[]
    mot_b=[]
    mot_g=[]
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
                if(len(col)>13): col = col[1:]
                print(col)
                if m_i:
                    rewards.append(int(col[8])+300)
                    actions.append(int(col[2])+250)
                    battery.append(100-int(col[3]))
                    curiosity.append(int(col[4]))
                    r_vl.append(int(col[5]))
                    g_vl.append(int(col[6]))
                    b_vl.append(int(col[7]))
                    mot.append(col[9])
                    mot_r.append(col[10])
                    mot_g.append(col[11])
                    mot_b.append(col[12])

                else: 

                    rewards.append(int(col[8]))
                    actions.append(int(col[2]))
                    if debug: print(col)
                    battery.append(100-int(col[3]))
                    curiosity.append(int(col[4]))
                    r_vl.append(int(col[5]))
                    g_vl.append(int(col[6]))
                    b_vl.append(int(col[7]))
                    mot.append(col[9])
                    mot_r.append(col[10])
                    mot_g.append(col[11])
                    mot_b.append(col[12])
                    
                
                exps.append(i)

            i+=1
    return [rewards, exps, actions, battery, curiosity, r_vl, g_vl, b_vl, mot, mot_r, mot_g, mot_b] # len 12
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
    "r_imp: ","g_imp: ","b_imp: ", "hug_drive: ", "cur_drive: "
]

remove_strings_from_file(file1, strings_to_remove)
remove_strings_from_file(file2, strings_to_remove)

## Get data
results1 = get_data_impulses(file1)
results2 = get_data_drives(file2)

# rewards, exps, actions, batery, curiosity, r, g, b
print(f"num Exps: {len(results1[1])}")

print(f"Mean rewards 1: {statistics.mean(results1[0])}. Stdv: +- {statistics.stdev(results1[0])} ")
print(f"Mean rewards 2: {statistics.mean(results2[0])}. Stdv: +- {statistics.stdev(results2[0])} ")
if(statistics.stdev(results2[0]) != 0): print(f"Mean rewards 1/2: {statistics.mean(results1[0])/statistics.mean(results2[0])}. Stdv: +- {statistics.stdev(results1[0])/statistics.stdev(results2[0])} ")

print(f"Mean actions 1: {statistics.mean(results1[2])}. Stdv: +- {statistics.stdev(results1[2])} ")
print(f"Mean actions 2: {statistics.mean(results2[2])}. Stdv: +- {statistics.stdev(results2[2])} ")
if(statistics.stdev(results2[2]) != 0): print(f"Mean actions 1/2: {statistics.mean(results1[2])/statistics.mean(results2[2])}. Stdv: +- {statistics.stdev(results1[2])/statistics.stdev(results2[2])} ")

print(f"Mean battery usage 1: {statistics.mean(results1[3])}. Stdv: +- {statistics.stdev(results1[3])} ")
print(f"Mean battery usage 2: {statistics.mean(results2[3])}. Stdv: +- {statistics.stdev(results2[3])} ")
if(statistics.stdev(results2[3]) != 0): print(f"Mean battery usage 1/2: {statistics.mean(results1[3])/statistics.mean(results2[3])}. Stdv: +- {statistics.stdev(results1[3])/statistics.stdev(results2[3])} ")

print(f"Mean curiosity 1: {statistics.mean(results1[4])}. Stdv: +- {statistics.stdev(results1[4])} ")
print(f"Mean curiosity 2: {statistics.mean(results2[4])}. Stdv: +- {statistics.stdev(results2[4])} ")
if(statistics.stdev(results2[4]) != 0): print(f"Mean curiosity 1/2: {statistics.mean(results1[4])/statistics.mean(results2[4])}. Stdv: +- {statistics.stdev(results1[4])/statistics.stdev(results2[4])} ")

print(f"Mean R 1: {statistics.mean(results1[5])}. Stdv: +- {statistics.stdev(results1[5])} ")
print(f"Mean R 2: {statistics.mean(results2[5])}. Stdv: +- {statistics.stdev(results2[5])} ")
if(statistics.stdev(results2[5]) != 0): print(f"Mean R 1/2: {statistics.mean(results1[5])/statistics.mean(results2[5])}. Stdv: +- {statistics.stdev(results1[5])/statistics.stdev(results2[5])} ")

print(f"Mean g 1: {statistics.mean(results1[6])}. Stdv: +- {statistics.stdev(results1[6])} ")
print(f"Mean g 2: {statistics.mean(results2[6])}. Stdv: +- {statistics.stdev(results2[6])} ")
if(statistics.stdev(results2[6]) != 0): print(f"Mean g 1/2: {statistics.mean(results1[6])/statistics.mean(results2[6])}. Stdv: +- {statistics.stdev(results1[6])/statistics.stdev(results2[6])} ")

print(f"Mean b 1: {statistics.mean(results1[7])}. Stdv: +- {statistics.stdev(results1[7])} ")
print(f"Mean b 2: {statistics.mean(results2[7])}. Stdv: +- {statistics.stdev(results2[7])} ")
if(statistics.stdev(results2[7]) != 0): print(f"Mean b 1/2: {statistics.mean(results1[7])/statistics.mean(results2[7])}. Stdv: +- {statistics.stdev(results1[7])/statistics.stdev(results2[7])} ")


def get_mean_n_std(step, results):
    mean_rewards = []
    mean_actions = []
    mean_bat = []
    mean_cur = []
    mean_r = []
    mean_g = []
    mean_b = []
    dv_bat = []
    dv_cur = []
    dv_r = []
    dv_g = []
    dv_b = []
    dv_rewards = []
    dv_actions = []
    exps_s = []
    mean_rewards.append(0)
    mean_actions.append(0)
    mean_bat.append(0)
    mean_cur.append(0)
    mean_r.append(0)
    mean_g.append(0)
    mean_b.append(0)
    dv_bat.append(0)
    dv_cur.append(0)
    dv_r.append(0)
    dv_g.append(0)
    dv_b.append(0)
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
        am_bat = []
        am_cur = []
        am_r = []
        am_g = []
        am_b = []    
        max_rewards = 0
        max_exp_rew = 0
        max_actions = 0
        max_exp_act = 0
        max_bat = 0
        max_exp_bat = 0
        max_cur = 0
        max_exp_cur = 0
        max_r = 0
        max_exp_r = 0
        max_g = 0
        max_exp_g = 0
        max_b = 0
        max_exp_b = 0
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
            if results[3][n] > max_bat: 
                max_bat = results[3][n]
                max_exp_bat = results[1][n]  
            if results[4][n] > max_cur: 
                max_cur = results[4][n]
                max_exp_cur = results[1][n] 
            if(debug): print(f"R: {results[5][n]} / max_r: {max_r}")    
            if results[5][n] > max_r: 
                max_r = results[5][n]
                max_exp_r = results[1][n] 
            if(debug): print(f"G: {results[6][n]}")    
            if results[6][n] > max_g: 
                max_g = results[6][n]
                max_exp_g = results[1][n] 
            if(debug): print(f"B: {results[7][n]}")    
            if results[7][n] > max_b: 
                max_b = results[7][n]
                max_exp_b = results[1][n] 
            am_actions.append(results[2][n])
            am_bat.append(results[3][n])
            am_cur.append(results[4][n])
            am_r.append(results[5][n])
            am_g.append(results[6][n])
            am_b.append(results[7][n])
            
        mean_rewards.append(int(statistics.mean(am_rewards)))
        mean_actions.append(int(statistics.mean(am_actions)))
        mean_bat.append(int(statistics.mean(am_bat)))
        mean_cur.append(int(statistics.mean(am_cur)))
        mean_r.append(int(statistics.mean(am_r)))
        mean_g.append(int(statistics.mean(am_g)))
        mean_b.append(int(statistics.mean(am_b)))
        dv_rewards.append(int(statistics.stdev(am_rewards)))
        dv_actions.append(int(statistics.stdev(am_actions)))
        dv_bat.append(int(statistics.stdev(am_bat)))
        dv_cur.append(int(statistics.stdev(am_cur)))
        dv_r.append(int(statistics.stdev(am_r)))
        dv_g.append(int(statistics.stdev(am_g)))
        dv_b.append(int(statistics.stdev(am_b)))

    print(f"Max. reward: {max_rewards} Exp: {max_exp_rew}")
    print(f"Max. actions: {max_actions} Exp: {max_exp_act}")
    print(f"Max. Battery: {max_bat} Exp: {max_exp_bat}")
    print(f"Max. Curiosity: {max_cur} Exp: {max_exp_cur}")
    print(f"Max. R: {max_r} Exp: {max_exp_r}")
    print(f"Max. G: {max_g} Exp: {max_exp_g}")
    print(f"Max. B: {max_b} Exp: {max_exp_b}")
    print(f"len exps: {len(exps_s)}")
    return [mean_rewards, dv_rewards, mean_actions, dv_actions, mean_bat, dv_bat, 
            mean_cur, dv_cur, mean_r, dv_r, mean_g, dv_g, mean_b, dv_b, exps_s]
#print(exps_s)

print("Impulses")
plots1 = get_mean_n_std(10, results1)

print("Drives")
plots2 = get_mean_n_std(10, results2)

plt.rcParams['font.size'] = '32'

def plot_graphs_mean_dv(title, mean1, dv1, exp, mean2, dv2, max_ticks, step_ticks):
    
    Y_ticks = [i for i in range(0,max_ticks, step_ticks)]
    Y_ticks_act = [i for i in range(0,max_ticks, step_ticks)]

    plt.figure(figsize=(25,20))

#fig, axes = plt.subplots(6, 3, figsize=(30, 30))
    fig, ax1 = plt.subplots(figsize=(25, 20))
    ax1.set_ylim([0, max_ticks])
    color = 'tab:blue'
    ax1.set_xlabel('Experiment')
    
    ax1.set_yticks(Y_ticks_act)
    ax1.set_xticks(exp)
    ax1.tick_params(axis='y') # , labelcolor=color
    ax1.set_ylabel(title)  # we already handled the x-label with ax1
    ax1.plot(exp, mean1, '^b:', label="Impulses") #color=color
    plt.fill_between(exp,np.array(mean1)-np.array(dv1)/2,np.array(mean1)+np.array(dv1)/2,alpha=.1, color=color)
    #ax1.errorbar(exps_s, mean_actions, dv_actions, marker='x', markersize=5,  lw=1, capsize=5, capthick=1, color=color)
    #ax2 = ax1.twinx()  # instantiate a second axes that shares the same x-axis

    color = 'tab:red'

    #ax2.set_yticks(Y_ticks)
    ax1.set_ylabel(title) # , color=color
    ax1.plot(exp, mean2, 'sr-', label="Drives") #color=color
    plt.fill_between(exp,np.array(mean2)-np.array(dv2)/2,np.array(mean2)+np.array(dv2)/2,alpha=.1, color=color)
    #ax2.errorbar(exps_s, mean_rewards, dv_rewards, marker='+', markersize=5, lw=1, capsize=5, capthick=1, color=color)
    #ax1.tick_params(axis='y', labelcolor=color)

    #fig.tight_layout()  # otherwise the right y-label is slightly clipped
    plt.legend(loc="upper left")
    plt.savefig(output_folder+title+'.pdf')  

def plot_graphs(title, mean1, exp, mean2, max_ticks, step_ticks):
    
    Y_ticks = [i for i in range(0,max_ticks, step_ticks)]
    Y_ticks_act = [i for i in range(0,max_ticks, step_ticks)]

    plt.figure(figsize=(25,20))

#fig, axes = plt.subplots(6, 3, figsize=(30, 30))
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
    #else: ax1.bar(exp, mean1, width=width_bars, bottom=None, align='center', color='b', label="Impulses") #color=color
    else: ax1.plot(exp, mean1, '^b:', label="Impulses") #color=color
    
    #ax1.errorbar(exps_s, mean_actions, dv_actions, marker='x', markersize=5,  lw=1, capsize=5, capthick=1, color=color)
    #ax2 = ax1.twinx()  # instantiate a second axes that shares the same x-axis

    color = 'tab:red'

    #ax2.set_yticks(Y_ticks)
    ax1.set_ylabel(title) # , color=color
    if(len(exp)<90): ax1.plot(exp, mean2, 'sr-', label="Drives") #color=color
    else:
        #new_exp = [x+width_bars*1.5 for x in exp]
        #ax1.bar(new_exp, mean2, width=width_bars, bottom=None, align='center', color='r', label="Drives") #color=color
        #new_exp = [(x+width_space)/2 for x in exp]
        #plt.xticks(new_exp, exp)
        ax1.plot(exp, mean2, 'sr:', label="Drives") #color=color
        
    #ax2.errorbar(exps_s, mean_rewards, dv_rewards, marker='+', markersize=5, lw=1, capsize=5, capthick=1, color=color)
    #ax1.tick_params(axis='y', labelcolor=color)

    #fig.tight_layout()  # otherwise the right y-label is slightly clipped
    plt.legend(loc="upper left")
#line1, = plt.plot(exps, rewards, 'r', label='Recompensas')
#line2, = plt.plot(exps, actions, 'b', label= 'Número de ações')

# Create a legend for the first line.
#first_legend = plt.legend(handles=[line1], loc='upper right')

# Add the legend manually to the current Axes.
#ax = plt.gca().add_artist(first_legend)

# Create another legend for the second line.
#plt.legend(handles=[line2], loc='lower right')
    plt.savefig(output_folder+title+'.pdf')  
    #plt.show()

def plot_graphs_rgb(title, mean1r, mean1g, mean1b, exp, mean2r, mean2g, mean2b, max_ticks, step_ticks):
    
    Y_ticks = [i for i in range(0,max_ticks, step_ticks)]
    Y_ticks_act = [i for i in range(0,max_ticks, step_ticks)]

    plt.figure(figsize=(25,20))

#fig, axes = plt.subplots(6, 3, figsize=(30, 30))
    fig, ax1 = plt.subplots(figsize=(25, 20))
    ax1.set_ylim([0, max_ticks])
    color = 'tab:blue'
    ax1.set_xlabel('Experiment')
    
    ax1.set_yticks(Y_ticks_act)
    ax1.tick_params(axis='y') # , labelcolor=color
    ax1.set_ylabel(title)  # we already handled the x-label with ax1
        
    #ax1.bar(exp, mean1r, width=width_bars, bottom=None, align='center', color='#fc03b1', label="R - Impulses") #color=color
    #new_exp = [x+width_bars for x in exp]
    #ax1.bar(new_exp, mean1g, width=width_bars, bottom=None, align='center', color='#68ff0a', label="G - Impulses") #color=color
    #new_exp = [x+width_bars*2 for x in exp]
    #ax1.bar(new_exp, mean1b, width=width_bars, bottom=None, align='center', color='#03ecfc', label="B - Impulses") #color=color
    
    ax1.plot(exp, mean1r, '^m:', label="R - Impulses") #color=color
    ax1.plot(exp, mean1g, '^k:', label="G - Impulses") #color=color
    ax1.plot(exp, mean1b, '^c:', label="B - Impulses") #color=color
    
    #ax1.errorbar(exps_s, mean_actions, dv_actions, marker='x', markersize=5,  lw=1, capsize=5, capthick=1, color=color)
    #ax2 = ax1.twinx()  # instantiate a second axes that shares the same x-axis

    color = 'tab:red'

    #ax2.set_yticks(Y_ticks)
    ax1.set_ylabel(title) # , color=color
    #new_exp = [x+width_bars*3 for x in exp]
    #ax1.bar(new_exp, mean2r, width=width_bars, bottom=None, align='center', color='r', label="R - Drives") #color=color
    #new_exp = [x+width_bars*4 for x in exp]
    #ax1.bar(new_exp, mean2g, width=width_bars, bottom=None, align='center', color='g', label="G - Drives") #color=color
    #new_exp = [x+width_bars*5 for x in exp]
    #ax1.bar(new_exp, mean2b, width=width_bars, bottom=None, align='center', color='b', label="B - Drives") #color=color
    
    ax1.plot(exp, mean2r, 'sr:', label="R - Drives") #color=color
    ax1.plot(exp, mean2g, 'sg:', label="G - Drives") #color=color
    ax1.plot(exp, mean2b, 'sb:', label="B - Drives") #color=color

    #ax2.errorbar(exps_s, mean_rewards, dv_rewards, marker='+', markersize=5, lw=1, capsize=5, capthick=1, color=color)
    #ax1.tick_params(axis='y', labelcolor=color)
    
    #fig.tight_layout()  # otherwise the right y-label is slightly clipped
    plt.legend(loc="upper left")
#line1, = plt.plot(exps, rewards, 'r', label='Recompensas')
#line2, = plt.plot(exps, actions, 'b', label= 'Número de ações')

# Create a legend for the first line.
#first_legend = plt.legend(handles=[line1], loc='upper right')

# Add the legend manually to the current Axes.
#ax = plt.gca().add_artist(first_legend)

# Create another legend for the second line.
#plt.legend(handles=[line2], loc='lower right')
    plt.savefig(output_folder+title+'.pdf')  
    #plt.show()


def plot_graphs_mot(title, mot1, hung1, cur1, mot2, r2, g2, b2, exp, max_ticks, step_ticks):
    
    #Y_ticks = [i/10 for i in range(0,10, 1)]
    Y_ticks_act = [i for i in range(0,max_ticks, step_ticks)]
    print(f"Y_ticks_act: {Y_ticks_act}")
    plt.figure(figsize=(25,20))

#fig, axes = plt.subplots(6, 3, figsize=(30, 30))
    fig, ax1 = plt.subplots(figsize=(25, 20))
    #color = 'tab:blue'
    ax1.set_xlabel('Experiment')
    ax1.set_yticks(Y_ticks_act)
    ax1.tick_params(axis='y') # , labelcolor=color
    ax1.set_ylabel(title+"_Impulses")  # we already handled the x-label with ax1

    ax1.plot(exp, r2, '^r:', label="R - Impulses") #color=color
    ax1.plot(exp, g2, '^g:', label="G - Impulses") #color=color
    ax1.plot(exp, b2, '^b:', label="B - Impulses") #color=color
    ax1.set_ylim([0, max_ticks])
    ax1.legend(loc="upper left")
   # ax2.legend(loc="upper right")
    plt.savefig(output_folder+title+'_Impulses.pdf')      
    #ax2 = ax1.twinx()  # instantiate a second axes that shares the same x-axis

    #color = 'tab:red'

    #ax2.set_yticks(Y_ticks)
    
    #ax1.set_ylabel(title) # , color=color
    fig, ax1 = plt.subplots(figsize=(25, 20))
    #color = 'tab:blue'
    ax1.set_xlabel('Experiment')
    ax1.set_yticks(Y_ticks_act)
    ax1.tick_params(axis='y') # , labelcolor=color
    ax1.set_ylabel(title+"_Drives")  # we already handled the x-label with ax1
    ax1.plot(exp, cur1, 'sc:', label="Curiosity - Drives") #color=color
    ax1.plot(exp, hung1, 'sm:', label="Survival - Drives") #color=color
    ax1.set_ylim([0, max_ticks])
    #ax2.errorbar(exps_s, mean_rewards, dv_rewards, marker='+', markersize=5, lw=1, capsize=5, capthick=1, color=color)
    #ax1.tick_params(axis='y', labelcolor=color)
   # ax2.plot(exp, mot2, '^k:', label="Impulses") #color=color
   # ax2.plot(exp, mot1, 'sb:', label="Drives") #color=color
    
   # fig.tight_layout()  # otherwise the right y-label is slightly clipped
    
    ax1.legend(loc="upper left")
   # ax2.legend(loc="upper right")
    plt.savefig(output_folder+title+'_Drives.pdf')  
    #plt.show()

## Main 
    

# X Axis for Means     
exp1 = [0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100]

cut2 = -9
y_rewards = 85
ticks_rewards = 5
y_actions = 95
ticks_actions = 5
y_battery = 105
ticks_battery = 5
y_curiosity = 15
ticks_curiosity = 1
rgb_mean_y = 5
rgb_mean_ticks = 1
rgb_y = 8
rgb_ticks = 1
rgb_total_y = 11
rgb_total_ticks = 1
cur_y = 16
cur_ticks = 1
mot_y = 120
mot_ticks=10

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
plot_graphs("Battery Usage", plots1[4], exp1, plots2[4], y_battery, ticks_battery)
plot_graphs("Curiosity", plots1[6], exp1, plots2[6], y_curiosity, ticks_curiosity)
plot_graphs("R_mean", plots1[8], exp1, plots2[8], rgb_mean_y,rgb_mean_ticks)
plot_graphs("G_mean", plots1[10], exp1, plots2[10], rgb_mean_y,rgb_mean_ticks)
plot_graphs("B_mean", plots1[12], exp1, plots2[12], rgb_mean_y,rgb_mean_ticks)

#LEN exps is the x axis now 
if debug: print(f"len r1: {len(results1[5])}, r2: {len(results2[5])}, exps: {len(results1[1])}")
plot_graphs("R_total", results1[5], results1[1], results2[5], rgb_y,rgb_ticks)
if debug: print(f"len g1: {len(results1[6])}, g2: {len(results2[6])}, exps: {len(results1[1])}")
plot_graphs("G_total", results1[6], results1[1], results2[6], rgb_y,rgb_ticks)
if debug: print(f"len b1: {len(results1[7])}, b2: {len(results2[7])}, exps: {len(results1[1])}")
plot_graphs("B_total", results1[7], results1[1], results2[7], rgb_y,rgb_ticks)
plot_graphs_rgb("RGB_total", results1[5], results1[6], results1[7], results1[1], results2[5], results2[6], results2[7], rgb_total_y,rgb_total_ticks)
plot_graphs("Curiosity_Lv", results1[4], results1[1], results2[4], cur_y, cur_ticks)

def replace(results):
    aux = []
    for element in results:
        element = element.replace('\n', '')
        aux.append(float(element))
    return aux




results2[8] = replace(results2[8])
results2[9] = replace(results2[9])
results2[10] = replace(results2[10])

results1[8] = replace(results1[8])
results1[9] = replace(results1[9])
results1[10] = replace(results1[10])
results1[11] = replace(results1[11])
print(f"mot_y: {mot_y}")
plot_graphs_mot("Motivation", results2[8], results2[9], results2[10], results1[8], results1[9], results1[10], results1[11], results1[1], mot_y, mot_ticks)

if debug:
    print(f"Motivation max - Drives: {max(results2[8])}, hung:{max(results2[9])}, cur: {max(results2[10])}") 
    print(f"Motivation max - Impulses: {max(results1[8])}, r: {max(results1[9])}, g:{max(results1[10])}, b:{max(results1[11])}")

    print(f"Motivation len - Drives: {len(results2[8])}, hung:{len(results2[9])}, cur: {len(results2[10])}") 
    print(f"Motivation len - Impulses: {len(results1[8])}, r: {len(results1[9])}, g:{len(results1[10])}, b:{len(results1[11])}")

    print(f"Motivation - Drives")
    print(results2[8])

    print(f"Motivation - Drives hung")
    print(results2[9])

    print(f"Motivation - Drives cur")
    print(results2[10])

    print(f"Motivation - Impulses")
    print(results1[8])

    print(f"Motivation - Impulses r")
    print(results1[9])

    print(f"Motivation - Impulses g")
    print(results1[10])

    print(f"Motivation - Impulses b")
    print(results1[11])


print("Finished")
