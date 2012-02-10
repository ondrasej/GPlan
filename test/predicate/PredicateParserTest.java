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
package predicate;

import cz.matfyz.sykora.planning.*;
import cz.matfyz.sykora.planning.predicate.*;
import junit.framework.TestCase;

public class PredicateParserTest extends TestCase {
	public void testParseAction() throws Exception {
		PredicateParser parser = new PredicateParser("go-home :: at(X) => not at(X), at(home).");
		
		Action act = parser.parseAction();
		
		assertEquals(
				(Object)act.getPreconditions().getPredicate(0).getParameter(0),
				(Object)act.getNegativeEffects().getPredicate(0).getParameter(0));
		
		System.out.println(act.toString());
		
		parser = new PredicateParser("buy :: needs(X), at(shop), has(money) => not has(money), has(X).");
		act = parser.parseAction();
		
		System.out.println(act.toString());
	}
	
	public void testParsePredicate() throws Exception {
		PredicateParser parser = new PredicateParser("hello(world).");
		
		Predicate res = parser.parsePredicate();
		assertEquals(res.getPredicateName(), "hello");
		assertTrue(res.getParameter(0).isBound());
		
		System.out.println(res.toString());
		
		parser = new PredicateParser("how-are-you.");
		
		res = parser.parsePredicate();
		assertEquals(res.getPredicateName(), "how-are-you");
		
		System.out.println(res.toString());
		
		parser = new PredicateParser("hello_worlds(World1, World1).");
		res = parser.parsePredicate();
		assertFalse(res.getParameter(0).isBound());
		assertFalse(res.getParameter(1).isBound());
		assertEquals((Object)res.getParameter(0), res.getParameter(1));
		
//		System.out.println(res.toString());
	}
}
