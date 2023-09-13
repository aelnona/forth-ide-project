/**
 * @version 2 2022-11-08
 *
 */
public final class Token
{
	// Defines the possible types for each Token
	public static enum Type
	{
		WORD 
		, LITERAL 
		, COMMENT_START 
		, COMMENT_CONTENT
		, COMMENT_END 
		, PRINT_START
		, PRINT_CONTENT 
		, PRINT_END
		, IF
		, ELSE
		, THEN
		, DO
		, LOOP
		, FORGET 
		, VARIABLE
		, VARIABLE_NAME
		, VARIABLE_STORE
		, VARIABLE_GET
		, VARIABLE_PRINT
		, COLON
		, WORD_NAME
		, DEFINITION
		, SEMICOLON
		, END 
		, BREAK ; 
	}
	
	// The String/text of the token
	private final String szString ;
	// The token's type
	private Type type ;
	
	// Constructor
	public Token( final String szString , final Type type )
	{
		this.szString = szString ;
		this.setType( type ) ; 
		return ;
	}
	
	// Setters
	public void setType( final Type type ) { this.type = type ; }
	
	// Getters
	public String getString() { return this.szString ; } 
	public Type getType() { return this.type ; }
}
