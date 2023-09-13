/**
 * @version 5 2022-10-28
 *	
 */

import java.io.File ;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

@SuppressWarnings("serial")
public class FileTreePane extends JEditorPane implements HyperlinkListener
{
	private Node root ; // The root directory containing the currently opened file
	// Required to set open file in GUI when link is clicked. Set in GUI 
	private GUI gui ; // The GUI that the file tree is in. 
	
	public static final int MAX_LEVELS = 3 ; // Max number of levels to expand 

	/** Create new file tree pane */
	public FileTreePane()
	{
		super() ;
		this.reset() ; 
		return ; 
	}
	/** Sets to defaults */
	private void reset()
	{
		// Set to HTML to allow hyperlinks
		this.setContentType( "text/html" ) ;
		// Title
		this.setText( "\n( Files )\n" ) ;
		// Look
		this.setFont( GUI.FONT ) ; 
		this.setBackground( GUI.GREY ) ;
		// Must not be editable to allow hyperlinks
		this.setEditable( false ) ;	
		this.putClientProperty(
				JEditorPane.HONOR_DISPLAY_PROPERTIES , true ) ;

		// Hyperlinks used to open files 
		this.addHyperlinkListener( this ) ;
		return ; 
	}

	/** A node in the tree */
	private static final class Node
	{
		// Node content
		private File file ;
		// Child nodes
		private Node[] children ; 

		/** Create node containing file */
		public Node( final File file ) 
		{
			this.reset() ;
			this.setFile( file ) ; 
			return ; 
		}
		/** Set class members to null */
		public void reset()
		{
			this.setFile( null ) ;
			this.setChildren( null ) ;
			return ; 
		}

		// Setters and getters
		public void setChildren( final Node[] children ) 
		{
			this.children = children ; 
			return ; 
		}
		public void setFile( final File file )
		{ 
			this.file = file ; 
			return ; 
		}
		public Node[] getChildren() { return this.children ; } 
		public File getFile() { return this.file ; } 
	}

	/** Creates a tree and prints to editor pane */
	public void generateTree( final File rootDirectory ) 
	{
		this.setRoot( new Node( rootDirectory ) ) ; 
		this.initNode( this.getRoot() ) ;
		this.display( this.getRoot() , 1 ) ;
		return ; 
	}
	/** Recursively generates children for a node */
	public Node initNode( final Node node )
	{
		final File[] childFiles = node.getFile().listFiles() ; 
		node.setChildren( new Node[ childFiles.length ] ) ;

		// Create nodes from files
		for ( int i = 0 ; i < node.getChildren().length ; i ++ )
		{
			node.getChildren()[i] = new Node( childFiles[i] ) ; 
			// Add children 
			if ( node.getChildren()[i].getFile().isDirectory() )
			{
				node.getChildren()[i] = this.initNode( node.getChildren()[i] ) ;
			}
		}

		return node ; 
	}
	/** Sets the root directory */
	public void setRootDirectory( final File root )
	{
		this.setRoot( new Node( root ) ) ;
		return ; 
	}
	/** Returns the root directory */
	public File getRootDirectory() { return this.getRoot().getFile() ; }

	/** Sets text to navigable file tree */
	public void display( final Node node , final int iLevel )
	{
		// Link to parent directory
		final String szUp = String.format( "<a style=\"color:black\" "
				+ "href=\"%s\">../</a><br>" 
				, node.getFile().getParent() ) ;
		// Build tree and display
		this.setText( szUp + this.buildTextToDisplay( node , iLevel ) ) ;
		return ; 
	}
	private String buildTextToDisplay( final Node node , final int iLevel )
	{
		String sz = String.format( "<a style=\"color:black\" "
				+ "href=\"%s\">%s</a>" 
				, node.getFile().getAbsoluteFile() 
				, node.getFile().getName() ) ;

		if ( node.getFile().isDirectory() ) 
		{
			sz = sz + "/" ; 
		}
		if ( node.getChildren() != null && node.getChildren().length > 0 )
		{
			for ( int i = 0 ; i < node.getChildren().length ; i ++ )
			{
				if ( node.getFile().isDirectory() 
						&& iLevel < MAX_LEVELS )
				{
					sz = sz + "<br>" ;
					// this.append( String.format( ( "%-" + iLevel * 2 + "s" ) , " " ) ) ;
					for ( int j = 1 ; j < iLevel * 2 + 1 ; j ++ )
					{
						// Insert non-breaking space
						sz = sz + "\u00A0" ; 
					}
					// // Unicode for a L shape. 
					// sz = sz + "â””â”€â”€â”€" ;
					// +-- used instead in order to prevent mojibake or font problems
					sz = sz + "+--" ;
					sz = sz + buildTextToDisplay( node.getChildren()[ i ] 
							, iLevel + 1 ) ;
				}
			}
		}

		return sz ;		
	}

	@Override
	/** Opens clicked file */ 
	public void hyperlinkUpdate( final HyperlinkEvent e ) 
	{
		final File file ; 
		// If clicked
		if ( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
		{
			// Open file
			file = new File( e.getDescription() ) ;
			// If directory, generate new tree and move into directory
			if ( file.isDirectory() )
			{
				this.reset() ; 
				this.generateTree( file ) ;
				this.display( this.getRoot() , 0 ) ;
			}
			else
			{
				this.getGUI().setEditorToFile( file ) ;
			}
		}
		return ;
	}

	// Setters and Getters
	public void setGUI( final GUI gui ) 
	{
		this.gui = gui ; 
		return ;
	}
	public void setRoot( final Node root )
	{
		this.root = root ; 
		return ;
	}
	public GUI getGUI() { return this.gui ; }
	public Node getRoot() { return this.root ; }
}
