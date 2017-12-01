
public class StreamException extends Exception {
  
  private Stream stream;
  
  public StreamException(String message, Stream stream) {
    super(message);
    if (stream != null) {
      this.stream = stream.clone();
    }
  }
  
  public Stream getStream() {
    return this.stream.clone();
  }
  
  public void setStream(Stream stream) {
    this.stream = stream.clone();
  }
}
