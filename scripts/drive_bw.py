import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2
import statistics
import pandas as pd
import pingouin as pg
import seaborn as sns

debug = False
cfile1 = "../results/plot/qtb/2nd/Test/1QTable/profile/nrewards.txt"
sfile1 = "../results/plot/qtb/2nd/Test/1QTable/profile/nrewards.txt"

cfile2 = "../results/plot/qtb/2nd/Test/2QTables/profile/nrewards.txt"
sfile2 = "../results/plot/qtb/2nd/Test/2QTables/profile/nrewards.txt"

cfile1d = "../results/plot/dqn/2nd/Test/1QTable/profile/nrewards.txt"
sfile1d = "../results/plot/dqn/2nd/Test/1QTable/profile/nrewards.txt"

cfile2d = "../results/plot/dqn/2nd/Test/2QTables/profile/nrewards.txt"
sfile2d = "../results/plot/dqn/2nd/Test/2QTables/profile/nrewards.txt"


output_folder = "../results/"

n_exps = 100
n_expsd = 100
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
    "[","]","Exp number:", "Action num: ", "Battery:", "reward: ", "num_tables:",
    "Curiosity_lv: ", "Curiosity_lv:", "Red: ", "Green: ", "Blue: ", "Red:", "Green:", "Blue:","action:","mot_value: ",
    "r_imp: ","g_imp: ","b_imp: ", "hug_drive: ", "cur_drive: ", " QTables:", "cur_a: ", "sur_a: ",
    "Exp:", "Nact:", "Type:", "cur_a:", "sur_a:","exp_c:","exp_s:","dSurV:","SurV:","dCurV:","CurV:",
    "QTables:", "Ri:", "Ri S:", "Ri C:", "G_Reward S:", "G_Reward C:", " LastAct:", "Act C:", "Act S:"
]

def replace(results):
    aux = []
    for element in results:
        for i in range(len(element)):
            if(type(element[i])== str): element[i] = element[i].replace('\n', '')
        aux.append(element)
    return aux


def get_data_drives(file,ind,n_exps):
    # Dicionário para mapear os valores únicos do array aos seus índices
    drives = []
    exps = []
    actions = []
    #if ind == 9: ia = 16
    #else:  ia = 17
    ia = 4
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

                if int(col[1]) < n_exps:
                    #if(len(col)>5): col = col[1:]
                    #print("Exp:"+col[3])
                    actions.append(float(col[ia])) 
                    try:
                    
                        drives[int(col[ia])].append(float(col[ind]))
                    except:
                        print("ia:"+str(ia)+"ac:"+col[ia]+" i:"+str(ind))
                    if debug: print("1Q "+str(ind)+" "+str(int(col[ia]))+" "+str(float(col[ind])))
                               
                    

            

            i+=1
        #print('len act:'+str(len(actions)))
        for j, drive in enumerate(drives):
            
            if len(drive)==0:
                try:
                    drives[j] = np.mean(drives[j-1]) 
                except:
                    print(j)
            else: drives[j] = np.mean(drive)   
            exps.append(j)        
        print("Max actions:"+str(max(actions)))
        try:
            dv = int(statistics.stdev(drives))
        except:
            dv = 0.1
    return [exps, drives,dv] # len 2

def get_data_drives2q(file, ind,n_exps):
    # Dicionário para mapear os valores únicos do array aos seus índices
    drives = []
    exps = []
    actions = []
    #if ind == 9: ia = 16
    #else:  ia = 17
    ia = 4
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

                if int(col[2]) < n_exps and int(col[3]) < n_exps:
                    #if(len(col)>5): col = col[1:]
                    #print("Exp:"+col[3])
                    try:
                        drives[int(col[ia])].append(float(col[ind]))
                        #if debug: print(col)
                        actions.append(float(col[ia]))        
                    except:
                        print('fim exp')
            

            i+=1
        for j, drive in enumerate(drives):
            if len(drive)==0: drives[j] = np.mean(drives[j-1]) 
            else: drives[j] = np.mean(drive)     
            exps.append(j)        

        try:
            print("Max actions:"+str(max(actions)))
        except:
            print('no max act')
        try:
            dv = int(statistics.stdev(drives))
        except:
            dv = 0.1
    return [exps, drives, dv] # len 2

plt.rcParams['font.size'] = '42'

def plot_graphs(title, mean1, exp, mean2, max_ticks, step_ticks):
    
    Y_ticks = [i/10 for i in range(-max_ticks,max_ticks, step_ticks)]
    Y_ticks_act = [i/10 for i in range(-3,max_ticks+2, step_ticks)]
    exp = [expi+1 for expi in exp]
    plt.figure(figsize=(100,40))

    fig, ax1 = plt.subplots(figsize=(100, 40))
    ax1.set_ylim([0, max_ticks/10])
    color = 'tab:blue'
    ax1.set_xlabel('Action')
    
    ax1.set_yticks(Y_ticks_act)
    ax1.tick_params(axis='y') # , labelcolor=color
    ax1.set_ylabel(title)  # we already handled the x-label with ax1
        
    if(len(exp)<90): 
        ax1.set_xticks(exp)
        #print(len(exp))
        #print(len(mean1))
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
    Y_ticks_act = [i/10 for i in range(-3,max_ticks, step_ticks)]
    exp = [expi+1 for expi in exp]
    plt.figure(figsize=(100,40))

    fig, ax1 = plt.subplots(figsize=(100, 40))
    ax1.set_ylim([-0.3, max_ticks/10])
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

remove_strings_from_file(cfile1d, strings_to_remove)
remove_strings_from_file(sfile1d, strings_to_remove)

remove_strings_from_file(cfile2d, strings_to_remove)
remove_strings_from_file(sfile2d, strings_to_remove)

# Main
## Get data
results1s = get_data_drives(sfile1,7,n_exps)
results1c = get_data_drives(cfile1,9,n_exps)
results2s = get_data_drives2q(sfile2,7,n_exps)
results2c = get_data_drives2q(cfile2,9,n_exps)

results1sd = get_data_drives(sfile1d,7,n_expsd)
results1cd = get_data_drives(cfile1d,9,n_expsd)
results2sd = get_data_drives2q(sfile2d,7,n_expsd)
results2cd = get_data_drives2q(cfile2d,9,n_expsd)

m_i = False

if m_i:
    results1s[0][0] = 0
    results1c[0][0] = 0
    results2s[0][0] = 0
    results2c[0][0] = 0


    results1s[1][1] = 0.1
    results1c[1][1] = 0.9
    results2s[1][1] = 0.1
    results2c[1][1] = 0.9

    results1s[1][0] = 0
    results1c[1][0] = 1
    results2s[1][0] = 0
    results2c[1][0] = 1


max_ticks = 10
step_ticks = 1


results2c[1][0] = results2c[1][4]
results2c[1][1] = results2c[1][4]
results2c[1][2] = results2c[1][4]

results2s[1][0]  = results2s[1][4]
results2s[1][1]  = results2s[1][4]
results2s[1][2]  = results2s[1][4]

results1c[1][0] = results1c[1][4]
results1c[1][1] = results1c[1][4]
results1c[1][2] = results1c[1][4]

results1s[1][0]  = results1s[1][4]
results1s[1][1]  = results1s[1][4]
results1s[1][2]  = results1s[1][4]


try:
    plot_graphs("Activation - 1-QTable", results1c[1][:-1], results1c[0][:-1], results1s[1][:-1], max_ticks, step_ticks)
except:
    if(len(results1c[1])<len(results1s[1])):
        cut = len(results1s[1])-len(results1c[1])+1
        plot_graphs("Activation - 1-QTable", results1c[1][:-1], results1c[0][:-1], results1s[1][:-cut], max_ticks, 
                    step_ticks)
    elif (len(results1c[0])>len(results1s[1])):
        cut = len(results1c[0])-len(results1s[1])+1
        plot_graphs("Activation - 1-QTable", results1c[1][:-cut], results1c[0][:-cut], results1s[1][:-1], max_ticks, 
                    step_ticks)



#results2s[1][2]  = results2s[1][3]

try:
    plot_graphs("Activation - 2-QTables", results2c[1][:-1], results2c[0][:-1], results2s[1][:-1], max_ticks, step_ticks)
except:
    if(len(results2c[0])<len(results2s[1])):
        cut = len(results2s[1])-len(results2c[1])+1
        plot_graphs("Activation - 2-QTables", results2c[1][:-1], results2c[0][:-1], results2s[1][:-cut], max_ticks, 
                    step_ticks)
    elif (len(results2c[0])>len(results2s[1])):
        cut = len(results2c[1])-len(results2s[1])+1
        plot_graphs("Activation - 2-QTables", results2c[1][:-cut], results2c[0][:-cut], results2s[1][:-1], max_ticks, 
                    step_ticks)


try:
    stress_a1 = []
    
    for rc,rs in zip(results1c[1][:-1],results1s[1][:-1]):
        stress = rc+rs
        stress_a1.append(stress)
except:
    print("error")
try:    
    dv1 = float(statistics.stdev(stress_a1))
except:
    dv1=0.2

try:
    stress_a2 = []
    for rc,rs in zip(results2c[1][:-1],results2s[1][:-1]):
        stress = rc+rs
        stress_a2.append(stress)
except:
    print("error")
print(max(stress_a1))
print(max(stress_a2))
try:
    dv2=float(statistics.stdev(stress_a2))
except:
    dv2=0.2
max_ticks = 17
step_ticks = 1


try:
    stress_a1d = []
    
    for rc,rs in zip(results1cd[1][:-1],results1sd[1][:-1]):
        stress = rc+rs
        stress_a1d.append(stress)
except:
    print("error")
try:    
    dv1d = float(statistics.stdev(stress_a1d))
except:
    dv1d=0.2

try:
    stress_a2d = []
    for rc,rs in zip(results2cd[1][:-1],results2sd[1][:-1]):
        stress = rc+rs
        stress_a2d.append(stress)
except:
    print("error")
print(max(stress_a1d))
print(max(stress_a2d))
try:
    dv2d=float(statistics.stdev(stress_a2d))
except:
    dv2d=0.2
max_ticks = 17
step_ticks = 1


plot_graphs_stress("Stress", stress_a1, results2c[0][:-1], stress_a2, max_ticks, step_ticks, dv1,dv2)    

v = np.array((stress_a1,stress_a2))


df = pd.DataFrame()
df[["1QTable", "2QTables"]] = v.T

rma = pg.rm_anova(df)
print(rma)

# Function to downsample by picking evenly spaced points
def downsample(data, target_len):
    data = np.array(data)  # Ensure data is a NumPy array
    indices = np.linspace(0, len(data) - 1, target_len, dtype=int)  # Ensure integer indices
    return data[indices]  # Safe indexing

def compare(stress_a1, stress_a2):
    # Ensure both arrays have the same length
    min_len = min(len(stress_a1), len(stress_a2))

    # Create downsampled versions instead of modifying inputs
    stress_a1_ds = downsample(stress_a1, min_len) if len(stress_a1) > min_len else stress_a1
    stress_a2_ds = downsample(stress_a2, min_len) if len(stress_a2) > min_len else stress_a2

    # Debugging: Print lengths to check if they're identical
    print(f"Length stress_a1_ds: {len(stress_a1_ds)}, Length stress_a2_ds: {len(stress_a2_ds)}")

    # Convert to long format for RM-ANOVA
    df = pd.DataFrame({
        "Subject": np.arange(min_len),
        "Agent1": stress_a1_ds,
        "Agent2": stress_a2_ds
    })

    # Verify data consistency
    assert len(df["Agent1"]) == len(df["Agent2"]), "Data length mismatch after downsampling!"

    df_long = df.melt(id_vars=["Subject"], var_name="Agent", value_name="Stress")

    # Run Repeated Measures ANOVA
    rma = pg.rm_anova(data=df_long, dv="Stress", within="Agent", subject="Subject")
    return rma

# Example usage:
print("~~~~~~~~~~~  Compare 1 with 1 ~~~~~~~~~~~~~")
rma = compare(stress_a1, stress_a1d)
print(rma)

print("~~~~~~~~~~~  Compare 2 with 2 ~~~~~~~~~~~~~")
rma = compare(stress_a2, stress_a2d)
print(rma)

# Create a DataFrame for plotting
def create_dataframe(stress_a1, stress_a2):
    time_steps = np.arange(len(stress_a1))
    df = pd.DataFrame({
        "Time Step": np.concatenate([time_steps, time_steps]),
        "Stress": np.concatenate([stress_a1, stress_a2]),
        "Agent": ["Agent 1 (Q-Learning)"] * len(stress_a1) + ["Agent 2 (DQN)"] * len(stress_a2),
    })
    return df

# Generate the data for visualization
df = create_dataframe(downsample(stress_a1, len(stress_a1d)) , stress_a1d)

# Set Seaborn style
sns.set(style="whitegrid")

# Create figure
fig, axes = plt.subplots(1, 2, figsize=(14, 5))

# Line plot
sns.lineplot(data=df, x="Time Step", y="Stress", hue="Agent", ax=axes[0])
axes[0].set_title("Stress Over Time")
axes[0].set_xlabel("Time Step")
axes[0].set_ylabel("Stress Value")

# Box plot
sns.boxplot(data=df, x="Agent", y="Stress", ax=axes[1])
axes[1].set_title("Stress Distribution")
axes[1].set_ylabel("Stress Value")
sns.set(rc={"figure.figsize": (8, 5)})  # Set default size

# Show the plots
plt.tight_layout()
plt.savefig(output_folder+"title1QT"+'.pdf') 


df = create_dataframe(downsample(stress_a2, len(stress_a2d)) , stress_a2d)

# Set Seaborn style
sns.set(style="whitegrid")

# Create figure
fig, axes = plt.subplots(1, 2, figsize=(14, 5))

# Line plot
sns.lineplot(data=df, x="Time Step", y="Stress", hue="Agent", ax=axes[0])
axes[0].set_title("Stress Over Time")
axes[0].set_xlabel("Time Step")
axes[0].set_ylabel("Stress Value")

# Box plot
sns.boxplot(data=df, x="Agent", y="Stress", ax=axes[1])
axes[1].set_title("Stress Distribution")
axes[1].set_ylabel("Stress Value")
sns.set(rc={"figure.figsize": (8, 5)})  # Set default size

# Show the plots
plt.tight_layout()
plt.savefig(output_folder+"title2QT"+'.pdf') 