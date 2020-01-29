/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.cloudbus.cloudsim.Datacenter.ReplicaManagement;
import org.cloudbus.cloudsim.Datacenter.VMInfo;
import org.cloudbus.cloudsim.Datacenter.VMsTimeExecution;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.HostList;

 
/**
 * Datacenter class is a CloudResource whose hostList are virtualized. It deals with processing of
 * VM queries (i.e., handling of VMs) instead of processing Cloudlet-related queries. So, even
 * though an AllocPolicy will be instantiated (in the init() method of the superclass, it will not
 * be used, as processing of cloudlets are handled by the CloudletScheduler and processing of
 * VirtualMachines are handled by the VmAllocationPolicy.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class Datacenter extends SimEntity {

	/** The characteristics. */
	private DatacenterCharacteristics characteristics;

	/** The regional cis name. */
	private String regionalCisName;

	/** The vm provisioner. */
	private VmAllocationPolicy vmAllocationPolicy;

	/** The last process time. */
	private double lastProcessTime;

	/** The storage list. */
	private List<Storage> storageList;

	/** The vm list. */
	private List<? extends Vm> vmList;

	/** The scheduling interval. */
	private double schedulingInterval;

	//Added By Reza
	// Array of BW between VMs
	private int VMBW[][];
	public int NumberOfVms = 0;
	
	public VMtoCloudletEnergy IVMtoCloudletEnergy = new VMtoCloudletEnergy();

	
	public VMtoCloudletTime IVMtoCloudletTime = new VMtoCloudletTime();
	
	public VMsTimeExecution IVMsTimeExecution = new VMsTimeExecution();
	
	public VMInfo IVMInfo = new VMInfo();
	public ReplicaManagement IReplicaManagement = new ReplicaManagement();
	
	public Executiontable IExecutiontable = new Executiontable();
	public int FileNo =0;
	
	public List<File> cl ;
	 
	//Added By reza
	public ArrayList<Cloudlet> cloudletList;
	public ArrayList <Cloudlet> migration = new ArrayList<Cloudlet>();
	
	public boolean test = true;
		
	public class MigInfo {
		int VmSender;
		int VmReciever;
		double ProgressPercentage;
		double VmEnergy;
		double Clock;
		
		
		
		public void VmSenderSet (int SoR){
			VmSender  = SoR;
		}
		public void VmRecieverSet (int SoR){
			VmReciever  = SoR;
		}
		public String toString() {
		return  "Clock: " + Clock+", VmSender: " + VmSender+", VmReciever: "+VmReciever+", ProgressPercentage: "+ProgressPercentage+", VmEnergy: "+VmEnergy;
		}
		}
	public List <MigInfo> MigrationInfo = new ArrayList<MigInfo>();
	
	public void AddToMigrationInfo (int VmSender,int VmReciever,double ProgressPercentage,double VmEnergy,double Clock){
		MigInfo temp = new  MigInfo();
		temp.VmSender = VmSender;
		temp.VmReciever = VmReciever;
		temp.ProgressPercentage = ProgressPercentage;
		temp.VmEnergy = VmEnergy;
		temp.Clock = Clock;
		MigrationInfo.add(temp);
	}
	
	
	
	/**
	 * Allocates a new PowerDatacenter object.
	 * 
	 * @param name the name to be associated with this entity (as required by Sim_entity class from
	 *            simjava package)
	 * @param characteristics an object of DatacenterCharacteristics
	 * @param storageList a LinkedList of storage elements, for data simulation
	 * @param vmAllocationPolicy the vmAllocationPolicy
	 * @throws Exception This happens when one of the following scenarios occur:
	 *             <ul>
	 *             <li>creating this entity before initializing CloudSim package
	 *             <li>this entity name is <tt>null</tt> or empty
	 *             <li>this entity has <tt>zero</tt> number of PEs (Processing Elements). <br>
	 *             No PEs mean the Cloudlets can't be processed. A CloudResource must contain one or
	 *             more Machines. A Machine must contain one or more PEs.
	 *             </ul>
	 * @pre name != null
	 * @pre resource != null
	 * @post $none
	 */
	public Datacenter(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			List<File> cl,
			double schedulingInterval) throws Exception {
		super(name);

		setCharacteristics(characteristics);
		setVmAllocationPolicy(vmAllocationPolicy);
		setLastProcessTime(0.0);
		setStorageList(storageList);
		setFileList(cl);
		setVmList(new ArrayList<Vm>());
		setSchedulingInterval(schedulingInterval);

		for (Host host : getCharacteristics().getHostList()) {
			host.setDatacenter(this);
		}

		// If this resource doesn't have any PEs then no useful at all
		if (getCharacteristics().getNumberOfPes() == 0) {
			throw new Exception(super.getName()
					+ " : Error - this entity has no PEs. Therefore, can't process any Cloudlets.");
		}

		// stores id of this class
		getCharacteristics().setId(super.getId());
	}

	/**
	 * Overrides this method when making a new and different type of resource. <br>
	 * <b>NOTE:</b> You do not need to override {@link #body()} method, if you use this method.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void registerOtherEntity() {
		// empty. This should be override by a child class
	}

	/**
	 * Processes events or services that are available for this PowerDatacenter.
	 * 
	 * @param ev a Sim_event object
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev, Datacenter Networkdatacenter) {
		int srcId = -1;

		switch (ev.getTag()) {
		// Resource characteristics inquiry
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				srcId = ((Integer) ev.getData()).intValue();
				sendNow(srcId, ev.getTag(), getCharacteristics());
				break;

			// Resource dynamic info inquiry
			case CloudSimTags.RESOURCE_DYNAMICS:
				srcId = ((Integer) ev.getData()).intValue();
				sendNow(srcId, ev.getTag(), 0);
				break;

			case CloudSimTags.RESOURCE_NUM_PE:
				srcId = ((Integer) ev.getData()).intValue();
				int numPE = getCharacteristics().getNumberOfPes();
				sendNow(srcId, ev.getTag(), numPE);
				break;

			case CloudSimTags.RESOURCE_NUM_FREE_PE:
				srcId = ((Integer) ev.getData()).intValue();
				int freePesNumber = getCharacteristics().getNumberOfFreePes();
				sendNow(srcId, ev.getTag(), freePesNumber);
				break;

			// New Cloudlet arrives
			case CloudSimTags.CLOUDLET_SUBMIT:
				processCloudletSubmit(ev, false);
				break;

			// New Cloudlet arrives, but the sender asks for an ack
			case CloudSimTags.CLOUDLET_SUBMIT_ACK:
				processCloudletSubmit(ev, true);
				break;

			// Cancels a previously submitted Cloudlet
			case CloudSimTags.CLOUDLET_CANCEL:
				processCloudlet(ev, CloudSimTags.CLOUDLET_CANCEL);
				break;

			// Pauses a previously submitted Cloudlet
			case CloudSimTags.CLOUDLET_PAUSE:
				processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE);
				break;

			// Pauses a previously submitted Cloudlet, but the sender
			// asks for an acknowledgement
			case CloudSimTags.CLOUDLET_PAUSE_ACK:
				processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE_ACK);
				break;

			// Resumes a previously submitted Cloudlet
			case CloudSimTags.CLOUDLET_RESUME:
				processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME);
				break;

			// Resumes a previously submitted Cloudlet, but the sender
			// asks for an acknowledgement
			case CloudSimTags.CLOUDLET_RESUME_ACK:
				processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME_ACK);
				break;

			// Moves a previously submitted Cloudlet to a different resource
			case CloudSimTags.CLOUDLET_MOVE:
				processCloudletMove((int[]) ev.getData(), CloudSimTags.CLOUDLET_MOVE);
				break;

			// Moves a previously submitted Cloudlet to a different resource
			case CloudSimTags.CLOUDLET_MOVE_ACK:
				processCloudletMove((int[]) ev.getData(), CloudSimTags.CLOUDLET_MOVE_ACK);
				break;

			// Checks the status of a Cloudlet
			case CloudSimTags.CLOUDLET_STATUS:
				processCloudletStatus(ev);
				break;

			// Ping packet
			case CloudSimTags.INFOPKT_SUBMIT:
				processPingRequest(ev);
				break;

			case CloudSimTags.VM_CREATE:
				processVmCreate(ev, false);
				break;

			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev, true);
				break;

			case CloudSimTags.VM_DESTROY:
				processVmDestroy(ev, false);
				break;

			case CloudSimTags.VM_DESTROY_ACK:
				processVmDestroy(ev, true);
				break;

			case CloudSimTags.VM_MIGRATE:
				processVmMigrate(ev, false);
				break;

			case CloudSimTags.VM_MIGRATE_ACK:
				processVmMigrate(ev, true);
				break;

			case CloudSimTags.VM_DATA_ADD:
				processDataAdd(ev, false);
				break;

			case CloudSimTags.VM_DATA_ADD_ACK:
				processDataAdd(ev, true);
				break;

			case CloudSimTags.VM_DATA_DEL:
				processDataDelete(ev, false);
				break;

			case CloudSimTags.VM_DATA_DEL_ACK:
				processDataDelete(ev, true);
				break;

			case CloudSimTags.VM_DATACENTER_EVENT:
				updateCloudletProcessing();
				checkCloudletCompletion();
				break;

			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	/**
	 * Process data del.
	 * 
	 * @param ev the ev
	 * @param ack the ack
	 */
	protected void processDataDelete(SimEvent ev, boolean ack) {
		if (ev == null) {
			return;
		}

		Object[] data = (Object[]) ev.getData();
		if (data == null) {
			return;
		}

		String filename = (String) data[0];
		int req_source = ((Integer) data[1]).intValue();
		int tag = -1;

		// check if this file can be deleted (do not delete is right now)
		int msg = deleteFileFromStorage(filename);
		if (msg == DataCloudTags.FILE_DELETE_SUCCESSFUL) {
			tag = DataCloudTags.CTLG_DELETE_MASTER;
		} else { // if an error occured, notify user
			tag = DataCloudTags.FILE_DELETE_MASTER_RESULT;
		}

		if (ack) {
			// send back to sender
			Object pack[] = new Object[2];
			pack[0] = filename;
			pack[1] = Integer.valueOf(msg);

			sendNow(req_source, tag, pack);
		}
	}

	/**
	 * Process data add.
	 * 
	 * @param ev the ev
	 * @param ack the ack
	 */
	protected void processDataAdd(SimEvent ev, boolean ack) {
		if (ev == null) {
			return;
		}

		Object[] pack = (Object[]) ev.getData();
		if (pack == null) {
			return;
		}

		File file = (File) pack[0]; // get the file
		file.setMasterCopy(true); // set the file into a master copy
		int sentFrom = ((Integer) pack[1]).intValue(); // get sender ID

		/******
		 * // DEBUG Log.printLine(super.get_name() + ".addMasterFile(): " + file.getName() +
		 * " from " + CloudSim.getEntityName(sentFrom));
		 *******/

		Object[] data = new Object[3];
		data[0] = file.getName();

		int msg = addFile(file); // add the file

		if (ack) {
			data[1] = Integer.valueOf(-1); // no sender id
			data[2] = Integer.valueOf(msg); // the result of adding a master file
			sendNow(sentFrom, DataCloudTags.FILE_ADD_MASTER_RESULT, data);
		}
	}

	/**
	 * Processes a ping request.
	 * 
	 * @param ev a Sim_event object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processPingRequest(SimEvent ev) {
		InfoPacket pkt = (InfoPacket) ev.getData();
		pkt.setTag(CloudSimTags.INFOPKT_RETURN);
		pkt.setDestId(pkt.getSrcId());

		// sends back to the sender
		sendNow(pkt.getSrcId(), CloudSimTags.INFOPKT_RETURN, pkt);
	}

	/**
	 * Process the event for an User/Broker who wants to know the status of a Cloudlet. This
	 * PowerDatacenter will then send the status back to the User/Broker.
	 * 
	 * @param ev a Sim_event object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processCloudletStatus(SimEvent ev) {
		int cloudletId = 0;
		int userId = 0;
		int vmId = 0;
		int status = -1;

		try {
			// if a sender using cloudletXXX() methods
			int data[] = (int[]) ev.getData();
			cloudletId = data[0];
			userId = data[1];
			vmId = data[2];

			status = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId).getCloudletScheduler()
					.getCloudletStatus(cloudletId);
		}

		// if a sender using normal send() methods
		catch (ClassCastException c) {
			try {
				Cloudlet cl = (Cloudlet) ev.getData();
				cloudletId = cl.getCloudletId();
				userId = cl.getUserId();

				status = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId)
						.getCloudletScheduler().getCloudletStatus(cloudletId);
			} catch (Exception e) {
				Log.printLine(getName() + ": Error in processing CloudSimTags.CLOUDLET_STATUS");
				Log.printLine(e.getMessage());
				return;
			}
		} catch (Exception e) {
			Log.printLine(getName() + ": Error in processing CloudSimTags.CLOUDLET_STATUS");
			Log.printLine(e.getMessage());
			return;
		}

		int[] array = new int[3];
		array[0] = getId();
		array[1] = cloudletId;
		array[2] = status;

		int tag = CloudSimTags.CLOUDLET_STATUS;
		sendNow(userId, tag, array);
	}

	/**
	 * Here all the method related to VM requests will be received and forwarded to the related
	 * method.
	 * 
	 * @param ev the received event
	 * @pre $none
	 * @post $none
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printLine(getName() + ".processOtherEvent(): Error - an event is null.");
		}
	}

	/**
	 * Process the event for an User/Broker who wants to create a VM in this PowerDatacenter. This
	 * PowerDatacenter will then send the status back to the User/Broker.
	 * 
	 * @param ev a Sim_event object
	 * @param ack the ack
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev, boolean ack) {
		Vm vm = (Vm) ev.getData();

		boolean result = getVmAllocationPolicy().allocateHostForVm(vm);

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();

			if (result) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			send(vm.getUserId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_CREATE_ACK, data);
		}

		if (result) {
			getVmList().add(vm);

			if (vm.isBeingInstantiated()) {
				vm.setBeingInstantiated(false);
			}

			vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler()
					.getAllocatedMipsForVm(vm), (Datacenter) this );
		}

	}

	/**
	 * Process the event for an User/Broker who wants to destroy a VM previously created in this
	 * PowerDatacenter. This PowerDatacenter may send, upon request, the status back to the
	 * User/Broker.
	 * 
	 * @param ev a Sim_event object
	 * @param ack the ack
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmDestroy(SimEvent ev, boolean ack) {
		Vm vm = (Vm) ev.getData();
		getVmAllocationPolicy().deallocateHostForVm(vm);

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();
			data[2] = CloudSimTags.TRUE;

			sendNow(vm.getUserId(), CloudSimTags.VM_DESTROY_ACK, data);
		}

		getVmList().remove(vm);
	}

	/**
	 * Process the event for an User/Broker who wants to migrate a VM. This PowerDatacenter will
	 * then send the status back to the User/Broker.
	 * 
	 * @param ev a Sim_event object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmMigrate(SimEvent ev, boolean ack) {
		Object tmp = ev.getData();
		if (!(tmp instanceof Map<?, ?>)) {
			throw new ClassCastException("The data object must be Map<String, Object>");
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> migrate = (HashMap<String, Object>) tmp;

		Vm vm = (Vm) migrate.get("vm");
		Host host = (Host) migrate.get("host");

		getVmAllocationPolicy().deallocateHostForVm(vm);
		host.removeMigratingInVm(vm);
		boolean result = getVmAllocationPolicy().allocateHostForVm(vm, host);
		if (!result) {
			Log.printLine("[Datacenter.processVmMigrate] VM allocation to the destination host failed");
			System.exit(0);
		}

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();

			if (result) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(ev.getSource(), CloudSimTags.VM_CREATE_ACK, data);
		}

		Log.formatLine(
				"%.2f: Migration of VM #%d to Host #%d is completed",
				CloudSim.clock(),
				vm.getId(),
				host.getId());
		vm.setInMigration(false);
	}

	/**
	 * Processes a Cloudlet based on the event type.
	 * 
	 * @param ev a Sim_event object
	 * @param type event type
	 * @pre ev != null
	 * @pre type > 0
	 * @post $none
	 */
	protected void processCloudlet(SimEvent ev, int type) {
		int cloudletId = 0;
		int userId = 0;
		int vmId = 0;

		try { // if the sender using cloudletXXX() methods
			int data[] = (int[]) ev.getData();
			cloudletId = data[0];
			userId = data[1];
			vmId = data[2];
		}

		// if the sender using normal send() methods
		catch (ClassCastException c) {
			try {
				Cloudlet cl = (Cloudlet) ev.getData();
				cloudletId = cl.getCloudletId();
				userId = cl.getUserId();
				vmId = cl.getVmId();
			} catch (Exception e) {
				Log.printLine(super.getName() + ": Error in processing Cloudlet");
				Log.printLine(e.getMessage());
				return;
			}
		} catch (Exception e) {
			Log.printLine(super.getName() + ": Error in processing a Cloudlet.");
			Log.printLine(e.getMessage());
			return;
		}

		// begins executing ....
		switch (type) {
			case CloudSimTags.CLOUDLET_CANCEL:
				processCloudletCancel(cloudletId, userId, vmId);
				break;

			case CloudSimTags.CLOUDLET_PAUSE:
				processCloudletPause(cloudletId, userId, vmId, false);
				break;

			case CloudSimTags.CLOUDLET_PAUSE_ACK:
				processCloudletPause(cloudletId, userId, vmId, true);
				break;

			case CloudSimTags.CLOUDLET_RESUME:
				processCloudletResume(cloudletId, userId, vmId, false);
				break;

			case CloudSimTags.CLOUDLET_RESUME_ACK:
				processCloudletResume(cloudletId, userId, vmId, true);
				break;
			default:
				break;
		}

	}

	/**
	 * Process the event for an User/Broker who wants to move a Cloudlet.
	 * 
	 * @param receivedData information about the migration
	 * @param type event tag
	 * @pre receivedData != null
	 * @pre type > 0
	 * @post $none
	 */
	protected void processCloudletMove(int[] receivedData, int type) {
		updateCloudletProcessing();

		int[] array = receivedData;
		int cloudletId = array[0];
		int userId = array[1];
		int vmId = array[2];
		int vmDestId = array[3];
		int destId = array[4];

		// get the cloudlet
		Cloudlet cl = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId)
				.getCloudletScheduler().cloudletCancel(cloudletId);

		boolean failed = false;
		if (cl == null) {// cloudlet doesn't exist
			failed = true;
		} else {
			// has the cloudlet already finished?
			if (cl.getCloudletStatus() == Cloudlet.SUCCESS) {// if yes, send it back to user
				int[] data = new int[3];
				data[0] = getId();
				data[1] = cloudletId;
				data[2] = 0;
				sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_SUBMIT_ACK, data);
				sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
			}

			// prepare cloudlet for migration
			cl.setVmId(vmDestId);

			// the cloudlet will migrate from one vm to another does the destination VM exist?
			if (destId == getId()) {
				Vm vm = getVmAllocationPolicy().getHost(vmDestId, userId).getVm(vmDestId,userId);
				if (vm == null) {
					failed = true;
				} else {
					// time to transfer the files
					double fileTransferTime = 0.0;//predictFileTransferTime(cl.getRequiredFiles(),cl, vm, this.IReplicaManagement, this );
					//cl.SetReadDelay((long) fileTransferTime);
					vm.getCloudletScheduler().cloudletSubmit(cl, fileTransferTime, this);
				}
			} else {// the cloudlet will migrate from one resource to another
				int tag = ((type == CloudSimTags.CLOUDLET_MOVE_ACK) ? CloudSimTags.CLOUDLET_SUBMIT_ACK
						: CloudSimTags.CLOUDLET_SUBMIT);
				sendNow(destId, tag, cl);
			}
		}

		if (type == CloudSimTags.CLOUDLET_MOVE_ACK) {// send ACK if requested
			int[] data = new int[3];
			data[0] = getId();
			data[1] = cloudletId;
			if (failed) {
				data[2] = 0;
			} else {
				data[2] = 1;
			}
			sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_SUBMIT_ACK, data);
		}
	}

	/**
	 * Processes a Cloudlet submission.
	 * 
	 * @param ev a SimEvent object
	 * @param ack an acknowledgement
	 * @pre ev != null
	 * @post $none
	 */
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		updateCloudletProcessing();

		try {
			// gets the Cloudlet object
			Cloudlet cl = (Cloudlet) ev.getData();

			// checks whether this Cloudlet has finished or not
			if (cl.isFinished()) {
				String name = CloudSim.getEntityName(cl.getUserId());
				Log.printLine(getName() + ": Warning - Cloudlet #" + cl.getCloudletId() + " owned by " + name
						+ " is already completed/finished.");
				Log.printLine("Therefore, it is not being executed again");
				Log.printLine();

				// NOTE: If a Cloudlet has finished, then it won't be processed.
				// So, if ack is required, this method sends back a result.
				// If ack is not required, this method don't send back a result.
				// Hence, this might cause CloudSim to be hanged since waiting
				// for this Cloudlet back.
				if (ack) {
					int[] data = new int[3];
					data[0] = getId();
					data[1] = cl.getCloudletId();
					data[2] = CloudSimTags.FALSE;

					// unique tag = operation tag
					int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
					sendNow(cl.getUserId(), tag, data);
				}

				sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);

				return;
			}

			// process this Cloudlet to this CloudResource
			cl.setResourceParameter(getId(), getCharacteristics().getCostPerSecond(), getCharacteristics()
					.getCostPerBw());

			int userId = cl.getUserId();
			int vmId = cl.getVmId();

			
			Host host = getVmAllocationPolicy().getHost(vmId, userId);
			Vm vm = host.getVm(vmId, userId);
			
			// time to transfer the files
			double fileTransferTime = 0.0; //predictFileTransferTime(cl.getRequiredFiles(),cl, vm, this.IReplicaManagement, this );
			//pressAnyKeyToContinue();
			
			CloudletScheduler scheduler = vm.getCloudletScheduler();
			double estimatedFinishTime = scheduler.cloudletSubmit(cl, fileTransferTime,this);

			// if this cloudlet is in the exec queue
			if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime)) {
				estimatedFinishTime += fileTransferTime;
				send(getId(), estimatedFinishTime, CloudSimTags.VM_DATACENTER_EVENT);
			}

			if (ack) {
				int[] data = new int[3];
				data[0] = getId();
				data[1] = cl.getCloudletId();
				data[2] = CloudSimTags.TRUE;

				// unique tag = operation tag
				int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
				sendNow(cl.getUserId(), tag, data);
			}
		} catch (ClassCastException c) {
			Log.printLine(getName() + ".processCloudletSubmit(): " + "ClassCastException error.");
			c.printStackTrace();
		} catch (Exception e) {
			Log.printLine(getName() + ".processCloudletSubmit(): " + "Exception error.");
			e.printStackTrace();
		}

		checkCloudletCompletion();
	}

	/*
	 * New read dela
	 */
	protected double predictFileTransferTime(List<String> requiredFiles,Cloudlet cl, Vm vm, ReplicaManagement IReplicaManagement2, Datacenter linkDC ) {
		cl.Readmaintain = false; // for maintenance
		//Log.printLine("Start of reading phase of CLID: "+ cl.getCloudletId());
		//IReplicaManagement2.ReplicaTest(IReplicaManagement2,linkDC);
		//Log.printLine("time is equal to: "+ CloudSim.clock()+" , cloudlet id: "+ cl.getCloudletId());
		//pressAnyKeyToContinue();
		//pressAnyKeyToContinue();
		IReplicaManagement2.Maintain2(linkDC, CloudSim.clock());
		
		//IReplicaManagement2.ReplicaTest(IReplicaManagement2,linkDC);
		//pressAnyKeyToContinue();
		//pressAnyKeyToContinue();
		double time = 0.0;
		double ttime =0.0;
		int vmidts = 0;
		int vmidtd =0;
		int vmids = 0;
		int vmidd = 0;
		int localreadt = 0;
		int localread = 0;
		int storageid = 0;
		Iterator<String> iter = requiredFiles.iterator(); // reading all files may be one cloudlet need more than one file
		while (iter.hasNext()) {
			int FID =999;
			String fileName = iter.next();
			//Log.printLine("CloudletID: "+cl.getCloudletId()+ " need to read filename:"+ fileName);
			ttime = 9999999999999999.9;
			localreadt = 0;
			boolean IsEnergyEnoughS = false;
			for (int i = 0; i < linkDC.getStorageList().size(); i++) {
				
				Storage tempStorage = linkDC.getStorageList().get(i);
				File tempFile = tempStorage.getFile(fileName);
				
				if (tempFile != null) {
					//Log.printLine("HostID: "+ tempStorage.getHostID()+
					//		" , VMID: "+linkDC.getHostList().get(tempStorage.getHostID()).getVmList().get(0).getId()
					//		+" , File name: "+ tempFile.getName()+" FID: "+tempFile.getFID());
					FID = tempFile.getFID();
					//finding fastest file for reading
					double temp = 0;
					// In this part we should check this is local read or global read
					if (tempStorage.getHostID() == vm.getHost().getId()){
						temp = (double)tempFile.getSize() / (double)tempStorage.getMaxTransferRate();
						vmidtd = vm.getId();
						vmidts = vm.getId();
						localreadt = 1;
						//Log.printLine("Local read");
						
					}else {
						int VmS;
						int VmD;
						VmS = linkDC.getHostList().get(tempStorage.getHostID()).getVmList().get(0).getId();// because each host has just one vm
						VmD = vm.getId();
						
						temp = (double)tempFile.getSize() / (double)linkDC.getVMBW()[VmS][VmD];
						vmidts = VmS;
						vmidtd = VmD;
						//Log.printLine("Global Read, " + "VmD: "+VmD+" ,VmS: "+VmS);
					}
					
					
						boolean IsEnergyEnough = true;
						double EnrgyPerTimeUnitd = 0;
						double EnrgyPerTimeUnits = 0;
						if(localreadt == 1){
							EnrgyPerTimeUnitd = linkDC.getStorageList().get(storageid).getEnergypernuit();
							EnrgyPerTimeUnits = 0;
							}else {
							EnrgyPerTimeUnitd = (linkDC.IVMInfo.TransEnergyUnit.get(cl.getCloudletId()).get(vmidtd));
							EnrgyPerTimeUnits = (linkDC.IVMInfo.TransEnergyUnit.get(cl.getCloudletId()).get(vmidts));
							}
						//Energy from Destination
						double battery = linkDC.getVmList().get(vmidtd).getbattery();
						battery = (double) battery - ((double) temp * (double)(EnrgyPerTimeUnitd));
						double EnergyS = ((double) temp * (double)(EnrgyPerTimeUnitd));
						if (battery < 0.0){
							IsEnergyEnough = false;
							Log.printLine("fail in Energy of Destination");
						}
						
						battery = linkDC.getVmList().get(vmidts).getbattery();
						battery = (double) battery - ((double) temp * (double)(EnrgyPerTimeUnits));// the time is same because it depend to Bw between both node
						double EnergyD = ((double) temp * (double)(EnrgyPerTimeUnits));

						if (battery < 0.0){
							IsEnergyEnough = false;
							Log.printLine("fail in Energy of source");

						} else {
							IsEnergyEnoughS = true;
						}
						
						if(IsEnergyEnough){
							// use  Vm for selecting
							//Log.printLine("Time needed for read: "+ temp);
							//double costtemp = ((EnergyS * EnergyD)/1000);
							double costtemp = (1/battery); // Maximun energy in source
							if (ttime > costtemp){
								ttime = temp;
								localread = localreadt;
								vmidd = vmidtd;
								vmids = vmidts;
								storageid =i;
								
							}
						
						}
					
					//time += tempFile.getSize() / tempStorage.getMaxTransferRate();
					//Log.printLine( "this is predict time transfer: " + ttime + " ,for file:" + fileName);
					//break;
			}else {
					//Log.printLine( "file:" + fileName +" didn't find !!!!");
				}
				
			}
			
			if(ttime ==9999999999999999.9){
				ttime =0.0;
				//Log.printLine("In read phase fail and couldn't find file");
				if(IsEnergyEnoughS == true){
					Log.printLine("File name: "+ fileName +" ,Fail in Energy of Destination and need to migrate ");
					//CloudSim.terminateSimulation();
					//pressAnyKeyToContinue();
					//pressAnyKeyToContinue();
				return -10;
				} else {
					Log.printLine("File name: "+ fileName +" ,Fail in source energy and terminated");
					CloudSim.terminateSimulation();
					pressAnyKeyToContinue();
					pressAnyKeyToContinue();
					return -1;}
				//pressAnyKeyToContinue();
				//pressAnyKeyToContinue();
				//break;
			}
			time += ttime;
			//Log.printLine( "Partial read time VmS:"+vmids+ " from VmD: "+vmidd+ " is "+ttime);
			
			
			// Deductin energy of read
			
			double EnrgyPerTimeUnitd = 0;
			double EnrgyPerTimeUnits = 0;
			if(localread == 1){
			EnrgyPerTimeUnitd = linkDC.getStorageList().get(storageid).getEnergypernuit();
			EnrgyPerTimeUnits = 0;
			}else {
				EnrgyPerTimeUnitd = (linkDC.IVMInfo.TransEnergyUnit.get(cl.getCloudletId()).get(vmidd));
				EnrgyPerTimeUnits = (linkDC.IVMInfo.TransEnergyUnit.get(cl.getCloudletId()).get(vmids));
			}
			
			//Log.printLine("Read energy deduction, Energy per unit: "+EnrgyPerTimeUnitd+ " Read delay is: "+ttime  ); // each read shall deduct sepreatly
			//Deduct from Destination
			double battery = linkDC.getVmList().get(vmidd).getbattery();
			battery = (double) battery - ((double) ttime * (double)(EnrgyPerTimeUnitd));
			cl.AddReadEnergy((long) ((double) ttime * (double) (EnrgyPerTimeUnitd)));
			linkDC.getVmList().get(vmidd).AddReadE(((double) ttime * (double) (EnrgyPerTimeUnitd)));
			Log.printLine("Read energy deduct from destination VMID: "+vmidd +" , Deducted Energy: "+((long) ((double) ttime * (double) (EnrgyPerTimeUnitd)))
					+ " Remining energy : "+battery);
			linkDC.getVmList().get(vmidd).setbattery((long)(battery));
			
			
			// deduct from Source
			battery = linkDC.getVmList().get(vmids).getbattery();
			battery = (double) battery - ((double) ttime * (double)(EnrgyPerTimeUnits));// the time is same because it depend to Bw between both node
			linkDC.getVmList().get(vmids).AddReadE(((double) ttime * (double)(EnrgyPerTimeUnits)));
			
			Log.printLine("Read energy deduct from source VMID: "+vmids+" , Deducted Energy: "+ ((long) ((double) ttime * (double) (EnrgyPerTimeUnits)))
					+ " Remining energy : "+battery + " Energy per Unit: "+EnrgyPerTimeUnits);
			linkDC.getVmList().get(vmids).setbattery((long)(battery));
			// for source we didn't deducted 
			long STT = linkDC.IVMInfo.Energy.get(vmids) - (long) ((double) ttime * (double) (EnrgyPerTimeUnits));
			linkDC.IVMInfo.Energy.set(vmids, (int) STT) ;
			
			//updating VmReadList
			int VmreadlistID = linkDC.IReplicaManagement.FindFIDsID(FID, vmidd);
			//Log.printLine("Vmreadlist update on: "+ FID+" , "+ vmidd+
			///		"No of read needed:"+linkDC.IReplicaManagement.VmReadList.get(VmreadlistID).NoReadNeeded +
			//		" NoReadIsdone: "+linkDC.IReplicaManagement.VmReadList.get(VmreadlistID).NoReadIsDone);
			if(!(VmreadlistID==9999)){ // because of migration
				linkDC.IReplicaManagement.VmReadList.get(VmreadlistID).NoReadIsDone++;

			}
			//Log.printLine("After update NoReadIsdone: "+linkDC.IReplicaManagement.VmReadList.get(VmreadlistID).NoReadIsDone );
			//pressAnyKeyToContinue();
			//pressAnyKeyToContinue();
			//add NoA for replication managment
			int NoAtemp = IReplicaManagement2.NoA.get(FID);
			NoAtemp++;
			IReplicaManagement2.NoA.set(FID, NoAtemp);
			//IReplicaManagement2.ReplicaTest(IReplicaManagement2);

		}
		
		
		//IReplicaManagement2.Maintain(NetDatacenterBroker.linkDC, CloudSim.clock());
		//Log.printLine("End of reading phase");
		//Log.printLine( "Total read time: " +time);
		
		//IReplicaManagement2.ReplicaTest(IReplicaManagement2);
		
		
		cl.SetReadDelay(time);//time
		//Log.printLine("fileTransferTime submition: "+ cl.getReadDelay());
		//pressAnyKeyToContinue();
		//pressAnyKeyToContinue();

		return time;
	}

	
	
	
	
	
	
	
	
	
	/** Changed By Reza, because we will create replication, when cloudlet need to read file, simulator shall try to read fastest one
	 * In this regards there are two point, local read or global, their calculation is different 
	 * Predict file transfer time.
	 * 
	 * @param requiredFiles the required files
	 * @param vm 
	 * @return the double
	 */
	protected double predictFileTransferTime1(List<String> requiredFiles,Cloudlet cl, Vm vm) {
		
		
		double time = 0.0;
		double ttime =0.0;
		int vmidts = 0;
		int vmidtd =0;
		int vmids = 0;
		int vmidd = 0;
		int localreadt = 0;
		int localread = 0;
		int storageid = 0;
		Iterator<String> iter = requiredFiles.iterator(); // reading all files may be one cloudlet need more than one file
		while (iter.hasNext()) {
			String fileName = iter.next();
			ttime = 9999999.9;
			for (int i = 0; i < getStorageList().size(); i++) {
				
				Storage tempStorage = getStorageList().get(i);
				File tempFile = tempStorage.getFile(fileName);
				if (tempFile != null) {
					//finding fastest file for reading
					double temp = 0;
					// In this part we should check this is local read or global read
					if (tempStorage.getHostID() == vm.getHost().getId()){
						temp = (double)tempFile.getSize() / (double)tempStorage.getMaxTransferRate();
						vmidtd = vm.getId();
						localreadt = 1;
						//Log.printLine("Local read");
						
					}else {
						int VmS;
						int VmD;
						VmD = this.getHostList().get(tempStorage.getHostID()).getVmList().get(0).getId();
						VmS = vm.getId();
						
						temp = (double)tempFile.getSize() / (double)VMBW[VmS][VmD];
						vmidtd = this.getHostList().get(tempStorage.getHostID()).getVmList().get(0).getId();
						vmidts = VmS;
						//Log.printLine("Global Read");
					}
					
					if (ttime > temp){
						ttime = temp;
						localread = localreadt;
						vmidd = vmidtd;
						vmids = vmidts;
						storageid =i;
					}
					//time += tempFile.getSize() / tempStorage.getMaxTransferRate();
					//Log.printLine( "this is predict time transfer: " + ttime + " ,for file:" + fileName);
					//break;
				}else {
					//Log.printLine( "file:" + fileName +" didn't find !!!!");
				}
				
			}
			if(ttime ==9999999.9){
				ttime =0.0;
			}
			time += ttime;
			//Log.printLine( "Partial read time: " +time);
			/*
			// Deductin energy of read
			
			double EnrgyPerTimeUnitd = 0;
			double EnrgyPerTimeUnits = 0;
			if(localread == 1){
			EnrgyPerTimeUnitd = getStorageList().get(storageid).getEnergypernuit();
			EnrgyPerTimeUnits = 0;
			}else {
			EnrgyPerTimeUnitd = (NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(vmidd));
			EnrgyPerTimeUnits = (NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(vmids));
			}
			
			//Log.printLine("Energy per unit: "+EnrgyPerTimeUnitd+ " Read delay is: "+ttime  ); // each read shall deduct sepreatly
			//Deduct from Destination
			double battery = NetDatacenterBroker.linkDC.getVmList().get(vmidd).getbattery();
			battery = (double) battery - ((double) ttime * (double)(EnrgyPerTimeUnitd));
			
			cl.SetReadEnergy((long) ((double) ttime * (double) (EnrgyPerTimeUnitd)));
			NetDatacenterBroker.linkDC.getVmList().get(vmidd).setbattery((long)(battery));
			
			// deduct from Source
			battery = NetDatacenterBroker.linkDC.getVmList().get(vmids).getbattery();
			battery = (double) battery - ((double) ttime * (double)(EnrgyPerTimeUnits));// the time is same because it depend to Bw between both node
			
			cl.SetReadEnergy((long) ((double) ttime * (double) (EnrgyPerTimeUnits)));
			NetDatacenterBroker.linkDC.getVmList().get(vmids).setbattery((long)(battery));
			*/
		}
		Log.printLine( "Total read time: " +time);
		return time;
	}

	// changed by reza
	protected void CloudletSubmit(Cloudlet cl, boolean ack) {
		

		try {
			
			

			// process this Cloudlet to this CloudResource
			cl.setResourceParameter(getId(), getCharacteristics().getCostPerSecond(), getCharacteristics()
					.getCostPerBw());

			int userId = cl.getUserId();
			int vmId = cl.getVmId();

			
			Host host = getVmAllocationPolicy().getHost(vmId, userId);
			Vm vm = host.getVm(vmId, userId);
			
			// time to transfer the files
			double fileTransferTime = 0.0;//predictFileTransferTime(cl.getRequiredFiles(),cl, vm, this.IReplicaManagement, this );
			
			CloudletScheduler scheduler = vm.getCloudletScheduler();
			double estimatedFinishTime = scheduler.cloudletSubmit(cl, fileTransferTime,this);

			// if this cloudlet is in the exec queue
			if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime)) {
				estimatedFinishTime += fileTransferTime;
				send(getId(), estimatedFinishTime, CloudSimTags.VM_DATACENTER_EVENT);
			}

			
		} catch (ClassCastException c) {
			Log.printLine(getName() + ".processCloudletSubmit(): " + "ClassCastException error.");
			c.printStackTrace();
		} catch (Exception e) {
			Log.printLine(getName() + ".processCloudletSubmit(): " + "Exception error.");
			e.printStackTrace();
		}

		checkCloudletCompletion();
	}

	
	/**
	 * Processes a Cloudlet resume request.
	 * 
	 * @param cloudletId resuming cloudlet ID
	 * @param userId ID of the cloudlet's owner
	 * @param ack $true if an ack is requested after operation
	 * @param vmId the vm id
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletResume(int cloudletId, int userId, int vmId, boolean ack) {
		double eventTime = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId)
				.getCloudletScheduler().cloudletResume(cloudletId);

		boolean status = false;
		if (eventTime > 0.0) { // if this cloudlet is in the exec queue
			status = true;
			if (eventTime > CloudSim.clock()) {
				schedule(getId(), eventTime, CloudSimTags.VM_DATACENTER_EVENT);
			}
		}

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = cloudletId;
			if (status) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(userId, CloudSimTags.CLOUDLET_RESUME_ACK, data);
		}
	}

	/**
	 * Processes a Cloudlet pause request.
	 * 
	 * @param cloudletId resuming cloudlet ID
	 * @param userId ID of the cloudlet's owner
	 * @param ack $true if an ack is requested after operation
	 * @param vmId the vm id
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletPause(int cloudletId, int userId, int vmId, boolean ack) {
		boolean status = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId)
				.getCloudletScheduler().cloudletPause(cloudletId);

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = cloudletId;
			if (status) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(userId, CloudSimTags.CLOUDLET_PAUSE_ACK, data);
		}
	}

	/**
	 * Processes a Cloudlet cancel request.
	 * 
	 * @param cloudletId resuming cloudlet ID
	 * @param userId ID of the cloudlet's owner
	 * @param vmId the vm id
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletCancel(int cloudletId, int userId, int vmId) {
		Cloudlet cl = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId)
				.getCloudletScheduler().cloudletCancel(cloudletId);
		sendNow(userId, CloudSimTags.CLOUDLET_CANCEL, cl);
	}

	/**
	 * Updates processing of each cloudlet running in this PowerDatacenter. It is necessary because
	 * Hosts and VirtualMachines are simple objects, not entities. So, they don't receive events and
	 * updating cloudlets inside them must be called from the outside.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void updateCloudletProcessing() {
		// if some time passed since last processing
		// R: for term is to allow loop at simulation start. Otherwise, one initial
		// simulation step is skipped and schedulers are not properly initialized
		if (CloudSim.clock() < 0.111 || CloudSim.clock() > getLastProcessTime() + CloudSim.getMinTimeBetweenEvents()) {
			List<? extends Host> list = getVmAllocationPolicy().getHostList();
			double smallerTime = Double.MAX_VALUE;
			if(!migration.isEmpty()){
				
				//Log.printLine("There is migration");
				for(int i =0; i < migration.size();i++){
					Cloudlet cl =  migration.get(i);
					Vm vm1 = this.getVmList().get(cl.getVmId());
					CloudletScheduler scheduler = vm1.getCloudletScheduler();
					//Log.printLine("Source VmID: "+vm1.getId()+ "before migration of cloudlet ID"+cl.getCloudletId() );
					//scheduler.cloudletListPrint(vm1.getId());
					scheduler.migrateCloudlet(cl.getCloudletId());
					//Log.printLine("Source VmID: "+vm1.getId()+ "after migration of cloudlet ID"+cl.getCloudletId() );
					//scheduler.cloudletListPrint(vm1.getId());
					// increase vm migration of Vm in source
					vm1.finish++;
					
					//reset arrival time of cloudlet
					cl.setCloudletFinishedSoFar(0);
					cl.setExecStartTime(CloudSim.clock());
					cl.Readmaintain = false;
					// adding to new one
					int VmID2 = findingreplacVM(cl);
					if(!(VmID2==-2)){
					Vm vm2 = this.getVmList().get(VmID2);
					CloudletScheduler scheduler1 = vm2.getCloudletScheduler();
					cl.PreVMID = cl.getVmId();
					cl.migrateIsDone = true;
					cl.setVmId(vm2.getId());
					
					Log.printLine("Destination VmID: "+vm2.getId()+" before migration of cloudletID: "+cl.getCloudletId());
					scheduler1.cloudletListPrint(vm2.getId());
					scheduler1.cloudletSubmit(cl, 0.0, this);
					Log.printLine("Destination VmID: "+cl.getVmId()+" after migration of cloudletID: "+cl.getCloudletId());
					//scheduler1.cloudletListPrint(vm2.getId());
					
					
					// add to archive
					this.AddToMigrationInfo(vm1.getId(), vm2.getId(), 0, vm1.getbattery(), CloudSim.clock());
					}
					//pressAnyKeyToContinue();
					//pressAnyKeyToContinue();
					
					
					/*
					Log.printLine();
					Log.printLine();
					// migration of waiting list
					for(int j =0; j< scheduler.RgetcloudletWaitingList().size();j++){
						ResCloudlet rcl =  scheduler.RgetcloudletWaitingList().get(j);
						
						
						scheduler.migrateCloudlet(rcl.getCloudletId());
					
						//reset arrival time of cloudlet
						rcl.getCloudlet().setCloudletFinishedSoFar(0);
						rcl.getCloudlet().setExecStartTime(CloudSim.clock());
						rcl.getCloudlet().migrated = true;
						rcl.getCloudlet().migrateIsDone = false;
						rcl.getCloudlet().Readmaintain = false;
						// adding to new one
						VmID2 = findingreplacVM(rcl.getCloudlet());
						vm2 = this.getVmList().get(VmID2);
						CloudletScheduler scheduler11 = vm2.getCloudletScheduler();
						rcl.getCloudlet().PreVMID = cl.getVmId();
						rcl.getCloudlet().migrateIsDone = true;
						rcl.getCloudlet().setVmId(vm2.getId());
						//Log.printLine("Destination VmID: "+vm2.getId()+"waitinglist  before migration of cloudletID: "+rcl.getCloudletId());
						//scheduler1.cloudletListPrint(vm2.getId());
						scheduler11.cloudletSubmit(rcl.getCloudlet(), 0.0, this);
						//Log.printLine("Destination VmID: "+vm2.getId()+"waitinglist after migration of cloudletID: "+rcl.getCloudletId());
						//scheduler1.cloudletListPrint(vm2.getId());
						this.AddToMigrationInfo(vm1.getId(), vm2.getId(), 0, vm1.getbattery(), CloudSim.clock());
					}
					scheduler.clearcloudletWaitingList();
					//Log.printLine("Source VmID: "+vm1.getId()+ "before migration of waiting " );
					//scheduler.cloudletListPrint(vm1.getId());
					//pressAnyKeyToContinue();
					//pressAnyKeyToContinue();
					*/
					
				}
				
				//pressAnyKeyToContinue();
				//pressAnyKeyToContinue();
				
			}
			migration.clear();
			// for each host...
			for (int i = 0; i < list.size(); i++) {
				Host host = list.get(i);
				// inform VMs to update processing
				double time = host.updateVmsProcessing(CloudSim.clock(), this);
				// what time do we expect that the next cloudlet will finish?
				if (time < smallerTime) {
					smallerTime = time;
				}
			}
			// gurantees a minimal interval before scheduling the event
			if (smallerTime < CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01) {
				smallerTime = CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01;
			}
			
						
			if (smallerTime != Double.MAX_VALUE) {
				schedule(getId(), (smallerTime - CloudSim.clock()), CloudSimTags.VM_DATACENTER_EVENT);
			}
			
			setLastProcessTime(CloudSim.clock());
		}
	}
	public Vm findVmWithID( List <Vm> ListofVM, int VmId){
		Vm VMtemp;
		for (int i =0; i < ListofVM.size();i++){
			if(ListofVM.get(i).getId() == VmId){
				VMtemp = ListofVM.get(i);
				return VMtemp;
			}
		}
		return null;
	}
	private int findingreplacVM(Cloudlet cll) {
		int VMSelected = -1 ;
		
		for(int i=0; i < this.vmList.size();i++){
			double SimVmEnergy = this.vmList.get(i).getbattery();
			double SchedulingVmEnergy = this.IVMInfo.Energy.get(i);
			double ExeE = this.IVMtoCloudletEnergy.VMtoCloudletEnergy.get(cll.getCloudletId()).get(i);
			double VmEnergy =SimVmEnergy;
			if(SimVmEnergy>SchedulingVmEnergy){
				VmEnergy = SchedulingVmEnergy;
			}
			
			double CostTemp = ExeE; // it mean, selected one should have higher than ExeE and if there are more highest one
			if((VmEnergy > CostTemp)&&(!(cll.PreVMID==i))){
				CostTemp = VmEnergy; 
				VMSelected = i ;
				
			}
			
			
		}
		
		if(VMSelected == -1){
			// First phase, we should check is it enough energy to run application
			double VmETotal = 0.0;
			for(int i=0; i < this.vmList.size();i++){
				double SimVmEnergy = this.vmList.get(i).getbattery();
				double SchedulingVmEnergy = this.IVMInfo.Energy.get(i);
				VmETotal =VmETotal+ SimVmEnergy ; //Math.min(SimVmEnergy, SchedulingVmEnergy)
				}
			// if in total there is enough energy call rescheduling procedure
			double AveExeE = 0.0;
			for(int i=0; i < this.vmList.size();i++){
				AveExeE =+ this.IVMtoCloudletEnergy.VMtoCloudletEnergy.get(cll.getCloudletId()).get(i); 
				}
			AveExeE = AveExeE/this.vmList.size();
			
			if(VmETotal> AveExeE){
				// here we should call rescheduling function
				Log.printLine("Scheduling fail and need to be reschedule");
				pressAnyKeyToContinue();
				pressAnyKeyToContinue();
				if(Rescheduling(cll)== -1){
					Log.printLine("Rescheduling failed to find replacement, termination");
					CloudSim.terminateSimulation();
					pressAnyKeyToContinue();
					pressAnyKeyToContinue();
				} else {
					VMSelected = -2;
				}
				
			} else {
				Log.printLine("There is no enough energy for rescheduling");
				pressAnyKeyToContinue();
				pressAnyKeyToContinue();
			}

				
			}
			if((!(VMSelected == -2))&&(!(VMSelected == -1))){
				// deduct energy from scheduling energy
				double SchedulingVmEnergy = this.IVMInfo.Energy.get(VMSelected);
				double ExecE = this.IVMtoCloudletEnergy.VMtoCloudletEnergy.get(cll.getCloudletId()).get(VMSelected); 
				SchedulingVmEnergy = SchedulingVmEnergy - ExecE;
				this.IVMInfo.Energy.set(VMSelected, (int) SchedulingVmEnergy);
			}
			
	
		
			
		return VMSelected;
		
	}
	// Rescheduling procedure
	private int Rescheduling(Cloudlet clcasuerescheduling) {
		//phase 1: delete from waiting add to new list
		ArrayList <Integer> CLIDreplacmentList = new ArrayList<Integer>();
		CLIDreplacmentList.add(clcasuerescheduling.getCloudletId()); // the only cl in execution phase should rescedule is cl casue rescheduling
		for(int i=0; i < this.vmList.size();i++){
			Vm vm1 = this.getVmList().get(i);
			CloudletScheduler scheduler = vm1.getCloudletScheduler();
			for(int j=0;j<scheduler.RgetcloudletWaitingList().size();j++){
				CLIDreplacmentList.add(scheduler.RgetcloudletWaitingList().get(j).getCloudletId());
			//	Log.printLine("needed to reschedule CloudletID: "+scheduler.RgetcloudletWaitingList().get(j).getCloudletId() +
				//		" CID: "+ CLIDreplacmentList.get(CLIDreplacmentList.size()-1) );
			}
			scheduler.clearcloudletWaitingList();
		}
		
		// checking
		/*
		Log.printLine();
		Log.print(" All CIDs need reschedule: ");
		for(int i=0; i< CLIDreplacmentList.size();i++){
			Log.print( CLIDreplacmentList.get(i)+" , ");
		}
		//pressAnyKeyToContinue();
		//pressAnyKeyToContinue();
		*/
		//phase 2: update VmInfo Energy by sim battery
		for(int i=0; i < this.vmList.size();i++){
			this.IVMInfo.Energy.set(i, (int) this.getVmList().get(i).getbattery());
		}
		/*
		//checking
		Log.printLine();
		Log.print(" New Vm Energes: ");
		for(int i=0; i< this.vmList.size();i++){
			Log.print(this.IVMInfo.Energy.get(i)+ " , ");
		}
		//pressAnyKeyToContinue();
		//pressAnyKeyToContinue();
		 
		 */
		//phase 3: deduct remaining ExeE for those which have cl in execution
		for(int i=0; i < this.vmList.size();i++){
			Vm vm1 = this.getVmList().get(i);
			CloudletScheduler scheduler = vm1.getCloudletScheduler();
			for(int j=0;j<scheduler.RsgetCloudletExecList().size();j++){
				Cloudlet cl = scheduler.RsgetCloudletExecList().get(j).getCloudlet();
				double NeededExeE = scheduler.RsgetCloudletExecList().get(j).getRemainingCloudletLength(this.getVmList().get(i).getMips())
						* this.IVMInfo.ExecEnergyUnit.get(cl.getCloudletId()).get(i);
				double VmE = this.IVMInfo.Energy.get(i);
				VmE = VmE - NeededExeE;
				this.IVMInfo.Energy.set(i, (int) VmE);
			}
		}
		
		//checking
		/*
		Log.printLine();
		Log.print(" New Vm Energes after deductin Energy of executing energys: ");
		for(int i=0; i< this.vmList.size();i++){
			Log.print(this.IVMInfo.Energy.get(i)+ " , ");
		}
		pressAnyKeyToContinue();
		pressAnyKeyToContinue();
		*/
		// forth phase: clean ExecutionTable & VmTable
		//cleaning ExecutionTable
		this.IExecutiontable.CandidateCloudletBVm.clear();
		this.IExecutiontable.CandidateVmBC.clear();
		this.IExecutiontable.Deadline.clear();
		this.IExecutiontable.ExecutionTime.clear();
		this.IExecutiontable.FinishTime.clear();
		this.IExecutiontable.OneVmCandidate.clear();
		this.IExecutiontable.SelVm.clear();
		this.IExecutiontable.StartTime.clear();
		this.IExecutiontable.WorstCase.clear();
		//cleaning VmTable
		this.IVMsTimeExecution.VMsSchedule2.clear();
		
		//clean Vmreadlist
		this.IReplicaManagement.VmReadList.clear();
				
		//Initiating
		this.IExecutiontable.NPintial(cloudletList.size());
		this.IVMsTimeExecution.intial(this.NumberOfVms);
		this.IExecutiontable.Intial(vmList.size(), cloudletList.size(),  this.IVMtoCloudletTime);
		//pressAnyKeyToContinue();
		//pressAnyKeyToContinue();
		// fifth phase: rescheduling
		
		
		
		Costfun5678Scheduling(cloudletList,CLIDreplacmentList, this.IExecutiontable, 
				this.IVMtoCloudletTime, this.IVMtoCloudletEnergy, this.IVMsTimeExecution, this.IVMInfo, vmList);
		/*
		for(int i=0;i < getCloudletSubmittedList().size();i++){
    		for(int j=0; j< getCloudletSubmittedList().get(i).getRequiredFiles().size();j++){
    			datacenter.IReplicaManagement.AddVmToVmReadList(getCloudletSubmittedList().get(i).getRequiredFiles().get(j),getCloudletSubmittedList().get(i).getVmId());
    			//Log.printLine("CloudletID: "+getCloudletSubmittedList().get(i).getCloudletId()+" on VMID: "+getCloudletSubmittedList().get(i).getVmId() + " Need to read File: "+ getCloudletSubmittedList().get(i).getRequiredFiles().get(j));
    		}
    	}
		*/
		//pressAnyKeyToContinue();
		//pressAnyKeyToContinue();
		
		// sixth submit again
		for(int i=0; i < CLIDreplacmentList.size();i++){
			Cloudlet cl = cloudletList.get(FindIndexbyID(cloudletList,CLIDreplacmentList.get(i)));  
			
			//reset arrival time of cloudlet
			//reset arrival time of cloudlet
			cl.setCloudletFinishedSoFar(0);
			cl.setExecStartTime(CloudSim.clock());
			cl.migrated = true;
			cl.Readmaintain = false;
			// adding to new one
			cl.SetReadDelay(0.0);
			cl.setExecutionEnergy(0.0);
			cl.PreVMID = cl.getVmId();
			cl.migrateIsDone = true;
			cl.setCloudletLength(cl.OrginalLength); // return to orginal length for assignment
			// adding to new one
			int VmID = cl.getVmId();
			Vm vm2 = this.getVmList().get(VmID);
			cl.setVmId(vm2.getId());
			CloudletScheduler scheduler1 = vm2.getCloudletScheduler();
			if(scheduler1.RsgetCloudletExecList().isEmpty()){
				vm2.finish++;
			}
			//Log.printLine("Destination VmID: "+vm2.getId()+" before submition of rescheduling of cloudletID: "+cl.getCloudletId());
			//scheduler1.cloudletListPrint(vm2.getId());
			scheduler1.cloudletSubmit(cl, 0.0, this);
			//Log.printLine("Destination VmID: "+cl.getVmId()+" after submition of rescheduling of cloudletID: "+cl.getCloudletId());
			//scheduler1.cloudletListPrint(vm2.getId());
		}
		//pressAnyKeyToContinue();
		//pressAnyKeyToContinue();
		
		return -2;
	}
	
	public int FindIndexbyID(List<Cloudlet> cloudlet,int CID){
		for(int i =0; i < cloudlet.size(); i++){
			if (cloudlet.get(i).getCloudletId() == CID){
				return i;
			}
		}
		
		// if we can't find there is peoblem
		Log.printLine(" Fail becasue, CID couldn't find in cloudletlist and cause termination");
		CloudSim.terminateSimulation();
		pressAnyKeyToContinue();
		pressAnyKeyToContinue();
		
		return -1 ;
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void Costfun5678Scheduling (List<Cloudlet> cloudlet,ArrayList<Integer> cLIDreplacmentList, org.cloudbus.cloudsim.Datacenter.Executiontable IExecutiontable,
org.cloudbus.cloudsim.Datacenter.VMtoCloudletTime IVMtoCloudletTime,
org.cloudbus.cloudsim.Datacenter.VMtoCloudletEnergy iVMtoCloudletEnergy
,VMsTimeExecution IVMsTimeExecution, VMInfo IVMInfo, List<? extends Vm> vmList2){
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("-------------------------------------------        schaduleing         -------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");
Log.printLine("------------------------------------------------------------------------------------------------------------------------");



//This for loop of assignment of phase 5, based on rank up decreasment

double executionT = 0.0;
double StartT = 0.0;
double FinishT = 0.0; 
int VMID = 0;

//Assign all cloudlet based on rankup which sorted
for (int q =0; q < (cLIDreplacmentList.size()); q++ ){

	int x = FindIndexbyID(cloudlet,cLIDreplacmentList.get(q));
	
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

//CostF4=> VmEnergy-ExecutionEnergy
//CostF5=> (((EFT*ExecutionEnergy)/(VmEnergy-ExecutionEnergy)))
//CostF6=> (((EFT*ExecutionEnergy)*(VmEnergy-ExecutionEnergy))/1000000)
//CostF7=> ((EFT*ExecutionEnergy)*((VmEnergy-ExecutionEnergy)/VmEnergy))
//CostF8=> ((EFT*ExecutionEnergy)*(1-((VmEnergy-ExecutionEnergy)/VmEnergy)))
//CostF9=> ExecutionEnergy
double CostFun =VmEnergy-ExecutionEnergy;

//Log.printLine("EFT: "+EFT +" ExeE: "+ExecutionEnergy+" ReminingE: "+(VmEnergy-ExecutionEnergy)+" Costfun: "+ CostFun);
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
cloudlet.get(cloudletID).setVmId(VMID);
cloudlet.get(cloudletID).assigned = true;

//Updating VMexecutionTable
executionT = IVMtoCloudletTime.VMtoCloudletTime.get(cloudletID).get(VMID);
StartT = IExecutiontable.EST(cloudletID,VMID , StartT, executionT, IVMsTimeExecution, IVMtoCloudletTime);
FinishT = IExecutiontable.EFT(cloudletID,VMID , StartT, executionT, IVMsTimeExecution, IVMtoCloudletTime);

IExecutiontable.StartTime.set(cloudletID, StartT);
IExecutiontable.FinishTime.set(cloudletID, FinishT);
IExecutiontable.ExecutionTime.set(cloudletID, executionT);
IExecutiontable.SelVm.set(cloudletID, VMID);
Log.printLine("CLID: "+ cloudletID + " VMID: "+ VMID);

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

Log.printLine();
////Log.printLine("VM Energy:");
//Log.printLine(IVMInfo.Energy);


//Log.printLine();
Log.printLine("VMsSchedule2 is equal to:");
Log.printLine(IVMsTimeExecution.VMsSchedule2);
//Log.printLine();
*/


}
}

	
	
	
	
	
	
	
	/**
	 * Verifies if some cloudlet inside this PowerDatacenter already finished. If yes, send it to
	 * the User/Broker
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void checkCloudletCompletion() {
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
		for (int i = 0; i < list.size(); i++) {
			Host host = list.get(i);
			for (Vm vm : host.getVmList()) {
				while (vm.getCloudletScheduler().isFinishedCloudlets()) {
					Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
					if (cl != null) {
						sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
					}
				}
			}
		}
	}

	/**
	 * Adds a file into the resource's storage before the experiment starts. If the file is a master
	 * file, then it will be registered to the RC when the experiment begins.
	 * 
	 * @param file a DataCloud file
	 * @return a tag number denoting whether this operation is a success or not
	 */
	public int addFile(File file) {
		if (file == null) {
			return DataCloudTags.FILE_ADD_ERROR_EMPTY;
		}

		if (contains(file.getName())) {
			return DataCloudTags.FILE_ADD_ERROR_EXIST_READ_ONLY;
		}

		// check storage space first
		if (getStorageList().size() <= 0) {
			return DataCloudTags.FILE_ADD_ERROR_STORAGE_FULL;
		}

		Storage tempStorage = null;
		int msg = DataCloudTags.FILE_ADD_ERROR_STORAGE_FULL;

		for (int i = 0; i < getStorageList().size(); i++) {
			tempStorage = getStorageList().get(i);
			if (tempStorage.getAvailableSpace() >= file.getSize()) {
				tempStorage.addFile(file);
				msg = DataCloudTags.FILE_ADD_SUCCESSFUL;
				break;
			}
		}

		return msg;
	}

	/**
	 * Checks whether the resource has the given file.
	 * 
	 * @param file a file to be searched
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	protected boolean contains(File file) {
		if (file == null) {
			return false;
		}
		return contains(file.getName());
	}

	/**
	 * Checks whether the resource has the given file.
	 * 
	 * @param fileName a file name to be searched
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	protected boolean contains(String fileName) {
		if (fileName == null || fileName.length() == 0) {
			return false;
		}

		Iterator<Storage> it = getStorageList().iterator();
		Storage storage = null;
		boolean result = false;

		while (it.hasNext()) {
			storage = it.next();
			if (storage.contains(fileName)) {
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * Deletes the file from the storage. Also, check whether it is possible to delete the file from
	 * the storage.
	 * 
	 * @param fileName the name of the file to be deleted
	 * @return the error message
	 */
	private int deleteFileFromStorage(String fileName) {
		Storage tempStorage = null;
		File tempFile = null;
		int msg = DataCloudTags.FILE_DELETE_ERROR;

		for (int i = 0; i < getStorageList().size(); i++) {
			tempStorage = getStorageList().get(i);
			tempFile = tempStorage.getFile(fileName);
			tempStorage.deleteFile(fileName, tempFile);
			msg = DataCloudTags.FILE_DELETE_SUCCESSFUL;
		} // end for

		return msg;
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
		// this resource should register to regional GIS.
		// However, if not specified, then register to system GIS (the
		// default CloudInformationService) entity.
		int gisID = CloudSim.getEntityId(regionalCisName);
		if (gisID == -1) {
			gisID = CloudSim.getCloudInfoServiceEntityId();
		}

		// send the registration to GIS
		sendNow(gisID, CloudSimTags.REGISTER_RESOURCE, getId());
		// Below method is for a child class to override
		registerOtherEntity();
	}

	/**
	 * Gets the host list.
	 * 
	 * @return the host list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Host> List<T> getHostList() {
		return (List<T>) getCharacteristics().getHostList();
	}

	/**
	 * Gets the characteristics.
	 * 
	 * @return the characteristics
	 */
	protected DatacenterCharacteristics getCharacteristics() {
		return characteristics;
	}

	/**
	 * Sets the characteristics.
	 * 
	 * @param characteristics the new characteristics
	 */
	protected void setCharacteristics(DatacenterCharacteristics characteristics) {
		this.characteristics = characteristics;
	}

	/**
	 * Gets the regional cis name.
	 * 
	 * @return the regional cis name
	 */
	protected String getRegionalCisName() {
		return regionalCisName;
	}

	/**
	 * Sets the regional cis name.
	 * 
	 * @param regionalCisName the new regional cis name
	 */
	protected void setRegionalCisName(String regionalCisName) {
		this.regionalCisName = regionalCisName;
	}

	/**
	 * Gets the vm allocation policy.
	 * 
	 * @return the vm allocation policy
	 */
	public VmAllocationPolicy getVmAllocationPolicy() {
		return vmAllocationPolicy;
	}

	/**
	 * Sets the vm allocation policy.
	 * 
	 * @param vmAllocationPolicy the new vm allocation policy
	 */
	protected void setVmAllocationPolicy(VmAllocationPolicy vmAllocationPolicy) {
		this.vmAllocationPolicy = vmAllocationPolicy;
	}

	/**
	 * Gets the last process time.
	 * 
	 * @return the last process time
	 */
	protected double getLastProcessTime() {
		return lastProcessTime;
	}

	/**
	 * Sets the last process time.
	 * 
	 * @param lastProcessTime the new last process time
	 */
	protected void setLastProcessTime(double lastProcessTime) {
		this.lastProcessTime = lastProcessTime;
	}

	/**
	 * Gets the storage list.
	 * 
	 * @return the storage list
	 */
	public List<Storage> getStorageList() {
		return storageList;
	}

	/**
	 * Sets the storage list.
	 * 
	 * @param storageList the new storage list
	 */
	protected void setStorageList(List<Storage> storageList) {
		this.storageList = storageList;
	}

	//added by reza
	protected void setFileList(List<File> cl) {
		this.cl = cl;
	}
	
	/**
	 * Gets the vm list.
	 * 
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param vmList the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the scheduling interval.
	 * 
	 * @return the scheduling interval
	 */
	protected double getSchedulingInterval() {
		return schedulingInterval;
	}

	/**
	 * Sets the scheduling interval.
	 * 
	 * @param schedulingInterval the new scheduling interval
	 */
	protected void setSchedulingInterval(double schedulingInterval) {
		this.schedulingInterval = schedulingInterval;
	}
	
	//Added By Reza
	/**
	 * Gets the VMBW
	 * 
	 * @return the VMBW
	 */
	public int [][] getVMBW() {
		return VMBW;
	}

	/**
	 * Sets the set VMBW
	 * 
	 * @param set VMBW
	 */
	public void setVMBW(int [][] VMBW) {
		this.VMBW = VMBW;
	}
	

	
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////// This Class keep VM Execution Time  /////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public static class VMExecutionTimeB {
public double start;
public double end;
public int Cloudlet;

public String toString() {
return "Start: " + start + ", End: " + end + ", Cloudlet:" + Cloudlet ;
}
}

public class VMsTimeExecution {
public ArrayList<ArrayList<VMExecutionTimeB>> VMsSchedule2 = new ArrayList<ArrayList<VMExecutionTimeB>>(); 

//We need intial this class before using
public void intial(int VmSize){
VMExecutionTimeB IVMExecutionTimeB = new VMExecutionTimeB();
IVMExecutionTimeB.start = 0.0;
IVMExecutionTimeB.end = 0.0;
IVMExecutionTimeB.Cloudlet = 0;


for (int x =0; x<(VmSize+1);x++){
ArrayList<VMExecutionTimeB> VmExeTemp = new ArrayList<VMExecutionTimeB>();
VmExeTemp.add(IVMExecutionTimeB);
VMsSchedule2.add(x, VmExeTemp);
}

}
//checking is it vm is free for assignment
//StartT means minimun time cloudlet is ready to run in this Vm
public double VMisFree(int Vm, int Cloudlet, double StartT, double executionT, double Deadline, ArrayList<ArrayList<VMExecutionTimeB>> VMsSchedule2){
//Log.printLine("*$* Execution table of VM, VMsSchedule2 is equal:");
//Log.printLine(VMsSchedule2);

VMExecutionTimeB IVMExecutionTimeB = new VMExecutionTimeB();
IVMExecutionTimeB.start = StartT;
IVMExecutionTimeB.end = StartT+executionT;
IVMExecutionTimeB.Cloudlet = Cloudlet;
if(VMsSchedule2.get(Vm).size() == 1){
//Log.printLine("*$* VM is Free, there is no assignment yet");
return 0.0;
}else {
if(!((VMsSchedule2.get(Vm).get(1).start - 0) < StartT+executionT) ){
//VMsSchedule2.get(Vm).add(x, IVMExecutionTimeB);
//Log.printLine("*$* there is free time for execution period (first condition)");
return 0.0;
}
for (int x =0;x < (VMsSchedule2.get(Vm).size()-1);x++){
if(!(VMsSchedule2.get(Vm).get(x).end < StartT)){
//Log.printLine("start time:" + StartT + " < start time of vm:" + VMsSchedule2.get(Vm).get(x).start);
if(!(VMsSchedule2.get(Vm).get(x+1).start - VMsSchedule2.get(Vm).get(x).end < executionT) ){
//VMsSchedule2.get(Vm).add(x, IVMExecutionTimeB);
Log.printLine("*$* there is free time for execution period (second condition)");
return VMsSchedule2.get(Vm).get(x).end;
}
}
}

if(!(VMsSchedule2.get(Vm).get(VMsSchedule2.get(Vm).size()-1).end > StartT) ){
//VMsSchedule2.get(Vm).add(x, IVMExecutionTimeB);
Log.printLine("*$* there is free time for execution period (third condition)");
return VMsSchedule2.get(Vm).get(VMsSchedule2.get(Vm).size()-1).end;
}

//Log.printLine("*$* VM: "+Vm+" is not Free before start time, use forth condition as last task in vm");
return VMsSchedule2.get(Vm).get(VMsSchedule2.get(Vm).size()-1).end;

}
}

}











//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////This phase include read  BW between each nodes /////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


public static int [][] readingVM (int VmSize){

//This phase include read  BW between each nodes
int VMBW[][] = new int[VmSize+1][VmSize+1] ;
try{
InputStream inp1 = new FileInputStream("d:\\VMBW.xlsx");

Workbook wb1 = WorkbookFactory.create(inp1);
Sheet sheet = wb1.getSheetAt(0);
int ll =0;

//read all row
for (org.apache.poi.ss.usermodel.Row row : sheet) {
if (!(row.getRowNum() ==  0)){ // row 0 is description
//Log.printLine();
//Log.print(" VMBW " + ll +": ");
for (int l =0; l < (VmSize+1) ; l++){// read all row 
Cell BW = (Cell) row.getCell(l+1); //First coloum is VM no., it will be start from 1 to VmSize+1
VMBW[ll][l] = (int) (BW.getNumericCellValue()/8); // because transfer rate is by bit but storage is by byte
//Log.print(l+ ": " + VMBW[ll][l]+" "  );
}
ll++;
}
}
/*
int temp =0;
for ( ll =0; ll < (VmSize) ; ll++){
temp =0;
for (int l =0; l < (VmSize) ; l++){
temp = VMBW[ll][l] + temp ;
}
VMBW [ll][VmSize] = temp ;
}
*/


//Write the output to a file
FileOutputStream fileOut = new FileOutputStream("d:\\VMBW.xlsx");
wb1.write(fileOut);
fileOut.close();
} catch (Exception e) {
e.printStackTrace();
}

//Checking of reading
/*
Log.printLine();
Log.printLine("**************************************************************************************************************************");
Log.printLine("BW between VMs-------------------------------------------------------------------------------------------------------------");

for (int z =0; z<(VmSize); z++){

for (int zz =0; zz<(VmSize+1); zz++){
if (zz == 0){
Log.printLine();
Log.print(" VM ID: " + VMBW[z][zz]);
} else {
Log.print("  "+ VMBW[z][zz]  );
}
}
}

Log.printLine();
Log.printLine("BW between VMs--------------------------------------------------------------------------------------------------------------");
Log.printLine("**************************************************************************************************************************");
*/
return VMBW;
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////This Class keep VM information for Scheduling(including reading heterogeneous ratio of VMs) /////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public static class VMInfo {
public ArrayList<Integer> MIPS = new ArrayList<Integer>(); // MIPS of VMs
public ArrayList<Integer> Energy = new ArrayList<Integer>(); // Energy of VMs
public ArrayList<Integer> BW = new ArrayList<Integer>(); // BW of VMs

public ArrayList<ArrayList<Double>> MIPSRatioApp = new ArrayList<ArrayList<Double>>(); 
public ArrayList<ArrayList<Integer>> ExecEnergyUnit = new ArrayList<ArrayList<Integer>>(); 
public ArrayList<ArrayList<Integer>> TransEnergyUnit = new ArrayList<ArrayList<Integer>>(); 
public ArrayList<Integer> AveTransEnergyUnit = new ArrayList<Integer>();


//This function is for Initialing the data by reading from other class 
public void intial(List<Vm> vmSpecification,int VmSize){
//shall be based on No. of VM
for (int i=0; i < VmSize ; i++){
// First Step is intialzation of MIPS
MIPS.add(i,(int) vmSpecification.get(i).getMips());

//Second Energy
Energy.add(i,(int) vmSpecification.get(i).getbattery());

//Third BW
BW.add(i,(int) vmSpecification.get(i).getBw());
}
}
//This function is for Initialing the data by reading from  excel file
public void intialAppRatio(int CLID){
// Reading from file Energy and MIPS and TranEnergyUnit
	ArrayList<Double> MIPStemp = new ArrayList<Double>();
	ArrayList<Integer> Exetemp = new ArrayList<Integer>();
	ArrayList<Integer> Transtemp = new ArrayList<Integer>();

try{
InputStream inp1 = new FileInputStream("d:\\AppRatio.xlsx");
Workbook wb1 = WorkbookFactory.create(inp1);
Sheet sheet = wb1.getSheetAt((CLID%100));
int ll =0;

//read all row
for (org.apache.poi.ss.usermodel.Row row : sheet) {
if (!(row.getRowNum() ==  0)){ // row 0 is description
//Log.printLine();
//Log.print(" AppRatio VM" + ll +": ");

//First coloum is MIPSRatioApp
Cell Temp = (Cell) row.getCell(1); 
MIPStemp.add(ll, (double) Temp.getNumericCellValue());
//Log.print(ll+ ": " + MIPStemp.get(ll)+" "  );

//Second coloum is MIPSRatioApp
Temp = (Cell) row.getCell(2); 
Exetemp.add(ll, (int) Temp.getNumericCellValue());
//Log.print(ll+ ": " + Exetemp.get(ll)+" "  );

//Second coloum is MIPSRatioApp
Temp = (Cell) row.getCell(3); 
Transtemp.add(ll, (int) Temp.getNumericCellValue());
//Log.print(ll+ ": " + Transtemp.get(ll)+" "  );

ll++; //counting
}
}
// Write the output to a file
FileOutputStream fileOut = new FileOutputStream("d:\\AppRatio.xlsx");
wb1.write(fileOut);
fileOut.close();
} catch (Exception e) {
e.printStackTrace();
}
MIPSRatioApp.add( new ArrayList<Double>(MIPStemp));
ExecEnergyUnit.add(new ArrayList<Integer>(Exetemp));
TransEnergyUnit.add(new ArrayList<Integer>(Transtemp));
/*
Log.printLine();
Log.printLine("##################################  Checking ** VM information ** Checking ############################################");

Log.printLine("MIPSRatioApp: "+MIPSRatioApp);
Log.printLine("ExecEnergyUnit: "+ExecEnergyUnit);
Log.printLine("TransEnergyUnit: "+ TransEnergyUnit);

Log.printLine("##################################  Checking ** VM information ** Checking ############################################");

Log.printLine();	
*/
}// Intialzation finished

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
public static class ReplicaManagement {
	public ArrayList<File> Files = new ArrayList<File>(); // File Size
	public ArrayList<Integer> NOReplica = new ArrayList<Integer>(); // No of Replica
	public ArrayList<Integer> MasterReplica = new ArrayList<Integer>(); // No of Replica

	public ArrayList<Integer> NoA = new ArrayList<Integer>(); // No of Access
	public ArrayList<ArrayList<Integer>> RepsVm = new ArrayList<ArrayList<Integer>>(); 
	int FIDCounter =-1;
	
	public double PreviouseMaintain = 0.0;
	public double MTBT = 20000000.0; // MeadTimeBetweenmaintainance 
/////////////////////////////////////      This part belong to VmReadList Begin
	public class VmReadListClass {
		int FID;
		int VM;
		int NoReadNeeded = 0;
		int NoReadIsDone = 0;
		boolean ReplicaCreated = false;
		public VmReadListClass(int FID, int Vm, int NoReadNeeded, int NoReadIsDone, boolean ReplicaCreated){
			
			this.FID = FID;
			this.VM = Vm;
			this.NoReadNeeded = NoReadNeeded;
			this.NoReadIsDone = NoReadIsDone;
			this.ReplicaCreated = ReplicaCreated;
		}
		public String toString() {
		        return "FID: " + FID +"VM: " + VM + ", NoReadNeeded: " + NoReadNeeded+ ", NoReadIsDone: " + NoReadIsDone+ ", ReplicaCreated: " + ReplicaCreated;
		    }
	}
	
	
	public void AddVmToVmReadList(String FileName, int VM){
			
			int FID = FindFIDByName(FileName);
			int NoReadNeeded;
			int NoReadIsDone;
			boolean ReplicaCreated;
			int IdofVmReadLIst = FindFIDsID(FID, VM);
			if (IdofVmReadLIst== 9999){
				IdofVmReadLIst = VmReadList.size()+1; // It means we should add new record
				NoReadNeeded = 1;
				NoReadIsDone = 0;
				ReplicaCreated = false;
				VmReadListClass temp = new VmReadListClass(FID,VM, NoReadNeeded,NoReadIsDone,ReplicaCreated);
				//Log.printLine("new, FID: "+FID+ " VM: "+ VM+" NoReadNeeded: "+NoReadNeeded+" NoReadIsDone: "+NoReadIsDone+" ReplicaCreated: "+ReplicaCreated);
				VmReadList.add(temp);
				
			}else {
				NoReadNeeded = VmReadList.get(IdofVmReadLIst).NoReadNeeded+1;
				NoReadIsDone = VmReadList.get(IdofVmReadLIst).NoReadIsDone;
				ReplicaCreated = VmReadList.get(IdofVmReadLIst).ReplicaCreated;
				VmReadListClass temp = new VmReadListClass(FID,VM, NoReadNeeded,NoReadIsDone,ReplicaCreated);
				//Log.printLine("set, FID: "+FID+ " VM: "+ VM+" NoReadNeeded: "+NoReadNeeded+" NoReadIsDone: "+NoReadIsDone+" ReplicaCreated: "+ReplicaCreated);

				VmReadList.set(IdofVmReadLIst, temp);
			}
			
			
		}
	
	public void SetVmReadlistCreation(int FID, int VM){
		int ID = FindFIDsID(FID,VM);
		VmReadList.get(ID).ReplicaCreated = true;
	}
	public int FindFIDsID(int FID, int VM){
			for (int i=0; i < VmReadList.size();i++){
				if ((Objects.equals(VmReadList.get(i).FID, FID) )&&(Objects.equals(VmReadList.get(i).VM , VM))){
					return i;
				}
			}
			// There is no reocrd with this FID so need to be added we use "9999" digit to should it doesn't exict
			return 9999;
		}
	public int FindFIDByName(String FileName){
		for (int i=0; i < Files.size();i++){
			//Log.printLine("ID: "+ i+ " FilaName: "+ Files.get(i).getName()+ " ,needed to be compare with: "+ FileName);
			if (Objects.equals(FileName, Files.get(i).getName())){//Files.get(i).getName()
				return i;
			} 
		}
		// There is no reocrd with this FID so need to be added we use "9999" digit to should it doesn't exict
		return 9999;
	}

	// if is in Vmlist need to be added and becouase energy is already deduct just use itiation adding which is without energy deduction
	public boolean CheckReadForVmReadList(int fID, int vm, ReplicaManagement iReplicaManagement2) {
		for(int i=0; i < VmReadList.size();i++){
			if(VmReadList.get(i).FID == fID){
				if(VmReadList.get(i).VM == vm){
					// need to set replica craeted
					//Log.printLine("FID"+ fID + "Needed to be read on VmID: "+vm);
					VmReadList.get(i).ReplicaCreated = true;
					return false;
				}
				
			}
		}
		return true;
	}
	
	//It's not complete
	public int MigrationUpdate(int VmS, int VmD, ArrayList<File> MigratedFileList){
		for (int i=0; i < MigratedFileList.size();i++){
			//int FID = FindFIDByName(MigratedFileList.get(i));
			
		}
		// There is no reocrd with this FID so need to be added we use "9999" digit to should it doesn't exict
		return 9999;
	}
	
	public ArrayList<VmReadListClass> VmReadList = new ArrayList<VmReadListClass>() ;
	
/////////////////////////////////////      This part belong to VmReadList End
	
	
	
	
	
	
	
	public void IncFIDCounter (){
		FIDCounter++;
	}
	
	public int GetFIDCounter (){
		return FIDCounter;
	}
	
	public void SetPreviouseMaintain (double Time){
		PreviouseMaintain = Time  ;
	}
	
	public double GetPreviouseMaintain (){
		return PreviouseMaintain;
	}
	public void intiantionAddReplica(int FID,File Replica,int Vm){
		//Only first time need to add master copy in list
		
		if (Replica.isMasterCopy()){
		Files.add(FID, Replica);
		}
		
		if( (NOReplica.isEmpty())){
			NOReplica.add(FID, 1);
		} else {
			if (FID > FIDCounter){
				NOReplica.add(FID, 1);
			}else{
				NOReplica.set(FID, (NOReplica.get(FID)+1));
			}
			
		}
		
		
		NoA.add(FID, 0);
		
		
		if(FID > FIDCounter){
			ArrayList<Integer> VmT = new ArrayList<Integer>();
			VmT.add(Vm);
			RepsVm.add(FID, VmT);
		} else {
			ArrayList<Integer> VmT = new ArrayList<Integer>();
			VmT = RepsVm.get(FID);
			VmT.add(Vm);
			RepsVm.add(FID, VmT);
		}
		// Set ID of VM to distinguish MasterCopy
		if(Replica.isMasterCopy()){
			MasterReplica.add(FID, Vm);
		}
		
	}
	
	public void AddreplicaWithoutEnergyDeduction(int FID,int Vm, Datacenter linkDC){
		//Only first time need to add master copy in list
		if(!IsItDuplicate(FID,Vm)){
		NOReplica.set(FID, (NOReplica.get(FID)+1));
		ArrayList<Integer> VmT = new ArrayList<Integer>();
		VmT = RepsVm.get(FID);
		VmT.add(Vm);
		RepsVm.set(FID, VmT);
		linkDC.getStorageList().get(linkDC.getVmList().get(Vm).getHost().getId()).addFile(Files.get(FID));
		}
	}
		
public void addReplica(int FID,int Vm, Datacenter linkDC){
	//Deducting Energy From S&D
	ArrayList<Integer> VmT = new ArrayList<Integer>();
	VmT = RepsVm.get(FID);
	
	// finding fast and lowest energy consuming replica
	double temp =99999999999999999.0;
	int VmS =9999;
	double BW =0.0;
	
	
	for (int i=0; i< VmT.size();i++){
		BW = linkDC.getVMBW()[Vm][VmT.get(i)];
		if (temp > ((double) (linkDC.IVMInfo.AveTransEnergyUnit.get(VmT.get(i)))/ BW)){
			int VmID = VmT.get(i);
			double VmEnergy = linkDC.getVmList().get(VmID).getbattery();
			double TransferEnergy = ((((double)((Files.get(FID).getSize())*linkDC.IVMInfo.AveTransEnergyUnit.get(VmID))
					/(double)linkDC.IVMInfo.BW.get(VmID)))* 1.5) -1;
			if (VmEnergy > TransferEnergy ){
				temp = ((double) (linkDC.IVMInfo.AveTransEnergyUnit.get(VmT.get(i)))/ BW);
				VmS = VmT.get(i);
			}
			
		}
	}
	if (!(VmS == 9999)){
	// deducting energy of Source
	double deductedenergy = 0;
	double energy = 0;

	deductedenergy = linkDC.getVmList().get(VmS).getbattery();
	//Log.printLine("Replica adding Source, Energy of Vm:"+ VmS+" before deduction: "+ deductedenergy);
	energy = ((double)((Files.get(FID).getSize())*linkDC.IVMInfo.AveTransEnergyUnit.get(VmS))
			/(double)linkDC.getVMBW()[Vm][VmS]);
	//Log.printLine("File Size: "+ Files.get(FID).getSize()+ " AveTransEnergyUnit: "+linkDC.IVMInfo.AveTransEnergyUnit.get(VmS)+
		//	" BW: "+linkDC.getVMBW()[Vm][VmS]);
	linkDC.getVmList().get(VmS).AddReplicaManagmentE(energy);
	deductedenergy = deductedenergy - energy;
	//Log.printLine("Replica adding Source, Energy of Vm after deduction: "+ deductedenergy);
	linkDC.getVmList().get(VmS).setbattery((long) deductedenergy);
	linkDC.IVMInfo.Energy.set(VmS, (int) (linkDC.IVMInfo.Energy.get(VmS) - energy));
	// deducting energy from Destination
	
	deductedenergy = linkDC.getVmList().get(Vm).getbattery();
	//Log.printLine("Replica adding destination, Energy of Vm:"+ Vm+" before deduction: "+ deductedenergy);
	energy = ((double)((Files.get(FID).getSize())*linkDC.IVMInfo.AveTransEnergyUnit.get(Vm))
			/(double)linkDC.getVMBW()[Vm][VmS]);
	linkDC.getVmList().get(Vm).AddReplicaManagmentE(energy);
	deductedenergy = deductedenergy - energy;
	//Log.printLine("Replica adding destination, Energy of Vm after deduction: "+ deductedenergy + ", Energy deducted: "+energy);
	linkDC.getVmList().get(Vm).setbattery((long) deductedenergy);
	linkDC.IVMInfo.Energy.set(Vm, (int) (linkDC.IVMInfo.Energy.get(Vm)- energy));
	//increase No of replica
	NOReplica.set(FID,(NOReplica.get(FID)+1));
	
	// Add to Vm list
	VmT.add(Vm);
	RepsVm.set(FID, VmT);
	//adding to storagelist
	
	linkDC.getStorageList().get(linkDC.getVmList().get(Vm).getHost().getId()).addFile(Files.get(FID));
	if(!(linkDC.getStorageList().get(linkDC.getVmList().get(Vm).getHost().getId()).getFile( Files.get(FID).getName()) == null)){
		//Log.printLine("It sucessfully created");
	}
	} else {
		//Log.printLine("There is no source with enough energy to create replica");
	}
}

public void delReplica(int FID,int Vm, Datacenter linkDC){
	NOReplica.set(FID,(NOReplica.get(FID)-1));
	
	ArrayList<Integer> VmT = new ArrayList<Integer>();
	VmT = RepsVm.get(FID);
	int id=999999;
	for (int i=0;i< VmT.size();i++){
		if (VmT.get(i)==Vm){
			id =i;
		}
	}
	VmT.remove(id);
	RepsVm.set(FID, VmT);
	//Log.printLine("File: "+Files.get(FID).getName()+ " ,lost replica on VMID: "+Vm);
	
	for(int i=0; i < linkDC.getStorageList().size();i++){
		int HostID = linkDC.getStorageList().get(i).getHostID();
		int VmID = linkDC.getHostList().get(HostID).getVmList().get(0).getId();
		if(VmID == Vm){ 
			linkDC.getStorageList().get(i).deleteFile(Files.get(FID));

		}
	}
	if((linkDC.getStorageList().get(linkDC.getVmList().get(Vm).getHost().getId()).getFile( Files.get(FID).getName()) == null)){
		//Log.printLine("It sucessfully deleted");
	}
	
}

public boolean IsItDuplicate(int FID,int Vm){
		
	ArrayList<Integer> VmT = new ArrayList<Integer>();
	VmT = RepsVm.get(FID);
	int id=999999;
	for (int i=0;i< VmT.size();i++){
		if (VmT.get(i)==Vm){
			return true;
		}
	}
	return false;
}


public void Maintain2( Datacenter linkDC, double CurrentTime){
	// checking each Vm has 150% 0f their own file transferring
	double TransferEnergy =0.0;
	double VmEnergy =0.0;
	int VmID=0;
	
	
	for (int i=0; i < (FIDCounter+1); i++){// fow all FID
		ArrayList<Integer> VmListTemp = new ArrayList<Integer>();
		for (int j=0; j < RepsVm.get(i).size();j++ ){// for all VM for rach FID
			VmID = RepsVm.get(i).get(j);
			VmEnergy = linkDC.getVmList().get(VmID).getbattery();
			TransferEnergy = ((((double)((Files.get(i).getSize())* (double)linkDC.IVMInfo.AveTransEnergyUnit.get(VmID))
					/(double)linkDC.IVMInfo.BW.get(VmID)))* 1.5) -1;
			//Log.printLine("VmEnergy: "+ VmEnergy+ "TransferEnergy"+ TransferEnergy);
			if(VmEnergy < TransferEnergy ){
				//finding new replica
				//linkDC.IReplicaManagement.ReplicaTest( linkDC.IReplicaManagement);
				//Log.printLine("We need to delete replica FID:" +i+ " on VM: "+ VmID);
				
				int SelectedVmt =ReplacementM2( linkDC,i);
				//Log.printLine("New replica is in: "+ SelectedVmt);

				VmListTemp.add(VmID);
				//delReplica(i,VmID);
				
				if(SelectedVmt==9999){
					Log.printLine("Replica Managment couldn't find VM to create replica");
					//CloudSim.terminateSimulation();
				}else {
				//	Log.printLine("Replica Managment create replica on VMID :"+ SelectedVmt);
					if (!IsEnoughSpaceStorage (Files.get(i).getSize(), SelectedVmt, linkDC)){
						Log.printLine("there is no enough space");
					}
					addReplica(i,SelectedVmt, linkDC);
				}
				//linkDC.IReplicaManagement.ReplicaTest( linkDC.IReplicaManagement);

			}
			
		}
		for (int j=0; j < VmListTemp.size();j++ ){
			delReplica(i,VmListTemp.get(j),linkDC);
		}
		VmListTemp.clear();
	}
	//linkDC.IReplicaManagement.ReplicaTest( linkDC.IReplicaManagement);
	
	/*
	// Checking time for maintenance
	double TNoA =0;
	//Log.printLine("CurrentTime: "+CurrentTime);
	//Log.printLine("PreviouseMaintain: "+PreviouseMaintain);
	//Log.printLine();
	
	if (MTBT < (CurrentTime-PreviouseMaintain)){
		//Log.printLine("CurrentTime: "+CurrentTime);
		//Log.printLine("PreviouseMaintain: "+PreviouseMaintain);
		// Phase 1, Calculate AveNoA
		for(int i =0; i < (FIDCounter+1);i++){
			TNoA = TNoA + NoA.get(i);
		}
		TNoA = TNoA/(double)(FIDCounter+1);
		//Log.printLine("Ave NoA: "+TNoA);
		//Delete or add Replica 
		for (int i =0; i <(FIDCounter+1);i++ ){
			double TransferEnergyPerUnit =0.0;
			VmEnergy =0.0;
			double BW =0.0;
			double CompMetric = 0.0;
			double temp =99999999999999.0;
			int SelectedVm =9999;
			 if (NoA.get(i) < TNoA){ // shall delete one replica if NoReps are more than 3
				 if (NOReplica.get(i)>3){
					// Log.printLine("Shall delete ");
					 for(int j=0; j < NOReplica.get(i);j++){
						 VmID = RepsVm.get(i).get(j);
						 VmEnergy = linkDC.getVmList().get(VmID).getbattery();
						 TransferEnergyPerUnit = linkDC.IVMInfo.AveTransEnergyUnit.get(VmID);									
						 BW = linkDC.IVMInfo.BW.get(VmID);
						 CompMetric = (VmEnergy*BW)/TransferEnergyPerUnit;
						 
						 if (temp > CompMetric){
							 temp = CompMetric;
							 SelectedVm = VmID;
						 }
					}
					 delReplica( i,SelectedVm,linkDC);
				 }
			 }else if (NoA.get(i) > TNoA){// shall add new replica if NoReps are less than 7
				 if (NOReplica.get(i)<8){ // less than 8
					// Log.printLine("Shall Add ");
					 SelectedVm = ReplacementM2( linkDC,i);
					 if(!(SelectedVm == 9999)){
					 addReplica(i,SelectedVm, linkDC);
					 }else {
						// Log.printLine("There is no suitable Vm for adding replica during maintain");
					 }
				 }
			 }
		}
		SetPreviouseMaintain(CurrentTime);	
		//linkDC.IReplicaManagement.ReplicaTest( linkDC.IReplicaManagement);

	}
	*/

}

private int ReplacementM2(Datacenter linkDC,int FID) {//i = FID
	double tempt =99999999999999.0;
	int SelectedVmt = 9999;
	// part one if there is needed one more than two time
	for(int j=0; j < linkDC.IReplicaManagement.VmReadList.size(); j++ ){
		if(linkDC.IReplicaManagement.VmReadList.get(j).FID== FID){
			int VMt = linkDC.IReplicaManagement.VmReadList.get(j).VM;
			int ReadNo = 1;
			if(!(IsItDuplicate(FID,VMt))){
				if(ReadNo < (linkDC.IReplicaManagement.VmReadList.get(j).NoReadNeeded-linkDC.IReplicaManagement.VmReadList.get(j).NoReadIsDone)){
					// then we should check is there enough energy
					ReadNo = (linkDC.IReplicaManagement.VmReadList.get(j).NoReadNeeded-linkDC.IReplicaManagement.VmReadList.get(j).NoReadIsDone);
					double VmEnergyt= linkDC.getVmList().get(VMt).getbattery();
					double BWt = linkDC.getVMBW()[VMt][linkDC.getVmList().size()];
					double TransferEnergy = ((((double)((Files.get(FID).getSize())*(double)linkDC.IVMInfo.AveTransEnergyUnit.get(VMt)))
							/(double)BWt)* 2.5) -1;
					if (VmEnergyt>TransferEnergy ){
						if (IsEnoughSpaceStorage (Files.get(FID).getSize(), VMt, linkDC)){
							SelectedVmt = VMt;
						}
						
					}
					
				}
			}
		}
		
	}
	if(!(SelectedVmt==9999)){
		//Log.printLine("By VmReadList Selected, Energy of VMID: "+SelectedVmt+" , is equal to: "+  linkDC.getVmList().get(SelectedVmt).getbattery());

		return SelectedVmt;
	}
	
	// Second phase based on costfun
	for(int jj=0; jj < linkDC.getVmList().size();jj++){
		 
		if (!IsItDuplicate(FID,jj)){
		 int VmID = jj;
		 double VmEnergyt;
		 double TransferEnergyPerUnit;
		 double TransferEnergy;
		 double CostFun;
		 int BWt;
		 					 
		 VmEnergyt= linkDC.getVmList().get(jj).getbattery(); // IVMInfo
		 if(linkDC.IVMInfo.Energy.get(jj) < VmEnergyt){
			 VmEnergyt =linkDC.IVMInfo.Energy.get(jj);
		 }
		 TransferEnergyPerUnit = linkDC.IVMInfo.AveTransEnergyUnit.get(jj);									
		 BWt = linkDC.getVMBW()[jj][linkDC.getVmList().size()];
		 CostFun = (TransferEnergyPerUnit)/(BWt*VmEnergyt);
		 //Log.printLine("VmID: "+linkDC.getVmList().get(jj).getId()+" TransferEnergyPerUnit: "+TransferEnergyPerUnit+"BW: "+ BWt+
				// " VmEnergy: "+ VmEnergyt+" cost: "+ CostFun);
		 
		 TransferEnergy = ((((double)((Files.get(FID).getSize())*(double)linkDC.IVMInfo.AveTransEnergyUnit.get(jj)))
					/(double)BWt)* 2.5) -1;
		
		 if (tempt > CostFun){
			 //We need to check is there enough energy in VM to receive and at least one read we consider 250%
			 
			 if (TransferEnergy < VmEnergyt){
				 if (IsEnoughSpaceStorage (Files.get(FID).getSize(), jj, linkDC)){
					 tempt = CostFun;
					 SelectedVmt = jj;
					}
			 
			 }
		 }
	}
	}
	if(!(SelectedVmt == 9999)){
		//Log.printLine("based on cost Selected, Energy of VMID: "+SelectedVmt+" , is equal to: "+  linkDC.getVmList().get(SelectedVmt).getbattery());

	}
	return SelectedVmt;
}

// function for testing remining space of storage for creating file
public boolean IsEnoughSpaceStorage (int filesize, int VMID, Datacenter linkDC){
	
	int AvailableSpace = (int) linkDC.getStorageList().get(linkDC.getVmList().get(VMID).getHost().getId()).getAvailableSpace();
	if (filesize < AvailableSpace){
		return true;
	}else {
		return false;
	}
}



public static void ReplicaTest(ReplicaManagement IReplicaManagement, Datacenter datacenter)
{
	Log.printLine("Replica State based on data in Replica Management:");
	for (int tt=0; tt < IReplicaManagement.GetFIDCounter()+1;tt++){
	Log.printLine("File name: "+IReplicaManagement.Files.get(tt).getName()+ ", No of replica: "+ IReplicaManagement.NOReplica.get(tt)+
			", VM's which have Replica"+ IReplicaManagement.RepsVm.get(tt)+ ", MasterCopy ID: "+ IReplicaManagement.MasterReplica.get(tt));

	}
	
	Log.printLine("NoA State based on data in Replica Management:");
	for (int tt=0; tt < IReplicaManagement.GetFIDCounter()+1;tt++){
	Log.printLine("File name: "+IReplicaManagement.Files.get(tt).getName()+ ", No of Access: "+ IReplicaManagement.NoA.get(tt));

	}
	
	Log.printLine("NoA State based on data in Storage:");
	for(int i=0; i< datacenter.storageList.size();i++){
		for(int j =0; j<datacenter.storageList.get(i).getFileNameList().size();j++ ){
			Log.printLine("HostID: "+datacenter.storageList.get(i).getHostID() + 
					" VmID: "+ datacenter.getHostList().get(datacenter.storageList.get(i).getHostID()).getVmList().get(0).getId()+
					" File name: "+datacenter.storageList.get(i).getFileNameList().get(j)+
					" FID: "+ datacenter.storageList.get(i).getFile(datacenter.storageList.get(i).getFileNameList().get(j)).getFID());

		}

	}
	
	//pressAnyKeyToContinue();
	//pressAnyKeyToContinue();
}
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

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// This class is for keeping execution time of each cloudlet in each VM   /////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



public class VMtoCloudletTime {
public ArrayList<ArrayList<Integer>> VMtoCloudletTime = new ArrayList<ArrayList<Integer>>();  // Total time basde on each VM BW
public ArrayList<ArrayList<Integer>> VMtoCloudletTranTime = new ArrayList<ArrayList<Integer>>(); // Transmision time based on each VM
public ArrayList<ArrayList<Integer>> VMtoCloudletExecTime = new ArrayList<ArrayList<Integer>>(); // Total time based on MI/MIPS
public ArrayList<ArrayList<Integer>> VMtoCloudletReadTime = new ArrayList<ArrayList<Integer>>(); // Total time based on MI/MIPS
public int MaxReadDelay =0;
public int MaxWriteDelay =0;
public int MaxEnergyPerUnitReadWrite =0;
private ArrayList<Integer> temp = new ArrayList<Integer>(); 

public void intial(int cloudletsize,List<? extends Vm> vmList, int[][] VMBW, List<? extends Cloudlet> cloudletList, Datacenter datacenter){
int VmSize = vmList.size();
	//Calculation Execution time
int Itemp = 0 ;

for(int j=0; j < cloudletsize ;j++){
	for (int i =0; i < (VmSize); i++){
		Itemp =  (int) (((double)cloudletList.get(j).OrginalLength / (double)vmList.get(i).getMips())* (double)datacenter.IVMInfo.MIPSRatioApp.get(j).get(i));

		//Log.printLine(" "+j+", "+i+": "+ Itemp);
		temp.add(i,Itemp);
	}	
	VMtoCloudletExecTime.add( new ArrayList<Integer>(temp));
	temp.clear();
}
/*
Log.printLine();
Log.printLine("##################################  Checking ** Execution time ** Checking ############################################");
for(int j=0; j< cloudletsize; j++){
Log.printLine(" "+ VMtoCloudletExecTime.get(j));
}	

Log.printLine("##################################  Checking ** Execution time ** Checking ############################################");
*/



//Calculation Transmission time
//Log.printLine();
for(int j=0; j< cloudletsize; j++){
temp.clear();
for (int i =0; i < (VmSize); i++){
//Log.printLine(" CloudletN["+j+", "+i+"]: "+ CloudletN[j][4]);
//Log.printLine(" VMBW ["+j+", "+i+"]: "+ VMBW [i][i+1]);
Itemp =  (int) (cloudletList.get(j).getCloudletOutputSize()/(VMBW [i][VmSize]));

temp.add(i,Itemp);
}
VMtoCloudletTranTime.add( new ArrayList<Integer>(temp));
}
/*
Log.printLine();
Log.printLine("##################################  Checking ** Transmision time ** Checking ############################################");
for(int j=0; j< cloudletsize; j++){
Log.printLine(" "+ VMtoCloudletTranTime.get(j));
}
Log.printLine("##################################  Checking ** Transmision time ** Checking ############################################");
*/
//Read and write Energy
/*
for (int j=0;j< datacenter.storageList.size();j++){
//if (MaxReadDelay < Storagelist.get(j).getMaxTransferRate()){
MaxReadDelay = MaxReadDelay + (int) datacenter.storageList.get(j).getMaxTransferRate();
//}
}
MaxReadDelay = MaxReadDelay/datacenter.storageList.size();
//Log.printLine(" Storage Read Delay: "+ MaxReadDelay);

for (int j=0;j< datacenter.storageList.size();j++){
//if (MaxWriteDelay < Storagelist.get(j).getAvgSeekTime()){
MaxWriteDelay = MaxWriteDelay + (int) datacenter.storageList.get(j).getAvgSeekTime();
//}
}

MaxWriteDelay = MaxWriteDelay/datacenter.storageList.size();
//Log.printLine(" Storage Write Delay: "+ MaxWriteDelay);
MaxEnergyPerUnitReadWrite = 1;
//Log.printLine(" Storage Energy Per Unit Read/Write: "+ MaxEnergyPerUnitReadWrite);


int AveBW =0;
for(int j=0; j< VmSize; j++){
AveBW = AveBW+VMBW[j][VmSize];
}
AveBW = AveBW/VmSize;
int MinBW =500000;
for(int j=0; j< VmSize; j++){
if (MinBW>VMBW[j][VmSize]){
MinBW = VMBW[j][VmSize];
}
}
AveBW = AveBW;
Log.printLine("AveBW: "+ AveBW);
*/
//pressAnyKeyToContinue();
//cl1.setCreateFile(Writefiles.get(k)); // writing part send by list
for(int k=0; k< cloudletsize; k++){
	temp.clear();
	for (int i =0; i < (VmSize); i++){
		double readdelay =0;
		double writedelay =0;
		for(int cn=0; cn < cloudletList.get(k).getRequiredFiles().size();cn++){
			double filesize =0;
			String filename = "";
			filename = cloudletList.get(k).getRequiredFiles().get(cn);
			int fileID = 9999;
			for(int fl=0; fl < datacenter.IReplicaManagement.Files.size(); fl++){
				//Log.printLine(datacenter.IReplicaManagement.Files.get(fl).getName()+" == "+ filename);
				if(Objects.equals(datacenter.IReplicaManagement.Files.get(fl).getName(), filename)){
					fileID = fl;
		
				}
			}


			if((fileID==9999)){
				Log.printLine("cloudletID: "+cloudletList.get(k).getCloudletId()+" Failed because it cloudn't find file: "+ filename+" in scheduling");
				CloudSim.terminateSimulation();
				pressAnyKeyToContinue();
				pressAnyKeyToContinue();
			}

			filesize = datacenter.IReplicaManagement.Files.get(fileID).getSize();
			double AveBW = datacenter.getVMBW()[i][VmSize];
			//Log.printLine("cloudletID: "+cloudletList.get(k).getCloudletId()+" VmID: "+i+" File size: "+ filesize+" VmBW: "+ AveBW);
			
			readdelay = (double) readdelay+(filesize/AveBW);
		
			//Log.printLine("cloudletID: "+cloudletList.get(k).getCloudletId()+" VmID: "+i+ " Read Energy: "+ readdelay );
		}
		temp.add(i,(int) readdelay);
	}
	VMtoCloudletReadTime.add(k, new ArrayList<Integer>(temp));
}
/*
Log.printLine();
Log.printLine("##################################  Checking ** File Transfer Time ** Checking ############################################");
for(int j=0; j< cloudletsize; j++){
Log.printLine(" "+ VMtoCloudletReadTime.get(j));
}
Log.printLine("##################################  Checking ** File Transfer Time ** Checking ############################################");
*/


for(int j=0; j< cloudletsize; j++){
temp.clear();
for (int i =0; i < (VmSize); i++){
Itemp =  (int) (VMtoCloudletExecTime.get(j).get(i)+VMtoCloudletReadTime.get(j).get(i));
//Log.printLine(" "+j+", "+i+": "+ Itemp);
temp.add(i,Itemp);
}
VMtoCloudletExecTime.set(j, new ArrayList<Integer>(temp));
}

/*
Log.printLine();
Log.printLine("##################################  Checking **Exe + File Transfer Time ** Checking ############################################");
for(int j=0; j< cloudletsize; j++){
Log.printLine(" "+ VMtoCloudletExecTime.get(j));
}
Log.printLine("##################################  Checking **Exe + File Transfer Time ** Checking ############################################");
*/


//pressAnyKeyToContinue();
//pressAnyKeyToContinue();

//Calculation total execution time


for(int j=0; j< cloudletsize; j++){
temp.clear();
for (int i =0; i < (VmSize); i++){
Itemp =  (int) (VMtoCloudletExecTime.get(j).get(i)+VMtoCloudletTranTime.get(j).get(i));
//Log.printLine(" "+j+", "+i+": "+ Itemp);
temp.add(i,Itemp);
}
VMtoCloudletTime.add( new ArrayList<Integer>(temp));
}




/*
Log.printLine();
Log.printLine("##################################  Checking ** Total execution time ** Checking ############################################");
for(int j=0; j< cloudletsize; j++){
Log.printLine(" "+ VMtoCloudletTime.get(j));
}
Log.printLine("##################################  Checking ** Total execution time ** Checking ############################################");
*/
}
}	

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////This class is for keeping Energy of each VM for executing each cloudlet ////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


public class VMtoCloudletEnergy {
public ArrayList<ArrayList<Integer>> VMtoCloudletEnergy = new ArrayList<ArrayList<Integer>>();  // Total time basde on each VM BW
public ArrayList<ArrayList<Integer>> VMtoCloudletTranEnergy = new ArrayList<ArrayList<Integer>>(); // Transmision time based on each VM
public ArrayList<ArrayList<Integer>> VMtoCloudletExecEnergy = new ArrayList<ArrayList<Integer>>(); // Total time based on MI/MIPS
public ArrayList<ArrayList<Integer>> VMtoCloudletReadWriteEnergy = new ArrayList<ArrayList<Integer>>(); // Total time based on MI/MIPS
public int MaxReadDelay =0;
public int MaxWriteDelay =0;
public int MaxEnergyPerUnitReadWrite =0;

private ArrayList<Integer> temp = new ArrayList<Integer>(); 


public void intial(int cloudletsize,List<? extends Cloudlet> cloudletList,List<? extends Vm> vmList,VMInfo IVMInfo, VMtoCloudletTime iVMtoCloudletTime, int[][] VMBW, List<Storage> Storagelist, Datacenter datacenter){
int VmSize = vmList.size();
//Calculation Execution Energy
int Itemp = 0 ;
Log.printLine();
for(int j=0; j< cloudletsize; j++){
temp.clear();
for (int i =0; i < (VmSize); i++){
Itemp = (int) ((iVMtoCloudletTime.VMtoCloudletExecTime.get(j).get(i)* IVMInfo.ExecEnergyUnit.get(j).get(i)));
//if(j==0){
	//Log.printLine(" "+j+", "+i+": "+ iVMtoCloudletTime.VMtoCloudletExecTime.get(j).get(i)+" * "+IVMInfo.ExecEnergyUnit.get(j).get(i) +" = "+Itemp);
//}
temp.add(i,Itemp);
}
VMtoCloudletExecEnergy.add( new ArrayList<Integer>(temp));
}
/*
Log.printLine();
Log.printLine("##################################  Checking ** Execution Energy ** Checking ############################################");
for(int j=0; j< cloudletsize; j++){
Log.printLine(" "+ VMtoCloudletExecEnergy.get(j));
}
Log.printLine("##################################  Checking ** Execution Energy ** Checking ############################################");
*/

//Calculation Transmission energy
//Log.printLine();
for(int j=0; j< cloudletsize; j++){
temp.clear();
for (int i =0; i < (VmSize); i++){
Itemp =  (int) (((iVMtoCloudletTime.VMtoCloudletTranTime.get(j).get(i))* IVMInfo.TransEnergyUnit.get(j).get(i)));
//Log.printLine(" "+j+", "+i+": "+ Itemp);
temp.add(i,Itemp);
}
VMtoCloudletTranEnergy.add( new ArrayList<Integer>(temp));
}
/*
Log.printLine();
Log.printLine("##################################  Checking ** Transmision Energy ** Checking ############################################");
for(int j=0; j< cloudletsize; j++){
Log.printLine(" "+ VMtoCloudletTranEnergy.get(j));
}
Log.printLine("##################################  Checking ** Transmision Energy ** Checking ############################################");
*/
// Read and write Energy
for(int j=0; j< cloudletsize; j++){
temp.clear();
for (int i =0; i < (VmSize); i++){
Itemp =  (int) (((iVMtoCloudletTime.VMtoCloudletReadTime.get(j).get(i))* datacenter.IVMInfo.AveTransEnergyUnit.get(i)));

//Log.printLine(" "+j+", "+i+": "+ Itemp);
temp.add(i,Itemp);
}
VMtoCloudletReadWriteEnergy.add( new ArrayList<Integer>(temp));
}
			

/*
Log.printLine();
Log.printLine("##################################  Checking ** File Transfer Energy ** Checking ############################################");
for(int j=0; j< cloudletsize; j++){
Log.printLine(" "+ VMtoCloudletReadWriteEnergy.get(j));
}
Log.printLine("##################################  Checking ** File Transfer Energy ** Checking ############################################");
*/


//Calculation total execution energy
//Log.printLine();
for(int j=0; j< cloudletsize; j++){
	temp.clear();
	for (int i =0; i < (VmSize); i++){
		Itemp =  (int) (VMtoCloudletExecEnergy.get(j).get(i)+ VMtoCloudletTranEnergy.get(j).get(i)+ VMtoCloudletReadWriteEnergy.get(j).get(i)); //+ VMtoCloudletReadWriteEnergy.get(j)
		//Log.printLine(" "+j+", "+i+": "+ Itemp);
		temp.add(i,Itemp);
	}
	VMtoCloudletEnergy.add( new ArrayList<Integer>(temp));
}
/*
Log.printLine();
Log.printLine("##################################  Checking ** Total execution Energy ** Checking ############################################");
for(int j=0; j< cloudletsize; j++){
Log.printLine(" "+ VMtoCloudletEnergy.get(j));
}

Log.printLine("##################################  Checking ** Total execution Energy ** Checking ############################################");
*/
}// End of Initialization

}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////// This Class is main class for keeping all cloudlet execution info. ////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class Executiontable {

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
public void ExeTableVMupdate(int Vm,int CloudletN,Executiontable IExecutiontable, VMtoCloudletEnergy IVMtoCloudletEnergy, VMInfo IVMInfo,VMtoCloudletTime IVMtoCloudletTime,VMsTimeExecution IVMsTimeExecution){
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

Deadline.set(CloudletN, (Deadline.get(CloudletN)-ExecutionTime.get(CloudletN)));
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


// After selection and assignment, Energy of VM shall be deduct and VMEtoCloudletE should updated 
double Energyt = 0.0;
Energyt = IVMInfo.Energy.get(Vm);

Energyt = Energyt - IVMtoCloudletEnergy.VMtoCloudletEnergy.get(CloudletN).get(Vm) ;
IVMInfo.Energy.set(Vm,(int) Energyt);


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

public void NPintial(int cloudletsize){
ArrayList<Integer> temp = new ArrayList<Integer>(); 
temp.add(3000);
//
//create list for later puting info
for (int j=0; j <(cloudletsize);j++){
			
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




//This function is return Estimated Start time of cloudlet with considering VMs
public double EST(int cloudlet,int Vm,double StartT, double executionT,VMsTimeExecution IVMsTimeExecution,VMtoCloudletTime IVMtoCloudletTime){
//Log.printLine(" ****===> ESTofCloudlet is equal :" +ESTofCloudlet(cloudlet,IVMEtoCloudletE,IVMtoCloudletTime));
//Log.printLine(" ****===> VMisFree is equal :" +IVMsTimeExecution.VMisFree(Vm, cloudlet, StartT, executionT, 0.0,IVMsTimeExecution.VMsSchedule2));

return IVMsTimeExecution.VMisFree(Vm, cloudlet, StartT, executionT, 0.0,IVMsTimeExecution.VMsSchedule2);
}

//This function is return Estimated finish time of cloudlet with considering VMs
public double EFT(int cloudlet,int Vm,double StartT, double executionT,VMsTimeExecution IVMsTimeExecution,VMtoCloudletTime IVMtoCloudletTime){
double temp = 0.0 ;
temp = EST(cloudlet,Vm,StartT,executionT,IVMsTimeExecution,IVMtoCloudletTime);
// Log.printLine(" ****===> EST is equal :" +temp);
//Log.printLine(" ****===> Weight is equal :" +IVMtoCloudletTime.weight.get(cloudlet));

return (temp +IVMtoCloudletTime.VMtoCloudletTime.get(cloudlet).get(Vm));
}

//This function is for Initialing the data, for finding VmCandidate and cloudletcandidate
public void Intial(int VmSize, int cloudletsize, VMtoCloudletTime IVMtoCloudletTime){
// Initial CandidateCloudletBVm for further use
for (int x =0; x < (VmSize ); x++){
ArrayList<Integer> AAA = new ArrayList<Integer>();
AAA.add(9999);
CandidateCloudletBVm.add(x, new ArrayList<Integer>(AAA));
//Log.printLine("CandidateCloudletBVm.add("+x+", "+AAA+")");
}
//initial OnVmCandidate for further use
for (int x =0;x < (cloudletsize); x++){

OneVmCandidate.add(x, 9999);

}

// Initial StartT,FinishT,Deadline,SelVM for further use
for (int x =0; x < (cloudletsize); x++){
StartTime.add(x, 0.0);
FinishTime.add(x, 0.0);
ExecutionTime.add(x, 0.0);
Deadline.add(x, 0.0);
SelVm.add(x, 0);
//Log.printLine("CandidateCloudletBVm.add("+x+", "+AAA+")");
}
/*
// Finding Vm which have enough energy to run Cloudlet and add them to CandidateVmBC and CandidateCloudletBVm
for (int Ccounter =0;Ccounter < (cloudletsize); Ccounter++){
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
*/

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
*/ 
/*
Log.printLine("##################################  Checking ** Time After intiation ** Checking ############################################");

Log.printLine("Start Time: " + StartTime);

Log.printLine("WorstCase Time: " + WorstCase);

Log.printLine("Execution Time: " + ExecutionTime);

Log.printLine("Finish Time: " + FinishTime);

Log.printLine("Deadline Time: " + Deadline);


Log.printLine("##################################  Checking ** Time After intiation ** Checking ############################################");

Log.printLine();	
*/
// this part no need in new algorithm	
}// Intialzation finished


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//This function is for checking at least there is 1 VM candidate for each Cloudlet
public Boolean Atleast1VM(int cloudletsizw){
for (int x=0; x < (cloudletsizw); x++){
if (CandidateVmBC.get(x).isEmpty()){
return false;
}
}
return true;
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//This function is for checking sum of all Vm candidate is higher than sum of average energy which need 
public Boolean EnergyIsEnough(int VmSize,int cloudletsize, VMInfo IVMInfo,VMtoCloudletEnergy IVMtoCloudletEnergy){
int VmE = 0;
int CloudletE = 0;
for (int x=0; x < (VmSize); x++){
if (!CandidateCloudletBVm.get(x).isEmpty()){
VmE = VmE + IVMInfo.Energy.get(x) ;
}
}
for (int x1 =0; x1 < (cloudletsize); x1++){
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
public void findOneCandidate(VMInfo IVMInfo,VMtoCloudletEnergy IVMtoCloudletEnergy,List<Cloudlet> cloudlet, int cloudletsize, int VmSize){
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
for (int x=1; x < cloudletsize; x++){
if ((CandidateVmBC.get(x).size() == 1)&(cloudlet.get(x).assigned)){// if there is just one item need to be assign and return true
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

if ((CandidateCloudletBVm.get(vmid).get(y) == cloudletsize) ||
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


}
