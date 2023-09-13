/**
 * @version 5 2022-11-08
 *
 * The main object. Uses everything else
 *
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.UIManager;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class GUI implements ActionListener
{
	// Constants (gui stuff)
	public final static Font FONT = new Font(
			Font.MONOSPACED 
			, Font.PLAIN 
			, 12 ) ;
	public final static Color GREY = new Color( 220 , 220 , 227 ) ; 
	public final static int NUMLINES = 2048 ; // Number of line numbers in the editor
	// Aspect ratio 4:3
	private final static int WIDTH = 1024 * 3/4 ;
	private final static int HEIGHT = 1024 * 9/16 ;

	// These variables are not accessed by other classes, 
	//  so should not need getters or setters
	// Private class members
	private Interpreter interpreter ; 
	private Debugger debugger ;
	private Autocompleter autocompleter ;
	private FileTreePane fileTree ; 

	// GUI stuff
	private JTextPane editorArea ; // Stores code 
	private JTextArea numberArea ; // Stores syntax error messages
	private JTextArea consoleArea ; // Console

	private JFrame frame ; // Frame for GUI
	private File file ; // Stores currently open file

	/** Constructor */
	public GUI() 
	{
		this.reset() ;
		return ; 
	}
	/** Constructor with file. Not used, but may be useful */
	public GUI( final File file )
	{
		this() ;
		this.setEditorToFile( file ) ;
		return ; 
	}
	/** Creates the layout and displays */
	private void reset()
	{
		this.file = null ;
		this.initialiseComponents() ;

		// Taken from StackOverflow
		// Set standard out and err to print to console
		final PrintStream consoleOutput = new PrintStream( 
				new TextAreaOutputStream( this.consoleArea ) 
				) ;  
		System.setOut( consoleOutput ) ;
		System.setErr( consoleOutput ) ;
		return ; 
	}

	/** Sets components */
	private void initialiseComponents()
	{
		final JSplitPane[] panels = new JSplitPane[3] ;
		final JScrollPane editorArea ;
		final FConsole console ;

		this.editorArea = new JTextPane() ; 
		this.numberArea = new JTextArea() ;
		this.interpreter = new Interpreter() ; 
		this.debugger = new Debugger( this.interpreter ) ; 
		this.autocompleter = this.interpreter.getDictionary().getAutocompleter() ;

		console = new FConsole( this.interpreter ) ; 
		this.consoleArea = console.getConsoleArea() ; 
		this.interpreter.setConsole( console ) ; 

		this.fileTree = new FileTreePane() ; 
		this.fileTree.setGUI( this ) ; 

		this.editorArea.setText( "( Insert code here )\n" ) ; 
		this.numberArea.setEditable( false ) ;
		this.numberArea.setBackground( GUI.GREY ) ;
		this.editorArea.setFont( FONT ) ;
		this.numberArea.setFont( FONT ) ;
		this.consoleArea.setFont( FONT ) ;

		this.initialiseEditor() ;

		// Align line numbers with editor pane (GUI stuff) 
		editorArea = new JScrollPane ( 
				this.editorArea
				, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
				, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED 
				) ;
		// Add line numbers
		editorArea.setRowHeaderView( this.numberArea ) ;

		// Console / Command Line
		panels[0]  = new JSplitPane( 
				JSplitPane.VERTICAL_SPLIT  
				, new JScrollPane( console.getConsoleArea() ) 
				, console  
				) ;
		// No need to be resized
		panels[0].setDividerSize( 0 ) ;

		// Editor / (Console / Command Line)
		panels[1] = new JSplitPane( 
				JSplitPane.VERTICAL_SPLIT
				, editorArea
				, panels[0] 
				) ;
		// File Tree Pane | (Editor / (Console / Command Line))
		panels[2] = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT 
				, new JScrollPane( this.fileTree ) , panels[1]
				) ;
		// Put dividers in correct places
		panels[0].setResizeWeight( 1.0 ) ;   
		panels[1].setResizeWeight( 0.8 ) ; 
		panels[2].setResizeWeight( 0.2 ) ; 

		// Create root frame and set title
		this.frame = new JFrame( "New File*" ) ;

		this.frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ) ;
		this.frame.setJMenuBar( initialiseMenuBar() ) ;

		// Add panels
		this.frame.add( panels[ panels.length - 1 ] ) ; 

		// Display frame
		this.frame.pack() ;
		this.frame.setSize( GUI.WIDTH , GUI.HEIGHT ) ;
		this.frame.setVisible( true ) ;  
		this.frame.setResizable( true ) ;

		return ; 		
	}
	/** Creates the menu bar */
	private JMenuBar initialiseMenuBar() 
	{
		final JMenuBar menubar = new JMenuBar() ; 
		final JMenu fileMenu = new JMenu( "File" ) ;
		final JMenu editMenu = new JMenu( "Edit" ) ;
		final JButton runButton= new JButton( "Run" ) ;
		final JButton debugButton= new JButton( "Debug" ) ;

		final JMenuItem[] fileMenuItems = { 
				new JMenuItem( "New" ) 
				, new JMenuItem( "Open" )
				, new JMenuItem( "Save" )
				, new JMenuItem( "Save as" )
		} ; 
		final JMenuItem[] editMenuItems = {
				new JMenuItem( "Copy" ) 
				, new JMenuItem( "Paste" ) 
				, new JMenuItem( "Cut" )
				, new JMenuItem( "Find" )  
				, new JMenuItem( "Autocomplete" )  
				, new JMenuItem( "Highlight")
		} ;

		// Assign keyboard shortcuts
		fileMenu.setMnemonic( 'f' ) ;	
		editMenu.setMnemonic( 'e' ) ;
		fileMenuItems[0].setMnemonic( 'n' ) ;
		fileMenuItems[0].setAccelerator( KeyStroke.getKeyStroke( 
				KeyEvent.VK_N , KeyEvent.CTRL_DOWN_MASK ) ) ;
		fileMenuItems[1].setMnemonic( 'o' ) ;
		fileMenuItems[1].setAccelerator( KeyStroke.getKeyStroke( 
				KeyEvent.VK_O , KeyEvent.CTRL_DOWN_MASK ) ) ;
		fileMenuItems[2].setMnemonic( 's' ) ;
		fileMenuItems[2].setAccelerator( KeyStroke.getKeyStroke( 
				KeyEvent.VK_S , KeyEvent.CTRL_DOWN_MASK ) ) ;
		fileMenuItems[3].setMnemonic( 'a' ) ;
		fileMenuItems[3].setAccelerator( KeyStroke.getKeyStroke( 
				KeyEvent.VK_S 
				, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK ) ) ;
		editMenuItems[0].setMnemonic( 'c' ) ;
		editMenuItems[0].setAccelerator( KeyStroke.getKeyStroke( 
				KeyEvent.VK_C , KeyEvent.CTRL_DOWN_MASK ) ) ;
		editMenuItems[1].setMnemonic( 'p' ) ;
		editMenuItems[1].setAccelerator( KeyStroke.getKeyStroke( 
				KeyEvent.VK_V , KeyEvent.CTRL_DOWN_MASK ) ) ;
		editMenuItems[2].setMnemonic( 'u' ) ;
		editMenuItems[2].setAccelerator( KeyStroke.getKeyStroke( 
				KeyEvent.VK_X , KeyEvent.CTRL_DOWN_MASK ) ) ;
		editMenuItems[3].setMnemonic( 'f' ) ;
		editMenuItems[3].setAccelerator( KeyStroke.getKeyStroke( 
				KeyEvent.VK_F , KeyEvent.CTRL_DOWN_MASK ) ) ;
		editMenuItems[4].setMnemonic( 'a' ) ;
		editMenuItems[4].setAccelerator( KeyStroke.getKeyStroke( 
				KeyEvent.VK_A 
				, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK ) ) ;
		editMenuItems[5].setMnemonic( 'h' ) ;
		editMenuItems[5].setAccelerator( KeyStroke.getKeyStroke( 
				KeyEvent.VK_H
				, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK ) ) ;
		runButton.setMnemonic( 'r' ) ;
		debugButton.setMnemonic( 'd' ) ;

		// Add items to menus
		this.initialiseMenu( fileMenu , fileMenuItems ) ;
		this.initialiseMenu( editMenu , editMenuItems ) ;

		// Setup buttons
		runButton.setActionCommand( "Run" ) ;
		runButton.addActionListener( this ) ;
		runButton.setBorderPainted( true ) ;
		runButton.setFocusPainted( false ) ;
		debugButton.setActionCommand( "Debug" ) ;
		debugButton.addActionListener( this ) ;
		debugButton.setBorderPainted( true ) ;
		debugButton.setFocusPainted( false ) ;

		menubar.add( fileMenu ) ;
		menubar.add( editMenu ) ;
		menubar.add( new JLabel( "  |  " ) ) ;
		menubar.add( runButton ) ; 
		menubar.add( debugButton ) ; 
		menubar.add( new JLabel( "  |  " ) ) ;

		return menubar ; 
	}
	/** Adds an actionListener to each item and adds each item to the menu */
	private void initialiseMenu( final JMenu menu , final JMenuItem[] items )
	{
		int i ;
		for ( i = 0 ; i < items.length ; i ++ )
		{
			items[ i ].addActionListener( this ) ;
			menu.add( items[ i ] ) ;
		}
		return  ; 
	}

	/** Sets the line numbers, title behaviour */
	// Some GUI elements changed from Design
	private void initialiseEditor()
	{
		String numbers = "" ;

		this.numberArea.setText( "" ) ;
		this.numberArea.setEditable( false ) ;
		this.numberArea.putClientProperty(
				JEditorPane.HONOR_DISPLAY_PROPERTIES, true ) ;
		this.numberArea.setFont( GUI.FONT ) ;
		this.editorArea.setFont( GUI.FONT ) ;

		// Insert line numbers
		for ( int i = 1 ; i <= GUI.NUMLINES ; i ++ )
		{
			numbers = numbers + i + "\n" ;
		}
		this.numberArea.setText( numbers ) ;

		this.editorArea.getDocument().addDocumentListener( new DocumentListener() 
		{
			@Override
			public void insertUpdate( final DocumentEvent e ) 
			{
				this.update() ;
				return ;
			}
			@Override
			public void removeUpdate( final DocumentEvent e ) 
			{
				this.update() ;
				return ;				
			}
			@Override
			public void changedUpdate( final DocumentEvent e )
			{
				this.update() ;
				return ;
			}
			private void update() 
			{
				// Put * by file name if modified
				if ( GUI.this.file != null && 
						( GUI.this.frame.getTitle().equals( 
								GUI.this.file.getName() + "*" ) == false ) )
				{
					GUI.this.frame.setTitle( GUI.this.file.getName() + "*" ) ;
				}
				return ; 
			}
		} ) ;

		return ;
	}

	/** Sets the editor's contents to the file contents */
	public void setEditorToFile( final File file )
	{
		this.consoleArea.append( "\nFile opened: " + file.getPath() + " :: ok \n" ) ;

		// Put into textArea
		this.editorArea.setText( this.openFile( file ) ) ; 
		// Set window title
		this.frame.setTitle( file.getName() ) ; 
	}
	// Essentially just same as file.toString() ?
	/** Returns a file as a string */
	private String openFile( final File file )
	{
		String szFileContents = "" ;
		BufferedReader br ;

		int iLines = 0 ;
		int iLineNumber = 0 ;

		try
		{
			// Generate new file tree 
			this.fileTree.generateTree( file.getParentFile() ) ;

			// Open file
			br = new BufferedReader( new FileReader( file ) ) ;

			// Count lines
			while ( br.readLine() != null )
			{
				++ iLines ;  
			}
			br.close() ;

			// Re-open file to reset cursor position
			br = new BufferedReader( new FileReader( file ) ) ;

			// Add lines to String 
			for ( iLineNumber = 0 ; iLineNumber < iLines ; iLineNumber ++ )
			{
				szFileContents = szFileContents + br.readLine() + "\n" ;
			}

			br.close() ; 
			this.interpreter.reset() ;
		}
		catch( final FileNotFoundException e ) 
		{
			this.consoleArea.append( this.getClass().getName() + 
					" :: openFile() :: Error, file >>>" + file.getPath() + "<<< not found. " ) ;
		} 
		catch( final Exception e )
		{
			this.consoleArea.append( this.getClass().getName() + 
					" :: openFile() :: Error opening file >>>" + file.getPath() + "<<< " ) ;			
		}
		return szFileContents ; 
	}

	/** Sets a file's contents to a String */
	private void writeFile( final File file , final String szContents )
	{
		final BufferedWriter bw ; 
		try 
		{
			bw = new BufferedWriter( new FileWriter( file ) ) ;
			bw.write( szContents ) ;
			bw.close() ; 
			// Generate new file tree
			this.fileTree.generateTree( file.getParentFile() ) ;
		} 
		catch ( final IOException e ) 
		{
			this.consoleArea.append( this.getClass().getName() + 
					" :: writeFile() :: Error opening file >>>" + file.getPath() + "<<< " ) ;	
		} 
		return ; 
	}

	/** Advances the text in the pane to the query */
	private boolean advanceToString( final String szQuery , final JTextPane pane )
	{
		boolean bRC = true ;
		boolean bPass = false ;
		int i = pane.getCaretPosition() - 1 ;

		final String szPaneText = pane.getText().replaceAll( "(\n|\t)", " " ) ;
		final String szQueryText = szQuery.replaceAll( "(\n|\t)" , " " ) ;

		// While not query, or not end 
		do
		{
			// Increments i and checks if at end 
			if ( ++ i > szPaneText.length() - szQueryText.length() )
			{
				if ( bPass == true )
				{
					// Return false if second pass
					bRC = false ;
					break ; 
				}
				else
				{
					// Return to start of text if end of first pass
					// and query not found
					i = 0 ;
					pane.setCaretPosition( i ) ;
					// Set flag for second pass
					bPass = true ; 
				}
			}
			else
			{
				// Advance to next character if not at end
				pane.setCaretPosition( i ) ;
			}

		} while ( ( szPaneText.substring( i , i + szQueryText.length() )
				.equalsIgnoreCase( szQueryText ) == false ) ) ;
		return bRC ; 
	}
	/** Finds a word in a string, onwards from the current caret 
	 * position in the text area */
	// Changed from design, as the recursive method is
	// less efficient than this iterative method
	private boolean find( final String szQuery )
	{ 
		final boolean bRC ;

		// Added presence check after testing (T.3.6)
		if ( szQuery == null )
		{
			System.err.println( "Error: no input." ) ;
			bRC = false ;
		}
		else if ( szQuery.length() < 1 )
		{
			System.err.println( "Error: no input." ) ;
			bRC = false ;
		}
		else
		{
			// Go to string
			bRC = this.advanceToString( szQuery , this.editorArea ) ;
			// If found
			if ( bRC )
			{
				// Request focus and select text
				this.editorArea.requestFocus() ;
				this.editorArea.select( this.editorArea.getCaretPosition() , 
						this.editorArea.getCaretPosition() + szQuery.length() ) ;
			}
		}
		return bRC ;
	}

	/** Stores colour values */
	private static enum Cols
	{
		COLON ( new Color ( 80 , 80 , 200 ) )
		, PRINT ( new Color ( 200 , 150 , 60 ) )
		, BREAK ( new Color( 200 , 80 , 80 ) )
		, COMMENT ( new Color( 40 , 150 , 40 ) )
		, DOLOOP ( new Color( 150 , 80 , 150 ) )
		, DEFINITION ( new Color( 200 , 180 , 70 ) ) 
		, VARIABLE ( new Color( 140 , 40 , 70 ) )
		;
		public final Color colour ;

		private Cols( final Color col )
		{
			this.colour = col ;
			return ;
		}
	}
	/** Colours text in the editor
	 * @return true if errors, false otherwise */
	private boolean highlight( final JTextPane pane )
	{
		// Tokenise
		final Object[] tokens ;
		final int iInitialCaret = pane.getCaretPosition() ;
		Color col = Color.BLACK ;
		boolean bRC = false ; 
		try
		{
			tokens = new Lexer().tokenise( 
					( pane.getText() + "  " ).replaceAll( "(\n|\t)" , " " ) ).toArray() ;

			// Go to start
			pane.setCaretPosition( 0 ) ;

			// For each token
			for ( int i = 0 ; i < tokens.length ; i ++ )
			{
				// Colour text according to token type
				switch ( ( ( Token ) tokens[i] ).getType() )
				{
				case BREAK :
					col = Color.RED ;
					break ; 
				case COMMENT_START :
				case COMMENT_CONTENT :
				case COMMENT_END :
					col = Cols.COMMENT.colour ;
					break ; 
				case IF :
				case ELSE :
				case THEN :
					col = Cols.BREAK.colour  ;
					break ; 
				case DO :
				case LOOP :
					col = Cols.DOLOOP.colour ;
					break ;
				case COLON :
				case SEMICOLON :
				case WORD_NAME :
					col = Cols.COLON.colour ;
					break ; 
				case DEFINITION : 
					col = Cols.DEFINITION.colour ;
					break ; 
				case PRINT_START : 
				case PRINT_END :
				case PRINT_CONTENT :
					col = Cols.PRINT.colour ;
					break ;
				case VARIABLE : 
				case VARIABLE_STORE :
				case VARIABLE_GET :
				case VARIABLE_PRINT : 
					col = Cols.VARIABLE.colour ; 
					break ; 

				default : 
					col = Color.BLACK ; 
					break ; 
				}
				this.colourString( ( ( Token ) tokens[i] ).getString()
						, col , pane ) ;
			}
		}
		catch ( final RuntimeException e )
		{
			bRC = true ; 
		}
		finally 
		{
			// Return to initial position
			pane.setCaretPosition( iInitialCaret ) ;
			// Reset colour
			pane.setCharacterAttributes( StyleContext.getDefaultStyleContext()
					.addAttribute( 
							SimpleAttributeSet.EMPTY 
							, StyleConstants.Foreground
							, Color.BLACK )
					, false ) ;
			pane.replaceSelection( pane.getSelectedText() ) ;
		}
		return bRC ; 
	}
	/** Replaces a String in the pane with coloured text */
	private void colourString( final String szQuery 
			, final Color col , final JTextPane pane )
	{
		final String szCut ;

		if ( szQuery != null )
		{
			// Go to query 
			this.advanceToString( szQuery.trim() , pane ) ;
			// Select query
			pane.requestFocus() ;
			pane.select( pane.getCaretPosition() , 
					pane.getCaretPosition() + szQuery.length() ) ;
			// Store selected text
			szCut = pane.getSelectedText() ; 
			// Delete query
			pane.cut() ;
			// Set colour to @param col
			// https://docs.oracle.com/javase/8/docs/api/javax/swing/JTextPane.html
			pane.setCharacterAttributes( StyleContext.getDefaultStyleContext()
					.addAttribute( 
							SimpleAttributeSet.EMPTY // Use empty attribute set
							, StyleConstants.Foreground // Set foreground colour
							, col // To col 
							) , false ) ;
			// Insert coloured text
			pane.replaceSelection( szCut ) ;
			// Reset colour
			pane.setCharacterAttributes( StyleContext.getDefaultStyleContext()
					.addAttribute( 
							SimpleAttributeSet.EMPTY 
							, StyleConstants.Foreground
							, Color.BLACK )
					, false ) ;
		}
	}

	/** Executes the text in the editor using the specified Interpreter,
	 * and calls the syntax highlighter */
	// Not in design. Implemented in solution for cleaner code
	private void exec( final Interpreter intp , final JTextPane editorArea )
	{
		// Highlight syntax
		this.highlight( this.editorArea ) ;

		// Reset interpreter to clear previous words, stacks, etc. 
		intp.reset() ;
		// Changed from design to allow words added to the autocompleter to 
		// persist across instances
		intp.getDictionary().setAutocompleter( this.autocompleter ) ;
		// Execute
		intp.execute( editorArea.getText() ) ; 	
		return ; 
	}

	/** Runs when an action is performed, e.g. a menu is clicked */
	@Override
	public void actionPerformed( final ActionEvent e ) 
	{
		final JFileChooser fileChooser ; 
		final int iOpt ;
		final String[] szSuggestions ; 
		String szOptions = "" ;
		String szInput = "" ;
		int i = 0 ; 

		// Get which menu has been clicked
		switch ( e.getActionCommand() )
		{
		case "New" : 
			// Set to empty editor
			this.editorArea.setText( "" ) ;
			// Set frame title
			this.frame.setTitle( "*New File" ) ; 
			break ; 

		case "Open" : 
			// Choose and open file 
			fileChooser = new JFileChooser( "" ) ;
			iOpt = fileChooser.showOpenDialog( this.frame ) ;

			// If user clicks open, open file
			if( iOpt == JFileChooser.APPROVE_OPTION )
			{
				if ( false == ( this.file == null 
						|| this.file == fileChooser.getSelectedFile() ) )  
				{
					this.file = fileChooser.getSelectedFile() ;
					this.setEditorToFile( this.file ) ;
				}
				else
				{
					this.setEditorToFile( fileChooser.getSelectedFile() ) ;
				}
			}
			else
			{
				this.consoleArea.append( "\nFile open operation canceled :: ok \n" ) ;
			}

			break ;

		case "Save" :  
			// If file not already opened
			if ( this.file == null )
			{
				// Choose file to save to
				fileChooser = new JFileChooser() ;
				iOpt = fileChooser.showSaveDialog( this.frame ) ;
				this.file = fileChooser.getSelectedFile() ;

				// Write to file
				if( iOpt == JFileChooser.APPROVE_OPTION )
				{
					this.writeFile( file , this.editorArea.getText() ) ;
					this.consoleArea.append( "\nFile saved: " + file.getPath() + " :: ok \n" ) ;
					// Set window title
					this.frame.setTitle( this.file.getName() ) ; 
				}
				else
				{
					this.consoleArea.append( "\nFile save operation canceled :: ok \n" ) ;
				}
			}
			else
			{
				// Write to already opened file
				this.writeFile( this.file , this.editorArea.getText() ) ;
				this.consoleArea.append( "\nFile saved: " + file.getPath() + "\n" ) ;	
				// Set window title
				this.frame.setTitle( this.file.getName() ) ; 			
			}
			break ;

		case "Save as" : 
			// Choose file to save to 
			fileChooser = new JFileChooser() ;
			iOpt = fileChooser.showSaveDialog( this.frame ) ;
			this.file = fileChooser.getSelectedFile() ;
			// Write to file
			if( iOpt == JFileChooser.APPROVE_OPTION )
			{
				// Set window title
				this.frame.setTitle( this.file.getName() ) ; 
				this.writeFile( this.file , this.editorArea.getText() ) ;
				this.consoleArea.append( "\nFile saved: " + this.file.getPath() + " :: ok \n" ) ;
			}
			else
			{
				this.consoleArea.append( "\nFile save operation canceled :: ok \n" ) ;
			}
			break ; 

		case "Copy" : 
			this.editorArea.copy() ; 
			break ;

		case "Paste" : 
			this.editorArea.paste() ; 
			break ; 

		case "Cut" : 
			this.editorArea.cut() ; 
			break ;

		case "Find" :
			// Find query in text
			try
			{
				// Get user input and find in text
				if ( this.find( JOptionPane.showInputDialog( null , "Find:" ) ) ) 
				{
					// Log
					System.err.println( "Query found " ) ;
				}
				else
				{
					// Log
					System.err.println( "Query not found " ) ;
				}
			}
			// Catch error from cancellation
			catch( final Exception f )
			{
				f.printStackTrace() ; 
				System.err.println( "Operation cancelled " ) ;
			}
			break ; 

		case "Autocomplete" :
			// Get words from selection
			if ( this.editorArea.getSelectedText() != null )
			{
				// Get suggestions from autocompleter
				szSuggestions = this.autocompleter
						.getWordsFromStub( this.editorArea.getSelectedText()
								// Added after testing. See T.2.2
								.toLowerCase() ) ;

				// If word(s) found
				if ( szSuggestions != null )
				{
					// Add all suggestions to message to print 
					for ( i = 0 ; i < szSuggestions.length ; i ++ )
					{
						szOptions = szOptions + "[" + i + "] :: " 
								+ szSuggestions[i] + "\n" ; 
					}

					// If single entry, do not show choice dialogue
					if ( szSuggestions.length == 1 )
					{
						this.editorArea.replaceSelection( szSuggestions[0] ) ;
					}
					// If multiple entries, show menu to choose completion
					else
					{
						// Show dialogue
						szInput = JOptionPane.showInputDialog( null 
								, szOptions + "\nEnter choice: " ) ;

						// Validate
						if ( szInput.trim().matches( "^[0-9]{1,}$" ) ) 
						{
							iOpt = Integer.parseInt( szInput.trim() ) ;

							if ( iOpt < szSuggestions.length )
							{
								// Replace prefix with suggestion
								this.editorArea.replaceSelection( szSuggestions[ iOpt ] ) ;
							}
							else
							{
								System.err.println( "Index not in range. Aborting... " ) ;
							}
						}
						else
						{
							System.err.println( "Invalid entry. Aborting... " ) ;
						}
					}
				}
				else
				{
					System.out.println( "No results found" ) ;
				}
			}

			break ;

		case "Highlight" : 
			if ( this.highlight( this.editorArea ) ) 
			{
				System.err.println( "Invalid syntax" ) ;
			}
			else
			{
				System.out.println( "Done" ) ;				
			}
			break ;

		case "Run" :
			this.exec( this.interpreter , this.editorArea ) ;
			break ; 

		case "Debug" : 
			this.exec( this.debugger , this.editorArea ) ;
			break ;

		default : 
			break ;
		}
		return ; 
	}

	/** Creates GUI */
	public static void main( final String[] args ) 
	{
		try
		{
		UIManager.setLookAndFeel(
		    UIManager.getCrossPlatformLookAndFeelClassName());
		}
		catch( Exception e)
		{}
		// If no arguments given, create new GUI with no file
		if ( args.length == 0 )
		{
			new GUI() ;
		}
		// If arguments given, open files
		else
		{
			// For each argument
			for ( int i = 0 ; i < args.length ; i ++ ) 
			{
				// Create new GUI with file
				new GUI( new File( args[i] ) ) ;
			}
		}
	}
}
