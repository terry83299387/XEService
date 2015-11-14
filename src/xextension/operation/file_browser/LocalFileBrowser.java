package xextension.operation.file_browser;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class LocalFileBrowser {
	public static final int		FILES_ONLY			= JFileChooser.FILES_ONLY;
	public static final int		DIRECTORIES_ONLY	= JFileChooser.DIRECTORIES_ONLY;
	public static final int		FILES_DIRECTORIES	= JFileChooser.FILES_AND_DIRECTORIES;

	public static final String	FILE_SEPARATOR		= "|";

	private _FileChooser		fileChooser			= new _FileChooser();

	public void chooseFile() {
		fileChooser.start();
	}

	public boolean hasSelected() {
		return fileChooser.selected;
	}

	public String getSelectedFiles() {
		if (!fileChooser.selected)
			throw new IllegalStateException("file has not been selected");


		return fileChooser.selectedFiles;
	}

	private static class _FileChooser extends Thread {
		private boolean		multiSelection;
		private int			fileSelectionMode	= FILES_ONLY;
		private String		defaultDirectory;
		private FileFilter	fileFilter;

		private boolean		selected;
		private String		selectedFiles;

		public void run() {
			JFileChooser fileChooser = new JFileChooser();

			fileChooser.setMultiSelectionEnabled(multiSelection);
			fileChooser.setFileSelectionMode(fileSelectionMode);
			if (defaultDirectory != null && defaultDirectory.length() != 0) {
				fileChooser.setCurrentDirectory(new File(defaultDirectory));
			}

			if (fileFilter != null) {
				fileChooser.addChoosableFileFilter(fileFilter);
			}

			int button = fileChooser.showOpenDialog(null);

			selected = true;
			if (button != JFileChooser.APPROVE_OPTION) {
				return;
			}

			if (!multiSelection) {
				selectedFiles = fileChooser.getSelectedFile().getAbsolutePath();
			} else {
				StringBuilder sb = new StringBuilder(1024);
				File[] files = fileChooser.getSelectedFiles();
				for (File file : files) {
					if (sb.length() > 0) {
						sb.append(FILE_SEPARATOR);
					}
					sb.append(file.getAbsolutePath());
				}
				selectedFiles = sb.toString();
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
		if (fileSelectionMode == FILES_ONLY || fileSelectionMode == DIRECTORIES_ONLY
				|| fileSelectionMode == FILES_DIRECTORIES) {
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
