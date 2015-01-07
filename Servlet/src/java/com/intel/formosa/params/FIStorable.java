package com.intel.formosa.params;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * 
 * @author Shao-Wen Yang <shao-wen.yang@intel.com>
 *
 */
public interface FIStorable {
	
	public void load(Reader in) throws IOException;
	
	public void store(Writer out, String comment) throws IOException;
	
}
