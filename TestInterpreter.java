/**
 * @version 2 2022-11-15
 * 
 * Tests the interpreter
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public final class TestInterpreter 
{
	private static final int countLines( final BufferedReader br ) 
			throws IOException
	{
		int i = 0 ;
		while ( br.readLine() != null )
		{
			++ i ; 
		}
		return i ;
	}
	private static final String readFile( final String szPath )
			throws IOException
	{
		String sz = "" ;
		final File file = new File ( szPath ) ;
		BufferedReader br = new BufferedReader( new FileReader( file ) ) ;
		final int iLines = countLines( br ) ;
		br.close() ;
		br = new BufferedReader( new FileReader( file ) ) ;
		for ( int i = 0 ; i < iLines ; i ++ )
		{
			sz = sz + br.readLine() + "\n" ; 
		}
		return sz ; 
	}
	private static void printDiv()
	{
		for ( int i = 0 ; i < 64 ; i ++ )
		{
			System.out.print( "=" ) ;
		}
		System.out.println();
	}
	// Test
	public static void main( String[] args ) 
	{
		final Interpreter intp = new Interpreter() ; 
		final String[][][] szTestPaths = {
				{ 
					// Chapter 1
					{ "Spaces" , "ch01_01_spaces.fth" }
					, { "Asterisk" , "ch01_02_asterisk.fth" }
					, { "F" , "ch01_03_f.fth" }
					, { "XLERB" , "ch01_04_xlerb.fth" }
					, { "Greet" , "ch01_05_greet.fth" }
					, { "Arithmetic" , "ch01_06_arithemetic.fth" }
					, { "Stack" , "ch01_07_stack.fth" }
					, { "Empty Stack" , "ch01_08_stack_empty.fth" }
				}
				, {
					// Chapter 2
					{ "Addition" , "ch02_01_addition.fth" }		
					, { "Multiplication" , "ch02_02_multiplication.fth" }
					, { "Subtraction" , "ch02_03_subtraction.fth" }
					, { "Division" , "ch02_04_division.fth" }
					, { "Multiple" , "ch02_05_mutliple.fth" }
					, { "Definition Style" , "ch02_06_definition_style.fth" }
					, { "Sum" , "ch02_07_sum.fth" }
					, { "Flight distance" , "ch02_08_flight_distance.fth" }
					, { "Division operators" , "ch02_09_division_operators.fth" }
					, { "Stack manipulation" , "ch02_10_stack_manipulation.fth" }			
				}
				, {
					// Chapter 3
					{ "Dictionary" , "ch03_01_dictionary" }
				}
				, {
					// Chapter 4
					{ "Conditionals" , "ch04_01_conditionals.fth" }
					, { "Alternatives" , "ch04_02_alternatives.fth" } 
					, { "Nests" , "ch04_03_nests.fth" } 
					, { "Zeroes" , "ch04_04_zeroes.fth" } 
					, { "Boxtest" , "ch04_05_boxtest.fth" }
				} 
				, {
					// Chapter 5
					{ "Misc. maths" , "ch05_01_miscmaths.fth" }
					, { "Return stack" , "ch05_02_returnstack.fth" }
					, { "R%" , "ch05_03_rpercent.fth" }
					, { "Pi" , "ch05_04_pi.fth" }
				}
				, {
					// Chapter 6
					{ "Do loop" , "ch06_01_doloop.fth" }
					, { "Decade" , "ch06_02_decade.fth" }
					, { "Sample" , "ch06_03_sample.fth" }
					, { "Multiplication" , "ch06_04_multiplications.fth" }
					, { "Compound" , "ch06_05_compound_interest.fth" }
					, { "Rectangle" , "ch06_06_rectangle.fth" }
					, { "Poem" , "ch06_07_poem.fth" }
					, { "Nested loops" , "ch06_08_nested_loops.fth" }
					, { "+loop" , "ch06_09_plusloop.fth" }
				}
		} ;
		try
		{
			// For each chapter
			for ( int i = 0 ; i < szTestPaths.length ; i ++ )
			{
				// Execute each file
				for ( int j = 0 ; j < szTestPaths[i].length ; j ++ )
				{
					// // Reset interpreter
					// intp.reset()
					printDiv() ; 
					// Print chapter number, test number, and test name
					System.out.println( "Chapter " + ( i + 1 )
							+ " test " + ( j + 1 ) + " :: " + szTestPaths[i][j][0] ) ;
					printDiv() ;
					// Execute
					intp.execute( readFile( 
							String.format( "tests/ch%02d/%s" 
									, i + 1 , szTestPaths[i][j][1] ) ) ) ;
					// printDiv() ; 
					System.out.println( "\n\n" ) ;
				}
			}
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
		}
	}
}
