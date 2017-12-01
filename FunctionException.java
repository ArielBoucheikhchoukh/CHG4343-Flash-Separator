
public class FunctionException extends Exception {
	
	private String functionID;
	private Function function;
	
	public FunctionException(String message, String functionID, Function f) {
		super(message + "\nFunction ID: " + functionID);
		this.functionID = functionID;
		this.function = f.clone();
	}
	
	public String getFunctionID() {
		return this.functionID;
	}
	
	public void setFunctionID(String functionID) {
		this.functionID = functionID;
	}
	
	public Function getFunction() {
		return this.function.clone();
	}
	
	public void setFunction(Function f) {
		this.function = f.clone();
	}
	
}
