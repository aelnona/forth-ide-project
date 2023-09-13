/**
 * @version 3 2022-10-01
 *
 */
public class Lexer
{
	// Used for testing
	public static boolean bDebugMode = false ; 

	/**
	 * Takes a String and returns an array of Tokens
	 * @param szInput
	 * @param nextState
	 * @return a LinkedList of tokens
	 */
	public LinkedList<Token> tokenise( final String szInput ) 
	{
		// This is a LinkedList as it is easy to add items to the end
		LinkedList<Token> tokens = new LinkedList<Token>() ; 
		boolean bSet = false ; 

		// For each character
		for ( int i = 0 ; i < szInput.length() - 1 ; i ++ )
		{
			bSet = false ; 

			// If word definition
			if ( szInput.substring( i , i + 2 ).equals(": ") )
			{
				bSet = true ; 
				// Add colon
				i = i + this.addToken( tokens 
						, ":" , Token.Type.COLON ) ;

				// If not at end, check if semicolon
				if ( i < szInput.length() - 2 )
				{
					// Add identifier if present
					if ( ( szInput.charAt( i + 1 ) == ';' ) == false )
					{
						i = i + this.addToken( tokens
								, this.getBetweenDelimiters(
										szInput.substring( i + 1 ) 
										, " " )
								, Token.Type.WORD_NAME ) ;
						// Add definition if present
						if ( ( szInput.charAt( i + 1 ) == ';' ) == false )
						{
							i = i + this.addToken( tokens
									, this.getBetweenDelimiters(
											szInput.substring( i + 2 ) 
											, ";" )
									, Token.Type.DEFINITION ) ;
						}
					}
					else
					{
						// If no identifier or definition, set to null
						tokens.add( new Token( null , Token.Type.WORD_NAME ) ) ;
						tokens.add( new Token( null , Token.Type.DEFINITION) ) ;
					}
				}
				else
				{
					// If no identifier or definition, set to null
					tokens.add( new Token( null , Token.Type.WORD_NAME ) ) ;
					tokens.add( new Token( null , Token.Type.DEFINITION) ) ;
				}
			}
			// If semicolon
			else if ( szInput.substring( i , i + 2 ).equals( "; " ) )
			{
				bSet = true ; 
				i = i + this.addToken( tokens 
						, ";" , Token.Type.SEMICOLON ) ;
			}
			// If ! 
			else if ( szInput.substring( i , i + 2 ).equals( "! " ) 
					&& ( ( tokens.getLength() == 0 ) == false ) )
			{
				bSet = true ; 
				// Previous token is a variable name 
				tokens.peek().setType( Token.Type.VARIABLE_NAME ) ;
				i = i + this.addToken( tokens 
						, "!" , Token.Type.VARIABLE_STORE ) ;
			}
			// If @
			else if ( szInput.substring( i , i + 2 ).equals( "@ " )  
					&& ( ( tokens.getLength() == 0 ) == false ) )
			{
				bSet = true ; 
				// Previous token is a variable name 
				tokens.peek().setType( Token.Type.VARIABLE_NAME ) ;
				i = i + this.addToken( tokens 
						, "@" , Token.Type.VARIABLE_GET ) ;
			}
			// If ?
			else if ( szInput.substring( i , i + 2 ).equals( "? " ) 
					&& ( ( tokens.getLength() == 0 ) == false ) )
			{
				bSet = true ; 
				// Previous token is a variable name 
				tokens.peek().setType( Token.Type.VARIABLE_NAME ) ;
				i = i + this.addToken( tokens 
						, "?" , Token.Type.VARIABLE_PRINT ) ;
			}
			// If number
			else if ( this.getBetweenDelimiters(
					szInput.substring( i )
					, " " ).matches( "^-?[0-9]+$" ) )
			{
				bSet = true ; 
				i = i + this.addToken( tokens
						, this.getBetweenDelimiters(
								szInput.substring( i )
								, " " ) 
						, Token.Type.LITERAL ) ;					
			}
			// Check if comment
			else if ( szInput.substring( i , i + 2 ).equals("( ") )
			{
				bSet = true ; 
				// Add to tokens
				i = i + this.addToken( tokens 
						, "(" , Token.Type.COMMENT_START ) 
				+ this.addToken( tokens
						, this.getBetweenDelimiters(
								szInput.substring( i + 2
										, szInput.length() ) , ")" )
						, Token.Type.COMMENT_CONTENT )
				+ this.addToken( tokens 
						, ")" , Token.Type.COMMENT_END ) + 1 ;
			}
			// Check 2-long words
			else if ( i < szInput.length() - 2 )
			{
				// Check if print string
				if ( szInput.substring( i , i + 3 ).equals( ".\" " ) )
				{
					bSet = true ; 
					// Add to tokens
					i = i + this.addToken( tokens
							, ".\""
							, Token.Type.PRINT_START ) 
					+ this.addToken( tokens
							, this.getBetweenDelimiters(
									szInput.substring( i + 3 
											, szInput.length() ) , "\"" )
							, Token.Type.PRINT_CONTENT ) 
					+ this.addToken( tokens 
							, "\"" , Token.Type.PRINT_END ) + 1 ;
				}
				else if ( szInput.substring( i , i + 3 ).equalsIgnoreCase( "if " ) )
				{
					bSet = true ; 
					// if is IF 
					i = i + this.addToken( tokens
							, "if" , Token.Type.IF ) ;
				}
				else if ( szInput.substring( i , i + 3 ).equalsIgnoreCase( "do " ) )
				{
					bSet = true ; 
					i = i + this.addToken( tokens
							, "do" , Token.Type.DO ) ;
				}
				// Check 4-long words
				else if ( i < szInput.length() - 4 )
				{
					if ( szInput.substring( i , i + 5 )
							.equalsIgnoreCase("else ") )
					{
						bSet = true ; 
						i = i + this.addToken( tokens
								, "else" , Token.Type.ELSE ) ;
					}
					else if ( szInput.substring( i , i + 5 )
							.equalsIgnoreCase("then ") )
					{
						bSet = true ; 
						i = i + this.addToken( tokens
								, "then" , Token.Type.THEN ) ;
					}
					else if ( szInput.substring( i , i + 5 )
							.equalsIgnoreCase("loop ") )
					{
						bSet = true ; 
						i = i + this.addToken( tokens
								, "loop" , Token.Type.LOOP ) ;
					}
					else if ( i < szInput.length() - 5 )
					{
						if ( szInput.substring( i , i + 6 ).equalsIgnoreCase( "+loop " ) )
						{
							bSet = true ; 
							i = i + this.addToken( tokens
									, "+loop" , Token.Type.LOOP ) ;					
						}
						else if ( szInput.substring( i , i + 6 ).equalsIgnoreCase( "break " ) )
						{
							bSet = true ; 
							i = i + this.addToken( tokens
									, "break" , Token.Type.BREAK ) ;
						}
						else if ( i < szInput.length() - 6 )
						{
							if ( szInput.substring( i , i + 7 ).equalsIgnoreCase( "forget " ) )
							{
								bSet = true ; 
								i = i + this.addToken( tokens , "forget" , Token.Type.FORGET )
								+ this.addToken( tokens 
										, this.getBetweenDelimiters( 
												szInput.substring( i + 7 
														, szInput.length() ) , " " )  
										, Token.Type.WORD_NAME ) ; 
							} 				
							// Check variables
							else if ( i < szInput.length() - 8 )
							{
								if ( szInput.substring( i , i + 9 ).equalsIgnoreCase( "variable " ) )
								{
									bSet = true ; 
									i = i + this.addToken( tokens , "variable" , Token.Type.VARIABLE ) 
									+ this.addToken( tokens 
											, this.getBetweenDelimiters( 
													szInput.substring( i + 9  
															, szInput.length() ) , " " )  
											, Token.Type.VARIABLE_NAME ) ; 
								} 
							}
						}
					}

				}	
			}
			if ( ( false == Character.isWhitespace( szInput.charAt( i ) ) ) && ( bSet == false ) )
			{
				bSet = true ; 
				i = i + this.addToken( tokens
						, this.getBetweenDelimiters(
								szInput.substring( i )
								, " " ) 
						, Token.Type.WORD ) ;						
			}
		}
		tokens.add( new Token( null , Token.Type.END ) ) ;

		// Display token if debug mode
		if ( bDebugMode )
		{
			final Token[] ret = new Token[ tokens.getLength() ] ;
			for ( int i = 0 ; i < tokens.getLength() ; i ++ )
			{
				ret[i] = (Token) tokens.toArray()[i] ; 
				System.out.print( ret[i].getString() + " :: ");
				System.out.println( ret[i].getType() ) ;
			}
		}

		return tokens ;
	}
	/**
	 * Adds a token to the linked list
	 * @param tokens is the LinkedList<Token> to add tokens to 
	 * @param szContent is the Name of the token
	 * @param type is the Token.Type of the token 
	 * @return the length of the token content
	 */
	private int addToken( final LinkedList<Token> tokens 
			, final String szContent 
			, final Token.Type type )
	{

		// Add to tokens
		tokens.add( new Token( szContent
				, type ) ) ;
		// Return length
		return tokens.peek().getString().length() ;
	}

	/**
	 * Gets the content of a String until the delimiter
	 * @param szContent the String to search, 
	 * excluding the initial delimiter
	 * @param szDelimiter the delimiter, excluded from the return 
	 * @return a String
	 */
	private String getBetweenDelimiters( final String szContent
			, final String szDelimiter )
	{
		String szRet = "" ;
		int iCurrentChar = 0 ;
		boolean bEnd = false ; 

		// While not found 
		while ( bEnd == false )
		{
			// If matches first character of delimiter 
			if ( szContent.charAt(iCurrentChar) == szDelimiter.charAt( 0 ) )
			{
				// Set flag
				bEnd = true ;
				// Check if matches the rest
				for ( int i = iCurrentChar ; i < szDelimiter.length() 
						&& i < szContent.length() ; i ++ )
				{
					// If not, reset flag
					if ( szDelimiter.charAt( i ) 
							== szContent.charAt( i ) == false )
					{
						bEnd = false ; 
					}
				}
			}
			// If not reached the end 
			if ( ( iCurrentChar == szContent.length() - 1 ) 
					&& ( bEnd == false ) )
			{
				bEnd = true ; 
				throw new RuntimeException( "Error, missing >>>" + szDelimiter + "<<<" ) ;
			}
			// If not found 
			else if ( bEnd == false )
			{
				// Add to return String 
				szRet = szRet + szContent.charAt( iCurrentChar ) ;
				// Increment counter
				++ iCurrentChar ;
			}
		}
		return szRet ;
	}

	public static void main( String[] args ) 
	{
		final String[] tests = {
				// N
				"+ - / words cr "
				// N 
				, "1 2 3 4 5 6 7 8 9 0 10 -100 "
				// N
				, "( Hello ) "
				// N
				, ".\" Hello, World!\" "
				// N 
				, "if conditional else alternative then "
				// N 
				, "10 0 do i . loop "
				// N
				, "10 0 do i . 1 +loop "
				// N
				, "forget wordname "
				// N
				, "variable varname "
				// N
				+ "varname ! "
				// N 
				+ "varname @ "
				// N
				+ "varname ? "
				// N
				, ": wordname definition ; "
				// N
				, "break "
				// E
				, " " 
		} ;
		bDebugMode = true ; 
		Lexer lexer = new Lexer() ; 
		for ( int i = 0 ; i < tests.length ; i ++ )
		{
			try
			{
				System.out.printf( "%nT.5.%d%n" , i + 1 ) ;
				lexer.tokenise( tests[i] ) ;
			}
			catch ( final Exception e )
			{
				System.err.println( e.getMessage() ) ;
			}
		}
	}

}
