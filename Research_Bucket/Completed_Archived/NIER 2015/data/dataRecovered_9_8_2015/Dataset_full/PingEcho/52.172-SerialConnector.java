/**
 * <p>Title: SOWN Mote Field Setup</p>
 * <p>Description: Setup GUI sor SOWN Mote Field
 * called by Xetron Cardinal MissionGUI</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: UVa</p>
 * @author Ting Yan, Tian He, etc.
 * @version 1.2
 */

import java.io.*;
import net.tinyos.message.*;
import net.tinyos.packet.*;
import java.util.*;

public class SerialConnector {
    static private FieldPanel panel = null;
    static private SOWNMoteFieldGUI app = null;
    private net.tinyos.packet.PacketSource serialStub = null;
    private PacketReader packetReader = null;
    private boolean mica2dot = false;
    
	BufferedWriter bwout =null;
        private String PrintHexByte1(byte b) {
            int i = (int) b;
            i = (i >= 0) ? i : (i + 256);
            return Integer.toHexString(i / 16);
        }

        // get the second character of a hex string
        private String PrintHexByte2(byte b) {
            int i = (int) b;
            i = (i >= 0) ? i : (i + 256);
            return Integer.toHexString(i % 16);
        }

        // print a byte value in Hex
        private void PrintHexByte(byte b) {
            int i = (int) b;
            i = (i >= 0) ? i : (i + 256);
            System.out.print(Integer.toHexString(i / 16));
            System.out.print(Integer.toHexString(i % 16));
        }

    public void sendQueryParams(byte b0, byte b1, byte b2, byte b3, byte b4,
                                byte sequence) {
        byte[] b = new byte[21];
        b[0] = b0;
        b[1] = b1;
        b[2] = b2;
        b[3] = b3;
        b[4] = b4;
        b[5] = 3;
        b[6] = 1;
        b[7] = sequence;
        for (int i = 8; i < 21; i++) {
            b[i] = 0;
        }
        b[11] = 5; // attribute type
        for (int i = 0; i < 21; i++) {
         PrintHexByte(b[i]);
         System.out.print(" ");
           }
           System.out.println();
      
        try {
            if (serialStub != null) {
                serialStub.writePacket(b);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(byte b0, byte b1, byte b2, byte b3, byte b4,
                            byte[] b) {
        byte[] b_all = new byte[b4+5];
        b_all[0] = b0;
        b_all[1] = b1;
        b_all[2] = b2;
        b_all[3] = b3;
        b_all[4] = b4;
        for (int i = 0; i < b4; i++) {
            b_all[5 + i] = b[i];
            /*PrintHexByte(b[i]);
                System.out.print(" ");*/
        }
        //System.out.println();
        for (int i=0; i < b_all.length; i++) {
         PrintHexByte(b_all[i]);
         System.out.print(" ");
           }
           System.out.println();
        try {
            if (serialStub != null) {
                serialStub.writePacket(b_all);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendShort(byte b0, byte b1, byte b2, byte b3, byte b4,
                          byte[] b) {
        byte[] b_all = new byte[23];
        b_all[0] = b0;
        b_all[1] = b1;
        b_all[2] = b2;
        b_all[3] = b3;
        b_all[4] = b4;
        for (int i = 0; i < 18; i++) {
            b_all[5 + i] = b[i];
            /*PrintHexByte(b[i]);
                System.out.print(" ");*/
        }
        //System.out.println();
        for (int i=0; i < b_all.length; i++) {
         PrintHexByte(b_all[i]);
         System.out.print(" ");
           }
           System.out.println();
        try {
            if (serialStub != null) {
                serialStub.writePacket(b_all);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendRef(byte b0, byte b1, byte b2, byte b3, byte b4,
                          byte[] b) {
        byte[] b_all = new byte[25];
        b_all[0] = b0;
        b_all[1] = b1;
        b_all[2] = b2;
        b_all[3] = b3;
        b_all[4] = b4;
        for (int i = 0; i < 20; i++) {
            b_all[5 + i] = b[i];
            /*PrintHexByte(b[i]);
                System.out.print(" ");*/
        }
        //System.out.println();
        for (int i=0; i < b_all.length; i++) {
         PrintHexByte(b_all[i]);
         System.out.print(" ");
           }
           System.out.println();
        try {
            if (serialStub != null) {
                serialStub.writePacket(b_all);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendPing(byte b0, byte b1, byte b2, byte b3, byte b4,
                          byte[] b) {
        byte[] b_all = new byte[8];
        b_all[0] = b0;
        b_all[1] = b1;
        b_all[2] = b2;
        b_all[3] = b3;
        b_all[4] = b4;
        for (int i = 0; i < 3; i++) {
            b_all[5 + i] = b[i];
            /*PrintHexByte(b[i]);
                System.out.print(" ");*/
        }
        //System.out.println();
        for (int i=0; i < b_all.length; i++) {
         PrintHexByte(b_all[i]);
         System.out.print(" ");
           }
           System.out.println();
        try {
            if (serialStub != null) {
                serialStub.writePacket(b_all);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
   }
   
    private short calculateCRC(byte packet[]) {
        short crc;
        int i;
        int index = 0;
        int count = packet.length - 2;
        crc = 0;
        while (--count >= 0) {
            crc = (short) (crc ^ ( (short) (packet[index++]) << 8));
            i = 8;
            do {
                if ( (crc & 0x8000) != 0) {
                    crc = (short) (crc << 1 ^ ( (short) 0x1021));
                }
                else {
                    crc = (short) (crc << 1);
                }
            }
            while (--i > 0);
        }
        return (crc);
    }

    SerialConnector(SOWNMoteFieldGUI gui, FieldPanel pane, boolean replayMode, boolean mica2dot) {
        panel = pane;
        app = gui;
        try {
        	if (!replayMode) {
        		if (mica2dot){
            		serialStub = BuildSource.makePacketSource("serial@COM1:19200");
            		this.mica2dot = mica2dot;
            	}
            	else
            		serialStub = BuildSource.makePacketSource("serial@COM1:57600");
                Date startDate = new Date();
	   			try {
	  				bwout = new BufferedWriter(new FileWriter
	  				(startDate.toString().replaceAll(" ", "_").replaceAll(":", "_").concat("_log.txt")), 1024);
	  				
	  			} catch (IOException e) {
	  				e.printStackTrace();
	  			}

            }
            
            else 
            	serialStub = BuildSource.makePacketSource("old-network@127.0.0.1:9000");
            serialStub.open(net.tinyos.util.PrintStreamMessenger.err);
            packetReader = new SerialConnector.PacketReader();
            packetReader.start();
        }
        catch (Exception e) {
            //e.printStackTrace();
            System.exit(1);
        }

    }

    void connectSerial() {
        try {
        	if (mica2dot)
            	serialStub = BuildSource.makePacketSource("serial@COM1:19200");
            else
            	serialStub = BuildSource.makePacketSource("serial@COM1:57600");
            serialStub.open(net.tinyos.util.PrintStreamMessenger.err);
            packetReader = new SerialConnector.PacketReader();
            packetReader.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    void disconnectSerial() {
        try {
            packetReader.stopRun();
            serialStub.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected class PacketReader
        extends Thread {
        boolean run = true;
        byte[] packet = null;

        public void stopRun() {
            run = false;
        }

        // get the unsigned int value from the byte at certain offset
        private int OneByte(byte[] packet, int offset) {
            int p = (int) packet[offset];
            return ( (p >= 0) ? p : (p + 256));
        }

        // get the unsigned int value from two bytes at certain offset
        private int TwoBytes(byte[] packet, int offset) {
            int p0 = (int) packet[offset];
            int p1 = (int) packet[offset + 1];
            p0 = (p0 >= 0) ? p0 : (p0 + 256);
            p1 = (p1 >= 0) ? p1 : (p1 + 256);
            return (p0 + p1 * 256);
        }

        // get the signed int value from two bytes at certain offset
        private int SignedTwoBytes(byte[] packet, int offset) {
            int p0 = (int) packet[offset];
            int p1 = (int) packet[offset + 1];
            p0 = (p0 >= 0) ? p0 : (p0 + 256);
            // p1 = (p1 >= 0) ? p1 : (p1 + 256);
            return (p0 + p1 * 256);
        }

        // get the unsigned int value from four bytes at certain offset
        private long FourBytes(byte[] packet, int offset) {
            int p0 = (int) packet[offset];
            int p1 = (int) packet[offset + 1];
            int p2 = (int) packet[offset + 2];
            int p3 = (int) packet[offset + 3];
            p0 = (p0 >= 0) ? p0 : (p0 + 256);
            p1 = (p1 >= 0) ? p1 : (p1 + 256);
            p2 = (p2 >= 0) ? p2 : (p2 + 256);
            p3 = (p3 >= 0) ? p3 : (p3 + 256);
            int lower = p0 + p1 * 256 + p2 * 65536;
            long result = (long) lower + ( (long) p3) * 16777216L;
            return result;
        }

        // get the first character of a hex string
        private String PrintHexByte1(byte b) {
            int i = (int) b;
            i = (i >= 0) ? i : (i + 256);
            return Integer.toHexString(i / 16);
        }

        // get the second character of a hex string
        private String PrintHexByte2(byte b) {
            int i = (int) b;
            i = (i >= 0) ? i : (i + 256);
            return Integer.toHexString(i % 16);
        }

        // print a byte value in Hex
        private void PrintHexByte(byte b) {
            int i = (int) b;
            i = (i >= 0) ? i : (i + 256);
            System.out.print(Integer.toHexString(i / 16));
            System.out.print(Integer.toHexString(i % 16));
        }

        public void run() {
            try {
                while (run) {
                    packet = serialStub.readPacket();
                    Date dt = new Date();
					String str = Long.toString(dt.getTime());
					if (bwout != null) {
						bwout.write(str, 0, str.length());
					}
                    for (int k = 0; k < packet.length; k++) {
                        PrintHexByte(packet[k]);
                        System.out.print(" ");
						if (bwout != null) {
							try {
								bwout.write(" ", 0, 1);
								bwout.write(PrintHexByte1(packet[k]), 0, 1);
								bwout.write(PrintHexByte2(packet[k]), 0, 1);
							} catch (IOException e) {}
						}

                    }
                    System.out.println();
                    if (bwout != null) {
						bwout.newLine();
    	                bwout.flush();
                    }
                    byte flag = packet[Constants.flagPosition];
                    byte recordType = packet[Constants.recordTypePosition];
                    // System.out.println("flag: " + flag + " type: " + type);
                    flag &= 0x06;
                    if (flag == 0x04) {
                        if (recordType == 1 || recordType == 2 ||
                            recordType == 3 ||
                            recordType == 5) {
                            if (recordType == 1) {
                                // tracking
                                int tEventID = 0;
                                int tEventType = 0;
                                int tReportType = 0;
                                int tLeaderID = 0;
                                int tXVelocity = 0;
                                int tYVelocity = 0;
                                int tXCoord = 0;
                                int tYCoord = 0;
                                long tTimestamp = 0;
                                int tConfidenceLevel = 0;
                                int tMagnetNumber = 0;
                                int tMotionNumber = 0;
                                int tAcousticNumber = 0;
                                int tDelays = 0;
                                int tTimeDiff = 0;
                                int tGroupSize = 0;
                                tEventID = OneByte(packet,
                                    Constants.tEventIDPosition);
                                tEventType = OneByte(packet,
                                    Constants.tEventTypePosition);
                                tReportType = OneByte(packet, 
                                	Constants.tReportTypePosition);
                                if (Constants.DebugSwitch) {
	                                tLeaderID = TwoBytes(packet,
	                                    Constants.tLeaderIDDebugPosition);
	                                tXVelocity = SignedTwoBytes(packet,
	                                    Constants.tXVelocityDebugPosition);
	                                tYVelocity = SignedTwoBytes(packet,
	                                    Constants.tYVelocityDebugPosition);
	                                tXCoord = SignedTwoBytes(packet,
	                                    Constants.tXCoordDebugPosition);
	                                tYCoord = SignedTwoBytes(packet,
	                                    Constants.tYCoordDebugPosition);
	                                tTimestamp = FourBytes(packet,
	                                    Constants.tTimestampDebugPosition);
	                                tConfidenceLevel = OneByte(packet,
	                                    Constants.tConfidenceLevelDebugPosition);
	                                tMagnetNumber = OneByte(packet,
	                                    Constants.tMagnetNumberDebugPosition);
	                                tMotionNumber = OneByte(packet,
	                                    Constants.tMotionNumberDebugPosition);
	                                tAcousticNumber = OneByte(packet,
	                                    Constants.tAcousticNumberDebugPosition);
	                                tTimeDiff = SignedTwoBytes(packet,
	                                    Constants.tTimeDiffDebugPosition);
	                                tGroupSize = OneByte(packet,
	                                    Constants.tGroupSizeDebugPosition);
                                }
                                else {
	                                tXVelocity = SignedTwoBytes(packet,
	                                    Constants.tXVelocityPosition);
	                                tYVelocity = SignedTwoBytes(packet,
	                                    Constants.tYVelocityPosition);
	                                tXCoord = SignedTwoBytes(packet,
	                                    Constants.tXCoordPosition);
	                                tYCoord = SignedTwoBytes(packet,
	                                    Constants.tYCoordPosition);
	                                tTimestamp = FourBytes(packet,
	                                    Constants.tTimestampPosition);
	                                tConfidenceLevel = OneByte(packet,
	                                    Constants.tConfidenceLevelPosition);
	                                tDelays = TwoBytes(packet,
	                                    Constants.tDelaysPosition);
	                                tTimeDiff = OneByte(packet,
	                                    Constants.tTimeDiffPosition);
                                }
                                /* if (app.bwout != null) {
                                 try {
                                  for (int k=0; k < packet.length; k++) {
                                 app.bwout.write(PrintHexByte1(packet[k]), 0,1);
                                 app.bwout.write(PrintHexByte2(packet[k]), 0,1);
                                     app.bwout.write(" ");
                                  }
                                 } catch (IOException e) {}
                                         }
                                     try {
                                      String str = Long.toString(dt.getTime());
                                      if (app.bwout != null) {
                                       app.bwout.write(str, 0, str.length());
                                       app.bwout.newLine();
                                       app.bwout.flush();
                                      }

                                    } catch (IOException e) {}*/

                                panel.printTrackingMsg(tEventID, tEventType, tReportType, 
                                    tLeaderID,
                                    tXVelocity, tYVelocity, tXCoord, tYCoord,
                                    tTimestamp,
                                    tConfidenceLevel, tDelays, 
                                    tTimeDiff);

                                panel.receivedTrackingMsg(tEventID, tEventType, tReportType, 
                                    tLeaderID,
                                    tXVelocity, tYVelocity, tXCoord, tYCoord,
                                    tTimestamp,
                                    tConfidenceLevel, tDelays, 
                                    tTimeDiff);


                            }
                            else if (recordType == 2) {
                                // node status
                                int iNodeID = 0;
                                int iXCoord = 0;
                                int iYCoord = 0;
                                int iSentryID = 0;
                                int iParentID = 0;
                                int iNumSentries = 0;
                                int iNumNbrs = 0;
                                int iVoltage = 0;
                                int iState = 0;
                                int iSensorStatus = 0;
                                long iTime = 0;
                                iNodeID = TwoBytes(packet,
                                    Constants.iNodeIDPosition);
                                iXCoord = SignedTwoBytes(packet,
                                    Constants.iXCoordPosition);
                                iYCoord = SignedTwoBytes(packet,
                                    Constants.iYCoordPosition);
                                iSentryID = TwoBytes(packet,
                                    Constants.iSentryIDPosition);
                                iParentID = TwoBytes(packet,
                                    Constants.iParentIDPosition);
                                iNumSentries = OneByte(packet,
                                    Constants.iNumSentriesPosition);
                                iNumNbrs = OneByte(packet,
                                    Constants.iNumNbrsPosition);
                                iVoltage = TwoBytes(packet,
                                    Constants.iVoltagePosition);
                                iState = OneByte(packet,
                                                 Constants.iStatePosition);
                                iSensorStatus = OneByte(packet,
                                    Constants.iSensorStatusPosition);
                                if (Constants.DebugSwitch)
                                	iTime = FourBytes(packet,
                                                  Constants.iTimePosition);
                                panel.printIndividualNodeMsg(iNodeID, iXCoord,
                                    iYCoord,
                                    iSentryID,
                                    iParentID, iNumSentries, iNumNbrs,
                                    iVoltage, iState, iSensorStatus,
                                    iTime);
                                panel.receivedIndividualNodeMsg(iNodeID,
                                    iXCoord, iYCoord,
                                    iSentryID, iParentID,
                                    iNumSentries, iNumNbrs,
                                    iVoltage, iState, iSensorStatus,
                                    iTime);

                            }
                            else if (recordType == 3) {
                                // network status
                                // network configuration notification
                                int nNetworkPhase = OneByte(packet,
                                    Constants.nNetworkPhasePosition);
                                if (nNetworkPhase == 1) { // if INIT, disable
                                    if (app != null) {
                                        app.disableMasterClockResetButton();
                                    }
                                }
                                else { //
                                    if (app != null) {
                                        app.enableMasterClockResetButton();
                                    }
                                }

                                if (nNetworkPhase >= 0 && nNetworkPhase <= 4) {
                                    app.resetAll();
                                }
                                long nNetworkTime = FourBytes(packet,
                                    Constants.
                                    nNetworkTimePosition);
                                int nNetworkRound = OneByte(packet,
                                    Constants.nNetworkRoundPosition);
                                int nBaseID = TwoBytes(packet,
                                    Constants.nBaseIDPosition);
                                long nLatitude = FourBytes(packet,
                                    Constants.nLatitudePosition);
                                long nLongitude = FourBytes(packet,
                                    Constants.nLongitudePosition);

                                /* long nRotationPeriod = FourBytes(packet,
                                                                 Constants.
                                 nRotationPeriodPosition); */
                                panel.printNetworkConfiguration(nNetworkRound,
                                    nNetworkPhase,
                                    nBaseID,
                                    nLatitude,
                                    nLongitude,
                                    nNetworkTime);

                            }
                            else if (recordType == 5) {
                                System.out.println("param!");
                                int paramLength = 19;
                                if (Constants.DebugSwitch)
                                	paramLength = 22;
                                byte[] parameters = new byte[paramLength];
                                for (int k = 0; k < paramLength; k++) {
                                    parameters[k] = packet[k + 8];
                                }
                                FileOutputStream fOutput = null;
                                try {
                                    fOutput = new FileOutputStream(
                                        SOWNMoteFieldGUI.configFile);
                                }
                                catch (FileNotFoundException fnfe) {
                                    System.err.
                                        println("File cannot be written");
                                    return;
                                }

                                try {
                                    fOutput.write(parameters);
                                    fOutput.close();
                                }
                                catch (IOException ioe) {
                                    ioe.printStackTrace();
                                    return;
                                }

                                app.setParameters(parameters);

                            }

                        }

                    }

                }
            }
            catch (Exception e) {
            }
        }
    }

}
