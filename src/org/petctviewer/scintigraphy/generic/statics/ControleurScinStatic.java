package org.petctviewer.scintigraphy.generic.statics;

import java.awt.Button;
import java.awt.Color;
import java.awt.event.ActionEvent;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;

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
		this.getScin().getFenApplication().setText_instructions(s);
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
			return this.getScin().getFenApplication().getTextfield_instructions().getText();
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

	@Override
	public boolean saveCurrentRoi(String nomRoi, int indexRoi) {
		if (this.getSelectedRoi() != null) { // si il y a une roi sur l'image plus
			// on change la couleur pour l'overlay
			this.scin.getImp().getRoi().setStrokeColor(Color.YELLOW);
			// on enregistre la ROI dans le modele
			this.modele.enregistrerMesure(
					this.addTag(nomRoi), 
					this.scin.getImp());
	
			// On verifie que la ROI n'existe pas dans le ROI manager avant de l'ajouter
			// pour eviter les doublons
			if (this.roiManager.getRoi(indexRoi) == null) {
				//On utilise le macro car probleme d'ajout ROI identique sinon // pas toucher
				IJ.runMacro("roiManager(\"Add\");");
			} else { // Si il existe on l'ecrase
				this.roiManager.setRoi(this.scin.getImp().getRoi(), indexRoi);
				// on supprime le roi nouvellement ajoute de la vue
				this.scin.getFenApplication().getImagePlus().killRoi();
			}
	
			// precise la postion en z
			this.roiManager.
			getRoi(indexRoi).
			setPosition(this.getSliceNumberByRoiIndex(indexRoi));
	
			// changement de nom
			this.roiManager.rename(indexRoi, nomRoi);
	
			return true;
		}
		return false;
		
	}
}