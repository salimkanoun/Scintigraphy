package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.petctviewer.scintigraphy.scin.FenResultatImp;
import org.petctviewer.scintigraphy.scin.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.ImagePlus;
import ij.WindowManager;
import ij.process.ImageProcessor;

public class TabPostMict extends FenResultatImp implements ActionListener {

	private static final long serialVersionUID = 8125367912250906052L;

	private int width, height;

	public TabPostMict(VueScin vueScin, int w, int h) {
		super("Renal scintigraphy", vueScin, null, "");

		this.pack();
		this.width = (int) (w - this.getSide().getSize().getWidth());
		this.height = h;

		JButton btn_addImp = new JButton("Choose post-micturition dicom");
		btn_addImp.addActionListener(this);

		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(btn_addImp);
		box.add(Box.createHorizontalGlue());

		this.add(box, BorderLayout.CENTER);

		this.setPreferredSize(new Dimension(w, h));

		this.finishBuildingWindow(false);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		FenSelectionDicom fs = new FenSelectionDicom("post-micturition");
		fs.setModal(true);
		fs.setVisible(true);

		String[] nomFens = fs.getSelectedWindowsTitles();

		if (nomFens != null) {
			if (nomFens.length > 2) {
				// on previent l'utilisateur
				JOptionPane.showConfirmDialog(this, "WARNING too many dicom selected", "Please select only one dicom",
						JOptionPane.WARNING_MESSAGE, JOptionPane.OK_OPTION);
			} else {

				ImagePlus[] images = new ImagePlus[nomFens.length];
				for (int i = 0; i < nomFens.length; i++) {
					images[i] = WindowManager.getImage(nomFens[i]);
				}
				ImagePlus post = VueScin.splitAntPost(images)[1];

				this.setImp(post);

				ImageProcessor impc = post.getProcessor();

				int h = this.height;
				int w = (int) (impc.getWidth() * (this.height / (impc.getHeight() * 1.0)));
				impc.setInterpolationMethod(ImageProcessor.BICUBIC);
				post.setProcessor(impc.resize(w, h));

				for (ImagePlus imp : images) {
					imp.close();
				}
				
				JButton b = (JButton) arg0.getSource();
				b.setVisible(false);
				this.finishBuildingWindow(true);
			}
		}
	}

}
