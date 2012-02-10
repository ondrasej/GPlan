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

import java.util.*;

import cz.matfyz.sykora.planning.*;
import cz.matfyz.sykora.planning.graph.*;
import cz.matfyz.sykora.planning.predicate.*;

public class ActionTest extends TestCase {
	public void testFindPossibleInstances() {
		Action go_home_action =  Action.parse("go-home :: at(X) => not at(X), at(home).");
		Action go_nowhere_action = Action.parse("go-nowhere :: at(X), at(Y) => not at(school), not at(work).");
		
		PredicateLayer start_layer = new PredicateLayer();
		start_layer.add(Predicate.parse("at(work)."));
		start_layer.add(Predicate.parse("at(school)."));
		start_layer.add(Predicate.parse("at(home)"));
		
		start_layer.addMutex(start_layer.getPredicate(0), start_layer.getPredicate(1));
		start_layer.addMutex(start_layer.getPredicate(1), start_layer.getPredicate(2));
		
		TreeSet<Action> result = new TreeSet<Action>();
		go_home_action.findPossibleInstances(start_layer, result);
		go_nowhere_action.findPossibleInstances(start_layer, result);
		
		for(Action action : result)
			System.out.println(action.toString());
	}
	
	public static void main(String[] _args) {
		ActionTest test = new ActionTest();
		test.testFindPossibleInstances();
	}
}
