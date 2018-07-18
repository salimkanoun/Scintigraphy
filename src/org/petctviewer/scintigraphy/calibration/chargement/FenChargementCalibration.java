package org.petctviewer.scintigraphy.calibration.chargement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FenChargementCalibration extends JFrame {
	// manque securité

	// list of couple of image path and mask path
	// 1,2,3,4,5,6 rois for each sphere and 7 for background (noise)
	// [0] brut [1] mask
	private ArrayList<String[]> examList;

	private String path;
	private ControleurChargementCalibration ccc;

	private JPanel fen;

	private JLabel examText;
	private JLabel sphereText;
	
	public FenChargementCalibration() {

		this.examList = new ArrayList<String[]>();

		this.setTitle("Image selection");
		this.setLayout(new BorderLayout());

		String[] test = { "/Users/diego/Desktop/fantomes/Acqusition1/src_rec_apr_24_2018_float.nii",
				"/Users/diego/Desktop/fantomes/Acqusition1/src_rec_apr_24_2018_mask.nii" };
		// this.examList.add(test);

		String[] test1 = { "/Users/diego/Desktop/fantomes/Acquisition2/src_rec_apr_24_2018_float.nii",
				"/Users/diego/Desktop/fantomes/Acquisition2/src_rec_apr_24_2018_mask.nii" };
		this.examList.add(test1);

		String[] test2 = { "/Users/diego/Desktop/fantomes/Acquisition3/src_rec_apr_24_2018_float.nii",
				"/Users/diego/Desktop/fantomes/Acquisition3/src_rec_apr_24_2018_mask.nii" };
	//	 this.examList.add(test2);

		String[] test3 = { "/Users/diego/Desktop/fantomes/Acquisition4/src_rec_apr_24_2018_float.nii",
				"/Users/diego/Desktop/fantomes/Acquisition4/src_rec_apr_24_2018_mask.nii" };
	//	 this.examList.add(test3);

		String[] test4 = { "/Users/diego/Desktop/fantomes/Acquisition5/src_rec_apr_24_2018_float.nii",
				"/Users/diego/Desktop/fantomes/Acquisition5/src_rec_apr_24_2018_mask.nii" };
	//	 this.examList.add(test4);

		fen = createFileTab();
		this.add(fen, BorderLayout.CENTER);

		JButton lancer = new JButton("Run analysis");
		ccc = new ControleurChargementCalibration(this);
		lancer.addActionListener(ccc);

		JPanel south = new JPanel();
		south.add(lancer);
		 examText = new JLabel("");
		south.add(examText);
		
		 sphereText = new JLabel("");
		south.add(sphereText);
		
		this.add(south, BorderLayout.SOUTH);
		this.pack();
	}

	private void addPathBrut(String path) {
		String[] t = { path, null };
		this.examList.add(t);

		this.remove(this.fen);
		this.fen = createFileTab();
		this.add(this.fen);
		this.validate();
		this.pack();
	}

	private void addPathBrut(String path, int indice) {
		String[] t = { path, this.examList.get(indice)[1] };
		this.examList.remove(indice);
		this.examList.add(indice, t);

		this.remove(this.fen);
		this.fen = createFileTab();
		this.add(this.fen);
		this.validate();
		this.pack();
	}

	private void addPathMask(String path) {
		String[] t = { null, path };
		this.examList.add(t);

		this.remove(this.fen);
		this.fen = createFileTab();
		this.add(this.fen);
		this.revalidate();
		// this.repaint();
		this.pack();
	}

	private void addPathMask(String path, int indice) {
		String[] t = { this.examList.get(indice)[0], path };
		this.examList.remove(indice);
		this.examList.add(indice, t);

		this.remove(this.fen);
		this.fen = createFileTab();
		this.add(this.fen);
		this.revalidate();
		// this.repaint();
		this.pack();
	}

	private JButton addImageChooserButton() {
		JButton addImageFileButton = new JButton("Add image file");
		addImageFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setDialogTitle("Select Image File");

				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.addChoosableFileFilter(new FileNameExtensionFilter("nii documents", "nii"));
				fc.setAcceptAllFileFilterUsed(true);

				if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					addPathBrut(fc.getSelectedFile().getAbsolutePath());
				}
			}
		});
		return addImageFileButton;
	}

	private JButton addImageChooserButton(int indice) {

		JButton addImageFileButton = new JButton("Add image file");
		addImageFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setDialogTitle("Select Image File");

				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.addChoosableFileFilter(new FileNameExtensionFilter("nii documents", "nii"));
				fc.setAcceptAllFileFilterUsed(true);

				if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					addPathBrut(fc.getSelectedFile().getAbsolutePath(), indice);
				}
			}
		});
		return addImageFileButton;
	}

	private JButton addMaskChooserButton() {
		JButton addMaskFileButton = new JButton("Add Mask file");
		addMaskFileButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setDialogTitle("Select Image File");

				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.addChoosableFileFilter(new FileNameExtensionFilter("nii documents", "nii"));
				fc.setAcceptAllFileFilterUsed(true);

				if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					addPathMask(fc.getSelectedFile().getAbsolutePath());
				}
			}
		});
		return addMaskFileButton;
	}

	private JButton addMaskChooserButton(int indice) {
		JButton addMaskFileButton = new JButton("Add Mask file");
		addMaskFileButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setDialogTitle("Select Image File");

				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.addChoosableFileFilter(new FileNameExtensionFilter("nii documents", "nii"));
				fc.setAcceptAllFileFilterUsed(true);

				if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					addPathMask(fc.getSelectedFile().getAbsolutePath(), indice);
				}
			}
		});
		return addMaskFileButton;
	}

	/*****************/

	private JPanel createFileTab() {

		JPanel examSelectedPanel = new JPanel(new GridLayout(this.examList.size() + 2, 3));

		// title
		JLabel numLabel = new JLabel("N°");
		numLabel.setBackground(Color.LIGHT_GRAY);
		numLabel.setOpaque(true);
		examSelectedPanel.add(numLabel);

		JLabel imLabel = new JLabel("Image");
		imLabel.setBackground(Color.LIGHT_GRAY);
		imLabel.setOpaque(true);
		imLabel.setHorizontalAlignment(JLabel.CENTER);
		examSelectedPanel.add(imLabel);

		JLabel maskLabel = new JLabel("Mask");
		maskLabel.setBackground(Color.LIGHT_GRAY);
		maskLabel.setOpaque(true);
		maskLabel.setHorizontalAlignment(JLabel.CENTER);
		examSelectedPanel.add(maskLabel);

		//System.out.println(examList.size());
		for (int i = 0; i < examList.size(); i++) {

			JLabel numeroLabel = new JLabel((i+1) + "");
			JPanel d = new JPanel(new FlowLayout());
			d.add(numeroLabel);
			examSelectedPanel.add(d);

			if (this.examList.get(i)[0] == null) {
				examSelectedPanel.add(this.addImageChooserButton(i));
			} else {
				JLabel imagePathLabel = new JLabel(examList.get(i)[0]);
				examSelectedPanel.add(imagePathLabel);
			}

			if (this.examList.get(i)[1] == null) {
				examSelectedPanel.add(this.addMaskChooserButton(i));
			} else {
				JLabel maskPathLabel = new JLabel(examList.get(i)[1]);
				examSelectedPanel.add(maskPathLabel);
			}

		}
		// next
		JLabel numeroLabel = new JLabel(examList.size() + "");

		JPanel d = new JPanel(new FlowLayout());
		d.add(numeroLabel);
		examSelectedPanel.add(d);

		examSelectedPanel.add(this.addImageChooserButton());
		examSelectedPanel.add(this.addMaskChooserButton());

		JPanel fenCorrectSize = new JPanel(new FlowLayout());
		fenCorrectSize.add(examSelectedPanel);
		return fenCorrectSize;
	}

	public ArrayList<String[]> getExamList() {
		return this.examList;
	}

	public void setExamText(String text) {
	    examText.setText(text);
	    examText.revalidate();
	}
	
	public void setSphereText(String text) {
		sphereText.setText(text);;
		sphereText.revalidate();
	}
}
