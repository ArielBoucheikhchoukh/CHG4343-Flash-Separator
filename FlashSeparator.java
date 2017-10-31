public abstract class FlashSeparator {
 
  private double T;
  private double P;
  private double Q;
  private Behaviour behaviour;
  private Stream feedStream;
  
  public FlashSeparator(double T, double P, Stream feedStream) {
    this.T = T;
    this.P = P;
    this.Q = 0.;
    this.feedStream = new Stream(feedStream);
  }
  
  public FlashSeparator(double P, Stream feedStream) {
    this.T = 273.15;
    this.P = P;
    this.Q = 0.;
    this.feedStream = new Stream(feedStream);
  }
  
  public abstract Stream[] flashCalculation() throws FlashCalculationException;
  
  protected void flash(Stream flashStream) {
    this.behaviour.flash(flashStream); /* This method should perform a dew-point/bubble-point calculation,
                                          evaluate the K-values, and solve the Rachford-Rice equation.
                                          Should return an exception that extends FlashCalculationException in the 
                                          case where a flash calculation cannot be performed. */
  }
  
  public double getT() {
   return this.T; 
  }
  
  public void setT(double T) {
   this.T = T; 
  }
  
  public double getP() {
   return this.P; 
  }
  
  public void setP(double P) {
   this.P = P; 
  }
  
  public double getQ() {
   return this.Q; 
  }
  
  public void setQ(double Q) {
   this.Q = Q; 
  }
  
  public Stream getFeedStream() {
   return new Stream(this.feedStream); 
  }
  
  public void setFeedStream(Stream feedStream) {
   this.feedStream = new Stream(feedStream);
  }
  
  protected Behaviour getBehaviour() {
   return new Behaviour(this.behaviour); 
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