package net.roarsoftware.intellirc.core;

import net.roarsoftware.net.irc.MessageListener;
import net.roarsoftware.net.irc.ChannelMessage;
import net.roarsoftware.net.irc.User;

/**
 * Adapter Implementation of MessageListener.
 * 
 * @author Janni Kovacs
 */
public abstract class MessageAdapter implements MessageListener {
	public void onConnect() {
	}

	public void onDisconnect(boolean error) {
	}

	public void onChannelMessage(ChannelMessage msg) //String target, String nick, String msg);
	{
	}

	public void onStatus(String msg) {
	}

	public void onNames(String channel, User[] users) {
	}

	public void onServerReply(String line, int code, String[] params, String msg) {
	}

	public void onCtcpReply(String target, String query, String reply) {
	}
}
