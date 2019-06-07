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

public class PrefsWindows extends WindowAdapter implements PlugIn {
	public static final String PREF_HEADER = "petctviewer.scin";

	public static final int DURATION_SHORT = 2000;

	private Container main, renal, bone, gastric;

	private JLabel status;
	private Timer timer;

	@Override
	public void run(String arg) {
		this.main = new prefsTabMain(this);
		this.renal = new prefsTabRenal();
		this.bone = new prefsTabBone();
		this.gastric = new PrefsTabGastric(this);
		showGUI();
	}

	@Override
	public void windowClosed(WindowEvent e) {
		super.windowClosed(e);
		Prefs.savePreferences();
	}

	private void showGUI() {

		// Create and set up the window.
		JFrame frame = new JFrame("Results Renal Exam");
		frame.addWindowListener(this);
		frame.getContentPane().setLayout(new GridLayout(1, 1));

		JPanel panel = new JPanel(new BorderLayout());

		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);

		tabbedPane.addTab("Main", this.main);
		tabbedPane.addTab("Renal", this.renal);
		tabbedPane.addTab("Bone", this.bone);
		tabbedPane.addTab("Gastric", this.gastric);

		// Status bar
		JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
		statusBar.setBorder(new CompoundBorder(new LineBorder(Color.DARK_GRAY), new EmptyBorder(4, 4, 4, 4)));
		this.status = new JLabel();
		this.status.setFont(this.status.getFont().deriveFont(12f));
		statusBar.add(this.status);
		panel.add(statusBar, BorderLayout.SOUTH);

		panel.add(tabbedPane, BorderLayout.CENTER);
		frame.getContentPane().add(panel);

		// Display the window
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	/**
	 * Displays a message until a new one is set.<br>
	 * If the message is null, then this method removes the previous message.
	 * 
	 * @param message Message to display
	 * @see #displayMessage(String, int)
	 */
	public void displayMessage(String message) {
		this.status.setText(message);
	}

	/**
	 * Displays a message for a period of time in the status bar.<br>
	 * If the message is null, then no message is displayed (this is equivalent to
	 * call <code>displayMessage(message)</code>).<br>
	 * If a display method is called before the duration ends, then this message
	 * will be overridden.
	 * 
	 * @param message  Message to display
	 * @param duration Duration of the message (milliseconds)
	 * @see #displayMessage(String)
	 */
	public void displayMessage(String message, int duration) {
		// Remove previous timer
		if (this.timer != null)
			this.timer.stop();

		status.setText(message);

		this.timer = new Timer(2000, new ActionListener() {
			int alpha = 255;
			final int increment = 10;

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
					} else
						status.setForeground(new Color(status.getForeground().getRed(),
								status.getForeground().getGreen(), status.getForeground().getBlue(), alpha));
				}).start();
			}
		});
		timer.setRepeats(false);
		timer.start();
	}

}
