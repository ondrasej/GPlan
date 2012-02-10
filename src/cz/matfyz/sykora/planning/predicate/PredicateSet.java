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
package cz.matfyz.sykora.planning.predicate;

import java.util.*;

/**
 * Class used for storing set of predicates.
 *
 *	@author Ondra Sykora [ondrasej@matfyz.cz]
 */
public class PredicateSet implements Comparable<PredicateSet>, Iterable<Predicate> {
	/**
	 * List of predicates in the set.
	 */
	private ArrayList<Predicate> predicates;
	
	/**
	 * Adds a single predicate to the set.
	 *
	 *	@param _predicate predicate that should be added to the set.
	 *	@see #add(Iterable)
	 */
	public void add(Predicate _predicate) {
		if(_predicate == null)
			throw new NullPointerException();
		predicates.add(_predicate);
	}
	
	/**
	 * Adds a collection of predicates to the set.
	 *
	 *	@param _predicates collection of predicates that should be added to
	 *		the set.
	 *	@see #add(Predicate)
	 */
	public void add(Iterable<Predicate> _predicates) {
		for(Predicate predicate : _predicates)
			add(predicate);
	}
	
	/**
	 * Removes all predicates from the set.
	 */
	public void clear() {
		predicates.clear();
	}
	
	/**
	 * Clears variable bindings in all predicates in the set.
	 * 
	 *	@see Predicate#clearBindings()
	 */
	public void clearBindings() {
		for(Predicate predicate : predicates)
			predicate.clearBindings();
	}
	
	public int compareTo(PredicateSet _other) {
		if(!(_other instanceof PredicateSet))
			throw new ClassCastException("PredicateSet can only be compared to an other PredicateSet.");
		
		int tmp = 0;
		PredicateSet other = (PredicateSet)_other;
		int pred_count = Math.min(getPredicateCount(),
				other.getPredicateCount());
		for(int i=0; i < pred_count; i++) {
			tmp = getPredicate(i).compareTo(other.getPredicate(i));
			if(tmp != 0)
				return tmp;
		}
		if(getPredicateCount() > pred_count)
			return -1;
		else if(other.getPredicateCount() > pred_count)
			return 1;
		return 0;
	}
	
	/**
	 * Tests if this set contains a specified predicate.
	 * 
	 *	@param _predicate tested predicate.
	 *	@return true if the set contains the predicate <i>_predicate</i>.
	 */
	public boolean contains(Predicate _predicate) {
		return predicates.contains(_predicate);
	}
	
	/**
	 * Tests if a set of predicates is subset of this set.
	 * 
	 * 	@param _predicate_set testes set of predicates.
	 * 	@return true if all predicates from <i>_predicate_set</i> are contained
	 * 		in this set.
	 */
	public boolean contains(PredicateSet _predicate_set) {
		for(Predicate single : _predicate_set)
			if(!contains(single))
				return false;
		return true;
	}
	
	/**
	 * Getter for a single predicate in the list.
	 * 
	 *	@param _index index of the predicate.
	 *	@return predicate at index <i>_index</i>.
	 */
	public Predicate getPredicate(int _index) {
		return predicates.get(_index);
	}
	
	/**
	 * Returns number of predicates in the set.
	 * 
	 *	@return number of predicates in the set.
	 */
	public int getPredicateCount() {
		return predicates.size();
	}
	
	/**
	 * Initializes internal structures of the predicate set.
	 * 
	 *	@see #predicates
	 */
	private void initialize() {
		predicates = new ArrayList<Predicate>();
	}
	
	/**
	 * Returns iterator for enumeration of all predicates in the set.
	 *
	 *	@see #predicates
	 */
	public Iterator<Predicate> iterator() {
		return predicates.iterator();
	}
	
	/**
	 * Tests if all predicates in the set are grounded.
	 * 
	 *	@return true if all predicates in the set are grounded, false
	 *		otherwise.
	 *	@see Predicate#isGrounded()
	 */
	public boolean isGrounded() {
		for(Predicate predicate : predicates) {
			if(!predicate.isGrounded())
				return false;
		}
		return true;
	}
	
	/**
	 * Removes a single predicate from the set.
	 * 
	 *	@param _predicate predicate that shoul be removed from the set.
	 */
	public void remove(Predicate _predicate) {
		if(_predicate == null)
			throw new NullPointerException();
		predicates.remove(_predicate);
	}
	
	/**
	 * Converts the predicate set to string representation. Returns all
	 * predicates in textual form, separated by commas.
	 *	
	 *	@see Predicate#toString()
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < predicates.size(); i++) {
			if(i > 0)
				builder.append(',');
			builder.append(predicates.get(i).toString());
		}
		return builder.toString();
	}
	
	/**
	 * Constructor. Initializes an empty predicate set.
	 */
	public PredicateSet() {
		initialize();
	}
}
