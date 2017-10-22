//Case 2

public class AdiabaticFlashTemp extends FlashSeparator {
  
  public AdiabaticFlashTemp(double feedT, double tankP, double F, double[] z, int[] componentIndices) {
    super(tankP, new Stream(feedT, F, z, componentIndices));
  }
  
  public Stream[] flashCalculation() {
    Stream[] flashStreams = new Stream[2];
    
    return flashStreams;
  }
  
  public void setBehaviour(boolean nonIdealBehaviour) {
    super.setBehaviour(nonIdealBehaviour);
  }
  
}