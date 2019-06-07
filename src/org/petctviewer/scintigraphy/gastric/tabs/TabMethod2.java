package org.petctviewer.scintigraphy.gastric.tabs;

import ij.ImagePlus;
import ij.Prefs;
import org.petctviewer.scintigraphy.gastric.Model_Gastric;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.model.ModelWorkflow;
import org.petctviewer.scintigraphy.scin.model.Result;
import org.petctviewer.scintigraphy.scin.model.Unit;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabGastric;

import javax.swing.*;
import java.awt.*;

public class TabMethod2 extends TabResultDefault {


	public TabMethod2(FenResults parent, ImagePlus capture) {
		super(parent, capture, "Gastric Only", Unit.valueOf(Prefs.get(PrefTabGastric.PREF_UNIT_USED,
																	  Unit.COUNTS.name())), Unit.TIME, Model_Gastric.SERIES_DECAY_FUNCTION);
	}

	@Override
	protected JPanel additionalResults() {
		JPanel panel = new JPanel();
		panel.add(new JLabel("Corrected with " + ((ModelWorkflow) this.parent.getModel()).getIsotope()));
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
		final Result[] results = new Result[]{Model_Gastric.RES_TIME, Model_Gastric.RES_STOMACH_COUNTS};
		final Unit[] units = new Unit[]{Unit.MINUTES, this.unitDefault};
		JPanel panTable = new JPanel(new BorderLayout());
		JTable table = tableResults(results, units);
		panTable.add(table.getTableHeader(), BorderLayout.PAGE_START);
		panTable.add(table, BorderLayout.CENTER);
		panCenter.add(panTable, BorderLayout.CENTER);


		final Result[] resultsRequested = new Result[]{Model_Gastric.LAG_PHASE_GEOAVG, Model_Gastric.T_HALF_GEOAVG,
				Model_Gastric.RETENTION_GEOAVG};
		final Unit[] unitsRequested = new Unit[]{Unit.MINUTES, Unit.MINUTES, Unit.PERCENTAGE};
		panCenter.add(this.infoResults(resultsRequested, unitsRequested), BorderLayout.SOUTH);
		panel.add(panCenter, BorderLayout.CENTER);

		// - Custom retention
		JPanel panCustomRetention = this.createPanelCustomRetention();
		panel.add(panCustomRetention, BorderLayout.SOUTH);

		return panel;
	}
}
