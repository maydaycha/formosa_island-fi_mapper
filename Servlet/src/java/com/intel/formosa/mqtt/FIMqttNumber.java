package com.intel.formosa.mqtt;

import com.intel.formosa.FIMessage;
import com.intel.formosa.params.FIParams;

/**
*
* @author Shao-Wen Yang <shao-wen.yang@intel.com>
* @author Ren-Jie Wu <ren-jie.wu@intel.com>
*
*/
public class FIMqttNumber extends FIMqttSource {
	
	private final float mNumber;

	public FIMqttNumber(String uri, String name, FIParams params, String source) {
		super(uri, name, params, source);
		
		mNumber = params.getParameter("constant", 1);		
	}

	@Override
	public void onFIMessageArrived(FIMessage message) {
		sink(0);
	}

	@Override
	public <T extends Number> void sink(T unused) {		
		publish(mNumber);
	}

}
