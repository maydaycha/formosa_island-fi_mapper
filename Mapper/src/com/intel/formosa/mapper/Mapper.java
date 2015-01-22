package com.intel.formosa.mapper;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import com.intel.formosa.test.Go;
import com.intel.formosa.test.Go2;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Mapper {

    private final String USER_AGENT = "Mozilla/5.0";

    private HashMap runnableInstance = new HashMap();
    public String[] need_sensor = new String[20];
    public String[] have_sensor = new String[20];

    //private com.intel.formosa.mapper.Parameters parameters = null;


    class Device {
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


    Device[] arr = new Device[50];  // new stands for create an array object


    /** for Jason Chiu testing */
    public JSONObject run1(String jsonObjString) throws ParseException {

        JSONParser parser = new JSONParser();

        JSONObject jsonObj = (JSONObject)parser.parse(jsonObjString);
        String sessionId = "";

        jsonObj.remove("type");
        jsonObj.put("type", "resp");
        JSONArray a = (JSONArray) jsonObj.get("flow");
        int i = 0;
        for (Object o : a) {
            JSONObject sensor = (JSONObject) o;
            sessionId = sensor.get("z").toString();
            Boolean check = (Boolean) sensor.get("check");
            if(check){
                String type = (String) sensor.get("type");
                sensor.remove("deviceName");
                sensor.put("deviceName", "Device" + i + "II");
                i++;
            }
        }

        if (runnableInstance.containsKey(sessionId)) {
            Go2 g = (Go2) runnableInstance.get(sessionId);
            g.setAliveFlag(false);
            g = null;
            runnableInstance.remove(sessionId);
        }

        Go2 go = new Go2();
        Thread t1 = new Thread(go);
        t1.start();
        runnableInstance.put(sessionId, go);
//        System.out.println("Add " + sessionId + " to Hash Map");

        jsonObj.put("success", true);
//        jsonObj.put("success", false);
        return jsonObj;
    }

    public JSONObject run(String jsonObjString) throws Exception {

        int num = 0;
        int i = 0;
        int wehave = 0;  //how many "need sensor" do we have
        String sessionId = "";

        JSONParser parser = new JSONParser();

        JSONObject jsonObj = (JSONObject)parser.parse(jsonObjString);
		

        jsonObj.remove("type");
        jsonObj.put("type", "resp");
        JSONArray a = (JSONArray) jsonObj.get("flow");


        for (Object o : a)   // get the sensors we need
        {
            JSONObject sensor = (JSONObject) o;

            sessionId = sensor.get("z").toString();

            String check = (String) sensor.get("deviceName");

            if(check != null){
                String type = (String) sensor.get("type");
                need_sensor[num] = type;
                num++;
            }
        }

        sendGet();

        for (Object o : a)
        {
            JSONObject sensor = (JSONObject) o;


            String check = (String) sensor.get("deviceName");

            if(check != null){
                sensor.remove("deviceName");
                sensor.put("deviceName", have_sensor[i]);
                i++;
            }
        }

        for(i = 0;i < num;i++){
            if(have_sensor[i] != "")
                wehave++;
        }
        if(wehave == num){ //success
            jsonObj.put("success", true);

            if (runnableInstance.containsKey(sessionId)) {
                Go g = (Go) runnableInstance.get(sessionId);
                g.setAliveFlag(false);
                g = null;
                runnableInstance.remove(sessionId);
            }

            Go go = new Go(a);
            Thread t1 = new Thread(go);
            t1.start();
            runnableInstance.put(sessionId, go);
            System.out.print("add " + sessionId + " to HashMap");

        } else{
            jsonObj.put("success", false);
        }
        return jsonObj;
    }


    // HTTP GET request
    private void sendGet() throws Exception {


//        String url_broker = "http://192.168.184.129:8000/wsbroker/api/networks";
		String url_broker = "http://127.0.0.1:8000/wsbroker/api/networks";

        int index_sa = 0;
        int n = 0;
        int m = 0;
        int j = 0;

        int flag = 0;

        String d_name = null;
        String d_type = null;
        String d_mac = null;
        String d_address = null;
        Boolean d_alive = null;
        Boolean get_name = true;
        String MSB = null;
        String update = null;
        String IP = null;

        URL obj_broker = new URL(url_broker);


        HttpURLConnection con_broker = (HttpURLConnection) obj_broker.openConnection();

        // optional default is GET
        con_broker.setRequestMethod("GET");

        //add request header

        con_broker.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode_broker = con_broker.getResponseCode();
//		System.out.println("Response Code for broker : " + responseCode_broker);


        BufferedReader in_broker = new BufferedReader(new InputStreamReader(con_broker.getInputStream()));
        String inputLine_broker = null;
        String network_jason = null;
        StringBuffer response_broker = new StringBuffer();


        while ((inputLine_broker = in_broker.readLine()) != null) {
            network_jason = inputLine_broker + " ";
            response_broker.append(inputLine_broker);

            for( m = 0; m < inputLine_broker.length() ; m++){

                if(inputLine_broker.regionMatches(m, "ipaddress", 0, 9) == true){  // record update time
                    IP = inputLine_broker.substring(m + 13, m+34);
                }
            }
        }


		String url = "http://127.0.0.1:8080/wsgtwy/net/getlist/";
//        String url = "http://192.168.184.129:8080/wsgtwy/net/getlist/";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();


        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            String SA = new String("sa_");

            for(int i = 0; i<inputLine.length();i++){  //search string

                if(inputLine.regionMatches(i, SA, 0, 3) == true){  //find sa, i = index of sa
                    index_sa = i;

                    for(j=i-1;j<i+50;j++){
                        if(inputLine.regionMatches(j, ",", 0, 1) == true){
                            d_type = inputLine.substring(index_sa + 20, j-1);
                            break;
                        }
                    }
                }

                if(inputLine.regionMatches(i, "Device_Address", 0, 14) == true){ //get device address , check alive
                    d_address = inputLine.substring(i + 17, i+22);

                    for( m = 0; m < network_jason.length() ; m++){   // find alive
                        if(network_jason.regionMatches(m, inputLine.substring(i + 17, i+22), 0, 5) == true){  // record update time
                            flag = 1;

                            for(int x=m;x<m+150;x++){

                                if(network_jason.regionMatches(x, "name", 0, 4) == true){
                                    for(j=x-1;j<x+50;j++){

                                        if(network_jason.regionMatches(j, ",", 0, 1) == true){

                                            if(get_name){
                                                d_name = network_jason.substring(x + 8, j-1);
                                                get_name = false;
                                            }
                                            else{
                                                d_name = d_name+"/"+network_jason.substring(x + 8, j-1);
                                                get_name = true;
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        if(network_jason.regionMatches(m, "last_updated", 0, 12) == true && flag ==1){
                            update = network_jason.substring(m + 16, m+35);
                            update = update.replace('T', ' ');

                            //	String dateString = "2010-03-02 20:25:58";

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                            Date date = sdf.parse(update);
                            Date d=new Date();
                            //	System.out.println(date);

                            long now_time = (d.getTime())/1000;
                            long update_time = (date.getTime())/1000;

                            if(now_time - update_time < 5)
                                d_alive = true;
                            else
                                d_alive = false;

                            flag = 0;
                        }
                    }
                }

                if(inputLine.regionMatches(i, "MSB", 0, 3) == true){

                    MSB = inputLine.substring(i + 7, i+15);
                }
                if(inputLine.regionMatches(i, "LSB", 0, 3) == true){

                    d_mac = MSB + inputLine.substring(i + 7, i+15);
                    arr[n] = new Device(d_name ,d_type ,d_mac, d_address, d_alive); // name / type / mac
                    n++;
                    d_name = "";
                    d_type = "";
                    d_mac = "";
                    d_address = "";
                    d_alive = false;
                }
            }

            for(m = 0; m < n; m++){
                if(arr[m].d_type.equals("Meter_S")){
                    arr[m].d_alive = true;
                }

                if(arr[m].d_type.equals("IASWD_S")){
                    arr[m].d_alive = true;
                }

                System.out.println(m+" : "+arr[m].d_name + "+" + arr[m].d_type + "+" + arr[m].d_mac + "+" +arr[m].d_address + "+" + arr[m].d_alive);
            }
        }

        in_broker.close();
        in.close();

        for(j = 0;j < 20;j ++)
            have_sensor[j] = "";

        for(m = 0;m < n;m++){

            for(j = 0;j < 20;j ++){

                if((arr[m].d_type).equals(need_sensor[j]) && arr[m].d_alive == true){ 	// alive
                    have_sensor[j] = arr[m].d_name;
                    break;
                }
            }
        }
    }
 /*
	// HTTP POST request
	private void sendPost() throws Exception {
 
		String url = "https://selfsolve.apple.com/wcResults.do";
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
 
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
 
		String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
 
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		System.out.println(response.toString());
 
	}
   */

    public void stopRuleEngine(String sessionId) {
        System.out.println("[stopRuleEngine] function call");
        if (sessionId != null && !sessionId.equals("")) {
            if (runnableInstance.containsKey(sessionId)) {
                Go g = (Go) runnableInstance.get(sessionId);
                System.out.println("[stopRuleEngine] get runnable by sessionId: " + sessionId);
                g.setAliveFlag(false);
                System.out.println("[stopRuleEngine] set flag false: " + g.getAliveFlag());
                g = null;
                runnableInstance.remove(sessionId);
            }
        } else {
            System.out.println("[stopRuleEngine] not entry ");
        }
    }


//    public static void main(String[] args) {
//        Go2 go = new Go2();
//        new Thread(go).start();
//    }
}
