package org.petctviewer.scintigraphy.scin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import ij.ImagePlus;
import ij.WindowManager;
import ij.util.DicomTools;

public class FenSelectionDicom extends JDialog {

	JTable table;
	String[] titresDicoms;	
	String[] selectedWindowsTitles;
	JButton btn_select, btn_selectAll;

	public FenSelectionDicom(String title) {
		this.titresDicoms = WindowManager.getImageTitles();
		this.setTitle("Select the dicoms for the " + title + " exam");

		String[] columnNames = { "Patient", "Study", "Date", "Series", "Dimensions", "Stack Size" };
		String[][] tableData = getTableData();

		table = new JTable(tableData, columnNames);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		table.setFocusable(false);
		table.setDefaultEditor(Object.class, null);
		
		table.addMouseListener(new MouseAdapter() {
			
			private boolean[] rowSelection= new boolean[titresDicoms.length];
			
			@Override
			public void mousePressed(MouseEvent event) {
				table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			}
			
			@Override
			public void mouseReleased(MouseEvent event) {
				table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				JTable tab = (JTable) event.getSource();		
				int row = tab.rowAtPoint(event.getPoint());

				rowSelection[row] = !rowSelection[row];
				
				tab.clearSelection();
				
				for(int i = 0; i < rowSelection.length; i++) {
					if(rowSelection[i]) {
						tab.addRowSelectionInterval(i, i);
					}
				}
			}
			
		});

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JScrollPane tablePane = new JScrollPane(table);
		
		JPanel jp = new JPanel();
		
		controleurDialog ctrl = new controleurDialog();
		
		btn_select = new JButton("Select");
		btn_select.addActionListener(ctrl);
		
		btn_selectAll = new JButton("Select All");
		btn_selectAll.addActionListener(ctrl);

		panel.add(tablePane, BorderLayout.CENTER);
		
		jp.add(btn_select);
		jp.add(btn_selectAll);
		
		panel.add(jp, BorderLayout.SOUTH);
		
		this.add(panel);
		this.setPreferredSize(new Dimension(400, 200));
		this.setLocationRelativeTo(null);
		this.pack();
	}

	private String[][] getTableData() {
		String[][] data = new String[this.titresDicoms.length][6];
		for (int i = 0; i < this.titresDicoms.length; i++) {
			ImagePlus imp = WindowManager.getImage(titresDicoms[i]);
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

	private class controleurDialog implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton b = (JButton) e.getSource();
			
			if(b == FenSelectionDicom.this.btn_select) {
				int[] rows = table.getSelectedRows();
				String[] titresFenSelected = new String[rows.length];
				for (int i = 0; i < rows.length; i++) {
					titresFenSelected[i] = titresDicoms[rows[i]];
				}
				FenSelectionDicom.this.selectedWindowsTitles = titresFenSelected;
			}else if(b == FenSelectionDicom.this.btn_selectAll){
				FenSelectionDicom.this.selectedWindowsTitles = FenSelectionDicom.this.titresDicoms;
			}

			FenSelectionDicom.this.dispose();
		}
	}
	
	public String[] getSelectedWindowsTitles() {		
		return this.selectedWindowsTitles;
	}

	private String replaceNull(String s) {
		if (s == null) {
			return "";
		}
		return s;
	}

}
