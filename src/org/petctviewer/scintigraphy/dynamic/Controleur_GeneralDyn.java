package org.petctviewer.scintigraphy.dynamic;

import java.awt.Button;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.ZProjector;

public class Controleur_GeneralDyn extends ControleurScin {
	
	public static int MAXROI = 5;
	private int nbOrganes = 0;
	private boolean over;

	protected Controleur_GeneralDyn(Vue_GeneralDyn vue) {
		super(vue);
		this.setOrganes(new String[MAXROI]);
		this.setModele(new Modele_GeneralDyn(vue));
		this.over = false;
	}

	@Override
	public void setInstructionsDelimit(int indexRoi) {
		String s;
		if (this.roiManager.getCount() > indexRoi) {
			s = this.roiManager.getRoi(indexRoi).getName();
		} else {
			s = "roi" + indexRoi;
		}
		this.getVue().getFen_application().getField_instructions().setText(s);
	}
	
	@Override
	public void notifyClic(ActionEvent arg0) {
		Button b = (Button) arg0.getSource();
		FenApplication_GeneralDyn fen = (FenApplication_GeneralDyn) this.getVue().getFen_application();
		
		if(b == fen.getBtn_finish()) {
			this.fin();
		}
	}
	
	@Override
	public Roi getOrganRoi(int lastRoi) {
		if(this.isOver()) {
			return this.roiManager.getRoi(this.indexRoi % nbOrganes);
		}
		return null;
	}
	
	@Override
	public void fin() {
		this.over = true;		
		this.nbOrganes = this.roiManager.getCount();		
		Vue_Dynamic vue = (Vue_Dynamic) this.getVue();		
		this.removeImpListener();

		ImagePlus imp = vue.getImp();
		BufferedImage capture;
		
		boolean postExists = false;
		
		String[] roiNames = new String[this.nbOrganes];
		for(int i = 0; i < this.roiManager.getCount(); i++) {
			roiNames[i] = this.roiManager.getRoi(i).getName();
		}
		
		FenGroup_GeneralDyn fenGroup = new FenGroup_GeneralDyn(roiNames);
		fenGroup.setModal(true);
		fenGroup.setVisible(true);
		String[][] asso = fenGroup.getAssociation();
		
		if(vue.getImpAnt() != null) {
			capture = ModeleScin.captureImage(imp, 300, 300).getBufferedImage();
			Modele_GeneralDyn modele = saveValues(vue.getImpAnt());
			new FenResultat_GeneralDyn(vue, capture, modele, asso, "Ant");
		}
		
		if(vue.getImpPost() != null) {
			postExists = true;
			ImagePlus imp2 = ZProjector.run(vue.getImpPost(), "sum");
			imp2.setOverlay(imp.getOverlay());
			
			vue.getFen_application().setImage(imp2);
			vue.getFen_application().setExtendedState(Frame.MAXIMIZED_BOTH);
			vue.getFen_application().toFront();
			
			Thread th = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					BufferedImage c = ModeleScin.captureImage(imp, 300, 300).getBufferedImage();
					
					Modele_GeneralDyn modele = saveValues(vue.getImpPost());
					new FenResultat_GeneralDyn(vue, c, modele, asso, "Post");
					
					Controleur_GeneralDyn.this.finishDrawingResultWindow();
				}
			});
			th.start();			
		}
		
		if(!postExists) {
			this.finishDrawingResultWindow();
		}
		
	}
	
	private void finishDrawingResultWindow() {
		Vue_Dynamic vue = (Vue_Dynamic) this.getVue();
		this.indexRoi = this.nbOrganes;
		this.over = false;
		this.addImpListener();
		vue.getFen_application().setImage(vue.getImpProjetee());
		vue.setImp(vue.getImpProjetee());
		vue.getFen_application().setExtendedState(Frame.MAXIMIZED_BOTH);
	}
	
	private Modele_GeneralDyn saveValues(ImagePlus imp) {
		Vue_GeneralDyn vue = (Vue_GeneralDyn) this.getVue();
		Modele_GeneralDyn modele = new Modele_GeneralDyn(vue);
		
		this.getVue().setImp(imp);
		indexRoi = 0;
		// on copie les roi sur toutes les slices
		for (int i = 1; i <= imp.getStackSize(); i++) {
			imp.setSlice(i);
			for (int j = 0; j < nbOrganes; j++) {
				imp.setRoi(getOrganRoi(indexRoi));
				String nom = this.getNomOrgane(indexRoi);
				modele.enregisterMesure(this.addTag(nom), imp);
				indexRoi++;
			}
		}
		
		return modele;
	}

	@Override
	public String getNomOrgane(int index) {
		if(!isOver()) {
			return this.getVue().getFen_application().getField_instructions().getText();
		}
		return this.roiManager.getRoi(index % this.nbOrganes).getName();
	}

	@Override
	public boolean isOver() {
		if(this.over) {
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
