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
import cz.matfyz.sykora.planning.predicate.Predicate;

/**
 * Action layer in the planning graph.
 * 
 *	@author Ondra Sykora [ondrasej@matfyz.cz]
 */
public class ActionLayer implements Iterable<Action> {
	
	/**
	 * Helper class used to store (ordered) pairs of actions. This is used to
	 * represent action mutexes.
	 *	
	 *	@see ActionLayer#mutexes
	 */
	public static class ActionPair implements Comparable<ActionPair> {
		/**
		 * First action in the pair.
		 *
		 *	@see #getFirst()
		 */
		private Action first;
		/**
		 * Second action in the pair.
		 *
		 *	@see #getSecond()
		 */
		private Action second;
		
		/**
		 * Compares this action with another one.
		 *
		 *	@param _next the other action.
		 *	@return -1 if this action is greater than the other, 0 if they are
		 *		equal and 1 if this action is lower than the other one.
		 */
		public int compareTo(ActionPair _next) {
			ActionPair next = (ActionPair)_next;
			int res = getFirst().compareTo(next.getFirst());
			if(res != 0)
				return res;
			return getSecond().compareTo(next.getSecond());
		}
		
		/**
		 * Returns first action in the pair.
		 *
		 *	@return first action in the pair.
		 */
		public Action getFirst() {
			return first;
		}
		
		/**
		 * Returns second action in the pair.
		 * 
		 *	@return second action in the pair.
		 */
		public Action getSecond() {
			return second;
		}
		
		/**
		 * Reinitializes this pair for new actions. This is used as
		 * optimization for mutex lookup.
		 * 
		 *	@param _first new first action for this pair.
		 *	@param _second new second action for this pair.
		 *	@see ActionLayer#isMutex(Action, Action)
		 */
		public void reset(Action _first, Action _second) {
			first = _first;
			second = _second;
		}
		
		/**
		 * Converts the action pair to string.
		 *
		 *	@return string representation of the action pair.
		 */
		public String toString() {
			return getFirst().toString() + " --- " + second.toString() + "\n";
		}
		
		/**
		 * Creates a new action pair with specified actions.
		 *
		 *	@param _first first action.
		 *	@param _second second action.
		 */
		public ActionPair(Action _first, Action _second) {
			first = _first;
			second = _second;
		}
	}
	
	/**
	 * List of actions in this layer.
	 *
	 *	@see #addAction(Action)
	 */
	private ArrayList<Action> actions;
	/**
	 * List of action mutexes in this layer.
	 *
	 *	@see #addMutex(Action, Action)
	 *	@see #findMutexActions()
	 *	@see #getMutexPairs()
	 *	@see #isMutex(Action, Action)
	 */
	private TreeSet<ActionPair> mutexes;
	
	/**
	 * Adds an action to this layer.
	 *
	 *	@param _action action that is added to the layer.
	 */
	public void addAction(Action _action) {
		actions.add(_action);
	}
	
	/**
	 * Adds a mutex between two actions.
	 * 
	 * @param _first first action in the mutex.
	 * @param _second second action in the mutex.
	 */
	public void addMutex(Action _first, Action _second) {
		mutexes.add(new ActionPair(_first, _second));
	}
	
	/**
	 * Builds predicate layer from (positive) effects of actions in this layer.
	 *
	 *	@param _support if non-null, this map is used to store mapping from
	 *		predicates to actions that support them.
	 *	@return a new predicate layer based on effects of actions in this
	 *		layer.
	 */
	public PredicateLayer buildPredicateLayer(Map<Predicate, ActionList> _support) {
		TreeSet<Predicate> predicates = new TreeSet<Predicate>();
		Map<Predicate, ActionList> support = _support != null ? _support : new TreeMap<Predicate, ActionList>();
		
		// find all positive effects of actions in this layer
		for(Action action : this) {
			for(Predicate predicate : action.getPositiveEffects()) {
				if(!predicates.contains(predicate))
					predicates.add(predicate);
				ActionList list = support.get(predicate);
				if(list == null) {
					list = new ActionList();
					support.put(predicate, list);
				}
				list.add(action);
			}
		}
		
		PredicateLayer result = new PredicateLayer();
		for(Predicate predicate : predicates)
			result.add(predicate);
		
		// add mutexes for predicate pairs such that all action pairs that
		// support them are mutex.
		for(Predicate first : predicates) {
			for(Predicate second : predicates) {
				ActionList actions_first = support.get(first);
				ActionList actions_second = support.get(second);
				
				boolean found_support = false;
				
				actions:
				for(Action first_action : actions_first)
					for(Action second_action : actions_second) {
						if(!isMutex(first_action, second_action)) {
							found_support = true;
							break actions;
						}
					}
				if(!found_support)
					result.addMutex(first, second);
			}
		}
		
		return result;
	}
	
	/**
	 * Finds actions that are mutex because of their effects and preconditions
	 * (dependent actions).
	 */
	public void findMutexActions() {
		for(Action first : this) {
			for(Action second : this) {
				if(first.equals(second))
					continue;
				next_action:
				for(Predicate predicate_first : first.getNegativeEffects()) {
					for(Predicate predicate_second : second.getPositiveEffects())
						if(predicate_first.equals(predicate_second)
								&& !isMutex(first, second)) {
							addMutex(first, second);
							break next_action;
						}
					for(Predicate predicate_second : second.getPreconditions()) {
						if(predicate_first.equals(predicate_second)
								&& !isMutex(first, second)) {
							addMutex(first, second);
							break next_action;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Provides access to list of mutex pairs in this layer.
	 *
	 *	@return list of mutex pairs in this layer.
	 */
	public Set<ActionPair> getMutexPairs() {
		return mutexes;
	}
	
	/**
	 * Initializes internal structures of this class.
	 */
	private void initialize() {
		actions = new ArrayList<Action>();
		mutexes = new TreeSet<ActionPair>();
	}
	
	private ActionPair isMutexHelperPair = new ActionPair(null, null);
	
	/**
	 * Tests if two of the actions in the layer are muex.
	 *
	 *	@param _first first tested action.
	 *	@param _second second tested action.
	 *	@return true if the two actions are mutex in this layer.
	 */
	public boolean isMutex(Action _first, Action _second) {
		isMutexHelperPair.reset(_first, _second);
		if(mutexes.contains(isMutexHelperPair))
			return true;
		isMutexHelperPair.reset(_second, _first);
		return mutexes.contains(isMutexHelperPair);
//		return mutexes.contains(new ActionPair(_first, _second))
//			|| mutexes.contains(new ActionPair(_second, _first));
	}
	
	/**
	 * Returns iterator for all actions in this layer.
	 *
	 *	@return iterator for all actions in this layer.
	 */
	public Iterator<Action> iterator() {
		return actions.iterator();
	}
	
	/**
	 * Constructor. Creates a new empty action layer.
	 *
	 */
	public ActionLayer() {
		initialize();
	}
	
	/**
	 * Constructor. Creates a new action layer containing specified actions.
	 * 
	 *	@param _actions actions for the new action layer.
	 */
	public ActionLayer(Iterable<Action> _actions) {
		initialize();
		for(Action action : _actions)
			addAction(action);
	}
}
