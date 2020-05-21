package org.cloudbus.cloudsim.assignment;

public class CloudSimAssignmentConstent {
//    public static String allocationPolicy = "bestfit"; // best fit
//    public static String allocationPolicy = "firstfit"; // first fit
//    public static String allocationPolicy = "BFD"; // best fit decreasing
//    public static String allocationPolicy = "FFD"; // first fit decreasing
    public static String allocationPolicy = "CSVP";
    public static int totalVms = 10;
//    public static int totalVms = 100;
//    public static int totalVms = 1000;
//    public static int totalVms = 5000;
//    public static int totalVms = 7650; //1835007650
    public static int totalHosts = 10;
//    public static int totalHosts = 100;
//    public static int totalHosts = 1000;
//    public static int totalHosts = 5000;
//    public static int totalHosts = 7650; //1835007650
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
