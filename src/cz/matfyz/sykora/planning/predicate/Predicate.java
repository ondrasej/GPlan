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

import java.io.IOException;
import java.util.*;

/**
 * Representation of a single predicate. Predicate is an entity that has a name
 * and a list of parameters, each of these may or may not be bound to a value.
 * 
 *	@author Ondra Sykora [ondrasej@matfyz.cz]
 */
public class Predicate implements Comparable<Predicate> {
	/**
	 * List of parameters of this predicate.
	 *	
	 *	@see #getParameter(int)
	 *	@see #getParameterCount()
	 */
	public ValueBinding[] parameters;
	
	/**
	 * Name of this predicate.
	 */
	private String predicateName;
	
	/**
	 * Clears all variable bindings in the predicate (resets all parameters to
	 * unbound state).
	 * 
	 *	@see #parameters
	 */
	public void clearBindings() {
		for(ValueBinding binding : parameters)
			binding.clear();
	}
	
	/**
	 * Compares two predicates. A lexicographical ordering is used - names of
	 * the predicates are compared first, then all parameters from the most
	 * left one to the most right one.
	 * 
	 *	@param _next object to that is the predicate compared. Predicate may
	 *		only be compared to other predicates.
	 *	@return -1 if this predicate is "lower" than the other one, 0 if they
	 *		are equal and 1 if this predicate is "greater" than the other one.
	 *	@throws ClassCastException if this object is compared to instance of
	 *		other class than Predicate
	 */
	public int compareTo(Predicate _next) {
		if(!(_next instanceof Predicate))
			throw new ClassCastException("Predicates only can be compared to other predicated");
		
		Predicate next = (Predicate)_next;
		int tmp = getPredicateName().compareTo(next.getPredicateName());
		if(tmp != 0)
			return tmp;
		int param_count = Math.min(getParameterCount(), next.getParameterCount());
		for(int param_index = 0; param_index < param_count; param_index++) {
			String first_param = getParameter(param_index).getValue();
			String second_param = next.getParameter(param_index).getValue();
			
			if(first_param != null && second_param != null)
				tmp = first_param.compareTo(second_param);
			else if(first_param != null)
				return 1;
			else if(second_param != null)
				return 0;
			else
				tmp = 0;
			
			if(tmp != 0)
				return tmp;
		}
		
		return tmp;
	}
	
	/**
	 * Tests the predicate for equality. Two predicates are equal if and only
	 * if their names are equal and all parameters are bound to equal values.
	 *
	 *	@param _object object to that is the predicate compared.
	 *	@return true if the predicates are equal, false otherwise.
	 */
	public boolean equals(Object _object) {
		if(!(_object instanceof Predicate))
			return false;
		Predicate predicate = (Predicate)_object;
		if(!predicateName.equals(predicate.predicateName))
			return false;
		if(parameters.length != predicate.parameters.length)
			return false;
		for(int i=0; i < parameters.length; i++) {
			if(!parameters[i].equals(predicate.parameters[i]))
				return false;
		}
		return true;
	}
	
	/**
	 * Returns parameter with index <i>_index</i>.
	 *
	 *	@param _index index of the requested parameter.
	 *	@return parameter with index <i>_index</i>.
	 *	@see #parameters
	 *	@see #getParameterCount()
	 */
	public final ValueBinding getParameter(int _index) {
		if(_index < 0 || _index >= parameters.length)
			throw new IndexOutOfBoundsException("_index is out of range");
		return parameters[_index];
	}
	
	/**
	 * Returns number of parameters of this predicate.
	 *
	 *	@return number of parameters of this predicate.
	 *	@see #parameters
	 */
	public final int getParameterCount() {
		return parameters.length;
	}
	
	/**
	 * Returns name of the predicate.
	 * 
	 *	@return name of the predicate.
	 *	@see #predicateName
	 */
	public String getPredicateName() {
		return predicateName;
	}
	
	/**
	 * Returns a clone of the predicate. This method reuqires that all
	 * parameters in the predicate are bound to some value.
	 * 
	 *	@return a new instance representing the same predicate with the same
	 *		parameters.
	 */
	public Predicate groundedClone() {
		ValueBinding[] parameters_clone = new ValueBinding[parameters.length];
		for(int param_index = 0; param_index < parameters_clone.length; param_index++) {
			assert(parameters[param_index] != null);
			assert(parameters[param_index].isBound());
			//TODO vyhodit nejakou vyjimku pokud promenne v predikatu nejsou vazane
			parameters_clone[param_index] = new ValueBinding(parameters[param_index].getValue());
		}
		return new Predicate(getPredicateName(), parameters_clone);
	}
	
	/**
	 * Initializes the predicate for specified number of parameters. All
	 * parameters are initialized as unbound and independent of each other.
	 *
	 *	@param _parameter_count number of parameters for this predicate.
	 */
	private void initialize(int _parameter_count) {
		parameters = new ValueBinding[_parameter_count];
		for(int i = 0; i < parameters.length; i++)
			parameters[i] = new ValueBinding();
	}
	
	/**
	 * Tests if all parameters are bound to solid values.
	 * 
	 *	@return true if all parameters are bound to solid values, false
	 *		otherwise.
	 */
	public boolean isGrounded() {
		for(ValueBinding binding : parameters) {
			if(!binding.isBound())
				return false;
		}
		return true;
	}
	
	/**
	 * Converts the predicate to it's string representation. Values are
	 * represented by it's string value, unbound parameters are represented
	 * as "$unbound" (not reflecting dependencies between them).
	 * 
	 *	@return string representation of the predicate.
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append(getPredicateName());
		if(getParameterCount() > 0) {
			builder.append('(');
			for(int param_index = 0; param_index < getParameterCount(); param_index++) {
				if(param_index > 0)
					builder.append(',');
				ValueBinding current_parameter = getParameter(param_index);
				if(current_parameter.isBound())
					builder.append(current_parameter.getValue());
				else
					builder.append("$unbound");
			}
			builder.append(')');
		}
		return builder.toString();
	}
	
	/**
	 * Performs simplified unification procedure on the predicate and on an
	 * other one. This method requires the other predicate to be grounded.
	 * 
	 *	@param _predicate predicate to unfiy with.
	 *	@param _bound_variables list of parameters that were bound during the
	 *		procedure.
	 *	@return true if unification of the two predicates was possible, false
	 *		otherwise.
	 */
	public boolean unifyWith(Predicate _predicate, List<ValueBinding> _bound_variables) {
		assert(_predicate != null);
		if(!getPredicateName().equals(_predicate.getPredicateName()))
			return false;
		ArrayList<ValueBinding> current_bingins = new ArrayList<ValueBinding>();
		for(int param_index = 0; param_index < getParameterCount(); param_index++) {
			ValueBinding parameter = getParameter(param_index);
			ValueBinding source = _predicate.getParameter(param_index);
			
			assert(source.isBound());
			if(parameter.isBound()) {
				if(!parameter.getValue().equals(source.getValue())) {
					// clear all values bound in this step before reporting failure
					for(ValueBinding binding : current_bingins)
						binding.clear();
					return false;
				}
			}
			else {
				if(!parameter.bind(source)) {
					// clear all values bound in this step before reporting failure
					for(ValueBinding binding : current_bingins)
						binding.clear();
					return false;					
				}
				current_bingins.add(parameter);
			}
		}
		if(_bound_variables != null)
			_bound_variables.addAll(current_bingins);
		return true;
	}
	
	/**
	 * Creates a new predicate from it's string representation. Excpects that
	 * unbound parameters start with a capital letter, solid values start with
	 * low letter.
	 * 
	 *	@param _predicate string representation of the predicate.
	 *	@return a new instance of the predicate based on it's string
	 *		representation or null if parsing fails.
	 */
	public static Predicate parse(String _predicate) {
		try {
			PredicateParser parser = new PredicateParser(_predicate);
			return parser.parsePredicate();
		}
		catch(IOException err) {
			System.err.println("There was an I/O error parsing the predicate: " + err.getMessage());
			assert(false);
			return null;
		}
		catch(PredicateParser.TokenException err) {
			System.err.println("There was an error parsing the predicate: " + err.getMessage());
			assert(false);
			return null;
		}
	}
	
	/**
	 * Creates a new instance of predicate with specified name and number of
	 * parameters. These parameters are initialized as unbound and independent
	 * on each other.
	 * 
	 *	@param _predicte_name name of the new predicate.
	 *	@param _parameter_count number of parameters for the predicate.
	 */
	public Predicate(String _predicte_name, int _parameter_count) {
		predicateName = _predicte_name;
		initialize(_parameter_count);
	}
	
	/**
	 * Creates a new instance of predicate with specified name and list of
	 * predicates.
	 *
	 *	@param _predicate_name name of the new predicate.
	 *	@param _parameters
	 */
	public Predicate(String _predicate_name, ValueBinding[] _parameters) {
		predicateName = _predicate_name;
		parameters = _parameters;
	}
}
