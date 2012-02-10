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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

import cz.matfyz.sykora.planning.graph.*;
import cz.matfyz.sykora.planning.predicate.*;
import cz.matfyz.sykora.planning.predicate.PredicateParser.TokenException;

/**
 * Class representing the whole planning problem. Takes care of performing
 * single steps necessary for problem evaluation.
 * 
 *	@author Ondra Sykora [ondrasej@matfyz.cz]
 */
public class PlanningProblem {
	
	/**
	 * Text of Identifier that preceeds goal predicate in the input stream.
	 *
	 *	@see #GOALS_STRING
	 *	@see #initialize(Reader)
	 */
	private final String GOAL_STRING = "goal";
	
	/**
	 * Text of identifier that preceeds goal predicates in the input stream.
	 *
	 *	@see #GOAL_STRING
	 *	@see #initialize(Reader)
	 */
	private final String GOALS_STRING = "goals";
	
	/**
	 * List of goals of the planning problem. All these predicates must be
	 * contained in the result state of the world.
	 * 
	 *	@see #verifyGoals()
	 */
	private PredicateSet goals;
	
	/**
	 * Cache for result of the <i>verifyGoals</i> method.
	 * 
	 *	@see #isGoalsFulfilled()
	 *	@see #verifyGoals()
	 */
	private boolean goalsFulfiled;
	
	/**
	 * Reference to fixed point layer in the planning graph.
	 * 
	 * Fixed point is such layer, that has the same predicates, actions and
	 * mutexes on both actions and predicates at preceeding layer.
	 * 
	 * If fixed point has not been reached yet, this reference is set to null.
	 * 
	 *	@see #singleStep()
	 *	@see #solve()
	 */
	private BiLayer fixedPoint;
	
	/**
	 * Flag specifying how much information is printed to std output when
	 * processing the problem.
	 */
	private boolean verboseOutput;
	
	/**
	 * List of possible action in the planning problem. Actions in this list
	 * need not to be fully instantiated (have grounded predicates only).
	 * Instantiation takes place in the <i>PredicateLayer.buildActionLayer</i>
	 * method
	 * 
	 *	@see PredicateLayer#buildActionLayer(Iterable)
	 *	@see #buildNextLayer()
	 */
	private ArrayList<Action> possibleActions;
	
	/**
	 * List of layers in the layered plan. Each <i>BiLayer</i> class instance
	 * holds both action and resulting predicate layer.
	 * 
	 * The only exception is the layer 0 which only contains predicates
	 * describing the initial state of the planning system.
	 * 
	 * For access to last action and predicate layers do not use this list, use
	 * <i>getLastActionLayer</i> and <i>getLastPredicateLayer</i> methods
	 * instead.
	 * 
	 *	@see BiLayer
	 *	@see #buildNextLayer()
	 *	@see #getLastActionLayer()
	 *	@see #getLastPredicateLayer()
	 */
	private ArrayList<BiLayer> layers;
	
	/**
	 * Helper class used to hold both action layer and it's succeeding
	 * predicate layer in the plan graph.
	 * 
	 *	@see PlanningProblem#layers 
	 */
	protected class BiLayer {
		/**
		 * Action layer.
		 * 
		 *	@see #getActions()
		 */
		private ActionLayer actions;
		
		/**
		 * Predicate layer.
		 * 
		 *	@see #getPredicates()
		 */
		private PredicateLayer predicates;
		
		/**
		 * Mapping of predicates to actions that support it.
		 * 
		 *	@see #getSupport()
		 */
		private Map<Predicate, ActionList> support;
		
		/**
		 * List of no-good combinations for this layer.
		 *	
		 *	@see #addNoGood(PredicateSet)
		 *	@see #isNoGood(PredicateSet)
		 */
		private ArrayList<PredicateSet> noGoods;
		
		/**
		 * Adds a new no-good combination of predicates to the list.
		 * 
		 *	@param _no_good new no-good combination of predicates.
		 */
		public void addNoGood(PredicateSet _no_good) {
			if(noGoods == null)
				noGoods = new ArrayList<PredicateSet>();
			noGoods.add(_no_good);
		}
		
		/**
		 * Getter for action layer hold by this class.
		 * 
		 *	@return action layer hold by this class.
		 *	@see #actions 
		 */
		public ActionLayer getActions() {
			return actions;
		}
		
		/**
		 * Returns number of no-good combinations in this layer.
		 * 
		 *	@return number of no-good combinations in this layer.
		 */
		public int getNoGoodSize() {
			return noGoods == null ? 0 : noGoods.size();
		}
		
		/**
		 * Getter for predicate layer hold by this class.
		 * 
		 *	@return predicate layer hold by this class.
		 *	@see #predicates
		 */
		public PredicateLayer getPredicates() {
			return predicates;
		}
		
		/**
		 * Returns mapping from predicates to actions that support them.
		 * 
		 *	@return mapping from predicates to actions that support them.
		 */
		public Map<Predicate, ActionList> getSupport() {
			return support;
		}
		
		/**
		 * Finds out whether the given set of predicates is a no-good
		 * combination (a combination of predicates that was not solved in this
		 * layer before).
		 * 
		 * This method does not test <i>_predicates</i> for equality, but for
		 * inclusion of a no-good combination
		 * 
		 *	@param _predicates combination of predicates that is tested for
		 * 		no-good.
		 *	@return true if the set contains a no-good combination, false
		 *		otherwise.
		 */
		public boolean isNoGood(PredicateSet _predicates) {
			if(noGoods == null)
				return false;
			for(PredicateSet no_good : noGoods)
				if(_predicates.contains(no_good)/* && no_good.contains(_predicates)*/)
					return true;
			return false;
		}
		
		/**
		 * Constructor. Creates a new instance of BiLayer for a set of
		 * predicates.
		 * 
		 * This constructor is used to create a BiLayer with no action layer
		 * that may be used as the first BiLayer holding the initial state of
		 * the planning system.
		 * 
		 *	@param _predicates set of predicates describing the initial state
		 *		of the planning system.
		 */
		public BiLayer(PredicateSet _predicates) {
			actions = null;
			predicates = new PredicateLayer();
			predicates.add(_predicates);
		}
		
		/**
		 * Constructor. Creates a new instance of BiLayer for specified action
		 * and predicate layer.
		 *	@param _actions action layer
		 *	@param _predicates predicate layer succeeding the action layer
		 * 		<i>actions</i>
		 */
		public BiLayer(ActionLayer _actions, PredicateLayer _predicates,
					Map<Predicate, ActionList> _support) {
			actions = _actions;
			predicates = _predicates;
			support = _support;
		}
	}
	
	/**
	 * Builds next action layer-predicate layer tuple from the last predicate
	 * layer in the graph.
	 *
	 *	@return action layer-predicate layer tuple build from the last predicate
	 *		layer.
	 *	@see BiLayer
	 *	@see #getLastPredicateLayer()
	 *	@see #singleStep()
	 */
	protected BiLayer buildNextLayer() {
		PredicateLayer last_predicates = getLastPredicateLayer();
		if(last_predicates == null)
			throw new RuntimeException("Planning problem was not initialized yet.");
		
		TreeMap<Predicate, ActionList> support_map = new TreeMap<Predicate, ActionList>();
		ActionLayer actions = last_predicates.buildActionLayer(possibleActions);
		PredicateLayer predicates = actions.buildPredicateLayer(support_map);
		
		return new BiLayer(actions, predicates,support_map);
	}
	
	/**
	 * Selects an action sequence from each action layer in the graph that
	 * solves the planning problem. 
	 * 
	 *	@return a serial plan that solves the planning problem or null if no
	 * 		such plan exists.
	 */
	public ActionList findSerialPlan() {
		if(!isGoalsFulfilled()) {
			// TODO: hodit nejakou vyjimku a nepatlat se s tim
			return null;
		}
		ActionList result = findSerialPlanActions(layers.size() - 1, goals);
		return result;
	}
	
	/**
	 * Helper method that is used in search for the serial plan. This method
	 * is used to find action that satisfies a predicate and that is compatible
	 * with other selected actions.
	 * 
	 *	@param _source BiLayer in which is the action looked for.
	 *	@param _predicates list of predicates that need to be satisfied.
	 *	@param _current_predicate index of the current predicate in the
	 *		<i>_predicates</i> list.
	 *	@param _resolved set containing predicates that are resolved by
	 *		selected actions.
	 *	@param _current_actions actions that were selected.
	 *	@param _current_layer index of the current layer in the <i>layers</i>
	 *		list.
	 *	@return A serial plan for the specified layer and list of predicates
	 *		that need to be fulfilled by selected actions. The plan starts from
	 *		the initial state of the planning system.
	 *	@see #findSerialPlan()
	 *	@see #findSerialPlanActions(int, PredicateSet)
	 */
	private ActionList findPredicateAction(BiLayer _source, PredicateSet _predicates,
			int _current_predicate, Set<Predicate> _resolved, ActionList _current_actions,
			int _current_layer) {
		// TODO: misto Set<Predicate> _resolved pouzit mapu s pocitanim podpor,
		// ktera by umoznila pridavat do _resolved vsechny pozitivni efekty
		// vybranych akci a zrychlit tak prohledavani.
		if(_predicates.getPredicateCount() == _current_predicate) {
			// all predicates from the working set are fulfilled, this layer is
			// resolved, try to resolve next layer.
			// If all layers are resolved, then the plan was found -> return
			// selected actions
			if(_current_layer == 1)
				return _current_actions;
			PredicateSet current_goals = _current_actions.getPreconditions();
			ActionList next_actions = findSerialPlanActions(_current_layer - 1, current_goals);
			
			if(next_actions == null)
				return null;
			next_actions.addAll(_current_actions);
			return next_actions;
		}
		
		Predicate predicate = _predicates.getPredicate(_current_predicate);
		if(!_resolved.contains(predicate)) {
			// current predicate is not resolved yet, let's do something about it
			actions:
			for(Action action : _source.getSupport().get(predicate)) {
				// find actions that has predicate as a positive effect
//				if(action.getPositiveEffects().contains(predicate)) {
					for(Action previous_action : _current_actions) {
						if(_source.getActions().isMutex(action, previous_action)) {
							continue actions;
						}
					}
					_resolved.add(predicate);
					boolean appended = false;
					if(!_current_actions.contains(action)) {
						_current_actions.add(action);
						appended = true;
					}
					ActionList result = findPredicateAction(_source, _predicates, _current_predicate + 1,
							_resolved, _current_actions, _current_layer);
					if(result != null)
						return result;
					_resolved.remove(predicate);
					if(appended)
						_current_actions.remove(_current_actions.size() - 1);
//				}
			}
			return null;
		}
		else
			return findPredicateAction(_source, _predicates, _current_predicate + 1, _resolved, _current_actions, _current_layer);
	}
	
	/**
	 * Finds serial plan to resolve a set of predicates (preconditions of
	 * actions from the following action layer) in a layer.
	 * 
	 *	@param _current_layer index of current layer in the <i>layers</i>
	 *		array.
	 *	@param _predicates set of predicates that should be resolved in the
	 * 		layer with index <i>_current_layer</i>.
	 *	@return serial plan for
	 *	@see #findPredicateAction(BiLayer, PredicateSet, int, Set, ActionList, int)
	 *	@see #findSerialPlan() 
	 */
	private ActionList findSerialPlanActions(int _current_layer, PredicateSet _predicates) {
		BiLayer source = layers.get(_current_layer);
		if(source == null)
			throw new NullPointerException("_source must not be null");
		if(_predicates == null)
			throw new NullPointerException("_predicates must not be null");
		
		if(!source.getPredicates().contains(_predicates))
			return null;
		
		if(source.isNoGood(_predicates))
			return null;
		ActionList actions = findPredicateAction(source, _predicates, 0, new TreeSet<Predicate>(), new ActionList(), _current_layer);
		if(actions == null)
			source.addNoGood(_predicates);
		return actions;
	}
	
	/**
	 * Returns the last action layer from the planning graph.
	 * 
	 *	@return last action layer from the planning graph.
	 *	@see #layers
	 *	@see #getLastLayers()
	 *	@see #getLastPredicateLayer()
	 */
	public ActionLayer getLastActionLayer() {
		BiLayer last_bi = getLastLayers();
		return last_bi.getActions();
	}
	
	/**
	 * Returns last BiLayer class holding last predicate and action layers in
	 * the planning graph.
	 * 
	 *	@return BiLayer class with last action and last predicate layers in the
	 *		planning graph.
	 *	@see #layers
	 *	@see #getLastActionLayer()
	 *	@see #getLastPredicateLayer()
	 */
	public BiLayer getLastLayers() {
		return layers.get(layers.size() - 1);
	}
	
	/**
	 * Returns the last predicate layer from the planning graph.
	 * 
	 *	@return last predicate layer from the planning graph.
	 *	@see #layers
	 *	@see #getLastActionLayer()
	 *	@see #getLastLayers()
	 */
	public PredicateLayer getLastPredicateLayer() {
		BiLayer last_bi = getLastLayers();
		return last_bi.getPredicates();
	}
	
	/**
	 * Initializes internal structures in the class.
	 *	
	 *	@see #PlanningProblem(boolean)
	 */
	private void initialize() {
		layers = new ArrayList<BiLayer>();
		possibleActions = new ArrayList<Action>();
		goals = new PredicateSet();
	}
	
	/**
	 * Initializes the planning problem for a specified input data. The
	 * planning problem specification is read from a string variable.
	 * 
	 *	@param _input string containing the specification of the planning
	 * 		problem.
	 *	@throws IOException on input/output error while reading the planning
	 *		problem specification.
	 *	@throws TokenException on invalid input data format.
	 *	@see #initialize(Reader)
	 */
	public void initialize(String _input) throws IOException, TokenException {
		initialize(new StringReader(_input));
	}
	
	/**
	 * Initializes the planning problem for a specified input data. The
	 * planning problem specification is read from an input stream represented
	 * by a reader.
	 *  
	 *	@param _input_reader input stream reader.
	 *	@throws IOException on input/output error while reading the planning
	 * 		problem specification from the input stream.
	 *	@throws TokenException on invalid input data format.
	 *	@see #initialize(String)
	 */
	public void initialize(Reader _input_reader) throws IOException, TokenException {
		PredicateParser parser = new PredicateParser(_input_reader);
		PredicateSet initial_state = new PredicateSet();
		boolean read_goals = false;
		
		goals.clear();
		possibleActions.clear();
		layers.clear();
		 
		PredicateParser.Token current = parser.nextToken();
		while(current != null) {
			boolean current_is_goal = read_goals;
			if(current.getTokenType() == PredicateParser.Token.Type.IDENTIFIER
					&& current.getTokenText().equals(GOAL_STRING)) {
				current_is_goal = true;
				
				current = parser.nextToken();
			}
			else if(current.getTokenType() == PredicateParser.Token.Type.IDENTIFIER
										&& current.getTokenText().equals(GOALS_STRING)) {
				read_goals = true;
				current_is_goal = true;
				
				current = parser.nextToken();
			}
			parser.pushToken(current);
			
			if(current.getTokenType() != PredicateParser.Token.Type.IDENTIFIER)
				throw parser.new UnexpectedTokenException(current, "Identifier expected");

			Object current_object = parser.parse();
			if(current_object instanceof Action)
				possibleActions.add((Action)current_object);
			else if(current_object instanceof Predicate) {
				if(current_is_goal)
					goals.add((Predicate)current_object);
				else
					initial_state.add((Predicate)current_object);
			}
			else
				break;
			
			current = parser.nextToken();
		}
		
		layers.add(new BiLayer(initial_state));
		
		goalsFulfiled = false;
		fixedPoint = null;
	}
	
	/**
	 * Initializes the planning problem for a set of possible actions, set of
	 * predicates specifying the initial state of the system and set of goal
	 * predicates.
	 * 
	 *	@param _actions list of possible actions.
	 *	@param _initiali_state list of predicates describing the initial state
	 *		of the system.
	 *	@param _goals list of goal predicates.
	 */
	public void initialize(Iterable<Action> _actions, PredicateSet _initiali_state,
							PredicateSet _goals) {
		possibleActions.clear();
		for(Action action : _actions)
			possibleActions.add(action);
		
		goals.clear();
		goals.add(_goals);
		
		layers.clear();
		layers.add(new BiLayer(_initiali_state));
		
		goalsFulfiled = false;
		fixedPoint = null;
	}
	
	/**
	 * Finds out whether all goal predicates are present and no mutex is
	 * between any two of them in the last predicate layer in the planning
	 * graph. This method only works with the <i>goalsFulfiled</i> cache, to
	 * calculate this value again one needs to call the <i>verifyGoals</i>
	 * method.
	 * 
	 *	@return true if all goal predicates were fulfulled when the 
	 * 		<i>singleStep</i> method was called for the last time.
	 *	@see #goalsFulfiled
	 *	@see #singleStep()
	 *	@see #verifyGoals()
	 */
	public boolean isGoalsFulfilled() {
		return goalsFulfiled;
	}
	
	/**
	 * Performs a single step of the planning graphs extension. Uses last
	 * predicate layer to build a succeeding action layer and builds next
	 * predicate layer upon it.
	 * 
	 *	@return false if the termination condition was reached, true otherwise.
	 *	@see #buildNextLayer()
	 *	@see #fixedPoint
	 *	@see #verifyGoals()
	 */
	public boolean singleStep() {
		BiLayer next_bi = buildNextLayer();
		PredicateLayer previous = getLastPredicateLayer();
		
		if(next_bi == null)
			return false;
		layers.add(next_bi);
		if(fixedPoint == null && previous.equals(getLastPredicateLayer()))
			fixedPoint = next_bi;
		if(verifyGoals()) {
			goalsFulfiled = true;
			if(verboseOutput)
				System.out.println("All goals are contained in predicate layer and there are no mutexes among them.");
			return true;
		}
		else if(fixedPoint != null)
			return false;
		return true;
	}
	
	/**
	 * Solves the planning problem or finds out that no solution exists.
	 *	@return true if serial plan was found, false if no solution exists.
	 */
	public ActionList solve() {
		int counter = 0;
		while(singleStep()) {
			System.out.println("Layer #" + ++counter + " built.");
			if(isGoalsFulfilled()) {
				if(verboseOutput)
					System.out.println("Layered plan was found.");
				int old_no_good = fixedPoint != null ? fixedPoint.getNoGoodSize() : 0;
				ActionList plan = findSerialPlan();
				if(plan != null) {
					if(verboseOutput)
						System.out.println("Serial plan was found");
					return plan.getPureList();
				}
				else {
					if(fixedPoint == null) {
						if(verboseOutput)
							System.out.println("Fixed point not reached yet - resuming");
						continue;
					}
					else if(fixedPoint.getNoGoodSize() != old_no_good) {
						if(verboseOutput)
							System.out.println("New no-good combination found at fixed point - resuming");
						continue;
					}
					return null;
				}
			}
		}
		if(verboseOutput)
			System.out.println("Termination condition reached.");
		return null;
	}
	
	/**
	 * Tests if all goals are contained in the last predicate layer and that no
	 * mutex is between them.
	 * 
	 *	@return if all goals are contained in the last predicate layer and if
	 *		no mutex is between them.
	 *	@see #goals
	 *	@see #goalsFulfiled
	 *	@see #isGoalsFulfilled()
	 */
	protected boolean verifyGoals() {
		PredicateLayer last = getLastPredicateLayer();
		assert(last != null);
		
		return last.contains(goals);
	}
	
	/**
	 * Constructor. Initializes an empty planning problem.
	 * 
	 *	@see #initialize()
	 */
	public PlanningProblem(boolean _verbose) {
		verboseOutput = _verbose;
		initialize();
	}
}
