/*
 * 
 * 
 */

package zipdiff.output;

import java.io.OutputStream;

import earcompare.Differences;

/**
 * 
 * Builder pattern: <a href="http://wiki.cs.uiuc.edu/patternStories/BuilderPattern">
 *     http://wiki.cs.uiuc.edu/patternStories/BuilderPattern</a>
 * 
 * @author Sean C. Sullivan
 *
 */
public interface Builder {
	public void build(OutputStream out, Differences d);
	public void build(String filename, Differences d) throws java.io.IOException;
}
