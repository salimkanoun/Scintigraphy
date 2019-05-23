package org.petctviewer.scintigraphy.cardiac;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.lang.ArrayUtils;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.controller.Controller_OrganeFixe;
import org.petctviewer.scintigraphy.scin.exceptions.NoDataException;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.RoiScaler;

public class Controller_Cardiac extends Controller_OrganeFixe {

	private boolean finContSlice1;
	private boolean finContSlice2;
	private static String[] organes = { "Bladder", "Kidney R", "Kidney L", "Heart", "Bkg noise" };
	private Controller_Cardiac controler;

	protected Controller_Cardiac(Scintigraphy scin, ImageSelection[] selectedImages, String studyName) {
		super(scin, new Model_Cardiac(scin, selectedImages, studyName));
		controler = this;

		// on declare si il y a deux prises
		((Model_Cardiac) this.model).setDeuxPrise(this.isDeuxPrises());

		((Model_Cardiac) this.model).calculerMoyGeomTotale();

		// double les organes pour prise ant/post
		List<String> organesAntPost = new ArrayList<>();
		for (String s : organes) {
			organesAntPost.add(s);
			organesAntPost.add(s);
		}
		this.setOrganes(organesAntPost.toArray(new String[0]));

		// on lance le mode decontamination
		((FenApplication_Cardiac) this.getScin().getFenApplication()).startContaminationMode();
	}

	@Override
	public boolean isOver() {
		return this.getIndexRoi() + 1 >= this.getOrganes().length;
	}

	@Override
	public void end() {
		// suppression du controleur de l'imp
		// this.removeImpListener();
		((Model_Cardiac) this.model).getResults();
		((Model_Cardiac) this.model).calculateResults();
		
		BufferedImage capture = Library_Capture_CSV.captureImage(controler.getScin().getFenApplication().getImagePlus(), 400, 0).getBufferedImage();
		new FenResultat_Cardiac(controler.getScin(), capture, this);
	}

	@Override
	public boolean isPost() {
		ImagePlus imp = this.model.getImageSelection()[0].getImagePlus();

		if (imp.getRoi() != null) {
			return imp.getRoi().getXBase() > imp.getWidth() / 2;
		} else if (this.model.getRoiManager().getRoi(indexRoi - 1) != null) {
			return this.model.getRoiManager().getRoi(indexRoi - 1).getXBase() > imp.getWidth() / 2;
		}

		return false;
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
			Roi roi = (Roi) this.model.getRoiManager().getRoi(this.getIndexRoi() - 2).clone();

			// on fait le symetrique de la roi
			roi = RoiScaler.scale(roi, -1, 1, true);

			int quart = (this.model.getImageSelection()[0].getImagePlus().getWidth() / 4);
			int newX = (int) (roi.getXBase() - Math.abs(2 * (roi.getXBase() - quart)) - roi.getFloatWidth());
			roi.setLocation(newX, roi.getYBase());
			return roi;
		}

		// recupere la roi de l'organe symetrique
		Roi lastOrgan = (Roi) this.model.getRoiManager().getRoi(getIndexRoi() - 1);
		if (lastOrgan == null) { // si elle n'existe pas, on renvoie null
			return null;
		}
		lastOrgan = (Roi) lastOrgan.clone();

		// si la derniere roi etait post ou ant
		boolean OrganPost = lastOrgan.getXBase() > this.model.getImageSelection()[0].getImagePlus().getWidth() / 2;

		// si on doit faire le symetrique et que l'on a appuye sur next
		if (this.getIndexRoi() % 2 == 1 && lastRoi < this.indexRoi) {

			if (OrganPost) { // si la prise est ant, on decale l'organe precedent vers la droite
				lastOrgan.setLocation(lastOrgan.getXBase() - (this.model.getImageSelection()[0].getImagePlus().getWidth() / 2),
						lastOrgan.getYBase());
			} else { // sinon vers la gauche
				lastOrgan.setLocation(lastOrgan.getXBase() + (this.model.getImageSelection()[0].getImagePlus().getWidth() / 2),
						lastOrgan.getYBase());
			}

			return lastOrgan;
		}

		return null;
	}

	private boolean isDeuxPrises() {
		return this.model.getImageSelection()[0].getImagePlus().getImageStackSize() > 1;
	}

	private void clicNewCont() {
		// sauvegarde du roi courant
		String nom = "ContE";
		if (this.finContSlice1) {
			nom = "ContL";
		}

		try {
			this.saveRoiAtIndex(nom, this.indexRoi);

			this.indexRoi++;
			this.preparerRoi(this.indexRoi - 1);

			// on affiche les instructions
			if (this.getIndexRoi() % 2 == 0) {
				this.getScin().getFenApplication().setText_instructions("Delimit a new contamination");
				FenApplication_Cardiac fac = (FenApplication_Cardiac) this.getScin().getFenApplication();
				fac.getBtn_continue().setEnabled(true);
				fac.getBtn_newCont().setLabel("Next");
			} else {
				this.getScin().getFenApplication().setText_instructions("Adjust contamination zone");
				FenApplication_Cardiac fac = (FenApplication_Cardiac) this.getScin().getFenApplication();
				fac.getBtn_continue().setEnabled(false);
				fac.getBtn_newCont().setLabel("Save");
			}
		} catch (NoDataException e) {
			JOptionPane.showMessageDialog(vue, e.getMessage(), "", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void clicEndCont() {
		// on set la slice
		if ((this.model.getImageSelection()[0].getImagePlus().getCurrentSlice() == 1 && this.isDeuxPrises())) {
			// on relance le mode decontamination, cette fois ci pour la deuxieme slice
			this.finContSlice1 = true;
			this.setSlice(2);
			((FenApplication_Cardiac) this.getScin().getFenApplication()).startContaminationMode();

		} else { // on a trait� toutes les contaminations
			this.finContSlice2 = true;
			((FenApplication_Cardiac) this.getScin().getFenApplication()).stopContaminationMode();
			String[] conts = new String[this.indexRoi];
			for (int i = 0; i < conts.length; i++) {
				conts[i] = "Cont";
			}
			// on ajoute de nouvelles cases dans le tableau organes pour ne pas modifier
			// l'indexRoi
			this.setOrganes((String[]) ArrayUtils.addAll(conts, this.getOrganes()));
		}
	}

	@Override
	public String addTag(String nomOrgane) {
		String nom = nomOrgane;

		// on ajoute au nom P ou A pour Post ou Ant
		if (this.isPost()) {
			nom += " P";

		} else {
			nom += " A";
		}

		if (!this.finContSlice2) {
			String count = this.getSameNameRoiCount(nom);
			nom += count;

		}

		return nom;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		super.actionPerformed(arg0);
		// permet d'appeller les methodes correspondant au clic des deux nouveaux
		// boutons
		Button b = (Button) arg0.getSource();
		if (b == ((FenApplication_Cardiac) this.getScin().getFenApplication()).getBtn_newCont()) {
			this.clicNewCont();
		} else if (b == ((FenApplication_Cardiac) this.getScin().getFenApplication()).getBtn_continue()) {
			this.clicEndCont();
		}
	}

	public Model_Cardiac getModele() {
		return (Model_Cardiac) this.model;
	}

}
