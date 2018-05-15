package org.petctviewer.scintigraphy.scin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import ij.ImagePlus;
import ij.WindowManager;
import ij.util.DicomTools;

public class FenSelectionDicom extends JDialog {

	private static final long serialVersionUID = 6706629497515318270L;

	JTable table;
	String[] titresDicoms;	
	String[] selectedWindowsTitles;
	JButton btn_select, btn_selectAll;

	/**
	 * Permet de selectionner les dicom utilisees par le plugin
	 * @param examType : type d'examen
	 */
	public FenSelectionDicom(String examType) {
		this.titresDicoms = WindowManager.getImageTitles();
		
		//on ajoute le titre a la fenetre
		this.setTitle("Select the dicoms for the " + examType + " exam");

		//creation du tableeau
		String[] columnNames = { "Patient", "Study", "Date", "Series", "Dimensions", "Stack Size" };
		String[][] tableData = getTableData();

		this.table = new JTable(tableData, columnNames);
		this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.table.setFocusable(false);
		this.table.setDefaultEditor(Object.class, null);
		
		//listener personalise pour selectionner plusieurs dicoms sans utiliser ctrl
		this.table.addMouseListener(new MouseAdapter() {
			
			private boolean[] rowSelection= new boolean[FenSelectionDicom.this.titresDicoms.length];
			
			@Override
			public void mousePressed(MouseEvent event) {
				//on passe la selection en mode unique
				FenSelectionDicom.this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			}
			
			@Override
			public void mouseReleased(MouseEvent event) {
				FenSelectionDicom.this.table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				
				JTable tab = (JTable) event.getSource();		
				int row = tab.rowAtPoint(event.getPoint());

				//on selectionne on deselectionne la ligne dans le tableau de selection
				this.rowSelection[row] = !this.rowSelection[row];
				
				//on vide la selection du tableau
				tab.clearSelection();
				
				//on selectionne les lignes de la JTable selon le tableau de booleens
				for(int i = 0; i < this.rowSelection.length; i++) {
					if(this.rowSelection[i]) {
						tab.addRowSelectionInterval(i, i);
					}
				}
			}
			
		});

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JScrollPane tablePane = new JScrollPane(this.table);
		
		JPanel jp = new JPanel();
		
		controleurDialog ctrl = new controleurDialog();
		
		this.btn_select = new JButton("Select");
		this.btn_select.addActionListener(ctrl);
		
		this.btn_selectAll = new JButton("Select All");
		this.btn_selectAll.addActionListener(ctrl);

		panel.add(tablePane, BorderLayout.CENTER);
		
		jp.add(this.btn_select);
		jp.add(this.btn_selectAll);
		
		panel.add(jp, BorderLayout.SOUTH);
		
		this.add(panel);
		this.setPreferredSize(new Dimension(400, 200));
		this.setLocationRelativeTo(null);
		this.pack();
	}

	//renvoie les informations a afficher dans le tableau
	private String[][] getTableData() {
		String[][] data = new String[this.titresDicoms.length][6];
		for (int i = 0; i < this.titresDicoms.length; i++) {
			ImagePlus imp = WindowManager.getImage(this.titresDicoms[i]);
			HashMap<String, String> hm = ModeleScin.getPatientInfo(imp);

			data[i][0] = replaceNull(hm.get("name"));
			data[i][1] = replaceNull(DicomTools.getTag(imp, "0008,1030").trim());
			data[i][2] = replaceNull(hm.get("date"));
			data[i][3] = replaceNull(DicomTools.getTag(imp, "0008,103E").trim());
			data[i][4] = imp.getDimensions()[0] + "x" + imp.getDimensions()[1];
			data[i][5] = "" + imp.getStack().getSize();
		}
		return data;
	}

	//controleur prive pour les boutons "select" et "select all"
	private class controleurDialog implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton b = (JButton) e.getSource();
			
			if(b == FenSelectionDicom.this.btn_select) {
				//recuperation des lignes selectionnees
				int[] rows = FenSelectionDicom.this.table.getSelectedRows();
				
				//construction du tableau de nm de dicoms selectionnees
				String[] titresFenSelected = new String[rows.length];
				for (int i = 0; i < rows.length; i++) {
					titresFenSelected[i] = FenSelectionDicom.this.titresDicoms[rows[i]];
				}
				
				FenSelectionDicom.this.selectedWindowsTitles = titresFenSelected;
			}else if(b == FenSelectionDicom.this.btn_selectAll){
				//on selectionne toutes les fenetres
				FenSelectionDicom.this.selectedWindowsTitles = FenSelectionDicom.this.titresDicoms;
			}

			//on ferme la fenetre de selection
			FenSelectionDicom.this.dispose();
		}
	}
	
	public String[] getSelectedWindowsTitles() {		
		return this.selectedWindowsTitles;
	}

	//si le string est null, on renvoie un string vide
	private static String replaceNull(String s) {
		if (s == null) {
			return "";
		}
		return s;
	}

}
