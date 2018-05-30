package net.roarsoftware.intellirc.ui;

import java.awt.Color;
import javax.swing.JComponent;

import net.roarsoftware.intellirc.core.IrcPlugin;
import net.roarsoftware.net.irc.ChannelMessage;
import net.roarsoftware.net.irc.IrcConnection;
import net.roarsoftware.net.irc.IrcUtil;
import net.roarsoftware.net.irc.MessageListener;
import net.roarsoftware.net.irc.MessageType;
import net.roarsoftware.net.irc.User;

/**
 * @author Janni Kovacs
 */
public class ConnectionHandler implements MessageListener {

	private IrcConnection connection;
	private TabGroup tabs;
	private StatusPanel hostTab;

	public ConnectionHandler(IrcConnection c, TabGroup tabs) {
		this.connection = c;
		this.tabs = tabs;
		hostTab = new StatusPanel();
		hostTab.setConnection(c);
		tabs.addTab(c.getHost(), hostTab);
		c.addMessageListener(this);
	}

	public TabGroup getTabGroup() {
		return tabs;
	}

	public void onConnect() {
	}

	public void onDisconnect(boolean error) {
		hostTab.appendMessage(IrcPlugin.getString("irc.you-quit"));
		if (!error) {
			tabs.removeAll();
		}
	}

	public void onChannelMessage(ChannelMessage msg) {
		MessageType type = msg.getType();
		String nick = msg.getSource() != null ? IrcUtil.getNick(msg.getSource()) : null;
		boolean we = nick != null && nick.equals(connection.getNick());
		String message = msg.getMessage();
		String target = msg.getTarget();
		ChatPanel panel = getPanel(target);
		switch (type) {
			case JOIN:
				if (we) {
					tabs.addTab(message, new ChatPanel(connection, message));
				} else {
					panel = getPanel(msg.getMessage());
					panel.userJoined(nick);
				}
				break;
			case PART:
				if (we) {
					tabs.removeTab(target);
				} else {
					panel.appendMessage(String.format(IrcPlugin.getString("irc.part"), nick, target));
					panel.userLeft(nick);
				}
				break;
			case QUIT:
				for (JComponent component : tabs.getAllTabs()) {
					if (component instanceof ChatPanel) {
						ChatPanel p = (ChatPanel) component;
						if (p.getUser(nick) != null) {
							p.appendMessage(String.format(IrcPlugin.getString("irc.quit"), nick, message));
							p.userLeft(nick);
						}
					}
				}
				break;
			case PRIVMSG:
			case PRIVMSG_SENT:
				String title = target;
				if (!IrcUtil.isChannel(target)) {
					if (connection.getNick().equals(nick)) {
						nick = target;
					}
					panel = getPanel(title = nick);
					if (panel == null) {
						panel = new ChatPanel(connection, nick);
						tabs.addTab(title, panel);
					}
				}
				panel.appendMessage(msg);
				int index = tabs.getTabbedPane().indexOfTab(title);
				if (index != -1 && index != tabs.getTabbedPane().getSelectedIndex()) {
					tabs.getTabbedPane().setForegroundAt(index, Color.RED);
				}
				break;
			case NOTICE:
				((StatusPanel) tabs.getActiveTab()).appendMessage(msg);
				break;
			case NICK:
				boolean you = we || message.equals(connection.getNick());
				String res = you ? "irc.you-nick" : "irc.nick";
				for (JComponent tab : tabs.getAllTabs()) {
					if (tab instanceof ChatPanel) {
						ChatPanel p = (ChatPanel) tab;
						p.changeNick(nick, message);
						if (you) {
							p.appendMessage(String.format(IrcPlugin.getString(res), message));
						} else {
							p.appendMessage(String.format(IrcPlugin.getString(res), nick, message));

						}
					}
				}
				break;
			case MODE:
				if (!target.equals(connection.getNick())) {
					panel.setMode(message);
					panel.appendMessage(String.format(IrcPlugin.getString("irc.mode"), message, target, nick));
				}
				break;
			case TOPIC:
				if (panel != null) {
					if (nick == null) {
						panel.appendMessage(String.format(IrcPlugin.getString("irc.init-topic"), message));
					} else {
						panel.appendMessage(
								String.format(IrcPlugin.getString("irc.topic"), nick, message));
					}
				}
				break;
			case ACTION:
				panel.appendMessage(msg);
				break;
			case KICK:
				String[] tmp = target.split(" ");
				String channel = tmp[0],
						user_ = tmp[1];
				ChatPanel cpanel = getPanel(channel);
				we = user_.equals(connection.getNick());
				if (we) {
					String fMsg = String.format(IrcPlugin.getString("irc.you-kick"), channel, nick, message);
					hostTab.appendMessage(fMsg);
					tabs.removeTab(channel);
				} else {
					cpanel.appendMessage(String.format(IrcPlugin.getString("irc.kick"), user_, channel, nick, message));
					cpanel.userLeft(user_);
				}
				break;
			case STATUS:
				panel.appendMessage(message);
				break;
			default:
		}
	}

	private ChatPanel getPanel(String name) {
		return (ChatPanel) tabs.getTab(name);
	}

	public void onStatus(String msg) {
		hostTab.appendMessage(msg);
	}

	public void onNames(String channel, User[] users) {
		ChatPanel panel = (ChatPanel) tabs.getTab(channel);
		panel.setUsers(users);
	}

	public void onServerReply(String line, int code, String[] params, String msg) {
		hostTab.appendMessage(msg != null ? msg : line);
	}

	public void onCtcpReply(String target, String query, String reply) {
	}
}
