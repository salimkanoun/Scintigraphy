package org.petctviewer.scintigraphy.lympho;

import java.awt.Color;
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
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;

public class ControleurLympho extends ControleurScin{
	
	
	public String[] organes = { "L. foot", "R. foot"};

	
	private static final int FIRST_IMAGE = 0, SECOND_IMAGE = 1;
	
	private int organe;
	
	private int organeRoiMaganer;
	
	private int etape;
	
	private static final int CAPTURE_FIRST_ANT = 0,CAPTURE_FIRST_POST = 1, CAPTURE_SECOND_ANT = 2,CAPTURE_SECOND_POST = 3, TOTAL_CAPTURES = 4;

	
	private ImagePlus[] captures;
	
	private FenResults fenResults;
	
	private boolean firstOrientationOver;

	public ControleurLympho(Scintigraphy main, FenApplication vue, String examType,ImageSelection[] selectedImages) {
		super(main, vue, new ModeleLympho(selectedImages,examType));


		// on bloque le modele pour ne pas enregistrer les valeurs de la projection
		((ModeleLympho) model).setLocked(true);
		
		this.organe = 0;
		this.organeRoiMaganer = 0;
		this.firstOrientationOver = false;
		etape = 0;
		
		this.captures = new ImagePlus[TOTAL_CAPTURES];
		
		this.fenResults = new FenResults(this.model);
		this.fenResults.setVisible(false);
		
		this.changerImage();
	}

	@Override
	public boolean isOver() {
		return this.organeRoiMaganer >= this.organes.length*2 - 1;
	}


	
	@Override
	public void clicSuivant() {
		if (this.saveCurrentRoi(this.organes[this.organe]
				+ (this.etape == FIRST_IMAGE ? "_F" : "_S") + (firstOrientationOver ? "_P" : "_A"))) {
			super.clicSuivant();

			this.displayRoi(this.position - 1);
			
			if (this.allOrgansDelimited()) {
				this.capture();
				if (this.firstOrientationOver)
					if (this.etape == this.organes.length - 1)
						this.end();
					else
						this.nextStep();
				else
					this.nextOrientation();;
			} else
				this.nextOrgan();

			
		}
	}
	
	
	@Override
	public void clicPrecedent() {
		super.clicPrecedent();

		if (this.organe == 0)
			if (!this.firstOrientationOver)
				this.previousStep();
			else
				this.previousOrientation();
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
			this.firstOrientationOver = false;
			changerImage();
		}

		private void previousStep() {
			this.etape--;
			this.organe = this.organes.length - 1;
			this.organeRoiMaganer--;
			this.firstOrientationOver = true;
			changerImage();
		}
		
		private void nextOrientation() {
			this.firstOrientationOver = true;
			this.organe = 0;
			this.organeRoiMaganer -= this.organes.length - 1;
			this.prepareOrientation();
		}
		
		private void previousOrientation() {
			this.firstOrientationOver = false;
			this.organe = this.organes.length - 1;
			this.organeRoiMaganer += this.organes.length - 1;
			this.prepareOrientation();
		}
		
		
		
		private void capture() {
			ImagePlus capture = Library_Capture_CSV.captureImage(this.vue.getImagePlus(), 512, 0);
			if (this.etape == FIRST_IMAGE) 
				if (firstOrientationOver)
					this.captures[CAPTURE_FIRST_ANT] = capture;
				else
					this.captures[CAPTURE_FIRST_POST] = capture;
			else if (firstOrientationOver)
					this.captures[CAPTURE_SECOND_ANT] = capture;
			else
				this.captures[CAPTURE_SECOND_POST] = capture;
				
			

		}
		
		private boolean allOrgansDelimited() {
			return this.organe >= this.organes.length - 1;
		}
		
		
		private void editOrgan() {
			boolean existed = false;

			existed = this.editRoi(this.position);
			if (!existed) {
				existed = this.editCopyRoi(this.organeRoiMaganer * 2 - this.organe);
			}

			if (existed)
				this.displayInstructionorgane("Adjust");
			else
				this.displayInstructionorgane("Delimit");
		}

	
	
		private void displayInstructionorgane(String type) {
			this.displayInstruction(type + " the " + this.organes[this.organe]);
		}
		
		private final void DEBUG(String location) {
			if (location != null)
				System.out.println("== " + location.toUpperCase() + " ==");
			System.out.println(
					"Current step: " + this.etape + " [" + (this.etape == 0 ? "FirstImage" : "SecondImage") + "]");
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
		int organ = 0;
		for (int i = 0; i < this.model.getRoiManager().getRoisAsArray().length; i++) {

			Roi r = this.model.getRoiManager().getRoisAsArray()[i];
			

			if (i < this.organes.length) {
				img = this.model.getImageSelection()[FIRST_IMAGE].getImagePlus();
				img.setSlice(firstSlice);
				

			} else if (i < 2 * this.organes.length) {
				img = this.model.getImageSelection()[FIRST_IMAGE].getImagePlus();
				img.setSlice(secondSlice);
			} else if (i < 3 * this.organes.length) {
				img = this.model.getImageSelection()[SECOND_IMAGE].getImagePlus();
				img.setSlice(firstSlice);
			}
			else {
				img = this.model.getImageSelection()[SECOND_IMAGE].getImagePlus();
				img.setSlice(secondSlice);

			} 

			img.setRoi(r);
			((ModeleLympho) this.model).calculerCoups(organ, img);
			organ++;

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
		
		this.vue.setImage(this.model.getImageSelection()[this.etape].getImagePlus());
		this.vue.getImagePlus().setOverlay(Library_Gui.initOverlay(this.vue.getImagePlus()));

		// Remove overlay
		
		this.resetOverlay(this.vue.getImagePlus());
		
		// Display ant image
		if(firstOrientationOver)
			this.vue.getImagePlus().setSlice(2);
		else
			this.vue.getImagePlus().setSlice(1);
		this.editOrgan();
	}
	
	
	public void resetOverlay(ImagePlus imp) {
		imp.setOverlay(Library_Gui.initOverlay(imp));
		Library_Gui.setOverlayDG(imp, Color.YELLOW);
		Library_Gui.setOverlayDG(this.vue.getImagePlus(), Color.YELLOW);
		Library_Gui.setOverlayTitle("Ant", this.vue.getImagePlus(), Color.YELLOW, 1);
		Library_Gui.setOverlayTitle("Post", this.vue.getImagePlus(), Color.YELLOW, 2);
	}
	
	
	private void prepareOrientation() {
		// Remove overlay
		this.resetOverlay();

		this.vue.setImage(this.model.getImageSelection()[this.etape].getImagePlus());
		if(firstOrientationOver)
			this.vue.getImagePlus().setSlice(2);
		else
			this.vue.getImagePlus().setSlice(1);
		this.editOrgan();
	}


}
