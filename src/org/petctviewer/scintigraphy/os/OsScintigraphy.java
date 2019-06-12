package org.petctviewer.scintigraphy.os;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.ReversedChronologicalAcquisitionComparator;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * DISCLAIMER : Dans cette application, il a été fait comme choix d'initialiser le module par le biais du Contrôleur,
 * qui va ensuite créer la vue et le modèle.
 */
public class OsScintigraphy extends Scintigraphy {
	public static final String STUDY_NAME = "Bone Scintigraphy";
	private boolean process;

	public OsScintigraphy() {
		super(STUDY_NAME);
		this.process = true;
	}

	/**
	 * Lance la FenSelectionDicom qui permet de selectionner les images qui seront traité par ce plug-in.
	 */
	@Override
	public void run(String arg) {
		FenSelectionDicom fen = new FenSelectionDicom(this);
		fen.setVisible(true);
		fen.pack();
	}

	@Override
	public FenSelectionDicom.Column[] getColumns() {
		return FenSelectionDicom.Column.getDefaultColumns();
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages) throws WrongInputException {
		if (selectedImages.size() < 1 || selectedImages.size() > 3) throw new WrongNumberImagesException(
				selectedImages.size(), 1, 3);

		ImageSelection impSorted;
		List<ImageSelection> impsSortedAntPost = new ArrayList<>();

		for (ImageSelection imp : selectedImages) { // Modifie l'ImagePlus pour mettre ANT en slice 1 et POST en
			// slice 2
			if (imp.getImageOrientation() == Orientation.ANT_POST) {
				impSorted = Library_Dicom.ensureAntPostFlipped(imp);
			} else if (imp.getImageOrientation() == Orientation.POST_ANT) {
				impSorted = Library_Dicom.ensureAntPostFlipped(imp);
			} else if (imp.getImageOrientation() == Orientation.POST) {
				impSorted = imp.clone();
			} else {
				throw new WrongColumnException.OrientationColumn(imp.getRow(), imp.getImageOrientation(),
																 new Orientation[]{Orientation.ANT_POST,
																				   Orientation.POST_ANT,
																				   Orientation.POST});
			}

			impsSortedAntPost.add(impSorted);
			imp.getImagePlus().close();
		}

		impsSortedAntPost.sort(new ReversedChronologicalAcquisitionComparator());

		ArrayList<String> patientID = new ArrayList<>();
		ArrayList<String> patientName = new ArrayList<>();
		for (ImageSelection slctd : impsSortedAntPost) {
			HashMap<String, String> infoPatient = Library_Capture_CSV.getPatientInfo(slctd.getImagePlus());
			if (!patientID.contains(infoPatient.get("id"))) {
				patientID.add(infoPatient.get("id"));
				patientName.add(infoPatient.get("name"));
			}
		}

		if (patientID.size() > 1) {
			process = false;
			Fen_MultiplePatient fen = new Fen_MultiplePatient(patientID, patientName);
			fen.setModal(true);
			fen.setVisible(true);
			fen.setAlwaysOnTop(true);
			fen.setLocationRelativeTo(null);
		}

		return impsSortedAntPost;
	}

	@Override
	public String instructions() {
		return "1 to 3 images in Ant-Post (or Post-Ant) or Post orientation";
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
	 */
	public void start(ImageSelection[] selectedImages) {
		if (process) {
			// FenApplication_Os fen = new FenApplication_Os(this, buffer);

			Controleur_Os controleur_os = new Controleur_Os(selectedImages);
			FenApplication_Os fen = controleur_os.getFenApplicatio_Os();
			fen.setVisible(true);

			JFrame frame = new JFrame(this.getStudyName());
			frame.add(fen);
			frame.pack();
			frame.setVisible(true);
			frame.setResizable(true);
			frame.setLocationRelativeTo(null);

			try {
				frame.setIconImage(ImageIO.read(new File("images/icons/frameIconBis.png")));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class Fen_MultiplePatient extends JDialog implements ActionListener {
		private static final long serialVersionUID = 1L;
		private final JButton btn_y;
		private final JButton btn_n;

		public Fen_MultiplePatient(ArrayList<String> patientID, ArrayList<String> patientName) {

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

			String[] entetes = {"", "ID", "Name"};

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
