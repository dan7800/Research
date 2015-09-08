/* draw sensor points and link the moving neighbors
 * bibble
 * May 22, 2002
 * 
 * Add multistreams support, colorful
 * Add grid
 * User Vector
 * Mark points
 * Clicking a point will show the coordinats of the point
 * May 23, 2002
 *
 * Points and lines will be relevantly changed with the change of grid interval.
 * May 24, 2002
 *
 * Add replay support
 * Add interface to lower level
 * Add dynamic menu
 * Use java.util.Timer
 * Use java.util.TimerTask
 * May 28, 2002
 *
 * Make the lines more solid
 * Add file management
 * Jun 13, 2002
 */

/*
 * Modify to SOCOM demo
 * 1. draw motes on the screen. Green means active; Blue means sleep.
 * 2. simulate moving target on the field.
 * 3. implement replay function.
 *
 * Jun Xie Apr 6, 2003
 * jxie@cs.virginia.edu
 *
 * Apr 7, 2003
 * Add Ting Yan's code to connect to a camera
 * Modify to if there is any moving on the left field, show camera; otherwise
 *     stop camera.
 * Need more work on repain.
 * bibble
 *
 * Apr 18, 2003
 * Integrate Ting's Java Camera control program
 * Use JMF to control camera, have to connect camera before install JMF
 * bibble
 * 
 * Apr 19, 2003, Ver 4.1
 * Read nodes and traces from file
 * Main frame repaint every 1 sec if a target is moving in the field
 * bibble
 *
 * May 1, 2003, Ver 4.3
 * Link next point to add point into a stream;
 * Add Holding delay after a moving stops;
 * bibble
 *
 * May 1, 2003, Ver 5.0
 * Integrate with Ting's code so that we can receive data from motes
 * Parser need more work
 * bibble
 *
 * May 4, 2003, Ver5.2
 * Integrate with Tian's message format Node message
 * Add spanning tree on the screen
 * bibble
 *
 * May 4, 2003, Ver5.5.5_toTian
 * Add blinking function
 * Coordinates are changing with grid size
 * Quench the bug in tracking
 * bibble
 *
 */

/*
 * Copy right -- Jun Xie, Ting Yan
 *
 * May 2003
 *
 */

//package net.tinyos.SPEED; 

import javax.swing.*;          //This is the final package name.
//import com.sun.java.swing.*; //Used by JDK 1.2 Beta 4 and all
                               //Swing releases before Swing 1.1 Beta 3.
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.util.*;
import java.util.Vector;
import java.util.TimerTask;
import java.io.*;
import java.net.*;

/*
import javax.media.*;
import javax.media.util.*;
import javax.media.format.*;
import javax.media.control.*;
import javax.media.protocol.*;
import javax.media.bean.playerbean.*;
import javax.media.rtp.*;
import javax.media.rtp.event.*;

import com.sun.media.util.JMFI18N;
import com.sun.media.rtp.RTPSessionMgr;
*/


class DisplayConstants {
	public static final int NodeNumber = 7;
  // SerialForward Server Information - port number and IP
  public static final int serverPort = 9000;
  public static final String serverIP = "127.0.0.1";

  // Packet Format
  public static final int packetSize = 36;
  public static final int macHeaderSize = 5;
  public static final int routingHeaderSize = 4;
  public static final int transportHeaderSize = 4;

  // Relevant Information Positions
  public static final int modePosition = macHeaderSize + routingHeaderSize + 2;
  public static final int payloadPosition = macHeaderSize + routingHeaderSize + transportHeaderSize;
  public static final int nodeIDPosition = payloadPosition;
  public static final int xPosition = payloadPosition + 2;
  public static final int yPosition = payloadPosition + 4;
  public static final int sentryPosition = payloadPosition + 6;
  public static final int leaderPosition = payloadPosition + 8;
  public static final int parentPosition = payloadPosition + 8;
  public static final int voltagePosition = payloadPosition + 10;
  public static final int numSentriesPosition = payloadPosition + 12;
  public static final int numNeighborsPosition = payloadPosition + 13;
  public static final int statePosition = payloadPosition + 14;

  // State Enum
  public static final byte SENTRY = 0;
  public static final byte NON_SENTRY_AWAKE = 1;
  public static final byte NON_SENTRY_SLEEP = 2;

}

// To connect to motes
class ClientThread extends Observable implements Runnable {
  private byte[] packet;
  private Thread thread;
  private DrawLinePanel spane;
  private DisplayConstants consts;
  private Socket sock;
  private InputStream packetStream = null;
  //private boolean bShutdown = true;
  private int nPackets = 0;

  ClientThread(DrawLinePanel pane) {
    super();
    thread = new Thread(this);
    consts = new DisplayConstants();
    packet = new byte[consts.packetSize];
    this.spane = pane;
    thread.start();
  }

  public Thread getThread() {
    return thread;
  }

  public void run() {
    //bShutdown = false;
    readServer();
  }

  private void readServer() {
    int nBytesRead = 0;
    int nBytesReturned = 0;

    try {
      sock = new Socket(consts.serverIP, consts.serverPort);
      packetStream = sock.getInputStream();
    }
    catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

	int i, j;
	//spane.receivedNodeMsg(0, 100, 100, 0, 0, 0);
	for (i = 0; i < 5 ; i++) {
		for (j = 0; j < 2; j++) {
			if (!(i == 0 && j == 1)){
			spane.receivedNodeMsg(i + j * 5, 100 + 200 /2 * i , 100 + 200 * j, 0, 0, 0);
			}
		}
	}
/*	for (i = 0; i < consts.NodeNumber; i++) {
		spane.receivedNodeMsg(i, 80 + 40 * i, 200, 0, 0, 0);
	}
*/
    try {
      nBytesReturned = packetStream.read(packet, nBytesRead,
                                         consts.packetSize - nBytesRead);
    }
    catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    while (nBytesReturned != -1) {
      nBytesRead += nBytesReturned;
      // System.out.println(nBytesRead);
      if (nBytesRead == consts.packetSize) {
        nPackets++;
        int  k= 0;
        for (k = 0; k < consts.packetSize; k++) {
          System.out.print(packet[k] >= 0 ? packet[k] : (int) packet[k] + 256);
          System.out.print(" ");
        }
        System.out.println("\n");


        nBytesRead = 0;
        int mode=0, nodeID=0, x=0, y=0, leader=0, parentID=0, voltage=0, state=0;

	//mode means port in Tian's doc -- bibble
        // mode = TwoBytes(packet, consts.modePosition);

	//nodeID is groupiD as well
        // nodeID = TwoBytes(packet, consts.nodeIDPosition);
        // x = TwoBytes(packet, consts.xPosition);
        // y = TwoBytes(packet, consts.yPosition);
        // leader = TwoBytes(packet, consts.leaderPosition);
        // voltage = TwoBytes(packet, consts.voltagePosition);
        // state = OneByte(packet, consts.statePosition);
        // parentID = TwoBytes(packet, consts.parentPosition);

	/*
        if (mode == 1) {
          spane.receivedTrackMsg(nodeID, x, y, leader);
        }
        if (mode == 2) {
          spane.receivedNodeMsg(nodeID, x, y, sentry, parentID, voltage, numSentries, numNeighbors, state);
        }
	*/
	nodeID = 1; //TwoBytes(packet, 5);
	x = TwoBytes(packet, 11);
	y = TwoBytes(packet, 13);
	leader = TwoBytes(packet, 17);

	System.out.println("receive msg, x:"+x+"y:"+y);
	x = 100 + 100 * x / 256;
	y = 100 + 200 * y / 256;
	System.out.println("receive msg,after coordinates transfer x:"+x+"y:"+y);
        System.out.println("group id:"+nodeID+"\n");
	spane.receivedTrackMsg(1, x, y, leader);
	nodeID = TwoBytes(packet, 27);
	spane.blinkNode(nodeID);
	System.out.println("Blink !!! "+nodeID);
        try {
          nBytesReturned = packetStream.read(packet, nBytesRead,
                                             consts.packetSize - nBytesRead);
        }
        catch (IOException e) {
          e.printStackTrace();
          System.exit(1);
        }

      }
    }
  }

  private int OneByte(byte[] packet, int offset) {
    int p = (int) packet[offset];
    return (p >= 0 ? p : p + 256);
  }

  private int TwoBytes(byte[] packet, int offset) {
    int p0 = (int) packet[offset];
    int p1 = (int) packet[offset + 1];
    p0 = (p0 >= 0) ? p0 : (p0 + 256);
    p1 = (p1 >= 0) ? p1 : (p1 + 256);
    return p0 + p1 * 256;
  }
}

// to control a camera
/*
class MyListener implements ControllerListener {
  boolean realized = false;
  public synchronized void controllerUpdate ( ControllerEvent event ) {
    if (event instanceof RealizeCompleteEvent) {
      realized = true;
    }
  }
}
*/


//this class draw a moving object
class MovingObject extends Frame implements Runnable {
	java.util.Timer movingTimer = null;
	java.util.Timer holdingTimer = null;
	static DrawLinePanel spane;
	private static final long movingInterval = 50; //50 millis
	private static final long holdingInterval = 50; //50 millis
	int repaintValue = 0; //every refresh increase 1
	final int repainThreshold = 100; //when repaintValue reach this, repaint

	Point startLoc; //start position
	Point endLoc; //end position after moving
	Point curLoc;
	Point preLoc;
	Color color;

	boolean isHolding;
	boolean isMoving;

	double velocityX = 5.; //everytime add 5 on x_axis
	double velocityY = 5.; //everytime add 5 on y_axis at most
	
	//draw a rect as a moving object
	int width;
	int height;

	//background color
	Color backColor;

	Graphics graphics;

	//to control moving event
	Vector pointsH;
	int numPoints;
	int curPoint;

	class movingTask extends TimerTask {
		public void run() {
		    //System.out.println("Time's up!");
		    refresh();
		}
	}

	// holding a target for a while after it reachs the end point
	class holdingTask extends TimerTask {
		public void run() {
			//Draw the object at current location
			drawObjectAtCurPos();
		}
	}

	public void run() {
		super.repaint();
	}

	//construct function
	MovingObject() {
		startLoc = new Point();
		endLoc = new Point();
		curLoc = new Point();
		preLoc = new Point();

		curPoint = 0;
		//backColor = new Color();
		//color = new Color();
		isHolding = false;
		isMoving = false;
	}

	//construct function with background color
	MovingObject(Color back) {
		startLoc = new Point();
		endLoc = new Point();
		curLoc = new Point();
		preLoc = new Point();

		curPoint = 0;
		//color = new Color();
		isHolding = false;
		isMoving = false;

		width = 15; //default
		height = 15; //default
		backColor = back;
	}

	//construct function with width & height
	MovingObject(int w, int h, Color back) {
		startLoc = new Point();
		endLoc = new Point();
		curLoc = new Point();
		preLoc = new Point();

		curPoint = 0;
		//backColor = new Color();
		isHolding = false;
		isMoving = false;

		width = w; //default
		height = h; //default
		backColor = back;
		//color = new Color();
	}

	//whether on left
	public boolean onLeft(int middleX) {
		//if (curPoint == numPoints) 
		if (curPoint >= pointsH.size() - 3)
		//if (curLoc.x == endLoc.x && curLoc.y == endLoc.y)
			return false;

		if (curLoc.x <= middleX)
			return true;
		else
			return false;
	}

	//set the Panel which contain the moving object
	public void setPanel(DrawLinePanel pane) {
		spane = pane;
	}

	public void setStartAndEndLocation(Point p1, Point p2) {
		startLoc = new Point(p1);
		endLoc = new Point(p2);
	}

	//set the trace of the stream so that the moving obj can find out 
	//  the coming position
	public void setStream(Vector stream) {
		pointsH = stream;
	}

	public void setCurrentLocation(Point point) {
		curLoc = point;
	}

	public void setColor(Color c) {
		color = c;
	}

	public void setBackgroundColor(Color back) {
		backColor = back;
	}

	//reset the moving target, so that it can be replayed later
	public void reset() {
		curPoint = 0;
		if (movingTimer != null)
			movingTimer.cancel();
		if (holdingTimer != null)
			holdingTimer.cancel();
		isMoving = false;
		isHolding = false;
	}
	
	//store Graphics instance
	private void setGraphics(Graphics g) {
		graphics = g;
	}

	//use this function to move an object
	public void move(Graphics g, DrawLinePanel pane) {

		numPoints = pointsH.size() - 3;
		if (numPoints < 2)
			return; //no position for moving

		if (isMoving) {
			return;
		}

		if (isHolding) {
			isHolding = false;
			repaintValue = 0;
			holdingTimer.cancel();
		}

		isMoving = true;

		// if first move, set current position 1
		// otherwise just leave it unchange
		if (curPoint == 0) 
			curPoint = 1; //current start point

		setStartAndEndLocation((Point)(pointsH.elementAt(curPoint+2)), (Point)(pointsH.elementAt(curPoint+3)));
		curLoc = startLoc;
		preLoc = curLoc;
		//repainThreshold = (int)(width/velocityX);
		repaintValue = 0;
		setGraphics(g); //store Graphics instance
		setPanel(pane); //store the Panel
		drawObject(g, curLoc);
		
		System.out.println("move...");

		getNextPoint(curLoc);
		//drawObject(g, curLoc, color);

		if (movingTimer != null)
			movingTimer.cancel();

		movingTimer = new java.util.Timer();
		movingTimer.schedule(new movingTask(), 0, movingInterval);

	}

	private void refresh() {
		
		//System.out.println("refresh");
		drawObject(graphics, preLoc, backColor); //erase previous one
		preLoc = curLoc;
		getNextPoint(curLoc);
		drawObject(graphics, curLoc, color);
		//spane.cameraControl(); //call camera control

		spane.isMoving = true; //control repaint
		isMoving = true;

		if (curLoc.x == endLoc.x && curLoc.y == endLoc.y) {
			//moving is divided into many phrases
			//one phrase ends, start next phrase
			//till to the last point
			curPoint++;
			//System.out.println("size of stream "+pointsH.size());
			//System.out.println("current point "+curPoint);

			if (curPoint >= pointsH.size() - 3) {
				isHolding = true;
				movingTimer.cancel();
				isMoving = false;
				spane.isMoving = false;
				if (holdingTimer != null)
					holdingTimer.cancel();
				holdingTimer = new java.util.Timer();
				holdingTimer.schedule(new holdingTask(), 0, holdingInterval);
				spane.repaint();

			}
			else {
				System.out.println("size of stream "+pointsH.size());
				System.out.println("current point"+curPoint);
				setStartAndEndLocation((Point)(pointsH.elementAt(curPoint+2)), (Point)(pointsH.elementAt(curPoint+3)));
			}
		}

	}

	//for holding task
	public void drawObjectAtCurPos() {
		repaintValue++;
		if (repaintValue < repainThreshold) {
			//spane.repaint();
			drawObject(graphics, curLoc, color);
		}
		else {
			repaintValue = 0;
			holdingTimer.cancel();
			isHolding = false;
			//spane.cameraControl(); //call camera control
			spane.repaint();
		}
	}
	
	//get the next point the moving object will move to
	private void getNextPoint(Point next) {
		//double velocityX = 5.; //everytime add 5 on x_axis
		//double velocityY = 5.; //everytime add 5 on y_axis at most
		double slope;
		Point p1 = new Point(curLoc);
		Point p2 = new Point(endLoc);

		int deltx = Math.abs(p2.x - p1.x);
		int delty = Math.abs(p2.y - p1.y);

		if (deltx >= delty && deltx > velocityX) {
			slope = ((double)(delty))/((double)(deltx));
			if (p2.y > p1.y)
				p1.y = p1.y + (int)(slope*velocityX);
			else if (p2.y < p1.y)
				p1.y = p1.y - (int)(slope*velocityX);
			else
				;
			//p1.y = (int)(((double)(p2.y - p1.y)/((1.)*(p2.x - p1.x)))*velocityX) + p1.y;
			if (p2.x >= p1.x)
				p1.x = p1.x + (int)velocityX;
			else
				p1.x = p1.x - (int)velocityX;
		}
		else if (deltx < delty && delty > velocityY) {
			slope = ((double)(deltx))/((double)(delty));
			if (p2.x > p1.x)
				p1.x = p1.x + (int)(slope*velocityY);
			else if (p2.x < p1.x)
				p1.x = p1.x - (int)(slope*velocityY);
			else
				;
			//p1.x = (int)(((double)(p2.x - p1.x)/((1.)*(p2.y - p1.y)))*velocityY) + p1.x;
			if (p2.y >= p1.y)
				p1.y = p1.y + (int)velocityY;
			else
				p1.y = p1.y - (int)velocityY;
		}
		else {
			//the start and end points are too close
			p1.x = p2.x;
			p1.y = p2.y;
		}

		next.x = p1.x;
		next.y = p1.y;

	}

	public void erase(Graphics g) {
		drawObject(g, preLoc, backColor);
	}

	//draw moving target at point
	private void drawObject(Graphics g, Point point) {
		g.fillRect((int)(spane.co*point.x) - 1, (int)(spane.co*point.y) - 1, width, height);
	}

	//draw moving target at point with color c
	private void drawObject(Graphics g, Point point, Color c) {
		Color oldColor;
		oldColor = g.getColor(); //save old color

		g.setColor(c);
		g.fillRect((int)(spane.co*point.x) - 1, (int)(spane.co*point.y) - 1, width, height);

		g.setColor(oldColor); //restore old color
	}
		
      
}
	

//this class presents nodes
class Node {
	int nodeId; //the node id in sensor network
	Point loc;
	int mySentry;
	int parent;
	int power;
	int sentries;
	int neighbors;
	int status; //2 -- leader; 1 -- active; 0 -- sleep or dead

	// generate a new node
	public Node() {
		loc = new Point();
		parent = 0;
	}

	// add a point without status
	public Node(int id, Point p) {
		nodeId = id;
		loc = p;
		status = 1; //default the node is active
		parent = 0;
	}
	
	// add a point with status
	public Node(int id, Point p, int stas) {
		nodeId = id;
		loc = p;
		status = stas;
		parent = 0;
	}

	public Node(int nodeID, Point p, int sentry, int parentID, int voltage, int numSentries, int numNeighbors, int state) {
		nodeId = nodeID;
		loc = p;
		status = state;
		mySentry = sentry;
		parent = parentID;
		power = voltage;
		sentries = numSentries;
		neighbors = numNeighbors;
		
	}

	public void updateStatus(int stas) {
		status = stas;
	}

	public void update(int sentry, int parentID, int voltage, int numSentries, int numNeighbors, int state) {
		status = state;
		mySentry = sentry;
		parent = parentID;
		power = voltage;
		sentries = numSentries;
		neighbors = numNeighbors;
		
	}

	public int getNodeID() {
		return nodeId;
	}

	public int getParent() {
		return parent;
	}
}

// When a node gets a new msg, blink the node
class Blink {
	DrawLinePanel spane;
	Node node;
	java.util.Timer timer = new java.util.Timer();
	int count = 0;
	int maxCount = 10;
	int blinkInterval = 100; //100 miliseconds
	Graphics g;
	Color curColor;
	Color backgroundColor;

	public class BlinkTask extends TimerTask {
		public void run() {
			blink();
		}
	}

	public Blink(DrawLinePanel p, Node nd, Graphics g1, Color cur, Color back) {
		spane = p;
		node = nd;
		g = g1;
		curColor = cur;
		backgroundColor = back;

		timer.schedule(new BlinkTask(), 0, blinkInterval);
	}

	public void blink() {
		Point p3 = new Point();
		count++;
		if (count > maxCount) {
			timer.cancel();
			//destroy();
			spane.repaint();
			return;
		}
		if ((count/2)*2 == count) {
			g.setColor(curColor);
		}
		else
			g.setColor(backgroundColor);

		p3.x = (int)(spane.co*node.loc.x);
		p3.y = (int)(spane.co*node.loc.y);
		spane.paintPoint(g, p3);
	}
}    


//this class draws the points and lines
class DrawLinePanel extends JPanel implements Runnable
{  
   java.util.Timer timer;
   java.util.Timer repaintTimer;

   Vector nodesHolder = new Vector(); //Hold all the nodes in sensor network
   Vector movingStreamsHolder = new Vector(); //Hold moving streams

   Vector streamsHolder = new Vector(); //Hold streams

   public int numStreams = 0; //the number of streams
   public int numMovingStreams = 0; //the number of moving streams
   public int defaultInterval = 100; //the initial interval
   private int interval = defaultInterval; //the interval for grid
   public double co = 1.; //coordinates trans
   private double sleepCriticalValue = 0.5; // less than 0.5 sleep, >.5 active
   private static final int pointWidth = 6;
   private static final int pointHeight = 10;
   private static final long replayInterval = 1000; //1000 millis
   private static final long repaintInterval = 1000; //1000 millis
   private boolean replaying = false;
   private int lines = 0;

   //repaint control
   //when a target is moving, set it true
   //otherwise, set it false
   //RepaintTask repaint screen every 1 sec if isMoving is true
   public boolean isMoving = false;

   //for camera control
   //use socket stream to connect to camera window
   //private MediaPlayer mediaPlayer1 = null;
   //private MediaPlayer mediaPlayer2 = null;
   //public Component cameraComp1 = null;	
   //public Component cameraComp2 = null;	
  //private DataSource dataSource1 = null;
  //private DataSource dataSource2 = null;
  //private Vector vectorDevices = null;
  //private Vector vectorVideoDevices = null;
  //private MyListener listener1 = null;
  //private MyListener listener2 = null;
  private boolean on1;//, on2;

   
   //mouse click coordinates
   private int xpos = 0;
   private int ypos = 0;
   
   //lines' colors
   Color colors[] = new Color [7];
   private int numColors = 7;
      
   public DrawLinePanel() {
   	
   	colors[0] = new Color(0, 0, 0);		//black
   	colors[1] = new Color(0, 0, 255);	//tlines
   	colors[2] = new Color(0, 255, 0);	//plines
	colors[3] = new Color(255, 0, 0);	//red
	colors[4] = new Color(255, 0, 255);	//origin
   	colors[5] = new Color(0, 255, 255);	//pback
   	colors[6] = new Color(192, 100, 10);	//tback

	//camera initilize
	//cameraInit();
	
   	repaintTimer = new java.util.Timer();
   	repaintTimer.schedule(new RepaintTask(), 0, repaintInterval);
   }
   
    class ReplayTask extends TimerTask {
        public void run() {
            //System.out.println("Time's up!");
            Graphics g = getGraphics();
            replay(g);
        }
    }
    
    class RepaintTask extends TimerTask {
        public void run() {
            //System.out.println("Time's up!");
            //Graphics g = getGraphics();
	    if (isMoving)
		repaint();
        }
    }

    //save all the streams to a file pointed by path
    //File format: the first int: number of streams
    //followed by all the streams
    //At the beginning of a stream, write the size of it
    //public void save(String path) 
    public void save(File f) {
    	Point point = new Point();
    	Vector points = new Vector();
    	System.out.println("save...");
    	System.out.println(f.getName());
    	try {
    		FileOutputStream fs = new FileOutputStream(f);
    		DataOutputStream out = new DataOutputStream(fs);
    		out.writeInt( streamsHolder.size() ); //write the number of streams
    		
    		for(int i=0; i<streamsHolder.size(); i++) {
    			points = ((Vector)(streamsHolder.elementAt(i)));
    			out.writeInt( points.size() ); //the size of this stream
    			out.writeInt( ((Integer)(points.elementAt(0))).intValue() );
    			out.writeBoolean( ((Boolean)(points.elementAt(1))).booleanValue() );
    			for(int j=2; j<points.size(); j++) {
    				point = (Point)(points.elementAt(j));
    				out.writeInt(point.x);
    				out.writeInt(point.y);
    			}
    		}
    		
    		out.close();
    	} catch(Exception e) {
    		System.out.println("write file error!");
    	}
    			
    }

    //Open a file
    //File format: the first int: number of streams
    //followed by all the streams
    //return value: the number of streams
    //			if error, return 0
    public int open(File f) {
    	Point point = new Point();
    	int size;

    	clear();
    	try {
    		FileInputStream fs = new FileInputStream(f);
    		DataInputStream in = new DataInputStream(fs);
    		//out.writeInt( streamsHolder.size() ); //write the number of streams
    		numStreams = in.readInt();
    		
    		for(int i=0; i<numStreams; i++) {
    			Vector points = new Vector();
    			size = in.readInt();
    			//points = ((Vector)(streamsHolder.elementAt(streamID)));
    			points.addElement(new Integer( in.readInt() ));
    			points.addElement(new Boolean( in.readBoolean() ));
    			for(int j=2; j<size; j++) {
    				point.x = in.readInt();
    				point.y = in.readInt();
    				points.addElement(new Point(point));
    			}
    			streamsHolder.add(points);
    		}
    		
    		in.close();
    		return numStreams;
    	} catch(Exception e) {
    		System.out.println("read file error!");
    		//numStreams = 0;
    		//streamsHolder.clear();
    		clear();
    		return 0;
    	}
    			
    }
   
    //get points from a file
    //Open a file
    //File format: the first int: number of nodes
    //followed by all the coordinates of the nodes
    //followed by a stream
    //       number of coordinates in the stream
    //       coordinates
    //return value: the number of nodes
    //			if error, return 0
    public int getPointsFromFile(File f) {
    	Point point = new Point();
    	int size;
	int numNodes;
	String str, str0, str1, str2, str3;

    	clear();
	System.out.println("open...");
    	try {
    		//FileInputStream fs = new FileInputStream(f);
    		FileReader fs = new FileReader(f);
    		//FileReader fs = new FileReader("./trace.tra");
    		//DataInputStream in = new DataInputStream(fs);
		BufferedReader in = new BufferedReader(fs);
    		//out.writeInt( streamsHolder.size() ); //write the number of streams
		System.out.println("read...");

		if ((str = in.readLine()) == null) {
			in.close();
			return 0;
		}
		//str0 = str.substring(0, str.indexOf(" "));
    		numNodes = Integer.parseInt(str);
		System.out.println(numNodes);
    		
    		for(int i=0; i<numNodes; i++) {
			if ((str = in.readLine()) == null) break;
			str0 = str.substring(0, str.indexOf(" "));
			str1 = str.substring(str.indexOf(" ")+1, str.length());
			str2 = str1.substring(str1.indexOf(" ") +1, str1.length());
			str3 = str2.substring(str2.indexOf(" ") +1, str2.length());
			str1 = str1.substring(0, str1.indexOf(" "));
			str2 = str2.substring(0, str2.indexOf(" "));
			int nodeId = Integer.parseInt(str0);
			int stats = Integer.parseInt(str1);
			int x = Integer.parseInt(str2);
			int y = Integer.parseInt(str3);
			System.out.println(nodeId);
			System.out.println(stats);
			System.out.println(x);
			System.out.println(y);
			//insertAbsNode(in.readInt(), in.readInt(), in.readInt(), in.readInt());
			insertAbsNode(nodeId, stats, x, y);
			//nodeId, stats, x, y

			/*
    			for(int j=2; j<size; j++) {
    				point.x = in.readInt();
    				point.y = in.readInt();
    				points.addElement(new Point(point));
    			}
    			streamsHolder.add(points);
			*/
    		}

		if ((str = in.readLine()) == null) {
			in.close();
			return 0;
		}
		//str0 = str.substring(0, str.indexOf(" "));
    		numStreams = Integer.parseInt(str);
		System.out.println(numStreams);
    		
    		for(int i=0; i<numStreams; i++) {
			int groupId;
    			Vector points = new Vector();

			str = in.readLine();
			str0 = str.substring(0, str.indexOf(" "));
			groupId = Integer.parseInt(str0);
			System.out.println(groupId);
			str1 = str.substring(str.indexOf(" ")+1, str.length());
			size = Integer.parseInt(str1);
			System.out.println(size);
    			//points = ((Vector)(streamsHolder.elementAt(streamID)));
    			//points.addElement(new Integer( in.readInt() ));
    			//points.addElement(new Boolean( in.readBoolean() ));
			addMovingStream(groupId);
    			for(int j=0; j<size; j++) {
				if ((str = in.readLine()) == null) break;
				str0 = str.substring(0, str.indexOf(" "));
				str1 = str.substring(str.indexOf(" ")+1, str.length());
    				point.x = Integer.parseInt(str0);
    				point.y = Integer.parseInt(str1);
				System.out.println(point.x);
				System.out.println(point.y);
				insertAbsPoint(groupId, point.x, point.y);
    			}
    			//streamsHolder.add(points);
    		}
    		
    		in.close();
    		return numStreams;
    	} catch(Exception e) {
		System.out.println(e.getMessage());
    		System.out.println("read file error!");
		e.printStackTrace();
    		//numStreams = 0;
    		//streamsHolder.clear();
    		clear();
    		return 0;
    	}
    			
    }
   public void doReplay() {
   	replaying = true;
   	lines = 0;
   	timer = new java.util.Timer();

	//reset the moving targets so that we can replay
	resetAllMobiles(); 

   	timer.schedule(new ReplayTask(), 0, replayInterval);
   	//while (replaying) {
   	//	repaint();
   	//	System.out.println("doReplay...");
   	//	try {
        //		Thread.sleep(replayInterval); //1000ms
        //	} catch (InterruptedException e) {};
        //}

   }

   /* use this function to insert a new node into the node list
    */
   public void insertNode(int nodeID, double x, double y) {
   	Vector points = new Vector();
   	int streamID;
   	//Point point = new Point((int)(x*defaultInterval), (int)(y*defaultInterval));
   	Point point = new Point((int)(x*interval), (int)(y*interval));
	Node node = new Node(nodeID, point);
   	
   	System.out.println("Insert node to the Panel ("+nodeID+','+(int)(x*defaultInterval)+','+(int)(x*defaultInterval)+")\n");

	nodesHolder.addElement(node);

	repaint();
   }
   	
   /* use this function to insert a new node with status into the node list
    */
   public void insertNode(int nodeID, int stas, double x, double y) {
   	Vector points = new Vector();
   	int streamID;
   	//Point point = new Point((int)(x*defaultInterval), (int)(y*defaultInterval));
   	Point point = new Point((int)(x*interval), (int)(y*interval));
	Node node = new Node(nodeID, point, stas);
   	
   	System.out.println("Insert node to the Panel ("+nodeID+','+(int)(x*defaultInterval)+','+(int)(x*defaultInterval)+")\n");

	nodesHolder.addElement(node);

	repaint();
   }
   	
   /* use this function to insert a new node with status into the node list
    * with absolute coordinates
    */
   public void insertAbsNode(int nodeID, int stas, int x, int y) {
   	Vector points = new Vector();
   	int streamID;
   	//Point point = new Point((int)(x*defaultInterval), (int)(y*defaultInterval));
   	Point point = new Point((int)(x), (int)(y));
	Node node = new Node(nodeID, point, stas);
   	
   	System.out.println("Insert node to the Panel ("+nodeID+','+(int)(x*defaultInterval)+','+(int)(x*defaultInterval)+")\n");

	nodesHolder.addElement(node);

	repaint();
   }
   	
   /* use this function to insert a new node with status into the node list
    * with absolute coordinates
    */
   public void insertAbsNode(int nodeID, int x, int y, int sentry, int parentID, int voltage, int numSentries, int numNeighbors, int state) {
   	Vector points = new Vector();
   	int streamID;
   	//Point point = new Point((int)(x*defaultInterval), (int)(y*defaultInterval));
   	Point point = new Point((int)(x), (int)(y));
	//Node node = new Node(nodeID, point, stas);
	Node node = new Node(nodeID, point, sentry, parentID, voltage, numSentries, numNeighbors, state);
   	
   	System.out.println("Insert node to the Panel ("+nodeID+','+(int)(x*defaultInterval)+','+(int)(x*defaultInterval)+")\n");

	nodesHolder.addElement(node);

	repaint();
   }
   	
   // Interface for lower lever -- add a node in network
   // check whether the node exist or not
   // if exist, update
   // otherwise, insert
   public void addNode(int nodeID, int stas, int x, int y) {
	Node nd = null;
	nd = lookupNode(nodeID);
	if (nd != null) {
		nd.updateStatus(stas);
	}
	else
		insertAbsNode(nodeID, stas, x, y);
		
   }

   // Interface for lower lever -- add a node in network
   // check whether the node exist or not
   // if exist, update
   // otherwise, insert
   public void addNode(int nodeID, int x, int y, int sentry, int parentID, int voltage, int numSentries, int numNeighbors, int state) {
	Node nd = null;
	nd = lookupNode(nodeID);
	if (nd != null) {
		//nd.updateStatus(stas);
		nd.update(sentry, parentID, voltage, numSentries, numNeighbors, state);
	}
	else
		insertAbsNode(nodeID, x, y, sentry, parentID, voltage, numSentries, numNeighbors, state);
		//insertAbsNode(nodeID, stas, x, y);
		
   }

   public Node lookupNode(int nodeID) {
	Node nd;
	for (int i=0; i<nodesHolder.size(); i++) {
		nd = (Node)(nodesHolder.elementAt(i));
		if (nd.getNodeID() == nodeID)
			return nd;
	}
	return null;
   }
   
   /* use this function to add points into a specific stream
    * streamID is the same with groupID. it begins from 0.
    */
   public void insertPoint(int groupID, double x, double y) {
   	Vector points = new Vector();
   	int streamID;
   	//Point point = new Point((int)(x*defaultInterval), (int)(y*defaultInterval));
   	Point point = new Point((int)(x*interval), (int)(y*interval));
   	
   	System.out.println("InsertPoint to the Panel ("+groupID+','+(int)(x*defaultInterval)+','+(int)(x*defaultInterval)+")\n");
   	
   	for (streamID=0; streamID < movingStreamsHolder.size(); streamID++) {
   		points = ((Vector)(movingStreamsHolder.elementAt(streamID)));
   		if (groupID == ((Integer)(points.elementAt(0))).intValue()) {
   		 System.out.println("InsertPoint to the Panel Success\n");
   			points.addElement(point);

			//to repaint more work needed here!!!

   			repaint();
   			return;
   		}   	
   	}
   	System.out.println("InsertPoint to the Panel failed\n");
   }
   
   /* use this function to add points into a specific stream
    * streamID is the same with groupID. it begins from 0.
    */
   public void insertAbsPoint(int groupID, int x, int y) {
   	Vector points = new Vector();
   	int streamID;
   	//Point point = new Point((int)(x*defaultInterval), (int)(y*defaultInterval));
   	Point point = new Point((int)(x), (int)(y));
   	
   	System.out.println("InsertAbsPoint to the Panel ("+groupID+','+x+','+y+")\n");
   	
   	for (streamID=0; streamID < movingStreamsHolder.size(); streamID++) {
   		points = ((Vector)(movingStreamsHolder.elementAt(streamID)));
   		if (groupID == ((Integer)(points.elementAt(0))).intValue()) {
   		 System.out.println("InsertPoint to the Panel Success\n");
   			points.addElement(point);

			//to repaint more work needed here!!!

   			//repaint();
   			return;
   		}   	
   	}
   	System.out.println("InsertPoint to the Panel failed\n");
   }
   
   //if doesnt exist, return -1
   public int lookupGroupID(int groupID) {
   	Vector points = new Vector();
   	int streamID;
   	
   	for (streamID=0; streamID < movingStreamsHolder.size(); streamID++) {
   		points = ((Vector)(movingStreamsHolder.elementAt(streamID)));
   		if (groupID == ((Integer)(points.elementAt(0))).intValue()) {
   			return streamID;
   		}
   	}
   	return -1;
   }
   
   //add a stream without its group id
   public void addStream() {
   	Vector pointsH = new Vector(100);
   	boolean show = true;
   	int groupID = -1;
   	pointsH.addElement(new Integer(groupID));	//the first element in a stream is groupID
   	pointsH.addElement(new Boolean(show));	//the second element is a var to decide 
   				//whether this stream is visiable or not
   	streamsHolder.add(numStreams, pointsH);
   	numStreams++;
   }

   //add a stream with its group id
   public void addStream(int groupID) {
   	Vector pointsH = new Vector(100);
   	boolean show = true;
   	pointsH.add(new Integer(groupID));	//the first element in a stream is groupID
   	pointsH.add(new Boolean(show));	//the second element is a var to decide 
   				//whether this stream is visiable or not
   	streamsHolder.add(numStreams, pointsH);
   	numStreams++;
   }
   
   //add a moving stream without its group id
   public void addMovingStream() {
   	Vector pointsH = new Vector(100);
   	boolean show = true;
   	int groupID = -1;
   	pointsH.addElement(new Integer(groupID));	//the first element in a stream is groupID
   	pointsH.addElement(new Boolean(show));	//the second element is a var to decide 
   				//whether this stream is visiable or not
      	Color c = ((Component)this).getBackground(); //get the color of backgroung
	MovingObject mObj = new MovingObject(c);
	mObj.setPanel(this);
	pointsH.add(mObj); //the third element is the moving object

   	movingStreamsHolder.add(numMovingStreams, pointsH);
   	numMovingStreams++;
	//When push addStream button, moving here
	moveSimulator(pointsH);

   }

   //add a moving stream with its group id
   public void addMovingStream(int groupID) {
   	Vector pointsH = new Vector(100);
   	boolean show = true;
   	pointsH.add(new Integer(groupID));	//the first element in a stream is groupID
   	pointsH.add(new Boolean(show));	//the second element is a var to decide 
   				//whether this stream is visiable or not
   	//Graphics g = getGraphics();
      	Color c = ((Component)this).getBackground(); //get the color of backgroung
	MovingObject mObj = new MovingObject(c);
	mObj.setPanel(this);
	pointsH.add(mObj); //the third element is the moving object

   	movingStreamsHolder.add(numMovingStreams, pointsH);
   	numMovingStreams++;
	//moveSimulator(pointsH);

   }

   // Interface for lower level
   public void addMovingPoint(int groupID, int x, int y) {
	if (lookupGroupID(groupID) == -1) {
		addMovingStream(groupID);
	}
	insertAbsPoint(groupID, x, y);
	doMove(groupID);
   }

   //Move a target according to the group id
   public void doMove(int groupID) {
   	Vector points = new Vector();

	for (int i=0; i<movingStreamsHolder.size(); i++) {
   		points = ((Vector)(movingStreamsHolder.elementAt(i)));
   		if (groupID == ((Integer)(points.elementAt(0))).intValue()) {
			doMove(points, i);
   			return;
   		}
		//pointsH = (
		//doMove((Vector)movingStreamsHolder.elementAt(i));
   		//points = ((Vector)(streamsHolder.elementAt(streamID)));
	}
   }

   //Move a target according to all the points
   public void doMove(Vector pointsH, int i) {
	//get the color
	int j = movingStreamsHolder.size() - 1;
	int k = i - numColors*(i/numColors);      		
	//g.setColor(colors[k]);	//set different colors for different streams
	MovingObject mObj = (MovingObject)(pointsH.elementAt(2));
	mObj.setColor(colors[k]);
	//mObj.setStartAndEndLocation(p1, p2);
	mObj.setStream(pointsH);
	Graphics g = getGraphics();
	mObj.move(g, this);
	
   }

   //move all the trace
   public void moveAll() {
	//int i = movingStreamsHolder.size();
	//Vector pointsH = new Vector();
	
	for (int i=0; i<movingStreamsHolder.size(); i++) {
		//pointsH = (
		doMove((Vector)movingStreamsHolder.elementAt(i), i);
   		//points = ((Vector)(streamsHolder.elementAt(streamID)));
	}
   }

   //reset all the moving target
   public void resetAllMobiles() {
	for (int i=0; i<movingStreamsHolder.size(); i++) {
		Vector pointsH = (Vector)movingStreamsHolder.elementAt(i);
		MovingObject mObj = (MovingObject)(pointsH.elementAt(2));
		mObj.reset();
	}
   }
   
   private void moveSimulator(Vector pointsH) {
	//1. get start point & end point
	int x = (int)(Math.random()*getWidth());
	int y = (int)(Math.random()*getHeight());
	Point p1 = new Point(x,y);
	System.out.println("moveSimulator");
	System.out.println("x:"+p1.x+"y:"+p1.y);

	x = (int)(Math.random()*getWidth());
	y = (int)(Math.random()*getHeight());
	Point p2 = new Point(x,y);
	System.out.println("x:"+p2.x+"y:"+p2.y);

	//2. generates all the nodes between start and end points
	//   put them into the stream
	pointsH.add(new Point(p1));
	pointsH.add(new Point(p2));

	//get the color
	int j = movingStreamsHolder.size() - 1;
	int k = j - numColors*(j/numColors);      		
	//g.setColor(colors[k]);	//set different colors for different streams
	MovingObject mObj = (MovingObject)(pointsH.elementAt(2));
	mObj.setColor(colors[k]);
	//mObj.setStartAndEndLocation(p1, p2);
	mObj.setStream(pointsH);
	Graphics g = getGraphics();
	mObj.move(g, this);
	
   }

   //set stream's group id
   public void setStreamGroupID(int streamID, int groupID) {
   	Vector points = new Vector();
   	
   	if (streamID < streamsHolder.size()) {
   		points = ((Vector)(streamsHolder.elementAt(streamID)));
   		points.setElementAt(new Integer(groupID), 0);	//0 is the location for groupID
   	}
   	else {
   		System.out.println("insertPoint: out of range for streamID");
   	}
   }
   
   //set moving stream's group id
   public void setMovingStreamGroupID(int streamID, int groupID) {
   	Vector points = new Vector();
   	
   	if (streamID < movingStreamsHolder.size()) {
   		points = ((Vector)(movingStreamsHolder.elementAt(streamID)));
   		points.setElementAt(new Integer(groupID), 0);	//0 is the location for groupID
   	}
   	else {
   		System.out.println("insertPoint: out of range for streamID");
   	}
   }
   
   //get stream's group id
   //if no stream here, return -2
   public int getStreamGroupID(int streamID) {
   	int groupID;
   	Vector points = new Vector();
   	
   	if (streamID < streamsHolder.size()) {
   		points = ((Vector)(streamsHolder.elementAt(streamID)));
   		groupID = ((Integer)(points.elementAt(0))).intValue();	//0 is the location for groupID
   	}
   	else {
   		System.out.println("insertPoint: out of range for streamID");
   		groupID = -2;
   	}
   	return groupID;
   }

   //get moving stream's group id
   //if no stream here, return -2
   public int getMovingStreamGroupID(int streamID) {
   	int groupID;
   	Vector points = new Vector();
   	
   	if (streamID < movingStreamsHolder.size()) {
   		points = ((Vector)(movingStreamsHolder.elementAt(streamID)));
   		groupID = ((Integer)(points.elementAt(0))).intValue();	//0 is the location for groupID
   	}
   	else {
   		System.out.println("insertPoint: out of range for streamID");
   		groupID = -2;
   	}
   	return groupID;
   }

   public void deleteStream(int streamID) {
   	
   	if (streamID < streamsHolder.size()) {
   		streamsHolder.remove(streamID);
   		repaint();
   	}
   	else {
   		System.out.println("deleteStream: out of range for streamID");
   	}
   }
   
   public void deleteMovingStream(int streamID) {
   	
   	if (streamID < movingStreamsHolder.size()) {
   		movingStreamsHolder.remove(streamID);
   		repaint();
   	}
   	else {
   		System.out.println("deleteStream: out of range for streamID");
   	}
   }
   
   public void hideTraceByStreamID(int streamID) {
   	Vector points = new Vector();
   	
   	if (streamID < streamsHolder.size()) {
   		points = ((Vector)(streamsHolder.elementAt(streamID)));
   		points.setElementAt(new Boolean(false), 1);	//1 is the location for show var
   		repaint();
   	}
   	else {
   		System.out.println("hideTraceByStreamID: out of range for streamID");
   	}
   }

   public void hideTraceByMovingStreamID(int streamID) {
   	Vector points = new Vector();
   	
   	if (streamID < movingStreamsHolder.size()) {
   		points = ((Vector)(movingStreamsHolder.elementAt(streamID)));
   		points.setElementAt(new Boolean(false), 1);	//1 is the location for show var
   		repaint();
   	}
   	else {
   		System.out.println("hideTraceByStreamID: out of range for streamID");
   	}
   }

   public void hideTraceByGroupID(int groupID) {
   	Vector points = new Vector();
   	int streamID;
   	
   	for (streamID=0; streamID < streamsHolder.size(); streamID++) {
   		points = ((Vector)(streamsHolder.elementAt(streamID)));
   		if (((Integer)(points.elementAt(0))).intValue() == groupID) {
   			points.setElementAt(new Boolean(false), 1);	//1 is the location for show var
   			repaint();
   		};
   	}
   }

   public void hideTraceByMovingGroupID(int groupID) {
   	Vector points = new Vector();
   	int streamID;
   	
   	for (streamID=0; streamID < movingStreamsHolder.size(); streamID++) {
   		points = ((Vector)(movingStreamsHolder.elementAt(streamID)));
   		if (((Integer)(points.elementAt(0))).intValue() == groupID) {
   			points.setElementAt(new Boolean(false), 1);	//1 is the location for show var
   			repaint();
   		};
   	}
   }

   public void showTraceByStreamID(int streamID) {
   	Vector points = new Vector();
   	
   	if (streamID < streamsHolder.size()) {
   		points = ((Vector)(streamsHolder.elementAt(streamID)));
   		points.setElementAt(new Boolean(true), 1);	//1 is the location for show var
   		repaint();
   	}
   	else {
   		System.out.println("deleteStream: out of range for streamID");
   	}
   }
   	
   public void showTraceByMovingStreamID(int streamID) {
   	Vector points = new Vector();
   	
   	if (streamID < movingStreamsHolder.size()) {
   		points = ((Vector)(movingStreamsHolder.elementAt(streamID)));
   		points.setElementAt(new Boolean(true), 1);	//1 is the location for show var
   		repaint();
   	}
   	else {
   		System.out.println("deleteStream: out of range for streamID");
   	}
   }
   	
   public void showTraceByGroupID(int groupID) {
   	Vector points = new Vector();
   	int streamID;
   	
   	for (streamID=0; streamID < streamsHolder.size(); streamID++) {
   		points = ((Vector)(streamsHolder.elementAt(streamID)));
   		if (((Integer)(points.elementAt(0))).intValue() == groupID) {
   			points.setElementAt(new Boolean(true), 1);	//1 is the location for show var
   			repaint();
   		};
   	}
   }

   public void showTraceByMovingGroupID(int groupID) {
   	Vector points = new Vector();
   	int streamID;
   	
   	for (streamID=0; streamID < movingStreamsHolder.size(); streamID++) {
   		points = ((Vector)(movingStreamsHolder.elementAt(streamID)));
   		if (((Integer)(points.elementAt(0))).intValue() == groupID) {
   			points.setElementAt(new Boolean(true), 1);	//1 is the location for show var
   			repaint();
   		};
   	}
   }

   
   protected void setMouseX(int x) {
   	xpos = x;
   }

   protected void setMouseY(int y) {
   	ypos = y;
   }
   
   //conclude whether current mouse point to an existed point
   protected void clickShow(JLabel mouseLabel) {
      int i, j;
      //Vector points = new Vector();
      Point p1 = new Point(0, 0);
      Point p2 = new Point(0, 0);
      Node node = new Node();
      
      //for (j=0; j<numStreams; j++) {
      	//points = ((Vector)(streamsHolder.elementAt(j)));
      	for (i=0; i<nodesHolder.size(); i++) { //the first element begins from location 0
		node = (Node)(nodesHolder.elementAt(i));
      		p1 = node.loc;
      		System.out.println("x:"+p1.x+"y:"+p1.y);
      		System.out.println("co:"+co);
      		p2.x = (int)(co*p1.x);
      		p2.y = (int)(co*p1.y);
      		
      		if (atPoint(p2)) {
      			mouseLabel.setText("Node "+(node.nodeId)+" Stats "+(node.status)+" ("+p1.x+","+p1.y+")"+"Parent:"+(node.parent));
      			return;
      		}
      		
      	}
      //}
      mouseLabel.setText("mouse clicked ("+ xpos+ "," + ypos+")");
      repaint();
   }
   
   //whether mouse point to a specific point
   private boolean atPoint(Point p) {
   	if (xpos+1 >= p.x && xpos+1 < p.x+pointWidth && ypos+1 >= p.y && ypos+1 < p.y+pointHeight)
   		return true;
   	else
   		return false;
   }


   protected void setInterval(int inter) {
   	interval = inter;
   	co = ((double)interval)/defaultInterval;
   }

   
   //remove all points
   public void clear() {
   	Vector points = new Vector();
	resetAllMobiles();

	//delete streams
   	for (int i=0; i<streamsHolder.size(); i++) {
   		points = ((Vector)(streamsHolder.elementAt(i)));
   		points.clear();
   	}
   	streamsHolder.clear();

	//delete moving streams
   	for (int i=0; i<movingStreamsHolder.size(); i++) {
   		points = ((Vector)(movingStreamsHolder.elementAt(i)));
   		points.clear();
   	}
   	movingStreamsHolder.clear();

	nodesHolder.clear();

   	numStreams = 0;
	numMovingStreams = 0;
   	xpos = 0;
   	ypos = 0;
   	
   	//set default slider
   	co = 1.;
   	interval = defaultInterval;
   	
   	replaying = false;
   	lines = 0;
   }
   	
   	
   //draw a point as a pointWidth*pointHeight rect
   protected void paintPoint(Graphics g, Point point) {
   	g.fillRect(point.x - 1, point.y - 1, pointWidth, pointHeight);
   }
   
   //gernerate a new node in network
   protected void getPoint() {
	int stats;
   	Vector points = new Vector();
   	System.out.println("in getPoint function");
   	//for (int i=0; i<numStreams; i++) {
		//int nodeId = (int)(Math.random()*1000);
		int nodeId = (int)(nodesHolder.size());
		int parent = nodeId + 1;
   		int x = (int)(Math.random()*getWidth());
   		int y = (int)(Math.random()*getHeight());
		if (Math.random() < sleepCriticalValue)
			stats = 0; //sleep
		else
			stats = 1; //active
   	
   		Point point = new Point(x, y);
		//Node node = new Node(nodeId, point, stats);
		Node node = new Node(nodeId, point, 0, parent, 0, 0, 0, stats);
   		//points = ((Vector)(streamsHolder.elementAt(i)));
   		nodesHolder.addElement(node);
   		System.out.println("nodeId:"+nodeId);
   		System.out.println("x:"+point.x);
   		System.out.println("y:"+point.y);
		System.out.println("status:"+stats);
   	//}
   	
   	System.out.println("getPoint success");
	blinkNode(nodeId);
   }
   
   //each time generates one point for every moving stream
   protected void getMovingPoint() {
   	Vector points = new Vector();
   	System.out.println("in getPoint function");
   	for (int i=0; i<numMovingStreams; i++) {
   		int x = (int)(Math.random()*getWidth());
   		int y = (int)(Math.random()*getHeight());
   	
   		Point point = new Point(x, y);
   		points = ((Vector)(movingStreamsHolder.elementAt(i)));
   		points.addElement(point);
   		System.out.println("x:"+point.x);
   		System.out.println("y:"+point.y);
   	}
   	
   	System.out.println("getPoint success");
   }
   
   //draw grid
   private void paintGrid(Graphics g, int inter) {
   	Color c = new Color(243, 233, 211);
	int screenWid = getWidth();
	int screenHei = getHeight();
   	g.setColor(c);
   	g.drawString("(0,0)", 0, 13);
   	
   	for (int i=0; i<screenWid; i+=inter) {
   		g.drawLine(i, 0, i, screenHei-1);
   	}
	g.drawLine(screenWid-1, 0, screenWid-1, screenHei-1);
   	
   	for (int j=0; j<screenHei; j+=inter) {
		g.drawLine(0, j, screenWid-1, j);
   	}
      			
   }

   /* draw the points and lines
    */
   public void paintComponent(Graphics g)
   {  
      int i, j;
      Vector points = new Vector();
      Node node = new Node();
      Node nd = new Node();
      Point p1 = new Point();
      Point p2 = new Point();
      Point p3 = new Point();
      Point p4 = new Point();

      //System.out.println("in paintComponent");
      
      if (replaying) {
      	//replay(g);
      	return;
      }
      
      super.paintComponent(g); //paint background
      paintGrid(g, interval);
      
      //System.out.println("numStreams = "+numStreams);

      //draw spanning tree firstly
      g.setColor(new Color(0, 255, 0));
      for (i=0; i<nodesHolder.size(); i++) {
	node = (Node)(nodesHolder.elementAt(i));
	nd = lookupNode(node.getParent());
	if (nd != null)
   		g.drawLine((int)(co*node.loc.x), (int)(co*node.loc.y), (int)(co*nd.loc.x), (int)(co*nd.loc.y));
      }

      //draw all the nodes on the field
      for (i=0; i<nodesHolder.size(); i++) {
	node = (Node)(nodesHolder.elementAt(i));
	if (node.status == 0) {
		//the node is asleep or dead
		g.setColor(new Color(0, 100, 240));
	}
	if (node.status == 1) {
		//the node is active
		g.setColor(new Color(0, 255, 0));
	}
	if (node.status == 2) {
		//the node is leader
		g.setColor(new Color(255, 0, 0));
	}
	p3.x = (int)(co*node.loc.x);
	p3.y = (int)(co*node.loc.y);
	paintPoint(g, p3);
   	g.drawString((new Integer(node.nodeId)).toString(), (int)((p3.x+5)), (int)((p3.y+10)));
      }
      
   }
   
   //for replaying
   public void replay(Graphics g) {
      int i, j;
      
      Vector points = new Vector();
      Point p1 = new Point();
      Point p2 = new Point();
      Point p3 = new Point();
      Point p4 = new Point();
      Node node;

      super.paintComponent(g); //paint background
      //g.dispose();
      System.out.println("replay...");
      //g.clearRect(0, 0, 1024, 650);
      
      paintGrid(g, interval);
      
      //draw all the nodes on the field
      for (i=0; i<nodesHolder.size(); i++) {
	node = (Node)(nodesHolder.elementAt(i));
	if (node.status == 0) {
		//the node is asleep or dead
		g.setColor(new Color(0, 100, 240));
	}
	if (node.status == 1) {
		//the node is active
		g.setColor(new Color(0, 255, 0));
	}
	p3.x = (int)(co*node.loc.x);
	p3.y = (int)(co*node.loc.y);
	paintPoint(g, p3);
      }
      
      
      for (j=0; j<numMovingStreams; j++) {
      	points = ((Vector)(movingStreamsHolder.elementAt(j)));
      	if ( ((Boolean)(points.elementAt(1))).booleanValue() ) {
      		int k = j - numColors*(j/numColors);
      		//g.setColor(colors[k]);	//set different colors for different streams
		MovingObject mObj = (MovingObject)(points.elementAt(2));
		mObj.setColor(colors[k]);
		p1 = (Point)(points.elementAt(3));
		p2 = (Point)(points.elementAt(4));
		mObj.setStartAndEndLocation(p1, p2);
		mObj.move(g, this);

      	}
      }
      
      replaying = false;
      lines = 0;
      timer.cancel(); //Terminate the timer thread
      System.out.println("replaying:"+replaying);
      
    }

    public void run() {
    	repaint();
    }

    //camera control
    public void cameraControl() {
	if (existObjectOnLeft()) {
		//there is at least one object on left field
		//open camera
		//cameraComp1.setVisible(true);
		//mediaPlayer1.start();
	}
	else {
		//no object is on left field
		//stop camera
		//cameraComp1.setVisible(false);
		//mediaPlayer1.stop();
	}
    }


    /*
    //camera initialize
    public void cameraInit() {

	vectorDevices = CaptureDeviceManager.getDeviceList(null);
	int nCount = vectorDevices.size();
	vectorVideoDevices = new Vector();
	for (int i = 0; i< nCount; i++) {
		CaptureDeviceInfo infoCaptureDevice = (CaptureDeviceInfo) vectorDevices.elementAt(i);
		Format[] arrFormats = infoCaptureDevice.getFormats();
		for (int j = 0; j < arrFormats.length; j++) {
			if (arrFormats[j] instanceof VideoFormat) {
			  vectorVideoDevices.addElement(infoCaptureDevice);
			  // System.out.println(arrFormats[j]);
			  break;
			}
		}
	}
	System.out.println(vectorVideoDevices.size());
	try {
	    System.out.println("create data source 0...");
	dataSource1 = javax.media.Manager.createDataSource(((CaptureDeviceInfo) vectorVideoDevices.elementAt(0)).getLocator());
	//dataSource2 = javax.media.Manager.createDataSource(((CaptureDeviceInfo) vectorVideoDevices.elementAt(1)).getLocator());
	System.out.println("create data source...");
	} catch (IOException e) {
	    System.out.println(e.getMessage());
	    System.out.println("exc1 ...");
	} catch (NoDataSourceException e) {
	    System.out.println(e.getMessage());
	    System.out.println("exc2 ...");
	}
	if ((dataSource1 != null)) {
	FormatControl formatControls1 [];
	//FormatControl formatControls2 [];
	Format[] formats1;//, formats2;
	formatControls1 = ((CaptureDevice) dataSource1).getFormatControls();
	//formatControls2 = ((CaptureDevice) dataSource2).getFormatControls();
	formats1 = formatControls1[0].getSupportedFormats();
	//formats2 = formatControls2[0].getSupportedFormats();
	formatControls1[0].setFormat(formats1[3]);
	System.out.println(formats1[3]);
	//formatControls2[0].setFormat(formats2[3]);

	try {
		dataSource1.connect();
		//dataSource2.connect();
		    System.out.println("connect...");
	} catch (IOException e) {
	}
	//MyListener listener1 = new MyListener();
	//MyListener listener2 = new MyListener();


	mediaPlayer1 = new MediaPlayer();
	mediaPlayer1.setDataSource(dataSource1);
	//mediaPlayer2 = new MediaPlayer();
	//mediaPlayer2.setDataSource(dataSource2);
	if (mediaPlayer1 == null || /*mediaPlayer2 == null )
	System.exit(2);
	mediaPlayer1.setPopupActive ( false );
	mediaPlayer1.setControlPanelVisible ( false );
	mediaPlayer1.addControllerListener ( listener1 );


	//mediaPlayer2.setPopupActive ( false );
	//mediaPlayer2.setControlPanelVisible ( false );
	//mediaPlayer2.addControllerListener ( listener2 );
	mediaPlayer1.realize();
	//mediaPlayer2.realize();
	System.out.println("realizing");

	while (listener1.realized == false){
	;
	}

	cameraComp1 = mediaPlayer1.getVisualComponent();
	//comp2 = mediaPlayer2.getVisualComponent();
	on1 = true;
	//on2 = true;
	System.out.println("here we go");
	//frame.getContentPane().setLayout(null);
	//frame.getContentPane().add(comp1);
	//frame.getContentPane().add(comp2);
	//frame.setSize(frameWidth, frameHeight);
	//frame.setVisible(true);
	//frame.setResizable(false);
	mediaPlayer1.start();
	//mediaPlayer2.start();
	} // if

    } // cameraInit()
    */

    //whether there is an obj on left
    private boolean existObjectOnLeft() {
      MovingObject mObj;
      Vector points;
      int j;
      
      for (j=0; j<movingStreamsHolder.size(); j++) {
		points = ((Vector)(movingStreamsHolder.elementAt(j)));
		mObj = (MovingObject)(points.elementAt(2)); //get the moving obj
		if (mObj.onLeft((int)(this.getWidth()/2)))
			return true;
      }
      return false;
    }

  public void receivedNodeMsg(int nodeID, int x, int y, int voltage, int state,int parentID) {
    // when a Node Status message is received, Jun, you may delete all these output and insert your code here

    addNode(nodeID, state, x, y); //2--leader, 0 , 0 are fake
    blinkNode(nodeID);
    System.out.println("Node ID: " + nodeID);
    System.out.println("x : " + x + " y : " + y);
  }

  public void receivedNodeMsg(int nodeID, int x, int y, int sentry, int parentID, int voltage, int numSentries, int numNeighbors, int state) {
    // when a Node Status message is received, Jun, you may delete all these output and insert your code here

    addNode(nodeID, x, y, sentry, parentID, voltage, numSentries, numNeighbors, state);

    //blink node when receive a new msg from it
    blinkNode(nodeID);

    //addNode(nodeID, state, x, y); //2--leader, 0 , 0 are fake
    System.out.println("Node ID: " + nodeID);
    System.out.println("x : " + x + " y : " + y);
  }


  public void receivedTrackMsg(int groupID, int x, int y, int leader) {
    // when a Tracking message is received, Jun, you may delete all these output and insert your code here
    addMovingPoint(groupID, x, y);
    //addNode(leader, 2, 0, 0); //2--leader, 0 , 0 are fake
    System.out.println("Tracking - x : " + x + " y : " + y);
  }

   public void blinkNode(int nodeID) {
   Color back = ((Component)this).getBackground(); //get the color of backgroung
	Graphics g = ((Component)this).getGraphics();
	Node nd = lookupNode(nodeID);
	Color cur;

	if (nd == null)
		return;

	if (nd.status == 0) {
		//the node is asleep or dead
		cur = new Color(0, 100, 240);
	}
	else if (nd.status == 1) {
		//the node is active
		cur = new Color(0, 255, 0);
	}
	else if (nd.status == 2) {
		//the node is leader
		cur = new Color(255, 0, 0);
	}
	else
		cur = new Color(100, 100, 100);

	Blink bk = new Blink(this, nd, g, cur, back);
  }

} //DrawLinePanel

//FileFilter for tracing
class TraceFilter extends javax.swing.filechooser.FileFilter 
{
  /**
    This is the one of the methods that is declared in 
    the abstract class
   */
  public boolean accept(File f) 
  {
    //if it is a directory -- we want to show it so return true.
    if (f.isDirectory()) 
      return true;
  
    //get the extension of the file

    String extension = getExtension(f);
    //check to see if the extension is equal to "tra"
    if ((extension.equals("tra"))) 
       return true; 

    //default -- fall through. False is return on all
    //occasions except:
    //a) the file is a directory
    //b) the file's extension is what we are looking for.
    return false;
  }
    
  /**
    Again, this is declared in the abstract class

    The description of this filter
   */
  public String getDescription() 
  {
      return "Motes trace files (*.tra)";
  }

  /**
    Method to get the extension of the file, in lowercase
   */
  private String getExtension(File f) 
  {
    String s = f.getName();
    int i = s.lastIndexOf('.');
    if (i > 0 &&  i < s.length() - 1) 
      return s.substring(i+1).toLowerCase();
    return "";
  }
}


public class SwingMotes {
    private final int	ITEM_PLAIN	=	0;	// Item types
    private final int	ITEM_CHECK	=	1;
    private final int	ITEM_RADIO	=	2;

    private static String labelPrefix = "Number of points: ";
    private static String streamLabelPrefix = "Number of streams: ";
    private static String sliderLabelPrefix = "Interval of grid: ";
    private int numClicks = 0;
    private int numClicks2 = 0;
    final static DrawLinePanel spane = new DrawLinePanel();
    private static ClientThread ct = null; // connect to motes
    final JLabel label = new JLabel(labelPrefix + "0    ");
    final JLabel streamLabel = new JLabel(streamLabelPrefix + "0    ");
    final JLabel sliderLabel = new JLabel(sliderLabelPrefix + "100   ");
    final JLabel mouseLabel = new JLabel("         ");
    final JSlider slider = new JSlider(JSlider.HORIZONTAL, 50, 150, spane.defaultInterval);
    
    private	File	file = null;
    
    private	static	JFrame		frame;
    private	int	numStreams = 0;
    private	JMenuBar	menuBar;
    private	JMenu		menuFile;
    private	JMenu		menuEdit;
    private	JMenu		menuView;
    private	JMenuItem	menuFileNew;
    private	JMenuItem	menuFileOpen;
    private	JMenuItem	menuFileSave;
    private	JMenuItem	menuFileSaveAs;
    private	JMenuItem	menuFileExit;
    private	JMenuItem	menuEditAddStream;
    private	JMenuItem	menuEditGridInt;
    private	JMenuItem	menuEditClear;
    private	JMenuItem	menuEditReplay;
    private	JMenuItem	menuViewAllStreams;
    
    private	Vector	showlist = new Vector();

    // init the connection to motes
    public static void initMotes() {
	ct = new ClientThread(spane);
    }

    /*
     * interface with lower level
     */
    public void  insertPoint(int groupID, double x, double y) {   //GroupID is the same as Stream ID
    	if ( isTraceThere(groupID) ) {
    		spane.insertPoint(groupID, x, y);
    	}
    	else {
    		//add new trace
	    	numClicks2++;
                streamLabel.setText("new stream. GroupID:" + groupID);
                System.out.println("Add New Stream with ID "+groupID);
                spane.addMovingStream(groupID);
                addViewStream( numStreams++ ); //add stream to menu
                
    		spane.insertPoint(groupID, x, y);
    	};
    }

	public void  addTrace(int groupID, int num, double[][] cor) {
		for (int i=0; i<num; i++){
			insertPoint(groupID, cor[i][0], cor[i][1]);			
		}
	}

    public void  deleteTrace(int groupID) {
    	int streamID = spane.lookupGroupID(groupID);
    	if ( streamID >= 0 ) {
    		spane.deleteMovingStream(streamID);
    		//need more work to modify menu here
    		//!!!!
    	};
    }

    public void  hideTrace(int groupID) {
    	int streamID = spane.lookupGroupID(groupID);
    	if ( streamID >= 0 ) {
    		spane.hideTraceByMovingStreamID(streamID);
    		
    		((JCheckBoxMenuItem)(menuView.getItem(streamID+2))).doClick();
    		
    	};
    }

    public void  showTrace(int groupID) {
    	int streamID = spane.lookupGroupID(groupID);
    	if ( streamID >= 0 ) {
    		spane.showTraceByMovingStreamID(streamID);
    		
    		((JCheckBoxMenuItem)(menuView.getItem(streamID+2))).doClick();
    		
    	};
    }

    public boolean isTraceThere(int groupID) {
    	if ( spane.lookupGroupID(groupID) == -1 ) {
    		return false;
    	}
    	return true;
    }
    
    public void setDefaultSlider(int value) {
    	slider.setValue(value);
    }

    //add a menulist in View for a new stream
    public void addViewStream(int streamID) {
    	JMenuItem menuViewNew = CreateMenuItem( menuView, ITEM_CHECK,
    							"Hide stream "+streamID, null,
    							0, "Hide stream "+streamID,
    		new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int streamID;
			JCheckBoxMenuItem item = (JCheckBoxMenuItem)(e.getSource());
			
			//get streamID from ActionCommand
			String streamString = e.getActionCommand();
			int index = streamString.lastIndexOf(" ");
			streamID = (Integer.valueOf(streamString.substring(index+1))).intValue();

			if ( item.getState() ) {
				spane.hideTraceByMovingStreamID(streamID);
				((JCheckBoxMenuItem)menuViewAllStreams).setState(false);
			}
			else {
				spane.showTraceByMovingStreamID(streamID);
			}
			spane.repaint();
		}
		} );
    }
    
    public void deleteViewStreams() {
    	int size = menuView.getItemCount();
    	
    	for ( int i=size-1; i>=2; i--) {
    		menuView.remove(i);
    	}
    }
    
    public void clearAllStreams(){
    
    spane.clear();
    }

    public JPanel createComponents(JFrame frame ) {
    	
        //final JLabel label = new JLabel(labelPrefix + "0    ");
        //final JLabel streamLabel = new JLabel(streamLabelPrefix + "0    ");
        //final DrawLinePanel spane = new DrawLinePanel();

	new Thread(spane).start();
	
        final JPanel pane = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        pane.setLayout(gridbag);
        
        //add menu
        menuBar = new JMenuBar();
        frame.setJMenuBar( menuBar );
        
        // Create the file menu
	menuFile = new JMenu( "File" );
	menuFile.setMnemonic( 'F' );
	menuBar.add( menuFile );
	
	// Create the file menu
	// Build a file menu items
	menuFileNew = CreateMenuItem( menuFile, ITEM_PLAIN,
							"New", null, 'N', "Create a new file", 
	    new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
            		numClicks = 0;
            		numClicks2 = 0;
            		numStreams = 0;
            		file = null;
            		deleteViewStreams();
            		streamLabel.setText(streamLabelPrefix + numClicks2);
            		label.setText(labelPrefix + numClicks);
            		mouseLabel.setText("     ");
                	spane.clear();
                	setDefaultSlider(spane.defaultInterval);
                	spane.repaint();
                }
            });
	menuFileOpen = CreateMenuItem( menuFile, ITEM_PLAIN, "Open...",
							null, 'O',
							"Open a new file", 
	    new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		JFileChooser fc = new JFileChooser(); //create a file chooser
	    		TraceFilter filter = new TraceFilter();
	    		//filter.addExtension("tra");
	    		//filter.setDescription("Trace record file");
	    		fc.setFileFilter(filter);
	    		
	    		int returnVal = fc.showOpenDialog(pane); //show it
	    		
	    		//query JFileChooser to get the input from user
	    		if(returnVal == JFileChooser.APPROVE_OPTION) {
	    			file = fc.getSelectedFile();
	    			//numStreams = spane.open(file);
	    			numStreams = spane.getPointsFromFile(file);
	    			if(numStreams > 0) {
	    				for(int k=0; k<numStreams; k++) {
	    					addViewStream(k);
	    				}
	    				spane.repaint();
					spane.moveAll();
	    			}
	    			if(numStreams == 0) {
	    				mouseLabel.setText("file format wrong!");
	    			}
	    			
	    		}
	    	}
	    });
	menuFileSave = CreateMenuItem( menuFile, ITEM_PLAIN, "Save",
							null, 'S',
							" Save this file", 
	    new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		if(file != null) {
	    			//save this file
	    			spane.save(file);
	    			return;
	    		}
	    		JFileChooser fc = new JFileChooser(); //create a file chooser
	    		TraceFilter filter = new TraceFilter();
	    		//filter.addExtension("tra");
	    		//filter.setDescription("Trace record file");
	    		fc.setFileFilter(filter);
	    		
	    		int returnVal = fc.showSaveDialog(pane); //show it
	    		
	    		//query JFileChooser to get the input from user
	    		if(returnVal == JFileChooser.APPROVE_OPTION) {
	    			file = fc.getSelectedFile();
	    			spane.save(file);
	    		}
	    		
	    	}
	    });
	menuFileSaveAs = CreateMenuItem( menuFile, ITEM_PLAIN,
							"Save As...", null, 'A',
							"Save this data to a new file",
	    new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		JFileChooser fc = new JFileChooser(); //create a file chooser
	    		TraceFilter filter = new TraceFilter();
	    		//filter.addExtension("tra");
	    		//filter.setDescription("Trace record file");
	    		fc.setFileFilter(filter);

	    		int returnVal = fc.showSaveDialog(pane); //show it
	    		
	    		//query JFileChooser to get the input from user
	    		if(returnVal == JFileChooser.APPROVE_OPTION) {
	    			file = fc.getSelectedFile();
	    			spane.save(file);
	    		}
	    	}
	    });
	//menuFile.addSeparator();
	menuFileExit = CreateMenuItem( menuFile, ITEM_PLAIN,
							"Exit", null, 'x',
							"Exit the program", 
	    new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		System.exit(0);
	    	}
	    });
	
	menuEdit = new JMenu( "Edit" );
	menuEdit.setMnemonic( 'E' );
	menuBar.add( menuEdit );
	
	menuEditAddStream = CreateMenuItem( menuEdit, ITEM_PLAIN, "Add Stream", null,
							'd', "Add Stream", 
	    new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		numClicks2++;
                	streamLabel.setText(streamLabelPrefix + numClicks2);
                	spane.addMovingStream();
                	addViewStream( numStreams++ );
                }
            });
	menuEditGridInt = CreateMenuItem( menuEdit, ITEM_PLAIN, "Grid Interval", null,
							'G', "Grid Interval", null );
	menuEditClear = CreateMenuItem( menuEdit, ITEM_PLAIN, "Clear", null, 'C', "Clear",
	    new ActionListener() {
		public void actionPerformed(ActionEvent e) {
            		numClicks = 0;
            		numClicks2 = 0;
            		numStreams = 0;
            		file = null;
            		deleteViewStreams();
            		streamLabel.setText(streamLabelPrefix + numClicks2);
            		label.setText(labelPrefix + numClicks);
            		mouseLabel.setText("     ");
                	spane.clear();
                	setDefaultSlider(spane.defaultInterval);
                	spane.repaint();
            	}
            });
	menuEditReplay = CreateMenuItem( menuEdit, ITEM_PLAIN, "Replay", null,
							'R', "Replay", 
	    new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		System.out.println("click replay");
	    		//	getComponent().repaint();
	    		spane.doReplay();
	    	}
	    } );
	
	menuView = new JMenu( "View" );
	menuView.setMnemonic( 'V' );
	menuBar.add( menuView );
	
	menuViewAllStreams = CreateMenuItem( menuView, ITEM_CHECK, 
							"Show all streams", null, 'h',
							"Show all streams",
	    new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		JCheckBoxMenuItem item = (JCheckBoxMenuItem)(e.getSource());
	    		if (item.getState()) {
	    			for (int i=0; i<numStreams; i++) {
	    				spane.showTraceByStreamID(i);
	    				JCheckBoxMenuItem iteml = (JCheckBoxMenuItem)(menuView.getItem(i+2));
	    				iteml.setState(false);
	    			}
	    		}
	    	}
	    } );
	menuView.addSeparator();
							


        JButton button = new JButton("Next Point");
        button.setMnemonic(KeyEvent.VK_N);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	//if (spane.numStreams > 0) {
                	numClicks++;
                	label.setText(labelPrefix + numClicks);
                
                	spane.getPoint();
			//switch to moving version
                	spane.getMovingPoint();
			spane.moveAll(); //move the target in the field
                
                	spane.repaint(); //draw the new point and line
                //}
            }
        });
        label.setLabelFor(button);
        
        // button and label for adding stream
        JButton streamButton = new JButton("Add Stream");
        streamButton.setMnemonic(KeyEvent.VK_A);
        streamButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                numClicks2++;
                streamLabel.setText(streamLabelPrefix + numClicks2);
                spane.addMovingStream();
                addViewStream( numStreams++ );
            }
        });
        streamLabel.setLabelFor(streamButton);

	// button for reset
	JButton resetButton = new JButton("Reset");
        resetButton.setMnemonic(KeyEvent.VK_R);
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	numClicks = 0;
            	numClicks2 = 0;
            	numStreams = 0;
            	file = null;
            	deleteViewStreams();
            	streamLabel.setText(streamLabelPrefix + numClicks2);
            	label.setText(labelPrefix + numClicks);
            	mouseLabel.setText("     ");
                spane.clear();
                setDefaultSlider(spane.defaultInterval);
                spane.repaint();
            }
        });
        
        //add mouse click event
        spane.addMouseListener(new MouseListener() {
        	public void mouseClicked(MouseEvent e) {
        		spane.setMouseX(e.getX());
        		spane.setMouseY(e.getY());
        		spane.clickShow(mouseLabel);
        	}
        	
        	public void mousePressed (MouseEvent e) {}
        	public void mouseReleased (MouseEvent e) {}
        	public void mouseEntered (MouseEvent e) {}
        	public void mouseExited (MouseEvent e) {}
        });
        

        /*
         * An easy way to put space between a top-level container
         * and its contents is to put the contents in a JPanel
         * that has an "empty" border.
         */
	/*
        JPanel pane = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        pane.setLayout(gridbag);
	*/
        //c.fill = GridBagConstraints.HORIZONTAL;
        
        //first button
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        gridbag.setConstraints(button, c);
        pane.add(button);

        //streamButton
        c.gridx = 1;
        c.gridy = 0;
        gridbag.setConstraints(streamButton, c);
        pane.add(streamButton);

        //resetButton
        c.gridx = 2;
        c.gridy = 0;
        gridbag.setConstraints(resetButton, c);
        pane.add(resetButton);

        //slider
        slider.addChangeListener(new SliderListener());
        slider.setMajorTickSpacing(100);
        slider.setMinorTickSpacing(4);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setValue(spane.defaultInterval); //100 is default value
        slider.setBorder(
                BorderFactory.createEmptyBorder(0,0,10,0));
	c.gridx = 3;
	c.gridy = 0;
	gridbag.setConstraints(streamLabel, c);
	pane.add(slider);


        //first label
        c.gridx = 0;
        c.gridy = 1;
        gridbag.setConstraints(label, c);
        pane.add(label);
        
        //streamLabel
        c.gridx = 1;
        c.gridy = 1;
        gridbag.setConstraints(streamLabel, c);
        pane.add(streamLabel);
        
        //mouseLabel
        c.gridx = 2;
        c.gridy = 1;
        gridbag.setConstraints(mouseLabel, c);
        pane.add(mouseLabel);
        
        //sliderLabel
        c.gridx = 3;
        c.gridy = 1;
        gridbag.setConstraints(sliderLabel, c);
        pane.add(sliderLabel);
        
        //add an area to draw points and lines
        c.weighty = 1.0;
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 2;
        c.gridheight = 10;
        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(spane, c);
        pane.add(spane);

	//add two areas to show cameras
	//c.ipady = 0;
	/*
	c.weightx = 0;
	c.weighty = 0;
	c.gridx = 3;
	c.gridy = 2;
	c.gridwidth = 1;
	c.gridheight = 5;
        //c.fill = GridBagConstraints.BOTH;
        c.fill = GridBagConstraints.CENTER;
        gridbag.setConstraints(spane.cameraComp1, c);
        pane.add(spane.cameraComp1);
	spane.cameraComp1.setVisible(true);
	*/

	/*
	c.gridx = 3;
	c.gridy = 7;
	c.gridwidth = 1;
	c.gridheight = 5;
        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(spane.cameraComp1, c);
        pane.add(spane.cameraComp1);
	spane.cameraComp1.setVisible(true);
	*/

        return pane;
    }
    
    /** Listens to the slider. */
    class SliderListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting()) {
            	sliderLabel.setText(sliderLabelPrefix + (int)source.getValue());
                spane.setInterval((int)source.getValue());
                spane.repaint();
            }
        }
    }    

    public JMenuItem CreateMenuItem( JMenu menu, int iType, String sText,
					ImageIcon image, int acceleratorKey,
					String sToolTip, ActionListener ac )
    {
	// Create the item
	JMenuItem menuItem;

	switch( iType )
	{
		case ITEM_RADIO:
			menuItem = new JRadioButtonMenuItem();
			break;

		case ITEM_CHECK:
			menuItem = new JCheckBoxMenuItem();
			break;

		default:
			menuItem = new JMenuItem();
			break;
	}

	// Add the item test
	menuItem.setText( sText );

	// Add the optional icon
	if( image != null )
		menuItem.setIcon( image );

	// Add the accelerator key
	if( acceleratorKey > 0 )
		menuItem.setMnemonic( acceleratorKey );

	// Add the optional tool tip text
	if( sToolTip != null )
		menuItem.setToolTipText( sToolTip );

	// Add an action handler to this menu item
	if( ac == null ) {
        	menuItem.addActionListener(new ActionListener() {
            		public void actionPerformed(ActionEvent e) {
			System.out.println( e );
            		}
        	});
        }
        else {
        	menuItem.addActionListener(ac);
        }

	menu.add( menuItem );

	return menuItem;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) { }

        //Create the top-level container and add contents to it.
        frame = new JFrame("SwingApplication");
        SwingMotes app = new SwingMotes();
        
        Component contents = app.createComponents(frame);
        frame.getContentPane().add(contents, BorderLayout.CENTER);
        
        frame.setSize(1024, 768);

        //Finish setting up the frame, and show it.
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        //frame.pack();
        frame.setVisible(true);

	//init motes
	initMotes();
    }


}
