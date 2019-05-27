package org.petctviewer.scintigraphy.scin.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class DocumentationDialog extends JDialog {

	public final static String FIELD_DESIGNER = "designer", FIELD_DEVELOPER = "developer", FIELD_REFERENCE =
			"reference", FIELD_DOC = "documentation", FIELD_YOUTUBE = "youtube";

	private Map<String, JComponent[]> fields;
	private JPanel panel;

	public DocumentationDialog(FenApplication parent) {
		super(parent, "Documentation of " + parent.getStudyName(), false);

		// Initialize fields
		this.fields = new HashMap<>();

		JPanel pan = new JPanel(new BorderLayout());

		// Title
		JLabel title = new JLabel(parent.getStudyName() + " documentation");
		title.setFont(title.getFont().deriveFont(Font.BOLD, 17f));
		title.setHorizontalAlignment(JLabel.CENTER);
		pan.add(title, BorderLayout.NORTH);

		// Content
		this.panel = new JPanel(new GridLayout(0, 2));
		this.panel.setBorder(new EmptyBorder(10, 20, 10, 20));
		pan.add(this.panel, BorderLayout.CENTER);

		this.setContentPane(pan);

		this.pack();
		this.setLocationRelativeTo(parent);
	}

	/**
	 * Creates and set the field for the designer.
	 *
	 * @param designer Designer of the program
	 */
	public void setDesigner(String designer) {
		this.createField(FieldType.TEXT, FIELD_DESIGNER, "Designed by:", designer);
	}

	/**
	 * Creates and set the field for the developer.
	 *
	 * @param developer Developer of the program
	 */
	public void setDeveloper(String developer) {
		this.createField(FieldType.TEXT, FIELD_DEVELOPER, "Developed by:", developer);
	}

	/**
	 * Creates and set the field for the reference used for the study.
	 *
	 * @param reference Reference used for the study
	 */
	public void setReference(String reference) {
		this.createField(FieldType.TEXT, FIELD_REFERENCE, "Reference used:", reference);
	}

	/**
	 * Creates and set the field for the online documentation.
	 *
	 * @param documentation Link to the online documentation
	 */
	public void setDocumentation(String documentation) {
		this.createField(FieldType.LINK, FIELD_DOC, "Online documentation:", documentation);
	}

	/**
	 * Creates and set the field for the youtube demo of the program.
	 *
	 * @param youtube Link to the youtube demo
	 */
	public void setYoutube(String youtube) {
		this.createField(FieldType.LINK, FIELD_YOUTUBE, "Youtube demonstration:", youtube);
	}

	/**
	 * Creates a field with the specified ID.<br>
	 * A field is composed of a label and a value. For instance: "<i>Designed by:</i> <b>Jack</b>"<br>
	 * If the value is null or empty, then this method is equal to {@link #removeField(String)}.<br>
	 * This method should be used if the default fields are not enough for the documentation.
	 *
	 * @param type    Type of the field to create
	 * @param fieldId ID of the field (unique)
	 * @param label   Label of the field
	 * @param value   Value of the label
	 */
	public void createField(FieldType type, String fieldId, String label, String value) {
		if (value == null || value.isEmpty()) {
			this.removeField(fieldId);
			return;
		}

		JLabel l = new JLabel(label);
		l.setHorizontalAlignment(JLabel.LEFT);

		JComponent val = null;
		if (type == FieldType.TEXT) {
			val = new JLabel(value);
			((JLabel) val).setHorizontalAlignment(JLabel.LEFT);
		} else if (type == FieldType.LINK) {
			try {
				val = new Link(value, new URI(value));
			} catch(URISyntaxException e) {
				System.err.println("URI is wrong");
				val = new JLabel(value);
			}
		}

		this.panel.add(l);
		this.panel.add(val);

		this.fields.put(fieldId, new JComponent[]{l, val});

		this.pack();
		this.setLocationRelativeTo(this.getParent());
	}

	/**
	 * Removes the field with the specified ID.
	 *
	 * @param fieldId ID of the field to remove
	 */
	public void removeField(String fieldId) {
		JComponent[] labels = this.fields.remove(fieldId);
		if (labels != null) {
			// Remove from the screen
			for (JComponent label : labels)
				this.panel.remove(label);
		}
	}

	public enum FieldType {
		TEXT, LINK;
	}

}
