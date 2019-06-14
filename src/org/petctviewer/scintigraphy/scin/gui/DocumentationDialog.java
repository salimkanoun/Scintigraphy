package org.petctviewer.scintigraphy.scin.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This class is used to display all the information needed for the documentation. Some pre-defined fields have been
 * defined, like the designer, the developer of the application or links to the online documentation and youtube
 * demonstration.<br>The documentation dialog matches this representation:
 * <pre>
 * +-----------------------------------------------+
 * |                  TITLE                        |
 * |                                               |
 * |   [              Info area                 ]  |
 * |                                               |
 * |   [--- Papers references ---               ]  |
 * |   [             . . .                      ]  |
 * |   [                                        ]  |
 * |                                               |
 * |                                               |
 * |  { BUTTON YOUTUBE }   { BUTTON ONLINE DOC }   |
 * |                                               |
 * +-----------------------------------------------+
 * </pre>
 * The info area regroups all of the information about the application, like the designers and the developers.<br> On
 * the Paper references area, the name of the researcher and the date of publication can be placed.<br>Finally, the
 * youtube and online documentation buttons can be set with URL.
 * <p>
 * The Info area and the buttons are optionals, but the Papers references must be provided with at least 1 entry.
 *
 * @author Titouan QUÉMA
 */
public class DocumentationDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private final JPanel panPapers;
	private final JPanel panInfo;
	private Link linkYoutube, linkOnlineDoc;
	private Field designer, developper;

	public DocumentationDialog(FenApplication parent) {
		super(parent, "Documentation of " + parent.getStudyName(), false);

		JPanel pan = new JPanel(new BorderLayout());

		// Title
		JLabel title = new JLabel(parent.getStudyName() + " documentation");
		title.setFont(title.getFont().deriveFont(Font.BOLD, 17f));
		title.setHorizontalAlignment(JLabel.CENTER);
		pan.add(title, BorderLayout.NORTH);

		// Content
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(new EmptyBorder(10, 20, 10, 20));

		// Panel infos
		panInfo = new JPanel(new GridLayout(0, 2));
		panInfo.setBorder(new EmptyBorder(10, 20, 10, 20));
		panel.add(panInfo);

		// Panel papers
		panPapers = new JPanel(new GridLayout(0, 2));
		panPapers.setBorder(BorderFactory.createTitledBorder("Based on paper(s)"));
		panel.add(panPapers);

		// Panel buttons
		JPanel panButtons = new JPanel(new FlowLayout());
		this.linkYoutube = new Link();
		this.linkYoutube.setText("Youtube");
		this.linkYoutube.setVisible(false);
		panButtons.add(linkYoutube);
		this.linkOnlineDoc = new Link();
		this.linkOnlineDoc.setText("Online Doc");
		this.linkOnlineDoc.setVisible(false);
		panButtons.add(linkOnlineDoc);
		panel.add(panButtons);

		pan.add(panel, BorderLayout.CENTER);

		this.setContentPane(pan);

		this.pack();
		this.setLocationRelativeTo(parent);
	}

	/**
	 * Sets the field for the designer. If null is passed, then this field is removed.
	 *
	 * @param designer Designer of the program
	 */
	public void setDesigner(String designer) {
		if (designer == null) {
			// Remove field
			if (this.designer != null) {
				this.panInfo.remove(this.designer.label);
				this.panInfo.remove(this.designer.value);
			}
		} else {
			if (this.designer == null) {
				this.designer = new Field("Designed by", designer);
				this.addInfoField(this.designer);
			} else this.designer.changeValue(designer);
		}
	}

	/**
	 * Sets the field for the developer. If null, then this field is removed.
	 *
	 * @param developer Developer of the program
	 */
	public void setDeveloper(String developer) {
		if (developer == null) {
			// Remove field
			if (this.developper != null) {
				this.panInfo.remove(this.developper.label);
				this.panInfo.remove(this.developper.value);
			}
		} else {
			if (this.developper == null) {
				this.developper = new Field("Developed by", developer);
				this.addInfoField(this.developper);
			} else this.developper.changeValue(developer);
		}
	}

	/**
	 * Adds a documentation reference in the Papers area.
	 *
	 * @param documentation Field to add
	 */
	public void addReference(Field documentation) {
		this.panPapers.add(documentation.label);
		this.panPapers.add(documentation.value);

		this.pack();
		this.setLocationRelativeTo(this.getParent());
	}

	/**
	 * Adds an info on the Info area.
	 *
	 * @param field Field to add
	 */
	public void addInfoField(Field field) {
		this.panInfo.add(field.label);
		this.panInfo.add(field.value);

		this.pack();
		this.setLocationRelativeTo(this.getParent());
	}

	/**
	 * Creates the youtube button of this documentation with the specified URL. If a previous button was already in
	 * place, then this method will update the URL.<br>If the url is null, then this will remove the link.<br>If the
	 * URL
	 * is invalid, then this will remove the link.
	 *
	 * @param url Valid URL for youtube video
	 */
	public void setYoutube(String url) {
		if (url != null) {
			try {
				this.linkYoutube.setUri(new URI(url));
				this.linkYoutube.setVisible(true);
			} catch (URISyntaxException e) {
				this.linkYoutube.setVisible(false);
				e.printStackTrace();
			}
		} else {
			this.linkYoutube.setVisible(false);
		}

		this.pack();
		this.setLocationRelativeTo(this.getParent());
	}

	/**
	 * Creates the online doc button of this documentation with the specified URL. If a previous button was already in
	 * place, then this method will update the URL.<br>If the url is null, then this will remove the link.<br>If the
	 * URL
	 * is invalid, then this will remove the link.
	 *
	 * @param url Valid URL for online documentation
	 */
	public void setOnlineDoc(String url) {
		if (url != null) {
			try {
				this.linkOnlineDoc.setUri(new URI(url));
				this.linkOnlineDoc.setVisible(true);
			} catch (URISyntaxException e) {
				this.linkOnlineDoc.setVisible(false);
				e.printStackTrace();
			}
		} else {
			this.linkOnlineDoc.setVisible(false);
		}

		this.pack();
		this.setLocationRelativeTo(this.getParent());
	}

	/**
	 * This class represents a field of the documentation dialog. A field can be composed of a label or a link.
	 */
	public static class Field {
		private JLabel label;
		private JComponent value;
		private String stringValue;

		public Field(String label, String value) {
			this.label = new JLabel(label);
			this.value = new JLabel(value);
			this.stringValue = value;
		}

		public Field(String label, String value, String url) {
			this.label = new JLabel(label);
			this.stringValue = value;
			this.toLink(url);
		}

		/**
		 * Creates a text field with the specified label and value.
		 *
		 * @param label Label of the field (key of the value)
		 * @param value Value of the field
		 * @return created field
		 */
		public static Field createTextField(String label, String value) {
			return new Field(label, value);
		}

		/**
		 * Creates a link field with the specified label and value. The link will redirect to the specified URL .<br>If
		 * the URL is invalid, then this method will return a text field.
		 *
		 * @param label Label of the field (key of the value)
		 * @param value Value of the field (displayed)
		 * @param url   URL of the link (displayed only in the tooltip)
		 * @return created field
		 */
		public static Field createLinkField(String label, String value, String url) {
			return new Field(label, value, url);
		}

		/**
		 * Creates a link field with the specified label and with the URL as value. The link will redirect to the
		 * specified URL.<br>If the URL is invalid, then this method will return a text field.
		 *
		 * @param label Label of the field (key of the value)
		 * @param url   Value and URL of the field (displayed)
		 * @return created field
		 */
		public static Field createLinkField(String label, String url) {
			return new Field(label, url, url);
		}

		/**
		 * Updates the value of the field. If this field is a link, then this method will change the displayed value,
		 * but not the URL.
		 *
		 * @param value New value to display
		 */
		public void changeValue(String value) {
			this.stringValue = value;
			if (this.value instanceof Link) ((Link) this.value).setText(value);
			else ((JLabel) this.value).setText(value);
		}

		/**
		 * Changes this field to become a link that will redirect to the specified URL. The value displayed will remain
		 * the same. If this field was already a link, then this method will update the URL of the link.<br>If the URL
		 * is invalid, then this method will change this field to become a text field.
		 *
		 * @param url URL of the link
		 */
		public void toLink(String url) {
			try {
				if (this.value instanceof Link) ((Link) this.value).setUri(new URI(url));
				else this.value = new Link(stringValue, new URI(url));
			} catch (URISyntaxException e) {
				this.value = new JLabel(stringValue);
				e.printStackTrace();
			}
		}
	}
}
