/*
 * 
 * 
 */
package zipdiff.output;

import java.io.OutputStream;
import java.io.PrintWriter;

import earcompare.Differences;

/**
 * 
 * @author Sean C. Sullivan
 *
 */
public class TextBuilder extends AbstractBuilder {
	public void build(OutputStream out, Differences d) {
		PrintWriter pw = new PrintWriter(out);
		pw.println(d.toString());
		pw.flush();
	}
}
