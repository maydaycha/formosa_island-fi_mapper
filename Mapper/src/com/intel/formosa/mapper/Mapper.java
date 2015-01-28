package com.intel.formosa.mapper;

import com.intel.formosa.test.Go;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Mapper {

    private final String gateway_ip = "127.0.0.1";
    private final int gateway_port = 8080;
    private final String gateway_url = "http://" + gateway_ip + ":" + gateway_port;

    private final String FIELD_USERNAME = "username";
    private final String FIELD_PASSWORD = "password";
    private final String FILED_AUTHORIZATION ="Authorization";
    private final String FILED_STRTOKEN = "bearer ";

    private final String uriDeviceInfo = "/api/provision/devices/info/";
    private final String USER_AGENT = "Mozilla/5.0";

    private final String mqttBroker = "tcp://localhost:1883";

    private final String username = "admin";
    private final String password = "admin";

    private String token;

    public String[] requested_sensor = new String[20];
    public String[] available_sensor = new String[20];

    private long protocol_id = 0;

    private ArrayList<Device> deviceList = new ArrayList<Device>();

    private HashMap runnableInstance = new HashMap();

    private MqttClient mqttClient;

    public Mapper() throws MqttException {
        mqttClient = new MqttClient(mqttBroker, MqttClient.generateClientId());
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        mqttClient.connect(connOpts);
    }


    public static void main (String[] args) throws Exception{
        JSONObject result;
        Mapper mapper = new Mapper();

        JSONObject jsonObj = (JSONObject) new JSONParser().parse(new FileReader("input.json"));

        /** the parameter of run() should be the JSON string passed from Web */
        result = mapper.run(jsonObj.toJSONString());

        System.out.println(result);
    }

    public static void startDiscoverable () {
        new Thread(new Discovery()).start();
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


    public JSONObject run1(String jsonObjString) throws Exception {
        JSONObject jsonObj = (JSONObject) new JSONParser().parse(jsonObjString);
        HttpClient client = new DefaultHttpClient();
        return jsonObj;
    }




    public JSONObject run(String jsonObjString) throws Exception {

        int num = 0;
        int i = 0;
        int index = 0;
        int sensorRequest = 0;  // number of requested sensors we found
        String sessionId = null;


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
                requested_sensor[num] = deviceType;
                num++;
            }
        }

        /** retrieve all devices info */
        retrieveData(retrieveDevicesList(uriDeviceInfo));

        for(i = 0;i < num;i++){

            for(index = 0; index<deviceList.size();index++){

                if(requested_sensor[i].equals(deviceList.get(index).get_s_id().substring(3))){

                    System.out.println("Sensor: "+requested_sensor[i]+" from "+deviceList.get(index).get_d_mac());
                    available_sensor[i] = "/"+protocol_id+"/"+deviceList.get(index).get_d_mac()+"/"+deviceList.get(index).get_s_id();
                    sensorRequest++;
                    break;

                }
            }
        }

        i = 0;
        for (Object o : a) {
            JSONObject sensor = (JSONObject) o;
            String check = (String) sensor.get("deviceName");

            if(check != null){
                sensor.remove("deviceName");
                sensor.put("deviceName", available_sensor[i]);
                i++;
            }
        }


        if(sensorRequest == num) { //success

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

            jsonObj.put("success", true);
        } else {
            jsonObj.put("success", false);
        }

        return jsonObj;
    }

    private void retrieveData(String jsonObjString) throws Exception {

        int counter = 0;

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

                        Device device = new Device(deviceName ,sensor_name ,deviceMAC, sensor_identifier);
                        deviceList.add(device);

                        counter++;
                    }
                }
            }
        }

        counter = 0;
        // Search requested sensors/actuators from the list of available devices
        for(int index =0; index < deviceList.size(); index ++){
            String deviceType = deviceList.get(index).get_s_id();
//    	   Boolean deviceStatus = deviceList.get(index).getAlive();

            if(deviceType.equals(available_sensor[index])){
                available_sensor[counter] = deviceList.get(index).get_s_id();
                counter++;
                break;
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
