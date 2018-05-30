package net.roarsoftware.intellirc.core;

import net.roarsoftware.net.irc.IrcConnection;

/**
 * The <code>ConnectionManager</code> manages all IrcConnections, notifies {@link ConnectionListener}s and
 * provides methods for connecting to servers and joining channels.
 *
 * 
 * @author Janni Kovacs
 */
public interface ConnectionManager {

	public void connect(ServerSettings server);

	public void join(ServerSettings server, String channel);

	public void closeAllConnections(String quit);

	public void addConnectionListener(ConnectionListener l);

	public void removeConnectionListener(ConnectionListener l);

	public int getConnectionCount();

	public void close(IrcConnection c, String signoffMessage);

}
