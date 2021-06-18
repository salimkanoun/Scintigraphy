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
	private final JPanel pnl_titre;
	/**
	 * Preference window where this tab is linked to.<br> Can be null!
	 */
	protected JFrame parent;
	private final String prefTabName;
	private final JLabel title;

	/**
	 * Instantiates a new preference tab not linked to any preference window.
	 *
	 * @param tabName Name of the tab (used for display)
	 */
	public PrefTab(String tabName) {
		this(tabName, null);
	}

	/**
	 * Instantiates a new preference tab.
	 *
	 * @param tabName Name of the tab (used for display)
	 * @param parent  Window this tab is linked to (can be null if no parent)
	 */
	public PrefTab(String tabName, JFrame parent) {
		// Set variable
		this.prefTabName = tabName;
		this.parent = parent;

		this.setLayout(new BorderLayout());
		pnl_titre = new JPanel();
		this.title = new JLabel(tabName);
		pnl_titre.add(title);
		this.add(pnl_titre, BorderLayout.NORTH);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		this.add(mainPanel, BorderLayout.CENTER);
	}

	/**
	 * Changes the preference window of this tab.<br> This method is used to delay the instantiation of the tab.
	 *
	 * @param parent Preference window this tab is linked to
	 */
	public void setParent(PrefWindow parent) {
		this.parent = parent;
	}

	/**
	 * @return name of this tab
	 */
	public String getTabName() {
		return this.prefTabName;
	}

	/**
	 * Sets the title on the tab. The title is different than the name. The title is displayed on the panel. By
	 * default,
	 * the title is set with the name of the tab. If you want to remove the title, then pass null to this method.
	 *
	 * @param title Title for the tab (null removes title)
	 */
	public void setTitle(String title) {
		if (title == null)
			// Remove title
			this.pnl_titre.remove(this.title);
		else this.title.setText("<html><h3>" + title + "</h3></html>");
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
			if (this.parent != null && this.parent instanceof PrefWindow) ((PrefWindow) this.parent).displayMessage(
					"Please close the window to save the preferences", PrefWindow.DURATION_SHORT);
		}
	}
}
