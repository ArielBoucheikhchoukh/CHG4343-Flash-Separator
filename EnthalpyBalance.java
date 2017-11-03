public class EnthalpyBalance extends BoundedFunction {
  
  private double Tref;
  private boolean isInlet; // true if the unknown temperature streams are inlet streams, false if they are outlet streams
  private Stream[] unknownTempStreams; //Inlet or outlet streams, all evaluated at the same temperature
  private Stream[] inletStreams; //Inlet streams with constant temperatures
  private Stream[] outletStreams; //Outlet streams with constant temperatures
  private Behaviour behaviour;
  
  
/**********************************************************************************************************************
  1) Constructor
---------------------------------------------------------------------------------------------------------------------*/
  public EnthalpyBalance(double Tref, Stream[] unknownTempStreams, Stream[] inletStreams, Stream[] outletStreams, Behaviour behaviour, double minX, 
                         double maxX) {
    super(minX, maxX);
    this.Tref = Tref;
    this.isInlet = isInlet;
    this.behaviour = behaviour;
    
    if (unknownTempStreams != null) {
      this.unknownTempStreams = new Stream[unknownTempStreams.length];
      for (int i = 0; i < unknownTempStreams.length; i++) {
        this.unknownTempStreams[i] = new Stream(unknownTempStreams[i]);
      }
    }
    
    if (inletStreams != null) {
      this.inletStreams = new Stream[inletStreams.length];
      for (int i = 0; i < inletStreams.length; i++) {
        this.inletStreams[i] = new Stream(inletStreams[i]);
      }
    }
    
    if (outletStreams != null) {
      this.outletStreams = new Stream[outletStreams.length];
      for (int i = 0; i < outletStreams.length; i++) {
        this.outletStreams[i] = new Stream(outletStreams[i]);
      }
    }
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  2) evaluateWithinBounds() : Returns the energy that must be added to the system
                              If Q is positive, then energy must be added to the system.
                              If Q is negative, then energy must be removed from the system.
---------------------------------------------------------------------------------------------------------------------*/
  protected double evaluateWithinBounds(double x, double[] constants) {
    double Q = 0;
    
    if (this.unknownTempStreams != null) {
      for (int i = 0; i < unknownTempStreams.length; i++) {
        if (this.isInlet) {
          Q -= this.behaviour.evaluateStreamEnthalpy(x, this.Tref, unknownTempStreams[i]);
        }
        else {
          Q += this.behaviour.evaluateStreamEnthalpy(x, this.Tref, unknownTempStreams[i]);
        }
      }
    }
    
    if (this.inletStreams != null) {
      for (int i = 0; i < inletStreams.length; i++) {
        Q -= this.behaviour.evaluateStreamEnthalpy(inletStreams[i].getT(), this.Tref, inletStreams[i]);
      }
    }
    
    if (this.outletStreams != null) {
      for (int i = 0; i < outletStreams.length; i++) {
        Q += this.behaviour.evaluateStreamEnthalpy(outletStreams[i].getT(), this.Tref, outletStreams[i]);
      }
    }
    
    return Q;
  }
/*********************************************************************************************************************/
  
  protected double evaluateDerivativeWithinBounds(double x, double[] constants) {
    return 0; // temporary
  }
  
}