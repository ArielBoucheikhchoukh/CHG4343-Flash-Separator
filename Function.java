public interface Function {
  
  public double evaluate(double x) throws NumericalMethodException;
  
  public double evaluateDerivative(double x) throws NumericalMethodException;
  
}