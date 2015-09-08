package	edu.mit.mers.localization;

import net.tinyos.message.*;
import net.tinyos.util.*;

import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;

/**
 * The LocationCore class connects together the GUI, serial port link and socket
 * server, and calculates the rover location based on distance from the anchors.
 * The class implements the PacketListener interface so that the serial link can
 * report the arrival of new packets, and the Locator interface so the socket
 * server can request the location. The core maintains a list of anchor nodes,
 * and the most recent distance from each anchor to the base node. These
 * distances are then used to calculate the location of the base node that
 * minimizes the total error.
 * <p>
 * The update method of the GUI is called whenever an anchor or the base node
 * location is changed so that the GUI can redraw its display. The writeLog
 * method is called whenever a packet is sent or received so that it can be
 * recorded and displayed by the GUI.
 * <p>
 * A scale factor is required to covert the distance measured by radio signal
 * strength into a real-world distance that can be compared to the locations of
 * the anchors. This must be adjusted to take account of the physical
 * distribution of the nodes.
 */
public class LocationCore implements MessageListener, Locator {
	
    /**
     * The GUI connected to this core.
     */
    private LocationGUI gui;
    /**
     * The serial port link connected to this core.
     */
    //	private SerialIO serialLink;
    private MoteIF commLink;

    /**
     * A mapping from an anchor node ID String to a Node object representing the
     * anchor.
     */
    private Map anchors;
    
    private Map nodes;
	
    private Map tags;
	
    /**
     * A Node representing the current estimate of the base node's location.
     */
    private Node baseNode;
	
    /**
     * The scale factor for converting the radio signal distance into real-world
     * distance.
     */
    private double scaleFactor;
    private int    numCols;
    private int    numRows;

    private int    desiredNumberNeighbors;

    private boolean isRollCall = false;

    public static int baseStationID = 0;

    private TaskQueue taskQueue;

    public static int PotMapLen = 8;

    private int potMap[];

    public GroundTruthDB db;
    public MoteFieldRecord currentField;

    private String dbName;

	private double unitDistanceScaleX;
	private double unitDistanceScaleY;

    private void resetCore () {
      anchors  = new HashMap();
      nodes    = new HashMap();
      tags     = new HashMap();
      numRows  = 5;
      numCols  = 5;
      potMap   = new int[PotMapLen];
      desiredNumberNeighbors = 8;
      baseNode = new Node(baseStationID, 0.0, 0.0);
      setNodePosition(baseNode);
      nodes.put(new Integer(baseStationID), baseNode);
      scaleFactor = 1.0/numCols; // TODO: MAX cols/rows

      taskQueue = new TaskQueue(this);

      loadDB(dbName + ".db");
    }

    /**
     * Creates a new core with the specified rover name. The socket server
     * starts listening for connections on the specified port and the serial
     * link starts listening for packets arriving on the serial port. The
     * current location is set to (0.5,0.5) and the scale factor is set to 0.05.
     */
    public LocationCore(String serialForwarderAddress, String dbName, LocationGUI gui) {
	this.gui = gui;
	this.dbName = dbName;
		
	resetCore();

	try {
	    commLink = new MoteIF(serialForwarderAddress, 9000, 0x7d);
	    LocationMsg msg1 = new LocationMsg();
	    //		LocationMsg msg2 = new LocationMsg();
	    //		LocationMsg msg3 = new LocationMsg();
	    msg1.setType(LocationMsg.MSG_COMMAND);
	    //		msg2.setType(LocationMsg.MSG_GRADIENT);
	    //		msg3.setType(LocationMsg.MSG_UPDATE);
	    commLink.registerListener(msg1, this);
	    //		commLink.registerListener(msg2, this);
	    //		commLink.registerListener(msg3, this);
	    commLink.start();

	} catch(Exception e) {
	    e.printStackTrace();
	}		
    }

    public void loadDB(String fname) {
	try
	{
	    db = new GroundTruthDB(fname);
	    currentField = (MoteFieldRecord)(db.moteFieldDB.get(0));
	    
	    // Poke the relevant settings from the field into the GUI.
	    gui.setMoteFieldInfo(currentField.getScreen1X(),
				 currentField.getScreen1Y(),
				 currentField.getWorld1X(),
				 currentField.getWorld1Y(),
				 currentField.getScreen2X(),
				 currentField.getScreen2Y(),
				 currentField.getWorld2X(),
				 currentField.getWorld2Y());
	    
		unitDistanceScaleX = currentField.getUnitDistanceScaleX();
		unitDistanceScaleY = currentField.getUnitDistanceScaleY();
		
	    // Fill er up with nodes!
	    Iterator iNodeRecs = db.moteDB.iterator();
	    while(iNodeRecs.hasNext())
	    {
		    MoteRecord rec = (MoteRecord)iNodeRecs.next();
		    Node node = new Node(rec);

		    if(node.isAnchor())
		    {
			anchors.put(new Integer(node.getID()), node);
			nodes.put(new Integer(node.getID()), node);
		    }
		    else
		    {
			nodes.put(new Integer(node.getID()), node);
		    }
	    }

	    // Fill up tag info
	    Iterator iTagRecs = db.tagDB.iterator();
	    while(iTagRecs.hasNext())
	    {
		TagRecord rec = (TagRecord)iTagRecs.next();
		Tag tag = new Tag(rec);

		if(tag != null)
		    tags.put(new Integer(tag.getID()), tag);
	    }
	    
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    System.out.println("Unable to load DB!");
	}  

    }
	
    /**
     * Returns an iterator over the list of anchors. The anchors are returned as
     * Node objects.
     */
    public Iterator getAnchors() {
	return anchors.values().iterator();
    }
	
    public Iterator getNodes() {
	return nodes.values().iterator();
    }

    public int nodeCount() {
	return nodes.size();
    }

    public int getNumRows() {
	return numRows;
    }

    public int getNumCols() {
	return numCols;
    }

    public int getHoodSize() {
	return desiredNumberNeighbors;
    }

    public void setHoodSize(int hoodSize) {
	this.desiredNumberNeighbors = hoodSize;
    }

    /**
     * Add an anchor. The correct command is sent over the serial link and the
     * anchor is added to the list, with the specified nodeID used as the name,
     * and the specified co-ordinates. The GUI is updated to display the new
     * anchor. If the specified node is already an anchor, the command will be
     * sent again to the base node and the co-ordinates updated.
     */
    public void addAnchor(int nodeID, float x, float y) {

	Node node = ensureNode(nodeID);
	anchors.put(new Integer(nodeID), node);
	node.setX(x);
	node.setY(y);
	node.makeAnchor();

	LocationMsg msg = new LocationMsg();
	msg.setType(LocationMsg.MSG_COMMAND);
	msg.set_dest(nodeID);
	msg.set_cmd_no(LocationMsg.CMD_SET_ANCHOR);
	msg.set_args_status(0x4); // 4 waves
	sendMessage(MoteIF.TOS_BCAST_ADDR, msg);
	gui.update();
    }
	
    public void setAnchor(int nodeID) {
	Node node = ensureNode(nodeID);
	addAnchor(nodeID, (float)node.getX(), (float)node.getY());
    }

    /**
     * Remove an anchor. The correct command is sent over the serial link and
     * the anchor is removed from the list. The GUI is updated to remove the old
     * anchor. If the specified node is not an anchor, the command will be sent
     * to the base node anyway.
     */
    public void removeAnchor(int nodeID) {
	anchors.remove(new Integer(nodeID));
	nodes.remove(new Integer(nodeID));

	LocationMsg msg = new LocationMsg();
	msg.setType(LocationMsg.MSG_COMMAND);
	msg.set_dest(nodeID);
	msg.set_cmd_no(LocationMsg.CMD_SET_ANCHOR);
	msg.set_args_status(0x0);
	sendMessage(MoteIF.TOS_BCAST_ADDR, msg);
	gui.update();
    }
	
    public Iterator getTags() {
	return tags.values().iterator();
    }

    public int getNumTags() {
	return tags.size();
    }

    /**
     * Set the radio strength of a node. The correct command is sent over the
     * serial link to set the specified node's pot to the specified value (0 =
     * highest strength, 99 = lowest strength).
     */
    public void setPot(int nodeID, int potValue) {
	LocationMsg msg = new LocationMsg();
	msg.setType(LocationMsg.MSG_COMMAND);
	msg.set_dest(nodeID);
	msg.set_cmd_no(LocationMsg.CMD_SET_POT);
	msg.set_args_pot_value(potValue);
	sendMessage(MoteIF.TOS_BCAST_ADDR, msg);
    }
	
    /**
     * Set the radio strength to distance mapping.
     * the potMap is a vector of increasing pot values where their index+1
     * indicates the distance if pot value is less than that value when scanned 
     * from zero.
     */
    public void setPotMap(int nodeID, int potMap[]) {
	LocationMsg msg = new LocationMsg();
	msg.setType(LocationMsg.MSG_COMMAND);
	msg.set_dest(nodeID);
	msg.set_cmd_no(LocationMsg.CMD_SET_POT_MAP);
	msg.set_args_pot_map_pots(potMap);
	sendMessage(MoteIF.TOS_BCAST_ADDR, msg);
    }
	
    /**
     * Set the radio strength of all nodes. 
     */
    public void setPots() {
	taskQueue.setNodeRadios(nodes.values());
    }
	
    /**
     * Acknowledge hello message.
     */
    public void getFound(int nodeID) {
	LocationMsg msg = new LocationMsg();
	msg.setType(LocationMsg.MSG_COMMAND);
	msg.set_dest(nodeID);
	msg.set_cmd_no(LocationMsg.CMD_HELLO_ACK);
	sendMessage(MoteIF.TOS_BCAST_ADDR, msg);
    }
	
    /**
     * Test radio strength of a node. The correct command is sent over the
     * serial link to set the specified node to sequence through pot values
     * to be recorded by base station and relayed back to host where results
     * are tallied.
     */
    public void testRadio(int nodeID) {
	Iterator iNodes = nodes.values().iterator();
	while(iNodes.hasNext()) {
	    Node node = (Node)iNodes.next();
	    node.zapPotValueCounts();
	}
	LocationMsg msg = new LocationMsg();
	msg.setType(LocationMsg.MSG_COMMAND);
	msg.set_dest(nodeID);
	msg.set_cmd_no(LocationMsg.CMD_TEST_RADIO);
	sendMessage(MoteIF.TOS_BCAST_ADDR, msg);
    }

    public void calibrateAll() {
	taskQueue.calibrateNodes(nodes.values());
    }
	
    /**
     * Reset network.
     */
    public void reset() {
	LocationMsg msg = new LocationMsg();
	msg.setType(LocationMsg.MSG_COMMAND);
	msg.set_dest(-1);
	msg.set_cmd_no(LocationMsg.CMD_RESET);
	sendMessage(MoteIF.TOS_BCAST_ADDR, msg);
	resetCore();
	gui.update();
    }
	
    /**
     * Tally node radio strength.  This summarizes received results from testRadio.
     */
    public void statRadio(int nodeID) {
	LocationMsg msg = new LocationMsg();
	msg.setType(LocationMsg.MSG_COMMAND);
	msg.set_dest(nodeID);
	msg.set_cmd_no(LocationMsg.CMD_TEST_STAT);
	sendMessage(MoteIF.TOS_BCAST_ADDR, msg);
	gui.update();
	/*
	Node node = ensureNode(nodeID);
	for (int i=0; i<256; i++) {
	    int count = node.getPotValueCount(i);
	    if (count > 0) {
		gui.writeLog("Value " + i + " Count " + count + "\n", Color.blue);
	    }
	}
	*/
    }
	
    /**
     * Tally node radio strength.  This summarizes received results from testRadio.
     */
    public void statRadioJR(int nodeID) {
	LocationMsg msg = new LocationMsg();
	msg.setType(LocationMsg.MSG_COMMAND);
	msg.set_dest(nodeID);
	msg.set_cmd_no(LocationMsg.CMD_TEST_STAT_JR);
	sendMessage(MoteIF.TOS_BCAST_ADDR, msg);
	gui.update();
    }
	
    /**
     * Node info.
     */
    public void nodeInfo(int nodeID) {
	Iterator anchors = getAnchors();
	Node node        = ensureNode(nodeID);
	
	gui.writeLog("Node " + nodeID + " (" + node.getX() + "," + node.getY() + ") : ", Color.blue);
	while(anchors.hasNext()) {
	    Node anchor = (Node)anchors.next();
	    
	    float distance = (float)node.getDistance(anchor.getID());
	    if(distance==0) continue;
	    gui.writeLog(node.getID() + "/" + distance + " ", Color.blue);
	}
	gui.writeLog("\n", Color.blue);
	gui.update();
    }
	
    /**
     * Mote Field Info.
     */
    public void info() {
	gui.writeLog("#anchors = " + anchors.size() + "\n", Color.red);
	gui.writeLog("#nodes   = " + nodes.size() + "\n", Color.red);
	gui.writeLog("hoodSize = " + desiredNumberNeighbors + "\n", Color.red);
	gui.writeLog("scale    = " + scaleFactor + "\n", Color.red);
	gui.update();
    }
	
    /**
     * Request the distance of a node from an anchor. The correct command is
     * sent over the serial link to ask the specified node to report its
     * distance from the specifed anchor node.
     */
    public void getDistance(int nodeID, int anchorID) {
	LocationMsg msg = new LocationMsg();
	msg.setType(LocationMsg.MSG_COMMAND);
	msg.set_dest(nodeID);
	msg.set_cmd_no(LocationMsg.CMD_GET_DISTANCE);
	msg.set_args_anchor(anchorID);
	sendMessage(MoteIF.TOS_BCAST_ADDR, msg);
    }

    public void getAllDistances() {
	taskQueue.getDistances(nodes.values(), anchors.values());
    }

    public void calcError() {
	Iterator iNodes = nodes.values().iterator();
	double error = 0.0;
	while(iNodes.hasNext()) {
	    Node node = (Node)iNodes.next();
	    
	    if (!isAnchor(node)) {
		double diffX     = node.getX() - node.getGroundX();
		double locErrorX = (diffX*diffX);
		double diffY     = node.getY() - node.getGroundX();
		double locErrorY = (diffY*diffY);
		error += locErrorX + locErrorY;
	    }
	}
	gui.writeLog("Total Error " + error + "\n", Color.blue);
	gui.update();
    }

    public void pingNode(int nodeID) {
	Node firstAnchor = (Node)(getAnchors().next());
	getDistance(nodeID, firstAnchor.getID());
    }

    public void setNodePosition(Node node) {
	int id = node.getID();
	int row = id / numCols;
	int col = id % numCols;
	node.setX((double)col);
	node.setY((double)row);
    }

    public Node addKnownNode(int nodeID) {
	Node node = new Node(nodeID, 0.0, 0.0);
	nodes.put(new Integer(nodeID), node);
	setNodePosition(node);
	return node;
    }

    /**
     * Figures out the next node/anchor distance pair to request, and requests it.
     */
    public void inquireNextDistance() {
	// First, check all the nodes we currently know about.
	Iterator iNodes = nodes.values().iterator();
	while(iNodes.hasNext()) {
	    Node node = (Node)iNodes.next();
	    
	    if (!isAnchor(node)) {
		Iterator iAnchors = getAnchors();
		while(iAnchors.hasNext()) {
		    Node anchor = (Node)iAnchors.next();

		    if(!node.knowsDistanceToNode(anchor.getID())) {
			getDistance(node.getID(), anchor.getID());
			return;
		    }
		}
	    }
	}
    }
	
    /**
     * Send the specified packet over the serial link and log the event with the
     * GUI.
     */
    private void sendMessage(int bcastaddr, LocationMsg msg) {
	try {
	    commLink.send(0x7e, msg); // bcastaddr
	    gui.writeLog("Sent " + msg.toString(), Color.blue);
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
    }
	
    public void logTask(String msg)
    {
	gui.writeLog(msg + "\n", Color.orange);
	gui.update();
    }

    Node findNode (int id) {
	Node node = (Node)nodes.get(new Integer(id));
	if (node == null) 
	    return (Node)anchors.get(new Integer(id));
	else
	    return node;
    }

    Node ensureNode (int id) {
	Node node = (Node)nodes.get(new Integer(id));
	if (node == null)
	    node = addKnownNode(id);

	node.setFound(true);

	return node;
    }

    /**
     * Called by the serial link when a valid packet is received from the base
     * node. The event is logged with the GUI. If the packet is an update
     * message then the relevant anchor is updated with the new distance and the
     * updateLocation method is called. The GUI is then updated to display the
     * new rover location. If the packet is not an update message then no
     * further action is taken.
     */
    public void messageReceived(int to, net.tinyos.message.Message message) {
	
	
	SwingUtilities.invokeLater(new MessageReceivedProcessor(to, message));
    }

    public class MessageReceivedProcessor implements Runnable 
    {
	private int to;
	private net.tinyos.message.Message message;

	public MessageReceivedProcessor(int to, net.tinyos.message.Message message)
	{
	    this.to = to;
	    this.message = message;
	}
	
	public void run()
	{
	LocationMsg packet = (LocationMsg) message;

	// gui.writeLog(packet.toString(), Color.green);
	
	// Unsolicited gradient distance update from the base-station
	if((packet.amType() == LocationMsg.MSG_COMMAND) &&
	   (packet.get_cmd_no() == LocationMsg.CMD_UPDATE)) {
	    baseNode.setDistance(packet.get_args_reply_anchor(), packet.get_args_reply_distance());
	    
	    gui.writeLog("Update for anchor " + packet.get_args_reply_anchor() +
			 " distance is " + packet.get_args_reply_distance() +
			 " version  is " + packet.get_args_reply_version() + "\n", Color.orange);
	    updateLocations();
	    gui.update();
	}
	// Reply from our request to get distance information from a node.
	else if((packet.amType() == LocationMsg.MSG_COMMAND) &&
		(packet.get_cmd_no() == LocationMsg.CMD_REPLY)) {
	    Node node = ensureNode(packet.get_source());
	    node.setDistance(packet.get_args_reply_anchor(), packet.get_args_reply_distance());
	    gui.writeLog("Node " + packet.get_source() +
			 " update for anchor " + packet.get_args_reply_anchor() +
			 " distance is " + packet.get_args_reply_distance() +
			 " version  is " + packet.get_args_reply_version() + "\n", Color.orange);
	    updateLocations();
	    gui.update();

	    taskQueue.gotDistance(packet.get_source(), packet.get_args_reply_anchor());

	    // inquireNextDistance();
	}
       
	// Reply from our request to get potValue information from a node.
	else if((packet.amType() == LocationMsg.MSG_COMMAND) &&
		(packet.get_cmd_no() == LocationMsg.CMD_POT_REPLY)) {
	    Node node  = ensureNode(packet.get_source());
	    int  value = packet.get_args_pot_value();
	    gui.writeLog("Node " + packet.get_source() +
			 " pot_value " + packet.get_args_pot_value() + "\n", Color.red);
	    gui.update();
	    taskQueue.setNodeRadioReceived(node, value);
	}
       
	// Reply from our request to get potValue information from a node.
	else if((packet.amType() == LocationMsg.MSG_COMMAND) &&
		(packet.get_cmd_no() == LocationMsg.CMD_POT_MAP_REPLY)) {
	    Node node  = ensureNode(packet.get_source());
	    gui.writeLog("Node " + packet.get_source() + " got pot map\n", Color.blue);
	    gui.update();
	    taskQueue.setPotMapReceived(node, potMap);
	}
       
	// Reply from our request to get potValue information from a node.
	else if((packet.amType() == LocationMsg.MSG_COMMAND) &&
		(packet.get_cmd_no() == LocationMsg.CMD_TEST_STAT_REPLY)) {
	    int  pv      = -1;
	    Node node    = ensureNode(packet.get_source());
	    byte stats[] = packet.get_args_hoods_sizes();
	    int n        = (int)(desiredNumberNeighbors * (node.getIsEdge() ? 0.5 : 1.0));
	    node.setRadioStats(stats);
	    gui.writeLog("Node " + packet.get_source() + " totalNeighbors " + packet.get_args_hoods_total() + " radioStats ", Color.orange);
	    for (int i = 0; i < stats.length; i++) {
		if (stats[i] >= n) {
		    node.setPotValue(i);
		    pv = i;
		}
		gui.writeLog(stats[i] + "@" + i + " ", Color.orange);
	    }
	    gui.writeLog(" potValue for " + n + " neighbors = " + pv + "\n", Color.orange);
	    gui.update();

	    taskQueue.nodeCalibrationResultsReceived(node);
	}
       
	// Mote says hello!
	else if((packet.amType() == LocationMsg.MSG_COMMAND) &&
		(packet.get_cmd_no() == LocationMsg.CMD_HELLO)) {
	    Node node = ensureNode(packet.get_source());
	    node.setFound(true);
	    gui.writeLog("Node " + packet.get_source() + " found\n", Color.green);
	    getFound(packet.get_source());
	    gui.update();
	    }
       
	// Tag message 
	else if((packet.amType() == LocationMsg.MSG_COMMAND) &&
		(packet.get_cmd_no() == LocationMsg.CMD_TAG)) {
	    int  id       = packet.get_args_tag_id();
	    long time     = packet.get_args_tag_time();
	    int  strength = packet.get_sig_strength();
	    Node node     = ensureNode(packet.get_source());
	    if(!node.isRelay())
	    {
		if (!tags.containsKey(new Integer(id))) 
		    tags.put(new Integer(id),new Tag(id));
		Tag  tag  = (Tag)tags.get(new Integer(id));
		tag.addObservation(node, strength);
		gui.writeLog("Tag " + id + " node " + packet.get_source() + " time " + System.currentTimeMillis() + " strength " + strength + "\n", Color.blue);
		gui.update();
	    }
	}
	}}

    public void timerEvent () {
	// gui.writeLog("Timer\n", Color.yellow);
	// gui.update();
	
	taskQueue.processQueueTick();
    }

    public boolean isAnchor (Node node) {
	return anchors.containsKey(new Integer(node.getID()));
    }

    /**
     * Updates the location of the base node based on the distances from all the
     * anchors. An iterative gradient descent method is used to choose the
     * location that makes the distances to all the anchors as close as possible
     * to the received distances.
     */
    public void updateLocations() {
	Iterator iNodes = nodes.values().iterator();
        while(iNodes.hasNext()) {
	    Node node = (Node)iNodes.next();
	    if (!isAnchor(node))
		updateLocationForNode(node);
	}
    }

    public void updateLocationForNode(Node node) {
	Iterator anchors = getAnchors();
	Node anchor;
	double dedx = 0;
	double dedy = 0;
	double rn = 0;
	double xn = 0;
	double yn = 0;
	double xb = node.getX() / unitDistanceScaleX;
	double yb = node.getY() / unitDistanceScaleY;
	double alpha = 0.01;

	for(int i=0;i<1000;i++) {

	    while(anchors.hasNext()) {
		anchor = (Node)anchors.next();

		if(node.getDistance(anchor.getID())==0) continue;

		xn = anchor.getGroundX() / unitDistanceScaleX;
		yn = anchor.getGroundY() / unitDistanceScaleY;

		rn = (double)node.getDistance(anchor.getID());

		dedx += -4.0*((xn-xb)*(xn-xb) + (yn-yb)*(yn-yb) - rn*rn)*(xn-xb);
		dedy += -4.0*((xn-xb)*(xn-xb) + (yn-yb)*(yn-yb) - rn*rn)*(yn-yb);
	    }

	    xb += -alpha*dedx;
	    yb += -alpha*dedy;
	    dedx = 0;
	    dedy = 0;

	    anchors = getAnchors();
	}
	node.setX(xb * unitDistanceScaleX);
	node.setY(yb * unitDistanceScaleY);
    }
	
    /**
     * Returns the scale factor used to convert the radio signal distance into
     * real-world distance.
     */
    public double getScaleFactor() {
	return scaleFactor;
    }
	
    /**
     * Sets the scale factor used to convert the radio signal distance into
     * real-world distance. The updateLocation method is then called and the GUI
     * is updated to take account of the new scale factor.
     */
    public void setScaleFactor(double scaleFactor) {
	this.scaleFactor = scaleFactor;
	updateLocations();
	gui.update();
    }
	
    /**
     * Returns a string describing the location of the specified rover. If the
     * specified rover name is not the same as the name of the rover being
     * tracked, an empty string is returned. Otherwise, a string of the form
     * "X[x co-ordinate]Y[y co-ordinate]P[orientation]" is returned with the
     * current x and y co-ordinates of the base node and the orientation set to
     * 0.0 (e.g. "X0.134Y0.755P0.0").
     */
    public String getLocation(String roverName) {
	return "X" + baseNode.getX() + "Y" + baseNode.getY() + "P0.0";
    }
	
    /**
     * Returns a Node object representing the current location of the base node.
     */
    public Node getLocation() {
	return baseNode;
    }
}
