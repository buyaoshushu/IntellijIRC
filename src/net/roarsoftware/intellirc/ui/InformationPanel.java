package net.roarsoftware.intellirc.ui;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;

import com.intellij.openapi.ui.Messages;

import net.roarsoftware.rex.anim.TransparentPanel;

/**
 * @author Janni Kovacs
 */
public class InformationPanel extends TransparentPanel {

	private JLabel iconLabel = new JLabel();
	private JLabel textLabel = new JLabel();

	public InformationPanel() {
		this(null);
	}

	public InformationPanel(String text) {
		this(InformationType.INFO, text);
	}

	public InformationPanel(InformationType type, String text) {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEtchedBorder());
		setType(type);
		setText(text);
		iconLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		textLabel.setVerticalAlignment(JLabel.TOP);
		add(iconLabel, BorderLayout.WEST);
		add(textLabel, BorderLayout.CENTER);
	}

	public void setType(InformationType type) {
		if (type != null)
			iconLabel.setIcon(type.getIcon());
	}

	public void setText(String text) {
		textLabel.setText("<html>" + text + "</html>");
	}

	public static enum InformationType {
		INFO(Messages.getInformationIcon()),
		QUESTION(Messages.getQuestionIcon()),
		WARNING(Messages.getWarningIcon()),
		ERROR(Messages.getErrorIcon());

		private Icon icon;

		InformationType(Icon icon) {
			this.icon = icon;
		}

		public Icon getIcon() {
			return icon;
		}
	}
}
