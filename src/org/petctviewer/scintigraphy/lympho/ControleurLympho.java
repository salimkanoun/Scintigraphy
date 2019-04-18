package org.petctviewer.scintigraphy.lympho;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.renal.RenalScintigraphy;
import org.petctviewer.scintigraphy.renal.gui.FenResultats_Renal;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.Controleur_OrganeFixe;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.shunpo.FenResults;
import org.petctviewer.scintigraphy.shunpo.ModeleShunpo;
import org.petctviewer.scintigraphy.shunpo.TabResult;
import org.petctviewer.scintigraphy.shunpo.ControleurShunpo.State;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;

public class ControleurLympho extends ControleurScin{
	
	
	public String[] organes = { "L. foot", "R. foot"};
	
	private int organe;
	
	private int organeRoiMaganer;
	
	private enum Etat {
		FIRST_IMAGE, SECOND_IMAGE;
	}
	
	private Etat etat;

	public ControleurLympho(Scintigraphy main, FenApplication vue, String examType,ImageSelection[] selectedImages) {
		super(main, vue, new ModeleLympho(selectedImages,examType));

		this.etat = Etat.FIRST_IMAGE;
		// on bloque le modele pour ne pas enregistrer les valeurs de la projection
		((ModeleLympho) model).setLocked(true);
		
		this.organe = 0;
		
		this.organeRoiMaganer = 0;
	}

	@Override
	public boolean isOver() {
		return this.organeRoiMaganer >= this.organes.length*2 - 1;
	}

	
	/*
	@Override
	protected void end() {
		
		LymphoSintigraphy scinRenal = (LymphoSintigraphy) this.getScin();
		ModeleLympho modele = (ModeleLympho) this.model;
		
		ImagePlus imp = model.getImagePlus();
		
		// capture de l'imageplus ainsi que de l'overlay
		BufferedImage capture = Library_Capture_CSV.captureImage(this.model.getImagePlus(), 300, 300).getBufferedImage();
		
		// on enregistre la mesure pour chaque slice
		int indexRoi = 0;
		for (int i = 1; i <= imp.getStackSize(); i++) {
			imp.setSlice(i);
			for (int j = 0; j < this.getOrganes().length; j++) {
				imp.setRoi(this.model.getRoiManager().getRoi(indexRoi % this.getOrganes().length));
				String nom = this.getNomOrgane(indexRoi);
				//modele.enregistrerMesure(this.addTag(nom), imp);
				indexRoi++;
			}
		}
		
		// on calcule les resultats
		modele.calculerResultats();
		
		ImagePlus[] captures = new ImagePlus[2];
		ImageStack stackCapture = Library_Capture_CSV.captureToStack(captures);
		ImagePlus montage = this.montage(stackCapture);
		
		FenResults fenResult = new FenResults(this.model);
		fenResult.addTab(new TabResult(fenResult, "Result", true) {
			@Override
			public Component getSidePanelContent() {
				String[] result = ((ModeleShunpo) model).getResult();
				JPanel res = new JPanel(new GridLayout(result.length, 1));
				for(String s : result)
					res.add(new JLabel(s));
				return res;
			}
			@Override
			public JPanel getResultContent() {
				return new DynamicImage(montage.getImage());
			}
		});

		// SK On rebloque le modele pour la prochaine generation
		modele.setLocked(true);
		
	}
	
	*/
	
	@Override
	public void clicSuivant() {
		if (this.saveCurrentRoi(this.organes[this.organe]
				+ (this.etat == Etat.FIRST_IMAGE ? "_F" : "_S"))) {
			super.clicSuivant();

			this.displayRoi(this.position - 1);
			this.organeRoiMaganer++;

			// All organs delimited
			if (this.allOrgansDelimited()) {
				// Capture
				int indexCapture = this.currentStep + (this.state == State.DELEMIT_ORGAN_POST ? this.steps.length : 0);
				this.captures[indexCapture] = Library_Capture_CSV.captureImage(this.vue.getImagePlus(), 512, 0);
				// Next step
				this.currentStep++;
				// All steps completed
				if (this.allStepsCompleted()) {
					switch (this.state) {
					case DELIMIT_ORGAN_ANT:
						// Next state
						this.prepareState(State.DELEMIT_ORGAN_POST);
						break;
					case DELEMIT_ORGAN_POST:
						// End
						this.end();
						break;
					default:
						break;
					}
				} else {
					this.currentOrgan = 0;
					this.prepareStep(this.currentStep);
				}
			}
			// There is organs to be delimited
			else {
				// Next organ
				this.organeSuivant();
			}
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private boolean feetDone() {
		
	}

}
