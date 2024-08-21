import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2
import statistics
debug = False
from matplotlib.ticker import PercentFormatter
file = "results/stage3/rewards.txt"
output_folder = "results/stage3/"
rewards = []
exps = []
actions = []
with open(file,"r") as f: # Open file
    #name = file.split('.')[0] 
    #last_name = name.split('/')[2] 
    data = f.readlines()
    for line in data: 
        col = line.split(' ') 
        rewards.append(int(col[1]))
        exps.append(int(col[2]))
        actions.append(int(col[3]))
#plt.xlabel('Experimento')
#plt.legend([rewards, actions], [, ])

step = 20

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

for st in range(step):
    
    n0 = st*10
    n1 = n0+10
    exps_s.append(n1)
    am_rewards = []
    am_actions = []    
    max_rewards = 0
    max_exp_rew = 0
    max_actions = 0
    max_exp_act = 0
    
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
        am_actions.append(actions[n])
    mean_rewards.append(int(statistics.mean(am_rewards)))
    mean_actions.append(int(statistics.mean(am_actions)))
    dv_rewards.append(int(statistics.stdev(am_rewards)))
    dv_actions.append(int(statistics.stdev(am_actions)))

print(f"Max. reward: {max_rewards} Exp: {max_exp_rew}")
print(f"Max. actions: {max_actions} Exp: {max_exp_act}")
#print(exps_s)

Y_ticks = [i for i in range(0,800, 20)]
Y_ticks_act = [i for i in range(0,800, 20)]

plt.figure(figsize=(25,20))

#fig, axes = plt.subplots(6, 3, figsize=(30, 30))
fig, ax1 = plt.subplots(figsize=(25, 20))

color = 'tab:blue'
ax1.set_xlabel('Experimento')

ax1.set_yticks(Y_ticks_act)
ax1.set_xticks(exps_s)
ax1.tick_params(axis='y', labelcolor=color)
ax1.set_ylabel('Número de ações', color=color)  # we already handled the x-label with ax1
ax1.plot(exps_s, mean_actions, 'b:') #color=color
plt.fill_between(exps_s,np.array(mean_actions)-np.array(dv_actions)/2,np.array(mean_actions)+np.array(dv_actions)/2,alpha=.1, color=color)
#ax1.errorbar(exps_s, mean_actions, dv_actions, marker='x', markersize=5,  lw=1, capsize=5, capthick=1, color=color)
ax2 = ax1.twinx()  # instantiate a second axes that shares the same x-axis

color = 'tab:red'

ax2.set_yticks(Y_ticks)
ax2.set_ylabel('Recompensas', color=color)
ax2.plot(exps_s, mean_rewards, 'r-') #color=color
plt.fill_between(exps_s,np.array(mean_rewards)-np.array(dv_rewards)/2,np.array(mean_rewards)+np.array(dv_rewards)/2,alpha=.1, color=color)
#ax2.errorbar(exps_s, mean_rewards, dv_rewards, marker='+', markersize=5, lw=1, capsize=5, capthick=1, color=color)
ax2.tick_params(axis='y', labelcolor=color)

fig.tight_layout()  # otherwise the right y-label is slightly clipped


#line1, = plt.plot(exps, rewards, 'r', label='Recompensas')
#line2, = plt.plot(exps, actions, 'b', label= 'Número de ações')

# Create a legend for the first line.
#first_legend = plt.legend(handles=[line1], loc='upper right')

# Add the legend manually to the current Axes.
#ax = plt.gca().add_artist(first_legend)

# Create another legend for the second line.
#plt.legend(handles=[line2], loc='lower right')
plt.savefig(output_folder+'rewards.pdf')  

plt.show()
plt.hist(actions, range = (1,500), density=True)
#plt.gca().yaxis.set_major_formatter(PercentFormatter(1))
plt.show() 