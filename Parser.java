/**
 * @version 5 2022-10-15
 * 
 */

import java.util.Stack ;

public class Parser 
{
	/**
	 * Generate a parse tree
	 * @param tokens the lexed result of Lexer.tokenise
	 * @return an AST
	 */
	public AST parse( final LinkedList<Token> tokens ) 
	{
		final AST ret = new AST() ;
		final Stack<Token> tokenStack = new Stack<Token>() ; 
		int i ;

		// For each token, in reverse order. 
		// Reverse order is required so that the stack is the correct order.
		for ( i = tokens.getLength() - 1  ; i >= 0 ; i -- )
		{
			// Get current token and push to stack
			tokenStack.push( ( Token ) tokens.peek( i ) ) ;
		}

		// Parse and add to AST
		ret.setRoot( new AST.Expression( this.parse( 
				tokenStack , Token.Type.END ) ) ) ;

		return ret ; 
	}

	/** 
	 * Parses a stack of tokens 
	 * @param tokenStack
	 * @param end
	 * @return the LinkedList of Expressions
	 */
	private LinkedList<AST.Expression> parse( final Stack<Token> tokenStack 
			, final Token.Type end )
	{
		// To be returned
		final LinkedList<AST.Expression> ret = new LinkedList<AST.Expression>() ;
		// For use in nests 
		AST.Expression toAdd = null ;

		// While there are still tokens left in the stack, 
		// and while the end token is not reached
		while ( ( tokenStack.isEmpty() == false ) 
				&& ( ( tokenStack.peek().getType() == end ) == false ) )
		{
			// Check type of Token
			switch ( tokenStack.peek().getType() )
			{
			case COMMENT_START :
				// Comments are placed into the tree
				// Create Expression containing Expressions
				ret.add(
						new AST.Expression( 
								// Add (
								new AST.Expression( tokenStack.pop() )
								// Add contents
								, new AST.Expression( tokenStack.pop() )
								// Add )
								, new AST.Expression( tokenStack.pop() ) ) 
						) ;
				break ;

			case PRINT_START : 
				// Print statements are placed into the tree
				ret.add(
						new AST.Expression( 
								// Add ."
								new AST.Expression( tokenStack.pop() )
								// Add contents
								, new AST.Expression( tokenStack.pop() )
								// Add "
								, new AST.Expression( tokenStack.pop() ) ) 
						) ;
				break ;

			case COLON :
				// Word definition
				ret.add( 
						new AST.Expression( 
								// Add :
								new AST.Expression( tokenStack.pop() )
								// Add identifier
								, new AST.Expression( tokenStack.pop() )
								// Add definition
								, new AST.Expression( tokenStack.pop() )
								// Add ;
								, new AST.Expression( tokenStack.pop() ) ) 
						) ; 	
				break ; 

			case DO :
				ret.add(
						new AST.Expression(
								// Add 'DO'
								new AST.Expression( tokenStack.pop() ) 
								// Add contents; recurse
								, new AST.Expression(
										this.parse( tokenStack , Token.Type.LOOP ) ) 
								// Add 'LOOP'
								, new AST.Expression( tokenStack.pop() ) ) ) ;
				break ;

			case IF : 
				// Create new LinkedList to hold if statement
				toAdd = new AST.Expression( new LinkedList<AST.Expression>() ) ;
				// Add to list
				toAdd.getChildren().add( 
						// Add 'IF'
						new AST.Expression( tokenStack.pop() ) 
						// Add if clause; recurse
						, new AST.Expression( 
								this.parse( tokenStack , Token.Type.ELSE ) ) ) ;

				// If ELSE found
				if ( tokenStack.peek().getType() == Token.Type.ELSE )
				{
					// Add 'else'
					toAdd.getChildren().add(
							// Add 'else'
							new AST.Expression( tokenStack.pop() )
							// Add else clause
							, new AST.Expression( 
									this.parse( tokenStack , Token.Type.THEN ) ) ) ;
				}
				// If ELSE not found
				else
				{
					toAdd.getChildren().add( 
							// Add 'else'
							new AST.Expression( 
									new Token ( "else" , Token.Type.ELSE ) ) 
							// Add empty else clause
							, new AST.Expression( 
									new LinkedList<AST.Expression>() ) ) ;
				}
				// Add 'THEN'
				toAdd.getChildren().add( new AST.Expression( tokenStack.pop() ) ) ;
				ret.add( toAdd ) ;
				break ; 

			case THEN : 
				// If THEN is found before ELSE, return 
				if ( end == Token.Type.ELSE )
				{
					return ret ; 
				}
				// If not looking for ELSE, fall into error block
			case ELSE : 
			case COMMENT_CONTENT :
			case COMMENT_END : 
			case SEMICOLON :
			case LOOP :
				// These should never be evaluated on their own without being 
				// passed as @param end, so any time they appear is unexpected
				throw new RuntimeException( "Error: unexpected >>>" 
						+ tokenStack.peek().getString() + "<<<" ) ;
			default :
				// Anything else is added as a single expression
				ret.add( new AST.Expression( tokenStack.pop() ) ) ; 
				break ; 
			}
		}
		// If at end without finding delimiter (@param end), throw error 
		if ( tokenStack.isEmpty() )
		{
			throw new RuntimeException( "Error: expected but did not find >>>" + end + "<<<" ) ; 
		} 
		return ret ; 
	}
}
