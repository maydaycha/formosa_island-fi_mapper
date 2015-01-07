package com.intel.formosa;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
*
* @author Shao-Wen Yang <shao-wen.yang@intel.com>
* @author Ren-Jie Wu <ren-jie.wu@intel.com>
*
*/
public class FIMessage {

	public String id;

	public byte[] payload;
	
	public FIMessage(String id, byte[] payload) {
		this.id = id;
		this.payload = Arrays.copyOf(payload, payload.length);
	}
	
	public FIMessage(String id, String message) {
		this(id, message.getBytes());
	}
	
	public <T extends Number> FIMessage(String id, T number) {
		this(id, String.valueOf(number));
	}
	
	@Override
	public String toString() {
		String obj = null;
		
		try {
			obj = new String(payload , "UTF-8");
		} catch (UnsupportedEncodingException e) {
			
		}
		
		return obj;
	}
	
	public <T> T value(T defaultValue) {
		String obj = toString();

		if (obj != null && !obj.isEmpty()) {
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
		} else {
			return defaultValue;
		}
	}
	
}