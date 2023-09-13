/** 
 * @version 5 2022-11-05
 * 
 * Abstract Syntax Tree
 * 
 */
public class AST
{
	private Expression root ; 
	public AST()
	{
		this.reset() ;
		return ;
	}
	public void reset() 
	{
		this.root = new Expression( new LinkedList<Expression>() ) ;
		return ;
	}
	public static final class Expression
	{
		// Final fields do not need setters
		// Child expressions
		// The order of the list is very important, as the tree must be 
		// traversed from left to right, so Hashtables and HashMaps cannot 
		// be used. A LinkedList is used as nodes are easy to add to the end
		// of the list, and it is simple to traverse the list in order.
		private final LinkedList<Expression> children ; 
		// Leaf nodes have no children, but must have content
		private final Token content ; 

		/** Constructor for a leaf node */
		public Expression( final Token content )
		{
			this.children = null ; 
			this.content = content ; 
			return ;
		}
		/** 
		 * Constructor for a non-leaf node 
		 * @param expression : sets children to expression 
		 */
		public Expression( final LinkedList<Expression> expression )
		{
			this.children = expression ;
			this.content = null ;
			return ;
		}
		/** 
		 * Constructor for a non-leaf node 
		 * @param expression : varargs children
		 */
		public Expression( final Expression... expression )
		{
			this.children = new LinkedList<Expression>( expression ) ;
			this.content = null ;
			return ;
		}

		// Getters
		public LinkedList<Expression> getChildren() 
		{
			return this.children ; 
		} 
		public Token getContent() { return this.content ; } 
	}

	/** Pretty-printer */
	// Only used for testing
	public void display( final int iLevel )
	{
		// Build tree and display
		System.out.println( this.buildTextToDisplay(
				this.getRoot() , iLevel ) ) ;
		// displayNode( node ) ; 
		return ; 
	}
	private String buildTextToDisplay( final AST.Expression node 
			, final int iLevel )
	{
		String sz = "" ; 
		if ( node.getContent() != null )
		{
			sz = String.format( "%s :: %s" 
					, node.getContent().getString()
					, node.getContent().getType() ) ;
		}
		else
		{
			sz = "Expr" ;
		}

		if ( node.getChildren() != null 
				&& node.getChildren().getLength() > 0 )
		{
			for ( int i = 0 ; i < node.getChildren().getLength() ; i ++ )
			{
				sz = sz + "\n" ;
				for ( int j = 0 ; j < iLevel * 3 ; j ++ )
				{
					// sz = sz + "\u00A0 " ;
					// If under +
					if ( j % 3 == 0 )
					{
						sz = sz + "." ;
					}
					sz = sz + " " ;
				}
				sz = sz + "+-- " ;
				sz = sz + this.buildTextToDisplay( ( ( AST.Expression ) 
						node.getChildren().toArray()[ i ] ) , iLevel + 1 ) ;
			}
		}

		return sz ;		
	}

	public void setRoot( final Expression root ) 
	{
		this.root = root ; 
		return ; 
	}
	public Expression getRoot() { return this.root ; } 
}
