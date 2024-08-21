import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2
import statistics
import pandas as pd
debug = False
# Impulses
file1 = "../backup/2nd pack/Test/Impulses/txt/actions.txt"

from colorama import Fore, Back, Style, init

# Inicializa a biblioteca colorama
init()

def quadrado(square_color):
    # Define a cor do quadrado (nesse caso, vermelho)
    # square_color = Back.RED

    # Define o tamanho do quadrado
    square_size = 2

    # Cria o quadrado usando espaços coloridos
    square = square_color + " " * square_size + Style.RESET_ALL

        # Texto antes do quadrado
    text_before_square = " "

    # Texto depois do quadrado
    text_after_square = " "

    # Calcula o espaço restante para centralizar o quadrado
    remaining_space = len(text_before_square) - len(square)

    # Centraliza o quadrado na string
    centered_square = square.center(len(square) + remaining_space)

    # Concatena tudo para formar a string final
    final_string = f"{text_before_square}{centered_square}{text_after_square}"
    print(final_string)
    return final_string

# Drives
file2 = "../backup/2nd pack/Test/Drives/txt/actions.txt"
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
    "r_imp: ","g_imp: ","b_imp: ", "hug_drive: ", "cur_drive: "
]

def replace(results):
    aux = []
    for element in results:
        for i in range(len(element)):
            if(type(element[i])== str): element[i] = element[i].replace('\n', '')
        aux.append(element)
    return aux

m_i = False

def get_data_drives(file,exp):
    n_action = []
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
    am0 =  []
    am1 =  []
    am2 =  []
    am3 =  [] 
    am4 =  []
    am5 =  []
    am6 =  []
    am7 =  []
    am8 =  []
    am9 =  []
    am10 =  []
    am11 =  []
    am12 =  []
    am13 =  []
    aa0 =  []
    aa1 =  []
    aa2 =  []
    am14 =  []
    am15 =  []
    am16 =  []
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
                if(len(col)>12): col = col[1:]
                #print(col[1])
                if(int(col[1])==exp):
                    if m_i:
                        n_action.append(col[8])
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

                        n_action.append(col[8])
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
                        
                    if col[8] == "am0":
                        am0.append(1)
                    else: am0.append(0)  

                    if col[8] == "am1":
                        am1.append(1)
                    else: am1.append(0)  

                    if col[8] == "am2":
                        am2.append(1)
                    else: am2.append(0)  

                    if col[8] == "am3":
                        am3.append(1)
                    else: am3.append(0)  

                    if col[8] == "am4":
                        am4.append(1)
                    else: am4.append(0)  

                    if col[8] == "am5":
                        am5.append(1)
                    else: am5.append(0)  

                    if col[8] == "am6":
                        am6.append(1)
                    else: am6.append(0)  

                    if col[8] == "am7":
                        am7.append(1)
                    else: am7.append(0)  

                    if col[8] == "am8":
                        am8.append(1)
                    else: am8.append(0)  

                    if col[8] == "am9":
                        am9.append(1)
                    else: am9.append(0)  

                    if col[8] == "am10":
                        am10.append(1)
                    else: am10.append(0)  

                    if col[8] == "am11":
                        am11.append(1)
                    else: am11.append(0)  

                    if col[8] == "am12":
                        am12.append(1)
                    else: am12.append(0)  

                    if col[8] == "am13":
                        am13.append(1)
                    else: am13.append(0)  

                    if col[8] == "am14":
                        am14.append(1)
                    else: am14.append(0)  

                    if col[8] == "am15":
                        am15.append(1)
                    else: am15.append(0)  

                    if col[8] == "am16":
                        am16.append(1)
                    else: am16.append(0)    

                    if col[8] == "aa0":
                        aa0.append(1)
                    else: aa0.append(0)    

                    if col[8] == "aa1":
                        aa1.append(1)
                    else: aa1.append(0)    

                    if col[8] == "aa2":
                        aa2.append(1)
                    else: aa2.append(0) 
                    exps.append(i)

                    i+=1
    actions = [am0, am1, am2, am3, am4, am5, am6, am7, am8, am9, am10, am11, am12, am13, am14, am15, am16, aa0, aa1, aa2]                   
    return [n_action, exps, actions, battery, curiosity, r_vl, g_vl, b_vl, mot, mot_hung, mot_cur, actions] # len 12



def get_data_impulses(file, exp):
    am0 =  []
    am1 =  []
    am2 =  []
    am3 =  [] 
    am4 =  []
    am5 =  []
    am6 =  []
    am7 =  []
    am8 =  []
    am9 =  []
    am10 =  []
    am11 =  []
    am12 =  []
    am13 =  []
    aa0 =  []
    aa1 =  []
    aa2 =  []
    am14 =  []
    am15 =  []
    am16 =  []
    n_action = []
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
                #print(col[1])
                if(int(col[1])==exp):
                    if m_i:
                        n_action.append(col[8])
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

                        n_action.append(col[8])
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
                        
                    if col[8] == "am0":
                        am0.append(1)
                    else: am0.append(0)  

                    if col[8] == "am1":
                        am1.append(1)
                    else: am1.append(0)  

                    if col[8] == "am2":
                        am2.append(1)
                    else: am2.append(0)  

                    if col[8] == "am3":
                        am3.append(1)
                    else: am3.append(0)  

                    if col[8] == "am4":
                        am4.append(1)
                    else: am4.append(0)  

                    if col[8] == "am5":
                        am5.append(1)
                    else: am5.append(0)  

                    if col[8] == "am6":
                        am6.append(1)
                    else: am6.append(0)  

                    if col[8] == "am7":
                        am7.append(1)
                    else: am7.append(0)  

                    if col[8] == "am8":
                        am8.append(1)
                    else: am8.append(0)  

                    if col[8] == "am9":
                        am9.append(1)
                    else: am9.append(0)  

                    if col[8] == "am10":
                        am10.append(1)
                    else: am10.append(0)  

                    if col[8] == "am11":
                        am11.append(1)
                    else: am11.append(0)  

                    if col[8] == "am12":
                        am12.append(1)
                    else: am12.append(0)  

                    if col[8] == "am13":
                        am13.append(1)
                    else: am13.append(0)  

                    if col[8] == "am14":
                        am14.append(1)
                    else: am14.append(0)  

                    if col[8] == "am15":
                        am15.append(1)
                    else: am15.append(0)  

                    if col[8] == "am16":
                        am16.append(1)
                    else: am16.append(0)    

                    if col[8] == "aa0":
                        aa0.append(1)
                    else: aa0.append(0)    

                    if col[8] == "aa1":
                        aa1.append(1)
                    else: aa1.append(0)    

                    if col[8] == "aa2":
                        aa2.append(1)
                    else: aa2.append(0)     
                    
                    exps.append(i)

                    i+=1
    actions = [am0, am1, am2, am3, am4, am5, am6, am7, am8, am9, am10, am11, am12, am13, am14, am15, am16, aa0, aa1, aa2] 
    return [n_action, exps, actions, battery, curiosity, r_vl, g_vl, b_vl, mot, mot_r, mot_g, mot_b, actions] # len 13


remove_strings_from_file(file1, strings_to_remove)
remove_strings_from_file(file2, strings_to_remove)



def plot_gantt(n_action1, n_action2, exp1, exp2, mot1, mot2,max_ticks, step_ticks):

    Y_ticks = [i for i in range(0,max_ticks, step_ticks)]
    X_ticks = [i for i in range(0,max(exp1), step_ticks)]
    actions = ["am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", 
               "am13", "am14", "am15", "am16", "aa0", "aa1", "aa2"] 
    actions_l = ["ma0", "ma1", "ma2", "ma3", "ma4", "ma5", "ma6", "ma7", "ma8", "ma9", "ma10", "ma11", "ma12", 
               "ma13", "ma14", "ma15", "ma16", "aa0", "aa1", "aa2"] 
    actions_t = ["ma0: focus", "ma1: neck left", "ma2: neck right", "ma3: head up", "ma4: head down", "ma5: fovea 0", "ma6: fovea 1", "ma7: fovea 2", "ma8: fovea 3", "ma9: fovea 4", "ma10: neck tofocus", "ma11: head tofocus", "ma12: neck awayfocus", 
               "ma13: head awayfocus", "ma14: int. red object", "ma15: int. green object", "ma16: int. blue object", "aa0: focus td color", "aa1: focus td depth", "aa2: focus td region"]
    
    colors = ['tab:gray', 'tab:orange', 'tab:orange', 'tab:purple', 'tab:purple', 'tab:olive', 'tab:olive', 'tab:olive', 'tab:olive', 'tab:olive', 'tab:cyan', 'tab:pink', 'tab:cyan', 'tab:pink', 'tab:red', 'tab:green', 'tab:blue', 'tab:brown', 'tab:brown', 'tab:brown']
    plt.figure(figsize=(25,15))
    
    colors_l = [Back.LIGHTBLACK_EX, Back.LIGHTRED_EX, Back.LIGHTRED_EX, Back.LIGHTMAGENTA_EX, Back.LIGHTMAGENTA_EX, Back.YELLOW, Back.YELLOW, Back.YELLOW, Back.YELLOW, Back.YELLOW, Back.CYAN, Back.MAGENTA, Back.CYAN, Back.MAGENTA, Back.RED, Back.GREEN, Back.BLUE, Back.LIGHTYELLOW_EX, Back.LIGHTYELLOW_EX, Back.LIGHTYELLOW_EX]
    #actions_ti = [f"{action_i} {quadrado(colors_l[i])}" for i,action_i in enumerate(actions_ti) ]
    #print(actions_t)
    fig, ax1 = plt.subplots(nrows=3, ncols=2, figsize=(30, 15), sharex=True)
    ax1[0,0].xaxis.set_ticks_position("top")

    #plt.tight_layout()

    plt.subplot(321)
    ax1[0,0].set_title("Impulses")
    ax1[0,0].set_xlabel('Step')
    ax1[0,0].set_yticks(Y_ticks)
    ax1[0,0].tick_params(axis='y') # , labelcolor=color
    ax1[0,0].set_ylabel("Impulses")  # we already handled the x-label with ax1
    
    ax1[0,0].plot(exp1, mot1[0][0], 'sr:', label="R - Impulses") #color=color
    ax1[0,0].plot(exp1, mot1[1][0], 'og:', label="G - Impulses") #color=color
    ax1[0,0].plot(exp1, mot1[2][0], '^b:', label="B - Impulses") #color=color
    ax1[0,0].set_ylim([0, max_ticks])
    ax1[0,0].set_xlim([0, max(exp1)])
    ax1[0,0].set_xticks(X_ticks, rotation=45)
    ax1[0,0].legend(loc="upper right")

    plt.subplot(323)

    ax1[1,0].set_xlabel('Step')
    ax1[1,0].tick_params(axis='y') # , labelcolor=color
    ax1[1,0].set_ylabel("Gantt_Chart_Actions_Impulses")  # we already handled the x-label with ax1    

    # Agrupando ações iguais
    steps_agrupados = {}
    for acao in actions:
        for i, step in enumerate(n_action1):
            if step == acao:
                if acao in steps_agrupados: 
                    steps_agrupados[acao].extend([exp1[i]])
                else:
                    steps_agrupados[acao] = [exp1[i]]

    if debug: print(steps_agrupados)
# Plotando as barras horizontais
    print(len(steps_agrupados))
    for i, step_group in enumerate(steps_agrupados.values()):
        for step in step_group:
            ax1[1,0].broken_barh([(int(step), 1)], (i - 0.4, 0.8), edgecolor='black', facecolors=colors[i])

    # Configurando o eixo y com as ações agrupadas
    ax1[1,0].set_yticks(range(len(actions)))
    ax1[1,0].set_yticklabels(actions_t)

        # Configurando o eixo x2
    ax1[1,0].set_xticks(X_ticks, rotation=45)
    ax1[1,0].set_xticklabels(X_ticks)  
    ax1[1,0].legend(loc="upper left")
    
    plt.subplot(322)

    ### DRIVES ### 
    ax1[0,1].set_title("Drives")
    ax1[0,1].xaxis.set_ticks_position("top")
    ax1[0,1].set_xlabel('Step')
    ax1[0,1].set_yticks(Y_ticks)
    ax1[0,1].tick_params(axis='y') # , labelcolor=color
    ax1[0,1].set_ylabel("Drives")  # we already handled the x-label with ax1
    mot = [100 - mota for mota in mot2[1][0]]
    ax1[0,1].plot(exp2, mot2[1][0], 'sr:', label="Hunger - Drives") #color=color
    ax1[0,1].plot(exp2, mot, '^b:', label="Curiosity - Drives") #color=color
    ax1[0,1].set_ylim([0, max_ticks])
    ax1[0,1].set_xlim([0, max(exp2)])
    ax1[0,1].set_xticks(X_ticks, rotation=45)
    ax1[0,1].legend(loc="upper right")

    plt.subplot(324)
    ax1[1,1].set_ylabel("Gantt_Chart_Actions_Drives")  # we already handled the x-label with ax1    
     # Agrupando ações iguais
    steps_agrupados = {}
    for acao in actions:
        for i, step in enumerate(n_action2):
            if step == acao:
                if acao in steps_agrupados: 
                    steps_agrupados[acao].extend([exp2[i]])
                else:
                    steps_agrupados[acao] = [exp2[i]]

    if debug: print(steps_agrupados)
# Plotando as barras horizontais
    print(len(steps_agrupados))
    for i, step_group in enumerate(steps_agrupados.values()):
        for step in step_group:
            ax1[1,1].broken_barh([(int(step), 1)], (i - 0.4, 0.8), edgecolor='black', facecolors=colors[i])

    # Configurando o eixo y com as ações agrupadas
    ax1[1,1].set_yticks(range(len(actions)))
    ax1[1,1].set_yticklabels(actions_l)

        # Configurando o eixo x
    ax1[1,1].set_xlabel('Step')
    ax1[1,1].set_xticks(X_ticks, rotation=45)
    ax1[1,1].set_xticklabels(X_ticks)        
    ax1[1,1].legend(loc="upper left")
    plt.subplot(325)
    plt.savefig(output_folder+'Gantt_Chart_Actions_Drives.pdf')      
    plt.close()
   
# Main
## Get data

results1 = get_data_impulses(file1,93)
results2 = get_data_drives(file2,1)

results2 = replace(results2)

print(len(results1))
print(len(results2))


def replace_m(element):
    aux = []
    for i in range(len(element)):
        if(type(element[i])== str): element[i] = float(element[i].replace('\n', ''))
    aux.append(element)
    return aux


results2[8] = replace_m(results2[8])
results2[9] = replace_m(results2[9])
results2[10] = replace_m(results2[10])

if debug:
    print("results2[9]")
    print(results2[9])

    print("results2[10]")
    print(results2[10])

results1[8] = replace_m(results1[8])
results1[9] = replace_m(results1[9])
results1[10] = replace_m(results1[10])
results1[11] = replace_m(results1[11])

#print(results1[12])
i = 0
for action in results1[12]:
    action = replace_m(action)
    results1[12][i] = action
    i += 1    

#print(results2[11])
i = 0
for action in results2[11]:
    action = replace_m(action)
    results2[11][i] = action
    i += 1    

if debug:
    print(results1[0])
    print(results1[1])
    print(results2[0])
plot_gantt(results1[0], results2[0], results1[1], results2[1], [results1[9], results1[10], results1[11]], [results2[8], results2[9]], 130, 10)

if debug: print(len(results2[0]))


def not_used(file):
    act_n = []
    exps = []
    actions = []

    with open(file,"r") as f: # Open file
        data = f.readlines()
        fig, ax1 = plt.subplots()

        color = 'tab:blue'
        
        for line in data: 
            col = line.split(' ') 
            actions.append(int(col[1]))
            exps.append(int(col[2]))
            act_n.append(int(col[3]))
        
        plt.scatter(exps, act_n, c=actions, cmap='gist_ncar', s=0.1, alpha=1)
        plt.legend(actions, bbox_to_anchor = (3 , 6))
        