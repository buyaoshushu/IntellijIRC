package net.roarsoftware.intellirc.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.Messages;

import net.roarsoftware.intellirc.core.ConnectionListener;
import net.roarsoftware.intellirc.core.ConnectionManager;
import net.roarsoftware.intellirc.core.IrcLog;
import net.roarsoftware.intellirc.core.IrcPlugin;
import net.roarsoftware.intellirc.core.ServerSettings;
import net.roarsoftware.net.irc.IrcConnection;

/**
 * <code>IrcConnectionsWindow</code> is used as a ToolWindow for displaying chat messages.
 *
 * @author Janni Kovacs
 */
public class IrcConnectionsWindow extends JPanel implements ConnectionListener, ChangeListener {

	private JTabbedPane tabs;
	private JPanel startPanel;
	private ConnectionManager connectionManager;

	private Map<IrcConnection, ConnectionHandler> handlers = new HashMap<IrcConnection, ConnectionHandler>();
	private TabHandler handler;


	public IrcConnectionsWindow(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
		connectionManager.addConnectionListener(this);
		tabs = new JTabbedPane();
		tabs.addChangeListener(this);
		handler = new TabHandler(tabs);

		startPanel = new JPanel(new GridBagLayout());
		JPanel buttons = new JPanel();
		JButton b = new JButton(IrcPlugin.getString("window.irc.connect"),
				new ImageIcon(IrcConnectionsWindow.class.getResource("res/connect-large.png")));
		ConnectAction connectAction = new ConnectAction();
		b.addActionListener(connectAction);
		b.setVerticalTextPosition(JButton.BOTTOM);
		b.setHorizontalTextPosition(JButton.CENTER);
		buttons.add(b);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		startPanel.add(buttons, gbc);
		tabs.addTab(IrcPlugin.getString("window.irc.start"), startPanel);

		DefaultActionGroup actions = new DefaultActionGroup();
		actions.add(connectAction);
		actions.add(new JoinAction());
		actions.add(new CloseAction());
		actions.add(new ShowLogAction());
		ActionToolbar ab = ActionManager.getInstance().createActionToolbar("IrcConnectionsWindow", actions, true);
		JComponent toolBar = ab.getComponent();

		setLayout(new BorderLayout());
		add(toolBar, BorderLayout.NORTH);
		add(tabs);
	}

	public void connectionOpened(IrcConnection c) {
		tabs.remove(startPanel);
		handlers.put(c, new ConnectionHandler(c, new TabGroup(handler)));
	}

	public void connectionClosed(IrcConnection c) {
		handlers.remove(c);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (tabs.getTabCount() == 0) {
					tabs.add(IrcPlugin.getString("window.irc.start"), startPanel);
				}
			}
		});
	}

	public void terminate() {
		connectionManager.removeConnectionListener(this);
	}

	public void stateChanged(ChangeEvent changeEvent) {
		if (tabs.getSelectedIndex() != -1)
			tabs.setForegroundAt(tabs.getSelectedIndex(), null);
		Component c = tabs.getSelectedComponent();
		if (c instanceof StatusPanel) {
			StatusPanel panel = (StatusPanel) c;
			panel.requestFocus();
		}
	}

	private class ConnectAction extends IdeaAction implements ActionListener {

		public ConnectAction() {
			super(IrcPlugin.getString("window.irc.connect"),
					new ImageIcon(IrcConnectionsWindow.class.getResource("res/connect.png")));
		}

		public void actionPerformed(AnActionEvent e) {
			actionPerformed();
		}

		public void actionPerformed(ActionEvent actionEvent) {
			actionPerformed();
		}

		private void actionPerformed() {
			ServerConfigDialog d = new ServerConfigDialog(IrcPlugin.getInstance());
			d.setLocationRelativeTo(null);
			d.setVisible(true);
			if (d.getSelection() == JOptionPane.OK_OPTION) {
				final ServerSettings s = new ServerSettings();
				d.getData(s);
				new Thread() {
					public void run() {
						connectionManager.connect(s);
					}
				}.start();
			}
		}
	}

	private class JoinAction extends IdeaAction {

		public JoinAction() {
			super(IrcPlugin.getString("window.irc.join"),
					new ImageIcon(IrcConnectionsWindow.class.getResource("res/join.png")));
		}

		public void actionPerformed(AnActionEvent e) {
			StatusPanel p = (StatusPanel) tabs.getSelectedComponent();
			IrcConnection c = p.getConnection();
			String s = Messages
					.showInputDialog(IrcConnectionsWindow.this, IrcPlugin.getString("window.favs.addChannel.message"),
							IrcPlugin.getString("window.irc.join"), Messages.getQuestionIcon());
			if (s == null)
				return;
			if (!s.startsWith("#"))
				s = "#" + s;
			c.join(s);
		}

		@Override
		protected boolean isEnabled() {
			Component comp = tabs.getSelectedComponent();
			return connectionManager.getConnectionCount() != 0 && ((StatusPanel) comp)
					.getConnection().isConnected();
		}
	}

	private class CloseAction extends IdeaAction {

		public CloseAction() {
			super(IrcPlugin.getString("window.irc.quit"),
					new ImageIcon(IrcConnectionsWindow.class.getResource("res/quit.png")));
		}

		public void actionPerformed(AnActionEvent e) {
			StatusPanel p = (StatusPanel) tabs.getSelectedComponent();
			IrcConnection c = p.getConnection();
			if (p instanceof ChatPanel) {
				ChatPanel cp = (ChatPanel) p;
				if (cp.getReciever().startsWith("#"))
					c.part(cp.getReciever());
				else
					tabs.remove(cp);
			} else {
				connectionManager.close(c, IrcPlugin.getInstance().getSettings().getSignoffMessage());
			}
		}

		@Override
		protected boolean isEnabled() {
			return tabs.getSelectedComponent() != startPanel;
		}
	}

	private class ShowLogAction extends IdeaAction {

		public ShowLogAction() {
			super(IrcPlugin.getString("window.irc.show-log"),
					new ImageIcon(IrcConnectionsWindow.class.getResource("res/showlog.png")));
		}

		public void actionPerformed(AnActionEvent e) {
			final ChatPanel p = (ChatPanel) tabs.getSelectedComponent();
			final IrcConnection c = p.getConnection();
			new Thread() {
				public void run() {
					IrcLog log = IrcPlugin.getInstance().getLog();
					String s = log.getLog(c.getHost(), p.getReciever());
					JDialog d = new JDialog();
					d.setTitle("Log");
					d.add(new JScrollPane(new JTextArea(s)));
					d.setSize(400, 300);
					d.setLocationRelativeTo(null);
					d.setVisible(true);
				}
			}.start();
		}

		@Override
		protected boolean isEnabled() {
			return connectionManager.getConnectionCount() != 0 && tabs.getSelectedComponent() instanceof ChatPanel;
		}
	}
}