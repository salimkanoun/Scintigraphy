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
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;

import ij.Prefs;

public class FenNeph extends JDialog implements ActionListener {

	private JButton btn_patlak, btn_ok;
	private JValueSetter jvaluesetter;
	private Modele_Renal modele;
	private JValueSetter patlakChart;

	public FenNeph(ChartPanel cp, Component parentComponent, Modele_Renal modele) {
		super();
		this.modele = modele;
		
		// creation du panel du bas
		this.btn_patlak = new JButton("Patlak");
		this.btn_patlak.addActionListener(this);
		this.btn_ok = new JButton("Ok");
		this.btn_ok.addActionListener(this);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(wrapComponent(btn_ok), BorderLayout.CENTER);
		bottomPanel.add(wrapComponent(btn_patlak), BorderLayout.WEST);

		this.setLayout(new BorderLayout());

		this.setTitle("Please adjust the nephrogram values");

		//creation du jvaluesetter
		this.jvaluesetter = prepareValueSetter(cp);
		this.add(jvaluesetter, BorderLayout.CENTER);
		
		this.add(bottomPanel, BorderLayout.SOUTH);

		this.pack();
		this.setLocationRelativeTo(parentComponent);
	}

	private JValueSetter prepareValueSetter(ChartPanel chart) {
		XYPlot plot = chart.getChart().getXYPlot();
		chart.getChart().getPlot().setBackgroundPaint(null);

		// on cree toutes les valueSelector que l'on va utiliser
		Selector tmaxl = new Selector("TMax L", ModeleScinDyn.getAbsMaxY(plot.getDataset(), 0), 0,
				RectangleAnchor.BOTTOM_LEFT);
		Selector tmaxr = new Selector("TMax R", ModeleScinDyn.getAbsMaxY(plot.getDataset(), 1), 1,
				RectangleAnchor.TOP_LEFT);
		Selector start = new Selector(" ", 1, -1, RectangleAnchor.TOP_LEFT);
		Selector end = new Selector(" ", 3, -1, RectangleAnchor.BOTTOM_RIGHT);
		Selector lasilix = new Selector("Lasilix", Prefs.get("renal.lasilix.preferred", 20.0), -1, RectangleAnchor.BOTTOM_LEFT);

		// ajout des selecteurs dans le listener
		JValueSetter jvs = new JValueSetter(chart.getChart());
		jvs.addSelector(tmaxl, "tmax L");
		jvs.addSelector(tmaxr, "tmax R");
		jvs.addSelector(start, "start");
		jvs.addSelector(end, "end");
		jvs.addSelector(lasilix, "lasilix");
		jvs.addArea("start", "end", "integral", null);

		// renomme les series du chart pour que l'interface soit plus comprehensible
		XYSeriesCollection dataset = ((XYSeriesCollection) chart.getChart().getXYPlot().getDataset());
		dataset.getSeries("Final KL").setKey("Left Kidney");
		dataset.getSeries("Final KR").setKey("Right Kidney");

		return jvs;
	}

	private void clicPatlak() {
		FenPatlak fpt = new FenPatlak(modele, this);
		fpt.setModal(true);
		fpt.setVisible(true);
		
		this.patlakChart = fpt.getValueSetter();
	}

	private void clickOk() {
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

		HashMap<Comparable, Double> values = sl.getValues();

		Double debut = Math.min(values.get("start"), values.get("end"));
		Double fin = Math.max(values.get("start"), values.get("end"));

		XYDataset dataCropped = Modele_Renal.cropDataset(data, debut, fin);

		for (int i = 1; i < dataCropped.getItemCount(0); i++) {
			Double N1 = (dataCropped.getYValue(0, i) / dataCropped.getYValue(1, i));
			Double N = (dataCropped.getYValue(0, i - 1) / dataCropped.getYValue(1, i - 1));
			Double ecart = Math.abs(1 - N1 / N);
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
		}else {
			this.clicPatlak();
		}
	}
	
	public JValueSetter getValueSetter(){
		return this.jvaluesetter;
	}
	
	public JValueSetter getPatlakChart() {
		return this.patlakChart;
	}

}
