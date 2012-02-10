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
import junit.framework.TestCase;

import cz.matfyz.sykora.planning.*;
import cz.matfyz.sykora.planning.predicate.*;

public class PlanningProblemTest extends TestCase {
	public void testHanoiTowers() throws Exception {
		String planning_problem_string =
			"presun :: vetsi(X, Y), volna(X), volna(Y), na(X, Z) => volna(Z), not volna(Y), not na(X, Z), na(X, Y)." +
			
			"vetsi(k1,k2)." +
			"vetsi(k1,k3)." +
			"vetsi(k1,k4)." +
			"vetsi(k2,k3)." +
			"vetsi(k2,k4)." +
			"vetsi(k3,k4)." +
			"vetsi(k1,d1)." +
			"vetsi(k2,d1)." +
			"vetsi(k3,d1)." +
			"vetsi(k4,d1)." +
			"vetsi(k1,d2)." +
			"vetsi(k2,d2)." +
			"vetsi(k3,d2)." +
			"vetsi(k4,d2)." +
			"vetsi(k1,d3)." +
			"vetsi(k2,d3)." +
			"vetsi(k3,d3)." +
			"vetsi(k4,d3)." +
			
			"na(k1,k2)." +
			"na(k2,k3)." +
			"na(k3,k4)." +
			"na(k4,d1)." +
			"volna(k1)." +
			"volna(d2)." +
			"volna(d3)." +
			
			"goal na(k1,k2)." +
			"goal na(k2,k3)." +
			"goal na(k3,k4)." +
			"goal na(k4,d3)." +
			"goal volna(k1)." +
			"goal volna(d1)." +
			"goal volna(d2).";
	
		String planning_problem_string3 =
			"presun :: vetsi(X, Y), volna(X), volna(Y), na(X, Z) => volna(Z), not volna(Y), not na(X, Z), na(X, Y)." +
			
			"vetsi(k1,k2)." +
			"vetsi(k1,k3)." +
			"vetsi(k2,k3)." +
			"vetsi(k1,d1)." +
			"vetsi(k2,d1)." +
			"vetsi(k3,d1)." +
			"vetsi(k1,d2)." +
			"vetsi(k2,d2)." +
			"vetsi(k3,d2)." +
			"vetsi(k1,d3)." +
			"vetsi(k2,d3)." +
			"vetsi(k3,d3)." +
			
			"na(k1,k2)." +
			"na(k2,k3)." +
			"na(k3,d1)." +
			"volna(k1)." +
			"volna(d2)." +
			"volna(d3)." +
			
			"goal na(k1,k2)." +
			"goal na(k2,k3)." +
			"goal na(k3,d3)." +
			"goal volna(k1)." +
			"goal volna(d1)." +
			"goal volna(d2).";
		
		System.out.println("Hanoi towers (3)");
		System.out.print("Initializing planning problem... ");
		PlanningProblem problem = new PlanningProblem(true);
		problem.initialize(planning_problem_string);
		System.out.println("done");
		
		System.out.print("Performing steps");
		long time = System.currentTimeMillis();
		problem.solve();
		System.out.println("Solved in " + (System.currentTimeMillis() - time) + " milliseconds.");
		System.out.println();
	}
	
	public void testSingleStep() {
		ActionList actions = new ActionList();
		actions.add(Action.parse("kup :: mam(penize),zbozi(X) => not mam(penize),mam(X)."));
		actions.add(Action.parse("prodej :: mam(X),zbozi(X) => not mam(X),mam(penize)."));
		
		PredicateSet initial_state = new PredicateSet();
		initial_state.add(Predicate.parse("mam(orezavatko)."));
		initial_state.add(Predicate.parse("zbozi(orezavatko)."));
		initial_state.add(Predicate.parse("zbozi(brambory)."));
		
		PredicateSet goals = new PredicateSet();
		goals.add(Predicate.parse("mam(brambory)."));
		
		System.out.println("Simple shopping");
		System.out.print("Initializing planning problem... ");
		PlanningProblem problem = new PlanningProblem(true);
		problem.initialize(actions, initial_state, goals);
		System.out.println("done");
		
		System.out.print("Performing steps");
		int counter = 0;
		while(problem.singleStep()) {
			System.out.println("Step #" + (++counter) + " finished");
			if(problem.isGoalsFulfilled()) {
				System.out.println("Layered plan was found.");
				ActionList plan = problem.findSerialPlan();
				System.out.println(plan != null ? "Serial plan was found : " + plan.toString() : "Serial plan was not found");
				break;
			}
		}
		if(!problem.isGoalsFulfilled()) {
			System.out.println("Layered plan was not found.");
		}
	}
	
	public static void main(String[] _args) {
		PlanningProblemTest test = new PlanningProblemTest();
		try {
//			test.testHanoiTowers();
			test.testSingleStep();
		}
		catch(Exception err) {
			
		}
	}
}
