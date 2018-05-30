package net.roarsoftware.intellirc.ui;

import javax.swing.Icon;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author Janni Kovacs
 */
public abstract class IdeaAction extends AnAction {

	public IdeaAction(String text, Icon icon) {
		super(text, text, icon);
	}

	@Override
	public void update(AnActionEvent e) {
		e.getPresentation().setEnabled(isEnabled());
	}

	protected boolean isEnabled() {
		return true;
	}

}
