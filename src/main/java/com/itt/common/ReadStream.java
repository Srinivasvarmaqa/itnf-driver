package com.itt.common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadStream implements Runnable {
    
    private static final Logger LOG = LoggerFactory.getLogger(ReadStream.class);
    
    private String name;
    private InputStream is;
    private Thread thread;
    private StringBuffer output = new StringBuffer();
    
    public ReadStream(String name, InputStream is) {
        this.name = name;
        this.is = is;
    }       
    public void start () {
        thread = new Thread (this);
        thread.start ();
    }       
    public void run () {
        try {
            String line = "";
            InputStreamReader isr = new InputStreamReader (is);
            BufferedReader br = new BufferedReader (isr);   
            while ((line = br.readLine()) != null) {
                LOG.debug(line);
                output.append(line);
            }
            is.close ();    
        } catch (Exception ex) {
            LOG.info("Problem reading stream " + name + "... :" + ex.getMessage());
        }
    }
    
    public String getOutput() {
        return output.toString();
    }
}
