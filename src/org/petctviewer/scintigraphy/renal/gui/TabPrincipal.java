package org.petctviewer.scintigraphy.renal.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.renal.RenalScintigraphy;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.ImagePlus;
import ij.plugin.ZProjector;
import ij.process.ImageProcessor;

class TabPrincipal extends TabResult {

	private BufferedImage capture;

	/**
	 * affiche les resultats de l'examen renal
	 * 
	 * @param vueScin    la vue
	 * @param capture    capture du rein projetee
	 * @param chartPanel chartpanel avec l'overlay d'ajustation
	 */
	public TabPrincipal(RenalScintigraphy vue, BufferedImage capture, FenResults parent) {
		super(parent, "Main", true);
		this.capture = capture;
	}

	@Override
	public Component getSidePanelContent() {
		boolean[] kidneys = ((Modele_Renal) this.parent.getModel()).getKidneys();

		JPanel flow_wrap = new JPanel();

		// creation du panel d'affichage des pourcentage
		Box res = Box.createVerticalBox();

		if (kidneys[0] && kidneys[1]) {
			res.add(this.getPanelSep());
		}

		res.add(Box.createVerticalStrut(10));
		res.add(this.getPanelSize());
		res.add(Box.createVerticalStrut(10));
		res.add(this.getPanelTiming());
		res.add(Box.createVerticalStrut(10));
		res.add(this.getPanelExcr());
		res.add(Box.createVerticalStrut(10));
		res.add(this.getPanelROE());
		res.add(Box.createVerticalStrut(10));
		res.add(this.getPanelNora());
		res.add(Box.createVerticalStrut(25));

		SidePanel.setFontAllJLabels(res, new Font("Calibri", Font.BOLD, 13));

		flow_wrap.add(res);

		return flow_wrap;
	}

	private Component getPanelSize() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);

		Double[] size = ((Modele_Renal)this.parent.getModel()).getSize();

		JPanel pnl_size = new JPanel(new GridLayout(2, 3, 0, 3));
		pnl_size.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		pnl_size.add(new JLabel(" Kidney Size"));
		pnl_size.add(lbl_L);
		pnl_size.add(lbl_R);

		pnl_size.add(new JLabel(""));
		JLabel lbl_heightL = new JLabel(size[0] + " cm");
		lbl_heightL.setHorizontalAlignment(JLabel.CENTER);
		pnl_size.add(lbl_heightL);

		JLabel lbl_heightR = new JLabel(size[1] + " cm");
		lbl_heightR.setHorizontalAlignment(JLabel.CENTER);
		pnl_size.add(lbl_heightR);

		return pnl_size;
	}

	private Component getPanelROE() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);

		Double xLasilix = ((Modele_Renal)this.parent.getModel()).getAdjustedValues().get("lasilix");
		// minutes a observer pour la capacite d'excretion
		Double[] mins = new Double[] { Library_Quantif.round(xLasilix - 1, 1), Library_Quantif.round(xLasilix + 2, 1),
				Library_Quantif.round(((Modele_Renal)this.parent.getModel()).getSerie("Blood Pool").getMaxX(), 1) };

		// panel roe
		JPanel pnl_roe = new JPanel(new GridLayout(4, 3, 0, 3));
		pnl_roe.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		pnl_roe.add(new JLabel(" ROE "));
		pnl_roe.add(lbl_L);
		pnl_roe.add(lbl_R);

		boolean[] kidneys = ((Modele_Renal)this.parent.getModel()).getKidneys();

		for (int i = 0; i < 3; i++) {
			// aligne a droite
			JLabel lbl_min = new JLabel(mins[i] + "  min");
			pnl_roe.add(lbl_min);

			JLabel lbl_g = null;
			if (kidneys[0]) {
				lbl_g = new JLabel(((Modele_Renal)this.parent.getModel()).getROE(mins[i], "L") + " %");
			} else {
				lbl_g = new JLabel("N/A");
			}
			lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
			pnl_roe.add(lbl_g);

			JLabel lbl_d = null;
			if (kidneys[1]) {
				lbl_d = new JLabel(((Modele_Renal)this.parent.getModel()).getROE(mins[i], "R") + " %");
			} else {
				lbl_d = new JLabel("N/A");
			}
			lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
			pnl_roe.add(lbl_d);

		}

		return pnl_roe;
	}

	private Component getPanelNora() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);

		// panel Nora
		Double[][] nora = ((Modele_Renal)this.parent.getModel()).getNora();
		JPanel pnl_nora = new JPanel(new GridLayout(4, 3, 0, 3));
		pnl_nora.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		pnl_nora.add(new JLabel(" NORA"));
		pnl_nora.add(lbl_L);
		pnl_nora.add(lbl_R);
		for (int i = 0; i < 3; i++) {
			// aligne a droite
			pnl_nora.add(new JLabel(nora[0][i] + "  min"));

			for (int j = 1; j <= 2; j++) {
				if (nora[j][i] != null) {
					JLabel lbl_g = new JLabel(nora[j][i] + " %");
					lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
					pnl_nora.add(lbl_g);
				} else {
					JLabel lbl_na = new JLabel("N/A");
					lbl_na.setHorizontalAlignment(SwingConstants.CENTER);
					pnl_nora.add(lbl_na);
				}
			}

		}
		return pnl_nora;
	}

	private Component getPanelExcr() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);

		// panel Excr
		Double[][] Excr = ((Modele_Renal)this.parent.getModel()).getExcr();
		JPanel pnl_Excr = new JPanel(new GridLayout(4, 3, 0, 3));
		pnl_Excr.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		pnl_Excr.add(new JLabel(" Excretion ratio"));
		pnl_Excr.add(lbl_L);
		pnl_Excr.add(lbl_R);
		for (int i = 0; i < 3; i++) {
			// aligne a droite
			pnl_Excr.add(new JLabel(Excr[0][i] + "  min"));

			for (int j = 1; j <= 2; j++) {
				if (Excr[j][i] != null) {
					JLabel lbl_g = new JLabel(Excr[j][i] + " %");
					lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
					pnl_Excr.add(lbl_g);
				} else {
					JLabel lbl_na = new JLabel("N/A");
					lbl_na.setHorizontalAlignment(SwingConstants.CENTER);
					pnl_Excr.add(lbl_na);
				}
			}

		}
		return pnl_Excr;
	}

	private Component getPanelTiming() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);

		// panel de timing
		Double[][] timing = ((Modele_Renal)this.parent.getModel()).getTiming();
		JPanel pnl_timing = new JPanel(new GridLayout(3, 3, 0, 3));
		pnl_timing.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		pnl_timing.add(new JLabel(" Timing "));
		pnl_timing.add(lbl_L);
		pnl_timing.add(lbl_R);

		JLabel lbl_tmax = new JLabel("TMax (min)");
		pnl_timing.add(lbl_tmax);

		JLabel lbl_gMax = new JLabel("" + timing[0][0]);
		lbl_gMax.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_timing.add(lbl_gMax);

		JLabel lbl_dMax = new JLabel("" + timing[0][1]);
		lbl_dMax.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_timing.add(lbl_dMax);

		JLabel lbl_tdemi = new JLabel("T1/2 (min)");
		pnl_timing.add(lbl_tdemi);

		String s = "" + timing[1][0];
		if (timing[1][0].equals(Double.NaN))
			s = "N/A";

		JLabel lbl_g = new JLabel(s);
		lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_timing.add(lbl_g);

		s = "" + timing[1][1];
		if (timing[1][1].equals(Double.NaN))
			s = "N/A";

		JLabel lbl_d = new JLabel(s);
		lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_timing.add(lbl_d);

		pnl_timing.add(lbl_d);

		return pnl_timing;
	}

	private Component getPanelSep() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

		// panel de fonction separee
		Double[] sep = ((Modele_Renal)this.parent.getModel()).getSeparatedFunction();
		JPanel pnl_sep = new JPanel(new GridLayout(2, 3));
		pnl_sep.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		pnl_sep.add(new JLabel("Relative function"));
		pnl_sep.add(lbl_L);
		pnl_sep.add(lbl_R);

		pnl_sep.add(new JLabel("integral"));
		JLabel lbl_d = new JLabel(sep[0] + " %");
		lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_sep.add(lbl_d);

		JLabel lbl_g = new JLabel(sep[1] + " %");
		lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_sep.add(lbl_g);

		if (((Modele_Renal)this.parent.getModel()).getPatlakPente() != null) {
			double[] patlak = ((Modele_Renal)this.parent.getModel()).getPatlakPente();
			pnl_sep.setLayout(new GridLayout(3, 3));

			pnl_sep.add(new JLabel("patlak"));
			JLabel lbl_pd = new JLabel(patlak[0] + " %");
			lbl_pd.setHorizontalAlignment(SwingConstants.CENTER);
			pnl_sep.add(lbl_pd);

			JLabel lbl_pg = new JLabel(patlak[1] + " %");
			lbl_pg.setHorizontalAlignment(SwingConstants.CENTER);
			pnl_sep.add(lbl_pg);
		}

		return pnl_sep;
	}

	@Override
	public JPanel getResultContent() {
		HashMap<Comparable, Double> adjusted = ((Modele_Renal) parent.getModel()).getAdjustedValues();
// l'intervalle est defini par l'utilisateur
		Double x1 = adjusted.get("start");
		Double x2 = adjusted.get("end");
		Double debut = Math.min(x1, x2);
		Double fin = Math.max(x1, x2);

		int slice1 = ModeleScinDyn.getSliceIndexByTime(debut * 60 * 1000, ((Modele_Renal)this.parent.getModel()).getFrameduration());
		int slice2 = ModeleScinDyn.getSliceIndexByTime(fin * 60 * 1000, ((Modele_Renal)this.parent.getModel()).getFrameduration());
		JValueSetter chartNephrogram = ((Modele_Renal)this.parent.getModel()).getNephrogramChart();
		ImagePlus proj = ZProjector.run(parent.getModel().getImagePlus(), "sum", slice1, slice2);
		proj.getProcessor().setInterpolationMethod(ImageProcessor.BICUBIC);
		JPanel grid = new JPanel(new GridLayout(2, 1));

		// creation du panel du haut
		JPanel panel_top = new JPanel(new GridLayout(1, 2));

		// ajout de la capture et du montage
		panel_top.add(new DynamicImage(capture));
		panel_top.add(new DynamicImage(proj.getImage()));

		// on ajoute les panels a la grille principale
		grid.add(panel_top);
		grid.add(chartNephrogram);

		chartNephrogram.removeChartMouseListener(chartNephrogram);

		return grid;
	}

}
