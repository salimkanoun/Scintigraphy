package org.petctviewer.scintigraphy.scin.instructions.prompts;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.controller.ControleurScin;

/**
 * This class represents a dialog prompt for a time (hours, minutes, seconds) in
 * 24H format.
 * 
 * @author Titouan QUÉMA
 *
 */
public class PromptTime extends PromptDialog {
	private static final long serialVersionUID = 1L;

	protected JComboBox<Integer> hours, minutes, seconds;
	protected Date autofillDate;

	/**
	 * Panel used for displaying the instructions and informations for the user.<br>
	 * Typically, this is where the message passed on the constructor is displayed.
	 */
	protected JPanel panMsg;

	/**
	 * @param timeToPrompt String representing the time prompted (used for display)
	 */
	public PromptTime(ControleurScin controller, String timeToPrompt) {
		this(controller, timeToPrompt, null, null);
	}

	/**
	 * @param timeToPrompt String representing the time prompted (used for display)
	 * @param message      Message shown to the user (can be set later using the
	 *                     panCenter panel)
	 */
	public PromptTime(ControleurScin controller, String timeToPrompt, String message) {
		this(controller, timeToPrompt, message, null);
	}

	/**
	 * @param timeToPrompt String representing the time prompted (used for display)
	 * @param autofillDate Time autofilled on the combo box
	 * @throws IllegalArgumentException if the controller is null
	 */
	public PromptTime(ControleurScin controller, String timeToPrompt, Date autofillDate) {
		this(controller, timeToPrompt, null, autofillDate);
	}

	/**
	 * @param timeToPrompt String representing the time prompted (used for display)
	 * @param message      Message shown to the user (can be set later using the
	 *                     panCenter panel)
	 * @param autofillDate Time autofilled on the combo box
	 * @throws IllegalArgumentException if the controller is null
	 */
	public PromptTime(ControleurScin controller, String timeToPrompt, String message, Date autofillDate)
			throws IllegalArgumentException {
		if (controller == null)
			throw new IllegalArgumentException("The controller must not be null");

		if (timeToPrompt != null)
			this.setTitle(timeToPrompt + " Time");
		else
			this.setTitle("Time prompt");

		this.autofillDate = autofillDate;

		// Autofill the fields of the combo box with the autofillDate
		if (this.autofillDate != null) {
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTime(this.autofillDate);
			this.hours = this.generateComboBox(24, calendar.get(Calendar.HOUR_OF_DAY));
			this.minutes = this.generateComboBox(60, calendar.get(Calendar.MINUTE));
			this.seconds = this.generateComboBox(60, calendar.get(Calendar.SECOND));
		} else {
			this.hours = this.generateComboBox(24, 0);
			this.minutes = this.generateComboBox(24, 0);
			this.seconds = this.generateComboBox(24, 0);
		}

		JPanel panel = new JPanel(new BorderLayout());

		JPanel panCenter = new JPanel(new BorderLayout());
		this.panMsg = new JPanel(new GridLayout(0, 1));
		if (message != null)
			this.panMsg.add(new JLabel(message));
		panCenter.add(panMsg, BorderLayout.CENTER);
		JPanel panTime = new JPanel();
		panTime.add(new JLabel("Hours:"));
		panTime.add(this.hours);
		panTime.add(new JLabel("Minutes:"));
		panTime.add(this.minutes);
		panTime.add(new JLabel("Seconds:"));
		panTime.add(this.seconds);
		panCenter.add(panTime, BorderLayout.SOUTH);
		panel.add(panCenter, BorderLayout.CENTER);

		JButton btnOk = new JButton("Validate");
		btnOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isInputValid()) {
					dispose();
					controller.clickNext();
				}
			}
		});
		panel.add(btnOk, BorderLayout.SOUTH);

		this.getContentPane().add(panel);
		this.pack();
	}

	/**
	 * Generates a JComboBox with all numbers from 0 to number (excluded).
	 * 
	 * @param number Maximum number (excluded) to generate the answers
	 * @return JComboBox with 0 to number answers
	 */
	private JComboBox<Integer> generateComboBox(int number, int defaultValue) {
		JComboBox<Integer> comboBox = new JComboBox<>();
		for (int i = 0; i < number; i++)
			comboBox.addItem(i);
		comboBox.setSelectedIndex(defaultValue);
		return comboBox;
	}

	@Override
	public Date getResult() {
		Calendar calendar = GregorianCalendar.getInstance();
		if (this.autofillDate != null)
			calendar.setTime(this.autofillDate);
		calendar.set(Calendar.HOUR_OF_DAY, (int) this.hours.getSelectedItem());
		calendar.set(Calendar.MINUTE, (int) this.minutes.getSelectedItem());
		calendar.set(Calendar.SECOND, (int) this.seconds.getSelectedItem());
		return calendar.getTime();
	}

	@Override
	public boolean isInputValid() {
		// The time is always valid since the combo boxes only display valid time
		return true;
	}

}
