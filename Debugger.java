/**
 * @version 5 2022-11-05
 *
 */

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

// Inherits from Interpreter, as the Debugger will do the same things as the 
// Interpreter, and will be used in the same way, but will display its values 
// on a breakpoint (as opposed to ignoring breakpoints).
public class Debugger extends Interpreter 
{
	final public static Font FONT = GUI.FONT ;
	private int iBreakpointNum ;

	public Debugger( final Interpreter interpreter )
	{
		super() ; 
		return ; 
	}
	
	// Not in design, but added to allow 
	// reset of breakpoint number 
	@Override
	public void reset() 
	{
		// Call Interpreter.reset() 
		super.reset() ;
		// Reset breakpoint number
		this.setBreakpointNum( 1 ) ;
		return ;
	}

	// At a breakpoint
	@Override
	protected void breakpoint()
	{
		// GUI stuff
		final JTextArea text = new JTextArea( this.getValuesToDisplay() ) ;
		final JScrollPane scrollPane = new JScrollPane( text ) ;
		text.setFont( Debugger.FONT ) ;
		scrollPane.setPreferredSize( new Dimension( 512 , 512 ) ) ;
		
		// Show dialogue
		// This pauses the program until the user chooses to proceed
		JOptionPane.showMessageDialog( null , scrollPane 
				, "Break" , JOptionPane.OK_OPTION ) ;
		return ; 
	}

	/**
	 * @return text displaying the breakpoint number, and values of the stacks and dictionary 
	 */
	private String getValuesToDisplay()
	{
		// String to return
		String szRet = "( Debugger ) \ncurrent breakpoint := " 
				+ this.getBreakpointNum() + "\n\n( Parameter stack ) \n" ;
		// Increments iBreakpointNum
		this.setBreakpointNum( this.getBreakpointNum() + 1 ) ;
		// Arrays to store Words and Variables
		String[] szWords ; 
		String[] szVariables ; 

		// Print stacks
		szRet = szRet + getStack( this.getPStack() )
		+ "\n( Return stack ) \n"
		+ getStack( this.getRStack() ) ; 

		// Print words
		szRet = szRet + "\n( Words ) \n" ; 

		// Split into array at whitespace
		szWords = this.getWords().split( "\n| " ) ; 

		// Print definitions
		for ( int i = 0 ; i < szWords.length ; i ++ )
		{
			// Print if not empty string 
			if ( szWords[i].trim() == "" == false )
			{
				szRet = String.format( "%s%-12s :=   %-64s" 
						, szRet
						, szWords[i].trim() 
						, this.getDictionary().getDefinition( 
								szWords[i].trim().toLowerCase() ) 
						).trim() + "\n" ;
			}
		}

		// Print variables
		szRet = szRet + "\n( Variables ) \n" ; 

		// Split into array at whitespace
		szVariables = this.getVariables().split( "\n| " ) ; 

		// Print definitions
		for ( int i = 0 ; i < szVariables.length ; i ++ )
		{
			// Print if not empty string 
			if ( ( szVariables[i].trim() == "" ) == false )
			{
				szRet = String.format( "%s%-12s :=   %-64s" 
						, szRet
						, szVariables[i].trim() 
						, this.getDictionary().getVariable( 
								szVariables[i].trim().toLowerCase() ) 
						).trim() + "\n" ;
			}
		}


		return szRet ; 
	}

	/** @return a String containing the values of the passed stack */
	public String getStack( final ParamStack stack ) 
	{
		String szRet = "" ;
		// If empty
		if ( 0 >= stack.getSize() )
		{
			szRet = szRet + "empty\n" ; 
		}
		// Won't run if empty, so 'else' is unnecessary
		for ( int i = 0 ; i < stack.getSize() ; i ++ )
		{
			// Add top of stack
			szRet = szRet + stack.peek( i )  ;

			// If not at end
			if ( i != stack.getSize() - 1 ) 
			{
				// Append comma
				szRet = szRet + ", " ;
				// At end of line, add new line
				if ( ( i + 1 ) % 8 == 0 )
				{
					szRet = szRet + "\n" ; 
				}
			}
		}
		return szRet ; 
	}
	
	// Not in design, but added to encapsulate 
	// Setters
	public void setBreakpointNum( final int iNum )
	{
		this.iBreakpointNum = iNum ; 
		return ;
	}
	// Getters
	public int getBreakpointNum() { return this.iBreakpointNum ; }
}
