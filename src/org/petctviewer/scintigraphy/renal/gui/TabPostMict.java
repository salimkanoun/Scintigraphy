package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.basic.CustomControleur;
import org.petctviewer.scintigraphy.scin.basic.Vue_Basic;
import org.petctviewer.scintigraphy.scin.gui.FenResultatImp;

import ij.ImageJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.util.DicomTools;

public class TabPostMict extends FenResultatImp implements ActionListener, CustomControleur {

	private static final long serialVersionUID = 8125367912250906052L;
	private Vue_Basic vueBasic;
	private JButton btn_addImp, btn_quantify;
	private boolean bladder;

	private JPanel pnl_nora, pnl_bladder;

	public TabPostMict(VueScin vue, int w, int h) {
		super("Renal scintigraphy", vue, null, "postmict");
		this.bladder = Prefs.get("renal.bladder.preferred", true);

		this.pack();

		this.pnl_bladder = new JPanel();
		this.pnl_nora = new JPanel();

		btn_addImp = new JButton("Choose post-micturition dicom");
		btn_addImp.addActionListener(this);

		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(btn_addImp);
		box.add(Box.createHorizontalGlue());

		this.add(box, BorderLayout.CENTER);

		this.setPreferredSize(new Dimension(w, h));
		finishBuildingWindow(false);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == this.btn_addImp) {
			
			Modele_Renal modele = (Modele_Renal) this.getVue().getFenApplication().getControleur().getModele();

			// ajout des organes a delimiter selon le nombre de rein du patient
			List<String> organes = new ArrayList<String>();
			if (modele.getKidneys()[0]) {
				organes.add("L. Kidney");
				organes.add("L. bkg");
			}
			if (modele.getKidneys()[1]) {
				organes.add("R. Kidney");
				organes.add("R. bkg");
			}
			// si la vessie est selectionnee on l'ajoute a la liste
			if (this.bladder) {
				organes.add("Bladder");
			}

			// creation de la vue sans lancement de l'application
			this.vueBasic = new Vue_Basic(organes.toArray(new String[] {}), this);
			this.vueBasic.run("standby");

			// si l'utilisateur a choisi une image
			if (this.vueBasic.getImp() != null) {
				this.btn_addImp.setVisible(false);
				this.btn_quantify.setVisible(true);
				
				ImagePlus imp = this.vueBasic.getImp();
				ImagePlus impPost = null;
				if (imp.getStackSize() >= 2) { // si il y a deux slices on sellectionne la deuxieme
					impPost = new ImagePlus(imp.getTitle(), imp.getStack().getProcessor(2));
				} else { // si il n'y en a qu'une, on la selectionne
					impPost = imp;
				}
				// on retourne l'image plus selectionnee
				for (int i = 0; i < imp.getStackSize(); i++) {
					impPost.getProcessor().flipHorizontal();
				}

				this.setImp(this.vueBasic.getImp());
				
				// rafraichit la fenetre pour afficher l'imp
				finishBuildingWindow(false);
			}else {
				//sinon on supprime la fenetre
				this.vueBasic = null;
			}
		} else {
			this.btn_quantify.setVisible(false);
			this.vueBasic.lancerProgramme();
		}
	}

	@Override
	public void fin() {
		Modele_Renal modele = (Modele_Renal) this.getVue().getFenApplication().getControleur().getModele();

		HashMap<String, Double> data = this.vueBasic.getData();
		this.setImp(vueBasic.getImp());

		// TODO moy geom si ant post

		Double rg = null, rd = null;
		int duration = Integer.parseInt(DicomTools.getTag(this.getImagePlus(), "0018,1242").trim());
		if (modele.getKidneys()[0]) {
			rg = data.get("L. Kidney P0") - data.get("L. bkg P0");
			// on calcule les valeurs en coups/sec
			rg /= (duration / 1000);
		}
		if (modele.getKidneys()[1]) {
			rd = data.get("R. Kidney P0") - data.get("R. bkg P0");
			// on calcule les valeurs en coups/sec
			rd /= (duration / 1000);
		}

		// creation du panel nora rein gauche et droit
		this.pnl_nora = (JPanel) this.getPanelNoRa(rg, rd);

		// ajout de la vessie dans la liste d'organes si elle est selectionnee
		if (bladder) {
			Double bld = data.get("Bladder P0");
			bld /= (duration / 1000);
			this.pnl_bladder.add(new JLabel("Bladder : " + ModeleScinDyn.round(modele.getNoRABladder(bld), 2) + " %"));
		}
		
		finishBuildingWindow(true);
		pack();
	}

	@Override
	public Component getSidePanelContent() {
		if (this.getImagePlus() != null) {
			Box box = Box.createVerticalBox();
			JPanel flow = new JPanel();
			flow.add(this.pnl_nora);

			box.add(flow);
			box.add(this.pnl_bladder);
			box.add(this.getBoxSlider());

			return box;
		}

		this.btn_quantify = new JButton("Quantify");
		this.btn_quantify.addActionListener(this);
		this.btn_quantify.setVisible(false);
		
		JPanel flow = new JPanel();
		flow.add(btn_quantify);
		
		return flow;
	}

	@Override
	public Roi getOrganRoi(Roi roi) {
		int index = this.vueBasic.getFenApplication().getControleur().getIndexRoi();
		if (index == 1 || index == 3) {
			return VueScin.createBkgRoi(roi, this.vueBasic.getFenApplication().getImagePlus(), VueScin.KIDNEY);
		}
		return null;
	}

	@Override
	public void notifyClic(ActionEvent arg0) {
		Overlay ov = this.vueBasic.getImp().getOverlay();

		if (ov.getIndex("L. bkg") != -1) {
			VueScin.editLabelOverlay(ov, "L. bkg", "", Color.GRAY);
		}

		if (ov.getIndex("R. bkg") != -1) {
			VueScin.editLabelOverlay(ov, "R. bkg", "", Color.GRAY);
		}
	}

	private Component getPanelNoRa(Double rg, Double rd) {
		Modele_Renal modele = (Modele_Renal) this.getVue().getFenApplication().getControleur().getModele();
		Double[][] nora = modele.getNoRAPM(rg, rd);

		// elements du tableau
		JLabel[] lbls = new JLabel[] { new JLabel("L"), new JLabel("R"), new JLabel("Max"),
				new JLabel("" + naIfNull(nora[0][0])), new JLabel("" + naIfNull(nora[1][0])),
				new JLabel("" + ModeleScin.round(modele.getAdjustedValues().get("lasilix") - 1, 1) + " min"),
				new JLabel("" + naIfNull(nora[0][1])), new JLabel("" + naIfNull(nora[1][1])), };

		// panel nora
		JPanel pnl_nora = new JPanel(new GridLayout(3, 3, 0, 3));
		pnl_nora.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		pnl_nora.add(new JLabel("NORA Post-Mict"));
		// on centre les contenu du tableau
		for (JLabel l : lbls) {
			l.setHorizontalAlignment(JLabel.CENTER);
			pnl_nora.add(l);
		}

		return pnl_nora;
	}

	private String naIfNull(Double d) {
		if (d == null) {
			return "N/A";
		}
		return d + " %";
	}
}
