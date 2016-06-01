package xextension.http;

/**
 * Default strategy for creating and cleaning up temporary files.
 */
public class DefaultTempFileManagerFactory implements TempFileManagerFactory {

  @Override
  public TempFileManager create() {
      return new DefaultTempFileManager();
  }
}
