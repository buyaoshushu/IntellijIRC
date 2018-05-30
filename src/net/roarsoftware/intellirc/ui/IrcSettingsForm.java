package net.roarsoftware.intellirc.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ResourceBundle;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.roarsoftware.intellirc.core.IrcPlugin;
import net.roarsoftware.intellirc.core.IrcSettings;

/**
 * @author Janni Kovacs
 */
public class IrcSettingsForm {
	private JCheckBox logConversations;
	private JCheckBox showTimestamp;
	private JCheckBox enableColors;
	private JCheckBox enableSmilies;
	private JPanel rootComponent;
	private JLabel logFolderLabel;
	private JTextField textField1;
	private JCheckBox enableIdentd;
	private JTextField identdUser;
	private JFormattedTextField identdPort;
	private JTextField identdSystem;
	private JLabel userIdLabel;
	private JLabel systemLabel;
	private JLabel portLabel;
	private JTextField nickname;
	private JTextField username;
	private JTextField realname;
	private JTextField logsWillBeSavedTextField;
	private InformationPanel informationPanel1;

	public IrcSettingsForm() {
		enableIdentd.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				boolean b = enableIdentd.isSelected();
				identdPort.setEnabled(b);
				portLabel.setEnabled(b);
				identdUser.setEnabled(b);
				userIdLabel.setEnabled(b);
				identdSystem.setEnabled(b);
				systemLabel.setEnabled(b);
			}
		});
		logConversations.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				boolean b = logConversations.isSelected();
				logFolderLabel.setEnabled(b);
				logsWillBeSavedTextField.setEnabled(b);
			}
		});
		logsWillBeSavedTextField.setText(IrcPlugin.getInstance().getLog().getPath().getAbsolutePath());
	}

	public JPanel getRootComponent() {
		return rootComponent;
	}

	public void setData(IrcSettings data) {
		logConversations.setSelected(data.isLogConversations());
		enableSmilies.setSelected(data.isEnableSmilies());
		enableColors.setSelected(data.isEnableColors());
		showTimestamp.setSelected(data.isShowTimestamp());
		enableIdentd.setSelected(data.isEnableIdentd());
		nickname.setText(data.getNickname());
		username.setText(data.getUsername());
		realname.setText(data.getRealname());
		textField1.setText(data.getSignoffMessage());
		identdUser.setText(data.getIdentdUser());
		identdSystem.setText(data.getIdentdSystem());
		identdPort.setValue(data.getIdentdPort());
	}

	public void getData(IrcSettings data) {
		data.setLogConversations(logConversations.isSelected());
		data.setEnableSmilies(enableSmilies.isSelected());
		data.setEnableColors(enableColors.isSelected());
		data.setShowTimestamp(showTimestamp.isSelected());
		data.setEnableIdentd(enableIdentd.isSelected());
		data.setNickname(nickname.getText());
		data.setUsername(username.getText());
		data.setRealname(realname.getText());
		data.setSignoffMessage(textField1.getText());
		data.setIdentdUser(identdUser.getText());
		data.setIdentdSystem(identdSystem.getText());
		data.setIdentdPort((Integer) identdPort.getValue());
	}

	public boolean isModified(IrcSettings data) {
		if (logConversations.isSelected() != data.isLogConversations()) return true;
		if (enableSmilies.isSelected() != data.isEnableSmilies()) return true;
		if (enableColors.isSelected() != data.isEnableColors()) return true;
		if (showTimestamp.isSelected() != data.isShowTimestamp()) return true;
		if (enableIdentd.isSelected() != data.isEnableIdentd()) return true;
		if (nickname.getText() != null ? !nickname.getText().equals(data.getNickname()) : data.getNickname() != null)
			return true;
		if (username.getText() != null ? !username.getText().equals(data.getUsername()) : data.getUsername() != null)
			return true;
		if (realname.getText() != null ? !realname.getText().equals(data.getRealname()) : data.getRealname() != null)
			return true;
		if (textField1.getText() != null ? !textField1.getText().equals(data.getSignoffMessage()) : data
				.getSignoffMessage() != null) return true;
		if (identdUser.getText() != null ? !identdUser.getText().equals(data.getIdentdUser()) : data
				.getIdentdUser() != null) return true;
		if (identdSystem.getText() != null ? !identdSystem.getText().equals(data.getIdentdSystem()) : data
				.getIdentdSystem() != null) return true;
		if (identdPort.getValue() != null ? !((Integer) identdPort.getValue() == data.getIdentdPort()) : data
				.getIdentdPort() != 0) return true;
		return false;
	}

	private void createUIComponents() {
		ResourceBundle bundle = ResourceBundle.getBundle("net.roarsoftware.intellirc.ui.res.Forms");
		informationPanel1 = new InformationPanel(bundle.getString("irc.nick-not-mandatory"));
	}

}
