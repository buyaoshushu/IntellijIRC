package net.roarsoftware.intellirc.core;

/**
 * @author Janni Kovacs
 */
public class ServerSettings {

	private String host, nick, user, real, password;

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	private Integer port;
	private boolean autoConnect;

	public ServerSettings() {
	}

	public ServerSettings(String host, String nick) {
		this(host, nick, nick, nick, null);
	}

	public ServerSettings(String host, String nick, String password) {
		this(host, nick, nick, nick, password);
	}

	public ServerSettings(String host, String nick, String real, String user, String password) {
		this.host = host;
		this.nick = nick;
		this.real = real;
		this.user = user;
		this.password = password;
	}

	@Override
	public int hashCode() {
		return (host+port).hashCode();
	}

	public String getHost() {
		return host;
	}

	public String getNick() {
		return nick;
	}

	public String getPassword() {
		return password;
	}

	public String getReal() {
		return real;
	}

	public String getUser() {
		return user;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setReal(String real) {
		this.real = real;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public boolean isAutoConnect() {
		return autoConnect;
	}

	public void setAutoConnect(final boolean autoConnect) {
		this.autoConnect = autoConnect;
	}
}
