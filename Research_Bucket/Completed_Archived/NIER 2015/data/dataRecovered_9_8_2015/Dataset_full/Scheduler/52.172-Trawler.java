/*
 * Copyright (c) 2006 Moteiv Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached MOTEIV-LICENSE     
 * file. If you do not find these files, copies can be found at
 * http://www.moteiv.com/MOTEIV-LICENSE.txt and by emailing info@moteiv.com.
 */

package com.moteiv.trawler;

import java.io.*;
import java.util.*;
import java.util.prefs.*;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.AbstractButton;
import javax.swing.JPanel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JSlider;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.geom.*;
import edu.uci.ics.jung.graph.Graph;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.ArchetypeEdge;
import edu.uci.ics.jung.graph.ArchetypeGraph;
import edu.uci.ics.jung.graph.ArchetypeVertex;

import edu.uci.ics.jung.visualization.GraphDraw;
import edu.uci.ics.jung.graph.predicates.EdgePredicate;

import org.apache.commons.collections.functors.TruePredicate;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.graph.decorators.*;
import edu.uci.ics.jung.utils.UserData;
import java.awt.geom.Point2D;
import java.awt.Color;
import java.awt.event.MouseEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import com.moteiv.oscope.*;
import net.tinyos.message.MoteIF;

public class Trawler implements ActionListener {

    public static long startTime = System.currentTimeMillis();
    static Graph g;
    static Indexer indexer;
    static Timer timer;
    static MoteInterface mif;
    static LayoutMutable layout;

    public static String NODEFILE = ".trawler.net";
    public static int GROUPID = -1;

    public static final String PREF_V_SAVE = "v_save";
    public static final String PREF_V_BLINK = "v_blink";
    public static final String PREF_V_LABELS = "v_labels";
    public static final String PREF_E_LABELS = "e_labels";
    public static final String PREF_E_FILTER = "e_filter";
    public static final String PREF_V_DISPOSE = "v_dispose";
    public static final String PREF_E_DISPOSE = "e_dispose";
    
    VisualizationViewer vv; 
    PluggableRenderer pr; 

    private JMenuBar mainMenuBar = null;
    private JMenu jFileMenu = null;
    private JMenu jOptionsMenu = null;    
    private JMenuItem jMenuItemOpen = null;
    private JMenuItem jMenuItemSave = null;
    private JMenuItem jMenuItemQuit = null;
    private JMenuItem jMenuItemProperties = null;
    private JMenuItem jMenuItemControls = null;
    
    private Box controls = null; 

    private JSlider eDispose = null;
    private JSlider vDispose = null;
    private JCheckBox vLog = null;
    private JCheckBox vLabels = null;
    private JCheckBox vBlink = null;
    private JCheckBox vSave = null;
    private JCheckBox eLabels = null; 
    private JCheckBox eFilter = null; 
    private JButton graphReset = null;

    private JFrame jf = null;
    private JFrame controlBoxFrame = null;
    private GraphZoomScrollPane scrollPane = null;
    private UartDetect uartDetect = null; 
    private VertexStringer m_vs;
    private EdgeStringer m_es;
    
    private void usage() {
	System.out.println(getUsage());
	System.exit(-1);
    }

    public String getUsage() {
	return
	    "usage: java com.moteiv.trawler.Trawler [options]\n"
	    + "  options are:\n"
	    + "   -n,  --nodes=<file>  : Node location file [default = " + Trawler.NODEFILE + "]\n"
	    + "   -tg, --tosgid=<num>  : TinyOS Group ID [default = " + Trawler.GROUPID + "]\n"
	    ;
    }

    protected Box getControls() { 
	java.util.Dictionary labels;
	if (controls == null) { 
	    controls = Box.createVerticalBox();
	    JPanel runtimeControls = new JPanel(new GridLayout(0,1));
	    runtimeControls.setBorder(BorderFactory.createTitledBorder("Runtime Controls")); 
	    
	    vLog = new JCheckBox("Log packets", false);
	    vLog.addActionListener(this);
	    runtimeControls.add(vLog);
	    runtimeControls.add(new JLabel("Edge persistence (sec)", JLabel.LEFT)); 
	    eDispose = new JSlider(0,300, 10);
	    eDispose.setMajorTickSpacing(100);
	    eDispose.setMinorTickSpacing(10);
	    eDispose.setPaintTicks(true); 
	    eDispose.setPaintTrack(true);
	    //	    labels = eDispose.getLabelTable();
	    
	    eDispose.setPaintLabels(true);
	    eDispose.addChangeListener(new ChangeListener() {
		    public void stateChanged(ChangeEvent e) { 
			LinkData.setEdgeDelay((1+eDispose.getValue())*1000);
			savePrefs();
		    }
		});
	    runtimeControls.add(eDispose);
	    runtimeControls.add(new JLabel("Vertex persistence (sec)", JLabel.LEFT));
	    vDispose = new JSlider(0, 300, 30); 
	    vDispose.setMajorTickSpacing(100);
	    vDispose.setMinorTickSpacing(10);
	    vDispose.setPaintTicks(true);
	    vDispose.setPaintTrack(true);
	    vDispose.setPaintLabels(true);
	    vDispose.addChangeListener(new ChangeListener() {
		    public void stateChanged(ChangeEvent e) {
			NodeData.setNodeDelay((1+vDispose.getValue())*1000);
			savePrefs();
		    }
		});
	    runtimeControls.add(vDispose);
	    graphReset = new JButton("Reset Nodes");
	    graphReset.addActionListener(this);
	    runtimeControls.add(graphReset);
	    controls.add(runtimeControls);

	    JPanel vertexControl = new JPanel(new GridLayout(0,1)); 
	    vertexControl.setBorder(BorderFactory.createTitledBorder("Node display")); 
	    vLabels = new JCheckBox("Display vertex details", true);
	    vLabels.addActionListener(this); 

	    vertexControl.add(vLabels);
	    vBlink = new JCheckBox("Blink on incoming packets");
	    vBlink.addActionListener(this);
	    vertexControl.add(vBlink);
	    vSave = new JCheckBox("Save node locations", true); 
	    vSave.addActionListener(this); 
	    vertexControl.add(vSave); 
	    controls.add(vertexControl); 
	    JPanel edgeControl = new JPanel(new GridLayout(0,1)); 
	    edgeControl.setBorder(BorderFactory.createTitledBorder("Link diplay"));
	    eLabels = new JCheckBox("Display link quality", true);
	    eLabels.addActionListener(this);
	    edgeControl.add(eLabels);
	    eFilter = new JCheckBox("Show alternate parents", true); 
	    eFilter.addActionListener(this); 
	    edgeControl.add(eFilter); 
	    controls.add(edgeControl);
	} 
	return controls; 
    }

    private void init(String[] args) {
	for ( int i = 0; i < args.length; i++ ) {
	    if (args[i].length() > 3 && args[i].substring(0,4).equals("-tg=")) {
		Trawler.GROUPID = Integer.parseInt( args[i].substring(4,args[i].length()) );
	    }
	    else if (args[i].length() > 8 && args[i].substring(0,9).equals("--tosgid=")) {
		Trawler.GROUPID = Integer.parseInt( args[i].substring(9,args[i].length()) );
	    }
	    else if (args[i].length() > 2 && args[i].substring(0,3).equals("-n=")) {
		Trawler.NODEFILE = args[i].substring(3,args[i].length()).trim();
	    }
	    else if (args[i].length() > 7 && args[i].substring(0,8).equals("--nodes=")) {
		Trawler.NODEFILE = args[i].substring(8,args[i].length()).trim();
	    }
	    else if (args[i].equals("-h") || args[i].equals("--help"))
		usage();
	    else {
		usage();
	    }
	}
    }

    /**
     * Creates the oscilloscope panel and its associated controls and layouts
     *
     * @param m A handle to the MoteIF structure.  If non-null, the panel will
     * have an associated ScopeDriver that will make it act as a regular
     * oscilloscope connected to m
     * @param panelIdentifier a string identifying this panel.  Given a Graph
     * g, the panelIdentifier will serve as a key to retrieve the GraphPanel
     * @param start a default minimum value for the X axis
     * @param bottom a default minimum value for the Y axis
     * @param end a default maximum for the X axis
     * @param top a default maximum for the Y axis
     * @param xLabel the X-axis label
     * @param yLabel the Y-axis label
     */ 

    protected JPanel createOscopePanel (MoteIF m,
					String panelIdentifier, 
					int start,
					int bottom, 
					int end,
					int top, 
					String xLabel, 
					String yLabel) { 
 	JPanel contentPane = new JPanel(new BorderLayout());
	GraphPanel oscopePanel = new GraphPanel(start, bottom, end, top); 
	oscopePanel.setYLabel(yLabel);
	oscopePanel.setXLabel(xLabel);

	g.addUserDatum(panelIdentifier, oscopePanel,  UserData.SHARED); 
	ControlPanel controlPanel = new ControlPanel(oscopePanel);
	if (m != null) {
	    ScopeDriver driver = new ScopeDriver(m, oscopePanel);
	    controlPanel.setScopeDriver(driver);
	}
	contentPane.add("Center", oscopePanel); 
	contentPane.add("South", controlPanel); 
	return contentPane;
    }

    public Trawler(String[] args) {
	init(args);
    // Install a different look and feel; specifically, the Windows look and feel
      try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //"com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
      } catch (InstantiationException e) {
      } catch (ClassNotFoundException e) {
      } catch (UnsupportedLookAndFeelException e) {
      } catch (IllegalAccessException e) {
      }	
    jf = new JFrame("Trawler");
	jf.setJMenuBar(getMainMenuBar());

	g = new SparseGraph();

	layout = new FRLayout(g);
	indexer = Indexer.newIndexer(g,0);
	//       	layout.initialize(new Dimension(100, 100));
	mif = new MoteInterface(g, Trawler.GROUPID, layout);
	uartDetect = new UartDetect(mif.getMoteIF());
	Thread th = new Thread(uartDetect);
	th.start();
	pr = new myPluggableRenderer();
	vv = new VisualizationViewer(layout, pr); 

	vv.init();

        vv.setPickSupport(new ShapePickSupport());
	vv.setBackground(Color.white);
	vv.setToolTipListener(new NodeTips(vv));
	myVertexShapeFunction vsf = new myVertexShapeFunction(uartDetect);
	pr.setVertexShapeFunction(vsf);
	pr.setVertexIconFunction(vsf);//new myVertexShapeFunction());
	m_vs = new VertexLabel();
	java.awt.Font f = new java.awt.Font("Arial", Font.PLAIN, 12);
	pr.setEdgeFontFunction(new ConstantEdgeFontFunction(f));
	pr.setVertexStringer(m_vs);
	m_es = new myEdgeLabel();
	pr.setEdgeStringer(m_es);
	((AbstractEdgeShapeFunction)pr.getEdgeShapeFunction()).setControlOffsetIncrement(-50.f);
	//	pr.setVertexColorFunction(new myVertexColorFunction(Color.RED.darker().darker(), Color.RED, 500l));
	pr.setEdgeStrokeFunction(new EdgeWeightStrokeFunction());
	pr.setEdgeLabelClosenessFunction(new ConstantDirectionalEdgeValue(0.5,0.5));
	pr.setEdgePaintFunction(new myEdgeColorFunction());

	scrollPane = new GraphZoomScrollPane(vv);
	jf.getContentPane().setLayout(new BoxLayout(jf.getContentPane(), BoxLayout.LINE_AXIS));
	JTabbedPane pane = new JTabbedPane();
	pane.addTab("Network Topology", scrollPane);
	pane.addTab("Sensor readings", createOscopePanel(mif.getMoteIF(),
							 "ADC Readings",
							 -33, 
							 -456, 
							 300, 
							 4100, 
							 "Time (seconds)",
							 "ADC counts"
							 ));
	pane.addTab("Link Quality", createOscopePanel(mif.getMoteIF(),
							 "LinkQualityPanel",
							 -33, 
							 -15, 
							 300, 
							 135, 
							 "Time (seconds)",
							 "Link Quality Indicator"
							 ));
	ImageIcon trawlerIcon = new ImageIcon(Trawler.class.getResource("images/trawler-icon.gif"));
	Image imageTrawler = trawlerIcon.getImage();
	jf.getContentPane().add(pane);
	controlBoxFrame = new JFrame("Vizualization Control");
	controlBoxFrame.getContentPane().add(getControls());
	controlBoxFrame.setIconImage(imageTrawler);
	controlBoxFrame.pack();
	//	jf.getContentPane().add(getControls());
	jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	layout.initialize(vv.getSize());
	layout.resize(vv.getSize());
	layout.update();

	loadPrefs();
	GraphIO.loadGraph(g, layout, mif, Trawler.NODEFILE);

	// need this mouse model to keep the mouse clicks 
	// aligned with the nodes
	DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
	gm.setMode(ModalGraphMouse.Mode.PICKING);
        vv.setGraphMouse( gm );
	jf.setIconImage(imageTrawler);
	jf.pack();
	
	jf.show();
	controlBoxFrame.setVisible(true);
	timer = new Timer();
	timer.schedule(new UpdateTask(), 500, 500); 

    }

    private void processEvent(AbstractButton source) {
	if (source == vLog) {
	    if (source.isSelected()) {
		JFileChooser jfc = new JFileChooser();
		File logFile;
		int retval;
		retval = jfc.showSaveDialog(null);
		if (retval == JFileChooser.APPROVE_OPTION) { 
		    mif.setLogFile(jfc.getSelectedFile());
		} else {
		    vLog.setSelected(false);
		}
	    } else {
		mif.setLogFile(null);
	    }
	} else if (source == vLabels) {
	    if (source.isSelected()) {
		pr.setVertexStringer(m_vs);
	    } else {
		pr.setVertexStringer(new ConstantVertexStringer(null));
	    }
	} else if (source == vBlink) {
	    if (source.isSelected() ) {
		pr.setVertexColorFunction(new myVertexColorFunction(Color.RED.darker().darker(), Color.RED, 500));;
	    } else {
		pr.setVertexPaintFunction(new PickableVertexPaintFunction(pr, Color.BLACK, Color.RED, Color.ORANGE));
	    }
	} else if (source == vSave) {
	    if (source.isSelected() ) {
	    }
	    else {
	    }
	} else if (source == eLabels) {
	    if (source.isSelected()) {
		pr.setEdgeStringer(m_es);
	    } else {
		pr.setEdgeStringer(new ConstantEdgeStringer(null));
	    }
	} else if (source == eFilter) {
	    if (source.isSelected()) {
		pr.setEdgeIncludePredicate(TruePredicate.getInstance());
	    } else {
		pr.setEdgeIncludePredicate(new myEdgeFilter());
	    }
	} else if (source == graphReset) {
	    GraphIO.resetPrefs(g, layout, mif, Trawler.NODEFILE);
	}
	savePrefs();
    }

    public void actionPerformed(ActionEvent e) { 
	AbstractButton source = (AbstractButton) e.getSource();
	processEvent(source);
    }

    /**
     * This method initializes mainMenuBar	
     * 	
     * @return javax.swing.JMenuBar	
     */
    private JMenuBar getMainMenuBar() {
	if (mainMenuBar == null) {
	    mainMenuBar = new JMenuBar();
	    mainMenuBar.add(getJFileMenu());
	    mainMenuBar.add(getJOptionsMenu());
	} 
	return mainMenuBar;
    }

    /**
     * This method initializes jFileMenu	
     * 	
     * @return javax.swing.JMenu	
     */
    private JMenu getJFileMenu() {
	if (jFileMenu == null) {
	    jFileMenu = new JMenu();
	    jFileMenu.setName("File");
	    jFileMenu.setText("File");
	    //jFileMenu.add(getJMenuItemOpen());
	    jFileMenu.add(getJMenuItemQuit());
	    //jFileMenu.addSeparator();
	    //jFileMenu.add(getJMenuItemProperties());
	    //jFileMenu.addSeparator();
	    //jFileMenu.add(getJMenuItemSave());
	}
	return jFileMenu;
    }
    private JMenu getJOptionsMenu() {
	if (jOptionsMenu == null) {
	    jOptionsMenu = new JMenu();
	    jOptionsMenu.setName("Options");
	    jOptionsMenu.setText("Options");
	    //jOptionsMenu.add(getJMenuItemOpen());
	    jOptionsMenu.add(getJMenuItemControls());
	    //jOptionsMenu.addSeparator();
	    //jOptionsMenu.add(getJMenuItemProperties());
	    //jOptionsMenu.addSeparator();
	    //jOptionsMenu.add(getJMenuItemSave());
	}
	return jOptionsMenu;
    }
    /**
     * This method initializes jMenuItemOpen	
     * 	
     * @return javax.swing.JMenuItem	
     */
    private JMenuItem getJMenuItemOpen() {
	if (jMenuItemOpen == null) {
	    jMenuItemOpen = new JMenuItem();
	    jMenuItemOpen.setName("Open");
	    jMenuItemOpen.setText("Open");
	}
	return jMenuItemOpen;
    }
    private JMenuItem getJMenuItemControls() {
	if (jMenuItemControls == null) {
	    jMenuItemControls = new JMenuItem();
	    jMenuItemControls.setName("Controls");
	    jMenuItemControls.setText("Vizualization");
	    jMenuItemControls.addActionListener(new ActionListener() { 
		    public void actionPerformed(ActionEvent e) { 
			controlBoxFrame.setVisible(true);
		    }
		});
	}
	return jMenuItemControls;
    }

    /**
     * This method initializes jMenuItemSave	
     * 	
     * @return javax.swing.JMenuItem	
     */
    private JMenuItem getJMenuItemSave() {
	if (jMenuItemSave == null) {
	    jMenuItemSave = new JMenuItem();
	    jMenuItemSave.setName("Save");
	    jMenuItemSave.setText("Save");
	}
	return jMenuItemSave;
    }

    /**
     * This method initializes jMenuItemSave	
     * 	
     * @return javax.swing.JMenuItem	
     */
    private JMenuItem getJMenuItemProperties() {
	if (jMenuItemProperties == null) {
	    jMenuItemProperties = new JMenuItem();
	    jMenuItemProperties.setName("Properties");
	    jMenuItemProperties.setText("Properties");
	}
	return jMenuItemProperties;
    }
    /**
     * This method initializes jMenuItemQuit	
     * 	
     * @return javax.swing.JMenuItem	
     */
    private JMenuItem getJMenuItemQuit() {
	if (jMenuItemQuit == null) {
	    jMenuItemQuit = new JMenuItem();
	    jMenuItemQuit.setName("Quit");
	    jMenuItemQuit.setText("Quit");
	    jMenuItemQuit.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { 
		System.exit(0);
	    }
		});
	}
	return jMenuItemQuit;
    }

    public static void main(String[] args) throws IOException {
 	Trawler tg = new Trawler(args);
    }

    private void savePrefs() {
	Preferences prefs = Preferences.userNodeForPackage(com.moteiv.trawler.Trawler.class);
	prefs.putBoolean(PREF_V_SAVE, vSave.isSelected());
	prefs.putBoolean(PREF_V_BLINK, vBlink.isSelected());
	prefs.putBoolean(PREF_V_LABELS, vLabels.isSelected());
	prefs.putBoolean(PREF_E_LABELS, eLabels.isSelected());
	prefs.putBoolean(PREF_E_FILTER, eFilter.isSelected());
	prefs.putInt(PREF_V_DISPOSE, vDispose.getValue());
	prefs.putInt(PREF_E_DISPOSE, eDispose.getValue());
    }

    private void loadPrefs() {
	// extract preference data
	Preferences prefs = Preferences.userNodeForPackage(com.moteiv.trawler.Trawler.class);
	// set the data
	vSave.setSelected(prefs.getBoolean(PREF_V_SAVE, true));
	vBlink.setSelected(prefs.getBoolean(PREF_V_BLINK, false));
	vLabels.setSelected(prefs.getBoolean(PREF_V_LABELS, true));
	eLabels.setSelected(prefs.getBoolean(PREF_E_LABELS, true));
	eFilter.setSelected(prefs.getBoolean(PREF_E_FILTER, true));
	vDispose.setValue(prefs.getInt(PREF_V_DISPOSE, NodeData.getNodeDelay()/1000));
	eDispose.setValue(prefs.getInt(PREF_E_DISPOSE, LinkData.getEdgeDelay()/1000));
	// update the processing engines to reflect the new data
	processEvent(vSave);
	processEvent(vBlink);
	processEvent(vLabels);
	processEvent(eLabels);
	processEvent(eFilter);
	LinkData.setEdgeDelay(eDispose.getValue()*1000);
	NodeData.setNodeDelay(vDispose.getValue()*1000);
    }

    private void saveGraph() {
	GraphIO.saveGraph(g, layout, mif, Trawler.NODEFILE);
    }

    /** 
     * redraw routine 
     */ 

    void process() {
	synchronized(g) {
	    layout.update();
	    vv.repaint();
	    if (vSave.isSelected()) {
		saveGraph();
	    }
	}
    }

    /**
     * A timer task that will continually redraw the the screen
     */ 

    class UpdateTask extends TimerTask {
        public void run() {
            process();
        }
    }

    /** 
     * Simple extensions to the pluggable renderer:  labels are placed
     * correctly on QuadShape edges, and rather than rendering broken icons,
     * we use the vertex shape. 
     */ 

    class myPluggableRenderer extends PluggableRenderer {
	
	/**
	 * When there is no designated icon for the vertex fall through and
	 * draw a shape instead.
	 */
	public void paintIconForVertex(Graphics g, Vertex v, int x, int y) { 
	    Icon icon = vertexIconFunction.getIcon(v);
	    if((icon == null) && (g instanceof Graphics2D)) {
		Shape s = vertexShapeFunction.getShape(v);
		paintShapeForVertex((Graphics2D)g, v, AffineTransform.getTranslateInstance(x,y).createTransformedShape(s));
	    } else {
		super.paintIconForVertex(g, v, x, y);
	    }
	}
	/**
	 *  For QuadShape edges, the labels are placed in the middle of the
	 *  edge, on the outside of the curve, some effort is made to ensure
	 *  that the labels are right-side up
	 */

	protected void labelEdge(Graphics2D g2d, Edge e, String label, int x1, int x2, int y1, int y2)  
	{
	    try {
	    int distX = x2 - x1;
	    int distY = y2 - y1;
	    double totalLength = Math.sqrt(distX * distX + distY * distY);

	    double closeness = edgeLabelClosenessFunction.getNumber(e).doubleValue();

	    int posX = (int) (x1 + (closeness) * distX);
	    int posY = (int) (y1 + (closeness) * distY);

	    int xDisplacement = (int) (LABEL_OFFSET * (distY / totalLength));
	    int yDisplacement = (int) (LABEL_OFFSET * (-distX / totalLength));
        
	    Component component = prepareRenderer(graphLabelRenderer, label, true, e);
        
	    Font font = edgeFontFunction.getFont(e);
	    component.setForeground((Color)getEdgePaintFunction().getDrawPaint(e));
	    if(font != null){
		component.setFont(font);
	    }
        
	    Dimension d = component.getPreferredSize();
	    Shape edgeShape = edgeShapeFunction.getShape(e);
        
	    double parallelOffset = 1;
	    parallelOffset += parallelEdgeIndexFunction.getIndex(e);

	    if(edgeShape instanceof Ellipse2D) {
		parallelOffset += edgeShape.getBounds().getHeight();
		parallelOffset = -parallelOffset;
	    }
        
	    parallelOffset *= d.height;
        
	    AffineTransform old = g2d.getTransform();
	    AffineTransform xform = new AffineTransform(old);
	    xform.translate(posX+xDisplacement, posY+yDisplacement );
	    double dx = x2 - x1;
	    double dy = y2 - y1;
	    if(graphLabelRenderer.isRotateEdgeLabels()) {
		double theta = Math.atan2(dy, dx);
		xform.rotate(theta);
	    }
	    if (edgeShape instanceof QuadCurve2D) {
		parallelOffset += -((QuadCurve2D) edgeShape).getCtrlY()/2;
	    }
	    xform.translate(0, -parallelOffset);
	    xform.translate(-d.width/2, (d.height/2));
	    if (dx < 0){
		xform.translate(d.width/2, (d.height/2));

		xform.rotate(Math.PI);
		xform.translate(-d.width/2, -(d.height/2));
	    }
	    g2d.setTransform(xform);
	    rendererPane.paintComponent(g2d, component, screenDevice, 
					0, 0,
					d.width, d.height, true);
	    g2d.setTransform(old);
	    } catch (IllegalArgumentException iae) { 
		// can occur if the edge was removed during painting
		throw new ConcurrentModificationException(iae.toString());
	    }
	}
	
    }

    static class myVertexShapeFunction implements VertexShapeFunction,VertexIconFunction {
	protected static VertexShapeFunction evsf = new EllipseVertexShapeFunction(new ConstantVertexSizeFunction(20), new ConstantVertexAspectRatioFunction(1.0f));
	protected static ImageIcon icon; 
	protected static Shape pc; 
	protected UartDetect uartDetect;
	static {
	    icon = new ImageIcon(Trawler.class.getResource("images/base.gif"));
	    Image image = icon.getImage();
	    int w = image.getWidth(null); 
	    int h =image.getHeight(null); 
	    pc = AffineTransform.getTranslateInstance(-w/2, -h/2).createTransformedShape(FourPassImageShaper.getShape(image, 30));
	}

	public myVertexShapeFunction(UartDetect _uartDetect) {
	    uartDetect = _uartDetect;
	}

	public Icon getIcon(ArchetypeVertex v) {
 	    if (v instanceof NodeData) {
 		if (((NodeData)v).getAddress() == uartDetect.getBaseAddress()) {// UART address
 		    return icon; 
 		}
 	    } 
	    return null;
	}	    
	public Shape getShape(Vertex v) { 
	    if (v instanceof NodeData) {
		if (((NodeData)v).getAddress() == 0) {// UART address
		    return pc; 
		}
	    } 
	    return evsf.getShape(v);
	}
    }

    /**
     * A class for computing the Vertex labels.  The vertex labels are HTML
     * tags that include Node number, number of packets received (since the
     * begining of time, and number of packets lost. 
     */ 
    class VertexLabel implements VertexStringer { 
	public VertexLabel() {
	}

	public String getLabel(ArchetypeVertex av) {
	    if (av instanceof NodeData) {
		NodeData nd = (NodeData) av;
		if (nd.getAddress() == 126)
		    return null;
		return "<html><center>"+
		    "Node " + nd.getAddress() +"<br>"+
		    "Received " +nd.getNumPacketsReceived() + "<br>"+
		    "Lost " + nd.getNumPacketsLost()
		    +"</center></html>" 
		    ;
	    }
	    return null;
	}
    }

    /** 
     * A class encapsulating the edge labels.  It produces the plain text
     * labels for currently active links, and light gray labels for currently
     * inactive labels (using HTML rendering)
     */

    class myEdgeLabel implements EdgeStringer { 
	public myEdgeLabel() {
	}

	public String getLabel(ArchetypeEdge e) {
	    if (e instanceof LinkData) {
		LinkData nd = (LinkData) e;
		return Integer.toString(nd.getQuality());
		//		if (nd.getActiveStatus()) 
		//		    return (new Integer(nd.getQuality())).toString();
		//		else 
		//		    return "<html><body text=silver>"+nd.getQuality()+"</body></html>";
	    }
	    return null;
	}

	

    } 

    /**
     * A class encapsulating the different edge strokes.  Links between the
     * child and its parent are drawn with a thick continuous line, links
     * between hte child and its neighbors are drawn with a thin, dashed
     * line. 
     */

    static class EdgeWeightStrokeFunction implements EdgeStrokeFunction {
	protected final static Stroke active = new BasicStroke(2); 
	protected final static Stroke inactive = new BasicStroke(1);
	protected  static Stroke dashed;
	static {
	    float [] dashArray = new float[2];
	    dashArray[0] = 0.5f; dashArray[1] = 2.0f;
	    dashed = new BasicStroke(0.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, dashArray, 0.0f);
	}
	public Stroke getStroke(Edge e) { 
	    if (e instanceof LinkData) { 
		if (((LinkData)e).getActiveStatus()) 
		    return active; 
		else
		    return dashed;
	    }
	    return inactive;
	}
    }

    /**
     * A function for determining the color of edges.  The edges are drawn as
     * black when they are active (currently used link between the parent and
     * child) and as gray when they are inactive (a currently known link
     * between the child and members of the neighbor table)
     */
    static class myEdgeColorFunction implements EdgePaintFunction {
	public Paint getDrawPaint(Edge e) { 
	    if (e instanceof LinkData) {
		if (((LinkData) e).getActiveStatus())
		    return Color.BLACK;
		else 
		    return Color.GRAY;
	    }
	    return Color.BLACK;
	}
	public Paint getFillPaint(Edge e) {
	    return null;
	}
    }

    static class myEdgeFilter extends EdgePredicate {
	public boolean evaluateEdge(ArchetypeEdge e) {
	    if (e instanceof LinkData) {
		return ((LinkData)e).getActiveStatus();
	    } 
	    return true;
	}
    }
    
    /**
     * Class for coloring of vertices.  Implements the following policy:  if a
     * vertex is redrawn with <i>t</i> milliseconds timeout from its last
     * update, then it is drawn with a highlight color, otherwise it is drawn
     * with the standard color
     */

    static class myVertexColorFunction implements VertexColorFunction{ 
	Color regular;
	Color highlight;
	long timeout;
	myVertexColorFunction(Color r, Color h, long t) {
	    regular = r;
	    highlight = h; 
	    timeout = t; 
	}

	public Color getBackColor(Vertex v) {
	    long now = System.currentTimeMillis();
	    if (v instanceof NodeData) { 
		NodeData nd = (NodeData) v; 
		if ((now - nd.getLastHeard()) < timeout) {
		    return highlight;
		}
	    }
	    return regular;
	}

	public Color getForeColor(Vertex v) {
	    return Color.BLACK;
	}
    }
}