 /**
  * @version 5 2022-11-05
  *
  * A structure used for a Forth word. Stores the word's current and previous definitions. 
  * Does not store the word's name, as this is stored by the Dictionary
  *
  */

public class Word 
{
	/** The current definition */
	private String szDefinition ;
	/** The history of definitions */ 
	private LinkedList<String> defHistory ;
	
	/** Constructors */
	// szFunction renamed to szDefinition
	public Word( final String szDefinition )
	{
		this.reset() ; 
		this.assignDefinition( szDefinition ) ;
		return ;
	}
	/** Sets class variables to default values */
	public void reset()
	{
		this.setDefinition( "" ) ;
		this.defHistory = new LinkedList<String>() ;
		return ; 
	}
	
	/** Public method to add new function */
	public void assignDefinition( final String szDefinition )
	{
		// Add to history
		this.defHistory.add( szDefinition ) ;
		// Set current definition
		this.setDefinition( szDefinition ) ;
		return ; 
	}
	/** Setter for szDefinition */
	public void setDefinition( final String szDefinition )
	{
		// Set szDefinition
		this.szDefinition = szDefinition ;
		return ; 
	}
	/** Removes last added function from history */
	public String removeLastDefinition()
	{
		// Removes last added definition
		this.defHistory.pop() ;
		// If history exists, use last definition
		if ( this.defHistory.getLength() > 0 )
		{
			this.setDefinition( (String) this.defHistory.peek() ) ;
		}
		// If history does not exist, use empty string
		else
		{
			this.setDefinition( "" ) ;
		}
		return this.szDefinition ;
	}
	
	/** Getter for definition */
	public String getDefinition() { return this.szDefinition ; } 
	
	/** Getter for history */
	public LinkedList<String> getHistory() { return this.defHistory ; } 
}
