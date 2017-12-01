public class NumericalMethodException extends Exception {
  
  private String numericalMethodName;
  private String functionName;
  private RootFinder rootFinder;
  private Function function;
  
  public NumericalMethodException(String numericalMethodName, String functionName, 
                                  RootFinder rootFinder, Function function) {
    super();
    this.numericalMethodName = numericalMethodName;
    this.functionName = functionName;
    if (rootFinder != null) {
      this.rootFinder = rootFinder.clone();
    }
    if (function != null) {
      this.function = function.clone();
    }
  }
  
  public String getMessage() {
    return "NumericalMethodException: The function '" + this.functionName + "' evaluated via '" 
      + this.numericalMethodName + "' resulted in an error.";
  }
  
  public String getNumericalMethodName() {
    return this.numericalMethodName;
  }
  
  public void setNumericalMethodName(String numericalMethodName) {
    this.numericalMethodName = numericalMethodName;
  }
  
  public String getFunctionName() {
    return this.functionName;
  }
  
  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }
  
  public RootFinder getRootFinder() {
    return this.rootFinder.clone();
  }
  
  public void setRootFinder(RootFinder rootFinder) {
    this.rootFinder = rootFinder.clone();
  }
  
  public Function getFunction() {
    return this.function.clone();
  }
  
  public void setFunction(Function function) {
    this.function = function.clone();
  }
  
  
  
}