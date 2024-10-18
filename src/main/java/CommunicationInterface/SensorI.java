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
 
package CommunicationInterface;

/**
 *
 * @author L. M. Berto
 * @author L. L. Rossi (leolellisr)
 */
public interface SensorI {
    public void setExp(int exp);
    public void setExp(int exp, String s);
    public int getExp();
    public int getExp(String s);
    public Object getData();
    public void resetData();
    public int getMaxActions();
    public int getMaxExp();
    
    public int getStage();
public int getAux();
    public void setStage(int stage);
}
