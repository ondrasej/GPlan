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
package cz.matfyz.sykora.planning.graph;

import java.util.*;

import cz.matfyz.sykora.planning.*;
import cz.matfyz.sykora.planning.predicate.*;

/**
 * Representation of predicate layer in the planning graph.
 * 
 * The layer is implemented as a set of predicates with added support for
 * predicate mutexes.
 * 
 * @author Ondra Sykora [ondrasej@matfyz.cz]
 */
public class PredicateLayer extends PredicateSet {
	
	/**
	 * Helper class that holds predicate pairs.
	 *
	 *	@see PredicateLayer#mutexes
	 */
	public static class PredicatePair implements Comparable<PredicatePair> {
		/**
		 * Firs predicate in the pair.
		 *
		 *	@see #getFirst()
		 */
		private Predicate first;
		/**
		 * Second predicate in the pair.
		 *
		 *	@see #getSecond()
		 */
		private Predicate second;
		
		/**
		 * Compares two predicate pairs. The comparsion is done
		 * "lexicographically" - first predicates at first, second predicates
		 * later.
		 * 
		 *	@param _next the other predicate pair.
		 *	@return -1 if this predicate pair is lower than the other, 0 if
		 *		they are equal and 1 if the other predicate is greater.
		 *	@see Predicate#compareTo(Object)
		 */
		public int compareTo(PredicatePair _next) {
			if(!(_next instanceof PredicatePair))
				throw new ClassCastException("PredicatePair can only be compared to a PredicatePair");
			
			PredicatePair next_pair = (PredicatePair)_next;
			int tmp = first.compareTo(next_pair.getFirst());
			if(tmp != 0)
				return tmp;
			return second.compareTo(next_pair.getSecond());
		}
		
		/**
		 * Tests this predicate pair with an other object for equality.
		 * Predicate pairs are equal if and only if both their first and second
		 * predicates are equal.
		 *
		 *	@param _next the other predicate pair.
		 *	@return true if the other predicate pair is equal to this one,
		 *		false is equal to this one.
		 * 	@see Predicate#equals(Object)
		 */
		public boolean equals(Object _next) {
			if(!(_next instanceof PredicatePair))
				return false;
			return compareTo((PredicatePair)_next) == 0;
		}
		
		/**
		 * Returns first predicate in the pair.
		 *
		 *	@return first predicate in the pair.
		 * 	@see #first
		 */
		public final Predicate getFirst() {
			return first;
		}
		
		/**
		 * Returns second predicate in the pair.
		 *	
		 *	@return second predicate in the pair.
		 *	@see #second
		 */
		public final Predicate getSecond() {
			return second;
		}
		
		/**
		 * Resets this predicate to specified predicates. This is used as
		 * optimization to prevent unnecessary allocaions in the <i>isMutex</i>
		 * method.
		 * 
		 *	@param _first new first predicate for this pair.
		 *	@param _second new second predicate for this pair.
		 */
		public void reset(Predicate _first, Predicate _second) {
			first = _first;
			second = _second;
		}
		
		/**
		 * Converts the predicate pair to it's string representation.
		 *
		 *	@return string retpresentation of the predicate pair.
		 *	@see Predicate#toString()
		 */
		public String toString() {
			return first.toString() + " - " + second.toString();
		}
		
		/**
		 * Constructor. Creates a new instance of the predicate pair.
		 *
		 *	@param _first first predicate in the pair.
		 *	@param _second second predicate in the pair.
		 */
		public PredicatePair(Predicate _first, Predicate _second) {
			first = _first;
			second = _second;
		}
	}
	
	/**
	 * List of mutexes in this layer.
	 * 
	 * The list is implemented as binary tree to enable fast searches.
	 * 
	 *	@see #addMutex(Predicate, Predicate)
	 *	@see #isMutex(Predicate, Predicate)
	 *	@see #getMutexPairs()
	 */
	public TreeSet<PredicatePair> mutexes;
	
	/**
	 * Adds a new mutex to the list.
	 * 
	 *	@param _first first predicate in the mutex.
	 *	@param _second second predicate in the mutex.
	 */
	public void addMutex(Predicate _first, Predicate _second) {
		if(isMutex(_first, _second))
			return;
		mutexes.add(new PredicatePair(_first, _second));
	}
	
	/**
	 * Builds action layer upon this predicate layer.
	 * 
	 *	@param _actions list of action schemas from that action instances are
	 *		generated.
	 *	@return a new action layer based on predicates in this layer
	 *	@see PlanningProblem#buildNextLayer()
	 */
	public ActionLayer buildActionLayer(Iterable<Action> _actions) {
		TreeSet<Action> res_actions = new TreeSet<Action>();
		TreeMap<Predicate, ActionList> dependencies = new TreeMap<Predicate, ActionList>();
		
		for(Action action : _actions)
			action.findPossibleInstances(this, res_actions, dependencies);
		for(Predicate predicate : this) {
			Action no_op = Action.noOpAction(predicate);
			if(!res_actions.contains(no_op))
				res_actions.add(no_op);
			ActionList support = dependencies.get(predicate);
			if(support == null) {
				support = new ActionList();
				dependencies.put(predicate, support);
			}
			support.add(no_op);
		}
	
		ActionLayer res_layer = new ActionLayer(res_actions);

		for(PredicatePair mutex : mutexes) {
			ActionList first_list = dependencies.get(mutex.getFirst());
			ActionList second_list = dependencies.get(mutex.getSecond());
			
			if(first_list == null || second_list == null)
				continue;
			for(Action first_action : first_list)
				for(Action second_action : second_list)
					res_layer.addMutex(first_action, second_action);
		}
		
		res_layer.findMutexActions();
		return res_layer;
	}
	
	/**
	 * Tests if the predicate layer contains specified set of predicates and
	 * that there is no mutex between any two of them.
	 *
	 *	@param _predicate_set tested predicate set.
	 *	@return true if all predicates from <i>_predicate_set</i> are contained
	 *		in this layer and there is no mutex between ano two of them, false
	 *		otherwise.
	 */
	public boolean contains(PredicateSet _predicate_set) {
		if(!super.contains(_predicate_set))
			return false;
		for(Predicate first : _predicate_set)
			for(Predicate second : _predicate_set)
				if(isMutex(first, second))
					return false;
		return true;
	}
	
	/**
	 * Tests two predicate layers for equality. Two layers are equal if they
	 * have the same predicates and mutexes betwen the same pairs of
	 * predicates.
	 * 
	 *	@param _other the other predicate layer.
	 *	@return true if the other predicate layer is equal to this one as
	 *		described above.
	 *	@see PlanningProblem#singleStep()
	 */
	public boolean equals(Object _other) {
		if(!(_other instanceof PredicateLayer))
			return false;
		PredicateLayer other = (PredicateLayer)_other;
		if(getPredicateCount() != other.getPredicateCount())
			return false;
		if(getMutexPairs().size() != other.getMutexPairs().size())
			return false;
		for(Predicate predicate : this)
			if(!other.contains(predicate))
				return false;
		for(PredicatePair pair : mutexes)
			if(!other.isMutex(pair.first, pair.second))
				return false;
		for(PredicatePair pair : other.getMutexPairs())
			if(!isMutex(pair.getFirst(), pair.getSecond()))
				return false;
		return true;
	}
	
	/**
	 * Provides access to set of mutexes in this predicate layer.
	 * 
	 *	@return list of mutexes in this predicate layer.
	 */
	public Set<PredicatePair> getMutexPairs() {
		return mutexes;
	}
	
	/**
	 * Initializes internal structures of this layer.
	 */
	private void initialize() {
		mutexes = new TreeSet<PredicatePair>();
	}
	
	private PredicatePair isMutexHelperPair = new PredicatePair(null, null);
	
	/**
	 * Tests if there is mutex between two predicates in this predicate layer.
	 * 
	 *	@param _first first predicate.
	 *	@param _second second predicate.
	 *	@return true if there is mutex between <i>_first</i> and
	 *		<i>_second</i>, false otherwise.
	 */
	public boolean isMutex(Predicate _first, Predicate _second) {
		isMutexHelperPair.reset(_first, _second);
		if(mutexes.contains(isMutexHelperPair))
			return true;
		isMutexHelperPair.reset(_second, _first);
		return mutexes.contains(isMutexHelperPair);
	}
	
	/**
	 * Constructor. Creates an empty predicate layer.
	 *
	 *	@see #initialize()
	 */
	public PredicateLayer() {
		initialize();
	}
}
