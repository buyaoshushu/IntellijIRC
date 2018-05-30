package net.roarsoftware.intellirc.core;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * The <code>FavoritesModel</code> manages IRC Favorites.
 * 
 * @author Janni Kovacs
 */
public class FavoritesModel {

	private Map<ServerSettings, List<String>> favorites = new HashMap<ServerSettings, List<String>>();
	private List<FavoritesChangeListener> listeners = new ArrayList<FavoritesChangeListener>();

	public void addFavoriteServer(ServerSettings server) {
		favorites.put(server, new ArrayList<String>());
		notifyListeners();
	}

	public void removeFavoriteServer(ServerSettings server) {
		favorites.remove(server);
		notifyListeners();
	}

	public void addFavoriteChannel(ServerSettings server, String channel) {
		favorites.get(server).add(channel);
		notifyListeners();
	}

	public void removeFavoriteChannel(ServerSettings server, String channel) {
		favorites.get(server).remove(channel);
		notifyListeners();
	}

	public void addChangeListener(FavoritesChangeListener l) {
		listeners.add(l);
	}

	public void removeChangeListener(FavoritesChangeListener l) {
		listeners.remove(l);
	}

	public Map<ServerSettings, List<String>> getFavorites() {
		return favorites;
	}

	private void notifyListeners() {
		for (FavoritesChangeListener listener : listeners) {
			listener.favoritesChanged(this);
		}
	}
}
