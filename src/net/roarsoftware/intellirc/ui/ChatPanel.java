package net.roarsoftware.intellirc.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;

import net.roarsoftware.intellirc.core.IrcPlugin;
import net.roarsoftware.intellirc.core.IrcLog;
import net.roarsoftware.net.irc.ChannelMessage;
import net.roarsoftware.net.irc.IrcConnection;
import net.roarsoftware.net.irc.IrcUtil;
import net.roarsoftware.net.irc.MessageType;
import net.roarsoftware.net.irc.User;
import net.roarsoftware.net.irc.MessageListener;

/**
 * @author Janni Kovacs
 */
public class ChatPanel extends StatusPanel {
	public class UserListCellRenderer extends DefaultListCellRenderer {

		private Icon op = new ImageIcon(getClass().getResource("res/op.png"));
		private Icon halfop = new ImageIcon(getClass().getResource("res/halfop.png"));
		private Icon voice = new ImageIcon(getClass().getResource("res/voice.png"));
		private Icon blank = new Icon() {

			public int getIconHeight() {
				return 12;
			}

			public int getIconWidth() {
				return 12;
			}

			public void paintIcon(Component component, Graphics graphics, int i, int i1) {
			}
		};

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
													  boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			User u = (User) value;
			setText(u.getNick());
			if (u.isOp()) {
				setIcon(op);
			} else if (u.isHalfOp()) {
				setIcon(halfop);
			} else if (u.hasVoice()) {
				setIcon(voice);
			} else {
				setIcon(blank);
			}
			return this;
		}
	}

	public static class UserComparator implements Comparator<User> {

		public int compare(User o1, User o2) {
			if (o1.isOp()) {
				if (!o2.isOp())
					return -1;
				return o1.getNick().compareToIgnoreCase(o2.getNick());
			} else if (o1.isHalfOp()) {
				if (!o2.isHalfOp())
					return (o2.isOp() ? 1 : -1);
				return o1.getNick().compareToIgnoreCase(o2.getNick());
			} else if (o1.hasVoice()) {
				if (!o2.hasVoice())
					return (o2.isOp() || o2.isHalfOp() ? 1 : -1);
				return o1.getNick().compareToIgnoreCase(o2.getNick());
			} else {
				if (o2.isOp() || o2.isHalfOp() || o2.hasVoice())
					return 1;
				return o1.getNick().compareToIgnoreCase(o2.getNick());
			}
		}

	}

	private List<User> users = new ArrayList<User>();
	private DefaultListModel listModel = new DefaultListModel();
	private String channel;

	public ChatPanel(IrcConnection c, String channel) {
		this.channel = channel;
		setConnection(c);
		if (channel.startsWith("#")) {
			split.setResizeWeight(1);
			split.setOneTouchExpandable(true);
			JList userList = new JList(listModel);
			userList.setCellRenderer(new UserListCellRenderer());
			split.setRightComponent(new JScrollPane(userList));
			split.setDividerSize(UIManager.getInt("SplitPane.dividerSize"));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String text = input.getText();
		if (text.charAt(0) == '/') {
			if (text.substring(1, 3).equalsIgnoreCase("me")) {
				String s = text.substring(4);
				connection.sendAction(channel, s);
				for (MessageListener l : connection.getMessageListeners()) {
					l.onChannelMessage(new ChannelMessage(MessageType.ACTION, System.currentTimeMillis(), connection.getNick(),
						channel, s));
				}
			} else {
				connection.sendRaw(text.substring(1));
			}
		} else {
			connection.sendMessage(channel, text);
			for (MessageListener l : connection.getMessageListeners()) {
			l.onChannelMessage(new ChannelMessage(MessageType.PRIVMSG_SENT, System.currentTimeMillis(), connection.getNick(),
					channel,
					text));
			}
		}
		input.setText(null);
	}

	public void userJoined(String nick) {
		appendMessage(String.format(IrcPlugin.getString("irc.join"), nick, channel));
		users.add(new User(nick));
		updateList();
	}

	public void userLeft(String nick) {
		users.remove(getUser(nick));
		updateList();
	}

	public String getReciever() {
		return channel;
	}

	public void setMode(String mode) {
		int index = mode.indexOf(' ');
		if (index == -1)
			return;
		char modetype = mode.charAt(0);
		boolean yes = (modetype == '+');
		char[] modes = mode.substring(1, index).toCharArray();
		String[] nicks = mode.substring(index + 1).split(" ");
		for (int i = 0; i < modes.length; i++) {
			String nick = IrcUtil.getNick(nicks[i]);
			User user = getUser(nick);
			if (user != null)
				user.setMode(modes[i], yes);
		}
		updateList();
	}

	public User getUser(String nick) {
		for (User u : users) {
			if (u.getNick().equals(nick)) {
				return u;
			}
		}
		return null;
	}

	public void setUsers(User[] users) {
		this.users = new ArrayList<User>(Arrays.asList(users));
		updateList();
	}

	public void changeNick(String oldNick, String newNick) {
		if (getUser(oldNick) != null) {
			getUser(oldNick).setNick(newNick);
			updateList();
		}
	}

	private void updateList() {
		listModel.removeAllElements();
		Collections.sort(this.users, new UserComparator());
		for (User u : users) {
			listModel.addElement(u);
		}
	}

	@Override
	public String appendMessage(ChannelMessage msg) {
		String s = super.appendMessage(msg);
		if (IrcPlugin.getInstance().getSettings().isLogConversations()) {
			IrcLog log = IrcPlugin.getInstance().getLog();
			log.append(connection.getHost(), channel, s);
		}
		return s;
	}
}