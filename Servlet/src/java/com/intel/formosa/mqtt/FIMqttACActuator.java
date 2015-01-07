package com.intel.formosa.mqtt;

import com.intel.formosa.FIMessage;
import com.intel.formosa.params.FIParams;

/**
*
* @author Shao-Wen Yang <shao-wen.yang@intel.com>
*
*/
public class FIMqttACActuator extends FIMqttSink {

	private final String mACSink;
	
	public FIMqttACActuator(String uri, String name, FIParams params, String source) {
		super(uri, name, params, source);
		
		mACSink = params.getParameter("ameliacreek", "");
	}

	@Override
	public <T extends Number> void source(T number) {
		publish(new FIMessage(mACSink, number));
	}

}
