package com.intel.formosa.mqtt;

import com.intel.formosa.FIPublisher;

/**
*
* @author Shao-Wen Yang <shao-wen.yang@intel.com>
*
*/
public interface FIMqttPublisher extends FIPublisher {
	
	public void publish(byte[] payload);
	
	public void publish(String message);
	
	public <T extends Number> void publish(T number);
	
}
