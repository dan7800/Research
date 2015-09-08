// ========================================================================
// $Id: Server.java,v 1.5 2004/05/09 20:31:06 gregwilkins Exp $
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.util.InetAddrPort;
import org.mortbay.util.LifeCycleThread;
import org.mortbay.util.LogSupport;

public class Server extends LifeCycleThread
{
    private static Log log = LogFactory.getLog(Server.class);

    Selector _selector;
    InetSocketAddress _address;
    ByteBufferPool _bufferPool;
    ArrayList _pending=new ArrayList();
    
    /* ------------------------------------------------------------ */
    public Server()
        throws IOException
    {}
    
    /* ------------------------------------------------------------ */
    public Server(ByteBufferPool pool,InetSocketAddress address)
        throws IOException
    {
        _address=address;
        _bufferPool=pool;
    }
    
    /* ------------------------------------------------------------ */
    public Server(ByteBufferPool pool,InetAddrPort address)
        throws IOException
    {
        _address=new InetSocketAddress(address.getInetAddress(),
                                       address.getPort());
        _bufferPool=pool;
    }

    /* ------------------------------------------------------------ */
    public InetSocketAddress getInetSocketAddress()
    {
        return _address;
    }
    
    /* ------------------------------------------------------------ */
    public void setInetSocketAddress(InetSocketAddress address)
    {
        if (isStarted())
            throw new IllegalStateException("Started");
        _address=address;
    }
    
    /* ------------------------------------------------------------ */
    public ByteBufferPool getBufferPool()
    {
        return _bufferPool;
    }

    /* ------------------------------------------------------------ */
    public void setBufferPool(ByteBufferPool bufferPool)
    {
        _bufferPool = bufferPool;
    }

    /* ------------------------------------------------------------ */
    public synchronized void connect(Connection connection)
        throws IOException
    {
        SocketChannel socket_channel= SocketChannel.open();
        socket_channel.configureBlocking(false);
        if(log.isDebugEnabled())log.debug("Connecting... "+socket_channel);

        if (socket_channel.connect(_address))
            connection.connected(socket_channel,_selector);
        
        _pending.add(socket_channel);
        _pending.add(connection);
        
        if(log.isDebugEnabled())log.debug("wakeup "+_selector);
        _selector.wakeup();
    }

    /* ------------------------------------------------------------ */
    public void start()
        throws Exception
    {
        if (isStarted())
            throw new IllegalStateException("Started");
        
        _selector=Selector.open();
        
        super.start();
    }

    /* ------------------------------------------------------------ */
    public void loop()
        throws Exception
    {
        if(log.isDebugEnabled())log.debug("server keys="+_selector.keys());
        if (_selector.select()>0)
        {
            Set ready=_selector.selectedKeys();
            Iterator iter = ready.iterator();
            while(iter.hasNext())
            {
                SelectionKey key = (SelectionKey)iter.next();
                iter.remove();
                
                Channel channel = key.channel();
                if(log.isDebugEnabled())log.debug("Ready key "+key+" for "+channel);

                if (!channel.isOpen())
                    key.cancel();
                else if (channel instanceof SocketChannel)
                {
                    SocketChannel socket_channel=(SocketChannel)channel;
                    Connection connection=(Connection)key.attachment();
                    
                    if ((key.interestOps()&SelectionKey.OP_CONNECT)!=0)
                    {
                        boolean connected=false;
                        
                        try{connected=socket_channel.finishConnect();}
                        catch(Exception e)
                        {
                            if (log.isDebugEnabled())log.warn(LogSupport.EXCEPTION,e);
                            else log.info(e.toString());
                            key.cancel();
                            connection.deallocate();
                        }
                        
                        if (connected)
                        {
                            connection.connected(socket_channel,_selector);
                            socket_channel.socket().setTcpNoDelay(true);
                            key.interestOps(key.interestOps()&~SelectionKey.OP_CONNECT
                                            |SelectionKey.OP_READ);
                        }
                        else
                            if(log.isDebugEnabled())log.debug("Not Connected "+socket_channel);
                    }
                    else if ((key.interestOps()&SelectionKey.OP_WRITE)!=0)
                        connection.serverWriteWakeup(key);
                    else if ((key.interestOps()&SelectionKey.OP_READ)!=0)
                        connection.server2client(key);

                }
            }
        }

        synchronized(this)
        {
            // Add pending connections.
            for (int i=0;i<_pending.size();i++)
            {
                SocketChannel sc = (SocketChannel)_pending.get(i++);
                Connection c=(Connection)_pending.get(i);
                if(log.isDebugEnabled())log.debug("register "+sc);
                sc.register(_selector,
                            sc.isConnected()
                            ?SelectionKey.OP_WRITE:SelectionKey.OP_CONNECT,
                            c);
            }
            _pending.clear();
        }
    }    
}
