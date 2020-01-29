/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.Datacenter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cloudbus.cloudsim.Datacenter.ReplicaManagement;
import org.cloudbus.cloudsim.Datacenter.VMInfo;
import org.cloudbus.cloudsim.Datacenter.VMsTimeExecution;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;


/**
 * DatacentreBroker represents a broker acting on behalf of a user. It hides VM management, as vm
 * creation, sumbission of cloudlets to this VMs and destruction of VMs.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class DatacenterBroker extends SimEntity {

	/** The vm list. */
	protected List<? extends Vm> vmList;

	/** The vms created list. */
	protected List<? extends Vm> vmsCreatedList;

	/** The cloudlet list. */
	protected List<? extends Cloudlet> cloudletList;

	/** The cloudlet submitted list. */
	protected List<? extends Cloudlet> cloudletSubmittedList;

	/** The cloudlet received list. */
	protected List<? extends Cloudlet> cloudletReceivedList;

	/** The cloudlets submitted. */
	protected int cloudletsSubmitted;

	/** The vms requested. */
	protected int vmsRequested;

	/** The vms acks. */
	protected int vmsAcks;

	/** The vms destroyed. */
	protected int vmsDestroyed;

	/** The datacenter ids list. */
	protected List<Integer> datacenterIdsList;

	/** The datacenter requested ids list. */
	protected List<Integer> datacenterRequestedIdsList;

	/** The vms to datacenters map. */
	protected Map<Integer, Integer> vmsToDatacentersMap;

	/** The datacenter characteristics list. */
	protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;

	/**
	 * Created a new DatacenterBroker object.
	 * 
	 * @param name name to be associated with this entity (as required by Sim_entity class from
	 *            simjava package)
	 * @throws Exception the exception
	 * @pre name != null
	 * @post $none
	 */
	public DatacenterBroker(String name) throws Exception {
		super(name);

		setVmList(new ArrayList<Vm>());
		setVmsCreatedList(new ArrayList<Vm>());
		setCloudletList(new ArrayList<Cloudlet>());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());

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
	 * @pre list !=null
	 * @post $none
	 */
	public void submitCloudletList(List<? extends Cloudlet> list) {
		getCloudletList().addAll(list);
	}

	/**
	 * Specifies that a given cloudlet must run in a specific virtual machine.
	 * 
	 * @param cloudletId ID of the cloudlet being bount to a vm
	 * @param vmId the vm id
	 * @pre cloudletId > 0
	 * @pre id > 0
	 * @post $none
	 */
	public void bindCloudletToVm(int cloudletId, int vmId) {
		CloudletList.getById(getCloudletList(), cloudletId).setVmId(vmId);
	}

	/**
	 * Processes events available for this Broker.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev, Datacenter Networkdatacenter) {
		switch (ev.getTag()) {
		// Resource characteristics request
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
				processResourceCharacteristicsRequest(ev);
				break;
			// Resource characteristics answer
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				processResourceCharacteristics(ev);
				break;
			// VM Creation answer
			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev,Networkdatacenter,Networkdatacenter.IReplicaManagement);
				break;
			// A finished cloudlet returned
			case CloudSimTags.CLOUDLET_RETURN:
				processCloudletReturn(ev);
				break;
			// if the simulation finishes
			case CloudSimTags.END_OF_SIMULATION:
				shutdownEntity();
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
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev) {
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			setDatacenterRequestedIdsList(new ArrayList<Integer>());
			createVmsInDatacenter(getDatacenterIdsList().get(0));
		}
	}

	/**
	 * Process a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
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
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev,Datacenter datacenter, ReplicaManagement IReplicaManagement) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
			Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
					+ " has been created in Datacenter #" + datacenterId + ", Host #"
					+ VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
		} else {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
					+ " failed in Datacenter #" + datacenterId);
		}

		incrementVmsAcks();

		// all the requested VMs have been created
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			submitCloudlets();
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				for (int nextDatacenterId : getDatacenterIdsList()) {
					if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
						createVmsInDatacenter(nextDatacenterId);
						return;
					}
				}

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
		// added by reza, creating Replicas after VM creation
		if((datacenter.NumberOfVms-1) == vmId ){
			WarmUp(datacenter, IReplicaManagement);
			//pressAnyKeyToContinue();
			//pressAnyKeyToContinue();
			
			
			// Scheduling => assignment of cloudlet to VM
		
			datacenter.IExecutiontable.NPintial(getCloudletSubmittedList().size());
			datacenter.IVMsTimeExecution.intial(datacenter.NumberOfVms);
			datacenter.IVMtoCloudletTime.intial(getCloudletSubmittedList().size(), vmList, datacenter.getVMBW(), getCloudletSubmittedList(), datacenter);
			datacenter.IVMtoCloudletEnergy.intial(getCloudletSubmittedList().size(), getCloudletSubmittedList(), vmList, datacenter.IVMInfo,datacenter.IVMtoCloudletTime, datacenter.getVMBW(),datacenter.getStorageList(),datacenter);
			// finding miminum energy consumption
			
						double MinimumExeE = BestExeECal(datacenter,getCloudletSubmittedList().size(),vmList.size());
						Log.printLine("Minimum Execution Energy is equal to: "+ MinimumExeE);
						
						pressAnyKeyToContinue();
						pressAnyKeyToContinue();
						//
			
			datacenter.IExecutiontable.Intial(vmList.size(), getCloudletSubmittedList().size(),  datacenter.IVMtoCloudletTime);
			Costfun5678Scheduling(getCloudletSubmittedList(), datacenter.IExecutiontable, 
					datacenter.IVMtoCloudletTime, datacenter.IVMtoCloudletEnergy, datacenter.IVMsTimeExecution, datacenter.IVMInfo, vmList);
			datacenter.cloudletList = new ArrayList<Cloudlet>(cloudletSubmittedList);
			//checking
			/*
			for(int i=0;i < getCloudletSubmittedList().size();i++){
	    			Log.printLine("CloudletID: "+getCloudletSubmittedList().get(i).getCloudletId()+" on VMID: "+getCloudletSubmittedList().get(i).getVmId());
	    		
	    	}
			*/
			
			for(int i=0;i < getCloudletSubmittedList().size();i++){
	    		for(int j=0; j< getCloudletSubmittedList().get(i).getRequiredFiles().size();j++){
	    			datacenter.IReplicaManagement.AddVmToVmReadList(getCloudletSubmittedList().get(i).getRequiredFiles().get(j),getCloudletSubmittedList().get(i).getVmId());
	    			//Log.printLine("CloudletID: "+getCloudletSubmittedList().get(i).getCloudletId()+" on VMID: "+getCloudletSubmittedList().get(i).getVmId() + " Need to read File: "+ getCloudletSubmittedList().get(i).getRequiredFiles().get(j));
	    		}
	    	}
			
	    	//Added by Reza for checking VmReadlist Checking
			
			/*
			Log.printLine("Checking VmReadLis: ");
			for (int i=0; i < datacenter.IReplicaManagement.VmReadList.size();i++){
				Log.printLine(datacenter.IReplicaManagement.VmReadList.get(i));
			}
			
			pressAnyKeyToContinue();
			pressAnyKeyToContinue();
			*/
		 
		}
		
		
		
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
	
	/**
	 * Calculated best Energy consumption by considering minimum energy for executing each task without considering battery limitation
	 * @param CloudletSize 
	 * @param VmSize 
	 */
	public double  BestExeECal(Datacenter datacenter, int CloudletSize, int VmSize)
	{ 	   
		   double TotalExeE =0.0;
		   double TotalExeT = 0.0;
		   double tempT;
		   double temp = 9999999999999999999.9;
	       for (int i =0; i < CloudletSize; i++){
	    	   temp = 9999999999999999999.9;
	    	   tempT = 9999999999999999999.9;
	    	   for (int j=0; j< VmSize;j++){
	    		   double ExeE = datacenter.IVMtoCloudletEnergy.VMtoCloudletEnergy.get(i).get(j);
	    		   double ExeT = datacenter.IVMtoCloudletTime.VMtoCloudletTime.get(i).get(j);
	    		   if(temp > ExeE){
	    			   temp = ExeE;
	    		   }
	    		   if(tempT > ExeT){
	    			   tempT = ExeT;
	    		   }
	    	   }
	    	   //Log.printLine("CLID: "+i+" ExeE: "+ temp + "partial tottally: " +TotalExeE);
	    	   TotalExeE = TotalExeE + temp;
	    	   TotalExeT = TotalExeT + tempT;
	       }
	       Log.printLine("Minimum execution Time: "+ (TotalExeT/VmSize)+" With considering All machine Runining parallel");
		return TotalExeE/1000;
	}	
	
	
	
	
	
	
	/**
	 * Added by reza for creating replicas from reading file
	 */
	private void WarmUp(Datacenter datacenter, ReplicaManagement IReplicaManagement) {

		
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
				Cell FID   = (Cell) row.getCell(0);
				Cell FileName = (Cell) row.getCell(1);
				Cell FileSize = (Cell) row.getCell(2);
				Cell MasterSID   = (Cell) row.getCell(3);
				Cell Replica1SID   = (Cell) row.getCell(4);
				Cell Replica2SID   = (Cell) row.getCell(5);
			
				File file = new File(FileName.getStringCellValue()  , (int) FileSize.getNumericCellValue());
				file.setfID((int) FID.getNumericCellValue());
				file.setMasterCopy(false);
				File master = file.makeMasterCopy();
				master.setfID((int) FID.getNumericCellValue());
			
				
				int FileCounter = IReplicaManagement.GetFIDCounter();
				FileCounter++;
				//checking proocess
				if (!(((int) FID.getNumericCellValue()) ==FileCounter)){
					Log.printLine("termination, there is mismatching in creating files between FIDs");
					CloudSim.terminateSimulation();
					pressAnyKeyToContinue();
					pressAnyKeyToContinue();
				}
				
				// Adding master copy 
				datacenter.getStorageList().get((int) MasterSID.getNumericCellValue()).addFile(master);
				int StorageHostID = datacenter.getStorageList().get((int) MasterSID.getNumericCellValue()).getHostID();
				int VmID = datacenter.getHostList().get(StorageHostID).getVmList().get(0).getId();
				IReplicaManagement.intiantionAddReplica((FileCounter), master, VmID);
				
				IReplicaManagement.IncFIDCounter(); // initial value is "-1"
				
				// Adding Replica1 copy 
				datacenter.getStorageList().get((int) Replica1SID.getNumericCellValue()).addFile(file);
				StorageHostID = datacenter.getStorageList().get((int) Replica1SID.getNumericCellValue()).getHostID();
				VmID = datacenter.getHostList().get(StorageHostID).getVmList().get(0).getId();
				IReplicaManagement.intiantionAddReplica((FileCounter), file, VmID);
				
				// Adding Replica1 copy 
				datacenter.getStorageList().get((int) Replica2SID.getNumericCellValue()).addFile(file);
				StorageHostID = datacenter.getStorageList().get((int) Replica2SID.getNumericCellValue()).getHostID();
				VmID = datacenter.getHostList().get(StorageHostID).getVmList().get(0).getId();
				IReplicaManagement.intiantionAddReplica((FileCounter), file, VmID);
				
				
			}
		
			}
	// Write the output to a file
    FileOutputStream fileOut = new FileOutputStream("FileToWrite.xls");
    wb1.write(fileOut);
    fileOut.close();
	} catch (Exception e) {
		e.printStackTrace();
	}
		IReplicaManagement.ReplicaTest(IReplicaManagement,datacenter);		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		//Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId()
		//		+ " received");
		cloudletsSubmitted--;
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
			Log.printLine(CloudSim.clock() + ": " + getName() + ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();
				createVmsInDatacenter(0);
			}

		}
	}

	/**
	 * Overrides this method when making a new and different type of Broker. This method is called
	 * by {@link #body()} for incoming unknown tags.
	 * 
	 * @param ev a SimEvent object
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
	 * Create the virtual machines in a datacenter.
	 * 
	 * @param datacenterId Id of the chosen PowerDatacenter
	 * @pre $none
	 * @post $none
	 */
	protected void createVmsInDatacenter(int datacenterId) {
		// send as much vms as possible for this datacenter before trying the next one
		int requestedVms = 0;
		String datacenterName = CloudSim.getEntityName(datacenterId);
		for (Vm vm : getVmList()) {
			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
						+ " in " + datacenterName);
				
				sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
				requestedVms++;
			}
		}

		getDatacenterRequestedIdsList().add(datacenterId);

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlets() {
		int vmIndex = 0;
		for (Cloudlet cloudlet : getCloudletList()) {
			Vm vm;
			// if user didn't bind this cloudlet and it has not been executed yet
			if (cloudlet.getVmId() == -1) {
				vm = getVmsCreatedList().get(vmIndex);
			} else { // submit to the specific vm
				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
							+ cloudlet.getCloudletId() + ": bount VM not available");
					continue;
				}
			}

			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
		}

		// remove submitted cloudlets from waiting list
		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
			getCloudletList().remove(cloudlet);
		}
	}

	/**
	 * Destroy the virtual machines running in datacenters.
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
	protected void finishExecution() {
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
		Log.printLine(getName() + " is starting..." + "getID() : " + getId() );
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
	public <T extends Cloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}

	/**
	 * Sets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletList the new cloudlet list
	 */
	protected <T extends Cloudlet> void setCloudletList(List<T> cloudletList) {
		this.cloudletList = cloudletList;
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
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void Costfun5678Scheduling (List<Cloudlet> cloudletz,org.cloudbus.cloudsim.Datacenter.Executiontable IExecutiontable,
		org.cloudbus.cloudsim.Datacenter.VMtoCloudletTime IVMtoCloudletTime,
		org.cloudbus.cloudsim.Datacenter.VMtoCloudletEnergy iVMtoCloudletEnergy
		,VMsTimeExecution IVMsTimeExecution, VMInfo IVMInfo, List<? extends Vm> vmList2){
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("-------------------------------------------  Test of HEST Plus schaduleing    -------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");

//providing one list then based on that provide sort of cloudlet deceasing based on length
ArrayList<Cloudlet> cloudlet = new ArrayList<Cloudlet>(cloudletz);


Cloudlet temp ;

for (int x =0; x < cloudlet.size(); x++ ){
for (int y =0; y < cloudlet.size(); y++ ){
if (cloudlet.get(x).getCloudletTotalLength()>cloudlet.get(y).getCloudletTotalLength()){
temp = cloudlet.get(x);
cloudlet.set(x, cloudlet.get(y));
cloudlet.set(y, temp);
}
}
}

for(int y=0; y < cloudlet.size(); y++ ){
	//Log.printLine("ID: "+cloudlet.get(y).getCloudletId()+ " , length: "+ cloudlet.get(y).getCloudletTotalLength() );
	
}

//This for loop of assignment of phase 5, based on rank up decreasment

double executionT = 0.0;
double StartT = 0.0;
double FinishT = 0.0; 
int VMID = 0;

//Assign all cloudlet based on rankup which sorted
for (int x =0; x < (cloudlet.size()); x++ ){
//find VMID with minimum of EFT
	int mintimevmID = 9999;
	double mintime =999999999999999999.9 ;
	double VmEnergy = 0;
	double ExecutionEnergy =0;
	double EFT =0;
	int select = 0;
	int cloudletID = cloudlet.get(x).getCloudletId();
	for(int m =0; m <vmList2.size();m++){//
		VMID = m;

		executionT = IVMtoCloudletTime.VMtoCloudletTime.get(cloudletID).get(VMID);
		StartT = 0;
		VmEnergy = (IVMInfo.Energy.get(VMID));
		ExecutionEnergy = iVMtoCloudletEnergy.VMtoCloudletEnergy.get(cloudletID).get(VMID);
		EFT = IExecutiontable.EFT(cloudletID,VMID , StartT, executionT, IVMsTimeExecution, IVMtoCloudletTime);
		//Log.printLine(" find VMID with minimum of EFT, VMID: "+ VMID + "is eual to" + IExecutiontable.EFT(RankupID.get(x),VMID , StartT, executionT, IVMsTimeExecution, IVMEtoCloudletE, IVMtoCloudletTime));
		//Log.printLine("mintime: "+mintime);
		
		//CostF1=> (((EFT*ExecutionEnergy*ExecutionEnergy)*(VmEnergy))/100000000)
		//CostF2=> ((EFT*ExecutionEnergy*ExecutionEnergy)*(1/VmEnergy))
		//CostF3=> (VmEnergy-ExecutionEnergy)
		//CostF4=> ExecutionEnergy
		
		
		double CostFun =((EFT*ExecutionEnergy*ExecutionEnergy)*(1/VmEnergy));
		
		//Log.printLine("VMID: "+ VMID+" EFT: "+EFT +" ExeE: "+ExecutionEnergy+" VmEnergy: "+VmEnergy+" Costfun: "+ CostFun);
		if((mintime > (CostFun))&&((VmEnergy-ExecutionEnergy)>0.0)){//
			select = 1;//Math.cbrt
			mintime = CostFun;
			mintimevmID = VMID; 
			}
		}
	if(select==0){
		Log.printLine("terminat during scheduling");
		CloudSim.terminateSimulation();
		pressAnyKeyToContinue();
		pressAnyKeyToContinue();
	}
	//Assign Cloudlet to VM(after this VMID is clear)
	VMID = mintimevmID;
	cloudletz.get(cloudletID).setVmId(VMID);
	cloudletz.get(cloudletID).assigned = true;
	
	//Updating VMexecutionTable
 
	StartT = IExecutiontable.EST(cloudletID,VMID , StartT, executionT, IVMsTimeExecution, IVMtoCloudletTime);
	FinishT = IExecutiontable.EFT(cloudletID,VMID , StartT, executionT, IVMsTimeExecution, IVMtoCloudletTime);
	
	IExecutiontable.StartTime.set(cloudletID, StartT);
	IExecutiontable.FinishTime.set(cloudletID, FinishT);
	IExecutiontable.ExecutionTime.set(cloudletID, executionT);
	IExecutiontable.SelVm.set(cloudletID, VMID);
	
	
	//Update VmsTimeExecution
	Datacenter.VMExecutionTimeB IVMExecutionTimeB = new Datacenter.VMExecutionTimeB();
	IVMExecutionTimeB.start = StartT;
	IVMExecutionTimeB.end = FinishT;
	IVMExecutionTimeB.Cloudlet = cloudletID;
	
	IVMsTimeExecution.VMsSchedule2.get(VMID).add(IVMExecutionTimeB);
	
	//Energy deduction and VMcandidate and one candidate
	VmEnergy = (IVMInfo.Energy.get(VMID));
	ExecutionEnergy = iVMtoCloudletEnergy.VMtoCloudletEnergy.get(cloudletID).get(VMID);
	IVMInfo.Energy.set(VMID, (int) (VmEnergy-ExecutionEnergy));
	//EnergyCandidateupdatePlus(VMID, cloudletID, IVMInfo, IVMtoCloudletEnergy, IVMEtoCloudletE,IExecutiontable);
	/*
	Log.printLine();
	Log.printLine("##################################  Checking **  ** Checking ############################################");
	Log.printLine("VmEnergy: "+VmEnergy+" , ExecutionEnergy: "+ExecutionEnergy+" ,ReminingVmEnergy: "+ (int) (VmEnergy-ExecutionEnergy));
	
	Log.printLine("Start time    : " + IExecutiontable.StartTime);
	
	Log.printLine("Finish time   : " + IExecutiontable.FinishTime);
	
	Log.printLine("Execution time: " + IExecutiontable.ExecutionTime);
	
	Log.printLine("SelVm         : " + IExecutiontable.SelVm);
	
	Log.printLine("##################################  Checking **  ** Checking ############################################");
	
	Log.printLine();
	pressAnyKeyToContinue();
	pressAnyKeyToContinue();
	*/
//	Log.printLine();
//	Log.printLine("VM Energy:");
//	Log.printLine(IVMInfo.Energy);
	
	
//	Log.printLine();
//	Log.printLine("VMsSchedule2 is equal to:");
//	Log.printLine(IVMsTimeExecution.VMsSchedule2);
//	Log.printLine();
	
	
	
	}
}


}
