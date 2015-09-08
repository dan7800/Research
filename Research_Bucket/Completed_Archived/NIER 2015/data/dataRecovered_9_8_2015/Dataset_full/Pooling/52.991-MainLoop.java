/********************************************************************************
 *
 * jMule - a Java massive parallel file sharing client
 *
 * Copyright (C) by the jMuleGroup ( see the CREDITS file )
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id: MainLoop.java,v 1.3 2003/11/30 16:05:54 pbolle Exp $
 *
 ********************************************************************************/

package org.jmule.ui.struts.common;

import org.jmule.core.ConnectionManager;
import org.jmule.core.DownloadManager;

import java.util.List;

public class MainLoop extends Thread{
    private ConnectionManager connectionManager;
    private DownloadManager downloadManager;
    private List protocols;
    private boolean netIO=true;
    private static MainLoop instance=null;

    private MainLoop(){
    }

    public static final MainLoop getInstance(){
        if (instance==null){
            instance=new MainLoop();
        }
        return (instance);
    }

    public void init(ConnectionManager connectionManager, DownloadManager downloadManager,List protocols) {
        this.connectionManager=connectionManager;
        this.downloadManager=downloadManager;
        this.protocols=protocols;
    }
    
    
    public void run() {
        long time = System.currentTimeMillis();
        long checktime = 0;

        // jMule main application, FOREVER loop
        while (isNetIO()) {

            time = System.currentTimeMillis();
            // use  connectionManager.doNetIo() readSelector.select(long time) instead of main "sleep"
            // => high speed network transfer and less pooling for none I/O
            if (checktime+100<time ) {
                checktime = time;
                downloadManager.check();
                // TODO Change from old to remote api
                //Main.checkProtocols(protocols);
                connectionManager.checkAllConnections();
            }
            connectionManager.doNetIo();
        }
    }

    public boolean isNetIO() {
        return netIO;
    }

    public void setNetIO(boolean netIO) {
        this.netIO = netIO;
        if (netIO==true){
            instance.start();
        }
    }
}
/********************************************************************************
 *
 * jMule - a Java massive parallel file sharing client
 *
 * Copyright (C) by the jMuleGroup ( see the CREDITS file )
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id: MainLoop.java,v 1.6 2003/12/05 17:17:54 emarant Exp $
 *
 ********************************************************************************/
package org.jmule.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;

public class MainLoop extends Thread {
    private ConnectionManager connectionManager = ConnectionManager.getInstance();
    private DownloadManager downloadManager = DownloadManager.getInstance();
    private UploadManager uploadManager = UploadManager.getInstance();
    private boolean netIOEnabled=true;
    private static MainLoop singleton=null;

    private MainLoop(){
    }

    public static final MainLoop getInstance(){
        if (singleton==null){
            singleton=new MainLoop();
        }
        return singleton;
    }  
    
    public void run() {
        //warning not the time spent but the time passed by
        long previoustime;
        long time = System.currentTimeMillis();
        long networkChecktime = time;
        long uploadmanagerChecktime = time;
        long downloadmanagerChecktime = time + 1300;
        long protocolChecktime = time + 1600;

        for (int i = 0; i < times.length; i++) {
            times[i] = 0;
        }
        starttime = time;
        
        // jMule main application, FOREVER loop
        while (isNetIOEnabled()) {
            
            previoustime = time;
            time = System.currentTimeMillis();
            // use  connectionManager.doNetIo() readSelector.select(long time) instead of main "sleep"
            // => high speed network transfer and less pooling for none I/O
            if (networkChecktime+100<time ) {
                networkChecktime = time;
                connectionManager.checkAllConnections();
                if (downloadmanagerChecktime+1000<time) {
                    downloadmanagerChecktime = time;
                    downloadManager.check();
                } else if (uploadmanagerChecktime+2000<time) {
                    uploadmanagerChecktime = time;
                    uploadManager.check();
                } else if (protocolChecktime+2000<time) {
                    protocolChecktime = time;
                    checkProtocols();
                }
            }
            connectionManager.doNetIo();
            
            if (time >= previoustime) {
                // on my machine, this is not always true (yeah I kno it's fast *g*), leading to an error. So i added this check. (casper)
                if (time - previoustime < 1000) {
                    times[(int) ((time - previoustime) / 10)]++;
                } else {
                    addtimescunt++;
                    addtimessum += (time - previoustime);
                }
            }
        }
    }

    public boolean isNetIOEnabled() {
        return netIOEnabled;
    }

    public void setNetIOEnabled(boolean netIO) {
        //if thrad need use thread excplicit, but currently it is poitnless to have main thread die and run this none deamon thread and vm waiting to finish appl. close procedure for this thread! 
/*        if (netIO==true){
            singleton.start();
        }*/
        netIOEnabled = netIO;
    }
    
    /** Loops over all known protocols and calls their check() method.
     * Only the enabled protocols will be called.
     */
    private void checkProtocols() {
        // XXX: Access throu the array is a bit faster than by the Iterators ... and this
        // is very frequently used method
        Iterator it = PluginManager.getInstance().iterateProtocols();;
        while (it.hasNext()) {
            P2PProtocol protocol = (P2PProtocol)it.next();
            if (protocol.isEnabled())
                protocol.check();
        }
    }

    //warning not the time spent but the time passed by
      static long[] times = new long[100];
      static long addtimescunt = 0;
      static long addtimessum = 0;
      static long starttime = 0;
      
      public void saveStatistics() {
          try {
              PrintStream printStream =
                  new PrintStream(new BufferedOutputStream(new FileOutputStream(new File("timings.log"))), false);

              for (int i = 0; i < times.length; i++) {
                  printStream.print(i * 10);
                  printStream.print('\t');
                  printStream.println(times[i]);
              }
              printStream.println("additonal: sum = " + addtimessum + " of " + addtimescunt + " turn(s) >= 1000 ms  ");
              printStream.println("overalltime = " + (System.currentTimeMillis() - starttime) + " in ms");
              printStream.close();
          } catch (IOException ioe) {
              for (int i = 0; i < times.length; i++) {
                  System.out.print(i * 10);
                  System.out.print('\t');
                  System.out.println(times[i]);
              }
              System.out.println("additonal: sum = " + addtimessum + " of " + addtimescunt + " turn(s) >= 1000 ms  ");
              System.out.println("overalltime = " + (System.currentTimeMillis() - starttime) + " in ms");
          }
      }
}
