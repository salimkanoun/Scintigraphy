package org.petctviewer.scintigraphy.shunpo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.frame.RoiManager;

public class FenResults extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final Font FONT_TITLE = new Font("Arial", Font.PLAIN, 25);

	private JLabel resultTitle;

	private String studyName;

	private JPanel panCenter, panResult;
	private SidePanel panEast;

	public FenResults() {
		this("Untitiled", "Unknown Study");
	}

	public FenResults(String resultTitle, String studyName) {
		this.setLocationRelativeTo(null);
		this.setTitle("Result for " + this.studyName);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.studyName = studyName;

		this.getContentPane().setLayout(new BorderLayout());

		// Center
		this.panCenter = new JPanel(new BorderLayout());
		this.panCenter.setAlignmentX(JPanel.CENTER_ALIGNMENT);

		this.resultTitle = new JLabel(resultTitle);
		this.resultTitle.setHorizontalAlignment(JLabel.CENTER);
		this.resultTitle.setFont(FONT_TITLE);
		panCenter.add(this.resultTitle, BorderLayout.NORTH);

		// Add components
		this.getContentPane().add(panCenter, BorderLayout.CENTER);
		this.pack();
	}

	public void setResultTitle(String title) {
		this.resultTitle.setText(title);
		this.pack();
	}

	public void setResult(JPanel panel) {
		if (this.panResult != null)
			this.panCenter.remove(this.panResult);
		this.panCenter.add(panel, BorderLayout.CENTER);
		this.panResult = panel;
		this.pack();
	}

	public void setComplement(String[] results) {
		JPanel panel = new JPanel(new GridLayout(results.length, 1));
		for (String row : results) {
			JLabel result = new JLabel(row);
			panel.add(result);
		}
		this.panEast.setSidePanelContent(panel);
		this.pack();
	}

	public void setInfos(SidePanel component) {
		if (this.panEast != null)
			this.getContentPane().remove(this.panEast);
		this.panEast = component;
		this.getContentPane().add(panEast, BorderLayout.EAST);
		this.pack();
	}

	public JButton createCaptureButton(ModeleScin model, Component[] hide, Component[] show,
			String additionalInfo) {
		JButton captureButton = new JButton();

		// generation du tag info
		String info = Library_Capture_CSV.genererDicomTagsPartie1(model.getImagesPlus()[0], this.studyName, model.getUID6digits())
				+ Library_Capture_CSV.genererDicomTagsPartie2(model.getImagesPlus()[0]);

		// on ajoute le listener sur le bouton capture
		captureButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				for (Component comp : hide)
					comp.setVisible(false);

				for (Component comp : show)
					comp.setVisible(true);

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						// Capture, nouvelle methode a utiliser sur le reste des programmes
						BufferedImage capture = new BufferedImage(FenResults.this.getWidth(),
								FenResults.this.getHeight(), BufferedImage.TYPE_INT_ARGB);
						FenResults.this.paint(capture.getGraphics());
						ImagePlus imp = new ImagePlus("capture", capture);

						for (Component comp : hide)
							comp.setVisible(true);

						for (Component comp : show)
							comp.setVisible(false);

						// on passe a la capture les infos de la dicom
						imp.setProperty("Info", info);
						// on affiche la capture
						imp.show();

						// on change l'outil
						IJ.setTool("hand");

						// generation du csv
						String resultats = model.toString();

						try {
							Library_Capture_CSV.exportAll(resultats, model.getRoiManager(), FenResults.this.studyName,
									imp, additionalInfo);

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

						System.gc();
					}
				});

			}
		});

		return captureButton;
	}

}
