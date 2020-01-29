/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.io.FileInputStream;
import java.lang.*;
import java.lang.reflect.Array;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.File;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Datacenter.VMExecutionTimeB;
import org.cloudbus.cloudsim.Datacenter.VMInfo;
import org.cloudbus.cloudsim.Datacenter.VMsTimeExecution;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * WorkflowApp is an example of AppCloudlet having three communicating tasks. Task A and B sends the
 * data (packet) while Task C receives them
 * 
 * Please refer to following publication for more details:
 * 
 * Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel Applications in Cloud
 * Simulations, Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.
 * 
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 1.0
 */


public class WorkflowApp extends AppCloudlet {

	public static int BumberofCloudletElement = 50;//14
	public static int NumberOfCloudlet = 5000;// make array of cloudlets, we consider maximum 20
	public static int CloudletN[][] = new int[NumberOfCloudlet][BumberofCloudletElement] ; // make array of cloudlets, we consider maximum 20
	public static int CloudletNt[][] = new int[NumberOfCloudlet][BumberofCloudletElement] ; // make array of cloudlets, we consider maximum 20
	public static int NumberOfEdge = 2000 ;// make array of Edges,  we consider Maximum edge is 100
	public static int NumberOfEdgeElement = 8;
	public static int MIPSE = 1; //Energy for executing one unit MIPS
	public static int TranE = 1; //Energy for sending one unit of Output size
	public static int Edge[][] = new int[NumberOfEdge][NumberOfEdgeElement] ; 
	public static int Edget[][] = new int[NumberOfEdge][NumberOfEdgeElement] ; 
	public static int VmSize = 0;
	public static int VmMIPS [][] = new int[1000][4] ; // i consider maximum of VM is 1000
	public static int VmMIPSt [][] = new int[1000][4] ; // i consider maximum of VM is 1000
	
	public static int i = 0; // Number of cloudlet
	public static int j = 0; // Number of Edge
	public static int ACfail = 0;
	public static int SheetNo =0;
	
	public static int VMtoClDone = 0;
	public static int TimeConstraint = 0;
	public static int AssignmentCompleted = 0;
	public static int Fail = 0;
	public static int NoVmFree = 0;
	List<Integer> VmFail = new ArrayList<Integer>();
	
	
	
	

	/* public class VMExecutionTimeB {
		public double start;
		public double end;
		public int Cloudlet;

		public String toString() {
		return "Start: " + start + ", End: " + end + ", Cloudlet:" + Cloudlet ;
		}
		}
*/	
	
	
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public class VMIdEnergy {
int VM;
double Energy;

public String toString() {
return "VM: " + VM + ", Energy: " + Energy;
}
}
public class VMIdMIPS {
int VM;
double MIPS;

public String toString() {
return "VM: " + VM + ", MIPS: " + MIPS;
}
}

public class CloudletMIPS {
int Cloudlet;
double MIPS;

public String toString() {
return "Cloudlet: " + Cloudlet + ", MIPS: " + MIPS;
}
}
public void CostFun2BestFitOneVm (Executiontable IExecutiontable,VMtoCloudletTime IVMtoCloudletTime,VMtoCloudletEnergy IVMtoCloudletEnergy, VMEtoCloudletE IVMEtoCloudletE,VMsTimeExecution IVMsTimeExecution, VMInfo IVMInfo, List<NetworkVm> vmSpecification){
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("-------------------------------------------  Test of My schaduleing    -------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");

// phase 2 checking each task has at least one VM candidate 
if (!IExecutiontable.Atleast1VM()){
Log.printLine(" ****===> This scheduling is imposible <===****");

} else {
Log.printLine(" ****===> At lease there is one VM with enough Energy which can run each tasks  <===****");

}


// phase 3 checking checking sum of Candidate Vm is bigger than total average Energy 

if (IExecutiontable.EnergyIsEnough(IVMInfo, IVMtoCloudletEnergy)){
Log.printLine(" ****===> There is minimum energy to start scheduling <===****");
} else {
Log.printLine(" ****===> There is no enough energy to start scheduling <===****");

}

// phase 4 finding one candidate
IExecutiontable.findOneCandidate(IVMInfo, IVMtoCloudletEnergy, IVMEtoCloudletE);
Log.printLine(" One VM candidate in Phase 4: "+ IExecutiontable.OneVmCandidate );

// phase 5 starting assignment based on rankup
// providing one list then based on that provide sort of cloudlet deceasing based on rank up
ArrayList<Integer> Rankup = new ArrayList<Integer>();
ArrayList<Integer> RankupID = new ArrayList<Integer>();
for (int x =0; x < IVMtoCloudletTime.RankupL.size(); x++ ){
Rankup.add(x, IVMtoCloudletTime.RankupL.get(x));
RankupID.add(x, x);
}
Log.printLine();
Log.printLine("**************************************   Start Of Phase 5   *************************************************");

// Log.printLine(" ****===> orginal Rankup:"+IVMtoCloudletTime.RankupL);
Log.printLine(" ****===> copy Rankup:"+Rankup);
Log.printLine(" ****===> orginal RankupID:"+RankupID);

int temp =0;
int temp1 = 0;
for (int x =0; x < IVMtoCloudletTime.RankupL.size(); x++ ){
for (int y =0; y < IVMtoCloudletTime.RankupL.size(); y++ ){
if (Rankup.get(x)>Rankup.get(y)){
temp = Rankup.get(x);
temp1 = RankupID.get(x);
Rankup.set(x, Rankup.get(y));
RankupID.set(x, RankupID.get(y));
Rankup.set(y, temp);
RankupID.set(y, temp1);
}
}
}
Log.printLine(" ****===> Rankup and ID after Sort:");

Log.printLine(" ****===> copy Rankup:"+Rankup);
Log.printLine(" ****===> orginal RankupID:"+RankupID);
Log.printLine();

Log.printLine();
Log.printLine("VM Energy:");
Log.printLine(IVMInfo.Energy);




// This for loop of assignment of phase 5, based on rank up decreasment
int mintimevmID = 0;
double executionT = 0.0;
double StartT = 0.0;
double FinishT = 0.0; 
int VMID = 0;

// Assign all cloudlet based on rankup which sorted
for (int x =0; x < (IVMtoCloudletTime.RankupL.size()); x++ ){
//if Candidate Vm size is equal to 1 assign  
if (!(IExecutiontable.OneVmCandidate.get(RankupID.get(x))== 9999)){
VMID = IExecutiontable.OneVmCandidate.get(RankupID.get(x));
Log.printLine(" Cloudlet ID:"+RankupID.get(x) + "has one candidate with VMID:" + VMID);


//Assign Cloudlet to VM(after this VMID is clear)

CloudletN[RankupID.get(x)][8] = VMID;
//CloudletN[RankupID.get(x)][12] = (int) (IVMtoCloudletTime.VMtoCloudletTime.get(RankupID.get(x)).get(VMID));
CloudletN[RankupID.get(x)][12] = (int) (IVMtoCloudletTime.VMtoCloudletExecTime.get(RankupID.get(x)).get(VMID));
CloudletN[RankupID.get(x)][13] = 1;

//Updating VMexecutionTable
executionT = IVMtoCloudletTime.VMtoCloudletTime.get(RankupID.get(x)).get(VMID);
StartT = IExecutiontable.EST(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime);
FinishT = IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime);

Log.printLine("VMID"+VMID);


IExecutiontable.StartTime.set(RankupID.get(x), StartT);
IExecutiontable.FinishTime.set(RankupID.get(x), FinishT);
IExecutiontable.ExecutionTime.set(RankupID.get(x), executionT);
IExecutiontable.SelVm.set(RankupID.get(x), VMID);

Log.printLine();
Log.printLine("##################################  Checking **  ** Checking ############################################");

Log.printLine("Start time    : " + IExecutiontable.StartTime);

Log.printLine("Finish time   : " + IExecutiontable.FinishTime);

Log.printLine("Execution time: " + IExecutiontable.ExecutionTime);

Log.printLine("SelVm         : " + IExecutiontable.SelVm);

Log.printLine("##################################  Checking **  ** Checking ############################################");

Log.printLine();

//Update VmsTimeExecution
Datacenter.VMExecutionTimeB IVMExecutionTimeB = new Datacenter.VMExecutionTimeB();
IVMExecutionTimeB.start = StartT;
IVMExecutionTimeB.end = FinishT;
IVMExecutionTimeB.Cloudlet = RankupID.get(x);
IVMsTimeExecution.VMsSchedule2.get(VMID).add(IVMExecutionTimeB);

//Log.printLine();
//Log.printLine("VMsSchedule2 is equal to:");
//Log.printLine(IVMsTimeExecution.VMsSchedule2);
//Log.printLine();

Log.printLine();
Log.printLine("VM Energy:");
Log.printLine(IVMInfo.Energy);



}else{//Candidate Vm size more than 1, need to find Vm with minimum EFT

Log.printLine(" Cloudlet :"+RankupID.get(x) + " has more than one candidate need to fine minimum EFT");



//find VMID with minimum of y			

double minenergy =99999999999999.9 ;


int selected = 0;
int VmEnergy =0;
int ExecutionEnergy = 0;
selected = 0;
for(int m =0; m <IExecutiontable.CandidateVmBC.get(RankupID.get(x)).size();m++){//
VMID = IExecutiontable.CandidateVmBC.get(RankupID.get(x)).get(m);

VmEnergy = (IVMInfo.Energy.get(VMID));
ExecutionEnergy = IVMtoCloudletEnergy.VMtoCloudletEnergy.get(RankupID.get(x)).get(VMID);
Log.printLine("VmEnergy["+VMID+"]: "+VmEnergy);
Log.printLine("ExecutionEnergy: "+ExecutionEnergy);
Log.printLine("VmEnergy - ExecutionEnergy: "+(VmEnergy-ExecutionEnergy) );

if((minenergy > (VmEnergy-ExecutionEnergy))&((VmEnergy-ExecutionEnergy)>0) ){//
selected = 1;
minenergy = VmEnergy-ExecutionEnergy ;
mintimevmID = VMID; 
}
}
if(selected == 0){
Log.printLine("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"
+     "	cloudletID: " + RankupID.get(x)+" It's fail     "
+     "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");


CloudSim.terminateSimulation();
} else {
Log.printLine("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% cloudletID: " + RankupID.get(x)+"assign to VMID: "+ mintimevmID); 
}

/*
//find VMID with minimum of EFT
double mintime =99999.9 ;
for(int m =0; m <IExecutiontable.CandidateVmBC.get(RankupID.get(x)).size();m++){
VMID = IExecutiontable.CandidateVmBC.get(RankupID.get(x)).get(m);
executionT = IVMtoCloudletTime.VMtoCloudletTime.get(RankupID.get(x)).get(VMID);
StartT = IExecutiontable.ESTofCloudlet(RankupID.get(x), IVMEtoCloudletE, IVMtoCloudletTime);
//Log.printLine(" find VMID with minimum of EFT, VMID: "+ VMID + "is eual to" + IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime));
//Log.printLine("mintime: "+mintime);
if(mintime > IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime)){

mintime = IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime);
mintimevmID = VMID; 
}
}

*/
//Assign Cloudlet to VM(after this VMID is clear)
VMID = mintimevmID;
CloudletN[RankupID.get(x)][8] = VMID;
//CloudletN[RankupID.get(x)][12] = (int) (IVMtoCloudletTime.VMtoCloudletTime.get(RankupID.get(x)).get(VMID));
CloudletN[RankupID.get(x)][12] = (int) (IVMtoCloudletTime.VMtoCloudletExecTime.get(RankupID.get(x)).get(VMID));
CloudletN[RankupID.get(x)][13] = 1;

//Updating VMexecutionTable
executionT = IVMtoCloudletTime.VMtoCloudletTime.get(RankupID.get(x)).get(VMID);
StartT = IExecutiontable.EST(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime);
FinishT = IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime);

IExecutiontable.StartTime.set(RankupID.get(x), StartT);
IExecutiontable.FinishTime.set(RankupID.get(x), FinishT);
IExecutiontable.ExecutionTime.set(RankupID.get(x), executionT);
IExecutiontable.SelVm.set(RankupID.get(x), VMID);


//Update VmsTimeExecution
Datacenter.VMExecutionTimeB IVMExecutionTimeB = new Datacenter.VMExecutionTimeB();
IVMExecutionTimeB.start = StartT;
IVMExecutionTimeB.end = FinishT;
IVMExecutionTimeB.Cloudlet = RankupID.get(x);

IVMsTimeExecution.VMsSchedule2.get(VMID).add(IVMExecutionTimeB);

//Energy deduction and VMcandidate and one candidate
EnergyCandidateupdate(VMID, RankupID.get(x), IVMInfo, IVMtoCloudletEnergy, IVMEtoCloudletE,IExecutiontable);

Log.printLine();
Log.printLine("##################################  Checking **  ** Checking ############################################");

Log.printLine("Start time    : " + IExecutiontable.StartTime);

Log.printLine("Finish time   : " + IExecutiontable.FinishTime);

Log.printLine("Execution time: " + IExecutiontable.ExecutionTime);

Log.printLine("SelVm         : " + IExecutiontable.SelVm);

Log.printLine("##################################  Checking **  ** Checking ############################################");

Log.printLine();

Log.printLine();
Log.printLine("VMsSchedule2 is equal to:");
for(int rr =0; rr< IVMsTimeExecution.VMsSchedule2.size();rr++){
Log.printLine(IVMsTimeExecution.VMsSchedule2.get(rr));
}

Log.printLine();

Log.printLine();
Log.printLine("VM Energy:");
Log.printLine(IVMInfo.Energy);
/*
Log.printLine("VME/CloudletE:");
Log.printLine(IVMEtoCloudletE.VMEtoCloudletE);
Log.printLine();
*/
}
}
}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void Costfun5678Scheduling (Executiontable IExecutiontable,VMtoCloudletTime IVMtoCloudletTime,VMtoCloudletEnergy IVMtoCloudletEnergy, VMEtoCloudletE IVMEtoCloudletE,VMsTimeExecution IVMsTimeExecution, VMInfo IVMInfo, List<NetworkVm> vmSpecification){
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("-------------------------------------------  Test of HEST Plus schaduleing    -------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");

// providing one list then based on that provide sort of cloudlet deceasing based on rank up
ArrayList<Integer> Rankup = new ArrayList<Integer>();
ArrayList<Integer> RankupID = new ArrayList<Integer>();
for (int x =0; x < IVMtoCloudletTime.RankupL.size(); x++ ){
Rankup.add(x, IVMtoCloudletTime.RankupL.get(x));
RankupID.add(x, x);
}
Log.printLine();
Log.printLine("**************************************   Start Of Phase 5   *************************************************");

Log.printLine(" ****===> orginal Rankup:"+IVMtoCloudletTime.RankupL);
Log.printLine(" ****===> copy Rankup:"+Rankup);
Log.printLine(" ****===> orginal RankupID:"+RankupID);

int temp =0;
int temp1 = 0;
for (int x =0; x < IVMtoCloudletTime.RankupL.size(); x++ ){
for (int y =0; y < IVMtoCloudletTime.RankupL.size(); y++ ){
if (Rankup.get(x)>Rankup.get(y)){
temp = Rankup.get(x);
temp1 = RankupID.get(x);
Rankup.set(x, Rankup.get(y));
RankupID.set(x, RankupID.get(y));
Rankup.set(y, temp);
RankupID.set(y, temp1);
}
}
}
Log.printLine(" ****===> Rankup and ID after Sort:");

Log.printLine(" ****===> copy Rankup:"+Rankup);
Log.printLine(" ****===> orginal RankupID:"+RankupID);
Log.printLine();

// This for loop of assignment of phase 5, based on rank up decreasment
int mintimevmID = 0;
double executionT = 0.0;
double StartT = 0.0;
double FinishT = 0.0; 
int VMID = 0;

// Assign all cloudlet based on rankup which sorted
for (int x =0; x < (IVMtoCloudletTime.RankupL.size()); x++ ){
//find VMID with minimum of EFT
double mintime =999999999999999999.9 ;
double VmEnergy = 0;
double ExecutionEnergy =0;
double EFT =0;
int select = 0;
for(int m =0; m <IExecutiontable.CandidateVmBC.get(RankupID.get(x)).size();m++){//
VMID = IExecutiontable.CandidateVmBC.get(RankupID.get(x)).get(m);
executionT = IVMtoCloudletTime.VMtoCloudletTime.get(RankupID.get(x)).get(VMID);
StartT = IExecutiontable.ESTofCloudlet(RankupID.get(x), IVMEtoCloudletE, IVMtoCloudletTime);
VmEnergy = (IVMInfo.Energy.get(VMID));
ExecutionEnergy = IVMtoCloudletEnergy.VMtoCloudletEnergy.get(RankupID.get(x)).get(VMID);
EFT = IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime);
//Log.printLine(" find VMID with minimum of EFT, VMID: "+ VMID + "is eual to" + IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime));
//Log.printLine("mintime: "+mintime);

//CostF5=> (((EFT*ExecutionEnergy)/(VmEnergy-ExecutionEnergy)))
//CostF6=> (((EFT*ExecutionEnergy)*(VmEnergy-ExecutionEnergy))/1000000)
//CostF7=> ((EFT*ExecutionEnergy)*((VmEnergy-ExecutionEnergy)/VmEnergy))
//CostF8=> ((EFT*ExecutionEnergy)*(1-((VmEnergy-ExecutionEnergy)/VmEnergy)))
//CostF9=> ExecutionEnergy
double CostFun =((EFT*ExecutionEnergy)*((VmEnergy-ExecutionEnergy)/VmEnergy));

if((mintime > (CostFun))&&((VmEnergy-ExecutionEnergy)>0.0)){//
select = 1;//Math.cbrt
mintime = CostFun;
mintimevmID = VMID; 
}
}
if(select==0){
CloudSim.terminateSimulation();
}
//Assign Cloudlet to VM(after this VMID is clear)
VMID = mintimevmID;
CloudletN[RankupID.get(x)][8] = VMID;
//CloudletN[RankupID.get(x)][12] = (int) (IVMtoCloudletTime.VMtoCloudletTime.get(RankupID.get(x)).get(VMID));
CloudletN[RankupID.get(x)][12] = (int) (IVMtoCloudletTime.VMtoCloudletExecTime.get(RankupID.get(x)).get(VMID));
CloudletN[RankupID.get(x)][13] = 1;

//Updating VMexecutionTable
executionT = IVMtoCloudletTime.VMtoCloudletTime.get(RankupID.get(x)).get(VMID);
StartT = IExecutiontable.EST(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime);
FinishT = IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime);

IExecutiontable.StartTime.set(RankupID.get(x), StartT);
IExecutiontable.FinishTime.set(RankupID.get(x), FinishT);
IExecutiontable.ExecutionTime.set(RankupID.get(x), executionT);
IExecutiontable.SelVm.set(RankupID.get(x), VMID);


//Update VmsTimeExecution
Datacenter.VMExecutionTimeB IVMExecutionTimeB = new Datacenter.VMExecutionTimeB();
IVMExecutionTimeB.start = StartT;
IVMExecutionTimeB.end = FinishT;
IVMExecutionTimeB.Cloudlet = RankupID.get(x);

IVMsTimeExecution.VMsSchedule2.get(VMID).add(IVMExecutionTimeB);

//Energy deduction and VMcandidate and one candidate
VmEnergy = (IVMInfo.Energy.get(VMID));
ExecutionEnergy = IVMtoCloudletEnergy.VMtoCloudletEnergy.get(RankupID.get(x)).get(VMID);
//IVMInfo.Energy.set(VMID, (int) (VmEnergy-ExecutionEnergy));
EnergyCandidateupdatePlus(VMID, RankupID.get(x), IVMInfo, IVMtoCloudletEnergy, IVMEtoCloudletE,IExecutiontable);

Log.printLine();
Log.printLine("##################################  Checking **  ** Checking ############################################");

Log.printLine("Start time    : " + IExecutiontable.StartTime);

Log.printLine("Finish time   : " + IExecutiontable.FinishTime);

Log.printLine("Execution time: " + IExecutiontable.ExecutionTime);

Log.printLine("SelVm         : " + IExecutiontable.SelVm);

Log.printLine("##################################  Checking **  ** Checking ############################################");

Log.printLine();

Log.printLine();
Log.printLine("VM Energy:");
Log.printLine(IVMInfo.Energy);

/*
Log.printLine();
Log.printLine("VMsSchedule2 is equal to:");
Log.printLine(IVMsTimeExecution.VMsSchedule2);
Log.printLine();



Log.printLine("VME/CloudletE:");
Log.printLine(IVMEtoCloudletE.VMEtoCloudletE);
Log.printLine();
*/

}
}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void CostFun3HESTScheduling (Executiontable IExecutiontable,VMtoCloudletTime IVMtoCloudletTime,VMtoCloudletEnergy IVMtoCloudletEnergy, VMEtoCloudletE IVMEtoCloudletE,VMsTimeExecution IVMsTimeExecution, VMInfo IVMInfo, List<NetworkVm> vmSpecification){
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("-------------------------------------------  Test of HEST schaduleing    -------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");

// providing one list then based on that provide sort of cloudlet deceasing based on rank up
ArrayList<Integer> Rankup = new ArrayList<Integer>();
ArrayList<Integer> RankupID = new ArrayList<Integer>();
for (int x =0; x < IVMtoCloudletTime.RankupL.size(); x++ ){
Rankup.add(x, IVMtoCloudletTime.RankupL.get(x));
RankupID.add(x, x);
}
Log.printLine();
Log.printLine("**************************************   Start    *************************************************");

Log.printLine(" ****===> orginal Rankup:"+IVMtoCloudletTime.RankupL);
Log.printLine(" ****===> copy Rankup:"+Rankup);
Log.printLine(" ****===> orginal RankupID:"+RankupID);

int temp =0;
int temp1 = 0;
for (int x =0; x < IVMtoCloudletTime.RankupL.size(); x++ ){
for (int y =0; y < IVMtoCloudletTime.RankupL.size(); y++ ){
if (Rankup.get(x)>Rankup.get(y)){
temp = Rankup.get(x);
temp1 = RankupID.get(x);
Rankup.set(x, Rankup.get(y));
RankupID.set(x, RankupID.get(y));
Rankup.set(y, temp);
RankupID.set(y, temp1);
}
}
}
Log.printLine(" ****===> Rankup and ID after Sort:");

Log.printLine(" ****===> copy Rankup:"+Rankup);
Log.printLine(" ****===> orginal RankupID:"+RankupID);
Log.printLine();

// This for loop of assignment of phase 5, based on rank up decreasment
int mintimevmID = 0;
double executionT = 0.0;
double StartT = 0.0;
double FinishT = 0.0; 
int VMID = 0;

// Assign all cloudlet based on rankup which sorted
for (int x =0; x < (IVMtoCloudletTime.RankupL.size()); x++ ){
//if Candidate Vm size is equal to 1 assign 
//find VMID with minimum of EFT
double mintime =99999.9 ;
for(int m =0; m <VmSize;m++){
VMID = m;
executionT = IVMtoCloudletTime.VMtoCloudletTime.get(RankupID.get(x)).get(VMID);
StartT = IExecutiontable.ESTofCloudlet(RankupID.get(x), IVMEtoCloudletE, IVMtoCloudletTime);
//Log.printLine(" find VMID with minimum of EFT, VMID: "+ VMID + "is eual to" + IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime));
//Log.printLine("mintime: "+mintime);
if(mintime > IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime)){

mintime = IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime);
mintimevmID = VMID; 
}
}
//Assign Cloudlet to VM(after this VMID is clear)
VMID = mintimevmID;
CloudletN[RankupID.get(x)][8] = VMID;
//CloudletN[RankupID.get(x)][12] = (int) (IVMtoCloudletTime.VMtoCloudletTime.get(RankupID.get(x)).get(VMID));
CloudletN[RankupID.get(x)][12] = (int) (IVMtoCloudletTime.VMtoCloudletExecTime.get(RankupID.get(x)).get(VMID));
CloudletN[RankupID.get(x)][13] = 1;

//Updating VMexecutionTable
executionT = IVMtoCloudletTime.VMtoCloudletTime.get(RankupID.get(x)).get(VMID);
StartT = IExecutiontable.EST(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime);
FinishT = IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime);

IExecutiontable.StartTime.set(RankupID.get(x), StartT);
IExecutiontable.FinishTime.set(RankupID.get(x), FinishT);
IExecutiontable.ExecutionTime.set(RankupID.get(x), executionT);
IExecutiontable.SelVm.set(RankupID.get(x), VMID);


//Update VmsTimeExecution
Datacenter.VMExecutionTimeB IVMExecutionTimeB = new Datacenter.VMExecutionTimeB();
IVMExecutionTimeB.start = StartT;
IVMExecutionTimeB.end = FinishT;
IVMExecutionTimeB.Cloudlet = RankupID.get(x);

IVMsTimeExecution.VMsSchedule2.get(VMID).add(IVMExecutionTimeB);

Log.printLine();
Log.printLine("##################################  Checking **  ** Checking ############################################");

Log.printLine("Start time    : " + IExecutiontable.StartTime);

Log.printLine("Finish time   : " + IExecutiontable.FinishTime);

Log.printLine("Execution time: " + IExecutiontable.ExecutionTime);

Log.printLine("SelVm         : " + IExecutiontable.SelVm);

Log.printLine("##################################  Checking **  ** Checking ############################################");

Log.printLine();
/*
Log.printLine();
Log.printLine("VMsSchedule2 is equal to:");
Log.printLine(IVMsTimeExecution.VMsSchedule2);
Log.printLine();

Log.printLine();
Log.printLine("VM Energy:");
Log.printLine(IVMInfo.Energy);

Log.printLine("VME/CloudletE:");
Log.printLine(IVMEtoCloudletE.VMEtoCloudletE);
Log.printLine();
*/

}
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



public void CostFun4HESTSchedulingPlus (Executiontable IExecutiontable,VMtoCloudletTime IVMtoCloudletTime,VMtoCloudletEnergy IVMtoCloudletEnergy, VMEtoCloudletE IVMEtoCloudletE,VMsTimeExecution IVMsTimeExecution, VMInfo IVMInfo, List<NetworkVm> vmSpecification){
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("-------------------------------------------  Test of HEST Plus schaduleing    -------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");

// providing one list then based on that provide sort of cloudlet deceasing based on rank up
ArrayList<Integer> Rankup = new ArrayList<Integer>();
ArrayList<Integer> RankupID = new ArrayList<Integer>();
for (int x =0; x < IVMtoCloudletTime.RankupL.size(); x++ ){
Rankup.add(x, IVMtoCloudletTime.RankupL.get(x));
RankupID.add(x, x);
}
Log.printLine();
Log.printLine("**************************************   Start Of Phase 5   *************************************************");

Log.printLine(" ****===> orginal Rankup:"+IVMtoCloudletTime.RankupL);
Log.printLine(" ****===> copy Rankup:"+Rankup);
Log.printLine(" ****===> orginal RankupID:"+RankupID);

int temp =0;
int temp1 = 0;
for (int x =0; x < IVMtoCloudletTime.RankupL.size(); x++ ){
for (int y =0; y < IVMtoCloudletTime.RankupL.size(); y++ ){
if (Rankup.get(x)>Rankup.get(y)){
temp = Rankup.get(x);
temp1 = RankupID.get(x);
Rankup.set(x, Rankup.get(y));
RankupID.set(x, RankupID.get(y));
Rankup.set(y, temp);
RankupID.set(y, temp1);
}
}
}
Log.printLine(" ****===> Rankup and ID after Sort:");

Log.printLine(" ****===> copy Rankup:"+Rankup);
Log.printLine(" ****===> orginal RankupID:"+RankupID);
Log.printLine();

// This for loop of assignment of phase 5, based on rank up decreasment
int mintimevmID = 9999;
double executionT = 0.0;
double StartT = 0.0;
double FinishT = 0.0; 
int VMID = 0;

// Assign all cloudlet based on rankup which sorted
for (int x =0; x < (IVMtoCloudletTime.RankupL.size()); x++ ){

//find VMID with minimum of EFT
double mintime =99999.9 ;
for(int m =0; m <IExecutiontable.CandidateVmBC.get(RankupID.get(x)).size();m++){
VMID = IExecutiontable.CandidateVmBC.get(RankupID.get(x)).get(m);
executionT = IVMtoCloudletTime.VMtoCloudletTime.get(RankupID.get(x)).get(VMID);
StartT = IExecutiontable.ESTofCloudlet(RankupID.get(x), IVMEtoCloudletE, IVMtoCloudletTime);
//Log.printLine(" find VMID with minimum of EFT, VMID: "+ VMID + "is eual to" + IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime));
//Log.printLine("mintime: "+mintime);
if(mintime > IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime)){

mintime = IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime);
mintimevmID = VMID; 
}
}
//Assign Cloudlet to VM(after this VMID is clear)
if (!(mintimevmID == 9999)){
	VMID = mintimevmID;
}else{
	CloudSim.terminateSimulation();
}

CloudletN[RankupID.get(x)][8] = VMID;
//CloudletN[RankupID.get(x)][12] = (int) (IVMtoCloudletTime.VMtoCloudletTime.get(RankupID.get(x)).get(VMID));
CloudletN[RankupID.get(x)][12] = (int) (IVMtoCloudletTime.VMtoCloudletExecTime.get(RankupID.get(x)).get(VMID));
CloudletN[RankupID.get(x)][13] = 1;

//Updating VMexecutionTable
executionT = IVMtoCloudletTime.VMtoCloudletTime.get(RankupID.get(x)).get(VMID);
StartT = IExecutiontable.EST(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime);
FinishT = IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime);

IExecutiontable.StartTime.set(RankupID.get(x), StartT);
IExecutiontable.FinishTime.set(RankupID.get(x), FinishT);
IExecutiontable.ExecutionTime.set(RankupID.get(x), executionT);
IExecutiontable.SelVm.set(RankupID.get(x), VMID);


//Update VmsTimeExecution
Datacenter.VMExecutionTimeB IVMExecutionTimeB = new Datacenter.VMExecutionTimeB();
IVMExecutionTimeB.start = StartT;
IVMExecutionTimeB.end = FinishT;
IVMExecutionTimeB.Cloudlet = RankupID.get(x);

IVMsTimeExecution.VMsSchedule2.get(VMID).add(IVMExecutionTimeB);

//Energy deduction and VMcandidate and one candidate
EnergyCandidateupdatePlus(VMID, RankupID.get(x), IVMInfo, IVMtoCloudletEnergy, IVMEtoCloudletE,IExecutiontable);

Log.printLine();
Log.printLine("##################################  Checking **  ** Checking ############################################");

Log.printLine("Start time    : " + IExecutiontable.StartTime);

Log.printLine("Finish time   : " + IExecutiontable.FinishTime);

Log.printLine("Execution time: " + IExecutiontable.ExecutionTime);

Log.printLine("SelVm         : " + IExecutiontable.SelVm);

Log.printLine("##################################  Checking **  ** Checking ############################################");

Log.printLine();
Log.printLine("VM Energy:");
Log.printLine(IVMInfo.Energy);

Log.printLine();
/*
Log.printLine();
Log.printLine("VMsSchedule2 is equal to:");
Log.printLine(IVMsTimeExecution.VMsSchedule2);
Log.printLine();

Log.printLine();
Log.printLine("VM Energy:");
Log.printLine(IVMInfo.Energy);

Log.printLine("VME/CloudletE:");
Log.printLine(IVMEtoCloudletE.VMEtoCloudletE);
Log.printLine();
*/

}
}



public void CostFun1BestFit (Executiontable IExecutiontable,VMtoCloudletTime IVMtoCloudletTime,VMtoCloudletEnergy IVMtoCloudletEnergy, VMEtoCloudletE IVMEtoCloudletE,VMsTimeExecution IVMsTimeExecution, VMInfo IVMInfo, List<NetworkVm> vmSpecification){
//Log.printLine("------------------------------------------------------------------------------------------------------------------------");
//Log.printLine("------------------------------------------------------------------------------------------------------------------------");
//Log.printLine("-------------------------------------------  Test of BeestFit schaduleing    -------------------------------------------------");
//Log.printLine("------------------------------------------------------------------------------------------------------------------------");
//Log.printLine("------------------------------------------------------------------------------------------------------------------------");

// providing one list then based on that provide sort of cloudlet deceasing based on rank up
ArrayList<Integer> Rankup = new ArrayList<Integer>();
ArrayList<Integer> RankupID = new ArrayList<Integer>();
for (int x =0; x < IVMtoCloudletTime.RankupL.size(); x++ ){
Rankup.add(x, IVMtoCloudletTime.RankupL.get(x));
RankupID.add(x, x);
}

//Log.printLine(" ****===> orginal Rankup:"+IVMtoCloudletTime.RankupL);
// Log.printLine(" ****===> copy Rankup:"+Rankup);
// Log.printLine(" ****===> orginal RankupID:"+RankupID);

int temp =0;
int temp1 = 0;
for (int x =0; x < IVMtoCloudletTime.RankupL.size(); x++ ){
for (int y =0; y < IVMtoCloudletTime.RankupL.size(); y++ ){
if (Rankup.get(x)>Rankup.get(y)){
temp = Rankup.get(x);
temp1 = RankupID.get(x);
Rankup.set(x, Rankup.get(y));
RankupID.set(x, RankupID.get(y));
Rankup.set(y, temp);
RankupID.set(y, temp1);
}
}
}
// Log.printLine(" ****===> Rankup and ID after Sort:");
// 
/// Log.printLine(" ****===> copy Rankup:"+Rankup);
// Log.printLine(" ****===> orginal RankupID:"+RankupID);
// Log.printLine();

// This for loop of assignment of phase 5, based on rank up decreasment
int mintimevmID = 0;
double executionT = 0.0;
double StartT = 0.0;
double FinishT = 0.0; 
int VMID = 0;

// Assign all cloudlet based on rankup which sorted
for (int x =0; x < (IVMtoCloudletTime.RankupL.size()); x++ ){
//find VMID with minimum of EFT
double minenergy =99999999999.9 ;
int selected = 0;
int VmEnergy =0;
int ExecutionEnergy = 0;
selected = 0;
for(int m =0; m <VmSize;m++){
VMID = m;

VmEnergy = (IVMInfo.Energy.get(VMID));
ExecutionEnergy = IVMtoCloudletEnergy.VMtoCloudletEnergy.get(RankupID.get(x)).get(VMID);
//Log.printLine("VmEnergy["+VMID+"]: "+VmEnergy);
//Log.printLine("ExecutionEnergy: "+ExecutionEnergy);
//Log.printLine("VmEnergy - ExecutionEnergy: "+(VmEnergy-ExecutionEnergy) );

if((minenergy > (VmEnergy-ExecutionEnergy))&((VmEnergy-ExecutionEnergy)>0) ){
selected = 1;
minenergy = VmEnergy-ExecutionEnergy ;
mintimevmID = VMID; 
}
}
if(selected == 0){
Log.printLine("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"
+     "	cloudletID: " + RankupID.get(x)+" It's fail     "
+     "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
pressAnyKeyToContinue();
pressAnyKeyToContinue();
CloudSim.terminateSimulation();
} else {
Log.printLine("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% cloudletID: " + RankupID.get(x)+"assign to VMID: "+ mintimevmID); 
}
//Assign Cloudlet to VM(after this VMID is clear)
VMID = mintimevmID;
CloudletN[RankupID.get(x)][8] = VMID;
//CloudletN[RankupID.get(x)][12] = (int) (IVMtoCloudletTime.VMtoCloudletTime.get(RankupID.get(x)).get(VMID));
CloudletN[RankupID.get(x)][12] = (int) (IVMtoCloudletTime.VMtoCloudletExecTime.get(RankupID.get(x)).get(VMID));
CloudletN[RankupID.get(x)][13] = 1;

//Updating VMexecutionTable
executionT = IVMtoCloudletTime.VMtoCloudletTime.get(RankupID.get(x)).get(VMID);
StartT = IExecutiontable.EST(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime);
FinishT = IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime);

IExecutiontable.StartTime.set(RankupID.get(x), StartT);
IExecutiontable.FinishTime.set(RankupID.get(x), FinishT);
IExecutiontable.ExecutionTime.set(RankupID.get(x), executionT);
IExecutiontable.SelVm.set(RankupID.get(x), VMID);


//Update VmsTimeExecution
Datacenter.VMExecutionTimeB IVMExecutionTimeB = new Datacenter.VMExecutionTimeB();

IVMExecutionTimeB.start = StartT;
IVMExecutionTimeB.end = FinishT;
IVMExecutionTimeB.Cloudlet = RankupID.get(x);

IVMsTimeExecution.VMsSchedule2.get(VMID).add(IVMExecutionTimeB);

//Energy deduction and VMcandidate and one candidate
IVMInfo.Energy.set(VMID, (int)minenergy) ;
//EnergyCandidateupdatePlus(VMID, RankupID.get(x), IVMInfo, IVMtoCloudletEnergy, IVMEtoCloudletE,IExecutiontable);


//Log.printLine();
//Log.printLine("##################################  Checking **  ** Checking ############################################");

//	Log.printLine("Start time    : " + IExecutiontable.StartTime);
//	
//	Log.printLine("Finish time   : " + IExecutiontable.FinishTime);

//	Log.printLine("Execution time: " + IExecutiontable.ExecutionTime);

//	Log.printLine("SelVm         : " + IExecutiontable.SelVm);

//	Log.printLine("VmEnergy         : " + IVMInfo.Energy);

//	Log.printLine("##################################  Checking **  ** Checking ############################################");

//	Log.printLine();


}
}
		
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//This function is deduct energy and checking is there any new one candidate and update candidate list  
	public void EnergyCandidateupdatePlus(int vmid, int cloudlet, VMInfo IVMInfo,VMtoCloudletEnergy IVMtoCloudletEnergy, VMEtoCloudletE IVMEtoCloudletE,Executiontable IExecutiontable){
		Log.printLine();
		/*
		Log.printLine();
		Log.printLine("##########################################################################################################################");
		Log.printLine("##########################################      checking current  candidate   ############################################");
		Log.printLine("##########################################################################################################################");
		Log.printLine();
		Log.printLine("##########################################            CandidateVmBC           ############################################");
		Log.printLine(IExecutiontable.CandidateVmBC);
		Log.printLine("##########################################         CandidateCloudletBVm       ############################################");
		Log.printLine(IExecutiontable.CandidateCloudletBVm);
		Log.printLine("##########################################            OneVmCandidate          ############################################");
		Log.printLine(IExecutiontable.OneVmCandidate);
		Log.printLine("############################      IVMtoCloudletEnergy.VMtoCloudletEnergy      ############################################");
		Log.printLine(IVMtoCloudletEnergy.VMtoCloudletEnergy);
		Log.printLine("############################          IVMEtoCloudletE.VMEtoCloudletE          ############################################");
		Log.printLine(IVMEtoCloudletE.VMEtoCloudletE);
		Log.printLine("############################                  IVMInfo.Energy                  ############################################");
		Log.printLine( IVMInfo.Energy);
		Log.printLine( );
		Log.printLine( );
		Log.printLine( );
		*/
		Log.printLine( "############################              Start of EnergyCandidateupdate() Function          ##########################################");
			//Deduct the amount of energy needed for processing cloudlet from Vm Energy
			//Log.printLine("IVMInfo.Energy.get("+vmid+") ="+IVMInfo.Energy.get(vmid));
			//Log.printLine("IVMtoCloudletEnergy.VMtoCloudletEnergy.get("+cloudlet+").get("+vmid+")="+IVMtoCloudletEnergy.VMtoCloudletEnergy.get(cloudlet).get(vmid));
			IVMInfo.Energy.set(vmid,IVMInfo.Energy.get(vmid) - IVMtoCloudletEnergy.VMtoCloudletEnergy.get(cloudlet).get(vmid));
			//Log.printLine("after energy deduction IVMInfo.Energy.get("+vmid+") ="+IVMInfo.Energy.get(vmid));

			//updating VmEtoCloudletE
			IVMEtoCloudletE.VMEtoCloudletE.get(cloudlet).set(vmid, (double)(IVMInfo.Energy.get(vmid)/IVMtoCloudletEnergy.VMtoCloudletEnergy.get(cloudlet).get(vmid)));

			// checking with current energy this vm can process other cloudlet
			//Log.printLine("checking with current energy this vm can process other cloudlet");
			//Log.printLine("CandidateCloudletBVm.get("+vmid+").size() = "+CandidateCloudletBVm.get(vmid).size());
			ArrayList<Integer> temp = new ArrayList<Integer>();
			temp.clear();
			//Log.printLine("temp before run : " + temp ); 
			for (int y =0 ; y < IExecutiontable.CandidateCloudletBVm.get(vmid).size(); y++){


				//Log.printLine("CandidateCloudletB	Vm.get("+vmid+").("+y+") = "+CandidateCloudletBVm.get(vmid).get(y));
				//Log.printLine("IVMInfo.Energy.get("+vmid+") = " + IVMInfo.Energy.get(vmid));
				//Log.printLine("IVMtoCloudletEnergy.VMtoCloudletEnergy.get("+CandidateCloudletBVm.get(vmid).get(y)+").get("+vmid+") = " + IVMtoCloudletEnergy.VMtoCloudletEnergy.get(CandidateCloudletBVm.get(vmid).get(y)).get(vmid));
				if(!(IExecutiontable.CandidateCloudletBVm.get(vmid).get(y)==9999)){
				if ((IExecutiontable.CandidateCloudletBVm.get(vmid).get(y) == i) ||
						(IVMInfo.Energy.get(vmid)< IVMtoCloudletEnergy.VMtoCloudletEnergy.get(IExecutiontable.CandidateCloudletBVm.get(vmid).get(y)).get(vmid)) ){
					// remove vmid from vm's candidate of cloudlet
					for (int a =0; a < IExecutiontable.CandidateVmBC.get(IExecutiontable.CandidateCloudletBVm.get(vmid).get(y)).size(); a++){
						if (IExecutiontable.CandidateVmBC.get(IExecutiontable.CandidateCloudletBVm.get(vmid).get(y)).get(a) == vmid){
							IExecutiontable.CandidateVmBC.get(IExecutiontable.CandidateCloudletBVm.get(vmid).get(y)).remove(a);
							a = IExecutiontable.CandidateVmBC.get(IExecutiontable.CandidateCloudletBVm.get(vmid).get(y)).size() +100 ;
						}

					}
					//remove cloudlet from candidate cloudlt list
					temp.add(IExecutiontable.CandidateCloudletBVm.get(vmid).get(y));
				}
				}
			}
			//Log.printLine("temp : " + temp ); 
			for (int z=0; z< temp.size();z++){
				for (int l =0; l < IExecutiontable.CandidateCloudletBVm.get(vmid).size(); l++){
					if (temp.get(z)== IExecutiontable.CandidateCloudletBVm.get(vmid).get(l) )
						IExecutiontable.CandidateCloudletBVm.get(vmid).remove(l);
				}
			}



/*	
Log.printLine("######################        After algorithem finding one candidate run      ############################################");
Log.printLine();
Log.printLine("##########################################            CandidateVmBC           ############################################");
Log.printLine(IExecutiontable.CandidateVmBC);
Log.printLine("##########################################         CandidateCloudletBVm       ############################################");
Log.printLine(IExecutiontable.CandidateCloudletBVm);
Log.printLine("##########################################            OneVmCandidate          ############################################");
Log.printLine(IExecutiontable.OneVmCandidate);
Log.printLine("############################      IVMtoCloudletEnergy.VMtoCloudletEnergy      ############################################");
Log.printLine(IVMtoCloudletEnergy.VMtoCloudletEnergy);
Log.printLine("############################          IVMEtoCloudletE.VMEtoCloudletE          ############################################");
Log.printLine(IVMEtoCloudletE.VMEtoCloudletE);
Log.printLine("############################                  IVMInfo.Energy                  ############################################");
Log.printLine( IVMInfo.Energy);
*/
			
}//end of function

	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//This function is deduct energy and checking is there any new one candidate and update candidate list  
	public void EnergyCandidateupdate(int vmid, int cloudlet, VMInfo IVMInfo,VMtoCloudletEnergy IVMtoCloudletEnergy, VMEtoCloudletE IVMEtoCloudletE,Executiontable IExecutiontable){
		Log.printLine();
		/*
		Log.printLine();
		Log.printLine("##########################################################################################################################");
		Log.printLine("##########################################      checking current  candidate   ############################################");
		Log.printLine("##########################################################################################################################");
		Log.printLine();
		Log.printLine("##########################################            CandidateVmBC           ############################################");
		Log.printLine(IExecutiontable.CandidateVmBC);
		Log.printLine("##########################################         CandidateCloudletBVm       ############################################");
		Log.printLine(IExecutiontable.CandidateCloudletBVm);
		Log.printLine("##########################################            OneVmCandidate          ############################################");
		Log.printLine(IExecutiontable.OneVmCandidate);
		Log.printLine("############################      IVMtoCloudletEnergy.VMtoCloudletEnergy      ############################################");
		Log.printLine(IVMtoCloudletEnergy.VMtoCloudletEnergy);
		Log.printLine("############################          IVMEtoCloudletE.VMEtoCloudletE          ############################################");
		Log.printLine(IVMEtoCloudletE.VMEtoCloudletE);
		Log.printLine("############################                  IVMInfo.Energy                  ############################################");
		Log.printLine( IVMInfo.Energy);
		Log.printLine( );
		Log.printLine( );
		Log.printLine( );
		*/
		Log.printLine( "############################              Start of EnergyCandidateupdate() Function          ##########################################");
			//Deduct the amount of energy needed for processing cloudlet from Vm Energy
			//Log.printLine("IVMInfo.Energy.get("+vmid+") ="+IVMInfo.Energy.get(vmid));
			//Log.printLine("IVMtoCloudletEnergy.VMtoCloudletEnergy.get("+cloudlet+").get("+vmid+")="+IVMtoCloudletEnergy.VMtoCloudletEnergy.get(cloudlet).get(vmid));
			//if (!(cloudlet == 0) ){
			IVMInfo.Energy.set(vmid,IVMInfo.Energy.get(vmid) - IVMtoCloudletEnergy.VMtoCloudletEnergy.get(cloudlet).get(vmid));
			//}
			//Log.printLine("after energy deduction IVMInfo.Energy.get("+vmid+") ="+IVMInfo.Energy.get(vmid));

			//updating VmEtoCloudletE
			IVMEtoCloudletE.VMEtoCloudletE.get(cloudlet).set(vmid, (double)(IVMInfo.Energy.get(vmid)/IVMtoCloudletEnergy.VMtoCloudletEnergy.get(cloudlet).get(vmid)));

			// checking with current energy this vm can process other cloudlet
			//Log.printLine("checking with current energy this vm can process other cloudlet");
			//Log.printLine("CandidateCloudletBVm.get("+vmid+").size() = "+CandidateCloudletBVm.get(vmid).size());
			ArrayList<Integer> temp = new ArrayList<Integer>();
			temp.clear();
			//Log.printLine("temp before run : " + temp ); 
			for (int y =0 ; y < IExecutiontable.CandidateCloudletBVm.get(vmid).size(); y++){


				//Log.printLine("CandidateCloudletVm.get("+vmid+").("+y+") = "+IExecutiontable.CandidateCloudletBVm.get(vmid).get(y));
				//Log.printLine("IVMInfo.Energy.get("+vmid+") = " + IVMInfo.Energy.get(vmid));
				
				//Log.printLine("IVMtoCloudletEnergy.VMtoCloudletEnergy.get("+IExecutiontable.CandidateCloudletBVm.get(vmid).get(y)+").get("+vmid+") = " );
				//Log.print(IVMtoCloudletEnergy.VMtoCloudletEnergy.get(IExecutiontable.CandidateCloudletBVm.get(vmid).get(y)).get(vmid));
				
				if (!(IExecutiontable.CandidateCloudletBVm.get(vmid).get(y) == 9999)){
				if ((IExecutiontable.CandidateCloudletBVm.get(vmid).get(y) == i) ||
						(IVMInfo.Energy.get(vmid)< IVMtoCloudletEnergy.VMtoCloudletEnergy.get(IExecutiontable.CandidateCloudletBVm.get(vmid).get(y)).get(vmid)) ){
					// remove vmid from vm's candidate of cloudlet
					for (int a =0; a < IExecutiontable.CandidateVmBC.get(IExecutiontable.CandidateCloudletBVm.get(vmid).get(y)).size(); a++){
						if (IExecutiontable.CandidateVmBC.get(IExecutiontable.CandidateCloudletBVm.get(vmid).get(y)).get(a) == vmid){
							IExecutiontable.CandidateVmBC.get(IExecutiontable.CandidateCloudletBVm.get(vmid).get(y)).remove(a);
							a = IExecutiontable.CandidateVmBC.get(IExecutiontable.CandidateCloudletBVm.get(vmid).get(y)).size() +100 ;
						}

					}
					//remove cloudlet from candidate cloudlt list
					temp.add(IExecutiontable.CandidateCloudletBVm.get(vmid).get(y));
				}
				}//end
			}
			//Log.printLine("temp : " + temp ); 
			for (int z=0; z< temp.size();z++){
				for (int l =0; l < IExecutiontable.CandidateCloudletBVm.get(vmid).size(); l++){
					if (temp.get(z)== IExecutiontable.CandidateCloudletBVm.get(vmid).get(l) )
						IExecutiontable.CandidateCloudletBVm.get(vmid).remove(l);
				}
			}
/*			
			for (int z=0; z< i;z++){
				//for (int l =0; l < IExecutiontable.CandidateVmBC.size() ; l++){
					if (1 == IExecutiontable.CandidateVmBC.get(z).size() ){
						int VMID = IExecutiontable.CandidateVmBC.get(z).get(0);
						int CLOUDLETID = z;
						IExecutiontable.OneVmCandidate.set(CLOUDLETID, VMID);
						//IVMInfo.Energy.set(VMID,IVMInfo.Energy.get(VMID) - IVMtoCloudletEnergy.VMtoCloudletEnergy.get(CLOUDLETID).get(VMID));
					}
				//}
			}
*/

	
Log.printLine("######################        After algorithem finding one candidate run      ############################################");
Log.printLine();
Log.printLine("##########################################            CandidateVmBC           ############################################");
Log.printLine(IExecutiontable.CandidateVmBC);
Log.printLine("##########################################         CandidateCloudletBVm       ############################################");
Log.printLine(IExecutiontable.CandidateCloudletBVm);
Log.printLine("##########################################            OneVmCandidate          ############################################");
Log.printLine(IExecutiontable.OneVmCandidate);
/*
Log.printLine("############################      IVMtoCloudletEnergy.VMtoCloudletEnergy      ############################################");
Log.printLine(IVMtoCloudletEnergy.VMtoCloudletEnergy);
Log.printLine("############################          IVMEtoCloudletE.VMEtoCloudletE          ############################################");
Log.printLine(IVMEtoCloudletE.VMEtoCloudletE);
Log.printLine("############################                  IVMInfo.Energy                  ############################################");
Log.printLine( IVMInfo.Energy);
*/
			
// In this part is for checking there is new one candidate or no same function atleatonecandidate
IExecutiontable.findOneCandidate(IVMInfo, IVMtoCloudletEnergy, IVMEtoCloudletE);



}//end of function
	
	
	
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////// This Class is main class for keeping all cloudlet execution info. ////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public class Executiontable {
		public ArrayList<ArrayList<Integer>> PerviousNodes = new ArrayList<ArrayList<Integer>>(); 
		public ArrayList<ArrayList<Integer>> NextNodes = new ArrayList<ArrayList<Integer>>(); 

		public ArrayList<Double> StartTime = new ArrayList<Double>();
		public ArrayList<Double> FinishTime = new ArrayList<Double>(); 
		public ArrayList<Double> ExecutionTime = new ArrayList<Double>(); 
		public ArrayList<Double> Deadline = new ArrayList<Double>();
		public ArrayList<Double> WorstCase = new ArrayList<Double>();
		public ArrayList<Integer> SelVm = new ArrayList<Integer>();

		
		//List of VM which have enough energy to run this program
		public ArrayList<ArrayList<Integer>> CandidateVmBC = new ArrayList<ArrayList<Integer>>(); 
		public ArrayList<Integer> OneVmCandidate = new ArrayList<Integer>(); 

		public ArrayList<ArrayList<Integer>> CandidateCloudletBVm = new ArrayList<ArrayList<Integer>>(); 

		public ArrayList<Integer> temp = new ArrayList<Integer>();
		public ArrayList<Integer> tempd = new ArrayList<Integer>();

		int tempw = 0;
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/// after selecting VM by scheduler using this function to update execution table

		public void ExeTableVMupdate(int Vm,int CloudletN,Executiontable IExecutiontable, VMtoCloudletEnergy IVMtoCloudletEnergy, VMInfo IVMInfo, VMEtoCloudletE IVMEtoCloudletE,VMtoCloudletTime IVMtoCloudletTime,VMsTimeExecution IVMsTimeExecution){
			ArrayList<Integer> temp = new ArrayList<Integer>();
			
			Log.printLine();
			Log.printLine("##################################  Checking ** Update before selecting ** Checking ############################################");
			
			Log.printLine("Start time: " + StartTime);
			
			Log.printLine("Finish time: " + FinishTime);
			
			Log.printLine("Deadline: " + Deadline);
			
			Log.printLine("Execution time: " + ExecutionTime);

			
			Log.printLine("##################################  Checking ** Update before selecting ** Checking ############################################");

			Log.printLine();
			
			SelVm.set(CloudletN, Vm);
			Log.printLine("### SelVm :"+SelVm);

			ExecutionTime.set(CloudletN,(double) (IVMtoCloudletTime.VMtoCloudletTime.get(CloudletN).get(Vm) ));
			Log.printLine("### ExecutionTime: "+ExecutionTime);

			FinishTime.set(CloudletN, (StartTime.get(CloudletN)+ExecutionTime.get(CloudletN)));
			Log.printLine("### FinishTime: "+FinishTime);
			double deadlinetemp = 99999999999999.0;
			if (CloudletN == i){
				Deadline.set(CloudletN, (Deadline.get(CloudletN)-ExecutionTime.get(CloudletN)));

			} else {
				for (int x=0;x < NextNodes.get(CloudletN).size();x++){
					if (deadlinetemp > Deadline.get(NextNodes.get(CloudletN).get(x))){
						deadlinetemp = Deadline.get(NextNodes.get(CloudletN).get(x))- ExecutionTime.get(CloudletN);
					}
					Deadline.set(CloudletN, deadlinetemp);
				}
			}
			Log.printLine("### Deadline: "+Deadline);
			
			// current node shall be add to VmExecution table
			
			Datacenter.VMExecutionTimeB VmExeT = new Datacenter.VMExecutionTimeB();

			VmExeT.start = StartTime.get(CloudletN);
			VmExeT.end = FinishTime.get(CloudletN);
			VmExeT.Cloudlet = CloudletN;
			//VmExeT.SetVMExecutionTimeB(StartTime.get(CloudletN), FinishTime.get(CloudletN), CloudletN);
			//insert new object based on start time
			int added = 0;
			int counter = 0;
			Log.printLine("TEEEEEEEEEEEEEEEEEEEEEEEEESSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSST:"+IVMsTimeExecution.VMsSchedule2.get(Vm).size());
			Log.printLine("VMsSchedule2:"+IVMsTimeExecution.VMsSchedule2);
			int NoCInVm = IVMsTimeExecution.VMsSchedule2.get(Vm).size();

			while ((added == 0) & ((counter < NoCInVm))){

				Log.printLine("Before :"+IVMsTimeExecution.VMsSchedule2.get(Vm));
				
				if (counter ==0) {
					if (VmExeT.start<IVMsTimeExecution.VMsSchedule2.get(Vm).get(counter).start){
						/*if (NoCInVm ==1){
							//Log.printLine("IVMsTimeExecution.VMsSchedule2.get("+Vm+").set("+counter+", "+VmExeT+");" );
							// IVMsTimeExecution.VMsSchedule2.get(Vm).set(counter, VmExeT);
							//added = 1;
						}else {*/
							IVMsTimeExecution.VMsSchedule2.get(Vm).add(counter, VmExeT);
							Log.printLine("IVMsTimeExecution.VMsSchedule2.get(Vm).add(counter, VmExeT);" );
							added = 1;
						//}
					}
				}else {
					if (!(IVMsTimeExecution.VMsSchedule2.get(Vm).get(counter).start -  IVMsTimeExecution.VMsSchedule2.get(Vm).get(counter-1).end < (VmExeT.end-VmExeT.start))){
						IVMsTimeExecution.VMsSchedule2.get(Vm).add(counter, VmExeT);
						Log.printLine("IVMsTimeExecution.VMsSchedule2.get(Vm).add(counter, VmExeT);" );
						added = 1;
					}
				}
				counter++;
			}
			if(added ==0){
				Log.printLine("out of loop adding, still didn't added" );
				if (!(IVMsTimeExecution.VMsSchedule2.get(Vm).get(counter-1).end > VmExeT.start)){
					IVMsTimeExecution.VMsSchedule2.get(Vm).add((counter), VmExeT);
				}
			}
			Log.printLine("after :"+IVMsTimeExecution.VMsSchedule2.get(Vm));
			Log.printLine();
			Log.printLine("after :"+IVMsTimeExecution.VMsSchedule2);
			Log.printLine();
			
			Log.printLine("    Updating next nodes" );

			
			// update next nodes
			temp.clear(); 
			
			double Ctemp = 0;
			temp.add(CloudletN);// this list keep all list of next nodes
			while(!temp.isEmpty()){
				tempw = temp.remove(0);
				//Log.printLine("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% New Round %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%: ");
				//Log.printLine("### tempw: "+tempw);
				
				if(!(NextNodes.get(tempw).get(0)== 3000)){// there is next node
					for (int x=0;x < NextNodes.get(tempw).size();x++){// for all next nodes
						//checking other receiving finishing time for finding maximum date ready
						Ctemp = (FinishTime.get(tempw));
						//Log.printLine("### Ctemp: "+Ctemp);

						for (int y=0;y < PerviousNodes.get(NextNodes.get(tempw).get(x)).size();y++){
							if (!(PerviousNodes.get(NextNodes.get(tempw).get(x)).get(y)== tempw)){
								if (FinishTime.get(PerviousNodes.get(NextNodes.get(tempw).get(x)).get(y)) > Ctemp){
									Ctemp = FinishTime.get(PerviousNodes.get(NextNodes.get(tempw).get(x)).get(y));
									//Log.printLine("### FinishTime.get("+ PerviousNodes.get(NextNodes.get(tempw).get(x))+").get("+y+"):"+Ctemp);

								}
							}
						}
						/*
						VMExecutionTimeB VmExeTT = new VMExecutionTimeB();
						int clouldletidtemp = 999999;
						Vm = SelVm.get(NextNodes.get(tempw).get(x));
						for (int cc =0; cc < IVMsTimeExecution.VMsSchedule2.get(Vm).size() ; cc++){
							if (IVMsTimeExecution.VMsSchedule2.get(Vm).get(cc).Cloudlet == NextNodes.get(tempw).get(x)){
								VmExeTT = IVMsTimeExecution.VMsSchedule2.get(Vm).remove(cc);
								clouldletidtemp = cc;
							}
						}
						
						if (IVMsTimeExecution.VMisFree(Vm,NextNodes.get(tempw).get(x), Ctemp, ExecutionTime.get(NextNodes.get(tempw).get(x)), Deadline.get(NextNodes.get(tempw).get(x)), IVMsTimeExecution.VMsSchedule2)){
							StartTime.set(NextNodes.get(tempw).get(x),Ctemp);
							//Log.printLine("### StartTime.set("+NextNodes.get(tempw).get(x)+","+Ctemp+")");
							FinishTime.set(NextNodes.get(tempw).get(x), (StartTime.get(NextNodes.get(tempw).get(x))+ ExecutionTime.get(NextNodes.get(tempw).get(x))));
							
							
							//************ In this phase we should update VMschedule**************
							
							
							VmExeT.start = StartTime.get(NextNodes.get(tempw).get(x));
							VmExeT.end = FinishTime.get(NextNodes.get(tempw).get(x));
							VmExeT.Cloudlet = NextNodes.get(tempw).get(x);
							//VmExeT.SetVMExecutionTimeB(StartTime.get(CloudletN), FinishTime.get(CloudletN), CloudletN);
							//insert new object based on start time
							added = 0;
							counter = 0;
							Log.printLine("IVMsTimeExecution.VMsSchedule2.get("+Vm+").size(): "+IVMsTimeExecution.VMsSchedule2.get(Vm).size());
							Log.printLine("VMsSchedule2:"+IVMsTimeExecution.VMsSchedule2);
							NoCInVm = IVMsTimeExecution.VMsSchedule2.get(Vm).size();
							Log.printLine("Before :"+IVMsTimeExecution.VMsSchedule2.get(Vm));
							while ((added == 0) & ((counter < NoCInVm))){
								if (counter ==0) {
									if (VmExeT.start<IVMsTimeExecution.VMsSchedule2.get(Vm).get(counter).start){
											IVMsTimeExecution.VMsSchedule2.get(Vm).add(counter, VmExeT);
											Log.printLine("IVMsTimeExecution.VMsSchedule2.get(Vm).add(counter, VmExeT);" );
											added = 1;
										}
								}else {
									if (!(IVMsTimeExecution.VMsSchedule2.get(Vm).get(counter).start -  IVMsTimeExecution.VMsSchedule2.get(Vm).get(counter-1).end < (VmExeT.end-VmExeT.start))){
										IVMsTimeExecution.VMsSchedule2.get(Vm).add(counter, VmExeT);
										Log.printLine("IVMsTimeExecution.VMsSchedule2.get(Vm).add(counter, VmExeT);" );
										added = 1;
									}
								}
								counter++;
							}
							if(added ==0){
								Log.printLine("out of loop adding, still didn't added" );
								if (!(IVMsTimeExecution.VMsSchedule2.get(Vm).get(counter-1).end > VmExeT.start)){
									IVMsTimeExecution.VMsSchedule2.get(Vm).add((counter), VmExeT);
								}
							}
							Log.printLine("after :"+IVMsTimeExecution.VMsSchedule2.get(Vm));
							Log.printLine();
							Log.printLine("after :"+IVMsTimeExecution.VMsSchedule2);
							Log.printLine();
							
						}else {
							IVMsTimeExecution.VMsSchedule2.get(Vm).add(clouldletidtemp, VmExeTT); 
						}
						*/
						temp.add(NextNodes.get(tempw).get(x));
						
						/*
						// we should update execution time on VMTime
						VmExeT.start = StartTime.get(NextNodes.get(tempw).get(x));
						VmExeT.end = FinishTime.get(NextNodes.get(tempw).get(x));
						VmExeT.Cloudlet = tempw;
						Vm = SelVm.get(tempw);
						//insert new object based on start time
						added = 0;
						counter = 0;
						NoCInVm = IVMsTimeExecution.VMsSchedule2.get(Vm).size();
						
						Log.printLine(" Updating other nodes, Cloudlet "+tempw + ", Vm" + Vm + "VM.size" + NoCInVm  );

						while (!((added == 1) & (counter > NoCInVm))){
							counter++;
							if (VmExeT.start<IVMsTimeExecution.VMsSchedule2.get(Vm).get(counter).start){
								IVMsTimeExecution.VMsSchedule2.get(Vm).add(counter, VmExeT);
								added = 1;
							}
						}
						if(added ==0){
							IVMsTimeExecution.VMsSchedule2.get(Vm).add((counter+1), VmExeT);

						}
						*/

					}
				}
			}
			// After selection and assignment, Energy of VM shall be deduct and VMEtoCloudletE should updated 
			double Energyt = 0.0;
			Energyt = IVMInfo.Energy.get(Vm);
			
			Energyt = Energyt - IVMtoCloudletEnergy.VMtoCloudletEnergy.get(CloudletN).get(Vm) ;
			IVMInfo.Energy.set(Vm,(int) Energyt);
			IVMEtoCloudletE.VMEtoCloudletE.get(CloudletN).set(Vm, (double) (IVMInfo.Energy.get(Vm)/IVMtoCloudletEnergy.VMtoCloudletEnergy.get(CloudletN).get(Vm)));
			
			/*
			for (int cx =0; cx < (i+1); cx++){
				for (int Vx =0; Vx < IExecutiontable.CandidateVmBC.get(cx).size();Vx++ ){
					if(IExecutiontable.CandidateVmBC.get(cx).get(Vx)== Vm){
						if ((IVMInfo.Energy.get(Vx)- IVMtoCloudletEnergy.VMtoCloudletEnergy.get(cx).get(Vx))< 0){
							IExecutiontable.CandidateVmBC.get(cx).remove(Vx);
							Log.printLine("Energy of VM No.:" + Vx+ " is not enough to execute Cloudlet No.:"+ cx);

						}
						
					}
				}
			}
			*/
			
			
			Log.printLine();
			Log.printLine("##################################  Checking ** Update After selecting ** Checking ############################################");
			
			Log.printLine("Start time: " + StartTime);
			
			Log.printLine("Finish time: " + FinishTime);
			
			Log.printLine("Deadline: " + Deadline);
			
			Log.printLine("Execution time: " + ExecutionTime);

			
			Log.printLine("##################################  Checking ** Update After selecting ** Checking ############################################");

			Log.printLine();	
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//This function is for Initialing the data
		public void NPintial(){
			ArrayList<Integer> temp = new ArrayList<Integer>(); 
			temp.add(3000);
			//
			//create list for later puting info
			for (int j=0; j <(i+1);j++){
				PerviousNodes.add(j, new ArrayList<Integer>(temp));
				NextNodes.add(j, new ArrayList<Integer>(temp));				
				SelVm.add(j, 3000);
				
			}
			/*
			Log.printLine();
			Log.printLine("##################################  Checking ** VM information ** Checking ############################################");
			
			Log.printLine("intial state next node: " + NextNodes);
			
			Log.printLine("intial state perviouse node: " + PerviousNodes);
			
			Log.printLine("##################################  Checking ** VM information ** Checking ############################################");

			Log.printLine();	
			*/
		}
		
		//This function is return Estimated Start time of cloudlet
				public double ESTofCloudlet(int cloudlet,VMtoCloudletTime IVMtoCloudletTime){
					double maxtime =0;
					if (cloudlet == 0){
						StartTime.add(0 , 0.0);
						return 0.0 ; // finish time and start time is 0.0 for first node
					} else {
						for (int x =0; x < PerviousNodes.get(cloudlet).size();x++){
							if (FinishTime.get(PerviousNodes.get(cloudlet).get(x)) > maxtime){
								maxtime = FinishTime.get(PerviousNodes.get(cloudlet).get(x)) ;
							}
						}
						StartTime.add(cloudlet , maxtime);// maximum finish time of previsou node is start time current node
						return maxtime;
					}
				}
		
				
		//This function is return Estimated Start time of cloudlet with considering VMs
				public double EST(int cloudlet,int Vm,double StartT, double executionT,VMsTimeExecution IVMsTimeExecution, VMEtoCloudletE IVMEtoCloudletE,VMtoCloudletTime IVMtoCloudletTime){
					 //Log.printLine(" ****===> ESTofCloudlet is equal :" +ESTofCloudlet(cloudlet,IVMEtoCloudletE,IVMtoCloudletTime));
					 //Log.printLine(" ****===> VMisFree is equal :" +IVMsTimeExecution.VMisFree(Vm, cloudlet, StartT, executionT, 0.0,IVMsTimeExecution.VMsSchedule2));

					return Math.max(ESTofCloudlet(cloudlet,IVMEtoCloudletE,IVMtoCloudletTime),
							IVMsTimeExecution.VMisFree(Vm, cloudlet, StartT, executionT, 0.0,IVMsTimeExecution.VMsSchedule2));
				}
		
		//This function is return Estimated finish time of cloudlet with considering VMs
				public double EFT(int cloudlet,int Vm,double StartT, double executionT,VMsTimeExecution IVMsTimeExecution, VMEtoCloudletE IVMEtoCloudletE,VMtoCloudletTime IVMtoCloudletTime){
					double temp = 0.0 ;
					temp = EST(cloudlet,Vm,StartT,executionT,IVMsTimeExecution, IVMEtoCloudletE,IVMtoCloudletTime);
					// Log.printLine(" ****===> EST is equal :" +temp);
					 //Log.printLine(" ****===> Weight is equal :" +IVMtoCloudletTime.weight.get(cloudlet));

					return (temp +IVMtoCloudletTime.VMtoCloudletTime.get(cloudlet).get(Vm));
				}
				
		//This function is for Initialing the data, for finding VmCandidate and cloudletcandidate
		public void Intial(VMEtoCloudletE IVMEtoCloudletE,VMtoCloudletTime IVMtoCloudletTime){
			// Initial CandidateCloudletBVm for further use
			for (int x =0; x < (VmSize ); x++){
				ArrayList<Integer> AAA = new ArrayList<Integer>();
				AAA.add(9999);
				CandidateCloudletBVm.add(x, new ArrayList<Integer>(AAA));
				//Log.printLine("CandidateCloudletBVm.add("+x+", "+AAA+")");
			}
			//initial OnVmCandidate for further use
			for (int x =0;x < (i+1); x++){
				
				OneVmCandidate.add(x, 9999);
				
			}
			
			// Initial StartT,FinishT,Deadline,SelVM for further use
						for (int x =0; x < (i+1 ); x++){
							StartTime.add(x, 0.0);
							FinishTime.add(x, 0.0);
							ExecutionTime.add(x, 0.0);
							Deadline.add(x, 0.0);
							SelVm.add(x, 0);
							//Log.printLine("CandidateCloudletBVm.add("+x+", "+AAA+")");
						}
			
			// Finding Vm which have enough energy to run Cloudlet and add them to CandidateVmBC and CandidateCloudletBVm
			for (int Ccounter =0;Ccounter < (i+1); Ccounter++){
				temp.clear();
				//if (Ccounter == 0){      // this part is when we have start node sepreate from DAG
				//	temp.add(0);     
			//	} else {
						for (int Vmcounter =0;Vmcounter < (VmSize); Vmcounter++){
							if (IVMEtoCloudletE.VMEtoCloudletE.get(Ccounter).get(Vmcounter) >= 1 ) {
								temp.add(Vmcounter);
							//}
						}
				}
				CandidateVmBC.add(Ccounter, new ArrayList<Integer>(temp));
				//Log.printLine("cloudlet ID:" + Ccounter);
				for (int x =0 ; x < temp.size();x++){
					//Log.printLine(x+" :"+temp.get(x));
					//Log.printLine("CandidateCloudletBVm.get( "+temp.get(x)+" ): "+CandidateCloudletBVm.get(temp.get(x)));
					if (CandidateCloudletBVm.get(temp.get(x)).get(0) == 9999){
						CandidateCloudletBVm.get(temp.get(x)).set(0, Ccounter);
						//Log.printLine("CandidateCloudletBVm.get( "+temp.get(x)+" ).set( 0, "+Ccounter+")");

					}else {
						CandidateCloudletBVm.get(temp.get(x)).add(CandidateCloudletBVm.get(temp.get(x)).size(), Ccounter);
						//Log.printLine("CandidateCloudletBVm.get( "+temp.get(x)+" ).add("+CandidateCloudletBVm.get(temp.get(x)).size()+", "+Ccounter+")");

					}

				}
			}
			
			/*
			Log.printLine();
			Log.printLine("##################################  Checking ** cloudlet Candidate ** Checking ############################################");
			Log.printLine(CandidateVmBC);
			Log.printLine("##################################  Checking ** cloudlet Candidate ** Checking ############################################");
			
			Log.printLine("##################################  Checking ** VM Candidate ** Checking ############################################");
			Log.printLine(CandidateCloudletBVm);
			Log.printLine("##################################  Checking ** VM Candidate ** Checking ############################################");
			
			Log.printLine("##################################  Checking ** One VM Candidate ** Checking ############################################");
			Log.printLine(OneVmCandidate);
			Log.printLine("##################################  Checking ** One VM Candidate ** Checking ############################################");
			
			Log.printLine("intial state next node: " + NextNodes);
			
			Log.printLine("intial state perviouse node: " + PerviousNodes);
			
			Log.printLine("Worst case for each node: " + WorstCase);
			
			Log.printLine("Deadline case for each node: " + Deadline);
			
			Log.printLine("Start time for each node: " + StartTime);
					
			Log.printLine("Selected Node: " + SelVm);
			*/
			// from this part is related to find worst case and update executiontalbe based on that
			/* // this part no need in new algorithm
			int tempw =0;
			//WorstCase.add(0, 0.0);
			
			//Log.printLine("rezaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + IVMtoCloudletTime.VMtoCloudletTime);

			for (int counter =0;counter < (i+1); counter++){
					tempw =0;
					tempd.clear();
					tempd = new ArrayList<Integer>( CandidateVmBC.get(counter));
					
					
					
					
					for (int x=0;x < tempd.size();x++){
								if(tempw < IVMtoCloudletTime.VMtoCloudletTime.get(counter).get(tempd.get(x))  ){
									tempw = IVMtoCloudletTime.VMtoCloudletTime.get(counter).get(tempd.get(x));
									SelVm.set(counter, tempd.get(x));
								}
							}
					WorstCase.add(counter, (double)tempw)	;		
						}
			//WorstCase.add(i+2, 0.0);		
					
					
			Log.printLine();
			Log.printLine("##################################  Checking ** VM information ** Checking ############################################");
					
			Log.printLine("intial state next node: " + NextNodes);
					
			Log.printLine("intial state perviouse node: " + PerviousNodes);
			
			Log.printLine("Worst case for each node: " + WorstCase);
			
			Log.printLine("Deadline case for each node: " + Deadline);

					
			Log.printLine("Selected Node: " + SelVm);
			
			Log.printLine("##################################  Checking ** VM information ** Checking ############################################");

			Log.printLine();	
					
		
			//This part it will calculate worst case execution time for APP
			temp.clear();
			tempd.clear();
			for (int x=0; x<i+1;x++){
				temp.add(x,0);// using for checking cloudlet visitede or no
				StartTime.add(x,0.0);
				ExecutionTime.add(x,0.0);
				FinishTime.add(x,0.0);
				Deadline.add(x, 400.0);
			}
			
			Log.printLine("##################################  Checking ** time intiation ** Checking ############################################");
			
			Log.printLine("Start Time: " + StartTime);
			
			Log.printLine("WorstCase Time: " + WorstCase);
			
			Log.printLine("Execution Time: " + ExecutionTime);
			
			Log.printLine("Finish Time: " + FinishTime);
					
			Log.printLine("##################################  Checking ** time intiation ** Checking ############################################");

			Log.printLine();	
			
			Log.printLine("#################################################Checking intiation of table ##########################################################");
			Log.printLine("#################################################Checking intiation of table ##########################################################");
			int counter = 0;
			tempd.add(0);
			double tempdd =0.0;
			while (counter < (i+1)){
				
				//Log.printLine(" tempd:  "+ tempd);
				tempw = tempd.remove(0);
				//Log.printLine(" tempd:  "+ tempd+"tempw is equal:"+ tempw);
				//Log.printLine();
				
				ExecutionTime.set(tempw, (WorstCase.get(tempw)));
				//Log.printLine("ExecutionTime.set("+tempw+"," +(WorstCase.get(tempw))+")");
				
				// In this phase we should find out worst case of previous nodes and put worst case as start time
				if(!(PerviousNodes.get(tempw).get(0)== 3000)){
					for (int x=0;x < PerviousNodes.get(tempw).size();x++){
					
						if ( StartTime.get(tempw) < FinishTime.get(PerviousNodes.get(tempw).get(x))) {
							
							StartTime.set(tempw,(FinishTime.get(PerviousNodes.get(tempw).get(x))));

						}
						
					}
				}
				
				tempdd = WorstCase.get(tempw) + StartTime.get(tempw);
				FinishTime.set(tempw, tempdd);
				
				// In this phase next nodes which didn't visit shall be added to list
				if(!(NextNodes.get(tempw).get(0)== 3000)){
					//Log.printLine(" NextNodes.get("+ tempw+").size() is equal to:"+NextNodes.get(tempw).size());
					for (int x=0;x < NextNodes.get(tempw).size();x++){
						//Log.printLine(" temp.get(NextNodes.get("+ tempw+").get("+x+") is equal to:"+  temp.get(NextNodes.get(tempw).get(x)));
						if (temp.get(NextNodes.get(tempw).get(x)) == 0){
							
							temp.set(NextNodes.get(tempw).get(x),1);
							//Log.printLine("After visiting check, temp.get(NextNodes.get("+ tempw+").get("+x+") is equal to:"+  temp.get(NextNodes.get(tempw).get(x)));
							//Log.printLine("After visiting check, NextNodes.get("+ tempw+").get("+x+") is equal to:"+  NextNodes.get(tempw).get(x));

							//tempd is used for keep queue for checking
							//Log.printLine();
							//Log.printLine("before adding, tempd:  "+ tempd);

							tempd.add(NextNodes.get(tempw).get(x));
							//Log.printLine("After visiting check, tempd:  "+ tempd);

						}
					}
				}
				
				counter++;
			}
			Log.printLine("##################################  Checking ** Time After intiation ** Checking ############################################");
			
			Log.printLine("Start Time: " + StartTime);
			
			Log.printLine("WorstCase Time: " + WorstCase);
			
			Log.printLine("Execution Time: " + ExecutionTime);
			
			Log.printLine("Finish Time: " + FinishTime);
			
			Log.printLine("Deadline Time: " + Deadline);

					
			Log.printLine("##################################  Checking ** Time After intiation ** Checking ############################################");

			Log.printLine();	
			*/ // this part no need in new algorithm	
		}// Intialzation finished
		
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//This function is for checking at least there is 1 VM candidate for each Cloudlet
		public Boolean Atleast1VM(){
			for (int x=0; x < (i+1); x++){
				if (CandidateVmBC.get(x).isEmpty()){
					return false;
				}
				}
			return true;
			}
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//This function is for checking sum of all Vm candidate is higher than sum of average energy which need 
		public Boolean EnergyIsEnough(VMInfo IVMInfo,VMtoCloudletEnergy IVMtoCloudletEnergy){
			int VmE = 0;
			int CloudletE = 0;
			for (int x=0; x < (VmSize); x++){
				if (!CandidateCloudletBVm.get(x).isEmpty()){
					VmE = VmE + IVMInfo.Energy.get(x) ;
				}
			}
			for (int x1 =0; x1 < (i); x1++){
				int Etemp = 0;
				if(!CandidateVmBC.get(x1).isEmpty()){
				for (int y=0; y < CandidateVmBC.get(x1).size(); y++ ){
					Etemp= Etemp + IVMtoCloudletEnergy.VMtoCloudletEnergy.get(x1).get(y);
				}
				Etemp = Etemp /CandidateVmBC.get(x1).size();
				CloudletE = CloudletE + Etemp ;
			}
			}
			//Log.printLine( CloudletE + " ,"+VmE);
			if (VmE < CloudletE){
				return false;
			} else {
			return true;
			}
		}	
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//This function is find only one candidate and put in OneVmCandidate list and update the list based on that and check again  
		public void findOneCandidate(VMInfo IVMInfo,VMtoCloudletEnergy IVMtoCloudletEnergy, VMEtoCloudletE IVMEtoCloudletE){
			boolean finding = true ;
			Log.printLine();
			/*
			Log.printLine();
			Log.printLine("##########################################################################################################################");
			Log.printLine("##########################################      checking find one candidate   ############################################");
			Log.printLine("##########################################################################################################################");
			Log.printLine();
			Log.printLine("##########################################            CandidateVmBC           ############################################");
			Log.printLine(CandidateVmBC);
			Log.printLine("##########################################         CandidateCloudletBVm       ############################################");
			Log.printLine(CandidateCloudletBVm);
			Log.printLine("##########################################            OneVmCandidate          ############################################");
			Log.printLine(OneVmCandidate);
			Log.printLine("############################      IVMtoCloudletEnergy.VMtoCloudletEnergy      ############################################");
			Log.printLine(IVMtoCloudletEnergy.VMtoCloudletEnergy);
			Log.printLine("############################          IVMEtoCloudletE.VMEtoCloudletE          ############################################");
			Log.printLine(IVMEtoCloudletE.VMEtoCloudletE);
			Log.printLine("############################                  IVMInfo.Energy                  ############################################");
			Log.printLine( IVMInfo.Energy);
			Log.printLine( );
			Log.printLine( );
			Log.printLine( );
			*/
			Log.printLine( "############################              Progress of findOneCandidate function          ##########################################");
			while (finding) {
				finding = false ;
				for (int x=1; x < (i+1); x++){
					if ((CandidateVmBC.get(x).size() == 1)&(CloudletN[x][13] == 0)){// if there is just one item need to be assign and return true
						//Log.printLine("CandidateVmBC.get("+x+").size() =" +CandidateVmBC.get(x).size());
						finding = true ;
						int vmid = CandidateVmBC.get(x).get(0);
						//Log.printLine("vmid ="+vmid+",CandidateVmBC.get("+x+").get(0) = "+CandidateVmBC.get(x).get(0));
						
						//set in one Vm candidate list
						OneVmCandidate.set(x, vmid);
						Log.printLine(" set("+x+") ="+ vmid);

						//Deduct the amount of energy needed for processing cloudlet from Vm Energy
						//Log.printLine("IVMInfo.Energy.get("+vmid+") ="+IVMInfo.Energy.get(vmid));
						//Log.printLine("IVMtoCloudletEnergy.VMtoCloudletEnergy.get("+x+").get("+vmid+")="+IVMtoCloudletEnergy.VMtoCloudletEnergy.get(x).get(vmid));
						IVMInfo.Energy.set(vmid,IVMInfo.Energy.get(vmid) - IVMtoCloudletEnergy.VMtoCloudletEnergy.get(x).get(vmid));
						//Log.printLine("after energy deduction IVMInfo.Energy.get("+vmid+") ="+IVMInfo.Energy.get(vmid));
						
						//updating VmEtoCloudletE
						IVMEtoCloudletE.VMEtoCloudletE.get(x).set(vmid, (double)(IVMInfo.Energy.get(vmid)/IVMtoCloudletEnergy.VMtoCloudletEnergy.get(x).get(vmid)));
						
						// checking with current energy this vm can process other cloudlet
						Log.printLine("checking with current energy this vm can process other cloudlet");
						//Log.printLine("CandidateCloudletBVm.get("+vmid+").size() = "+CandidateCloudletBVm.get(vmid).size());
						ArrayList<Integer> temp = new ArrayList<Integer>();
						temp.clear();
						//Log.printLine("temp before run : " + temp ); 
						for (int y =0 ; y < CandidateCloudletBVm.get(vmid).size(); y++){
							
							
							//Log.printLine("CandidateCloudletBVm.get("+vmid+").("+y+") = "+CandidateCloudletBVm.get(vmid).get(y));
							//Log.printLine("IVMInfo.Energy.get("+vmid+") = " + IVMInfo.Energy.get(vmid));
							//Log.printLine("IVMtoCloudletEnergy.VMtoCloudletEnergy.get("+CandidateCloudletBVm.get(vmid).get(y)+").get("+vmid+") = " + IVMtoCloudletEnergy.VMtoCloudletEnergy.get(CandidateCloudletBVm.get(vmid).get(y)).get(vmid));
							
							if ((CandidateCloudletBVm.get(vmid).get(y) == i) ||
									(IVMInfo.Energy.get(vmid)< IVMtoCloudletEnergy.VMtoCloudletEnergy.get(CandidateCloudletBVm.get(vmid).get(y)).get(vmid)) ){
								// remove vmid from vm's candidate of cloudlet
								for (int a =0; a < CandidateVmBC.get(CandidateCloudletBVm.get(vmid).get(y)).size(); a++){
									if (CandidateVmBC.get(CandidateCloudletBVm.get(vmid).get(y)).get(a) == vmid){
										
										//update CandidateVmBC
										CandidateVmBC.get(CandidateCloudletBVm.get(vmid).get(y)).remove(a);
										a = CandidateVmBC.get(CandidateCloudletBVm.get(vmid).get(y)).size() +100 ;
									}
										
								}
								//remove cloudlet from candidate cloudlt list
								temp.add(CandidateCloudletBVm.get(vmid).get(y));
							}
						}
						// update CandidateCloudletBVm
						//Log.printLine("temp : " + temp ); 
						for (int z=0; z< temp.size();z++){
							for (int l =0; l < CandidateCloudletBVm.get(vmid).size(); l++){
								if (temp.get(z)== CandidateCloudletBVm.get(vmid).get(l) )
									CandidateCloudletBVm.get(vmid).remove(l);
							}
						}
						
					}
				}
			}
			/*
			Log.printLine("######################        After algorithem finding one candidate run      ############################################");
			Log.printLine();
			Log.printLine("##########################################            CandidateVmBC           ############################################");
			Log.printLine(CandidateVmBC);
			Log.printLine("##########################################         CandidateCloudletBVm       ############################################");
			Log.printLine(CandidateCloudletBVm);
			Log.printLine("##########################################            OneVmCandidate          ############################################");
			Log.printLine(OneVmCandidate);
			Log.printLine("############################      IVMtoCloudletEnergy.VMtoCloudletEnergy      ############################################");
			Log.printLine(IVMtoCloudletEnergy.VMtoCloudletEnergy);
			Log.printLine("############################          IVMEtoCloudletE.VMEtoCloudletE          ############################################");
			Log.printLine(IVMEtoCloudletE.VMEtoCloudletE);
			Log.printLine("############################                  IVMInfo.Energy                  ############################################");
			Log.printLine( IVMInfo.Energy);
			*/

		}//end of function
	}	

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////// This function used for reading DAG information from Excel file ///////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	public static void readingCloudlet (Executiontable IExecutiontable,int SheetNo){
	
	// This phase include read cloudlet and and initiate the required data in ????
		
		i = 0; // Number of cloudlet
		j = -1; // Number of Edge
		int CIDtemp = 0;
		ArrayList<Integer> temp = new ArrayList<Integer>();

		//Read App from File and create database of DAG
		try{
			InputStream inp = new FileInputStream("d:\\DAG.xlsx");
			Workbook wb = WorkbookFactory.create(inp);
			Sheet sheet = wb.getSheetAt(SheetNo);
					
			/* BDMCC:
			 * Phase on is reading all Cloudlet and their relationship and put in two array of cloudlet and Edges for scheduling
			 */
			//read all row
			for (org.apache.poi.ss.usermodel.Row row : sheet) {
			if (!(row.getRowNum() ==  0)){ // row 0 is description
					
			Cell CID = (Cell) row.getCell(0); //Cloudlet ID***************************
			Cell CL = (Cell) row.getCell(1); //Cloudlet Length
			Cell PesN = (Cell) row.getCell(2); //Cloudlet PesNumber	
			Cell FS = (Cell) row.getCell(3); //Cloudlet FileSize
			Cell OS = (Cell) row.getCell(4); //Cloudlet OutputSize********************
			Cell Battery = (Cell) row.getCell(5); //Cloudlet Battery
			Cell Memory = (Cell) row.getCell(6); //Cloudlet Memory
			Cell NumS = (Cell) row.getCell(7); //Cloudlet Number of Stages
			Cell vm = (Cell) row.getCell(8); //Cloudlet VM
			Cell StageNum = (Cell) row.getCell(9);// Stage Number
			Cell SType = (Cell) row.getCell(10);// Stage Type
			Cell Sdata = (Cell) row.getCell(11);// Stage data
			Cell Stime = (Cell) row.getCell(12);// Stage time
			Cell SID = (Cell) row.getCell(13);// Stage ID
			Cell SMemory = (Cell) row.getCell(14);// Stage Memory
			Cell SPeer = (Cell) row.getCell(15);// Stage Peer
			Cell SVPeer = (Cell) row.getCell(16);// Stage VPeer
			Cell CMIPS = (Cell) row.getCell(17);// Stage MIPS *************************
					
			CIDtemp = (int) CID.getNumericCellValue();
			if((!(CIDtemp == i)) ){i++;} // count number of cloudlet   || i == 0
			CloudletN[i][0] = i;
					
			CloudletN[i][1] = (int) CL.getNumericCellValue();
			CloudletN[i][2] = (int) PesN.getNumericCellValue();
			CloudletN[i][3] = (int) FS.getNumericCellValue();
			if (((int) SType.getNumericCellValue()) == 0){
			CloudletN[i][4] = (int) OS.getNumericCellValue();
			}
			CloudletN[i][5] = (int) Battery.getNumericCellValue();
			CloudletN[i][6] = (int) Memory.getNumericCellValue();
			CloudletN[i][7] = (int) NumS.getNumericCellValue();
			CloudletN[i][8] = (int) vm.getNumericCellValue();
			CloudletN[i][9] = (int) CMIPS.getNumericCellValue();
			//CloudletN[i][10] = 0; //Number of Send
			//CloudletN[i][11] = 0; //Number of Recieving
			CloudletN[i][12] = 0; //Time for processing, which will be calculated after VM allocation is done
			CloudletN[i][13] = 0;
			
		/* checking
		  
			Log.printLine("Network Cloulet-- Checking for reading is correct-------------------------------------------------");
			Log.printLine("CIDtemp:" + CIDtemp + " i:" + i + " CloudletN[i][0]:" + CloudletN[i][0] );
			Log.printLine("Cloudlet ID: " +  CloudletN[i][0] + " lenght: " + CloudletN[i][1] + " PesNumber	: " + CloudletN[i][2]  +
						  " Cloudlet FileSize:" + CloudletN[i][3] + " OutputSize:" + CloudletN[i][4] + " Battery:" +
						  CloudletN[i][5] + " Memory: " + CloudletN[i][6]+ " VM:: " + CloudletN[i][8]);
			Log.printLine("Network Cloulet---------------------------------------------------");
			Log.printLine(" ");
		//*/
					
					
			//Log.printLine("****checking Stype reading, i :" + i + " Stype:" + ((int) SType.getNumericCellValue())+ " j:" + j);
			if (((int) SType.getNumericCellValue()) == 1){ // if Stype is equal 1 it mean sending
					temp.clear();
					
					j++;				
					Edge[j][0] = j;
					Edge[j][1] = (int) CID.getNumericCellValue(); // cloudlet Source for sending 
					Edge[j][2] = (int) SVPeer.getNumericCellValue(); //cloudlet Destination for recieving
					Edge[j][3] = (int) OS.getNumericCellValue(); // data size
					Edge[j][4] = 0; // BW minmum of BW of sending VM and Receiving VM
					Edge[j][5] = 0; // time based on size it will be calculated (data size / BW)
					Edge[j][6] = 0; // Sum of Other edge sender node output file size
					Edge[j][7] = 0; // assigned ot no
					
					
					// Source and next node adding
					temp.clear();
					if (CloudletN[i][10]  == 0){
						temp.add(Edge[j][2]);
						IExecutiontable.NextNodes.add(Edge[j][1], new ArrayList<Integer> (temp));
					//	Log.printLine("current node "+i + " next node:"+IExecutiontable.NextNodes.get(Edge[j][1]));
						
					} else {

						temp.clear();
						temp = new ArrayList<Integer> (IExecutiontable.NextNodes.get(Edge[j][1]));
						//Log.printLine(IExecutiontable.NextNodes.get(Edge[j][1]));

						//Log.printLine(temp);

						temp.add(Edge[j][2]);
						//Log.printLine(temp);

						IExecutiontable.NextNodes.set(Edge[j][1], new ArrayList<Integer> (temp));
						//Log.printLine("Next Nodes: "+IExecutiontable.NextNodes);
					}
					
					
					CloudletN[i][10] ++;
					
					
					// Destination and previous node adding
					temp.clear();
					if (CloudletN[(Edge[j][2])][11]  == 0){
						temp.add(Edge[j][1]);
						IExecutiontable.PerviousNodes.get(Edge[j][2]).clear();
						IExecutiontable.PerviousNodes.get(Edge[j][2]).add(Edge[j][1]);
						//Log.printLine("current node: "+Edge[j][2]+"Perviouse node"+IExecutiontable.PerviousNodes.get(Edge[j][2]));
						
					} else {

						
						temp = new ArrayList<Integer> (IExecutiontable.PerviousNodes.get(Edge[j][2]));
						//Log.printLine(IExecutiontable.PerviousNodes.get(Edge[j][2]));

						//Log.printLine(temp);

						temp.add(Edge[j][1]);
						//Log.printLine(temp);

						IExecutiontable.PerviousNodes.set(Edge[j][2], new ArrayList<Integer> (temp));
						//Log.printLine("Pervious Nodes: "+IExecutiontable.PerviousNodes);
					}
					
					
					CloudletN[Edge[j][2]][11] ++;
					/*
					Edge[j][6] = (int) StageNum.getNumericCellValue();
					Edge[j][7] = (int) SType.getNumericCellValue();
					Edge[j][8] = (int) Sdata.getNumericCellValue();
					Edge[j][9] = (int) Stime.getNumericCellValue();
					Edge[j][10] = (int) SID.getNumericCellValue();
					Edge[j][11] = (int) SMemory.getNumericCellValue();
					Edge[j][12] = (int) SPeer.getNumericCellValue();
					Edge[j][13] = (int) SVPeer.getNumericCellValue();
					*/	
					// each edge contain two Stype sending and receiving which we only count sending
					} else if (((int) SType.getNumericCellValue()) == 2){ // if Stype is equal 2 it mean receiving
						//CloudletN[i][11] ++;
						
						// we need to define procedure to check sending and receiving are same, we need to define checking procedure
						// need to check is it equal to sending
					}
					if (((int) SType.getNumericCellValue()) == 0){ // if Stype is equal 0 it mean Execution
						// right now i don't have any especial action 
									}
					}
				    }
			// Write the output to a file
		    FileOutputStream fileOut = new FileOutputStream("d:\\DAG.xlsx");
		    wb.write(fileOut);
			fileOut.close();
			} catch (Exception e) {
					e.printStackTrace();
			}
				
			//checking reading is correct 
			/*

			for (int z =0; z<(i+1); z++){
				Log.printLine("Network Cloulet-- Checking for reading is correct-------------------------------------------------");
				Log.printLine("Cloudlet ID: " + z+ " "+  CloudletN[z][0] + " Cloudlet lenght: " + CloudletN[z][1] + " Cloudlet PesNumber	: " + CloudletN[z][2]  +
					  		  " Cloudlet FileSize:" + CloudletN[z][3] + " Cloudlet OutputSize:" + CloudletN[z][4] + " Cloudlet Battery:" +
							   CloudletN[z][5] + " Cloudlet Memory: " + CloudletN[z][6]+ " VM:: " + CloudletN[z][8]);
				Log.printLine("************************************************************************************************************************************");
				}
			
			for (int z =0; z<(j+1); z++){
				Log.printLine("**************************************************************************************************************************");
				Log.printLine("Edge of Cloudlet---------------------------------------------------");
				Log.printLine("Edge ID: " + Edge[z][0]+ " Source: " + Edge[z][1] + " Destination: " + Edge[z][2]  +
							  " Data size:" + Edge[z][3]  + " BW:" + Edge[z][4] + " Time:" +  Edge[z][5] );
				Log.printLine("Edge of Cloudlet---------------------------------------------------");
				Log.printLine("**************************************************************************************************************************");

				}
			
				
				*/
				//finishing checking
		
	}
	

	

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////This phase include read  files needed to be read or write in each cloudlet ///////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


public static void readingfiles (int VmSize,List<List<File>> readfiles,List<List<File>> writefiles,int SheetNo){

//This phase include read  read/write files in each cloudlet

try{
InputStream inp1 = new FileInputStream("d:\\CloudletFiles.xlsx");
Workbook wb1 = WorkbookFactory.create(inp1);
Sheet sheet = wb1.getSheetAt(SheetNo);
int ll =0;
int tempRW =-1;
int tempC = -1;
String tempName = null;
int tempSize = -1;
//intiate readfiles and writefiles
for (int z=0; z < i+1; z++){
	readfiles.add(z, new ArrayList<File>());
	writefiles.add(z, new ArrayList<File>());
}




//read all row
for (org.apache.poi.ss.usermodel.Row row : sheet) {
if (!(row.getRowNum() ==  0)){ // row 0 is description
//Log.printLine();
//Log.print(" CloudletFiles- line: " + ll +" ");
// read all row 

File filet = new File("file.dat", 900);


//reading this need to be read or write
Cell file0 = (Cell) row.getCell(0); 
tempRW = (int) file0.getNumericCellValue();

//checking belong to which clouslet
Cell file1 = (Cell) row.getCell(1); 
tempC = (int) file1.getNumericCellValue();

//Reading Nmae of the file
Cell file2 = (Cell) row.getCell(2); 
tempName = (String) file2.getStringCellValue() ;

Cell file3 = (Cell) row.getCell(3); 
tempSize = (int) file3.getNumericCellValue();

filet.setName(tempName);
filet.setFileSize(tempSize);

if (tempRW == 0){// "0" means read
	readfiles.get(tempC).add(filet);
		
}else {// "1" means write
	writefiles.get(tempC).add(filet);
	//Log.printLine("tempc: "+ tempC);
	// Reading Row finish
}
ll++;
}
}

//Write the output to a file
FileOutputStream fileOut = new FileOutputStream("d:\\CloudletFiles.xlsx");
wb1.write(fileOut);
fileOut.close();
} catch (Exception e) {
e.printStackTrace();
}

//Checking of reading
/*
Log.printLine();
Log.printLine("*******************************************Reading and writing files list************************************************************");
Log.printLine("reading list: "+ readfiles.get(1).get(0).getName());
Log.printLine("reading list: "+ readfiles.get(1).get(0).getSize());
Log.printLine("writing list: "+ writefiles);
*/
}

	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////// This Function Calculate Delay ///////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
public static void CalDelay (List<NetworkVm> vmSpecification, int[][] VMBW){
	
		Log.printLine();


				//third step calculating delay of transmission
				
				for( int l =0; l< (j+1) ; l++){
						NetworkVm vm1 = vmSpecification.get(CloudletN[Edge[l][1]][8]); //Sending VM CloudletN[Edge[l][1]][8]
						NetworkVm vm2 = vmSpecification.get(CloudletN[Edge[l][2]][8]); // Receiving VM
						if (!(vm1 == vm2)){
							//Edge[l][4] = (int) Math.min(vm1.getBw(),vm2.getBw()); // BW minimum of BW of sending VM and Receiving VM
							Edge[l][4] = (int) VMBW[vm1.getId()][vm2.getId()]; // BW minimum of BW of sending VM and Receiving VM
							Edge[l][5] = (int) (Edge[l][3]/Edge[l][4]); // calculating time for transferring by dividing the size / bandwidth
							Edge[l][4] = 0;
						}else {
							Edge[l][5] =0;
						}
					//Log.printLine("time of Edge of Cloudlet:" + Edge[l][5]);
					//Log.printLine("####################################################################");
						
						//Log.printLine("Edge ID:" + Edge[l][0] + " Edge source:" + Edge[l][1]+" , "+vm1.getId() +" Edge destination:" + Edge[l][2]+" , "+ vm2.getId() +" Edge size:" + Edge[l][3]+
							//		  " Edge BW:"+ Edge[l][4] + " Edge time:" + Edge[l][5]);
				}
		
			 
	}
	

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////// FIFO Scheduling Function /////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
public static void FIFOScheduling (List<NetworkVm> vmSpecification,VMtoCloudletTime IVMtoCloudletTime){
	// After assignment of cloudlet to VM, and delay calulation, in this phase NetworkCloudlet is going to be created
		for(int q=0; q < NumberOfCloudlet ;q++){
			    CloudletN[q][8] = (q%VmSize);
				//CloudletN[q][12] = (int) (CloudletN[q][9]/(vmSpecification.get(CloudletN[q][8]).getMips()));
				CloudletN[q][12] = (int) (IVMtoCloudletTime.VMtoCloudletTime.get(q).get(CloudletN[q][8]));

		}
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////// This Class keep the Energy of VM / Energy of Cloudlet /////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	public class VMEtoCloudletE {
		public ArrayList<ArrayList<Double>> VMEtoCloudletE = new ArrayList<ArrayList<Double>>(); // Total time based on MI/MIPS

		private ArrayList<Double> temp = new ArrayList<Double>(); 
		
		public void intial(int cloudletsize,List<NetworkVm> vmSpecification,VMtoCloudletEnergy IVMtoCloudletEnergy,VMInfo IVMInfo){
			//Calculation Execution time
			Double Itemp1 = (double) 0 ;
			Double Itemp2 = (double) 0 ;
			Double Itemp = (double) 0 ;
			Log.printLine();
			for(int j=0; j< cloudletsize+1; j++){
				temp.clear();
				for (int i =0; i < (VmSize); i++){
					Itemp1 = (double) ( IVMInfo.Energy.get(i));
					Itemp2 = (double) IVMtoCloudletEnergy.VMtoCloudletEnergy.get(j).get(i);
					Itemp =  ( Itemp1 / Itemp2);
					
					//Log.printLine(" "+j+", "+i+": "+ Itemp);
					temp.add(i,Itemp);
					}
				VMEtoCloudletE.add( new ArrayList<Double>(temp));
			}
			/*
			Log.printLine();
			Log.printLine("##################################  Checking ** VMEtoCloudletE  ** Checking ############################################");
			
				Log.printLine(" "+ VMEtoCloudletE);
			
			Log.printLine("##################################  Checking ** VMEtoCloudletE ** Checking ############################################");
			*/
		}
	}
	

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// This Class provide information related to VM MIPS / Cloudlet Millions of Instruction /////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class VMMIPStoCloudletMI {
public ArrayList<ArrayList<Double>> VMMIPStoCloudletMI = new ArrayList<ArrayList<Double>>(); // Total time based on MI/MIPS

private ArrayList<Double> temp = new ArrayList<Double>(); 

public void intial(int cloudletsize,List<NetworkVm> vmSpecification,VMtoCloudletEnergy IVMtoCloudletEnergy,VMInfo IVMInfo){
//Calculation Execution time
Double Itemp1 = (double) 0 ;
Double Itemp2 = (double) 0 ;
Double Itemp = (double) 0 ;
Log.printLine();
for(int j=0; j< cloudletsize+1; j++){
temp.clear();
for (int i =0; i < (VmSize); i++){
Itemp1 = (double) ( IVMInfo.MIPS.get(i)*IVMInfo.MIPSRatioApp.get(i));
Itemp2 = (double) CloudletN[j][9];
Itemp =  ( Itemp1 / Itemp2);

Log.printLine(" "+Itemp+"= "+Itemp1+" / "+ Itemp2);
temp.add(i,Itemp);
}
VMMIPStoCloudletMI.add( new ArrayList<Double>(temp));
}

Log.printLine();
Log.printLine("##################################  Checking ** VMMIPStoCloudletMI  ** Checking ############################################");

Log.printLine(" "+ VMMIPStoCloudletMI);

Log.printLine("##################################  Checking ** VMMIPStoCloudletMI ** Checking ############################################");
}
}




//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// This class is for keeping execution time of each cloudlet in each VM   /////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



	public class VMtoCloudletTime {
		public ArrayList<ArrayList<Integer>> VMtoCloudletTime = new ArrayList<ArrayList<Integer>>();  // Total time basde on each VM BW
		public ArrayList<ArrayList<Integer>> VMtoCloudletTranTime = new ArrayList<ArrayList<Integer>>(); // Transmision time based on each VM
		public ArrayList<ArrayList<Integer>> VMtoCloudletExecTime = new ArrayList<ArrayList<Integer>>(); // Total time based on MI/MIPS
		public ArrayList<Integer> weight = new ArrayList<Integer>(); 
		public ArrayList<Integer> ComCost = new ArrayList<Integer>();
		public ArrayList<Integer> RankupL = new ArrayList<Integer>();

		private ArrayList<Integer> temp = new ArrayList<Integer>(); 
		public void intial(int cloudletsize,List<NetworkVm> vmSpecification, int[][] VMBW,int SheetNo){
			//Calculation Execution time
			int Itemp = 0 ;
			Log.printLine();
			
			try{
				InputStream inp = new FileInputStream("d:\\CompCost.xlsx");
				Workbook wb = WorkbookFactory.create(inp);
				Sheet sheet = wb.getSheetAt(SheetNo);
				
				for (org.apache.poi.ss.usermodel.Row row : sheet) {
					temp.clear();
				if (!(row.getRowNum() ==  0)){ // row 0 is description
					for (int i =0; i < (VmSize); i++){
						
						Cell cell = (Cell) row.getCell(i); //Cloudlet ID***************************
						
						Itemp =  (int) cell.getNumericCellValue();
						
						//Log.printLine(" "+j+", "+i+": "+ Itemp);
						temp.add(i,Itemp);
						}	
					VMtoCloudletExecTime.add( new ArrayList<Integer>(temp));
				}
					
				}
				FileOutputStream fileOut = new FileOutputStream("d:\\CompCost.xlsx");
			    wb.write(fileOut);
				fileOut.close();
				} catch (Exception e) {
						e.printStackTrace();
				}
			
			
			
			/*
			for(int j=0; j< cloudletsize+1; j++){
				temp.clear();
				for (int i =0; i < (VmSize); i++){
					Itemp =  (int) (CloudletN[j][9]/(vmSpecification.get(i).getMips()));
					
					//Log.printLine(" "+j+", "+i+": "+ Itemp);
					temp.add(i,Itemp);
					}
				VMtoCloudletExecTime.add( new ArrayList<Integer>(temp));
			}
			*/
			
			Log.printLine();
			Log.printLine("##################################  Checking ** Execution time ** Checking ############################################");
			for(int j=0; j< cloudletsize+1; j++){
				Log.printLine(" "+ VMtoCloudletExecTime.get(j));
			}	
			
			Log.printLine("##################################  Checking ** Execution time ** Checking ############################################");
			
			
			
			
			//Calculation Transmission time
			//Log.printLine();
			for(int j=0; j< cloudletsize+1; j++){
				temp.clear();
				for (int i =0; i < (VmSize); i++){
					//Log.printLine(" CloudletN["+j+", "+i+"]: "+ CloudletN[j][4]);
					//Log.printLine(" VMBW ["+j+", "+i+"]: "+ VMBW [i][i+1]);
					Itemp =  (int) (CloudletN[j][4]/(VMBW [i][VmSize]));
					
					temp.add(i,Itemp);
					}
				VMtoCloudletTranTime.add( new ArrayList<Integer>(temp));
			}
			
			Log.printLine();
			Log.printLine("##################################  Checking ** Transmision time ** Checking ############################################");
			for(int j=0; j< cloudletsize+1; j++){
				Log.printLine(" "+ VMtoCloudletTranTime.get(j));
			}
			Log.printLine("##################################  Checking ** Transmision time ** Checking ############################################");
			
			//Calculation total execution time
			Log.printLine();
			
			for(int j=0; j< cloudletsize+1; j++){
				temp.clear();
				for (int i =0; i < (VmSize); i++){
					Itemp =  (int) (VMtoCloudletExecTime.get(j).get(i)+VMtoCloudletTranTime.get(j).get(i));
					//Log.printLine(" "+j+", "+i+": "+ Itemp);
					temp.add(i,Itemp);
					}
				VMtoCloudletTime.add( new ArrayList<Integer>(temp));
			}
			
			Log.printLine();
			Log.printLine("##################################  Checking ** Total execution time ** Checking ############################################");
			for(int j=0; j< cloudletsize+1; j++){
				Log.printLine(" "+ VMtoCloudletTime.get(j));
			}
			Log.printLine("##################################  Checking ** Total execution time ** Checking ############################################");
			
			
			for(int j=0; j< cloudletsize+1; j++){
				Itemp =0;
				for (int i =0; i < (VmSize); i++){
					Itemp = Itemp +  VMtoCloudletExecTime.get(j).get(i);
					
					}
				weight.add(j, (int) (Itemp/VmSize));
			}
			/*
			Log.printLine();
			Log.printLine("##################################  Checking ** Average execution time ** Checking ############################################");
			
				Log.printLine(" "+ weight);
			
			Log.printLine("##################################  Checking ** Average execution time ** Checking ############################################");
			*/
			
			for(int j=0; j< cloudletsize+1; j++){
				Itemp = 0;
				for (int i =0; i < (VmSize); i++){
					Itemp = Itemp +  VMtoCloudletTranTime.get(j).get(i);
					
					}
				ComCost.add(j, (int) (Itemp/VmSize));;
			}
			/*
			Log.printLine();
			Log.printLine("##################################  Checking ** Average Trans time ** Checking ############################################");
			
				Log.printLine(" "+ ComCost);
			
			Log.printLine("##################################  Checking ** Average Trans time ** Checking ############################################");
			*/
			for(int j=0; j< cloudletsize+1; j++){
				
				RankupL.add(j, 0);
				}
			
		} // End of Initialization
		
		
		public int Rankup (int cloudletsize, int cloudlet, List<NetworkVm> vmSpecification,Executiontable IExecutiontable){
			int temp = 0;
			int Rankupset[] = new int[cloudletsize+1];  
			for ( int i =0; i < cloudletsize+1; i++){
				Rankupset[i] = 0;
			}
			
			
			if (Rankupset[cloudlet]== 0){
			//Log.printLine(" Rank up sarat**************************************************************************************");
			if (cloudlet == cloudletsize){
				RankupL.set(cloudletsize, weight.get(cloudletsize));
				//Log.printLine("last cloudlet: "+ cloudletsize+ " ,weight:"+RankupL.get(cloudletsize));
				Rankupset[cloudlet] = 1;
				return (weight.get(cloudletsize));
				
			} else {
				
				for (int i =0; i < IExecutiontable.NextNodes.get(cloudlet).size(); i++){
					temp = Math.max(temp,(ComCost.get(cloudlet)+ 
							Rankup(cloudletsize,IExecutiontable.NextNodes.get(cloudlet).get(i),vmSpecification,IExecutiontable)));
					//Log.printLine("cloudletID["+IExecutiontable.NextNodes.get(cloudlet).get(i)+ "]: "+temp);
				}
			
			RankupL.set(cloudlet, weight.get(cloudlet)+temp);
			//Log.printLine("cloudlet: "+ cloudlet+ " ,weight:"+RankupL.get(cloudlet)+"="+weight.get(cloudlet)+"+"+temp);
			
			Rankupset[cloudlet] = 1;
			return (weight.get(cloudlet)+ temp );
			} 
			} else {
				Rankupset[cloudlet] = 1;
				return (weight.get(cloudlet) );
				}
			
			
		}	
	}	
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////This class is for keeping Energy of each VM for executing each cloudlet ////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	
	public class VMtoCloudletEnergy {
		public ArrayList<ArrayList<Integer>> VMtoCloudletEnergy = new ArrayList<ArrayList<Integer>>();  // Total time basde on each VM BW
		public ArrayList<ArrayList<Integer>> VMtoCloudletTranEnergy = new ArrayList<ArrayList<Integer>>(); // Transmision time based on each VM
		public ArrayList<ArrayList<Integer>> VMtoCloudletExecEnergy = new ArrayList<ArrayList<Integer>>(); // Total time based on MI/MIPS
		public ArrayList<Integer> VMtoCloudletReadWriteEnergy = new ArrayList<Integer>(); // Total time based on MI/MIPS
		public int MaxReadDelay =0;
		public int MaxWriteDelay =0;
		public int MaxEnergyPerUnitReadWrite =0;
		
		private ArrayList<Integer> temp = new ArrayList<Integer>(); 
		
		
		public void intial(int cloudletsize,List<NetworkVm> vmSpecification,VMInfo IVMInfo, VMtoCloudletTime iVMtoCloudletTime, int[][] VMBW, List<Storage> Storagelist, List<List<File>> Readfiles, List<List<File>> Writefiles){
			//Calculation Execution Energy
			int Itemp = 0 ;
			Log.printLine();
			for(int j=0; j< cloudletsize+1; j++){
				temp.clear();
				for (int i =0; i < (VmSize); i++){
					//Itemp =  (int) ((iVMtoCloudletTime.VMtoCloudletExecTime.get(j).get(i)*IVMInfo.MIPSRatioApp.get(i)*IVMInfo.ExecEnergyUnit.get(i)));
					Itemp =  (int) ((iVMtoCloudletTime.VMtoCloudletExecTime.get(j).get(i)*IVMInfo.ExecEnergyUnit.get(i)));

					//Log.printLine(" "+j+", "+i+": "+ Itemp);
					temp.add(i,Itemp);
					}
				VMtoCloudletExecEnergy.add( new ArrayList<Integer>(temp));
			}
			
			Log.printLine();
			Log.printLine("##################################  Checking ** Execution Energy ** Checking ############################################");
			for(int j=0; j< cloudletsize+1; j++){
				Log.printLine(" "+ VMtoCloudletExecEnergy.get(j));
			}
			Log.printLine("##################################  Checking ** Execution Energy ** Checking ############################################");
			
			
			//Calculation Transmission energy
			//Log.printLine();
			for(int j=0; j< cloudletsize+1; j++){
				temp.clear();
				for (int i =0; i < (VmSize); i++){
					Itemp =  (int) ((CloudletN[j][4]/VMBW[i][VmSize])*IVMInfo.TransEnergyUnit.get(i));
					//Log.printLine(" "+j+", "+i+": "+ Itemp);
					temp.add(i,Itemp);
					}
				VMtoCloudletTranEnergy.add( new ArrayList<Integer>(temp));
			}
			
			Log.printLine();
			Log.printLine("##################################  Checking ** Transmision Energy ** Checking ############################################");
			for(int j=0; j< cloudletsize+1; j++){
				Log.printLine(" "+ VMtoCloudletTranEnergy.get(j));
			}
			Log.printLine("##################################  Checking ** Transmision Energy ** Checking ############################################");
			
			// Read and write Energy
			
			for (int j=0;j< Storagelist.size();j++){
				//if (MaxReadDelay < Storagelist.get(j).getMaxTransferRate()){
					MaxReadDelay = MaxReadDelay + (int) Storagelist.get(j).getMaxTransferRate();
				//}
			}
			MaxReadDelay = MaxReadDelay/Storagelist.size();
			
			for (int j=0;j< Storagelist.size();j++){
				//if (MaxWriteDelay < Storagelist.get(j).getAvgSeekTime()){
					MaxWriteDelay = MaxWriteDelay + (int) Storagelist.get(j).getAvgSeekTime();
				//}
			}
			
			MaxWriteDelay = MaxWriteDelay/Storagelist.size();
			
			for (int j=0;j< Storagelist.size();j++){
				//if (MaxEnergyPerUnitReadWrite < Storagelist.get(j).getEnergypernuit()){
					MaxEnergyPerUnitReadWrite = MaxEnergyPerUnitReadWrite + (int) Storagelist.get(j).getEnergypernuit();
				//}
			}
			
			MaxEnergyPerUnitReadWrite = MaxEnergyPerUnitReadWrite / Storagelist.size();
			
			
			int AveBW =0;
			for(int j=0; j< VmSize; j++){
				AveBW = AveBW+VMBW[j][VmSize];
			}
			AveBW = AveBW/VmSize;
			int MinBW =50000;
			for(int j=0; j< VmSize; j++){
				if (MinBW>VMBW[j][VmSize]){
					MinBW = VMBW[j][VmSize];
				}
			}
			//AveBW = MinBW;
			//Log.printLine("AveBW: "+ AveBW);
			//pressAnyKeyToContinue();
			//cl1.setCreateFile(Writefiles.get(k)); // writing part send by list
			for(int k=0; k< cloudletsize+1; k++){
				double readdelay =0;
				double writedelay =0;
				for(int cn=0; cn < Readfiles.get(k).size();cn++){
					double filesize =0;
					filesize = Readfiles.get(k).get(cn).getSize();
					filesize = (double) filesize * (double) MaxEnergyPerUnitReadWrite;
					readdelay = (double) readdelay+(filesize/AveBW)+(double)(filesize/MaxReadDelay);
				}
				for(int cn=0; cn < Writefiles.get(k).size();cn++){
					double filesize =0;
					filesize = (double) Writefiles.get(k).get(cn).getSize();
					filesize =(double) filesize* (double) MaxEnergyPerUnitReadWrite; 
					writedelay = readdelay+(filesize/(double) AveBW)+(filesize/ (double) MaxWriteDelay);
				}
				
				VMtoCloudletReadWriteEnergy.add(k, (int) ((readdelay+writedelay)));
			}
			
			Log.printLine();
			Log.printLine("##################################  Checking ** File Transfer Energy ** Checking ############################################");
			for(int j=0; j< cloudletsize+1; j++){
				Log.printLine(" "+ VMtoCloudletReadWriteEnergy.get(j));
			}
			Log.printLine("##################################  Checking ** File Transfer Energy ** Checking ############################################");
								
			
			
			
			
			
			
			//Calculation total execution energy
			//Log.printLine();
			for(int j=0; j< cloudletsize+1; j++){
				temp.clear();
				for (int i =0; i < (VmSize); i++){
					Itemp =  (int) (VMtoCloudletExecEnergy.get(j).get(i)+ VMtoCloudletTranEnergy.get(j).get(i)+ VMtoCloudletReadWriteEnergy.get(j));
					//Log.printLine(" "+j+", "+i+": "+ Itemp);
					temp.add(i,Itemp);
					}
				VMtoCloudletEnergy.add( new ArrayList<Integer>(temp));
			}
			
			Log.printLine();
			Log.printLine("##################################  Checking ** Total execution Energy ** Checking ############################################");
			for(int j=0; j< cloudletsize+1; j++){
				Log.printLine(" "+ VMtoCloudletEnergy.get(j));
			}
				
			Log.printLine("##################################  Checking ** Total execution Energy ** Checking ############################################");
			
		}// End of Initialization
		
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////This function used for calculating EnergypertimeUnit for each cloudlet on each VM//////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void EnergyPerTimeUnit (List<NetworkVm> vmSpecification,VMtoCloudletTime IVMtoCloudletTime,VMtoCloudletEnergy IVMtoCloudletEnergy){
		// After assignment of cloudlet to VM, and delay calulation, in this phase NetworkCloudlet is going to be created
			for(int x=0; x < VmSize ;x++){
		    	ArrayList<Double> EnrgyPerTimeUnit = new ArrayList<Double>();
				for (int y =0; y < i;y++){
				    	//calculating EnergyPerTimeunit
				    	if (y == 0 || y == i){
				    		EnrgyPerTimeUnit.add(y , 0.0);
				    	} else {
				    		EnrgyPerTimeUnit.add(y, (double) (IVMtoCloudletEnergy.VMtoCloudletEnergy.get(y).get(x)/IVMtoCloudletTime.VMtoCloudletTime.get(y).get(x)));

				    		//EnrgyPerTimeUnit.add(y, (double) (IVMtoCloudletEnergy.VMtoCloudletEnergy.get(y).get(x)));
				    	}
				    	vmSpecification.get(x).setEnrgyPerTimeUnit(EnrgyPerTimeUnit);
				    }
			}
			Log.printLine("********************    Energy Per Time Unit     *****************************************");

			for(int x=0; x < VmSize ;x++){
				Log.printLine(vmSpecification.get(x).getEnrgyPerTimeUnit());
			}
			Log.printLine("********************    Energy Per Time Unit     *****************************************");

		}
	
	
	
	
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public WorkflowApp(int type, int appID, double deadline, int numbervm, int userId) {
		super(type, appID, deadline, numbervm, userId);
		exeTime = 100;
		this.numbervm = 3;
	}
	
		
	@Override
	public void createCloudletList(int AppNo,List<Integer> vmIdList, List<NetworkVm> vmSpecification, int VMBW [][],int NoApp,VMsTimeExecution IVMsTimeExecution, VMInfo IVMInfo, NetworkDatacenter linkDC) {
		//pressAnyKeyToContinue();
		//pressAnyKeyToContinue();
		
		SheetNo = NoApp;
		long fileSize = NetworkConstants.FILE_SIZE;
		long outputSize = NetworkConstants.OUTPUT_SIZE;
		int memory = 100;
		int charge = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		//int i = 0; //orginal
		
		//Reza,BDMCC: Array for Scheduling
		VmSize = vmIdList.size();
		
		// List of file need to be read and write by each cloudlet
		List<List<File>> Readfiles= new ArrayList<List<File>>();
		List<List<File>> Writefiles= new ArrayList<List<File>>();

			
		//Elements of array is as sort of this: Cloudlet ID, FileSize, Outputsize, Battery, memory, Vm, MIPS, No. of Send, Number of receive
		
		for (int i=0; i<NumberOfCloudlet; i++ ){
					for (int j=0; j<BumberofCloudletElement; j++ ){
						CloudletN[i][j] = 0;
					}
		}
		
		//Elements of array is as sort of this: EdgeID, Source, Destination, Data Size, BW, Time
		for ( i=0; i<NumberOfEdge; i++ ){
					for ( j=0; j<NumberOfEdgeElement; j++ ){
						Edge[i][j] = 0; 
					}
		}
		
		/*************************************************************************************************************************************
		 * ***********************************************************************************************************************************
		 * Phase one reading file and get all cloudlet and Edge 
		 */
		
		Executiontable IExecutiontable = new Executiontable();
		IExecutiontable.NPintial();
		
		readingCloudlet(IExecutiontable,SheetNo);//reading cloudlets and Edges
		
		
		
		readingfiles(VmSize,Readfiles,Writefiles,SheetNo);
		
		
		
		//IVMInfo.intialAppRatio(vmSpecification, SheetNo);
		
		VMtoCloudletEnergy IVMtoCloudletEnergy = new VMtoCloudletEnergy();
		IVMtoCloudletEnergy.intial(i, vmSpecification, IVMInfo, IVMtoCloudletTime,VMBW,linkDC.getStorageList(),Readfiles,Writefiles);
		
		VMEtoCloudletE IVMEtoCloudletE = new VMEtoCloudletE();
		IVMEtoCloudletE.intial(i, vmSpecification, IVMtoCloudletEnergy, IVMInfo);
		
		
		//VMMIPStoCloudletMI IVMMIPStoCloudletMI = new VMMIPStoCloudletMI();
		//IVMMIPStoCloudletMI.intial(i, vmSpecification, IVMtoCloudletEnergy, IVMInfo);
		
		//VMsTimeExecution IVMsTimeExecution = new VMsTimeExecution();
		//IVMsTimeExecution.intial();
		
		IExecutiontable.Intial(IVMEtoCloudletE, IVMtoCloudletTime);
		
		Log.printLine();
				
		IVMtoCloudletTime.Rankup(i, 0, vmSpecification, IExecutiontable);
		/*
		Log.printLine();
		Log.printLine("#################################################  Weight  #####################################################");
		
		Log.printLine(IVMtoCloudletTime.weight);

		Log.printLine("#################################################  Weight  #####################################################");

		Log.printLine("#################################################  Rankup  #####################################################");

		Log.printLine(IVMtoCloudletTime.RankupL);

		Log.printLine("#################################################  Rankup  #####################################################");
		*/

		CostFun1BestFit(IExecutiontable,IVMtoCloudletTime,IVMtoCloudletEnergy,IVMEtoCloudletE,IVMsTimeExecution,IVMInfo, vmSpecification);

		//CostFun2BestFitOneVm(IExecutiontable,IVMtoCloudletTime,IVMtoCloudletEnergy,IVMEtoCloudletE,IVMsTimeExecution,IVMInfo, vmSpecification);
		
		//CostFun3HESTScheduling(IExecutiontable,IVMtoCloudletTime,IVMtoCloudletEnergy,IVMEtoCloudletE,IVMsTimeExecution,IVMInfo, vmSpecification);
		
		//CostFun4HESTSchedulingPlus(IExecutiontable,IVMtoCloudletTime,IVMtoCloudletEnergy,IVMEtoCloudletE,IVMsTimeExecution,IVMInfo, vmSpecification);
		
		//Costfun5678Scheduling(IExecutiontable,IVMtoCloudletTime,IVMtoCloudletEnergy,IVMEtoCloudletE,IVMsTimeExecution,IVMInfo, vmSpecification);

		// EnergyPerTimeUnit calculation
		//EnergyPerTimeUnit ( vmSpecification, IVMtoCloudletTime, IVMtoCloudletEnergy);
		int l =0;
		
		
		
			
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////        THIRD STEP CALCULATION OF DELAY AND PROCESS /////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
			CalDelay (vmSpecification,VMBW);
			
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////     CREATING NETWORKCLOUDLET AND THEIR STAGE    ////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
			
			/*
				 * Phase two scheduling is finished 
				 * Phase we should create NetworkCloudlet and their stages
				 */
			
		for(int k =0; k< (i+1); k++){
		 	NetworkCloudlet cl1 = new NetworkCloudlet(
					 		CloudletN[k][0],
					 		CloudletN[k][1],
					 		CloudletN[k][2],
					 		CloudletN[k][3],
					 		CloudletN[k][4],
					 		CloudletN[k][5],
					 		CloudletN[k][6],
							utilizationModel,
							utilizationModel,
							utilizationModel);
					cl1.numStage = CloudletN[k][7];
					cl1.setUserId(userId);
					cl1.submittime = CloudSim.clock();
					cl1.currStagenum = -1;
					cl1.setVmId(CloudletN[k][8]);
					cl1.setExeEnergy(new ArrayList<Integer> (IVMtoCloudletEnergy.VMtoCloudletEnergy.get(k)) );
					cl1.setExeTime(new ArrayList<Integer> (IVMtoCloudletTime.VMtoCloudletTime.get(k)) );
					cl1.AppNum = AppNo;
					/*
					Log.printLine();
					Log.printLine();
					Log.printLine("Network Cloulet---------------------------------------------------");
					Log.printLine("Cloudlet ID: " +  CloudletN[k][0] + " Cloudlet lenght: " + CloudletN[k][1] + " Cloudlet PesNumber	: " + CloudletN[k][2]  +
								  " Cloudlet FileSize:" + CloudletN[k][3] + " Cloudlet OutputSize:" + CloudletN[k][4] + " Cloudlet Battery:" +
								  CloudletN[k][5] + " Cloudlet Memory: " + CloudletN[k][6] + " VM for execution:" + CloudletN[k][8])  ;
					Log.printLine( " Cloudlet Battery:" + CloudletN[k][5] + " Cloudlet Memory: " + CloudletN[k][6] + " VM for execution:" + CloudletN[k][8]
								  + " NumStage:" + CloudletN[k][7])  ;
					Log.printLine("Network Cloulet---------------------------------------------------");
					*/
					//First we should check for receiving then execution then Sending
					//***
					//Receiving satges adding
					int SIDtemp = 0;
					//int transtime = 0;
					for( l =0; l< (j+1) ; l++){
						if (Edge[l][2] == k){
							cl1.stages.add(new TaskStage(
									NetworkConstants.WAIT_RECV,// Type of Stage is receiving
									0,// data
									0,// time //Edge[l][5]
									SIDtemp, //ID
									CloudletN[k][6], //Memory
									CloudletN[(Edge[l][1])][8],//Which vm is assigned
									Edge[l][1],
									new ArrayList<Integer> (IVMtoCloudletEnergy.VMtoCloudletEnergy.get(k))
									));//VPeer
					/*		
					Log.printLine("Stage of Receiving---------------------------------------------------");
					Log.printLine("Type of Stage: " + NetworkConstants.WAIT_RECV+ " data: " + 0 + " time: " + 0  +
								  " Stage ID:" + SIDtemp  + " Memory:" + CloudletN[k][6] + " Peer:" +
								  CloudletN[(Edge[l][1])][8] + " VPeer: " + Edge[l][1]);
					Log.printLine("Stage of Receiving---------------------------------------------------");
					*/
					//transtime = Edge[l][5]; // i used reciving because delay will be in second step
					//Log.printLine("time of Edge of Cloudlet:" + transtime);
					//Log.printLine("####################################################################");
					SIDtemp++;
					
					
					}
					}
					
					//Executing satge adding
					
					cl1.stages.add(new TaskStage(
									NetworkConstants.EXECUTION,// Type of Stage is receiving
									0,// data
									CloudletN[k][12] ,// time for execution and transmission time
									SIDtemp, //ID
									CloudletN[k][6], //Memory
									CloudletN[k][8] ,//Peer 
									CloudletN[k][0] ,
									new ArrayList<Integer> (IVMtoCloudletEnergy.VMtoCloudletEnergy.get(k))
							 ));//VPeer
					/*
					Log.printLine("Stage of execution---------------------------------------------------");
					Log.printLine("Type of Stage: " + NetworkConstants.EXECUTION + " data: " + 0 + " time: " + (CloudletN[k][12]+transtime)  + 
								  " Excution time:"+CloudletN[k][12] + " Transtime:" + transtime +
								  " Stage ID:" + SIDtemp  + " Memory:" + CloudletN[k][6] + " Peer:" +
								  CloudletN[k][8] + " VPeer: " + CloudletN[k][0]);
					Log.printLine("Stage of execution---------------------------------------------------");
					*/
					SIDtemp++;
					
					//Sending satges adding
					
					int p = 0;
					for( p =0; p < (j+1) ; p++){
						if (Edge[p][1] == k){
					cl1.stages.add(new TaskStage(
									NetworkConstants.WAIT_SEND,// Type of Stage is Sending
									0,// data
									Edge[p][5],// time   Edge[p][5]
									SIDtemp, //ID
									CloudletN[k][6], //Memory
									CloudletN[(Edge[p][2])][8],//Peer (because schaduling is not finished i used 0)
									Edge[p][2],
									new ArrayList<Integer> (IVMtoCloudletEnergy.VMtoCloudletEnergy.get(k))
									));//VPeer
					//Log.printLine("time of Edge of Cloudlet:" + Edge[p][5]);

					/* Log.printLine("Stage of Sendin---------------------------------------------------");
					Log.printLine("Type of Stage: " + NetworkConstants.WAIT_SEND + " data: " + 0 + " time: " + transtime  +
								  " Stage ID:" + SIDtemp  + " Memory:" + CloudletN[k][6] + " Peer:" +
								  CloudletN[(Edge[p][2])][8] + " VPeer: " + Edge[p][2]);
					Log.printLine("Stage of Sending---------------------------------------------------");
					*/
					SIDtemp++;
						}
					}
					
					// In this part we add Files which need to be read or write
					//cl1.setCreateFile(Writefiles.get(k)); // writing part send by list
					cl1.setCreateFile(Writefiles.get(k)); // writing part send by list

					for(int cn=0; cn < Readfiles.get(k).size();cn++){
						cl1.addRequiredFile(Readfiles.get(k).get(cn).getName());
						//Log.printLine("Need file name: "+Readfiles.get(k).get(cn).getName()+" , need to be read by VM: " +CloudletN[k][8] );
						linkDC.IReplicaManagement.AddVmToVmReadList(Readfiles.get(k).get(cn).getName(), CloudletN[k][8]);
					
					}
					
					
					
					
					
					
					
					clist.add(cl1);
					
			}
			
		for(int k =0; k< (i+1); k++){
			if(clist.get(k).getFileForWrite()){
				Log.printLine("Cloudlet ID: "+k+" has file need to write");
			}
		}
	}
	public void pressAnyKeyToContinue()
	{ 
	       System.out.println("Press Enter key to continue...");
	       try
	       {
	           System.in.read();
	       }  
	       catch(Exception e)
	       {}  
	}



}

