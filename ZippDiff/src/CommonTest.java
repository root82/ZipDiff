import java.io.File;
import java.util.Iterator;

import org.apache.commons.cli.*;

import zipdiff.output.Builder;
import zipdiff.output.HtmlBuilder;

public class CommonTest {

    private static final int EXITCODE_ERROR = 2;
	private static final int EXITCODE_DIFF = 1;
    private static final String OPTION_OUTPUT_FILE[] = {"o","outputfile"};
    private static final String OPTION_FILE1[] = {"c", "compare"};
    private static final String OPTION_FILE2[] = {"w", "with"};
    private static final String OPTION_EXIT_WITH_ERROR_ON_DIFF = "exitwitherrorondifference";
	private static final String OPTION_VERBOSE[] = {"v","verbose"};
	private static final Options options;
	
    // static initializer
    static {
        options = new Options();

        Option compareFile = new Option(OPTION_FILE1[0], OPTION_FILE1[1], true, "<filename> file to compare");
        compareFile.setRequired(true);

        Option withFile = new Option(OPTION_FILE2[0], OPTION_FILE2[1], true, "<filename> file to compare with");
        withFile.setRequired(true);

        Option outputFileOption = new Option(OPTION_OUTPUT_FILE[0], OPTION_OUTPUT_FILE[1], true, "output filename");
        outputFileOption.setRequired(false);
        
        
		/*Option exitWithError = 
			new Option(
				OPTION_EXIT_WITH_ERROR_ON_DIFF,
				OPTION_EXIT_WITH_ERROR_ON_DIFF,
				false,
				"if a difference is found then exit with error " + EXITCODE_DIFF);*/
				
		Option verboseOption = new Option(OPTION_VERBOSE[0], OPTION_VERBOSE[1], false, "verbose mode");
		
        options.addOption(compareFile);
        options.addOption(withFile);
        //options.addOption(exitWithError);
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
            
            filename1 = line.getOptionValue(OPTION_FILE1[0]);
            filename2 = line.getOptionValue(OPTION_FILE2[0]);

            File f1 = new File(filename1);
            File f2 = new File(filename2);

            checkFile(f1);
            checkFile(f2);
            
            System.out.println("File 1 = " + f1);
            System.out.println("File 2 = " + f2);
            

            DiffCals cals = new DiffCals(f1,f2);
            
            Diffs d = cals.getDifferences();
            
            if (d.hasDifferences()){
            	if (line.hasOption(OPTION_VERBOSE[0])) {
            		System.out.println(d);
            		System.out.println(d.getCompareFileName() + " and " + d.getWithFileName() + " are different.");
            	}
            	if (line.hasOption(OPTION_EXIT_WITH_ERROR_ON_DIFF)) {
            		System.exit(EXITCODE_DIFF);
            	}
            } else {
            	System.out.println("No differences found.");
            }
            System.out.println(d);
            
            
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
