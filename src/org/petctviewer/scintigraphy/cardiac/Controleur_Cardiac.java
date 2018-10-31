package org.petctviewer.scintigraphy.cardiac;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.ArrayUtils;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.RoiScaler;

public class Controleur_Cardiac extends ControleurScin {

	private boolean finContSlice1;
	private boolean finContSlice2;
	private static String[] organes = { "Bladder", "Kidney R", "Kidney L", "Heart", "Bkg noise" };

	protected Controleur_Cardiac(Scintigraphy vue) {
		super(vue);

		Modele_Cardiac modele = new Modele_Cardiac(this.getScin().getImp());

		// on declare si il y a deux prises
		modele.setDeuxPrise(this.isDeuxPrises());

		modele.calculerMoyGeomTotale();
		this.setModele(modele);

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
	public void fin() {
		// suppression du controleur de l'imp
		this.removeImpListener();

		ModeleScin mdl = this.getModele();
		mdl.calculerResultats();

		CardiacScintigraphy vue = (CardiacScintigraphy) this.getScin();
		//vue.getFenApplication().resizeCanvas();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				BufferedImage capture = Library_Capture_CSV.captureImage(vue.getImp(), 400, 0).getBufferedImage();	
				new FenResultat_Cardiac(vue, capture);
			}
		});

	}

	@Override
	public String getSameNameRoiCount(String nomRoi) {
		// on renvoie le nombre de roi identiques uniquement si toutes les
		// contaminations ont ete prises
		if (!this.finContSlice1 || !this.finContSlice2) {
			return super.getSameNameRoiCount(nomRoi);
		}
		return "";
	}

	@Override
	public boolean isPost() {
		ImagePlus imp = this.getScin().getImp();

		if (imp.getRoi() != null) {
			return imp.getRoi().getXBase() > imp.getWidth() / 2;
		} else if (this.roiManager.getRoi(indexRoi - 1) != null) {
			return this.roiManager.getRoi(indexRoi - 1).getXBase() > imp.getWidth() / 2;
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
			Roi roi = (Roi) this.roiManager.getRoi(this.getIndexRoi() - 2).clone();

			// on fait le symetrique de la roi
			roi = RoiScaler.scale(roi, -1, 1, true);

			int quart = (this.getScin().getImp().getWidth() / 4);
			int newX = (int) (roi.getXBase() - Math.abs(2 * (roi.getXBase() - quart)) - roi.getFloatWidth());
			roi.setLocation(newX, roi.getYBase());
			return roi;
		}

		//recupere la roi de l'organe symetrique
		Roi lastOrgan = (Roi) this.roiManager.getRoi(getIndexRoi() - 1);
		if(lastOrgan == null) { //si elle n'existe pas, on renvoie null
			return null;
		}
		lastOrgan = (Roi) lastOrgan.clone();

		// si la derniere roi etait post ou ant
		boolean OrganPost = lastOrgan.getXBase() > this.getScin().getImp().getWidth() / 2;

		// si on doit faire le symetrique et que l'on a appuye sur next
		if (this.getIndexRoi() % 2 == 1 && lastRoi < this.indexRoi) {

			if (OrganPost) { // si la prise est ant, on decale l'organe precedent vers la droite
				lastOrgan.setLocation(lastOrgan.getXBase() - (this.getScin().getImp().getWidth() / 2), lastOrgan.getYBase());
			} else { // sinon vers la gauche
				lastOrgan.setLocation(lastOrgan.getXBase() + (this.getScin().getImp().getWidth() / 2), lastOrgan.getYBase());
			}

			return lastOrgan;
		}

		return null;
	}

	private boolean isDeuxPrises() {
		return this.getScin().getImp().getImageStackSize() > 1;
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

		}
	}

	private void clicEndCont() {
		// on set la slice
		if ((this.getScin().getImp().getCurrentSlice() == 1 && this.isDeuxPrises())) {
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
		
		if(!this.finContSlice2) {
			return nom + this.roiManager.getCount();
		}
		
		// on ajoute au nom P ou A pour Post ou Ant
		if (this.isPost()) {
			nom += " P";
		} else {
			nom += " A";
		}

		return nom;
	}

	@Override
	public void notifyClic(ActionEvent arg0) {
		// permet d'appeller les methodes correspondant au clic des deux nouveaux
		// boutons
		Button b = (Button) arg0.getSource();
		if (b == ((FenApplication_Cardiac) this.getScin().getFenApplication()).getBtn_newCont()) {
			this.clicNewCont();
		} else if (b == ((FenApplication_Cardiac) this.getScin().getFenApplication()).getBtn_continue()) {
			this.clicEndCont();
		}
	}

}
