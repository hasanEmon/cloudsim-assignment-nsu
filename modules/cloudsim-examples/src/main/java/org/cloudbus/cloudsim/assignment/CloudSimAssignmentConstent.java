package org.cloudbus.cloudsim.assignment;

public class CloudSimAssignmentConstent {
    public static final String allocationPolicyBF = "bestfit"; // best fit
    public static final String allocationPolicyBFD = "BFD"; // best fit decreasing
    public static final String allocationPolicyFF = "firstfit"; // first fit
    public static final String allocationPolicyFFD = "FFD"; // first fit decreasing
    public static final String allocationPolicyCSVP = "CSVP";
    public static int totalVms10 = 10;
    public static int totalVms100 = 100;
    public static int totalVms1000 = 1000;
    public static int totalVms5000 = 5000;
    public static int totalVms7650 = 7650; // ID 1835007650
    public static double multiplyOf = 1;
    public static int totalHosts10 = (int) (multiplyOf * totalVms10);
    public static int totalHosts100 = (int) (multiplyOf * totalVms100);
    public static int totalHosts1000 = (int) (multiplyOf * totalVms1000);
    public static int totalHosts5000 = (int) (multiplyOf * totalVms5000);
    public static int totalHosts7650 = (int) (multiplyOf * totalVms7650); // ID 1835007650
    public static int vmMaxCpus = 5; //IF USE RANDOM NUMBER CPU
    public static int vmCpuMips = 1000;
    public static int vmTotalMem = 2048;
    public static int vmTotalBW = 1000;
    public static int vmTotalStorage = 1000;
    public static int hostTotalCpus = 10;
    public static int hostCpuMips = 1000;
    public static int hostTotalMem = 40960;
    public static int hostTotalBW = 10000;
    public static int hostTotalStorage = 100000;

}
