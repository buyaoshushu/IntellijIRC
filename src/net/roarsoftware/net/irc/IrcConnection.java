package net.roarsoftware.net.irc;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A <code>IrcConnection</code> establishes and manages a connection to an IRC server.
 *
 * @author Janni Kovacs
 * @version 0.3
 */
public class IrcConnection implements RawListener, CtcpSender {

	public static final String INTERNAL_VERSION = "Roar IrcConnection 0.3 [9/13/06]";

	public static final int ERR_NICKNAMEINUSE = 433;
	public static final int ERR_CHANOPRIVSNEEDED = 482;

	public static final int RPL_MOTD = 372;
	public static final int RPL_ENDOFMOTD = 376;
	public static final int RPL_TOPIC = 332;
	public static final int RPL_NOTOPIC = 331;
	public static final int RPL_TOPICSETTER = 333;
	public static final int RPL_NAMREPLY = 353;
	public static final int RPL_ENDOFNAMES = 366;
	public static final int RPL_LISTSTART = 321;
	public static final int RPL_LIST = 322;
	public static final int RPL_LISTEND = 323;
	public static final int RPL_AWAY = 301;
	public static final int RPL_UNAWAY = 305;
	public static final int RPL_NOWAWAY = 306;

	public static final String PING = "PING";
	public static final String VERSION = "VERSION";
	public static final String TIME = "TIME";

	private InputStream inputStream;
	private OutputStream outputStream;
	private BufferedWriter outputWriter;
	private Socket s;

	private String encoding = "ISO8859-15";
	private boolean isConnected = false;
	private String password = null;
	private String nick = User.validateNick(System.getProperty("user.name"));
	private String realName = nick;
	private String userName = nick;
	private String host;

	private List<MessageListener> messageListeners = new ArrayList<MessageListener>();
	private CtcpHandler ctcpHandler = new DefaultCtcpHandler();

	private boolean verbose = false;
	private List<RawListener> rawListeners = new ArrayList<RawListener>();
	private Map<String, List<User>> userNames = new HashMap<String, List<User>>();

	private IrcConnection(String host) {
		this.host = host;
	}

	public static IrcConnection openConnection(String host) {
		IrcConnection conn = new IrcConnection(host);
		// conn.connect();
		return conn;
	}

	public void connect(String nick, String userName) throws IOException {
		connect(nick, userName, userName, null,6667);
	}

	public void connect(String nick, String userName, String realName, String password,Integer port) throws IOException {
		if (isConnected()) {
			throw new UnsupportedOperationException("already connected");
		}
		if (nick == null) {
			throw new NullPointerException("nick must be non null");
		}
		addRawListener(this);
		this.nick = nick;
		this.userName = userName != null && userName.length() != 0 ? userName : nick;
		this.realName = realName != null && realName.length() != 0 ? realName : nick;
		this.password = password;
		s = new Socket(host, port);
		// s.setKeepAlive(true);
		outputStream = s.getOutputStream();
		outputWriter = new BufferedWriter(new OutputStreamWriter(outputStream, getEncoding()));
		if (password != null && password.trim().length() != 0)
			sendRaw("PASS " + password);
		sendRaw("NICK " + nick);
		sendRaw("USER " + this.userName + " \"\" \"" + host + "\" :" + this.realName);
		inputStream = s.getInputStream();
		s.setSoTimeout(5 * 60 * 1000);
		new IrcReaderThread(this).start();
	}

	Socket getSocket() {
		return s;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	// ----- IRC Methods

	public synchronized void disconnect() throws IOException {
		disconnect(nick);
	}

	public synchronized void disconnect(String quitmsg) throws IOException {
		if (isConnected) {
			quit(quitmsg);
			inputStream.close();
			outputStream.close();
		}
		if (s != null)
			s.close();
	}

	public void sendRaw(String raw) {
		try {
			outputWriter.write(raw);
			outputWriter.newLine();
			outputWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (RawListener l : rawListeners)
			l.lineSent(raw);
	}

	public void sendCtcpReply(String target, String msg) {
		sendNotice(IrcUtil.getNick(target), IrcUtil.quoteCtcp(msg));
	}

	public String getEncoding() {
		return encoding;
	}

	void recieveLine(String line) {
		if (verbose)
			System.out.println(line);
		if (!isConnected) {
			isConnected = true;
			for (MessageListener listener : messageListeners) {
				listener.onConnect();
			}
		}
		for (RawListener l : rawListeners)
			l.lineRecieved(line);
	}

	public void lineRecieved(String line) {
		long date = System.currentTimeMillis();
		if (line.startsWith("PING")) {
			String reciever = line.substring(line.indexOf(':') + 1, line.length());
			pong(reciever);
		} else if (line.startsWith("ERROR")) {
			String msg = line.substring(line.indexOf(':') + 1, line.length());
			isConnected = false;
			for (MessageListener listener : messageListeners) {
				listener.onStatus(msg);
				listener.onDisconnect(false);
			}
		} else if (line.startsWith(":")) {
			// Ganzes Kommando: ":Nick!~Nick@host TOPIC #channel :blah blah"
			int firstws = line.indexOf(' '), secondws = line.indexOf(' ', firstws + 1), thirdws = line.indexOf(
					' ',
					secondws + 1);
			// source (= "Nick!host@name")
			String source = line.substring(1, firstws);
			// command (z.B. "TOPIC")
			String cmd = line.substring(firstws + 1, secondws);
			int msgStartIndex = line.indexOf(':', 1);
			// message ("blah blah")
			String msg = null;
			if (msgStartIndex != -1)
				msg = line.substring(msgStartIndex + 1, line.length());
			else
				msgStartIndex = line.length();
			String param1 = null, addParam = null;
			if (thirdws == -1) {
				param1 = line.substring(secondws + 1, line.length());
			} else {
				param1 = line.substring(secondws + 1, thirdws);
				if (thirdws < msgStartIndex - 1)
					addParam = line.substring(thirdws + 1, (msgStartIndex != line.length() ? msgStartIndex - 1
							: msgStartIndex));
			}
			// target (= channel)
			String target = param1;

			/*
			 * if(cmd.equals("004") || cmd.equals("005") || cmd.equals("252") || cmd.equals("254")) { //
			 * listener.status() // } // String param = null; // if(line.charAt(secondws + 1) != ':') { //
			 * String param = line.substring(secondws + 1, thirdws); // } /* int msgStartIndex =
			 * line.indexOf(':', 1); String msg = null; if(msgStartIndex != -1) msg =
			 * line.substring(msgStartIndex +1, line.length()); else msgStartIndex = line.length(); String
			 * param = line.substring(secondws + 1, msgStartIndex); // String[] params = param.split(" ");
			 */// System.out.println("breakpoint");
			// source = getNick(source);
			int code = -1;
			try {
				code = Integer.parseInt(cmd);
			} catch (NumberFormatException e) {
			}

			if (code != -1) {
				if (code == ERR_NICKNAMEINUSE) {
					for (MessageListener listener : messageListeners) {
						listener.onStatus(msg);
					}
					changeNick(nick + "_");
				} else if (code == RPL_TOPIC) {
					// listener.onTopic(addParam, msg, null);
					// type = MessageType.TOPIC;
					for (MessageListener listener : messageListeners)
						listener.onChannelMessage(new ChannelMessage(MessageType.TOPIC, date, null, addParam, msg));
					// null));
				} else if (code == RPL_NAMREPLY) {
					List<User> users = makeUsers(msg);
					String chan = addParam.substring(addParam.lastIndexOf(' ') + 1);
					if (!userNames.containsKey(chan)) {
						userNames.put(chan, new ArrayList<User>());
					}
					userNames.get(chan).addAll(users);
				} else if (code == RPL_ENDOFNAMES) {
					String chan = addParam;
					List<User> users = userNames.get(chan);
					for (MessageListener listener : messageListeners) {
						listener.onNames(chan, users.toArray(new User[users.size()]));
					}
					userNames.remove(chan);
				} else {
					String[] params = line.substring(secondws + 1, msgStartIndex).split(" ");
					for (MessageListener listener : messageListeners) {
						listener.onServerReply(line, code, params, msg);
					}
				}
			} else {
				MessageType type = null;
				try {
					type = Enum.valueOf(MessageType.class, cmd);
				} catch (IllegalArgumentException e) {
				}

				if (type == MessageType.NICK) {
					if (IrcUtil.getNick(source).equals(nick))
						this.nick = msg;
				} else if (type == MessageType.PRIVMSG) {
					if (IrcUtil.isCtcp(msg)) {
						String qry = IrcUtil.unquoteCtcp(msg);
						int index = qry.indexOf(' ');
						if (qry.startsWith("ACTION")) {
							type = MessageType.ACTION;
							msg = qry.substring(7);
						} else {
							ctcpHandler.handleQuery(
									this,
									source,
									qry.substring(0, (index != -1 ? index : qry.length())),
									(index == -1 ? null : qry.substring(index)));
							return;
						}
					}
				} else if (type == MessageType.NOTICE) {
					if (IrcUtil.isCtcp(msg)) {
						msg = IrcUtil.unquoteCtcp(msg);
						int index = msg.indexOf(' ');
						for (MessageListener listener : messageListeners) {
							listener.onCtcpReply(source, msg.substring(0, index), msg.substring(index + 1));
						}
					} else {
					}
				} else if (type == MessageType.KICK) {
					target += " " + addParam;
				} else if (type == MessageType.MODE) {
					msg = (msg != null ? msg.trim() : addParam.trim());
				} else if (type == MessageType.JOIN) {
					msg = (msg != null ? msg : param1);
				} else if (type == null) {
					for (MessageListener listener : messageListeners) {
						listener.onStatus(msg);
					}
				}
				if (type != null) {
					ChannelMessage cmsg = new ChannelMessage(type, date, source, target, msg);
					for (MessageListener listener : messageListeners) {
						listener.onChannelMessage(cmsg);
					}
				}
			}

		} else if (line.startsWith("NOTICE")) {
			for (MessageListener listener : messageListeners) {
				listener.onChannelMessage(new ChannelMessage(
						MessageType.NOTICE,
						date,
						getHost(),
						getNick(),
						line.substring(line.indexOf(':') + 1)));
			}
		}

	}

	public void lineSent(String line) {
		if (verbose)
			System.out.println(line);
	}

	private List<User> makeUsers(String string) {
		String[] nicks = string.split(" ");
		List<User> users = new ArrayList<User>();
		for (int i = 0; i < nicks.length; i++) {
			if (nicks[i].length() == 0)
				continue;
			users.add(new User(nicks[i]));
		}
		return users;
	}

	public void addMessageListener(MessageListener listener) {
		messageListeners.add(listener);
	}

	public void removeMessageListener(MessageListener listener) {
		messageListeners.remove(listener);
	}

	public MessageListener[] getMessageListeners() {
		return messageListeners.toArray(new MessageListener[messageListeners.size()]);
	}

	public void addRawListener(RawListener listener) {
		rawListeners.add(listener);
	}

	public void removeRawListener(RawListener listener) {
		rawListeners.remove(listener);
	}

	public void changeNick(String newNick) {
		sendRaw("NICK " + newNick);
	}

	public String getNick() {
		return nick;
	}

	public void join(String channel) {
		sendRaw("JOIN " + channel);
	}

	public void join(String channel, String key) {
		sendRaw("JOIN " + channel + " " + key);
	}

	public void part(String channel) {
		sendRaw("PART " + channel);
	}

	public void changeMode(String channel, String target, String mode) {
		sendRaw("MODE " + channel + " " + mode + " " + target);
	}

	public void changeTopic(String channel, String topic) {
		sendRaw("TOPIC " + channel + " :" + topic);
	}

	public void sendMessage(String reciever, String msg) {
		sendRaw("PRIVMSG " + reciever + " :" + msg);
	}

	public void sendCtcpQuery(String reciever, String msg) {
		sendMessage(reciever, IrcUtil.quoteCtcp(msg));
	}

	public void sendAction(String reciever, String msg) {
		sendCtcpQuery(reciever, "ACTION " + msg);
	}

	public void sendNotice(String reciever, String msg) {
		sendRaw("NOTICE " + reciever + " :" + msg);
	}

	public void getNames(String channel) {
		sendRaw("NAMES " + channel);
	}

	public void ping(String reciever) {
		sendCtcpQuery(reciever, "PING " + System.currentTimeMillis());
	}

	public void pong(String server) {
		sendRaw("PONG " + server);
	}

	public void quit(String quitmsg) {
		sendRaw("QUIT :" + quitmsg);
		isConnected = false;
	}

	public String getHost() {
		return host;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void getTopic(String target) {
		sendRaw("TOPIC " + target);
	}

	public void kick(String chan, String target, String msg) {
		sendRaw("KICK " + chan + " " + target + " :" + msg);
	}

	// TODO: IrcConnection#exceptionOccured()
	void exceptionOccured(Exception e1) {
		if (verbose) {
			e1.printStackTrace();
		}

		boolean error = false;
		if (e1 instanceof SocketException) {
			for (MessageListener listener : messageListeners) {
				listener.onStatus(e1.getMessage());
			}
		} else {
			error = true;
			for (MessageListener listener : messageListeners) {
				listener.onStatus(e1.toString());
			}
			for (StackTraceElement elem : e1.getStackTrace()) {
				for (MessageListener listener : messageListeners) {
					listener.onStatus("at " + elem.toString());
				}
				// TODO: package namen anpassen
				if (elem.getClassName().startsWith("net.roarsoftware"))
					break;
			}
		}
		if (!s.isClosed()) {
			try {
				s.close();
			} catch (IOException e) {
			}
		}
		isConnected = false;
		for (MessageListener listener : messageListeners) {
			listener.onDisconnect(error);
		}
	}

	public void setCtcpHandler(CtcpHandler h) {
		this.ctcpHandler = h;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		if (isConnected)
			throw new IllegalStateException("Already connected");
		this.password = password;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		if (isConnected)
			throw new IllegalStateException("Already connected");
		this.realName = (realName != null ? realName : System.getProperty("user.name"));
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		if (isConnected)
			throw new IllegalStateException("Already connected");
		this.userName = User.validateNick((userName != null ? userName : System.getProperty("user.name")));
	}

	// TODO: IrcConnection#setEncoding()
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setNick(String nick) {
		if (isConnected)
			throw new IllegalStateException("Already connected");
		this.nick = User.validateNick((nick != null ? nick : System.getProperty("user.name")));
	}

}
