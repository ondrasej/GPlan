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

import cz.matfyz.sykora.planning.*;
import cz.matfyz.sykora.planning.graph.*;
import cz.matfyz.sykora.planning.predicate.*;
import junit.framework.TestCase;

public class PredicateLayerTest extends TestCase {
	public void testBuildActionLayer() {
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
		for(Action act : layer)
			System.out.println(act.toString());
		
		System.out.println();
		System.out.println("Mutexes:");
		for(ActionLayer.ActionPair pair : layer.getMutexPairs()) {
			System.out.println(pair.getFirst().toString() + " --- " + pair.getSecond().toString());
		}
	}
	
	public void testContains() {
		PredicateLayer layer = new PredicateLayer();
		
		layer.add(Predicate.parse("at(home)."));
		layer.add(Predicate.parse("at(school)."));
		layer.add(Predicate.parse("at(work)."));
		layer.add(Predicate.parse("mam(brambory)."));
		layer.add(Predicate.parse("mam(pravitko)."));
		layer.add(Predicate.parse("mam(penize)."));
		
		layer.addMutex(layer.getPredicate(0), layer.getPredicate(1));
		layer.addMutex(layer.getPredicate(1), layer.getPredicate(2));
		layer.addMutex(layer.getPredicate(0), layer.getPredicate(2));
		layer.addMutex(layer.getPredicate(3), layer.getPredicate(5));
		layer.addMutex(layer.getPredicate(4), layer.getPredicate(5));
		
		assertTrue(layer.contains(Predicate.parse("at(home).")));
		assertTrue(layer.contains(Predicate.parse("mam(brambory).")));
		assertFalse(layer.contains(Predicate.parse("mam(dost-casu).")));
		assertFalse(layer.contains(Predicate.parse("mam.")));
		
		PredicateSet contained = new PredicateSet();
		contained.add(Predicate.parse("at(home)."));
		contained.add(Predicate.parse("mam(brambory)."));
		contained.add(Predicate.parse("mam(pravitko)."));
		
		assertTrue(layer.contains(contained));
		
		PredicateSet not_contained = new PredicateSet();
		not_contained.add(Predicate.parse("at(home)."));
		not_contained.add(Predicate.parse("at(school)."));
		assertFalse(layer.contains(not_contained));
	}
}
