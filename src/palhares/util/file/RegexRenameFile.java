package palhares.util.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class RegexRenameFile {
    private File directory;
    private File[] fileSelected;
    private List<File[]> rollbackFile = new ArrayList<File[]>(); 

    public RegexRenameFile(File directory) {
	this.directory = directory;
    }

    /**
    * Realiza o rename.
    */
   private String performRename(String regex, String replacement) {
       this.rollbackFile.clear();
       String fileErros = "";
       for(int i=0; i < this.fileSelected.length; i++) {
           File file = this.fileSelected[i];
           File newFile = new File(file.getParentFile(), file.getName().replaceAll(regex, replacement));
           if (!file.renameTo(newFile)) {
               fileErros = fileErros + file.getName() + "\n";
           } else {
               this.rollbackFile.add(new File[]{newFile, file});
               this.fileSelected[i] = newFile;
           }
       }
       return fileErros;
   }

   /**
    * Realiza o rollback dos renames realizados.
    */
   private void performRollback() {
       String fileErros = "";
       List<File[]> rollbackListTemp = new ArrayList<File[]>();
       for (File[] file : this.rollbackFile) {
           if (!file[0].renameTo(file[1])) {
               fileErros = fileErros + file[0].getName() + "\n";
               rollbackListTemp.add(file);
           } else {
               for(int i=0; i < this.fileSelected.length; i++) {
                   if (this.fileSelected[i].equals(file[0])) {
                       this.fileSelected[i] = file[1];
                   }
               }
           }
       }
       this.rollbackFile = rollbackListTemp;
       this.updatePreview();
   }
}
