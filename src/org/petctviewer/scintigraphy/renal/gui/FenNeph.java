package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Model_Renal;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabRenal;

import ij.Prefs;

public class FenNeph extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JButton btn_ok;
	private final JValueSetter jvaluesetter;
	private final Model_Renal model;
	private JValueSetter patlakChart;

	public FenNeph(ChartPanel cp, Component parentComponent, Model_Renal model) {
		super();
		this.model = model;

		// creation du panel du bas
		JButton btn_patlak = new JButton("Patlak");
		btn_patlak.addActionListener(this);
		this.btn_ok = new JButton("Ok");
		this.btn_ok.addActionListener(this);
		

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(wrapComponent(btn_ok), BorderLayout.CENTER);
		bottomPanel.add(wrapComponent(btn_patlak), BorderLayout.WEST);
		if(!(model.getKidneys()[0] && model.getKidneys()[1])) {
			btn_patlak.setEnabled(false);
			btn_patlak.setToolTipText("Only available with 2 kidneys");
		}
		this.setLayout(new BorderLayout());

		this.setTitle("Please adjust the nephrogram values");

		// creation du jvaluesetter
		this.jvaluesetter = prepareValueSetter(cp);
		this.add(jvaluesetter, BorderLayout.CENTER);

		this.add(bottomPanel, BorderLayout.SOUTH);

		this.pack();
		this.setLocationRelativeTo(parentComponent);
	}

	/// prepare la fenetre de selection des abscisses
	private JValueSetter prepareValueSetter(ChartPanel chart) {
		XYPlot plot = chart.getChart().getXYPlot();
		chart.getChart().getPlot().setBackgroundPaint(null);
		JValueSetter jvs = new JValueSetter(chart.getChart());

		// si il y a un rein gauche
		if (model.getKidneys()[0]) {
			// on cree toutes les valueSelector que l'on va utiliser
			Selector tmaxl = new Selector("TMax L", Library_JFreeChart.getAbsMaxY(plot.getDataset(), 0), 0, 
					RectangleAnchor.BOTTOM_LEFT);
			jvs.addSelector(tmaxl, "tmax L");
		}

		// si il y a un rein droit
		if (model.getKidneys()[1]) {
			//si il y a le rein, gauche, l'index du rein droit est 1
			int index = 0;
			if(model.getKidneys()[0]) {
				index = 1;
			}
			
			Selector tmaxr = new Selector("TMax R", Library_JFreeChart.getAbsMaxY(plot.getDataset(), index), 1,
					RectangleAnchor.TOP_LEFT);
			jvs.addSelector(tmaxr, "tmax R");
		}

		Selector start = new Selector(" ", 1, -1, RectangleAnchor.TOP_LEFT);
		Selector end = new Selector(" ", 3, -1, RectangleAnchor.BOTTOM_RIGHT);
		Selector lasilix = new Selector("Lasilix", Prefs.get(PrefTabRenal.PREF_LASILIX_INJECT_TIME, 20.0), -1,
				RectangleAnchor.BOTTOM_LEFT);

		jvs.addSelector(start, "start");
		jvs.addSelector(end, "end");
		jvs.addSelector(lasilix, "lasilix");
		jvs.addArea("start", "end", "integral", null);

		// renomme les series du chart pour que l'interface soit plus comprehensible
		XYSeriesCollection dataset = ((XYSeriesCollection) chart.getChart().getXYPlot().getDataset());
		if (model.getKidneys()[0])
			dataset.getSeries("Final KL").setKey("Left Kidney");

		if (model.getKidneys()[1])
			dataset.getSeries("Final KR").setKey("Right Kidney");

		return jvs;
	}

	private void clicPatlak() {
		FenPatlak fpt = new FenPatlak(model);
		
		//fpt.pack();
		fpt.setLocationRelativeTo(null);
		fpt.pack();
		fpt.setModal(true);
		fpt.setVisible(true);
		this.patlakChart = fpt.getValueSetter();
	}

	private void clickOk() {
		if(!(this.model.getKidneys()[0] && this.model.getKidneys()[1])) {
			this.dispose();
			return;
		}
		
		boolean checkOffset = checkOffset(this.jvaluesetter);
		if (!checkOffset) {
			String message = "Inconsistent differencial function during interval integration. \n Would you like to redefine the interval ?";
			int dialogResult = JOptionPane.showConfirmDialog(this, message, "WARNING", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (dialogResult != JOptionPane.YES_OPTION) {
				this.dispose();
			}
		} else {
			this.dispose();
		}
	}

	// returns true if passed
	private boolean checkOffset(JValueSetter sl) {
		XYDataset data = sl.getChart().getXYPlot().getDataset();


		@SuppressWarnings("rawtypes")
		HashMap<Comparable, Double> values = sl.getValues();

		Double debut = Math.min(values.get("start"), values.get("end"));
		Double fin = Math.max(values.get("start"), values.get("end"));

		XYDataset dataCropped = Library_JFreeChart.cropDataset(data, debut, fin);

		for (int i = 1; i < dataCropped.getItemCount(0); i++) {
			double N1 = (dataCropped.getYValue(0, i) / dataCropped.getYValue(1, i));
			double N = (dataCropped.getYValue(0, i - 1) / dataCropped.getYValue(1, i - 1));
			double ecart = Math.abs(1 - N1 / N);
			if (ecart > 0.05) {
				return false;
			}
		}

		return true;
	}

	private JPanel wrapComponent(Component c) {
		JPanel p = new JPanel();
		p.add(c);
		return p;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		JButton b = (JButton) arg0.getSource();
		if (b == this.btn_ok) {
			this.clickOk();
		} else {
			this.clicPatlak();
		}
	}

	public JValueSetter getValueSetter() {
		return this.jvaluesetter;
	}

	public JValueSetter getPatlakChart() {
		return this.patlakChart;
	}

}