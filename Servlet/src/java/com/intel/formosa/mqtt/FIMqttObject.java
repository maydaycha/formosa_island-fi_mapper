package com.intel.formosa.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.intel.formosa.FIMessage;
import com.intel.formosa.FIObject;
import com.intel.formosa.params.FIParams;

/**
*
* @author Shao-Wen Yang <shao-wen.yang@intel.com>
*
*/
public abstract class FIMqttObject implements FIObject, FIMqttPublisher, MqttCallback {
	
	protected MqttClient mMqttClient;
	
	private final String mUri;
	
	private final String mName;
	
	private final FIParams mParams;
	
	public FIMqttObject(String uri, String name, FIParams params) {
		mUri = uri;
		
		mName = name;
		
		mParams = params.copy();
		
		prepare();
	}
	
	@Override
	public void finalize() {
		release();
	}
	
	private void prepare() {  
        try {
			mMqttClient = new MqttClient(mUri, MqttClient.generateClientId(), new MemoryPersistence());
	        	        
	        mMqttClient.setCallback(this);

			MqttConnectOptions opts =  new MqttConnectOptions();
			opts.setKeepAliveInterval(10);	        
			opts.setCleanSession(true);
			
	        mMqttClient.connect(opts);
		} catch (MqttException e) {

		}	
	}
	
	private void release() {
		try {
			if (mMqttClient != null) {
				mMqttClient.disconnect();
				mMqttClient.setCallback(null);
				mMqttClient = null;
			}
		} catch (MqttException e) {

		}		
	}
    
    protected void reset() {
    	release();
    	prepare();
    }
    
    @Override
    public void publish(FIMessage message) {
    	if (mMqttClient != null && mMqttClient.isConnected() && !message.id.isEmpty()) {    	
	    	MqttMessage mqttMessage = new MqttMessage(message.payload);
	    	try {
	        	mqttMessage.setQos(1);
	        	mMqttClient.getTopic(message.id).publish(mqttMessage);
			} catch (MqttPersistenceException e) {
	
			} catch (MqttException e) {
	
			}
    	}	
    }
    
    @Override
    public void publish(byte[] payload) {
    	publish(new FIMessage(mName, payload));
    }
	
    @Override
	public void publish(String message) {
    	publish(new FIMessage(mName, message));
	}
	
    @Override
	public <T extends Number> void publish(T number) {
    	publish(new FIMessage(mName, number));		
	}
    
    public void publish() {
    	publish(new FIMessage(mName, ""));
    }

	@Override
	public void messageArrived(String topic, MqttMessage message) {
		onFIMessageArrived(new FIMessage(topic, message.toString().getBytes()));
	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO: Test whether or not connectionLost() function even works.
		reset(); 
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken taoken) {
		
	}
	
	@Override
	public String getName() {
		return mName;
	}
	
	@Override
	public FIParams getParams() {
		return mParams;
	}
	
}
