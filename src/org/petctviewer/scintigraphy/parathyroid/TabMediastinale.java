package org.petctviewer.scintigraphy.parathyroid;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

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

import ij.ImagePlus;


public class TabMediastinale extends TabContrastModifier implements ActionListener {
    
	private JButton btn_addImp;
	private boolean imgSelected;
	private ImageSelection[] images;

	public TabMediastinale(FenResults parent) {
		super(parent, "Mediastinale");
		this.imgSelected = false;

		this.reloadDisplay();
	}


	@Override
	public Component getSidePanelContent() {

		Box side = Box.createVerticalBox();
		JPanel flow = new JPanel();

        JPanel panel_excr = new JPanel();
		flow.add(panel_excr);
		side.add(flow);
		JPanel panel_bladder = new JPanel();
		side.add(panel_bladder);

		

		// Simulate a \n
		side.add(new JLabel(""));

		if (this.imgSelected) {
			btn_addImp.setVisible(false);
			side.add(super.getSidePanelContent());
		}
		return side;
	}

	@Override
	public JPanel getResultContent() {
		if (!this.imgSelected) {
			Box box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());

			btn_addImp = new JButton("Choose mediastinal dicom");
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
					return "Mediastinale";
				}

				@Override
				public Column[] getColumns() {
					return Column.getDefaultColumns();
				}

				@Override
				public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages) throws
						WrongInputException {
					// Check number of images
					if (selectedImages.size() != 1) {
						throw new WrongNumberImagesException(selectedImages.size(), 1);
					}

					List<ImageSelection> selections = new ArrayList<>();

					// Check orientation and prepare image
					if (selectedImages.get(0).getImageOrientation() == Orientation.ANT) {
						selections.add(selectedImages.get(0).clone());
					} else {
						throw new WrongOrientationException(selectedImages.get(0).getImageOrientation(),
															new Orientation[]{Orientation.ANT});
					}

					selectedImages.get(0).close();

                    TabMediastinale.this.imgSelected = true;
                    
					ImagePlus temp = Library_Dicom.resize(selections.get(0).getImagePlus(), 512, 512);
					selections.get(0).getImagePlus().close();
					selections.get(0).setImagePlus(temp);
					Library_Gui.setCustomLut(selections.get(0).getImagePlus());
					
					Library_Gui.initOverlay(selections.get(0).getImagePlus());
					Library_Gui.setOverlayTitle("Ant", selections.get(0).getImagePlus(), Color.YELLOW, 1);
					Library_Gui.setOverlayGD(selections.get(0).getImagePlus(), Color.YELLOW);
					TabMediastinale.this.setImage(selections.get(0).getImagePlus());

					return selections;
				}

				@Override
				public String instructions() {
					return "1 image in Ant orientation";
				}

				@Override
				public void start(List<ImageSelection> preparedImages) {
					images = preparedImages.toArray(new ImageSelection[0]);
				}
			});
			fen.setVisible(true);

		}
	}



}