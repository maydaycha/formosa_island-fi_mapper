package com.intel.formosa.mqtt;

import com.intel.formosa.params.FIParams;

/**
*
* @author Shao-Wen Yang <shao-wen.yang@intel.com>
*
*/
public class FIMqttLooper extends FIMqttOperator {
	
	public FIMqttLooper(String uri, String name, FIParams params, String ... sources) {
		
		super(uri, name, params, sources);
	}

	@Override
	public <T extends Number> void run(T ... unused) {
		publish();
	}

}
