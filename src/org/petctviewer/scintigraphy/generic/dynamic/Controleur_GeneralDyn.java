package org.petctviewer.scintigraphy.generic.dynamic;

import java.awt.Button;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.petctviewer.scintigraphy.scin.Controleur_OrganeFixe;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.ZProjector;

public class Controleur_GeneralDyn extends Controleur_OrganeFixe {

	public static int MAXROI = 100;
	private int nbOrganes = 0;
	private boolean over;
	private ImagePlus impProjetee;

	protected Controleur_GeneralDyn(GeneralDynamicScintigraphy scin) {
		super(scin);
		this.setOrganes(new String[MAXROI]);
		
		this.over = false;

		this.getScin().getFenApplication().getTextfield_instructions().addKeyListener(new KeyListener() {

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
		this.getScin().getFenApplication().getTextfield_instructions().setText(s);
	}

	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		super.actionPerformed(arg0);
		Button b = (Button) arg0.getSource();
		FenApplication_GeneralDyn fen = (FenApplication_GeneralDyn) this.getScin().getFenApplication();

		if (b == fen.getBtn_finish()) {
			this.clicSuivant();
			this.end();
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
	public void end() {
		//on sauvegarde l'imp projetee pour la reafficher par la suite
		this.impProjetee = this.getScin().getImp().duplicate();
		this.over = true; 
		this.nbOrganes = this.roiManager.getCount();
		GeneralDynamicScintigraphy scindyn = (GeneralDynamicScintigraphy) this.getScin();
		this.removeImpListener();

		ImagePlus imp = scin.getImp();
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

		if (scindyn.getImpAnt() != null) {
			capture = Library_Capture_CSV.captureImage(imp, 300, 300).getBufferedImage();
			saveValues(scindyn.getImpAnt());
			new FenResultat_GeneralDyn(scindyn, capture, (ModeleScinDyn) scin.getModele(), asso, "Ant");
		}

		if (scindyn.getImpPost() != null) {
			postExists = true;
			ImagePlus imp2 = ZProjector.run(scindyn.getImpPost(), "sum");
			imp2.setOverlay(imp.getOverlay());
			
			imp2.setProperty("Info", this.getScin().getImp().getInfoProperty());

			scindyn.setImp(imp2);
			scindyn.getFenApplication().setImage(imp2);
			scindyn.getFenApplication().resizeCanvas();
			scindyn.getFenApplication().toFront();

			Thread th = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					BufferedImage c = Library_Capture_CSV.captureImage(imp, 300, 300).getBufferedImage();

					saveValues(scindyn.getImpPost());
					new FenResultat_GeneralDyn(scindyn, c, (ModeleScinDyn) scin.getModele(), asso, "Post");

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
		GeneralDynamicScintigraphy vue = (GeneralDynamicScintigraphy) this.getScin();
		this.indexRoi = this.nbOrganes;
		this.over = false;
		this.addImpListener();
		
		vue.getFenApplication().setImage(this.impProjetee);
		vue.setImp(this.impProjetee);
		
		vue.getFenApplication().resizeCanvas();
	}

	private void saveValues(ImagePlus imp) {

		this.getScin().setImp(imp);
		this.indexRoi = 0;
		
		HashMap<String, List<Double>> mapData=new HashMap<String, List<Double>>();
		// on copie les roi sur toutes les slices
		for (int i = 1; i <= imp.getStackSize(); i++) {
			imp.setSlice(i);
			for (int j = 0; j < this.nbOrganes; j++) {
				imp.setRoi(getOrganRoi(this.indexRoi));
				String name = this.getNomOrgane(this.indexRoi);
				
				//String name = nom.substring(0, nom.lastIndexOf(" "));
				// on cree la liste si elle n'existe pas
				if ( mapData.get(name) == null) {
					mapData.put(name, new ArrayList<Double>());
				}
				// on y ajoute le nombre de coups
				mapData.get(name).add(Library_Quantif.getCounts(imp));
				
				this.indexRoi++;
			}
		}
		//set data to the model
		((ModeleScinDyn) scin.getModele()).setData(mapData);
		scin.getModele().calculerResultats();
		
	}

	@Override
	public String getNomOrgane(int index) {
		if (!isOver()) {
			return this.getScin().getFenApplication().getTextfield_instructions().getText();
		}
		System.out.println(roiManager.getRoi(index % this.nbOrganes).getName());
		return roiManager.getRoi(index % this.nbOrganes).getName();
	}

	@Override
	public boolean isOver() {
		return this.over;
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

	@Override
	public boolean saveCurrentRoi(String nomRoi, int indexRoi) {
		if (this.getSelectedRoi() != null) { // si il y a une roi sur l'image plus
			// on change la couleur pour l'overlay
			this.scin.getImp().getRoi().setStrokeColor(Color.YELLOW);
			// on enregistre la ROI dans le modele
			//SK ICI ON REMPLACE
			/*(( GeneralDynamicScintigraphy)scin.getModele()).enregistrerMesure(
					this.addTag(nomRoi), 
					this.scin.getImp());*/
	
			// On verifie que la ROI n'existe pas dans le ROI manager avant de l'ajouter
			// pour eviter les doublons
			if (this.roiManager.getRoi(indexRoi) == null) {
				roiManager.addRoi(this.scin.getImp().getRoi());
			} else { // Si il existe on l'ecrase
				this.roiManager.setRoi(this.scin.getImp().getRoi(), indexRoi);
				// on supprime le roi nouvellement ajoute de la vue
				this.scin.getFenApplication().getImagePlus().killRoi();
			}
	
			// precise la postion en z
			this.roiManager.
			getRoi(indexRoi).
			setPosition(this.getSliceNumberByRoiIndex(indexRoi));
	
			// changement de nom
			this.roiManager.rename(indexRoi, nomRoi);
	
			return true;
		}
		return false;
		
	}
}

