public abstract class FlashSeparator {
 
  private double T;
  private double P;
  private Behaviour behaviour;
  private Stream feedStream;
  
  public FlashSeparator(double T, double P, Stream feedStream) {
    this.T = T;
    this.P = P;
    this.feedStream = new Stream(feedStream);
  }
  
  public FlashSeparator(double P, Stream feedStream) {
    this.T = 273.15;
    this.P = P;
    this.feedStream = new Stream(feedStream);
  }
  
  public abstract Stream[] flashCalculation() throws FlashCalculationException;
  
  public void setT(double T) {
   this.T = T; 
  }
  
  protected void setBehaviour(boolean nonIdealBehaviour) {
    if (!nonIdealBehaviour) {
     this.behaviour = new Behaviour(); 
    }
    else {
      this.behaviour = new NonIdealBehaviour();
    }
  }
  
}