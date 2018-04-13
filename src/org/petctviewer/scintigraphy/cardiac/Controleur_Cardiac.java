package org.petctviewer.scintigraphy.cardiac;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.controleur.ControleurScin;
import org.petctviewer.scintigraphy.scin.view.ModeleScin;
import org.petctviewer.scintigraphy.scin.view.VueScin;

import ij.gui.Roi;

public class Controleur_Cardiac extends ControleurScin {

	private static String[] organesPrecoce = { "Right Liver", "Left Liver", "Bladder", "Right Liver", "Left Liver",
			"Heart" };
	private static String[] organes = { "Bladder", "Right Liver", "Left Liver", "Heart" };

	protected Controleur_Cardiac(VueScin vue, ModeleScin leModele) {
		super(vue, leModele);

		String[] org = null;
		if (this.isPrecoce()) {
			org = Controleur_Cardiac.organesPrecoce;
		} else {
			org = Controleur_Cardiac.organes;
		}

		// double les organes pour prise ant/post
		List<String> organesAntPost = new ArrayList<String>();
		for (String s : org) {
			organesAntPost.add(s);
			organesAntPost.add(s);
		}
		this.setOrganes(organesAntPost.toArray(new String[0]));

		traiterContamination();
	}

	@Override
	public void preparerRoi() {
		// on vide l'overlay et affiche la slice courante
		this.showSlice(getCurrentSlice());
		this.clearOverlay();		

		// on affiche les roi pour cette slide
		for(Roi roi : this.getRoisSlice(this.getCurrentSlice())) {
			this.ajouterRoiOverlay(roi);
		}

		Roi roi = null;
		if (this.getRoiManager().getRoi(this.getIndexRoi()) != null) { // si la roi existe dans le manager
			roi = this.getRoiManager().getRoi(getIndexRoi());
		} else {
			if (this.isPost()) { // si la prise est post, on decale l'organe precedent
				roi = (Roi) this.getRoiManager().getRoi(getIndexRoi() - 1).clone();
				roi.setLocation(roi.getXBase() + (this.getVue().getImp().getWidth() / 2), roi.getYBase());
			}
		}

		// selectionne la roi si elle n'est pas nulle
		this.selectRoi(roi);
		this.afficherInstruction();

		// changement de slice si la prise contient une precoce
		if (this.isPrecoce()) {
			if (this.getIndexRoi() == 4) {
				this.showSlice(2);
			}
		}
	}

	@Override
	public boolean isOver() {
		return this.getRoiManager().getCount() >= this.getOrganes().length;
	}

	@Override
	public void fin() {
		System.out.println("Fin de la prise de roi");
	}

	private boolean isPrecoce() {
		return this.getVue().getImp().getNSlices() >= 1;
	}

	@Override
	public boolean isPost() {
		return (this.getIndexRoi() % 2 == 1);
	}

	private void traiterContamination() {
		new JOptionPane();
		int reponse = JOptionPane.showConfirmDialog(this.getVue().getFen_application(), "Is there any contamination ?",
				"Remove contamination", JOptionPane.YES_NO_OPTION);
		if (reponse == JOptionPane.YES_OPTION) {
			this.getVue().getFen_application().startContaminationMode();
		}
	}

	@Override
	public String createNomRoi() {
		// création du nom du ROI selon la prise post ou ant
		String nomRoi = this.getOrganes()[this.getIndexRoi()];
		if (this.isPost()) {
			nomRoi += " Post";
		} else {
			nomRoi += " Ant";
		}

		return nomRoi;
	}

	@Override
	public int createNumeroRoi() {
		int numero;
		// on ajoute le numero de la slide au nom
		if (this.getVue().isAntPost()) {
			numero = this.getIndexRoi() / (this.getOrganes().length * 2);
		} else {
			numero = this.getIndexRoi() / this.getOrganes().length;
		}
		return numero;
	}

	@Override
	public Roi[] getRoisSlice(int nSlice) {// un peu sale, peut etre ameliore
		List<Roi> rois = new ArrayList<Roi>();
		int indexRoiDebut, indexRoiFin;

		// si il s'agit de la premiere slice, on renvoie les 4 premieres roi (si elles existent)
		if (this.getCurrentSlice() == 1) {
			indexRoiDebut = 0;
			indexRoiFin = 3;
		} else { //sinon on renvoie toutes les autres
			indexRoiDebut = 4;
			indexRoiFin = this.getRoiManager().getCount();
		}

		//construction de la liste
		for (int i = indexRoiDebut + this.getNbContamination(); i <= indexRoiFin + this.getNbContamination(); i++) {
			Roi roiIt = (Roi) this.getRoiManager().getRoi(i + this.getNbContamination());
			if (roiIt != null && this.getIndexRoi() != i) {
				rois.add(roiIt);
			}
		}
		
		return rois.toArray(new Roi[0]);
	}

}
