package com.intel.formosa.mapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;




//import com.intel.formosa.test.Go;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Mapper {

    private final String gateway_ip = "192.168.184.131";
    private final String gateway_port = "8080";
    private final String gateway_url = "http://" + gateway_ip + ":" + gateway_port;

    private final String FIELD_USERNAME = "username";
    private final String FIELD_PASSWORD = "password";
    private final String FILED_AUTHORIZATION ="Authorization";
    private final String FILED_STRTOKEN = "bearer ";

    private final String uriDeviceInfo = "/api/provision/devices/info/";
    private final String USER_AGENT = "Mozilla/5.0";
    private final String ip_address = "127.0.0.1";
    private final int port = 8080;

    private final String username = "admin";
    private final String password = "admin";

    private String token;

//    public String[] requested_sensor = new String[20];
//    public String[] available_sensor = new String[20];
//    public String[] requested_actuator = new String[20];
    private ArrayList<String> requested_sensor = new ArrayList<String>();
    private ArrayList<String> available_sensor = new ArrayList<String>();
    private ArrayList<String> requested_actuator = new ArrayList<String>();
    private long protocol_id = 0;

    //private Device[] arrDevice = new Device[50];
    private ArrayList<Device> deviceList = new ArrayList<Device>();
    private ArrayList<Device> acList = new ArrayList<Device>();

    private HashMap runnableInstance = new HashMap();


    public static void main (String[] args) throws Exception{

//        String data = "";
        JSONObject result;
        Mapper conn = new Mapper();
        
        

        /** this will do in the run function */
//        conn.generateToken();


        /** this will do in the run function */
        // retrieve all devices info
//        data = conn.retrieveDevicesList(uriDeviceInfo);

        // retrieve SPECIFIC device info
        String deviceMAC = "00137a000001b448";
        //	conn.retrieveDevicesList(uriDeviceInfo + deviceMAC);

        JSONObject jsonObj = (JSONObject) new JSONParser().parse(new FileReader("input.json"));
        JSONObject jsonObj1 = (JSONObject) new JSONParser().parse(new FileReader("input2.json"));
        
        

        /** the parameter of run() should be the JSON string passed from Web */
        result = conn.run(jsonObj.toJSONString());
        System.out.println(result);
//        result = conn.run(jsonObj1.toJSONString());
//        System.out.println(result);

    }

    // Generate token from Amelia Creek 1.1
    public void generateToken(){
        String requestURL = "http://" + gateway_ip + ":" + gateway_port + "/user/login/token";

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(requestURL);

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair(FIELD_USERNAME, username));
            nameValuePairs.add(new BasicNameValuePair(FIELD_PASSWORD, password));
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String strToken = rd.readLine();

            if (strToken != null) {
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObj;

                try {
                    jsonObj = (JSONObject) jsonParser.parse(strToken);
                    token = jsonObj.get("token").toString();
                } catch(ParseException e) {
                    e.printStackTrace();
                } finally {
                    /** close everything and release resource */
                    rd.close();
                    rd = null;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Retrieve devices data via Amelia Creek 1.1 API */
    public String retrieveDevicesList(String uri) throws IOException {

        String strToken ="qww";
        String data ="";

        BufferedReader rd = null;

        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(gateway_url+uri);

        try {
            get.addHeader(FILED_AUTHORIZATION, FILED_STRTOKEN+ token);

            HttpResponse response = client.execute(get);
            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            while ((strToken = rd.readLine()) != null) {
                data = strToken;
            }

        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            /** close everything and release resource */
            if (rd != null) {
                rd.close();
                rd = null;
            }
        }
        return data;
    }

    public JSONObject run(String jsonObjString) throws Exception {

        int num = 0;
        int i = 0;
        int index = 0;
        boolean pass = true;     
        int sensorRequest = 0;  // number of requested sensors we found
        String sessionId = null;
        
        requested_sensor.clear();
        available_sensor.clear();

        JSONObject jsonObj = (JSONObject) new JSONParser().parse(jsonObjString);

        /** get AC 1.1 authentication token */
        generateToken();

        jsonObj.remove("type");
        jsonObj.put("type", "resp");
        sessionId = jsonObj.get("session_id").toString();

        JSONArray a = (JSONArray) jsonObj.get("flow");

        /** Search for the requested sensor */
        for (Object o : a) {
            JSONObject sensor = (JSONObject) o;
            Boolean check = (Boolean) sensor.get("check");
                 
            if (check){
                String deviceType = (String) sensor.get("deviceType");                
                requested_sensor.add(num, deviceType);
                num++;
            }    
        }

        /** retrieve all devices info */
        retrieveData(retrieveDevicesList(uriDeviceInfo));
        
        i = 0;
        for(index = 0; index<deviceList.size();index++){
        	if(deviceList.get(index).get_alive().equals("true"))
        		i++;
        }
        if(num>i){
        	pass = false;
        }
        
        if(pass){
        for(i = 0;i < num;i++){

            for(index = 0; index<deviceList.size();index++){
          //  	System.out.println("Sensor: "+requested_sensor.get(i)+" from "+deviceList.get(index).get_s_id().substring(3));
                if(requested_sensor.get(i).equals(deviceList.get(index).get_s_id().substring(3))){

                    System.out.println("Sensor: "+requested_sensor.get(i)+" from "+deviceList.get(index).get_d_mac());
                    available_sensor.add(i,deviceList.get(index).get_alive()+"/"+protocol_id+"/"+deviceList.get(index).get_d_mac()+"/"+deviceList.get(index).get_s_id());
                    sensorRequest++;
                    break;
                }
            }
        }
        
        i = 0;
        for (Object o : a) {
            JSONObject sensor = (JSONObject) o;
            Boolean check = (Boolean) sensor.get("check");

            if(check){
            	
                String categoly = (String) sensor.get("categoly");
            	if(available_sensor.get(i) != null){
            		
            		String[] names = available_sensor.get(i).split("/");
                    
                    String topic = "/"+names[1]+"/"+names[2]+"/"+names[3];

            		if(categoly.equals("input")){
            			if(names[0].equals(true))
            				sensor.remove("deviceName");
            				sensor.put("deviceName", topic);
            				System.out.println("use : "+topic);
            			
            		}	
            		else{
            				sensor.remove("deviceName");
        					sensor.put("deviceName", topic);
        					System.out.println("use : "+topic);
            			
            		}
            		i++;
            	}
            }
        }
        }
        if(sensorRequest == num) { //success
            if (runnableInstance.containsKey(sessionId)) {
                Go g = (Go) runnableInstance.get(sessionId);
     //           g.setAliveFlag(false);
                g = null;
                runnableInstance.remove(sessionId);
            }

            Go go = new Go(a);
            Thread t1 = new Thread(go);
            t1.start();
            runnableInstance.put(sessionId, go);
            System.out.print("add " + sessionId + " to HashMap");

            jsonObj.put("success", true);
        } else {
            jsonObj.put("success", false);
        }

        return jsonObj;
    }

    private void retrieveData(String jsonObjString) throws Exception {

        int counter = 0;
        String sensor_alive = "false";

//        JSONParser jsonParser = new JSONParser();
//        JSONObject objDevices = (JSONObject) jsonParser.parse(readerDevicesList);

        //TODO: Get Gateway IP Address

        JSONArray objDevices = (JSONArray) new JSONParser().parse(jsonObjString);


        for (Object first_child : objDevices)
        {
            JSONObject all_device = (JSONObject) first_child;
            protocol_id = Long.parseLong(all_device.get("protocol_id").toString());
            JSONArray child = (JSONArray) all_device.get("children");


            for (Object sec_child : child)
            {
                JSONObject each_device = (JSONObject) sec_child;
                String device_status = (String) each_device.get("device_status");

                if(device_status.equals("online,accepted")){

                    String deviceName = (String) each_device.get("device_name");
                    String deviceMAC = (String) each_device.get("device_identifier");

                    JSONArray children = (JSONArray) each_device.get("children");

                    for (Object sensor : children){
                        //System.out.println("QQ : "+ each_device);
                        JSONObject each_sensor = (JSONObject) sensor;

                        String sensor_name = (String) each_sensor.get("sensor_name");
                        String sensor_identifier = (String) each_sensor.get("sensor_identifier");
                        //sensor_identifier = sensor_identifier.substring(3);
                        String sensor_datetime = (String) each_sensor.get("sensor_datetime");
                        
                        
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        
                        Date date = sdf.parse(sensor_datetime);
                        Date d=new Date();
                        //  System.out.println(date);

                        long now_time = (d.getTime())/1000;
                        long update_time = (date.getTime())/1000;

                       if(now_time - update_time < 30)
                        	sensor_alive = "true";
                       else
                    	    sensor_alive = "false";
                        
                        Device device = new Device(deviceName ,sensor_name ,deviceMAC, sensor_identifier, sensor_alive);
                        deviceList.add(device);

                        counter++;
                    }
                }
            }
        }

    	 counter = 0;
       // Search requested sensors/actuators from the list of available devices
    	 
       int compare = deviceList.size() >= available_sensor.size() ? available_sensor.size() : deviceList.size();
    	 
       for(int index =0; index < compare; index ++){
    	   String deviceType = deviceList.get(index).get_s_id();
    	   //Boolean deviceStatus = deviceList.get(index).get_alive();
    	   
    	   if(deviceType.equals(requested_sensor.get(index))){
    		   available_sensor.add(counter,deviceList.get(index).get_s_id());
    		   counter++;
    		   break;
    	   }
       }
    }
}
