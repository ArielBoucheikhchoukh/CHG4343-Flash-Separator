public class Behaviour implements Cloneable {
  
  public Stream flash(Stream stream) {
   return new Stream(1); //temporary 
  }
  
  public double evaluateStreamEnthalpy(double T, double Tref, Stream stream) {
   return 0.; 
  }
  
  public Behaviour clone() {
    try {
      return (Behaviour) super.clone(); // might be subject to change
    }
    catch (CloneNotSupportedException e) {
     return null; 
    }
  }
  
}