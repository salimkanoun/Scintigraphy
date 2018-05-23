package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Component;
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

public class TabPostMict extends FenResultatImp implements ActionListener{

	private static final long serialVersionUID = 8125367912250906052L;

	private int width, height;
	
	public TabPostMict(VueScin vueScin, int widthTab, int heightTab) {
		super("Renal Scintigraphy", vueScin, null, "");
		
		this.pack();
		this.width = (int) (widthTab - this.getSide().getSize().getWidth());
		this.height = heightTab;
		
		JButton btn_addImp = new JButton("Choose post-micturition dicom");
		btn_addImp.addActionListener(this);
		
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(btn_addImp);
		box.add(Box.createHorizontalGlue());
		
		this.add(box, BorderLayout.CENTER);
		
		this.finishBuildingWindow(false);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		FenSelectionDicom fs = new FenSelectionDicom("post-micturition");
		fs.setModal(true);
		fs.setVisible(true);
		
		String[] nomFens = fs.getSelectedWindowsTitles();
		
		if(nomFens != null) {
			if(nomFens.length > 1) {
				//on previent l'utilisateur
				JOptionPane.showConfirmDialog(this, "WARNING too many dicom selected", "Please select only one dicom", JOptionPane.WARNING_MESSAGE, JOptionPane.OK_OPTION);
			}else {
				ImagePlus imp = WindowManager.getImage(nomFens[0]).duplicate();
				this.setImp(imp);
				
				ImageProcessor impc = imp.getProcessor();
				
				int h = this.height;
				int w = (int) (impc.getWidth() * (this.height/(impc.getHeight() * 1.0)));
				impc.setInterpolationMethod(ImageProcessor.BICUBIC);
				imp.setProcessor(impc.resize(w, h));
				
				imp.close();
				JButton b = (JButton) arg0.getSource();
				b.setVisible(false);
				this.finishBuildingWindow(true);
			}
		}
	}

}
