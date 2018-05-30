package net.roarsoftware.net.irc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class IrcReaderThread extends Thread {

	private IrcConnection con;

	public IrcReaderThread(IrcConnection connection) {
		this.con = connection;
	}

	public void run() {
		try {
			Socket s = con.getSocket();
			BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream(), con.getEncoding()));
			String line = r.readLine();
			while(!s.isClosed() && line != null) {
				con.recieveLine(line);
				line = r.readLine();
			}
		} catch(Exception e1) {
			con.exceptionOccured(e1);
		}
	}
}
