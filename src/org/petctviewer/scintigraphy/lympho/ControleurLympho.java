package org.petctviewer.scintigraphy.lympho;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;




import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;



import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;

public class ControleurLympho extends ControleurScin{
	
	
	public String[] organes = { "L. foot", "R. foot"};
	private enum Etat { FIRST_IMAGE, SECOND_IMAGE; }
	
	private static final int FIRST_IMAGE = 0, SECOND_IMAGE = 1;
	
	private int organe;
	
	private int organeRoiMaganer;
	
	private int etape;
	
	private static final int CAPTURE_FIRST_ANT = 0, CAPTURE_SECOND_ANT = 1, TOTAL_CAPTURES = 2;
	
	private Etat etat;
	
	private ImagePlus[] captures;
	
	private FenResults fenResults;

	public ControleurLympho(Scintigraphy main, FenApplication vue, String examType,ImageSelection[] selectedImages) {
		super(main, vue, new ModeleLympho(selectedImages,examType));

		this.etat = Etat.FIRST_IMAGE;
		// on bloque le modele pour ne pas enregistrer les valeurs de la projection
		((ModeleLympho) model).setLocked(true);
		
		this.organe = 0;
		this.organeRoiMaganer = 0;
		
		etape = 0;
		
		this.captures = new ImagePlus[TOTAL_CAPTURES];
		
		this.fenResults = new FenResults(this.model);
		this.fenResults.setVisible(false);
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
		int organeRoiMaganer = 0;
		for (int i = 1; i <= imp.getStackSize(); i++) {
			imp.setSlice(i);
			for (int j = 0; j < this.getOrganes().length; j++) {
				imp.setRoi(this.model.getRoiManager().getRoi(organeRoiMaganer % this.getOrganes().length));
				String nom = this.getNomOrgane(organeRoiMaganer);
				//modele.enregistrerMesure(this.addTag(nom), imp);
				organeRoiMaganer++;
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
				String[] result = ((ModeleLympho) model).getResult();
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

			this.displayRoi(this.position);
			this.organeRoiMaganer++;
			
			if (this.allOrgansDelimited()) {
				this.capture();
				if (this.etape == this.organes.length - 1)
					this.end();
				else
					this.nextStep();
			} else
				this.nextOrgan();

			
		}
	}
	
	
	@Override
	public void clicPrecedent() {
		super.clicPrecedent();

		if (this.organe == 0)
			this.previousStep();
		else
			this.previousOrgan();

		this.displayRois(this.position - this.organe, this.position);

		this.fenResults.setVisible(false);
	}
		
		
		private void nextOrgan() {
			this.organe++;
			this.organeRoiMaganer++;
			this.editOrgan();

		}

		private void previousOrgan() {
			this.organe--;
			this.organeRoiMaganer--;
			this.editOrgan();
		}
		
		private void nextStep() {
			this.etape++;
			this.organe = 0;
			this.organeRoiMaganer++;
			changerImage();
		}

		private void previousStep() {
			this.etape--;
			this.organe = this.organes.length - 1;
			this.organeRoiMaganer--;
		}
		
		
		
		private void capture() {
			this.vue.getImagePlus().setSlice(1);
			ImagePlus captureAnt = Library_Capture_CSV.captureImage(this.vue.getImagePlus(), 512, 0);
			this.vue.getImagePlus().setSlice(2);
			ImagePlus capturePost = Library_Capture_CSV.captureImage(this.vue.getImagePlus(), 512, 0);
			if (this.etat == Etat.FIRST_IMAGE) {
					this.captures[CAPTURE_FIRST_ANT] = captureAnt;
					this.captures[CAPTURE_FIRST_ANT+1] = capturePost;

			}else {
				this.captures[CAPTURE_SECOND_ANT] = captureAnt;
				this.captures[CAPTURE_SECOND_ANT+1] = capturePost;
			}

		}
		
		private boolean allOrgansDelimited() {
			return this.organe >= this.organes.length - 1;
		}
		
		
		private void editOrgan() {
			DEBUG("EDIT ORGAN");
			boolean existed = false;

			existed = this.editRoi(this.position);
			if (!existed) {
				existed = this.editCopyRoi(this.organeRoiMaganer * 2 - this.organe);
			}

//			if (this.isNowPost()) {
//				existed = this.editRoi(this.organeRoiMaganer);
//			} else {
//				existed = this.editRoi(this.position);
//				if (!existed) {
//					existed = this.editCopyRoi(this.organeRoiMaganer);
//				}
//			}

			if (existed)
				this.displayInstructionorgane("Adjust");
			else
				this.displayInstructionorgane("Delimit");
			DEBUG();
		}

	
	
		private void displayInstructionorgane(String type) {
			this.displayInstruction(type + " the " + this.organes[this.organe]);
		}
		
		private final void DEBUG(String location) {
			if (location != null)
				System.out.println("== " + location.toUpperCase() + " ==");
			System.out.println(
					"Current step: " + this.etape + " [" + (this.etape == 0 ? "PULMON_KIDNEY" : "BRAIN") + "]");
			System.out.println(
					"Current organ: " + this.organe + " [" + this.organes[this.organe] + "]");
			System.out.println("Index ROI: " + this.organeRoiMaganer);
			System.out.println("Position: " + this.position);
			if (location == null)
				System.out.println("==============");
			System.out.println();
		}
		
		private final void DEBUG() {
			this.DEBUG(null);
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private boolean feetDone() {
		return (Boolean) null;
	}

	
	
	
	
	
	
	
	
	
	
	
	@Override
	protected void end() {
		this.organe++;
		this.vue.getTextfield_instructions().setText("End!");
		this.vue.getBtn_suivant().setEnabled(false);

		// Compute model
		int firstSlice = 1;
		int secondSlice = 2;
		ImagePlus img = this.model.getImageSelection()[FIRST_IMAGE].getImagePlus();
		this.model.getImageSelection()[FIRST_IMAGE].getImagePlus().setSlice(firstSlice);
		this.model.getImageSelection()[SECOND_IMAGE].getImagePlus().setSlice(firstSlice);
		for (int i = 0; i < this.model.getRoiManager().getRoisAsArray().length; i++) {

			Roi r = this.model.getRoiManager().getRoisAsArray()[i];
			int organ = 0;

			if (i < this.organes.length) {
				img = this.model.getImageSelection()[FIRST_IMAGE].getImagePlus();
				img.setSlice(firstSlice);
				organ++;

			} else {
				img = this.model.getImageSelection()[SECOND_IMAGE].getImagePlus();
				img.setSlice(firstSlice);

			} 

			
			img.setRoi(r);
			((ModeleLympho) this.model).calculerCoups(organ, img);
			
			
			img.setSlice(secondSlice);
			organ++;
			img.setRoi(r);
			((ModeleLympho) this.model).calculerCoups(organ, img);
		}
		this.model.calculerResultats();

		// Save captures
		ImageStack stackCapture = Library_Capture_CSV.captureToStack(this.captures);
		ImagePlus montage = this.montage(stackCapture);

		// Display result
		this.fenResults.setMainTab(new TabResult(fenResults, "Result", true) {
			@Override
			public Component getSidePanelContent() {
				String[] result = ((ModeleLympho) model).getResult();
				JPanel res = new JPanel(new GridLayout(result.length, 1));
				for (String s : result)
					res.add(new JLabel(s));
				return res;
			}

			@Override
			public JPanel getResultContent() {
				return new DynamicImage(montage.getImage());
			}
		});
		this.fenResults.pack();
		this.fenResults.setVisible(true);

	}
	
	
	
	
	
	
	
	
	private void changerImage() {
		// Remove overlay
		this.resetOverlay();

		this.vue.setImage(this.model.getImageSelection()[this.etape].getImagePlus());
		// Display ant image
		this.vue.getImagePlus().setSlice(1);

		this.editOrgan();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
