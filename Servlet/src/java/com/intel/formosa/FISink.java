package com.intel.formosa;

/**
*
* @author Shao-Wen Yang <shao-wen.yang@intel.com>
*
*/
public interface FISink extends FIObject {
	
	public <T extends Number> void source(T number);
	
}
