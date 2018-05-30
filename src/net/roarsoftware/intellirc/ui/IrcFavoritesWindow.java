package net.roarsoftware.intellirc.ui;

import java.awt.BorderLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.Icons;

import net.roarsoftware.intellirc.core.FavoritesChangeListener;
import net.roarsoftware.intellirc.core.FavoritesModel;
import net.roarsoftware.intellirc.core.IrcPlugin;
import net.roarsoftware.intellirc.core.ServerSettings;

/**
 * Displays favorite IRC Servers and Channels, separates them from offline/online, let you create and
 * delete favorites and connect to them.
 *
 * @author Janni Kovacs
 */
public class IrcFavoritesWindow extends JPanel implements FavoritesChangeListener {

	private IrcPlugin plugin;
	private DefaultMutableTreeNode root;

	private DefaultTreeModel model;
	private JTree tree;
	private ConnectAction connect;

	public IrcFavoritesWindow(IrcPlugin ircPlugin) {
		this.plugin = ircPlugin;
		ircPlugin.getFavoriteModel().addChangeListener(this);

		root = new DefaultMutableTreeNode();
		model = new DefaultTreeModel(root);

		tree = new JTree(model);
		tree.setToggleClickCount(3);
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(new FavoritesTreeCellRenderer());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setRootVisible(false);
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					connect();
				}
			}
		});

		favoritesChanged(ircPlugin.getFavoriteModel());

		DefaultActionGroup actions = new DefaultActionGroup();
		AddServerAction addServer = new AddServerAction();
		AddChannelAction addChannel = new AddChannelAction();
		RemoveAction remove = new RemoveAction();
		remove.getTemplatePresentation().setEnabled(false);
		PropertiesAction properties = new PropertiesAction();
		connect = new ConnectAction();
		properties.getTemplatePresentation().setEnabled(false);
		actions.add(addServer);
		actions.add(addChannel);
		actions.add(remove);
		actions.add(properties);
		actions.add(connect);

		ActionToolbar ab = ActionManager.getInstance().createActionToolbar("IrcFavoritesWindow", actions, true);
		JComponent toolBar = ab.getComponent();
		ActionPopupMenu popupMenu = ActionManager.getInstance().createActionPopupMenu("IrcFavoritesWindow", actions);
		JPopupMenu popup = popupMenu.getComponent();
		popup.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
				Point p = MouseInfo.getPointerInfo().getLocation();
				SwingUtilities.convertPointFromScreen(p, tree);
				int row = tree.getRowForLocation(p.x, p.y);
				if (row != -1)
					tree.setSelectionRow(row);
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
			}

			public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
			}
		});
		tree.setComponentPopupMenu(popup);

		setLayout(new BorderLayout());
		add(new JScrollPane(tree));
		add(toolBar, BorderLayout.NORTH);
	}

	public void favoritesChanged(FavoritesModel model) {
		root.removeAllChildren();
		Map<ServerSettings, List<String>> favorites = model.getFavorites();
		for (ServerSettings settings : favorites.keySet()) {
			DefaultMutableTreeNode serverNode = new DefaultMutableTreeNode(settings);
			List<String> list = favorites.get(settings);
			for (String channel : list) {
				serverNode.add(new DefaultMutableTreeNode(channel));
			}
			root.add(serverNode);
		}
		this.model.reload();
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

	private void connect() {
		new Thread() {
			public void run() {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
				Object o = node.getUserObject();
				if (o instanceof ServerSettings) {
					plugin.getConnectionManager().connect((ServerSettings) o);
				} else {
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode) tree.getSelectionPath()
							.getPathComponent(1);
					plugin.getConnectionManager().join((ServerSettings) parent.getUserObject(), (String) o);
				}
			}
		}.start();
	}

	private class AddServerAction extends AnAction {

		public AddServerAction() {
			super(IrcPlugin.getString("window.favs.addServer"), null,
					new ImageIcon(IrcFavoritesWindow.class.getResource("res/addServerFavorite.png")));
		}

		public void actionPerformed(AnActionEvent e) {
			ServerConfigDialog d = new ServerConfigDialog(plugin);
			d.setLocationRelativeTo(null);
			d.setVisible(true);
			if (d.getSelection() == JOptionPane.OK_OPTION) {
				ServerSettings s = new ServerSettings();
				d.getData(s);
				plugin.getFavoriteModel().addFavoriteServer(s);
			}
		}
	}


	private class AddChannelAction extends IdeaAction {

		public AddChannelAction() {
			super(IrcPlugin.getString("window.favs.addChannel"),
					new ImageIcon(IrcFavoritesWindow.class.getResource("res/addChannelFavorite.png")));
		}

		public void actionPerformed(AnActionEvent e) {
			DefaultMutableTreeNode serverNode = (DefaultMutableTreeNode) tree.getSelectionPath()
					.getPathComponent(1);
			ServerSettings server = (ServerSettings) serverNode.getUserObject();
			String s = Messages
					.showInputDialog(IrcFavoritesWindow.this, IrcPlugin.getString("window.favs.addChannel.message"),
							IrcPlugin.getString("window.favs.addChannel.title"), Messages.getQuestionIcon());
			if (s == null)
				return;
			if (!s.startsWith("#"))
				s = "#" + s;
			if (plugin.getFavoriteModel().getFavorites().get(server).contains(s)) {
				Messages.showMessageDialog(IrcFavoritesWindow.this,
						IrcPlugin.getString("window.favs.addChannel.alreadyExists.message"),
						IrcPlugin.getString("window.favs.addChannel.alreadyExists.title"),
						Messages.getInformationIcon());
			} else {
				plugin.getFavoriteModel().addFavoriteChannel(server, s);
			}
		}

		@Override
		protected boolean isEnabled() {
			return !tree.isSelectionEmpty();
		}

	}

	private class RemoveAction extends IdeaAction {

		public RemoveAction() {
			super(IrcPlugin.getString("window.favs.remove"), Icons.DELETE_ICON);
		}

		@Override
		protected boolean isEnabled() {
			return !tree.isSelectionEmpty();
		}

		public void actionPerformed(AnActionEvent e) {
			DefaultMutableTreeNode sel = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			Object obj = sel.getUserObject();
			if (obj instanceof ServerSettings) {
				plugin.getFavoriteModel().removeFavoriteServer((ServerSettings) obj);
			} else {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) sel.getParent();
				plugin.getFavoriteModel().removeFavoriteChannel((ServerSettings) parent.getUserObject(), (String) obj);
			}
		}
	}


	private class PropertiesAction extends IdeaAction {

		public PropertiesAction() {
			super(IrcPlugin.getString("window.favs.properties"),
					new ImageIcon(IrcFavoritesWindow.class.getResource("res/properties.png")));
		}

		@Override
		protected boolean isEnabled() {
			return !tree.isSelectionEmpty();
		}

		public void actionPerformed(AnActionEvent e) {
			DefaultMutableTreeNode sel = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			Object obj = sel.getUserObject();
			if (obj instanceof ServerSettings) {
				ServerSettings settings = (ServerSettings) obj;
				ServerConfigDialog d = new ServerConfigDialog(plugin);
				d.setData(settings);
				d.setLocationRelativeTo(null);
				d.setVisible(true);
				if (d.getSelection() == JOptionPane.OK_OPTION) {
					d.getData(settings);
					favoritesChanged(plugin.getFavoriteModel());
				}
			} else {
				DefaultMutableTreeNode serverNode = (DefaultMutableTreeNode) sel.getParent();
				ServerSettings server = (ServerSettings) serverNode.getUserObject();
				String old = obj.toString();
				String s = Messages.showInputDialog(IrcPlugin.getString("window.favs.addChannel.message"),
						IrcPlugin.getString("window.favs.addChannel.title"), Messages.getQuestionIcon(), old, null);
				if(s == null)
					return;
				if (!s.startsWith("#"))
					s = "#" + s;
				List<String> channels = plugin.getFavoriteModel().getFavorites().get(server);
				int index = channels.indexOf(old);
				channels.remove(index);
				channels.add(index, s);
				favoritesChanged(plugin.getFavoriteModel());
			}
		}
	}

	private class ConnectAction extends IdeaAction {

		public ConnectAction() {
			super(IrcPlugin.getString("window.favs.connect"),
					new ImageIcon(IrcFavoritesWindow.class.getResource("res/connect.png")));
		}

		@Override
		protected boolean isEnabled() {
			return !tree.isSelectionEmpty();
		}

		public void actionPerformed(AnActionEvent e) {
			connect();
		}
	}

}
