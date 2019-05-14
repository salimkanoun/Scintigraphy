package org.petctviewer.scintigraphy.gastric_refactored;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JLabel;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.instructions.prompts.PromptTime;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

/**
 * This class represents a dialog prompt for the ingestion time.
 * 
 * @author Titouan QUÉMA
 *
 */
public class PromptIngestionTime extends PromptTime {
	private static final long serialVersionUID = 1L;

	private JLabel lError;

	private Date acquisitionTime;

	public PromptIngestionTime(ControleurScin controller) {
		super(controller, "Ingestion",
				generateSupposedIngestionTime(Library_Dicom.getDateAcquisition(controller.getModel().getImagePlus())));

		this.setTitle("Ingestion Time");
		this.setLocationRelativeTo(controller.getVue());

		this.acquisitionTime = Library_Dicom.getDateAcquisition(controller.getModel().getImagePlus());

		panMsg.add(new JLabel("The acquisition time of the first image is "
				+ DateFormat.getTimeInstance().format(this.acquisitionTime)));
		panMsg.add(new JLabel("Please enter the time of the ingestion:"));
		lError = new JLabel();
		lError.setForeground(Color.RED);
		panMsg.add(this.lError);

		CheckTimeListener listener = new CheckTimeListener();
		this.hours.addItemListener(listener);
		this.minutes.addItemListener(listener);
		this.seconds.addItemListener(listener);

		this.pack();
	}

	private static Date generateSupposedIngestionTime(Date acquisitionDate) {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(acquisitionDate);
		// Generally, the ingestion time is 30 minutes before the first acquisition
		// (this is just for convenience)
		calendar.add(Calendar.MINUTE, -30);
		return calendar.getTime();
	}

	@Override
	public Date getResult() {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(this.acquisitionTime);
		calendar.set(Calendar.HOUR_OF_DAY, (int) this.hours.getSelectedItem());
		calendar.set(Calendar.MINUTE, (int) this.minutes.getSelectedItem());
		calendar.set(Calendar.SECOND, (int) this.seconds.getSelectedItem());
		return calendar.getTime();
	}

	@Override
	public boolean isInputValid() {
		// Time of ingestion must be before time of first acquisition
		return this.getResult().before(acquisitionTime);
	}

	/**
	 * This listener checks if this prompt is completed and displays a message if
	 * not.
	 * 
	 * @author Titouan QUÉMA
	 *
	 */
	private class CheckTimeListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				if (isInputValid()) {
					lError.setText("");
				} else {
					lError.setText("The time of ingestion must be before the acquisition time.");
					pack();
				}
			}
		}
	}

}
