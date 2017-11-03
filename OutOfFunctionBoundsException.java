public class OutOfFunctionBoundsException extends FunctionException {
   public OutOfFunctionBoundsException() {
     super("OutOfFunctionBoundsException: The independent variable does not fall within the bounds of the function."); 
   }
 }