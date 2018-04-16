package org.petctviewer.scintigraphy.cardiac;

import java.awt.Button;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.controleur.ControleurScin;
import org.petctviewer.scintigraphy.scin.view.ModeleScin;
import org.petctviewer.scintigraphy.scin.view.VueScin;

import ij.gui.Roi;

public class Controleur_Cardiac extends ControleurScin {

	private static String[] organesDeuxPrises = { "Right Liver", "Left Liver", "Bladder", "Right Liver", "Left Liver",
			"Heart", "Background noise" };
	private static String[] organesUnePrise = { "Bladder", "Right Liver", "Left Liver", "Heart", "Background noise" };

	private int nbContamination;
	private int[] contSlice;

	protected Controleur_Cardiac(VueScin vue, ModeleScin leModele) {
		super(vue, leModele);

		this.contSlice = new int[2];

		String[] org = null;
		if (this.isDeuxPrises()) {
			org = Controleur_Cardiac.organesDeuxPrises;
		} else {
			org = Controleur_Cardiac.organesUnePrise;
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
	public boolean isOver() {
		return this.getRoiManager().getCount() >= this.getOrganes().length + this.sumCont();
	}

	@Override
	public void fin() {
		System.out.println("Fin de la prise de roi");
	}

	@Override
	public boolean isPost() {
		return (this.getIndexRoi() % 2 == 1);
	}



	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		// changement de slice si la prise contient une precoce
		if (this.isDeuxPrises()) {
			if (roiIndex >= 4 + this.contSlice[0]) {
				return 2;
			}
		}
		return 1;
	}

	@Override
	public Roi getOrganRoi() {
		if (this.isPost()) { // si la prise est post, on decale l'organe precedent

			Roi roi = (Roi) this.getRoiManager().getRoi(getIndexRoi() - 1).clone();

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
			this.getVue().getFen_application().startContaminationMode();
		}
	}

	private boolean isDeuxPrises() {
		return this.getVue().getImp().getNSlices() >= 1;
	}

	private void clicNewCont() {
		String name = "Contamination";

		this.saveCurrentRoi(name);

		this.contSlice[this.getCurrentSlice() - 1] += 1;

		this.preparerRoi();

		// on affiche les instructions
		this.getVue().getFen_application().setInstructions("Delimit a new contamination");
	}

	@Override
	public void traitementBouton(Button b) {
		if (b == this.getVue().getFen_application().getBtn_newCont()) {
			this.clicNewCont();
		}

		else if (b == this.getVue().getFen_application().getBtn_continue()) {
			this.getVue().getFen_application().stopContaminationMode();
		}

	}

	@Override
	public int getIndexRoi() {
		return this.indexRoi + this.sumCont();
	}

	@Override
	public void notifyClick() {
		if (this.getIndexRoi() == this.contSlice[0] + 4)
			this.traiterContamination();
	}

	private int sumCont() {
		int sum = 0;
		for (Integer i : this.contSlice)
			sum += i;
		return sum;
	}

}
