package com.intel.formosa.mapper;

public class Device {
    private String d_name;
    private String s_name;
    private String d_mac;
    private String s_id;
    //private boolean d_alive;

    // constructor
    public Device(String d_name, String s_name, String d_mac, String s_id) {
        this.d_name = d_name;
        this.s_name = s_name;
        this.d_mac = d_mac;
        this.s_id = s_id;
        //    this.d_alive = d_alive; // 0 : die , 1 : alive
    }

    // getter
    public String get_d_name() { return d_name; }
    public String get_s_name() { return s_name; }
    public String get_d_mac() { return d_mac; }
    public String get_s_id() {return s_id;}
    //public boolean getAlive() {return d_alive;}

    // setter
    public void setName(String name) { this.d_name = name; }
    public void setType(String type) { this.s_name = type; }
    public void setMac(String mac) { this.d_mac = mac; }
    public void setAddress(String address) {this.s_id = address; }
    //public void setAlive(boolean alive) {this.d_alive = alive; }
}
