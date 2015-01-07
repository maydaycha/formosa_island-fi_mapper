package com.intel.formosa.params;

import java.util.Set;

/**
 * 
 * @author Shao-Wen Yang <shao-wen.yang@intel.com>
 *
 */
public interface FIParams {

	public FIParams copy();
	
	public FIParams setParameters(FIParams params);
	
	public FIParams setParameters(FIParameterizable parameterizable);
	
	public <T> FIParams setParameter(String name, T value);
	
	public <T> T getParameter(String name, T defaultValue);
	
	public boolean removeParameter(String name);
	
	public Set<String> parameterNames();
	
	public int size();
	
	public void clear();
	
}
