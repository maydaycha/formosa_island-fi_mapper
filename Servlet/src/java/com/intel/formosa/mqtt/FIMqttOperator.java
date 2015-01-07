package com.intel.formosa.mqtt;

import java.util.HashSet;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.intel.formosa.FIMessage;
import com.intel.formosa.FIOperator;
import com.intel.formosa.params.FIParams;

/**
*
* @author Shao-Wen Yang <shao-wen.yang@intel.com>
*
*/

public abstract class FIMqttOperator extends FIMqttObject implements FIOperator {
	
	protected final String[] mSources;
	
	private HashSet<String> mReceived;
	
	public FIMqttOperator(String uri, String name, FIParams params, String ... sources) {
		super(uri, name, params);		

		mSources = new String[sources.length];
		mReceived = new HashSet<String>();
		
		System.arraycopy(sources, 0, mSources, 0, sources.length);
	}

	@Override
	public void start() {
		try {
			if (mMqttClient != null) {
		        for (String source : mSources) {
		        	mMqttClient.subscribe(source);
		        }
			}
		} catch (MqttException e) {
			
		}
	}

	@Override
	public void stop() {        
		try {
			if (mMqttClient != null) {   
		        for (String source : mSources) {
		        	mMqttClient.unsubscribe(source);
		        }
			}
		} catch (MqttException e) {

		}		
	}

	@Override
	public void onFIMessageArrived(FIMessage message) {			
		if (!mReceived.contains(message.id)) {
			mReceived.add(message.id);
		}
		
		if (mReceived.size() >= mSources.length) {
			run();
			mReceived.clear();
		}
	}

}
