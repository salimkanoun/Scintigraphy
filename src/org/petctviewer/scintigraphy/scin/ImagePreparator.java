package org.petctviewer.scintigraphy.scin;

import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;

import java.util.List;

/**
 * This interface is used by the {@link org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom} to prepare the images
 * and detect if the selection is valid.
 *
 * @author Titouan QUÉMA
 */
public interface ImagePreparator {

	/**
	 * This method returns a readable name of this preparator. This is used to identify it.
	 *
	 * @return name of this preparator
	 */
	String getStudyName();

	/**
	 * This method returns an array of columns that will be used in the window.
	 *
	 * @return array of columns used
	 */
	FenSelectionDicom.Column[] getColumns();

	/**
	 * This method should prepare the images that the user opened (like setting the right orientation...). This method
	 * should also check that the openedImages are correct (according to the program specification).<br> For instance,
	 * if the program needs a specific amount of images, then this method should check for that amount.<br> If this
	 * method returns null, then the program will NOT be launched.<br> If this method returns something, then the
	 * program will be launched. The returned images of this method must be clones of the real images provided.<br>
	 * <p>
	 * During this process, if a DICOM tag could not be retrieve but can be ignored (like the name of the patient for
	 * instance) then this method do not have to throw a {@link ReadTagException}. But if a tag is necessary for the
	 * scintigraphy, then this method will throw an exception.
	 * </p>
	 *
	 * @param openedImages Images that the user selected when clicking on the 'Select' button in the FenSelectionDicom
	 * @return The well formatted images. If this return value is null, then the program will NOT be launched
	 * @throws WrongInputException if the images opened cannot be used by this controller (too many, not enough, wrong
	 *                             orientation...)
	 * @throws ReadTagException    if a DICOM tag could not be retrieve and <b>must</b> be present on the image
	 */
	List<ImageSelection> prepareImages(List<ImageSelection> openedImages) throws WrongInputException, ReadTagException;

	/**
	 * This method returns a string describing the images needed for this preparator. This message is intended to the
	 * user.
	 *
	 * @return instructions of the requested images
	 */
	String instructions();

	/**
	 * Launches the program with the specified images.
	 *
	 * @param preparedImages Image prepared
	 */
	void start(List<ImageSelection> preparedImages);

}
