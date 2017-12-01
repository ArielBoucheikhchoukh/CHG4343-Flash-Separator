
public class StreamCompositionException extends StreamException {
	
	public StreamCompositionException(Stream stream, String arrayName) {
		super("The length of the array '" + arrayName + "' is inconsistent with the composition of stream " 
				+ stream.getName(), stream);
	}
}
