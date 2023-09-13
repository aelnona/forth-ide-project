/**
 * @version 5 2022-11-05
 * 
 * An output stream that prints to a JTextArea object
 * Just boilerplate
 * 
 */

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextArea;

// From https://stackoverflow.com/questions/5107629/
// how-to-redirect-console-content-to-a-textarea-in-java
public class TextAreaOutputStream extends OutputStream 
{
	private final JTextArea textArea ; 
	
	/**
	 * Constructor
	 * @param textArea the text area to print to 
	 */
	public TextAreaOutputStream( final JTextArea textArea )
	{
		this.textArea = textArea ;
		return ;
	}
	/**
	 * Get textArea 
	 * @return this.textArea
	 */
	public JTextArea getTextArea() { return this.textArea ; } 

	/**
	 * @param b the byte
	 */
	@Override
	public void write( final int b ) throws IOException
	{
		// Convert to String and append to textArea
		this.textArea.append( String.format( "%s" , ( char ) b ) ) ;
        // Scrolls the text area to the end of data. Used so that user 
		//  does not need to scroll down on longer code print
        this.textArea.setCaretPosition( 
        		this.textArea.getDocument().getLength() ) ;
		return ; 
	} 
}
