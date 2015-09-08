package net.larsan.urd.impl.test;

import junit.framework.*;
import junit.swingui.*;
import junit.runner.*;
import java.io.*;
import java.net.*;
import java.util.*;

import net.larsan.norna.base.*;
import net.larsan.urd.impl.*;
import net.larsan.urd.archive.*;
import net.larsan.urd.util.fileset.*;
import net.larsan.urd.util.*;
import net.larsan.urd.jndi.*;
import net.larsan.urd.*;
import javax.naming.*;

// this unit test depends on the 'calculator' test case
// which must be compiled prior to running this test

public class TestBase extends TestCase {

    protected SharedSpace space; // shared class space
    protected File root; // folder
    protected File service; // service file
    protected DefaultErrorHandler handler;
    protected ContextBase context;

    public TestBase(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        // BODGE WARNING: We don't know the working directory, so will asume a file
        // url, replace the url encoding for space, strip protocol and then run
        URL url = getClass().getClassLoader().getResource("net/larsan/urd/impl/test/calc.jar");
        if(url == null) url = getClass().getClassLoader().getResource("calc.jar");
        String str = url.toString().replaceAll("%20", " ");
        context = RootContext.CONTEXT;
        if(str.startsWith("file:")) str = str.substring(5);
        try {
            class Handler extends ErrorHandler {
                public void handleException(String msg, Object source, Throwable exception) {
                    String str = msg + "; " + source + "; " + exception;
                    fail(str);
                }
            };
            handler = new DefaultErrorHandler(context);
            service = new File(str);
            root = service.getParentFile();
            space = new SharedSpaceLoader(getClass().getClassLoader());
            ContextUtils.creationBind(context, "/urd/classloader/service", getClass().getClassLoader());
            ContextUtils.creationBind(context, "/urd/classloader/shared", space);
            ContextUtils.creationBind(context, "/urd/threads", new ThreadPool("Pool", 1));
        } catch(Exception e) {
            e.printStackTrace();
            super.fail(e.getMessage());
        }
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
        ((ThreadPool)context.lookup("/urd/threads")).close();
        RootContext.CONTEXT.clear();
    }
}