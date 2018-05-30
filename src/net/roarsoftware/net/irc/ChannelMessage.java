package net.roarsoftware.net.irc;

public class ChannelMessage {

	private MessageType type;
	private String target, source, msg;
	private long date;

	public ChannelMessage(MessageType type, long date, String source, String target, String msg) {
		this.type = type;
		this.date = date;
		this.source = source;
		this.target = target;
		this.msg = msg;
	}

	public long getDate() {
		return date;
	}

	public String getMessage() {
		return msg;
	}

	public String getTarget() {
		return target;
	}

	public MessageType getType() {
		return type;
	}

	public String getSource() {
		return source;
	}
}
