// ========================================================================
// $Id: ByteBufferPool.java,v 1.2 2004/05/09 20:31:06 gregwilkins Exp $
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

import java.nio.ByteBuffer;
import java.util.ArrayList;


public class ByteBufferPool
{
    private ArrayList _pool=new ArrayList();
    private int _capacity =4096;
    private boolean _direct =false;

    /* ------------------------------------------------------------ */
    public ByteBufferPool()
    {}
    
    /* ------------------------------------------------------------ */
    public ByteBufferPool(int capacity, boolean direct)
    {
        _capacity=capacity;
        _direct=direct;
    }
    
    /* ------------------------------------------------------------ */
    public int getCapacity()
    {
        return _capacity;
    }
    
    /* ------------------------------------------------------------ */
    public void setCapacity(int capacity)
    {
        _capacity = capacity;
    }
    
    /* ------------------------------------------------------------ */
    public boolean isDirect()
    {
        return _direct;
    }
    
    /* ------------------------------------------------------------ */
    public void setDirect(boolean direct)
    {
        _direct = direct;
    }

    /* ------------------------------------------------------------ */
    public synchronized ByteBuffer get()
    {
        if (_pool.isEmpty())
        {
            if (_direct)
                return ByteBuffer.allocateDirect(_capacity);
            return ByteBuffer.allocate(_capacity);
        }

        ByteBuffer buffer = (ByteBuffer)_pool.remove(_pool.size()-1);
        buffer.clear();
        return buffer;
    }

    /* ------------------------------------------------------------ */
    public synchronized void add(ByteBuffer buffer)
    {
        if (buffer.capacity()==_capacity)
            _pool.add(buffer);
    }
    
}

