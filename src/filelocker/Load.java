package filelocker;

import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Load {

	public static void main(String[] args) throws IOException {
		FileLocker test = new FileLocker();
		String file = null;
		try {
            Options options = new Options();

            options.addOption("h"   ,false, "Print help for file storing");
            options.addOption("file" ,true,  "The file name that needs to be loaded");

            BasicParser parser = new BasicParser();
            CommandLine cl = parser.parse(options, args);

            if( cl.hasOption('h') ) {
                HelpFormatter f = new HelpFormatter();
                f.printHelp("Help information", options);
           		return;
            }
            else{
                file = cl.getOptionValue("file");
            }
        }
        catch(ParseException e) {
            e.printStackTrace();
        }

		try {
			// Load the file to output folder from the locker
			if(test.loadFile("output\\" + file) > 0)
				System.out.println("The file " + file + " is loaded from file locker successfully!");
			test.closeDB();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
