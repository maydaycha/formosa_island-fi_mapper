package com.intel.formosa.mapper;

import org.eclipse.paho.client.mqttv3.*;

import java.net.SocketException;

/**
 * Created by Maydaycha on 2/2/15.
 */
public class SubscribeAliveRequest implements Runnable, MqttCallback {
    String topicAliveRequest = "/ping/0/request";
    String topicAliveResponse;
    String mqttBroker;
    int subQoS = 0;
    MqttAsyncClient asyncClient;


    public SubscribeAliveRequest(String mqttBroker) throws SocketException, MqttException {
        this.mqttBroker = mqttBroker;
        topicAliveResponse = "/ping/0/" + Mapper.getHostIpAddress();
    }

    @Override
    public void run() {
        try {
            asyncClient = new MqttAsyncClient(mqttBroker, MqttAsyncClient.generateClientId());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            asyncClient.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    System.out.println("async client connected");
                    try {
                        asyncClient.subscribe(topicAliveRequest, subQoS);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    System.out.println("Mqtt connect failure");
                }
            });

            asyncClient.setCallback(this);

        } catch (MqttSecurityException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("connection Lost");

    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        System.out.println("topic: " + topic + ", message: " + mqttMessage);
        try {
            if (topic.equals(topicAliveRequest)) {
                MqttMessage mMessage = new MqttMessage("I am alive".getBytes());
                mMessage.setQos(1);
                asyncClient.publish(topicAliveResponse, mMessage);
                System.out.println("asyncClient publish: " + mMessage);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("deliveryComplete");
    }


}
