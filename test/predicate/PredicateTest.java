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

import cz.matfyz.sykora.planning.predicate.*;

import junit.framework.TestCase;

public class PredicateTest extends TestCase {
	public void testUnifyWith_Simple() {
		Predicate first = new Predicate("predikat",
				new ValueBinding[] {
					new ValueBinding("prvni-hodnota"),
					new ValueBinding("druha-hodnota")
		});
		Predicate second = new Predicate("jiny-predikat", 2);
		Predicate third = new Predicate("predikat", 2);
		
		assertFalse(second.unifyWith(first, null));
		assertTrue(third.unifyWith(first, null));
		
		System.out.println(first.toString());
		System.out.println(second.toString());
		System.out.println(third.toString());
	}
	
	public void testUnifyWith_Advanced() {
		ValueBinding binding = new ValueBinding();
		Predicate first = new Predicate("predikat",
				new ValueBinding[] {
					binding, binding
		});
		Predicate second = new Predicate("jiny-predikat",
				new ValueBinding[] {
					new ValueBinding("prvni-hodnota"),
					new ValueBinding("druha-hodnota")
		});
		Predicate third = new Predicate("predikat",
				new ValueBinding[] {
					new ValueBinding("jedina-hodnota"),
					new ValueBinding("jedina-hodnota")
		});
		
		assertFalse(first.unifyWith(second, null));
		assertTrue(first.unifyWith(third, null));
	}
}
