public class TooManyFunctionEvaluationsException extends NumericalMethodException {
  public TooManyFunctionEvaluationsException() {
   super("TooManyFunctionEvaluationsException: The numerical method exceeded the maximum number of function evaluations."); 
  }
}