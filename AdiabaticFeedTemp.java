//Case 3

public class AdiabaticFeedTemp extends FlashSeparator {
  
  public AdiabaticFeedTemp(double tankT, double tankP, double F, double[] z, int[] componentIndices) {
    super(tankT, tankP, new Stream(F, z, componentIndices));
  }
  
  public Stream[] flashCalculation() {
    Stream[] flashStreams = new Stream[2];
    
    return flashStreams;
  }
  
  public void setBehaviour(boolean nonIdealBehaviour) {
    super.setBehaviour(nonIdealBehaviour);
  }
  
}