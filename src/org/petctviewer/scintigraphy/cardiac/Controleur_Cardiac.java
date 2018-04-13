package org.petctviewer.scintigraphy.cardiac;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.controleur.ControleurScin;
import org.petctviewer.scintigraphy.scin.view.ModeleScin;
import org.petctviewer.scintigraphy.scin.view.VueScin;

import ij.gui.Roi;

public class Controleur_Cardiac extends ControleurScin {

	private static String[] organesPrecoce = {"Right Liver", "Left Liver", "Bladder", "Heart","Right Liver", "Left Liver"};
	private static String[] organes = {"Bladder", "Heart","Right Liver", "Left Liver"};
	
	protected Controleur_Cardiac(VueScin vue, ModeleScin leModele) {
		super(vue, leModele, null);
		
		String[] org = null;
		if(this.isPrecoce()) {
			org = Controleur_Cardiac.organesPrecoce;
		}else {
			org = Controleur_Cardiac.organes;
		}
		
		//double les organes pour prise ant/post
		List<String> organesAntPost = new ArrayList<String>();
		for(String s : org) {
			organesAntPost.add(s);
			organesAntPost.add(s);
		}
		this.setOrganes(organesAntPost.toArray(new String[0]));
		
		traiterContamination();
	}

	@Override
	public void preparerRoi() {
		//changement de slice si la prise contient une precoce
		if(this.isPrecoce()) {
			if(this.getIndexRoi() >= 3) {
				this.showSlice(2);
			}
		}
		
		//on affiche la roi precedente
		if(this.getRoiManager().getRoi(this.getIndexRoi() - 1) != null) {
			this.ajouterRoiOverlay(this.getRoiManager().getRoi(this.getIndexRoi() - 1));
		}
		
		Roi roi = null;
		if (this.getRoiManager().getRoi(this.getIndexRoi()) != null) { // si la roi existe dans le manager
			roi = this.getRoiManager().getRoi(getIndexRoi());
		} else {
			if(this.isPost()) { // si la prise est post, on decale l'organe precedent
				roi = (Roi) this.getRoiManager().getRoi(getIndexRoi() - 1).clone();
				roi.setLocation(roi.getXBase() + (this.getVue().getImp().getWidth() / 2), roi.getYBase());
			}
		}
		
		//selectionne la roi si elle n'est pas nulle
		this.selectRoi(roi);
		this.afficherInstruction();
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
		return this.getVue().getImp().getNSlices() == 2;
	}

	@Override
	public boolean isPost() {
		return (this.getIndexRoi() % 2 == 1);
	}

	private void traiterContamination() {
		JOptionPane d = new JOptionPane();
		int reponse = JOptionPane.showConfirmDialog(this.getVue().getFen_application(), "Is there any contaminations ?", "Remove contamination", JOptionPane.YES_NO_OPTION);
		if(reponse == JOptionPane.YES_OPTION) {
			this.startDecontamination();
		}
	}

	private void startDecontamination() {
		this.getVue().getFen_application().startContaminationMode();
	}

	@Override
	public String createNomRoi() {
		int nOrganeCourant = this.getIndexRoi() % this.organes.length;

		// création du nom du ROI selon la prise post ou ant
		String nomRoi = this.getOrganes()[nOrganeCourant];
		if (this.isPost()) {
			nomRoi += " Post";
		} else {
			nomRoi += " Ant";
		}

		// on enregistre la ROI dans le modele
		// TODO
		// leModele.enregisterMesure(nomRoi, laVue.getFen_application().getImagePlus());
	}
}
