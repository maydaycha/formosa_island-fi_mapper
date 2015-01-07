package com.intel.formosa;

/**
*
* @author Shao-Wen Yang <shao-wen.yang@intel.com>
*
*/
public interface FISource extends FIObject {
	
	public <T extends Number> void sink(T number);
	
}
