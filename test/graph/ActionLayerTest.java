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
package graph;

import junit.framework.TestCase;

import cz.matfyz.sykora.planning.*;
import cz.matfyz.sykora.planning.graph.*;
import cz.matfyz.sykora.planning.graph.PredicateLayer.PredicatePair;
import cz.matfyz.sykora.planning.predicate.*;

public class ActionLayerTest extends TestCase {
/*	public void testBuildPredicateLayerMutexes() {
		PredicateLayer predicate_layer = new PredicateLayer();
		
		predicate_layer.add(Predicate.parse("at(home)."));
		predicate_layer.add(Predicate.parse("at(school)."));
		predicate_layer.add(Predicate.parse("at(obchod)."));
		predicate_layer.add(Predicate.parse("mam(brambory)."));
		predicate_layer.add(Predicate.parse("mam(pravitko)."));
		predicate_layer.add(Predicate.parse("misto(home)."));
		predicate_layer.add(Predicate.parse("misto(school)"));
		predicate_layer.add(Predicate.parse("misto(obchod)"));
		
		predicate_layer.addMutex(predicate_layer.getPredicate(0), predicate_layer.getPredicate(1));
		predicate_layer.addMutex(predicate_layer.getPredicate(1), predicate_layer.getPredicate(2));
		predicate_layer.addMutex(predicate_layer.getPredicate(0), predicate_layer.getPredicate(2));
		
		ActionList actions = new ActionList();
		actions.add(Action.parse("go-to :: at(X), misto(Y), distinct(X, Y) => not at(X), at(Y)."));
		actions.add(Action.parse("prodej :: at(obchod), mam(X) => not mam(X), mam(penize)."));
		
		ActionLayer layer = predicate_layer.buildActionLayer(actions);
		
		System.out.println("Actions: ");
		for(Action act : layer)
			System.out.println(act.toString());
		
		System.out.println();
		System.out.println("Action mutexes:");
		for(ActionLayer.ActionPair pair : layer.getMutexPairs()) {
			System.out.println(pair.getFirst().toString() + " --- " + pair.getSecond().toString());
		}


		PredicateLayer result = layer.buildPredicateLayer();
		System.out.println("Predicate mutexes: ");
		for(PredicatePair pair : result.getMutexPairs()) {
			System.out.println(pair.getFirst().toString() + " --- " + pair.getSecond().toString());
		}
	}*/
	
	public void testBuildPredicateLayer() {
		ActionLayer source = new ActionLayer();
		
		source.addAction(Action.parse("go-to-obchod :: at(home) => at(obchod), not at(home)."));
		source.addAction(Action.parse("go-to-home :: at(obchod) => at(home), not at(obchod)."));
		
		source.findMutexActions();
		
		PredicateLayer result = source.buildPredicateLayer(null);
		for(Predicate predicate : result) {
			System.out.println(predicate.toString());
		}
		System.out.println("Predicate mutexes: ");
		for(PredicatePair pair : result.getMutexPairs()) {
			System.out.println(pair.getFirst().toString() + " --- " + pair.getSecond().toString());
		}
	}
}
