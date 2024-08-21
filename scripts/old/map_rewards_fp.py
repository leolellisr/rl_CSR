import argparse
import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2
import statistics
debug = True
from matplotlib.ticker import PercentFormatter

# Non-Constructive
file1 = "../results/rewards.txt"


# Constructive
file2 = "../results/rewards1.txt"
output_folder = "../results/"
m_i = True

def get_data(file):
    rewards = []
    exps = []
    actions = []
    mem = []
    with open(file,"r") as f: # Open file
        #name = file.split('.')[0] 
        #last_name = name.split('/')[2] 
        data = f.readlines()
        i = 0
        for line in data: 
            col = line.split(' ') 
            if m_i:
                rewards.append(int(col[1])+300)
                actions.append(int(col[3])+250)
                
            else: 

                rewards.append(int(col[1]))
                actions.append(int(col[3]))
                if debug: print(col)
            
            exps.append(i)

            mem.append(int(col[4]))
            i+=1
    return rewards, exps, actions, mem

rewards1, exps1, actions1, mem1 = get_data(file1)
rewards2, exps2, actions2, mem2 = get_data(file2)

print(f"num Exps: {len(exps1)}")

print(f"Mean rewards 2: {statistics.mean(rewards1)}. Stdv: +- {statistics.stdev(rewards1)} ")
print(f"Mean rewards 3: {statistics.mean(rewards2)}. Stdv: +- {statistics.stdev(rewards2)} ")
print(f"Mean rewards 2/3: {statistics.mean(rewards1)/statistics.mean(rewards2)}. Stdv: +- {statistics.stdev(rewards1)/statistics.stdev(rewards2)} ")

print(f"Mean actions 2: {statistics.mean(actions1)}. Stdv: +- {statistics.stdev(actions1)} ")
print(f"Mean actions 3: {statistics.mean(actions2)}. Stdv: +- {statistics.stdev(actions2)} ")
print(f"Mean actions 2/3: {statistics.mean(actions1)/statistics.mean(actions2)}. Stdv: +- {statistics.stdev(actions1)/statistics.stdev(actions2)} ")

print(f"Mean memory usage 2: {statistics.mean(mem1)}. Stdv: +- {statistics.stdev(mem1)} ")
print(f"Mean memory usage 3: {statistics.mean(mem2)}. Stdv: +- {statistics.stdev(mem2)} ")
print(f"Mean memory usage 2/3: {statistics.mean(mem1)/statistics.mean(mem2)}. Stdv: +- {statistics.stdev(mem1)/statistics.stdev(mem2)} ")


def get_mean_n_std(step, rewards, exps, actions, mem):
    mean_rewards = []
    mean_actions = []
    mean_mem = []
    dv_mem = []
    dv_rewards = []
    dv_actions = []
    exps_s = []
    mem_s = []
    mean_rewards.append(0)
    mean_actions.append(0)
    mean_mem.append(0)
    dv_mem.append(0)
    dv_rewards.append(0)
    dv_actions.append(0)
    exps_s.append(0)

    for st in range(step):        
        n0 = int(st*len(rewards)/step)
        n1 = int(n0+len(rewards)/step)
        exps_s.append(n1)
        am_rewards = []
        am_actions = [] 
        am_mem = []    
        max_rewards = 0
        max_exp_rew = 0
        max_actions = 0
        max_exp_act = 0
        max_mem = 0
        max_exp_mem = 0
        
        if(debug): print(f"n0: {n0} n1: {n1}")
        for n in range(n0,n1):
            if(debug): print(n)
            am_rewards.append(rewards[n])
            if rewards[n] > max_rewards: 
                max_rewards = rewards[n]
                max_exp_rew = exps[n]
            if actions[n] > max_actions: 
                max_actions = actions[n]
                max_exp_act = exps[n]
            if mem[n] > max_mem: 
                max_mem = mem[n]
                max_exp_mem = exps[n]    
            am_actions.append(actions[n])
            am_mem.append(mem[n])
            
        mean_rewards.append(int(statistics.mean(am_rewards)))
        mean_actions.append(int(statistics.mean(am_actions)))
        mean_mem.append(int(statistics.mean(am_mem)))
        dv_rewards.append(int(statistics.stdev(am_rewards)))
        dv_actions.append(int(statistics.stdev(am_actions)))
        dv_mem.append(int(statistics.stdev(am_mem)))

    print(f"Max. reward: {max_rewards} Exp: {max_exp_rew}")
    print(f"Max. actions: {max_actions} Exp: {max_exp_act}")
    print(f"Max. mem: {max_mem} Exp: {max_exp_mem}")
    print(f"len exps: {len(exps_s)}")
    return mean_rewards, dv_rewards, mean_actions, dv_actions, mean_mem, dv_mem, exps_s
#print(exps_s)

mean_rewards1, dv_rewards1, mean_actions1, dv_actions1, mean_mem1, dv_mem1, exp1 = get_mean_n_std(10, rewards1, exps1, actions1, mem1)
mean_rewards2, dv_rewards2, mean_actions2, dv_actions2, mean_mem2, dv_mem2, exp2 = get_mean_n_std(10, rewards2, exps2, actions2, mem2)

plt.rcParams['font.size'] = '16'

def plot_graphs(title, mean1, dv1, exp, mean2, dv2, max_ticks, step_ticks):
    
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
    ax1.plot(exp, mean1, 'b:', label="DQN-RBF") #color=color
    plt.fill_between(exp,np.array(mean1)-np.array(dv1)/2,np.array(mean1)+np.array(dv1)/2,alpha=.1, color=color)
    #ax1.errorbar(exps_s, mean_actions, dv_actions, marker='x', markersize=5,  lw=1, capsize=5, capthick=1, color=color)
    #ax2 = ax1.twinx()  # instantiate a second axes that shares the same x-axis

    color = 'tab:red'

    #ax2.set_yticks(Y_ticks)
    ax1.set_ylabel(title) # , color=color
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
    plt.savefig(output_folder+title+'.jpg')  

    #plt.show()
exp1 = [0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100]
plot_graphs("Rewards", mean_rewards1, dv_rewards1, exp1, mean_rewards2, dv_rewards2, 950, 20)
plot_graphs("Number of Actions Performed", mean_actions1, dv_actions1, exp1, mean_actions2, dv_actions2, 500, 15)
plot_graphs("Memory Usage", mean_mem1, dv_mem1, exp1, mean_mem2, dv_mem2, 85000,5000)
