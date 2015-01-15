package com.intel.formosa.test;


import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.intel.formosa.mqtt.FIMqttACActuator;
import com.intel.formosa.mqtt.FIMqttACSensor;
import com.intel.formosa.mqtt.FIMqttLessThanOperator;
import com.intel.formosa.mqtt.FIMqttLooper;
import com.intel.formosa.mqtt.FIMqttNumber;
import com.intel.formosa.mqtt.FIMqttObject;
import com.intel.formosa.params.FIConfigParams;


public class Go implements Runnable {


    FIMqttLooper looper;

    FIMqttObject Illuminance = null;
    FIMqttObject Temperature = null;
    FIMqttObject number = null;
    FIMqttObject lessThanOperator = null;
    FIMqttObject lessEqualThanOperator = null;
    FIMqttObject EqualOperator = null;
    FIMqttObject powerSwitch = null;
    FIMqttObject WarningDevice = null;
    JSONArray jsonarray = null;
    Parameters parameters = null;
    String topic = null;
    String broker = "tcp://192.168.184.129:1883";

	
	/*
	public Go(String sessionId) {
		FIConfigParams parameter = new FIConfigParams(); 
		looper = new FIMqttLooper("tcp://192.168.184.129:1883", "/formosa/1/Looper/", parameter.setParameter("", ""), "/Gateway1/Illuminance/Illuminance2");
		lightSensor = new FIMqttLightSensor("tcp://192.168.184.129:1883", "/formosa/1/Illuminance/", parameter.setParameter("ameliacreek", "/Gateway1/Illuminance/Illuminance"), "/formosa/1/Looper/");
		number = new FIMqttNumber("tcp://192.168.184.129:1883", "/formosa/1/Number/", "/formosa/1/Looper/", parameter.setParameter("", ""));
		lessThanOperator = new FIMqttLessThanOperator("tcp://192.168.184.129:1883", "/formosa/1/LessThanOperator/", parameter.setParameter("", ""), "/formosa/1/Number/", "/formosa/1/Illuminance/");
		powerSwitch = new FIMqttPowerSwitch("tcp://192.168.184.129:1883", "/formosa/1/PowerSwitch", "/formosa/1/LessThanOperator/", parameter.setParameter("ameliacreek", "/Gateway1/Illuminance/Illuminance2"));
	}
	
	public Go() {
		FIConfigParams parameter = new FIConfigParams(); 
		looper = new FIMqttLooper("tcp://192.168.184.129:1883", "/formosa/1/Looper/", parameter.setParameter("", ""), "/Gateway1/Illuminance/Illuminance2");
		lightSensor = new FIMqttLightSensor("tcp://192.168.184.129:1883", "/formosa/1/Illuminance/", parameter.setParameter("ameliacreek", "/Gateway1/Illuminance/Illuminance"), "/formosa/1/Looper/");
		number = new FIMqttNumber("tcp://192.168.184.129:1883", "/formosa/1/Number/", "/formosa/1/Looper/", parameter.setParameter("", ""));
		lessThanOperator = new FIMqttLessThanOperator("tcp://192.168.184.129:1883", "/formosa/1/LessThanOperator/", parameter.setParameter("", ""), "/formosa/1/Number/", "/formosa/1/Illuminance/");
		powerSwitch = new FIMqttPowerSwitch("tcp://192.168.184.129:1883", "/formosa/1/PowerSwitch", "/formosa/1/LessThanOperator/", parameter.setParameter("ameliacreek", "/Gateway1/Illuminance/Illuminance2"));
	} */



    public Go(JSONArray jsonarray) {
        // TODO Auto-generated constructor stub
        this.jsonarray = jsonarray;
        parameters = new Parameters();

    }

    public enum Nodes {
        Meter_S("Meter_S"),
        IASWD_S("IASWD_S"),
        LessEqualThan("LessEqualThan"),
        LessThan("LessThan"),
        Equal("Equal"),
        Number("Number"),
        IlluminanceMeasurement_S("IlluminanceMeasurement_S"),
        OccupancySensing_S("OccupancySensing_S"),
        TemperatureMeasurement_S("TemperatureMeasurement_S");

        private String name;

        Nodes(final String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        public static Nodes getByName (final String name) {
            for (Nodes n: Nodes.values()) {
                if (n.name.equalsIgnoreCase(name)) {
                    return n;
                }
            }
            return null;
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        if(jsonarray != null){
            //FIConfigParams parameter = new FIConfigParams();
            for (Object o : jsonarray)
            {
                //System.out.println(o);
                JSONObject object = (JSONObject) o;
                JSONArray a = (JSONArray) object.get("wires");
                String operator = null;
                boolean Alarm = false;

                switch(Nodes.getByName(object.get("type").toString())){

                    case Meter_S:

                        System.out.println("case Meter_S");
                        Alarm = false;
                        topic = "/formosa/"+(String) object.get("z")+"/finish";

                        powerSwitch = new FIMqttACActuator(
                                "tcp://192.168.184.129:1883",
                                "/formosa/"+object.get("z")+"/"+object.get("id"),
                                new FIConfigParams().setParameter("ameliacreek", object.get("deviceName")),
                                Alarm,
                                "/formosa/"+object.get("z")+"/"+ a.get(0).toString().substring(2, a.get(0).toString().length()-2));

                        powerSwitch.start();

                        looper = new FIMqttLooper(
                                "tcp://192.168.184.129:1883",
                                "/formosa/"+object.get("z")+"/Looper",
                                new FIConfigParams(),
                                "/formosa/"+object.get("z")+"/"+object.get("id"));
                        looper.start();
                        break;

                    case IASWD_S:

                        System.out.println("case IASWD_S");
                        Alarm = true;
                        topic = "/formosa/"+(String) object.get("z")+"/finish";

                        WarningDevice = new FIMqttACActuator(
                                "tcp://192.168.184.129:1883",
                                "/formosa/"+object.get("z")+"/"+object.get("id"),
                                new FIConfigParams().setParameter("ameliacreek", object.get("deviceName")),
                                Alarm,
                                "/formosa/"+object.get("z")+"/"+ a.get(0).toString().substring(2, a.get(0).toString().length()-2));

                        WarningDevice.start();

                        looper = new FIMqttLooper(
                                "tcp://192.168.184.129:1883",
                                "/formosa/"+object.get("z")+"/Looper",
                                new FIConfigParams(),
                                "/formosa/"+object.get("z")+"/"+object.get("id"));
                        looper.start();
                        break;

                    case LessEqualThan:

                        System.out.println("case LessThan");
                        operator = "LessEqualThan";
                        lessEqualThanOperator = new FIMqttLessThanOperator(
                                "tcp://192.168.184.129:1883",
                                "/formosa/"+object.get("z")+"/"+object.get("id"),
                                new FIConfigParams(),
                                operator,
                                "/formosa/"+object.get("z")+"/"+ a.get(0).toString().substring(2, a.get(0).toString().length()-2),
                                "/formosa/"+object.get("z")+"/"+ a.get(1).toString().substring(2, a.get(1).toString().length()-2));

                        lessEqualThanOperator.start();
                        break;

                    case LessThan:

                        System.out.println("case LessThan");
                        operator = "LessThan";
                        lessThanOperator = new FIMqttLessThanOperator(
                                "tcp://192.168.184.129:1883",
                                "/formosa/"+object.get("z")+"/"+object.get("id"),
                                new FIConfigParams(),
                                operator,
                                "/formosa/"+object.get("z")+"/"+ a.get(0).toString().substring(2, a.get(0).toString().length()-2),
                                "/formosa/"+object.get("z")+"/"+ a.get(1).toString().substring(2, a.get(1).toString().length()-2));

                        lessThanOperator.start();
                        break;

                    case Equal:

                        System.out.println("case Equal");
                        operator = "Equal";
                        EqualOperator = new FIMqttLessThanOperator(
                                "tcp://192.168.184.129:1883",
                                "/formosa/"+object.get("z")+"/"+object.get("id"),
                                new FIConfigParams(),
                                operator,
                                "/formosa/"+object.get("z")+"/"+ a.get(0).toString().substring(2, a.get(0).toString().length()-2),
                                "/formosa/"+object.get("z")+"/"+ a.get(1).toString().substring(2, a.get(1).toString().length()-2));

                        EqualOperator.start();
                        break;

                    case Number:

                        System.out.println("case Number");

                        number = new FIMqttNumber(
                                "tcp://192.168.184.129:1883",
                                "/formosa/"+object.get("z")+"/"+object.get("id"),
                                new FIConfigParams().setParameter("constant", object.get("value")),
                                "/formosa/"+object.get("z")+"/Looper");
                        number.start();
                        break;

                    case IlluminanceMeasurement_S:

                        System.out.println("case IlluminanceMeasurement_S");

                        Illuminance = new FIMqttACSensor(
                                "tcp://192.168.184.129:1883",
                                "/formosa/"+object.get("z")+"/"+object.get("id"),
                                new FIConfigParams().setParameter("ameliacreek", object.get("deviceName")),
                                "/formosa/"+object.get("z")+"/Looper");
                        Illuminance.start();
                        break;

                    case TemperatureMeasurement_S:

                        System.out.println("case TemperatureMeasurement_S");

                        Temperature = new FIMqttACSensor(
                                "tcp://192.168.184.129:1883",
                                "/formosa/"+object.get("z")+"/"+object.get("id"),
                                new FIConfigParams().setParameter("ameliacreek", object.get("deviceName")),
                                "/formosa/"+object.get("z")+"/Looper");
                        Temperature.start();
                        break;
                }
            }
			  
			
			
	/*		looper = new FIMqttLooper(
				"tcp://192.168.184.129:1883",
				"/formosa/1/Looper",
				new FIConfigParams(),
				"/formosa/1/PowerSwitch");
			lightSensor = new FIMqttACSensor(
				"tcp://192.168.184.129:1883",
				"/formosa/1/Illuminance", 
				new FIConfigParams().setParameter("ameliacreek", "/Gateway1/Illuminance/Illuminance"),
				"/formosa/1/Looper");
			number = new FIMqttNumber(
				"tcp://192.168.184.129:1883",
				"/formosa/1/Number",
				new FIConfigParams().setParameter("constant", 1), 
				"/formosa/1/Looper");
			lessThanOperator = new FIMqttLessThanOperator(
				"tcp://192.168.184.129:1883",
				"/formosa/1/LessThanOperator",
				new FIConfigParams(),
				"/formosa/1/Number",
				"/formosa/1/Illuminance");
			powerSwitch = new FIMqttACActuator(
				"tcp://192.168.184.129:1883",
				"/formosa/1/PowerSwitch",
				new FIConfigParams().setParameter("ameliacreek", "/Gateway1/OnOff/OnOff"),
				"/formosa/1/LessThanOperator");

		while (true) {
			
			try {
				
				System.out.println("finish");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} */
            //	looper.start();
            //	Illuminance.start();
            //	number.start();
            //	lessThanOperator.start();
            //	powerSwitch.start();
            looper.run();

            parameters.five_s_alive = true;

            while(true) {
//                System.out.println("[while] parameters.five_s_alive = " + parameters.five_s_alive);
                if (!parameters.five_s_alive){
                    System.out.println("[in while in if] parameters.five_s_alive = " + parameters.five_s_alive);
                    if(Illuminance != null){
                        Illuminance.stop();
                        Illuminance.finalize();
                        Illuminance = null;
                    }
                    if(Temperature != null){
                        Temperature.stop();
                        Temperature.finalize();
                        Temperature = null;
                    }
                    if(number != null){
                        number.stop();
                        number.finalize();
                        number = null;
                    }
                    if(lessThanOperator != null){
                        lessThanOperator.stop();
                        lessThanOperator.finalize();
                        lessThanOperator = null;
                    }
                    if(lessEqualThanOperator != null){
                        lessEqualThanOperator.stop();
                        lessEqualThanOperator.finalize();
                        lessThanOperator = null;
                    }
                    if(EqualOperator != null){
                        EqualOperator.stop();
                        EqualOperator.finalize();
                        EqualOperator = null;
                    }
                    if(powerSwitch != null){
                        powerSwitch.stop();
                        powerSwitch.finalize();
                        powerSwitch = null;
                    }
                    if(WarningDevice != null){
                        WarningDevice.stop();
                        WarningDevice.finalize();
                        WarningDevice = null;
                    }

                    String content  = "{123}";
                    MqttClient mMqttClient;
                    try {
                        mMqttClient = new MqttClient(broker,MqttClient.generateClientId());

                        MqttConnectOptions connOpts = new MqttConnectOptions();
                        connOpts.setCleanSession(true);

                        mMqttClient.connect(connOpts);

                        System.out.println("finish topic: " + topic);
                        MqttMessage message = new MqttMessage(content.getBytes());
                        message.setQos(1);
                        mMqttClient.publish(topic, message);

                        mMqttClient.disconnect();
                    } catch (MqttException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        System.out.println("[exception]" + e);
                    }

                    System.out.println("[Rule Engine] STOP!");
                    break;
                }

//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

            }
        }
        System.gc();
    }

    public void setAliveFlag(boolean alive) {
        parameters.five_s_alive = alive;
        System.out.println("[setAliveFlag] parameters.five_s_alive = " + parameters.five_s_alive);
    }

    public boolean getAliveFlag() {
        return parameters.five_s_alive;
    }

}
