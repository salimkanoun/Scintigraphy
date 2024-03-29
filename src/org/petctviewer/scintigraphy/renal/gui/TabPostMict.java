package org.petctviewer.scintigraphy.renal.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.renal.Model_Renal;
import org.petctviewer.scintigraphy.renal.postMictional.Model_PostMictional;
import org.petctviewer.scintigraphy.renal.postMictional.PostMictional;
import org.petctviewer.scintigraphy.scin.ImagePreparator;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongOrientationException;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.gui.TabContrastModifier;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabRenal;

import ij.ImagePlus;
import ij.Prefs;
import ij.util.DicomTools;

public class TabPostMict extends TabContrastModifier implements ActionListener {
	private final boolean bladder;
	private JButton btn_addImp, btn_quantify;
	private boolean imgSelected;
	private ImageSelection[] images;

	private JPanel panel_excr, panel_bladder;
	private boolean examDone;
	private Model_PostMictional modelPostMictional;

	public TabPostMict(FenResults parent) {
		super(parent, "Post Mictional");
		this.bladder = Prefs.get(PrefTabRenal.PREF_BLADDER, true);
		this.imgSelected = false;
		this.examDone = false;

		this.reloadDisplay();
	}

	private String[] createOrgans() {
		Model_Renal modele = (Model_Renal) this.parent.getModel();

		// ajout des organes a delimiter selon le nombre de rein du patient
		List<String> organes = new ArrayList<>();
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

	private void inflatePanelExcr(Double rg, Double rd) {
		Model_Renal modele = (Model_Renal) this.parent.getModel();
		Double[][] excr = modele.getExcrPM(rg, rd);

		// elements du tableau
		JLabel[] lbls = new JLabel[]{new JLabel("L"), new JLabel("R"), new JLabel("Max"), new JLabel(
				"" + naIfNull(excr[0][0])), new JLabel("" + naIfNull(excr[1][0])), new JLabel(
				"" + Library_Quantif.round(modele.getAdjustedValues().get("lasilix") - 1, 1) + " min"), new JLabel(
				"" + naIfNull(excr[0][1])), new JLabel("" + naIfNull(excr[1][1])),};

		// panel excr
		this.panel_excr.removeAll();
		this.panel_excr.setLayout(new GridLayout(3, 3, 0, 3));
		this.panel_excr.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		this.panel_excr.add(new JLabel("Excretion ratio Post-Mict"));
		// on centre les contenu du tableau
		for (JLabel l : lbls) {
			l.setHorizontalAlignment(JLabel.CENTER);
			this.panel_excr.add(l);
		}
	}

	private String naIfNull(Double d) {
		if (d == null) {
			return "N/A";
		}
		return d + " %";
	}

	@Override
	public Component getSidePanelContent() {

		Box side = Box.createVerticalBox();
		JPanel flow = new JPanel();

		this.panel_excr = new JPanel();
		flow.add(this.panel_excr);
		side.add(flow);
		this.panel_bladder = new JPanel();
		side.add(this.panel_bladder);

		this.btn_quantify = new JButton("Quantify");
		this.btn_quantify.addActionListener(this);
		side.add(btn_quantify);
		
		this.setComponentToHide((new ArrayList<> (Collections.singletonList(btn_quantify))));

		// Simulate a \n
		side.add(new JLabel(""));

		if (!this.imgSelected) {
			this.btn_quantify.setVisible(false);

			return side;
		} else if (examDone) {
			return updateResultFrame(this.modelPostMictional);
		} else {
			btn_addImp.setVisible(false);
			btn_quantify.setVisible(true);
			side.add(super.getSidePanelContent());

			return side;
		}
	}

	@Override
	public JPanel getResultContent() {
		if (!this.imgSelected) {
			Box box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());

			btn_addImp = new JButton("Choose post-mictional dicom");
			btn_addImp.addActionListener(this);
			box.add(btn_addImp);
			box.add(Box.createHorizontalGlue());

			JPanel pan = new JPanel();
			pan.add(box);
			return pan;
		} else return super.getResultContent();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == this.btn_addImp) {

			FenSelectionDicom fen = new FenSelectionDicom(new ImagePreparator() {
				@Override
				public String getStudyName() {
					return "Post-mictional";
				}

				@Override
				public Column[] getColumns() {
					return Column.getDefaultColumns();
				}

				@Override
				public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages) throws
						WrongInputException, ReadTagException {
					// Check number of images
					if (selectedImages.size() != 1) {
						throw new WrongNumberImagesException(selectedImages.size(), 1);
					}

					List<ImageSelection> selections = new ArrayList<>();

					// Check orientation and prepare image
					if (selectedImages.get(0).getImageOrientation() == Orientation.ANT_POST || selectedImages.get(
							0).getImageOrientation() == Orientation.POST_ANT) {
						selections.add(Library_Dicom.splitAntPost(selectedImages.get(0))[1]);
					} else if (selectedImages.get(0).getImageOrientation() == Orientation.POST) {
						selections.add(selectedImages.get(0).clone());
					} else {
						throw new WrongOrientationException(selectedImages.get(0).getImageOrientation(),
															new Orientation[]{Orientation.ANT_POST,
																			  Orientation.POST_ANT, Orientation.POST});
					}

					selectedImages.get(0).close();

					TabPostMict.this.imgSelected = true;
					
//					IJ.run(selections.get(0).getImagePlus(), "Size...",
//							   "width=" + 512 + " height=" + 512 + " depth=2 constrain average " +
//									   "interpolation=Bilinear");
					
					
					ImagePlus temp = Library_Dicom.resize(selections.get(0).getImagePlus(), 512, 512);
					selections.get(0).getImagePlus().close();
					selections.get(0).setImagePlus(temp);
					Library_Gui.setCustomLut(selections.get(0).getImagePlus());
					
					Library_Gui.initOverlay(selections.get(0).getImagePlus());
					Library_Gui.setOverlayTitle("Post", selections.get(0).getImagePlus(), Color.YELLOW, 1);
					Library_Gui.setOverlayGD(selections.get(0).getImagePlus(), Color.YELLOW);
					TabPostMict.this.setImage(selections.get(0).getImagePlus());

					return selections;
				}

				@Override
				public String instructions() {
					return "1 image in Ant-Post (or Post-Ant) or Post orientation";
				}

				@Override
				public void start(List<ImageSelection> preparedImages) {
					images = preparedImages.toArray(new ImageSelection[0]);
				}
			});
			fen.setVisible(true);

		} else if (arg0.getSource().equals(this.btn_quantify)) {
			// SK A REVOIR
			PostMictional vueBasic = new PostMictional(createOrgans(), this);
			try {
				vueBasic.start(vueBasic.prepareImages(Arrays.asList(TabPostMict.this.images)));
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public JPanel updateResultFrame(Model_PostMictional model) {
		Model_Renal modele = (Model_Renal) this.parent.getModel();

		HashMap<String, Double> data = model.getData();

		Double rg = null, rd = null;
		int duration = Integer.parseInt(DicomTools.getTag(this.getAssociatedImage(), "0018,1242").trim());
		if (modele.getKidneys()[0]) {
			rg = data.get("L. Kidney") - data.get("L. bkg");
			// on calcule les valeurs en coups/sec
			rg /= (duration / 1000.);
		}
		if (modele.getKidneys()[1]) {
			rd = data.get("R. Kidney") - data.get("R. bkg");
			// on calcule les valeurs en coups/sec
			rd /= (duration / 1000.);
		}

		// creation du panel excr rein gauche et droit
		this.inflatePanelExcr(rg, rd);

		// ajout de la vessie dans la liste d'organes si elle est selectionnee
		if (bladder) {
			Double bld = data.get("Bladder");
			bld /= (duration / 1000.);
			this.panel_bladder.add(
					new JLabel("Bladder : " + Library_Quantif.round(modele.getExcrBladder(bld), 2) + " %"));
		}

		JPanel flow = new JPanel(new GridLayout(3, 1));
		flow.add(panel_excr);
		// Equivalent to a \n
		flow.add(new JLabel(""));
		flow.add(super.getSidePanelContent());
		return flow;
	}

	public void setExamDone(boolean boobool) {
		this.examDone = boobool;
	}

	public void setModelPostMictional(Model_PostMictional model) {
		this.modelPostMictional = model;
	}

}