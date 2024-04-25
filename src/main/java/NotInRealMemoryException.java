import java.lang.Exception;

/**
 * Exception for when a page is not in real memory
 */
public class NotInRealMemoryException extends Exception{
    public NotInRealMemoryException(String message){
        super(message);
    }
}
