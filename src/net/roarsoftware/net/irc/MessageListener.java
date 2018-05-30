package net.roarsoftware.net.irc;

public interface MessageListener {

	public void onConnect();

	public void onDisconnect(boolean error);

	/**
	 * Informiert �ber jede Nachricht f�r den User.
	 * @param msg Die Nachricht
	 */
	public void onChannelMessage(ChannelMessage msg); //String target, String nick, String msg);

	public void onStatus(String msg);

	public void onNames(String channel, User[] users);

	public void onServerReply(String line, int code, String[] params, String msg);

	public void onCtcpReply(String target, String query, String reply);
}
