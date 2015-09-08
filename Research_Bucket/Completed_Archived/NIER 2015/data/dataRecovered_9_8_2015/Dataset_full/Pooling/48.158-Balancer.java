// ========================================================================
// $Id: Balancer.java,v 1.3 2004/05/09 20:31:06 gregwilkins Exp $
// Copyright 2002-2004 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.loadbalancer;

import org.mortbay.util.InetAddrPort;

/* ------------------------------------------------------------ */
/** TCP/IP connection load balancer
 *
 * @version $Id: Balancer.java,v 1.3 2004/05/09 20:31:06 gregwilkins Exp $
 * @author Greg Wilkins (gregw)
 */
public class Balancer 
{
    public static void usage()
    {
        System.err.println("Usage - java org.mortbay.loadbalancer.Balancer [[host]:port ... ] - [[server]:port ... ]");
        System.exit(1);
    }
    
    /* ------------------------------------------------------------ */
    public static void main(String[] arg)
        throws Exception
    {
        if (arg.length<3)
            usage();
        
        ByteBufferPool pool = new ByteBufferPool(4096,true);

        int c=-1;
        for (int i=0;i<arg.length;i++)
            if (arg[i].equals("-"))
                c=i;
        if (c<0)
            usage();

        Listener[] listener= new Listener[c];
        Server[] server= new Server[arg.length-c-1];
        Policy policy = new Policy(server);

        for (int i=0;i<arg.length;i++)
        {
            if (i<c)
                listener[i] = new Listener(pool,new
                    InetAddrPort(arg[i]),policy);
            if (i>c)
                server[i-c-1] = new Server(pool,new InetAddrPort(arg[i]));
        }
        
        for (int i=arg.length;i-->0;)
        {
            if (i<c)
                listener[i].start();
            if (i>c)
                server[i-c-1].start();
        }
    }
}
