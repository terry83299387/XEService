package xextension.operation.file_browser;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class LocalFileBrowser {
	private _FileChooser		fileChooser = new _FileChooser();

	public void chooseFile() {
		fileChooser.start();
	}

	public boolean hasSelected() {
		return fileChooser.selected;
	}

	public File[] getSelectedFiles() {
		if (!fileChooser.selected)
			throw new IllegalStateException("file has not been selected");

		return fileChooser.selectedFiles;
	}

	public String getSelectedFilesStr() {
		if (!fileChooser.selected)
			throw new IllegalStateException("file has not been selected");

		File[] files = fileChooser.selectedFiles;
		if (files == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder(1024);
		for (File file : files) {
			if (sb.length() > 0) {
				sb.append(FileBrowser.FILE_SEPARATOR);
			}
			sb.append(file.getAbsolutePath());
		}
		return sb.toString();
	}

	public void closeFileChooser() {
		if (fileChooser.fileChooser != null) {
			fileChooser.fileChooser.cancelSelection();
		}
	}

	/**
	 * Open a local FileChooser dialog which runs in an individual thread.
	 * 
	 * @author QiaoMingkui
	 *
	 */
	private static class _FileChooser extends Thread {
		private boolean				multiSelection;
		private int						fileSelectionMode;
		private String				defaultDirectory;
		private FileFilter		fileFilter;
		private boolean				selected;
		private File[]				selectedFiles;
		private JFileChooser	fileChooser;

		public void run() {
			fileChooser = new JFileChooser();

			fileChooser.setMultiSelectionEnabled(multiSelection);
			fileChooser.setFileSelectionMode(fileSelectionMode);
			if (defaultDirectory != null && defaultDirectory.length() != 0) {
				fileChooser.setCurrentDirectory(new File(defaultDirectory));
			}
			if (fileFilter != null) {
				fileChooser.addChoosableFileFilter(fileFilter);
			}

			// let file chooser always be top
			JFrame frame = new JFrame("");
			frame.setAlwaysOnTop(true);
			frame.setState(JFrame.ICONIFIED);
			frame.setVisible(true);
			int button = fileChooser.showOpenDialog(frame);
			selected = true;
			frame.dispose(); // dispose frame window
			if (button == JFileChooser.APPROVE_OPTION) {
				if (multiSelection) {
					selectedFiles = fileChooser.getSelectedFiles();
				} else {
					selectedFiles = new File[] {fileChooser.getSelectedFile()};
				}
			}
		}
	}

	/**
	 * @return the multiSelection
	 */
	public boolean isMultiSelection() {
		return fileChooser.multiSelection;
	}

	/**
	 * @param multiSelection
	 *        the multiSelection to set
	 */
	public void setMultiSelection(boolean multiSelection) {
		fileChooser.multiSelection = multiSelection;
	}

	/**
	 * @return the fileSelectionMode
	 */
	public int getFileSelectionMode() {
		return fileChooser.fileSelectionMode;
	}

	/**
	 * @param fileSelectionMode
	 *        the fileSelectionMode to set
	 */
	public void setFileSelectionMode(int fileSelectionMode) {
		if (fileSelectionMode == FileBrowser.FILES_ONLY || fileSelectionMode == FileBrowser.DIRECTORIES_ONLY
				|| fileSelectionMode == FileBrowser.FILES_DIRECTORIES) {
			fileChooser.fileSelectionMode = fileSelectionMode;
		}
	}

	/**
	 * @return the defaultDirectory
	 */
	public String getDefaultDirectory() {
		return fileChooser.defaultDirectory;
	}

	/**
	 * @param defaultDirectory
	 *        the defaultDirectory to set
	 */
	public void setDefaultDirectory(String defaultDirectory) {
		fileChooser.defaultDirectory = defaultDirectory;
	}

	/**
	 * @return the fileFilter
	 */
	public FileFilter getFileFilter() {
		return fileChooser.fileFilter;
	}

	/**
	 * @param fileFilter
	 *        the fileFilter to set
	 */
	public void setFileFilter(FileFilter fileFilter) {
		fileChooser.fileFilter = fileFilter;
	}

	/**
	 * @param exts
	 *        the filter extensions
	 * @param desc
	 *        the filter description
	 */
	public void setFileFilter(String[] exts, String desc) {
		if (exts == null || exts.length == 0) {
			throw new IllegalArgumentException("filter extension can not be null");
		}

		fileChooser.fileFilter = new FileNameExtensionFilter(desc, exts);
	}

}
