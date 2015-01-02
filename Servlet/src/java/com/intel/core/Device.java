package com.intel.core;

/**
 * Created by Maydaycha on 1/2/15.
 */
public class Device {
        private String d_name;
        private String d_type;
        private String d_mac;
        private String d_address;
        private boolean d_alive;

        // constructor
        public Device(String d_name, String d_type, String d_mac, String d_address, boolean d_alive) {
            this.d_name = d_name;
            this.d_type = d_type;
            this.d_mac = d_mac;
            this.d_address = d_address;
            this.d_alive = d_alive; // 0 : die , 1 : alive
        }

        // getter
        public String getName() { return d_name; }
        public String getType() { return d_type; }
        public String getMac() { return d_mac; }
        public String getAddress() {return d_address;}
        public boolean getAlive() {return d_alive;}
        // setter

        public void setName(String name) { this.d_name = name; }
        public void setType(String type) { this.d_type = type; }
        public void setMac(String mac) { this.d_mac = mac; }
        public void setAddress(String address) {this.d_address = address; }
        public void setAlive(boolean alive) {this.d_alive = alive; }
}
