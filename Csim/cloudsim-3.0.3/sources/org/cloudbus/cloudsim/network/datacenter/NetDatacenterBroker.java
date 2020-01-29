/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Datacenter.ReplicaManagement;
import org.cloudbus.cloudsim.Datacenter.VMInfo;
import org.cloudbus.cloudsim.Datacenter.VMsTimeExecution;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.File;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.distributions.UniformDistr;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * NetDatacentreBroker represents a broker acting on behalf of Datacenter provider. It hides VM
 * management, as vm creation, submission of cloudlets to this VMs and destruction of VMs. NOTE- It
 * is an example only. It work on behalf of a provider not for users. One has to implement
 * interaction with user broker to this broker.
 * 
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 3.0
 */
public class NetDatacenterBroker extends SimEntity {

	// TODO: remove unnecessary variables

	public class VMExecutionTimeB {

	}

	/** The vm list. */
	private List<? extends Vm> vmList;

	/** The vms created list. */
	private List<? extends Vm> vmsCreatedList;

	/** The cloudlet list. */
	private List<? extends NetworkCloudlet> cloudletList;

	private List<? extends AppCloudlet> appCloudletList;

	/** The Appcloudlet submitted list. */
	private final Map<Integer, Integer> appCloudletRecieved;

	private List<? extends Cloudlet> cloudletSubmittedList;

	/** The cloudlet received list. */
	private List<? extends Cloudlet> cloudletReceivedList;

	/** The cloudlets submitted. */
	private int cloudletsSubmitted;

	/** The vms requested. */
	private int vmsRequested;

	/** The vms acks. */
	private int vmsAcks;

	/** The vms destroyed. */
	private int vmsDestroyed;

	/** The datacenter ids list. */
	private List<Integer> datacenterIdsList;

	/** The datacenter requested ids list. */
	private List<Integer> datacenterRequestedIdsList;

	/** The vms to datacenters map. */
	private Map<Integer, Integer> vmsToDatacentersMap;

	/** The datacenter characteristics list. */
	private Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;

	public static NetworkDatacenter linkDC;

	public boolean createvmflag = true;

	public static int cachedcloudlet = 0;
	
	private List<NetworkVm> vmSpecification;


	//added by reza




	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Created a new DatacenterBroker object.
	 * 
	 * @param name name to be associated with this entity (as required by Sim_entity class from
	 *        simjava package)
	 * 
	 * @throws Exception the exception
	 * 
	 * @pre name != null
	 * @post $none
	 */
	public NetDatacenterBroker(String name) throws Exception {
		super(name);

		setVmList(new ArrayList<NetworkVm>());
		setVmsCreatedList(new ArrayList<NetworkVm>());
		setCloudletList(new ArrayList<NetworkCloudlet>());
		setAppCloudletList(new ArrayList<AppCloudlet>());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());
		appCloudletRecieved = new HashMap<Integer, Integer>();

		cloudletsSubmitted = 0;
		setVmsRequested(0);
		setVmsAcks(0);
		setVmsDestroyed(0);

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterRequestedIdsList(new ArrayList<Integer>());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

	}

	/**
	 * This method is used to send to the broker the list with virtual machines that must be
	 * created.
	 * 
	 * @param list the list
	 * 
	 * @pre list !=null
	 * @post $none
	 */
	public void submitVmList(List<? extends Vm> list) {
		getVmList().addAll(list);
	}

	/**
	 * This method is used to send to the broker the list of cloudlets.
	 * 
	 * @param list the list
	 * 
	 * @pre list !=null
	 * @post $none
	 */
	public void submitCloudletList(List<? extends NetworkCloudlet> list) {
		getCloudletList().addAll(list);
	}

	public void setLinkDC(NetworkDatacenter alinkDC) {
		linkDC = alinkDC;
	}

	/**
	 * Processes events available for this Broker.
	 * 
	 * @param ev a SimEvent object
	 * 
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev, NetworkDatacenter Networkdatacenter) {
		switch (ev.getTag()) {
		// Resource characteristics request
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
				processResourceCharacteristicsRequest(ev);
				break;
			// Resource characteristics answer
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				processResourceCharacteristics(ev,  Networkdatacenter);
				break;
			// VM Creation answer

			// A finished cloudlet returned
			case CloudSimTags.CLOUDLET_RETURN:
				processCloudletReturn(ev,  Networkdatacenter);
				break;
			// if the simulation finishes
			case CloudSimTags.END_OF_SIMULATION:
				shutdownEntity();
				break;
			case CloudSimTags.NextCycle:
				if (NetworkConstants.BASE) {
					createVmsInDatacenterBase(linkDC.getId(),  Networkdatacenter);
				}

				break;
			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	/**
	 * Process the return of a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * 
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev, NetworkDatacenter Networkdatacenter) {
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			setDatacenterRequestedIdsList(new ArrayList<Integer>());
			createVmsInDatacenterBase(getDatacenterIdsList().get(0),  Networkdatacenter);
		}
	}

	/**
	 * Process a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * 
	 * @pre ev != $null
	 * @post $none
	 */

	protected void processResourceCharacteristicsRequest(SimEvent ev) {
		setDatacenterIdsList(CloudSim.getCloudResourceList());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloud Resource List received with "
				+ getDatacenterIdsList().size() + " resource(s)");

		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
	}

	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev a SimEvent object
	 * 
	 * @pre ev != null
	 * @post $none
	 */

	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * 
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev, NetworkDatacenter Networkdatacenter) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		cloudletsSubmitted--;
		// all cloudlets executed
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0 && NetworkConstants.iteration > 10) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else { // some cloudlets haven't finished yet
			if (getAppCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();
				createVmsInDatacenterBase(0,  Networkdatacenter);
			}

		}
	}

	/**
	 * Overrides this method when making a new and different type of Broker. This method is called
	 * by {@link #body()} for incoming unknown tags.
	 * 
	 * @param ev a SimEvent object
	 * 
	 * @pre ev != null
	 * @post $none
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null.");
			return;
		}

		Log.printLine(getName() + ".processOtherEvent(): "
				+ "Error - event unknown by this DatacenterBroker.");
	}

	/**
	 * Create the virtual machines in a datacenter and submit/schedule cloudlets to them.
	 * 
	 * @param datacenterId Id of the chosen PowerDatacenter
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void createVmsInDatacenterBase(int datacenterId, NetworkDatacenter Networkdatacenter) {
		// send as much vms as possible for this datacenter before trying the
		// next one
		int requestedVms = 0;
		int AppTypes=0;
		// All host will have two VMs (assumption) VM is the minimum unit
		if (createvmflag) {
			CreateVMs(datacenterId,  Networkdatacenter);
			createvmflag = false;
		}
		// Added By reza, reading app and type from file
		//This phase include read  read/write files in each cloudlet
		List<Integer> AppsList = new ArrayList<Integer>();
		
		try{
		InputStream inp1 = new FileInputStream("d:\\AppsType.xlsx");
		Workbook wb1 = WorkbookFactory.create(inp1);
		Sheet sheet = wb1.getSheetAt(0);
		
		//read all row
		for (org.apache.poi.ss.usermodel.Row row : sheet) {
		if (!(row.getRowNum() ==  0)){ // row 0 is description
		
		//reading this need to be read or write
		Cell file1 = (Cell) row.getCell(0); 
		Cell file0 = (Cell) row.getCell(1); 
		AppsList.add((int) file0.getNumericCellValue());
		Log.printLine("AppsList"+AppsList);
		}else {
			Cell file0 = (Cell) row.getCell(0); 
			Cell file1 = (Cell) row.getCell(1);
			Cell file2 = (Cell) row.getCell(2);
			AppTypes = (int) file2.getNumericCellValue();
		}
		}
		//Write the output to a file
		FileOutputStream fileOut = new FileOutputStream("d:\\AppsType.xlsx");
		wb1.write(fileOut);
		fileOut.close();
		} catch (Exception e) {
		e.printStackTrace();
		}
	
		// generate Application execution Requests
		int NoApp = AppsList.size();
		//int TypeApp = 1;
		for (int i = 0; i < NoApp; i++) {
			this.getAppCloudletList().add(
					new WorkflowApp(AppCloudlet.APP_Workflow, NetworkConstants.currentAppId, 0, 0, getId()));
			NetworkConstants.currentAppId++;

		}
		int k = 0;

		
		// Reza,BDMCC,this is end of my change for VM allocation to APP
		
		vmSpecification = linkDC.getVmList();
			
		linkDC.setVMBW(linkDC.readingVM(vmSpecification.size()))  ;// reading Bw between VMs
		for (int z =0; z<(vmSpecification.size()); z++){
			vmSpecification.get(z).setBw((long) linkDC.getVMBW()[z][vmSpecification.size()]); 
		}
					
		
		linkDC.IVMInfo.intial(vmSpecification,vmSpecification.size());
		int testtt =0;
		Log.printLine("No. of execution,testtt: "+ testtt);
		testtt++;
		linkDC.IVMsTimeExecution.intial(vmSpecification.size()); 
		
		// reading app ratio
		for (int z =0; z<(AppTypes); z++){
			linkDC.IVMInfo.intialAppRatio(vmSpecification, z);
		}
		
				
		//Random AppR = new Random(TypeApp+1);
		//int RandomApp = 0;
		
		
		// creat files and warmup
		
		Log.printLine("Static replica, warm up");
		WarmUp(linkDC.cl,linkDC,linkDC.IReplicaManagement);
		linkDC.IReplicaManagement.ReplicaTest( linkDC.IReplicaManagement);// this function used for checking out put

		//pressAnyKeyToContinue();
		
		// schedule the application on VMs
		int appcounter =0;
		for (AppCloudlet app : this.getAppCloudletList()) {

			List<Integer> vmids = new ArrayList<Integer>();
			int numVms = linkDC.getVmList().size();
			
			// Reza,BDMCC, I changed this part to assign VMs to App
			/*
			UniformDistr ufrnd = new UniformDistr(0, numVms, 5);
			for (int i = 0; i < app.numbervm; i++) {

				int vmid = (int) ufrnd.sample();
				vmids.add(vmid);

			}
			*/
			
			for (int i = 0; i < numVms; i++) {
				
				vmids.add(i);

			}
			
			
			if (vmids != null) {
				if (!vmids.isEmpty()) {
					app.createCloudletList(appcounter,vmids,vmSpecification,linkDC.getVMBW(),AppsList.get(appcounter),linkDC.IVMsTimeExecution,linkDC.IVMInfo,linkDC);
					//for (int i = 0; i < app.numbervm; i++) {
					for (int i = 0; i < app.clist.size(); i++) {
						app.clist.get(i).setUserId(getId());
						appCloudletRecieved.put(app.appID, app.numbervm);
						this.getCloudletSubmittedList().add(app.clist.get(i));
						cloudletsSubmitted++;

						// Sending cloudlet
						sendNow(
								getVmsToDatacentersMap().get(this.getVmList().get(0).getId()),
								CloudSimTags.CLOUDLET_SUBMIT,
								app.clist.get(i));
					}
					System.out.println("app" + (k++));
				}
			}
			appcounter++;
			//pressAnyKeyToContinue();
			//pressAnyKeyToContinue();
		}
		
		
		
		setAppCloudletList(new ArrayList<AppCloudlet>());
		if (NetworkConstants.iteration < 10) {

			NetworkConstants.iteration++;
			this.schedule(getId(), NetworkConstants.nexttime, CloudSimTags.NextCycle);
		}

		setVmsRequested(requestedVms);
		setVmsAcks(0);
		

		//Added by Reza for checking VmReadlist Checking
		/*
		Log.printLine("Checking VmReadLis: ");
		for (int i=0; i < linkDC.IReplicaManagement.VmReadList.size();i++){
			Log.printLine(linkDC.IReplicaManagement.VmReadList.get(i));
		}
		linkDC.IReplicaManagement.ReplicaTest(linkDC.IReplicaManagement);
		*/
		//pressAnyKeyToContinue();
		//pressAnyKeyToContinue();
	}

	/* orginal function for creating VMs
	private void CreateVMs(int datacenterId) {
		// two VMs per host
		int numVM = linkDC.getHostList().size() * NetworkConstants.maxhostVM;
		for (int i = 0; i < numVM; i++) {
			int vmid = i;
			int mips = 1;
			long size = 10000; // image size (MB)
			long battery = 10;
			int ram = 512; // vm memory (MB)
			long bw = 1000;
			int pesNumber = NetworkConstants.HOST_PEs / NetworkConstants.maxhostVM;
			String vmm = "Xen"; // VMM name

			// create VM
			NetworkVm vm = new NetworkVm(
					vmid,
					getId(),
					mips,
					pesNumber,
					ram,
					bw,
					size,
					battery,
					vmm,
					new NetworkCloudletSpaceSharedScheduler());
			linkDC.processVmCreateNetwork(vm);
			// add the VM to the vmList
			getVmList().add(vm);
			getVmsToDatacentersMap().put(vmid, datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vmid));
		}
	}
*/ //Orginal part for creating VMs
	
	// Reza, BDMCC, Read Excel file for creating  Machine
		private void CreateVMs(int datacenterId, NetworkDatacenter Networkdatacenter) {
					try {
					InputStream inp = new FileInputStream("d:\\VMlist.xlsx");

					Workbook wb = WorkbookFactory.create(inp);
					Sheet sheet = wb.getSheetAt(0);
					//read all row
					for (org.apache.poi.ss.usermodel.Row row : sheet) {
					// org.apache.poi.ss.usermodel.Row row = sheet.getRow(1);
					if (!(row.getRowNum() ==  0)){
					
					Cell VMID = (Cell) row.getCell(0);
					Cell VMMIPS  = (Cell) row.getCell(1);
					Cell VMSize  = (Cell) row.getCell(2);
					Cell VMBattery = (Cell) row.getCell(3);
					Cell VMRAM = (Cell) row.getCell(4);
					Cell VMBW = (Cell) row.getCell(5);
					Cell VMPesN = (Cell) row.getCell(6);
					Cell VMM = (Cell) row.getCell(7);
						       
						    Log.printLine("Test---------------------------------------------------");
						    Log.printLine("VM ID: " + VMID.getNumericCellValue() + "VMMIPS: " + VMMIPS.getNumericCellValue() + "VMSize: " + VMSize.getNumericCellValue() + 
						    		"VMBattery:" + VMBattery.getNumericCellValue() + "VMRAM:" + VMRAM.getNumericCellValue() + "VMBW:" + VMBW.getNumericCellValue()+ 
						    		"VMPesN:" + VMPesN.getNumericCellValue() + "VMM:" + VMM.getStringCellValue()) ;
						   
						    Log.printLine("Test---------------------------------------------------");
						    
			
		//	int numVM = linkDC.getHostList().size() * NetworkConstants.maxhostVM;
		//	for (int i = 0; i < numVM; i++) {
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
				NetworkVm vm = new NetworkVm(
						vmid,
						getId(),
						mips,
						pesNumber,
						ram,
						bw,
						size,
						battery,
						vmm,
						new NetworkCloudletSpaceSharedScheduler());
				linkDC.processVmCreateNetwork(vm,  Networkdatacenter);
				// add the VM to the vmList
				getVmList().add(vm);
				getVmsToDatacentersMap().put(vmid, datacenterId);
				getVmsCreatedList().add(VmList.getById(getVmList(), vmid));
				
		        	} // belong to IF
				    } // Belong to for
					// Write the output to a file
				    FileOutputStream fileOut = new FileOutputStream("VMlist.xls");
				    wb.write(fileOut);
				    fileOut.close();
					} catch (Exception e) {
						e.printStackTrace();
					} 
					
			}   
		// end of reading file for creating VMs
	
	
	
	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none /** Destroy the virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		for (Vm vm : getVmsCreatedList()) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
		}

		getVmsCreatedList().clear();
	}

	/**
	 * Send an internal event communicating the end of the simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	private void finishExecution() {
		sendNow(getId(), CloudSimTags.END_OF_SIMULATION);
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#shutdownEntity()
	 */
	@Override
	public void shutdownEntity() {
		Log.printLine(getName() + " is shutting down...");
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmList the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet list
	 */
	@SuppressWarnings("unchecked")
	public <T extends NetworkCloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}

	/**
	 * Sets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletList the new cloudlet list
	 */
	protected <T extends NetworkCloudlet> void setCloudletList(List<T> cloudletList) {
		this.cloudletList = cloudletList;
	}

	@SuppressWarnings("unchecked")
	public <T extends AppCloudlet> List<T> getAppCloudletList() {
		return (List<T>) appCloudletList;
	}

	public <T extends AppCloudlet> void setAppCloudletList(List<T> appCloudletList) {
		this.appCloudletList = appCloudletList;
	}

	/**
	 * Gets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet submitted list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletSubmittedList() {
		return (List<T>) cloudletSubmittedList;
	}

	/**
	 * Sets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletSubmittedList the new cloudlet submitted list
	 */
	protected <T extends Cloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
		this.cloudletSubmittedList = cloudletSubmittedList;
	}

	/**
	 * Gets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet received list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}

	/**
	 * Sets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletReceivedList the new cloudlet received list
	 */
	protected <T extends Cloudlet> void setCloudletReceivedList(List<T> cloudletReceivedList) {
		this.cloudletReceivedList = cloudletReceivedList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmsCreatedList() {
		return (List<T>) vmsCreatedList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmsCreatedList the vms created list
	 */
	protected <T extends Vm> void setVmsCreatedList(List<T> vmsCreatedList) {
		this.vmsCreatedList = vmsCreatedList;
	}

	/**
	 * Gets the vms requested.
	 * 
	 * @return the vms requested
	 */
	protected int getVmsRequested() {
		return vmsRequested;
	}

	/**
	 * Sets the vms requested.
	 * 
	 * @param vmsRequested the new vms requested
	 */
	protected void setVmsRequested(int vmsRequested) {
		this.vmsRequested = vmsRequested;
	}

	/**
	 * Gets the vms acks.
	 * 
	 * @return the vms acks
	 */
	protected int getVmsAcks() {
		return vmsAcks;
	}

	/**
	 * Sets the vms acks.
	 * 
	 * @param vmsAcks the new vms acks
	 */
	protected void setVmsAcks(int vmsAcks) {
		this.vmsAcks = vmsAcks;
	}

	/**
	 * Increment vms acks.
	 */
	protected void incrementVmsAcks() {
		vmsAcks++;
	}

	/**
	 * Gets the vms destroyed.
	 * 
	 * @return the vms destroyed
	 */
	protected int getVmsDestroyed() {
		return vmsDestroyed;
	}

	/**
	 * Sets the vms destroyed.
	 * 
	 * @param vmsDestroyed the new vms destroyed
	 */
	protected void setVmsDestroyed(int vmsDestroyed) {
		this.vmsDestroyed = vmsDestroyed;
	}

	/**
	 * Gets the datacenter ids list.
	 * 
	 * @return the datacenter ids list
	 */
	protected List<Integer> getDatacenterIdsList() {
		return datacenterIdsList;
	}

	/**
	 * Sets the datacenter ids list.
	 * 
	 * @param datacenterIdsList the new datacenter ids list
	 */
	protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
		this.datacenterIdsList = datacenterIdsList;
	}

	/**
	 * Gets the vms to datacenters map.
	 * 
	 * @return the vms to datacenters map
	 */
	protected Map<Integer, Integer> getVmsToDatacentersMap() {
		return vmsToDatacentersMap;
	}

	/**
	 * Sets the vms to datacenters map.
	 * 
	 * @param vmsToDatacentersMap the vms to datacenters map
	 */
	protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
		this.vmsToDatacentersMap = vmsToDatacentersMap;
	}

	/**
	 * Gets the datacenter characteristics list.
	 * 
	 * @return the datacenter characteristics list
	 */
	protected Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
		return datacenterCharacteristicsList;
	}

	/**
	 * Sets the datacenter characteristics list.
	 * 
	 * @param datacenterCharacteristicsList the datacenter characteristics list
	 */
	protected void setDatacenterCharacteristicsList(
			Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
		this.datacenterCharacteristicsList = datacenterCharacteristicsList;
	}

	/**
	 * Gets the datacenter requested ids list.
	 * 
	 * @return the datacenter requested ids list
	 */
	protected List<Integer> getDatacenterRequestedIdsList() {
		return datacenterRequestedIdsList;
	}

	/**
	 * Sets the datacenter requested ids list.
	 * 
	 * @param datacenterRequestedIdsList the new datacenter requested ids list
	 */
	protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
		this.datacenterRequestedIdsList = datacenterRequestedIdsList;
	}
	
	
	
	// Added by Reza
		private static long WarmUp(List<File> cl, NetworkDatacenter networkdatacenter, ReplicaManagement IReplicaManagement) {
			//random numbers are 0,1,2,3 
			double Writedelay =0.0;
			try{
				  
	        	//Write Vm Energy
				XSSFWorkbook workbook = new XSSFWorkbook();
				XSSFSheet sheet1 = workbook.createSheet("StaticReplica");
	        	int rowCount = 0;
	        	
	        	Row row0 = sheet1.createRow(rowCount++);
	        		
	        	Cell C02 = row0.createCell(0); 
	        	C02.setCellValue("File Name"); 
	        		
	        	Cell C12 = row0.createCell(1); 
	        	C12.setCellValue("File Size");
	        		        		
	        	Cell C13 = row0.createCell(2); 
	        	C13.setCellValue("Replica");

	        	Cell C14 = row0.createCell(3); 
	        	C14.setCellValue("VmID");
	       
	        	Cell C15 = row0.createCell(4); 
	        	C15.setCellValue("Vm Energy deduction");
			
			Iterator<File> iter = cl.iterator();
			double deductedenergy = 0;
			double energy = 0;
			
			int VmID;
			int StorageHostID;
			
			
			while (iter.hasNext()) {
				int select = 0;
				File file = iter.next();
				File master = file.makeMasterCopy();
				ArrayList<Integer> numbers = new ArrayList<Integer>();   
				Random randomGenerator = new Random();
				while (!(select== 1)){
					int temphost;
					int random = randomGenerator .nextInt(networkdatacenter.getStorageList().size());
				    if (!numbers.contains(random)) {
				    	temphost = random;
					    StorageHostID = networkdatacenter.getStorageList().get(temphost).getHostID();
						VmID = networkdatacenter.getHostList().get(StorageHostID).getVmList().get(0).getId();
						deductedenergy = 0;
						deductedenergy = NetDatacenterBroker.linkDC.getVmList().get(VmID).getbattery();
						energy = ((master.getSize()*NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(VmID))
								/networkdatacenter.getVmList().get(VmID).getBw());
						if (!(energy > deductedenergy)){
							select = 1;
							numbers.add(random);//random
						}
				    }
				    
				}
				int pickedS = 0;
	        if(select == 1){	
			pickedS = numbers.get(numbers.size()-1);
			select = 0;
	        }else {
	        	Log.printLine("Fail to creat replica for wrm up");
	        	return 0;
	        }
				
				//Master Copy
	        	int FileCounter = IReplicaManagement.GetFIDCounter();
	        	FileCounter++;
	        	master.setfID(FileCounter);// for future use we need to set FID in file
	        	file.setfID(FileCounter);// 
				networkdatacenter.getStorageList().get(pickedS).addFile(master);
				
				StorageHostID = networkdatacenter.getStorageList().get(pickedS).getHostID();
				VmID = networkdatacenter.getHostList().get(StorageHostID).getVmList().get(0).getId();
					
					//Deducting Energy
					deductedenergy = 0;
					//Log.printLine("Energy of Vm before deduction: "+ deductedenergy);

					deductedenergy = NetDatacenterBroker.linkDC.getVmList().get(VmID).getbattery();
					//Log.printLine("Energy of Vm:"+ VmID+" before deduction: "+ deductedenergy);
					//Log.printLine("File Size"+ master.getSize()+"BW: "+ networkdatacenter.getVmList().get(VmID).getBw()+"Energy per sec: "+NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(VmID));
					energy = ((master.getSize()*NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(VmID))
							/networkdatacenter.getVmList().get(VmID).getBw());
					deductedenergy = deductedenergy - energy;
					//Log.printLine("Energy of Vm after deduction: "+ deductedenergy);

					//linkDC.getVmList().get(VmID).setbattery((long) deductedenergy);
					//networkdatacenter.IVMInfo.Energy.set(VmID, (int) deductedenergy);
					
					//Intiation of Replicas
					IReplicaManagement.intiantionAddReplica((FileCounter), master, VmID);
					IReplicaManagement.IncFIDCounter();
					
					Row row1 = sheet1.createRow(rowCount++);
	        		
		        	Cell C11 = row1.createCell(0); 
		        	C11.setCellValue(master.getName()); 
		        		
		        	Cell C22 = row1.createCell(1); 
		        	C22.setCellValue(master.getSize());
		        		        		
		        	Cell C33 = row1.createCell(2); 
		        	C33.setCellValue("Master");

		        	Cell C44 = row1.createCell(3); 
		        	C44.setCellValue(VmID);
		       
		        	Cell C55 = row1.createCell(4); 
		        	C55.setCellValue(energy);
					
					
					
					
					
				//Replica 1
		        	while (!(select== 1)){
						int temphost;
						int random = randomGenerator .nextInt(networkdatacenter.getStorageList().size());
					    if (!numbers.contains(random)) {
					    	temphost = random;
						    StorageHostID = networkdatacenter.getStorageList().get(temphost).getHostID();
							VmID = networkdatacenter.getHostList().get(StorageHostID).getVmList().get(0).getId();
							deductedenergy = 0;
							deductedenergy = NetDatacenterBroker.linkDC.getVmList().get(VmID).getbattery();
							energy = ((master.getSize()*NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(VmID))
									/networkdatacenter.getVmList().get(VmID).getBw());
							if (!(energy > deductedenergy)){
								select = 1;
								numbers.add(random);//
							}
					    }
					    
					}
		        if(select == 1){	
				pickedS = numbers.get(numbers.size()-1);
				select = 0;
		        }else {
		        	Log.printLine("Fail to creat replica for wrm up");
		        	return 0;
		        }
		        file.setMasterCopy(false);
				networkdatacenter.getStorageList().get(pickedS).addFile(file);
				StorageHostID = networkdatacenter.getStorageList().get(pickedS).getHostID();
				VmID = networkdatacenter.getHostList().get(StorageHostID).getVmList().get(0).getId();

					
					//Deducting Energy
					deductedenergy = 0;
					deductedenergy = NetDatacenterBroker.linkDC.getVmList().get(VmID).getbattery();
					//Log.printLine("Energy of Vm before deduction: "+ deductedenergy);
					energy = ((master.getSize()*NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(VmID))
							/networkdatacenter.getVmList().get(VmID).getBw());
					deductedenergy = deductedenergy - energy;
					//	Log.printLine("Energy of Vm after deduction: "+ deductedenergy);
					//NetDatacenterBroker.linkDC.getVmList().get(VmID).setbattery((long) deductedenergy);
					//networkdatacenter.IVMInfo.Energy.set(VmID, (int) deductedenergy);
					
					//Initiation of Replicas, Just in master needed to increase FID Counter
					FileCounter = IReplicaManagement.GetFIDCounter();
					IReplicaManagement.intiantionAddReplica((FileCounter), file, VmID);
					
					
					
					Row row2 = sheet1.createRow(rowCount++);
	        		
		        	Cell C61 = row2.createCell(0); 
		        	C61.setCellValue(master.getName()); 
		        		
		        	Cell C62 = row2.createCell(1); 
		        	C62.setCellValue(master.getSize());
		        		        		
		        	Cell C63 = row2.createCell(2); 
		        	C63.setCellValue("Replica 1");

		        	Cell C64 = row2.createCell(3); 
		        	C64.setCellValue(VmID);
		       
		        	Cell C65 = row2.createCell(4); 
		        	C65.setCellValue(energy);
					
					
					
					
					
					
					
					
					
					
				//Replica 2
		        	while (!(select== 1)){
						int temphost;
						int random = randomGenerator .nextInt(networkdatacenter.getStorageList().size());
					    if (!numbers.contains(random)) {
					    	temphost = random;
						    StorageHostID = networkdatacenter.getStorageList().get(temphost).getHostID();
							VmID = networkdatacenter.getHostList().get(StorageHostID).getVmList().get(0).getId();
							deductedenergy = 0;
							deductedenergy = NetDatacenterBroker.linkDC.getVmList().get(VmID).getbattery();
							energy = ((master.getSize()*NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(VmID))
									/networkdatacenter.getVmList().get(VmID).getBw());
							if (!(energy > deductedenergy)){
								select = 1;
								numbers.add(random);//random
							}
					    }
					    
					}
		        if(select == 1){	
				pickedS = numbers.get(numbers.size()-1);
				select = 0;
		        }else {
		        	Log.printLine("Fail to creat replica for wrm up");
		        	return 0;
		        }
		        file.setMasterCopy(false);
				networkdatacenter.getStorageList().get(pickedS).addFile(file);
				StorageHostID = networkdatacenter.getStorageList().get(pickedS).getHostID();
				VmID = networkdatacenter.getHostList().get(StorageHostID).getVmList().get(0).getId();

					//Deducting Energy
					deductedenergy = 0;
					deductedenergy = NetDatacenterBroker.linkDC.getVmList().get(VmID).getbattery();
				//	Log.printLine("Energy of Vm before deduction: "+ deductedenergy);
					energy = ((master.getSize()*NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(VmID))
							/networkdatacenter.getVmList().get(VmID).getBw());
					deductedenergy = deductedenergy - energy;
					//	Log.printLine("Energy of Vm after deduction: "+ deductedenergy);
					//NetDatacenterBroker.linkDC.getVmList().get(VmID).setbattery((long) deductedenergy);
					//networkdatacenter.IVMInfo.Energy.set(VmID, (int) deductedenergy);
					
					//Initiation of Replicas, Just in master needed to increase FID Counter
					FileCounter = IReplicaManagement.GetFIDCounter();
					IReplicaManagement.intiantionAddReplica((FileCounter), file, VmID);
					
					
					
					
					Row row3 = sheet1.createRow(rowCount++);
	        		
		        	Cell C71 = row3.createCell(0); 
		        	C71.setCellValue(master.getName()); 
		        		
		        	Cell C72 = row3.createCell(1); 
		        	C72.setCellValue(master.getSize());
		        		        		
		        	Cell C73 = row3.createCell(2); 
		        	C73.setCellValue("Replica 2");

		        	Cell C74 = row3.createCell(3); 
		        	C74.setCellValue(VmID);
		       
		        	Cell C75 = row3.createCell(4); 
		        	C75.setCellValue(energy);
				
			}
			
		 	try (FileOutputStream outputStream = new FileOutputStream("d:\\wresult\\Replica-StaticR.xlsx")) {
	    		workbook.write(outputStream);
	    	}

			} catch (Exception e) {
	    	e.printStackTrace();
	    }
			
		return (long)0;
		}
		
	private void pressAnyKeyToContinue()
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
