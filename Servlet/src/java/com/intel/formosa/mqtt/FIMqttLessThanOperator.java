package com.intel.formosa.mqtt;

import com.intel.formosa.params.FIParams;
import com.intel.formosa.test.Parameters;

/**
*
* @author Shao-Wen Yang <shao-wen.yang@intel.com>
*
*/
public class FIMqttLessThanOperator extends FIMqttBinaryOperator {

    private Parameters parameters = null;

	
	public FIMqttLessThanOperator(String uri, String name, FIParams params, Parameters parameters, String lhs, String rhs) {
		super(uri, name, params, lhs, rhs);
        this.parameters = parameters;
	}

	@Override
	public <T extends Number> void run(T ... numbers) {
		assert numbers.length >= 2;
		
		if(parameters.compare.equals("LessEqualThan")){
    	publish(numbers[0].floatValue() <= numbers[1].floatValue() ? 1 : 0);
		}
    	else if(parameters.compare.equals("LessThan")){
		publish(numbers[0].floatValue() < numbers[1].floatValue() ? 1 : 0);
    	}
		else{
    		if(numbers[0].floatValue() == numbers[1].floatValue())
    		publish(1);
    		else
    		publish(0);
    	}
    }
}
