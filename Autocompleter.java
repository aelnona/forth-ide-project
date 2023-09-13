/**
 * @version 5 2022-11-05
 * 
 * A class with a trie and functions used for autocompletion  
 *
 */

/*
 * Downstream refers to descendant nodes 
 * Upstream refers to ancestor nodes
 */

import java.util.Enumeration;
import java.util.Hashtable;

public class Autocompleter 
{
	private Node root ;

	/** Constructor. Creates an empty trie */
	public Autocompleter()
	{
		// Initialised with no Character, as each branch will be a child of the root
		this.root = new Node() ; 
		return ; 
	}

	/** Node for trie */
	private final static class Node
	{
		private boolean bIsWord ;  // Stores true if represents last character of a word, else false
		private char chContent ; // Stores content
		// This is a Hashtable in order to be able to easily get a Node
		// based on its content. The Character in the Hashtable is the same
		// as chContent of the child Node, referenced by the Node of the 
		// Hashtable. It also prevents duplicate keys, so the same character
		// cannot be added twice. 
		// A HashMap could also be used, but would allow null keys and values, 
		// which would not make sense here. 
		private Hashtable<Character , Node> children ; // References children 
		private Node parent ; // References parent

		/** Constructor. Creates an empty node with no children */
		public Node() 
		{
			this.setChildren( new Hashtable<Character , Node>() );
			return ;
		}
		/** Constructor. Creates a node with content */
		public Node( final char chContent )
		{
			this() ;
			this.setContent( chContent ) ;
			return ; 
		}

		/** Sets if word */
		public void setIsWord( final boolean bIsWord )
		{
			this.bIsWord = bIsWord ; 
			return ;
		}
		/** Sets content of node */
		public void setContent( final char chContent )
		{
			this.chContent = chContent ;
			return ; 
		}
		/** Sets node children */
		public void setChildren( final Hashtable<Character , Node> children )
		{
			this.children = children ; 
			return ;
		}
		/** Sets node parent */
		public void setParent( final Node parent )
		{
			this.parent = parent ;
			return ; 
		}
		// Getters
		public boolean getIsword() { return this.bIsWord ; }
		public char getContent() { return this.chContent ; } 
		public Hashtable<Character , Node> getChildren() { return this.children ; }
		public Node getParent() { return this.parent ; }
	}

	/** Inserts a string into the trie */
	public void insert( final String szValue )
	{
		char chCurrentChar ; 
		int i ;
		Node current = this.root ; 
		Node temp = null ; 

		// If not null or empty 
		if ( szValue != null )
		{
			if ( szValue.length() > 0 )
			{
				// For each character in input string 
				for ( i = 0 ; i < szValue.length() ; i ++ )
				{
					// Set current character
					chCurrentChar = szValue.charAt( i ) ;

					// If exists in children, go to node of character
					if ( current.getChildren().containsKey( chCurrentChar ) )
					{
						// Consume child holding character
						current = current.getChildren().get( chCurrentChar ) ; 
					}
					// If does not exist in children, create new child 
					else
					{
						temp = current ; 
						current = new Node( chCurrentChar ) ;
						current.setParent( temp ) ; 
						// Add to trie
						temp.getChildren().put( chCurrentChar , current ) ;
					}
				}
				current.setIsWord( true ) ;
			}
		}
		return ;  
	}

	/** Prints the trie */
	public void display() 
	{
		System.out.println( "Autocompleter :: display(): " ) ;
		this.printChildren( root , 0 ) ;
		return ; 
	}
	/** Recurse through trie and print */ 
	private void printChildren( final Node node , final int iLevel )
	{
		int i = 0 ;
		Node current ; 
		// Holds all children of current node
		final Enumeration<Node> children = node.getChildren().elements() ;

		// For each child 
		while ( children.hasMoreElements() )
		{
			// Print word
			current = children.nextElement() ;

			// Print indentation
			for ( i = 0 ; i < iLevel ; i ++ ) 
			{
				System.out.print( "  " ) ;
			}

			// Print content
			System.out.println( current.getContent() ) ;

			// If not leaf, print children 
			if ( current.getChildren().size() != 0 )
			{
				// Recurse
				this.printChildren( current , iLevel + 1 ) ;
			}
		}
		return ;
	}

	// quickSort and checkStringLesser were added after design 
	// in order to sort the results of the autocomplete operation
	// Quicksort is used due to its efficiency. As there are not likely to
	// be that many items to sort, merge sort is not used. 
	// It may be possible (but not likely) that there are many items to 
	// sort however, so insertion and bubble sort are not used, as these 
	// would be slow and inefficient for longer lists. 
	/** Performs a quicksort on the words */
	public LinkedList<String> quickSort( final LinkedList<String> inputList )
	{
		final LinkedList<String> ret = new LinkedList<String>() ; 
		final LinkedList<String> smaller = new LinkedList<String>() ;
		final LinkedList<String> greater = new LinkedList<String>() ;
		final String pivot ;

		// If items left, recursively partition until no items left
		if ( inputList.getLength() > 0 ) 
		{
			// Partition
			// Get pivot
			pivot = inputList.pop() ;
			// For each item in list
			for ( int i = 0 ; i < inputList.getLength() ; i ++ )
			{
				// If item at index i is lesser than pivot, add to smaller
				if ( this.checkStringLesser( inputList.peek(i) , pivot ) ) 
				{
					smaller.add( inputList.peek(i) ) ;
				}
				// If greater or equal, add to greater
				else
				{
					greater.add( inputList.peek(i) ) ;
				}
			}
			// Recurse and add to return list 
			ret.append( this.quickSort( smaller ) ) ;
			ret.add( pivot ) ;
			ret.append( this.quickSort( greater ) ) ;
		}
		return ret ;
	}
	/** Checks if a string is alphabetically smaller than another */
	private boolean checkStringLesser( final String szA , final String szB )
	{
		boolean bRC = false ;
		if ( szA.equals( "" ) || szA.equals( szB ) ) 
		{
			bRC = true ;
		} 
		else if ( szB.equals( "" ) )
		{
			bRC = false ;
		}
		// If same character
		else if ( szA.charAt( 0 ) == szB.charAt( 0 ) )
		{
			if ( szA.length() > 1 && szB.length() > 1 )
			{
				// Recurse on the rest of the strings
				bRC = this.checkStringLesser( 
						szA.substring( 1 ) , szB.substring( 1 ) ) ;
			}
			else if ( szA.length() < szB.length() )
			{
				bRC = true ;
			}
		}
		// Else check lesser
		else if ( szA.charAt( 0 ) < szB.charAt( 0 ) )
		{
			bRC = true ; 
		}
		return bRC ; 
	}

	/** Returns an array of strings */
	public String[] getWordsFromStub( final String szStub )
	{
		// If not found
		if ( this.findStubNode( this.root , szStub , 0 ) == null ) 
		{
			// Return null
			return null ;
		}
		// If found
		else
		{
			// Get array of words
			return ( this.quickSort( this.getWords( 
					this.findStubNode( this.root , szStub , 0 ) 
					, new LinkedList<String>() ) ).toStringArray() ) ;
		}
	}

	/** Gets the words downstream of a node 
	 * @param node is the Node to get the words downstream of 
	 * @param ll is the LinkedList to add words to. Typically will 
	 * be passed as new LinkedList<String>() unless a recursive call
	 * by this function. */
	private LinkedList<String> getWords( final Node node , final LinkedList<String> ll )
	{
		Node current ; 
		final Enumeration<Node> children = node.getChildren().elements() ;

		// If not leaf
		if ( node.getChildren().size() > 0 )
		{
			// For each child 
			while ( children.hasMoreElements() )
			{
				// Print word
				current = children.nextElement() ;

				// If not word, get children 
				if ( current.getChildren().size() > 0 )
				{
					this.getWords( current , ll ) ;
				}
				// If word, add to list
				if ( current.getIsword() ) 
				{
					ll.add( this.getWordFromLeaf( current ).trim() ) ; 
				}
			}
		}
		// If leaf, add to list
		else
		{
			ll.add( this.getWordsFromStub( "" + node.getContent() ) ) ;
		}
		return ll ; 
	}

	/** Returns the word represented by a branch */
	private String getWordFromLeaf( Node leaf )
	{
		String szRet = "" ;

		// Traverse upstream and append
		while ( leaf.getParent() != null )
		{
			szRet = leaf.getContent() + szRet ; 
			// Ascend to the ancestors 
			leaf = leaf.getParent() ; 
		}
		// Append first character 
		szRet = leaf.getContent() + szRet ; 

		return szRet ; 
	}

	/** Find the node representing the given prefix, or return null if not found */
	private Node findStubNode( final Node node , final String szStub , final int iIndex )
	{
		Node current = null ; 
		Node ret = null ;
		final Enumeration<Node> children = node.getChildren().elements() ;

		// For each child 
		while ( children.hasMoreElements() )
		{
			current = children.nextElement() ;

			// If same as stub
			if ( current.getContent() == szStub.charAt( iIndex ) )
			{
				ret = current ; 
				// If not leaf, check children
				if ( current.getChildren().size() != 0 && iIndex < szStub.length() - 1 )
				{
					// Recurse
					ret = findStubNode( current , szStub , iIndex + 1 ) ;
				}
			}
		}
		return ret ; 
	}

	/** Returns number of leaf nodes */ 
	public int getLength() { return this.getLength( this.root ) ; }

	/** Returns number of leaf nodes downstream of a node */
	private int getLength( final Node node ) 
	{
		int iLen = 0 ;
		Node current ; 
		// Enumeration usage ( .hasMoreElements(), .nextElement() ) from 
		// www.java2novice.com/java-collections-and-util/hashtable/enumeration/
		final Enumeration<Node> children = node.getChildren().elements() ;

		// For each child, if leaf, increment length
		// if not leaf, recurse on node, adding result to length
		while ( children.hasMoreElements() )
		{
			current = children.nextElement() ;

			// If not leaf, add more 
			if ( current.getChildren().size() != 0 )
			{
				// Recurse
				iLen = iLen + this.getLength( current ) ;
			}
			else
			{
				// Increment length if leaf node
				++ iLen ; 
			}
		}

		return iLen ; 
	}

	// Testing
	public static void main( String[] args ) 
	{
		final Autocompleter at = new Autocompleter() ;

		at.insert( "HELLO" ) ;
		at.insert( "HELP" ) ;
		at.insert( "HERMIT" ) ;
		at.insert( "GOODBYE" ) ;
		at.display();

		String[] autoc = at.getWordsFromStub( "HEL" ) ;
		for ( int i = 0 ; i < autoc.length ; i ++ )
		{
			System.out.println( i + " : " + autoc[i] ) ;
		}
	}
}
