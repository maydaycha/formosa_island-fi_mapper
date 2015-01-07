package com.intel.formosa.params;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;
import java.util.Set;

/**
 * Used for storing, configuring, and serializing parameters.
 * 
 * @author Shao-Wen Yang <shao-wen.yang@intel.com>
 *
 */
public class FIConfigParams implements FIParams, FIStorable, Cloneable {
	
	protected final Properties mProperties;

	public FIConfigParams() {
		mProperties = new Properties();		
	}
	
	public FIConfigParams(FIParams params) {
		this();
		
		setParameters(params);
	}

	@Override
	public FIParams copy() {
		return (FIParams) clone();
	}
	
	@Override
	public FIParams setParameters(FIParams params) {
		if (params instanceof FIConfigParams) {
			mProperties.putAll(((FIConfigParams) params).mProperties);
		}
		
		return this;
	}
	
	@Override
	public FIParams setParameters(FIParameterizable parameterizable) {
		return setParameters(parameterizable.getParameters());
	}

	@Override
	public <T> FIParams setParameter(String name, T value) {
		mProperties.setProperty(name, value.toString());
		
		return this;
	}

	@Override
	public <T> T getParameter(String name, T defaultValue) {
		String obj = mProperties.getProperty(name, defaultValue.toString());
		if (obj != null) {
			try {
				if (defaultValue instanceof String) {
					@SuppressWarnings("unchecked")
					T value = (T) obj;
					return value;
				} else if (defaultValue instanceof Number) {
					if (defaultValue instanceof Integer) {
						@SuppressWarnings("unchecked")
						T value = (T) Integer.valueOf(obj);
						return value;
					} else if (defaultValue instanceof Double) {
						@SuppressWarnings("unchecked")
						T value = (T) Double.valueOf(obj);
						return value;						
					} else {
						return defaultValue;
					}
				} else if (defaultValue instanceof Boolean) {
					@SuppressWarnings("unchecked")
					T value = (T) Boolean.valueOf(obj);
					return value;
				} else {
					return defaultValue;
				}
			} catch (ClassCastException e) {
				return defaultValue;
			}
		}
		else {
			return defaultValue;
		}
	}

	@Override
	public boolean removeParameter(String name) {
		if (mProperties.containsKey(name)) {
			mProperties.remove(name);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public Set<String> parameterNames() {
		return mProperties.stringPropertyNames();
	}
	
	@Override
	synchronized
	public void load(Reader in) throws IOException {
		mProperties.load(in);
	}
	
	@Override
	synchronized
	public void store(Writer out, String comment) throws IOException {
		mProperties.store(out, comment);
	}
	
	@Override
	public int size() {
		return mProperties.size();
	}
	
	@Override
	public FIConfigParams clone() {
		return new FIConfigParams(this);
	}

	@Override
	public void clear() {
		mProperties.clear();		
	}
	
}
