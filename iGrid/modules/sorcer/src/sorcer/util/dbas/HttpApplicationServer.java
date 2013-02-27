/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sorcer.util.dbas;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TooManyListenersException;
import java.util.Vector;

/**
 * Implementation of a basic HTTP server for Firewall
 * tunneling. The server supports both Client->Server and
 * Server->Client communication.
 */
public class HttpApplicationServer extends ApplicationDomain implements Runnable {
  // Accept socket
  private ServerSocket serverSocket;

  // Server Listener
  private ServerListener listener;
  
  // Thread accepting connections
  private Thread serverThread;
  
  // Handler threads
  private Pool pool;
  
  // Client sockets
  private Vector clients;
  
  // Default HTTP Response
  private String httpResponse; 

  
  /**
   * Initializes a new HttpApplicationServer instance
   * @param port Port to listen on
   * @param poolSize Number of handler threads
   * @throws IOException Thrown if the accept socket cannot be opened
   */
  public void initialize(int port, String password) {
    //number of handler threads
    String poolSize = props.getProperty("applicationServer.poolSize.http", "5");
    createSocket(port);
    httpResponse = "HTTP/1.0 200 HttpApplicationServer \nCache-Control: no-cache\nPragma: no-cache \r\n\r\n";
    try {
      pool = new Pool(Integer.parseInt(poolSize), HttpServerWorker.class);
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new InternalError(e.getMessage());
    }
    clients = new Vector();
    serverThread = new Thread(this);
    serverThread.start();
  }
  

  /**
   * Adds a new client 
   * @param s Socket
   * @return void
   */
  synchronized void addClient(Socket s) {
    clients.addElement(s);
  }
  

  /**
   * Adds a new ServerListener. Only one listener can
   * be added
   * @param l ServerListener
   * @return void
   * @throws TooManyListenersException Thrown if more then one listener is added
   */
  public void addHttpServerListener(ServerListener l) throws TooManyListenersException {
    if (listener == null) {
      listener = l;
    }
    else {
      throw new TooManyListenersException();
    }
  }
  

  /**
   * Removes a new ServerListener. 
   * be added
   * @param l ServerListener
   * @return void
   */
  public void removeHttpServerListener(ServerListener l) {
    listener = null;
  }
  

  /**
   * Notifies the listener when a message arrives
   * @param data Message data
   * @param out Stream to write results too
   * @return void
   */
  synchronized void notifyListener(InputStream data, OutputStream out) {
    if (listener != null) {
      listener.service(data, out);
    }
  }


  /**
   * Simple implementation that sends data to all clients
   * @param data Array of bytes containing data to send
   * @return void
   */
  synchronized public void send(byte data[]) {
    Enumeration elements = clients.elements();
    while (elements.hasMoreElements()) {
      Socket s = (Socket)elements.nextElement();
      try {
	DataOutputStream output = new 
	  DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
	int length;
	writeResponse(output);
	output.writeInt(data.length);
	output.write(data);
	output.flush();
	output.close();
      }
      catch (IOException e) {
	e.printStackTrace();
      }
      finally {
	try {
	  s.close();
	}
	catch(IOException e) {
	  e.printStackTrace();
	}
      }
    }
    clients.removeAllElements();
  }
  

  /**
   * Thread to accept connections
   * @return void
   */
  public void run() {
    while (true) {
      try {
	Socket s = serverSocket.accept();
	Hashtable data = new Hashtable();
	data.put("Socket", s);
	data.put("HttpServer", this);
	pool.performWork (data);
      }
      catch (Exception e) {
	e.printStackTrace();
      }
    }
  }
  

  /**
   * Convience method to write the HTTP Response header
   * @param out Stream to write the response too
   * @return void
   * @throws IOException Thrown if response can't be written
   */
  void writeResponse(DataOutputStream out) throws IOException {
    out.writeBytes (httpResponse);
  }


  // Start the server up, listening on an optionally specified port
  public static void main(String[] argv) throws Exception {
      init(argv);
      HttpApplicationServer.initQueryTable();

    int port = DEFAULT_PORT;
    String str = props.getProperty("applicationServer.port");
    if (str!=null)
      port = Integer.parseInt(str);

    HttpApplicationServer.createServer(port, password);
  }
  

  /*
   * Creates an accept server socket
   * @param port Port to listen on
   * @throws IOException Thrown if the accept socket cannot be opended
   **/
  protected void createSocket(int port) {
    try { 	
      serverSocket = new ServerSocket(port); 
    }
    catch (IOException e) {
      fail(e, "Exception creating server socket");
    }
  }


  public static HttpApplicationServer createServer(int port, String password) 
    throws Exception {
    //Determine whether to use extented ApplicationServer - a subclass
    String str = props.getProperty("applicationServer.isExtended", "false");
    String cn = appPrefix + "HttpApplicationServer";
    HttpApplicationServer as;
    if (str.equalsIgnoreCase("true")) {
      try {
	as = (HttpApplicationServer)Class.
	  forName(appPrefix + ".dbas." + cn).newInstance();
      }
      catch(Exception e) {
	System.err.println("Failed to create : " +
			   HttpApplicationServer.appName + 
			   "HttpApplicationServer");
	e.printStackTrace();
	return null;
      }
    }
    else 
      as = new HttpApplicationServer();
    as.initialize(port, password);
    
    if (as.isNotifierEnabled()) 
      as.createNotifier();

    as.addHttpServerListener(new HttpProtocolConnection());

    return as;
  } 


  // Exit with an error message, when an exception occurs.
  public static void fail(Exception e, String msg) {
    System.err.println(msg + ": " +  e);
    System.exit(1);
  } 
}
