package org.petctviewer.scintigraphy.cardiac;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.RoiScaler;
import ij.util.DicomTools;

public class Controleur_Cardiac extends ControleurScin {

	private static String[] organes = { "Bladder", "Kidney R", "Kidney L", "Heart", "Bkg noise" };

	private int[] contSlice;

	private boolean modeCont = false;

	protected Controleur_Cardiac(VueScin vue) {
		super(vue);

		Modele_Cardiac mdl = new Modele_Cardiac(this.getVue().getImp());
		this.contSlice = new int[2];

		mdl.setDeuxPrise(this.isDeuxPrises());

		mdl.calculerMoyGeomTotale(this.getVue().getImp());
		this.setModele(mdl);

		// double les organes pour prise ant/post
		List<String> organesAntPost = new ArrayList<String>();
		for (String s : organes) {
			organesAntPost.add(s);
			organesAntPost.add(s);
		}
		this.setOrganes(organesAntPost.toArray(new String[0]));

		traiterContamination();
	}

	@Override
	public boolean isOver() {
		return this.roiManager.getCount() >= this.getOrganes().length + this.sumCont();
	}

	@Override
	public void fin() {
		Modele_Cardiac mdl = (Modele_Cardiac) this.getModele();
		mdl.calculerResultats();
		System.out.println(mdl);
		this.ajouterRoiOverlay(this.roiManager.getRoi(roiManager.getCount() - 1));
		
		new FenResultatCardiac(this.getVue(), mdl.getResults(), this.getDicomInfo(this.getVue().getImp()));

		this.getVue().getFen_application().dispose();
	}

	@Override
	public boolean isPost() {
		return (this.getIndexRoi() % 2 == 1);
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		// changement de slice si la prise contient une precoce
		if (this.isDeuxPrises()) {
			if (this.sumCont() > this.contSlice[0] || this.sumCont() == 0 || this.getIndexRoi() > this.sumCont()) {
				return 2;
			}
		}
		return 1;
	}

	@Override
	public Roi[] getRoisSlice(int nSlice) {
		// on affiche les contaminations si on est sur la premiere slide
		if (this.isDeuxPrises() && nSlice == 1) {
			Roi[] rois = new Roi[this.sumCont()];
			for (int i = 0; i < this.sumCont(); i++) {
				rois[i] = this.getRoiManager().getRoi(i);
			}
			return rois;
		} else {
			return super.getRoisSlice(nSlice);
		}
	}

	@Override
	public Roi getOrganRoi() {
		// symetrique du coeur
		if (this.getIndexRoi() == this.getOrganes().length + this.sumCont() - 2) {
			Roi roi = (Roi) this.roiManager.getRoi(this.getIndexRoi() - 2).clone();

			roi = RoiScaler.scale(roi, -1, 1, true);

			int quart = (this.getVue().getImp().getWidth() / 4);
			int newX = (int) (roi.getXBase() - Math.abs(2 * (roi.getXBase() - quart)) - roi.getFloatWidth());
			roi.setLocation(newX, roi.getYBase());
			return roi;
		}

		if (this.isPost()) { // si la prise est post, on decale l'organe precedent

			Roi roi = (Roi) this.roiManager.getRoi(getIndexRoi() - 1).clone();

			// on décale d'une demi largeur
			roi.setLocation(roi.getXBase() + (this.getVue().getImp().getWidth() / 2), roi.getYBase());
			return roi;
		}
		return null;
	}

	private void traiterContamination() {
		new JOptionPane();
		int reponse = JOptionPane.showConfirmDialog(this.getVue().getFen_application(), "Is there any contamination ?",
				"Remove contamination", JOptionPane.YES_NO_OPTION);
		if (reponse == JOptionPane.YES_OPTION) {
			((FenApplication_Cardiac) this.getVue().getFen_application()).startContaminationMode();
			this.modeCont = true;
		} else {
			if (this.getVue().getImp().getCurrentSlice() == 1) {
				this.showSliceWithOverlay(2);
				this.traiterContamination();
			}
		}
	}

	private boolean isDeuxPrises() {
		return this.getVue().getImp().getImageStackSize() > 1;
	}

	private void clicNewCont() {
		String name = this.createNomRoi("Contamination");

		// sauvegarde du roi courant
		boolean saved = this.saveCurrentRoi(name);

		if (saved) {
			this.contSlice[this.getVue().getImp().getCurrentSlice() - 1] += 1;

			this.preparerRoi();

			// on affiche les instructions
			this.getVue().getFen_application().setInstructions("Delimit a new contamination");
		}
	}

	@Override
	public int getIndexRoi() {
		return this.indexRoi + this.sumCont();
	}

	@Override
	public void notifyClick(ActionEvent arg0) {
		Button b = (Button) arg0.getSource();

		if (b == ((FenApplication_Cardiac) this.getVue().getFen_application()).getBtn_newCont()) {
			this.clicNewCont();
		}

		else if (b == ((FenApplication_Cardiac) this.getVue().getFen_application()).getBtn_continue()) {
			if (!this.isPost()) {
				((FenApplication_Cardiac) this.getVue().getFen_application()).stopContaminationMode();
				this.modeCont = false;
				if ((this.getVue().getImp().getCurrentSlice() == 1 && this.isDeuxPrises())) {
					this.showSliceWithOverlay(2);
					this.traiterContamination();
				}
			} else {
				IJ.log("Please delimit the Post contamination zone");
			}
		}

	}

	@Override
	public String getSameNameRoiCount(String nomRoi) {
		// si il s'agit d'un contamination, on affiche un numero pour les differencier
		if (nomRoi.contains("Conta")) {
			return this.getSameNameRoiCount("") + super.getSameNameRoiCount(nomRoi);
		}

		// sinon on renvoie L pour Late
		return "L";
	}

	private int sumCont() {
		int sum = 0;
		for (Integer i : this.contSlice)
			sum += i;
		return sum;
	}

}
