package org.petctviewer.scintigraphy.renal;

import java.awt.Color;

import org.jfree.chart.ChartPanel;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationDyn;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.Toolbar;
import ij.plugin.ZProjector;

public class RenalScintigraphy extends DynamicScintigraphy {

	JValueSetter nephrogramChart, patlakChart;

	public RenalScintigraphy() {
		super("Renal scintigraphy");
	}

	@Override
	protected ImagePlus preparerImp(ImagePlus[] images) {
		super.preparerImp(images);

		// on inverse l'image pour garder l'orientation gauche / droite
		for (int i = 1; i <= this.impPost.getStackSize(); i++) {
			this.impPost.getStack().getProcessor(i).flipHorizontal();
		}

		ImagePlus impProjetee = projeter(this.impPost);
		ImageStack stack = impProjetee.getStack();
		
		//deux premieres minutes
		int fin = ModeleScinDyn.getSliceIndexByTime(2 * 60 * 1000, this.getFrameDurations());
		ImagePlus impPostFirstMin = projeter(this.impPost, 0, fin);
		stack.addSlice(impPostFirstMin.getProcessor());
		// MIP
		ImagePlus pj = ZProjector.run(this.impPost, "max", 0, this.impPost.getNSlices());
		stack.addSlice(pj.getProcessor());

		// ajout de la prise ant si elle existe
		if (this.impAnt != null) {
			for (int i = 1; i <= this.impAnt.getStackSize(); i++) {
				this.impAnt.getStack().getProcessor(i).flipHorizontal();
			}
			ImagePlus impProjAnt = projeter(impAnt);
			stack.addSlice(impProjAnt.getProcessor());
		}

		//ajout du stack a l'imp
		impProjetee.setStack(stack);
		return impProjetee.duplicate();
	}
	

	@Override
	public void lancerProgramme() {
		Overlay overlay = Scintigraphy.initOverlay(impProjetee, 12);
		Scintigraphy.setOverlayGD(overlay, impProjetee, Color.yellow);
		Scintigraphy.setOverlayTitle("Post",overlay, impProjetee, Color.yellow, 1);
		Scintigraphy.setOverlayTitle("2 first min posterior", overlay, impProjetee, Color.YELLOW, 2);
		Scintigraphy.setOverlayTitle("MIP", overlay, impProjetee, Color.YELLOW, 3);
		if (this.impAnt != null) {
			Scintigraphy.setOverlayTitle("Ant", overlay, impProjetee, Color.yellow, 4);
		}

		this.setFenApplication(new FenApplicationDyn(this.getImp(), this.getExamType(), this));
		this.getImp().setOverlay(overlay);
		this.getFenApplication().setControleur(new Controleur_Renal(this));
	}

	public JValueSetter getNephrogramChart() {
		return nephrogramChart;
	}

	public void setNephrogramChart(JValueSetter nephrogramChart) {
		this.nephrogramChart = nephrogramChart;
	}

	public JValueSetter getPatlakChart() {
		return patlakChart;
	}

	public void setPatlakChart(JValueSetter patlakChart) {
		this.patlakChart = patlakChart;
	}
}
