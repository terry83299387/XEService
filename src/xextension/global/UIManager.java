/**
 * 
 */
package xextension.global;

/**
 * @author QiaoMingkui
 * 
 */
public class UIManager {

	/**
	 * Initialize UI look and feel (L&F).
	 */
	public static void initLookAndFeel() {
		try {
			boolean windows = System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1;
			if (windows) {
				javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			}
		} catch (Exception e) {
		}
	}
}
