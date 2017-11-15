/*Case 1*/

public class IsothermalHeat extends FlashSeparator {
  
/**********************************************************************************************************************
  1) Constructor
---------------------------------------------------------------------------------------------------------------------*/
  public IsothermalHeat(double T, double tankP, double F, double[] z, int[] speciesIndices) {
    super(T, tankP, new Stream("Feed Stream", T, F, z, speciesIndices));
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  2) falshCalculation()
---------------------------------------------------------------------------------------------------------------------*/
  public Stream[] flashCalculation() throws FlashCalculationException, FunctionException, NumericalMethodException {
    
    Stream feedStream = super.getFeedStream();
    
    System.out.println("Test - IsothermalHeat Class: Print the names of the components in the feed stream.");
    for (int i = 0; i < feedStream.getComponentCount(); i++) {
      System.out.println("Component " + i + ": " + Menu.getSpeciesName(feedStream.getSpeciesIndex(i)));
    }
    
    Stream flashStream = new Stream(feedStream);
    flashStream.setP(super.getP());
    
    super.flash(flashStream);
    System.out.println("Test - IsothermalHeat Class: Stream was successfully flashed.");
    
    double Tref = super.selectReferenceTemperature();
    EnthalpyBalance enthalpyBalance = new EnthalpyBalance(Tref, null, new Stream[]{new Stream(feedStream)}, 
                                                          new Stream[]{new Stream(flashStream)}, super.getBehaviour(), true);
    
    super.setQ(enthalpyBalance.evaluate(0., new double[]{Tref}));
    System.out.println("Test - IsothermalHeat Class: Heat was successfully calculated.");
    
    Stream[] flashStreams = super.splitPhases(flashStream);
    System.out.println("Test - IsothermalHeat Class: Flash stream was successfully split into phases.");
    
    return flashStreams;
  }
/*********************************************************************************************************************/
  
}