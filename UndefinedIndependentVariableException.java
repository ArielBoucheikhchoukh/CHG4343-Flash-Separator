
public class UndefinedIndependentVariableException extends FunctionException {
	
	public UndefinedIndependentVariableException(String functionID) {
		super("UndefinedIndependentVariableException: Function was evaluated at an undefined value.", 
				functionID);
	}
	
}
