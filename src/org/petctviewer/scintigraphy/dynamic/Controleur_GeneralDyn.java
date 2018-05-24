package org.petctviewer.scintigraphy.dynamic;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.VueScinDyn;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.ZProjector;

public class Controleur_GeneralDyn extends ControleurScin {

	public static int MAXROI = 100;
	private int nbOrganes = 0;
	private boolean over;
	private ImagePlus impProjetee;

	protected Controleur_GeneralDyn(Vue_GeneralDyn vue) {
		super(vue);
		this.setOrganes(new String[MAXROI]);
		this.setModele(new Modele_GeneralDyn(vue.getFrameDurations()));
		this.over = false;

		this.getVue().getFenApplication().getField_instructions().addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent arg0) {
				//non utilise
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				//non utilise
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					Controleur_GeneralDyn.this.clicSuivant();
				}
			}
		});
	}

	@Override
	public void setInstructionsDelimit(int indexRoi) {
		String s;
		if (this.roiManager.getCount() > this.indexRoi) {
			s = this.roiManager.getRoi(this.indexRoi).getName();
		} else {
			s = "roi" + this.indexRoi;
		}
		this.getVue().getFenApplication().getField_instructions().setText(s);
	}

	@Override
	public void notifyClic(ActionEvent arg0) {
		Button b = (Button) arg0.getSource();
		FenApplication_GeneralDyn fen = (FenApplication_GeneralDyn) this.getVue().getFenApplication();

		if (b == fen.getBtn_finish()) {
			this.clicSuivant();
			this.fin();
		}
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		if (this.isOver()) {
			return this.roiManager.getRoi(this.indexRoi % this.nbOrganes);
		}
		return null;
	}

	@Override
	public void fin() {
		//on sauvegarde l'imp projetee pour la reafficher par la suite
		this.impProjetee = this.getVue().getImp().duplicate();
		this.over = true;
		this.nbOrganes = this.roiManager.getCount();
		VueScinDyn vue = (VueScinDyn) this.getVue();
		this.removeImpListener();

		ImagePlus imp = vue.getImp();
		BufferedImage capture;

		boolean postExists = false;

		String[] roiNames = new String[this.nbOrganes];
		for (int i = 0; i < this.roiManager.getCount(); i++) {
			roiNames[i] = this.roiManager.getRoi(i).getName();
		}

		FenGroup_GeneralDyn fenGroup = new FenGroup_GeneralDyn(roiNames);
		fenGroup.setModal(true);
		fenGroup.setLocationRelativeTo(this.getVue().getFenApplication());
		fenGroup.setVisible(true);
		String[][] asso = fenGroup.getAssociation();

		if (vue.getImpAnt() != null) {
			capture = ModeleScin.captureImage(imp, 300, 300).getBufferedImage();
			ModeleScinDyn modele = saveValues(vue.getImpAnt());
			new FenResultat_GeneralDyn(vue, capture, modele, asso, "Ant");
		}

		if (vue.getImpPost() != null) {
			postExists = true;
			ImagePlus imp2 = ZProjector.run(vue.getImpPost(), "sum");
			imp2.setOverlay(imp.getOverlay());
			
			imp2.setProperty("Info", this.getVue().getImp().getInfoProperty());

			vue.setImp(imp2);
			vue.getFenApplication().setImage(imp2);
			vue.getFenApplication().adaptWindow(256);
			vue.getFenApplication().toFront();

			Thread th = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					BufferedImage c = ModeleScin.captureImage(imp, 300, 300).getBufferedImage();

					ModeleScinDyn modele = saveValues(vue.getImpPost());
					new FenResultat_GeneralDyn(vue, c, modele, asso, "Post");

					Controleur_GeneralDyn.this.finishDrawingResultWindow();
				}
			});
			th.start();
		}

		if (!postExists) {
			this.finishDrawingResultWindow();
		}

	}

	private void finishDrawingResultWindow() {
		VueScinDyn vue = (VueScinDyn) this.getVue();
		this.indexRoi = this.nbOrganes;
		this.over = false;
		this.addImpListener();
		
		vue.getFenApplication().setImage(this.impProjetee);
		vue.setImp(this.impProjetee);
		
		vue.getFenApplication().adaptWindow(256);
	}

	private ModeleScinDyn saveValues(ImagePlus imp) {
		Vue_GeneralDyn vue = (Vue_GeneralDyn) this.getVue();
		ModeleScinDyn modele = new Modele_GeneralDyn(vue.getFrameDurations());

		this.getVue().setImp(imp);
		this.indexRoi = 0;
		// on copie les roi sur toutes les slices
		for (int i = 1; i <= imp.getStackSize(); i++) {
			imp.setSlice(i);
			for (int j = 0; j < this.nbOrganes; j++) {
				imp.setRoi(getOrganRoi(this.indexRoi));
				String nom = this.getNomOrgane(this.indexRoi);
				modele.enregistrerMesure(this.addTag(nom), imp);
				this.indexRoi++;
			}
		}
		modele.calculerResultats();
		
		return modele;
	}

	@Override
	public String getNomOrgane(int index) {
		if (!isOver()) {
			return this.getVue().getFenApplication().getField_instructions().getText();
		}
		return this.roiManager.getRoi(index % this.nbOrganes).getName();
	}

	@Override
	public boolean isOver() {
		if (this.over) {
			return true;
		}
		return false;
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 1;
	}

	@Override
	public boolean isPost() {
		ImagePlus impPost = ((Vue_GeneralDyn) this.getVue()).getImpPost();
		return this.getVue().getImp().equals(impPost);
	}

}
