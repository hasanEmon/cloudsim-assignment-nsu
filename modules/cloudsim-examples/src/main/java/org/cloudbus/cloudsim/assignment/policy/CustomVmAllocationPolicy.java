package org.cloudbus.cloudsim.assignment.policy;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class CustomVmAllocationPolicy extends VmAllocationPolicy {

    private int totalVms;
    private String allocationPolicy;
    private final String base = System.getProperty("user.dir");
    private final String filePath = base + "\\modules\\cloudsim-examples\\src\\main\\java\\org\\cloudbus\\cloudsim\\assignment\\output.txt";

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
    public CustomVmAllocationPolicy(List<? extends Host> list, String allocationPolicy, int totalVms) {
        super(list);
        this.allocationPolicy = allocationPolicy;
        this.totalVms = totalVms;
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
        List<Integer> freePesTmp = new ArrayList<>(this.getFreePes());

        if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
            do {// we still trying until we find a host or until we try all of them
                int min = Integer.MAX_VALUE;
                int max = Integer.MIN_VALUE;
                int idx = -1;
//                String vmAllocationPolicy = CloudSimAssignmentConstent.allocationPolicy;
                //policies name
                String CSVP = "CSVP";
                String FFD = "FFD";
                String BFD = "BFD";
                String BEST_FIT = "bestfit";
                String FIRST_FIT = "firstfit";

                if (allocationPolicy.equalsIgnoreCase(BEST_FIT) || allocationPolicy.equalsIgnoreCase(BFD)) {
                    for (int i = 0; i < freePesTmp.size(); i++) {
                        if (freePesTmp.get(i) < min && freePesTmp.get(i) >= requiredPes) {
                            min = freePesTmp.get(i);
                            idx = i;
                        }
                    }
                }else if (allocationPolicy.equalsIgnoreCase(FIRST_FIT) || allocationPolicy.equalsIgnoreCase(FFD)) {
                    for (int i = 0; i < freePesTmp.size(); i++) {
                        if (freePesTmp.get(i) >= requiredPes) {
                            idx = i;
                            break;
                        }
                    }
                }else if(allocationPolicy.equalsIgnoreCase(CSVP)){
                    int mid = totalVms / 2;
                    for (int i = 0; i < freePesTmp.size(); i++) {
                        if (freePesTmp.get(i) > max && vm.getId() < mid) {
                            max = freePesTmp.get(i);
                            idx = i;
                        } else if (vm.getId() >= mid && freePesTmp.get(i) < min && freePesTmp.get(i) >= requiredPes) {
                            min = freePesTmp.get(i);
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
                        printVMAllocation(vm, allocationPolicy, totalVms) ; // Printing allocation information
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

    private void printVMAllocation(Vm vm, String selectedPolicy, int totalVms) {
//        StringBuilder builder = new StringBuilder();
        String INDENT = "   ";
        if (vm.getId ()==0) {//FIRST VM
            Log.printLine("============================================================================");
//            builder.append("============================================================================\n");
            Log.printLine("Vm ID" + INDENT + "Vm's PEs" + INDENT + "Host's ID" + INDENT + "Host's Allocated PEs" + INDENT + "Host's Free PEs");
//            builder.append("Vm ID").append(INDENT).append("Vm's PEs").append(INDENT).append("Host's ID").append(INDENT)
//                    .append("Host's Allocated PEs").append(INDENT).append("Host's Free PEs\n");
        }
        int hostAllocatedPes = (vm.getHost().getNumberOfPes() - getFreePes().get(vm.getHost().getId()));
        int hostFreePesAfterAfterAllocation = vm.getHost().getNumberOfFreePes() - hostAllocatedPes;
        Log.print(INDENT + vm.getId() + INDENT + INDENT + INDENT + vm.getNumberOfPes() + INDENT + INDENT + INDENT);
        Log.printLine(vm.getHost().getId() + INDENT + INDENT + INDENT + INDENT + hostAllocatedPes + INDENT + INDENT
                    + INDENT + INDENT + INDENT + INDENT + INDENT + hostFreePesAfterAfterAllocation);
//        builder.append(INDENT).append(vm.getId()).append(INDENT).append(INDENT).append(INDENT).append(vm.getNumberOfPes())
//                .append(INDENT).append(INDENT).append(INDENT).append(vm.getHost().getId()).append(INDENT).append(INDENT)
//                .append(INDENT).append(INDENT).append(hostAllocatedPes).append(INDENT).append(INDENT).append(INDENT)
//                .append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(hostFreePesAfterAfterAllocation);
//        this.writeFile(builder.toString());
        if (vm.getId() == totalVms -1) {//LAST VM
            Log.printLine("============================================================================");
//            this.writeFile("============================================================================");
            printHostAllocation(selectedPolicy);
        }
    }

    private void printHostAllocation(String policyName) {
        int hostUnderU = 0;
        int hostOverU = 0;
        int hostFree = 0;
        List<Integer> pesTmp = new ArrayList<>(this.getFreePes());
        int hostTotal = pesTmp.size();
        for (int i = 0; i < pesTmp.size(); i++) {
            if (pesTmp.get(i) == 10) {
                hostFree++;
            } else if (pesTmp.get(i) >= this.getHostList().get(i).getNumberOfPes() / 2) {
                hostUnderU++;
            } else if (pesTmp.get(i) < this.getHostList().get(i).getNumberOfPes() / 2) {
                hostOverU++;
            }
        }

        StringBuilder content = new StringBuilder();
//        Log.printLine ("====================================================================");
//        Log.printLine("Selected algorithm   :" + policyName);
//        Log.printLine ("Total Host          : " + hostTotal);
//        Log.printLine ("Over Utilized Host  : " + hostOverU);
//        Log.printLine ("Under Utilized Host : " + hostUnderU);
//        Log.printLine ("Free Host           : " + hostFree );
//        Log.printLine("====================================================================");
        content.append("====================================================================\n")
                .append("Selected algorithm   : ").append(policyName).append("\n")
                .append("Total VM            : ").append(vmTable.size()).append("\n")
                .append("Total Host          : ").append(hostTotal).append("\n")
                .append("Over Utilized Host  : ").append(hostOverU).append("\n")
                .append("Under Utilized Host : ").append(hostUnderU).append("\n")
                .append("Free Host           : ").append(hostFree).append("\n")
                .append("====================================================================");
        Log.printLine(content.toString());
        this.writeFile(content.toString());
    }

    private void writeFile(String line) {
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                line = this.readFile().length() == 0 || this.readFile() == null ? line : "\n" + line;
                writer.append(line);
            }
        } catch (Exception e) {
            Log.print(e);
        }
    }
    public String readFile() {
        try {
            String id;
            try (Scanner sc = new Scanner(new File(filePath))) {
                id = "";
                while (sc.hasNextLine()) {
                    id = sc.nextLine();
                }
            }
            return id;
        } catch (Exception e) {
            Log.print(e);
            return null;
        }
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