/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.File;
import org.cloudbus.cloudsim.HarddriveStorage;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;


/**
 * A simple example showing how to create
 * a datacenter with one host and run two
 * cloudlets on it. The cloudlets run in
 * VMs with the same MIPS requirements.
 * The cloudlets will take the same time to
 * complete the execution.
 */
public class CloudSimExample2 {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {

		Log.printLine("Starting CloudSimExample2...");

	        try {
	        	// First step: Initialize the CloudSim package. It should be called
	            	// before creating any entities.
	            	int num_user = 1;   // number of cloud users
	            	Calendar calendar = Calendar.getInstance();
	            	boolean trace_flag = false;  // mean trace events

	            	// Initialize the CloudSim library
	            	CloudSim.init(num_user, calendar, trace_flag);

	            	// Second step: Create Datacenters
	            	//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
	            	@SuppressWarnings("unused")
					Datacenter datacenter0 = createDatacenter("Datacenter_0");
	            	// checking
	            	/*
	            	Log.printLine("Test---------------------------------------------------");
	            	for (int i =0; i < datacenter0.getHostList().size();i++ ){
	            		Host hostt = datacenter0.getHostList().get(i);
	            		Storage hd1 = datacenter0.getStorageList().get(i);
	            		Log.printLine("No.: "+ i+ " ,Host ID: " + hostt.getId() + " RAM: " + hostt.getRam() + " BW: " + hostt.getBw() + " Storage:" 
	            		+ hostt.getStorage() + " Battery:" + hostt.getbattery() + " HMIPS:" + hostt.getTotalMips()+
	            		" StorageRead Delay: "+hd1.getMaxTransferRate()+"StorageWriteDelay: "+hd1.getAvgSeekTime()+" EnergyPerUnit: "+ hd1.getEnergypernuit());
            		}
	            	Log.printLine("Test---------------------------------------------------");		
	            	pressAnyKeyToContinue();
	        		pressAnyKeyToContinue();
	            	*/
	            	
	            	
	            	
	            	//Third step: Create Broker
	            	DatacenterBroker broker = createBroker();
	            	int brokerId = broker.getId();
	            	
	            	//Fourth step: Create one virtual machine
	            	vmlist = new ArrayList<Vm>();
	            	List<Vm> vmlistt = new ArrayList<Vm>();
	            	vmlistt = CreateVMs(datacenter0,brokerId);
	            	
	            	//checking
	            	/*
	            	Log.printLine("Test---------------------------------------------------");
	            	for(int i=0; i <vmlistt.size();i++ ){
	            		Vm vmt = vmlistt.get(i);
	            		Log.printLine("No.: "+i+" VM ID: " + vmt.getId() + " VMMIPS: " + vmt.getMips() + " VMSize: " + vmt.getSize() + 
				    	" VMBattery: " + vmt.getbattery() + " VMRAM: " + vmt.getRam() + " VMBW: " + vmt.getBw()+ 
				    	" VMPesN: " + vmt.getNumberOfPes() + " VMM: " + vmt.getVmm()) ;
				   
	            	}
					Log.printLine("Test---------------------------------------------------");
					pressAnyKeyToContinue();
	        		pressAnyKeyToContinue();
	            	*/
	            	
	            	datacenter0.NumberOfVms = vmlistt.size();
	            	//Reading VMBW
	            	
	            	datacenter0.setVMBW(datacenter0.readingVM(vmlistt.size()))  ;// reading Bw between VMs
	    			for (int z =0; z<vmlistt.size(); z++){
	    				vmlistt.get(z).setBw((long) datacenter0.getVMBW()[z][vmlistt.size()]);
	    			}
	    			//checking
	    			/*
	            	Log.printLine("Test---------------------------------------------------");
	            	for(int i=0; i <vmlistt.size();i++ ){
	            		for (int j=0; j< vmlistt.size()+1;j++ ){
	            			Log.print(datacenter0.getVMBW()[i][j]+" ,");
	            		}
	            		Log.printLine("  Vm.BW upated to: "+vmlistt.get(i).getBw());
	            	}
					Log.printLine("Test---------------------------------------------------");
					pressAnyKeyToContinue();
	        		pressAnyKeyToContinue();
	        		*/
	    			
	        		vmlist = vmlistt; // after update vm.bw
	            	//submit vm list to the broker
	            	broker.submitVmList(vmlist);
	            	
	            	
	    			
	            	
	            	
	            	//Fifth step: Create  Cloudlets
	            	cloudletList = new ArrayList<Cloudlet>();
	            	cloudletList = CreateCloudlets(datacenter0,brokerId);
	            	//Checking
	            	/*
	            	Log.printLine("Test---------------------------------------------------");
	            	for(int i=0; i < cloudletList.size(); i++){
	            		Cloudlet cl = cloudletList.get(i);
	            		Log.printLine("No.: "+i+" CLID: "+ cl.getCloudletId()+" CL Length: "+cl.getCloudletLength()+" CL FileSize: "+cl.getCloudletFileSize());
	            	}
	            	Log.printLine("Test---------------------------------------------------");
					pressAnyKeyToContinue();
	        		pressAnyKeyToContinue();
	            	*/
	            	
	            	// reading Vm infor and app ratio
	            	
	            	datacenter0.IVMInfo.intial(vmlist, vmlist.size());
	            	
	            	//checking
	            	/*
	            	Log.printLine("Test---------------------------------------------------");
	            	for(int i=0; i < vmlist.size(); i++){
	            		
	            		Log.printLine("No.: "+i+" VM MIPS: "+ datacenter0.IVMInfo.MIPS.get(i) +" VM Energy: "+datacenter0.IVMInfo.Energy.get(i)
	            		+" VM BW: "+datacenter0.IVMInfo.BW.get(i));
	            	}
	            	Log.printLine("Test---------------------------------------------------");
					pressAnyKeyToContinue();
	        		pressAnyKeyToContinue();
	        		*/
	        		
	            	for (int z =0; z<(cloudletList.size()); z++){
	        			datacenter0.IVMInfo.intialAppRatio(cloudletList.get(z).getCloudletId());
	        		}
	            	//checking
	            	/*           	
	            	Log.printLine();
	            	Log.printLine("##################################  Checking ** VM information ** Checking ############################################");

	            	Log.printLine("MIPSRatioApp: "+datacenter0.IVMInfo.MIPSRatioApp);
	            	Log.printLine("ExecEnergyUnit: "+datacenter0.IVMInfo.ExecEnergyUnit);
	            	Log.printLine("TransEnergyUnit: "+ datacenter0.IVMInfo.TransEnergyUnit);

	            	Log.printLine("##################################  Checking ** VM information ** Checking ############################################");
					pressAnyKeyToContinue();
	        		pressAnyKeyToContinue();
	        		*/
	            	
	            		        		
	            	for (int z =0; z<(vmlist.size()); z++){
	            		int temp =0;
	            		for (int zz =0; zz<(cloudletList.size()); zz++){
	            			/*
	            			if(zz==0){
	    	            		datacenter0.IVMInfo.AveTransEnergyUnit.add(datacenter0.IVMInfo.TransEnergyUnit.get(zz).get(z));
	    	            		} else {
	    	            			int temp =0;
	    	            			temp = (datacenter0.IVMInfo.AveTransEnergyUnit.get(z)+ datacenter0.IVMInfo.TransEnergyUnit.get(zz).get(z));
	    	            			datacenter0.IVMInfo.AveTransEnergyUnit.set(z, temp);
	    	            		}
	    	            	*/
	            			//Log.printLine("zz: "+zz+" z: "+ z);
	            			temp = temp + datacenter0.IVMInfo.TransEnergyUnit.get(zz).get(z);
	            		}
	            		
	            		datacenter0.IVMInfo.AveTransEnergyUnit.add(z, (temp/cloudletList.size()));
	        		}
	            	/*
	            	Log.printLine("test aveTransEnergyPerUnit: "+datacenter0.IVMInfo.AveTransEnergyUnit);
	            	pressAnyKeyToContinue();
	        		pressAnyKeyToContinue();
	        		*/
	            	
	            	List<List<File>> Readfiles= new ArrayList<List<File>>();
	        		List<List<File>> Writefiles= new ArrayList<List<File>>();
	            	
	            	readingfiles (cloudletList.size(),Readfiles,Writefiles);
	            	for(int i=0;i < cloudletList.size();i++){
	            		for(int j=0; j< Readfiles.get(i).size();j++){
	            			//cloudletList.get(i).addRequiredFile(Readfiles.get(i).get(j).getName());
	            			cloudletList.get(i).addRequiredFile(Readfiles.get(i).get(j).getName());
	            		}
	            	}
	            	
	            	/*
	            	for(int i=0; i < cloudletList.size();i++  ){
	            		for(int j=0; j < cloudletList.get(i).getRequiredFiles().size();j++  ){
	            		Log.printLine("CloudletID.: "+cloudletList.get(i).getCloudletId()+ " File Name: "+ 
	            				cloudletList.get(i).getRequiredFiles().get(j));
	            	
	            		}
	            	}
	        		pressAnyKeyToContinue();
	        		pressAnyKeyToContinue();
	            	*/
	            	
	            	//submit cloudlet list to the broker
	            	broker.submitCloudletList(cloudletList);

	            	
	            	//bind the cloudlets to the vms. This way, the broker
	            	// will submit the bound cloudlets only to the specific VM
	            	//broker.bindCloudletToVm(cloudlet1.getCloudletId(),vm1.getId());
	            	//broker.bindCloudletToVm(cloudlet2.getCloudletId(),vm2.getId());

	            	// Sixth step: Starts the simulation
	            	CloudSim.startSimulation(datacenter0);


	            	// Final step: Print results when simulation is over
	            	List<Cloudlet> newList = broker.getCloudletReceivedList();
	            	
	            	
	            	CloudSim.stopSimulation();

	            
	            	printCloudletList(newList,broker.getVmList(),datacenter0);

	            	Log.printLine("CloudSimExample2 finished!");
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	            Log.printLine("The simulation has been terminated due to an unexpected error");
	        }
	    }

		

		private static Datacenter createDatacenter(String name){

	        // Here are the steps needed to create a PowerDatacenter:
	        // 1. We need to create a list to store
	    	//    our machine
	    	List<Host> hostList = new ArrayList<Host>();

			LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are adding Host storage to this list with Host ID
			Random rand = new Random(); //added by reza
			// Read Excel file for creating  Machine
			try{
			InputStream inp = new FileInputStream("d:\\Hostlist.xlsx");

			Workbook wb1 = WorkbookFactory.create(inp);
			Sheet sheet = wb1.getSheetAt(0);
			//read all row
			for (org.apache.poi.ss.usermodel.Row row : sheet) {
			// org.apache.poi.ss.usermodel.Row row = sheet.getRow(1);
			if (!(row.getRowNum() ==  0)){
			Cell HID = (Cell) row.getCell(0);
			    
			Cell HRAM = (Cell) row.getCell(1);
			Cell HBW = (Cell) row.getCell(2);
			Cell HStorage = (Cell) row.getCell(3);
			Cell HBattery = (Cell) row.getCell(4);
			Cell HMIPS = (Cell) row.getCell(5);
			Cell HReadDelay = (Cell) row.getCell(6);
			Cell HWriteDelay = (Cell) row.getCell(7);
			Cell HEnergy = (Cell) row.getCell(8);
				    /*   
				    Log.printLine("Test---------------------------------------------------");
				    Log.printLine("Host ID: " + HID.getNumericCellValue() + "RAM: " + HRAM.getNumericCellValue() + "BW: " + HBW.getNumericCellValue() + 
				    		"Storage:" + HStorage.getNumericCellValue() + "Battery:" + HBattery.getNumericCellValue() + "HMIPS:" + HMIPS.getNumericCellValue() );
				   
				    Log.printLine("Test---------------------------------------------------");
				    */

			// 2. A Machine contains one or more PEs or CPUs/Cores.
			// In this example, it will have only one core.
			List<Pe> peList = new ArrayList<Pe>();

			int mips = (int) HMIPS.getNumericCellValue(); // add because of reading from file
			//int mips = 1000;

			// 3. Create PEs and add these into a list.
			peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to
			// store Pe id and MIPS Rating

			// 4. Create Host with its id and list of PEs and add them to the list
			// of machines
			int hostId = (int) HID.getNumericCellValue(); // added becasue we reading from file
			
			//int ram = 2048; // host memory (MB)
			int ram = (int) HRAM.getNumericCellValue(); // host memory (MB)
			
			//long storage = 1000000; // host storage
			long storage = (long) HStorage.getNumericCellValue(); // host storage
			
			// Creating Storage and add to StorageList
			//Creating Storages
			
			
			HarddriveStorage hd = new HarddriveStorage(storage); //Creat HardDisk
			double ReadDelay = (double) HReadDelay.getNumericCellValue();
			hd.setMaxTransferRate((int) ReadDelay);//Set Read Speed
			
			double WriteDelay = HWriteDelay.getNumericCellValue();
			hd.setAvgSeekTime( WriteDelay);// set for write speed
			
			double Energy = HEnergy.getNumericCellValue();
			hd.SetEnergypernuit(Energy);//588*(pickedS/126000)
			hd.SetHostID(hostId);// Set HostId
			storageList.add(hd); //adding to Sorage List then attaching to datacenter
			
			

			//long battery = 150; // host storage
			long battery = (long) HBattery.getNumericCellValue();
			
			//int bw = 10000;
			int bw = (int) HBW.getNumericCellValue();
			
				// 2. A Machine contains one or more PEs or CPUs/Cores.
				// In this example, it will have only one core.
				// 3. Create PEs and add these into an object of PowerPeList.
				
				
				
				// 4. Create PowerHost with its id and list of PEs and add them to
				// the list of machines
				hostList.add(new Host(
						hostId,
						new RamProvisionerSimple(ram),
						new BwProvisionerSimple(bw),
						storage,
						battery,
						peList,
						new VmSchedulerTimeShared(peList))); // This is our machine
			
				    

			//Log.printLine("Test---------------------------------------------------");
			//Log.printLine("Host ID: " + hostId + " RAM: " + ram + " BW: " + bw + " Storage:" + storage + " Battery:" + battery + " HMIPS:" + mips );
			//Log.printLine("Test---------------------------------------------------");
				    }
				    }
					// Write the output to a file
				    FileOutputStream fileOut = new FileOutputStream("Hostlist.xls");
				    wb1.write(fileOut);
				    fileOut.close();
					} catch (Exception e) {
						e.printStackTrace();
					}

			
			List<File> cl = new ArrayList<File>();
			
			
			// 5. Create a DatacenterCharacteristics object that stores the
			// properties of a data center: architecture, OS, list of
			// Machines, allocation policy: time- or space-shared, time zone
			// and its price (G$/Pe time unit).
			String arch = "x86"; // system architecture
			String os = "Linux"; // operating system
			String vmm = "Xen";
			double time_zone = 10.0; // time zone this resource located
			double cost = 3.0; // the cost of using processing in this resource
			double costPerMem = 0.05; // the cost of using memory in this resource
			double costPerStorage = 0.001; // the cost of using storage in this
			// resource
			double costPerBw = 0.0; // the cost of using bw in this resource
			
			

			DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
					arch,
					os,
					vmm,
					hostList,
					time_zone,
					cost,
					costPerMem,
					costPerStorage,
					costPerBw);
			// 6. Finally, we need to create a NetworkDatacenter object.
			Datacenter datacenter = null;
			try {
				datacenter = new Datacenter(
						name,
						characteristics,
						new VmAllocationPolicySimple(hostList),
						storageList,
						cl,
						0);

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
	return datacenter;
	}
			
		
	    //We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	    //to the specific rules of the simulated scenario
	    private static DatacenterBroker createBroker(){

	    	DatacenterBroker broker = null;
	        try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	    	return broker;
	    }

	    /**
	     * Prints the Cloudlet objects
	     * @param list  list of Cloudlets
	     */
	    private static void printCloudletList(List<Cloudlet> list, List<Vm> list2, Datacenter DC01) throws IOException {//
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////         CREATING fILES FOR OUTPUT SIMULATION         //////////////////////////////////////

try{
XSSFWorkbook workbook = new XSSFWorkbook();
XSSFSheet sheet = workbook.createSheet("cloudlet");
//Write all row
int rowCount = 0;
Row row = sheet.createRow(rowCount++);


int size = list.size();
Cloudlet cloudlet;

String indent = "    ";
Log.printLine();
Log.printLine("========== OUTPUT ==========");
Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
+ indent + "Time" + indent + "Start Time" + indent + "Finish Time");

Cell C01 = row.createCell(0); 
C01.setCellValue("Cloudlet ID"); 

Cell C11 = row.createCell(1); 
C11.setCellValue("STATUS");

Cell C2 = row.createCell(2); 
C2.setCellValue("VM ID");

Cell C3 = row.createCell(3); 
C3.setCellValue("Time");

Cell C4 = row.createCell(4); 
C4.setCellValue("Start Time");

Cell C5 = row.createCell(5); 
C5.setCellValue("Finish Time");

Cell C6 = row.createCell(6); 
C6.setCellValue("Read Delay");

Cell C7 = row.createCell(7); 
C7.setCellValue("read Energy");

Cell C8 = row.createCell(8); 
C8.setCellValue("Trans. Delay");

Cell C9 = row.createCell(9); 
C9.setCellValue("Trans. Energy");

Cell C10 = row.createCell(10); 
C10.setCellValue("Exe. Energy");



DecimalFormat dft = new DecimalFormat("###.##");
for (int i = 0; i < size; i++) {
Row row2 = sheet.createRow(rowCount++);
cloudlet = list.get(i);
Log.print(indent + cloudlet.getCloudletId() + indent + indent);
Cell C90 = row2.createCell(0); 
C90.setCellValue((int) cloudlet.getCloudletId());

if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
Log.print("SUCCESS");
Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent
+ cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
+ indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
+ dft.format(cloudlet.getFinishTime()));
}

Cell C91 = row2.createCell(1); 
C91.setCellValue(cloudlet.getCloudletStatus());

Cell C92 = row2.createCell(2); 
C92.setCellValue(cloudlet.getVmId());

Cell C93 = row2.createCell(3); 
C93.setCellValue(dft.format(cloudlet.getActualCPUTime()));

Cell C94 = row2.createCell(4); 
C94.setCellValue(dft.format(cloudlet.getExecStartTime()));

Cell C95 = row2.createCell(5); 
C95.setCellValue(dft.format(cloudlet.getFinishTime()));

Cell C96 = row2.createCell(6); 
C96.setCellValue(dft.format(cloudlet.getReadDelay()));

Cell C97 = row2.createCell(7); 
C97.setCellValue(dft.format(cloudlet.getReadEnergy()));

Cell C98 = row2.createCell(8); 
C98.setCellValue(dft.format(cloudlet.getTransDelay()));

Cell C99 = row2.createCell(9); 
C99.setCellValue(dft.format(cloudlet.getTransEnergy()));

Cell C991 = row2.createCell(10); 
C991.setCellValue(dft.format(cloudlet.getExecutionEnergy()));

}
try (FileOutputStream outputStream = new FileOutputStream("d:\\wresult\\cloudlet-execution.xlsx")) {
workbook.write(outputStream);
}
} catch (Exception e) {
e.printStackTrace();
}
try{

//Write Vm Energy
XSSFWorkbook workbook = new XSSFWorkbook();
XSSFSheet sheet1 = workbook.createSheet("VmEnergy");
int rowCount = 0;

for (int x=0; x < (list2.size()+1); x++){

Row row1 = sheet1.createRow(rowCount++);
if (!(rowCount == 1)){
Cell C02 = row1.createCell(0); 
C02.setCellValue((int) list2.get(x-1).getId()); // VmID

Cell C12 = row1.createCell(1); 
C12.setCellValue((int) list2.get(x-1).getbattery());// VmEnergy

Cell C22 = row1.createCell(2); 
C22.setCellValue((int) list2.get(x-1).getExeE());// Execution Energy

Cell C32 = row1.createCell(3); 
C32.setCellValue((int) list2.get(x-1).getTransE());// Trans Energy

Cell C42 = row1.createCell(4); 
C42.setCellValue((int) list2.get(x-1).getReadE());// Read Energy

Cell C52 = row1.createCell(5); 
C52.setCellValue((int) list2.get(x-1).getReplicaManagmentE());// Replica Management Energy

}else {
Cell C02 = row1.createCell(0); 
C02.setCellValue("VmID"); // VmID

Cell C12 = row1.createCell(1); 
C12.setCellValue("Battery");// VmEnergy

Cell C22 = row1.createCell(2); 
C22.setCellValue("ExeE");// Execution Energy

Cell C32 = row1.createCell(3); 
C32.setCellValue("TransE");// trans Energy

Cell C42 = row1.createCell(4); 
C42.setCellValue("ReadE");// Read Energy

Cell C52 = row1.createCell(5); 
C52.setCellValue("ReplicaManagementE");// Replica Management Energy
}

}


try (FileOutputStream outputStream = new FileOutputStream("d:\\wresult\\Vm-Energy.xlsx")) {
workbook.write(outputStream);
}




} catch (Exception e) {
e.printStackTrace();
}

// added by me to output about energy
/*
String indent = "    ";
Vm vm;
Log.printLine();
Log.printLine("========== Energy OUTPUT ==========");
Log.printLine("VM ID" + indent +indent+indent+indent+ "Energy" + indent );

for (int i = 0; i < list2.size(); i++) {
vm = list2.get(i);
Log.print(indent + vm.getId() + indent + indent);
Log.printLine(indent + indent + vm.getbattery());
}
*/
Log.printLine(" No of migration");
for (int i=0;i< DC01.MigrationInfo.size(); i++){
Log.printLine(DC01.MigrationInfo.get(i));
}

}
	    
	    private static List<Vm> CreateVMs(Datacenter Networkdatacenter,int brockerID) {
	    	vmlist = new ArrayList<Vm>();
			try {
			InputStream inp = new FileInputStream("d:\\VMlist.xlsx");

			Workbook wb = WorkbookFactory.create(inp);
			Sheet sheet = wb.getSheetAt(0);
			//read all row
			for (org.apache.poi.ss.usermodel.Row row : sheet) {
			if (!(row.getRowNum() ==  0)){
			
			Cell VMID = (Cell) row.getCell(0);
			Cell VMMIPS  = (Cell) row.getCell(1);
			Cell VMSize  = (Cell) row.getCell(2);
			Cell VMBattery = (Cell) row.getCell(3);
			Cell VMRAM = (Cell) row.getCell(4);
			Cell VMBW = (Cell) row.getCell(5);
			Cell VMPesN = (Cell) row.getCell(6);
			Cell VMM = (Cell) row.getCell(7);
				       
				   // Log.printLine("Test---------------------------------------------------");
				   //Log.printLine("VM ID: " + VMID.getNumericCellValue() + "VMMIPS: " + VMMIPS.getNumericCellValue() + "VMSize: " + VMSize.getNumericCellValue() + 
				    	//	"VMBattery:" + VMBattery.getNumericCellValue() + "VMRAM:" + VMRAM.getNumericCellValue() + "VMBW:" + VMBW.getNumericCellValue()+ 
				    	//	"VMPesN:" + VMPesN.getNumericCellValue() + "VMM:" + VMM.getStringCellValue()) ;
				   
				   // Log.printLine("Test---------------------------------------------------");
				    

		int vmid = (int) VMID.getNumericCellValue();
		int mips = (int) VMMIPS.getNumericCellValue();
		long size = (int) VMSize.getNumericCellValue(); // image size (MB)
		long battery = (int) VMBattery.getNumericCellValue(); // image size (MB)
		int ram = (int) VMRAM.getNumericCellValue(); // vm memory (MB)
		long bw = (int) VMBW.getNumericCellValue();
		//int pesNumber = NetworkConstants.HOST_PEs / NetworkConstants.maxhostVM;
		int pesNumber = (int) VMPesN.getNumericCellValue();
		//String vmm = "Xen"; // VMM name
		String vmm = (String) VMM.getStringCellValue();
		// create VM
		Vm vm = new Vm(
				vmid,
				brockerID,
				mips,
				pesNumber,
				ram,
				bw,
				size,
				battery,
				vmm,
				new CloudletSchedulerSpaceShared() );//CloudletSchedulerTimeShared()   CloudletSchedulerSpaceShared()
		
		vmlist.add(vm);
        	} // belong to IF
		    } // Belong to for
			// Write the output to a file
		    FileOutputStream fileOut = new FileOutputStream("VMlist.xls");
		    wb.write(fileOut);
		    fileOut.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return vmlist; 
			
	}   
// end of reading file for creating VMs
	    private static List <Cloudlet> CreateCloudlets(Datacenter datacenter0, int brokerId) {
			cloudletList = new ArrayList<Cloudlet>();
			try {
				InputStream inp = new FileInputStream("d:\\Cloudletlist.xlsx");

				Workbook wb = WorkbookFactory.create(inp);
				Sheet sheet = wb.getSheetAt(0);
				//read all row
				for (org.apache.poi.ss.usermodel.Row row : sheet) {
				// org.apache.poi.ss.usermodel.Row row = sheet.getRow(1);
				if (!(row.getRowNum() ==  0)){
				
				Cell id = (Cell) row.getCell(0);
				Cell length  = (Cell) row.getCell(1);
				Cell pesNumber  = (Cell) row.getCell(2);
				Cell fileSize = (Cell) row.getCell(3);
				Cell charge = (Cell) row.getCell(4);
				Cell outputSize = (Cell) row.getCell(5);
				
            	UtilizationModel utilizationModel = new UtilizationModelFull();


			int clid = (int) id.getNumericCellValue();
			int clPesNumber = (int) pesNumber.getNumericCellValue();
			long cllength = (int) length.getNumericCellValue(); // image size (MB)
			long clfileSize = (int) fileSize.getNumericCellValue(); // image size (MB)
			int clcharge = (int) charge.getNumericCellValue(); // vm memory (MB)
			long cloutputSize = (int) outputSize.getNumericCellValue();


			/*
			Log.printLine("Test---------------------------------------------------");
				    Log.printLine("Cloudlet ID: " + clid + " ,PesNumber: " + clPesNumber + " ,length: " + cllength + 
				    		" ,clfileSize:" + clfileSize + " ,clcharge:" + clcharge + " ,cloutputSize :" + cloutputSize) ;
		    Log.printLine("Test---------------------------------------------------");
			*/	    
	
			// create Cloudlet
        	Cloudlet Cloudlet1 = new Cloudlet(
					clid,
					cllength,
					clPesNumber,
					clfileSize,
					clcharge,
					cloutputSize,
					utilizationModel,
					utilizationModel,
					utilizationModel);
        	Cloudlet1.setUserId(brokerId);
        	Cloudlet1.OrginalLength = cllength;
        	
        	
        	cloudletList.add(Cloudlet1);
			
	        	} // belong to IF
			    } // Belong to for
				// Write the output to a file
			    FileOutputStream fileOut = new FileOutputStream("Cloudletlist.xls");
			    wb.write(fileOut);
			    fileOut.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			return cloudletList;
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////This phase include read  files needed to be read or write in each cloudlet ///////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


public static void readingfiles (int i,List<List<File>> readfiles,List<List<File>> writefiles){// i is cloudlet size

//This phase include read  read/write files in each cloudlet

try{
InputStream inp1 = new FileInputStream("d:\\CloudletFiles.xlsx");
Workbook wb1 = WorkbookFactory.create(inp1);
Sheet sheet = wb1.getSheetAt(0);
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
//read all row 

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
public static void pressAnyKeyToContinue()
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
