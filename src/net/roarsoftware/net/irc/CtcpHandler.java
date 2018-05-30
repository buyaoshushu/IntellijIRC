package net.roarsoftware.net.irc;

public interface CtcpHandler {

	public void handleQuery(CtcpSender s, String source, String query, String param);

}
