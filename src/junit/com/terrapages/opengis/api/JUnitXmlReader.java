package com.terrapages.opengis.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;

import net.opengis.xls.XLSType;

import com.terrapages.opengis.api.util.MarshallerMediator;
import com.terrapages.opengis.api.util.Standard;
import com.terrapages.opengis.api.util.UnmarshallerMediator;

/**
 * This test iterates thru test xml documents in the <code>xmlInputDir</code> directory. If the
 * created JAXB classes are unable to make scense of the incoming documents, then there is obviously
 * a bug with the current JAXB customization. It also needs the jaxb package name for the incoming
 * document to be marshalled to. This is the <code>packageName</code>.
 * @author ahughes
 * @version $Id$
 */
public class JUnitXmlReader implements Iterator {

	Iterator filesIter = null;
 
	/**
	 * To customize your own test you must copy/rename this class and change the packageName and
	 * xmlInputDir
	 */

	public JUnitXmlReader(String xmlInputDir, String packageName) throws FileNotFoundException {
		// Read in the input files from the directory
		File tempDir = new File(xmlInputDir);
		List files = getFileListing(tempDir);
		filesIter = files.iterator();
	}

	public boolean hasNext() {
		return filesIter.hasNext();
	}

	public File next() {
		return (File) filesIter.next();
	}

	public void remove() {
		filesIter.remove();
	}

	/**
	 * Attempts to unmarshaled and then marshaled the specified file.
	 * @param inputFile The XML file
	 * @return True if the file successfully unmarshaled and marshaled, else false. 
	 */
	protected boolean unmarshall(File inputFile) {
		
		boolean success = false;
		try {	
			// Unmarshalls the XML file to JAXBElements		
			JAXBElement element = (JAXBElement) UnmarshallerMediator.unmarshall(Standard.XLS, UnmarshallerMediator.XLS_XSD_PATH, inputFile);
			
			// Marshalls the JAXBElements to XML files	
			MarshallerMediator.marshall(Standard.XLS, UnmarshallerMediator.XLS_XSD_PATH, element, System.out);
			
			// If the JAXBElement is not null and is instance of the XLSType then the test was successful
			if (element != null && element.getValue() instanceof XLSType)
				success = true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.flush();
		}
		return success;
	}

	/**
	 * Recursively walk a directory tree and return a List of all Files found; the List is sorted
	 * using File.compareTo.
	 * @param aStartingDir is a valid directory, which can be read.
	 */
	private List<File> getFileListing(File aStartingDir) throws FileNotFoundException {
		validateDirectory(aStartingDir);
		List<File> result = new ArrayList<File>();

		File[] filesAndDirs = aStartingDir.listFiles();
		List filesDirs = Arrays.asList(filesAndDirs);
		Iterator filesIter = filesDirs.iterator();
		File file = null;
		while (filesIter.hasNext()) {
			file = (File) filesIter.next();
			// ignore directories... we dont care about them
			if (file.isFile()) {
				// add only files that end in .xml
				if (file.getName().toLowerCase().endsWith(".xml")) {
					result.add(file); // ignore directories
				}
			} else {
				// must be a directory
				// recursive call!
				List<File> deeperList = getFileListing(file);
				result.addAll(deeperList);
			}

		}
		Collections.sort(result);
		return result;
	}

	/**
	 * Directory is valid if it exists, does not represent a file, and can be read.
	 */
	private void validateDirectory(File aDirectory) throws FileNotFoundException {
		if (aDirectory == null) {
			throw new IllegalArgumentException("Directory should not be null.");
		}
		if (!aDirectory.exists()) {
			throw new FileNotFoundException("Directory does not exist: " + aDirectory);
		}
		if (!aDirectory.isDirectory()) {
			throw new IllegalArgumentException("Is not a directory: " + aDirectory);
		}
		if (!aDirectory.canRead()) {
			throw new IllegalArgumentException("Directory cannot be read: " + aDirectory);
		}
	}
}
