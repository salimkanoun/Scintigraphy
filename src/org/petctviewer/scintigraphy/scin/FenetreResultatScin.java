package org.petctviewer.scintigraphy.scin;

import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import ij.ImagePlus;

public class FenetreResultatScin extends JFrame{
	
	public static void setCaptureButton(JButton btn_capture, JLabel lbl_credits, VueScin vue, JFrame jf) {

		btn_capture.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JButton b = (JButton) (e.getSource());
				b.setVisible(false);
				lbl_credits.setVisible(true);
				
				jf.pack();
				Container c = jf.getContentPane();

				// Capture, nouvelle methode a utiliser sur le reste des programmes
				BufferedImage capture = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
				c.paint(capture.getGraphics());
				ImagePlus imp = new ImagePlus("capture", capture);
				
				imp.setProperty("Info", ModeleScin.genererDicomTagsPartie1(vue.getImp(), vue.getExamType())
						+ ModeleScin.genererDicomTagsPartie2(vue.getImp()));

				imp.show();
				
				String[] arrayRes = vue.getFen_application().getControleur().getModele().getResultsAsArray();

				try {
					ModeleScin.exportAll(arrayRes, 2, vue.getFen_application().getControleur().getRoiManager(),
							vue.getExamType(), imp);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}

				jf.dispose();
				
				vue.getFen_application().getControleur().getRoiManager().close();
			}
		});
	}
	
}
