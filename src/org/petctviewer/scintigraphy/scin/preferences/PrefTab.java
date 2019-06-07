package org.petctviewer.scintigraphy.scin.preferences;

import ij.Prefs;
import org.petctviewer.scintigraphy.scin.library.Library_Debug;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class PrefTab extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	protected final JPanel mainPanel;
	protected final PrefWindow parent;
	private final JPanel pnl_titre;
	private String prefTabName;
	private JLabel title;

	public PrefTab(PrefWindow parent, String tabName) {
		// Set variable
		this.prefTabName = tabName;
		this.parent = parent;

		this.setLayout(new BorderLayout());
		pnl_titre = new JPanel();
		this.add(pnl_titre, BorderLayout.NORTH);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		this.add(mainPanel, BorderLayout.CENTER);
	}

	public String getTabName() {
		return this.prefTabName;
	}

	public void setTitle(String title) {
		if (this.title == null) {
			this.title = new JLabel();
			this.pnl_titre.add(this.title);
		}
		this.title.setText("<html><h3>" + title + "</h3></html>");
	}

	/**
	 * Creates a checkbox. When using this method, any change on this checkbox will be save in the preferences
	 * automatically.
	 *
	 * @param prefName     String of the preference (cannot be null)
	 * @param text         Text displayed for the user on the checkbox (null accepted)
	 * @param defaultValue Default value if the preference doesn't exist
	 * @return JCheckBox created
	 */
	public JCheckBox createCheckbox(String prefName, String text, boolean defaultValue) {
		JCheckBox checkBox = new JCheckBox(Library_Debug.preventNull(text));
		checkBox.setActionCommand(prefName);
		checkBox.addActionListener(this);
		// Get state
		boolean state = Prefs.get(prefName, defaultValue);
		checkBox.setSelected(state);
		return checkBox;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JCheckBox) {
			JCheckBox check = (JCheckBox) e.getSource();
			// Save value in prefs
			Prefs.set(check.getActionCommand(), check.isSelected());
			this.parent.displayMessage("Please close the window to save the preferences", PrefWindow.DURATION_SHORT);
		}
	}
}
