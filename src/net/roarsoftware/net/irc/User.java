package net.roarsoftware.net.irc;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class User {

	private static final char MODE_OP = 'o';
	private static final char MODE_HALFOP = 'h';
	private static final char MODE_VOICE = 'v';

	private static final char MODE_OP_PREFIX = '@';
	private static final char MODE_HALFOP_PREFIX = '%';
	private static final char MODE_VOICE_PREFIX = '+';

	/* Server specific modes */
	private static final char MODE_CHANNEL_OWNER_PREFIX = '*';
	private static final char MODE_HIGH_LEVEL_PREFIX = '!';

	private static String validFirstChar = "[\\w\\\\\\[\\]`^\\{\\}&&[^\\d]]";
	private static String validNick = User.validFirstChar + "[\\w\\[\\]\\`^\\{\\}-]*";
//	private static Pattern validNickPattern = Pattern.compile(validNick);//new char[] {'_', '[', ']', '\\', '`', '^', '{', '}'};
	private static Pattern invalidNickPattern = Pattern.compile("(^\\d)|[^\\w\\[\\]\\\\`^\\{\\}-]");

	private String nick;
	private String prefix = "";
	private boolean voice;
	private boolean op;
	private boolean halfop;

	public User(String nick) {
		char c = nick.charAt(0);
		if(!User.isValidNickChar(c)) {
			this.nick = nick.substring(1);
			setMode(c, true);
		} else {
			this.nick = nick;
		}
	}

	private static boolean isValidNickChar(char c) {
		return String.valueOf(c).matches(User.validFirstChar);
	}

	public static String validateNick(String nick) {
		Matcher m = User.invalidNickPattern.matcher(nick);
		while(m.find()) {
			int start = m.start();
			if(start == 0) {
				nick = nick.substring(m.end());
				m = User.invalidNickPattern.matcher(nick);
				continue;
			} //else {
//				nick = nick.substring(0, m.start());
//				break;
//			}
		}
		return nick;
	}

	public String getNick() {
		return nick;
	}

	public String getPrefix() {
		return String.valueOf(prefix);
	}

	public void setNick(String newNick) {
		nick = newNick;
	}

	public boolean isOp() {
		return op;
	}

	public boolean isHalfOp() {
		return (!op && halfop);
	}

	public boolean hasVoice() {
		return (!(op || halfop) && voice);
	}

	public boolean equals(Object o) {
		if(o == this)
			return true;
		if(!(o instanceof User))
			return false;
		User u = (User) o;
		return u.nick.equals(nick) && u.prefix.equals(prefix);
	}

	public int hashCode() {
		return nick.hashCode() + prefix.hashCode();
	}

	public String toString() {
		return prefix + nick;
	}

	public void setMode(char modetype, boolean yes) {
		if(modetype == User.MODE_OP || modetype == User.MODE_OP_PREFIX || modetype == User.MODE_HIGH_LEVEL_PREFIX
				|| modetype == User.MODE_CHANNEL_OWNER_PREFIX) {
			op = yes;
		} else if(modetype == User.MODE_HALFOP || modetype == User.MODE_HALFOP_PREFIX) {
			halfop = yes;
		} else if(modetype == User.MODE_VOICE || modetype == User.MODE_VOICE_PREFIX) {
			voice = yes;
		}
		prefix = User.getPrefix(this);
	}

	private static String getPrefix(User user) {
		if(user.isOp())
			return String.valueOf(User.MODE_OP_PREFIX);
		if(user.isHalfOp())
			return String.valueOf(User.MODE_HALFOP_PREFIX);
		if(user.hasVoice())
			return String.valueOf(User.MODE_VOICE_PREFIX);
		return "";
	}
}
