package com.intel.formosa.mqtt;

import com.intel.formosa.params.FIParams;

/**
*
* @author Shao-Wen Yang <shao-wen.yang@intel.com>
*
*/
public class FIMqttLessThanOperator extends FIMqttBinaryOperator {

    
    private String Operator = null;
	
	public FIMqttLessThanOperator(String uri, String name, FIParams params, String operator, String lhs, String rhs) {
		super(uri, name, params, lhs, rhs);
		Operator = operator;
	}

	@Override
	public <T extends Number> void run(T ... numbers) {
		assert numbers.length >= 2;
		
		if(Operator.equals("LessEqualThan")){
    	publish(numbers[0].floatValue() <= numbers[1].floatValue() ? 1 : 0);
		}
    	else if(Operator.equals("LessThan")){
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
