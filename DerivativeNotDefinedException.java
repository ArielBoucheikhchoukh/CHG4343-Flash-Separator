
public class DerivativeNotDefinedException extends FunctionException {
	
	public DerivativeNotDefinedException(String functionID) {
		super("DerivativeNotDefinedException: The derivative of the function is undefined.", functionID);
	}
	
}
