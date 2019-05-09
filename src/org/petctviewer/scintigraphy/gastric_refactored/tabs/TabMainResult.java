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
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit.FitType;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit.NoFit;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Debug;

import ij.ImagePlus;
import ij.ImageStack;

public class TabMainResult extends TabResult {

	private ImagePlus capture;

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
				ResultValue res = model.getImageResult(Result.imageResults()[j], i);
				arr[j] = res.value();
			}
			tableModel.addRow(arr);
		}
		table.setRowHeight(30);
		MatteBorder border = new MatteBorder(1, 1, 1, 1, Color.BLACK);
		table.setBorder(border);
		return table;
	}

	/**
	 * Displays the result.
	 * 
	 * @param infoRes Panel on which to place the result in
	 * @param result  Result of the model to display
	 * @return
	 */
	private void displayResult(JPanel infoRes, ResultValue result) {
		Library_Debug.checkNull("infoRes", infoRes);
		Library_Debug.checkNull("result", result);
		Library_Debug.checkNull("result.type", result.type);
		infoRes.add(new JLabel(result.type.getName() + ":"));
		JLabel lRes = new JLabel(result.value() + " " + result.type.getUnit());
		if (result.extrapolation == FitType.NONE)
			lRes.setForeground(Color.RED);
		infoRes.add(lRes);
	}

	// TODO: maybe combine this method with displayResult()
	/**
	 * Displays the result for the retention at a certain time.
	 * 
	 * @param infoRes Panel on which to place the result in
	 * @param time    Time of the retention in minutes
	 * @param result  Result of the model to display
	 * @throws IllegalArgumentException if the result is not a RETENTION type
	 */
	private void displayRetentionResult(JPanel infoRes, double time, ResultValue result)
			throws IllegalArgumentException {
		if (result.type != Result.RETENTION)
			throw new IllegalArgumentException("Result type must be " + Result.RETENTION);

		infoRes.add(new JLabel(result.type.getName() + " at " + (int) (time / 60) + "h:"));
		// The value cannot be negative, so we restrain it with Math.max
		JLabel lRes = new JLabel(result.value() + " " + result.type.getUnit());
		if (result.extrapolation == FitType.NONE)
			lRes.setForeground(Color.RED);
		infoRes.add(lRes);
	}

	private JPanel infoResultats() {
		Model_Gastric model = (Model_Gastric) this.parent.getModel();

		JPanel panel = new JPanel(new BorderLayout());

		boolean hasExtrapolatedValue = false;

		JPanel infoRes = new JPanel();
		infoRes.setLayout(new GridLayout(0, 2));

		ResultValue result = model.getResult(Result.START_ANTRUM);
		hasExtrapolatedValue = result.extrapolation != null;
		this.displayResult(infoRes, result);

		result = model.getResult(Result.START_INTESTINE);
		hasExtrapolatedValue = result.extrapolation != null;
		this.displayResult(infoRes, result);

		result = model.getResult(Result.LAG_PHASE);
		hasExtrapolatedValue = result.extrapolation != null;
		this.displayResult(infoRes, result);

		result = model.getResult(Result.T_HALF);
		hasExtrapolatedValue = result.extrapolation != null;
		this.displayResult(infoRes, result);

		for (double time = 60.; time <= 240.; time += 60.) {
			result = model.retentionAt(time);
			hasExtrapolatedValue = result.extrapolation != null;
			this.displayRetentionResult(infoRes, time, result);
		}

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
