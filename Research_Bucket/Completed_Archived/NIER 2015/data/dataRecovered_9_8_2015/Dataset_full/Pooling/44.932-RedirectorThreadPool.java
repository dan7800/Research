/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// RedirectorThreadPool.java

package vu.globe.svcs.gred;


import java.net.ServerSocket;


/**
 * This class represents a simple thread pool. The thread pool consists of
 * a collection of redirector pool threads. Each pool thread sits in an
 * infinite loop waiting for connection requests on a (shared) server
 * socket.
 */
public class RedirectorThreadPool
{
  ThreadGroup _group;
  Thread _pool[];


  /**
   * Instance creation. Creates and starts the pool threads. Each pool
   * thread sits in an infinite loop waiting for connection requests
   * on the server socket.
   *
   * @param  n     number of threads in the thread pool
   * @param  sock  server listening socket
   */
  public RedirectorThreadPool(int n, ServerSocket sock)
  {
    _group = new ThreadGroup("Redirector Thread Pool");
    _pool = new Thread[n];

    for (int i = 0; i < n; i++) {
      _pool[i] = new RedirectorPoolThread(i, _group, sock);
      _pool[i].start();
    }
  }


  /**
   * Returns an estimate of the number of active threads in the thread pool.
   */
  public int activeCount()
  {
    return _group.activeCount();
  }


  /**
   * Return the number of threads in this thread pool.
   */
  public int size()
  {
    return _pool.length;
  }
}
