package net.roarsoftware.intellirc.core;

import net.roarsoftware.net.irc.User;

/**
 * Stores Plugin Settings.
 * 
 * @author Janni Kovacs
 */
public class IrcSettings {
	public boolean enableSmilies = true;
	public boolean enableColors = true;
	public boolean showTimestamp = true;
	public boolean logConversations = true;
	public boolean enableIdentd = false;
	public String identdSystem = "UNIX";
	public String identdUser = "User";
	public int identdPort = 113;
	public String signoffMessage = "Used IntellIRC";
	public String nickname = User.validateNick(System.getProperty("user.name"));
	public String username = nickname;
	public String realname = nickname;

	public boolean isEnableSmilies() {
		return enableSmilies;
	}

	public void setEnableSmilies(final boolean enableSmilies) {
		this.enableSmilies = enableSmilies;
	}

	public boolean isEnableColors() {
		return enableColors;
	}

	public void setEnableColors(final boolean enableColors) {
		this.enableColors = enableColors;
	}

	public boolean isShowTimestamp() {
		return showTimestamp;
	}

	public void setShowTimestamp(final boolean showTimestamp) {
		this.showTimestamp = showTimestamp;
	}

	public boolean isLogConversations() {
		return logConversations;
	}

	public void setLogConversations(final boolean logConversations) {
		this.logConversations = logConversations;
	}

	public boolean isEnableIdentd() {
		return enableIdentd;
	}

	public void setEnableIdentd(final boolean enableIdentd) {
		this.enableIdentd = enableIdentd;
	}

	public String getIdentdSystem() {
		return identdSystem;
	}

	public void setIdentdSystem(final String identdSystem) {
		this.identdSystem = identdSystem;
	}

	public String getIdentdUser() {
		return identdUser;
	}

	public void setIdentdUser(final String identdUser) {
		this.identdUser = identdUser;
	}

	public String getSignoffMessage() {
		return signoffMessage;
	}

	public void setSignoffMessage(final String signoffMessage) {
		this.signoffMessage = signoffMessage;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(final String nickname) {
		this.nickname = nickname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(final String realname) {
		this.realname = realname;
	}

	public int getIdentdPort() {
		return identdPort;
	}

	public void setIdentdPort(int identdPort) {
		this.identdPort = identdPort;
	}
}
