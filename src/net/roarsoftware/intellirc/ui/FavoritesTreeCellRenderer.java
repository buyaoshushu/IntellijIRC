package net.roarsoftware.intellirc.ui;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.roarsoftware.intellirc.core.ServerSettings;

/**
 * @author Janni Kovacs
 */
public class FavoritesTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final Icon SERVER_ICON = new ImageIcon(
			FavoritesTreeCellRenderer.class.getResource("res/serverFavorite.png"));

	private static final Icon CHANNEL_ICON = new ImageIcon(
			FavoritesTreeCellRenderer.class.getResource("res/channelFavorite.png"));

	@Override
	public Component getTreeCellRendererComponent(JTree jTree, Object object, boolean b, boolean b1, boolean b2, int i,
												  boolean b3) {
		super.getTreeCellRendererComponent(jTree, object, b, b1, b2, i, b3);

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) object;
		Object data = node.getUserObject();
		if (data instanceof ServerSettings) {
			ServerSettings s = (ServerSettings) data;
			setText(s.getHost()+":"+s.getPort());
			setIcon(SERVER_ICON);
		} else {
			setIcon(CHANNEL_ICON);
		}
		return this;
	}
}
