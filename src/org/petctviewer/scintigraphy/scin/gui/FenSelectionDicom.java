package org.petctviewer.scintigraphy.scin.gui;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.util.DicomTools;
import org.petctviewer.scintigraphy.scin.ImagePreparator;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Debug;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.*;

/**
 * Window to select DICOM files. The list of DICOM files can add information on the files to be selected.
 *
 * @author Titouan QUÉMA - Restructuration and amerlioration of the code
 */
public class FenSelectionDicom extends JDialog implements ActionListener, ImageListener, WindowListener {
	private static final long serialVersionUID = 6706629497515318270L;

	protected final JTable table;
	private final JButton btn_selectAll;
	private DefaultTableModel dataModel;
	private List<Column> columns;

	private ImagePreparator preparator;
	private List<ImageSelection> selectedImages;

	/**
	 * @param preparator Module to prepare the images of this selection window
	 */
	public FenSelectionDicom(ImagePreparator preparator) {
		super((JFrame) null, true);
		this.preparator = preparator;
		ImagePlus.addImageListener(this);
		this.columns = new ArrayList<>();

		// on ajoute le titre a la fenetre
		this.setTitle("Select the DICOMs for " + preparator.getName());

		// creation du tableau
		table = new JTable();
		this.declareColumns(preparator.getColumns());

		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setFocusable(false);
		table.setDefaultEditor(Object.class, null);

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());

		JScrollPane tablePane = new JScrollPane(table);

		JPanel jp = new JPanel();

		JButton btn_select = new JButton("Select");
		btn_select.addActionListener(this);

		this.btn_selectAll = new JButton("Select All");
		this.btn_selectAll.addActionListener(this);

		panel.add(tablePane, BorderLayout.CENTER);

		JLabel instructions = new JLabel("Requirements: " + preparator.instructions());
		instructions.setMaximumSize(new Dimension(200, instructions.getMaximumSize().height));
		instructions.setFont(instructions.getFont().deriveFont(14f));
		panel.add(instructions, BorderLayout.NORTH);

		jp.add(btn_select);
		jp.add(this.btn_selectAll);

		panel.add(jp, BorderLayout.SOUTH);

		this.add(panel);
		this.setPreferredSize(new Dimension(700, 300));
		this.pack();
		this.setLocationRelativeTo(null);
	}

	public void declareColumns(Column[] columns) {
		this.columns.clear();
		this.columns.add(Column.ID);
		this.columns.addAll(Arrays.asList(columns));
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
				DefaultCellEditor celleditor = new DefaultCellEditor(new JComboBox<>(col.authorizedValues));
				manifacturer.setCellEditor(celleditor);
			}
			// TODO: remove this and use TableModel to do it properly
			if (!col.isVisible()) {
				this.table.getColumnModel().getColumn(0).setMinWidth(0);
				this.table.getColumnModel().getColumn(0).setMaxWidth(0);
			}
		}
		resizeColumnWidth(table);
	}

	public List<ImageSelection> retrieveSelectedImages() {
		return this.selectedImages;
	}

	private List<String[]> getTableData() {
		List<String[]> data = new ArrayList<>(WindowManager.getImageCount());

		int[] idList = WindowManager.getIDList();
		if (idList != null) {
			int countErrors = 0;
			for (int idImgOpen : idList) {
				ImagePlus imp = WindowManager.getImage(idImgOpen);

				// Do not show image of FenApplication
				if (imp.getWindow() instanceof FenApplication) continue;

				HashMap<String, String> infosPatient = Library_Capture_CSV.getPatientInfo(imp);

				String[] imageData = new String[this.columns.size()];

				try {
					int index = 0;
					for (Column c : this.columns) {
						if (c == Column.PATIENT) {
							imageData[index] = infosPatient.get("name");
						} else if (c == Column.STUDY) {
							imageData[index] = Library_Debug.replaceNull(DicomTools.getTag(imp, "0008,1030")).trim();
						} else if (c == Column.DATE) {
							imageData[index] = Library_Debug.replaceNull(infosPatient.get("date"));
						} else if (c == Column.SERIES) {
							imageData[index] = Library_Debug.replaceNull(DicomTools.getTag(imp, "0008,103E")).trim();
						} else if (c == Column.DIMENSIONS) {
							imageData[index] = imp.getDimensions()[0] + "x" + imp.getDimensions()[1];
						} else if (c == Column.STACK_SIZE) {
							imageData[index] = "" + imp.getStack().getSize();
						} else if (c.getName().equals(Column.ORIENTATION.getName())) {
							imageData[index] = determineImageOrientation(imp).toString();
						} else if (c.getName().equals(Column.ID.getName())) {
							imageData[index] = Integer.toString(idImgOpen);
						} else {
							imageData[index] = "CHOOSE VALUE";
						}
						index++;
					}
					data.add(imageData);
				} catch (ReadTagException e) {
					countErrors++;
				}
			}
			if (countErrors > 0) System.out.println(countErrors + " images could not be opened");
		}
		return data;
	}

	/**
	 * Determines the orientation of an ImagePlus.
	 * <p>
	 *
	 * @param imp Image to analyze
	 * @return Orientation of the image (UNKNOWN if the orientation could not be determined)
	 */
	private Orientation determineImageOrientation(ImagePlus imp) throws ReadTagException {

		boolean sameCameraMultiFrame = Library_Dicom.isSameCameraMultiFrame(imp);
		boolean firstImageAnt = Library_Dicom.isAnterior(imp);

		if (imp.getStackSize() == 1) {
			if (firstImageAnt) {
				return Orientation.ANT;
			} else {
				return Orientation.POST;
			}

		} else if (imp.getStackSize() == 2) {
			if (firstImageAnt) {
				return Orientation.ANT_POST;
			} else {
				return Orientation.POST_ANT;
			}

		} else if (imp.getStackSize() > 2 && sameCameraMultiFrame) {
			if (firstImageAnt) {
				return Orientation.DYNAMIC_ANT;
			} else {
				return Orientation.DYNAMIC_POST;
			}
		} else if (imp.getStackSize() > 2 && !sameCameraMultiFrame) {
			if (firstImageAnt) {
				return Orientation.DYNAMIC_ANT_POST;
			} else {
				return Orientation.DYNAMIC_POST_ANT;
			}
		}
		return Orientation.UNKNOWN;

	}

	private void resizeColumnWidth(JTable table) {
//		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		for (int col = 0; col < table.getColumnCount(); col++) {
			DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
			TableColumn column = colModel.getColumn(col);

			int width = 100;
			TableCellRenderer renderer;
			for (int row = 0; row < table.getRowCount(); row++) {
				renderer = table.getCellRenderer(row, col);
				Component component = renderer.getTableCellRendererComponent(table, table.getValueAt(row, col), false,
																			 false, row, col);
				width = Math.max(width, component.getPreferredSize().width);
			}
			column.setPreferredWidth(width + 2);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		JButton b = (JButton) event.getSource();

		if (b == this.btn_selectAll) {
			// on selectionne toutes les fenetres
			table.selectAll();
		}

		List<ImageSelection> selectedImages = this.getSelectedImages();

		try {

			this.checkForUnauthorizedValues(selectedImages);
			this.checkSamePatient(selectedImages);
			this.prepareImages(selectedImages);

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
	private void checkForUnauthorizedValues(List<ImageSelection> selectedImages) throws WrongColumnException {
		for (ImageSelection ims : selectedImages) {
			for (Column column : this.columns) {
				String value = ims.getValue(column.getName());
				if (!column.isAuthorizedValue(value)) throw new WrongColumnException(column, ims.getRow(),
																					 "The value " + value +
																							 " is incorrect, it must" +
																							 " " + "be one of: " +
																							 Arrays.toString(
																									 column.getAuthorizedValues()));
			}
		}
	}

	/**
	 * Checks that all selected images are for the same patient (id and name). If not, the user can override the
	 * process.
	 *
	 * @throws WrongInputException if selected images belong to multiple patient and the user do not override the
	 *                             process
	 */
	private void checkSamePatient(List<ImageSelection> selectedImages) throws WrongInputException {
		Set<String> patientIds = new HashSet<>();
		Set<String> patientNames = new HashSet<>();

		for (ImageSelection ims : selectedImages) {
			HashMap<String, String> infoPatient = Library_Capture_CSV.getPatientInfo(ims.getImagePlus());
			patientIds.add(infoPatient.get(Library_Capture_CSV.PATIENT_INFO_ID));
			patientNames.add(infoPatient.get(Library_Capture_CSV.PATIENT_INFO_NAME));
		}

		int result = JOptionPane.YES_OPTION;
		if (patientIds.size() > 1 || patientNames.size() > 1) {
			String message = "Selected images might belong to different patient:\nDifferent IDs: " + patientIds +
					"\nDifferent Names: " + patientNames + "\n\nWould you like to continue?";
			result = JOptionPane.showConfirmDialog(this, message, "Multiple patient found", JOptionPane.YES_NO_OPTION,
												   JOptionPane.WARNING_MESSAGE);
		}

		if (result != JOptionPane.YES_OPTION) throw new WrongInputException("Images belong to multiple patients");
	}

	/**
	 * This method is used to get all of the images selected by the user.
	 *
	 * @return images selected by the user
	 */
	private List<ImageSelection> getSelectedImages() {
		int[] rows = this.table.getSelectedRows();
		List<ImageSelection> selectedImages = new ArrayList<>(rows.length);

		for (int row : rows) {
			// Generate values for the selection
			String[] values = new String[this.columns.size()];
			for (int col = 0; col < values.length; col++) {
				values[col] = (String) this.table.getValueAt(row, col);
			}
			// Generate columns array
			String[] columns = new String[this.columns.size()];
			for (int idCol = 0; idCol < columns.length; idCol++)
				columns[idCol] = this.columns.get(idCol).getName();
			ImageSelection imageSelected = new ImageSelection(WindowManager.getImage(Integer.parseInt(values[0])),
															  columns, values);
			selectedImages.add(imageSelected);
			// TODO: do not add row here, use the invisible columns
			imageSelected.setRow(row + 1);
		}

		return selectedImages;
	}

	/**
	 * This method prepares the images. Once this method has been called, the scintigraphy can retrieve the images.
	 *
	 * @param selectedImages Images selected by the user (at this point, they are not conform to the Controller's
	 *                       requirements)
	 */
	private void prepareImages(List<ImageSelection> selectedImages) {
		// Apply marco to convert images to 32bit
		selectedImages.forEach(ims -> IJ.run(ims.getImagePlus(), "32-bit", ""));

		try {
			List<ImageSelection> userSelection = this.preparator.prepareImages(selectedImages);
			if (userSelection != null) {
				ImagePlus.removeImageListener(this);
				this.selectedImages = userSelection;
				this.dispose();
			}
		} catch (WrongInputException e) {
			JOptionPane.showMessageDialog(this, "Error while selecting images:\n" + e.getMessage(), "Selection error",
										  JOptionPane.ERROR_MESSAGE);
		} catch (ReadTagException e) {
			JOptionPane.showMessageDialog(this, "Error while preparing images.\nThe tag for " + e.getTagName() + " [" +
					e.getTagCode() + "] " + "could" + " not be found.\n" + e.getMessage());
		}
	}

	private void updateTable() {

		this.dataModel.setRowCount(0);
		for (String[] s : this.getTableData()) {
			this.dataModel.addRow(s);
		}

	}

	@Override
	public void imageOpened(ImagePlus imp) {
		this.updateTable();
		resizeColumnWidth(table);
	}

	@Override
	public void imageClosed(ImagePlus imp) {
		this.updateTable();
	}

	@Override
	public void imageUpdated(ImagePlus imp) {
	}

	@Override
	public void windowOpened(WindowEvent e) {

	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
		ImagePlus.removeImageListener(this);
	}

	@Override
	public void windowIconified(WindowEvent e) {

	}

	@Override
	public void windowDeiconified(WindowEvent e) {

	}

	@Override
	public void windowActivated(WindowEvent e) {

	}

	@Override
	public void windowDeactivated(WindowEvent e) {

	}

	/**
	 * Represents a column for the selection table.
	 */
	public static class Column {
		public static final Column PATIENT = new Column("Patient"), STUDY = new Column("Study"), DATE = new Column(
				"Date"), SERIES = new Column("Series"), DIMENSIONS = new Column("Dimensions"), STACK_SIZE = new Column(
				"Stack Size"), ORIENTATION, ROW = new Column("Index", null, false), ID = new Column("ID", null, false);

		static {
			String[] s = Orientation.allOrientations();
			ORIENTATION = new Column("Orientation", s);
		}

		private String name;
		private String[] authorizedValues;
		private boolean visible;

		/**
		 * Creates a new column with the specified name. The authorized values are displayed as a list for the user to
		 * choose from. <br> If the authorized value is empty, then no value will be authorized. If the authorized
		 * value
		 * is null, then all values will be authorized.
		 *
		 * @param name             Name of the column (shown in the header of the table)
		 * @param authorizedValues Possible values for the user to choose from. Null means all values authorized. Empty
		 *                         means no values authorized
		 * @param visible          TRUE if the column should be visible and FALSE if the column should be hidden
		 */
		public Column(String name, String[] authorizedValues, boolean visible) {
			this.name = name;
			this.authorizedValues = authorizedValues;
			this.visible = visible;
		}

		/**
		 * Creates a new column with the specified name. The authorized values are displayed as a list for the user to
		 * choose from. <br> If the authorized value is empty, then no value will be authorized. If the authorized
		 * value
		 * is null, then all values will be authorized.
		 *
		 * @param name             Name of the column (shown in the header of the table)
		 * @param authorizedValues Possible values for the user to choose from. Null means all values authorized. Empty
		 *                         means no values authorized
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

		public static Column[] getDefaultColumns() {
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
		 * Checks if the specified value matches any of the authorized value defined by the column. If no authorized
		 * values are defined, then this method will always return TRUE.
		 *
		 * @param value Value to check
		 * @return TRUE if the value matches at least one authorized value or if there is no authorized value defined
		 * and FALSE otherwise
		 */
		public boolean isAuthorizedValue(String value) {
			if (this.authorizedValues == null) return true;
			for (String s : this.authorizedValues)
				if (s.equals(value)) return true;
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
		 * @return array of authorized values (this method can return null if there is no restriction on the values)
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

		/**
		 * @return TRUE if the column is visible and FALSE otherwise
		 */
		public boolean isVisible() {
			return this.visible;
		}
	}
}
