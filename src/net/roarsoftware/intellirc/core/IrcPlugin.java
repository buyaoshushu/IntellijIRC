package net.roarsoftware.intellirc.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerAdapter;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import net.roarsoftware.intellirc.ui.IrcConnectionsWindow;
import net.roarsoftware.intellirc.ui.IrcFavoritesWindow;
import net.roarsoftware.intellirc.ui.IrcSettingsForm;

/**
 * Handles the Plugin's lifecycle and configuration.
 *
 * @author Janni Kovacs
 */
public class IrcPlugin implements ApplicationComponent, Configurable, JDOMExternalizable {

	private static ResourceBundle resourceBundle;

	private FavoritesModel favoritesModel = new FavoritesModel();
	private IrcSettingsForm settingsForm = null;
	private IrcSettings settings = new IrcSettings();
	private ConnectionManager connectionManager;
	private static IrcPlugin INSTANCE;
	private IrcLog log;

	public IrcPlugin() {
		INSTANCE = this;
		connectionManager = new ConnectionManagerImpl();
		log = new IrcLog(PathManager.getConfigPath() + System.getProperty("file.separator") + "intellirc");
	}

	public static IrcPlugin getInstance() {
		return INSTANCE;
	}

	public FavoritesModel getFavoriteModel() {
		return favoritesModel;
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public void initComponent() {
		ProjectManager pm = ProjectManager.getInstance();
		Project[] projects = pm.getOpenProjects();
		for (Project project : projects) {
			regForProject(project);
		}
		pm.addProjectManagerListener(new ProjectManagerAdapter() {
			public void projectOpened(Project project) {
				regForProject(project);
			}

			public void projectClosed(Project project) {
				unregForProject(project);
			}
		});
	}

	private void regForProject(Project project) {
		ToolWindowManager twm = ToolWindowManager.getInstance(project);
		IrcFavoritesWindow favoritesWindow = new IrcFavoritesWindow(this);
		ToolWindow favWindow = twm
				.registerToolWindow(getString("window.favs.title"), favoritesWindow, ToolWindowAnchor.RIGHT);
		favWindow.setIcon(new ImageIcon(IrcConnectionsWindow.class.getResource("res/favorites.png")));
		IrcConnectionsWindow connectionsWindow = new IrcConnectionsWindow(connectionManager);
		ToolWindow ircWindow = twm
				.registerToolWindow(getString("window.irc.title"), connectionsWindow, ToolWindowAnchor.BOTTOM);
		ircWindow.setIcon(new ImageIcon(IrcConnectionsWindow.class.getResource("res/intellirc_small.png")));
		if (ProjectManager.getInstance().getOpenProjects().length == 1) {
			Map<ServerSettings, List<String>> favorites = getFavoriteModel().getFavorites();
			for (ServerSettings serverSettings : favorites.keySet()) {
				if (serverSettings.isAutoConnect()) {
					connectionManager.connect(serverSettings);
				}
			}
		}
	}

	private void unregForProject(Project project) {
		ToolWindowManager twm = ToolWindowManager.getInstance(project);
		try {
			ToolWindow window = twm.getToolWindow(getString("window.irc.title"));
			((IrcConnectionsWindow) window.getComponent()).terminate();
			twm.unregisterToolWindow(getString("window.favs.title"));
			twm.unregisterToolWindow(window.getTitle());
		} catch (IllegalArgumentException e) {
			// will never occur
		}
	}

	public void disposeComponent() {
		connectionManager.closeAllConnections(settings.getSignoffMessage());
		getLog().closeAll();
	}

	public String getComponentName() {
		return "IrcPlugin";
	}

	/**
	 * Returns the Plugin's ResourceBundle, containig locale sensitive strings.
	 *
	 * @return the ResourceBundle
	 */
	public static ResourceBundle getResources() {
		if (resourceBundle == null) {
			resourceBundle = ResourceBundle.getBundle("net.roarsoftware.intellirc.ui.res.Language");
		}
		return resourceBundle;
	}

	/**
	 * Returns a localized String for the given key.
	 *
	 * @param key
	 * @return A localized String
	 */
	public static String getString(String key) {
		return getResources().getString(key);
	}

	public String getDisplayName() {
		return "IntellIRC";
	}

	public Icon getIcon() {
		return new ImageIcon(IrcConnectionsWindow.class.getResource("res/intellirc.png"));
	}

	@Nullable
	@NonNls
	public String getHelpTopic() {
		return null;
	}

	public JComponent createComponent() {
		if (settingsForm == null) {
			settingsForm = new IrcSettingsForm();
			settingsForm.setData(settings);
		}
		return settingsForm.getRootComponent();
	}

	public boolean isModified() {
		return (settingsForm != null && settingsForm.isModified(settings));
	}

	public void apply() throws ConfigurationException {
		if (settingsForm != null) {
			settingsForm.getData(settings);
		}
	}

	public void reset() {
		if (settingsForm != null) {
			settingsForm.setData(settings);
		}
	}

	public void disposeUIResources() {
		settingsForm = null;
	}


	public void readExternal(Element element) throws InvalidDataException {
		List children = element.getChildren("server");
		for (Object o : children) {
			Element server = (Element) o;
			ServerSettings settings = new ServerSettings();
			settings.setHost(server.getChildText("host"));
			settings.setPort(Integer.parseInt(server.getChildText("port")));
			settings.setNick(server.getChildText("nick"));
			settings.setUser(server.getChildText("user"));
			settings.setReal(server.getChildText("real"));
			settings.setAutoConnect(Boolean.valueOf(server.getChildText("auto-connect")));
			settings.setPassword(server.getChildText("pass"));
			Element channelsElement = server.getChild("channels");
			List<String> channels = new ArrayList<String>();
			for (Object o1 : channelsElement.getChildren("channel")) {
				channels.add(((Element) o1).getText());
			}
			favoritesModel.getFavorites().put(settings, channels);
		}
		Element preferences = element.getChild("preferences");
		if (preferences != null) {
			DefaultJDOMExternalizer.readExternal(settings, preferences);
		}
	}

	public void writeExternal(Element element) throws WriteExternalException {
		Map<ServerSettings, List<String>> favorites = favoritesModel.getFavorites();
		for (ServerSettings serverSettings : favorites.keySet()) {
			Element server = new Element("server");
			server.addContent(new Element("host").setText(serverSettings.getHost()));
			server.addContent(new Element("port").setText(serverSettings.getPort()==null?"6667":(serverSettings.getPort()+"")));
			server.addContent(new Element("nick").setText(serverSettings.getNick()));
			server.addContent(new Element("real").setText(serverSettings.getReal()));
			server.addContent(new Element("user").setText(serverSettings.getUser()));
			server.addContent(new Element("auto-connect").setText(String.valueOf(serverSettings.isAutoConnect())));
			server.addContent(new Element("pass").setText(serverSettings.getPassword()));
			Element channels = new Element("channels");
			for (String channel : favorites.get(serverSettings)) {
				channels.addContent(new Element("channel").setText(channel));
			}
			server.addContent(channels);
			element.addContent(server);
		}
		Element preferences = new Element("preferences");
		DefaultJDOMExternalizer.writeExternal(settings, preferences);
		element.addContent(preferences);
	}

	public IrcSettings getSettings() {
		return settings;
	}

	public IrcLog getLog() {
		return log;
	}
}
