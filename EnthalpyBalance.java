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
  public EnthalpyBalance(double Tref, Stream[] unknownTempStreams, Stream[] inletStreams, Stream[] outletStreams, Behaviour behaviour) {
    
    super(0., 1.);
    this.Tref = Tref;
    this.isInlet = isInlet;
    this.behaviour = behaviour;
    
    double[] boundaryValues = {Double.MIN_VALUE, Double.MAX_VALUE};
    
    if (unknownTempStreams != null) {
      this.unknownTempStreams = new Stream[unknownTempStreams.length];
      for (int i = 0; i < unknownTempStreams.length; i++) {
        this.unknownTempStreams[i] = new Stream(unknownTempStreams[i]);
        boundaryValues = this.calculateBoundaryValues(boundaryValues[0], boundaryValues[1], this.unknownTempStreams[i]);
      }
    }
    
    if (inletStreams != null) {
      this.inletStreams = new Stream[inletStreams.length];
      for (int i = 0; i < inletStreams.length; i++) {
        this.inletStreams[i] = new Stream(inletStreams[i]);
        boundaryValues = this.calculateBoundaryValues(boundaryValues[0], boundaryValues[1], this.inletStreams[i]);
      }
    }
    
    if (outletStreams != null) {
      this.outletStreams = new Stream[outletStreams.length];
      for (int i = 0; i < outletStreams.length; i++) {
        this.outletStreams[i] = new Stream(outletStreams[i]);
        boundaryValues = this.calculateBoundaryValues(boundaryValues[0], boundaryValues[1], this.outletStreams[i]);
      }
    }
    
    super.setMinX(boundaryValues[0]);
    super.setMaxX(boundaryValues[1]);
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  2) evaluateWithinBounds() : Returns the energy that must be added to the system.
---------------------------------------------------------------------------------------------------------------------*/
  protected double evaluateWithinBounds(double x, double[] constants) throws FunctionException {
    //System.out.println("Test - EnthalpyBalance Class - evaluateWithinBounds Method: x = " + x);
    return this.evaluateHeat(x, false);
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  3) evaluateDerivativeWithinBounds() : Returns the derivative of the energy that must be added to the system with 
                                        respect to the unknown temperature.
---------------------------------------------------------------------------------------------------------------------*/
  protected double evaluateDerivativeWithinBounds(double x, double[] constants) throws FunctionException {
    //System.out.println("Test - EnthalpyBalance Class - evaluateDerivativeWithinBounds Method: x = " + x);
    return this.evaluateHeat(x, true);
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  4) evaluateHeat() : Returns the heat of the flash separation, Q.
                      If Q is positive, then energy must be added to the system.
                      If Q is negative, then energy must be removed from the system.
---------------------------------------------------------------------------------------------------------------------*/
  private double evaluateHeat(double T, boolean derivative) throws FunctionException {
    
    double Q = 0;
    
    //System.out.println("Test - EnthalpyBalance Class: T = " + T);
    //System.out.println("Test - EnthalpyBalance Class: Test 1");
    if (this.unknownTempStreams != null) {
      for (int i = 0; i < unknownTempStreams.length; i++) {
        
        unknownTempStreams[i].setT(T);
        
        if (this.isInlet) {
          Q -= this.behaviour.evaluateStreamEnthalpy(this.Tref, unknownTempStreams[i], derivative);
        }
        else {
          Q += this.behaviour.evaluateStreamEnthalpy(this.Tref, unknownTempStreams[i], derivative);
        }
      }
    }
    //System.out.println("Test - EnthalpyBalance Class: Test 2");
    
    if (this.inletStreams != null) {
      for (int i = 0; i < inletStreams.length; i++) {
        Q -= this.behaviour.evaluateStreamEnthalpy(this.Tref, inletStreams[i], derivative);
      }
    }
    //System.out.println("Test - EnthalpyBalance Class: Test 3");
    
    if (this.outletStreams != null) {
      for (int i = 0; i < outletStreams.length; i++) {
        Q += this.behaviour.evaluateStreamEnthalpy(this.Tref, outletStreams[i], derivative);
      }
    }
    //System.out.println("Test - EnthalpyBalance Class: Test 4");
    
    return Q;
  }
/*********************************************************************************************************************/
  
  
  private double[] calculateBoundaryValues(double minX, double maxX, Stream stream) {
    
    for (int j = 0; j < stream.getComponentCount(); j++) {
      minX = Math.max(minX, Menu.getSpecies(stream.getSpeciesIndex(j)).getCorrelation(Species.ENTHALPY_LIQUID).getMinX());
      minX = Math.max(minX, Menu.getSpecies(stream.getSpeciesIndex(j)).getCorrelation(Species.ENTHALPY_VAPOUR).getMinX());
      
      maxX = Math.min(maxX, Menu.getSpecies(stream.getSpeciesIndex(j)).getCorrelation(Species.ENTHALPY_LIQUID).getMaxX());
      maxX = Math.min(maxX, Menu.getSpecies(stream.getSpeciesIndex(j)).getCorrelation(Species.ENTHALPY_VAPOUR).getMaxX());
    }
    
    //System.out.println("Test - EnthalpyBalance Class - calculateBoundaryValues() Method: minX = " + minX);
    //System.out.println("Test - EnthalpyBalance Class - calculateBoundaryValues() Method: maxX = " + maxX);
    
    return new double[]{minX, maxX};
  }
  
}