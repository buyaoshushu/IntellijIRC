package net.roarsoftware.net.irc;

import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

public class IdentServer extends Thread {

	private static List<IdentServer> servers = new Vector<IdentServer>();
	private String user;
	private String system;
	private int port;

	private ServerSocket ss;

	public IdentServer(String user, String system, int port) {
		Iterator<IdentServer> it = IdentServer.servers.iterator();
		while(it.hasNext()) {
			IdentServer s = it.next();
			if(s.port == port) {
				try {
					s.join();
				} catch(InterruptedException e) {
				}
				it.remove();
			}
		}
		this.user = user;
		this.system = system;
		this.port = port;
		try {
			ss = new ServerSocket(port);
			ss.setSoTimeout(60000);
		} catch(IOException e) {
			throw new RuntimeException("Error intializing Ident server", e);
		}
		IdentServer.servers.add(this);
	}

	public void run() {
		try {
			Socket socket = ss.accept();
			socket.setSoTimeout(60000);

			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			String line = reader.readLine();
			if(line != null) {
				line = line + " : USERID : " + system + " : " + user;
				writer.write(line + "\r\n");
				writer.flush();
				writer.close();
			}
		} catch(Exception e) {
		//	throw new RuntimeException("Error in Ident server", e);
		}

		try {
			ss.close();
		} catch(Exception e) {
			throw new RuntimeException("Unable to close Ident server", e);
		}
	}
}
