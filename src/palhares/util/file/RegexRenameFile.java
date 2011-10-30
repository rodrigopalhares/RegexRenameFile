package palhares.util.file;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Aplicação swing que permite renomear arquivos usando o regex do java.
 * @author Palhares
 */
public class RegexRenameFile extends JPanel {
    private static final long serialVersionUID = 6806238886135776531L;

    private JTextField regexField;
    private JTextField replacementField;
    private DefaultTableModel previewData;
    private JButton renameButton;
    private JButton openButton;
    private JButton rollbackButton;

    private File[] fileSelected;
    private List<File[]> rollbackFile = new ArrayList<File[]>(); 

    public RegexRenameFile() {
        super(new BorderLayout());

        this.previewData = new DefaultTableModel(new String[]{"Antes", "Depois"}, 0);

        final JTable table = new JTable(this.previewData);
        table.setPreferredScrollableViewportSize(new Dimension(500, 200));
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        this.add(scrollPane);

        this.openButton = new JButton("Open...");
        this.openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                chooseFiles();
            }
        });

        this.rollbackButton = new JButton("Rollback");
        this.rollbackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                performRollback();
            }
        });

        this.renameButton = new JButton("Renomear");
        this.renameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                performRename();
            }
        });

        JPanel panel = new JPanel(new GridLayout(2, 3));
        this.regexField = new JTextField();
        this.regexField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent evt) {
                updatePreview();
            }
        });

        this.replacementField = new JTextField();
        this.replacementField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent evt) {
                updatePreview();
            }
        });

        panel.add(new JLabel("Pattern: ", SwingConstants.RIGHT));
        panel.add(this.regexField);
        panel.add(this.replacementField);
        panel.add(this.openButton);
        panel.add(this.rollbackButton);
        panel.add(this.renameButton);
        panel.setPreferredSize(new Dimension(0, 50));
        this.add(panel, BorderLayout.PAGE_END);

        this.updatePreview();
    }

    /**
     * Abre a caixa de dialog de escolha de arquivo.
     */
    private void chooseFiles() {
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fileSelected = fc.getSelectedFiles();
            updatePreview();
        }
    }

    /**
     * Realiza o rename.
     */
    private void performRename() {
        if (this.fileSelected == null) {
            JOptionPane.showMessageDialog(this, "Os arquivos não foram selecionados.", "Files not selected", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String regex = this.regexField.getText();
        String replecement = this.replacementField.getText();
        if (regex.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Os padrões de rename não foram informados.", "Regex not informed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.rollbackFile.clear();
        String fileErros = "";
        for(int i=0; i < this.fileSelected.length; i++) {
            File file = this.fileSelected[i];
            File newFile = new File(file.getParentFile(), file.getName().replaceAll(regex, replecement));
            if (!file.renameTo(newFile)) {
                fileErros = fileErros + file.getName() + "\n";
            } else {
                this.rollbackFile.add(new File[]{newFile, file});
                this.fileSelected[i] = newFile;
            }
        }
        if (!fileErros.isEmpty()) {
            JOptionPane.showMessageDialog(this, fileErros.substring(0, fileErros.length()-1), "Arquivos não renomeados", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Arquivos renomeados com sucesso", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        }
        this.updatePreview();
    }

    /**
     * Realiza o rollback dos renames realizados.
     */
    private void performRollback() {
        if (this.fileSelected == null || this.rollbackFile.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O rollback não disponível.", "O rollback não disponível", JOptionPane.ERROR_MESSAGE);
            return;
        }
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
        if (!fileErros.isEmpty()) {
            JOptionPane.showMessageDialog(this, fileErros.substring(0, fileErros.length()-1), "Arquivos não renomeados", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Arquivos renomeados com sucesso", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        }
        this.rollbackFile = rollbackListTemp;
        this.updatePreview();
    }

    /**
     * Atualiza a lista de preview.
     */
    private void updatePreview() {
        if (this.fileSelected == null) {
            return;
        }
        this.previewData.setRowCount(0);
        String regex = this.regexField.getText();
        String replecement = this.replacementField.getText();
        for(File file : this.fileSelected) {
            String fileName = file.getName();
            String fileNameUpdated;
            if (regex.isEmpty()) {
                fileNameUpdated = "";
            } else {
                fileNameUpdated = fileName.replaceAll(regex, replecement);
            }
            this.previewData.addRow(new String[]{fileName, fileNameUpdated});
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Regex Rename File");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        RegexRenameFile newContentPane = new RegexRenameFile();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
