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
import org.petctviewer.scintigraphy.scin.Controleur_OrganeFixe;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.gui.Roi;

public class Controleur_HepaticDyn extends Controleur_OrganeFixe {

	public static String[] organes = { "R. Liver", "L. Liver", "Hilium", "CBD", "Duodenom", "Blood pool" };

	protected Controleur_HepaticDyn(HepaticDynamicScintigraphy scin, ImageSelection[] selectedImages, String studyName) {
		super(scin, new Modele_HepaticDyn(scin, selectedImages, studyName));
		this.setOrganes(organes);
		((Modele_HepaticDyn) this.model).setLocked(true);

	}

	@Override
	public boolean isOver() {
		return this.indexRoi >= organes.length - 1;
	}

	@Override
	public void end() {
		HepaticDynamicScintigraphy scin = (HepaticDynamicScintigraphy) this.getScin();

		ImagePlus imp = this.model.getImagePlus();
		BufferedImage capture = Library_Capture_CSV.captureImage(imp, 300, 300).getBufferedImage();

		ModeleScinDyn modele = (ModeleScinDyn) this.model;
		modele.setLocked(false);

		// on copie les roi sur toutes les slices
		for (int i = 1; i <= scin.getImpAnt().getStackSize(); i++) {
			scin.getImpAnt().setSlice(i);
			for (int j = 0; j < this.getOrganes().length; j++) {
				scin.getImpAnt().setRoi(getOrganRoi(this.indexRoi));
				((Modele_HepaticDyn) modele).enregistrerMesure(this.addTag(this.getNomOrgane(this.indexRoi)),
						scin.getImpAnt());
				this.indexRoi++;
			}
		}

		modele.calculerResultats();

		// TODO remove start
		List<Double> bp = modele.getData("Blood pool");
		List<Double> rliver = modele.getData("R. Liver");

		Double[] deconv = new Double[bp.size()];
		for (int i = 0; i < bp.size(); i++) {
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

		// remove finish
		new FenResultat_HepaticDyn(scin, capture, this);
		this.getScin().getFenApplication().dispose();
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 1;
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		return this.model.getRoiManager().getRoi(this.indexRoi % Controleur_HepaticDyn.organes.length);
	}

	@Override
	public boolean isPost() {
		return false;
	}

}
