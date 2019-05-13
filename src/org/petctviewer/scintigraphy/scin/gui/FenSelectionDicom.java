package org.petctviewer.scintigraphy.scin.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.util.DicomTools;

/**
 * Window to select DICOM files. The list of DICOM files can add informations on
 * the files to be selected.
 * 
 * @author Titouan QUÉMA
 *
 */
public class FenSelectionDicom extends JFrame implements ActionListener, ImageListener {
	private static final long serialVersionUID = 6706629497515318270L;

	// TODO: make a column invisible to add data
	/**
	 * Represents a column for the selection table.
	 *
	 */
	public static class Column {
		public static final Column PATIENT = new Column("Patient"), STUDY = new Column("Study"),
				DATE = new Column("Date"), SERIES = new Column("Series"), DIMENSIONS = new Column("Dimensions"),
				STACK_SIZE = new Column("Stack Size"), ORIENTATION, ROW = new Column("Index", null, false);
		static {
			String[] s = Orientation.allOrientations();
			ORIENTATION = new Column("Orientation", s);
		}

		private String name;
		private String[] authorizedValues;
//		private boolean visible;

		/**
		 * Creates a new column with the specified name. The authorized values are
		 * displayed as a list for the user to choose from. <br>
		 * If the authorized value is empty, then no value will be authorized. If the
		 * authorized value is null, then all values will be authorized.
		 * 
		 * @param name             Name of the column (shown in the header of the table)
		 * @param authorizedValues Possible values for the user to choose from. Null
		 *                         means all values authorized. Empty means no values
		 *                         authorized
		 * @param visible          TRUE if the column should be visible and FALSE if the
		 *                         column should be hidden
		 */
		public Column(String name, String[] authorizedValues, boolean visible) {
			this.name = name;
			this.authorizedValues = authorizedValues;
//			this.visible = visible;
		}

		/**
		 * Creates a new column with the specified name. The authorized values are
		 * displayed as a list for the user to choose from. <br>
		 * If the authorized value is empty, then no value will be authorized. If the
		 * authorized value is null, then all values will be authorized.
		 * 
		 * @param name             Name of the column (shown in the header of the table)
		 * @param authorizedValues Possible values for the user to choose from. Null
		 *                         means all values authorized. Empty means no values
		 *                         authorized
		 */
		public Column(String name, String[] authorizedValues) {
			this(name, authorizedValues, true);
		}

		/**
		 * Creates a new column with the specified name.
		 * 
		 * @param name Name of the column (shown in the header of the table)
		 */
		public Column(String name) {
			this(name, null);
		}

		public static final Column[] getDefaultColumns() {
			Column[] columns = new Column[7];
			columns[0] = Column.PATIENT;
			columns[1] = Column.STUDY;
			columns[2] = Column.DATE;
			columns[3] = Column.SERIES;
			columns[4] = Column.DIMENSIONS;
			columns[5] = Column.STACK_SIZE;
			columns[6] = Column.ORIENTATION;
			return columns;
		}

		/**
		 * Checks if the specified value matches any of the authorized value defined by
		 * the column. If no authorized values are defined, then this method will always
		 * return TRUE.
		 * 
		 * @param value Value to check
		 * @return TRUE if the value matches at least one authorized value or if there
		 *         is no authorized value defined and FALSE otherwise
		 */
		public boolean isAuthorizedValue(String value) {
			if (this.authorizedValues == null)
				return true;
			for (String s : this.authorizedValues)
				if (s.equals(value))
					return true;
			return false;
		}

		/**
		 * Checks if this column has defined authorized values.
		 * 
		 * @return TRUE if authorized values are defined and FALSE otherwise
		 */
		public boolean hasAuthorizedValues() {
			return this.authorizedValues != null;
		}

		/**
		 * @return array of authorized values (this method can return null if there is
		 *         no restriction on the values)
		 */
		public String[] getAuthorizedValues() {
			return this.authorizedValues;
		}

		/**
		 * @return name of the column (displayed in the header of the table)
		 */
		public String getName() {
			return this.name;
		}

//		/**
//		 * @return TRUE if the column is visible and FALSE otherwise
//		 */
//		public boolean isVisible() {
//			return this.visible;
//		}
	}

	private JButton btn_select, btn_selectAll;
	private Scintigraphy scin;
	private DefaultTableModel dataModel;
	protected JTable table;

	private List<Column> columns;

	/**
	 * Permet de selectionner les dicom utilisees par le plugin
	 * 
	 * @param examType : type d'examen
	 * @param scin     : scintigraphie a demarrer quand les dicoms sont selectionnes
	 */
	public FenSelectionDicom(String examType, Scintigraphy scin) {
		this.scin = scin;
		ImagePlus.addImageListener(this);
		this.columns = new ArrayList<>();

		// on ajoute le titre a la fenetre
		this.setTitle("Select Series");

		// creation du tableau
		table = new JTable();
		// use default columns
		this.declareColumns(Column.getDefaultColumns());

		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setFocusable(false);
		table.setDefaultEditor(Object.class, null);

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());

		JScrollPane tablePane = new JScrollPane(table);

		JPanel jp = new JPanel();

		this.btn_select = new JButton("Select");
		this.btn_select.addActionListener(this);

		this.btn_selectAll = new JButton("Select All");
		this.btn_selectAll.addActionListener(this);

		panel.add(tablePane, BorderLayout.CENTER);

		panel.add(new JLabel("Select the dicoms for " + examType), BorderLayout.NORTH);

		jp.add(this.btn_select);
		jp.add(this.btn_selectAll);

		panel.add(jp, BorderLayout.SOUTH);

		this.add(panel);
		this.setPreferredSize(new Dimension(500, 500));
		this.pack();
		this.setLocationRelativeTo(null);
	}

	public void declareColumns(Column[] columns) {
		this.columns = Arrays.asList(columns);
		String[] columnsName = new String[this.columns.size()];

		// Create model
		this.dataModel = new DefaultTableModel(columnsName, 0);
		this.updateTable();
		this.table.setModel(this.dataModel);

		for (int i = 0; i < this.columns.size(); i++) {
			Column col = this.columns.get(i);
			columnsName[i] = col.getName();
			TableColumn manifacturer = this.table.getColumnModel().getColumn(i);
			manifacturer.setHeaderValue(columnsName[i]);
			if (col.hasAuthorizedValues()) {
				DefaultCellEditor celleditor = new DefaultCellEditor(new JComboBox<String>(col.authorizedValues));
				manifacturer.setCellEditor(celleditor);
			}
		}
		resizeColumnWidth(table);
	}

	// TODO: Do not assume that index of rows matches the ID of the image in the
	// WindowManager!
	private List<String[]> getTableData() {
		List<String[]> data = new ArrayList<>(WindowManager.getImageCount());

		int[] idList = WindowManager.getIDList();
		if (idList != null) {
			int countErrors = 0;
			for (int idImgOpen = 0; idImgOpen < WindowManager.getIDList().length; idImgOpen++) {
				ImagePlus imp = WindowManager.getImage(WindowManager.getIDList()[idImgOpen]);
				HashMap<String, String> infosPatient = Library_Capture_CSV.getPatientInfo(imp);

				String[] imageData = new String[this.columns.size()];

				try {
					for (int i = 0; i < this.columns.size(); i++) {
						Column c = this.columns.get(i);
						if (c == Column.PATIENT) {
							imageData[i] = infosPatient.get("name");
						} else if (c == Column.STUDY) {
							imageData[i] = replaceNull(DicomTools.getTag(imp, "0008,1030")).trim();
						} else if (c == Column.DATE) {
							imageData[i] = replaceNull(infosPatient.get("date"));
						} else if (c == Column.SERIES) {
							imageData[i] = replaceNull(DicomTools.getTag(imp, "0008,103E")).trim();
						} else if (c == Column.DIMENSIONS) {
							imageData[i] = imp.getDimensions()[0] + "x" + imp.getDimensions()[1];
						} else if (c == Column.STACK_SIZE) {
							imageData[i] = "" + imp.getStack().getSize();
						} else if (c.getName().equals(Column.ORIENTATION.getName())) {
							imageData[i] = determineImageOrientation(imp).toString();
						} else {
							imageData[i] = "CHOOSE VALUE";
						}
					}
					data.add(imageData);
				} catch (Exception e) {
					countErrors++;
				}
			}
			if (countErrors > 0)
				System.err.println(countErrors + " images could not be opened");
		}
		return data;
	}

	/**
	 * Determines the orientation of an ImagePlus.
	 * 
	 * TODO: Maybe move this method in a library???
	 * 
	 * @param imp Image to analyze
	 * @return Orientation of the image (UNKNOWN if the orientation could not be
	 *         determined)
	 */
	private Orientation determineImageOrientation(ImagePlus imp) {
		boolean sameCameraMultiFrame = Library_Dicom.isSameCameraMultiFrame(imp);
		Boolean firstImageAnt = Library_Dicom.isAnterieur(imp);

		if (imp.getStackSize() == 1 && firstImageAnt != null) {
			if (firstImageAnt) {
				return Orientation.ANT;
			} else {
				return Orientation.POST;
			}

		} else if (imp.getStackSize() == 2 && firstImageAnt != null) {
			if (firstImageAnt) {
				return Orientation.ANT_POST;
			} else {
				return Orientation.POST_ANT;
			}

		} else if (imp.getStackSize() > 2 && sameCameraMultiFrame && firstImageAnt != null) {
			if (firstImageAnt) {
				return Orientation.DYNAMIC_ANT;
			} else if (!firstImageAnt) {
				return Orientation.DYNAMIC_POST;
			}
		} else if (imp.getStackSize() > 2 && !sameCameraMultiFrame && firstImageAnt != null) {
			if (firstImageAnt) {
				return Orientation.DYNAMIC_ANT_POST;
			} else if (!firstImageAnt) {
				return Orientation.DYNAMIC_POST_ANT;
			}
		}
		return Orientation.UNKNOWN;
	}

	/**
	 * Replaces a null or empty string with 'N/A' annotation.
	 * 
	 * TODO: Maybe move this method in a library???
	 * 
	 * @param s String to replace
	 * @return 'N/A' if the string is null or empty otherwise returns the string
	 *         unchanged
	 */
	private static String replaceNull(String s) {
		if (s == null || s == "") {
			return "N/A";
		}
		return s;
	}

	private void resizeColumnWidth(JTable table) {
		final TableColumnModel columnModel = table.getColumnModel();
		for (int column = 0; column < table.getColumnCount(); column++) {
			int width = 15; // Min width
			for (int row = 0; row < table.getRowCount(); row++) {
				TableCellRenderer renderer = table.getCellRenderer(row, column);
				Component comp = table.prepareRenderer(renderer, row, column);
				width = Math.max(comp.getPreferredSize().width + 1, width);
			}
			if (width > 300)
				width = 300;
			columnModel.getColumn(column).setPreferredWidth(width);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		JButton b = (JButton) event.getSource();

		if (b == this.btn_selectAll) {
			// on selectionne toutes les fenetres
			table.selectAll();
		}

		ImageSelection[] selectedImages = this.getSelectedImages();

		try {

			this.checkForUnauthorizedValues(selectedImages);
			this.checkSamePatient(selectedImages);
			this.startExam(selectedImages);

		} catch (WrongColumnException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "", JOptionPane.ERROR_MESSAGE);
		} catch (WrongInputException e) {
			JOptionPane.showMessageDialog(this, "Selection aborted", "", JOptionPane.INFORMATION_MESSAGE);
		}

	}

	/**
	 * Checks that all selected rows have authorized value in all columns.
	 * 
	 * @throws WrongColumnException if a column has an unauthorized value
	 */
	private void checkForUnauthorizedValues(ImageSelection[] selectedImages) throws WrongColumnException {
		for (int i = 0; i < selectedImages.length; i++) {
			ImageSelection ims = selectedImages[i];
			for (Column column : this.columns) {
				String value = ims.getValue(column.getName());
				if (!column.isAuthorizedValue(value))
					throw new WrongColumnException(column, ims.getRow(), "The value " + value
							+ " is incorrect, it must be one of: " + Arrays.toString(column.getAuthorizedValues()));
			}
		}
	}

	/**
	 * Checks that all selected images are for the same patient (id and name). If
	 * not, the user can override the process.
	 * 
	 * @throws WrongInputException if selected images belong to multiple patient and
	 *                             the user do not override the process
	 */
	private void checkSamePatient(ImageSelection[] selectedImages) throws WrongInputException {
		Set<String> patientIds = new HashSet<>();
		Set<String> patientNames = new HashSet<>();

		for (ImageSelection ims : selectedImages) {
			HashMap<String, String> infoPatient = Library_Capture_CSV.getPatientInfo(ims.getImagePlus());
			patientIds.add(infoPatient.get(Library_Capture_CSV.PATIENT_INFO_ID));
			patientNames.add(infoPatient.get(Library_Capture_CSV.PATIENT_INFO_NAME));
		}

		int result = JOptionPane.YES_OPTION;
		if (patientIds.size() > 1 || patientNames.size() > 1) {
			String message = "Selected images might belong to different patient:\nDifferent IDs: " + patientIds
					+ "\nDifferent Names: " + patientNames + "\n\nWould you like to continue?";
			result = JOptionPane.showConfirmDialog(this, message, "Multiple patient found", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
		}

		if (result != JOptionPane.YES_OPTION)
			throw new WrongInputException("Images belong to multiple patients");
	}

	/**
	 * This method is used to get all of the images selected by the user.
	 * 
	 * @return images selected by the user
	 */
	private ImageSelection[] getSelectedImages() {
		int[] rows = this.table.getSelectedRows();
		ImageSelection[] selectedImages = new ImageSelection[rows.length];

		// ATTENTION NE PAS FAIRE DE HIDE OU DE CLOSE CAR DECLANCHE LE LISTENER
		// IMAGE PLUS DOIVENT ETRE DUPLIQUEE ET FERMEE DANS LES PROGRAMMES LANCES
		for (int i = 0; i < rows.length; i++) {
			int row = rows[i];
			// Generate values for the selection
			String[] values = new String[this.columns.size()];
			for (int col = 0; col < values.length; col++) {
				values[col] = (String) this.table.getValueAt(row, col);
			}
			// Generate columns array
			String[] columns = new String[this.columns.size()];
			for (int idCol = 0; idCol < columns.length; idCol++)
				columns[idCol] = this.columns.get(idCol).getName();
			selectedImages[i] = new ImageSelection(WindowManager.getImage(WindowManager.getIDList()[row]), columns,
					values);
			// TODO: do not add row here, use the invisible columns
			selectedImages[i].setRow(row + 1);
		}

		return selectedImages;
	}

	/**
	 * This method starts the exam by calling the <code>lancerProgramme()</code>
	 * method on {@link Scintigraphy}.
	 * 
	 * @param selectedImages Images selected by the user (at this point, they are
	 *                       not conform to the Controller's requirements)
	 */
	protected void startExam(ImageSelection[] selectedImages) {
		try {
			ImageSelection[] userSelection = this.scin.preparerImp(selectedImages);
			if (userSelection != null) {
				ImagePlus.removeImageListener(this);
				this.dispose();
				this.scin.lancerProgramme(userSelection);
			}
		} catch (WrongInputException e) {
			JOptionPane.showMessageDialog(this, "Error while selecting images:\n" + e.getMessage(), "Selection error",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	private void updateTable() {

		this.dataModel.setRowCount(0);
		for (String[] s : this.getTableData()) {
			this.dataModel.addRow(s);
		}
		resizeColumnWidth(table);

	}

	@Override
	public void imageOpened(ImagePlus imp) {
		this.updateTable();
	}

	@Override
	public void imageClosed(ImagePlus imp) {
		this.updateTable();
	}

	@Override
	public void imageUpdated(ImagePlus imp) {
	}
}
