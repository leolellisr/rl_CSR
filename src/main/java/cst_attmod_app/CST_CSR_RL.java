/*
 * /*******************************************************************************
 *  * Copyright (c) 2012  DCA-FEEC-UNICAMP
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the GNU Lesser Public License v3
 *  * which accompanies this distribution, and is available at
 *  * http://www.gnu.org/licenses/lgpl.html
 *  * 
 *  * Contributors:
 *  *     K. Raizer, A. L. O. Paraense, R. R. Gudwin - initial API and implementation
 *  ******************************************************************************/
 
package cst_attmod_app;

import br.unicamp.cst.util.viewer.MindViewer;
import outsideCommunication.OutsideCommunication;

import java.io.File;
import java.io.IOException;


/**
 *
 * 
 * @author L. L. Rossi (leolellisr)
 */
public class CST_CSR_RL {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
    	// removing previous .txt files expect QTable
    	File folder = new File(".");
    	for (File f : folder.listFiles()) {
    		if(f.getName().endsWith(".txt") && !(f.getName().endsWith("QTable.txt")) && !(f.getName().endsWith("QTable_CURIOSITY.txt")) && !(f.getName().endsWith("QTable_SURVIVAL.txt"))) {
    			f.delete();
    		}
    	}
        String mode = "exploring";
        int n_tables = 2;
        long seed = 1234;
        OutsideCommunication oc = new OutsideCommunication(50,mode,n_tables,seed);
        oc.start(); 
        //  (OutsideCommunication oc, String mode, String motivation, int num_tables, int print_step)
        AgentMind am = new AgentMind(oc, mode, "drives",n_tables, 10,seed); // OC, mode, Num. QTables,  PrintStep

    }
    
}
