import java.io.File;
import java.util.Iterator;

import org.apache.commons.cli.*;

import zipdiff.output.Builder;
import zipdiff.output.HtmlBuilder;

public class CommonTest {

    private static final int EXITCODE_ERROR = 2;
	private static final int EXITCODE_DIFF = 1;
    private static final String OPTION_OUTPUT_FILE = "outputfile";
    private static final String OPTION_FILE1 = "file1";
    private static final String OPTION_FILE2 = "file2";
    private static final String OPTION_EXIT_WITH_ERROR_ON_DIFF = "exitwitherrorondifference";
	private static final String OPTION_VERBOSE = "verbose";
	private static final Options options;
	
    // static initializer
    static {
        options = new Options();

        Option file1 = new Option(OPTION_FILE1, OPTION_FILE1, true, "<filename> first file to compare");
        file1.setRequired(true);

        Option file2 = new Option(OPTION_FILE2, OPTION_FILE2, true, "<filename> second file to compare");
        file2.setRequired(true);

        Option outputFileOption =
        	new Option(
        			OPTION_OUTPUT_FILE,
					OPTION_OUTPUT_FILE,
					true,
					"output filename");
        outputFileOption.setRequired(false);
        
        
		Option exitWithError = 
			new Option(
				OPTION_EXIT_WITH_ERROR_ON_DIFF,
				OPTION_EXIT_WITH_ERROR_ON_DIFF,
				false,
				"if a difference is found then exit with error " + EXITCODE_DIFF);
				
		Option verboseOption = 
			new Option(
				OPTION_VERBOSE,
				OPTION_VERBOSE,
				false,
				"verbose mode");
		
        options.addOption(file1);
        options.addOption(file2);
        options.addOption(exitWithError);
        options.addOption(verboseOption);
        options.addOption(outputFileOption);
    }
    
    private static void checkFile(java.io.File f) {
        String filename = f.toString();

        if (!f.exists()) {
            System.err.println("'" + filename + "' does not exist");
            System.exit(EXITCODE_ERROR);
        }

        if (!f.canRead()) {
            System.err.println("'" + filename + "' is not readable");
            System.exit(EXITCODE_ERROR);
        }

        if (f.isDirectory()) {
            System.err.println("'" + filename + "' is a directory");
            System.exit(EXITCODE_ERROR);
        }

    }

    private static void writeOutputFile(String filename, Diffs d)
    	throws java.io.IOException{
    	Builder builder = null;
    	builder = new HtmlBuilder();
    	//builder.build(filename, d);
    	
    }
    
	/**
	 * 
	 * The command line interface to zipdiff utility
	 * 
	 * @param args The command line parameters
	 * 
	 */
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {

		CommandLineParser parser = new GnuParser();
		
		try {
			CommandLine line = parser.parse(options, args);
			
            String filename1 = null;
            String filename2 = null;
            
            filename1 = line.getOptionValue(OPTION_FILE1);
            filename2 = line.getOptionValue(OPTION_FILE2);

            File f1 = new File(filename1);
            File f2 = new File(filename2);

            checkFile(f1);
            checkFile(f2);
            
            System.out.println("File 1 = " + f1);
            System.out.println("File 2 = " + f2);
            

            DiffCals cals = new DiffCals(f1,f2);
            
            Diffs d = cals.getDifferences();
            
            if (d.hasDifferences()){
            	if (line.hasOption(OPTION_VERBOSE)) {
            		System.out.println(d);
            		System.out.println(d.getFilename1() + " and " + d.getFilename2() + " are different.");
            	}
            	if (line.hasOption(OPTION_EXIT_WITH_ERROR_ON_DIFF)) {
            		System.exit(EXITCODE_DIFF);
            	}
            } else {
            	System.out.println("No differences found.");
            }
            //System.out.println(d);
            
            Iterator iter = d.getChanged().keySet().iterator();
            while(iter.hasNext()){
            	System.out.println(iter.next());
            }
            
            writeOutputFile("a.html", d);
		} catch (ParseException e) {
            System.err.println(e.getMessage());
            new HelpFormatter().printHelp("CommonTest [options] ", options);
            System.exit(EXITCODE_ERROR);
		} catch (Exception ex) {
            ex.printStackTrace();
            System.exit(EXITCODE_ERROR);
        }

	}

}
