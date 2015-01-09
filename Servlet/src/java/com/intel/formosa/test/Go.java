package com.intel.formosa.test;


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

                switch(Nodes.getByName(object.get("type").toString())){

                    case Meter_S:

                        System.out.println("case Meter_S");

                        powerSwitch = new FIMqttACActuator(
                                "tcp://192.168.184.129:1883",
                                "/formosa/"+object.get("z")+"/"+object.get("id"),
                                new FIConfigParams().setParameter("ameliacreek", object.get("deviceName")),
                                parameters,
                                "/formosa/"+object.get("z")+"/"+ a.get(0).toString().substring(2, a.get(0).toString().length()-2));

                        powerSwitch.start();

                        looper = new FIMqttLooper(
                                "tcp://192.168.184.129:1883",
                                "/formosa/"+object.get("z")+"/Looper",
                                new FIConfigParams(),
                                parameters,
                                "/formosa/"+object.get("z")+"/"+object.get("id"));
                        looper.start();
                        break;

                    case IASWD_S:

                        System.out.println("case IASWD_S");
                        parameters.Alarm = true;

                        WarningDevice = new FIMqttACActuator(
                                "tcp://192.168.184.129:1883",
                                "/formosa/"+object.get("z")+"/"+object.get("id"),
                                new FIConfigParams().setParameter("ameliacreek", object.get("deviceName")),
                                parameters,
                                "/formosa/"+object.get("z")+"/"+ a.get(0).toString().substring(2, a.get(0).toString().length()-2));

                        WarningDevice.start();

                        looper = new FIMqttLooper(
                                "tcp://192.168.184.129:1883",
                                "/formosa/"+object.get("z")+"/Looper",
                                new FIConfigParams(),
                                parameters,
                                "/formosa/"+object.get("z")+"/"+object.get("id"));
                        looper.start();
                        break;

                    case LessEqualThan:

                        System.out.println("case LessThan");
                        parameters.compare = "LessEqualThan";
                        lessEqualThanOperator = new FIMqttLessThanOperator(
                                "tcp://192.168.184.129:1883",
                                "/formosa/"+object.get("z")+"/"+object.get("id"),
                                new FIConfigParams(),
                                parameters,
                                "/formosa/"+object.get("z")+"/"+ a.get(0).toString().substring(2, a.get(0).toString().length()-2),
                                "/formosa/"+object.get("z")+"/"+ a.get(1).toString().substring(2, a.get(1).toString().length()-2));

                        lessEqualThanOperator.start();
                        break;

                    case LessThan:

                        System.out.println("case LessThan");
                        parameters.compare = "LessThan";
                        lessThanOperator = new FIMqttLessThanOperator(
                                "tcp://192.168.184.129:1883",
                                "/formosa/"+object.get("z")+"/"+object.get("id"),
                                new FIConfigParams(),
                                parameters,
                                "/formosa/"+object.get("z")+"/"+ a.get(0).toString().substring(2, a.get(0).toString().length()-2),
                                "/formosa/"+object.get("z")+"/"+ a.get(1).toString().substring(2, a.get(1).toString().length()-2));

                        lessThanOperator.start();
                        break;

                    case Equal:

                        System.out.println("case Equal");
                        parameters.compare = "Equal";
                        EqualOperator = new FIMqttLessThanOperator(
                                "tcp://192.168.184.129:1883",
                                "/formosa/"+object.get("z")+"/"+object.get("id"),
                                new FIConfigParams(),
                                parameters,
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

                        System.out.println("case IlluminanceMeasurement_S : "+object.get("deviceName"));

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
            while(true){
                if(!parameters.five_s_alive){

                    if(Illuminance != null)
                        Illuminance.finalize();
                    if(Temperature != null)
                        Temperature.finalize();
                    if(number != null)
                        number.finalize();
                    if(lessThanOperator != null)
                        lessThanOperator.finalize();
                    if(lessEqualThanOperator != null)
                        lessEqualThanOperator.finalize();
                    if(EqualOperator != null)
                        EqualOperator.finalize();
                    if(powerSwitch != null)
                        powerSwitch.finalize();
                    if(WarningDevice != null)
                        WarningDevice.finalize();

                    break;
                }
            }
        }
    }

    public void setAliveFlag(boolean alive) {
        parameters.five_s_alive = alive;
    }

}
