package net.roarsoftware.intellirc.core;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.roarsoftware.net.irc.IdentServer;
import net.roarsoftware.net.irc.IrcConnection;
import net.roarsoftware.net.irc.MessageListener;

/**
 * Default Implementation of <code>ConnectionManager</code>.
 *
 * @author Janni Kovacs
 */
public class ConnectionManagerImpl implements ConnectionManager {

	private List<ConnectionListener> listeners = new ArrayList<ConnectionListener>();
	private Map<ServerSettings, IrcConnection> connections = new HashMap<ServerSettings, IrcConnection>();

	public void connect(final ServerSettings server) {
		if (connections.containsKey(server)) {
			return;
		}
		IrcSettings settings = IrcPlugin.getInstance().getSettings();
		if (settings.isEnableIdentd()) {
			IdentServer s = new IdentServer(settings.getIdentdUser(), settings.getIdentdSystem(),
					settings.getIdentdPort());
			s.start();
		}
		final IrcConnection connection = IrcConnection.openConnection(server.getHost());
		connection.setEncoding(System.getProperty("file.encoding"));
		MessageAdapter messageAdapter = new MessageAdapter() {

			@Override
			public void onDisconnect(boolean error) {
				connections.remove(server);
				for (ConnectionListener listener : listeners) {
					listener.connectionClosed(connection);
				}
			}
		};
		connections.put(server, connection);
		for (ConnectionListener listener : listeners) {
			listener.connectionOpened(connection);
		}
		connection.addMessageListener(messageAdapter);
		try {
			connection.connect(server.getNick(), server.getUser(), server.getReal(), server.getPassword(),server.getPort());
		} catch (IOException e) {
			String message = String
					.format(IrcPlugin.getString("irc.error-on-connect"), server.getHost(), e.getMessage());
			if (e instanceof UnknownHostException)
				message = String.format(IrcPlugin.getString("irc.unknown-host"), server.getHost());
			for (MessageListener listener : connection.getMessageListeners()) {
				listener.onStatus(message);
				listener.onDisconnect(true);
			}
			connection.removeMessageListener(messageAdapter);
			connections.remove(server);
		}
	}

	public void join(ServerSettings server, String channel) {
		if (!connections.containsKey(server)) {
			connect(server);
		}
		IrcConnection ircConnection = connections.get(server);
		if (ircConnection != null)
			ircConnection.join(channel);
	}

	public void closeAllConnections(String quit) {
		for (IrcConnection connection : connections.values()) {
			try {
				connection.disconnect(quit);
			} catch (IOException e) {
				for (MessageListener listener : connection.getMessageListeners()) {
					listener.onStatus(e.getMessage());
					listener.onDisconnect(true);
				}
			} finally {
				for (ConnectionListener listener : listeners) {
					listener.connectionClosed(connection);
				}
			}
		}
	}

	public void addConnectionListener(ConnectionListener l) {
		listeners.add(l);
	}

	public void removeConnectionListener(ConnectionListener l) {
		listeners.remove(l);
	}

	public int getConnectionCount() {
		return connections.size();
	}

	public void close(IrcConnection c, String signoffMessage) {
		try {
			if (!c.isConnected()) {
				for (MessageListener listener : c.getMessageListeners()) {
					listener.onDisconnect(false);
				}
			} else {
				c.disconnect(signoffMessage);
			}
		} catch (IOException e) {
			for (MessageListener listener : c.getMessageListeners()) {
				listener.onStatus(e.getMessage());
				listener.onDisconnect(true);
			}
		} finally {
			for (ConnectionListener listener : listeners) {
				listener.connectionClosed(c);
			}
		}
	}
}
