package org.petctviewer.scintigraphy.os;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongOrientationException;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import ij.ImagePlus;

/**
 * DISCLAIMER : Dans cette application, il a été fait comme choix d'initialiser
 * le module par le biais du Contrôleur, qui va ensuite créer la vue et le
 * modèle.
 */
public class OsScintigraphy extends Scintigraphy {

	ImagePlus[][] buffer;
	public OsScintigraphy() {
		super("Scinti Os");
		this.process = true;
	}

	/**
	 * Lance la FenSelectionDicom qui permet de selectionner les images qui seront
	 * traité par ce plug-in.
	 * 
	 * @param selectedImages
	 *            liste des images transmises depuis FenSelectionDicom
	 * @return
	 */
	@Override
	public void run(String arg) {
		// SK FAIRE DANS UN AUTRE THREAD ?
		FenSelectionDicom fen = new FenSelectionDicom(this.getStudyName(), this);
		fen.setVisible(true);
		fen.pack();
	}

	/**
	 * Prépare les images à traiter.<br/>
	 * Dans ce plug-in, les images qui seront envoyées doivent être POST/ANT ou
	 * ANT/POST, au nombre maximum de 3<br/>
	 * Dans un premier temps, travaille les images pour mettre le ANT en slice 1 et
	 * le POST en slice 2.<br/>
	 * Tri ensuite les ImagePlus par date, avec la plus récente en première.<br/>
	 * Sépare ensuite les deux slice en deux ImagePlus, en transmettant les
	 * informations de l'image originale.<br/>
	 * Enregistre les images dans un buffer, qui sera transmis à la fenêtre traitant
	 * les images de Scinty Osseuse.<br/>
	 * Le buffer enregistré est un tableau à double dimension possédant 2 colonnes
	 * et n ligne (n = nombre de patient).<br/>
	 * Chaque ligne est un patient. <br/>
	 * La colonne 0 : l'ImagePlus ANT du patient --/-- la colonne 1 : l'ImagePlus
	 * POST du patient.<br/>
	 * 
	 * @param selectedImages
	 *            liste des images transmises depuis FenSelectionDicom
	 * @return
	 */
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {

		if (selectedImages.length > 3)
			throw new WrongNumberImagesException(selectedImages.length, 1, 3);
		buffer = new ImagePlus[selectedImages.length][2];

		ImagePlus impSorted = null;
		ImagePlus[] impsSortedAntPost = new ImagePlus[selectedImages.length];

		for (int i = 0; i < selectedImages.length; i++) { // Modifie l'ImagePlus pour mettre ANT en slice 1 et POST en
															// slice 2
			impSorted = null;
			ImagePlus imp = selectedImages[i].getImagePlus();
			if (selectedImages[i].getImageOrientation() == Orientation.ANT_POST) {
				impSorted = Library_Dicom.sortImageAntPost(imp);
			} else if (selectedImages[i].getImageOrientation() == Orientation.POST_ANT) {
				impSorted = Library_Dicom.sortImageAntPost(imp);
			} else if (selectedImages[i].getImageOrientation() == Orientation.POST) {
				impSorted = imp.duplicate();
			} else {
				throw new WrongOrientationException(selectedImages[i].getImageOrientation(),
						new Orientation[] { Orientation.ANT_POST, Orientation.POST_ANT, Orientation.POST });
			}

			impsSortedAntPost[i] = impSorted;
			selectedImages[i].getImagePlus().close();
		}

		ImagePlus[] impsSortedByTime = Library_Dicom.orderImagesByAcquisitionTime(impsSortedAntPost, true);

		/*
		 * 
		 * ArrayList<ImagePlus> arrayBufferForSortByTime = new
		 * ArrayList<ImagePlus>(Arrays.asList(impsSortedAntPost)); ImagePlus[]
		 * impsSortedByTime =
		 * Library_Dicom.orderImagesByAcquisitionTime(arrayBufferForSortByTime);
		 * 
		 * int reverseIndex = 0; int nbImpsSortedByTime = impsSortedByTime.length;
		 * ImagePlus tempImp;
		 * 
		 * for (reverseIndex = 0 ; reverseIndex < nbImpsSortedByTime / 2 ;
		 * reverseIndex++){ tempImp = impsSortedByTime[reverseIndex];
		 * impsSortedByTime[reverseIndex] = impsSortedByTime[nbImpsSortedByTime -
		 * reverseIndex - 1]; impsSortedByTime[nbImpsSortedByTime - reverseIndex - 1] =
		 * tempImp; }
		 */

		for (int i = 0; i < impsSortedByTime.length; i++) {
			for (int j = 0; j < 2; j++) {

				ImagePlus Ant = new ImagePlus("Ant", impsSortedByTime[i].getStack().getProcessor(1));
				Ant.setProperty("Info", impsSortedByTime[i].getStack().getSliceLabel(1));
				buffer[i][0] = Ant;

				ImagePlus Post = new ImagePlus("Post", impsSortedByTime[i].getStack().getProcessor(2));
				Post.setProperty("Info", impsSortedByTime[i].getStack().getSliceLabel(2));
				buffer[i][1] = Post;
			}
		}

		ArrayList<String> patientID = new ArrayList<>();
		ArrayList<String> patientName = new ArrayList<>();
		for (ImagePlus[] slctd : buffer) {
			HashMap<String, String> infoPatient = Library_Capture_CSV.getPatientInfo(slctd[0]);
			if (!patientID.contains(infoPatient.get("id"))) {
				patientID.add(infoPatient.get("id"));
				patientName.add(infoPatient.get("name"));
			}
		}

		if (patientID.size() > 1) {
			process = false;
			Fen_MultiplPatient fen = new Fen_MultiplPatient(patientID, patientID);
			fen.setModal(true);
			fen.setVisible(true);
			fen.setAlwaysOnTop(true);
			fen.setLocationRelativeTo(null);
		}

		ImageSelection[] selection = new ImageSelection[1];
		selection[0] = new ImageSelection(impSorted, null, null);
		return selection;

	}

	/**
	 * Créé un JFrame et la fenêtre qui traitera les images de scinty osseuse<br/>
	 * Cette fenêtre prend en paramètre la classe actuelle (dérivée de Scinty), et
	 * le buffer d'images.<br/>
	 * Le buffer enregistré est un tableau à double dimension possédant 2 colonnes
	 * et n ligne (n = nombre de patient)<br/>
	 * Chaque ligne est un patient. <br/>
	 * La colonne 0 : l'ImagePlus ANT du patient --/-- la colonne 1 : l'ImagePlus
	 * POST du patient.<br/>
	 * 
	 * @return
	 */
	public void lancerProgramme(ImageSelection[] selectedImages) {

		if (process) {
			// FenApplication_Os fen = new FenApplication_Os(this, buffer);

		Controleur_Os controleur_os = new Controleur_Os(buffer, this);
		FenApplication_Os fen = controleur_os.getFenApplicatio_Os();
		fen.setVisible(true);

			JFrame frame = new JFrame("Bone Scinthigraphy");
			frame.add(fen);
			frame.pack();
			frame.setVisible(true);
			frame.setResizable(true);
		}
	}

	private class Fen_MultiplPatient extends JDialog implements ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JButton btn_y, btn_n;

		public Fen_MultiplPatient(ArrayList<String> patientID, ArrayList<String> patientName) {

			JPanel flow = new JPanel(new GridLayout(4, 1));

			flow.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

			this.setTitle("Multiple patient ID");
			flow.add(this.add(new JLabel("There is more than one patient ID")));

			Object[][] donnees = new Object[patientID.size()][3];

			for (int i = 0; i < patientID.size(); i++) {
				donnees[i][0] = "Exam" + i;
				donnees[i][1] = patientID.get(i);
				donnees[i][2] = patientName.get(i);
			}

			String[] entetes = { "", "ID", "Name" };

			JTable tableau = new JTable(donnees, entetes);

			JPanel tableContainer = new JPanel(new BorderLayout());
			tableContainer.add(tableau.getTableHeader(), BorderLayout.NORTH);
			tableContainer.add(tableau, BorderLayout.CENTER);

			flow.add(tableContainer);

			flow.add(this.add(new JLabel("Do you want to still process the exam ?")));

			JPanel radio = new JPanel();
			this.btn_y = new JButton("Yes");
			this.btn_y.addActionListener(this);
			radio.add(btn_y);
			this.btn_n = new JButton("No");
			this.btn_n.addActionListener(this);
			radio.add(btn_n);

			flow.add(radio);

			this.add(flow);

			this.setLocationRelativeTo(null);

			this.pack();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			JButton b = (JButton) arg0.getSource();
			if (b == this.btn_y) {
				process = true;
			} else if (b == this.btn_n) {
				process = false;
			}

			this.dispose();
		}

	}
}
