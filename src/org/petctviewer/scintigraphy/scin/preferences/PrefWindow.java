package org.petctviewer.scintigraphy.scin.preferences;

import ij.Prefs;
import ij.plugin.PlugIn;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class PrefWindow extends WindowAdapter implements PlugIn {
	public static final String PREF_HEADER = "petctviewer.scin";

	public static final int DURATION_SHORT = 2000;

	private List<PrefTab> tabs;

	private JLabel status;
	private Timer timer;

	@Override
	public void run(String arg) {
		this.tabs = new ArrayList<>();

		this.addTab(new PrefTabMain(this));
		this.addTab(new PrefTabRenal(this));
		this.addTab(new PrefTabBone(this));
		this.addTab(new PrefTabGastric(this));

		showGUI();
	}

	@Override
	public void windowClosed(WindowEvent e) {
		super.windowClosed(e);
		Prefs.savePreferences();
	}

	/**
	 * Adds the specified preference tab to this window.
	 *
	 * @param tab Tab to add
	 */
	private void addTab(PrefTab tab) {
		this.tabs.add(tab);
	}

	private void showGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("Preferences");
		frame.addWindowListener(this);
		frame.getContentPane().setLayout(new GridLayout(1, 1));

		JPanel panel = new JPanel(new BorderLayout());

		// Tabs
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		for (PrefTab tab : this.tabs)
			tabbedPane.addTab(tab.getTabName(), tab);
		panel.add(tabbedPane, BorderLayout.CENTER);

		// Status bar
		JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
		statusBar.setBorder(new CompoundBorder(new LineBorder(Color.DARK_GRAY), new EmptyBorder(4, 4, 4, 4)));
		this.status = new JLabel();
		this.status.setFont(this.status.getFont().deriveFont(12f));
		statusBar.add(this.status);
		panel.add(statusBar, BorderLayout.SOUTH);

		frame.getContentPane().add(panel);

		// Display the window
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
	}

	/**
	 * Displays a message until a new one is set.<br> If the message is null, then this method removes the previous
	 * message.
	 *
	 * @param message Message to display
	 * @see #displayMessage(String, int)
	 */
	public void displayMessage(String message) {
		this.status.setText(message);
	}

	/**
	 * Displays a message for a period of time in the status bar.<br> If the message is null, then no message is
	 * displayed (this is equivalent to call <code>displayMessage(message)</code>).<br> If a display method is called
	 * before the duration ends, then this message will be overridden.
	 *
	 * @param message  Message to display
	 * @param duration Duration of the message (milliseconds)
	 * @see #displayMessage(String)
	 */
	public void displayMessage(String message, int duration) {
		// Remove previous timer
		if (this.timer != null) this.timer.stop();

		status.setText(message);

		this.timer = new Timer(2000, new ActionListener() {
			final int increment = 10;
			int alpha = 255;

			@Override
			public void actionPerformed(ActionEvent e) {
				// Fade out
				new Timer(80, e1 -> {
					alpha -= increment;
					if (alpha <= 0) {
						alpha = 0;
						((Timer) e1.getSource()).stop();
						status.setText(null);
						status.setForeground(Color.BLACK);
					} else status.setForeground(
							new Color(status.getForeground().getRed(), status.getForeground().getGreen(),
									  status.getForeground().getBlue(), alpha));
				}).start();
			}
		});
		timer.setRepeats(false);
		timer.start();
	}

}