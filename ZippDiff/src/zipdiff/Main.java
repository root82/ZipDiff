/*
 * 
 * 
 */
package zipdiff;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import zipdiff.output.*;

import org.apache.commons.cli.*;

/**
 * 
 * Provides a command line interface to zipdiff
 * 
 * @author Sean C. Sullivan, J.Stewart
 * 
 */
public class Main {
    private static final int EXITCODE_ERROR = 2;
	private static final int EXITCODE_DIFF = 1;
    private static final String OPTION_COMPARE_CRC_VALUES = "comparecrcvalues";
    private static final String OPTION_COMPARE_TIMESTAMPS = "comparetimestamps";
    private static final String OPTION_IGNORE_CVS_FILES = "ignorecvsfiles";
    private static final String OPTION_OUTPUT_FILE = "outputfile";
    private static final String OPTION_FILE1 = "file1";
    private static final String OPTION_FILE2 = "file2";
    private static final String OPTION_REGEX = "regex";
    private static final String OPTION_EXIT_WITH_ERROR_ON_DIFF = "exitwitherrorondifference";
	private static final String OPTION_VERBOSE = "verbose";
    private static final Options options;

    // static initializer
    static {
        options = new Options();

        Option compareTS =
            new Option(OPTION_COMPARE_TIMESTAMPS, OPTION_COMPARE_TIMESTAMPS, false, "Compare timestamps");
        compareTS.setRequired(false);

        Option compareCRC =
            new Option(OPTION_COMPARE_CRC_VALUES, OPTION_COMPARE_CRC_VALUES, false, "Compare CRC values");
        compareCRC.setRequired(false);

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
        
        Option regex =
            new Option(
                OPTION_REGEX,
                OPTION_REGEX,
                true,
                "regular expression to match files to exclude e.g. (?i)meta-inf.*");
        regex.setRequired(false);

        Option ignoreCVSFilesOption =
        	new Option(
        		OPTION_IGNORE_CVS_FILES,
				OPTION_IGNORE_CVS_FILES,
				false,
				"ignore CVS files");
        ignoreCVSFilesOption.setRequired(false);
        
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
		
        options.addOption(compareTS);
        options.addOption(compareCRC);
        options.addOption(file1);
        options.addOption(file2);
        options.addOption(regex);
        options.addOption(ignoreCVSFilesOption);
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

    private static void writeOutputFile(String filename, Differences d)
    	throws java.io.IOException
    {
		Builder builder = null;
		if (filename.endsWith(".html"))
		{
			builder = new HtmlBuilder();
		}
		else if (filename.endsWith(".xml"))
		{
			builder = new XmlBuilder();
		}
		else 
		{
			builder = new TextBuilder();
		}
		builder.build(filename, d);
		
    }
 
	/**
	 * 
	 * The command line interface to zipdiff utility
	 * 
	 * @param args The command line parameters
	 * 
	 */
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

            DifferenceCalculator calc = new DifferenceCalculator(f1, f2);

            String regularExpression = null;

            // todo - calc.setFilenamesToIgnore();

            if (line.hasOption(OPTION_COMPARE_CRC_VALUES)) {
                calc.setCompareCRCValues(true);
            } else {
                calc.setCompareCRCValues(false);
            }

            if (line.hasOption(OPTION_IGNORE_CVS_FILES)) {
            	calc.setIgnoreCVSFiles(true);
            } else {
            	calc.setIgnoreCVSFiles(false);
            }
            
            if (line.hasOption(OPTION_COMPARE_TIMESTAMPS)) {
                calc.setIgnoreTimestamps(false);
            } else {
                calc.setIgnoreTimestamps(true);
            }

            if (line.hasOption(OPTION_REGEX)) {
                regularExpression = line.getOptionValue(OPTION_REGEX);
                Set regexSet = new HashSet();
                regexSet.add(regularExpression);

                calc.setFilenameRegexToIgnore(regexSet);
            }
            
            boolean exitWithErrorOnDiff = false;
            if (line.hasOption(OPTION_EXIT_WITH_ERROR_ON_DIFF)) {
            	exitWithErrorOnDiff = true;
            }
            
            Differences d = calc.getDifferences();
            
            if (line.hasOption(OPTION_OUTPUT_FILE))
            {
            	String outputFilename = line.getOptionValue(OPTION_OUTPUT_FILE);
            	writeOutputFile(outputFilename, d);
            }
            
            
            if (d.hasDifferences()) {
            	if (line.hasOption(OPTION_VERBOSE)) {
					System.out.println(d);
					System.out.println(d.getFilename1() + " and " + d.getFilename2() + " are different.");
            	}
                if (exitWithErrorOnDiff) {
                	System.exit(EXITCODE_DIFF); 
                }
            } else {
                System.out.println("No differences found.");
            }
        } catch (ParseException pex) {
            System.err.println(pex.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("zipdiff.Main [options] ", options);
            System.exit(EXITCODE_ERROR);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(EXITCODE_ERROR);
        }

    }

}
