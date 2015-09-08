package	edu.mit.mers.localization;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/**
 * The LocationGUI provides a graphical font end for the LocationCore,
 * displaying log messages and the locations of the anchors and base node. It
 * also allows the user to pass commands to the core. The update method is
 * called by the core whenever an anchor or the base node location is changed.
 * The writeLog method is called by the core whenever a packet is sent or
 * received. The GUI uses a DisplayPanel to draw the locations of the nodes.
 */
public class LocationGUI implements ActionListener {
	
    /**
     * The location core for this GUI.
     */
    private LocationCore core;
	
    /**
     * The text pane used to display the packet log.
     */
    private JTextPane packetLogPane;
    /**
     * The document associated with the packet log pane.
     */
    private StyledDocument packetLogDoc;
    /**
     * The display panel used to draw the locations of the anchors and base
     * node.
     */
    private DisplayPanel display;
	
    private JSplitPane splitPane;

    private javax.swing.Timer timer;
	
    private int delay = 1000;

    private int pingee = 0;

    private boolean isEnabled = false;
	
    /**
     * Creates and shows a new GUI, creating a core with the specified rover
     * name and a socket server listening on the specified port.
     */
    public LocationGUI(String serialForwarderAddress, String dbName) {
		
		
	// Create the frame	and	content pane.
	JFrame mainFrame = new JFrame("Localization with Mica Motes");
	JPanel mainPanel = new JPanel();
	mainPanel.setLayout(new	BorderLayout());
		
	// Add the widgets.
	addWidgets(mainPanel);
		
	// Add the panel to	the	frame.
	mainFrame.setContentPane(mainPanel);
		
	mainFrame.setLocation(40, 40);

	// Exit	when the window	is closed.
	mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	// Show	the window.
	mainFrame.pack();
	mainFrame.setVisible(true);

	splitPane.setDividerLocation(0.8);

	isEnabled = true;
	timer = new javax.swing.Timer(delay, this);
	timer.start();

	core = new LocationCore(serialForwarderAddress, dbName, this);

	display.setCore(core);

    }
	
    public void setMoteFieldInfo(int scr1x, int scr1y, double rea1x, double rea1y,
				 int scr2x, int scr2y, double rea2x, double rea2y)
    {
	display.setMoteFieldInfo(scr1x, scr1y, rea1x, rea1y,
				 scr2x, scr2y, rea2x, rea2y);
    }

    /**
     * Adds the buttons, text pane and display panel to the main panel.
     */
    private void addWidgets(JPanel mainPanel) {
		
	JButton	reset = new JButton("Reset");
	reset.addActionListener(this);
	reset.setActionCommand("reset");
		
	JButton	removeAnchor = new JButton("Del Anchor");
	removeAnchor.addActionListener(this);
	removeAnchor.setActionCommand("removeAnchor");
		
	JButton	setPot = new JButton("Set Pot");
	setPot.addActionListener(this);
	setPot.setActionCommand("setPot");
		
	JButton	getDistance = new JButton("Get Distance");
	getDistance.addActionListener(this);
	getDistance.setActionCommand("getDistance");
		
	JButton	setScaleFactor = new JButton("Set Scale");
	setScaleFactor.addActionListener(this);
	setScaleFactor.setActionCommand("setScaleFactor");

	JButton nodeExists = new JButton("Add Known");
	nodeExists.addActionListener(this);
	nodeExists.setActionCommand("addKnownNode");

	JButton setAnchor = new JButton("Set Anchor");
	setAnchor.addActionListener(this);
	setAnchor.setActionCommand("setAnchor");

	JButton inquireNext = new JButton("Inquire Next");
	inquireNext.addActionListener(this);
	inquireNext.setActionCommand("inquireNext");

	JButton setPingee = new JButton("Set Pingee");
	setPingee.addActionListener(this);
	setPingee.setActionCommand("setPingee");

	JButton doPing    = new JButton("Ping Pingee");
	doPing.addActionListener(this);
	doPing.setActionCommand("doPing");

	JButton testRadio = new JButton("Test Radio");
	testRadio.addActionListener(this);
	testRadio.setActionCommand("testRadio");

	JButton statRadio = new JButton("Stat Radio");
	statRadio.addActionListener(this);
	statRadio.setActionCommand("statRadio");

	JButton statRadioJR = new JButton("Stat Radio JR");
	statRadioJR.addActionListener(this);
	statRadioJR.setActionCommand("statRadioJR");

	JButton info = new JButton("Info");
	info.addActionListener(this);
	info.setActionCommand("info");

	JButton nodeInfo = new JButton("Node Info");
	nodeInfo.addActionListener(this);
	nodeInfo.setActionCommand("nodeInfo");

	JButton calibrateAll = new JButton("Calibrate All");
	calibrateAll.addActionListener(this);
	calibrateAll.setActionCommand("calibrateAll");

	JButton getAllDistances = new JButton("Get All Dists");
	getAllDistances.addActionListener(this);
	getAllDistances.setActionCommand("getAllDistances");

	JButton setAllPots = new JButton("Set All Radios");
	setAllPots.addActionListener(this);
	setAllPots.setActionCommand("setAllPots");

	JButton setHoodSize = new JButton("Set Hood Size");
	setHoodSize.addActionListener(this);
	setHoodSize.setActionCommand("setHoodSize");

	JButton calcError = new JButton("Calc Error");
	calcError.addActionListener(this);
	calcError.setActionCommand("calcError");

	JButton togLocDraw = new JButton("Tog Loc");
	togLocDraw.addActionListener(this);
	togLocDraw.setActionCommand("togLocDraw");

	JPanel sidePanel = new JPanel();
	sidePanel.setLayout(new GridLayout(21,1));
	sidePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
	sidePanel.add(reset);
	sidePanel.add(info);
	sidePanel.add(nodeInfo);
	sidePanel.add(setAnchor);
	sidePanel.add(removeAnchor);
	sidePanel.add(inquireNext);
	sidePanel.add(getDistance);
	sidePanel.add(testRadio);
	sidePanel.add(statRadio);
	sidePanel.add(statRadioJR);
	sidePanel.add(setPot);
	sidePanel.add(setScaleFactor);
	sidePanel.add(setPingee);
	sidePanel.add(doPing);
	sidePanel.add(nodeExists);
	sidePanel.add(calibrateAll);
	sidePanel.add(getAllDistances);
	sidePanel.add(setHoodSize);
	sidePanel.add(setAllPots);
	sidePanel.add(calcError);
	sidePanel.add(togLocDraw);

	Iterator iButtons = (Arrays.asList(sidePanel.getComponents())).iterator();
	while(iButtons.hasNext())
	{
	    JButton button = (JButton)iButtons.next();
	    button.setMargin(new Insets(0, 5, 0, 5));
	}

		
	mainPanel.add(sidePanel,BorderLayout.WEST);
	

	packetLogDoc = new DefaultStyledDocument();
	packetLogPane = new JTextPane (packetLogDoc);

	JScrollPane scrollPane = new JScrollPane(packetLogPane);
	scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	// scrollPane.setPreferredSize(new Dimension(650,250));
		
	display = new DisplayPanel(core);
	display.setPreferredSize(new Dimension(700,700));

	splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	splitPane.setLeftComponent(display);
	splitPane.setRightComponent(scrollPane);		

	mainPanel.add(splitPane,BorderLayout.CENTER);	
    }


    protected int inputNodeID(String desc) {
	String valueStr = JOptionPane.showInputDialog(null,"Please enter node",desc,JOptionPane.QUESTION_MESSAGE);
	if (valueStr == null) {
	    return -2;
	}

	int nodeID = 0;
	try {
	    nodeID = Integer.decode(valueStr).intValue();
	} catch(NumberFormatException ex) {
	    JOptionPane.showMessageDialog(null, "Please enter an integer greater than 0.",desc,JOptionPane.WARNING_MESSAGE);
	    return -2;
	}
	if (nodeID<-1) {
	    JOptionPane.showMessageDialog(null, "Please enter an integer greater than 0.",desc,JOptionPane.WARNING_MESSAGE);
	    return -2;
	}	
	return nodeID;
    }

    protected int inputHoodSize(String desc) {
	String valueStr = JOptionPane.showInputDialog(null,"Please enter hood size",desc,JOptionPane.QUESTION_MESSAGE);
	if (valueStr == null) {
	    return -2;
	}

	int hoodSize = 0;
	try {
	    hoodSize = Integer.decode(valueStr).intValue();
	} catch(NumberFormatException ex) {
	    JOptionPane.showMessageDialog(null, "Please enter an integer greater than 0.",desc,JOptionPane.WARNING_MESSAGE);
	    return -2;
	}
	if (hoodSize<-1) {
	    JOptionPane.showMessageDialog(null, "Please enter an integer greater than 0.",desc,JOptionPane.WARNING_MESSAGE);
	    return -2;
	}	
	return hoodSize;
    }

    protected int inputPotVal(String desc) {
	String valueStr = JOptionPane.showInputDialog(null,"Please enter pot value",desc,JOptionPane.QUESTION_MESSAGE);
	if (valueStr == null) {
	    return -1;
	}

	int nodeID = 0;
	try {
	    nodeID = Integer.decode(valueStr).intValue();
	} catch(NumberFormatException ex) {
	    JOptionPane.showMessageDialog(null, "Please enter an integer between 1 and 99.",desc,JOptionPane.WARNING_MESSAGE);
	    return -1;
	}
	if (nodeID<=0 || nodeID > 99) {
	    JOptionPane.showMessageDialog(null, "Please enter an integer between 1 and 99.",desc,JOptionPane.WARNING_MESSAGE);
	    return -1;
	}	
	return nodeID;
    }
	
    /** 
     * Requests any extra information required when the user presses a button,
     * then passes the command on to the core.
     */
    public void	actionPerformed(ActionEvent event) {
	if ("removeAnchor".equals(event.getActionCommand())) {
	    int nodeID = inputNodeID("Remove Anchor");
	    if(nodeID > -1)
		core.removeAnchor(nodeID);
	} else if ("setPot".equals(event.getActionCommand())) {
	    int nodeID = inputNodeID("Set Pot");
	    if(nodeID > -2)
		{
		    int potValue = inputPotVal("Set Pot");
		    if(potValue > 0)
			core.setPot(nodeID,potValue);
		}
	} else if ("getDistance".equals(event.getActionCommand())) {
	    int nodeID = inputNodeID("Source Node");
	    int anchorID = inputNodeID("Anchor ID");
	    if(nodeID > -1 && anchorID > -1)
		core.getDistance(nodeID,anchorID);
	} else if ("setPingee".equals(event.getActionCommand())) {
	    int potentialPingee = inputNodeID("Pingee");
	    if(potentialPingee > -1)
		pingee = potentialPingee;
	} else if ("reset".equals(event.getActionCommand())) {
	    core.reset();
	} else if ("doPing".equals(event.getActionCommand())) {
	    core.pingNode(pingee);
	} else if ("testRadio".equals(event.getActionCommand())) {
	    int potentialPingee = inputNodeID("Pingee");
	    if (potentialPingee > -1)
		core.testRadio(potentialPingee);
	} else if ("statRadio".equals(event.getActionCommand())) {
	    int potentialPingee = inputNodeID("Pingee");
	    if (potentialPingee > -1)
		core.statRadio(potentialPingee);
	} else if ("statRadioJR".equals(event.getActionCommand())) {
	    int potentialPingee = inputNodeID("Pingee");
	    if (potentialPingee > -1)
		core.statRadioJR(potentialPingee);
	} else if ("nodeInfo".equals(event.getActionCommand())) {
	    int potentialPingee = inputNodeID("Pingee");
	    if (potentialPingee > -2)
		core.nodeInfo(potentialPingee);
	} else if ("info".equals(event.getActionCommand())) {
	    core.info();
	} else if ("calibrateAll".equals(event.getActionCommand())) {
	    core.calibrateAll();
	} else if ("getAllDistances".equals(event.getActionCommand())) {
	    core.getAllDistances();
	} else if ("setAllPots".equals(event.getActionCommand())) {
	    core.setPots();
	} else if ("setHoodSize".equals(event.getActionCommand())) {
	    int hoodSize = inputHoodSize("");
	    if (hoodSize > 0)
		core.setHoodSize(hoodSize);
	} else if ("calcError".equals(event.getActionCommand())) {
	    core.calcError();
	} else if ("addKnownNode".equals(event.getActionCommand())) {
	    int nodeID = inputNodeID("Node ID");
	    if(nodeID > 0)
		core.addKnownNode(nodeID);
	} else if ("setAnchor".equals(event.getActionCommand())) {
	    int nodeID = inputNodeID("Node ID");
	    if(nodeID > -1)
		core.setAnchor(nodeID);
	} else if ("inquireNext".equals(event.getActionCommand())) {
	    core.inquireNextDistance();
	} else if ("togLocDraw".equals(event.getActionCommand())) {
		display.toggleLocalizationDrawing();
	} else if ("setScaleFactor".equals(event.getActionCommand())) {
	    String valueStr = (String)JOptionPane.showInputDialog(null,"Please enter scale factor","Set Scale Factor",JOptionPane.QUESTION_MESSAGE,null,null,Double.toString(core.getScaleFactor()));
	    if (valueStr == null) {
		return;
	    }
	    double scale = 0;
	    try {
		scale = Double.parseDouble(valueStr);
	    } catch(NumberFormatException ex) {
		JOptionPane.showMessageDialog(null, "Please enter a value greater than 0.","Set Scale Factor",JOptionPane.WARNING_MESSAGE);
		return;
	    }
	    if (scale<=0) {
		JOptionPane.showMessageDialog(null, "Please enter a value greater than 0.","Set Scale Factor",JOptionPane.WARNING_MESSAGE);
		return;
	    }
	    core.setScaleFactor(scale);
	} else {
	    // TODO: what's the test for a timer event?
		if(core != null)
			core.timerEvent();
	    update();
	}
    }
	
    /**
     * Writes the specified string to the log pane using the specified color.
     */
    public void writeLog(String info, Color color) {
	if (isEnabled) {
	try {
	    MutableAttributeSet attr = new SimpleAttributeSet();
	    StyleConstants.setForeground(attr, color);
	    packetLogDoc.insertString(packetLogDoc.getLength(),info,attr);
	    packetLogPane.setCaretPosition(packetLogDoc.getLength());
	} catch (BadLocationException ex) {
	    ex.printStackTrace();
	}
	}
    }
	
    /**
     * Tells the display panel to repaint itself.
     */
    public void update() {
	if (isEnabled && (display != null))
	display.repaint();
    }
	
    /**
     * Creates a new LocationGUI object with the rover name and server port
     * provided as the two command line arguments.
     */
    public static void main(String[] args) {
	/*
	if(args.length != 2) {
	    System.out.println("Usage: java edu.mit.mers.localization <roverName> <serverPort>");
	    System.exit(0);
	}

	String roverName = args[0];
	int serverPort = 0;
	try {
	    serverPort = Integer.decode(args[1]).intValue();
	} catch(NumberFormatException ex) {
	    System.out.println("Usage: java edu.mit.mers.localization <roverName> <serverPort>");
	    System.exit(0);
	}
	*/

	String serialForwarderAddress = "127.0.0.1";
	String dbName = "church";

	if(args.length >= 1)
	    serialForwarderAddress = args[0];
	if(args.length >= 2)
	    dbName = args[1];

	LocationGUI gui	= new LocationGUI(serialForwarderAddress, dbName);
    }
}
