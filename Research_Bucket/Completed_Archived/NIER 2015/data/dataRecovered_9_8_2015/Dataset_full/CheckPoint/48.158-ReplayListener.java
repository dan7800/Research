/*
 * Copyright (c) Core Developers Network LLC, All rights reserved
 */
package org.objectweb.howl.journal;

/**
 * @version $Revision: 1.1 $ $Date: 2004/01/26 20:59:30 $
 */
public interface ReplayListener {
    
    public void add( byte []data);
    public void startCheckPoint( long checkpointID );
    public void clearCheckPoint( long checkpointID );
    
}
