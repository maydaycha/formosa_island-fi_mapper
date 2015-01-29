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

import com.intel.formosa.test.Go;
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

    private final String gateway_ip = "localhost";
    private final int gateway_port = 8080;
    private final String gateway_url = "http://" + gateway_ip + ":" + gateway_port;

    private final String FIELD_USERNAME = "username";
    private final String FIELD_PASSWORD = "password";
    private final String FILED_AUTHORIZATION ="Authorization";
    private final String FILED_STRTOKEN = "bearer ";

    private final String uriDeviceInfo = "/api/provision/devices/info/";
    private final String USER_AGENT = "Mozilla/5.0";

    private final String username = "admin";
    private final String password = "admin";

    private String token;

    private ArrayList<String> requested_sensor = new ArrayList<String>();
    private ArrayList<String> available_sensor = new ArrayList<String>();
    private ArrayList<String> requested_actuator = new ArrayList<String>();

    private long protocol_id = 0;
    private int numOfRequest = 0;

    //private Device[] arrDevice = new Device[50];
    private ArrayList<Device> deviceList = new ArrayList<Device>();
    private ArrayList<Device> acList = new ArrayList<Device>();

    private HashMap runnableInstance = new HashMap();


    public static void startDiscoverable () {
        new Thread(new Discoverable()).start();
    }


    public static void main (String[] args) throws Exception{

        JSONObject result;
        Mapper conn = new Mapper();

        JSONObject jsonObj = (JSONObject) new JSONParser().parse(new FileReader("input.json"));

        /** the parameter of run() should be the JSON string passed from Web */
        result = conn.run(jsonObj.toJSONString());

        System.out.println(result);
    }

    // Generate token from Amelia Creek 1.1
    public void generateToken() {

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
    public String retrieveDevicesList(String ip, String uri) throws IOException {

        String strToken ="";
        String data ="";

        BufferedReader rd = null;

        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(ip+uri);

        try {
            get.addHeader(FILED_AUTHORIZATION, FILED_STRTOKEN + token);

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

    /** For json local testing */
    public JSONObject run1 (String jsonObjString) throws ParseException {
        JSONObject jsonObj = (JSONObject) new JSONParser().parse(jsonObjString);
        return jsonObj;
    }

    public JSONObject run (String jsonObjString) throws Exception {

        int num = 0;
        int i = 0;
        int index = 0;
        boolean pass = true;
        int sensorRequest = 0;  // number of requested sensors we found
        String sessionId = null;

        /** clear the devices list find previously */
        requested_sensor.clear();
        available_sensor.clear();


        /** Node RED User Input **/
        JSONObject userInputJsonObj = (JSONObject) new JSONParser().parse(jsonObjString);
        userInputJsonObj.remove("type");
        userInputJsonObj.put("type", "resp");
        sessionId = userInputJsonObj.get("session_id").toString();

        JSONArray flow = (JSONArray) userInputJsonObj.get("flow");

        /** Search for the requested sensor */
        for (Object o : flow) {
            JSONObject sensor = (JSONObject) o;
            Boolean check = (Boolean) sensor.get("check");

            if (check){
                String deviceType = (String) sensor.get("deviceType");
                requested_sensor.add(numOfRequest, deviceType);
                numOfRequest++;
            }
        }

        /** get AC 1.1 authentication token */
        generateToken();


        //TODO 1: Call Mqtt to listen to discoverable topic. Store discovered pc into list
        ArrayList<String> discover_list = new ArrayList<String>();
        discover_list.add(gateway_ip);

        //TODO 2: Identify role - will stored into list
        String role = "master";	// remove it once TODO2 done.

        //TODO 3: Different process for master and slave
        if (role.equals("master")) {
            //TODO: Pass the list of discoverable device to generate available devices
            generateAvailableDevices(discover_list);

            //TODO: Master assign job
            assignJob(flow);
            System.out.println("flow after assign : ");
            System.out.println(flow);

            //TODO: Create while loop - waiting for acknowledgement from slave
            //TODO: Run rule-engine after receive all acknowledgement


        	/*if(sensorRequest == numOfRequest) { //success
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

                userInputJsonObj.put("success", true);
            } else {
                userInputJsonObj.put("success", false);
            }*/

        }
        else{
            //TODO: Add while loop - waiting for job assigned -listen via http
            //TODO: Once received job, begin mapping
            //TODO: Return acknowledgement back to master

        }

        return userInputJsonObj;
    }

    private void retrieveData(String jsonObjString) throws Exception {

        int counter = 0;
        String sensor_alive = "false";

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


    private void generateAvailableDevices (ArrayList <String> ListOfDiscoverableDevice) throws Exception{
        //TODO: This function with parameter = discoverable array list
        //TODO: For Loop - loop the discoverable item to retrieve available sensor/actuator from each gateway

        int numOfPC = 2; //TODO: need to replace with size of array
        int index = 0;

        for(index=0; index<numOfPC; index++){
            String url = ListOfDiscoverableDevice.get(index).toString();
            String allInfo;

            try {
                allInfo = retrieveDevicesList(url, uriDeviceInfo);
                retrieveData(allInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    private void assignJob (JSONArray flow) {
        //TODO: hardcode the job

        int index;
        int i = 0;
        boolean pass = true;
        int sensorRequest = 0;  // number of requested sensors we found


        for (index = 0; index < deviceList.size(); index++) {
            if (deviceList.get(index).get_alive().equals("true"))
                i++;
        }

        if(numOfRequest > i) {
            pass = false;
        }

        if (pass) {

            for (i = 0;i < numOfRequest;i++) {

                for (index = 0; index<deviceList.size(); index++) {
                    if(requested_sensor.get(i).equals(deviceList.get(index).get_s_id().substring(3))){

                        System.out.println("Sensor: "+requested_sensor.get(i)+" from "+deviceList.get(index).get_d_mac());

                        available_sensor.add(i,deviceList.get(index).get_alive()+"/"+protocol_id+"/"+deviceList.get(index).get_d_mac()+"/"+deviceList.get(index).get_s_id());
                        sensorRequest++;
                        break;
                    }
                }
            }

            i = 0;

            for (Object o : flow) {
                JSONObject sensor = (JSONObject) o;
                Boolean check = (Boolean) sensor.get("check");

                if(check){
                    String categoly = (String) sensor.get("categoly");
                    if(available_sensor.get(i) != null){

                        String[] names = available_sensor.get(i).split("/");


                        //TODO: Change format - <protocol>://<ip_address>[<path_to_resource>]
                        //tcp://192.168.0.1:1883/pub/1/000000000002/s_illuminance_0

                        String topic = "/"+names[1]+"/"+names[2]+"/"+names[3];

                        if (categoly.equals("input")) {
                            if (names[0].equals(true)) {
                                sensor.remove("deviceName");
                            }
                            sensor.put("deviceName", topic);
                            System.out.println("use : "+topic);

                        } else {
                            sensor.remove("deviceName");
                            sensor.put("deviceName", topic);
                            System.out.println("use : "+topic);
                        }

                        i++;
                    }
                }
            }
        }
    }

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


}
