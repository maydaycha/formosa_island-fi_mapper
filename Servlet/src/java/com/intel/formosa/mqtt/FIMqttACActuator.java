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
	private Double output = 0.0;
    private Boolean Alarm;
	
	public FIMqttACActuator(String uri, String name, FIParams params, boolean alarm, String source) {
		super(uri, name, params, source);
		
		mACSink = params.getParameter("ameliacreek", "");
		
		Alarm = alarm;
	}

	@Override
	public <T extends Number> void source(T number) {

		if(Alarm){
			if(output.equals(number)){
				output = (Double) number;
			}
			else{
				publish(new FIMessage(mACSink, number));
				output = (Double)number;
			}
		}
		else
		    publish(new FIMessage(mACSink, number));
	}

}
