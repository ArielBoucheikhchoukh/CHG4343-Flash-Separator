//Case 1

public class IsothermalHeat extends FlashSeparator {
  
  public IsothermalHeat(double T, double tankP, double F, double[] z, int[] componentIndices) {
    super(T, tankP, new Stream(T, F, z, componentIndices));
  }
  
  public Stream[] flashCalculation() {
    Stream[] flashStreams = new Stream[2];
    
    return flashStreams;
  }
  
  public void setBehaviour(boolean nonIdealBehaviour) {
    super.setBehaviour(nonIdealBehaviour);
  }
  
}