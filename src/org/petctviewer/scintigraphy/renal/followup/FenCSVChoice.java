package org.petctviewer.scintigraphy.renal.followup;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FenCSVChoice extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final ArrayList<String> chemins;
	private final JPanel affichageChemin;

	public FenCSVChoice() {

		this.chemins = new ArrayList<>();
		
		this.setTitle("Renal Follow-Up");
		this.setLayout(new BorderLayout());
		this.setSize(400, 400);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		
		JButton ajout = new JButton("Add CSV file");
		ajout.addActionListener(e -> {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setDialogTitle("Select CSV File");
			fc.addChoosableFileFilter(new FileNameExtensionFilter("CSV Documents", "csv"));
			fc.setAcceptAllFileFilterUsed(true);

			if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
		    {
				if(ajouterChemin(fc.getSelectedFile().getAbsolutePath())) {
					ajouterLabelChemin(fc.getSelectedFile().getAbsolutePath());
				}
		    }
		});
		
		this.affichageChemin =  new JPanel();
		affichageChemin.setLayout(new GridLayout(3,1));
		this.add(ajout, BorderLayout.NORTH);
		this.add(affichageChemin,BorderLayout.CENTER);
		
		JButton tracer = new JButton("Draw");	
		tracer.addActionListener(e -> {
			FenApplication_FollowUp fenCSV;
			fenCSV = new FenApplication_FollowUp(getChemins());
			fenCSV.setVisible(true);
		});
		this.add(tracer, BorderLayout.SOUTH);

	}
	
	// add a label with the new path
	private void ajouterLabelChemin(String nom) {
		this.affichageChemin.add(new JLabel("CSV : "+nom));
		this.revalidate();
		// bug
	}
	
	private ArrayList<String> getChemins() {
		return this.chemins;
	}
	
	//return true if CSV file meets conditions
	private boolean ajouterChemin(String chemin) {
		boolean res = false;
		// controle : qu'il n'y ait pas plus de NBMAXCSV = 3 CSV
		int NBMAXCSV = 3;
		if(this.chemins.size() >= NBMAXCSV) {
			JOptionPane.showMessageDialog(null,"Ajout de fichier CSV limité à "+ NBMAXCSV);
		}else {
			//controle : que l'id du patient soit le meme (non bloquant : choix)
			if(this.chemins.size() != 0 && !getIdPatient(this.chemins.get(this.chemins.size()-1)).equals(getIdPatient(chemin)) ) {
				int option = JOptionPane.showConfirmDialog(null, "Le patient n'est pas le même !\nVoulez-vous continuer ?","Attention",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
				if(option == JOptionPane.YES_OPTION) {
					this.chemins.add(chemin);
					res = true;
				}
			}else {
				this.chemins.add(chemin);
				res = true;
			}
		}
		return res;
	}
	
	//retourne l'id d'un patient pour un chemin de csv donné
	private String getIdPatient(String chemin) {
		String res ="";
		try {
			File file = new File(chemin);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			br.readLine();// lecture ligne 1 : studyName
			
			//lecture ligne 2 : id
			res = br.readLine().split(",")[1]; 
			
			br.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return res;
	}
}

