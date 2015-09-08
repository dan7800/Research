/**
 * <p>Title: SOWN Mote Field Setup</p>
 * <p>Description: Setup GUI sor SOWN Mote Field
 * called by Xetron Cardinal MissionGUI</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: UVa</p>
 * @author Ting Yan, Tian He, etc.
 * @version 1.3
 */

import javax.swing.*; //This is the final package name.
//import com.sun.java.swing.*; //Used by JDK 1.2 Beta 4 and all
//Swing releases before Swing 1.1 Beta 3.
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.Date;
import java.util.*;

public class SOWNMoteFieldGUI implements ActionListener{

    //static int sequence = 0;
    static int fileSequence = 0;
    static JTabbedPane tp = null;
    final JLabel statusLabel = new JLabel(
        "<html><font size=+0 color=red>SOWN MOTE FIELD SETUP</font>");

    static FieldPanel panel = new FieldPanel();
    private static SerialConnector serialConnector = null;
    boolean showSystem = false;

	int north = 1;
	int east = -1;

	int refNorth = 1;
	int refEast = -1;


    // public static BufferedWriter bwout = null;

    IntegerEditor clockField;
    IntegerEditor delayField;

    IntegerEditor gridXField;
    IntegerEditor magThresholdField;
    IntegerEditor sentrySendPowerField;
    IntegerEditor powerModeField;
    IntegerEditor sDThresholdField;
    IntegerEditor pMTimeoutField;
    IntegerEditor DetectionThresholdField;
    IntegerEditor AcousticThresholdField;
    IntegerEditor PIRThresholdField;
    IntegerEditor shutdownThresholdField;
    IntegerEditor phaseDelayField;
    IntegerEditor reportPeriodField;
    IntegerEditor pmPhaseCountField;
    IntegerEditor FlowRateField;
    IntegerEditor syncDelayField;
    IntegerEditor sDBeaconCountField;

    // GPS panel
    IntegerEditor gpsRefPointLonField;
    IntegerEditor gpsRefPointLatField;
    IntegerEditor gpsSendPowerField;
    IntegerEditor gpsSendPeriodField;
    IntegerEditor upperBoundField;
    IntegerEditor deltaRSSIField;
    JComboBox deployTypeField;

	IntegerEditor latDegreeField;
	// IntegerEditor latMinuteIntField;
	DecEditor latMinuteDecField;
	
	IntegerEditor longDegreeField;
	// IntegerEditor longMinuteIntField;
	DecEditor longMinuteDecField;

	IntegerEditor refLatDegreeField;
	// IntegerEditor refLatMinuteIntField;
	DecEditor refLatMinuteDecField;
	
	IntegerEditor refLongDegreeField;
	// IntegerEditor refLongMinuteIntField;
	DecEditor refLongMinuteDecField;
	
	final JLabel latLabel = new JLabel("Latitude: ");
	final JLabel longLabel = new JLabel("Longitude: ");
	final JLabel latDegLabel = new JLabel("Deg:", SwingConstants.RIGHT);
	final JLabel longDegLabel = new JLabel("Deg:", SwingConstants.RIGHT);
	final JLabel latMinLabel = new JLabel("Min:", SwingConstants.RIGHT);
	final JLabel longMinLabel = new JLabel("Min:", SwingConstants.RIGHT);
	final JLabel latMinuteDotLabel = new JLabel(".");
	final JLabel longMinuteDotLabel = new JLabel(".");

	final JLabel refLatLabel = new JLabel("Latitude: ");
	final JLabel refLongLabel = new JLabel("Longitude: ");
	final JLabel refLatDegLabel = new JLabel("Deg:", SwingConstants.RIGHT);
	final JLabel refLongDegLabel = new JLabel("Deg:", SwingConstants.RIGHT);
	final JLabel refLatMinLabel = new JLabel("Min:", SwingConstants.RIGHT);
	final JLabel refLongMinLabel = new JLabel("Min:", SwingConstants.RIGHT);
	final JLabel refLatMinuteDotLabel = new JLabel(".");
	final JLabel refLongMinuteDotLabel = new JLabel(".");

    static JCheckBox scheduleBitCB00; // msb
    static JCheckBox scheduleBitCB01;
    static JCheckBox scheduleBitCB02;
    static JCheckBox scheduleBitCB03;
    static JCheckBox scheduleBitCB04;
    static JCheckBox scheduleBitCB05;
    static JCheckBox scheduleBitCB06;
    static JCheckBox scheduleBitCB07;
    static JCheckBox scheduleBitCB08;
    static JCheckBox scheduleBitCB09;
    static JCheckBox scheduleBitCB10;
    static JCheckBox scheduleBitCB11;
    static JCheckBox scheduleBitCB12;
    static JCheckBox scheduleBitCB13;
    static JCheckBox scheduleBitCB14;
    static JCheckBox scheduleBitCB15; // lsb

    static JCheckBox skipBitCB0;
    static JCheckBox skipBitCB1;
    static JCheckBox skipBitCB2;
    static JCheckBox skipBitCB3;
    static JCheckBox skipBitCB4;
    static JCheckBox skipBitCB5;
    static JCheckBox skipBitCB6;
    static JCheckBox skipBitCB7;
    static JCheckBox skipBitCB8;
    static JCheckBox skipBitCB9;
    static JCheckBox skipBitCB10;
    static JCheckBox skipBitCB11;
    static JCheckBox skipBitCB12;
    static JCheckBox skipBitCB13;
    static JCheckBox skipBitCB14;
    static JCheckBox skipBitCB15;

    static JCheckBox gpsMote;
    JButton gpsResetButton;

    final JButton masterClockButton = new JButton("MasterClock");
    ;
    final JButton resetValueButton = new JButton("SetParams");

    public void enableMasterClockResetButton() {
        if (masterClockButton != null) {
            masterClockButton.setEnabled(true);
        }
        if (resetValueButton != null) {
            resetValueButton.setEnabled(true);
        }
    }

    public void disableMasterClockResetButton() {
        if (masterClockButton != null) {
            masterClockButton.setEnabled(false);
        }
        if (resetValueButton != null) {
            resetValueButton.setEnabled(false);
        }
    }

    public void setParameters(byte[] bytes) {
    	if (Constants.DebugSwitch) {
	        if (bytes.length != 22) {
    	        System.err.println("Wrong configuration format!");
        	}
        }
        else {
	        if (bytes.length != 19) {
    	        System.err.println("Wrong configuration format!");
        	}
        }
      
            int gridX, magThreshold, sentrySendPower, powerMode, sDThreshold;
            int pMTimeout, DetectionThreshold, AcousticThreshold;
            int PIRThreshold, shutdownThreshold, phaseDelay, reportPeriod;
            int pmPhaseCount, phaseState, flowRate, schedule, syncDelay;
            int sDBeaconCount;

            gridX = OneByte(bytes, 1);
            magThreshold = OneByte(bytes, 9);
            sentrySendPower = OneByte(bytes, 2);
            powerMode = OneByte(bytes, 3);
            sDThreshold = OneByte(bytes, 4);
            pMTimeout = OneByte(bytes, 5);
            DetectionThreshold = OneByte(bytes, 8);
            AcousticThreshold = OneByte(bytes, 10);
            PIRThreshold = OneByte(bytes, 7);
            shutdownThreshold = OneByte(bytes, 11);
            phaseDelay = OneByte(bytes, 12);

            if (Constants.DebugSwitch) {
	            pmPhaseCount = TwoBytes(bytes, 13);
	            phaseState = TwoBytes(bytes, 15);
	            schedule = TwoBytes(bytes, 17);
	            flowRate = OneByte(bytes, 6);
	            syncDelay = OneByte(bytes, 19);
	            reportPeriod = OneByte(bytes, 20);
	            sDBeaconCount = OneByte(bytes, 21);
			}
			else {		
	            pmPhaseCount = TwoBytes(bytes, 13);
	            phaseState = TwoBytes(bytes, 15);
	            schedule = TwoBytes(bytes, 17);
	            flowRate = OneByte(bytes, 6);
			}

            gridXField.setValue(new Long(gridX));
            magThresholdField.setValue(new Long(magThreshold));
            sentrySendPowerField.setValue(new Long(sentrySendPower));
            powerModeField.setValue(new Long(powerMode));
            sDThresholdField.setValue(new Long(sDThreshold));
            pMTimeoutField.setValue(new Long(pMTimeout));
            DetectionThresholdField.setValue(new Long(DetectionThreshold));
            AcousticThresholdField.setValue(new Long(AcousticThreshold));
            PIRThresholdField.setValue(new Long(PIRThreshold));
            shutdownThresholdField.setValue(new Long(shutdownThreshold));
            phaseDelayField.setValue(new Long(phaseDelay));
            if (Constants.DebugSwitch) 
	            reportPeriodField.setValue(new Long(reportPeriod));
            pmPhaseCountField.setValue(new Long(pmPhaseCount));
            FlowRateField.setValue(new Long(flowRate));
            if (Constants.DebugSwitch) {

	            syncDelayField.setValue(new Long(syncDelay));
    	        sDBeaconCountField.setValue(new Long(sDBeaconCount));
			}
			
            // TODO schedule, phaseState

            boolean[] bSkip, bSchedule;
            bSkip = TwoBytesToBools(phaseState);
            bSchedule = TwoBytesToBools(schedule);

            scheduleBitCB15.setSelected(bSchedule[15]);
            scheduleBitCB14.setSelected(bSchedule[14]);
            scheduleBitCB13.setSelected(bSchedule[13]);
            scheduleBitCB12.setSelected(bSchedule[12]);
            scheduleBitCB11.setSelected(bSchedule[11]);
            scheduleBitCB10.setSelected(bSchedule[10]);
            scheduleBitCB09.setSelected(bSchedule[9]);
            scheduleBitCB08.setSelected(bSchedule[8]);
            scheduleBitCB07.setSelected(bSchedule[7]);
            scheduleBitCB06.setSelected(bSchedule[6]);
            scheduleBitCB05.setSelected(bSchedule[5]);
            scheduleBitCB04.setSelected(bSchedule[4]);
            scheduleBitCB03.setSelected(bSchedule[3]);
            scheduleBitCB02.setSelected(bSchedule[2]);
            scheduleBitCB01.setSelected(bSchedule[1]);
            scheduleBitCB00.setSelected(bSchedule[0]);

            skipBitCB0.setSelected(bSkip[0]);
            skipBitCB1.setSelected(bSkip[1]);
            skipBitCB2.setSelected(bSkip[2]);
            skipBitCB3.setSelected(bSkip[3]);
            skipBitCB4.setSelected(bSkip[4]);
            skipBitCB5.setSelected(bSkip[5]);
            skipBitCB6.setSelected(bSkip[6]);
            skipBitCB7.setSelected(bSkip[7]);
            skipBitCB8.setSelected(bSkip[8]);
            skipBitCB9.setSelected(bSkip[9]);
            skipBitCB10.setSelected(bSkip[10]);
            skipBitCB11.setSelected(bSkip[11]);
            skipBitCB12.setSelected(bSkip[12]);
            skipBitCB13.setSelected(bSkip[13]);
            skipBitCB14.setSelected(bSkip[14]);
            skipBitCB15.setSelected(bSkip[15]);

        

    }

    static long calculateSchedule() {
        long res = 0L;
        res += (scheduleBitCB00.isSelected() ? 1 : 0);
        res += ( (scheduleBitCB01.isSelected() ? 1 : 0) << 1);
        res += ( (scheduleBitCB02.isSelected() ? 1 : 0) << 2);
        res += ( (scheduleBitCB03.isSelected() ? 1 : 0) << 3);
        res += ( (scheduleBitCB04.isSelected() ? 1 : 0) << 4);
        res += ( (scheduleBitCB05.isSelected() ? 1 : 0) << 5);
        res += ( (scheduleBitCB06.isSelected() ? 1 : 0) << 6);
        res += ( (scheduleBitCB07.isSelected() ? 1 : 0) << 7);
        res += ( (scheduleBitCB08.isSelected() ? 1 : 0) << 8);
        res += ( (scheduleBitCB09.isSelected() ? 1 : 0) << 9);
        res += ( (scheduleBitCB10.isSelected() ? 1 : 0) << 10);
        res += ( (scheduleBitCB11.isSelected() ? 1 : 0) << 11);
        res += ( (scheduleBitCB12.isSelected() ? 1 : 0) << 12);
        res += ( (scheduleBitCB13.isSelected() ? 1 : 0) << 13);
        res += ( (scheduleBitCB14.isSelected() ? 1 : 0) << 14);
        res += ( (scheduleBitCB15.isSelected() ? 1 : 0) << 15);
        return res;
    }

    static long calculateSkip() {
        long res = 0L;
        res += (skipBitCB0.isSelected() ? 1 : 0);
        res += ( (skipBitCB1.isSelected() ? 1 : 0) << 1);
        res += ( (skipBitCB2.isSelected() ? 1 : 0) << 2);
        res += ( (skipBitCB3.isSelected() ? 1 : 0) << 3);
        res += ( (skipBitCB4.isSelected() ? 1 : 0) << 4);
        res += ( (skipBitCB5.isSelected() ? 1 : 0) << 5);
        res += ( (skipBitCB6.isSelected() ? 1 : 0) << 6);
        res += ( (skipBitCB7.isSelected() ? 1 : 0) << 7);
        res += ( (skipBitCB8.isSelected() ? 1 : 0) << 8);
        res += ( (skipBitCB9.isSelected() ? 1 : 0) << 9);
        res += ( (skipBitCB10.isSelected() ? 1 : 0) << 10);
        res += ( (skipBitCB11.isSelected() ? 1 : 0) << 11);
        res += ( (skipBitCB12.isSelected() ? 1 : 0) << 12);
        res += ( (skipBitCB13.isSelected() ? 1 : 0) << 13);
        res += ( (skipBitCB14.isSelected() ? 1 : 0) << 14);
        res += ( (skipBitCB15.isSelected() ? 1 : 0) << 15);
        System.out.println("settingBits is : " + res);
        return res;
    }

    static int eventTypeValue = 0;
    static int attributeTypeValue = 1;
    static int paramTypeValue = 1;

    public void resetAll() {
        // numStreams = 0;
        panel.clear();
    }

    private static String xSliderLabelPrefix = "size ";

    // private static String ySliderLabelPrefix = "ySec ";

    private static String logfile = "log.txt";

    final JLabel dynamicLabel = new JLabel("     ");
    final JLabel nodesLabel = new JLabel("<html><font face=\"Courier New\" size=-1 color=green>--- nodes ---% sentries</font>");
    final JLabel detDelayLabel = new JLabel("<html><font face=\"Courier New\" size=-1 color=green> EID -- DetDelay ---</font>");
    final JLabel clsDelayLabel = new JLabel("<html><font face=\"Courier New\" size=-1 color=green> EID -- ClsDelay ---</font>");
    final JLabel velDelayLabel = new JLabel("<html><font face=\"Courier New\" size=-1 color=green> EID -- VelDelay ---</font>");
    

    final JSlider xSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 1);
    final JLabel xSliderLabel = new JLabel(xSliderLabelPrefix + "1");

    // final JSlider ySlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 1);
    // final JLabel ySliderLabel = new JLabel(ySliderLabelPrefix + "1");

    private static JFrame frame;

    public static void startSerialConnector(SOWNMoteFieldGUI app, boolean replayMode, boolean mica2dot) {
        serialConnector = new SerialConnector(app, panel, replayMode, mica2dot);
    }

    public SOWNMoteFieldGUI() {
        panel.setStatusLabel(statusLabel);
        panel.setDynamicLabel(dynamicLabel);
		panel.setOtherLabels(nodesLabel, detDelayLabel, clsDelayLabel, velDelayLabel);
    }

    static FileInputStream fis = null;
    static FileOutputStream fos = null;
    static File configFile = null;

    public static void main(String[] args) {
        /* if (args.length != 0 && args.length != 1) {
          System.err.println("No or only oneparameter allowed!");
          System.exit(0);
             }

             if (args.length == 1) {
         logfile = args[0];
             }

         try {
          bwout = new BufferedWriter(new FileWriter(logfile), 1024);
         } catch (IOException e) {
          e.printStackTrace();
         }*/
		boolean replayMode = false;
		boolean mica2dot = false;
    	if (args.length == 0) {
    		replayMode = false;
    		mica2dot = false;
    	}
    	else if (args.length == 1 && (args[0].equals("replay")|| args[0].equals("mica2dot"))) {
    		if (args[0].equals("replay"))
    			replayMode = true;
    		if (args[0].equals("mica2dot"))
    			mica2dot = true;
    	}
    	else {
            System.err.println("Usage: java SOWNMoteFieldGUI [replay|mica2dot]");
            System.exit(1);
    	}
    	
    	

        configFile = new File("log.txt");

        try {
            UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch (Exception e) {

        }

		// JFrame.setDefaultLookAndFeelDecorated(false);


        frame = new JFrame("SOWN Mote Field Setup - Version 1.3");
        SOWNMoteFieldGUI app = new SOWNMoteFieldGUI();

        tp = app.createComponents(frame);
        Component contents = tp;

        frame.getContentPane().add(contents, BorderLayout.CENTER);

        frame.setSize(800, 570);//(1024, 768); 

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        frame.setVisible(true);
        frame.setResizable(true);

        startSerialConnector(app, replayMode, mica2dot);

    }

    public void paintComponent(Graphics g) {

    }

    /**
     * Return the value (as a int) of the field 'addr'
     */
    static long resetSequence = 0;
    static byte paramSequence = 0;
    static void sendMasterClockCommand(int clock, int delay, int latitude, int longitude) {
        byte[] b = new byte[18];
        for (int i = 0; i < 18; i++) {
            b[i] = 0;
        }

        MyMessage m = new MyMessage(b);
        m.amTypeSet(Constants.AM_TYPE);
        b[0] = 3;
        b[1] = 2;
        m.setUIElement(16, 8, resetSequence);
        b[3] = 4;
        m.setUIElement(32, 32, (long) clock);
        m.setUIElement(64, 16, (long) delay);
        m.setUIElement(80, 32, (long) latitude);
        m.setUIElement(112, 32, (long) longitude);
        serialConnector.sendShort( (byte) - 1, (byte) - 1, (byte) 55,
                                  (byte) 125,
                                  (byte) 18,
                                  m.dataGet());

    }

    static void sendReferenceCommand(int latitude, int longitude) {
        byte[] b = new byte[20];
        for (int i = 0; i < 20; i++) {
            b[i] = 0;
        }

        MyMessage m = new MyMessage(b);
        m.amTypeSet(Constants.AM_TYPE);
        b[0] = 3;
        b[1] = 2;
        m.setUIElement(16, 8, resetSequence);
        b[3] = 3;
        m.setUIElement(32, 32, (long) latitude);
        m.setUIElement(64, 32, (long) longitude);
        m.setUIElement(96, 32, (long) latitude);
        m.setUIElement(128, 32, (long) longitude);
        serialConnector.sendRef( (byte) - 1, (byte) - 1, (byte) 55,
                                  (byte) 125,
                                  (byte) 20,
                                  m.dataGet());

    }

    static void sendPing() {
        byte[] b = new byte[3];
        for (int i = 0; i < 3; i++) {
            b[i] = 0;
        }

        MyMessage m = new MyMessage(b);
        m.amTypeSet(Constants.AM_TYPE);
        b[0] = 3;
        b[1] = 4;
        m.setUIElement(16, 8, resetSequence);  
        serialConnector.sendPing( (byte) - 1, (byte) - 1, (byte) 55,
                                  (byte) 125,
                                  (byte) 3,
                                  m.dataGet());

   }
   
    static void sendRequestCommand(int clock,
                                   int sequence, int eventType,
                                   int attributeType,
                                   int confidence, int delay, int periodicity) {
        /* byte[] b = new byte[Constants.sendPacketSize
         - Constants.macHeaderSize];
         for (int i = 0; i < Constants.sendPacketSize -
         Constants.macHeaderSize; i++) {
          b[i] = 0;
             }
             b[1] = 1;
             MyMessage m = new MyMessage(b);
             m.amTypeSet(Constants.AM_TYPE);
             m.setUIElement(56, 16, (long) sourceID);
             m.setUIElement(72, 16, (long) sequence);
             m.setUIElement(88, 8, (long) eventType);
             m.setUIElement(96, 16, (long) attributeType);
             m.setUIElement(112, 16, (long) confidence);
             m.setUIElement(128, 16, (long) delay);
             m.setUIElement(144, 32, (long) periodicity);
             ct.send(Constants.TOS_BCAST_ADDR, m);*/
        // System.out.println(calculateSchedule());
        // System.out.println(calculateSkip());
    }

    static void sendGpsCmd(int type, int lat, int lon, int power,
                           int period) {
        byte[] b = new byte[29];
        for (int i = 0; i < 29; i++) {
            b[i] = 0;
        }

        MyMessage m = new MyMessage(b);

        if (gpsMote.isSelected()) {
            m.amTypeSet(Constants.GPS_AM_TYPE);
            m.setUIElement(0, 16, 0);
            m.setUIElement(16, 8, type);
            if (type == 1) {
                m.setUIElement(24, 32, lat);
                m.setUIElement(56, 32, lon);
                m.setUIElement(88, 8, power);
                m.setUIElement(96, 16, period);
            }
        }
        else {
            m.amTypeSet(Constants.AM_TYPE);
            m.setUIElement(0, 8, 3); // flag
            m.setUIElement(8, 8, 2); // recordType
            m.setUIElement(16, 8, 0); // messageID
            m.setUIElement(24, 8, 3); // parameterCode
            m.setUIElement(32, 32, lat); // latitude
            m.setUIElement(64, 32, lon); // longitude
        }

        serialConnector.sendMessage( (byte) - 1, (byte) - 1,
                                    (byte) m.amType(),
                                    (byte) 125, (byte) 29,
                                    m.dataGet());

    }

    static void sendConfigCmd(int type, int deployType, int upperBound,
                              int deltaRSSI) {
        byte[] b = new byte[29];
        for (int i = 0; i < 29; i++) {
            b[i] = 0;
        }

        MyMessage m = new MyMessage(b);

        m.amTypeSet(Constants.GPS_AM_TYPE);
        m.setUIElement(0, 16, 0); // sender
        m.setUIElement(16, 8, type); // message type
        if (type == 11) {
            m.setUIElement(24, 8, deployType);
            m.setUIElement(32, 16, upperBound);
            m.setUIElement(48, 16, deltaRSSI);
        }

        serialConnector.sendMessage( (byte) - 1, (byte) - 1,
                                    (byte) m.amType(),
                                    (byte) 125, (byte) 29,
                                    m.dataGet());

    }

    static byte LongToFirstByte(long longVal) {
        longVal &= 0x00000000000000ffL;
        if (longVal > 127) {
            longVal -= 256;
        }
        return (byte) longVal;
    }

    static byte LongToSecondByte(long longVal) {
        longVal &= 0x000000000000ff00L;
        longVal = longVal >> 8;
        if (longVal > 127) {
            longVal -= 256;
        }
        return (byte) longVal;
    }

    static int OneByte(byte[] packet, int offset) {
        int p = (int) packet[offset];
        return ( (p >= 0) ? p : (p + 256));
    }

    // get the unsigned int value from two bytes at certain offset
    static int TwoBytes(byte[] packet, int offset) {
        int p0 = (int) packet[offset];
        int p1 = (int) packet[offset + 1];
        p0 = (p0 >= 0) ? p0 : (p0 + 256);
        p1 = (p1 >= 0) ? p1 : (p1 + 256);
        return (p0 + p1 * 256);
    }

    static boolean[] OneByteToBools(int oneByte) {
        // msb res[7]
        int i = 0;
        int mask = 0x00000001;
        boolean[] res = new boolean[8];
        res[0] = ! ( (oneByte & mask) == 0);
        for (i = 1; i < 8; i++) {
            mask = mask << 1;
            res[i] = ! ( (oneByte & mask) == 0);
        }

        return res;
    }

    static boolean[] TwoBytesToBools(int twoBytes) {
        // msb res[15]
        int i = 0;
        int mask = 0x00000001;
        boolean[] res = new boolean[16];
        res[0] = ! ( (twoBytes & mask) == 0);
        for (i = 1; i < 16; i++) {
            mask = mask << 1;
            res[i] = ! ( (twoBytes & mask) == 0);
        }

        return res;
    }

    static void sendResetCommandDebug(long gridX, long magThreshold,
                                 long sentry_Send_Power,
                                 long power_Mode, long sDThreshold,
                                 long pMTimeout,
                                 long DetectionThreshold, long AcousticThreshold,
                                 long PIR_Threshold,
                                 long shutdownThreshold, long phase_Delay, long report_Period,
                                 long pm_Phase_Count, long phase_State,
                                 long flowRate, long schedule, long syncDelay,
                                 long sDBeaconCount) {
        byte[] b = new byte[29];
        for (int i = 0; i < 29; i++) {
            b[i] = 0;
        }
        b[0] = 3;
        b[1] = 2;
        MyMessage m = new MyMessage(b);
        m.amTypeSet(Constants.AM_TYPE);

        m.setUIElement(16, 8, resetSequence);
        resetSequence++;
        m.setUIElement(24, 8, 1L); // fixed for INITIALIZATION now
        m.setUIElement(32, 8, (long) gridX);
        m.setUIElement(40, 8, (long) sentry_Send_Power);
        m.setUIElement(48, 8, (long) power_Mode);
        m.setUIElement(56, 8, (long) sDThreshold);
        m.setUIElement(64, 8, (long) pMTimeout);
        m.setUIElement(72, 8, (long) flowRate);
        m.setUIElement(80, 8, (long) PIR_Threshold);
		m.setUIElement(88, 8, (long) DetectionThreshold);
        m.setUIElement(96, 8, (long) magThreshold);
        m.setUIElement(104, 8, (long) AcousticThreshold);
        m.setUIElement(112, 8, (long) shutdownThreshold);
        m.setUIElement(120, 8, (long) phase_Delay);

        m.setUIElement(128, 16, (long) pm_Phase_Count);
        // System.out.println(pm_Phase_Count);
        m.setUIElement(144, 16, (long) phase_State);
        m.setUIElement(160, 16, (long) schedule);
        m.setUIElement(176, 8, (long) syncDelay);
        m.setUIElement(184, 8, (long) report_Period);
        m.setUIElement(192, 8, (long) sDBeaconCount);
        System.out.println("SDCount it  " + sDBeaconCount);
        /*m.setUIElement(56, 16, (long) sourceID);
             m.setUIElement(72, 16, (long) sequence);
             m.setUIElement(88, 16, (long) paramType);
             m.setUIElement(104, 32, (long) paramType);*/
        serialConnector.sendMessage( (byte) - 1, (byte) - 1, (byte) 55,
                                    (byte) 125,
                                    (byte) 25,
                                    m.dataGet());
    }

    static void sendResetCommand(long gridX, long magThreshold,
                                 long sentry_Send_Power,
                                 long power_Mode, long sDThreshold,
                                 long pMTimeout,
                                 long DetectionThreshold, long AcousticThreshold,
                                 long PIR_Threshold,
                                 long shutdownThreshold, long phase_Delay,
                                 long pm_Phase_Count, long phase_State,
                                 long flowRate, long schedule) {
        byte[] b = new byte[26];
        for (int i = 0; i < 26; i++) {
            b[i] = 0;
        }
        b[0] = 3;
        b[1] = 2;
        MyMessage m = new MyMessage(b);
        m.amTypeSet(Constants.AM_TYPE);

        m.setUIElement(16, 8, resetSequence);
        resetSequence++;
        m.setUIElement(24, 8, 1L); // fixed for INITIALIZATION now
        m.setUIElement(32, 8, (long) gridX);
        m.setUIElement(40, 8, (long) sentry_Send_Power);
        m.setUIElement(48, 8, (long) power_Mode);
        m.setUIElement(56, 8, (long) sDThreshold);
        m.setUIElement(64, 8, (long) pMTimeout);
        m.setUIElement(72, 8, (long) flowRate);
        m.setUIElement(80, 8, (long) PIR_Threshold);
		m.setUIElement(88, 8, (long) DetectionThreshold);
        m.setUIElement(96, 8, (long) magThreshold);
        m.setUIElement(104, 8, (long) AcousticThreshold);
        m.setUIElement(112, 8, (long) shutdownThreshold);
        m.setUIElement(120, 8, (long) phase_Delay);

        m.setUIElement(128, 16, (long) pm_Phase_Count);
        // System.out.println(pm_Phase_Count);
        m.setUIElement(144, 16, (long) phase_State);
        m.setUIElement(160, 16, (long) schedule);
        // System.out.println("SDCount it  " + sDBeaconCount);
        /*m.setUIElement(56, 16, (long) sourceID);
             m.setUIElement(72, 16, (long) sequence);
             m.setUIElement(88, 16, (long) paramType);
             m.setUIElement(104, 32, (long) paramType);*/
        serialConnector.sendMessage( (byte) - 1, (byte) - 1, (byte) 55,
                                    (byte) 125,
                                    (byte) 22,
                                    m.dataGet());
    }


    public JTabbedPane createComponents(final JFrame frame) {

        new Thread(panel).start();

		final JLabel timeLabel = new JLabel("--:--:--");

		
		java.util.Timer timer = new java.util.Timer();
		timer.scheduleAtFixedRate(new TimerTask() {public void run() {
			Date currDate = new Date();
			String dStr = currDate.toString();
			
			timeLabel.setText(dStr.substring(11, 19));
			}}, 500, 500);
		

        final JTabbedPane tabbedPane = new JTabbedPane();
        final JPanel pane = new JPanel();
        final JPanel pane2 = new JPanel();
        final JPanel pane3 = new JPanel();

        final JPanel subPanel1 = new JPanel();
        final JPanel subPanel2 = new JPanel();
        final JPanel subPanel3 = new JPanel();
        final JPanel subPanel4 = new JPanel();
		final JPanel subPanel5 = new JPanel();
		final JPanel subPanel6 = new JPanel();
		
        tabbedPane.addTab("Monitor", pane);
        tabbedPane.addTab("Command", pane2);
        tabbedPane.addTab("GPS Init", pane3);

        Insets insets = pane.getInsets();
        Insets insets2 = pane2.getInsets();
        Insets insets3 = pane3.getInsets();

        pane.setLayout(new GridBagLayout());

        pane2.setLayout(new GridBagLayout());
        GridBagConstraints c2 = new GridBagConstraints();
        c2.fill = GridBagConstraints.BOTH;
        
        // subPanel4.setBounds(5 + insets2.left, 5 + insets2.top, 1000, 250);
        subPanel4.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Parameters"));
        c2.weightx = 0.8;
        c2.weighty = 0.8;
        // c2.ipady = 120;
        c2.gridx = 0;
        c2.gridy = 0;
        pane2.add(subPanel4, c2);
        subPanel4.setLayout(new GridBagLayout());
        Insets insetsSub4 = subPanel4.getInsets();

        // subPanel3.setBounds(5 + insets2.left, 265 + insets2.top, 1000, 100);
        subPanel3.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Current Tripwire Schedule"));
        // c2.ipady = 0;
        c2.gridx = 0;
        c2.gridy = 1;
        pane2.add(subPanel3, c2);
        subPanel3.setLayout(new GridBagLayout());
        Insets insetsSub3 = subPanel3.getInsets();

        // subPanel2.setBounds(5 + insets2.left, 370 + insets2.top, 1000, 165);
        
        subPanel2.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Options"));
        // c2.ipady = 0;
        c2.gridx = 0;
        c2.gridy = 2;
        pane2.add(subPanel2, c2);
        subPanel2.setLayout(new GridBagLayout());
        Insets insetsSub2 = subPanel2.getInsets();


        // subPanel1.setBounds(5 + insets2.left, 540 + insets2.top, 1000, 90);
        subPanel1.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "System Control"));
        // c2.ipady=10;
        c2.gridx = 0;
        c2.gridy = 3;
        pane2.add(subPanel1, c2);
        subPanel1.setLayout(new GridBagLayout());
        Insets insetsSub1 = subPanel1.getInsets();



        pane3.setLayout(new GridBagLayout());
        GridBagConstraints c3 = new GridBagConstraints();
        c3.fill = GridBagConstraints.BOTH;
		
		c3.weightx = 0.5;
		c3.weighty = 0.5;
		// subPanel5.setBounds(35 + insets3.left, 5 + insets3.top, 375, 240);
		subPanel5.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Reference Point"));
        c3.gridx = 0;
        c3.gridy = 0;
		pane3.add(subPanel5, c3);
        subPanel5.setLayout(new GridBagLayout());
        Insets insetsSub5 = subPanel5.getInsets();

		c3.gridx = 1;
		c3.gridy = 0;
		pane3.add(subPanel6, c3);
		// subPanel6.setBounds(545 + insets3.left, 5 + insets3.top, 375, 240);
		subPanel6.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Sensor Mote Configuration"));
        subPanel6.setLayout(new GridBagLayout());
        Insets insetsSub6 = subPanel6.getInsets();


		final JPanel emptyPanel = new JPanel();
		c3.gridx = 0;
		c3.gridy = 1;
		c3.gridwidth = 2;
		pane3.add(emptyPanel, c3);

        panel.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                panel.clickShow(e.getX(), e.getY());

            }

            public void mousePressed(MouseEvent e) {}

            public void mouseReleased(MouseEvent e) {}

            public void mouseEntered(MouseEvent e) {}

            public void mouseExited(MouseEvent e) {}

        });


        // pane.add(panel);
        // panel.setBounds(10 + insets.left, 5 + insets.top, 720, 370);

        /* final JButton globalButton = new JButton("View Sections");
             globalButton.setMnemonic(KeyEvent.VK_V);
             globalButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (panel.global) {
              panel.global = false;
              globalButton.setText("View Global");
              panel.repaint();
            }
            else {
              panel.global = true;
              globalButton.setText("View Sections");
              panel.repaint();
            }
          }
             });*/

        final JButton serialButton = new JButton("Stop Serial");
        serialButton.setMnemonic(KeyEvent.VK_T);
        serialButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (panel.serialStarted) {
                    panel.serialStarted = false;
                    serialButton.setText("Start Serial");
                    serialConnector.disconnectSerial();
                }
                else {
                    panel.serialStarted = true;
                    serialButton.setText("Stop Serial");
                    serialConnector.connectSerial();
                }
                panel.repaint();
            }
        });

        final JButton resetButton = new JButton("Clear");
        resetButton.setMnemonic(KeyEvent.VK_A);
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetAll();
                panel.clearDynamic();
                panel.statusHtmlTextArea.setText(
                 "<html><font size=+0 color=red>SOWN MOTE FIELD SETUP</font>");
                statusLabel.setText(panel.statusHtmlTextArea.getText());
                panel.repaint();
            }
        });

        final JButton systemButton = new JButton("Enable System Parameters");
        systemButton.setMnemonic(KeyEvent.VK_Y);

        systemButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!showSystem) {
                    showSystem = true;
                    magThresholdField.setEnabled(true);
                    sentrySendPowerField.setEnabled(true);
                    powerModeField.setEnabled(true);
                    sDThresholdField.setEnabled(true);
                    pMTimeoutField.setEnabled(true);
                    DetectionThresholdField.setEnabled(true);
                    AcousticThresholdField.setEnabled(true);
                    PIRThresholdField.setEnabled(true);
                    shutdownThresholdField.setEnabled(true);
                    reportPeriodField.setEnabled(true);
                    FlowRateField.setEnabled(true);
                    syncDelayField.setEnabled(true);
                    sDBeaconCountField.setEnabled(true);

                    systemButton.setText("Disable System Parameters");
                }
                else {
                    showSystem = false;
                    magThresholdField.setEnabled(false);
                    sentrySendPowerField.setEnabled(false);
                    powerModeField.setEnabled(false);
                    sDThresholdField.setEnabled(false);
                    pMTimeoutField.setEnabled(false);
                    DetectionThresholdField.setEnabled(false);
                    AcousticThresholdField.setEnabled(false);
                    PIRThresholdField.setEnabled(false);
                    shutdownThresholdField.setEnabled(false);
                    reportPeriodField.setEnabled(false);
                    FlowRateField.setEnabled(false);
                    syncDelayField.setEnabled(false);
                    sDBeaconCountField.setEnabled(false);
                    systemButton.setText("Enable System Parameters");
                }
            }
        });

		final JButton defaultButton = new JButton("Return to Default Values");
		defaultButton.setMnemonic(KeyEvent.VK_D);
		defaultButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	


                    gridXField.setValue(new Long(Constants.Default_GridX));
                    phaseDelayField.setValue(new Long(Constants.Default_PhaseDelay));
                    pmPhaseCountField.setValue(new Long(Constants.Default_PMPhaseCount));
                    magThresholdField.setValue(new Long(Constants.Default_MagThreshold));
                    sentrySendPowerField.setValue(new Long(Constants.Default_SentrySendPower));
                    powerModeField.setValue(new Long(Constants.Default_PowerMode));
                    sDThresholdField.setValue(new Long(Constants.Default_SDThreshold));
                    pMTimeoutField.setValue(new Long(Constants.Default_PMTimeout));
                    DetectionThresholdField.setValue(new Long(Constants.Default_DetectionThreshold));
                    AcousticThresholdField.setValue(new Long(Constants.Default_AcousticThreshold));
                    PIRThresholdField.setValue(new Long(Constants.Default_PIRThreshold));
                    shutdownThresholdField.setValue(new Long(Constants.Default_ShutdownThreshold));
                    reportPeriodField.setValue(new Long(Constants.Default_ReportPeriod));
                    FlowRateField.setValue(new Long(Constants.Default_FlowRate));
                    syncDelayField.setValue(new Long(Constants.Default_SyncDelay));
                    sDBeaconCountField.setValue(new Long(Constants.Default_SDBeaconCount));

					boolean[] skipBool = new boolean[16];
					skipBool = TwoBytesToBools(Constants.Default_SkipBits);
					boolean[] scheduleBool = new boolean[16];
					scheduleBool = TwoBytesToBools(Constants.Default_ScheduleBits);

					scheduleBitCB00.setSelected(scheduleBool[0]);
					scheduleBitCB01.setSelected(scheduleBool[1]);
					scheduleBitCB02.setSelected(scheduleBool[2]);
					scheduleBitCB03.setSelected(scheduleBool[3]);
					scheduleBitCB04.setSelected(scheduleBool[4]);
					scheduleBitCB05.setSelected(scheduleBool[5]);
					scheduleBitCB06.setSelected(scheduleBool[6]);
					scheduleBitCB07.setSelected(scheduleBool[7]);
					scheduleBitCB08.setSelected(scheduleBool[8]);
					scheduleBitCB09.setSelected(scheduleBool[9]);
					scheduleBitCB10.setSelected(scheduleBool[10]);
					scheduleBitCB11.setSelected(scheduleBool[11]);
					scheduleBitCB12.setSelected(scheduleBool[12]);
					scheduleBitCB13.setSelected(scheduleBool[13]);
					scheduleBitCB14.setSelected(scheduleBool[14]);
					scheduleBitCB15.setSelected(scheduleBool[15]);

					skipBitCB0.setSelected(skipBool[0]);
					skipBitCB1.setSelected(skipBool[1]);
					skipBitCB2.setSelected(skipBool[2]);
					skipBitCB3.setSelected(skipBool[3]);
					skipBitCB4.setSelected(skipBool[4]);
					skipBitCB5.setSelected(skipBool[5]);
					skipBitCB6.setSelected(skipBool[6]);
					skipBitCB7.setSelected(skipBool[7]);
					skipBitCB8.setSelected(skipBool[8]);
					skipBitCB9.setSelected(skipBool[9]);
					skipBitCB10.setSelected(skipBool[10]);
					skipBitCB11.setSelected(skipBool[11]);
					skipBitCB12.setSelected(skipBool[12]);
					skipBitCB13.setSelected(skipBool[13]);
					skipBitCB14.setSelected(skipBool[14]);
					skipBitCB15.setSelected(skipBool[15]);
					
            }
        });
		
		
        final JButton showButton = new JButton("NoRoute");
        showButton.setMnemonic(KeyEvent.VK_R);
        showButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (panel.showSpan) {
                    panel.showSpan = false;
                    showButton.setText("NoRoute");
                }
                else {
                    panel.showSpan = true;
                    showButton.setText("Route");
                }
                panel.repaint();
            }
        });

        final JButton filterButton = new JButton("NoFilter");
        filterButton.setMnemonic(KeyEvent.VK_F);
        filterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (panel.filter) {
                    panel.filter = false;
                    filterButton.setText("NoFilter");
                }
                else {
                    panel.filter = true;
                    filterButton.setText("Filter");
                }
                panel.repaint();
            }
        });

        final JButton recordButton = new JButton("LogData");
        recordButton.setMnemonic(KeyEvent.VK_C);
        recordButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String fileName = configFile.getName() + "." + (fileSequence++);
                panel.recordHopCounts(fileName);
            }
        });

        final JButton loadButton = new JButton("Load");
        loadButton.setMnemonic(KeyEvent.VK_L);
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (configFile == null) {
                    return;
                }
                try {
                    fis = new FileInputStream(configFile);
                }
                catch (FileNotFoundException fnfe) {
                    System.err.println("file not find");
                    return;
                }
                long length = configFile.length();
                if (length != 22) {
                    System.err.println("wrong file size");
                    return;
                }

                byte[] bytes = new byte[22];
                int offset = 0, numRead = 0;
                try {
                    while (offset < bytes.length
                           &&
                           (numRead = fis.read(bytes, offset,
                                               bytes.length - offset)) >=
                           0) {
                        offset += numRead;
                    }

                    // Ensure all the bytes have been read in
                    if (offset < bytes.length) {
                        throw new IOException
                            ("Could not completely read file " +
                             configFile.getName());
                    }
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }

                setParameters(bytes);

                if (fis != null) {
                    try {
                        fis.close();
                    }
                    catch (IOException ioe) {
                    }
                }
            }

        });
        // subPanel1.add(loadButton);
        loadButton.setBounds(5 + insetsSub1.left, 35 + insetsSub1.top, 80, 25);

        final JButton saveButton = new JButton("Save");
        saveButton.setMnemonic(KeyEvent.VK_V);
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                byte[] bytes = new byte[22];
                /* bytes[0] = (byte) - 1;
                         bytes[1] = (byte) - 1;
                         bytes[2] = (byte) 55;
                         bytes[3] = (byte) 125;
                         bytes[4] = (byte) 25;
                         bytes[5] = (byte) 3;
                         bytes[6] = (byte) 2;
                         bytes[7] = (byte) resetSequence; */
                bytes[0] = (byte) 1;
                try {
                    //clockField.commitEdit();
                    //valueField.commitEdit();
                    gridXField.commitEdit();
                    magThresholdField.commitEdit();
                    sentrySendPowerField.commitEdit();
                    powerModeField.commitEdit();
                    sDThresholdField.commitEdit();
                    pMTimeoutField.commitEdit();
                    DetectionThresholdField.commitEdit();
                    AcousticThresholdField.commitEdit();
                    PIRThresholdField.commitEdit();
                    shutdownThresholdField.commitEdit();
                    phaseDelayField.commitEdit();
                    reportPeriodField.commitEdit();
                    pmPhaseCountField.commitEdit();
                    FlowRateField.commitEdit();
                    syncDelayField.commitEdit();
                    sDBeaconCountField.commitEdit();

                }
                catch (java.text.ParseException pe) {

                }

                bytes[1] =
                    LongToFirstByte(((Long) gridXField.getValue()).intValue());
                bytes[2] = LongToFirstByte( ( (Long) sentrySendPowerField.
                                             getValue()).
                                           intValue());
                bytes[3] = LongToFirstByte( ( (Long) powerModeField.getValue()).
                                           intValue());
                bytes[4] = LongToFirstByte(((Long) sDThresholdField.getValue()).
                                           intValue());
                bytes[5] = LongToFirstByte( ( (Long) pMTimeoutField.getValue()).
                                           intValue());
                bytes[6] =
                    LongToFirstByte(((Long) FlowRateField.getValue()).intValue());
                bytes[7] =LongToFirstByte(((Long) PIRThresholdField.getValue()).
                                           intValue());
                bytes[8] = LongToFirstByte( ( (Long) DetectionThresholdField.
                                             getValue()).
                                           intValue());
                bytes[9] =
                  LongToFirstByte(((Long) magThresholdField.getValue()).intValue());
                bytes[10] = LongToFirstByte( ( (Long) AcousticThresholdField.
                                             getValue()).
                                           intValue());
                bytes[11] = LongToFirstByte( ( (Long) shutdownThresholdField.getValue()).
                                            intValue());
                bytes[12] = LongToFirstByte(((Long) phaseDelayField.getValue()).
                                            intValue());
                bytes[13] = LongToFirstByte( ( (Long) pmPhaseCountField.
                                              getValue()).
                                            intValue());
                bytes[14] = LongToSecondByte( ( (Long) pmPhaseCountField.
                                               getValue()).
                                             intValue());
                bytes[15] = LongToFirstByte(calculateSkip());
                bytes[16] = LongToSecondByte(calculateSkip());
                bytes[17] = LongToFirstByte(calculateSchedule());
                bytes[18] = LongToSecondByte(calculateSchedule());
                bytes[19] = LongToFirstByte(((Long) syncDelayField.getValue()).
                                            intValue());
                bytes[20] = LongToFirstByte( ( (Long) reportPeriodField.
                                              getValue()).
                                            intValue());
                bytes[21] = LongToFirstByte( ( (Long) sDBeaconCountField.
                                              getValue()).
                                            intValue());

                System.out.println("Print he packet"); ;

                for (int i = 0; i < 22; i++) {
                    System.out.println(" Byte[" + i + "] =" + bytes[i]);

                }
                try {
                    fos = new FileOutputStream(configFile);
                }
                catch (FileNotFoundException fnfe) {
                    System.err.println("File cannot be written");
                    return;
                }

                try {
                    fos.write(bytes);
                    fos.close();
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                    return;
                }

            }

        });

        // subPanel1.add(saveButton);
        saveButton.setBounds(90 + insetsSub1.left, 35 + insetsSub1.top, 80, 25);

        resetValueButton.setEnabled(true);
        resetValueButton.setMnemonic(KeyEvent.VK_S);
        resetValueButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // System.out.println("Reset");
                try {
                    //clockField.commitEdit();
                    //valueField.commitEdit();
                    gridXField.commitEdit();
                    magThresholdField.commitEdit();
                    sentrySendPowerField.commitEdit();
                    powerModeField.commitEdit();
                    sDThresholdField.commitEdit();
                    pMTimeoutField.commitEdit();
                    DetectionThresholdField.commitEdit();
                    AcousticThresholdField.commitEdit();
                    PIRThresholdField.commitEdit();
                    shutdownThresholdField.commitEdit();
                    phaseDelayField.commitEdit();
                    if (Constants.DebugSwitch)
                    	reportPeriodField.commitEdit();
                    pmPhaseCountField.commitEdit();
                    FlowRateField.commitEdit();
                    if (Constants.DebugSwitch) {
	                    syncDelayField.commitEdit();
	                    sDBeaconCountField.commitEdit();
					}
                }
                catch (java.text.ParseException pe) {

                }
                //        System.out.println(clockField.getValue().getClass());
                /* sendResetCommand( ( (Long) clockField.getValue()).intValue(),
                                 SOWNMoteFieldGUI.sequence, paramTypeValue,
                 ( (Long) valueField.getValue()).intValue()); */
                if (Constants.DebugSwitch) 
                	sendResetCommandDebug( ( (Long) gridXField.getValue()).intValue(),
                                 ( (Long) magThresholdField.getValue()).intValue(),
                                 ( (Long) sentrySendPowerField.getValue()).
                                 intValue(),
                                 ( (Long) powerModeField.getValue()).intValue(),
                                 ((Long) sDThresholdField.
                                  getValue()).intValue(),
                                 ( (Long) pMTimeoutField.getValue()).intValue(),
                                 ( (Long) DetectionThresholdField.getValue()).
                                 intValue() ,
                                 ( (Long) AcousticThresholdField.getValue()).
                                 intValue() ,
                                 ( (Long) PIRThresholdField.getValue()).
                                 intValue(),
                                 ( (Long) shutdownThresholdField.getValue()).intValue(),
                                 ( (Long) phaseDelayField.
                                   getValue()).intValue(),
                                 ( (Long) reportPeriodField.getValue()).
                                 intValue(),
                                 ( (Long) pmPhaseCountField.getValue()).
                                 intValue(),
                                 calculateSkip(),
                                 ( (Long) FlowRateField.getValue()).intValue(),
                                 calculateSchedule(),
                                 ( (Long) syncDelayField.getValue()).intValue(),
                                 ( (Long) sDBeaconCountField.getValue()).
                                 intValue());
				else 
                	sendResetCommand( ( (Long) gridXField.getValue()).intValue(),
                                 ( (Long) magThresholdField.getValue()).intValue(),
                                 ( (Long) sentrySendPowerField.getValue()).
                                 intValue(),
                                 ( (Long) powerModeField.getValue()).intValue(),
                                 ((Long) sDThresholdField.
                                  getValue()).intValue(),
                                 ( (Long) pMTimeoutField.getValue()).intValue(),
                                 ( (Long) DetectionThresholdField.getValue()).
                                 intValue(),
                                 ( (Long) AcousticThresholdField.getValue()).
                                 intValue(),
                                 ( (Long) PIRThresholdField.getValue()).
                                 intValue(),
                                 ( (Long) shutdownThresholdField.getValue()).intValue(),
                                 ( (Long) phaseDelayField.
                                   getValue()).intValue(),
                                 ( (Long) pmPhaseCountField.getValue()).
                                 intValue(),
                                 calculateSkip(),
                                 ( (Long) FlowRateField.getValue()).intValue(),
                                 calculateSchedule());
				
                // SOWNMoteFieldGUI.sequence++;
            }
        });

		GridBagConstraints cControl = new GridBagConstraints();
		cControl.fill = GridBagConstraints.HORIZONTAL;
        cControl.weightx = 0.8;
        cControl.weighty = 1;
        

		// for GPS reference point
		cControl.gridx = 0;
		cControl.gridy = 0;
		subPanel1.add(latLabel, cControl);
		
		// latLabel.setBounds(5 + insetsSub1.left, 5 + insetsSub1.top, 
		// 					80, 25);

		cControl.gridx = 0;
		cControl.gridy = 1;
		subPanel1.add(longLabel, cControl);
		// longLabel.setBounds(5 + insetsSub1.left, 35 + insetsSub1.top, 
		// 					80, 25);
		
		JRadioButton rbNorth = new JRadioButton("N");
		rbNorth.setActionCommand("N");
		rbNorth.setSelected(true);
		
		JRadioButton rbSouth = new JRadioButton("S");
		rbSouth.setActionCommand("S");
		
		ButtonGroup groupNS = new ButtonGroup();
		groupNS.add(rbNorth);
		groupNS.add(rbSouth);

		cControl.gridx = 1;
		cControl.gridy = 0;
		subPanel1.add(rbNorth, cControl);
		cControl.gridx = 2;
		cControl.gridy = 0;
		subPanel1.add(rbSouth, cControl);
		
		
		// rbNorth.setBounds(90 + insetsSub1.left, 5 + insetsSub1.top, 
		//					40, 25);
		rbNorth.addActionListener(this);
		// rbSouth.setBounds(135 + insetsSub1.left, 5 + insetsSub1.top, 
		//					40, 25);
		rbSouth.addActionListener(this);
		
		JRadioButton rbEast = new JRadioButton("E");
		rbEast.setActionCommand("E");
		
		JRadioButton rbWest = new JRadioButton("W");
		rbWest.setActionCommand("W");
		rbWest.setSelected(true);
		
		ButtonGroup groupEW = new ButtonGroup();
		groupEW.add(rbEast);
		groupEW.add(rbWest);

		cControl.gridx = 1;
		cControl.gridy = 1;
		subPanel1.add(rbEast, cControl);
		cControl.gridx = 2;
		cControl.gridy = 1;
		subPanel1.add(rbWest, cControl);
		
		
		// rbEast.setBounds(90 + insetsSub1.left, 35 + insetsSub1.top, 
		// 					40, 25);
		rbEast.addActionListener(this);
		// rbWest.setBounds(135 + insetsSub1.left, 35 + insetsSub1.top, 
		// 					40, 25);
        rbWest.addActionListener(this);

		cControl.gridx = 3;
		cControl.gridy = 0;
        subPanel1.add(latDegLabel, cControl);
        // latDegLabel.setBounds(180 + insetsSub1.left, 5 + insetsSub1.top, 
		// 					35, 25);
        
		cControl.gridx = 3;
		cControl.gridy = 1;
        subPanel1.add(longDegLabel, cControl);
        // longDegLabel.setBounds(180 + insetsSub1.left, 35 + insetsSub1.top, 
		// 					35, 25);
        
		latDegreeField = new IntegerEditor(Constants.Min_LatitudeDegree, 
											Constants.Max_LatitudeDegree, 
											Constants.Default_LatitudeDegree);
		cControl.gridx = 4;
		cControl.gridy = 0;
		subPanel1.add(latDegreeField, cControl);
		latDegreeField.setHorizontalAlignment(JTextField.RIGHT);
		// latDegreeField.setBounds(215 + insetsSub1.left, 5 + insetsSub1.top, 
		// 					30, 25);										

		longDegreeField = new IntegerEditor(Constants.Min_LongitudeDegree, 
											Constants.Max_LongitudeDegree, 
											Constants.Default_LongitudeDegree);
		cControl.gridx = 4;
		cControl.gridy = 1;
		subPanel1.add(longDegreeField, cControl);
		longDegreeField.setHorizontalAlignment(JTextField.RIGHT);
		// longDegreeField.setBounds(215 + insetsSub1.left, 35 + insetsSub1.top, 
		//					30, 25);										
		cControl.gridx = 5;
		cControl.gridy = 0;
		subPanel1.add(latMinLabel, cControl);
		// latMinLabel.setBounds(270 + insetsSub1.left, 5 + insetsSub1.top, 
		// 					30, 25);
		cControl.gridx = 5;
		cControl.gridy = 1;
		subPanel1.add(longMinLabel, cControl);
		// longMinLabel.setBounds(270 + insetsSub1.left, 35 + insetsSub1.top, 
		// 					30, 25);

		
		/* latMinuteIntField = new IntegerEditor(Constants.Min_MinuteInt, 
											Constants.Max_MinuteInt, 
											Constants.Default_LatMinuteInt);
		cControl.gridx = 6;
		cControl.gridy = 0;
		subPanel1.add(latMinuteIntField, cControl);
		latMinuteIntField.setHorizontalAlignment(JTextField.RIGHT);*/
		// latMinuteIntField.setBounds(305 + insetsSub1.left, 5 + insetsSub1.top, 
		// 					30, 25);										
		/* cControl.gridx = 7;
		cControl.gridy = 0;
		subPanel1.add(latMinuteDotLabel, cControl);*/
		// latMinuteDotLabel.setBounds(340 + insetsSub1.left, 5 + insetsSub1.top, 
		// 					5, 25);

		/* longMinuteIntField = new IntegerEditor(Constants.Min_MinuteInt, 
											Constants.Max_MinuteInt, 
											Constants.Default_LongMinuteInt);
		cControl.gridx = 6;
		cControl.gridy = 1;
		subPanel1.add(longMinuteIntField, cControl);
		longMinuteIntField.setHorizontalAlignment(JTextField.RIGHT);*/
		// longMinuteIntField.setBounds(305 + insetsSub1.left, 35 + insetsSub1.top, 
		// 					30, 25);										
		/* cControl.gridx = 7;
		cControl.gridy = 1;
		subPanel1.add(longMinuteDotLabel, cControl);*/
		// longMinuteDotLabel.setBounds(340 + insetsSub1.left, 35 + insetsSub1.top, 
		//					5, 25);
        
		/*latMinuteDecField = new IntegerEditor(Constants.Min_MinuteDec, 
											Constants.Max_MinuteDec, 
											Constants.Default_LatMinuteDec);*/
		latMinuteDecField = new DecEditor(Constants.Min_MinuteDec, Constants.Max_MinuteDec, Constants.Default_LatMinuteDec);
		cControl.gridx = 8;
		cControl.gridy = 0;
		subPanel1.add(latMinuteDecField, cControl);
		latMinuteDecField.setHorizontalAlignment(JTextField.RIGHT);
		// latMinuteDecField.setBounds(345 + insetsSub1.left, 5 + insetsSub1.top, 
		// 					30, 25);										
        
		longMinuteDecField = new DecEditor(Constants.Min_MinuteDec, 
											Constants.Max_MinuteDec, 
											Constants.Default_LongMinuteDec);
		cControl.gridx = 8;
		cControl.gridy = 1;
		subPanel1.add(longMinuteDecField, cControl);
		longMinuteDecField.setHorizontalAlignment(JTextField.RIGHT);
		// longMinuteDecField.setBounds(345 + insetsSub1.left, 35 + insetsSub1.top, 
		// 					30, 25);										
		
		final JPanel invPanel1 = new JPanel();
		final JPanel invPanel2 = new JPanel();


		cControl.gridx = 9;
		cControl.gridy = 0;
        subPanel1.add(invPanel1, cControl);		
		cControl.gridx = 9;
		cControl.gridy = 1;
        subPanel1.add(invPanel2, cControl);		
        
		cControl.gridx = 10;
		cControl.gridy = 1;
        subPanel1.add(resetValueButton, cControl);
        // resetValueButton.setBounds(750 + insetsSub1.left, 35 + insetsSub1.top,
        //                            120, 25);

        // masterClockButton = new JButton("MasterClock");
        masterClockButton.setMnemonic(KeyEvent.VK_M);
        masterClockButton.setEnabled(true);
        masterClockButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	
            	try {
                    latDegreeField.commitEdit();
                    // latMinuteIntField.commitEdit();
                    latMinuteDecField.commitEdit();
                    longDegreeField.commitEdit();
                    // longMinuteIntField.commitEdit();
                    longMinuteDecField.commitEdit();
                }
                catch (java.text.ParseException pe) {
            	}

				sendReferenceCommand(
					north * (( (Long) latDegreeField.getValue())
                       .intValue() * 3600000 + /* ( (Long) latMinuteIntField.getValue())
                       .intValue() * 60000 +*/ (int) (( (Double) latMinuteDecField.getValue())
                       .doubleValue() * 60000)), 
                       east * (( (Long) longDegreeField.getValue())
                       .intValue() * 3600000 + /* ( (Long) longMinuteIntField.getValue())
                       .intValue() * 60000 +*/ (int) (( (Double) longMinuteDecField.getValue())
                       .doubleValue() * 60000))
				);
				
				java.util.Timer timer = new java.util.Timer();
				timer.schedule(new TimerTask() {public void run() {
					try {
	                    clockField.commitEdit();
	                    delayField.commitEdit();
	                    latDegreeField.commitEdit();
	                    // latMinuteIntField.commitEdit();
	                    latMinuteDecField.commitEdit();
	                    longDegreeField.commitEdit();
	                    // longMinuteIntField.commitEdit();
	                    longMinuteDecField.commitEdit();
	                }
	                catch (java.text.ParseException pe) {
	
	            	}
	            	
	            	
	            	
	                Date currentData = new Date();
	                System.out.println("Current Time is "+currentData + " " + currentData.getTime());
	                
	                sendMasterClockCommand( (int)(currentData.getTime()/1000),
	                                       ( (Long) delayField.getValue())
	                                       .intValue(), 
	                                       north * (( (Long) latDegreeField.getValue())
	                                       .intValue() * 3600000 + /* ( (Long) latMinuteIntField.getValue())
	                                       .intValue() * 60000 +*/ (int) (( (Double) latMinuteDecField.getValue())
	                                       .doubleValue() * 60000)), 
	                                       east * (( (Long) longDegreeField.getValue())
	                                       .intValue() * 3600000 + /* ( (Long) longMinuteIntField.getValue())
	                                       .intValue() * 60000 +*/ (int) (( (Double) longMinuteDecField.getValue())
	                                       .doubleValue() * 60000))
	                                       );
					}}, 1000);

				
                /* try {
                    clockField.commitEdit();
                    delayField.commitEdit();
                    latDegreeField.commitEdit();
                    latMinuteIntField.commitEdit();
                    latMinuteDecField.commitEdit();
                    longDegreeField.commitEdit();
                    longMinuteIntField.commitEdit();
                    longMinuteDecField.commitEdit();
                }
                catch (java.text.ParseException pe) {

            	}
            	
            	
            	
                Date currentData = new Date();
                System.out.println("Current Time is "+currentData + " " + currentData.getTime());
                
                sendMasterClockCommand( (int)(currentData.getTime()/1000),
                                       ( (Long) delayField.getValue())
                                       .intValue(), 
                                       north * (( (Long) latDegreeField.getValue())
                                       .intValue() * 3600000 + ( (Long) latMinuteIntField.getValue())
                                       .intValue() * 60000 + ( (Long) latMinuteDecField.getValue())
                                       .intValue() * 60), 
                                       east * (( (Long) longDegreeField.getValue())
                                       .intValue() * 3600000 + ( (Long) longMinuteIntField.getValue())
                                       .intValue() * 60000 + ( (Long) longMinuteDecField.getValue())
                                       .intValue() * 60)
                                       );
                // SOWNMoteFieldGUI.sequence++;*/
            }
        });

		cControl.gridx = 11;
		cControl.gridy = 1;
        subPanel1.add(masterClockButton, cControl);
        // masterClockButton.setBounds(870 + insetsSub1.left, 35 + insetsSub1.top,
        //                            120, 25);

        final JButton queryParameterButton = new JButton("QueryParams");
        queryParameterButton.setMnemonic(KeyEvent.VK_P);
        queryParameterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	
            	/* sendPing();*/
                serialConnector.sendQueryParams( (byte) - 1, (byte) - 1,
                                                (byte) 55,
                                                (byte) 125,
                                                (byte) 16, paramSequence++);                                               
            }
        });


		cControl.gridx = 10;
		cControl.gridy = 0;
        subPanel1.add(queryParameterButton, cControl);
        
        // queryParameterButton.setBounds(750 + insetsSub1.left,
        //                                5 + insetsSub1.top, 120,
        //                               25);

        JLabel clockLabel = new JLabel("clock:");
        //subPanel1.add(clockLabel);
        clockLabel.setBounds(435 + insetsSub1.left, 5 + insetsSub1.top, 50, 20);

        clockField = new IntegerEditor(Constants.Min_Clock, Constants.Max_Clock,
                                       Constants.Default_Clock);
        // subPanel1.add(clockField);
        clockField.setHorizontalAlignment(JTextField.RIGHT);
        clockField.setBounds(485 + insetsSub1.left, 5 + insetsSub1.top, 70, 20);

        JLabel delayLabel = new JLabel("delay:");
        //subPanel1.add(delayLabel);
        delayLabel.setBounds(590 + insetsSub1.left, 5 + insetsSub1.top, 50, 20);

        delayField = new IntegerEditor(Constants.Min_Delay, Constants.Max_Delay,
                                       Constants.Default_Delay);
        //subPanel1.add(delayField);
        delayField.setHorizontalAlignment(JTextField.RIGHT);
        delayField.setBounds(635 + insetsSub1.left, 5 + insetsSub1.top, 70, 20);

        GridBagConstraints cParam = new GridBagConstraints();
        cParam.fill = GridBagConstraints.HORIZONTAL;
        cParam.weightx = 0.5;
        cParam.weighty = 1;


        JLabel gridXLabel = new JLabel("GridX (m) ", SwingConstants.RIGHT);
        cParam.gridx = 0;
        cParam.gridy = 0;
        subPanel4.add(gridXLabel, cParam);

        // gridXLabel.setBounds(5 + insetsSub4.left, insetsSub4.top, 120, 20);
        /* gridXField = new JFormattedTextField(
            NumberFormat.getIntegerInstance());
             gridXField.setValue(new Integer(5));*/
        gridXField = new IntegerEditor(Constants.Min_GridX, Constants.Max_GridX,
                                       Constants.Default_GridX);
        cParam.gridx = 1;
        cParam.gridy = 0;
        subPanel4.add(gridXField, cParam);
        gridXField.setHorizontalAlignment(JTextField.RIGHT);
        // gridXField.setBounds(145 + insetsSub4.left, insetsSub4.top, 60, 20);
        gridXField.setEnabled(true);

        JLabel phaseDelayLabel = new JLabel("PhaseDelay (s) ", SwingConstants.RIGHT);
        cParam.gridx = 2;
        cParam.gridy = 0;
        subPanel4.add(phaseDelayLabel, cParam);
        // phaseDelayLabel.setBounds(385 + insetsSub4.left, insetsSub4.top, 120,
                                  // 20);
        /* phaseDelayField = new JFormattedTextField(
            NumberFormat.getIntegerInstance());
             phaseDelayField.setValue(new Integer(20));*/
        phaseDelayField = new IntegerEditor(Constants.Min_PhaseDelay,
                                            Constants.Max_PhaseDelay,
                                            Constants.Default_PhaseDelay);
        cParam.gridx = 3;
        cParam.gridy = 0;
        subPanel4.add(phaseDelayField, cParam);
        phaseDelayField.setHorizontalAlignment(JTextField.RIGHT);
        // phaseDelayField.setBounds(515 +insetsSub4.left, insetsSub4.top, 60, 20);
        phaseDelayField.setEnabled(true);

        JLabel pmPhaseCountLabel = new JLabel("TPhaseCnt ", SwingConstants.RIGHT);
        cParam.gridx = 4;
        cParam.gridy = 0;
        subPanel4.add(pmPhaseCountLabel, cParam);
        // pmPhaseCountLabel.setBounds(755 + insetsSub4.left, 0 + insetsSub4.top,
        //                            120, 20);

        /* pmPhaseCountField = new JFormattedTextField(
            NumberFormat.getIntegerInstance());
             pmPhaseCountField.setValue(new Integer(10)); */
        pmPhaseCountField = new IntegerEditor(Constants.Min_PMPhaseCount,
                                              Constants.Max_PMPhaseCount,
                                              Constants.Default_PMPhaseCount);
        cParam.gridx = 5;
        cParam.gridy = 0;
        subPanel4.add(pmPhaseCountField, cParam);
        pmPhaseCountField.setHorizontalAlignment(JTextField.RIGHT);
        // pmPhaseCountField.setBounds(880 + insetsSub4.left, insetsSub4.top, 60,
        //                            20);
        pmPhaseCountField.setEnabled(true);

        JLabel powerModeLabel = new JLabel("PowerMode ", SwingConstants.RIGHT);
        cParam.gridx = 0;
        cParam.gridy = 1;
        subPanel4.add(powerModeLabel, cParam);
        // powerModeLabel.setBounds(5 + insetsSub4.left, 30 + insetsSub4.top, 120,
        //                         20);

        /* powerModeField = new JFormattedTextField(
            NumberFormat.getIntegerInstance());
             powerModeField.setValue(new Integer(4));*/
        powerModeField = new IntegerEditor(Constants.Min_PowerMode,
                                           Constants.Max_PowerMode,
                                           Constants.Default_PowerMode);
        cParam.gridx = 1;
        cParam.gridy = 1;
        subPanel4.add(powerModeField, cParam);
        powerModeField.setHorizontalAlignment(JTextField.RIGHT);
        // powerModeField.setBounds(145 + insetsSub4.left, 30 + insetsSub4.top, 60,
        //                         20);
        powerModeField.setEnabled(false);

        JLabel sDThresholdLabel = new JLabel("SDThreshold ", SwingConstants.RIGHT);
        cParam.gridx = 2;
        cParam.gridy = 1;
        subPanel4.add(sDThresholdLabel, cParam);
        // sDThresholdLabel.setBounds(385 + insetsSub4.left, 30 + insetsSub4.top,
        //                           120, 20);

        /* sDThresholdField = new JFormattedTextField(
            NumberFormat.getIntegerInstance());
             sDThresholdField.setValue(new Integer(70));*/
        sDThresholdField = new IntegerEditor(Constants.Min_SDThreshold,
                                             Constants.Max_SDThreshold,
                                             Constants.Default_SDThreshold);
		cParam.gridx = 3;
		cParam.gridy = 1;
        subPanel4.add(sDThresholdField, cParam);
        sDThresholdField.setHorizontalAlignment(JTextField.RIGHT);
        // sDThresholdField.setBounds(515 + insetsSub4.left, 30 + insetsSub4.top,
        //                           60, 20);
        sDThresholdField.setEnabled(false);

        JLabel pMTimeoutLabel = new JLabel("PMTimeout (s) ", SwingConstants.RIGHT);
        cParam.gridx = 4;
        cParam.gridy = 1;
        subPanel4.add(pMTimeoutLabel, cParam);
        // pMTimeoutLabel.setBounds(755 + insetsSub4.left, 30 + insetsSub4.top,
        //                         120, 20);

        /* pMTimeoutField = new JFormattedTextField(
            NumberFormat.getIntegerInstance());
             pMTimeoutField.setValue(new Integer(10));*/
        pMTimeoutField = new IntegerEditor(Constants.Min_PMTimeout,
                                           Constants.Max_PMTimeout,
                                           Constants.Default_PMTimeout);
        cParam.gridx = 5;
        cParam.gridy = 1;
        subPanel4.add(pMTimeoutField, cParam);
        pMTimeoutField.setHorizontalAlignment(JTextField.RIGHT);
        // pMTimeoutField.setBounds(880 + insetsSub4.left, 30 + insetsSub4.top, 60,
        //                         20);
        pMTimeoutField.setEnabled(false);

        JLabel DetectionThresholdLabel = new JLabel("DetectionThreshold ", SwingConstants.RIGHT);
        cParam.gridx = 0;
        cParam.gridy = 2;
        subPanel4.add(DetectionThresholdLabel, cParam);
        // DetectionThresholdLabel.setBounds(5 + insetsSub4.left,
        //                                 60 + insetsSub4.top, 120,
        //                                 20);


        /* DetectionThresholdField = new JFormattedTextField(
            NumberFormat.getIntegerInstance());
             DetectionThresholdField.setValue(new Integer(2)); */
        DetectionThresholdField = new IntegerEditor(Constants.
                                                  Min_DetectionThreshold,
                                                  Constants.
                                                  Max_DetectionThreshold,
                                                  Constants.
                                                  Default_DetectionThreshold);
        cParam.gridx = 1;
        cParam.gridy = 2;
        subPanel4.add(DetectionThresholdField, cParam);
        DetectionThresholdField.setHorizontalAlignment(JTextField.RIGHT);
        // DetectionThresholdField.setBounds(145 + insetsSub4.left,
        //                                 60 + insetsSub4.top, 60,
        //                                 20);
        DetectionThresholdField.setEnabled(false);



        JLabel AcousticThresholdLabel = new JLabel("AcousticThreshold ", SwingConstants.RIGHT);
        cParam.gridx = 2;
        cParam.gridy = 2;
        subPanel4.add(AcousticThresholdLabel, cParam);
        // AcousticThresholdLabel.setBounds(385 + insetsSub4.left,
        //                                60 + insetsSub4.top, 130,
        //                               20);


        /* AcousticThresholdField = new JFormattedTextField(
            NumberFormat.getIntegerInstance());
             AcousticThresholdField.setValue(new Integer(50));*/
        AcousticThresholdField = new IntegerEditor(Constants.Min_AcousticThreshold,
                                                 Constants.Max_AcousticThreshold,
                                                 Constants.
                                                 Default_AcousticThreshold);
        cParam.gridx = 3;
        cParam.gridy = 2;
        subPanel4.add(AcousticThresholdField, cParam);
        AcousticThresholdField.setHorizontalAlignment(JTextField.RIGHT);
        // AcousticThresholdField.setBounds(515 + insetsSub4.left,
        //                                60 + insetsSub4.top, 60,
        //                                20);
        AcousticThresholdField.setEnabled(false);

        JLabel PIRThresholdLabel = new JLabel("PIRThreshold ", SwingConstants.RIGHT);
        cParam.gridx = 4;
        cParam.gridy = 2;
        subPanel4.add(PIRThresholdLabel, cParam);
        // PIRThresholdLabel.setBounds(755 + insetsSub4.left, 60 + insetsSub4.top,
        //                             120, 20);

        /* magThresholdField = new JFormattedTextField(
            NumberFormat.getIntegerInstance());
             magThresholdField.setValue(new Integer(8)); */
        PIRThresholdField = new IntegerEditor(Constants.Min_PIRThreshold,
                                              Constants.Max_PIRThreshold,
                                              Constants.Default_PIRThreshold);
        cParam.gridx = 5;
        cParam.gridy = 2;
        subPanel4.add(PIRThresholdField, cParam);
        PIRThresholdField.setHorizontalAlignment(JTextField.RIGHT);
        // PIRThresholdField.setBounds(880 + insetsSub4.left, 60 + insetsSub4.top,
        //                             60, 20);
        PIRThresholdField.setEnabled(false);





        JLabel shutdownThresholdLabel = new JLabel("ShutdownThreshold ", SwingConstants.RIGHT);
        cParam.gridx = 0;
        cParam.gridy = 3;
        subPanel4.add(shutdownThresholdLabel, cParam);
        // shutdownThresholdLabel.setBounds(5 + insetsSub4.left, 90 + insetsSub4.top, 120, 20);


        shutdownThresholdField = new IntegerEditor(Constants.Min_ShutdownThreshold, Constants.Max_ShutdownThreshold,
                                     Constants.Default_ShutdownThreshold);
        cParam.gridx = 1;
        cParam.gridy = 3;
        subPanel4.add(shutdownThresholdField, cParam);
        shutdownThresholdField.setHorizontalAlignment(JTextField.RIGHT);
        // shutdownThresholdField.setBounds(145 + insetsSub4.left, 90 + insetsSub4.top, 60, 20);
        shutdownThresholdField.setEnabled(false);



        JLabel magThresholdLabel = new JLabel("MagThreshold ", SwingConstants.RIGHT);
        cParam.gridx = 2;
        cParam.gridy = 3;
        subPanel4.add(magThresholdLabel, cParam);
        // magThresholdLabel.setBounds(385 + insetsSub4.left, 90 + insetsSub4.top, 120,
        //                        20);

        magThresholdField = new IntegerEditor(Constants.Min_MagThreshold,
                                         Constants.Max_MagThreshold,
                                         Constants.Default_MagThreshold);
		cParam.gridx = 3;
		cParam.gridy = 3;
        subPanel4.add(magThresholdField, cParam);
        magThresholdField.setHorizontalAlignment(JTextField.RIGHT);
        // magThresholdField.setBounds(515 + insetsSub4.left, 90 + insetsSub4.top, 60,
        //                        20);
        magThresholdField.setEnabled(false);

        JLabel flowRateLabel = new JLabel("FlowRate (s) ", SwingConstants.RIGHT);
        cParam.gridx = 4;
        cParam.gridy = 3;
        subPanel4.add(flowRateLabel, cParam);
        // flowRateLabel.setBounds(755 + insetsSub4.left, 90 + insetsSub4.top, 120,
        //                      20);

        /* FlowRateField = new JFormattedTextField(
            NumberFormat.getIntegerInstance());
             FlowRateField.setValue(new Integer(30)); */
        FlowRateField = new IntegerEditor(Constants.Min_FlowRate, Constants.Max_FlowRate,
                                       Constants.Default_FlowRate);
        cParam.gridx = 5;
        cParam.gridy = 3;
        subPanel4.add(FlowRateField, cParam);
        FlowRateField.setHorizontalAlignment(JTextField.RIGHT);
        // FlowRateField.setBounds(880 + insetsSub4.left, 90 + insetsSub4.top, 60,
        //                      20);
        FlowRateField.setEnabled(false);


        JLabel sentrySendPowerLabel = new JLabel("SentryRange (m) ", SwingConstants.RIGHT);
        cParam.gridx = 0;
        cParam.gridy = 4;
        subPanel4.add(sentrySendPowerLabel, cParam);
        // sentrySendPowerLabel.setBounds(5 + insetsSub4.left,
        //                                120 + insetsSub4.top, 120,
        //                                20);


        /* sentrySendPowerField = new JFormattedTextField(
            NumberFormat.getIntegerInstance());
             sentrySendPowerField.setValue(new Integer(47));*/
        sentrySendPowerField = new IntegerEditor(Constants.Min_SentrySendPower,
                                                 Constants.Max_SentrySendPower,
                                                 Constants.
                                                 Default_SentrySendPower);
        cParam.gridx = 1;
        cParam.gridy = 4;
        subPanel4.add(sentrySendPowerField, cParam);
        sentrySendPowerField.setHorizontalAlignment(JTextField.RIGHT);
        // sentrySendPowerField.setBounds(145 + insetsSub4.left,
        //                                120 + insetsSub4.top, 60,
        //                                20);
        sentrySendPowerField.setEnabled(false);

        // cParam.fill = GridBagConstraints.NONE;
        cParam.gridx = 0;
        cParam.gridy = 5;
        cParam.gridwidth = 2;
        cParam.anchor = GridBagConstraints.LAST_LINE_START;
        subPanel4.add(defaultButton, cParam);
        // defaultButton.setBounds(5 + insetsSub4.left, 200 + insetsSub4.top, 240,
        //                        20);

        cParam.gridx = 4;
        cParam.gridy = 5;
        cParam.gridwidth = 2;
        cParam.anchor = GridBagConstraints.LAST_LINE_END;
        subPanel4.add(systemButton, cParam);
        // systemButton.setBounds(720 + insetsSub4.left, 200 + insetsSub4.top, 240,
        //                        20);







        JLabel reportPeriodLabel = new JLabel("ReportPeriod (s)");
        if (Constants.DebugSwitch)
        	subPanel4.add(reportPeriodLabel);
        reportPeriodLabel.setBounds(385 + insetsSub4.left, 120 + insetsSub4.top,
                                    120, 20);



        JLabel syncDelayLabel = new JLabel("SyncDelay (s)");
        if (Constants.DebugSwitch)
        	subPanel4.add(syncDelayLabel);
        syncDelayLabel.setBounds(755 + insetsSub4.left, 120 + insetsSub4.top,
                                 120, 20);

        JLabel sDBeaconCountLabel = new JLabel("SDBeaconCount");
        if (Constants.DebugSwitch)
        	subPanel4.add(sDBeaconCountLabel);
        sDBeaconCountLabel.setBounds(5 + insetsSub4.left, 150 + insetsSub4.top,
                                     120, 20);
        /* sDPowerField = new JFormattedTextField(
            NumberFormat.getIntegerInstance());
             sDPowerField.setValue(new Integer(0xff));*/




        /* reportPeriodField = new JFormattedTextField(
            NumberFormat.getIntegerInstance());
             reportPeriodField.setValue(new Integer(5));*/
        reportPeriodField = new IntegerEditor(Constants.Min_ReportPeriod,
                                              Constants.Max_ReportPeriod,
                                              Constants.Default_ReportPeriod);
        if (Constants.DebugSwitch)
        	subPanel4.add(reportPeriodField);
        reportPeriodField.setHorizontalAlignment(JTextField.RIGHT);
        reportPeriodField.setBounds(515 + insetsSub4.left, 120 + insetsSub4.top,
                                    60, 20);
        reportPeriodField.setEnabled(false);


        /* syncDelayField = new JFormattedTextField(
            NumberFormat.getIntegerInstance());
             syncDelayField.setValue(new Integer(30)); */
        syncDelayField = new IntegerEditor(Constants.Min_SyncDelay,
                                           Constants.Max_SyncDelay,
                                           Constants.Default_SyncDelay);
        if (Constants.DebugSwitch)
        	subPanel4.add(syncDelayField);
        syncDelayField.setHorizontalAlignment(JTextField.RIGHT);
        syncDelayField.setBounds(880 + insetsSub4.left, 120 + insetsSub4.top,
                                 60, 20);
        syncDelayField.setEnabled(false);

        /* sDBeaconCountField = new JFormattedTextField(
            NumberFormat.getIntegerInstance());
             sDBeaconCountField.setValue(new Integer(5));*/
        sDBeaconCountField = new IntegerEditor(Constants.Min_SDBeaconCount,
                                               Constants.Max_SDBeaconCount,
                                               Constants.Default_SDBeaconCount);
        if (Constants.DebugSwitch)
        	subPanel4.add(sDBeaconCountField);
        sDBeaconCountField.setHorizontalAlignment(JTextField.RIGHT);
        sDBeaconCountField.setBounds(145 + insetsSub4.left,
                                     150 + insetsSub4.top, 60, 20);
        sDBeaconCountField.setEnabled(false);

		GridBagConstraints cMain = new GridBagConstraints();
		cMain.fill = GridBagConstraints.BOTH;

		cMain.weightx = 1;
		cMain.weighty = 1;
		
        JScrollPane scroll = new JScrollPane(panel);
        panel.setPreferredSize(new Dimension(50000, 50000));
        cMain.gridx = 0;
        cMain.gridy = 0;
        cMain.gridwidth = 8;
        pane.add(scroll, cMain);
        // scroll.setBounds(10 + insets.left, 5 + insets.top, 990, 560);

		cMain.weighty = 0;

		cMain.fill = GridBagConstraints.HORIZONTAL;

        xSlider.addChangeListener(new xSliderListener());
        xSlider.setMajorTickSpacing(9);
        xSlider.setMinorTickSpacing(1);
        xSlider.setPaintTicks(true);
        // xSlider.setPaintLabels(true);
        xSlider.setValue(1);
        xSlider.setBorder(
            BorderFactory.createEmptyBorder(0, 0, 10, 0));
		cMain.ipadx = 200;
		cMain.gridwidth = 2;
		cMain.gridx = 0;
		cMain.gridy = 1;
        pane.add(xSlider, cMain);
        // xSlider.setBounds(10 + insets.left, 570 + insets.top, 100, 40);

		cMain.ipadx = 0;
        cMain.gridx = 2;
		cMain.gridy = 1;
		cMain.gridwidth = 1;

        pane.add(xSliderLabel, cMain);
        
        // xSliderLabel.setBounds(110 + insets.left, 570 + insets.top, 60, 25);

		cMain.ipadx = 0;
        cMain.gridx = 3;
		cMain.gridy = 1;
        pane.add(showButton, cMain);
        // showButton.setBounds(180 + insets.left, 570 + insets.top, 100, 20);

        cMain.gridx = 4;
		cMain.gridy = 1;
        pane.add(filterButton, cMain);
        // filterButton.setBounds(290 + insets.left, 570 + insets.top, 100, 20);

        cMain.gridx = 5;
		cMain.gridy = 1;
        pane.add(recordButton, cMain);
        // recordButton.setBounds(400 + insets.left, 570 + insets.top, 100, 20);
		

        cMain.gridx = 6;
		cMain.gridy = 1;
        pane.add(serialButton, cMain);
        // serialButton.setBounds(510 + insets.left, 570 + insets.top, 110, 20);

        cMain.gridx = 7;
		cMain.gridy = 1;
        pane.add(resetButton, cMain);
        // resetButton.setBounds(630 + insets.left, 570 + insets.top, 100, 20);

        /* pane.add(globalButton);
         globalButton.setBounds(335 + insets.left, 380 + insets.top, 120, 25);*/

		cMain.gridx = 0;
		cMain.gridy = 2;
		pane.add(nodesLabel, cMain);
		
		cMain.gridx = 2;
		cMain.gridy = 2;
		cMain.gridwidth = 2;
		pane.add(detDelayLabel, cMain);
		
		cMain.gridx = 4;
		cMain.gridy = 2;
		pane.add(clsDelayLabel, cMain);
		
		cMain.gridx = 6;
		cMain.gridy = 2;
		pane.add(velDelayLabel, cMain);



		cMain.gridwidth = 8;
		cMain.gridheight = 2;
        cMain.gridx = 0;
		cMain.gridy = 3;
		cMain.ipady = 40;
        pane.add(dynamicLabel, cMain);
        // dynamicLabel.setBounds(10 + insets.left, 610 + insets.top, 720, 40);


        panel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        panel.statusHtmlTextArea.setText(
            "<html><font size=+0 color=red>SOWN MOTE FIELD SETUP</font>");

        statusLabel.setText(panel.statusHtmlTextArea.getText());
		cMain.fill = GridBagConstraints.HORIZONTAL;
		cMain.ipady = 0;
		cMain.gridx = 0;
		cMain.gridy = 5;
		cMain.gridheight = 1;
		cMain.gridwidth = 7;
        pane.add(statusLabel, cMain);
        // statusLabel.setBounds(10 + insets.left, 660 + insets.top, 720, 16);

		cMain.gridx = 7;
		cMain.gridy = 5;
		cMain.gridwidth = 2;
		pane.add(timeLabel, cMain);

		boolean[] skipBits = new boolean[16];
		skipBits = TwoBytesToBools(Constants.Default_SkipBits);
		boolean[] scheduleBits = new boolean[16];
		scheduleBits = TwoBytesToBools(Constants.Default_ScheduleBits);
		
        GridBagConstraints cOption = new GridBagConstraints();
		cOption.fill = GridBagConstraints.HORIZONTAL;

        skipBitCB0 = new JCheckBox("LedEnable", skipBits[0]);
        cOption.weightx = 0.8;
        cOption.weighty = 1;
        cOption.gridx = 0;
        cOption.gridy = 0;
        subPanel2.add(skipBitCB0, cOption);
        // skipBitCB0.setBounds(10 + insetsSub2.left, 10 + insetsSub2.top, 170, 20);

        skipBitCB1 = new JCheckBox("Skip Sym Detection", skipBits[1]);
        cOption.gridx = 1;
        cOption.gridy = 0;
        subPanel2.add(skipBitCB1, cOption);
        // skipBitCB1.setBounds(260 + insetsSub2.left, 10 + insetsSub2.top, 170, 20);

        skipBitCB2 = new JCheckBox("Skip Report", skipBits[2]);
        cOption.gridx = 2;
        cOption.gridy = 0;
        subPanel2.add(skipBitCB2, cOption);
        // skipBitCB2.setBounds(510 + insetsSub2.left, 10 + insetsSub2.top, 170, 20);

        skipBitCB3 = new JCheckBox("Skip Sntry Selection", skipBits[3]);
        cOption.gridx = 3;
        cOption.gridy = 0;
        subPanel2.add(skipBitCB3, cOption);
        // skipBitCB3.setBounds(760 + insetsSub2.left, 10 + insetsSub2.top, 170, 20);

        skipBitCB4 = new JCheckBox("Skip PM", skipBits[4]);
        cOption.gridx = 0;
        cOption.gridy = 1;
        subPanel2.add(skipBitCB4, cOption);
        // skipBitCB4.setBounds(10+ insetsSub2.left, 45 + insetsSub2.top, 170, 20);

        skipBitCB5 = new JCheckBox("Skip Tracking", skipBits[5]);
        cOption.gridx = 1;
        cOption.gridy = 1;
        subPanel2.add(skipBitCB5, cOption);
        // skipBitCB5.setBounds(260 + insetsSub2.left, 45 + insetsSub2.top, 170,
                             // 20);

        skipBitCB6 = new JCheckBox("Real Localization", skipBits[6]);
        cOption.gridx = 2;
        cOption.gridy = 1;
        subPanel2.add(skipBitCB6, cOption);
        // skipBitCB6.setBounds(510 + insetsSub2.left, 45 + insetsSub2.top, 170,
                             // 20);

        skipBitCB7 = new JCheckBox("Soft Range", skipBits[7]);
        cOption.gridx = 3;
        cOption.gridy = 1;
        subPanel2.add(skipBitCB7, cOption);
        // skipBitCB7.setBounds(760 + insetsSub2.left, 45 + insetsSub2.top, 170,
                             // 20);

        skipBitCB8 = new JCheckBox("SentryMode", skipBits[8]);
        cOption.gridx = 0;
        cOption.gridy = 2;
        subPanel2.add(skipBitCB8, cOption);
        // skipBitCB8.setBounds(10+ insetsSub2.left, 75 + insetsSub2.top, 170, 20);

        skipBitCB9 = new JCheckBox("TripWireMode", skipBits[9]);
        cOption.gridx = 1;
        cOption.gridy = 2;
        subPanel2.add(skipBitCB9, cOption);
        // skipBitCB9.setBounds(260 + insetsSub2.left, 75 + insetsSub2.top, 170,
                             // 20);

        skipBitCB10 = new JCheckBox("RawData", skipBits[10]);
        cOption.gridx = 2;
        cOption.gridy = 2;
        subPanel2.add(skipBitCB10, cOption);
        // skipBitCB10.setBounds(510 + insetsSub2.left, 75 + insetsSub2.top, 170,
                              // 20);

        skipBitCB11 = new JCheckBox("MAG_ENABLE", skipBits[11]);
        cOption.gridx = 3;
        cOption.gridy = 2;
        subPanel2.add(skipBitCB11, cOption);
        // skipBitCB11.setBounds(760 + insetsSub2.left, 75 + insetsSub2.top, 170,
                              // 20);

        skipBitCB12 = new JCheckBox("PIR_ENABLE", skipBits[12]);
        cOption.gridx = 0;
        cOption.gridy = 3;
        subPanel2.add(skipBitCB12, cOption);
        // skipBitCB12.setBounds(10 + insetsSub2.left, 105 + insetsSub2.top, 170,
                              // 20);

        skipBitCB13 = new JCheckBox("ACOUSTIC_ENABLE", skipBits[13]);
        cOption.gridx = 1;
        cOption.gridy = 3;
        subPanel2.add(skipBitCB13, cOption);
        // skipBitCB13.setBounds(260 + insetsSub2.left, 105 + insetsSub2.top, 170,
                              // 20);
        skipBitCB13.setEnabled(true);

        skipBitCB14 = new JCheckBox("SentryRptOnly", skipBits[14]);
        cOption.gridx = 2;
        cOption.gridy = 3;
        subPanel2.add(skipBitCB14, cOption);
        // skipBitCB14.setBounds(510 + insetsSub2.left, 105 + insetsSub2.top, 170,
                              // 20);
        skipBitCB14.setEnabled(true);

        skipBitCB15 = new JCheckBox("MultiSyncRoot", skipBits[15]);
        cOption.gridx = 3;
        cOption.gridy = 3;
        subPanel2.add(skipBitCB15, cOption);
        // skipBitCB15.setBounds(760 + insetsSub2.left, 105 + insetsSub2.top, 170,
                              // 20);
        skipBitCB15.setEnabled(true);

        GridBagConstraints cSched = new GridBagConstraints();
		cSched.fill = GridBagConstraints.HORIZONTAL;
        scheduleBitCB00 = new JCheckBox("00", scheduleBits[0]);
       	cSched.weightx = 0.8;
       	cSched.weighty = 1;
       	cSched.gridx = 0;
        subPanel3.add(scheduleBitCB00 ,cSched);
        // scheduleBitCB00.setBounds(30 + insetsSub3.left, 20 + insetsSub3.top, 40, 20);

        scheduleBitCB01 = new JCheckBox("01", scheduleBits[1]);
       	cSched.gridx = 1;
        subPanel3.add(scheduleBitCB01 ,cSched);
        // scheduleBitCB01.setBounds(90 +insetsSub3.left, 20 + insetsSub3.top, 40, 20);

        scheduleBitCB02 = new JCheckBox("02", scheduleBits[2]);
       	cSched.gridx = 2;
        subPanel3.add(scheduleBitCB02 ,cSched);
        // scheduleBitCB02.setBounds(150 +insetsSub3.left, 20 + insetsSub3.top, 40, 20);

        scheduleBitCB03 = new JCheckBox("03", scheduleBits[3]);
       	cSched.gridx = 3;
        subPanel3.add(scheduleBitCB03 ,cSched);
        // scheduleBitCB03.setBounds(210 +insetsSub3.left, 20 + insetsSub3.top, 40, 20);

        scheduleBitCB04 = new JCheckBox("04", scheduleBits[4]);
       	cSched.gridx = 4;
        subPanel3.add(scheduleBitCB04 ,cSched);
        // scheduleBitCB04.setBounds(270 +insetsSub3.left, 20 + insetsSub3.top, 40, 20);

        scheduleBitCB05 = new JCheckBox("05", scheduleBits[5]);
       	cSched.gridx = 5;
        subPanel3.add(scheduleBitCB05 ,cSched);
        // scheduleBitCB05.setBounds(330 +insetsSub3.left, 20 + insetsSub3.top, 40, 20);

        scheduleBitCB06 = new JCheckBox("06", scheduleBits[6]);
       	cSched.gridx = 6;
        subPanel3.add(scheduleBitCB06 ,cSched);
        // scheduleBitCB06.setBounds(390 +insetsSub3.left, 20 + insetsSub3.top, 40, 20);

        scheduleBitCB07 = new JCheckBox("07", scheduleBits[7]);
       	cSched.gridx = 7;
        subPanel3.add(scheduleBitCB07 ,cSched);
        // scheduleBitCB07.setBounds(450 +insetsSub3.left, 20 + insetsSub3.top, 40, 20);

        scheduleBitCB08 = new JCheckBox("08", scheduleBits[8]);
       	cSched.gridx = 8;
        subPanel3.add(scheduleBitCB08 ,cSched);
        // scheduleBitCB08.setBounds(510 +insetsSub3.left, 20 + insetsSub3.top, 40, 20);

        scheduleBitCB09 = new JCheckBox("09", scheduleBits[9]);
       	cSched.gridx = 9;
        subPanel3.add(scheduleBitCB09 ,cSched);
        // scheduleBitCB09.setBounds(570 +insetsSub3.left, 20 + insetsSub3.top, 40, 20);

        scheduleBitCB10 = new JCheckBox("10", scheduleBits[10]);
       	cSched.gridx = 10;
        subPanel3.add(scheduleBitCB10 ,cSched);
        // scheduleBitCB10.setBounds(630 +insetsSub3.left, 20 + insetsSub3.top, 40, 20);

        scheduleBitCB11 = new JCheckBox("11", scheduleBits[11]);
       	cSched.gridx = 11;
        subPanel3.add(scheduleBitCB11 ,cSched);
        // scheduleBitCB11.setBounds(690 +insetsSub3.left, 20 + insetsSub3.top, 40, 20);

        scheduleBitCB12 = new JCheckBox("12", scheduleBits[12]);
       	cSched.gridx = 12;
        subPanel3.add(scheduleBitCB12 ,cSched);
        // scheduleBitCB12.setBounds(750 +insetsSub3.left, 20 + insetsSub3.top, 40, 20);

        scheduleBitCB13 = new JCheckBox("13", scheduleBits[13]);
       	cSched.gridx = 13;
        subPanel3.add(scheduleBitCB13 ,cSched);
        // scheduleBitCB13.setBounds(810 +insetsSub3.left, 20 + insetsSub3.top, 40, 20);

        scheduleBitCB14 = new JCheckBox("14", scheduleBits[14]);
       	cSched.gridx = 14;
        subPanel3.add(scheduleBitCB14 ,cSched);
        // scheduleBitCB14.setBounds(870 +insetsSub3.left, 20 + insetsSub3.top, 40, 20);

        scheduleBitCB15 = new JCheckBox("15", scheduleBits[15]);
       	cSched.gridx = 15;
        subPanel3.add(scheduleBitCB15 ,cSched);
        // scheduleBitCB15.setBounds(930 +insetsSub3.left, 20+ insetsSub3.top, 40, 20);



        /* ySlider.addChangeListener(new ySliderListener());
             ySlider.setMajorTickSpacing(9);
             ySlider.setMinorTickSpacing(1);
             ySlider.setPaintTicks(true);
             // ySlider.setPaintLabels(true);
             ySlider.setValue(1);
             ySlider.setBorder(
            BorderFactory.createEmptyBorder(0, 0, 10, 0));

             pane.add(ySlider);
             ySlider.setBounds(180 + insets.left, 380 + insets.top, 100, 40);

             pane.add(ySliderLabel);
         ySliderLabel.setBounds(280 + insets.left, 380 + insets.top, 60, 25); */

        //**********************************************************************
         // GPS Panel widgets
        /* JLabel gpsRefPointLabel = new JLabel("Reference Point");
        subPanel5.add(gpsRefPointLabel);
        gpsRefPointLabel.setBounds(40 +insetsSub5.left, 20 + insetsSub5.top, 150, 20); */

		GridBagConstraints cRef = new GridBagConstraints();
		cRef.fill = GridBagConstraints.HORIZONTAL;
        cRef.weightx = 0.8;
        cRef.weighty = 1;
        

		// for GPS reference point
		cRef.gridx = 0;
		cRef.gridy = 0;
		subPanel5.add(refLatLabel, cRef);
		
		// latLabel.setBounds(5 + insetsSub1.left, 5 + insetsSub1.top, 
		// 					80, 25);

		cRef.gridx = 0;
		cRef.gridy = 1;
		subPanel5.add(refLongLabel, cRef);
		// longLabel.setBounds(5 + insetsSub1.left, 35 + insetsSub1.top, 
		// 					80, 25);
		
		JRadioButton refRbNorth = new JRadioButton("N");
		refRbNorth.setActionCommand("RN");
		refRbNorth.setSelected(true);
		
		JRadioButton refRbSouth = new JRadioButton("S");
		refRbSouth.setActionCommand("RS");
		
		ButtonGroup refGroupNS = new ButtonGroup();
		refGroupNS.add(refRbNorth);
		refGroupNS.add(refRbSouth);

		cRef.gridx = 1;
		cRef.gridy = 0;
		subPanel5.add(refRbNorth, cRef);
		cRef.gridx = 2;
		cRef.gridy = 0;
		subPanel5.add(refRbSouth, cRef);
		
		
		// rbNorth.setBounds(90 + insetsSub1.left, 5 + insetsSub1.top, 
		//					40, 25);
		refRbNorth.addActionListener(this);
		// rbSouth.setBounds(135 + insetsSub1.left, 5 + insetsSub1.top, 
		//					40, 25);
		refRbSouth.addActionListener(this);
		
		JRadioButton refRbEast = new JRadioButton("E");
		refRbEast.setActionCommand("RE");
		
		JRadioButton refRbWest = new JRadioButton("W");
		refRbWest.setActionCommand("RW");
		refRbWest.setSelected(true);
		
		ButtonGroup refGroupEW = new ButtonGroup();
		refGroupEW.add(refRbEast);
		refGroupEW.add(refRbWest);

		cRef.gridx = 1;
		cRef.gridy = 1;
		subPanel5.add(refRbEast, cRef);
		cRef.gridx = 2;
		cRef.gridy = 1;
		subPanel5.add(refRbWest, cRef);
		
		
		// rbEast.setBounds(90 + insetsSub1.left, 35 + insetsSub1.top, 
		// 					40, 25);
		refRbEast.addActionListener(this);
		// rbWest.setBounds(135 + insetsSub1.left, 35 + insetsSub1.top, 
		// 					40, 25);
        refRbWest.addActionListener(this);

		cRef.gridx = 3;
		cRef.gridy = 0;
        subPanel5.add(refLatDegLabel, cRef);
        // latDegLabel.setBounds(180 + insetsSub1.left, 5 + insetsSub1.top, 
		// 					35, 25);
        
		cRef.gridx = 3;
		cRef.gridy = 1;
        subPanel5.add(refLongDegLabel, cRef);
        // longDegLabel.setBounds(180 + insetsSub1.left, 35 + insetsSub1.top, 
		// 					35, 25);
        
		refLatDegreeField = new IntegerEditor(Constants.Min_LatitudeDegree, 
											Constants.Max_LatitudeDegree, 
											Constants.Default_LatitudeDegree);
		cRef.gridx = 4;
		cRef.gridy = 0;
		subPanel5.add(refLatDegreeField, cRef);
		refLatDegreeField.setHorizontalAlignment(JTextField.RIGHT);
		// latDegreeField.setBounds(215 + insetsSub1.left, 5 + insetsSub1.top, 
		// 					30, 25);										

		refLongDegreeField = new IntegerEditor(Constants.Min_LongitudeDegree, 
											Constants.Max_LongitudeDegree, 
											Constants.Default_LongitudeDegree);
		cRef.gridx = 4;
		cRef.gridy = 1;
		subPanel5.add(refLongDegreeField, cRef);
		refLongDegreeField.setHorizontalAlignment(JTextField.RIGHT);
		// longDegreeField.setBounds(215 + insetsSub1.left, 35 + insetsSub1.top, 
		//					30, 25);										
		cRef.gridx = 5;
		cRef.gridy = 0;
		subPanel5.add(refLatMinLabel, cRef);
		// latMinLabel.setBounds(270 + insetsSub1.left, 5 + insetsSub1.top, 
		// 					30, 25);
		cRef.gridx = 5;
		cRef.gridy = 1;
		subPanel5.add(refLongMinLabel, cRef);
		// longMinLabel.setBounds(270 + insetsSub1.left, 35 + insetsSub1.top, 
		// 					30, 25);

		
		/* refLatMinuteIntField = new IntegerEditor(Constants.Min_MinuteInt, 
											Constants.Max_MinuteInt, 
											Constants.Default_LatMinuteInt);
		cRef.gridx = 6;
		cRef.gridy = 0;
		subPanel5.add(refLatMinuteIntField, cRef);
		refLatMinuteIntField.setHorizontalAlignment(JTextField.RIGHT);*/
		// latMinuteIntField.setBounds(305 + insetsSub1.left, 5 + insetsSub1.top, 
		// 					30, 25);										
		/* cRef.gridx = 7;
		cRef.gridy = 0;
		subPanel5.add(refLatMinuteDotLabel, cRef);*/
		// latMinuteDotLabel.setBounds(340 + insetsSub1.left, 5 + insetsSub1.top, 
		// 					5, 25);

		/* refLongMinuteIntField = new IntegerEditor(Constants.Min_MinuteInt, 
											Constants.Max_MinuteInt, 
											Constants.Default_LongMinuteInt);
		cRef.gridx = 6;
		cRef.gridy = 1;
		subPanel5.add(refLongMinuteIntField, cRef);
		refLongMinuteIntField.setHorizontalAlignment(JTextField.RIGHT);*/
		// longMinuteIntField.setBounds(305 + insetsSub1.left, 35 + insetsSub1.top, 
		// 					30, 25);										
		/* cRef.gridx = 7;
		cRef.gridy = 1;
		subPanel5.add(refLongMinuteDotLabel, cRef);*/
		// longMinuteDotLabel.setBounds(340 + insetsSub1.left, 35 + insetsSub1.top, 
		//					5, 25);
        
		refLatMinuteDecField = new DecEditor(Constants.Min_MinuteDec, 
											Constants.Max_MinuteDec, 
											Constants.Default_LatMinuteDec);
		cRef.gridx = 8;
		cRef.gridy = 0;
		subPanel5.add(refLatMinuteDecField, cRef);
		refLatMinuteDecField.setHorizontalAlignment(JTextField.RIGHT);
		// latMinuteDecField.setBounds(345 + insetsSub1.left, 5 + insetsSub1.top, 
		// 					30, 25);										
        
		refLongMinuteDecField = new DecEditor(Constants.Min_MinuteDec, 
											Constants.Max_MinuteDec, 
											Constants.Default_LongMinuteDec);
		cRef.gridx = 8;
		cRef.gridy = 1;
		subPanel5.add(refLongMinuteDecField, cRef);
		refLongMinuteDecField.setHorizontalAlignment(JTextField.RIGHT);
		// longMinuteDecField.setBounds(345 + insetsSub1.left, 35 + insetsSub1.top, 
		// 					30, 25);										
		



        // Reference Point Latitude
        /* JLabel gpsLatLabel = new JLabel("Latitude:");
        subPanel5.add(gpsLatLabel);
        gpsLatLabel.setBounds(50 + insetsSub5.left, 10 + insetsSub5.top, 70, 20);

        gpsRefPointLatField = new IntegerEditor(Constants.Min_GPSRefPointLat,
                                                Constants.Max_GPSRefPointLat,
                                                Constants.
                                                Default_GPSRefPointLat);
        subPanel5.add(gpsRefPointLatField);
        gpsRefPointLatField.setHorizontalAlignment(JTextField.RIGHT);
        gpsRefPointLatField.setBounds(170 + insetsSub5.left, 10 + insetsSub5.top, 90,
                                      20);
		gpsRefPointLatField.setEnabled(false);
		
        // Reference Point Longitude
        JLabel gpsLonLabel = new JLabel("Longitude:");
        subPanel5.add(gpsLonLabel);
        gpsLonLabel.setBounds(50 + insetsSub5.left, 30 + insetsSub5.top, 70, 20);

        gpsRefPointLonField = new IntegerEditor(Constants.Min_GPSRefPointLon,
                                                Constants.Max_GPSRefPointLon,
                                                Constants.
                                                Default_GPSRefPointLon);
        subPanel5.add(gpsRefPointLonField);
        gpsRefPointLonField.setHorizontalAlignment(JTextField.RIGHT);
        gpsRefPointLonField.setBounds(170 + insetsSub5.left, 30 + insetsSub5.top, 90,
                                      20);
		gpsRefPointLonField.setEnabled(false); */
		
        // Select Base Mote or GPS Mote
        gpsMote = new JCheckBox("GPS Mote", true);
        cRef.gridx = 0;
        cRef.gridy = 2;
        cRef.gridwidth = 2;
        subPanel5.add(gpsMote, cRef);
        gpsMote.setSelected(false);
        // gpsMote.setBounds(10 + insetsSub5.left, 50 + insetsSub5.top, 150, 20);

        gpsMote.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (gpsMote.isSelected()) {
                    gpsSendPowerField.setEnabled(true);
                    gpsSendPeriodField.setEnabled(true);
                    gpsResetButton.setEnabled(true);
                }
                else {
                    gpsSendPowerField.setEnabled(false);
                    gpsSendPeriodField.setEnabled(false);
                    gpsResetButton.setEnabled(false);
                }
            }
        });

        // Sending Power
        JLabel gpsSendPowerLabel = new JLabel("Sending Power: ", SwingConstants.RIGHT);
        cRef.gridx = 0;
        cRef.gridy = 3;
        cRef.gridwidth = 2;
        subPanel5.add(gpsSendPowerLabel, cRef);
        // gpsSendPowerLabel.setBounds(50 + insetsSub5.left, 70 + insetsSub5.top, 150,
        //                             20);
        gpsSendPowerField = new IntegerEditor(Constants.Min_GPSSendPower,
                                              Constants.Max_GPSSendPower,
                                              Constants.Default_GPSSendPower);
        cRef.gridx = 2;
        cRef.gridy = 3;
        cRef.gridwidth = 2;
        subPanel5.add(gpsSendPowerField, cRef);
        gpsSendPowerField.setHorizontalAlignment(JTextField.RIGHT);
        // gpsSendPowerField.setBounds(170 + insetsSub5.left, 70 + insetsSub5.top, 90,
        //                             20);
        gpsSendPowerField.setEnabled(false);

        // Sending Period
        JLabel gpsSendPeriodLabel = new JLabel("Sending Period: ", SwingConstants.RIGHT);
        cRef.gridx = 0;
        cRef.gridy = 4;
        cRef.gridwidth = 2;
        
        subPanel5.add(gpsSendPeriodLabel, cRef);
        // gpsSendPeriodLabel.setBounds(50 + insetsSub5.left, 90 + insetsSub5.top, 150,
        //                              20);

        gpsSendPeriodField = new IntegerEditor(Constants.Min_GPSSendPeriod,
                                               Constants.Max_GPSSendPeriod,
                                               Constants.Default_GPSSendPeriod);
        cRef.gridx = 2;
        cRef.gridy = 4;
        cRef.gridwidth = 2;
        subPanel5.add(gpsSendPeriodField, cRef);
        gpsSendPeriodField.setHorizontalAlignment(JTextField.RIGHT);
        // gpsSendPeriodField.setBounds(170 + insetsSub5.left, 90 + insetsSub5.top, 90,
        //                              20);
        gpsSendPeriodField.setEnabled(false);

        // Send GPS button
        final JButton gpsSendButton = new JButton("Send GPS");
        gpsSendButton.setMnemonic(KeyEvent.VK_S);
        gpsSendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // gpsRefPointLatField.commitEdit();
                    // gpsRefPointLonField.commitEdit();
                    refLatDegreeField.commitEdit();
                    // refLatMinuteIntField.commitEdit();
                    refLatMinuteDecField.commitEdit();
                    refLongDegreeField.commitEdit();
                    // refLongMinuteIntField.commitEdit();
                    refLongMinuteDecField.commitEdit();
                    gpsSendPowerField.commitEdit();
                    gpsSendPeriodField.commitEdit();
                }
                catch (java.text.ParseException pe) {

                }
                sendGpsCmd(1,
                                       refNorth * (( (Long) refLatDegreeField.getValue())
                                       .intValue() * 3600000 + /* ( (Long) refLatMinuteIntField.getValue())
                                       .intValue() * 60000 +*/ (int) (( (Double) refLatMinuteDecField.getValue())
                                       .doubleValue() * 60000)), 
                                       refEast * (( (Long) refLongDegreeField.getValue())
                                       .intValue() * 3600000 + /* ( (Long) refLongMinuteIntField.getValue())
                                       .intValue() * 60000 +*/ (int) (( (Double) refLongMinuteDecField.getValue())
                                       .doubleValue() * 60000)), 
                           ( (Long) gpsSendPowerField.getValue()).intValue(),
                           ( (Long) gpsSendPeriodField.getValue()).intValue());
            }
        });

        cRef.gridx = 1;
        cRef.gridy = 5;
        cRef.gridwidth = 2;
        subPanel5.add(gpsSendButton, cRef);
        gpsSendButton.setBounds(90 + insetsSub5.left, 130 + insetsSub5.top, 120, 25);

        // Reset GPS button
        gpsResetButton = new JButton("Reset GPS");
        gpsResetButton.setMnemonic(KeyEvent.VK_G);
        gpsResetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendGpsCmd(2, 0, 0, 0, 0);
            }
        });

        cRef.gridx = 1;
        cRef.gridy = 6;
        cRef.gridwidth = 2;
        subPanel5.add(gpsResetButton, cRef);
        gpsResetButton.setBounds(90 +insetsSub5.left, 170 + insetsSub5.top, 120, 25);
        gpsResetButton.setEnabled(false);


		GridBagConstraints cCfg = new GridBagConstraints();
		cCfg.fill = GridBagConstraints.HORIZONTAL;
        cCfg.weightx = 0.8;
        cCfg.weighty = 1;


        // Deployment Type
        JLabel deployTypeLabel = new JLabel("Deployment Type:");
        cCfg.gridx = 0 ;
        cCfg.gridy = 0 ;
        cCfg.gridwidth = 1;
        subPanel6.add(deployTypeLabel, cCfg);
        // deployTypeLabel.setBounds(30 +insetsSub6.left, 10 + insetsSub6.top, 150, 20);

        String[] deployTypes = {
            "Turn ON at Deployment", "ON All the Time"};
        deployTypeField = new JComboBox(deployTypes);
        deployTypeField.setSelectedIndex(1);
        cCfg.gridx = 1;
        cCfg.gridy = 0;
        cCfg.gridwidth = 2;
        subPanel6.add(deployTypeField, cCfg);
        // deployTypeField.setBounds(150 +insetsSub6.left, 10 + insetsSub6.top, 180, 20);

		cCfg.fill = GridBagConstraints.NONE;


        // Upper Bound RSSI
        JLabel upperBoundLabel = new JLabel("Upper Bound RSSI: ", SwingConstants.RIGHT);
        cCfg.gridx = 0;
        cCfg.gridy = 1;
        cCfg.gridwidth = 1;
        subPanel6.add(upperBoundLabel, cCfg);
        upperBoundLabel.setBounds(30 +insetsSub6.left, 70 + insetsSub6.top, 150, 20);

        upperBoundField = new IntegerEditor(Constants.Min_UpperBound,
                                            Constants.Max_UpperBound,
                                            Constants.Default_UpperBound);
        cCfg.gridx = 1;
        cCfg.gridy = 1;
        cCfg.gridwidth = 1;
        cCfg.ipadx = 50;
        subPanel6.add(upperBoundField, cCfg);
        upperBoundField.setHorizontalAlignment(JTextField.RIGHT);
        // upperBoundField.setBounds(150 + insetsSub6.left, 70 + insetsSub6.top, 90, 20);

        // Delta RSSI
        JLabel deltaRSSILabel = new JLabel("Delta RSSI: ", SwingConstants.RIGHT);
        cCfg.gridx = 0;
        cCfg.gridy = 2;
        cCfg.gridwidth = 1;
        cCfg.ipadx = 0;
        subPanel6.add(deltaRSSILabel, cCfg);
        deltaRSSILabel.setBounds(30 + insetsSub6.left, 90 + insetsSub6.top, 150, 20);

        deltaRSSIField = new IntegerEditor(Constants.Min_DeltaRSSI,
                                           Constants.Max_DeltaRSSI,
                                           Constants.Default_DeltaRSSI);
        cCfg.gridx = 1;
        cCfg.gridy = 2;
        cCfg.gridwidth = 1;
        cCfg.ipadx = 50;
        
        subPanel6.add(deltaRSSIField, cCfg);
        deltaRSSIField.setHorizontalAlignment(JTextField.RIGHT);
        // deltaRSSIField.setBounds(150 + insetsSub6.left, 90 + insetsSub6.top, 90, 20);

		cCfg.fill = GridBagConstraints.NONE;

        // Send Config button
        final JButton configButton = new JButton("Send Config");
        configButton.setMnemonic(KeyEvent.VK_C);
        configButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    upperBoundField.commitEdit();
                    deltaRSSIField.commitEdit();
                }
                catch (java.text.ParseException pe) {

                }

                sendConfigCmd(11, deployTypeField.getSelectedIndex(),
                              ( (Long) upperBoundField.getValue()).intValue(),
                              ( (Long) deltaRSSIField.getValue()).intValue());
            }
        });
        cCfg.gridx = 1;
        cCfg.gridy = 3;
        cCfg.gridwidth = 1;
        subPanel6.add(configButton, cCfg);
        // configButton.setBounds(130 + insetsSub6.left, 130 + insetsSub6.top, 120, 25);

        // Reset Config button
        final JButton resetConfigButton = new JButton("Reset Config");
        resetConfigButton.setMnemonic(KeyEvent.VK_E);
        resetConfigButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendConfigCmd(12, 0, 0, 0);
            }
        });
        cCfg.gridx = 1;
        cCfg.gridy = 4;
        cCfg.gridwidth = 1;
        subPanel6.add(resetConfigButton, cCfg);
        // resetConfigButton.setBounds(130 + insetsSub6.left, 170 + insetsSub6.top, 120,
        //                            25);

        return tabbedPane;
    }

    /** Listens to the slider. */
    class xSliderListener
        implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                xSliderLabel.
                    setText(xSliderLabelPrefix + (int) source.getValue());

                panel.xSection = (int) source.getValue();
                if (panel.xSection == 1) {
                    panel.global = true;
                }
                else {
                    panel.global = false;
                }
                panel.setPreferredSize(new Dimension(50000 * panel.xSection,
                    350 * panel.xSection));
                panel.revalidate();
                panel.repaint();
            }
        }
    }

    /** Listens to the slider. */
    /* class ySliderListener
        implements ChangeListener {
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
          ySliderLabel.setText(ySliderLabelPrefix + (int) source.getValue());
          panel.ySection = (int) source.getValue();
          panel.repaint();
        }
      }
       }*/

    public void actionPerformed(ActionEvent e) {
		String str = e.getActionCommand();
		// System.out.println(str);
		if (str.equals("N")) 
			north = 1;
		else if (str.equals("S")) 
			north = -1;
		else if (str.equals("E")) 
			east = 1;
		else if (str.equals("W")) 
			east = -1;
		else if (str.equals("RN")) 
			refNorth = 1;
		else if (str.equals("RS")) 
			refNorth = -1;
		else if (str.equals("RE")) 
			refEast = 1;
		else if (str.equals("RW")) 
			refEast = -1;
			
		
    }
	
}
