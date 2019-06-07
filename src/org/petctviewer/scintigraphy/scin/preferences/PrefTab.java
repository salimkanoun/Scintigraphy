package org.petctviewer.scintigraphy.scin.preferences;

import ij.Prefs;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;

public abstract class PrefTab extends JPanel implements ActionListener {

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

	public JCheckBox createCheckbox(String prefName, String text, boolean defaultValue) {
		JCheckBox checkBox = new JCheckBox(text);
		checkBox.setActionCommand(prefName);
		checkBox.addActionListener(this);
		// Get state
		boolean state = Prefs.get(prefName, defaultValue);
		checkBox.setSelected(state);
		return checkBox;
	}

	public JPanel createTextInput(String prefName, String text, String afterText, int length) {
		JPanel panel = new JPanel();

		String defaultValue = Integer.toString((int) Prefs.get(prefName, 1));

		JLabel label = new JLabel(text);
		panel.add(label);

		JTextField textField = new JTextField(defaultValue, length);
		textField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				savePref();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				savePref();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				savePref();
			}

			private void savePref() {
				try {
					Prefs.set(prefName, Integer.parseInt(textField.getText()));
					parent.displayMessage(null);
				} catch (NumberFormatException e) {
					Prefs.set(prefName, 1);
					parent.displayMessage("Cannot save (" + textField.getText() + ") -> not a number");
				}
			}
		});
		panel.add(textField);

		if (afterText != null) {
			JLabel afterLabel = new JLabel(afterText);
			panel.add(afterLabel);
		}

		return panel;
	}

}
