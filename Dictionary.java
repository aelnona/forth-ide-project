/**
 * @version 5 2022-11-05
 *
 * An object that stores words and their definitions
 *
 */

import java.util.Hashtable;

public class Dictionary
{
	// These are Hashtables to allow fast and efficient adding or getting/removing 
	// of Words/Variabes, and to prevent duplicate keys, so that Words and Variables 
	// cannot be defined twice. 
	private Hashtable<String , Word> words ;
	private Hashtable<String , String> variables ;
	// The autocompleter. Words are added to the autocompleter when they are defined.
	private Autocompleter autocompleter ;

	// Constructor
	public Dictionary()
	{
		this.words = new Hashtable<>() ;
		this.variables = new Hashtable<>() ;
		this.autocompleter = new Autocompleter() ; 
		this.reset() ;
		return ;
	}
	private void reset()
	{
		// Clear words and variables. 
		this.getWords().clear() ;
		this.getVariables().clear() ;
		// Do not create new Autocompleter
		return ;
	}

	/** Adds a word with its definition to the dictionary, and adds to autocompleter */
	public void addWord( final String szName , final String szDefinition )
	{
		// Add new word if undefined
		if ( this.checkWordDefined( szName ) == false )
		{
			this.getWords().put( szName.trim().toLowerCase()
					, new Word( szDefinition.trim() ) ) ;
			// Add to autocompleter
			this.autocompleter.insert( szName ) ;
		}
		// If already defined, add new definition
		else if ( szName.matches( "\\s*" ) == false )
		{
			this.getWords().get( szName.toLowerCase() )
			.assignDefinition( szDefinition.trim() ) ;
		}
		return ;
	}
	/** Reverts a word in the dictionary to a previous definition,
	 *  or removes the word */
	public void undefineWord( final String szName )
	{
		this.undefineWord( szName , this.getWord( szName ) ) ;
		return ;
	}
	/** Reverts a word in the dictionary to a previous definition,
	 * or removes the word */
	private void undefineWord( final String szName , final Word word )
	{
		// Replace with previous version
		word.removeLastDefinition() ;

		// Remove from dictionary if undefined
		if ( word.getHistory().getLength() == 0 )
		{
			this.getWords().remove( szName.toLowerCase() ) ;
		}
		return ;
	}

	/** Adds a variable to the dictionary, and adds to autocompleter */
	public void addVariable( final String szKey , final String szValue )
	{
		this.getVariables().put( szKey.toLowerCase().trim() , szValue ) ;
		this.autocompleter.insert( szKey.toLowerCase().trim() ) ;
		return ;
	}

	// Setters
	// Changed from design to allow words added to the autocompleter to 
	// persist across instances of the Dictionary
	/** Sets the autocompleter */
	public void setAutocompleter ( final Autocompleter autocompleter )
	{
		this.autocompleter = autocompleter ; 
		return ; 
	}
	// Getters
	/** @return the hash table of words */
	public Hashtable<String, Word> getWords()
	{
		return this.words ;
	}
	/** @return the hash table of variables */
	public Hashtable<String, String> getVariables()
	{
		return this.variables ;
	}
	/** @return the Autocompleter */
	public Autocompleter getAutocompleter()
	{
		return this.autocompleter ;
	}
	/** @return the Word with name szName */
	public Word getWord( final String szName )
	{
		return this.words.get( szName.toLowerCase() ) ;
	}
	/** @return the Variable with name szName */
	public String getVariable( final String szName )
	{
		return this.variables.get( szName.toLowerCase() ) ;
	}
	/** @return the definition of the Word with name szName */
	public String getDefinition( final String szName )
	{
		final Word word = this.getWords().get(
				szName.trim().toLowerCase() ) ;
		String szRet = "" ;
		if ( word == null )
		{
			szRet = "" ;
		}
		else
		{
			szRet = word.getDefinition() ;
		}
		return szRet ;
	}

	/** Checks if a word is in the dictionary */
	public boolean checkWordDefined( final String szName )
	{
		boolean bRC = false ;
		// Set to false if null
		if ( this.getWords() == null )
		{
			bRC = false ;
		}
		// If not null, check if exists
		else if ( this.getWords().containsKey(
				szName.trim().toLowerCase() ) )
		{
			bRC = true ;
		}
		return bRC ;
	}
	/** Checks if a word's definition matches a specific String */
	public boolean checkWordDefinedAs( final String szName
			, final String szDefinition )
	{
		if ( this.getWord( szName.trim().toLowerCase() ) == null )
		{
			return false ;
		}
		return this.getDefinition( szName.trim() ).trim()
				.equalsIgnoreCase( szDefinition.trim() ) ;
	}

	/** Checks if a variable is defined */
	public boolean checkVariableDefined( final String szName )
	{
		boolean bRC = false ;

		if ( this.getVariables() == null )
		{
			bRC = false ;
		}
		else if ( this.getVariables().containsKey(
				szName.trim().toLowerCase() ) )
		{
			bRC = true ;
		}
		return bRC ;
	}

	// For testing
	public static void main( String[] args )
	{
		Dictionary dict = new Dictionary() ;
		dict.addWord( "Print" , ".\" " ) ;
		dict.addWord( "Print" , ". " ) ;
		dict.addWord( "Pre" , "1 2 3 " ) ;
		dict.addWord( "Hello" , "( a )" );

		System.out.println( dict.getWords() );
		System.out.println( dict.getDefinition( "Hello" ) );
		System.out.println( "Is \'Hello\' defined? "
				+ dict.checkWordDefined( "Hello" ) );
		System.out.println( "Is \'Goodbye' defined? "
				+ dict.checkWordDefined( "Goodbye" ) );
		System.out.println( "Is \'Print\' defined as \'.\'? "
				+ dict.checkWordDefinedAs( "Print" , "." ) );
		System.out.println( "Is \'Print\' defined as \'.\"\'? "
				+ dict.checkWordDefinedAs( "Print" , ".\"" ) );

		System.out.println( "Still defined? " + dict.checkWordDefined( "Print" )
		+ " - defined as: >>>" + dict.getDefinition( "Print" ) + "<<<" ) ;
		System.out.println("Undefining \'Print\'...") ;
		dict.undefineWord( "Print" ) ;
		System.out.println( "Still defined? "
				+ dict.checkWordDefined( "Print" ) + " - defined as: >>>"
				+ dict.getDefinition( "Print" ) + "<<<" ) ;
		System.out.println("Undefining \'Print\'...") ;
		dict.undefineWord( "Print" ) ;
		System.out.println( "Still defined? "
				+  dict.checkWordDefined( "Print" ) ) ;

		dict.getAutocompleter().display();

	}

}
