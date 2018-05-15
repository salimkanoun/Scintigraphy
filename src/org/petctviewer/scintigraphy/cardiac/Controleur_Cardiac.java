package org.petctviewer.scintigraphy.cardiac;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.IJ;
import ij.gui.Roi;
import ij.plugin.RoiScaler;

public class Controleur_Cardiac extends ControleurScin {

	private boolean finContSlice1;
	private boolean finContSlice2;
	private static String[] organes = { "Bladder", "Kidney R", "Kidney L", "Heart", "Bkg noise" };

	protected Controleur_Cardiac(VueScin vue) {
		super(vue);

		Modele_Cardiac mdl = new Modele_Cardiac(this.getVue().getImp());

		//on declare si il y a deux prises
		mdl.setDeuxPrise(this.isDeuxPrises());

		mdl.calculerMoyGeomTotale();
		this.setModele(mdl);

		// double les organes pour prise ant/post
		List<String> organesAntPost = new ArrayList<>();
		for (String s : organes) {
			organesAntPost.add(s);
			organesAntPost.add(s);
		}
		this.setOrganes(organesAntPost.toArray(new String[0]));

		//on lance le mode decontamination
		((FenApplication_Cardiac) this.getVue().getFen_application()).startContaminationMode();
	}

	@Override
	public boolean isOver() {
		return this.getIndexRoi() + 1 >= this.getOrganes().length;
	}

	@Override
	public void fin() {
		//suppression du controleur de l'imp
		this.removeImpListener();
		
		ModeleScin mdl = this.getModele();
		mdl.calculerResultats();

		Vue_Cardiac vue = (Vue_Cardiac) this.getVue();
		BufferedImage capture = ModeleScin.captureImage(vue.getImp(), 410, 820).getBufferedImage();
		new FenResultat_Cardiac(vue, capture);

	}
	
	@Override
	public String getSameNameRoiCount(String nomRoi) {
		//on renvoie le nombre de roi identiques uniquement si toutes les contaminations ont ete prises
		if(!this.finContSlice1 || !this.finContSlice2) {
			return super.getSameNameRoiCount(nomRoi);
		}
		return "";
	}

	@Override
	public boolean isPost() {
		return (this.getIndexRoi() % 2 == 1);
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		// changement de slice si la prise contient une precoce
		if (this.isDeuxPrises()) {
			if (this.finContSlice1) {
				return 2;
			}
		}
		return 1;
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		// symetrique du coeur
		if (this.getIndexRoi() == this.getOrganes().length - 2) {
			Roi roi = (Roi) this.roiManager.getRoi(this.getIndexRoi() - 2).clone();

			//on fait le symetrique de la roi
			roi = RoiScaler.scale(roi, -1, 1, true);

			int quart = (this.getVue().getImp().getWidth() / 4);
			int newX = (int) (roi.getXBase() - Math.abs(2 * (roi.getXBase() - quart)) - roi.getFloatWidth());
			roi.setLocation(newX, roi.getYBase());
			return roi;
		}

		if (this.isPost() && lastRoi < this.indexRoi) { // si la prise est post et que , on decale l'organe precedent
			Roi roi = (Roi) this.roiManager.getRoi(getIndexRoi() - 1).clone();
			// on d�cale d'une demi largeur
			roi.setLocation(roi.getXBase() + (this.getVue().getImp().getWidth() / 2), roi.getYBase());
			return roi;
		}
		
		return null;
	}

	private boolean isDeuxPrises() {
		return this.getVue().getImp().getImageStackSize() > 1;
	}

	private void clicNewCont() {
		// sauvegarde du roi courant
		String nom = "ContE";
		if (this.finContSlice1) {
			nom = "ContL";
		}
		
		boolean saved = this.saveCurrentRoi(nom, this.indexRoi);

		if (saved) {
			this.indexRoi++;
			this.preparerRoi(this.indexRoi-1);

			// on affiche les instructions
			if (!this.isPost()) {
				this.getVue().getFen_application().setInstructions("Delimit a new contamination");
				FenApplication_Cardiac fac = (FenApplication_Cardiac) this.getVue().getFen_application();
				fac.getBtn_continue().setEnabled(true);
				fac.getBtn_newCont().setLabel("Next");
			} else {
				this.getVue().getFen_application().setInstructions("Adjust contamination zone");
				FenApplication_Cardiac fac = (FenApplication_Cardiac) this.getVue().getFen_application();
				fac.getBtn_continue().setEnabled(false);
				fac.getBtn_newCont().setLabel("Save");
			}

		}
	}

	private void clicEndCont() {
		if (!this.isPost()) {			
			// on set la slice
			if ((this.getVue().getImp().getCurrentSlice() == 1 && this.isDeuxPrises())) {
				//on relance le mode decontamination, cette fois ci pour la deuxieme slice
				this.finContSlice1 = true;
				this.setSlice(2);				
				((FenApplication_Cardiac) this.getVue().getFen_application()).startContaminationMode();
				
			} else { // on a trait� toutes les contaminations
				this.finContSlice2 = true;
				((FenApplication_Cardiac) this.getVue().getFen_application()).stopContaminationMode();
				String[] conts = new String[this.indexRoi];
				for (int i = 0; i < conts.length; i++) {
					conts[i] = "Cont";
				}
				// on ajoute de nouvelles cases dans le tableau organes pour ne pas modifier
				// l'indexRoi
				this.setOrganes((String[]) ArrayUtils.addAll(conts, this.getOrganes()));
			}
		} else {
			IJ.log("Please delimit the Post contamination zone");
		}
	}

	@Override
	public void notifyClic(ActionEvent arg0) {
		//permet d'appeller les methodes correspondant au clic des deux nouveaux boutons
		Button b = (Button) arg0.getSource();
		if (b == ((FenApplication_Cardiac) this.getVue().getFen_application()).getBtn_newCont()) {
			this.clicNewCont();
		} else if (b == ((FenApplication_Cardiac) this.getVue().getFen_application()).getBtn_continue()) {
			this.clicEndCont();
		}
	}

}
