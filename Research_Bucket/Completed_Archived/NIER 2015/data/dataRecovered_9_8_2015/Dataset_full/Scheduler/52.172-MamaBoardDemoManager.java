package com.shockfish.tinyos.apps.demo;

import java.util.Timer;

import com.shockfish.tinyos.tools.MamaBoardManager;
import com.shockfish.tinyos.net.CldcFtpClient;
import com.shockfish.tinyos.gateway.UpdateDate;
import com.shockfish.tinyos.gateway.SenderTask;
import com.shockfish.tinyos.gateway.SenderTimerControl;
import com.shockfish.tinyos.tools.CldcLogger;


public class MamaBoardDemoManager extends MamaBoardManager implements SenderTimerControl {


	private final static String OTAP_SECRET = "mysecret";
	
    private OscopeDataCaptureThread oscopeThread;
    private SenderTask senderTask;
    private Timer senderTimer;
    private UpdateDate updateDate;

    
	public MamaBoardDemoManager() {
		super();
        // GPIO and packetizer are initialized after this.
        CldcLogger.setLevelOfDisplay(CldcLogger.DEBUG_LEVEL);
        
        oscopeThread = new OscopeDataCaptureThread(this);
        
        // kick the capture thread
        oscopeThread.start();

        // kick the sender thread
        startSenderTimer();
        
        // kick the time update thread
        updateDate= new UpdateDate(this);
        updateDate.start();
	}

    
    // this is terribly ugly and needs to be changed.
    public void startSenderTimer() {
        this.senderTask = new SenderTask(this, oscopeThread.buffer, this);
        this.senderTimer = new Timer();
        this.senderTimer.schedule(senderTask, 20 * 1000);
    }
    
    public void stopSenderTimer() {
        senderTimer.cancel();
    }
    
	protected String getSecret() {
		return OTAP_SECRET;
	}	
	
    public void enterBridgeMode() {	 
        // TODO stop the capture thread
    }
    public void leaveBridgeMode() {  
        // TODO restart the capture thread
    }
	
	protected boolean customCommandHandler(String command, String args) {

		if (command.equals("SENDSTATUSFTP")) {
			uploadStatus();
			return true;
		}
		return false;
	}

	void uploadStatus() {
		try {
            CldcLogger.info("Uploading ");
			CldcFtpClient ftp = new CldcFtpClient();
            String report = "Report\n" + getTc65Status();
			String fileName = "gprseport" + System.currentTimeMillis() + ".txt";
            ftp.connect("shockfish.com", 21, "tinynodetest", "shockfish", getGprsConf());
			ftp.ascii();
			ftp.putData(report, fileName);
			ftp.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}