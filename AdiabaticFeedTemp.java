//Case 3

public class AdiabaticFeedTemp extends FlashSeparator {
  
  public AdiabaticFeedTemp(double tankT, double tankP, double F, double[] z, int[] speciesIndices) {
    super(tankT, tankP, new Stream("Feed Stream", 273.15, F, z, speciesIndices));
  }
  
  public Stream[] flashCalculation() throws FlashCalculationException, FunctionException, NumericalMethodException {
    
    Stream feedStream = super.getFeedStream();
    
    System.out.println("Test - AdiabaticFeedTemp Class: Print the names of the components in the feed stream.");
    for (int i = 0; i < feedStream.getComponentCount(); i++) {
      System.out.println("Component " + i + ": " + Menu.getSpeciesName(feedStream.getSpeciesIndex(i)));
    }
    
    Stream flashStream = new Stream(feedStream);
    flashStream.setT(super.getT());
    flashStream.setP(super.getP());
    
    super.flash(flashStream);
    System.out.println("Test - AdiabaticFeedTemp Class: Stream was successfully flashed.");
    
    double Tref = super.selectReferenceTemperature();
    //Tref = 100.;
    EnthalpyBalance enthalpyBalance = new EnthalpyBalance(Tref, new Stream[]{new Stream(feedStream)}, null, 
                                                          new Stream[]{new Stream(flashStream)}, super.getBehaviour(), true);
    //System.out.println("Test - AdiabaticFeedTemp Class: test Q = " + enthalpyBalance.evaluate(373., null));
    try {
      double T_guess = enthalpyBalance.getMinX(); 
      double T_feed = Menu.findRoot(enthalpyBalance, null, T_guess, super.ENTHALPY_BALANCE_INCREMENT_LENGTH,
                                    super.ENTHALPY_BALANCE_TOLERANCE, super.ENTHALPY_BALANCE_MAX_EVALUATION_COUNT);
      super.setFeedStreamTemperature(T_feed);
      System.out.println("Test - AdiabaticFeedTemp Class: Feed temperature was successfully calculated.");
    }
    //catch (OutOfFunctionBoundsException | NoRootWithinFunctionBoundsException e) {
    catch (Exception e) {
      System.out.println(e.getMessage());
      System.out.println("Test - AdiabaticFeedTemp Class: Feed temperature could not be calculated due to correlation limitations.");
    }
    
    Stream[] flashStreams = super.splitPhases(flashStream);
    System.out.println("Test - AdiabaticFeedTemp Class: Flash stream was successfully split into phases.");
    
    return flashStreams;
  }
  
}