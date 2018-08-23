package org.petctviewer.scintigraphy.scin;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

public abstract class Scintigraphy implements PlugIn {
	
	private String examType;
	private FenApplication fen_application;

	private ImagePlus imp;
	protected int nombreAcquisitions;

	protected Scintigraphy(String examType) {
		this.examType = examType;
	}

	/*************************** Public ************************/
	/**
	 * Lance la fen�tre de dialogue permettant le lancemet du programme. </br>
	 * passer "standby" en parametre pour ne pas executer l'application directement
	 */
	@Override
	public void run(String arg) {
		FenSelectionDicom fen = new FenSelectionDicom(this.getExamType(), this);
		fen.setVisible(true);
	}

	public void startExam(ImagePlus[] images) {
		if (images != null) {
			if (images.length > 0) {
				this.imp = preparerImp(images);
				// on set la lut des preferences
				StaticMethod.setCustomLut(imp);

				this.lancerProgramme();
			}
		}
	}

	
	

	/*************************** Abstract ************************/
	/**
	 * Prepare l'image plus selon les fenetres de dicoms ouvertes
	 * 
	 * @param titresFenetres
	 *            liste des fenetres ouvertes
	 * @return
	 */
	protected abstract ImagePlus preparerImp(ImagePlus[] images);

	/**
	 * lance le programme
	 */
	public abstract void lancerProgramme();
	
	
	
	/*********************** Setter ******************/
	public void setImp(ImagePlus imp) {
		this.imp = imp;
	}
	
	/**
	 * Prepare le bouton capture de la fenetre resultat
	 * 
	 * @param btn_capture
	 *            le bouton capture, masque lors de la capture
	 * @param show
	 *            le label de credits, affiche lors de la capture
	 * @param cont
	 *            la jframe
	 * @param modele
	 *            le modele
	 * @param additionalInfo
	 *            string a ajouter a la fin du nom de la capture si besoin
	 */
	public void setCaptureButton(JButton btn_capture, Component[] show, Component[] hide, Component cont,
			ModeleScin modele, String additionalInfo) {

		String examType = this.getExamType();

		// generation du tag info
		String info = modele.genererDicomTagsPartie1SameUID(this.getImp(), this.getExamType())
				+ StaticMethod.genererDicomTagsPartie2(this.getImp());

		// on ajoute le listener sur le bouton capture
		btn_capture.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				for (Component comp : hide) {
					comp.setVisible(false);
				}

				for (Component comp : show) {
					comp.setVisible(true);
				}

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						Container root = StaticMethod.getRootContainer(cont);

						// Capture, nouvelle methode a utiliser sur le reste des programmes
						BufferedImage capture = new BufferedImage(root.getWidth(), root.getHeight(),
								BufferedImage.TYPE_INT_ARGB);
						root.paint(capture.getGraphics());
						ImagePlus imp = new ImagePlus("capture", capture);

						for (Component comp : hide) {
							comp.setVisible(true);
						}

						for (Component comp : show) {
							comp.setVisible(false);
						}

						//TODO garder cette partie ?
						//if (root instanceof Window) // on ferme la fenetre
						//	((Window) root).dispose();

						// on passe a la capture les infos de la dicom
						imp.setProperty("Info", info);
						// on affiche la capture
						imp.show();

						// on change l'outil
						IJ.setTool("hand");

						// generation du csv
						String resultats = modele.toString();

						try {
							StaticMethod.exportAll(resultats, getFenApplication().getControleur().getRoiManager(),
									examType, imp, additionalInfo);

							getFenApplication().getControleur().getRoiManager().close();

							imp.killRoi();
						} catch (Exception e1) {
							e1.printStackTrace();
						}

						// Execution du plugin myDicom
						try {
							IJ.run("myDicom...");
						} catch (Exception e1) {
							e1.printStackTrace();
						}

						Scintigraphy.this.fen_application.windowClosing(null);
						System.gc();
					}
				});

			}
		});
	}
	
	/**
	 * permet de preparer le bouton de capture de la frame.
	 * 
	 * @param btn_capture
	 * @param lbl_credits
	 * @param cont
	 * @param modele
	 * @param additionalInfo
	 */
	public void setCaptureButton(JButton btn_capture, Component lbl_credits, Container cont, ModeleScin modele,
			String additionalInfo) {
		setCaptureButton(btn_capture, new Component[] { lbl_credits }, new Component[] { btn_capture }, cont, modele,
				additionalInfo);
	}
	
	public void setExamType(String examType) {
		this.examType = examType;
	}
	
	public void setFenApplication(FenApplication fen_application) {
		this.fen_application = fen_application;
	}
	
	
	/********************** Getter **************************/
	public ImagePlus getImp() {
		return this.imp;
	}

	public String getExamType() {
		return this.examType;
	}

	public FenApplication getFenApplication() {
		return this.fen_application;
	}
	
	
	

	
}
