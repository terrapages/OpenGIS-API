package com.terrapages.opengis.api;

import java.io.File;
import java.io.FileNotFoundException;

import junit.framework.TestCase;

/**
 * This is the unmarshalling of xml input test documents To add new standards add a new method like
 * the one below To add new testing documents just add them in the src/junit-xml directory
 * @author ahughes
 * @version $Id$
 */
public class UnmarshallTest extends TestCase {
	
	@org.junit.Test
	public void testOls() {
		try {
			JUnitXmlReader inputXml = new JUnitXmlReader("src/junit-xml/ols", "net.opengis.xls");
			while (inputXml.hasNext()) {
				File currentFile = inputXml.next();
				assertTrue("Testing Input Source:" + currentFile, inputXml.unmarshall(currentFile));
			}
		} catch (FileNotFoundException e) {
			assertTrue("Input test xml files not found in supplied directory.", false);
		}
	}

}
