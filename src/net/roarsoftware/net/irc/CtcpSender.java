package net.roarsoftware.net.irc;

public interface CtcpSender {

	public void sendCtcpReply(String target, String msg);
}
