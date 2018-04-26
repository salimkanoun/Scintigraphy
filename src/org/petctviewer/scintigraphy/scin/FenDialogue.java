package org.petctviewer.scintigraphy.scin;

import ij.WindowManager;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FenDialogue extends Frame {
	private static final long serialVersionUID = 7249861393425869097L;
	private Label lbl_message;
	private Button btn_valider;
	private VueScin vue;

	/**
	 * Cree et ouvre ue fenetre de dialogue demandant a l'utilisateur d'ouvrir toutes les dicom a traiter
	 * @param examType Libell� de l'examen de scintigraphie
	 * @param vue vue de ce type d'examen, appelle la methode {@link VueScin#ouvertureImage(String[])} a l'appui du bouton valider
	 */
	public FenDialogue(String examType, VueScin vue) {
		this.vue = vue;

		Panel pan = new Panel();
		pan.setLayout(new GridLayout(2, 1));
		initBtnValider();
		this.lbl_message = new Label();
		this.lbl_message.setText("Please open the " + examType + " image(s) then confirm.");
		pan.add(this.lbl_message);
		pan.add(this.btn_valider);
		add(pan);

		setLocationRelativeTo(null);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(200, 300);
		pack();
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
		setVisible(true);
		setResizable(false);

		setAlwaysOnTop(true);
		toFront();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.gc();
				FenDialogue.this.dispose();
			}
		});
	}

	private void initBtnValider() {
		this.btn_valider = new Button("Confirm");
		this.btn_valider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (WindowManager.getCurrentImage() != null) {
					FenDialogue.this.dispose();
					String[] titresFenetres = WindowManager.getImageTitles();
					//new FenSelectionDicom();
					//FenDialogue.this.vue.ouvertureImage(titresFenetres);
				} else {
					System.out.println("Pas de dicom ouverte");
				}
			}
		});
	}
}
