package com.intel.formosa.mqtt;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.intel.formosa.params.FIParams;

/**
 *
 * @author Shao-Wen Yang <shao-wen.yang@intel.com>
 *
 */
public class FIMqttLooper extends FIMqttOperator {

    volatile Boolean alive = true;
    Timer timer = new Timer();
    String topic = null;
    String broker = "tcp://192.168.184.129:1883";
    private static int counter = 0;

    public FIMqttLooper(String uri, String name, FIParams params, String ... sources) {
        super(uri, name, params, sources);

        String[] names = name.split("/");

        topic = "/"+names[1]+"/"+names[2]+"/"+"finish";

    }

    @Override
    public <T extends Number> void run(T ... unused) {

        if (alive) {

            publish();
            counter = 0;

            if(timer != null)
                timer.cancel();

            timer = new Timer();
            timer.schedule(new TimerTask() {

                public void run() {

                    counter++;
                    System.out.println(counter);

                    if(counter > 8){
                        Die();
                        timer.cancel();
                    }
                }
            },1000,1000);
        }
    }

    protected void Die() {
        alive = false;
        String content  = "{123}";
        MqttClient mMqttClient;
        try {
            mMqttClient = new MqttClient(broker,MqttClient.generateClientId());

            MqttConnectOptions     connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            mMqttClient.connect(connOpts);

            System.out.println(topic);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(1);
            mMqttClient.publish(topic, message);

            mMqttClient.disconnect();
        } catch (MqttException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}