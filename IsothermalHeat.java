/*Case 1*/

public class IsothermalHeat extends FlashSeparator {
  
  /**********************************************************************************************************************
    * 1) Constructor
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public IsothermalHeat(double T, double tankP, double F, double[] z, int[] speciesIndices,
                        Behaviour behaviour) throws StreamException {
    super("Isothermal Heat", T, tankP, new Stream("Feed Stream", T, F, z, speciesIndices), behaviour);
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 2) Copy Constructor
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public IsothermalHeat(IsothermalHeat source) throws StreamException {
    super(source);
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 3) clone()
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public IsothermalHeat clone() {
    try {
      return new IsothermalHeat(this);
    }
    catch (StreamException e) {
      return null;
    }
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 4) falshCalculation() : Performs flash calculation and return outlet liquid-phase and vapour-phase 
    *        outlet streams; computes the heat duty of the separator under isothermal conditions.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public Stream[] flashCalculation() 
    throws FlashCalculationException, NumericalMethodException, 
    FunctionException, StreamException {
    
    Stream feedStream = super.getFeedStream();
    
    // Step 1. Attempt Flash Separator
    Stream flashStream = super.performFlash();
    
    // Step 2. Select Reference Temperature
    double Tref = super.selectReferenceTemperature(); // Returns lowest pure-species normal boiling point 
    
    // Step 3. Calculate Heat Duty [J/h]
    // The feed and flash streams are passed to the enthalpy balance as inlet and outlet streams
    // of known temperature respectively 
    EnthalpyBalance enthalpyBalance = new EnthalpyBalance(Tref, null, new Stream[] { new Stream(feedStream) },
                                                          new Stream[] { new Stream(flashStream) }, super.getBehaviour(), true, false);
    
    try {
      super.setQ(enthalpyBalance.evaluate(enthalpyBalance.getMinX(), new double[] { Tref }));
    } catch (FunctionException e) {
      Menu.appendToMessages("Error: Unable to compute heat of flash. \n" + e.getMessage());
    }
    
    // Step 4. Split the Flash Stream into Liquid and Vapour/Gas Phases
    Stream[] outletStreams = super.splitPhases();
    
    return outletStreams;
  }
  /*********************************************************************************************************************/
  
}