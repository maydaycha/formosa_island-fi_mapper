package com.intel.core;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Maydaycha on 1/2/15.
 */
public class Mapper {
    private final String USER_AGENT = "Mozilla/5.0";

    Device[] arr = new Device[50];  // new stands for create an array object

    public String run() throws Exception {

//        Main http = new Main();
        String input = null;
        int num = 0;
        int i = 0;
        boolean x = true;
        //	System.out.println("start");

	/*	try{
			BufferedReader br =
	                      new BufferedReader(new InputStreamReader(System.in));

			//while((input=br.readLine())!=null && x){
			input=br.readLine();
				System.out.println(input);
				System.out.println("XX");
				x = false;
			//}

			System.out.println("YY");
		}catch(IOException io){
			io.printStackTrace();
		} */

        //	JSONParser parser = new JSONParser();



        //    Object jsonObj = parser.parse(new FileReader(
        //            "C:\\Users\\renjiewu\\Downloads\\input.js"));

        JSONObject jsonObj = (JSONObject) new JSONParser().parse(new FileReader("./result1.json"));
        //        System.out.println("AAA"+jsonObj);
        //      JSONObject jsonObj = (JSONObject) new JSONParser().parse(input);

        jsonObj.remove("type");
        jsonObj.put("type", "resp");
        JSONArray a = (JSONArray) jsonObj.get("flow");
        //      String flow = (String) jsonObj.get("flow");

        //      JSONParser parser2 = new JSONParser();

        //	  JSONArray a = (JSONArray) parser2.parse(flow);

        for (Object o : a)
        {
            JSONObject sensor = (JSONObject) o;

            Boolean check = (Boolean) sensor.get("check");


            if(check){
                String type = (String) sensor.get("type");
                parameters.need_sensor[num] = type;
                //	    	System.out.println("sensor"+num+":"+com.intel.core.parameters.need_sensor[num]);
                num++;
            }
        }

        int wehave = sendGet();

        //	System.out.println("wehave = "+wehave);
        //	System.out.println("num = " + num);

        for (Object o : a)
        {
            JSONObject sensor = (JSONObject) o;

            sensor.remove("deviceName");
            sensor.put("deviceName", parameters.have_sensor[i]);
            i++;

            // System.out.println("sensor"+num+":"+com.intel.core.parameters.need_sensor[num]);
            // num++;
        }

        if(wehave == num){ //success
            jsonObj.put("success", "true");
        }
        else{
            jsonObj.put("success", "false");
        }


	        /*

		 FileWriter file = new FileWriter("C:\\Users\\renjiewu\\Downloads\\qq.js");
	        try {
	            file.write(jsonObj.toString());
	            System.out.println("Successfully Copied JSON Object to File...");


	        } catch (IOException e) {
	            e.printStackTrace();

	        } finally {
	            file.flush();
	            file.close();
	        } */

        try {
            BufferedWriter log = new BufferedWriter(new OutputStreamWriter(System.out));

            log.write(jsonObj.toString());
            log.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("\nTesting 2 - Send Http POST request");
        //http.sendPost();

        return jsonObj.toJSONString();

    }


    // HTTP GET request
    private int sendGet() throws Exception {


//		String url_broker = "http://192.168.184.129:8000/wsbroker/api/networks";
        String url_broker = "http://127.0.0.1:8000/wsbroker/api/networks";

        int index_sa = 0;
        int n = 0;
        int m = 0;
        int j = 0;
        int num_s = 0;
        int flag = 0;
        String d_name = null;
        String d_type = null;
        String d_mac = null;
        String d_address = null;
        Boolean d_alive = null;
        String MSB = null;
        String update = null;
        String IP = null;
        int[] need2use = new int[50];

        URL obj_broker = new URL(url_broker);


        HttpURLConnection con_broker = (HttpURLConnection) obj_broker.openConnection();

        // optional default is GET
        con_broker.setRequestMethod("GET");

        //add request header

        con_broker.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode_broker = con_broker.getResponseCode();
//		System.out.println("Response Code for broker : " + responseCode_broker);


        BufferedReader in_broker = new BufferedReader(
                new InputStreamReader(con_broker.getInputStream()));
        String inputLine_broker = null;
        String network_jason = null;
        StringBuffer response_broker = new StringBuffer();


        while ((inputLine_broker = in_broker.readLine()) != null) {
            network_jason = inputLine_broker + " ";
            response_broker.append(inputLine_broker);
            //		System.out.println("FOR = " + inputLine_broker);

            for( m = 0; m < inputLine_broker.length() ; m++){

                if(inputLine_broker.regionMatches(m, "ipaddress", 0, 9) == true){  // record update time
                    IP = inputLine_broker.substring(m + 13, m+34);
                    //							System.out.println("IP :" + IP);
                }
            }
            n = 0;
        }


        String url = "http://127.0.0.1:8080/wsgtwy/net/getlist/";
//		String url = "http://192.168.184.129:8080/wsgtwy/net/getlist/";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
//		System.out.println("\nSending 'GET' request to URL : " + url);
//		System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();



        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            String SA = new String("sa_");
//			System.out.println("this = " + inputLine);
//			System.out.println(inputLine.length());
            for(int i = 0; i<inputLine.length();i++){  //search string

                if(inputLine.regionMatches(i, SA, 0, 3) == true){  //find sa, i = index of sa
                    index_sa = i;


                    for(j=i-1;j<i+50;j++){
                        //System.out.println("QQ"+inputLine.substring(index_sa + 17, index_sa + 30));

                        if(inputLine.regionMatches(j, ",", 0, 1) == true){

                            //	d_name = inputLine.substring(index_sa + 17, j-1);
                            d_type = inputLine.substring(index_sa + 20, j-1);
                            break;

                        }
                    }
                }

                Date d=new Date();
                //System.out.println(d.toString());
                String now_date = d.toString().substring(8,10);
                //System.out.println("QQQ" +now_date);  // get current Date
                String now_clock = d.toString().substring(11,13);
                //System.out.println("KKK" +now_clock);  // get current time


                Calendar c=Calendar.getInstance();
                int now_year = c.get(Calendar.YEAR);
                int now_month = c.get(Calendar.MONTH) + 1;

                if(now_month == 13)
                    now_month = 1;



                if(inputLine.regionMatches(i, "Device_Address", 0, 14) == true){ //get device address , check alive
                    d_address = inputLine.substring(i + 17, i+22);
//					System.out.println("Device_Address : "+inputLine.substring(i + 17, i+22));


                    for( m = 0; m < network_jason.length() ; m++){   // find alive
                        if(network_jason.regionMatches(m, inputLine.substring(i + 17, i+22), 0, 5) == true){  // record update time
                            flag = 1;

                            for(int x=m;x<m+200;x++){

                                if(network_jason.regionMatches(x, "name", 0, 4) == true){

                                    for(j=x-1;j<x+50;j++){
                                        //System.out.println("QQ"+inputLine.substring(index_sa + 17, index_sa + 30));
                                        if(network_jason.regionMatches(j, ",", 0, 1) == true){

                                            d_name = network_jason.substring(x + 8, j-1);

                                            break;
                                        }
                                    }
                                }
                            }

                        }

                        if(network_jason.regionMatches(m, "last_updated", 0, 12) == true && flag ==1){
                            update = network_jason.substring(m + 16, m+32);
                            String update_year = update.substring(0,4);
                            String update_month = update.substring(5,7);
                            String update_date = update.substring(8,10);
                            String update_hour = update.substring(11,13);
                            String update_minute = update.substring(14,16);
//									System.out.println(update_year+"/"+update_month+"/"+update_date+"/"+update_hour+"/"+update_minute);
                            //		System.out.println("update time2 : " + update.substring(6,8));
                            //		System.out.println("update time3 : " + update.substring(3,5));
                            //		System.out.println("update time4 : " + now_date);
                            if(Integer.parseInt(now_clock) - Integer.parseInt(update_hour) < 3 && update_date.equals(now_date) && update_month.equals(Integer.toString(now_month)) /*&& update_year.equals(now_year)*/)
                                d_alive = true;
                            else
                                d_alive = false;

                            flag = 0;
                        }

                        //if(update.substring(3, 4) == now_date)

                    }
                }

                if(inputLine.regionMatches(i, "MSB", 0, 3) == true){
                    //System.out.println("MAC : "+inputLine.substring(i + 7, i+15));
                    MSB = inputLine.substring(i + 7, i+15);
                }
                if(inputLine.regionMatches(i, "LSB", 0, 3) == true){

                    d_mac = MSB + inputLine.substring(i + 7, i+15);
                    //System.out.println("ASD : " + d_name);
                    //System.out.println("ASD2 : " + d_type);
                    arr[n] = new Device(d_name ,d_type ,d_mac, d_address, d_alive); // name / type / mac
//					System.out.println(arr[n].d_name + "/" + arr[n].d_type + "/" + arr[n].d_mac + "/" +arr[n].d_address + "/" + arr[n].d_alive);
                    n++;
                    d_name = "";
                }

            }

//			for(m = 0; m < n; m++)
//				System.out.println(m+" : "+arr[m].d_name + "/" + arr[m].d_type + "/" + arr[m].d_mac + "/" +arr[m].d_address + "/" + arr[m].d_alive);

        }
        in_broker.close();
        in.close();

        for(j = 0;j < 20;j ++)
            parameters.have_sensor[j] = "";

        for(m = 0;m < n;m++){

            for(j = 0;j < 20;j ++){

                //System.out.println(arr[m].d_type);
                //System.out.println(com.intel.core.parameters.need_sensor[j]);
                if((arr[m].getType()).equals(parameters.need_sensor[j]) && arr[m].getAlive() == true){ // exit and alive
                    //
                    parameters.have_sensor[j] = arr[m].getName();
//					System.out.println(m + "  in  " + j);
                    num_s++;
                    break;
                }
            }
        }


        return num_s;

    }

}
