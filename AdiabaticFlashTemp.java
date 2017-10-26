//Case 2

public class AdiabaticFlashTemp extends FlashSeparator {
  
  public AdiabaticFlashTemp(double feedT, double tankP, double F, double[] z, int[] speciesIndices) {
    super(tankP, new Stream(feedT, F, z, speciesIndices));
  }
  
  public Stream[] flashCalculation() throws FlashCalculationException {
    Stream[] flashStreams = new Stream[2];
    
    return flashStreams;
  }
  
}