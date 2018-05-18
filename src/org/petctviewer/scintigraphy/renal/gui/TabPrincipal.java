package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.FenResultatSidePanel;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.ImagePlus;
import ij.plugin.ZProjector;
import ij.process.ImageProcessor;

public class TabPrincipal extends FenResultatSidePanel {

	private static final long serialVersionUID = 5670592335800832792L;

	private Modele_Renal modele;

	/**
	 * affiche les resultats de l'examen renal
	 * 
	 * @param vueScin
	 *            la vue
	 * @param capture
	 *            capture du rein projetee
	 * @param chartPanel
	 *            chartpanel avec l'overlay d'ajustation
	 */
	public TabPrincipal(VueScin vueScin, BufferedImage capture, ChartPanel chartPanel) {
		super("Renal scintigraphy", vueScin, capture, "");
		
		Double[] adjusted = ((Modele_Renal) vueScin.getFen_application().getControleur().getModele()).getAdjustedValues();
		double debut = Math.min(adjusted[4], adjusted[5]);
		double fin = Math.max(adjusted[4], adjusted[5]);
		
		int slice1 = ModeleScinDyn.getSliceIndexByTime(debut * 60 * 1000);
		int slice2 = ModeleScinDyn.getSliceIndexByTime(fin * 60 * 1000);
		
		ImagePlus proj = ZProjector.run(vueScin.getImp(), "sum", slice1, slice2);
		proj.getProcessor().setInterpolationMethod(ImageProcessor.BICUBIC);

		BufferedImage imgProj = proj.getBufferedImage();
		
		imgProj = resize(imgProj, capture.getWidth(), capture.getHeight());
		
		this.modele = (Modele_Renal) vueScin.getFen_application().getControleur().getModele();
		
		JPanel grid = new JPanel(new GridLayout(2, 1));

		// on affiche la capture
		JLabel lbl_capture = new JLabel();
		lbl_capture.setIcon(new ImageIcon(capture));
		lbl_capture.setHorizontalAlignment(SwingConstants.CENTER);
		
		JLabel lbl_proj = new JLabel();
		
		int width = imgProj.getWidth();
		int height = imgProj.getHeight();
		
		lbl_proj.setIcon(new ImageIcon(imgProj));
		lbl_proj.setHorizontalAlignment(SwingConstants.CENTER);
		
		// largeur et hauteur des graphiques
		int w = capture.getWidth() * 3;
		int h = capture.getHeight() * 2;

		// creation du panel du haut
		JPanel panel_top = new JPanel(new GridLayout(1, 2));

		// ajout de la capture et du montage
		panel_top.add(lbl_capture);
		panel_top.add(lbl_proj);

		// creation du panel du bas
		chartPanel.setPreferredSize(new Dimension(w, h/2));
		chartPanel.getChart().setTitle("Nephrogram");
		
		Box box = Box.createVerticalBox();
		box.add(Box.createVerticalGlue());
		box.add(panel_top);
		box.add(Box.createVerticalGlue());
		
		// on ajoute les panels a la grille principale
		grid.add(box);
		grid.add(chartPanel);

		// ajout de la grille a la fenetre
		this.add(new JPanel(), BorderLayout.WEST);
		this.add(grid, BorderLayout.CENTER);

		this.finishBuildingWindow();
		this.setVisible(false);
	}
	
	public static BufferedImage resize(BufferedImage img, int newW, int newH) { 
	    BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g = dimg.createGraphics();
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
			    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	    g.drawImage(img, 0, 0, newW, newH, 0, 0, img.getWidth(), img.getHeight(), null);
	    g.dispose();

	    return dimg;
	}  

	@Override
	public Component[] getSidePanelContent() {
		JPanel flow_wrap = new JPanel();

		// creation du panel d'affichage des pourcentage
		Box res = Box.createVerticalBox();

		res.add(this.getPanelSep());
		
		// espace entre les tableaux
		res.add(Box.createVerticalStrut(25));

		res.add(this.getPanelTiming());

		// espace entre les tableaux
		res.add(Box.createVerticalStrut(25));

		res.add(this.getPanelNoRa());

		// espace entre les tableaux
		res.add(Box.createVerticalStrut(25));

		res.add(this.getPanelROE());

		res.add(Box.createVerticalStrut(50));

		flow_wrap.add(res);

		return new Component[] { flow_wrap };
	}
	
	private Component getPanelROE() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);
		
		// minutes a observer pour la capacite d'excretion
		int[] mins = new int[] { 20, 22, 30 };

		// on recupere les series
		XYSeries serieRK = this.modele.getSerie("Output KR");
		XYSeries serieLK = this.modele.getSerie("Output KL");

		// panel roe
		JPanel pnl_roe = new JPanel(new GridLayout(4, 3, 0, 3));
		pnl_roe.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		pnl_roe.add(new JLabel(" ROE "));
		pnl_roe.add(lbl_L);
		pnl_roe.add(lbl_R);
		for (int i = 0; i < 3; i++) {
			// aligne a droite
			JLabel lbl_min = new JLabel(mins[i] + "  min");
			pnl_roe.add(lbl_min);

			JLabel lbl_g = new JLabel(modele.getPercentage(mins[i], serieLK, "L") + " %");
			lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
			pnl_roe.add(lbl_g);
			
			JLabel lbl_d = new JLabel(modele.getPercentage(mins[i], serieRK, "R") + " %");
			lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
			pnl_roe.add(lbl_d);

		}

		return pnl_roe;
	}

	private Component getPanelNoRa() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);
		
		// panel nora
		Double[][] nora = modele.getNoRA();
		JPanel pnl_nora = new JPanel(new GridLayout(4, 3, 0, 3));
		pnl_nora.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		pnl_nora.add(new JLabel(" NORA "));
		pnl_nora.add(lbl_L);
		pnl_nora.add(lbl_R);
		for (int i = 0; i < 3; i++) {
			// aligne a droite
			pnl_nora.add(new JLabel(nora[0][i] + "  min"));
			
			JLabel lbl_g = new JLabel(nora[1][i] + " %");
			lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
			pnl_nora.add(lbl_g);
			
			JLabel lbl_d = new JLabel(nora[2][i] + " %");
			lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
			pnl_nora.add(lbl_d);
		}
		return pnl_nora;
	}

	private Component getPanelTiming() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);
		
		// panel de timing
		Double[][] timing = modele.getTiming();
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
		if (timing[1][0] == -1)
			s = "N/A";
		
		JLabel lbl_g = new JLabel(s);
		lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_timing.add(lbl_g);
		
		s = "" + timing[1][1];
		if (timing[1][1] == -1)
			s = "N/A";
		
		JLabel lbl_d = new JLabel(s);
		lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_timing.add(lbl_d);
		
		pnl_timing.add(lbl_d);

		return pnl_timing;
	}

	private Component getPanelRet() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);

		// panel de retention
		Double[] ret = modele.getRetention();
		JPanel pnl_ret = new JPanel(new GridLayout(2, 3));
		pnl_ret.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		pnl_ret.add(new JLabel("Renal retention"));
		pnl_ret.add(lbl_L);
		pnl_ret.add(lbl_R);
		
		pnl_ret.add(new JLabel(""));

		JLabel lbl_g = new JLabel(ret[0] + " %");
		lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_ret.add(lbl_g);

		JLabel lbl_d = new JLabel(ret[1] + " %");
		lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_ret.add(lbl_d);

		return pnl_ret;
	}

	private Component getPanelSep() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

		// panel de fonction separee
		Double[] sep = modele.getSeparatedFunction();
		JPanel pnl_sep = new JPanel(new GridLayout(2, 3));
		pnl_sep.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		pnl_sep.add(new JLabel("Relative function"));
		pnl_sep.add(lbl_L);
		pnl_sep.add(lbl_R);
		
		pnl_sep.add(new JLabel(""));
		JLabel lbl_d = new JLabel(sep[0] + " %");
		lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_sep.add(lbl_d);

		JLabel lbl_g = new JLabel(sep[1] + " %");
		lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_sep.add(lbl_g);

		return pnl_sep;
	}

}
