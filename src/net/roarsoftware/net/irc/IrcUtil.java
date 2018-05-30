package net.roarsoftware.net.irc;


/**
 * Provides utility methods for IRC and CTCP.
 *
 * @author Janni Kovacs
 * @version 0.2
 */
public class IrcUtil {

	public static final String ctcpQuote = Character.toString((char) 1);

	private IrcUtil() {
	}

	public static boolean isChannel(String s) {
		return s.startsWith("#") || s.startsWith("&");
	}

	public static String getNick(String s) {
		if (s == null)
			return null;
		int index = s.indexOf('!');
		return (index != -1 ? s.substring(0, index) : s);
	}

	public static String getHost(String s) {
		if (s == null)
			return null;
		int index = s.indexOf('!');
		return (index != -1 ? s.substring(index + 1, s.length()) : s);
	}

	public static String quoteCtcp(String msg) {
		StringBuffer sb = new StringBuffer(msg.length() + 2);
		sb.append(ctcpQuote);
		sb.append(msg);
		sb.append(ctcpQuote);
		return sb.toString();
	}

	public static boolean isCtcp(String msg) {
		return msg.startsWith(ctcpQuote) && msg.endsWith(ctcpQuote);
	}

	public static String unquoteCtcp(String msg) {
		if (!isCtcp(msg))
			throw new IllegalArgumentException("not a ctcp message: " + msg);
		return msg.substring(1, msg.length() - 1);
	}
}
