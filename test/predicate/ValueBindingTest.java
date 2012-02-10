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

import junit.framework.TestCase;
import cz.matfyz.sykora.planning.predicate.*;

public class ValueBindingTest extends TestCase {
	
	public void testSetValueSimple() {
		ValueBinding binding = new ValueBinding();
		
		assertNull("binding.getValue() must return null as it has no value assigned yet",
				binding.getValue());
		assertFalse("binding.isBound() must return false as it has no value assigned yet",
				binding.isBound());
		
		binding.bind("hello");
		assertTrue("binding.isBound() must return true because we already assigned value to it",
				binding.isBound());
		assertTrue("binding is bound to other value than we passed to binding.setValue(...)",
				binding.getValue().equals("hello"));
	}
	
	public void testSetValueForBoundVariables() {
		ValueBinding first = new ValueBinding();
		ValueBinding second = new ValueBinding();
		ValueBinding third = new ValueBinding();
		
		second.bind(first);
		third.bind(second);
		
		assertFalse("isBound() must return false for all bindings as we have not set value for any of them",
				first.isBound() || second.isBound() || third.isBound());
		
		first.bind("hello world");
		assertTrue("isBound() must return true because all bindings are bound and we set value for one of them",
				first.isBound() && second.isBound() && third.isBound());
		
		assertEquals(third.getValue(), "hello world");
		
		// let's try it with different topology
		ValueBinding b1 = new ValueBinding();
		ValueBinding b2 = new ValueBinding();
		ValueBinding b3 = new ValueBinding();
		ValueBinding b4 = new ValueBinding();
		
		b1.bind(b2);
		b3.bind(b4);
		b2.bind(b4);
		
		b1.bind("foo");
		assertEquals(b3.getValue(), "foo");
	}
}
