package de.binarytree.plugins.qualitygates.steps.dependencycheck.parser;

import java.io.File;
import java.net.URL;

/**
 * Base class for parser tests.<br>
 * contains utility methods for file manipulations<br>
 * 
 * 
 * @author Vincent Sellier
 * 
 */
public class AbstractParserTestUtils {

    /**
     * return a file searching in the classpath
     * 
     * @param fileName
     *            the name of the file relative to the classpath
     * @return the file
     * @throws Exception
     */
    public static File getFile(String fileName) throws Exception {
        URL fileURL = AbstractParserTestUtils.class.getResource(fileName);
        File file = new File(fileURL.toURI());
        return file;
    }

}
