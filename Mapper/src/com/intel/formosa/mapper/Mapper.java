package com.intel.formosa.mapper;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intel.formosa.test.Go;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.paho.client.mqttv3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Mapper implements MqttCallback {

    private final String gateway_ip = "localhost";
    private final int gateway_port = 8080;

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
//    private ArrayList<String> available_sensor = new ArrayList<String>();
    private ArrayList<Device> available_sensor = new ArrayList<Device>();

    private long protocol_id = 0;
    private int numOfRequest = 0;

    private ArrayList<Device> deviceList = new ArrayList<Device>();
//    private ArrayList<Device> acList = new ArrayList<Device>();
    public static List<List<String>> acList = new ArrayList<List<String>>();

    private HashMap runnableInstance = new HashMap();

    private ArrayList<Computer> availableWorkers = new ArrayList<Computer>();
    private ArrayList<Computer> selectedWorkers = new ArrayList<Computer>();

    private String hostIpAddress = "";

    private static String host, role;
    private boolean isWaiting = true;

    private MqttClient mqttClient;

    private ConfigParams config;

    private boolean isFirst = true;



    public Mapper() {
        try {
            hostIpAddress = getHostIpAddress();
            config = new ConfigParams();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** For Servlet use while bootstrapping
     *  publish self information (cpu, mem)
     * */
    public static void startDiscoverable () {
        new Thread(new Discoverable()).start();
    }

    /** For Servlet using while bootstrapping
     *  listening the Master broadcast aliveRequest topic, and response self IP address
     * */
    public static void initTaskOfRole () throws IOException, MqttException {
        ConfigParams config = new ConfigParams();
        /** User should configure slave by himself */
//        String role = config.getParameter("role", "master");
        String mqttBroker = config.getParameter("mqttBroker", "tcp://localhost:1883");

        /** Each computer may be master or slave, so all need to subscribe alive request */
        new Thread(new SubscribeAliveRequest(mqttBroker)).start();
    }


    public static void main (String[] args) throws Exception{
    // ===========================test======================
//        new Mapper().broadcastAliveRequest(new MqttClient("tcp://localhost:1883", MqttClient.generateClientId()));
        initTaskOfRole();

//        startDiscoverable();
    // ===========================test======================
        JSONObject result;


        Mapper mapper = new Mapper();
        JSONObject jsonObj = (JSONObject) new JSONParser().parse(new FileReader("input.json"));
        JSONObject jsonObj2 = (JSONObject) new JSONParser().parse(new FileReader("input2.json"));

        /** the parameter of run() should be the JSON string passed from Web */
        result =  mapper.run(jsonObj.toJSONString());

        mapper.run(jsonObj2.toJSONString());

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
    public String retrieveDevicesList (String baseUrl, String path) throws IOException {
        System.out.println("token: " + token);
        String strToken ="";
        String data ="";

        BufferedReader rd = null;

        System.out.println("url: " +  baseUrl + path);
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(baseUrl+ path);

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

    public String retrieveDevicesList2() throws IOException, ParseException {
        JSONArray jsonArray = (JSONArray) new JSONParser().parse(new FileReader("api_info.json"));
        System.out.println(jsonArray.toJSONString());
        return jsonArray.toJSONString();
    }

    /** For json local testing */
    public JSONObject run1 (String jsonObjString) throws ParseException {
        JSONObject jsonObj = (JSONObject) new JSONParser().parse(jsonObjString);
        return jsonObj;
    }

    @SuppressWarnings("deprecation")
    public JSONObject run (String jsonObjString) throws Exception {

        int num = 0;
        boolean pass = true;

        String sessionId = null;

        /** clear the devices list find previously */
        requested_sensor.clear();

        for (Device d : available_sensor) {
            d = null;
        }
        available_sensor.clear();


        /** Node RED User Input **/
        JSONObject userInputJsonObj = (JSONObject) new JSONParser().parse(jsonObjString);

        sessionId = userInputJsonObj.get("session_id").toString();

        JSONArray flow = (JSONArray) userInputJsonObj.get("flow");

        Boolean isMaster = (Boolean) userInputJsonObj.get("is_master");


        /** Search for the requested sensor */
        for (Object o : flow) {
            JSONObject sensor = (JSONObject) o;
            Boolean check = (Boolean) sensor.get("check");

            if (check){
                String deviceType = (String) sensor.get("deviceType");
                requested_sensor.add(deviceType);
                num++;
            }
        }


        /** get AC 1.1 authentication token */
        generateToken();


        if (isMaster) {

            if (mqttClient == null) {
                String mqttBroker = config.getParameter("mqttBroker", "tcp://localhost:1883");
                mqttClient = new MqttClient(mqttBroker, MqttClient.generateClientId());
            }

            for (Computer c : availableWorkers) {
                c = null;
            }
            availableWorkers.clear();
            
            broadcastAliveRequest(mqttClient);

            //TODO: Pass the list of discoverable device to generate available devices
            generateAvailableDevices(availableWorkers);

            //TODO: Master assign job
            if (availableWorkers.size() > 0) {
            	assignJob(userInputJsonObj, sessionId);
            }

            System.out.println("userInputJsonObj after assign : ");
            System.out.println(userInputJsonObj);
            System.out.println("---------------------------------");

            System.out.println(flow);

            if (available_sensor.size() == requested_sensor.size()) { //success
                if (runnableInstance.containsKey(sessionId)) {
                    System.out.println("remove flow: " + sessionId);
                    Go g = (Go) runnableInstance.get(sessionId);
                    g.setAliveFlag(false);
                    g = null;
                    runnableInstance.remove(sessionId);

                    for (int j = 0; j < acList.size(); j++) {
                        if (sessionId.equals(acList.get(j).get(1))) {
                            acList.remove(j);
                        }
                    }
                }

                System.out.println("acList size: " + acList.size());

                for (int j = 0; j < acList.size() - 1; j++) {
                    if (acList.get(j + 1) != null) {
                        if (acList.get(j).get(0).equals(acList.get(j + 1).get(0))) {
                            System.out.println("monitor");
                            Monitor monitor = new Monitor (acList.get(j).get(0));
                            Thread t = new Thread(monitor);
                            t.start();
                        }
                    }
                }

                Go go = new Go(flow, isMaster, sessionId);
                Thread t1 = new Thread(go);
                t1.start();
                runnableInstance.put(sessionId, go);
                System.out.println("Add " + sessionId + " to HashMap");

                userInputJsonObj.put("success", true);
            } else {
                userInputJsonObj.put("success", false);
            }

        } else {

            if (runnableInstance.containsKey(sessionId)) {
                Go g = (Go) runnableInstance.get(sessionId);
                g.setAliveFlag(false);
                g = null;
                runnableInstance.remove(sessionId);
            }

            Go go = new Go(flow, isMaster, sessionId);
            Thread t1 = new Thread(go);
            t1.start();
            runnableInstance.put(sessionId, go);
            System.out.print("add " + sessionId + " to HashMap");

            userInputJsonObj.put("success", true);
            userInputJsonObj.remove("type");
            userInputJsonObj.put("type", "resp");

        }

        System.out.println("final userInputJsonObj: ");
        System.out.println(userInputJsonObj);

        return userInputJsonObj;

    }

    private void retrieveData (String jsonArrayString, String gatewayIP) throws Exception {

        boolean sensor_alive;

        JSONArray objDevices = (JSONArray) new JSONParser().parse(jsonArrayString);

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
                        Date d = new Date();

                        long now_time = d.getTime() / 1000;
                        long update_time = date.getTime() / 1000;

                        if (now_time - update_time < 30)
                            sensor_alive = true;
                        else
                            sensor_alive = false;

                        Device device = new Device(gatewayIP, deviceName ,sensor_name ,deviceMAC, sensor_identifier, sensor_alive);
                        deviceList.add(device);

                    }
                }
            }
        }
    }


    private void generateAvailableDevices (ArrayList <Computer> availableWorkers) throws Exception{
        //TODO: This function with parameter = discoverable array list
        //TODO: For Loop - loop the discoverable item to retrieve available sensor/actuator from each gateway

        for (Computer c : availableWorkers) {

            String url = "http://" + c.getIp() + ":8080";
            String allInfo;

            try {
                allInfo = retrieveDevicesList(url, uriDeviceInfo);
                /** debug use*/
//                allInfo = retrieveDevicesList2();

                System.out.println("allinfo: ");
                System.out.println(allInfo);

                retrieveData(allInfo, c.getIp());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private void assignJob (JSONObject userInputJsonObj, String sessionId) throws IOException {

        JSONArray flow = (JSONArray) userInputJsonObj.get("flow");

        System.out.println("before assgin: ");
        System.out.println(flow);

        int index;
        int i = 0;
        boolean pass = true;
        boolean exist = false;

        for (index = 0; index < deviceList.size(); index++) {
            System.out.println("alive: " + deviceList.get(index).get_alive());
            if (!deviceList.get(index).get_alive()) {
                pass = false;
                break;
            }
        }

        if (pass) {
            for (i = 0; i < requested_sensor.size(); i++) {

                for (index = 0; index < deviceList.size(); index++) {

                    if (requested_sensor.get(i).equals(deviceList.get(index).get_s_id().substring(3))) {

                        System.out.println("Sensor: "+requested_sensor.get(i)+" from "+ deviceList.get(index).get_d_mac());

                        available_sensor.add(i, deviceList.get(index));
                        break;
                    }
                }
            }

            i = 0;

            for (Object o : flow) {
                JSONObject sensor = (JSONObject) o;
                Boolean check = (Boolean) sensor.get("check");

                System.out.println("check: " + check);

                if (check) {
                    String category = (String) sensor.get("category");

                    if (available_sensor.get(i) != null) {

                        Device device = available_sensor.get(i);

                        System.out.println("sensor" + i + ": " + available_sensor.get(i).get_s_name());

                        // TODO: Change format - <protocol>://<ip_address>[<path_to_resource>]
                        // tcp://192.168.0.1:1883/pub/1/000000000002/s_illuminance_0
                        String topic = "tcp://" + device.get_gatewayIP() + ":1883" + "/pub/1/" + device.get_d_mac() + "/" + device.get_s_id();

                        if (category.equals("input")) {
                            if (device.get_alive()) {
                                sensor.remove("deviceName");
                            }
                            sensor.put("deviceName", topic);
                        } else if (category.equals("output")) {
                            sensor.remove("deviceName");
                            sensor.put("deviceName", topic);

                            /** record the reuse actuator */
                            for(int j = 0; j < acList.size(); j++) {
                                if (topic.equals(acList.get(j).get(0))) {
                                    List<String> acListData = new ArrayList<String>();
                                    acListData.add(topic);
                                    acListData.add(sessionId);
                                    acList.add(acListData);
                                    exist = true;
                                    break;
                                }
                            }

                            if(!exist){
                                List<String> acListData = new ArrayList<String>();
                                acListData.add(topic);
                                acListData.add(sessionId);
                                acList.add(acListData);
                            }

                        } else {
                            sensor.remove("deviceName");
                            sensor.put("deviceName", topic);
                        }

                        System.out.println("use : "+topic);

                        i++;
                    }
                }

                /** Assign the job to workers */
                // TODO: Need to apply selection mechanism
                System.out.println("availableWorkers.size: " + availableWorkers.size());
                int randomNumber = new Random().nextInt(availableWorkers.size());
                System.out.println("random: " + randomNumber);
                Computer computer = availableWorkers.get(randomNumber);
                computer.setTaskSessionId(sessionId);
                sensor.remove("runningHost");
                sensor.put("runningHost", computer.getIp());
                
                if (!selectedWorkers.contains(computer)) {
                	selectedWorkers.add(computer);
                }

            }
            userInputJsonObj.remove("is_master");
            userInputJsonObj.put("is_master", false);


            /** Ask selected workers to start working, expect self */
            /** uncomment this when you have multiple computers */
            System.out.println("selectedWorkers size: " + selectedWorkers.size());
            for (Computer c: selectedWorkers) {
                /** If the selected worker is not myself, fire it the work */
                if (!c.getIp().equals(hostIpAddress)) {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost("http://" + c.getIp() + ":8081/Mapper-Servlet/Mappers");
                    StringEntity params = new StringEntity(userInputJsonObj.toJSONString());

                    post.addHeader("content-type", "application/json");
                    post.setEntity(params);
                    
                    System.out.println("@@@client post: " + c.getIp());
                    HttpResponse response = client.execute(post);
                }
            }

            /** set is_master back to true */
            userInputJsonObj.remove("is_master");
            userInputJsonObj.put("is_master", true);
        }
    }

    public void stopRuleEngine (String sessionId) throws ClientProtocolException, IOException {
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

            /** stop the slave worker that have this sessionId task */
              for (Computer c : selectedWorkers) {
                  if (c.getTaskSessionId().equals(sessionId)) {
                      HttpClient client = new DefaultHttpClient();
                      HttpDelete delete = new HttpDelete("http://" + c.getIp() + ":8081/Mapper-Servlet/" + c.getTaskSessionId());
                      client.execute(delete);
                      selectedWorkers.remove(c);
                      c = null;
                  }
              }

        } else {
            System.out.println("[stopRuleEngine] not entry ");
        }
    }

    public static String getHostIpAddress() throws SocketException {
        /** get the host ip address */
        Enumeration<NetworkInterface> networkInterface = NetworkInterface.getNetworkInterfaces();

        String hostIpAddress = "";

        while (networkInterface.hasMoreElements()) {
            NetworkInterface e = networkInterface.nextElement();

            Enumeration<InetAddress> inetAddress = e.getInetAddresses();
            while (inetAddress.hasMoreElements()) {
                InetAddress addr = inetAddress.nextElement();
                String candicateIp = addr.getHostAddress();

                /** ignore mac address */
                if (candicateIp.split("\\.").length == 4) {
                    if (!candicateIp.equals("127.0.0.1")) {
                        hostIpAddress = candicateIp;
                    }
                }
            }
        }

        return hostIpAddress;
    }

    public static boolean isPortInUse (int port) {
        Socket socket;
        boolean isInUse;
        try {
            socket = new Socket(host, port);
            socket.close();
            isInUse = true;
        } catch (Exception e) {
            isInUse = false;
        }
        return isInUse;
    }

    public void broadcastAliveRequest(MqttClient mqttClient){
        String broadcastTopic = "/ping/0/request";
        String message ="Master requests the status of slave";

        try {
            if (!mqttClient.isConnected()) {
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                mqttClient.connect(connOpts);
                mqttClient.setCallback(this);
            }

            int subQoS = 0;
            System.out.println("subscribe /ping/0/#");
            mqttClient.subscribe("/ping/0/#", subQoS);

            MqttMessage broadcastMessage = new MqttMessage(message.getBytes());
            broadcastMessage.setQos(1);
            mqttClient.publish(broadcastTopic, broadcastMessage);

            /** if is Mapper.run() is call first time, then wait 3 sec to get response from all slave */
            /**
             * If we clear the available list each time Mapper call the run()
             * I maybe need to wait n second to get response message and store to available list
             * */
            Thread.sleep(3000);
            System.out.println("thread awake!");

        } catch (MqttException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }




    @Override
    public void connectionLost (Throwable throwable) {
        System.out.println("Mqtt connection lost");

    }

    /** Callback function: Use to assign alive computer to availableList */
    @Override
    public void messageArrived (String topic, MqttMessage mqttMessage) throws Exception {
        System.out.println("[messageArrived] topic: " + topic + ", message: " + mqttMessage);

        String topicDiscoverable = "/ping/0/";
        String msgDiscoverable = "I am alive";
        String topicAliveRequest = "/ping/0/request";

        if (topic.contains(topicDiscoverable)) {

            /** the response of aliveRequest */
            if (mqttMessage.toString().contains(msgDiscoverable)) {
                String ipAddress = topic.replace(topicDiscoverable, "").trim();

                /** Check if the computer is already in availableWorkers list */
                boolean exist = false;
                for (Computer c : availableWorkers) {
                    if (c.getIp().equals(ipAddress)) {
                        exist = true;
                        break;
                    }
                }
                if (!exist) {
                    System.out.println("[messageArrived] Add computer to available list");
                    availableWorkers.add(new Computer(ipAddress));
                }
            } else {
                /** If not "I am alive", message is either cpu or mem information */

                /** Regular expression. match example: "/ping/0/140.113.72.8/cpu" */
                String patternStr = "/ping/0/[0-9]*\\.[0-9]*\\.[0-9]*\\.[0-9]*/[a-zA-Z]*";
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(topic);
                boolean matchFound = matcher.find();
                System.out.println("match:" + matchFound );
                if (matchFound) {
                    String[] topicSplit = topic.split("/");

                    for (Computer c : availableWorkers) {
                        if (c.getIp().equals(topicSplit[3])) {
                            String type = topicSplit[4].toLowerCase();

                            if (type.equals("cpu")) {
                                c.setCpuIdle(Long.parseLong(mqttMessage.toString()));
                            } else if (type.equals("mem")) {
                                c.setMemFree(Long.parseLong(mqttMessage.toString()));
                            }

                        }
                    }

                }
            }

        }
    }

    @Override
    public void deliveryComplete (IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
