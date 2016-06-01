package xextension.http;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default strategy for creating and cleaning up temporary files.
 * <p/>
 * <p>
 * This class stores its files in the standard location (that is, wherever
 * <code>java.io.tmpdir</code> points to). Files are added to an internal
 * list, and deleted when no longer needed (that is, when
 * <code>clear()</code> is invoked at the end of processing a request).
 * </p>
 */
public class DefaultTempFileManager implements TempFileManager {
  /**
   * logger to log to.
   */
  private static final Logger LOG = Logger.getLogger(DefaultTempFileManager.class.getName());

  private final File tmpdir;

  private final List<TempFile> tempFiles;

  public DefaultTempFileManager() {
      this.tmpdir = new File(System.getProperty("java.io.tmpdir"));
      if (!tmpdir.exists()) {
          tmpdir.mkdirs();
      }
      this.tempFiles = new ArrayList<TempFile>();
  }

  @Override
  public void clear() {
      for (TempFile file : this.tempFiles) {
          try {
              file.delete();
          } catch (Exception ignored) {
              LOG.log(Level.WARNING, "could not delete file ", ignored);
          }
      }
      this.tempFiles.clear();
  }

  @Override
  public TempFile createTempFile(String filename_hint) throws Exception {
      DefaultTempFile tempFile = new DefaultTempFile(this.tmpdir);
      this.tempFiles.add(tempFile);
      return tempFile;
  }
}
