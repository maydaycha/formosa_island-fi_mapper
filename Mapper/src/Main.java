

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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class Main {

	private static final String gateway_ip = "192.168.184.130";
	private static final String gateway_port = "8080";
	private static final String gateway_url = "http://" + gateway_ip + ":" + gateway_port;
	
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_PASSWORD = "password";
    private static final String FILED_AUTHORIZATION ="Authorization";
    private static final String FILED_STRTOKEN = "bearer ";
    
	private static final String username = "admin";
	private static final String password = "admin";
	private static String token;
	
	private static final String uriDeviceInfo = "/api/provision/devices/info/";
	
	
    private final String USER_AGENT = "Mozilla/5.0";
    private HashMap runnableInstance = new HashMap();
    public static String[] requested_sensor = new String[20];
    public static String[] available_sensor = new String[20];
    private String ip_address = "127.0.0.1";
    private String port ="8080";
    private long protocol_id = 0;

    class Device {
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

    	static  Device[] arrDevice = new Device[50]; 
    	static  ArrayList<Device> deviceList = new ArrayList<Device>();
     
	
	public static void main(String[] args) throws Exception{
		
		String data = "";
		JSONObject result = null;
		Main conn = new Main();
		conn.generateToken();
		
		
		// retrieve all devices info
		data = conn.retrieveDevicesList(uriDeviceInfo);
    	// retrieve SPECIFIC device info
    	String deviceMAC = "00137a000001b448";
    //	conn.retrieveDevicesList(uriDeviceInfo + deviceMAC); 
    	
    	
		
		
		
		result = conn.run(data);
		
	}
	
	// Generate token from Amelia Creek 1.1
	public void generateToken(){
		String requestURL = "http://"+gateway_ip+":"+gateway_port+"/user/login/token";
		
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(requestURL);
		
		try{
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair(FIELD_USERNAME, username));
			nameValuePairs.add(new BasicNameValuePair(FIELD_PASSWORD, password));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String strToken = rd.readLine();
	        if(strToken != null)
	        {
	        	JSONParser jsonParser = new JSONParser();
		        JSONObject jsonObj;
		        
		        try{
		        	jsonObj = (JSONObject) jsonParser.parse(strToken);
		        	token = jsonObj.get("token").toString();
		        }
		        catch(ParseException e){
		        	e.printStackTrace();
		        }
	        }
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	// Retrieve devices data via Amelia Creek 1.1 API
	public String retrieveDevicesList(String uri){
		
		String strToken ="qww";
		String data ="";
		
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(gateway_url+uri);
		
		try{
			get.addHeader(FILED_AUTHORIZATION, FILED_STRTOKEN+ token);

			HttpResponse response = client.execute(get);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			
			while((strToken = rd.readLine()) != null){
				data = strToken;
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return data;
	}
	
	
	
	
	public JSONObject run(String jsonObjString) throws Exception {
//	    public static void main(String[] args) throws Exception {
	    	
	        int num = 0;
	        int i = 0;
	        int index = 0;
	        int sensorRequest = 0;  // number of requested sensors we found
	        String sessionId = null;

	        JSONObject jsonObj = (JSONObject) new JSONParser().parse(new FileReader("C:\\Users\\renjiewu\\Downloads\\in_from_ui.json"));
	        
//	        JSONParser parser = new JSONParser();
//	        JSONObject jsonObj = (JSONObject)parser.parse(jsonObjString);

	        
	        jsonObj.remove("type");
	        jsonObj.put("type", "resp");
	        sessionId = jsonObj.get("session_id").toString();
	        
	        JSONArray a = (JSONArray) jsonObj.get("flow");

	        // Search for the requested sensor
	        for (Object o : a)   
	        {
	            JSONObject sensor = (JSONObject) o;
	            Boolean check = (Boolean) sensor.get("check");

	            if(check){
	            	
	                String deviceType = (String) sensor.get("deviceType");
	                requested_sensor[num] = deviceType;
	                
	                num++;             
	            }
	        }

	        retrieveData(jsonObjString);

	        for(i = 0;i < num;i++){
	        	
	        	for(index = 0; index<deviceList.size();index++){
	        		   	
	        		if(requested_sensor[i].equals(deviceList.get(index).get_s_id())){
	        			
	        			System.out.println("Sensor: "+requested_sensor[i]+" from "+deviceList.get(index).get_d_mac());
	        			available_sensor[i] = "/"+protocol_id+"/"+deviceList.get(index).get_d_mac()+"/"+deviceList.get(index).get_s_id();
	        			sensorRequest++;
	        			break;

	        		}        
	        	}
	        }
	        i = 0;
	        for (Object o : a)
	        {
	            JSONObject sensor = (JSONObject) o;
	            String check = (String) sensor.get("deviceName");

	            if(check != null){
	                sensor.remove("deviceName");
	                
	                sensor.put("deviceName", available_sensor[i]);
	                System.out.println(available_sensor[i]);
	                i++;
	            }
	        }
	        
	        
	        if(sensorRequest == num){ //success
	            jsonObj.put("success", true);

//	            if (runnableInstance.containsKey(sessionId)) {
//	                Go g = (Go) runnableInstance.get(sessionId);
//	                g.setAliveFlag(false);
//	                g = null;
//	                runnableInstance.remove(sessionId);
//	            }
	//
//	            Go go = new Go(a);
//	            Thread t1 = new Thread(go);
//	            t1.start();
//	            runnableInstance.put(sessionId, go);
//	            System.out.print("add " + sessionId + " to HashMap");

	        } else{
	            jsonObj.put("success", false);
	        }
	        return jsonObj;
	    }
	
	
	
    private void retrieveData(String jsonObjString) throws Exception {
    	
    	int counter = 0;

//        JSONParser jsonParser = new JSONParser();
//        JSONObject objDevices = (JSONObject) jsonParser.parse(readerDevicesList);
        
        //TODO: Get Gateway IP Address
        
    	JSONArray objDevices = (JSONArray) new JSONParser().parse(jsonObjString);
    	
   	
    	 for (Object first_child : objDevices)   
         {
    		 JSONObject all_device = (JSONObject) first_child;
    		 protocol_id = (long) all_device.get("protocol_id");
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
                    	 	sensor_identifier = sensor_identifier.substring(3);
                    	 	
                    	 	Device device = new Device(deviceName ,sensor_name ,deviceMAC, sensor_identifier);
                            deviceList.add(device);           	 	
                    	 	
                    	 	counter++;
                	 	}              	 	
            	 	}          	 	
             }
         }
//    	 counter = 0;
//       // Search requested sensors/actuators from the list of available devices
//       for(int index =0; index < deviceList.size(); index ++){
//    	   String deviceType = deviceList.get(index).get_s_id();
////    	   Boolean deviceStatus = deviceList.get(index).getAlive();
//
//    	   if(deviceType.equals(available_sensor[index])){
//    		   available_sensor[counter] = deviceList.get(index).get_s_id();
//    		   counter++;
//    		   break;
//    	   }
//       }
    }
}
