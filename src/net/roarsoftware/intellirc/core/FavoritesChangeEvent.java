package net.roarsoftware.intellirc.core;

import java.util.EventObject;

/**
 * @author Janni Kovacs
 */
public class FavoritesChangeEvent extends EventObject {
	public static enum FavoriteChangeType {
		SERVER_ADDED,
		SERVER_REMOVED,
		CHANNEL_ADDED,
		CHANNEL_REMOVED
	}

	private FavoriteChangeType type;
	private ServerSettings serverValue;
	private String channeValue;

	public FavoriteChangeType getType() {
		return type;
	}

	public FavoritesChangeEvent(FavoritesModel model, FavoriteChangeType type, ServerSettings server, String channel) {
		super(model);
		this.type = type;
		this.serverValue = server;
		this.channeValue = channel;
	}

	public ServerSettings getServer() {
		return serverValue;
	}

	public String getChannel() {
		return channeValue;
	}
}
