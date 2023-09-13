/**
 * @version 2 2022-11-15
 * 
 * Tests lexical and syntax analysis
 *
 */
public final class TestParser
{
	public enum TestType
	{
		NORMAL
		, ERRONEOUS
		, EXTREME
		, DESTRUCTIVE
	}
	private static class Test
	{
		final public String szName ;
		final public TestType type ;
		final public String szContent ;
		final public String szDescription ;

		public Test( final String szName 
				, final TestType type 
				, final String szContent 
				, final String szDescription )
		{
			this.szName = szName ;
			this.type = type ;
			this.szContent = szContent ;
			this.szDescription = szDescription ;
		}
	}	
	private static void printDiv( final String szDiv )
	{
		for ( int i = 0 ; i < 63 ; i ++ )
		{
			System.out.print( szDiv ) ;
		}
		System.out.println();
	}
	private static void displayTest( final int i , final Test test )
	{
		printDiv( "=" ) ;
		System.out.println( "T.6." + i + " : " ) ; 
		//System.out.println( "Test " + i + " :: " + test.szName + " :: " + test.type ) ;
		//System.out.println( test.szDescription ) ;
		printDiv( "-" ) ;
		System.out.println( "Code:" ) ;
		System.out.println( test.szContent ) ;
		printDiv( "-" ) ;
	}
	public static void main( String[] args ) 
	{
		final Test[] tests = {
				new Test( "Sequence" , TestType.NORMAL 
						, " 1 . 2 . 3 . cr " 
						, "  Tests that a sequence of non-branching tokens can"
								+ "\n  be placed into a single linear Expression, and "
								+ "\n  that numbers and words are correctly labelled" )
				
				, new Test( "Empty sequence" , TestType.EXTREME 
						, " " 
						, "  Tests that empty sequences can be correctly parsed")
				
				, new Test( "Single token sequence" , TestType.EXTREME 
						, "9 " 
						, "  Tests a single-token sequence can be correctly parsed")
				
				, new Test( "Print statements" , TestType.NORMAL
						, ".\" Hello, World!\" " 
						, "  Tests that print statements are correctly identified"
								+ "\n  and each part is assigned the correct Token type" )
				
				, new Test( "Empty print statements" , TestType.EXTREME
						, ".\" \" " 
						, "  Tests that printing empty strings can be correctly parsed")
				
				, new Test( "Missing print statement delimiter" , TestType.ERRONEOUS
						, ".\" Hello, World! " 
						, "  Tests that printing without a closing quotation mark is "
								+ "\n  correctly handled. The runtime exception is handled "
								+ "\n  by the interpreter")
				
				, new Test( "Comments" , TestType.NORMAL 
						, "( this is a comment ) " 
						, "  Tests that comments are correctly identified and that"
								+ "\n  each part is assigned the correct Token type")
				
				, new Test( "Missing closing bracket" , TestType.ERRONEOUS
						, "( this is a comment " 
						, "  Tests that comments without closing brackets are "
								+ "\n  correctly handled. A runtime exception should be thrown.")
				
				, new Test( "Definition" , TestType.NORMAL 
						, ": name definition ; " 
						, "  Tests that word definitions are correctly identified and"
								+ "\n  that each part is assigned the correct Token type")
				
				, new Test( "Definition" , TestType.EXTREME 
						, ": name ; "
						, "  Tests that words with empty definitions are correctly "
								+ "\n  identified, that each part is assigned the correct"
								+ "\n  token type, and that the tokens are placed correctly "
								+ "\n  into the Expression " )
				
				, new Test ( "Missing semicolon" , TestType.ERRONEOUS 
						, ": name definition "
						, "  Tests that a missing semicolon can be correctly handled. "
								+ "\n  The runtime exception will be handled by the interpreter. " )
				
				, new Test ( "If Else Then Statement" , TestType.NORMAL 
						, "0 0= if .\" true\" else .\" false \" then " 
						, "  Tests that an if statement containing an ELSE clause and"
								+ "\n  a THEN is correctly parsed.")
				
				, new Test ( "If Then Statement" , TestType.NORMAL 
						, "0 0= if .\" true\" then " 
						, "  Tests that an if statement without an ELSE but containing "
								+ "\n  a THEN is correctly parsed." )
				
				, new Test ( "Empty If Else Then Statement" , TestType.EXTREME
						, "0 0= if else then " 
						, "  Tests that an if statement with empty clauses is correctly"
								+ "\n  parsed." )
				
				, new Test ( "Missing Then" , TestType.ERRONEOUS
						, "0 0= if .\" true\" else " 
						, "  Tests that an if statement without a THEN is correctly"
								+ "\n  parsed." )
				
				, new Test ( "No Else, missing Then" , TestType.ERRONEOUS
						, "0 0= if .\" true\" " 
						, "  Tests that an if statement without an ELSE and without a"
								+ "\n  THEN is correctly parsed." )
				
				, new Test ( "Do Loop" , TestType.NORMAL
						, "1 10 1 do i * loop . " 
						, "  Tests that a do loop with valid syntax is correctly tokenised"
								+ "\n  and correctly parsed." )
				
				, new Test ( "Do +Loop" , TestType.NORMAL
						, "10 1 do i * 2 +loop . " 
						, "  Tests that a do +loop with valid syntax is correctly "
								+ "\n  tokenised and correctly parsed." )
				
				, new Test ( "Missing loop" , TestType.ERRONEOUS
						, "10 1 do i . " 
						, "  Tests that a do loop without a LOOP is correctly handled. "
								+ "\n  The runtime exception will be handled by the interpreter." )
				
				, new Test ( "Nested if statements" , TestType.NORMAL
						, "true if .\" true1\" "
								+ "\n\tfalse if .\" true2\" else .\" false2\" "
								+ "\n\t\ttrue if .\" true3\" else .\" false3\" then "
								+ "\n\tthen else .\" false1\" \nthen " 
						, "  Tests that a nested if statements are correctly tokenised"
								+ "\n  and parsed. " )
				
				, new Test ( "Nested do loops" , TestType.NORMAL
						, "10 0 do 5 0 do i j * 3 u.r loop cr loop cr " 
						, "  Tests that a nested do loops are correctly tokenised"
								+ "\n  and parsed. " )
				
				, new Test ( "If statement nested in a nested do loop" , TestType.NORMAL
						, "10 0 do 5 0 do i j * 3 mod 0= \nif .\" 3\" else i j * 3 u.r then loop cr loop cr " 
						, "  Tests that an if statement in a nested do loop is correctly "
								+ "\n  tokenised and parsed. " )
				
				/*
				, new Test ( "Do loop nested in a nested if statement" , TestType.NORMAL
						, "False False True if .\" true1\" "
								+ "\n\tif .\" true2\" else .\" false2\" "
								+ "\n\t\tif .\" true3\" else "
								+ "\n\t\t\t10 0 do .\" false3\" loop "
								+ "\n\t\tthen \n\tthen \nelse .\" false1\" \nthen "  
						, "  Tests that a do loop in a nested if statement is correctly "
								+ "\n  tokenised and parsed. " )
				*/
				, new Test ( "Many ifs" , TestType.EXTREME 
						, "1 if 1 if 1 if 1 if 1 if "
								+ "1 if 1 if 1 if 1 if 1 if "
								+ "1 if 1 if 1 if 1 if 1 if "
								+ "1 if 1 if 1 if 1 if 1 if "
								+ "1 if 1 if 1 if 1 if 1 if "
								//+ "if if if if if "
								//+ "if if if if if "
								//+ "if if if if if "
								//+ "if if if if if "
								//+ "then then then then then " 
								//+ "then then then then then "
								//+ "then then then then then "
								//+ "then then then then then "
								+ "then then then then then "
								+ "then then then then then "
								+ "then then then then then "
								+ "then then then then then "
								+ "then then then then then "
						, "  Tests that many levels of nesting can be parsed")
				, new Test ( "Many dos" , TestType.EXTREME 
						, "2 0 do 2 0 do 2 0 do 2 0 do 2 0 do "
								+ "2 0 do 2 0 do 2 0 do 2 0 do 2 0 do "
								+ "2 0 do 2 0 do 2 0 do 2 0 do 2 0 do "
								+ "2 0 do 2 0 do 2 0 do 2 0 do 2 0 do "
								+ "2 0 do 2 0 do 2 0 do 2 0 do 2 0 do .\" -\" "
								+ "1 +loop loop loop loop loop "
								+ "1 +loop loop loop loop loop "
								+ "1 +loop loop loop loop loop "
								+ "1 +loop loop loop loop loop "
								+ "1 +loop loop loop loop loop "
						, "  Tests that many levels of nesting can be parsed")
		} ; 
		
		for ( int i = 0 ; i < tests.length ; i ++ )
		{
			// Display test information
			displayTest( i + 1 , tests[i] ) ;
			try
			{
				// Parse and execute 
				new Parser().parse( new Lexer().tokenise( 
						tests[i].szContent.replaceAll( "\n|\t" , " " ) ) ).display(0) ;
			}
			catch ( final RuntimeException e )
			{
				// If runtime exception, print message
				System.out.println( e.getMessage() ) ;
			}
			printDiv( "=" ) ;
			System.out.println( "" ) ;
		}
	}
}
