package net.roarsoftware.intellirc.ui;

import java.util.List;
import java.util.ArrayList;
import javax.swing.JTabbedPane;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * @author Janni Kovacs
 */
public class TabHandler {

	private List<TabGroup> groups = new ArrayList<TabGroup>();
	private JTabbedPane tp;

	public TabHandler(JTabbedPane tp) {
		this.tp = tp;
	}

	public void addTab(TabGroup tabGroup, String name, JComponent component) {
		if(!groups.contains(tabGroup))
			groups.add(tabGroup);
		int index = getGroupIndex(tabGroup) + tabGroup.getTabCount();
		tp.insertTab(name, null, component, name, index);
		tp.setSelectedIndex(index);
	}

	public void removeGroup(TabGroup group) {
		group.removeAll();
		groups.remove(group);
	}

	public void removeTab(TabGroup tabGroup, String name) {
		int index = 0;
		for (TabGroup group : groups) {
			if (group == tabGroup) {
				for (int i = 0; i < tabGroup.getTabCount(); i++) {
					final int x = index + i;
					if (tp.getTitleAt(x).equals(name)) {
						if (!SwingUtilities.isEventDispatchThread()) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									tp.removeTabAt(x);
								}
							});
						} else {
							tp.removeTabAt(x);
						}
						break;
					}
				}
				break;
			}
			index += group.getTabCount();
		}
	}

	public JComponent getActiveTab(TabGroup tabGroup) {
		int index = tp.getSelectedIndex();
		int count = getGroupIndex(tabGroup);
		if (index < count || index >= count + tabGroup.getTabCount()) {
			if(tabGroup.getTabCount() == 0)
				return null;
			return (JComponent) tp.getComponentAt(count);
		}
		return (JComponent) tp.getComponentAt(index);
	}


	public JTabbedPane getTabbedPane() {
		return tp;
	}

	public int getGroupIndex(TabGroup tabGroup) {
		int index = 0;
		for (TabGroup group : groups) {
			if(group == tabGroup)
				break;
			index += group.getTabCount();
		}
		return index;
	}
}
