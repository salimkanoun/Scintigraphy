package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.basic.BasicScintigraphy;
import org.petctviewer.scintigraphy.scin.basic.CustomControleur;
import org.petctviewer.scintigraphy.scin.gui.FenResultatImp;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.util.DicomTools;

class TabPostMict extends FenResultatImp implements ActionListener, CustomControleur {

	private static final long serialVersionUID = 8125367912250906052L;
	private BasicScintigraphy vueBasic;
	private JButton btn_addImp, btn_quantify;
	private boolean bladder;

	private JPanel pnl_excr, pnl_bladder;

	public TabPostMict(Scintigraphy vue) {
		super("Renal scintigraphy", vue, null, "postmict");
		this.bladder = Prefs.get("renal.bladder.preferred", true);

		this.pnl_bladder = new JPanel();
		this.pnl_excr = new JPanel();

		btn_addImp = new JButton("Choose post-mictional dicom");
		btn_addImp.addActionListener(this);

		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(btn_addImp);
		box.add(Box.createHorizontalGlue());

		Component comp = this.getSidePanelContent();
		SidePanel side = new SidePanel(comp, "Renal Scintigraphy", vue.getImp());
		this.add(box, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == this.btn_addImp) {
			// Assez sale
			FenSelectionDicom fen = new FenSelectionDicom("Post-mictional", new Scintigraphy("") {
				@Override
				protected ImagePlus preparerImp(ImagePlus[] images) {
					if (images.length > 1) {
						return null;
					}

					btn_addImp.setVisible(false);
					btn_quantify.setVisible(true);

					TabPostMict.this.setImp(images[0]);

					return images[0];
				}

				@Override
				public void lancerProgramme() {
				}
			});
			fen.setVisible(true);
		} else {
			this.btn_quantify.setVisible(false);
			this.vueBasic = new BasicScintigraphy(createOrgans(), this);
			this.vueBasic.startExam(new ImagePlus[] { this.getImagePlus() });
		}
	}

	private String[] createOrgans() {
		Modele_Renal modele = (Modele_Renal) getVue().getFenApplication().getControleur().getModele();

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

		return organes.toArray(new String[0]);
	}

	@Override
	public void fin() {
		Modele_Renal modele = (Modele_Renal) getVue().getFenApplication().getControleur().getModele();

		HashMap<String, Double> data = this.vueBasic.getData();
		this.setImp(vueBasic.getImp());

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

		// creation du panel excr rein gauche et droit
		this.pnl_excr = (JPanel) this.getPanelExcr(rg, rd);

		// ajout de la vessie dans la liste d'organes si elle est selectionnee
		if (bladder) {
			Double bld = data.get("Bladder P0");
			bld /= (duration / 1000);
			this.pnl_bladder.add(new JLabel("Bladder : " + ModeleScinDyn.round(modele.getExcrBladder(bld), 2) + " %"));
		}

		Component comp = this.getSidePanelContent();
		SidePanel side = new SidePanel(comp, "Renal Scintigraphy", this.getImagePlus());
		side.addCaptureBtn(vueBasic, "_PostMict", new Component[] { this.getSlider() });
		this.add(side, new JPanel());
		this.add(side, BorderLayout.EAST);
	}

	public Component getSidePanelContent() {
		if (this.getImagePlus() != null) {
			Box box = Box.createVerticalBox();
			JPanel flow = new JPanel();
			flow.add(this.pnl_excr);

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
			return Scintigraphy.createBkgRoi(roi, this.vueBasic.getFenApplication().getImagePlus(),
					Scintigraphy.KIDNEY);
		}
		return null;
	}

	@Override
	public void notifyClic(ActionEvent arg0) {
		Overlay ov = this.vueBasic.getImp().getOverlay();

		if (ov.getIndex("L. bkg") != -1) {
			Scintigraphy.editLabelOverlay(ov, "L. bkg", "", Color.GRAY);
		}

		if (ov.getIndex("R. bkg") != -1) {
			Scintigraphy.editLabelOverlay(ov, "R. bkg", "", Color.GRAY);
		}
	}

	private Component getPanelExcr(Double rg, Double rd) {
		Modele_Renal modele = (Modele_Renal) this.getVue().getFenApplication().getControleur().getModele();
		Double[][] excr = modele.getExcrPM(rg, rd);

		// elements du tableau
		JLabel[] lbls = new JLabel[] { new JLabel("L"), new JLabel("R"), new JLabel("Max"),
				new JLabel("" + naIfNull(excr[0][0])), new JLabel("" + naIfNull(excr[1][0])),
				new JLabel("" + ModeleScin.round(modele.getAdjustedValues().get("lasilix") - 1, 1) + " min"),
				new JLabel("" + naIfNull(excr[0][1])), new JLabel("" + naIfNull(excr[1][1])), };

		// panel excr
		JPanel pnl_excr = new JPanel(new GridLayout(3, 3, 0, 3));
		pnl_excr.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		pnl_excr.add(new JLabel("Excretion ratio Post-Mict"));
		// on centre les contenu du tableau
		for (JLabel l : lbls) {
			l.setHorizontalAlignment(JLabel.CENTER);
			pnl_excr.add(l);
		}

		return pnl_excr;
	}

	private String naIfNull(Double d) {
		if (d == null) {
			return "N/A";
		}
		return d + " %";
	}
}
