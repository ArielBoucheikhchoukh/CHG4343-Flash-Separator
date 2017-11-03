public interface Function {
  
  public double evaluate(double x, double[] constants) throws FunctionException, NumericalMethodException;
  
  public double evaluateDerivative(double x, double[] constants) throws FunctionException, NumericalMethodException;
  
}