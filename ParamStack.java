/**
 * @version 5 2022-11-01
 * 
 * Parameter Stack
 *
 */

public class ParamStack 
{
	private boolean bFull = false ;
	private boolean bEmpty = true ;
	private int iMaxSize = 128 ;
	private int iSize = 0 ; 
	private int[] iItems = null ; 

	/** Constructor. Creates an integer stack of max size 128 */
	public ParamStack()
	{
		this.reset() ; 
		return ; 
	}
	/** Constructor. 
	 * @param iMaxSize : the maximum number of items in the stack */
	public ParamStack( final int iMaxSize )
	{
		this.iMaxSize = iMaxSize ; 
		this.reset() ;
		return ; 
	}
	/** Empties stack and sets max size */
	public void reset()
	{
		this.iItems = new int[ this.iMaxSize ] ; 
		this.iSize = 0 ;
		this.bFull = false ; 
		this.bEmpty = true ; 
		return ; 
	}

	/** Adds an item to the top of the stack 
	 * @param iValue : the value to add to the stack
	 * @return false if successful, true otherwise */
	public boolean push( final int iValue )
	{
		boolean bRC = false ;

		if ( bFull == true ) 
		{
			bRC = true ; 
			System.err.println( "Error, stack full " ) ;
		}
		else
		{
			this.iItems[ this.iSize ] = iValue ;
			// Move to next item
			++ this.iSize ; 
		}
		this.checkFullOrEmpty() ;
		return bRC ;
	}
	/** Alias for push */
	public boolean add( final int iValue )
	{
		return this.push ( iValue ) ;
	}
	/** Returns and removes the top item of the stack */
	public int pop()
	{
		int iRet = 0 ;
		if ( this.getEmpty() == false )
		{
			iRet = this.iItems[ -- this.iSize ] ;
		}
		else
		{
			System.err.println( "Error, stack empty " ) ;
		}
		this.checkFullOrEmpty() ; 
		return iRet ; 
	}
	public int peek()
	{
		this.checkFullOrEmpty() ;
		if ( this.getEmpty() == false )
		{
			return this.iItems[ this.iSize - 1 ] ;
		}
		else
		{
			System.err.println( "Error, stack empty " ) ;
			return 0 ; 
		}
	}
	public int peek( final int i )
	{
		this.checkFullOrEmpty() ;
		// If not empty, and i is >= 0
		if ( ( this.getEmpty() == false ) && ( i > -1 ) )
		{
			return this.iItems[ i ] ;
		}
		else
		{
			System.err.println( "Error, stack empty " ) ;
			return 0 ; 
		}
	}
	
	private void checkFullOrEmpty()
	{
		this.bFull = false ; 
		this.bEmpty = false ;
		if ( this.getSize() == 0 ) 
		{
			this.bEmpty = true ; 
		}
		else if ( this.getSize() >= this.iMaxSize - 1 )
		{
			this.bFull = true ;
		}
		return ;
	}
	
	/** Change the size of the stack. If reducing size, 
	 * some values may be lost. */
	public void setMaxSize( final int iMaxSize )
	{
		final int[] iTemp ;
		this.iMaxSize = iMaxSize ;
		iTemp = new int[ iMaxSize ] ;
		
		// If making smaller, print warning and move cursor 
		if ( this.iItems.length > iMaxSize )
		{
			System.err.println( "Warning: attempting to downsize stack."
					+ " Some values may be lost." ) ;
			// Move cursor to last element
			this.iSize = iMaxSize - 1 ; 
		}
		// Copy array into new size array
		for ( int i = 0 ; i < iMaxSize && i < this.iItems.length ; i ++ )
		{
			iTemp[i] = this.iItems[i] ; 
		}
		this.iItems = iTemp ; 
		return ; 
	}

	/** @return true if full, false otherwise */
	public boolean getFull() { return this.bFull ; }
	/** @return true if empty, false otherwise */ 
	public boolean getEmpty() { return this.bEmpty ; } 
	/** @return number of items in the stack */
	public int getSize() { return this.iSize ; } 
	/** @return max size */
	public int getMaxSize() { return this.iMaxSize ; } 

	public static void main( final String[] args ) 
	{
		final ParamStack stack = new ParamStack( 4 ) ;
		System.out.println( stack.push( 10 ) ) ;
		System.out.println( stack.peek() ) ; 
		System.out.println( stack.pop() ) ; 
		System.out.println( stack.pop() ) ; 
		System.out.println( stack.push( 10 ) ) ;
		System.out.println( stack.push( 20 ) ) ;
		System.out.println( stack.push( 30 ) ) ;
		System.out.println( stack.pop() ) ; 
		System.out.println( stack.pop() ) ; 
		System.out.println( stack.push( 20 ) ) ;
		System.out.println( stack.push( 30 ) ) ;
		System.out.println( stack.push( 40 ) ) ;		
	}
}
