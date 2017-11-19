
public class FunctionException extends Exception {
	
	private String functionID;
	
	public FunctionException(String message, String functionID) {
		super(message + "\nFunction ID: " + functionID);
		this.functionID = functionID;
	}
	
	public String getFunctionID() {
		return this.functionID;
	}
	
	public void setFunctionID(String functionID) {
		this.functionID = functionID;
	}
	
	
	
}
