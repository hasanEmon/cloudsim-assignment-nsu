package org.cloudbus.cloudsim.assignment;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.assignment.policy.CustomVmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.*;

/**
 * @author Rakibul Hasan Emon
 * @id 1835007650
 * @course CSE 522
 * @section 1
 */
public class CloudSimAssignment {
	private static final String indent = "   " ;
	private static List<Cloudlet> cloudletList;
	/** The vmlist. */
	private static List<Vm> vmlist;

	public static void main(String[] args) {
		runApp();
	}

	private static void runApp() {
		Log.printLine("Starting CloudSimAssignment...");

		try {
			// First step: Initialize the CloudSim package. It should be called before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance(); // Calendar whose fields have been initialized with the current date and time.
			boolean trace_flag = false; // trace events

			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			Datacenter datacenter0 = createDatacenter("Datacenter_0");

			// Third step: Create Broker
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			// Fourth step: Create one virtual machine
			vmlist = new ArrayList<>();
			// VM description
			int vmid = 0;
			int mips = CloudSimAssignmentConstent.vmCpuMips;
			long size = CloudSimAssignmentConstent.vmTotalStorage; // image size (MB)
			int ram = CloudSimAssignmentConstent.vmTotalMem; // vm memory (MB)
			long bw = CloudSimAssignmentConstent.vmTotalBW;
//			int pesNumber = 1; // number of cpus
			int totalVms = CloudSimAssignmentConstent.totalVms;

			// For random number o f cpus
			List<Integer> randomPes = new ArrayList<>(totalVms) ;
			for(int i = 0; i < totalVms; i++) {//CREATING RANDOM PEs
				// define the range
				int max = CloudSimAssignmentConstent.vmMaxCpus;
				int min = 1;
				int range = max - min + 1;
				int peNumber = min + (int) (Math.random() * range);
//				randomPes.add(peNumber) ;
				int[] test = {5,4,3,2,6,2,2,3,2,4};
				randomPes.add(test[i]) ;
			}
			String vmm = "Xen"; // VMM name

			if(CloudSimAssignmentConstent.allocationPolicy.equalsIgnoreCase("FFD")
					|| CloudSimAssignmentConstent.allocationPolicy.equalsIgnoreCase("BFD")) {
				randomPes.sort(Collections.reverseOrder());
			}

			if (CloudSimAssignmentConstent.allocationPolicy.equalsIgnoreCase("CSVP")) {
				int mid = randomPes.size()/2;
				randomPes.subList(0, mid).sort(Comparator.reverseOrder());
				randomPes.subList(mid, randomPes.size()).sort(Comparator.reverseOrder());
			}

			// create VM list
			for(int i =0; i<totalVms ; i++){
				Vm vm = new Vm(vmid+i , brokerId, mips, randomPes.get(i), ram,
						bw, size, vmm, new CloudletSchedulerTimeShared());
				vmlist.add(vm);
			}

			// submit vm list to the broker
			broker.submitVmList(vmlist);

			// Fifth step: Create one Cloudlet
			cloudletList = new ArrayList<>();

			// Cloudlet properties
			int id = 0;
			int pesRequirement = 1;
			long length = 400000;
			long fileSize = 300;
			long outputSize = 300;
			UtilizationModel utilizationModel = new UtilizationModelFull();

			Cloudlet cloudlet = new Cloudlet(id, length, pesRequirement, fileSize,
							outputSize, utilizationModel, utilizationModel,
							utilizationModel);
			cloudlet.setUserId(brokerId);
			cloudlet.setVmId(vmid);

			// add the cloudlet to the list
			cloudletList.add(cloudlet);

			// submit cloudlet list to the broker
			broker.submitCloudletList(cloudletList);

			// print the VM's information
			List<Vm> newVmList = broker.getVmList() ;
			printVMList(newVmList ) ;
			// print the Host's information
			List<Host> newHostList = datacenter0.getHostList() ;
			printHostList(newHostList ) ;

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			//Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);

			Log.printLine("CloudSimAssignment finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}


	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("================================+++++ OUTPUT +++++==================================");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS  ");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
			Log.printLine("-------------------------------------------------------------------------------------");
		}
	}

	/**
	 * Creates the datacenter.
	 *
	 * @param name the name
	 *
	 * @return the datacenter
	 */
	public static Datacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine
		List<Host> hostList = new ArrayList<>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<>();

		int mips = CloudSimAssignmentConstent.hostCpuMips;
		int totalCpus = CloudSimAssignmentConstent.hostTotalCpus ;

		// 3. Create PEs and add these into a list.
		for (int i = 0; i < totalCpus; i++) {
			peList.add(new Pe(i, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
		}

		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int hostId = 0;
		int ram = CloudSimAssignmentConstent.hostTotalMem; // host memory (MB)
		long storage = CloudSimAssignmentConstent.hostTotalStorage; // host storage
		int bw = CloudSimAssignmentConstent.hostTotalBW;

		int totalHosts = CloudSimAssignmentConstent.totalHosts ;

		for (int i = 0; i < totalHosts; i++) {
			hostList.add(
					new Host(
							hostId+i,
							new RamProvisionerSimple(ram),
							new BwProvisionerSimple(bw),
							storage,
							peList,
							new VmSchedulerTimeShared(peList)
					)
			); // This is our machine
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

		LinkedList<Storage> storageList = new LinkedList<>(); // we are not adding SAN
		// devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new CustomVmAllocationPolicy(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}



	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	/**
	 * Creates the broker.
	 *
	 * @return the datacenter broker
	 */
	public static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}


	public static void printVMList (List<Vm> list ) {
		Vm vm;

		Log.printLine ( ) ;
		Log.printLine("========== VM INFORMATION ============");
		Log.printLine ("Vm ID" + indent + "Vm's PEs" + indent+ "Vm's RAM" + indent + "Vm's BW"+ indent);
		for (Vm value : list) {
			vm = value;
			Log.print(indent + vm.getId() + indent + indent + vm.getNumberOfPes() + indent + indent + indent);
			Log.printLine(vm.getCurrentRequestedRam() + indent + indent + +vm.getCurrentRequestedBw() + indent + indent);
			Log.printLine();
		}
	}
	public static void printHostList(List<Host> list) {
		Host host;
		Log.printLine() ;
		Log.printLine("========== HOST INFORMATION ==============================");
		Log.printLine("Host ID" + indent+ "Host PEs" + indent + "Host RAM"+ indent + "Host BW" +indent);
		for (Host value : list) {
			host = value;
			Log.print(indent + host.getId() + indent + indent + indent);
			Log.printLine(host.getNumberOfPes() + indent + indent + host.getRam() + indent + indent + host.getBw());
		}
		Log.printLine();
	}
}