package org.petctviewer.scintigraphy.scin.instructions.prompts;

import javax.swing.JDialog;

public abstract class PromptDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public abstract Object getResult();

	public abstract boolean isCompleted();

}
