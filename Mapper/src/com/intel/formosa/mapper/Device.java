package com.intel.formosa.test;

public class Device {
    private String d_name;
    private String s_name;
    private String d_mac;
    private String s_id;
    private String s_alive;
    //private boolean d_alive;

    // constructor
    public Device(String d_name, String s_name, String d_mac, String s_id, String s_alive) {
        this.d_name = d_name;
        this.s_name = s_name;
        this.d_mac = d_mac;
        this.s_id = s_id;
        this.s_alive = s_alive;
        //    this.d_alive = d_alive; // 0 : die , 1 : alive
    }

    // getter
    public String get_d_name() { return d_name; }
    public String get_s_name() { return s_name; }
    public String get_d_mac() { return d_mac; }
    public String get_s_id() {return s_id;}
    public String get_alive() {return s_alive;}
    //public boolean getAlive() {return d_alive;}

    // setter
    public void setName(String name) { this.d_name = name; }
    public void setType(String type) { this.s_name = type; }
    public void setMac(String mac) { this.d_mac = mac; }
    public void setAddress(String address) {this.s_id = address; }
    public void setAlive(String alive) {this.s_alive = s_alive; }
}