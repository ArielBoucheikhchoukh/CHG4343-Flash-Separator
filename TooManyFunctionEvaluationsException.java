public class TooManyFunctionEvaluationsException extends NumericalMethodException {
  
  public TooManyFunctionEvaluationsException(String numericalMethodName, String functionName, 
                                             RootFinder rootFinder, Function function) {
    super(numericalMethodName, functionName, rootFinder, function);
  }
  
  public String getMessage() {
    return "TooManyFunctionEvaluationsException: The numerical method '" + super.getNumericalMethodName() 
      + "' evaluated '" + super.getFunctionName() + "' too many times.";
  }
  
}