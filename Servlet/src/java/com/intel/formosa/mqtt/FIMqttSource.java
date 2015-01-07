package com.intel.formosa.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.intel.formosa.FIMessage;
import com.intel.formosa.FISource;
import com.intel.formosa.params.FIParams;

/**
*
* @author Shao-Wen Yang <shao-wen.yang@intel.com>
*
*/
public abstract class FIMqttSource extends FIMqttObject implements FISource {	
	
	protected final String mSource;	
	String mname = null;
	public FIMqttSource(String uri, String name, FIParams params, String source) {		
		super(uri, name, params);
		mSource = source;
	}

	@Override
	public void start() {
		try {
			if (mMqttClient != null) {				
		        mMqttClient.subscribe(mSource);	
			}
		} catch (MqttException e) {
			
		}
	}

	@Override
	public void stop() {       
		try {
			if (mMqttClient != null) {       
		        mMqttClient.unsubscribe(mSource);
			}
		} catch (MqttException e) {

		}
	}

/*	@Override
	public void onFIMessageArrived(FIMessage message) {
	
		if (mSource.equals(message.id))
		sink(message.value(0));

	}  */
	
}
