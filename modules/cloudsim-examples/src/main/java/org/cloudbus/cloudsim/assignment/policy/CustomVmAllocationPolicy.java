package org.cloudbus.cloudsim.assignment.policy;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.assignment.CloudSimAssignmentConstent;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomVmAllocationPolicy extends VmAllocationPolicy {

    /**
     * The map between each VM and its allocated host.
     * The map key is a VM UID and the value is the allocated host for that VM.
     */
    private Map<String, Host> vmTable;

    /**
     * The map between each VM and the number of Pes used.
     * The map key is a VM UID and the value is the number of used Pes for that VM.
     */
    private Map<String, Integer> usedPes;

    /**
     * The number of free Pes for each host from {@link #getHostList() }.
     */
    private List<Integer> freePes;

    /**
     * Creates a new VmAllocationPolicySimple object.
     *
     * @param list the list of hosts
     * @pre $none
     * @post $none
     */
    public CustomVmAllocationPolicy(List<? extends Host> list) {
        super(list);

        setFreePes(new ArrayList<>());
        for (Host host : getHostList()) {
            getFreePes().add(host.getNumberOfPes());

        }

        setVmTable(new HashMap<>());
        setUsedPes(new HashMap<>());
    }

    /**
     * Allocates the host with less PEs in use for a given VM.
     *
     * @param vm {@inheritDoc}
     * @return {@inheritDoc}
     * @pre $none
     * @post $none
     */
    @Override
    public boolean allocateHostForVm(Vm vm) {
        int requiredPes = vm.getNumberOfPes();
        boolean result = false;
        int tries = 0;
        List<Integer> freePesTmp = new ArrayList<>(getFreePes());

        if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
            do {// we still trying until we find a host or until we try all of them
                int min = Integer.MAX_VALUE;
                int max = Integer.MIN_VALUE;
                int idx = -1;

                String vmAllocationPolicy = CloudSimAssignmentConstent.allocationPolicy;

                //policies name
                String CSVP = "CSVP";
                String FFD = "FFD";
                String BFD = "BFD";
                String BEST_FIT = "bestfit";
                String FIRST_FIT = "firstfit";

                if (vmAllocationPolicy.equalsIgnoreCase(BEST_FIT) || vmAllocationPolicy.equalsIgnoreCase(BFD)) {

                    for (int i = 0; i < freePesTmp.size(); i++) {
                        if (freePesTmp.get(i) < min && freePesTmp.get(i) > requiredPes) {
                            min = freePesTmp.get(i);
                            idx = i;
                        }
                    }
                }else if (vmAllocationPolicy.equalsIgnoreCase(FIRST_FIT) || vmAllocationPolicy.equalsIgnoreCase(FFD)) {
                    List<Host> hostList = this.getHostList();
                    int lastIndex = hostList.size() - 1;
                    for (int i = 0; i < hostList.size(); i++) {
                        Host host = hostList.get(lastIndex);
                        if (host.isSuitableForVm(vm)) {
                            idx = lastIndex;
                        }

                        lastIndex = --lastIndex % hostList.size();
                    }
                }else if(vmAllocationPolicy.equalsIgnoreCase(CSVP)){
                    //TODO

                }else {
                    //default worst fit
                    for (int i = 0; i < freePesTmp.size(); i++) {
                        if (freePesTmp.get(i) > max) {
                            max = freePesTmp.get(i);
                            idx = i;
                        }
                    }
                }

                Host host = this.getHostList().get(idx);
                result = host.vmCreate(vm);


                if (result) { // if vm were successfully created in the host
                        getVmTable().put(vm.getUid(), host);
                        getUsedPes().put(vm.getUid(), requiredPes);
                        getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
                        printVMAllocation(vm) ; // Printing allocation information
                        result = true;
                        break;
                } else {
                        freePesTmp.set(idx, Integer.MIN_VALUE);
                }
                tries++;
            } while (tries < getFreePes().size()) ;

        }
        return result;
    }

    private void printVMAllocation(Vm vm) {
        String INDENT = "   ";
        if (vm.getId ()==0) {//FIRST VM
            Log.printLine("============================================================================");
            Log.printLine("Vm ID" + INDENT + "Vm's PEs" + INDENT + "Host's ID" + INDENT + "Host's Allocated PEs" + INDENT + "Host's Free PEs");
        }
        int hostAllocatedPes = (vm.getHost().getNumberOfPes() - getFreePes().get(vm.getHost().getId()));
        int hostFreePesAfterAfterAllocation = vm.getHost().getNumberOfFreePes() - hostAllocatedPes;
        Log.print(INDENT + vm.getId() + INDENT + INDENT + INDENT + vm.getNumberOfPes() + INDENT + INDENT + INDENT);
        Log.printLine(vm.getHost().getId() + INDENT + INDENT + INDENT + INDENT + hostAllocatedPes + INDENT + INDENT
                    + INDENT + INDENT + INDENT + INDENT + INDENT + hostFreePesAfterAfterAllocation);
        if (vm.getId() == CloudSimAssignmentConstent.totalVms -1) {//LAST VM
            printHostAllocation();
        }
    }

    private void printHostAllocation() {
        int hostUnderU = 0;
        int hostOverU = 0;
        int hostFree = 0;
        List<Integer> pesTmp = new ArrayList<>(this.getFreePes());
        int hostTotal = pesTmp.size();
        for (int i = 0; i < pesTmp.size(); i++) {
            if (pesTmp.get(i) == 0) {
                hostFree++;
            } else if (pesTmp.get(i) < this.getHostList().get(i).getNumberOfPes() / 2) {
                hostUnderU++;
            } else if (pesTmp.get(i) >= this.getHostList().get(i).getNumberOfPes() / 2) {
                hostOverU++;
            }
        }
        Log.printLine ("====================================================================");
        Log.printLine ("Total Host          : " + hostTotal);
        Log.printLine ("Over Utilized Host  : " + hostOverU);
        Log.printLine ("Under Utilized Host : " + hostUnderU);
        Log.printLine ("Free Host           : " + hostFree );
        Log.printLine("====================================================================");
    }

    @Override
    public void deallocateHostForVm(Vm vm) {
        Host host = getVmTable().remove(vm.getUid());
        int idx = getHostList().indexOf(host);
        int pes = getUsedPes().remove(vm.getUid());
        if (host != null) {
            host.vmDestroy(vm);
            getFreePes().set(idx, getFreePes().get(idx) + pes);
        }
    }

    @Override
    public Host getHost(Vm vm) {
        return getVmTable().get(vm.getUid());
    }

    @Override
    public Host getHost(int vmId, int userId) {
        return getVmTable().get(Vm.getUid(userId, vmId));
    }

    /**
     * Gets the vm table.
     *
     * @return the vm table
     */
    public Map<String, Host> getVmTable() {
        return vmTable;
    }

    /**
     * Sets the vm table.
     *
     * @param vmTable the vm table
     */
    protected void setVmTable(Map<String, Host> vmTable) {
        this.vmTable = vmTable;
    }

    /**
     * Gets the used pes.
     *
     * @return the used pes
     */
    protected Map<String, Integer> getUsedPes() {
        return usedPes;
    }

    /**
     * Sets the used pes.
     *
     * @param usedPes the used pes
     */
    protected void setUsedPes(Map<String, Integer> usedPes) {
        this.usedPes = usedPes;
    }

    /**
     * Gets the free pes.
     *
     * @return the free pes
     */
    protected List<Integer> getFreePes() {
        return freePes;
    }

    /**
     * Sets the free pes.
     *
     * @param freePes the new free pes
     */
    protected void setFreePes(List<Integer> freePes) {
        this.freePes = freePes;
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        if (host.vmCreate(vm)) { // if vm has been succesfully created in the host
            getVmTable().put(vm.getUid(), host);

            int requiredPes = vm.getNumberOfPes();
            int idx = getHostList().indexOf(host);
            getUsedPes().put(vm.getUid(), requiredPes);
            getFreePes().set(idx, getFreePes().get(idx) - requiredPes);

            Log.formatLine(
                    "%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(),
                    CloudSim.clock());
            return true;
        }

        return false;
    }
}