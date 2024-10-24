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
 
package codelets.sensors;

import CommunicationInterface.SensorI;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import outsideCommunication.VirtualBattery;

/**
 *
 * @author L. M. Berto
 * @author L. L. Rossi (leolellisr)
 */
public class Sensor_Battery extends Codelet {
    private MemoryObject battery_read;
    private VirtualBattery battery;
    private int stage;
    private final boolean debug = false;
    public Sensor_Battery(VirtualBattery battery){
        this.battery = battery;
    }

    
    
    
    @Override
    public void accessMemoryObjects() {
        battery_read = (MemoryObject) this.getOutput("BATTERY");
    }

    @Override
    public void calculateActivation() {
        
    }

    @Override
    public void proc() {
        if((boolean)battery.getCharge() && (int)battery.getData()<100) battery.setData((int)battery.getData()+10);
        else if((int)battery.getData()>0) battery.setData((int)battery.getData()-10);
        battery_read.setI(battery.getData());
        if(debug) System.out.println("proc battery:"+(int)battery.getData());

    }
    
}
