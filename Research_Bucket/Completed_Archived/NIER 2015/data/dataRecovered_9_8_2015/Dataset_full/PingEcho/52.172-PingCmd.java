package net.tinyos.task.awtfield;

import java.awt.*;
import java.awt.event.*;
import java.text.*;

class PingCmd extends SimpleCommand {
    PingCmd(Tool parent) {
	super(parent, "Ping");
	parent.addCommand("ping", this);
    }

    public String result(FieldReplyMsg reply) {
	PingReplyMsg p = new PingReplyMsg(reply, reply.offset_result(0));

	return 
	    new DecimalFormat("0.0V").format(0.58 * 1024.0 / p.get_voltage()) +
	    ", Parent " + p.get_parent() +
	    ", RAM " + p.get_freeram() +
	    ", qln " + p.get_qlen() +
	    ", mhq " + p.get_mhqlen() +
	    ", dpth " + p.get_depth() + 
	    ", qual " + p.get_qual() +
	    ", q1 " + p.getElement_qids(0) +
	    ", q2 " + p.getElement_qids(1);
    }
}
