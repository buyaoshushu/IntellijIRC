package net.roarsoftware.intellirc.ui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.ResourceBundle;
import javax.swing.*;

import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.Spacer;

import net.roarsoftware.intellirc.core.IrcPlugin;
import net.roarsoftware.intellirc.core.ServerSettings;
import net.roarsoftware.intellirc.ui.InformationPanel.InformationType;
import net.roarsoftware.rex.anim.FadeEffect;
import net.roarsoftware.rex.anim.FadeEffect.FadeType;

public class ServerConfigDialog extends JDialog {
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JTextField host;
	private JPasswordField password;
	private JTextField nickname;
	private JTextField username;
	private JTextField realname;
	private InformationPanel informationPanel;
	private JButton expandArea;
	private JPanel expandedArea;
	private JCheckBox automaticallyConnectToThisCheckBox;
	private JTextField port;
	private int selection = JOptionPane.OK_OPTION;
	private boolean expanded;
	private IrcPlugin plugin;
	private ServerSettings data;

	public ServerConfigDialog(IrcPlugin plugin) {
		this.plugin = plugin;
		setTitle(IrcPlugin.getString("window.favs.addServer.title"));
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);
		expandArea.setIcon(UIManager.getIcon("Tree.collapsedIcon"));
		expandedArea.setVisible(false);
		
		final ResourceBundle b = ResourceBundle.getBundle("net.roarsoftware.intellirc.ui.res.Forms");
		if (!plugin.getSettings().getNickname().equals("")) {
			informationPanel.setText(b.getString("irc.pass-not-mandatory"));
		} else {
			informationPanel.setText(b.getString("irc.nickname-mandatory"));
		}

		buttonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});

// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		expandArea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				expanded = !expanded;
				expandArea.setIcon(
						expanded ? UIManager.getIcon("Tree.expandedIcon") : UIManager.getIcon("Tree.collapsedIcon"));
				expandArea.setText(expanded ? b.getString("less") : b.getString("more"));
				expandedArea.setVisible(expanded);
				// following code is quite o_O ...
				contentPane.revalidate();
				contentPane.repaint();
				Dimension pref = getPreferredSize();
				setSize(getWidth(), pref.height);
				validate();
				repaint();
			}
		});
		pack();
		setSize(350, getPreferredSize().height);
	}

	private void createUIComponents() {
		informationPanel = new InformationPanel();
	}
	
	private void onOK() {
		String warningMessage = null;
		ResourceBundle b = ResourceBundle.getBundle("net.roarsoftware.intellirc.ui.res.Forms");
		if ("".equals(host.getText())) {
			warningMessage = b.getString("irc.host-mandatory");
		} else if (findServer(host.getText(),Integer.parseInt(port.getText())) != null && findServer(host.getText(),Integer.parseInt(port.getText())) != this.data) {
			warningMessage = b.getString("irc.favorite-server-exists");
		} else if (plugin.getSettings().getNickname().equals("") && nickname.getText().equals("")) {
			warningMessage = b.getString("irc.nickname-mandatory");
		} else {
			dispose();
		}
		if (warningMessage != null) {
			final String warningMessage1 = warningMessage;
			new Thread() {
				@Override
				public void run() {
					Thread thread = FadeEffect.startFading(informationPanel, FadeType.FADE_OUT, 0.25f);
					try {
						thread.join();
					} catch (InterruptedException e) {
						// never
					}
					informationPanel.setType(InformationType.WARNING);
					informationPanel.setText(warningMessage1);
					FadeEffect.startFading(informationPanel, FadeType.FADE_IN, 0.25f);
				}
			}.start();
		}
	}

	private ServerSettings findServer(String host,Integer port) {
		for (ServerSettings settings : plugin.getFavoriteModel().getFavorites().keySet()) {
			if (settings.getHost().equals(host)&&settings.getPort().equals(port))
				return settings;
		}
		return null;
	}

	private void onCancel() {
		selection = JOptionPane.CANCEL_OPTION;
		dispose();
	}

	public int getSelection() {
		return selection;
	}

	public void setData(ServerSettings data) {
		this.data = data;
		host.setText(data.getHost());
		port.setText(data.getPort()+"");
		password.setText(data.getPassword());
		nickname.setText(data.getNick());
		username.setText(data.getUser());
		realname.setText(data.getReal());
		automaticallyConnectToThisCheckBox.setSelected(data.isAutoConnect());
	}

	public void getData(ServerSettings data) {
		String hoststr=host.getText();
		data.setHost(host.getText());
		data.setPort(Integer.parseInt(port.getText()));
		data.setPassword(password.getText());
		data.setNick(nickname.getText());
		data.setUser(username.getText());
		data.setReal(realname.getText());
		data.setAutoConnect(automaticallyConnectToThisCheckBox.isSelected());
	}

	public boolean isModified(ServerSettings data) {
		if (host.getText() != null ? !host.getText().equals(data.getHost()) : data.getHost() != null) return true;
		if (password.getText() != null ? !password.getText().equals(data.getPassword()) : data.getPassword() != null)
			return true;
		if (nickname.getText() != null ? !nickname.getText().equals(data.getNick()) : data.getNick() != null)
			return true;
		if (username.getText() != null ? !username.getText().equals(data.getUser()) : data.getUser() != null)
			return true;
		if (realname.getText() != null ? !realname.getText().equals(data.getReal()) : data.getReal() != null)
			return true;
		if (automaticallyConnectToThisCheckBox.isSelected() != data.isAutoConnect()) return true;
		return false;
	}

}
