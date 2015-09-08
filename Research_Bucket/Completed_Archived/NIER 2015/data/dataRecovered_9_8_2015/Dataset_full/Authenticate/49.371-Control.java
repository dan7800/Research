/**
* Copyright (C) 2002 Lars J. Nilsson, webmaster at larsan.net
*
*   This program is free software; you can redistribute it and/or
*   modify it under the terms of the GNU Lesser General Public License
*   as published by the Free Software Foundation; either version 2.1
*   of the License, or (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU Lesser General Public License for more details.
*
*   You should have received a copy of the GNU Lesser General Public License
*   along with this program; if not, write to the Free Software
*   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. 
*/

package net.larsan.urd;

import java.io.*;
import java.net.*;
import java.util.*;

import net.larsan.urd.util.*;

/**
* Command line control connector. This class can be used to get connect
* to the Urd Server command line server.
*
* @author Lars J. Nilsson
* @version ##URD-VERSION##
* @see net.larsan.urd.cmd.CmdServer
*/


/// TODO: more startup options: ssl, user, pass, also it should be possible to execute
/// single commands like so: 'java net.larsan.urd.Control start /services/myservice'


public class Control extends Thread {

    private final static String ln = "\r\n";
    private final static String hln = System.getProperty("line.separator", ln);
    private final static String delim = ln + "." + ln;


    public static void main(String[] args) {
        try {
            CmdOptions cmdOpt = CmdOptions.parse(args);
            if(cmdOpt.printHelp()) printHelp(cmdOpt);
            else {
                Control c = new Control();
                c.connect(cmdOpt);
            }
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }
    
    private static void printHelp(CmdOptions opt) {
        System.out.println("Usage: java net.larsan.urd.Control [option[ option]]");
        System.out.println("Where an option takes the following form:");
        System.out.println("");
        System.out.println("\tflag [value[,value]]");
        System.out.println("");
        System.out.println("And flag might be one of the folowing:");
        System.out.println("");
        System.out.println("Example: ");
        System.out.println("");
        System.out.println("\tjava net.larsan.urd.Control -u myUsername -pw myPass");
        System.out.println("");
        opt.printOptions();
    }


    private String prefix;
    private CmdReader reader;
    private CmdOptions options;
    private PrintWriter writer;
    private BufferedReader in;
    private Socket sock;

    public Control(String prefix) {
        super("Urd Command Line");
        this.prefix = prefix;
    }
    
    public Control() {
        this("urd");
    }
    
    public void connect(CmdOptions opt) throws IOException {
        this.options = opt;
        System.out.println("");
        System.out.println("Urd Server Control ver. 1.0");
        System.out.println("Connecting to host " + options.getAddress() + ":" + options.getPort() + " ...");
        sock = new Socket(InetAddress.getByName(opt.getAddress()), opt.getPort());
        sock.setSoTimeout(30000);
        reader = new CmdReader(new InputStreamReader(new BufferedInputStream(sock.getInputStream())));
        writer = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(System.in));
        super.start();
    }
    
    public void disconnect() throws IOException {
        reader.close();
        writer.close();
        sock.close();
    }
    
    public void run() {
        if(doAuth()) {
            do {
                try {
                    readAnswer();
                } catch(SocketException e) {
                    System.out.println("");
                    System.out.println("Connection lost to server");
                    return;
                } catch(IOException e) {
                    e.printStackTrace();
                }
            } while(write());
        }
    }

    public void readAnswer() throws IOException {
        String tmp = reader.readAnswer();
        System.out.print(tmp);
        System.out.print(prefix);
        System.out.print("> ");
    }
    
    public boolean write() {
        try {
            String tmp = in.readLine();
            writer.write(tmp);
            writer.write(ln);
            writer.flush();
            if(isClose(tmp.toLowerCase().trim())) {
                disconnect();
                return false;
            } else return true;
        } catch(IOException e) {
            e.printStackTrace();
            return true;
        }
    }
    
    private boolean isClose(String str) {
        return (str.startsWith("exit") || str.startsWith("kill") || str.startsWith("shutdown"));
    }
    
    // do authentication process
    private boolean doAuth() {
        try {
            
            // prompt fro user and pass
            String user = options.getUser();
            if(user == null || user.length() == 0) user = prompt("User name: ");
            String pass = options.getPass();
            if(pass == null || pass.length() == 0) pass = prompt("Password: ");
           
            // check the protocol
            String tmp = reader.readAnswer();
            if(!tmp.startsWith("AUTH ") || tmp.length() < 6) throw new InternalError("Unrecognized first command: " + tmp);
            tmp = tmp.substring(5).trim();
            
            // create keyd MD5 digest
            HMAC mac = new HMAC("MD5", 64, pass.getBytes("US-ASCII"));
            byte[] digest = mac.digest(tmp.getBytes("US-ASCII"));

            // write
            writer.write(user);
            writer.write(" ");
            writer.write(HexUtils.toString(digest));
            writer.write(ln);
            writer.flush();
            
            // check
            tmp = reader.readAnswer();
            if(tmp.startsWith("OK")) return true;
            else {
                System.out.println("*** Failed to log in: " + tmp.substring(3).trim());
                return false;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }   
    }
    
    // read a question form the command line
    private String prompt(String question) throws IOException {
        System.out.print(question);
        return in.readLine().trim();
    }

    private static class CmdReader extends BufferedReader {
    
        public CmdReader(Reader in) {
            super(in);
        }
        
        public String readAnswer() throws IOException {
            StringBuffer buff = new StringBuffer();
            String tmp = null;
            while((tmp = super.readLine()) != null) {
                if(tmp.equals(".")) break;
                else buff.append(tmp).append(hln);
            }
            return buff.toString();
        }
    }
}