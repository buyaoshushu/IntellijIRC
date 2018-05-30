package net.roarsoftware.net.irc;

import java.util.EnumSet;
import java.util.Iterator;

public enum MessageType {

	PRIVMSG, PRIVMSG_SENT, JOIN, PART, QUIT, MODE, NICK, KICK, NOTICE, ACTION, TOPIC, AWAY, STATUS;

	public static MessageType getType(int ordinal) {
		EnumSet<MessageType> set = EnumSet.allOf(MessageType.class);
		Iterator<MessageType> i = set.iterator();
		for (MessageType t : set) {
			if (t.ordinal() == ordinal)
				return t;
		}
		return null;
	}
}
