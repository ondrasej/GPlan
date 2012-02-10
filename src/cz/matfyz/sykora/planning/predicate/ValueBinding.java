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
 * Representation of a single variable in a predicate/action specification.
 * 
 * A variable can be either bound to a solid value (string) or linked with
 * other variables and recieve a value even if any other "linked" variable
 * revieved it.
 * 
 *	@author Ondra Sykora [ondrasej@centrum.cz]
 *	@see Predicate
 */
public class ValueBinding {
	/**
	 * Value that is this variable bound to. If this variable is not bound yet,
	 * this value is set to null.
	 *
	 *	@see #getValue()
	 *	@see #isBound()
	 */
	private String boundValue;
	
	/**
	 * List of dependent bindings. These variables are bound to the same value
	 * as this one and if any of these variables gets bound, all other
	 * variables are bound to the same value.
	 *	
	 *	@see #bind(ValueBinding)
	 */
	private LinkedList<ValueBinding> dependentBindings;
	
	/**
	 * List of variables that must have distinct values. This may be used to
	 * exclude some combinations of values in action preconditions without
	 * having to set up special predicates that specify which atoms (values)
	 * are different.
	 * 
	 * Binding to value that any of these variables is bound to fails
	 * immediately.
	 * 
	 *	@see #bind(String)
	 *	@see #addDistinctBinding(ValueBinding) 
	 */
	private LinkedList<ValueBinding> distinctBindings;
	
	/**
	 * Adds a variable binding to the list of distinct variables.
	 * 
	 *	@param _binding distinct variable.
	 */
	public void addDistinctBinding(ValueBinding _binding) {
		if(_binding == null)
			throw new NullPointerException("_binding parameter must not be null");
		if(_binding == this) {
			assert(false);
			return;
		}
		if(distinctBindings.contains(_binding))
			return;
		distinctBindings.add(_binding);
	}
	
	/**
	 * Binds this variable (and all linked variables) to a solid value. This
	 * binding procedure may fail if one of the "distinct" variables is already
	 * bound to the same value.
	 * 
	 *	@param _value value for this variable.
	 *	@return true if the binding procedure succeeded, false if binding was
	 * 		not possible.
	 *	@see #boundValue
	 *	@see #dependentBindings
	 *	@see #distinctBindings
	 *	@see #getValue()
	 */
	public boolean bind(String _value) {
		if(_value == null)
			throw new NullPointerException("_value parameter must not be null");
		for(ValueBinding distinct : distinctBindings) {
			if(distinct.isBound()
					&& distinct.getValue().equals(_value))
				return false;
		}
		for(ValueBinding binding : dependentBindings) {
			if(binding.isBound()) {
				assert(binding.getValue().equals(_value));
				return binding.getValue().equals(_value);
			}
			else
				binding.setValue(_value);
		}
		setValue(_value);
		return true;
	}
	
	/**
	 * Links this variable with another variable. Variable <i>_binding</i> and
	 * all it's linked variables are linked to this variable and to all it's
	 * linked variables.
	 *
	 *	@param _binding variable that should be linked with this variable.
	 *	@return true on successful linking, false if linking is not possible
	 *		(when the variables should have different values).
	 */
	public boolean bind(ValueBinding _binding) {
		if(_binding == null)
			throw new NullPointerException("_binding parameter must not be null");
		
		// linking is not possible if this two variables shold have
		// different values.
		if(distinctBindings.contains(_binding))
			return false;
		if(_binding.isBound())
			return bind(_binding.getValue());
		else {
			dependentBindings.addAll(_binding.dependentBindings);
			for(ValueBinding binding : dependentBindings)
				binding.dependentBindings = dependentBindings;
		}
		return true;
	}
	
	/**
	 * Resets the bound value. Also resets the bound value for all linked
	 * variables.
	 *
	 *	@see #boundValue
	 *	@see #setValue(String)
	 */
	public void clear() {
		if(!isBound())
			return;
		for(ValueBinding binding : dependentBindings)
			binding.setValue(null);
		setValue(null);
	}
	
	/**
	 * Tests other variable for equality.
	 * 
	 * Two variable bindings are considered equal when they are both bound to
	 * the same value or if they are linked. In other case they are considered
	 * different.
	 *
	 *	@return true if the variables are equal, false otherwise.
	 *	@see #isBound()
	 *	@see #getValue()
	 */
	public boolean equals(Object _other) {
		if(!(_other instanceof ValueBinding))
			return false;
		ValueBinding binding = (ValueBinding)_other;
		if(binding.isBound() != isBound())
			return false;
		if(binding.isBound())
			return binding.getValue().equals(getValue());
		for (ValueBinding dependentBinding : dependentBindings) {
			if (dependentBinding == binding) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Initializes internal structures of this class.
	 */
	private void initialize() {
		dependentBindings = new LinkedList<ValueBinding>();
		dependentBindings.add(this);
		
		distinctBindings = new LinkedList<ValueBinding>();
	}
	
	/**
	 * Returns value of this variable.
	 *
	 *	@return value to which this variable is bound or null when this
	 *		variable is not bound.
	 *	@see #boundValue
	 *	@see #isBound()
	 */
	public String getValue() {
		return boundValue;
	}
	
	/**
	 * Tests if this variable is bound to a solid value.
	 * 
	 *	@return true if this variable is bound, false otherwise.
	 *	@see #boundValue
	 */
	public boolean isBound() {
		return boundValue != null;
	}
	
	/**
	 * Sets the bound value.
	 * 
	 * This is helper method only, other objects should use <i>bind</i>
	 * instead.
	 *
	 *	@param _value value to bind this variable to.
	 */
	private void setValue(String _value) {
		boundValue = _value;
	}
	
	/**
	 * Constructor. Creates a new unbound variable.
	 */
	public ValueBinding() {
		initialize();
		boundValue = null;
	}
	
	/**
	 * Constructor. Creates a new variable bound to a specified value.
	 *
	 *	@param _value value for the new variable.
	 */
	public ValueBinding(String _value) {
		initialize();
		boundValue = _value;
	}
}
