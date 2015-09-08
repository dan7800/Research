/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

/*
 * Newsgroup.java
 *
 * IDEA: use SHA-1 digests of article as article ID.
 *
 * TODO: normal users cannot currently remove articles. Old articles are removed
 * by a special clean-up process that calls removeOldArticles_w() periodically
 * and will have a Globe user certificate to do so. Allowing users to remove the 
 * articles they posted is currently not supported by the Globe security framework.
 * We need an application level solution for this, e.g. return a cookie to the user
 * when posting, which is needed for removal, or something more complex that
 * takes into account malicious object servers.
 *
 * @author  arno
 */
package vu.gaia.apps.globevine;

import java.rmi.RemoteException;
import java.util.Date;
import java.io.IOException;

public interface Newsgroup extends java.rmi.Remote
{
    // Reading news
    public long getFirst()
        throws RemoteException;
    public long getLast()
        throws RemoteException;
    
    long[] newNews( Date Since )
        throws RemoteException;
    
    ArticleHeader getHeader( long N )
        throws RemoteException;
    
    /** 
     * Returns article body. This method signature assumes that the body is small enough
     * to be returned as a single block (i.e., not a 650 MB ISO9660 image).
     */
    byte[] getBody( long N )
        throws RemoteException, IOException;
    
    // Hmmm... don't like Java's tree support...
    //Tree getThread( long ParentN )
    //    throws RemoteException;
    
    // Posting news
    public long postArticle_w( ArticleHeader H, byte[] Body )
        throws RemoteException, IOException;
    public long postArticle_w( ArticleHeader H, byte[] Body, long ResponseToN )
        throws RemoteException, IOException;
    
    public void removeOldNews_w( Date Before )
        throws RemoteException;
}
