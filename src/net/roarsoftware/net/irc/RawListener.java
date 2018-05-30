package net.roarsoftware.net.irc;

public interface RawListener {

	public void lineRecieved(String line);

	public void lineSent(String line);
}
