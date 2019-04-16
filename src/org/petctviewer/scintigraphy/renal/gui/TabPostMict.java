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
import org.petctviewer.scintigraphy.renal.postMictional.PostMictional;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.gui.PanelImpContrastSlider;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.shunpo.FenResults;

import ij.ImagePlus;
import ij.Prefs;
import ij.util.DicomTools;

public class TabPostMict extends PanelImpContrastSlider implements ActionListener  {

	private static final long serialVersionUID = 8125367912250906052L;
	private PostMictional vueBasic;
	private JButton btn_addImp, btn_quantify;
	private boolean bladder;

	private JPanel panel_excr, panel_bladder;

	public TabPostMict(Scintigraphy vue, FenResults parent) {
		super("Renal scintigraphy", vue, "postmict", parent);
		this.bladder = Prefs.get("renal.bladder.preferred", true);

		

		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		
		btn_addImp = new JButton("Choose post-mictional dicom");
		btn_addImp.addActionListener(this);
		box.add(btn_addImp);
		box.add(Box.createHorizontalGlue());
		this.add(box, BorderLayout.CENTER);

		
		
		Box side = Box.createVerticalBox();
		JPanel flow = new JPanel();
		
		this.panel_excr = new JPanel();
		flow.add(this.panel_excr);
		side.add(flow);
		
		this.panel_bladder = new JPanel();
		side.add(this.panel_bladder);

		this.btn_quantify = new JButton("Quantify");
		this.btn_quantify.addActionListener(this);
		this.btn_quantify.setVisible(false);
		side.add(btn_quantify);
		
//		sidePanel = new SidePanel(side, "Renal Scintigraphy1", parent.getImagePlus());

//		this.add(sidePanel, BorderLayout.EAST);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == this.btn_addImp) {
			
			// Open DICOM dialog Selection to select post mictional image
			//SK A REFACTORISER
			FenSelectionDicom fen = new FenSelectionDicom("Post-mictional", new Scintigraphy("") {
				@Override
				protected ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws Exception {
					if (selectedImages.length > 1) {
						throw new Exception("Only one serie is expected");
					}
					if(selectedImages[0].getImageOrientation()==Orientation.ANT_POST || selectedImages[0].getImageOrientation()==Orientation.POST_ANT || selectedImages[0].getImageOrientation()==Orientation.POST ) {
						//SK A GERER RECUPERER SEULE L IMAGE POST SI STATIC A/P ?
						ImagePlus imp=selectedImages[0].getImagePlus().duplicate();
						TabPostMict.this.setImp(imp);
						btn_addImp.setVisible(false);
						btn_quantify.setVisible(true);
//						sidePanel.add(boxSlider);
						parent.getPanel().setSidePanelContent(boxSlider);
						
						ImageSelection[] selection = new ImageSelection[1];
						selection[0] = new ImageSelection(imp, null, null);
						return selection;
					}else {
						throw new Exception("No Static Posterior Image");
					}

				}

				@Override
				public void lancerProgramme(ImageSelection[] selectedImages) {
				}
			});
			
			fen.setVisible(true);
			
		} else if(arg0.getSource().equals(this.btn_quantify)){
			//SK A REVOIR
			this.vueBasic = new PostMictional(createOrgans(), this);
			try {
				this.vueBasic.startExam(this.parent.getModel().getImageSelection());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}

	private String[] createOrgans() {
		Modele_Renal modele = (Modele_Renal) this.parent.getModel();

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

	public void updateResultFrame() {
		Modele_Renal modele = (Modele_Renal) this.parent.getModel();

		HashMap<String, Double> data = this.vueBasic.getData();
		this.setImp(this.parent.getModel().getImagePlus());

		Double rg = null, rd = null;
		int duration = Integer.parseInt(DicomTools.getTag(this.getImagePlus(), "0018,1242").trim());
		if (modele.getKidneys()[0]) {
			rg = data.get("L. Kidney") - data.get("L. bkg");
			// on calcule les valeurs en coups/sec
			rg /= (duration / 1000);
		}
		if (modele.getKidneys()[1]) {
			rd = data.get("R. Kidney") - data.get("R. bkg");
			// on calcule les valeurs en coups/sec
			rd /= (duration / 1000);
		}

		// creation du panel excr rein gauche et droit
		this.panel_excr = (JPanel) this.getPanelExcr(rg, rd);

		// ajout de la vessie dans la liste d'organes si elle est selectionnee
		if (bladder) {
			Double bld = data.get("Bladder");
			bld /= (duration / 1000);
			this.panel_bladder.add(new JLabel("Bladder : " + Library_Quantif.round(modele.getExcrBladder(bld), 2) + " %"));
		}

//		this.remove(this.sidePanel);
		
		JPanel flow = new JPanel();
		flow.add(panel_excr);
		this.parent.getPanel().setSidePanelContent(flow);

//		sidePanel = new SidePanel(flow, "Renal Scintigraphy2", this.getImagePlus());
//		sidePanel.addCaptureBtn(vueBasic, "_PostMict", new Component[] { this.getSlider() }, model);
		this.parent.createCaptureButton("_PostMict");
//		this.add(sidePanel,BorderLayout.EAST);
		this.revalidate();
		this.repaint();

		
	}

	

	

	private Component getPanelExcr(Double rg, Double rd) {
		Modele_Renal modele = (Modele_Renal) this.parent.getModel();
		Double[][] excr = modele.getExcrPM(rg, rd);

		// elements du tableau
		JLabel[] lbls = new JLabel[] { new JLabel("L"), new JLabel("R"), new JLabel("Max"),
				new JLabel("" + naIfNull(excr[0][0])), new JLabel("" + naIfNull(excr[1][0])),
				new JLabel("" + Library_Quantif.round(modele.getAdjustedValues().get("lasilix") - 1, 1) + " min"),
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
