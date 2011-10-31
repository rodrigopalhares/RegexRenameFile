package palhares.util.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RegexRenameFile {
	private File[] files;
	private List<File[]> rollbackFile = new ArrayList<File[]>();

	public RegexRenameFile(File[] files) {
		this.files = files;
	}

	/**
	 * Realiza o rename.
	 */
	public String performRename(String regex, String replacement) {
		this.rollbackFile.clear();
		String fileErros = "";
		for (int i = 0; i < this.files.length; i++) {
			File file = this.files[i];
			File newFile = new File(file.getParentFile(), file.getName()
					.replaceAll(regex, replacement));
			if (!file.renameTo(newFile)) {
				fileErros = fileErros + file.getName() + "\n";
			} else {
				this.rollbackFile.add(new File[] { newFile, file });
				this.files[i] = newFile;
			}
		}
		return fileErros;
	}

	/**
	 * Realiza o rollback dos renames realizados.
	 */
	public String performRollback() {
		String fileErros = "";
		List<File[]> rollbackListTemp = new ArrayList<File[]>();
		for (File[] file : this.rollbackFile) {
			if (!file[0].renameTo(file[1])) {
				fileErros = fileErros + file[0].getName() + "\n";
				rollbackListTemp.add(file);
			} else {
				for (int i = 0; i < this.files.length; i++) {
					if (this.files[i].equals(file[0])) {
						this.files[i] = file[1];
					}
				}
			}
		}
		this.rollbackFile = rollbackListTemp;
		return fileErros;
	}

	public File[] getFiles() {
		return files;
	}
}
