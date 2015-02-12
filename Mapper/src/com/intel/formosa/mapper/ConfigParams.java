package com.intel.formosa.mapper;

import com.intel.formosa.params.FIConfigParams;

import java.io.*;

/**
 * Created by Maydaycha on 2/2/15.
 */
public class ConfigParams extends FIConfigParams {

    File propertiesFile;
    InputStream inputStream;
    Reader reader;

    public ConfigParams () throws IOException {
        propertiesFile = new File("config.properties");
        if (!propertiesFile.exists()) propertiesFile.createNewFile();
        inputStream = new FileInputStream(propertiesFile);
        reader = new InputStreamReader(inputStream);
        load(reader);
    }

    @Override
    public <T> T getParameter (java.lang.String name, T defaultValue) {
       return super.getParameter(name, defaultValue);
    }

    public synchronized void store (String comment) throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("config.properties"), "utf-8"));
        super.store(writer, comment);
    }

}
