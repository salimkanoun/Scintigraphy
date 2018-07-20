package org.petctviewer.scintigraphy.dynamic;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.ZProjector;

public class Controleur_GeneralDyn extends ControleurScin {

	public static int MAXROI = 100;
	private int nbOrganes = 0;
	private boolean over;
	private ImagePlus impProjetee;

	protected Controleur_GeneralDyn(GeneralDynamicScintigraphy vue) {
		super(vue);
		this.setOrganes(new String[MAXROI]);
		this.setModele(new Modele_GeneralDyn(vue.getFrameDurations()));
		this.over = false;

		this.getScin().getFenApplication().getField_instructions().addKeyListener(new KeyListener() {

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
		this.getScin().getFenApplication().getField_instructions().setText(s);
	}

	@Override
	public void notifyClic(ActionEvent arg0) {
		Button b = (Button) arg0.getSource();
		FenApplication_GeneralDyn fen = (FenApplication_GeneralDyn) this.getScin().getFenApplication();

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
		this.impProjetee = this.getScin().getImp().duplicate();
		this.over = true; 
		this.nbOrganes = this.roiManager.getCount();
		DynamicScintigraphy vue = (DynamicScintigraphy) this.getScin();
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
		fenGroup.setLocationRelativeTo(this.getScin().getFenApplication());
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
			
			imp2.setProperty("Info", this.getScin().getImp().getInfoProperty());

			vue.setImp(imp2);
			vue.getFenApplication().setImage(imp2);
			vue.getFenApplication().resizeCanvas();
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
		DynamicScintigraphy vue = (DynamicScintigraphy) this.getScin();
		this.indexRoi = this.nbOrganes;
		this.over = false;
		this.addImpListener();
		
		vue.getFenApplication().setImage(this.impProjetee);
		vue.setImp(this.impProjetee);
		
		vue.getFenApplication().resizeCanvas();
	}

	private ModeleScinDyn saveValues(ImagePlus imp) {
		GeneralDynamicScintigraphy vue = (GeneralDynamicScintigraphy) this.getScin();
		ModeleScinDyn modele = new Modele_GeneralDyn(vue.getFrameDurations());

		this.getScin().setImp(imp);
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
			return this.getScin().getFenApplication().getField_instructions().getText();
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
		return 0;
	}

	@Override
	public boolean isPost() {
		ImagePlus impPost = ((GeneralDynamicScintigraphy) this.getScin()).getImpPost();
		return this.getScin().getImp().equals(impPost);
	}

}
