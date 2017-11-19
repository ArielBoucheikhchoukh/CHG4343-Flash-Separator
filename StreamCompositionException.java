
public class StreamCompositionException extends StreamException {
	
	public StreamCompositionException(Stream stream) {
		super("StreamCompositionException: Composition of the stream '" + stream.getName() 
		+ "' is inconsistent.", stream);
	}
	
}
