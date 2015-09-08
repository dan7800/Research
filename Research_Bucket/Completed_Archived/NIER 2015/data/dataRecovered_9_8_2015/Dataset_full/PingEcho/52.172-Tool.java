package net.tinyos.task.awtfield;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import net.tinyos.message.*;
import net.tinyos.packet.*;
import net.tinyos.util.*;

public class Tool implements WindowListener, ActionListener, Messenger {
    final static int SCHEMA_ERROR = 1;

    static int[] wakeupPeriod = { 2000 };
    static int[] localId = { 101 };
    static int[] moteTimeout = { 10000 };
    static int[] sendCount = { 3 };
    static int[] sendInterval = { 500 };
    static int[] maxOutput = { 100 };

    Frame f;
    Panel cmdsElement;
    List motesElement;
    TextArea outputElement;
    MyLabel cmdNameElement;
    Scrollbar scrollElement;

    MoteIF moteIF;

    MoteList motes;
    Command executer;
    OutputList outputList;
    Settings settings;

    public Tool() {
	buildWindow();

	moteIF = new MoteIF(BuildSource.makePhoenix(this));

	motes = new MoteList(this);
	executer = new Command(this);
	outputList = new OutputList(this);

	new ResetCmd(this);
	new PingCmd(this);
    }

    // Default size (chosen for a Zaurus SL-5500)
    int width = 235;
    int height = 258;

    public void start() {
	f.pack();
	f.setSize(width, height);
	f.show();
    }

    public void addCommand(String name, SimpleCommand cmd) {
	cmdsElement.add(new CmdButton(name, cmd, this));
    }

    void buildWindow() {
	try {
	    // abort out if pre 1.3
	    Class.forName("java.awt.GraphicsEnvironment");

	    /* Resize up to a 480x640 screen */
	    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	    GraphicsConfiguration gc = gd.getDefaultConfiguration();
	    Rectangle s = gc.getBounds();

	    width = s.width;
	    height = s.height;

	    if (width > 480)
		width = 480;
	    if (height > 640)
		height = 640;
	}
	catch (Exception e) { }

	f = new Frame("Field Tool");
	f.addWindowListener(this);

	// There's a top half and a bottom half...
	f.setLayout(new GridLayout(2, 1));
	Panel top = new Panel(new BorderLayout());
	Panel bottom = new Panel(new BorderLayout());
	f.add(top);
	f.add(bottom);

	// Top is a list of commands and a list of motes
	cmdsElement = new Panel(new GridLayout(0, 1));
	top.add(cmdsElement);
	motesElement = new List(5, false);
	top.add(motesElement, BorderLayout.EAST);

	// Bottom is a title and left, right buttons, and, underneath
	// a text area
	Panel bottomTitle = new Panel(new GridBagLayout());
	bottom.add(bottomTitle, BorderLayout.NORTH);
	outputElement = new TextArea("", 5, 25,
				     TextArea.SCROLLBARS_VERTICAL_ONLY);
	bottom.add(outputElement);

	// the title and buttons
	cmdNameElement = new MyLabel("NO COMMAND", Color.red);
	GridBagConstraints namec = new GridBagConstraints();
	namec.gridx = 0;
	namec.fill = GridBagConstraints.HORIZONTAL;
	namec.anchor = GridBagConstraints.WEST;
	namec.weightx = 3;
	bottomTitle.add(cmdNameElement, namec);
	GridBagConstraints scrollc = new GridBagConstraints();
	scrollc.weightx = 1;
	scrollc.gridwidth = GridBagConstraints.REMAINDER;
	scrollc.fill = GridBagConstraints.HORIZONTAL;
	scrollElement = new MyScrollbar();
	bottomTitle.add(scrollElement, scrollc);

	MenuBar mb = new MenuBar();
	f.setMenuBar(mb);
	Menu file = new Menu("File");
	mb.add(file);
	MenuItem settingsItem = new MenuItem("Settings");
	file.add(settingsItem);
	settingsItem.addActionListener(this);

	settings = new Settings(f, 5);
	settings.add("Wakeup Interval", wakeupPeriod, 100, 60000);
	settings.add("Mote Timeout", moteTimeout, 1000, 600000);
	settings.add("Local Id", localId, 0, 255);
	settings.add("Command Interval", sendInterval, 100, 10000);
	settings.add("Command Count", sendCount, 1, 100);
	settings.finishDialog();
    }

    public void actionPerformed(ActionEvent e) {
	// settings menu item
	settings.show();
    }

    public void windowClosing(WindowEvent e) {
	// System.exit is sometimes *very* slow.
	// -> Commit suicide the hard way.
	/* Disabled. Made Qtopia desktop unhappy.
	  try {
	    String[] cmd = { "bash", "-c", "kill -3 $PPID" };
	    Runtime.getRuntime().exec(cmd);
	}
	catch (IOException ee) { }*/
	    
	System.exit(0);
    }

    public void windowClosed(WindowEvent e) { }
    public void windowActivated(WindowEvent e) { }
    public void windowIconified(WindowEvent e) { }
    public void windowDeactivated(WindowEvent e) { }
    public void windowDeiconified(WindowEvent e) { }
    public void windowOpened(WindowEvent e) { }

    public void message(String s) {
	new MessageBox(f, "Warning", s);
    }
}

// A horizontal scrollbar w/ increased height
// (work around problem on Zaurus SL6000)
class MyScrollbar extends Scrollbar {
    MyScrollbar() {
	super(Scrollbar.HORIZONTAL, 0, 1, 0, 1);
    }

    public Dimension getPreferredSize() {
	Dimension d = super.getPreferredSize();

	return new Dimension(d.width, d.height + 10);
    }
    
}

class MyLabel extends Component {
    String label;
    Color color;
    final static int XOFFSET = 5;
    final static int YOFFSET = 2;

    public MyLabel(String label, Color color) {
        this.label = label;
	this.color = color;
    }
    
    public Dimension getPreferredSize() {
	Graphics g = getGraphics();

        FontMetrics fm = g.getFontMetrics();
        int width = fm.stringWidth(label);
        int height = fm.getHeight();
        
	return new Dimension(XOFFSET * 2 + width, YOFFSET * 2 + height);
    }
    
    public void paint(Graphics g) {
	super.paint(g);
	g.setColor(color);

        FontMetrics fm = g.getFontMetrics();
        int y = getBounds().height / 2 + fm.getHeight() / 2 - YOFFSET;

	g.drawString(label, XOFFSET, y);
    }
    
    public void update(Graphics g) {
        paint(g);
    }

    void setText(String s) {
	label = s;
	repaint();
    }

    void setColor(Color c) {
	color = c;
	//repaint(); 
    }
}

class CmdButton extends Button implements ActionListener {
    SimpleCommand cmd;
    Tool parent;

    CmdButton(String name, SimpleCommand cmd, Tool parent) {
	super(name);
	this.cmd = cmd;
	this.parent = parent;
	addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
	SimpleCommand copy = (SimpleCommand)cmd.clone();
	int dest = parent.motes.getMote();
	String destName = dest == MoteIF.TOS_BCAST_ADDR ? "ALL" : "" + dest;
	Output o = new Output(parent, destName + ": " + getLabel());
	copy.run(dest, o);

	parent.outputList.add(o);
    }
}
