package com.intel.formosa.mapper;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Created by Maydaycha on 1/28/15.
 */
public class Discoverable implements Runnable {

    private MqttClient mqttClient;
    private Sigar sigar;

    private final String mqttBroker = "tcp://192.168.11.132:1883";

    @Override
    public void run() {
        try {
            mqttClient = new MqttClient(mqttBroker, MqttClient.generateClientId());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            mqttClient.connect(connOpts);


            /** get the host ip address */

            Enumeration<NetworkInterface> networkInterface = NetworkInterface.getNetworkInterfaces();

            String hostIpAddress = "";

            while (networkInterface.hasMoreElements()) {
                NetworkInterface e = networkInterface.nextElement();

                Enumeration<InetAddress> inetAddress = e.getInetAddresses();
                while (inetAddress.hasMoreElements()) {
                    InetAddress addr = inetAddress.nextElement();
                    String candicateIp = addr.getHostAddress();
                    if (candicateIp.split("\\.").length == 4) {
                        if (!candicateIp.equals("127.0.0.1")) {
                            hostIpAddress = candicateIp;
                        }
                    }
                }
            }

            String topic = "/ping/0/" + hostIpAddress;
            String message = "I am alive";
            String topicPrefix = "/pub/0/" + hostIpAddress;

            if (!hostIpAddress.equals("")) {
                /** send first message to info Master that you slave is alive */
                sendMqttMessage(topic, message);

                /** Continuously send the information of Hardware to broker */
                sigar = new Sigar();
                while (true) {
                    sendMqttMessage(topicPrefix + "/cpu", sigar.getCpu().toString());
                    System.out.println("CPU:" + sigar.getCpu());
                    sendMqttMessage(topicPrefix + "/mem", sigar.getMem().toString());
                    System.out.println("MEM:" + sigar.getMem());
                    Thread.sleep(2000);
                }
            }


        } catch (MqttException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SigarException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }


    public void sendMqttMessage (String topic, String message) throws MqttException, UnknownHostException {
        System.out.println(topic);
        MqttMessage mMessage = new MqttMessage(message.getBytes());
        mMessage.setQos(1);
        mqttClient.publish(topic, mMessage);
    }
}