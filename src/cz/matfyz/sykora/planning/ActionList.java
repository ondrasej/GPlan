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
package cz.matfyz.sykora.planning;

import java.util.*;

import cz.matfyz.sykora.planning.predicate.*;

/**
 * List of actions. Adds functions specific for lists of actions.
 * 
 *	@author Ondra Sykora [ondrasej@centrum.cz]
 */
public class ActionList extends ArrayList<Action> {
	/**
	 * Enumerates preconditions of all actions in the list.
	 *
	 *	@return union of precondition predicate sets for all actions in the
	 *		list.
	 */
	public PredicateSet getPreconditions() {
		PredicateSet result = new PredicateSet();
		for(Action action : this) {
			for(Predicate precondition : action.getPreconditions()) {
				if(!result.contains(precondition))
					result.add(precondition);
			}
		}
		return result;
	}
	
	/**
	 * Creates a copy of this list without the no-op actions. This method is
	 * used to remove all unnecessary items from the serial plan.
	 *
	 *	@return a copy of this list with all no-op actions removed.
	 */
	public ActionList getPureList() {
		ActionList result = new ActionList();
		for(int pos = 0; pos < size(); pos++) {
			Action action = get(pos);
			if(!action.getActionName().equals("no-op"))
				result.add(action);
		}
		return result;
	}
}
