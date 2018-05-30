package net.roarsoftware.net.irc;

import java.util.Date;

public class DefaultCtcpHandler implements CtcpHandler {

	protected static String INTERNAL_VERSION = IrcConnection.INTERNAL_VERSION;

	public void handleQuery(CtcpSender s, String source, String query, String param) {
		if(query.startsWith("PING")) {
			s.sendCtcpReply(source, "PING " + param);
		} else if(query.equals("VERSION")) {
			s.sendCtcpReply(source, "VERSION " + DefaultCtcpHandler.INTERNAL_VERSION);
		} else if(query.equals("TIME")) {
			s.sendCtcpReply(source, "TIME " + new Date());
		} else {
		//	System.err.println("unknown CTCP query: " + query);
		}
	}

}
