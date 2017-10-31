/*Case 1*/

public class IsothermalHeat extends FlashSeparator {
  
/**********************************************************************************************************************
  1) Constructor
---------------------------------------------------------------------------------------------------------------------*/
  public IsothermalHeat(double T, double tankP, double F, double[] z, int[] speciesIndices) {
    super(T, tankP, new Stream(T, F, z, speciesIndices));
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  2) falshCalculation()
---------------------------------------------------------------------------------------------------------------------*/
  public Stream[] flashCalculation() throws FlashCalculationException {
    
    Stream feedStream = super.getFeedStream();
    Stream flashStream = new Stream(feedStream);
    flashStream.setP(super.getP());
    
    super.flash(flashStream);
    
    double Tref = 0;
    EnthalpyBalance enthalpyBalance = new EnthalpyBalance(Tref, null, new Stream[]{new Stream(feedStream)}, 
                                                          new Stream[]{new Stream(flashStream)}, super.getBehaviour(), 0, 
                                                          1000000);
    
    super.setQ(enthalpyBalance.evaluate(0, Tref));
    
    Stream[] flashStreams = new Stream[2]; //maybe call some FlashSeparator method to return separate liquid and vapour streams
    
    return flashStreams;
  }
/*********************************************************************************************************************/
  
}