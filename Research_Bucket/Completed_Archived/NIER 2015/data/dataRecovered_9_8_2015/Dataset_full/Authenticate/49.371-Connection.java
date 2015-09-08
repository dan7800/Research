package net.larsan.urd.cmd;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import net.larsan.urd.*;
import net.larsan.urd.util.*;

import net.larsan.norna.Status;
import net.larsan.norna.service.user.*;

import javax.security.auth.*;

public class Connection {

    private CmdWriter writer;
    private StringBuffer buff;
    private SocketChannel channel;
    private ServerContext con;
    private boolean isAuth;
    private String cramText;
    private LoginSession session;
        
    public Connection(SocketChannel channel, ServerContext con) {
        this.writer = new CmdWriterImpl(channel);
        buff = new StringBuffer();
        this.channel = channel;
        this.con = con;
        sendFirst();
    }

        
    public void receive(String str) {
        buff.append(str);
        if(str.endsWith("\r\n")) {
            if(isAuth) parseLine(buff.toString().trim());
            else authenticate(buff.toString());
            buff.delete(0, buff.length());;
        }
    }
        
    /**
     * Parse a read line into command events.
     */
        
    private void parseLine(String line) {
           
        String[] args = StringUtils.tokenize(line, " ");
        if(args.length == 0) return;
        String com = args[0];
            
        if(args.length > 1) {
            args = StringUtils.tokenize(args[1], ",");
        } else args = null;
            
        if(com.equals("exit")) exit();
        else if(com.equals("kill")) closeDown(new CmdEvent(CmdEvent.KILL, writer));
        else if(com.equals("help")) con.fireEvent(new CmdEvent(CmdEvent.HELP, writer));
        else if(com.equals("list")) con.fireEvent(new CmdEvent(CmdEvent.LIST, writer));
        else if(com.equals("threads")) con.fireEvent(new CmdEvent(CmdEvent.THREADS, writer));
        else if(com.equals("shutdown")) closeDown(new CmdEvent(CmdEvent.SHUTDOWN, writer, args));
        else if(com.equals("stop")) {
            if(args == null) writeln("missing service argument");
            else con.fireEvent(new CmdEvent(CmdEvent.STOP, writer, args));
        } else if(com.equals("start")) {
            if(args == null) writeln("missing service argument");
            else con.fireEvent(new CmdEvent(CmdEvent.START, writer, args));
        } else if(com.equals("info")) {
            if(args == null) writeln("missing service argument");
            else con.fireEvent(new CmdEvent(CmdEvent.INFO, writer, args));
        } else {
            writeln("unknown command: " + com);
        }
    }
        
    /**
     * Closing down framework, stop this server as well
     */
        
    private void closeDown(CmdEvent event) {
        con.fireEvent(event);
        exit();
    }
        
        
    /**
     * Exit this connection and close channel
     */
        
    private void exit() {
        if(session != null) session.invalidate();
        try {
            channel.close();
        } catch(IOException e) {
            con.reportException(e);
        }
    }
        
    private void writeln(String str) {
        writer.println(str);
        writer.commit();
    }
        
    private String[] subarray(String[] arr, int off, int len) {
        String[] tmp = new String[len - off];
        System.arraycopy(arr, off, tmp, 0, len - off);
        return tmp;
    }
    
    
    /**
     * Attempt to parse the first line from the client. This will be
     * a user name and a CRAM-MD5 hash separated by a space.
     * 
     * <p>On success we'll send a simple 'OK' and on error an 'ERR' followed
     * by a message.
     */
    
    private void authenticate(String str) {
        int mark = str.indexOf(' ');
        
        // handle invalid format
        if(mark == -1 || mark + 1 >= str.length()) {
            writeln("Err Invalid format, expected user name and hash");
            exit();
            return;
        }
        
        str = str.trim();
        String name = str.substring(0, mark);
        str = str.substring(mark + 1);
        
        UserHandle handle = con.getUserService();
        
        if(handle == null || handle.getStatus() != Status.READY) {
            System.err.println("WARNING: User service unavailable, accepting command line from localhost.");
            checkLocal();
        } else {
            try {
                
                IndirectionFactory fact = handle.getIndirectionFactory();  
                Indirection ind = fact.createIndirection("CRAM-MD5");
                Map map = new HashMap();
                map.put("TEXT", cramText);
                ind.setOptions(map);
                
                Authenticator auth = handle.getAuthenticator(name, ind);
                session = auth.authenticate(str.toCharArray());
                
                writer.print("OK");
                writer.commit();
                
                writeln(getIntro());
                
                cramText = null;
                isAuth = true;
            
            } catch(IndirectionUnavailableException e) { 
                writeln("ERR Internal error, please try later");
                //writer.commit();
                con.reportException(e);
                exit();
            } catch(AuthenticationFailedException e) { 
                writeln("ERR Invalid password");
                //writer.commit();
                exit();
                //con.reportException(e);
            } catch(NoSuchUserException e) { 
                writeln("ERR No such user");
                //writer.commit();
                exit();
                //con.reportException(e);
            } 
        }
    }
    
    
    /**
     * Send a first line containing a CRAM-MD5 text.
     */
    
    private void sendFirst() {
        StringBuffer buff = new StringBuffer("<");
        buff.append(con.getRandom()).append(".");
        buff.append(System.currentTimeMillis());
        buff.append("@").append(con.getBindAddress());
        buff.append(">");
        cramText = buff.toString();
        writer.print("AUTH ");
        writer.print(cramText);
        writer.commit();
    }
    
    
    // check if a connection is local and reply to client
    private void checkLocal() {
        if(!isLocal()) {
            writer.print("ERR User service unavailable, we only accept localhost connections");
            writer.commit();
            isAuth = false;
            exit();
        } else {
            writer.print("OK");
            writer.commit();
            writeln(getIntro());
            isAuth = true;
        }
    }
    
    
    // is this a connection from localhost
    private boolean isLocal() {
        try {
            InetAddress addr = channel.socket().getInetAddress();
            //System.out.println(addr);
            InetAddress host = InetAddress.getLocalHost();   
            //System.out.println(host);
            return host.equals(addr);
        } catch(UnknownHostException e) {
            return false;
        }
    }
    
    
    /**
     * Get welcome text.
     */
        
    private String getIntro() {
        StringBuffer buff = new StringBuffer("\r\nWelcome! This is Urd Norna Framework ver. ");
        buff.append(Constants.URD_VERSION).append("\r\n");
        buff.append(Constants.JVM_NAME).append(" (").append(Constants.JVM_VENDOR).append(") ").append(Constants.JVM_VERSION).append("\r\n");
        buff.append(Constants.OS).append(" version ").append(Constants.OS_VERSION).append(" (").append(System.getProperty("os.arch")).append(")").append("\r\n");
        buff.append(new Date().toString());
        return buff.toString();
    }
}
