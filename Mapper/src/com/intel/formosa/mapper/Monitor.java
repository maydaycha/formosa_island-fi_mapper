package com.intel.formosa.mapper;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Monitor implements MqttCallback, Runnable {

    MqttClient client;
    private String topic;
    private String kill_topic;
    private int flag = 0;
    private int first_num = 0;
    private int result = 0;
    private int total = 0;
    private String first_topic = "";
    private String sec_topic = "";
    Timer timer = new Timer();
    String mqttBroker = "tcp://localhost:1883";


    public Monitor(String topic) {
        this.topic = topic;
        doDemo();
    }

    public void doDemo() {
        int j;
        try {
            client = new MqttClient(mqttBroker, "Sending");
            client.connect();
            client.setCallback(this);
            topic = "/pub" + topic + "/";
            client.subscribe(topic);
            System.out.println("topic : "+topic);

            for(j = 0;j < Mapper.acList.size();j++){
                if(Mapper.acList.get(j).get(0) == topic && flag == 0){

                    first_topic = Mapper.acList.get(j).get(1);
                    System.out.println("first_topic : "+first_topic);
                    flag++;
                }
                if(Mapper.acList.get(j).get(0) == topic && flag == 1){

                    sec_topic = Mapper.acList.get(j).get(1);
                    System.out.println("sec_topic : "+sec_topic);
                    flag++;
                }
            }

            flag = 0;

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        // TODO Auto-generated method stub

    }

    @Override
    public void messageArrived(String topic, MqttMessage message)throws Exception {

        flag++;

        JSONObject jsonObj = (JSONObject) new JSONParser().parse(message.toString());

        int data = Integer.parseInt(jsonObj.get("data").toString());

        if(flag == 1)
            first_num = data;

        if(flag == 2){

            result = first_num ^ data;
            total = total + result;
            System.out.println("first_num : "+first_num);
            System.out.println("sec_num : "+data);
            System.out.println("result : "+result);


            if(total == 1 && result == 1){

                if(timer != null)
                    timer.cancel();

                timer = new Timer();
                timer.schedule(new TimerTask() {

                    public void run() {
                        System.out.println("total : "+total);
                        if(total > 5){
                            kill_topic = "/formosa/"+ Mapper.acList.get(0).get(1)+"/finish";
                            Die();
//                    	System.out.println("you die : "+Mapper.acList.get(j).get(1));
                            timer.cancel();
                            total = 0;
                        }
                    }
                },1000,1000);
            }


            flag = 0;
        }

//	 System.out.println("message comming : "+jsonObj);   

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub

    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

    }

    protected void Die() {

        String content  = "{123}";
        MqttClient mMqttClient;
        try {
            mMqttClient = new MqttClient(mqttBroker,MqttClient.generateClientId());

            MqttConnectOptions     connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            mMqttClient.connect(connOpts);

            System.out.println("kill_topic : "+kill_topic);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(1);
            mMqttClient.publish(kill_topic, message);
            System.out.println("finish");
            mMqttClient.disconnect();
        } catch (MqttException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}