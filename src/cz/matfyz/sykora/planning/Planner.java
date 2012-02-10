/*
    Copyright 2006 Ondrej Sykora
 
    This file is part of GPlan.

    GPlan is free software: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GPlan is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GPlan.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.matfyz.sykora.planning;

import java.io.*;

import cz.matfyz.sykora.planning.predicate.*;

/**
 * Class containing the <i>main</i> method. This is only helper class used to
 * separate the command line utilities from the rest of the program.
 * 
 *	@author Ondra Sykora [ondrasej@matfyz.cz]
 */
public final class Planner {
	/**
	 * Prints info on usage of this application.
	 */
	public static void printUsage() {
		System.out.println("GraphPlan.jar");
		System.out.println("Usage: java -jar GraphPlan.jar [file]");
		System.out.println("   where file is planning problem specification in format described in the docs.");
	}
	
	/**
	 * Main function for running this application from command line.
	 *
	 *	@param _arguments list of command line arguments.
	 */
	public static void main(String[] _arguments) {
		if(_arguments.length == 0 || _arguments.length > 2) {
			printUsage();
			return;
		}
		
		boolean verbose = false;
		if(_arguments.length == 2) {
			if(!_arguments[0].equals("-v")) {
				printUsage();
				return;
			}
			verbose = true;
		}
		
		File file = new File(_arguments[_arguments.length - 1]);
		if(!file.exists() || !file.isFile() || !file.canRead()) {
			System.out.println("ERROR: File '" + _arguments[0] + "' does not exist or could not be read by the application");
			return;
		}
		
		PlanningProblem problem = new PlanningProblem(verbose);
		ActionList plan = null;
		try {
			problem.initialize(new FileReader(file));
			long start_time = System.currentTimeMillis();
			plan = problem.solve();
			long end_time = System.currentTimeMillis();
			
			System.out.println("Solved in " + (end_time - start_time) + " milliseconds.");
		}
		catch(PredicateParser.TokenException token_err) {
			System.out.println("ERROR: File format error: " + token_err.getMessage());
		}
		catch(IOException io_err) {
			System.out.println("ERROR: Input/output error: " + io_err.getMessage());
		}
		
		if(plan != null) {
			for(int i = 0; i < plan.size(); i++) {
				System.out.println(plan.get(i).toString());
			}
		}
		else {
			System.out.println("Serial plan does not exist. Planning failed.");
		}
	}
}
