package org.petctviewer.scintigraphy.gastric.tabs;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.gastric.ControllerWorkflow_Gastric;
import org.petctviewer.scintigraphy.gastric.Model_Gastric;
import org.petctviewer.scintigraphy.gastric.Result;
import org.petctviewer.scintigraphy.gastric.Unit;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

public class TabMethod1 extends TabResultDefault {

	private JButton btnDynAcquisition;

	public TabMethod1(FenResults parent, ImagePlus capture) {
		super(parent, capture, "Intragastric Distribution", Unit.PERCENTAGE, Unit.TIME,
				Model_Gastric.SERIES_STOMACH_PERCENTAGE);
	}

	public void deactivateDynamicAcquisition() {
		if (this.btnDynAcquisition != null) this.btnDynAcquisition.setEnabled(false);
	}

	@Override
	protected JPanel additionalResults() {
		JPanel panel = new JPanel(new GridLayout(0, 1));

		// Time of ingestion
		if (this.timeIngestion != null) {
			panel.add(new JLabel("Ingestion Time: " + new SimpleDateFormat("HH:mm:ss").format(timeIngestion)));
		}

		// Btn dynamic acquisition
		btnDynAcquisition = new JButton("Start dynamic acquisition");
		btnDynAcquisition
				.addActionListener((event) -> ((ControllerWorkflow_Gastric) parent.getController()).startDynamic());
		panel.add(btnDynAcquisition);

		return panel;
	}

	@Override
	public Component getSidePanelContent() {
		JPanel panel = new JPanel(new BorderLayout());

		// North
		panel.add(this.additionalResults(), BorderLayout.NORTH);

		// Center
		JPanel panCenter = new JPanel(new BorderLayout());

		// - Table
		final Result[] results =
				new Result[]{Model_Gastric.RES_TIME, Model_Gastric.RES_STOMACH, Model_Gastric.RES_FUNDUS,
						Model_Gastric.RES_ANTRUM};
		final Unit[] units = new Unit[]{Unit.MINUTES, this.unitDefault, this.unitDefault, this.unitDefault};
		JPanel panTable = new JPanel(new BorderLayout());
		JTable table = tablesResultats(results, units);
		panTable.add(table.getTableHeader(), BorderLayout.PAGE_START);
		panTable.add(table, BorderLayout.CENTER);
		panCenter.add(panTable, BorderLayout.CENTER);


		// Display start antrum and intestine only if dynamic has been made
		final Result[] resultsRequested;
		if (((ControllerWorkflow_Gastric) parent.getController()).isDynamicStarted()) {
			resultsRequested = new Result[]{Model_Gastric.START_ANTRUM, Model_Gastric.START_INTESTINE,
					Model_Gastric.LAG_PHASE_PERCENTAGE, Model_Gastric.T_HALF_PERCENTAGE,
					Model_Gastric.RETENTION_PERCENTAGE};
		} else {
			resultsRequested = new Result[]{Model_Gastric.LAG_PHASE_PERCENTAGE, Model_Gastric.T_HALF_PERCENTAGE,
					Model_Gastric.RETENTION_PERCENTAGE};
		}
		final Unit[] unitsRequested =
				new Unit[]{this.unitDefault, this.unitDefault, this.unitTime, this.unitTime, Unit.PERCENTAGE};
		panCenter.add(this.infoResultats(resultsRequested, unitsRequested), BorderLayout.SOUTH);
		panel.add(panCenter, BorderLayout.CENTER);

		// - Custom retention
		JPanel panCustomRetention = this.createPanelCustomRetention();
		panel.add(panCustomRetention, BorderLayout.SOUTH);

		return panel;
	}
}
