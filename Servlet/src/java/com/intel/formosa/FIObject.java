package com.intel.formosa;

import com.intel.formosa.params.FIParams;

/**
*
* @author Shao-Wen Yang <shao-wen.yang@intel.com>
*
*/
public interface FIObject extends FIPublisher, FICallback {
	
	public void start();
	
	public void stop();
	
	public String getName();
	
	public FIParams getParams();

}
