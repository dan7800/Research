import java.awt.*;

class SensorNode implements protocolInterface
{
    /*static final int HEARTBEAT_TIMER = 0;
    static final int BEACON_TIMER = 1;
    static final float HEARTBEAT_TIME = 2.0f;
	 static final float BEACON_TIME = 3.0f;
    static final float TIMEOUT_TIME = 10.0f;*/

    static final int RS_NONE=0;
    static final int RS_MEMBER=1;
    static final int RS_NEWCANDIDATE=2;
    static final int RS_LEADER=3;
    static final int RS_LEADERCANDIDATE=4;
    static final int RS_FOLLOWER=5;
    static final int RS_RESIGNINGLEADER=6;
    static final int RS_BASELEADER=7;
    static final int MAX_LENGTH=10000;
  float x1=-1;
  float y1=-1;
  float x2=-1;
  float y2=-1;
  public int size1 = 0;
  public int size2 = 0;
	physicalInterface phy;
    static XYAddress[] track1 = new XYAddress[MAX_LENGTH];
    static XYAddress[] track2 = new XYAddress[MAX_LENGTH];
    XYAddress addr = null;
    int sensorID;
    
    public int state;
    
    SensorNetwork sensor_net;
    
    public SensorNode( physicalInterface p, int sID, SensorNetwork sn)
    {
        int i;
        for (i = 0; i < MAX_LENGTH; i ++)
	{
	        XYAddress addr1 = new XYAddress(-1, -1);
	        track1[i] = addr1;
	        XYAddress addr2 = new XYAddress(-1, -1);
	        track2[i] = addr2;
	}
	size1 = 0;
	size2 = 0;

    	sensorID = sID;
        phy = p;
        state =RS_NONE;
        sensor_net=sn;
    }

    public int getSize1() {return this.size1;}

    public Color getColor()
    {
    	switch(state){
    		case RS_NONE:
    			return Color.black;
    		case RS_FOLLOWER:
    			return Color.yellow;
    		case RS_MEMBER:
    			return Color.green;
    		case RS_NEWCANDIDATE:
    			return Color.black;
    		case RS_LEADERCANDIDATE:
    			return Color.blue;
    		case RS_LEADER:
    			return Color.red;
    		case RS_RESIGNINGLEADER:
    			return Color.black;
    		case RS_BASELEADER:
    			return Color.white;
    	}
        return Color.orange;
    }

    public int getShape()
    {
	 return protocolInterface.shapeCircle;
    }
    public XYAddress getAddr1()
    {
    	XYAddress addr = new XYAddress(x1, y1);
//    	addr.x = (int)(x);
//    	addr.y = (int)(y);
    	return addr;
  	}
    public XYAddress getAddr2()
    {
    	XYAddress addr = new XYAddress(x2, y2);
//    	addr.x = (int)(x);
//    	addr.y = (int)(y);
    	return addr;
    }
    public XYAddress[] getTrack1()
    {
    	return track1;
    }
    public XYAddress[] getTrack2()
    {
    	return track2;
    }
    public int getSensorID(){ return sensorID; }
	
	int stringToState(String str){
		str=str.toUpperCase();
		if(str.equals("NONE"))return RS_NONE;
		if(str.equals("FOLLOWER"))return RS_FOLLOWER;
		if(str.equals("MEMBER"))return RS_MEMBER;
		if(str.equals("NEWCANDIDATE"))return RS_NEWCANDIDATE;
		if(str.equals("LEADERCANDIDATE"))return RS_LEADERCANDIDATE;
		if(str.equals("LEADER"))return RS_LEADER;
		if(str.equals("RESIGNINGLEADER"))return RS_RESIGNINGLEADER;
		if(str.equals("BASELEADER"))return RS_BASELEADER;
		
		return RS_NONE;
		
	}
	public String getStateString(){
		switch(state){
    		case RS_NONE:
    			return "";
    		case RS_FOLLOWER:
    			return "Follower";
    		case RS_MEMBER:
    			return "Member";
    		case RS_NEWCANDIDATE:
    			return "NewCandidate";
    		case RS_LEADERCANDIDATE:
    			return "LeaderCandidate";
    		case RS_LEADER:
    			return "Leader";
    		case RS_RESIGNINGLEADER:
    			return "ResigningLeader";
    		case RS_BASELEADER:
    			return "BL";
    	}
    	return "";
	}
    public String getDetails(){
    	return ""+sensorID+"-"+getStateString();
    }
    
    //*protocol handling
    /** handle timer event */
    public void handleTimer( int index){}

    /** handle message event */
    public void handleMsg( SimEventMsg msg ){}
    
    public String leaderInfo="";
    
    public String getProcessInfo(){
    	if(state==RS_NONE || state==RS_RESIGNINGLEADER)return "";
    	else return leaderInfo;	
    }
    
    public void handleEvent(SimEvent se){
    	SensorEvent ssevent=(SensorEvent)se;
    	
        if(ssevent.eventType()==SensorEvent.SSEVENT_UPDATE){
        	try{
        		state=stringToState(ssevent.event_info);
        	}catch(Exception e){}	
        }else if(ssevent.eventType()==SensorEvent.SSEVENT_REPORT){
        	leaderInfo=ssevent.event_info;
        }else if(ssevent.eventType()==SensorEvent.SSEVENT_SENDMSG){
        	try{
        		int dest_id=Integer.parseInt(ssevent.event_info);
        		OverlayNode dest=sensor_net.findNode(dest_id);
        		//unicast message;
        		if(dest!=null){
	        		float rcv_time=ssevent.getTime()+((SensorEventProcessor)sensor_net.seq).start_time;
	        		rcv_time+=((OverlayNode)phy).getDistanceTo(dest)/sensor_net.packet_speed;
	        			
	       			SensorMsg msg=new SensorMsg((OverlayNode)phy, dest, ssevent.event_info, rcv_time);
	        			
	       			sensor_net.seq.insertEvent(msg);
       			}
        	}catch(Exception e){
        		//broadcast message;
        		float bcast_dist;
        		if(ssevent.event_info.equals("BCT2"))
        			bcast_dist=sensor_net.bcast_dist*2.2f;
        		else bcast_dist=sensor_net.bcast_dist*1.2f;
        		
        		float rcv_time=ssevent.getTime()+((SensorEventProcessor)sensor_net.seq).start_time;
        		rcv_time+=bcast_dist/sensor_net.packet_speed;
        			
       			SensorMsg msg=new SensorMsg((OverlayNode)phy, null, ssevent.event_info, rcv_time);
        		msg.BCAST_DIST=bcast_dist;
        		
       			sensor_net.seq.insertEvent(msg);

        	}
        		
        }else if(ssevent.eventType()==SensorEvent.SSEVENT_TARGET_1){
        		x1 = ssevent.event_info_x;
			y1 = ssevent.event_info_y;
						//System.out.println("x = "+x+" Y= "+y);
        }else if(ssevent.eventType()==SensorEvent.SSEVENT_TARGET_2){
        		x2 = ssevent.event_info_x;
			y2 = ssevent.event_info_y;
						//System.out.println("x = "+x+" Y= "+y);
        }else if(ssevent.eventType()==SensorEvent.SSEVENT_TRACK_1){
        	int i = 0;
        	while ((track1[i].getFloatX() != -1) && (track1[i].getFloatY() != -1))
        	  i ++;
		System.out.println("====" + this.sensorID + "======x " + track1[0].getFloatX() + " y = " + track1[0].getFloatY());
		System.out.println("==========x " + track1[1].getFloatX() + " y = " + track1[1].getFloatY());
		System.out.println("==========x " + track1[2].getFloatX() + " y = " + track1[2].getFloatY());
		System.out.println("==========x " + track1[3].getFloatX() + " y = " + track1[3].getFloatY());
       		float x3 = ssevent.event_info_x;
		float y3 = ssevent.event_info_y;
		addr = new XYAddress(x3, y3);
		track1[i] = addr;
        }else if(ssevent.eventType()==SensorEvent.SSEVENT_TRACK_2){
        	int i = 0;
        	while ((track2[i].getFloatX() != -1) && (track2[i].getFloatY() != -1))
        	  i ++;
       		float x4 = ssevent.event_info_x;
		float y4 = ssevent.event_info_y;
		addr = new XYAddress(x4, y4);
		track2[i] = addr;
        }
        
    }

    /** start the protocol at this logical node */
    public void startProtocol( ){}
    /** reset the protocol to its default state */
    public void resetProtocol(){
    	state=RS_NONE;	
    }
    
}

class SensorMsg extends SimEventMsg{
	String info;
	float BCAST_DIST=OverlayNode.PACKET_BCAST_DIST+0.1f;

	public SensorMsg(OverlayNode osrc,OverlayNode odst, String text, float ctime)
    {
        super( osrc, odst, 0);
        time=ctime;
        info=text;
    }
	/**
     * return message's label, used for display
     */
	public String getMsgLabel(int i)
	{
  		return info;
	}
}
