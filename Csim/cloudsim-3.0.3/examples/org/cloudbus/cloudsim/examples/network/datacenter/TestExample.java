package org.cloudbus.cloudsim.examples.network.datacenter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.File;
import org.cloudbus.cloudsim.HarddriveStorage;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ParameterException;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.network.datacenter.EdgeSwitch;
import org.cloudbus.cloudsim.network.datacenter.NetDatacenterBroker;
import org.cloudbus.cloudsim.network.datacenter.NetworkCloudlet;
import org.cloudbus.cloudsim.network.datacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;
import org.cloudbus.cloudsim.network.datacenter.NetworkHost;
import org.cloudbus.cloudsim.network.datacenter.NetworkVm;
import org.cloudbus.cloudsim.network.datacenter.NetworkVmAllocationPolicy;
import org.cloudbus.cloudsim.network.datacenter.TaskStage;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class TestExample {

	/** The vmlist. */
	private static List<NetworkVm> vmlist;

	/**
	 * Creates main() to run this example.
	 * 
	 * @param args
	 *            the args
	 */
	public static void main(String[] args) {

		Log.printLine("Starting CloudSimExample1...");

		try {
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			NetworkDatacenter datacenter0 = createDatacenter("Datacenter_0");

			// Third step: Create Broker
			NetDatacenterBroker broker = createBroker();
			broker.setLinkDC(datacenter0);
			// broker.setLinkDC(datacenter0);
			// Fifth step: Create one Cloudlet

			vmlist = new ArrayList<NetworkVm>();

			// submit vm list to the broker

			broker.submitVmList(vmlist);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation(datacenter0);

			CloudSim.stopSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			//broker.getAppCloudletList().get(num_user).clist.get(0);
			printCloudletList(newList,datacenter0.getVmList(),datacenter0);
			System.out.println("numberofcloudlet " + newList.size() + " Cached "
					+ NetDatacenterBroker.cachedcloudlet + " Data transfered "
					+ NetworkConstants.totaldatatransfer);

			Log.printLine("CloudSimExample1 finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}


	//Reza, I change this part to read hosts from excel file
	private static NetworkDatacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine

		List<NetworkHost> hostList = new ArrayList<NetworkHost>();
		
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
			hostList.add(new NetworkHost(
					hostId,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					battery,
					peList,
					new VmSchedulerTimeShared(peList))); // This is our machine
		
			    

		Log.printLine("Test---------------------------------------------------");
		Log.printLine("Host ID: " + hostId + " RAM: " + ram + " BW: " + bw + " Storage:" + storage + " Battery:" + battery + " HMIPS:" + mips );
		Log.printLine("Test---------------------------------------------------");
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
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////         CREATING fILES WHICH NEED FOR SIMULATION         //////////////////////////////////////
		try{
			InputStream inp = new FileInputStream("d:\\FileToWrite.xlsx");
			
			Workbook wb1 = WorkbookFactory.create(inp);
			Sheet sheet = wb1.getSheetAt(0);
			//read all row
			for (org.apache.poi.ss.usermodel.Row row : sheet) {
			// org.apache.poi.ss.usermodel.Row row = sheet.getRow(1);
			if (!(row.getRowNum() ==  0)){
			Cell FileName = (Cell) row.getCell(2);
			Cell FileSize = (Cell) row.getCell(3);
			
			File file = new File(FileName.getStringCellValue()  , (int) FileSize.getNumericCellValue());
			file.setMasterCopy(true);
			//Random random = new Random(); 
			//int pickedS = random.nextInt(storageList.size()) ;
			//Master Copy
			
			cl.add(file);
			
			//storageList.get(pickedS).addFile(file);
		//	Log.printLine(" File name:" +file.getName()+ " with size of "+ file.getSize());
		//	Log.printLine(" harddisk read speed is :"+ storageList.get(pickedS).getMaxTransferRate()+ 
		//			"Harddisk write speed" +storageList.get(pickedS).getAvgSeekTime()+
		//			"harddisk energy unit" + storageList.get(pickedS).getEnergypernuit());
					
			
			}
		
			}
	// Write the output to a file
    FileOutputStream fileOut = new FileOutputStream("FileToWrite.xls");
    wb1.write(fileOut);
    fileOut.close();
	} catch (Exception e) {
		e.printStackTrace();
	}
		
		
		
		
		
		
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

		//test
		//for(int z=0; z < storageList.size();z++){
		//	System.out.println("Used disk space on hd "+z+" =" + storageList.get(z).getCurrentSize());
		//}
		
		
		// test
		/*
		File file1;
		try {
			file1 = new File("reza-r", 10000);
			file1.setMasterCopy(true);
			storageList.get(0).addFile(file1);
			
		} catch (ParameterException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		*/
		
		// 6. Finally, we need to create a NetworkDatacenter object.
		NetworkDatacenter datacenter = null;
		try {
			datacenter = new NetworkDatacenter(
					name,
					characteristics,
					new NetworkVmAllocationPolicy(hostList),
					storageList,
					cl,
					0);

		} catch (Exception e) {
			e.printStackTrace();
		}
		// Create Internal Datacenter network
		CreateNetwork(2, datacenter);
		
		
		//ReplicaPolicy(cl,datacenter);
		return datacenter;
		
		
	}
	
	//Reza, BDMCC: reading and creasting dactacener from file finished.
	
	
	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	/**
	 * Creates the broker.
	 * 
	 * @return the datacenter broker
	 */
	private static NetDatacenterBroker createBroker() {
		NetDatacenterBroker broker = null;
		try {
			broker = new NetDatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects.
	 * 
	 * @param list
	 *            list of Cloudlets
	 * @param list2 
	 * @param datacenter0 
	 * @throws IOException
	 */
	private static void printCloudletList(List<Cloudlet> list, List<Vm> list2, NetworkDatacenter DC01) throws IOException {//
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
        		        		
        	}else {
        		Cell C02 = row1.createCell(0); 
        		C02.setCellValue("VmID"); // VmID
        		
        		Cell C12 = row1.createCell(1); 
        		C12.setCellValue("Battery");// VmEnergy
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

	static void CreateNetwork(int numhost, NetworkDatacenter dc) {

		// Edge Switch
		EdgeSwitch edgeswitch[] = new EdgeSwitch[1];

		for (int i = 0; i < 1; i++) {
			edgeswitch[i] = new EdgeSwitch("Edge" + i, NetworkConstants.EDGE_LEVEL, dc);
			// edgeswitch[i].uplinkswitches.add(null);
			dc.Switchlist.put(edgeswitch[i].getId(), edgeswitch[i]);
			// aggswitch[(int)
			// (i/Constants.AggSwitchPort)].downlinkswitches.add(edgeswitch[i]);
		}
		int counter =0;
		for (Host hs : dc.getHostList()) {
			NetworkHost hs1 = (NetworkHost) hs;
			hs1.bandwidth = NetworkConstants.BandWidthEdgeHost;
			int switchnum = (int) (hs.getId() / NetworkConstants.EdgeSwitchPort);
			//Log.printLine("counter no.: "+ counter);
			edgeswitch[switchnum].hostlist.put(hs.getId(), hs1);
			dc.HostToSwitchid.put(hs.getId(), edgeswitch[switchnum].getId());
			hs1.sw = edgeswitch[switchnum];
			List<NetworkHost> hslist = hs1.sw.fintimelistHost.get(0D);
			if (hslist == null) {
				hslist = new ArrayList<NetworkHost>();
				hs1.sw.fintimelistHost.put(0D, hslist);
			}
			hslist.add(hs1);
			counter++;
		}

	}
	
	
}
