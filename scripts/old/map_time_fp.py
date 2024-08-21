import argparse
import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2
import statistics
debug = False
from matplotlib.ticker import PercentFormatter
file1 = "../results/txt_last_exp/actions_dqn.txt"
file2 = "../results/txt_last_exp/actions_tb.txt"
output_folder = "../results/"

def get_data(file):
    #rewards = []
    exps = []
    #actions = []
    times = []
    with open(file,"r") as f: # Open file
        #name = file.split('.')[0] 
        #last_name = name.split('/')[2] 
        data = f.readlines()
        for line in data: 
            col = line.split(' ') 
            #rewards.append(int(col[1]))
            exps.append(int(col[2]))
            #actions.append(int(col[3]))
            times.append(int(col[4]))
    return exps, times

exps1, time1 = get_data(file1)
exps2, time2 = get_data(file2)

print(f"Mean time elapsed per step DQN: {statistics.mean(time1)}. Stdv: +- {statistics.stdev(time1)} ")
print(f"Mean time elapsed per step Tabular: {statistics.mean(time2)}. Stdv: +- {statistics.stdev(time2)} ")
print(f"Mean time elapsed DQN/Tabular: {statistics.mean(time1)/statistics.mean(time2)}. Stdv: +- {statistics.stdev(time1)/statistics.stdev(time2)} ")
print(f"Mean time elapsed Tabular/DQN: {statistics.mean(time2)/statistics.mean(time1)}. Stdv: +- {statistics.stdev(time2)/statistics.stdev(time1)} ")

def get_mean_n_std(step, exps, time):
    mean_time = []
    dv_time = []
    exps_s = []
    mean_time.append(0)
    dv_time.append(0)
    exps_s.append(0)

    for st in range(step):        
        n0 = st*10
        n1 = n0+10
        exps_s.append(n1)
        am_time = []
        max_time = 0
        max_exp_time = 0
        
        if(debug): print(f"n0: {n0} n1: {n1}")
        for n in range(n0,n1):
            if(debug): print(n)
            am_time.append(time[n])
            if time[n] > max_time: 
                max_time = time[n]
                max_exp_time = exps[n]
            
        mean_time.append(int(statistics.mean(am_time)))
        dv_time.append(int(statistics.stdev(am_time)))

    print(f"Max. time: {max_time} Exp: {max_exp_time}")
    print(f"len exps: {len(exps_s)}")
    return mean_time, dv_time, exps_s
#print(exps_s)

mean_time1, dv_time1, exp1 = get_mean_n_std(100, time1, exps1)
mean_time2, dv_time2, exp2 = get_mean_n_std(100, time2, exps2)

plt.rcParams['font.size'] = '16'

def plot_graphs(title, mean1, dv1, exp, mean2, dv2, max_ticks, step_ticks):
    
    Y_ticks = [i for i in range(0,max_ticks, step_ticks)]
    Y_ticks_act = [i for i in range(0,max_ticks, step_ticks)]

    plt.figure(figsize=(25,20))

#fig, axes = plt.subplots(6, 3, figsize=(30, 30))
    fig, ax1 = plt.subplots(figsize=(25, 20))

    color = 'tab:blue'
    ax1.set_xlabel('Step')

    ax1.set_yticks(Y_ticks_act)
    ax1.set_xticks(exp)
    ax1.tick_params(axis='y', labelcolor=color)
    ax1.set_ylabel(title)  # we already handled the x-label with ax1
    ax1.plot(exp, mean1, 'b:', label="DQN") #color=color
    plt.fill_between(exp,np.array(mean1)-np.array(dv1)/2,np.array(mean1)+np.array(dv1)/2,alpha=.1, color=color)
    #ax1.errorbar(exps_s, mean_actions, dv_actions, marker='x', markersize=5,  lw=1, capsize=5, capthick=1, color=color)
    #ax2 = ax1.twinx()  # instantiate a second axes that shares the same x-axis

    color = 'tab:red'

    #ax2.set_yticks(Y_ticks)
    ax1.set_ylabel(title, color=color)
    ax1.plot(exp, mean2, 'r-', label="Tabular") #color=color
    plt.fill_between(exp,np.array(mean2)-np.array(dv2)/2,np.array(mean2)+np.array(dv2)/2,alpha=.1, color=color)
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

plot_graphs("Time Elapsed", mean_time1, dv_time1, exp1, mean_time2, dv_time2, 800, 20)
