package net.roarsoftware.intellirc.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * @author Janni Kovacs
 */
public class TabGroup {
	private class Tab {

		String name;
		JComponent component;

		public Tab(String name, JComponent component) {
			this.name = name;
			this.component = component;
		}
	}

	private TabHandler handler;
	private List<Tab> tabs = new ArrayList<Tab>();

	public TabGroup(TabHandler h) {
		this.handler = h;
	}

	public void addTab(String name, JComponent component) {
		handler.addTab(this, name, component);
		tabs.add(new Tab(name, component));
	}

	public void removeTab(String name) {
		for (Iterator<Tab> it = tabs.iterator(); it.hasNext();) {
			Tab tab = it.next();
			if (tab.name.equals(name)) {
				handler.removeTab(this, name);
				it.remove();
				break;
			}
		}
	}

	public void removeAll() {
		for (int i = 0, n = getTabCount(); i < n; i++) {
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						handler.getTabbedPane().removeTabAt(getGroupIndex());
					}
				});
			} else {
				handler.getTabbedPane().removeTabAt(getGroupIndex());
			}
		}
		tabs.clear();
	}

	public JComponent getTab(String name) {
		for (Tab tab : tabs) {
			if (tab.name.equals(name))
				return tab.component;
		}
		return null;
	}

	public int getTabCount() {
		return tabs.size();
	}

	public JComponent getActiveTab() {
		return handler.getActiveTab(this);
	}

	public JComponent[] getAllTabs() {
		JComponent[] c = new JComponent[tabs.size()];
		for (int i = 0; i < tabs.size(); i++) {
			Tab tab = tabs.get(i);
			c[i] = tab.component;
		}
		return c;
	}

	public JTabbedPane getTabbedPane() {
		return handler.getTabbedPane();
	}

	public int getGroupIndex() {
		return handler.getGroupIndex(this);
	}

	public String getTitleAt(int index) {
		return getTabbedPane().getTitleAt(getGroupIndex() + index);
	}

}
