/**
 * @version 3 2022-10-14
 *
 * A linked list implementation 
 *
 */

public class LinkedList <T> // T is the Object type of each Item
{
	/** The first item in the list */
	private Item root ;
	/** The length of the list */
	private int iLength ;

	/** Constructor */ 
	public LinkedList()
	{
		this.reset() ;
		return ; 
	}
	/** Constructor setting the root node content */
	public LinkedList( final T rootContent )
	{
		this() ; 
		this.add( rootContent ) ;
		return ; 
	}
	/** Constructor adding an indefinite number of nodes */
	@SafeVarargs
	public LinkedList( final T... content )
	{
		this() ;
		this.add( content ) ;
		return ; 
	}
	/** Set class members to defaults */
	public void reset()
	{
		this.setRoot( null ) ;
		this.setLength( 0 ) ;
		return ;
	}

	/** An item in the list */
	private final class Item
	{
		private T content = null ;
		private Item next = null ;

		public Item( final T content )
		{
			this.setContent( content ) ;
			return ;
		}
		public Item add( final T content )
		{
			this.setNext( new Item( content ) ) ;
			return this.getNext() ;
		}

		// Setters and getters
		public void setContent( final T content )
		{
			this.content = content ;
			return ;
		}
		public void setNext( final Item next )
		{
			this.next = next ;
			return ; 
		}
		public T getContent() { return this.content ; }
		public Item getNext() { return this.next ; } 
	}

	/** Returns the last added item of the list, without removing */
	public T peek()
	{
		return this.getLast() ;
	}
	/** Returns the item at index i */
	public T peek( int i )
	{
		Item ret = this.getRoot() ; 

		for ( ; i > 0 ; i -- )
		{
			ret = ret.getNext() ; 
		}

		return ret.getContent() ;
	}
	/** Returns the last object of the list */
	private T getLast()
	{
		return this.getLastItem().getContent() ;
	}
	/** Returns the last item of the list */
	private Item getLastItem()
	{
		Item last = this.getRoot() ;

		while ( last.getNext() != null )
		{
			last = last.getNext(); 
		}
		return last ; 
	}

	/** Adds objects to the list */
	@SafeVarargs
	public final void add( final T... content )
	{
		int i ;
		for ( i = 0 ; i < content.length ; i ++ )
		{
			this.insert( this , content[i] ) ;
			// Increment length
			this.incLength() ;
		}
		return ;
	}
	/** Appends a list */
	public void append( final LinkedList<T> content )
	{
		// If not null, add each item of the passed list
		//  to this list
		if ( content != null )
		{
			// For each item in the passed list
			for ( int i = 0 ; i < content.getLength() ; i ++ )
			{
				// Add to this array
				this.insert( this , content.peek( i ) ) ;
				// Increment length
				this.incLength() ; 
			}
		}
		return ;
	}
	/** Adds an object to the list. Alias for add */
	public void push( final T content )
	{
		this.add( content ) ; 
		return ;
	}
	/** Inserts an object to the list */
	private void insert( final LinkedList<T> list , final T content )
	{
		// If no root, add root
		if ( list.getRoot() == null )
		{
			list.setRoot( new Item( content ) ) ;
		}
		// If there is root, insert item at end of list
		else
		{
			list.getLastItem().add( content ) ;
		}
		return ;
	}

	/** Removes and returns the last added item from the list */
	public T pop()
	{
		final Item item = this.getLastItem() ;

		// Decrement length
		this.decLength() ; 
		// Trim list to length
		this.trim() ; 

		return item.getContent() ;
	}
	/** Trims to length, by replacing current root with new root of a new list */
	public void trim()
	{
		final LinkedList<T> newList = new LinkedList<T>() ;
		Item current = this.getRoot() ;

		// Until length is reached
		for ( int i = 0 ; i < this.getLength() ; i++ )
		{
			// Add to new list
			newList.add( current.getContent() ) ;
			// Next item
			current = current.getNext() ; 
		}
		// Set node
		this.setRoot( newList.getRoot() ) ; 
		return ;
	}

	/** Converts to an array
	 * @return a T[] */
	public Object[] toArray()
	{
		Item indexed = this.getRoot() ; 
		final Object[] ret = new Object[ this.getLength() ] ;

		int i = 0 ;

		// Repeat until no more items in list 
		while ( indexed != null && i < this.getLength() )
		{
			ret[ i ] = indexed.getContent() ; 
			indexed = indexed.getNext() ; 
			i ++ ; 
		}
		return ret ;
	}
	public String[] toStringArray()
	{
		Object[] obj = this.toArray() ;
		String[] ret = new String[ obj.length ] ;
		for ( int i = 0 ; i < obj.length ; i ++ )
		{
			ret[i] = ( String ) obj[ i ] ;
		}
		return ret ;
	}
	/** Displays items */
	public void display()
	{
		Item indexed = this.getRoot() ; 

		System.out.println( this.getClass().getName() + " :: display() " );

		// Repeat until no more items in list 
		while ( indexed != null )
		{
			System.out.print( "\'" + indexed.content + "\', " ) ;
			indexed = indexed.getNext() ; 
		}
		System.out.println( "length: " + this.getLength() ) ;
	}

	/** Set the number of items in the list */ 
	private void setLength( final int iLength ) 
	{ 
		this.iLength = iLength ;
		return ; 
	}
	/** @return the number of Items in the list */
	public int getLength() { return this.iLength ; }
	/** @return the root Item */
	public Item getRoot() { return this.root ; }
	/** Sets the root Item to the passed parameter */
	public void setRoot( final Item item ) 
	{
		this.root = item ; 
		return ;
	}

	/** Increases the length of the list by 1
	 * @return the new length of the list */
	private int incLength()
	{
		this.setLength( this.getLength() + 1 ) ;
		return this.getLength() ; 
	}
	/** Decreases the length of the list by 1  
	 * @return the new length of the list */
	private int decLength()
	{
		this.setLength( this.getLength() - 1 ) ;
		return this.getLength() ; 
	}

	/** For testing purposes */
	public static void main( final String[] args ) 
	{
		final LinkedList<String> list = new LinkedList<String>() ;

		list.display() ; 
		System.out.println( "\n-- Add x4" ) ;
		list.add( "Hello" ) ;
		list.add( "World" ) ;
		list.add( "!" ) ;
		list.add( "?" ) ;
		list.display() ; 

		System.out.println( "\n-- Peek x2" );
		System.out.println( list.peek() ) ;
		System.out.println( list.peek() ) ;
		list.display() ;

		System.out.println( "\n-- To (String)Array " ) ;
		final Object[] arr = list.toArray() ;
		for ( int i = 0 ; i < arr.length ; i ++ )
		{
			System.out.println( arr[i] );
		}

		System.out.println("\n-- Peek( i ) ") ;
		for ( int i = 0 ; i < list.getLength() ; i ++ )
		{
			System.out.println( list.peek( i ) ) ;
		}

		System.out.println( "\n-- Pop x4" ) ;
		System.out.println( list.pop() ) ;
		System.out.println( list.pop() ) ;
		System.out.println( list.pop() ) ;
		System.out.println( list.pop() ) ;
		list.display() ;
		
		list.add( "x" , "y" , "z" ) ;
		list.append( new LinkedList<String>( "a" , "b" , "c" , "d" ) ) ;
		list.display() ; 
	}

}
