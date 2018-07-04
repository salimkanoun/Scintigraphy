package org.petctviewer.scintigraphy.statics;

import java.awt.Button;
import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;

import org.petctviewer.scintigraphy.dynamic.FenApplication_GeneralDyn;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.MontageMaker;
import ij.plugin.frame.RoiManager;

public class ControleurScinStatic extends ControleurScin{

	public static int MAXROI = 100;
	private int nbOrganes = 0;
	
	protected ControleurScinStatic(Scintigraphy vue) {
		super(vue);
		
		this.setModele(new ModeleScinStatic());
		this.setOrganes(new String[MAXROI] );
	}

	@Override
	public void setInstructionsDelimit(int indexRoi) {
		String s;
		if (this.roiManager.getCount() > this.indexRoi) {
			s = this.roiManager.getRoi(this.indexRoi).getName();
		} else {
			s = "roi n" + this.indexRoi;
		}
		this.getScin().getFenApplication().getField_instructions().setText(s);
	}

	@Override
	public void notifyClic(ActionEvent arg0) {
		Button b = (Button) arg0.getSource();
		
		FenApplication_ScinStatic fen = (FenApplication_ScinStatic) this.getScin().getFenApplication();

		if (b == fen.getBtn_finish()) {
			this.clicSuivant();
			this.fin();
		}
	}
	
	@Override
	public String getNomOrgane(int index) {
		if (!isOver()) {
			return this.getScin().getFenApplication().getField_instructions().getText();
		}
		return this.roiManager.getRoi(index % this.nbOrganes).getName();
	}
	
	@Override
	public boolean isOver() {
		return false;
	}

	@Override
	public void fin() {
		ImagePlus imp = this.getScin().getImp();
		
		//pour la ant
		imp.setSlice(1);
		
		for(int i =0; i< this.roiManager.getCount(); i++) {
			Roi roi = this.roiManager.getRoi(i);
			imp.setRoi(roi);
			((ModeleScinStatic)this.getModele()).enregistrerMesureAnt(this.addTag(roi.getName()), imp);
		}
		
		//pour la post
		imp.setSlice(2);
		
		for(int i =0; i< this.roiManager.getCount(); i++) {
			Roi roi = this.roiManager.getRoi(i);
			imp.setRoi(roi);
			((ModeleScinStatic)this.getModele()).enregistrerMesurePost(this.addTag(roi.getName()), imp);
		}
		
		
		Thread t = new DoubleImageThread("test",this.getScin(),(ModeleScinStatic)this.getModele());
		t.start();
		
System.out.println(SwingUtilities.isEventDispatchThread()	);
		
	

	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 0;
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		return null;
	}

	@Override
	public boolean isPost() {
		return this.getScin().getImp().getCurrentSlice() == 2;
	}

}
