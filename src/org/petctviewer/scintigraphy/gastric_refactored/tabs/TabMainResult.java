package org.petctviewer.scintigraphy.gastric_refactored.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import org.petctviewer.scintigraphy.gastric_refactored.Model_Gastric;
import org.petctviewer.scintigraphy.gastric_refactored.Model_Gastric.Result;
import org.petctviewer.scintigraphy.gastric_refactored.Model_Gastric.ResultValue;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit.FitType;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit.NoFit;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.ImageStack;

public class TabMainResult extends TabResult {

	private ImagePlus capture;

	private Fit currentExtrapolation;

	public TabMainResult(FenResults parent, ImagePlus capture) {
		super(parent, "Result", true);
		this.capture = capture;
		this.reloadDisplay();
	}

	private JTable tablesResultats() {
		Model_Gastric model = (Model_Gastric) this.parent.getModel();
		JTable table = new JTable(0, 4);
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		String[] arr = new String[tableModel.getColumnCount()];
		tableModel.addRow(new String[] { "Time (min)", "Stomach (%)", "Fundus (%)", "Antrum (%)" });
		for (int i = 0; i < ((Model_Gastric) this.parent.getModel()).nbAcquisitions(); i++) {
			for (int j = 0; j < tableModel.getColumnCount(); j++) {
				arr[j] = model.getImageResult(j, i) + "";
			}
			tableModel.addRow(arr);
		}
		table.setRowHeight(30);
		MatteBorder border = new MatteBorder(1, 1, 1, 1, Color.BLACK);
		table.setBorder(border);
		return table;
	}
	
	private boolean displayResult(JPanel infoRes, ResultValue result) {
		Model_Gastric model = (Model_Gastric) this.parent.getModel();
		ResultValue result = model.getResult(res);
		boolean hasExtrapolatedValue = false;
		if (result.extrapolation != null)
			hasExtrapolatedValue = true;
		infoRes.add(new JLabel(res.getName() + ":"));
		JLabel lRes = new JLabel(result + " " + res.getUnit());
		if(result.extrapolation == FitType.NONE)
			lRes.setForeground(Color.RED);
		infoRes.add(lRes);
		return hasExtrapolatedValue;
	}

	private JPanel infoResultats() {
		Model_Gastric model = (Model_Gastric) this.parent.getModel();
		this.currentExtrapolation = model.getExtrapolation();

		JPanel panel = new JPanel(new BorderLayout());

		boolean hasExtrapolatedValue = false;

		JPanel infoRes = new JPanel();
		infoRes.setLayout(new GridLayout(0, 2));

		ResultValue result = model.getResult(Result.START_ANTRUM);
		infoRes.add(new JLabel("Start Antrum:"));
		infoRes.add(new JLabel(result + " min"));

		result = model.getResult(Result.START_INTESTINE);
		infoRes.add(new JLabel("Start Intestine:"));
		infoRes.add(new JLabel(result + " min"));

		this.displayResult(infoRes, Result.LAG_PHASE);

		result = model.getResult(Result.T_HALF);
		if (result.extrapolation != null)
			hasExtrapolatedValue = true;
		infoRes.add(new JLabel("T1/2:"));
		lRes = new JLabel(result + " %");
		if(result.extrapolation == FitType.NONE)
			lRes.setForeground(Color.RED);
		infoRes.add(lRes);

		result = model.retentionAt(60.);
		if (result.extrapolation != null)
			hasExtrapolatedValue = true;
		infoRes.add(new JLabel("Retention at 1h:"));
		lRes = new JLabel(result + " %");
		if(result.extrapolation == FitType.NONE)
			lRes.setForeground(Color.RED);
		infoRes.add(lRes);

		result = model.retentionAt(120.);
		if (result.extrapolation != null)
			hasExtrapolatedValue = true;
		infoRes.add(new JLabel("Retention at 2h:"));
		lRes = new JLabel(result + " %");
		if(result.extrapolation == FitType.NONE)
			lRes.setForeground(Color.RED);
		infoRes.add(lRes);

		result = model.retentionAt(180.);
		if (result.extrapolation != null)
			hasExtrapolatedValue = true;
		infoRes.add(new JLabel("Retention at 3h:"));
		lRes = new JLabel(result + " %");
		if(result.extrapolation == FitType.NONE)
			lRes.setForeground(Color.RED);
		infoRes.add(lRes);

		result = model.retentionAt(240.);
		if (result.extrapolation != null)
			hasExtrapolatedValue = true;
		infoRes.add(new JLabel("Retention at 4h:"));
		lRes = new JLabel(result + " %");
		if(result.extrapolation == FitType.NONE)
			lRes.setForeground(Color.RED);
		infoRes.add(lRes);

		panel.add(infoRes, BorderLayout.CENTER);
		if (hasExtrapolatedValue) {
			JLabel l = null;
			if (((Model_Gastric) this.parent.getModel()).getExtrapolation() instanceof NoFit) {
				l = new JLabel("(*) No fit has been selected to extrapolate the values!");
				l.setForeground(Color.RED);
			} else {
				l = new JLabel("(*) The results are calculated with a " + model.getExtrapolation().toString()
						+ " extrapolation");
			}
			panel.add(l, BorderLayout.SOUTH);
		}
		return panel;
	}

	@Override
	public Component getSidePanelContent() {
		String[] results = ((Model_Gastric) this.parent.getModel()).resultats();
		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.add(this.tablesResultats());
		panel.add(this.infoResultats());
		return panel;
	}

	@Override
	public JPanel getResultContent() {
		ImageStack ims = Library_Capture_CSV
				.captureToStack(new ImagePlus[] { capture, ((Model_Gastric) this.parent.getModel()).createGraph_3(),
						((Model_Gastric) this.parent.getModel()).createGraph_1(),
						((Model_Gastric) this.parent.getModel()).createGraph_2() });

		return new DynamicImage(((Model_Gastric) this.parent.getModel()).montage(ims).getImage());
	}

}
