package org.petctviewer.scintigraphy.hepatic.dyn;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.hepatic.dyn.gui.FenResultat_HepaticDyn;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;

import ij.ImagePlus;
import ij.gui.Roi;

public class Controleur_HepaticDyn extends ControleurScin {

	public static String[] organes = { "R. Liver", "L. Liver", "Hilium", "CBD", "Duodenom", "Blood pool" };

	protected Controleur_HepaticDyn(HepaticDynamicScintigraphy vue) {
		super(vue);
		this.setOrganes(organes);
		Modele_HepaticDyn modele = new Modele_HepaticDyn(vue);
		modele.setLocked(true);
		this.setModele(modele);
		
	}

	@Override
	public boolean isOver() {
		return this.indexRoi >= organes.length - 1;
	}

	@Override
	public void fin() {
		HepaticDynamicScintigraphy vue = (HepaticDynamicScintigraphy) this.getScin();
		
		ImagePlus imp = vue.getImp();
		BufferedImage capture = ModeleScin.captureImage(imp, 300, 300).getBufferedImage();
	
		ModeleScinDyn modele = (ModeleScinDyn) this.getModele();
		modele.setLocked(false);
		
		//on copie les roi sur toutes les slices
		for (int i = 1; i <= vue.getImpAnt().getStackSize(); i++) {
			vue.getImpAnt().setSlice(i);
			for (int j = 0; j < this.getOrganes().length; j++) {
				vue.getImpAnt().setRoi(getOrganRoi(this.indexRoi));
				modele.enregistrerMesure(this.addTag(this.getNomOrgane(this.indexRoi)), vue.getImpAnt());
				this.indexRoi++;
			}
		}
		
		modele.calculerResultats();
		
		//TODO remove start
		List<Double> bp = modele.getData("Blood pool");
		List<Double> rliver = modele.getData("R. Liver");
		
		Double[] deconv = new Double[bp.size()];
		for(int i = 0; i < bp.size(); i++) {
			deconv[i] = rliver.get(i) / bp.get(i);
		}
		
		XYSeriesCollection data = new XYSeriesCollection();
		data.addSeries(modele.createSerie(Arrays.asList(deconv), "deconv"));
		data.addSeries(modele.getSerie("Blood pool"));
		data.addSeries(modele.getSerie("R. Liver"));
		
		JFreeChart chart = ChartFactory.createXYLineChart("", "x", "y", data);
		
		ChartPanel chartpanel = new ChartPanel(chart);
		JFrame frame = new JFrame();
		frame.add(chartpanel);
		frame.pack();
		frame.setVisible(true);
		
		//remove finish
		
		new FenResultat_HepaticDyn(vue, capture);
		this.getScin().getFenApplication().dispose();
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 1;
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		return this.roiManager.getRoi(this.indexRoi % this.organes.length);
	}

	@Override
	public boolean isPost() {
		return false;
	}

}
