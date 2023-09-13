/**
 * @version 5 2022-10-28
 * 
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/** The console */
// Inherits from JPanel in order to allow simpler integration into the GUI
@SuppressWarnings("serial")
public class FConsole extends JPanel implements ActionListener
{
	private final JTextArea consoleArea ;
	private final JTextField commandLine ; 
	private final JButton executionButton ; 
	private final Interpreter interpreter ;

	public FConsole( final Interpreter interpreter ) 
	{
		super() ; 
		// Print out area
		this.consoleArea = new JTextArea( "Usage: click 'Run' to run code,"
				+ " 'Debug' to run in debug mode.\n"
				+ "Insert 'break' in the code to insert a breakpoint.\n\n" ) ;
		// Area for entering lines of code
		this.commandLine = new JTextField( "( Insert code here )" , 48 ) ;
		// Button for execute
		this.executionButton = new JButton( "Execute" ) ;
		
		// Reference to interpreter
		this.interpreter = interpreter ; 

		// Prevent editing 
		this.consoleArea.setEditable( false ) ;
		
		// Add to panel
		this.add( this.commandLine ) ;
		this.add( this.executionButton ) ;

		// GUI stuff
		this.executionButton.addActionListener( this ) ;
		this.executionButton.setFocusPainted( false ) ;
		this.executionButton.setMnemonic( 'x' ) ;
		this.commandLine.setFont( GUI.FONT ) ;
		this.consoleArea.setWrapStyleWord( true ) ;
		this.consoleArea.setLineWrap( true ) ;

		return ; 
	}
	@Override
	public void actionPerformed ( final ActionEvent e )
	{
		switch ( e.getActionCommand() ) 
		{
		case "Execute" : 
			this.executeLine( this.commandLine.getText() ) ;
			break ; 
		}
		return ; 
	}
	
	/** Shows an input dialogue and gets user input */
	public String readLine()
	{
		return JOptionPane.showInputDialog( null , "Input: " ) ;
	}
	
	public JTextArea getConsoleArea() { return this.consoleArea ; }
	
	/** Calls upon the interpreter to execute a given string */
	private void executeLine( final String szLine ) 
	{
		this.interpreter.execute( szLine ) ;
		this.commandLine.setText( "" ) ;
		return ; 
	}
}
