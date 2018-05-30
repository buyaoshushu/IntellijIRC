package net.roarsoftware.intellirc.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import net.roarsoftware.intellirc.core.IrcPlugin;
import net.roarsoftware.net.irc.ChannelMessage;
import net.roarsoftware.net.irc.Formatting;
import net.roarsoftware.net.irc.IrcConnection;
import net.roarsoftware.net.irc.IrcUtil;
import net.roarsoftware.net.irc.MessageType;

/**
 * @author Janni Kovacs
 */
public class StatusPanel extends JPanel implements ActionListener {

	protected JTextPane textArea;
	protected JTextField input;
	protected JButton send;
	protected JSplitPane split;
	protected IrcConnection connection;

	protected MutableAttributeSet red;
	protected MutableAttributeSet blue;
	protected MutableAttributeSet action;

	protected DateFormat dateFormat = new SimpleDateFormat("[HH:mm]");

	public StatusPanel() {
		textArea = new JTextPane();
		textArea.setEditable(false);
		textArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent mouseEvent) {
				textArea.copy();
				input.requestFocusInWindow();
			}
		});
		input = new JTextField();
		input.addActionListener(this);
		send = new JButton(IrcPlugin.getString("window.irc.send"));
		send.addActionListener(this);
		split = new JSplitPane();
		split.setLeftComponent(new JScrollPane(textArea));
		split.setRightComponent(null);
		split.setDividerSize(0);

		setLayout(new BorderLayout());
		add(split);
		JPanel bottom = new JPanel(new BorderLayout());
		bottom.add(input);
		bottom.add(send, BorderLayout.EAST);
		add(bottom, BorderLayout.SOUTH);

		red = new SimpleAttributeSet();
		StyleConstants.setForeground(red, Color.RED);
		blue = new SimpleAttributeSet();
		StyleConstants.setForeground(blue, Color.BLUE);
		action = new SimpleAttributeSet();
		StyleConstants.setForeground(action, new Color(156, 0, 156));
	}

	public void setConnection(IrcConnection c) {
		this.connection = c;
	}

	public IrcConnection getConnection() {
		return connection;
	}

	public void actionPerformed(ActionEvent e) {
		String text = input.getText();
		input.setText(null);
		if (connection == null) {
			appendMessage(IrcPlugin.getString("irc.not-connected"));
		} else {
			if (text.charAt(0) != '/') {
				appendMessage(IrcPlugin.getString("irc.not-on-channel"));
			} else if (connection.isConnected())
				connection.sendRaw(text.substring(1));
		}
	}

	public String appendMessage(ChannelMessage msg) {
		checkLines();
		StyledDocument doc = textArea.getStyledDocument();
		String nick = IrcUtil.getNick(msg.getSource());
		boolean we = nick.equals(connection.getNick());
		String s = IrcPlugin.getInstance().getSettings().isShowTimestamp() ? dateFormat
				.format(new Date(msg.getDate())) : "";
		AttributeSet as = blue;
		MutableAttributeSet base = null;
		if (we)
			as = red;
		if (msg.getType() == MessageType.NOTICE) {
			String x = we ? " -> -" : " -";
			s += x + nick + "- ";
			as = red;
			base = red;
		} else if (msg.getType() == MessageType.ACTION) {
			s += " * " + nick + " ";
			as = action;
			base = action;
		} else {
			s += " " + nick + ": ";
		}
		try {
			doc.insertString(doc.getLength(), s, as);
			if (IrcPlugin.getInstance().getSettings().isEnableColors())
				Formatting.formatText(doc, msg.getMessage(), base);
			else
				doc.insertString(doc.getLength(), msg.getMessage(), base);
			doc.insertString(doc.getLength(), "\n", null);
			textArea.setCaretPosition(doc.getLength());
		} catch (BadLocationException e) {
		}
		return s + msg.getMessage();
	}

	public void appendMessage(String string) {
		checkLines();
		StyledDocument doc = textArea.getStyledDocument();
		try {
			String str = dateFormat.format(new Date()) + " *** ";
			doc.insertString(doc.getLength(), str, red);
			Formatting.formatText(doc, string, red);
			doc.insertString(doc.getLength(), "\n", null);
			textArea.setCaretPosition(doc.getLength());
		} catch (BadLocationException e) {
		}
	}

	private void checkLines() {
		if (textArea.getDocument().getDefaultRootElement().getElementCount() >= 300) {
			try {
				textArea.getDocument().remove(0, textArea.getText().indexOf('\n') + 1);
			} catch (BadLocationException e) {
			}
		}
	}


	@Override
	public void requestFocus() {
		input.requestFocusInWindow();
	}
}
