package com.arsenal.rtcomm.server.http;

/*
 * Java Network Programming, Second Edition
 * Merlin Hughes, Michael Shoffner, Derek Hamner
 * Manning Publications Company; ISBN 188477749X
 *
 * http://nitric.com/jnp/
 *
 * Copyright (c) 1997-1999 Merlin Hughes, Michael Shoffner, Derek Hamner;
 * all rights reserved; see license.txt for details.
 */

import java.io.*;
import java.net.*;
import java.util.*;

import com.arsenal.rtcomm.server.ConnectionManager;
import com.arsenal.log.Log;

public class Httpd implements Runnable {

  // socket from initial connection
  protected Socket client;
  protected HttpInputStream httpIn = null;
  public ConnectionManager connectionManager = null;

  public Httpd (ConnectionManager connectionManager, Socket client, HttpInputStream httpIn) {
    Log.debug(this, "Httpd<init> socketID " + client);

    this.connectionManager = connectionManager;
    this.client = client;
    this.httpIn = httpIn;
    httpIn.setSocket (client);
  }



  public void run () {
    Log.debug(this, "Httpd.run socketID " + client);

    boolean close = true;

    try {
      HttpProcessor processor = getProcessor (httpIn);
      Log.debug(this, httpIn.getPath ());
      OutputStream out = client.getOutputStream ();
      HttpOutputStream httpOut = new HttpOutputStream (out, httpIn);
      httpOut.setSocket (client);
      close = processor.processRequest (httpOut);
      httpOut.flush ();

    } 
    catch (IOException ex) {
        Log.debug(this, "Httpd.run " + ex);
    } 
    finally {
      if (close) {
        try {
          httpIn.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close httpIn" + ignored);
        }

        try {
          client.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close socket" + ignored);
        }
        httpIn = null;
        client = null;
      } else {
        Log.debug(this, "Httpd.run NOT trying to close " + client);
      } 
    }
  }

  protected HttpProcessor getProcessor (HttpInputStream httpIn) {
    try {
      httpIn.readRequest ();
      String path = httpIn.getPath ();
      Log.debug(this, "HTTPD: path is: " + path);
      Log.debug(this, "Httpd.getProcessor " + path + " " + client);
      Log.debug(this, httpIn.getPath ());

      if (path.startsWith (HTTP.TUNNEL_CONNECT)){
        return new TunnelConnect (this.connectionManager, httpIn);
      }
      else if (path.startsWith (HTTP.TUNNEL_DISCONNECT)){
        return new TunnelDisconnect (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.TUNNEL_IN)){
       return new TunnelIn (this.connectionManager, httpIn, path);
      }
//      else if (path.startsWith (HTTP.TUNNEL_OUT)){
//        return new TunnelOut (httpIn);
//      }
     else if (path.startsWith (HTTP.TUNNEL_PING)){
        return new TunnelPing (httpIn);
      }
      else if (path.startsWith (HTTP.FILE_IN)){
       return new FileIn (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.FILE_OUT)){
       return new FileOut (this.connectionManager, httpIn, path);
      }
     else {
       return new HttpFile (httpIn);
     }

    } catch (HttpException ex) {
      return ex;

    } catch (Exception ex) {
      Log.debug(this, "Httpd.run close " + ex);

      StringWriter trace = new StringWriter ();
      ex.printStackTrace (new PrintWriter (trace, true));
      return new HttpException (HTTP.STATUS_INTERNAL_ERROR,
                                "<PRE>" + ex + "\n\n" + trace + "</PRE>");
    }
  }

  public static void main (String[] args) throws IOException {
  }
}package com.arsenal.rtcomm.server.http;

/*
 * Java Network Programming, Second Edition
 * Merlin Hughes, Michael Shoffner, Derek Hamner
 * Manning Publications Company; ISBN 188477749X
 *
 * http://nitric.com/jnp/
 *
 * Copyright (c) 1997-1999 Merlin Hughes, Michael Shoffner, Derek Hamner;
 * all rights reserved; see license.txt for details.
 */

import java.io.*;
import java.net.*;
import java.util.*;

import com.arsenal.rtcomm.server.ConnectionManager;
import com.arsenal.log.Log;

public class Httpd implements Runnable {

  // socket from initial connection
  protected Socket client;
  protected HttpInputStream httpIn = null;
  public ConnectionManager connectionManager = null;

  public Httpd (ConnectionManager connectionManager, Socket client, HttpInputStream httpIn) {
    Log.debug(this, "Httpd<init> socketID " + client);

    this.connectionManager = connectionManager;
    this.client = client;
    this.httpIn = httpIn;
    httpIn.setSocket (client);
  }



  public void run () {
    Log.debug(this, "Httpd.run socketID " + client);

    boolean close = true;

    try {
      HttpProcessor processor = getProcessor (httpIn);
      Log.debug(this, httpIn.getPath ());
      OutputStream out = client.getOutputStream ();
      HttpOutputStream httpOut = new HttpOutputStream (out, httpIn);
      httpOut.setSocket (client);
      close = processor.processRequest (httpOut);
      httpOut.flush ();

    } 
    catch (IOException ex) {
        Log.debug(this, "Httpd.run " + ex);
    } 
    finally {
      if (close) {
        try {
          httpIn.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close httpIn" + ignored);
        }

        try {
          client.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close socket" + ignored);
        }
        httpIn = null;
        client = null;
      } else {
        Log.debug(this, "Httpd.run NOT trying to close " + client);
      } 
    }
  }

  protected HttpProcessor getProcessor (HttpInputStream httpIn) {
    try {
      httpIn.readRequest ();
      String path = httpIn.getPath ();
      Log.debug(this, "HTTPD: path is: " + path);
      Log.debug(this, "Httpd.getProcessor " + path + " " + client);
      Log.debug(this, httpIn.getPath ());

      if (path.startsWith (HTTP.TUNNEL_CONNECT)){
        return new TunnelConnect (this.connectionManager, httpIn);
      }
      else if (path.startsWith (HTTP.TUNNEL_DISCONNECT)){
        return new TunnelDisconnect (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.TUNNEL_IN)){
       return new TunnelIn (this.connectionManager, httpIn, path);
      }
//      else if (path.startsWith (HTTP.TUNNEL_OUT)){
//        return new TunnelOut (httpIn);
//      }
     else if (path.startsWith (HTTP.TUNNEL_PING)){
        return new TunnelPing (httpIn);
      }
      else if (path.startsWith (HTTP.FILE_IN)){
       return new FileIn (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.FILE_OUT)){
       return new FileOut (this.connectionManager, httpIn, path);
      }
     else {
       return new HttpFile (httpIn);
     }

    } catch (HttpException ex) {
      return ex;

    } catch (Exception ex) {
      Log.debug(this, "Httpd.run close " + ex);

      StringWriter trace = new StringWriter ();
      ex.printStackTrace (new PrintWriter (trace, true));
      return new HttpException (HTTP.STATUS_INTERNAL_ERROR,
                                "<PRE>" + ex + "\n\n" + trace + "</PRE>");
    }
  }

  public static void main (String[] args) throws IOException {
  }
}package com.arsenal.rtcomm.server.http;

/*
 * Java Network Programming, Second Edition
 * Merlin Hughes, Michael Shoffner, Derek Hamner
 * Manning Publications Company; ISBN 188477749X
 *
 * http://nitric.com/jnp/
 *
 * Copyright (c) 1997-1999 Merlin Hughes, Michael Shoffner, Derek Hamner;
 * all rights reserved; see license.txt for details.
 */

import java.io.*;
import java.net.*;
import java.util.*;

import com.arsenal.rtcomm.server.ConnectionManager;
import com.arsenal.log.Log;

public class Httpd implements Runnable {

  // socket from initial connection
  protected Socket client;
  protected HttpInputStream httpIn = null;
  public ConnectionManager connectionManager = null;

  public Httpd (ConnectionManager connectionManager, Socket client, HttpInputStream httpIn) {
    Log.debug(this, "Httpd<init> socketID " + client);

    this.connectionManager = connectionManager;
    this.client = client;
    this.httpIn = httpIn;
    httpIn.setSocket (client);
  }



  public void run () {
    Log.debug(this, "Httpd.run socketID " + client);

    boolean close = true;

    try {
      HttpProcessor processor = getProcessor (httpIn);
      Log.debug(this, httpIn.getPath ());
      OutputStream out = client.getOutputStream ();
      HttpOutputStream httpOut = new HttpOutputStream (out, httpIn);
      httpOut.setSocket (client);
      close = processor.processRequest (httpOut);
      httpOut.flush ();

    } 
    catch (IOException ex) {
        Log.debug(this, "Httpd.run " + ex);
    } 
    finally {
      if (close) {
        try {
          httpIn.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close httpIn" + ignored);
        }

        try {
          client.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close socket" + ignored);
        }
        httpIn = null;
        client = null;
      } else {
        Log.debug(this, "Httpd.run NOT trying to close " + client);
      } 
    }
  }

  protected HttpProcessor getProcessor (HttpInputStream httpIn) {
    try {
      httpIn.readRequest ();
      String path = httpIn.getPath ();
      Log.debug(this, "HTTPD: path is: " + path);
      Log.debug(this, "Httpd.getProcessor " + path + " " + client);
      Log.debug(this, httpIn.getPath ());

      if (path.startsWith (HTTP.TUNNEL_CONNECT)){
        return new TunnelConnect (this.connectionManager, httpIn);
      }
      else if (path.startsWith (HTTP.TUNNEL_DISCONNECT)){
        return new TunnelDisconnect (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.TUNNEL_IN)){
       return new TunnelIn (this.connectionManager, httpIn, path);
      }
//      else if (path.startsWith (HTTP.TUNNEL_OUT)){
//        return new TunnelOut (httpIn);
//      }
     else if (path.startsWith (HTTP.TUNNEL_PING)){
        return new TunnelPing (httpIn);
      }
      else if (path.startsWith (HTTP.FILE_IN)){
       return new FileIn (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.FILE_OUT)){
       return new FileOut (this.connectionManager, httpIn, path);
      }
     else {
       return new HttpFile (httpIn);
     }

    } catch (HttpException ex) {
      return ex;

    } catch (Exception ex) {
      Log.debug(this, "Httpd.run close " + ex);

      StringWriter trace = new StringWriter ();
      ex.printStackTrace (new PrintWriter (trace, true));
      return new HttpException (HTTP.STATUS_INTERNAL_ERROR,
                                "<PRE>" + ex + "\n\n" + trace + "</PRE>");
    }
  }

  public static void main (String[] args) throws IOException {
  }
}package com.arsenal.rtcomm.server.http;

/*
 * Java Network Programming, Second Edition
 * Merlin Hughes, Michael Shoffner, Derek Hamner
 * Manning Publications Company; ISBN 188477749X
 *
 * http://nitric.com/jnp/
 *
 * Copyright (c) 1997-1999 Merlin Hughes, Michael Shoffner, Derek Hamner;
 * all rights reserved; see license.txt for details.
 */

import java.io.*;
import java.net.*;
import java.util.*;

import com.arsenal.rtcomm.server.ConnectionManager;
import com.arsenal.log.Log;

public class Httpd implements Runnable {

  // socket from initial connection
  protected Socket client;
  protected HttpInputStream httpIn = null;
  public ConnectionManager connectionManager = null;

  public Httpd (ConnectionManager connectionManager, Socket client, HttpInputStream httpIn) {
    Log.debug(this, "Httpd<init> socketID " + client);

    this.connectionManager = connectionManager;
    this.client = client;
    this.httpIn = httpIn;
    httpIn.setSocket (client);
  }



  public void run () {
    Log.debug(this, "Httpd.run socketID " + client);

    boolean close = true;

    try {
      HttpProcessor processor = getProcessor (httpIn);
      Log.debug(this, httpIn.getPath ());
      OutputStream out = client.getOutputStream ();
      HttpOutputStream httpOut = new HttpOutputStream (out, httpIn);
      httpOut.setSocket (client);
      close = processor.processRequest (httpOut);
      httpOut.flush ();

    } 
    catch (IOException ex) {
        Log.debug(this, "Httpd.run " + ex);
    } 
    finally {
      if (close) {
        try {
          httpIn.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close httpIn" + ignored);
        }

        try {
          client.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close socket" + ignored);
        }
        httpIn = null;
        client = null;
      } else {
        Log.debug(this, "Httpd.run NOT trying to close " + client);
      } 
    }
  }

  protected HttpProcessor getProcessor (HttpInputStream httpIn) {
    try {
      httpIn.readRequest ();
      String path = httpIn.getPath ();
      Log.debug(this, "HTTPD: path is: " + path);
      Log.debug(this, "Httpd.getProcessor " + path + " " + client);
      Log.debug(this, httpIn.getPath ());

      if (path.startsWith (HTTP.TUNNEL_CONNECT)){
        return new TunnelConnect (this.connectionManager, httpIn);
      }
      else if (path.startsWith (HTTP.TUNNEL_DISCONNECT)){
        return new TunnelDisconnect (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.TUNNEL_IN)){
       return new TunnelIn (this.connectionManager, httpIn, path);
      }
//      else if (path.startsWith (HTTP.TUNNEL_OUT)){
//        return new TunnelOut (httpIn);
//      }
     else if (path.startsWith (HTTP.TUNNEL_PING)){
        return new TunnelPing (httpIn);
      }
      else if (path.startsWith (HTTP.FILE_IN)){
       return new FileIn (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.FILE_OUT)){
       return new FileOut (this.connectionManager, httpIn, path);
      }
     else {
       return new HttpFile (httpIn);
     }

    } catch (HttpException ex) {
      return ex;

    } catch (Exception ex) {
      Log.debug(this, "Httpd.run close " + ex);

      StringWriter trace = new StringWriter ();
      ex.printStackTrace (new PrintWriter (trace, true));
      return new HttpException (HTTP.STATUS_INTERNAL_ERROR,
                                "<PRE>" + ex + "\n\n" + trace + "</PRE>");
    }
  }

  public static void main (String[] args) throws IOException {
  }
}package com.arsenal.rtcomm.server.http;

/*
 * Java Network Programming, Second Edition
 * Merlin Hughes, Michael Shoffner, Derek Hamner
 * Manning Publications Company; ISBN 188477749X
 *
 * http://nitric.com/jnp/
 *
 * Copyright (c) 1997-1999 Merlin Hughes, Michael Shoffner, Derek Hamner;
 * all rights reserved; see license.txt for details.
 */

import java.io.*;
import java.net.*;
import java.util.*;

import com.arsenal.rtcomm.server.ConnectionManager;
import com.arsenal.log.Log;

public class Httpd implements Runnable {

  // socket from initial connection
  protected Socket client;
  protected HttpInputStream httpIn = null;
  public ConnectionManager connectionManager = null;

  public Httpd (ConnectionManager connectionManager, Socket client, HttpInputStream httpIn) {
    Log.debug(this, "Httpd<init> socketID " + client);

    this.connectionManager = connectionManager;
    this.client = client;
    this.httpIn = httpIn;
    httpIn.setSocket (client);
  }



  public void run () {
    Log.debug(this, "Httpd.run socketID " + client);

    boolean close = true;

    try {
      HttpProcessor processor = getProcessor (httpIn);
      Log.debug(this, httpIn.getPath ());
      OutputStream out = client.getOutputStream ();
      HttpOutputStream httpOut = new HttpOutputStream (out, httpIn);
      httpOut.setSocket (client);
      close = processor.processRequest (httpOut);
      httpOut.flush ();

    } 
    catch (IOException ex) {
        Log.debug(this, "Httpd.run " + ex);
    } 
    finally {
      if (close) {
        try {
          httpIn.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close httpIn" + ignored);
        }

        try {
          client.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close socket" + ignored);
        }
        httpIn = null;
        client = null;
      } else {
        Log.debug(this, "Httpd.run NOT trying to close " + client);
      } 
    }
  }

  protected HttpProcessor getProcessor (HttpInputStream httpIn) {
    try {
      httpIn.readRequest ();
      String path = httpIn.getPath ();
      Log.debug(this, "HTTPD: path is: " + path);
      Log.debug(this, "Httpd.getProcessor " + path + " " + client);
      Log.debug(this, httpIn.getPath ());

      if (path.startsWith (HTTP.TUNNEL_CONNECT)){
        return new TunnelConnect (this.connectionManager, httpIn);
      }
      else if (path.startsWith (HTTP.TUNNEL_DISCONNECT)){
        return new TunnelDisconnect (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.TUNNEL_IN)){
       return new TunnelIn (this.connectionManager, httpIn, path);
      }
//      else if (path.startsWith (HTTP.TUNNEL_OUT)){
//        return new TunnelOut (httpIn);
//      }
     else if (path.startsWith (HTTP.TUNNEL_PING)){
        return new TunnelPing (httpIn);
      }
      else if (path.startsWith (HTTP.FILE_IN)){
       return new FileIn (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.FILE_OUT)){
       return new FileOut (this.connectionManager, httpIn, path);
      }
     else {
       return new HttpFile (httpIn);
     }

    } catch (HttpException ex) {
      return ex;

    } catch (Exception ex) {
      Log.debug(this, "Httpd.run close " + ex);

      StringWriter trace = new StringWriter ();
      ex.printStackTrace (new PrintWriter (trace, true));
      return new HttpException (HTTP.STATUS_INTERNAL_ERROR,
                                "<PRE>" + ex + "\n\n" + trace + "</PRE>");
    }
  }

  public static void main (String[] args) throws IOException {
  }
}package com.arsenal.rtcomm.server.http;

/*
 * Java Network Programming, Second Edition
 * Merlin Hughes, Michael Shoffner, Derek Hamner
 * Manning Publications Company; ISBN 188477749X
 *
 * http://nitric.com/jnp/
 *
 * Copyright (c) 1997-1999 Merlin Hughes, Michael Shoffner, Derek Hamner;
 * all rights reserved; see license.txt for details.
 */

import java.io.*;
import java.net.*;
import java.util.*;

import com.arsenal.rtcomm.server.ConnectionManager;
import com.arsenal.log.Log;

public class Httpd implements Runnable {

  // socket from initial connection
  protected Socket client;
  protected HttpInputStream httpIn = null;
  public ConnectionManager connectionManager = null;

  public Httpd (ConnectionManager connectionManager, Socket client, HttpInputStream httpIn) {
    Log.debug(this, "Httpd<init> socketID " + client);

    this.connectionManager = connectionManager;
    this.client = client;
    this.httpIn = httpIn;
    httpIn.setSocket (client);
  }



  public void run () {
    Log.debug(this, "Httpd.run socketID " + client);

    boolean close = true;

    try {
      HttpProcessor processor = getProcessor (httpIn);
      Log.debug(this, httpIn.getPath ());
      OutputStream out = client.getOutputStream ();
      HttpOutputStream httpOut = new HttpOutputStream (out, httpIn);
      httpOut.setSocket (client);
      close = processor.processRequest (httpOut);
      httpOut.flush ();

    } 
    catch (IOException ex) {
        Log.debug(this, "Httpd.run " + ex);
    } 
    finally {
      if (close) {
        try {
          httpIn.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close httpIn" + ignored);
        }

        try {
          client.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close socket" + ignored);
        }
        httpIn = null;
        client = null;
      } else {
        Log.debug(this, "Httpd.run NOT trying to close " + client);
      } 
    }
  }

  protected HttpProcessor getProcessor (HttpInputStream httpIn) {
    try {
      httpIn.readRequest ();
      String path = httpIn.getPath ();
      Log.debug(this, "HTTPD: path is: " + path);
      Log.debug(this, "Httpd.getProcessor " + path + " " + client);
      Log.debug(this, httpIn.getPath ());

      if (path.startsWith (HTTP.TUNNEL_CONNECT)){
        return new TunnelConnect (this.connectionManager, httpIn);
      }
      else if (path.startsWith (HTTP.TUNNEL_DISCONNECT)){
        return new TunnelDisconnect (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.TUNNEL_IN)){
       return new TunnelIn (this.connectionManager, httpIn, path);
      }
//      else if (path.startsWith (HTTP.TUNNEL_OUT)){
//        return new TunnelOut (httpIn);
//      }
     else if (path.startsWith (HTTP.TUNNEL_PING)){
        return new TunnelPing (httpIn);
      }
      else if (path.startsWith (HTTP.FILE_IN)){
       return new FileIn (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.FILE_OUT)){
       return new FileOut (this.connectionManager, httpIn, path);
      }
     else {
       return new HttpFile (httpIn);
     }

    } catch (HttpException ex) {
      return ex;

    } catch (Exception ex) {
      Log.debug(this, "Httpd.run close " + ex);

      StringWriter trace = new StringWriter ();
      ex.printStackTrace (new PrintWriter (trace, true));
      return new HttpException (HTTP.STATUS_INTERNAL_ERROR,
                                "<PRE>" + ex + "\n\n" + trace + "</PRE>");
    }
  }

  public static void main (String[] args) throws IOException {
  }
}package com.arsenal.rtcomm.server.http;

/*
 * Java Network Programming, Second Edition
 * Merlin Hughes, Michael Shoffner, Derek Hamner
 * Manning Publications Company; ISBN 188477749X
 *
 * http://nitric.com/jnp/
 *
 * Copyright (c) 1997-1999 Merlin Hughes, Michael Shoffner, Derek Hamner;
 * all rights reserved; see license.txt for details.
 */

import java.io.*;
import java.net.*;
import java.util.*;

import com.arsenal.rtcomm.server.ConnectionManager;
import com.arsenal.log.Log;

public class Httpd implements Runnable {

  // socket from initial connection
  protected Socket client;
  protected HttpInputStream httpIn = null;
  public ConnectionManager connectionManager = null;

  public Httpd (ConnectionManager connectionManager, Socket client, HttpInputStream httpIn) {
    Log.debug(this, "Httpd<init> socketID " + client);

    this.connectionManager = connectionManager;
    this.client = client;
    this.httpIn = httpIn;
    httpIn.setSocket (client);
  }



  public void run () {
    Log.debug(this, "Httpd.run socketID " + client);

    boolean close = true;

    try {
      HttpProcessor processor = getProcessor (httpIn);
      Log.debug(this, httpIn.getPath ());
      OutputStream out = client.getOutputStream ();
      HttpOutputStream httpOut = new HttpOutputStream (out, httpIn);
      httpOut.setSocket (client);
      close = processor.processRequest (httpOut);
      httpOut.flush ();

    } 
    catch (IOException ex) {
        Log.debug(this, "Httpd.run " + ex);
    } 
    finally {
      if (close) {
        try {
          httpIn.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close httpIn" + ignored);
        }

        try {
          client.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close socket" + ignored);
        }
        httpIn = null;
        client = null;
      } else {
        Log.debug(this, "Httpd.run NOT trying to close " + client);
      } 
    }
  }

  protected HttpProcessor getProcessor (HttpInputStream httpIn) {
    try {
      httpIn.readRequest ();
      String path = httpIn.getPath ();
      Log.debug(this, "HTTPD: path is: " + path);
      Log.debug(this, "Httpd.getProcessor " + path + " " + client);
      Log.debug(this, httpIn.getPath ());

      if (path.startsWith (HTTP.TUNNEL_CONNECT)){
        return new TunnelConnect (this.connectionManager, httpIn);
      }
      else if (path.startsWith (HTTP.TUNNEL_DISCONNECT)){
        return new TunnelDisconnect (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.TUNNEL_IN)){
       return new TunnelIn (this.connectionManager, httpIn, path);
      }
//      else if (path.startsWith (HTTP.TUNNEL_OUT)){
//        return new TunnelOut (httpIn);
//      }
     else if (path.startsWith (HTTP.TUNNEL_PING)){
        return new TunnelPing (httpIn);
      }
      else if (path.startsWith (HTTP.FILE_IN)){
       return new FileIn (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.FILE_OUT)){
       return new FileOut (this.connectionManager, httpIn, path);
      }
     else {
       return new HttpFile (httpIn);
     }

    } catch (HttpException ex) {
      return ex;

    } catch (Exception ex) {
      Log.debug(this, "Httpd.run close " + ex);

      StringWriter trace = new StringWriter ();
      ex.printStackTrace (new PrintWriter (trace, true));
      return new HttpException (HTTP.STATUS_INTERNAL_ERROR,
                                "<PRE>" + ex + "\n\n" + trace + "</PRE>");
    }
  }

  public static void main (String[] args) throws IOException {
  }
}package com.arsenal.rtcomm.server.http;

/*
 * Java Network Programming, Second Edition
 * Merlin Hughes, Michael Shoffner, Derek Hamner
 * Manning Publications Company; ISBN 188477749X
 *
 * http://nitric.com/jnp/
 *
 * Copyright (c) 1997-1999 Merlin Hughes, Michael Shoffner, Derek Hamner;
 * all rights reserved; see license.txt for details.
 */

import java.io.*;
import java.net.*;
import java.util.*;

import com.arsenal.rtcomm.server.ConnectionManager;
import com.arsenal.log.Log;

public class Httpd implements Runnable {

  // socket from initial connection
  protected Socket client;
  protected HttpInputStream httpIn = null;
  public ConnectionManager connectionManager = null;

  public Httpd (ConnectionManager connectionManager, Socket client, HttpInputStream httpIn) {
    Log.debug(this, "Httpd<init> socketID " + client);

    this.connectionManager = connectionManager;
    this.client = client;
    this.httpIn = httpIn;
    httpIn.setSocket (client);
  }



  public void run () {
    Log.debug(this, "Httpd.run socketID " + client);

    boolean close = true;

    try {
      HttpProcessor processor = getProcessor (httpIn);
      Log.debug(this, httpIn.getPath ());
      OutputStream out = client.getOutputStream ();
      HttpOutputStream httpOut = new HttpOutputStream (out, httpIn);
      httpOut.setSocket (client);
      close = processor.processRequest (httpOut);
      httpOut.flush ();

    } 
    catch (IOException ex) {
        Log.debug(this, "Httpd.run " + ex);
    } 
    finally {
      if (close) {
        try {
          httpIn.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close httpIn" + ignored);
        }

        try {
          client.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close socket" + ignored);
        }
        httpIn = null;
        client = null;
      } else {
        Log.debug(this, "Httpd.run NOT trying to close " + client);
      } 
    }
  }

  protected HttpProcessor getProcessor (HttpInputStream httpIn) {
    try {
      httpIn.readRequest ();
      String path = httpIn.getPath ();
      Log.debug(this, "HTTPD: path is: " + path);
      Log.debug(this, "Httpd.getProcessor " + path + " " + client);
      Log.debug(this, httpIn.getPath ());

      if (path.startsWith (HTTP.TUNNEL_CONNECT)){
        return new TunnelConnect (this.connectionManager, httpIn);
      }
      else if (path.startsWith (HTTP.TUNNEL_DISCONNECT)){
        return new TunnelDisconnect (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.TUNNEL_IN)){
       return new TunnelIn (this.connectionManager, httpIn, path);
      }
//      else if (path.startsWith (HTTP.TUNNEL_OUT)){
//        return new TunnelOut (httpIn);
//      }
     else if (path.startsWith (HTTP.TUNNEL_PING)){
        return new TunnelPing (httpIn);
      }
      else if (path.startsWith (HTTP.FILE_IN)){
       return new FileIn (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.FILE_OUT)){
       return new FileOut (this.connectionManager, httpIn, path);
      }
     else {
       return new HttpFile (httpIn);
     }

    } catch (HttpException ex) {
      return ex;

    } catch (Exception ex) {
      Log.debug(this, "Httpd.run close " + ex);

      StringWriter trace = new StringWriter ();
      ex.printStackTrace (new PrintWriter (trace, true));
      return new HttpException (HTTP.STATUS_INTERNAL_ERROR,
                                "<PRE>" + ex + "\n\n" + trace + "</PRE>");
    }
  }

  public static void main (String[] args) throws IOException {
  }
}package com.arsenal.rtcomm.server.http;

/*
 * Java Network Programming, Second Edition
 * Merlin Hughes, Michael Shoffner, Derek Hamner
 * Manning Publications Company; ISBN 188477749X
 *
 * http://nitric.com/jnp/
 *
 * Copyright (c) 1997-1999 Merlin Hughes, Michael Shoffner, Derek Hamner;
 * all rights reserved; see license.txt for details.
 */

import java.io.*;
import java.net.*;
import java.util.*;

import com.arsenal.rtcomm.server.ConnectionManager;
import com.arsenal.log.Log;

public class Httpd implements Runnable {

  // socket from initial connection
  protected Socket client;
  protected HttpInputStream httpIn = null;
  public ConnectionManager connectionManager = null;

  public Httpd (ConnectionManager connectionManager, Socket client, HttpInputStream httpIn) {
    Log.debug(this, "Httpd<init> socketID " + client);

    this.connectionManager = connectionManager;
    this.client = client;
    this.httpIn = httpIn;
    httpIn.setSocket (client);
  }



  public void run () {
    Log.debug(this, "Httpd.run socketID " + client);

    boolean close = true;

    try {
      HttpProcessor processor = getProcessor (httpIn);
      Log.debug(this, httpIn.getPath ());
      OutputStream out = client.getOutputStream ();
      HttpOutputStream httpOut = new HttpOutputStream (out, httpIn);
      httpOut.setSocket (client);
      close = processor.processRequest (httpOut);
      httpOut.flush ();

    } 
    catch (IOException ex) {
        Log.debug(this, "Httpd.run " + ex);
    } 
    finally {
      if (close) {
        try {
          httpIn.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close httpIn" + ignored);
        }

        try {
          client.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close socket" + ignored);
        }
        httpIn = null;
        client = null;
      } else {
        Log.debug(this, "Httpd.run NOT trying to close " + client);
      } 
    }
  }

  protected HttpProcessor getProcessor (HttpInputStream httpIn) {
    try {
      httpIn.readRequest ();
      String path = httpIn.getPath ();
      Log.debug(this, "HTTPD: path is: " + path);
      Log.debug(this, "Httpd.getProcessor " + path + " " + client);
      Log.debug(this, httpIn.getPath ());

      if (path.startsWith (HTTP.TUNNEL_CONNECT)){
        return new TunnelConnect (this.connectionManager, httpIn);
      }
      else if (path.startsWith (HTTP.TUNNEL_DISCONNECT)){
        return new TunnelDisconnect (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.TUNNEL_IN)){
       return new TunnelIn (this.connectionManager, httpIn, path);
      }
//      else if (path.startsWith (HTTP.TUNNEL_OUT)){
//        return new TunnelOut (httpIn);
//      }
     else if (path.startsWith (HTTP.TUNNEL_PING)){
        return new TunnelPing (httpIn);
      }
      else if (path.startsWith (HTTP.FILE_IN)){
       return new FileIn (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.FILE_OUT)){
       return new FileOut (this.connectionManager, httpIn, path);
      }
     else {
       return new HttpFile (httpIn);
     }

    } catch (HttpException ex) {
      return ex;

    } catch (Exception ex) {
      Log.debug(this, "Httpd.run close " + ex);

      StringWriter trace = new StringWriter ();
      ex.printStackTrace (new PrintWriter (trace, true));
      return new HttpException (HTTP.STATUS_INTERNAL_ERROR,
                                "<PRE>" + ex + "\n\n" + trace + "</PRE>");
    }
  }

  public static void main (String[] args) throws IOException {
  }
}package com.arsenal.rtcomm.server.http;

/*
 * Java Network Programming, Second Edition
 * Merlin Hughes, Michael Shoffner, Derek Hamner
 * Manning Publications Company; ISBN 188477749X
 *
 * http://nitric.com/jnp/
 *
 * Copyright (c) 1997-1999 Merlin Hughes, Michael Shoffner, Derek Hamner;
 * all rights reserved; see license.txt for details.
 */

import java.io.*;
import java.net.*;
import java.util.*;

import com.arsenal.rtcomm.server.ConnectionManager;
import com.arsenal.log.Log;

public class Httpd implements Runnable {

  // socket from initial connection
  protected Socket client;
  protected HttpInputStream httpIn = null;
  public ConnectionManager connectionManager = null;

  public Httpd (ConnectionManager connectionManager, Socket client, HttpInputStream httpIn) {
    Log.debug(this, "Httpd<init> socketID " + client);

    this.connectionManager = connectionManager;
    this.client = client;
    this.httpIn = httpIn;
    httpIn.setSocket (client);
  }



  public void run () {
    Log.debug(this, "Httpd.run socketID " + client);

    boolean close = true;

    try {
      HttpProcessor processor = getProcessor (httpIn);
      Log.debug(this, httpIn.getPath ());
      OutputStream out = client.getOutputStream ();
      HttpOutputStream httpOut = new HttpOutputStream (out, httpIn);
      httpOut.setSocket (client);
      close = processor.processRequest (httpOut);
      httpOut.flush ();

    } 
    catch (IOException ex) {
        Log.debug(this, "Httpd.run " + ex);
    } 
    finally {
      if (close) {
        try {
          httpIn.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close httpIn" + ignored);
        }

        try {
          client.close ();
        } 
        catch (IOException ignored) {
          Log.debug(this, "Httpd.run close socket" + ignored);
        }
        httpIn = null;
        client = null;
      } else {
        Log.debug(this, "Httpd.run NOT trying to close " + client);
      } 
    }
  }

  protected HttpProcessor getProcessor (HttpInputStream httpIn) {
    try {
      httpIn.readRequest ();
      String path = httpIn.getPath ();
      Log.debug(this, "HTTPD: path is: " + path);
      Log.debug(this, "Httpd.getProcessor " + path + " " + client);
      Log.debug(this, httpIn.getPath ());

      if (path.startsWith (HTTP.TUNNEL_CONNECT)){
        return new TunnelConnect (this.connectionManager, httpIn);
      }
      else if (path.startsWith (HTTP.TUNNEL_DISCONNECT)){
        return new TunnelDisconnect (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.TUNNEL_IN)){
       return new TunnelIn (this.connectionManager, httpIn, path);
      }
//      else if (path.startsWith (HTTP.TUNNEL_OUT)){
//        return new TunnelOut (httpIn);
//      }
     else if (path.startsWith (HTTP.TUNNEL_PING)){
        return new TunnelPing (httpIn);
      }
      else if (path.startsWith (HTTP.FILE_IN)){
       return new FileIn (this.connectionManager, httpIn, path);
      }
      else if (path.startsWith (HTTP.FILE_OUT)){
       return new FileOut (this.connectionManager, httpIn, path);
      }
     else {
       return new HttpFile (httpIn);
     }

    } catch (HttpException ex) {
      return ex;

    } catch (Exception ex) {
      Log.debug(this, "Httpd.run close " + ex);

      StringWriter trace = new StringWriter ();
      ex.printStackTrace (new PrintWriter (trace, true));
      return new HttpException (HTTP.STATUS_INTERNAL_ERROR,
                                "<PRE>" + ex + "\n\n" + trace + "</PRE>");
    }
  }

  public static void main (String[] args) throws IOException {
  }
}