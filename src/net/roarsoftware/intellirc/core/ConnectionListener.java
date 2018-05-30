package net.roarsoftware.intellirc.core;

import net.roarsoftware.net.irc.IrcConnection;

/**
 * A <code>ConnectionListener</code> is notified when IrcConnections are being opened or closed.
 * 
 * @author Janni Kovacs
 */
public interface ConnectionListener {

	public void connectionOpened(IrcConnection c);

	public void connectionClosed(IrcConnection c);
}
