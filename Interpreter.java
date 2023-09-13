/**
 * @version 5 2022-11-05
 *
 * The Forth interpreter emulator
 * This one uses normal-order instead of applicative like
 * the prototypes did
 *   
 */

import java.util.Enumeration;

public class Interpreter
{
	// Defines types of errors that can be thrown by the Interpreter
	private enum ErrorType 
	{
		WORDUNDEFINED
		, SYNTAX
		, DIVISIONBYZERO
		, UNEXPECTED
		;
	}
	private Dictionary dict ; // The dictionary
	private ParamStack pStack ; // The parameter stack 
	private ParamStack rStack ; // The return stack
	private FConsole console ; // The console to take input from

	// Constant to store the maximum number of loops
	// Not in design as infinite loop-related crashes 
	// were found only when testing during development
	private static final int MAX_LOOP = ( int ) Math.pow( 2 , 9 ) ; 

	/** Constructor */
	public Interpreter()
	{
		this.reset() ;
		return ;
	}
	/** Resets class members */
	public void reset() 
	{
		// New dictionary
		this.setDictionary( new Dictionary() ) ;
		// Create stacks with default sizes
		this.setPStack( new ParamStack() ) ;
		this.setRStack( new ParamStack() ) ;
		// Add predefined and built-in words to dictionary
		this.initWords() ;
		return ;
	}

	/** 
	 * Executes Forth code
	 * @param szCode is the Forth code to execute
	 * @return true if errors, false otherwise
	 */
	public boolean execute( final String szCode )
	{
		// Execute 
		final boolean bRC = this.executeLine( szCode ) ;
		if ( bRC == false ) 
		{
			// If no errors, print ok
			System.out.println( " ok" ) ;
		}
		else
		{
			// If errors, print newline
			System.out.println();
		}
		return bRC ;
	}

	/**
	 * Passes the code to the Lexer then Parser before executing
	 * @param szCode
	 * @return false if no errors, true otherwise
	 */
	public boolean executeLine( final String szCode )
	{
		boolean bRC ;
		final Parser parser = new Parser() ; 
		final Lexer lexer = new Lexer() ; 

		// Execute the parsed lexed code 
		try
		{
			bRC = this.executeAST( 
					// Parse
					parser.parse( 
						// Tokenise 
						lexer.tokenise(
							// Replace all tab/newline characters with spaces 
							szCode.replaceAll( "(\t|\n|\r)" , "  " )
							// Add trailing space, as all words must 
							// be followed by whitespace
							+ "  " ) ) ) ;
		}
		catch ( final RuntimeException e )
		{
			// If error occurs, print error message
			System.err.println( e.getMessage() ) ;
			bRC = true ; 
		}
		return bRC ; 
	}

	/**
	 * Executes an abstract syntax tree from the Parser
	 * @param ast
	 * @return false if no errors, true otherwise
	 */
	public boolean executeAST( final AST ast )
	{
		boolean bRC = false ;

		// ast.display( ast.getRoot() , 0 ) ; 
		// Execute the AST
		bRC = this.executeExpression( ast.getRoot() ) ;

		return bRC ; 
	}

	/**
	 * Executes an AST.Expression 
	 * @param expr is the Expression
	 * @param state is used to store the state
	 * @param szBuffer is used to transfer data between steps
	 * @return
	 */
	public boolean executeExpression( final AST.Expression expr )
	{
		int i ;

		boolean bRC = false ; 

		// If not leaf node
		if ( expr.getChildren() == null == false )
		{
			// For each child
			for ( i = 0 ; i < expr.getChildren().getLength() ; i ++ )
			{
				// If not leaf
				if ( ( expr.getChildren().peek( i ).getContent() == null ) == false )
				{
					// Execute expression
					switch ( expr.getChildren().peek( i ).getContent().getType() )
					{
					case BREAK :
						// Call method handling breakpoints
						// In Interpreter, this does nothing
						this.breakpoint() ; 
						break ;
					case COLON : // Advances i by 3
						// Add definition
						this.getDictionary().addWord( 
								// Add name
								expr.getChildren().peek( ++ i )
								.getContent().getString()
								// Add definition
								, expr.getChildren().peek( ++ i )
								.getContent().getString() ) ;
						++ i ; // Skip semicolon
						break ; 
					case FORGET : // Advances i by 2
						// Revert or remove word
						this.getDictionary().undefineWord( 
								expr.getChildren().peek( ++ i )
								.getContent().getString() ) ;
						break ;
					case PRINT_START : // Advances i by 2 
						// Print 
						System.out.print( expr.getChildren().peek( ++ i )
								.getContent().getString() ) ;
						++ i ;
						break ; 
					case VARIABLE : // If word is 'variable'. Advances i by 1
						// Define next name 
						this.getDictionary().addVariable( 
								expr.getChildren().peek( ++ i )
								.getContent().getString() 
								, "" ) ;
						break ;
					case VARIABLE_NAME : 
						// Check next child
						switch ( expr.getChildren().peek( i + 1 )
								.getContent().getType() )
						{
						// Name should be current token, operation next token
						// Add current token as variable name
						case VARIABLE_STORE : 
							// Pop stack into variable
							this.getDictionary().addVariable( 
									expr.getChildren().peek( i )
									.getContent().getString() 
									, "" + this.getPStack().pop() ) ;
							break ; 
						case VARIABLE_GET : 
							// Push variable value to stack
							this.getPStack().push( Integer.parseInt(	
									this.getDictionary().getVariable( 
											expr.getChildren().peek( i )
											.getContent().getString() ) ) ) ; 
							break ; 
						case VARIABLE_PRINT :
							// Print variable value
							System.out.print( Integer.parseInt(	
									this.getDictionary().getVariable( 
											expr.getChildren().peek( i )
											.getContent().getString() ) ) ) ;
							break ; 
						default : 
							// Undefined variable operation (word), print error
							this.printError( ErrorType.WORDUNDEFINED 
									, expr.getChildren().peek( i )
									.getContent().getString() ) ;
							break ; 
						}
						++ i ; 
						break ; 
					case DO :
						// Execute DO LOOP
						i = i + this.execDoLoop( this.getPStack().pop() 
								, this.getPStack().pop() , expr, ++ i , i ) ;				
						break ; 
					case IF : 
						// Execute IF Statement
						i = i + this.execIfStatement( expr , ++ i ) ; 
						break ; 
					case LITERAL : 
						// Push current token content to stack
						this.getPStack().push( 
								Integer.parseInt( expr.getChildren().peek( i )
										.getContent().getString() ) ) ;
						break ;
					case WORD : 
						// Execute word
						if ( this.execWord( expr.getChildren().peek( i )
								.getContent().getString() ) )
						{
							// If errors occurred, print error
							this.printError( ErrorType.WORDUNDEFINED 
									, expr.getChildren().peek( i )
									.getContent().getString() ) ;
						}
						break ;
					case END : 
					case COMMENT_START :
					case COMMENT_CONTENT :
					case COMMENT_END :
						// Ignore these tokens
						break ; 

					default :
						// If errors occurred, print error
						this.printError( ErrorType.SYNTAX 
								, expr.getChildren().peek( i )
								.getContent().getString() ) ;
						bRC = true ;
						break ;
					}
				}
				else
				{
					// Execute child
					bRC = bRC || this.executeExpression( expr.getChildren().peek( i ) ) ;
				}
			}
		}
		else
		{
			// If attempting to execute leaf node 
			// (should never happen, but here just in case)
			this.printError( ErrorType.UNEXPECTED 
					, "[INTERNAL ERROR]" ) ;
			bRC = true ; 
		}

		return bRC ; 
	}
	/** Interprets a word */
	private boolean execWord( final String szWord )
	{
		boolean bRC = false ; 

		// If defined as self, is primitive, so execute 
		if ( this.getDictionary().checkWordDefinedAs( szWord , szWord ) )
		{
			switch ( szWord.trim().toLowerCase() )
			{
			case "+" :
				this.add() ;
				break ;
			case "-" :
				this.subtract() ;
				break ;
			case "*":
				this.multiply() ; 
				break ;
			case "/":
				this.divide() ; 
				break ;
			case "." : 
				this.printTop() ; 
				break ;
			case "u.r" : 
				this.printNumRJ() ;
				break ; 
			case "cr" : 
				System.out.println() ;
				break ; 
			case "words" :
				this.printWords() ; 
				break ;
			case "abort" : 
				// Clear stacks 
				this.getPStack().reset() ; 
				break ; 
			case "leave" :
				// Set counter to max to exit loop
				this.getDictionary().addVariable( "i" 
						, "" + Integer.MAX_VALUE ) ;
				break ; 
			case "drop" :
				this.dropTop() ;
				break ;
			case "swap" :
				this.swap() ;
				break ;
			case "dup" : 
				this.dup() ;
				break ;
			case "over" :
				this.over() ;
				break ;
			case "rot" :
				this.rot() ; 
				break ; 
			case ">r" : 
				// Move to return from parameter stack
				this.getRStack().push( this.getPStack().pop() ) ;
				break ; 
			case "r>" :
				// Move from return to parameter stack
				this.getPStack().push( this.getRStack().pop() ) ;
				break ;
			case "r@" : 
				// Copy from return to parameter stack
				this.getPStack().push( this.getRStack().peek() ) ;
				break ; 
			case "<" :
				this.checkLesser() ; 
				break ;
			case ">" :
				this.checkGreater() ;
				break ;
			case "=" :
				this.checkEqual() ;
				break ;
			case "<>" :
				this.checkNotEqual() ;
				break ;
			case "mod" : 
				this.mod() ;
				break ; 
			case ".s" :
				this.printStack() ;
				break ;
			case "key" :
				this.getKeyCode() ; 
				break ; 
			case "emit" : 
				this.emit() ;
				break ; 
			case "accept" :
				this.readString() ;
				break ; 
			}
		}
		// If defined, interpret
		else if ( this.getDictionary().checkWordDefined( szWord ) ) 
		{
			// Will recurse if execWord is called by execute 
			bRC = this.executeLine( this.getDictionary().getDefinition( szWord ) ) ;
		}
		// If undefined, return true
		else if ( this.getDictionary().checkVariableDefined( szWord ) == false )  
		{
			bRC = true ; 
		}

		return bRC ;
	}

	/** To be overridden with a method defining 
	 * what to do on a breakpoint */
	// Used by the debugger 
	// Required for the Debugger
	protected void breakpoint() {}

	/** Executes a loop */
	private int execDoLoop( final int iStart , final int iEnd , 
			final AST.Expression expr , int i , final int iInitialI )
	{
		int j = iStart ;
		int iLoopNum = 0 ; 

		// Keep outer loop's counter in word j
		this.getDictionary().addWord( "j" 
				, this.getDictionary().getDefinition( "i" ) ) ;

		// Changed from design in order to allow negative value for +loop
		// While counter has not reached end point
		while ( ( j < iEnd && iEnd > iStart ) || ( j > iEnd && iEnd < iStart ) )
		{
			// Increment loop count and throw error if maximum number of loops 
			// is exceeded, due to bad condition
			// Required to avoid freezing or OOM
			if ( ++ iLoopNum > Interpreter.MAX_LOOP )
			{
				// Throw error
				throw new RuntimeException( 
						"Error, maximum number of iterations exceeded" ) ;
			}

			// Keep inner loop's counter in word i
			this.getDictionary().addWord( "i" 
					, "" + j ) ;

			// For each child
			for ( i = iInitialI ; i < expr.getChildren().getLength() ; i ++ )
			{
				// If leaf
				if ( expr.getChildren().peek( i ).getContent() == null == false )
				{
					// If LOOP
					if ( expr.getChildren().peek( i )
							.getContent().getType() == Token.Type.LOOP )
					{
						// If '+loop'
						if ( expr.getChildren().peek( i ).getContent()
								.getString().equalsIgnoreCase( "+loop" ) )
						{
							// Add top of stack to counter
							j = j + this.getPStack().pop() ;
						}
						// If 'loop'
						else
						{
							// Increment counter
							++ j ; 
						}
						// Loop
						continue ;
					}
				}
				// If not a leaf, or not LOOP, execute
				this.executeExpression( expr.getChildren().peek( i ) ) ;
			}
		}

		return i ; 
	}

	/** Executes an if statement */
	private int execIfStatement( final AST.Expression expr , int i )
	{
		final Token.Type toExec ;

		// Check if predicate true or false
		if ( this.getPStack().pop() == 0 )
		{
			// 0 is false
			toExec = Token.Type.ELSE ; 
		}
		else // If true 
		{
			toExec = Token.Type.IF ; 
		}

		// For each expression
		for ( i = 0 ; i < expr.getChildren().getLength() ; i ++ )
		{
			// If leaf
			if ( ( expr.getChildren().peek( i ).getContent() == null ) == false )
			{
				// If 'then', exit
				if ( expr.getChildren().peek( i ).getContent().getType() 
						== Token.Type.THEN )
				{
					break ; 
				}
				// If 'else', execute if condition is false (otherwise do nothing)
				else if ( expr.getChildren().peek( i ).getContent().getType() 
						== toExec )
				{
					this.executeExpression( 
							expr.getChildren().peek( ++ i ) ) ;
				}
			}
		}

		return i ;
	}

	/** Prints a list of words */
	private void printWords()
	{
		System.out.print( this.getWords() ) ;
	}
	/** Gets a list of words */ 
	public String getWords() 
	{
		int i = 0 ;
		String szRet = "" ; 
		String szWord = "" ; 
		final Enumeration<String> wordList = 
				this.getDictionary().getWords().keys() ;

		// Repeat until no more words 
		while ( wordList.hasMoreElements() )
		{
			// Print word
			szWord = wordList.nextElement() + " " ;
			szRet = szRet + szWord ; 
			i++ ; 

			// Print newline every 8 words
			if ( i % 8 == 0 )
			{
				szRet = szRet + "\n" ;
			}
		}
		return szRet ; 		
	}
	public String getVariables() 
	{
		int i = 0 ;
		String szRet = "" ; 
		String szVariable = "" ; 
		final Enumeration<String> variableList = 
				this.getDictionary().getVariables().keys() ;

		// Repeat until no more words 
		while ( variableList.hasMoreElements() )
		{
			// Print word
			szVariable = variableList.nextElement() + " " ;
			szRet = szRet + szVariable ; 
			i++ ; 

			// Print newline every 8 words
			if ( i % 8 == 0 )
			{
				szRet = szRet + "\n" ;
			}
		}
		return szRet ; 		
	}

	/** Prints and removes top item of parameter stack */
	private void printTop() 
	{
		System.out.print( this.getPStack().pop() + " " ) ;
		return ;
	}
	/** Prints the top item in 5 digits, right-justified */
	private void printNumRJ()
	{
		final int iDigits = this.getPStack().pop() ;
		final int iNum = this.getPStack().pop() ;

		String szToPrint = "" ;

		// If 6 digits or more 
		if ( iNum >= Math.pow( 10 , iDigits ) )
		{
			szToPrint = " %0" + iDigits + "d" ;
			// Get remainder and infill with 0s 
			System.out.printf( szToPrint
					, iNum % Math.pow( 10 , iDigits ) ) ;	
		}
		else
		{
			szToPrint = " %" + iDigits + "d" ;
			// Print number
			System.out.printf( szToPrint , iNum ) ;	
		}

		return ; 
	}
	/** Removes top item of parameter stack */
	private void dropTop()
	{
		this.getPStack().pop() ;
		return ; 
	}
	/** Swaps top two items of stack */
	private void swap() 
	{
		final int[] iTemp = { 0 , 0 } ;
		iTemp[0] = this.getPStack().pop() ;
		iTemp[1] = this.getPStack().pop() ; 
		this.getPStack().add( iTemp[ 0 ] ) ; 
		this.getPStack().add( iTemp[ 1 ] ) ;
		return ; 
	}
	/** Duplicates the top item of the stack */
	private void dup()
	{ 
		this.getPStack().add( this.getPStack().peek() ) ;
		return ; 
	}
	/** Copies second item to the top */
	private void over()
	{
		this.getPStack().push( this.getPStack().peek( this.getPStack().getSize() - 2 ) ) ;
		return ;
	}
	/** Moves third item to the top */
	private void rot() 
	{
		int n3 = this.getPStack().pop() ;
		int n2 = this.getPStack().pop() ;
		int n1 = this.getPStack().pop() ; 

		this.getPStack().push( n2 ) ;
		this.getPStack().push( n3 ) ;
		this.getPStack().push( n1 ) ;
		return ; 
	}
	/** Integer addition */ 
	private void add()
	{
		this.getPStack().push( this.getPStack().pop() + this.getPStack().pop() ) ;
		return ; 
	}
	/** Integer subtraction */ 
	private void subtract() 
	{
		// n1 - n2
		this.getPStack().push( - this.getPStack().pop() + this.getPStack().pop() ) ;
		return ; 
	}
	/** Integer multiplication */
	private void multiply() 
	{
		this.getPStack().push( this.getPStack().pop() * this.getPStack().pop() ) ;
		return ; 
	}
	/** Integer division */
	private void divide() 
	{
		// To be consistent with other Forths, order is n1 / n2
		if ( this.getPStack().peek() == 0 )
		{
			printError( ErrorType.DIVISIONBYZERO , "/" ) ;
		}
		else
		{
			this.swap() ; 
			this.getPStack().push( this.getPStack().pop() / this.getPStack().pop() ) ;
		}
		return ; 
	}
	/** < */
	private void checkLesser()						
	{
		// If n1 < n2 
		if ( this.getPStack().pop() > this.getPStack().pop() )
		{
			this.getPStack().push( -1 ) ;
		}
		else 
		{
			this.getPStack().push( 0 ) ; 
		}
		return ;
	}
	/** > */
	private void checkGreater()
	{
		// If n1 > n2
		if ( this.getPStack().pop() < this.getPStack().pop() )
		{
			this.getPStack().push( -1 ) ;
		}
		else 
		{
			this.getPStack().push( 0 ) ; 
		}
		return ; 
	}
	/** = */
	private void checkEqual()
	{
		// If n2 = n1
		if ( this.getPStack().pop() == this.getPStack().pop() )
		{
			this.getPStack().push( -1 ) ;
		}
		else 
		{
			this.getPStack().push( 0 ) ; 
		}
		return ;
	}
	/** <> */
	private void checkNotEqual()
	{
		// If n2 <> n1
		if ( this.getPStack().pop() == this.getPStack().pop() == false )
		{
			this.getPStack().push( -1 ) ;
		}
		else 
		{
			this.getPStack().push( 0 ) ; 
		}
		return ;
	}
	/** mod */
	private void mod()
	{
		// n1 mod n2 
		this.swap() ;
		this.getPStack().push( this.getPStack().pop() % this.getPStack().pop() ) ;
		return ; 
	}
	/** Stack printer - prints all values in the stack */
	private void printStack()
	{
		for ( int i = 0 ; i < this.getPStack().getSize() ; i ++ )
		{
			System.out.print( this.getPStack().peek( i ) + " " ) ;
		}
		return ; 
	}
	/** Gets a character code from a key press and pushes to stack */
	private void getKeyCode() 
	{
		this.getPStack().push( readKey() ) ; 
		return ; 
	}
	/** Gets a character from user input. Requires a console to be initialised */ 
	private char readKey()
	{
		char chRet = 0 ; 
		final String szIn = this.getConsole().readLine() ;
		// If cancel clicked, throw error
		if ( szIn == null )
		{
			throw new RuntimeException ( "Operation cancelled " ) ;
		}
		// Return newline if no input
		else if ( szIn.equals( "" ) ) 
		{
			chRet = '\n' ; 
		}
		else
		{
			chRet = szIn.charAt( 0 ) ;
		}
		return chRet ; 
	}
	/** Gets a string from user input */ 
	private void readString() 
	{
		final String szIn = this.getConsole().readLine() ;
		// Push each char to the stack 
		for ( int i = 0 ; i < szIn.length() ; i ++ )
		{
			this.getPStack().push( szIn.charAt( i ) ) ;
		}
		return ; 
	}
	/** Prints a character */
	private void emit() 
	{
		System.out.print( ( char ) this.getPStack().pop() ) ; 
		return ; 
	}

	/** Prints error messages */
	private void printError( final ErrorType type , final String szOffender )
	{
		switch ( type ) 
		{
		case WORDUNDEFINED :
			System.err.print( "Error, word >>>" + szOffender + "<<< is undefined. " ) ;
			break ; 
		case SYNTAX : 
			System.err.print( "Error, invalid syntax at >>>" + szOffender + "<<< " ) ;
			break ;
		case DIVISIONBYZERO : 
			System.err.print( "Error, divison by zero at >>>" + szOffender + "<<<" ) ;
			break ;
		case UNEXPECTED : 
			System.err.println( "Error, unexpected >>>" + szOffender + "<<< " ) ;
			break ;
		default : 
			System.err.print( "Unknown error at >>>" + szOffender + "<<< " ) ;
			break ;
		}
		return ; 
	}

	/** Adds hard-coded words to the dictionary */ 
	private void initWords()
	{
		final String[] szBuiltin = {
				"words" , "abort" , "." , "u.r" , "r>" , ">r" , "r@" , "drop" , "swap"
				, "dup" , "over" , "rot" , "+" , "-" , "*" , "/" , ".\"" , ".s" , "cr"
				, ":" , "if" , "else" , "<" , ">" , "=" , "<>" , "mod" , "do" , "loop" 
				, "+loop" , "leave" , "!" , "@" , "?" ,  "variable" , "key" , "emit" 
				, "accept"
		} ;
		final String[][] szPredefined = {
				{ "j" , "0" } // Used to store outer loop counter
				, { "i" , "0" } // Used to store inner loop counter
				, { "true" , "-1" } // True is -1
				, { "false" , "0" } // False is 0
				, { "0=" , "0 =" }
				, { "0<" , "0 <" }
				, { "0>" , "0 >" }
				, { "1+" , "1 +" }
				, { "2dup" , "over over" }
				, { "2drop" , "drop drop" }
				, { "invert" , "0 =" } 
				, { "or" , "+ if -1 else 0 then" } // Boolean OR 
				, { "and" , "* if -1 else 0 then" } // Boolean AND
				, { "spaces" , "0 do 32 emit loop" } // Prints spaces 
				, { "/mod" , "over over mod swap rot swap /" } 
				, { "abs" , "dup 0 < if -1 * then" }
				, { "negate" , "-1 *" } 
				, { "min" , "over over < if drop else swap drop then" }
				, { "max" , "over over > if drop else swap drop then" }
				, { "*/" , ">r * r> /" }
		} ; 

		// Add built-in words
		for ( int i = 0 ; i < szBuiltin.length ; i ++ )
		{
			this.getDictionary().addWord( szBuiltin[i] , szBuiltin[i] ) ; 
		}
		// Add predefined words
		for ( int i = 0 ; i < szPredefined.length ; i ++ )
		{
			this.getDictionary().addWord( szPredefined[i][0] , szPredefined[i][1] ) ;
		}

		return ;
	}


	// Setters
	public void setPStack( final ParamStack pStack )
	{
		// Only set stack if length greater than 0 
		if ( pStack.getMaxSize() > 0 )
		{
			this.pStack = pStack ;
		}
		else
		{
			System.err.println( "Error, invalid stack size" ) ;
		}
		return ;
	}
	public void setRStack( final ParamStack rStack )
	{
		// Only set stack if length greater than 0 
		if ( rStack.getMaxSize() > 0 )
		{
			this.rStack = rStack ;
		}
		else
		{
			System.err.println( "Error, invalid stack size" ) ;
		}
		return ;
	}
	public void setDictionary( final Dictionary dict )
	{
		this.dict = dict ;
		return ;
	}
	public void setConsole( final FConsole console ) 
	{
		this.console = console ;
		return ;
	}

	// Getters
	public Dictionary getDictionary() { return this.dict ; }
	public ParamStack getPStack() { return this.pStack ; }
	public ParamStack getRStack() { return this.rStack ; }
	public FConsole getConsole() { return this.console ; }

	public static void main( final String[] args ) 
	{
		final Interpreter intp = new Interpreter() ; 
		final FConsole fconsole = new FConsole( intp ) ;
		intp.setConsole( fconsole ) ;

		// Test basic arithmetic
		intp.execute( "1 2 + . " ) ;
		intp.execute( "1 2 - . " ) ;
		intp.execute( "-4 2 * . " ) ; 
		intp.execute( "15 6 / . " ) ;  
		intp.execute( "15 6 mod . " ) ;
		// Test word compilation and printing
		intp.execute( ": HELLO .\" Hello, World!\" ; HELLO " ) ;
		// Test variables 
		intp.execute( "variable a  10 a !  a @ . " ) ;
		// Test comparisons
		intp.execute( "1 2 < . " ) ; 
		intp.execute( "2 1 < . " ) ; 
		intp.execute( "9 -9 > . " ) ; 
		intp.execute( "-18 -9 > . " ) ; 
		intp.execute( "-9 -9 = . " ) ; 
		intp.execute( "-8 -9 = . " ) ; 
		intp.execute( "-9 -9 <> . " ) ; 
		intp.execute( "-8 -9 <> . " ) ; 
		// Test print
		intp.execute( ".\" GOODBYE \" " ) ;
		// Test nested do loop, cr, i, j, U.R
		intp.execute( ": times 11 1 do 11 1 do i j * 5 U.R loop cr loop ; times " ) ; 
		// Test if
		intp.execute( " 0 if .\" true \" then .\" end\" " ) ;
		intp.execute( "-1 if .\" true \" 3 2 * . then .\" end\" " ) ;
		intp.execute( " 0 if .\" true \" 3 2 * . else .\" false \" 4 5 * . then .\" end\" " ) ;
		intp.execute( ": test 1 . ; test  : test 2 . ; test  forget test  test " ) ;

		// // Will keep asking for input until empty input given
		// intp.execute( "1 0 do key 10 = if 2 else 0 then i . +loop " ) ;

		return ;
	}

}
