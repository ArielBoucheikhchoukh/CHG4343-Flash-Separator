//Case 3

public class AdiabaticFeedTemp extends FlashSeparator {
  
  public AdiabaticFeedTemp(double tankT, double tankP, double F, double[] z, int[] speciesIndices) {
    super(tankT, tankP, new Stream(F, z, speciesIndices));
  }
  
  public Stream[] flashCalculation() throws FlashCalculationException {
    Stream[] flashStreams = new Stream[2];
    
    return flashStreams;
  }
  
}