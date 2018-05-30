package net.roarsoftware.intellirc.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The <code>IrcLog</code> class manages IRC conversation logging.
 * 
 * @author Janni Kovacs
 */
public class IrcLog {

	private File path;
	private Map<String, BufferedWriter> writers = new HashMap<String, BufferedWriter>();

	public IrcLog(String path) {
		this.path = new File(path);
		if (!this.path.exists()) {
			this.path.mkdir();
		}
	}

	public void append(String server, String channel, String message) {
		String location = server + "-" + channel;
		try {
			BufferedWriter w = writers.get(location);
			if (w == null) {
				w = new BufferedWriter(new FileWriter(new File(path, location + ".txt"), true));
				writers.put(location, w);
			}
			w.write(message);
			w.newLine();
			w.flush();
		} catch (IOException e) {
			// ignore
		}
	}

	public File getPath() {
		return path;
	}

	public void closeAll() {
		for (BufferedWriter writer : writers.values()) {
			try {
				writer.close();
			} catch (IOException e) {
			}
		}
		writers.clear();
	}


	public String getLog(String host, String reciever) {
		String location = host + "-" + reciever;
		try {
			BufferedReader r = new BufferedReader(new FileReader(new File(path, location + ".txt")));
			StringBuilder result = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
				result.append(line);
				result.append('\n');
			}
			return result.toString();
		} catch (IOException e) {
			return null;
		}
	}
}
