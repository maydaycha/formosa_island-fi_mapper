package com.intel.formosa.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.intel.formosa.FIMessage;
import com.intel.formosa.params.FIParams;

/**
*
* @author Shao-Wen Yang <shao-wen.yang@intel.com>
*
*/
public class FIMqttACSensor extends FIMqttSource {
	
	private final String mACSource;
	
	private double mValue;

	public FIMqttACSensor(String uri, String name, FIParams params, String source) {
		super(uri, name, params, source);
		
		mACSource = params.getParameter("ameliacreek", "");
	}

	@Override
	public void start() {
		super.start();
		
		try {
			if (mMqttClient != null) {	
		        if(!mACSource.isEmpty()) {
		        	mMqttClient.subscribe(mACSource);
		        }
			}
		} catch (MqttException e) {
			
		}
	}

	@Override
	public void stop() {   
		super.stop();
		
		try {
			if (mMqttClient != null) {  	        
		        if(!mACSource.isEmpty()) {
		        	mMqttClient.unsubscribe(mACSource);
		        }
			}
		} catch (MqttException e) {

		}
	}

	@Override
	public void onFIMessageArrived(FIMessage message) {
		if (mACSource.equals(message.id)) {
			mValue = message.value(0.0);
			sink(mValue);
		} else {
			//sink(mValue);
		}
	}

	@Override
	public <T extends Number> void sink(T number) {
		
		publish(number);
	}

}
