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

import java.io.*;
import java.util.*;

import cz.matfyz.sykora.planning.*;

/**
 * A very simple parser of a Prolog-like language that is used to specify
 * planning problems.
 * @author Ondra Sykora [ondrasej@matfyz.cz]
 */
public class PredicateParser {
	/**
	 * Reader that is used to fetch the input.
	 *	@see #getChar()
	 */
	private Reader inputReader;
	
	/**
	 * Exception used to announce an error that occured while processing
	 * the input data.
	 */
	public class TokenException extends Exception {
		/**
		 * Constructor. Creates a new empty instance of TokenException.
		 */
		public TokenException() {
			
		}
		
		/**
		 * Constructor. Creates a new instance of TokenException with
		 * specified message.
		 * @param _message message for this exception.
		 */
		public TokenException(String _message) {
			super(_message);
		}
		
		/**
		 * Constructor. Creates a new instance of TokenException with specified
		 * message and inner exception.
		 *	@param _message message for this exception.
		 *	@param _cause inner exception that caused this exception.
		 */
		public TokenException(String _message, Throwable _cause) {
			super(_message, _cause);
		}
		
		/**
		 * Constructor. Creates a new instance of TokenException with specified
		 * inner exception.
		 * @param _cause inner exception that caused this exception.
		 */
		public TokenException(Throwable _cause) {
			super(_cause);
		}
	}
	
	/**
	 * Exception used to announce unexpected token error that occured while
	 * processing the input data.
	 */
	public class UnexpectedTokenException extends TokenException {
		
		/**
		 * Token that caused this exception.
		 */
		private Token token;
		
		/**
		 * Returns token that caused this exception. This is getter for
		 * the <i>token</i> property.
		 *	@return token that caused this exception.
		 */
		public Token getToken() {
			return token;
		}
		
		/**
		 * Constructor. Creates a new instance of this class for specified
		 * token and message.
		 * @param _token token that caused this exception.
		 * @param _message message for the token.
		 */
		public UnexpectedTokenException(Token _token, String _message) {
			super(_message);
			token = _token;
		}
	}
	
	/**
	 * Exception used to report unexpected end of the input stream. 
	 */
	public class UnexpectedEndOfInputException extends TokenException {
		/**
		 * Constructor. Creates a new isntance of this class with default error
		 * message.
		 */
		public UnexpectedEndOfInputException() {
			this("Unexpected end of input data");
		}
		
		/**
		 * Constructor. Creates a new instance of this class with specified
		 * error message.
		 * @param _message error message for this exception.
		 */
		public UnexpectedEndOfInputException(String _message) {
			super(_message);
		}
	}
	
	/**
	 * Representation of a single token in the input.
	 * 
	 * There is no token type for errors (they are implemented via exceptions)
	 * and no token type for end of file (when end of input stream is reached
	 * <i>PredicateParser.nextToken()</i> returns null.
	 *	@see PredicateParser#nextToken()
	 *	@see PredicateParser#pushToken(Token)
	 */
	public static class Token {
		/**
		 * Enumeration specifying type of the token.
		 * 
		 * For more information see descriptions of single values;
		 *	@see PredicateParser.Token#tokenType
		 */
		public enum Type {
			/**
			 * This token is identifier (name of action or predicate). Text of
			 * this token is stored in <i>tokenText</i> property.
			 *	@see PredicateParser.Token#tokenText
			 *	@see PredicateParser.Token#getTokenText()
			 */
			IDENTIFIER,
			/**
			 * This token is left parenthesis character.
			 */
			LEFT_PARENTHESIS,
			/**
			 * This token is right parenthesis character.
			 */
			RIGHT_PARENTHESIS,
			/**
			 * This token is comma character.
			 */
			COMMA,
			/**
			 * This token is dot character.
			 */
			DOT,
			/**
			 * Thist token is quad dot character sequence (::).
			 */
			QUADDOT,
			/**
			 * This token is arrow sequence (=>).
			 */
			ARROW
		}
		
		/**
		 * Text data of this token. This is only applicable for IDENTIFIER
		 * tokens. In other cases <i>tokenText</i> is null.
		 *	@see #getTokenText()
		 */
		private String tokenText;
		
		/**
		 * Type of this token.
		 *	@see #getTokenType()
		 */
		private Type tokenType;
		
		/**
		 * Returns text data of this token.
		 *	@return text of this token or null if this token has no text.
		 *	@see #tokenText
		 */
		public final String getTokenText() {
			return tokenText;
		}
		
		/**
		 * Returns type of this token.
		 *	@return type of this token.
		 *	@see #tokenType
		 */
		public final Type getTokenType() {
			return tokenType;
		}
		
		/**
		 * Constructor. Creates a new instance of this class for specified
		 * token type.
		 * 
		 * Token is created with no token text, thus this constructor may not
		 * be used to create identifier tokens.
		 * 
		 *	@param _token_type type of the token.
		 *	@see #Token(Type, String)
		 *	@throws IllegalArgumentException when this construcotr is called
		 *		with <i>Token.Type.IDENTIFIER</i> token type as the parameter.
		 */
		public Token(Type _token_type) {
			if(_token_type == Type.IDENTIFIER)
				throw new IllegalArgumentException("Token text must be specified for IDENTIFIER tokens.");
			tokenType = _token_type;
			tokenText = null;
		}
		
		/**
		 * Constructor. Creates a new instance of this class for specified
		 * token type and token text.
		 * 
		 * If the new token is not IDENTIFIER, <i>_text</i> parameter must be
		 * set to null.
		 * 
		 *	@param _token_type type of the token
		 *	@param _text text data for the token (for IDENTIFIER tokens) or null
		 *		for other token types.
		 *	@see #Token(Type)
		 *	@throws IllegalArgumentException when token text is specified for
		 *		token type other than <i>Token.Type.IDENTIFIER</i> or token
		 *		text is not specified for an identifier token.
		 */
		public Token(Type _token_type, String _text) {
			if(_token_type != Type.IDENTIFIER && _text != null)
				throw new IllegalArgumentException("Only IDENTIFIER tokens can have text data");
			if(_token_type == Type.IDENTIFIER && _text == null)
				throw new IllegalArgumentException("Token text must be specified for IDENTIFIER tokens.");
			tokenType = _token_type;
			tokenText = _text;
		}
	}
	
	/**
	 * Token that was pushed back by the <i>pushToken</i> method. There may be
	 * only one pushed-back token at a time. When there is no token pushed back
	 * this value is set to null.
	 * 
	 *	@see #nextToken()
	 *	@see #pushToken(Token)
	 */
	private Token pushed;
	
	/**
	 * A single character that was pushed back to input stream. There may be
	 * only one pushed-back character at a time. When there is no character
	 * pushed back, this value is set to -1.
	 * 
	 *	@see #getChar()
	 *	@see #pushChar(int)
	 */
	private int pushedChar = -1;
	
	/**
	 * Reads a single character from the input stream.
	 * 
	 * If there is a pushed-back character, then this character is returned
	 * first and pushed-char buffer is reset to -1.
	 *	@return character that was read from the input stream reader or -1
	 *		when end of input stream was reached.
	 *	@throws IOException on input/output error.
	 *	@see #pushedChar
	 */
	private int getChar() throws IOException {
		if(pushedChar == -1)
			return inputReader.read();
		else {
			int res = pushedChar;
			pushedChar = -1;
			return res;
		}
	}
	
	/**
	 * Reads a single token from the input stream.
	 * 
	 * If there is a pushed-back token, then this token is returned first and
	 * the pushed-token buffer is reset to null.
	 *	@return token that was read from the token or null when end of the
	 *		input stream was reached.
	 *	@throws IOException on input/output error.
	 *	@see #pushed
	 */
	public Token nextToken() throws IOException {
		if(pushed != null) {
			// return token from pushed-back buffer if it is not empty
			Token res = pushed;
			pushed = null;	
			return res;
		}

		int cur = -1;
		char current = 0;
		while(true) {
			skipWhiteSpace();
			
			cur = getChar();
			if(-1 == cur)
				return null;
			current = (char)cur;
			
			// skip comments on the current line
			if('%' == current) {
				while('\n' != current && '\r' != current) {
					cur = getChar();
					if(-1 == cur)
						return null;
					current = (char)cur;
				}
			}
			else
				break;
		}
		
		if('(' == current)
			return new Token(Token.Type.LEFT_PARENTHESIS);
		else if(')' == current)
			return new Token(Token.Type.RIGHT_PARENTHESIS);
		else if(',' == current)
			return new Token(Token.Type.COMMA);
		else if('.' == current)
			return new Token(Token.Type.DOT);
		else if(':' == current) {
			cur = getChar();
			if(-1 == cur)
				return null;
			current = (char)cur;
			if(':' == current)
				return new Token(Token.Type.QUADDOT);
		}
		else if('=' == current) {
			cur = getChar();
			if(-1 == cur)
				return null;
			current = (char)cur;
			if('>' == current)
				return new Token(Token.Type.ARROW);
		}
		else if(Character.isLetterOrDigit(current)) {
			StringBuilder builder = new StringBuilder();
			
			do {
				builder.append(current);
				cur = inputReader.read();
				if(-1 == cur)
					break;
				current = (char)cur;
			} while(Character.isLetterOrDigit(current)
					|| current == '-'
					|| current == '_');
			pushChar(current);
			
			return new Token(Token.Type.IDENTIFIER, builder.toString());
		}
		
		return null;
	}
	
	/**
	 * General method for reading actions and predicates from input reader. It
	 * determines whether the object is action or predicate and returns an
	 * apropriate object.
	 * 
	 *	@return a new instance of Action or Predicate class according to data
	 *		from the input reader. If end of input stream is reached, returns
	 *		null.
	 *	@throws IOException on input/output error
	 *	@throws TokenException on invalid input data format
	 */
	public Object parse() throws IOException, TokenException {
		// both kinds of object start with identifier specifying their name
		Token current = nextToken();
		if(current == null)
			return null;
		if(current.getTokenType() != Token.Type.IDENTIFIER)
			throw new UnexpectedTokenException(current, "Identifier (action or predicate name) expected");
		
		String name = current.getTokenText();
		
		current = nextToken();
		if(current == null)
			throw new UnexpectedEndOfInputException("Unexpected end of input stream. Current sentence is not complete");
		
		// determine the kind of the object according to the second token. If
		// the second token is left parenthesis or a dot, than the object is
		// predicate. If it is quad dot, than the second object is action.
		switch(current.getTokenType()) {
			case DOT:
			case LEFT_PARENTHESIS:
				pushToken(current);
				return parsePredicate(name, new Hashtable<String, ValueBinding>(), true);
			case QUADDOT:
				pushToken(current);
				return parseAction(name, new Hashtable<String, ValueBinding>());
			default:
				throw new UnexpectedTokenException(current, "Dot, left parenthesis or quad-dot was expected to follow the identifier");
		}
	}
	
	/**
	 * Reads action specification from the stream. Uses a new set of variable
	 * bindings.
	 * 
	 *	@return new action based on data from the input reader. 
	 *	@throws IOException on input/output error.
	 *	@throws TokenException on invalid input data format.
	 *	@see #parse()
	 *	@see #parseAction(Hashtable)
	 *	@see #parseAction(String, Hashtable)
	 */
	public Action parseAction() throws IOException, TokenException {
		return parseAction(new Hashtable<String, ValueBinding>());
	}
	
	/**
	 * Reads action specification from the stream. Uses a given set of variable
	 * bindings.
	 * 
	 *	@param _variables list of variable bindings
	 *	@return a new action based on data from the input reader. 
	 *	@throws IOException on input/output error.
	 *	@throws TokenException on invalid input data format.
	 *	@see #parse()
	 *	@see #parseAction()
	 *	@see #parseAction(String, Hashtable)
	 */
	public Action parseAction(Hashtable<String, ValueBinding> _variables) throws IOException, TokenException {
		// parse action name
		Token current = nextToken();
		if(current == null)
			return null;
		if(current.getTokenType() != Token.Type.IDENTIFIER)
			throw new UnexpectedTokenException(current, "Identifier (action name) expected");
		
		String action_name= current.getTokenText();
		
		// parse rest of the action data
		return parseAction(action_name, _variables);
	}
	
	/**
	 * Reads action specification from the stream. Uses a given set of variable
	 * bindinges.
	 * 
	 * This version does not read the identifier specifying name of the action.
	 * Thus it can be used from within the method parse which needs to process
	 * first two tokens of the object itself.
	 * 
	 *	@param _action_name name of the action.
	 *	@param _variables list of variable bindings that should be used to
	 * 		translate variable names to it's inner representation.
	 *	@return a new instance of action based on the data from the input
	 * 		stream.
	 *	@throws IOException on input/output error.
	 *	@throws TokenException on invalid input data format.
	 *	@see #parse
	 *	@see #parseAction(Hashtable)
	 */
	protected Action parseAction(String _action_name,
			Hashtable<String, ValueBinding> _variables) throws IOException, TokenException {
		
		// verify there is a quad dot after the action name 
		Token current = nextToken();
		if(current == null)
			throw new UnexpectedEndOfInputException();
		if(current.getTokenType() != Token.Type.QUADDOT)
			throw new UnexpectedTokenException(current, "Quad dot expected");
	
		// parse preconditions
		PredicateSet preconditions = new PredicateSet();
		
		current = nextToken();
		if(current == null)
			throw new UnexpectedEndOfInputException();
		while(current.getTokenType() == Token.Type.IDENTIFIER
				|| current.getTokenType() == Token.Type.COMMA) {
			if(current.getTokenType() == Token.Type.COMMA) {
				current = nextToken();
				if(current == null)
					throw new UnexpectedEndOfInputException();
				if(current.getTokenType() != Token.Type.IDENTIFIER)
					throw new UnexpectedTokenException(current, "Predicate starting with an identifier expected");
			}
			pushToken(current);
			
			Predicate precondition = parsePredicate(_variables, false);
			assert(precondition != null);
			
			if(precondition.getPredicateName().equals("distinct")) {
				ValueBinding first_variable = precondition.getParameter(0);
				ValueBinding second_variable = precondition.getParameter(1);
				
				first_variable.addDistinctBinding(second_variable);
				second_variable.addDistinctBinding(first_variable);
			}
			else
				preconditions.add(precondition);
			
			current = nextToken();
			if(current == null)
				throw new UnexpectedEndOfInputException();
		}
		if(current.getTokenType() != Token.Type.ARROW)
			throw new UnexpectedTokenException(current, "Arrow expected");
		
		// parse effects
		PredicateSet positive_effects = new PredicateSet();
		PredicateSet negative_effects = new PredicateSet();
		
		current = nextToken();
		if(current == null)
			throw new UnexpectedEndOfInputException();
		while(current.getTokenType() == Token.Type.IDENTIFIER
				|| current.getTokenType() == Token.Type.COMMA) {
			if(current.getTokenType() == Token.Type.COMMA) {
				current = nextToken();
				if(current == null)
					throw new UnexpectedEndOfInputException();
				if(current.getTokenType() != Token.Type.IDENTIFIER)
					throw new UnexpectedTokenException(current, "Predicate starting with an identifier expected");
			}
			boolean negative_effect = current.getTokenText().equals("not");
			if(!negative_effect)
				pushToken(current);
			
			Predicate effect = parsePredicate(_variables, false);
			assert(effect != null);
			
			if(negative_effect)
				negative_effects.add(effect);
			else
				positive_effects.add(effect);
			
			current = nextToken();
			if(current == null)
				throw new UnexpectedEndOfInputException();
		}
		if(current.getTokenType() != Token.Type.DOT)
			throw new UnexpectedTokenException(current, "Dot expected");
		
		return new Action(_action_name, preconditions, negative_effects, positive_effects);
	}
	
	/**
	 * Reads a single predicate from the input stream. The predicate
	 * specification must be terminated with a dot. Uses an empty set of
	 * variable bindings.
	 * 
	 *	@return a new predicate instance based on the data from the input
	 * 		stream.
	 *	@throws IOException on input/output error.
	 *	@throws TokenException on invalid input data format.
	 */
	public Predicate parsePredicate() throws IOException, TokenException {
		return parsePredicate(false);
	}
	
	/**
	 * Reads a single predicate from the input stream.
	 * 
	 *	@param _require_dot if set to true, then requires the predicate to be
	 *		followed by a dot. This is used for parsing separate predicates,
	 *		while predicates in action specification are followed by comma or
	 *		other special tokens.
	 *	@return a new predicate based on the data from the input stream.
	 *	@throws IOException on input/output error.
	 *	@throws TokenException on invalid input data format.
	 */
	public Predicate parsePredicate(boolean _require_dot) throws IOException, TokenException {
		return parsePredicate(new Hashtable<String, ValueBinding>(), _require_dot);
	}
	
	public Predicate parsePredicate(Hashtable<String, ValueBinding> _variables,
			boolean _require_dot) throws IOException, TokenException {
		Token current = nextToken();
		String predicate_name = null;
		
		// 	na prvnim miste ocekavej nazev predikatu
		if(current == null)
			return null;
		if(current.getTokenType() != Token.Type.IDENTIFIER)
			throw new UnexpectedTokenException(current, "Identifier expected");
		predicate_name = current.getTokenText();
		
		return parsePredicate(predicate_name, _variables, _require_dot);
	}
	
	/**
	 * Reads a single predicate from the input stream.
	 * 
	 * This version does not read the identifier specifying name of the
	 * predicate. Thus it can be used from within the method parse which needs
	 * to process first two tokens of the object itself.
	 * 
	 *	@param _predicate_name name of the predicate that was processed earlier.
	 *	@param _variables list of variable bindings that is used to	translate
	 *		variable names to it's inner representation.
	 *	@param _require_dot if set to true, than requires the predicate to be
	 *		followed by a dot. This is used for parsing separate predicates.
	 *	@return a new predicated based on the data from the input stream.
	 *	@throws IOException on input/output error.
	 *	@throws TokenException on invalid input data format.
	 */
	public Predicate parsePredicate(String _predicate_name,
			Hashtable<String, ValueBinding> _variables,
			boolean _require_dot) throws IOException, TokenException {
		// dalsi musi byt bud leva zavorka pro predikaty, nebo tecka
		// pro ukonceni zadavani predikatu
		Token current = nextToken();
		if(current == null)
			throw new UnexpectedEndOfInputException("Unexpected end of input data");
		switch(current.getTokenType()) {
			case DOT:
				// dot is not required after the predicate -> push it back to the
				// stream, maybe someone else will use it. This is important when
				// parsing an action and last of it's effects is without parameter.
				if(!_require_dot)
					pushToken(current);
				return new Predicate(_predicate_name, 0);
			case LEFT_PARENTHESIS:
			{
				ArrayList<ValueBinding> parameters = new ArrayList<ValueBinding>();
				current = nextToken();
				if(null == current)
					throw new UnexpectedEndOfInputException("Unexpected end of input data");
				while(Token.Type.RIGHT_PARENTHESIS != current.getTokenType()) {
					if(parameters.size() > 0) {
						if(current.getTokenType() != Token.Type.COMMA)
							throw new UnexpectedTokenException(current, "Comma or right parenthesis expected");
						current = nextToken();
						if(current == null)
							throw new UnexpectedEndOfInputException("Unexpected end of input data");
					}
					if(current.getTokenType() != Token.Type.IDENTIFIER)
						throw new UnexpectedTokenException(current, "Identifier expected");
					
					String current_text = current.getTokenText();
					if(Character.isUpperCase(current_text.charAt(0))) {
						// text zacina velkym pismenkem, jde tedy o promennou
						ValueBinding binding = _variables.get(current_text);
						if(binding == null) {
							binding = new ValueBinding();
							_variables.put(current_text, binding);
						}
						parameters.add(binding);
					}
					else
						parameters.add(new ValueBinding(current_text));
					
					current = nextToken();
					if(current == null)
						throw new UnexpectedEndOfInputException("Unexpected end of input data");
				}
				if(_require_dot) {
					current = nextToken();
					if(current == null)
						throw new UnexpectedEndOfInputException("Unexpected end of input data");
					if(current.getTokenType() != Token.Type.DOT)
						throw new UnexpectedTokenException(current, "Dot expected");
				}
				return new Predicate(_predicate_name, parameters.toArray(new ValueBinding[parameters.size()]));
			}
			default:
				if(_require_dot)
					throw new UnexpectedTokenException(current, "Dot or left parenthesis expected");
				else {
					pushToken(current);
					return new Predicate(_predicate_name, 0);
				}
		}
	}
	
	/**
	 * Pushes back a single character to be re-read on next <i>getChar</i>
	 * call.
	 * 
	 *	@param _char character that is pushed back to input stream.
	 */
	private void pushChar(int _char) {
		assert(pushedChar == -1);
		pushedChar = _char;
	}
	
	/**
	 * Pushes back a specified token for it's further re-processing.
	 * 
	 *	@param _token the token that is pushed back.
	 *	@see #nextToken()
	 *	@see #pushed
	 */
	public void pushToken(Token _token) {
		assert(pushed == null);
		pushed = _token;
	}
	
	/**
	 * Skips white space characters in the input stream and stops before first
	 * non-white character or before end of the input file.
	 * 
	 *	@throws IOException on input/output error.
	 *	@see #getChar()
	 */
	protected void skipWhiteSpace() throws IOException {
		int current = getChar();
		while(Character.isWhitespace(current)) {
			current = getChar();
		}
		pushChar(current);
	}
	
	/**
	 * Creates a new instance of parser that reads actions and predicates from
	 * a reader.
	 * 
	 *	@param _input reader providing input data.
	 */
	public PredicateParser(Reader _input) {
		inputReader = _input;
	}
	
	/**
	 * Creates a new instance of parser that reads actions and predicates from
	 * a string.
	 * 
	 *	@param _input string containing input data for the parser.
	 */
	public PredicateParser(String _input) {
		this(new StringReader(_input));
	}
}
