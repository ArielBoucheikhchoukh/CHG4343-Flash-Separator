
public class NoRootFoundException extends NumericalMethodException {
	
	public NoRootFoundException(String numericalMethodName, String functionName, 
			RootFinder rootFinder, Function function) {
		super(numericalMethodName, functionName, rootFinder, function);
	}
	
	public String getMessage() {
		return "NoRootFoundException: The numerical method '" + super.getNumericalMethodName() 
			+ "' could not find the root of '" + super.getFunctionName() + "'.";
	}
}
