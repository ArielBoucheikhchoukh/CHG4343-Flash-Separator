
public class DerivativeNotDefinedException extends FunctionException {
	
	public DerivativeNotDefinedException(String functionID, Function f) {
		super("DerivativeNotDefinedException: The derivative of the function is undefined.", functionID, f);
	}
	
}
