package org.petctviewer.scintigraphy.renal.gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Model_Renal;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FenPatlak extends JDialog implements ActionListener, ChartMouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JButton btn_ok;
	private final JLabel lbl_eqR;
	private final JLabel lbl_eqL;
	private int lastIndex;
	@SuppressWarnings("rawtypes")
	private final JComboBox combo;
	
	private final JValueSetter valueSetter;
	private final Model_Renal modele;
	private double debutG, debutD, finG, finD;
	private Selector debut, fin;
	private XYSeries lkPatlak, rkPatlak;
	private double[] regG, regD;
	
	public FenPatlak(Model_Renal modele) {
		this.modele = modele;
		JFreeChart patlakChart = this.createPatlakChart(modele);
		XYPlot plot = patlakChart.getXYPlot();
		plot.setBackgroundPaint(null);

		// initialisation des labels
		this.lbl_eqL = new JLabel();
		this.lbl_eqR = new JLabel();

		// on cree le selectorHandler
		this.valueSetter = createValueSetter(patlakChart);
		valueSetter.addChartMouseListener(this);
		
		// bouton de validation
		btn_ok = new JButton("OK");
		JPanel wrap_ok = new JPanel();
		wrap_ok.add(btn_ok);
		btn_ok.addActionListener(this);

		// panel de resulats
		Box pnl_equations = Box.createVerticalBox();

		// combo de selection
		this.combo = new JComboBox<>(new String[] { "Left kidney", "Right kidney" });
		this.lastIndex = 0;
		combo.setSelectedIndex(lastIndex);
		combo.addActionListener(this);

		// bord droit de la fenetre
		Box rightBox = Box.createVerticalBox();
		rightBox.add(pnl_equations);
		pnl_equations.add(new JLabel("Patlak fit :"));
		pnl_equations.add(lbl_eqL);
		pnl_equations.add(lbl_eqR);

		rightBox.add(Box.createVerticalStrut(100));

		JPanel wrap_eq = new JPanel();
		wrap_eq.add(pnl_equations);
		rightBox.add(wrap_eq);

		rightBox.add(Box.createVerticalGlue());

		JPanel wrap_combo = new JPanel();
		wrap_combo.add(combo);
		rightBox.add(wrap_combo);

		rightBox.add(Box.createVerticalGlue());

		this.fitPatlak();

		// construction de la fenetre
		this.setTitle("Patlak");
		this.setLayout(new BorderLayout());
		
		//ajoute le graphique au centre de l'ecran
		
		this.add(wrap_ok, BorderLayout.SOUTH);
		this.add(rightBox, BorderLayout.EAST);
		this.add(this.valueSetter, BorderLayout.CENTER);
		pack();
		
	}

	private JValueSetter createValueSetter(JFreeChart patlak) {
		JValueSetter sl = new JValueSetter(patlak);

		this.debutG = 1;
		this.debutD = 1;
		this.finG = 3;
		this.finD = 3;

		this.debut = new Selector(" ", debutD, -1, RectangleAnchor.TOP_LEFT);
		this.fin = new Selector(" ", finD, -1, RectangleAnchor.TOP_LEFT);
		sl.addSelector(debut, "start");
		sl.addSelector(fin, "end");

		sl.addArea("start", "end", "area", null);

		return sl;
	}

	private JFreeChart createPatlakChart(Model_Renal modele) {
		XYSeries bpl = modele.getSerie("BP norm L");
		XYSeries bpr = modele.getSerie("BP norm R");
		XYSeries lk = modele.getSerie("Final KL");
		XYSeries rk = modele.getSerie("Final KR");
		XYSeries bpi = modele.getSerie("BPI");
		XYSeries bp = modele.getSerie("Blood Pool");

		XYSeriesCollection data = new XYSeriesCollection();
		XYSeries lkPatlak = new XYSeries("Left Kidney");
		XYSeries rkPatlak = new XYSeries("Right Kidney");

		double minutesMax = 4.0;

		for (double t = 0.0; t < minutesMax; t += 2 / 60.0) {
			Double x = Library_JFreeChart.getY(bpi, t) / Library_JFreeChart.getY(bp, t);
			Double y1 = Library_JFreeChart.getY(lk, t) / Library_JFreeChart.getY(bpl, t);
			lkPatlak.add(x, y1);

			Double y2 = Library_JFreeChart.getY(rk, t) / Library_JFreeChart.getY(bpr, t);
			rkPatlak.add(x, y2);
		}
		data.addSeries(lkPatlak);
		data.addSeries(rkPatlak);

		this.lkPatlak = lkPatlak;
		this.rkPatlak = rkPatlak;

		return ChartFactory.createXYLineChart("Patlak", "x", "y", data, PlotOrientation.VERTICAL, true, true,
				true);
	}

	private void fitPatlak() {
		// on met a jour les bornes
		if (this.combo.getSelectedIndex() == 0) {
			debutG = Math.min(this.fin.getXValue(), this.debut.getXValue());
			finG = Math.max(this.fin.getXValue(), this.debut.getXValue());
		} else {
			debutD = Math.min(this.fin.getXValue(), this.debut.getXValue());
			finD = Math.max(this.fin.getXValue(), this.debut.getXValue());
		}

		// on coupe les series dans l'intervalle
		XYSeries lkCropped = Library_JFreeChart.cropSeries(this.lkPatlak, this.debutG, this.finG);
		XYSeries rkCropped = Library_JFreeChart.cropSeries(this.rkPatlak, this.debutD, this.finD);
		XYSeriesCollection data = new XYSeriesCollection();
		data.addSeries(lkCropped);
		data.addSeries(rkCropped);

		// calcul de la regression du rein gauche si il y a assez de points 
		if (lkCropped.getItemCount() >= 2) {
			this.regG = Regression.getOLSRegression(data, 0);
		}

		// calcul de la regression du rein droit si il y a assez de points 
		if (rkCropped.getItemCount() >= 2) {
			this.regD = Regression.getOLSRegression(data, 1);
		}

		// mise a jour des equations de droite
		this.lbl_eqL.setText("L. Kidney : " + Library_Quantif.round(regG[1], 2) + "x + " + Library_Quantif.round(regG[0], 2));
		this.lbl_eqR.setText("R. Kidney : " + Library_Quantif.round(regD[1], 2) + "x + " + Library_Quantif.round(regD[0], 2));

		// on trace les droites sur le graphique
		XYPlot plot = this.valueSetter.getChart().getXYPlot();
		plot.clearAnnotations();
		Stroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f,new float[] { 6.0f, 6.0f }, 0.0f);
		
		XYLineAnnotation rgFit = new XYLineAnnotation(0, regG[0], 100, 100 * regG[1] + regG[0], stroke, Color.red);
		plot.addAnnotation(rgFit);

		XYLineAnnotation rdFit = new XYLineAnnotation(0, regD[0], 100, 100 * regD[1] + regD[0], stroke, Color.blue);
		plot.addAnnotation(rdFit);
	}

	/*
	 * Methode pour les listener des selector
	 */
	@SuppressWarnings("rawtypes")
	private void comboUpdated(ActionEvent e) {
		JComboBox cb = (JComboBox) e.getSource();
		int indexCombo = cb.getSelectedIndex();

		if (indexCombo == this.lastIndex) {
			return;
		}
		this.lastIndex = indexCombo;

		if (indexCombo == 1) {
			debutG = Math.min(this.fin.getXValue(), this.debut.getXValue());
			finG = Math.max(this.fin.getXValue(), this.debut.getXValue());
			this.debut.setXValue(debutD);
			this.fin.setXValue(finD);
		} else {
			debutD = Math.min(this.fin.getXValue(), this.debut.getXValue());
			finD = Math.max(this.fin.getXValue(), this.debut.getXValue());
			this.debut.setXValue(debutG);
			this.fin.setXValue(finG);
		}

		this.valueSetter.updateAreas();
	}

	public JValueSetter getValueSetter() {
		this.valueSetter.removeSelector("start");
		this.valueSetter.updateAreas();
		return this.valueSetter;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == this.btn_ok) {
			double[] patlakRatio = new double[2];

			patlakRatio[0] = Library_Quantif.round(100 * regG[1] / (this.regG[1] + this.regD[1]), 1);
			patlakRatio[1] = Library_Quantif.round(100 * regD[1] / (this.regG[1] + this.regD[1]), 1);

			this.modele.setPatlakPente(patlakRatio);
			this.getValueSetter().removeChartMouseListener(this);
			this.dispose();
		} else {
			this.comboUpdated(e);
		}
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		// Auto-generated method stub
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		FenPatlak.this.fitPatlak();
	}

}
