/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Datacenter.ReplicaManagement;
import org.cloudbus.cloudsim.File;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;

/**
 * CloudletSchedulerSpaceShared implements a policy of scheduling performed by a virtual machine. It
 * consider that there will be only one cloudlet per VM. Other cloudlets will be in a waiting list.
 * We consider that file transfer from cloudlets waiting happens before cloudlet execution. I.e.,
 * even though cloudlets must wait for CPU, data transfer happens as soon as cloudlets are
 * submitted.
 * 
 * @author Saurabh Kumar Garg
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 3.0
 */
public class NetworkCloudletSpaceSharedScheduler extends CloudletScheduler {

	/** The cloudlet waiting list. */
	private List<? extends ResCloudlet> cloudletWaitingList;

	/** The cloudlet exec list. */
	private List<? extends ResCloudlet> cloudletExecList;

	/** The cloudlet paused list. */
	private List<? extends ResCloudlet> cloudletPausedList;

	/** The cloudlet finished list. */
	private List<? extends ResCloudlet> cloudletFinishedList;

	/** The current CPUs. */
	protected int currentCpus;

	/** The used PEs. */
	protected int usedPes;

	// for network

	public Map<Integer, List<HostPacket>> pkttosend;

	public Map<Integer, List<HostPacket>> pktrecv;

	// added by reza
		public int counter = 0;
		List <NetworkCloudlet> ResCloudletList = new ArrayList<NetworkCloudlet>();
		
	/**
	 * Creates a new CloudletSchedulerSpaceShared object. This method must be invoked before
	 * starting the actual simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public NetworkCloudletSpaceSharedScheduler() {
		super();
		cloudletWaitingList = new ArrayList<ResCloudlet>();
		cloudletExecList = new ArrayList<ResCloudlet>();
		cloudletPausedList = new ArrayList<ResCloudlet>();
		cloudletFinishedList = new ArrayList<ResCloudlet>();
		usedPes = 0;
		currentCpus = 0;
		pkttosend = new HashMap<Integer, List<HostPacket>>();
		pktrecv = new HashMap<Integer, List<HostPacket>>();
	}
//Added by reza
	//public boolean EnergyIsEnough = true;
	//public void SetEnergyIsEnough(boolean State){
		//EnergyIsEnough = State;
	//}
	
	
	
	
	
	
	
	/**
	 * Updates the processing of cloudlets running under management of this scheduler.
	 * 
	 * @param currentTime current simulation time
	 * @param mipsShare array with MIPS share of each processor available to the scheduler
	 * @return time predicted completion time of the earliest finishing cloudlet, or 0 if there is
	 *         no next events
	 * @pre currentTime >= 0
	 * @post $none
	 */
	@Override
	public double updateVmProcessing( double currentTime, List<Double> mipsShare, int VMID, NetworkDatacenter Networkdatacenter) {
		setCurrentMipsShare(mipsShare);
		// update
		double capacity = 0.0;
		int cpus = 0;

		for (Double mips : mipsShare) { // count the CPUs available to the VMM
			capacity += mips;
			if (mips > 0) {
				cpus++;
			}
		}
		currentCpus = cpus;
		capacity /= cpus; // average capacity of each cpu
		List<ResCloudlet> toRemove = new ArrayList<ResCloudlet>(); // moved by reza	
		//added by reza for test
		/*
		if(VMID == 9){
			cloudletListPrint(VMID);
			pressAnyKeyToContinue();
			pressAnyKeyToContinue();
		}
		*/
		
		
		
		
		
		for (ResCloudlet rcl : getCloudletExecList()) { // each machine in the
			// exec list has the
			// same amount of cpu
			NetworkCloudlet cl = (NetworkCloudlet) rcl.getCloudlet();
			boolean EnergyIsEnough = true;
			// check status
			// if execution stage
			// update the cloudlet finishtime
			// CHECK WHETHER IT IS WAITING FOR THE PACKET
			// if packet received change the status of job and update the time.
			//
			if ((cl.currStagenum != -1)) {
				if (cl.currStagenum == NetworkConstants.FINISH) {
					
					break;
				}
				TaskStage st = cl.stages.get(cl.currStagenum);
				//added By Reza Send
				if (st.type == NetworkConstants.WAIT_SEND ) {
					cl.timespentInStage = Math.round(CloudSim.clock() - cl.timetostartStage);
					//Log.printLine("Current stage: "+ cl.currStagenum+" in NetworkcloudletID: "+ cl.getCloudletId()+ " ,running on VMID: "+cl.getVmId()+"  ==> wait-sending");
					//pressAnyKeyToContinue();
					//pressAnyKeyToContinue(); 
					if ((!(cl.timespentInStage< (cl.getwriteDelay())))){
						//Log.printLine("Delay of writing& sending information: "+ cl.getwriteDelay()+", Time spend in this stage: "+cl.timespentInStage );
						//Log.printLine("Sending phase, battery of VMID:"+ VMID+" is equal to:" + NetDatacenterBroker.linkDC.getVmList().get(VMID).getbattery());
						Log.printLine();
						for (int r=0; r< cl.stages.size();r++){
							Log.printLine("Sending Pkt from (VmID,cloudletID): "+ cl.getVmId()+ " , "+cl.getCloudletId()+
									" to : "+ cl.stages.get(r).peer+" , "+ cl.stages.get(r).vpeer );
						}
						Log.printLine();
						int tempvmidforsending = cl.getVmId();
						if(cl.migrated == true){
							tempvmidforsending =cl.OrginalVmID;
						}
						HostPacket pkt = new HostPacket(
								tempvmidforsending, //cl.getVmId()
								cl.stages.get(counter).peer,
								cl.stages.get(counter).data,
								CloudSim.clock(),
								-1,
								cl.getCloudletId(),
								cl.stages.get(counter).vpeer);
						List<HostPacket> pktlist = pkttosend.get(cl.getVmId());//pkttosend.get(cl.getVmId())
						Log.printLine("tempvmidforsending: "+tempvmidforsending);

						if (pktlist == null) {
							pktlist = new ArrayList<HostPacket>();
						}
						Log.printLine("pktlist size before adding: "+ pktlist.size());
						pktlist.add(pkt);
						Log.printLine("pktlist size after adding: "+ pktlist.size());

						pkttosend.put(cl.getVmId(), pktlist);
						Log.printLine("App No.: " +cl.AppNum+" ,Sending Pkt from (VmID,cloudletID): "+ pktlist.get(0).sender+ " , "+pktlist.get(0).virtualsendid+
								" to : "+ pktlist.get(0).reciever +" , "+ pktlist.get(0).virtualrecvid+", Sendtime is: "+ pktlist.get(0).sendtime+ 
								" ,Energy of VMID: "+  cl.getVmId() + " is equal to : "+ Networkdatacenter.getVmList().get(cl.getVmId()).getbattery() );
						
						if(( pkt.reciever ==1)  ){
								
								
							//pressAnyKeyToContinue();
							//pressAnyKeyToContinue();
						
						}
						//pressAnyKeyToContinue();
						//pressAnyKeyToContinue();
						
						changetonextstage(cl, st,  Networkdatacenter,VMID,toRemove,rcl);
						
					}
				}
				
				
				if (st.type == NetworkConstants.EXECUTION) {
					//Log.printLine("Current stage: "+ cl.currStagenum+" in NetworkcloudletID: "+ cl.getCloudletId()+ "execution");
					//DBMCC => updating battery in VM and Host
					// It can have better implementation because of i have limited time i just pass this for later improvement
					//Log.printLine();
					//Log.printLine();
					//Log.printLine();
					//Log.printLine("____________________ Deducting energy from VM ------------------------");
					//added by reza
					
					double DelayOfRead =cl.getReadDelay();
					if(!cl.getReadCalDone()){
						//This part shall cal
						Log.printLine("This is file's read time by Cloudlet ID: "+cl.getCloudletId()+ "on VMID: "+ VMID);
						//pressAnyKeyToContinue();
						//pressAnyKeyToContinue();
						DelayOfRead =CalculateReadDelay(cl.getRequiredFiles(),cl,NetDatacenterBroker.linkDC.getVmList().get(VMID),NetDatacenterBroker.linkDC.IReplicaManagement,NetDatacenterBroker.linkDC);
						if (DelayOfRead==-1){
							EnergyIsEnough=false;
						}
						if(EnergyIsEnough==true){
						cl.SetReadDelay(DelayOfRead);
						cl.setReadCalDone(true);
						// fixing time shall spend in new VMID
						double passtime = cl.timespentInStage;
						cl.ReadDelayForEnergyDeductionPassing = DelayOfRead + passtime;

						Log.printLine("time spend in this stage: "+ cl.timespentInStage);
						double newtime = cl.VMtoCloudletTime.get(VMID);
						Log.printLine("new time: "+ newtime);
						Log.printLine("remining of progress: "+ (1-cl.ProgressPersentage));
						double reminingtime = (newtime*(1-cl.ProgressPersentage));
						Log.printLine("remining of reminingtime: "+ reminingtime);
						//st.time = st.time - (passtime - newpasstime);
						st.time = passtime + reminingtime;
						Log.printLine(" Time of stage (passtime + reminingtime): "+ st.time );
						Log.printLine("Read Delay : "+ cl.getReadDelay());
						//new time is fixed
						st.time = st.time + DelayOfRead;
						Log.printLine("Clock: "+ CloudSim.clock()+" ,total Time of stage : "+ st.time );
						Log.printLine("Energy of VMID: "+ NetDatacenterBroker.linkDC.getVmList().get(VMID).getbattery());
						Log.printLine("Deducted Energy: "+cl.getExecutionEnergy());
						//pressAnyKeyToContinue();
						//pressAnyKeyToContinue();
						}
					}
					
					double battery = NetDatacenterBroker.linkDC.getVmList().get(VMID).getbattery();
					//Log.printLine(" battery: "+battery);
					double executiontime = cl.timespentInStage;
					//Log.printLine(" cl.timespentInStage: "+cl.timespentInStage);

					//Log.printLine(" estimatedFinishTime: "+estimatedFinishTime);

					//Log.printLine(" remainingLength: "+remainingLength);

					if (rcl.getestimatedFinishTime() == 0.0 ){
						executiontime = 0.000 ;
					} else {
					executiontime = -(executiontime -( Math.round(CloudSim.clock() - cl.timetostartStage)));
					}
					//Log.printLine(" executiontime: "+executiontime);

					int EnrgyPerTimeUnit = 0;
					EnrgyPerTimeUnit = NetDatacenterBroker.linkDC.IVMInfo.ExecEnergyUnit.get(VMID);
							//(NetDatacenterBroker.linkDC.getVmList().get(VMID).getEnrgyPerTimeUnit());
					//Log.printLine(" EnrgyPerTimeUnit: "+EnrgyPerTimeUnit);

					
					battery = battery - (executiontime * (EnrgyPerTimeUnit));
					// if here is "-" we should migrate the task
					
					if(battery<0.0){
						EnergyIsEnough =false;
					}
					
					// Mig test area
					
					//if((cl.getCloudletId()== 0)&&(VMID == 11)&&(cl.migrated == false)&&((Math.round(CloudSim.clock() - cl.timetostartStage))>7)){
					//	EnergyIsEnough =false;
					//}
					//if((cl.getCloudletId()== 1)&&(VMID == 0)&&(cl.migrated == false)&&((Math.round(CloudSim.clock() - cl.timetostartStage))>50)){
					//	EnergyIsEnough =false;
					//}
					
					
   					if ((!EnergyIsEnough)){
						//if(!cl.migrated){
						Log.printLine("Cloudlet Id: "+rcl.getCloudletId()+ " assign to VMID: "+ VMID+", Migrate");
						MigFun(Networkdatacenter,ResCloudletList,cl,toRemove,rcl,VMID);
						//for(int oo=0;oo <toRemove.size();oo++ ){
						//	Log.printLine("list which needed to be remove: " + toRemove.get(oo).getCloudletId());
						//}
						Log.printLine("time spend in this stage: "+ cl.timespentInStage);
						
						//pressAnyKeyToContinue();
						//pressAnyKeyToContinue();
						//}
					} else {
					
						
					if (cl.timespentInStage > cl.ReadDelayForEnergyDeductionPassing){	
					//Log.printLine(" battery: "+battery);
					cl.AddExecutionEnergy((long) (executiontime * (EnrgyPerTimeUnit)));
					//rcl.setestimatedFinishTime(estimatedFinishTime);
					NetDatacenterBroker.linkDC.getVmList().get(VMID).setbattery((long)(battery));
					
					Log.printLine("Clock: "+CloudSim.clock()+" ,Execution stage, battery of VMID:"+ VMID+" is equal to:" + NetDatacenterBroker.linkDC.getVmList().get(VMID).getbattery()
							+ "Deducted energy: "+ cl.getExecutionEnergy());
					
					
					}
					///Log.printLine("______________________ Deducting energy from VM ------------------------");
					//Log.printLine();
					//Log.printLine();
					//Log.printLine();
					
					// update the time
					cl.timespentInStage = Math.round(CloudSim.clock() - cl.timetostartStage);
					
					// it will be used for migration
					cl.ProgressPersentage = ((cl.timespentInStage-cl.getReadDelay())/(st.time-cl.getReadDelay()));
					
					if (cl.timespentInStage >= st.time) {

						//BDMCC: access probability 
						/*
						 * In this part we define access probability when node finish task shall have coverage to send result to next node/nodes
						 */
						
						Random rand = new Random();
						if (rand.nextDouble() < 0.0) { // <-- 10% of the time.
							// it means system is going to send but dont have access need to reset process in new node
							Log.printLine("VMID: "+VMID+" is not reachable");
							
							
						}else{
							Log.printLine("App No.: "+ cl.AppNum+" ,CloudletID: "+cl.getCloudletId()+" Execution phase finished and battery of VMID:"+ VMID
									+" is equal to:" + NetDatacenterBroker.linkDC.getVmList().get(VMID).getbattery()+" Clock : "+ CloudSim.clock());
							/*
							if(true){
								cloudletListPrint(VMID);
								pressAnyKeyToContinue();
								pressAnyKeyToContinue();
							}
							*/
							/*
							if (NetDatacenterBroker.linkDC.getVmList().get(VMID).getbattery() < 0){
								Log.printLine("battery of VMID:"+ VMID+" is not enough");
								Log.printLine("**********************       FAIL         *******************");
								CloudSim.terminateSimulation();
							}
							*/
							changetonextstage(cl, st,  Networkdatacenter,VMID,toRemove,rcl);
							// change the stage
							
						}
						
					}
						
						
					}
				}
				if (st.type == NetworkConstants.WAIT_RECV) {
					//Log.printLine("Current stage: "+ cl.currStagenum+" in NetworkcloudletID: "+ cl.getCloudletId()+ "wait-recv");
										
					cl.timespentInStage = Math.round(CloudSim.clock() - cl.timetostartStage);
					
					if (!(cl.timespentInStage< st.time)){
						
					Log.printLine("App No.: " +cl.AppNum+ " ,VMID: "+VMID+" ,cloudletID: "+ cl.getCloudletId()+ " ,recieve from VMID: "
							+ st.peer + " with cloudletID: "+ st.vpeer + " ,Energy of VM" + Networkdatacenter.getVmList().get(VMID).getbattery());					
					//cloudletListPrint(VMID);
								
					
					List<HostPacket> pktlist = pktrecv.get(st.peer);
					/*
					Log.printLine("Size of pktrecv: "+ pktrecv.size());
					if(pktlist != null){
						Log.printLine("Sender: "+ st.peer+" ,Size of pktlist: "+ pktlist.size());
						for(int i=0; i < pktlist.size(); i++){
							Log.printLine("counter: "+ i+" ,pktlist virtualreciever: "+ pktlist.get(i).virtualrecvid);
						}
					}
					*/
					List<HostPacket> pkttoremove = new ArrayList<HostPacket>();
					if (pktlist != null) {
						Iterator<HostPacket> it = pktlist.iterator();
						HostPacket pkt = null;
						if (it.hasNext()) {
							pkt = it.next();
							// Asumption packet will not arrive in the same cycle
							if ((pkt.reciever == cl.getVmId()) ){//(pkt.reciever == cl.getVmId()) orginal
								pkt.recievetime = CloudSim.clock();
								st.time = CloudSim.clock() - pkt.sendtime;
								Log.printLine("App No.: " +cl.AppNum+ " ,VMID: "+VMID+" ,cloudletID: "+ cl.getCloudletId()+ " ,recieve from VMID: "
										+ pkt.sender+ " with cloudletID: "+ pkt.virtualsendid );
								/*
								if(cl.getVmId() == 1){//(cl.getCloudletId() == 10) (cl.AppNum == 7)&&(pkt.virtualsendid == 10 ), pkt.sender == 17
									pressAnyKeyToContinue();
									pressAnyKeyToContinue();
								}
								*/
								changetonextstage(cl, st,  Networkdatacenter,VMID,toRemove,rcl);
								pkttoremove.add(pkt);
							}
						}
						pktlist.removeAll(pkttoremove);
						// if(pkt!=null)
						// else wait for recieving the packet
					}
				}
				}

			} else {
				cl.currStagenum = 0;
				cl.timetostartStage = CloudSim.clock();
				
				if (cl.stages.get(0).type == NetworkConstants.EXECUTION) {
					
					NetDatacenterBroker.linkDC.schedule(
							NetDatacenterBroker.linkDC.getId(),
							cl.stages.get(0).time,
							CloudSimTags.VM_DATACENTER_EVENT);
				} else {
					NetDatacenterBroker.linkDC.schedule(
							NetDatacenterBroker.linkDC.getId(),
							0.0001,
							CloudSimTags.VM_DATACENTER_EVENT);
					// /sendstage///
				}
			}

		
		}
		if (getCloudletExecList().size() == 0 && getCloudletWaitingList().size() == 0) { // no
			// more cloudlets in this scheduler
			setPreviousTime(currentTime);
			return 0.0;
		}

		// update each cloudlet
		// changed by reza
		int finished =0;
		int checking = 0;
		Vm vm1 = Networkdatacenter.getVmList().get(VMID);
		if(vm1.migrate == true){
			finished = vm1.NoMigCuncurent;
			vm1.NoMigCuncurent =0;
			Networkdatacenter.getVmList().get(VMID).migrate = false;
			checking = 1;
			
		} else {
		finished = 0;
		}
		
		//List<ResCloudlet> toRemove = new ArrayList<ResCloudlet>();
		for (ResCloudlet rcl : getCloudletExecList()) {
			// rounding issue...
			if (((NetworkCloudlet) (rcl.getCloudlet())).currStagenum == NetworkConstants.FINISH) {
				// stage is changed and packet to send
				((NetworkCloudlet) (rcl.getCloudlet())).finishtime = CloudSim.clock();
				toRemove.add(rcl);
				cloudletFinish(rcl);
				finished++;
			}
		}
		getCloudletExecList().removeAll(toRemove);
		// add all the CloudletExecList in waitingList.
		// sort the waitinglist

		// for each finished cloudlet, add a new one from the waiting list
		if (!getCloudletWaitingList().isEmpty()) {
			for (int i = 0; i < finished; i++) {
				toRemove.clear();
				for (ResCloudlet rcl : getCloudletWaitingList()) {
					if ((currentCpus - usedPes) >= rcl.getNumberOfPes()) {
						rcl.setCloudletStatus(Cloudlet.INEXEC);
						for (int k = 0; k < rcl.getNumberOfPes(); k++) {
							rcl.setMachineAndPeId(0, i);
						}
						getCloudletExecList().add(rcl);
						usedPes += rcl.getNumberOfPes();
						toRemove.add(rcl);
						break;
					}
				}
				getCloudletWaitingList().removeAll(toRemove);
			}// for(cont)
		}

		// estimate finish time of cloudlets in the execution queue
		double nextEvent = Double.MAX_VALUE;
		for (ResCloudlet rcl : getCloudletExecList()) {
			double remainingLength = rcl.getRemainingCloudletLength();
			double estimatedFinishTime = currentTime + (remainingLength / (capacity * rcl.getNumberOfPes()));
			rcl.setestimatedFinishTime(estimatedFinishTime);
			/////////////////////////////
			
			if (estimatedFinishTime - currentTime < CloudSim.getMinTimeBetweenEvents()) {
				estimatedFinishTime = currentTime + CloudSim.getMinTimeBetweenEvents();
			}
			if (estimatedFinishTime < nextEvent) {
				nextEvent = estimatedFinishTime;
			}
		}
		setPreviousTime(currentTime);
		/*
		if(checking == 1){
			checking = 0;
			Log.print("After migration: ");
			cloudletListPrint(VMID);
			pressAnyKeyToContinue();
			pressAnyKeyToContinue();
		}*/
		return nextEvent;
	}

	public void MigFun(NetworkDatacenter networkdatacenter, List<NetworkCloudlet> resCloudletList2, NetworkCloudlet cl,
			List<ResCloudlet> toRemove, ResCloudlet rcl, int VMID) {
		
		NetworkCloudlet cltemp = (NetworkCloudlet) rcl.getCloudlet();
		networkdatacenter.migration.add(cltemp);
		
		ResCloudletList.add(cl);
		
		if (cl.migrated == false){
			cl.OrginalVmID = VMID;
		}
		cl.PreVMID = VMID;
		cl.migrated = true;
		cl.setReadCalDone(false);
		//cl.currStagenum = NetworkConstants.FINISH;
		//migrateCloudlet();
		//toRemove.add(rcl);
		//cl.currStagenum = NetworkConstants.FINISH;
		//pressAnyKeyToContinue();
		//pressAnyKeyToContinue(); 
	}

	private void changetonextstage(NetworkCloudlet cl, TaskStage st, NetworkDatacenter Networkdatacenter, int VMID, List<ResCloudlet> toRemove, ResCloudlet rcl) {
		cl.timespentInStage = 0;
		cl.timetostartStage = CloudSim.clock();
		int currstage = cl.currStagenum;
		if (currstage >= (cl.stages.size() - 1)) {
			cl.currStagenum = NetworkConstants.FINISH;
		} else {
			cl.currStagenum = currstage + 1;
			int i = 0;
			for (i = cl.currStagenum; i < cl.stages.size(); i++) {
				
				if (cl.stages.get(i).type == NetworkConstants.WAIT_SEND) {
					
					//Log.printLine("Current stage: "+ cl.currStagenum+" in NetworkcloudletID: "+ cl.getCloudletId()+ "sending");
					//added by reza
					
					if(cl.getFileForWrite() || (st.time>0.0)){
					cl.timespentInStage = Math.round(CloudSim.clock() - cl.timetostartStage);
					// calculate write delay
					//NetworkDatacenter.this.getStorageList();
					long DelayofWrite =0;
					DelayofWrite = 0;//WritePolicy(cl, st,  Networkdatacenter,Networkdatacenter.IReplicaManagement);
					
					/// Deducting Energy of sending energy
					double battery = NetDatacenterBroker.linkDC.getVmList().get(VMID).getbattery();
					double EnrgyPerTimeUnit = 0;
					EnrgyPerTimeUnit = (NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(VMID));
					Log.printLine("Energy of Trans. unit: "+EnrgyPerTimeUnit+ ", Trans Delay is: "+cl.stages.get(i).time + 
									", Trans Energy: "+(cl.stages.get(i).time * (EnrgyPerTimeUnit)));
					if(!(VMID == cl.stages.get(i).peer)){// checking source and destination
						Log.printLine("Source and destination are same");
						battery = battery - (cl.stages.get(i).time * (EnrgyPerTimeUnit));
					}
										
					cl.SetTransDelay((long) cl.stages.get(i).time);
					cl.SetTransEnergy((long) (cl.stages.get(i).time * (EnrgyPerTimeUnit)));					
					NetDatacenterBroker.linkDC.getVmList().get(VMID).setbattery((long)(battery));
					
					//Log.printLine("Delay of write: "+ (DelayofWrite));
					cl.SetWriteDelay((long) (DelayofWrite ));//+cl.stages.get(i).time
					cl.getCreateFile();
					Log.printLine("Current stage: "+ cl.currStagenum+" in NetworkcloudletID: "+ cl.getCloudletId()+ ", total delay is: "+ DelayofWrite+cl.stages.get(i).time);
					counter = i;
					Log.printLine("Counter is equal to :"+ counter + " , in cloudletID: "+cl.getCloudletId());
					//pressAnyKeyToContinue();
					//pressAnyKeyToContinue();
					break;
					
					}else {
						int tempvmidforsending = cl.getVmId();
						if(cl.migrated == true){
							tempvmidforsending =cl.OrginalVmID;
						}
						HostPacket pkt = new HostPacket(
								tempvmidforsending,
								cl.stages.get(i).peer,
								cl.stages.get(i).data,
								CloudSim.clock(),
								-1,
								cl.getCloudletId(),
								cl.stages.get(i).vpeer);
						List<HostPacket> pktlist = pkttosend.get(cl.getVmId());
						if (pktlist == null) {
							pktlist = new ArrayList<HostPacket>();
						}
						pktlist.add(pkt);
						pkttosend.put(cl.getVmId(), pktlist);
						Log.printLine("App No.:" +cl.AppNum+" ,Sending Pkt from (VmID,cloudletID): "+ pkt.sender+ " , "+pkt.virtualsendid+
								" to : "+ pkt.reciever +" , "+ pkt.virtualrecvid+", Sendtime is: "+ pkt.sendtime);
						/*
						if(true){
							pressAnyKeyToContinue();
							pressAnyKeyToContinue();
						}
					*/
						
					}
					
					
					
					
					
					
					
				
				}else {
					break;
				}
				//send = 0;
			}
			
			NetDatacenterBroker.linkDC.schedule(
					NetDatacenterBroker.linkDC.getId(),
					0.0001,
					CloudSimTags.VM_DATACENTER_EVENT);
			if (i == cl.stages.size()) {
				cl.currStagenum = NetworkConstants.FINISH;
			} else {
				cl.currStagenum = i;
				if (cl.stages.get(i).type == NetworkConstants.EXECUTION) {
					NetDatacenterBroker.linkDC.schedule(
							NetDatacenterBroker.linkDC.getId(),
							cl.stages.get(i).time,
							CloudSimTags.VM_DATACENTER_EVENT);
				}

			//}
		}
			
		
		}

	}
// Added by Reza

	private long WritePolicy(NetworkCloudlet cl, TaskStage st, NetworkDatacenter networkdatacenter, ReplicaManagement IReplicaManagement2) {
		double Writedelay =0.0;
		//Log.printLine("Before Writing Ploicy ");
		//IReplicaManagement2.ReplicaTest(IReplicaManagement2);
		List<String> requiredFiles;
		Iterator<File> iter = cl.getCreateFile().iterator();
		double deductedenergy = 0;
		while (iter.hasNext()) {
			int VmID;
			int HostFileID;
			int StorageHostID;
			File file = iter.next();
			VmID = cl.getVmId();
			HostFileID = networkdatacenter.getVmList().get(VmID).getHost().getId();
			File master = file.makeMasterCopy();
			
			double VmEnergy;
			double TransferEnergyPerUnit;
			double temp = 9999999999999999.9;
			double CompMetric;
			int SelectedVm =999999999;
			double BW;
			
			for(int j=0; j < NetDatacenterBroker.linkDC.getVmList().size();j++){
				 VmID = j;
				 VmEnergy = NetDatacenterBroker.linkDC.getVmList().get(VmID).getbattery();
				 TransferEnergyPerUnit = NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(VmID);									
				 BW = NetDatacenterBroker.linkDC.IVMInfo.BW.get(VmID);
				 CompMetric = (VmEnergy*TransferEnergyPerUnit)/BW;
				 
				 if (temp > CompMetric){
					 temp = CompMetric;
					 SelectedVm = VmID;
				 }
			}
			
			int pickedS = SelectedVm;
			//Master Copy
			networkdatacenter.getStorageList().get(pickedS).addFile(master);
			StorageHostID = networkdatacenter.getStorageList().get(pickedS).getHostID();
			if(HostFileID == StorageHostID){
				// local filesize/transrate
				Writedelay += file.getSize()/ networkdatacenter.getStorageList().get(pickedS).getAvailableSpace();
				deductedenergy = 0;
				deductedenergy = NetDatacenterBroker.linkDC.getVmList().get(VmID).getbattery();
				deductedenergy = deductedenergy - (master.getSize()/networkdatacenter.getVmList().get(VmID).getBw())*networkdatacenter.getStorageList().get(pickedS).getEnergypernuit();
				NetDatacenterBroker.linkDC.getVmList().get(VmID).setbattery((long) deductedenergy);
			}else {
				// Global Write filesize/BW
				int VmS;
				int VmD;
				VmS = networkdatacenter.getVmList().get(VmID).getId();
				VmD = NetDatacenterBroker.linkDC.getHostList().get(StorageHostID).getVmList().get(0).getId();
				Writedelay += file.getSize()/networkdatacenter.getVMBW()[VmS][VmD];
				//Deducting Energy
				deductedenergy = 0;
				deductedenergy = NetDatacenterBroker.linkDC.getVmList().get(VmID).getbattery();
				deductedenergy = deductedenergy - (master.getSize()/networkdatacenter.getVmList().get(VmID).getBw())*NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(VmID);
				NetDatacenterBroker.linkDC.getVmList().get(VmID).setbattery((long) deductedenergy);
			}
			
		}
		//IReplicaManagement2.Maintain(NetDatacenterBroker.linkDC, CloudSim.clock());
		
		//Log.printLine("end of Writing Ploicy ");
		//IReplicaManagement2.ReplicaTest(IReplicaManagement2);
		return (long)Writedelay;
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


	/**
	 * Cancels execution of a cloudlet.
	 * 
	 * @param cloudletId ID of the cloudlet being cancealed
	 * @return the canceled cloudlet, $null if not found
	 * @pre $none
	 * @post $none
	 */
	@Override
	public Cloudlet cloudletCancel(int cloudletId, int AppNo) {
		// First, looks in the finished queue
		for (ResCloudlet rcl : getCloudletFinishedList()) {
			if ((rcl.getCloudletId() == cloudletId)&&(rcl.getCloudlet().AppNum == AppNo)) {
				usedPes -= rcl.getNumberOfPes();
				getCloudletFinishedList().remove(rcl);
				pressAnyKeyToContinue();
				pressAnyKeyToContinue();
				return rcl.getCloudlet();
			}
		}

		// Then searches in the exec list
		for (ResCloudlet rcl : getCloudletExecList()) {
			if ((rcl.getCloudletId() == cloudletId)&&(rcl.getCloudlet().AppNum == AppNo)) {
				usedPes -= rcl.getNumberOfPes();
				getCloudletExecList().remove(rcl);
				if (rcl.getRemainingCloudletLength() == 0.0) {
					cloudletFinish(rcl);
				} else {
					rcl.setCloudletStatus(Cloudlet.CANCELED);
				}
				return rcl.getCloudlet();
			}
		}

		// Now, looks in the paused queue
		for (ResCloudlet rcl : getCloudletPausedList()) {
			if ((rcl.getCloudletId() == cloudletId)&&(rcl.getCloudlet().AppNum == AppNo)){
				usedPes -= rcl.getNumberOfPes();
				getCloudletPausedList().remove(rcl);
				pressAnyKeyToContinue();
				pressAnyKeyToContinue();
				return rcl.getCloudlet();
			}
		}

		// Finally, looks in the waiting list
		for (ResCloudlet rcl : getCloudletWaitingList()) {
			if ((rcl.getCloudletId() == cloudletId)&&(rcl.getCloudlet().AppNum == AppNo)) {
				rcl.setCloudletStatus(Cloudlet.CANCELED);
				usedPes -= rcl.getNumberOfPes();
				getCloudletWaitingList().remove(rcl);
				pressAnyKeyToContinue();
				pressAnyKeyToContinue();
				return rcl.getCloudlet();
			}
		}

		return null;

	}


	// Added by reza for checking list of cloudlet
	@Override
	public void cloudletListPrint(int VMID) {
		// First, looks in the finished queue
		Log.printLine("Print Cloudlet List on VMID: "+ VMID);
		Log.printLine("Finished Cloudlet List: ");
		Log.print("CloudletID: ");
		for (ResCloudlet rcl : getCloudletFinishedList()) {
			Log.print("("+rcl.getCloudletId()+" , "+ rcl.getCloudlet().AppNum+") , ");
		}
		Log.printLine();
		// Then searches in the exec list
		Log.printLine("Execution Cloudlet List: ");
		Log.print("CloudletID: ");
		for (ResCloudlet rcl : getCloudletExecList()) {
			Log.print("("+rcl.getCloudletId()+" , "+ rcl.getCloudlet().AppNum+") , ");
		}
		Log.printLine();
		// Now, looks in the paused queue
		Log.printLine("Paused Cloudlet List: ");
		Log.print("CloudletID: ");
		for (ResCloudlet rcl : getCloudletPausedList()) {
			Log.print("("+rcl.getCloudletId()+" , "+ rcl.getCloudlet().AppNum+") , ");
		}
		Log.printLine();
		// Finally, looks in the waiting list
		Log.printLine("Waiting Cloudlet List: ");
		Log.print("CloudletID: ");
		for (ResCloudlet rcl : getCloudletWaitingList()) {
			Log.print("("+rcl.getCloudletId()+" , "+ rcl.getCloudlet().AppNum+") , ");
		}
		Log.printLine();
		

	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Pauses execution of a cloudlet.
	 * 
	 * @param cloudletId ID of the cloudlet being paused
	 * @return $true if cloudlet paused, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	@Override
	public boolean cloudletPause(int cloudletId) {
		boolean found = false;
		int position = 0;

		// first, looks for the cloudlet in the exec list
		for (ResCloudlet rcl : getCloudletExecList()) {
			if (rcl.getCloudletId() == cloudletId) {
				found = true;
				break;
			}
			position++;
		}

		if (found) {
			// moves to the paused list
			ResCloudlet rgl = getCloudletExecList().remove(position);
			if (rgl.getRemainingCloudletLength() == 0.0) {
				cloudletFinish(rgl);
			} else {
				rgl.setCloudletStatus(Cloudlet.PAUSED);
				getCloudletPausedList().add(rgl);
			}
			return true;

		}

		// now, look for the cloudlet in the waiting list
		position = 0;
		found = false;
		for (ResCloudlet rcl : getCloudletWaitingList()) {
			if (rcl.getCloudletId() == cloudletId) {
				found = true;
				break;
			}
			position++;
		}

		if (found) {
			// moves to the paused list
			ResCloudlet rgl = getCloudletWaitingList().remove(position);
			if (rgl.getRemainingCloudletLength() == 0.0) {
				cloudletFinish(rgl);
			} else {
				rgl.setCloudletStatus(Cloudlet.PAUSED);
				getCloudletPausedList().add(rgl);
			}
			return true;

		}

		return false;
	}

	/**
	 * Processes a finished cloudlet.
	 * 
	 * @param rcl finished cloudlet
	 * @pre rgl != $null
	 * @post $none
	 */
	@Override
	public void cloudletFinish(ResCloudlet rcl) {
		rcl.setCloudletStatus(Cloudlet.SUCCESS);
		rcl.finalizeCloudlet();
		getCloudletFinishedList().add(rcl);
		usedPes -= rcl.getNumberOfPes();
	}

	/**
	 * Resumes execution of a paused cloudlet.
	 * 
	 * @param cloudletId ID of the cloudlet being resumed
	 * @return $true if the cloudlet was resumed, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	@Override
	public double cloudletResume(int cloudletId) {
		boolean found = false;
		int position = 0;

		// look for the cloudlet in the paused list
		for (ResCloudlet rcl : getCloudletPausedList()) {
			if (rcl.getCloudletId() == cloudletId) {
				found = true;
				break;
			}
			position++;
		}

		if (found) {
			ResCloudlet rcl = getCloudletPausedList().remove(position);

			// it can go to the exec list
			if ((currentCpus - usedPes) >= rcl.getNumberOfPes()) {
				rcl.setCloudletStatus(Cloudlet.INEXEC);
				for (int i = 0; i < rcl.getNumberOfPes(); i++) {
					rcl.setMachineAndPeId(0, i);
				}

				long size = rcl.getRemainingCloudletLength();
				size *= rcl.getNumberOfPes();
				rcl.getCloudlet().setCloudletLength(size);

				getCloudletExecList().add(rcl);
				usedPes += rcl.getNumberOfPes();

				// calculate the expected time for cloudlet completion
				double capacity = 0.0;
				int cpus = 0;
				for (Double mips : getCurrentMipsShare()) {
					capacity += mips;
					if (mips > 0) {
						cpus++;
					}
				}
				currentCpus = cpus;
				capacity /= cpus;

				long remainingLength = rcl.getRemainingCloudletLength();
				double estimatedFinishTime = CloudSim.clock()
						+ (remainingLength / (capacity * rcl.getNumberOfPes()));

				return estimatedFinishTime;
			} else {// no enough free PEs: go to the waiting queue
				rcl.setCloudletStatus(Cloudlet.QUEUED);

				long size = rcl.getRemainingCloudletLength();
				size *= rcl.getNumberOfPes();
				rcl.getCloudlet().setCloudletLength(size);

				getCloudletWaitingList().add(rcl);
				return 0.0;
			}

		}

		// not found in the paused list: either it is in in the queue, executing
		// or not exist
		return 0.0;

	}

	/**
	 * Receives an cloudlet to be executed in the VM managed by this scheduler.
	 * 
	 * @param cloudlet the submited cloudlet
	 * @param fileTransferTime time required to move the required files from the SAN to the VM
	 * @return expected finish time of this cloudlet, or 0 if it is in the waiting queue
	 * @pre gl != null
	 * @post $none
	 */
	@Override
	public double cloudletSubmit(Cloudlet cloudlet, double fileTransferTime) {
		// it can go to the exec list
		if ((currentCpus - usedPes) >= cloudlet.getNumberOfPes()) {
			ResCloudlet rcl = new ResCloudlet(cloudlet);
			rcl.setCloudletStatus(Cloudlet.INEXEC);
			for (int i = 0; i < cloudlet.getNumberOfPes(); i++) {
				rcl.setMachineAndPeId(0, i);
			}

			getCloudletExecList().add(rcl);
			//Log.printLine("CloudletID: "+ rcl.getCloudletId()+ " added in ExecutionList");
			usedPes += cloudlet.getNumberOfPes();
		} else {// no enough free PEs: go to the waiting queue
			ResCloudlet rcl = new ResCloudlet(cloudlet);
			rcl.setCloudletStatus(Cloudlet.QUEUED);
			getCloudletWaitingList().add(rcl);
			//Log.printLine("CloudletID: "+ rcl.getCloudletId()+ " added in QUEUED");

			return 0.0;
		}

		// calculate the expected time for cloudlet completion
		double capacity = 0.0;
		int cpus = 0;
		for (Double mips : getCurrentMipsShare()) {
			capacity += mips;
			if (mips > 0) {
				cpus++;
			}
		}

		currentCpus = cpus;
		capacity /= cpus;

		// use the current capacity to estimate the extra amount of
		// time to file transferring. It must be added to the cloudlet length
		double extraSize = capacity * fileTransferTime;
		long length = cloudlet.getCloudletLength();
		length += extraSize;
		cloudlet.setCloudletLength(length);
		
		return cloudlet.getCloudletLength() / capacity;
	}

	@Override
	public int MigCloudletSubmit(Cloudlet cloudlet) {
		// it can go to the exec list
		
			ResCloudlet rcl = new ResCloudlet(cloudlet);
			int ReturnTemp = 0;
			for(int i=0; i < getCloudletExecList().size(); i++){
				if(getCloudletExecList().get(i).getCloudlet().AppNum  >= cloudlet.AppNum){
					ReturnTemp = 1;
				}
				
			}
			
			
			//if(getCloudletExecList().isEmpty()){
				//if ((currentCpus - usedPes) >= rcl.getNumberOfPes()) {
					rcl.setCloudletStatus(Cloudlet.INEXEC);
					for (int k = 0; k < rcl.getNumberOfPes(); k++) {
						rcl.setMachineAndPeId(0, 0);// we always have one mochine because of that second parameter is 0
					}
					
					getCloudletExecList().add(rcl);
					
					usedPes += rcl.getNumberOfPes();
					
				//}
				
				return 1;
				/*
			} else {
				
				if (ReturnTemp == 1){
					
					rcl.setCloudletStatus(Cloudlet.INEXEC);
					for (int k = 0; k < rcl.getNumberOfPes(); k++) {
						rcl.setMachineAndPeId(0, 0);// we always have one mochine because of that second parameter is 0
					}
					getCloudletExecList().add(rcl);
					usedPes += rcl.getNumberOfPes();
					return 0;
				} else { 
					
					rcl.setCloudletStatus(Cloudlet.QUEUED);
					getCloudletWaitingList().add(0, rcl);
					//getCloudletWaitingList().add(rcl);
					return 0;
				}
				
			}

			*/
			
		

		
		
		
	}

	
	
	
	
	
	
	
	/*
	 * (non-Javadoc)
	 * @see cloudsim.CloudletScheduler#cloudletSubmit(cloudsim.Cloudlet)
	 */
	@Override
	public double cloudletSubmit(Cloudlet cloudlet) {
		cloudletSubmit(cloudlet, 0);
		return 0;
	}

	/**
	 * Gets the status of a cloudlet.
	 * 
	 * @param cloudletId ID of the cloudlet
	 * @return status of the cloudlet, -1 if cloudlet not found
	 * @pre $none
	 * @post $none
	 */
	@Override
	public int getCloudletStatus(int cloudletId) {
		for (ResCloudlet rcl : getCloudletExecList()) {
			if (rcl.getCloudletId() == cloudletId) {
				return rcl.getCloudletStatus();
			}
		}

		for (ResCloudlet rcl : getCloudletPausedList()) {
			if (rcl.getCloudletId() == cloudletId) {
				return rcl.getCloudletStatus();
			}
		}

		for (ResCloudlet rcl : getCloudletWaitingList()) {
			if (rcl.getCloudletId() == cloudletId) {
				return rcl.getCloudletStatus();
			}
		}

		return -1;
	}

	/**
	 * Get utilization created by all cloudlets.
	 * 
	 * @param time the time
	 * @return total utilization
	 */
	@Override
	public double getTotalUtilizationOfCpu(double time) {
		double totalUtilization = 0;
		for (ResCloudlet gl : getCloudletExecList()) {
			totalUtilization += gl.getCloudlet().getUtilizationOfCpu(time);
		}
		return totalUtilization;
	}

	/**
	 * Informs about completion of some cloudlet in the VM managed by this scheduler.
	 * 
	 * @return $true if there is at least one finished cloudlet; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	@Override
	public boolean isFinishedCloudlets() {
		return getCloudletFinishedList().size() > 0;
	}

	/**
	 * Returns the next cloudlet in the finished list, $null if this list is empty.
	 * 
	 * @return a finished cloudlet
	 * @pre $none
	 * @post $none
	 */
	@Override
	public Cloudlet getNextFinishedCloudlet() {
		if (getCloudletFinishedList().size() > 0) {
			return getCloudletFinishedList().remove(0).getCloudlet();
		}
		return null;
	}

	/**
	 * Returns the number of cloudlets runnning in the virtual machine.
	 * 
	 * @return number of cloudlets runnning
	 * @pre $none
	 * @post $none
	 */
	@Override
	public int runningCloudlets() {
		return getCloudletExecList().size();
	}

	/**
	 * Returns one cloudlet to migrate to another vm.
	 * 
	 * @return one running cloudlet
	 * @pre $none
	 * @post $none
	 */
	@Override
	public Cloudlet migrateCloudlet() {
		ResCloudlet rcl = getCloudletExecList().remove(0);
		rcl.finalizeCloudlet();
		Cloudlet cl = rcl.getCloudlet();
		usedPes -= cl.getNumberOfPes();
		return cl;
	}

	/**
	 * Gets the cloudlet waiting list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet waiting list
	 */
	@SuppressWarnings("unchecked")
	protected <T extends ResCloudlet> List<T> getCloudletWaitingList() {
		return (List<T>) cloudletWaitingList;
	}

	/**
	 * Cloudlet waiting list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletWaitingList the cloudlet waiting list
	 */
	protected <T extends ResCloudlet> void cloudletWaitingList(List<T> cloudletWaitingList) {
		this.cloudletWaitingList = cloudletWaitingList;
	}

	/**
	 * Gets the cloudlet exec list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet exec list
	 */
	@SuppressWarnings("unchecked")
	protected <T extends ResCloudlet> List<T> getCloudletExecList() {
		return (List<T>) cloudletExecList;
	}

	/**
	 * Sets the cloudlet exec list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletExecList the new cloudlet exec list
	 */
	protected <T extends ResCloudlet> void setCloudletExecList(List<T> cloudletExecList) {
		this.cloudletExecList = cloudletExecList;
	}

	/**
	 * Gets the cloudlet paused list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet paused list
	 */
	@SuppressWarnings("unchecked")
	protected <T extends ResCloudlet> List<T> getCloudletPausedList() {
		return (List<T>) cloudletPausedList;
	}

	/**
	 * Sets the cloudlet paused list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletPausedList the new cloudlet paused list
	 */
	protected <T extends ResCloudlet> void setCloudletPausedList(List<T> cloudletPausedList) {
		this.cloudletPausedList = cloudletPausedList;
	}

	/**
	 * Gets the cloudlet finished list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet finished list
	 */
	@SuppressWarnings("unchecked")
	protected <T extends ResCloudlet> List<T> getCloudletFinishedList() {
		return (List<T>) cloudletFinishedList;
	}

	/**
	 * Sets the cloudlet finished list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletFinishedList the new cloudlet finished list
	 */
	protected <T extends ResCloudlet> void setCloudletFinishedList(List<T> cloudletFinishedList) {
		this.cloudletFinishedList = cloudletFinishedList;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cloudbus.cloudsim.CloudletScheduler#getCurrentRequestedMips()
	 */
	@Override
	public List<Double> getCurrentRequestedMips() {
		List<Double> mipsShare = new ArrayList<Double>();
		if (getCurrentMipsShare() != null) {
			for (Double mips : getCurrentMipsShare()) {
				mipsShare.add(mips);
			}
		}
		return mipsShare;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cloudbus.cloudsim.CloudletScheduler# getTotalCurrentAvailableMipsForCloudlet
	 * (org.cloudbus.cloudsim.ResCloudlet, java.util.List)
	 */
	@Override
	public double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet rcl, List<Double> mipsShare) {
		double capacity = 0.0;
		int cpus = 0;
		for (Double mips : mipsShare) { // count the cpus available to the vmm
			capacity += mips;
			if (mips > 0) {
				cpus++;
			}
		}
		currentCpus = cpus;
		capacity /= cpus; // average capacity of each cpu
		return capacity;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cloudbus.cloudsim.CloudletScheduler# getTotalCurrentAllocatedMipsForCloudlet
	 * (org.cloudbus.cloudsim.ResCloudlet, double)
	 */
	@Override
	public double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet rcl, double time) {
		return 0.0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cloudbus.cloudsim.CloudletScheduler# getTotalCurrentRequestedMipsForCloudlet
	 * (org.cloudbus.cloudsim.ResCloudlet, double)
	 */
	@Override
	public double getTotalCurrentRequestedMipsForCloudlet(ResCloudlet rcl, double time) {
		return 0.0;
	}

	@Override
	public double getCurrentRequestedUtilizationOfBw() {
		return 0;
	}

	@Override
	public double getCurrentRequestedUtilizationOfRam() {
		return 0;
	}

	
	protected double CalculateReadDelay(List<String> requiredFiles,Cloudlet cl, Vm vm, ReplicaManagement IReplicaManagement2, NetworkDatacenter linkDC ) {
		//Log.printLine("Start of reading phase");
		IReplicaManagement2.ReplicaTest(IReplicaManagement2);
		//Log.printLine("time is equal to: "+ CloudSim.clock()+" , cloudlet id: "+ cl.getCloudletId());
		
		IReplicaManagement2.Maintain2(NetDatacenterBroker.linkDC, CloudSim.clock());

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
			int FID =0;
			String fileName = iter.next();
			ttime = 9999999.9;
			localreadt = 0;
			boolean IsEnergyEnoughS = false;
			for (int i = 0; i < linkDC.getStorageList().size(); i++) {
				
				Storage tempStorage = linkDC.getStorageList().get(i);
				File tempFile = tempStorage.getFile(fileName);
				
				if (tempFile != null) {
					Log.printLine("HostID: "+ tempStorage.getHostID()+
							" , VMID: "+linkDC.getHostList().get(tempStorage.getHostID()).getVmList().get(0).getId()
							+" , File name: "+ fileName);
					FID = tempFile.getFID();
					//finding fastest file for reading
					double temp = 0;
					// In this part we should check this is local read or global read
					if (tempStorage.getHostID() == vm.getHost().getId()){
						temp = (double)tempFile.getSize() / (double)tempStorage.getMaxTransferRate();
						vmidtd = vm.getId();
						localreadt = 1;
						Log.printLine("Local read");
						
					}else {
						int VmS;
						int VmD;
						VmS = linkDC.getHostList().get(tempStorage.getHostID()).getVmList().get(0).getId();// because each host has just one vm
						VmD = vm.getId();
						
						temp = (double)tempFile.getSize() / (double)linkDC.getVMBW()[VmS][VmD];
						vmidts = VmS;
						vmidtd = VmD;
						Log.printLine("Global Read");
					}
					
					
						boolean IsEnergyEnough = true;
						double EnrgyPerTimeUnitd = 0;
						double EnrgyPerTimeUnits = 0;
						if(localreadt == 1){
							EnrgyPerTimeUnitd = linkDC.getStorageList().get(storageid).getEnergypernuit();
							EnrgyPerTimeUnits = 0;
							}else {
							EnrgyPerTimeUnitd = (NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(vmidtd));
							EnrgyPerTimeUnits = (NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(vmidts));
							}
						//Energy from Destination
						double battery = NetDatacenterBroker.linkDC.getVmList().get(vmidtd).getbattery();
						battery = (double) battery - ((double) temp * (double)(EnrgyPerTimeUnitd));
						if (battery < 0.0){
							IsEnergyEnough = false;
							Log.printLine("fail in Energy of Destination");
						}
						
						battery = NetDatacenterBroker.linkDC.getVmList().get(vmidts).getbattery();
						battery = (double) battery - ((double) temp * (double)(EnrgyPerTimeUnits));// the time is same because it depend to Bw between both node
						if (battery < 0.0){
							IsEnergyEnough = false;
							Log.printLine("fail in Energy of source");

						} else {
							IsEnergyEnoughS = true;
						}
						
						if(IsEnergyEnough){
							if (ttime > temp){// if there is more than one replica with enough energy find one with minimum delay
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
					Log.printLine( "file:" + fileName +" didn't find !!!!");
				}
				
			}
			
			if(ttime ==9999999.9){
				ttime =0.0;
				Log.printLine("In read phase fail and couldn't find file");
				if(IsEnergyEnoughS == true){
				
				return -1;
				} else {
					Log.printLine("File name: "+ fileName +" ,Fail in source and terminated");
					CloudSim.terminateSimulation();
					return -1;}
				//pressAnyKeyToContinue();
				//pressAnyKeyToContinue();
				//break;
			}
			time += ttime;
			//Log.printLine( "Partial read time: " +time);
			
			// Deductin energy of read
			
			double EnrgyPerTimeUnitd = 0;
			double EnrgyPerTimeUnits = 0;
			if(localread == 1){
			EnrgyPerTimeUnitd = linkDC.getStorageList().get(storageid).getEnergypernuit();
			EnrgyPerTimeUnits = 0;
			}else {
			EnrgyPerTimeUnitd = (NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(vmidd));
			EnrgyPerTimeUnits = (NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(vmids));
			}
			
			//Log.printLine("Read energy deduction, Energy per unit: "+EnrgyPerTimeUnitd+ " Read delay is: "+ttime  ); // each read shall deduct sepreatly
			//Deduct from Destination
			double battery = NetDatacenterBroker.linkDC.getVmList().get(vmidd).getbattery();
			battery = (double) battery - ((double) ttime * (double)(EnrgyPerTimeUnitd));
			
			Log.printLine("Read energy deduct from destination VMID: "+vmidd +" , Deducted Energy: "+((long) ((double) ttime * (double) (EnrgyPerTimeUnitd)))
					+ " Remining energy : "+battery);
			NetDatacenterBroker.linkDC.getVmList().get(vmidd).setbattery((long)(battery));
			
			// deduct from Source
			battery = NetDatacenterBroker.linkDC.getVmList().get(vmids).getbattery();
			battery = (double) battery - ((double) ttime * (double)(EnrgyPerTimeUnits));// the time is same because it depend to Bw between both node
			
			Log.printLine("Read energy deduct from source VMID: "+vmids+" , Deducted Energy: "+ ((long) ((double) ttime * (double) (EnrgyPerTimeUnits)))
					+ " Remining energy : "+battery );
			cl.SetReadEnergy((long) ((double) ttime * (double) (EnrgyPerTimeUnits)));
			NetDatacenterBroker.linkDC.getVmList().get(vmids).setbattery((long)(battery));
			
			//add NoA for replication managment
			int NoAtemp = IReplicaManagement2.NoA.get(FID);
			NoAtemp++;
			IReplicaManagement2.NoA.set(FID, NoAtemp);
		}
		
		
		//IReplicaManagement2.Maintain(NetDatacenterBroker.linkDC, CloudSim.clock());
		//Log.printLine("End of reading phase");
		Log.printLine( "Total read time: " +time);
		
		//IReplicaManagement2.ReplicaTest(IReplicaManagement2);
		
		
		cl.SetReadDelay(time);//time
		//Log.printLine("fileTransferTime submition: "+ cl.getReadDelay());
		//pressAnyKeyToContinue();
		//pressAnyKeyToContinue();

		return time;
	}

	protected double CalculateReadDelay2(List<String> requiredFiles,Cloudlet cl, Vm vm, ReplicaManagement IReplicaManagement2, NetworkDatacenter linkDC ) {
		//Log.printLine("Start of reading phase");
		//IReplicaManagement2.ReplicaTest(IReplicaManagement2);
		//Log.printLine("time is equal to: "+ CloudSim.clock()+" , cloudlet id: "+ cl.getCloudletId());
		
		IReplicaManagement2.Maintain2(NetDatacenterBroker.linkDC, CloudSim.clock());
		
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
			int FID =0;
			String fileName = iter.next();
			ttime = 9999999.9;
			localreadt = 0;
			for (int i = 0; i < linkDC.getStorageList().size(); i++) {
				
				Storage tempStorage = linkDC.getStorageList().get(i);
				File tempFile = tempStorage.getFile(fileName);
				if (tempFile != null) {
					FID = tempFile.getFID();
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
						VmD = linkDC.getHostList().get(tempStorage.getHostID()).getVmList().get(0).getId();
						VmS = vm.getId();
						
						temp = (double)tempFile.getSize() / (double)linkDC.getVMBW()[VmS][VmD];
						vmidtd = linkDC.getHostList().get(tempStorage.getHostID()).getVmList().get(0).getId();
						vmidts = VmS;
						//Log.printLine("Global Read");
					}
					
					if (ttime > temp){
						//Log.printLine("find files with time: "+ temp);
						boolean IsEnergyEnough = true;
						double EnrgyPerTimeUnitd = 0;
						double EnrgyPerTimeUnits = 0;
						if(localreadt == 1){
							EnrgyPerTimeUnitd = linkDC.getStorageList().get(storageid).getEnergypernuit();
							EnrgyPerTimeUnits = 0;
							}else {
							EnrgyPerTimeUnitd = (NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(vmidtd));
							EnrgyPerTimeUnits = (NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(vmidts));
							}
						//Energy from Destination
						double battery = NetDatacenterBroker.linkDC.getVmList().get(vmidtd).getbattery();
						battery = (double) battery - ((double) temp * (double)(EnrgyPerTimeUnitd));
						if (battery < 0.0){
							IsEnergyEnough = false;
							Log.printLine("fail in Energy of Destination");
						}
						
						battery = NetDatacenterBroker.linkDC.getVmList().get(vmidts).getbattery();
						battery = (double) battery - ((double) temp * (double)(EnrgyPerTimeUnits));// the time is same because it depend to Bw between both node
						if (battery < 0.0){
							IsEnergyEnough = false;
							Log.printLine("fail in Energy of Destination");

						}
						
						if(IsEnergyEnough){
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
			if(ttime ==9999999.9){
				ttime =0.0;
				Log.printLine("In read phase fail and couldn't find file");
				CloudSim.terminateSimulation();
			}
			time += ttime;
			//Log.printLine( "Partial read time: " +time);
			
			// Deductin energy of read
			
			double EnrgyPerTimeUnitd = 0;
			double EnrgyPerTimeUnits = 0;
			if(localread == 1){
			EnrgyPerTimeUnitd = linkDC.getStorageList().get(storageid).getEnergypernuit();
			EnrgyPerTimeUnits = 0;
			}else {
			EnrgyPerTimeUnitd = (NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(vmidd));
			EnrgyPerTimeUnits = (NetDatacenterBroker.linkDC.IVMInfo.TransEnergyUnit.get(vmids));
			}
			
			//Log.printLine("Read energy deduction, Energy per unit: "+EnrgyPerTimeUnitd+ " Read delay is: "+ttime  ); // each read shall deduct sepreatly
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
			
			//add NoA for replication managment
			int NoAtemp = IReplicaManagement2.NoA.get(FID);
			NoAtemp++;
			IReplicaManagement2.NoA.set(FID, NoAtemp);
			// if is in Vmlist need to be added and becouase energy is already deduct just use itiation adding which is without energy deduction
			boolean CreateReplica = IReplicaManagement2.CheckReadForVmReadList(FID,vmidd,IReplicaManagement2);
			if (CreateReplica == true){
				IReplicaManagement2.AddreplicaWithoutEnergyDeduction(FID, vmids,linkDC);
				int FIDID = IReplicaManagement2.FindFIDsID(FID, vmids);
				IReplicaManagement2.SetVmReadlistCreation(FID, vmids);
				//Log.printLine("FID: "+FID+"Read by VMID: "+ vmids+ "added");
			}
		}
		
		
		//IReplicaManagement2.Maintain(NetDatacenterBroker.linkDC, CloudSim.clock());
		Log.printLine("End of reading phase");
		Log.printLine( "Total read time: " +time);
		
		IReplicaManagement2.ReplicaTest(IReplicaManagement2);
		
		
		cl.SetReadDelay(time);//time
		Log.printLine("fileTransferTime submition: "+ cl.getReadDelay());
		//pressAnyKeyToContinue();
		
		return time;
	}


	
	
}
