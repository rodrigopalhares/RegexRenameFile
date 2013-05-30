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
 * 
 * @author Palhares
 */
public class RegexRenameFilePanel extends JPanel {
	private static final long serialVersionUID = 6806238886135776531L;

	private JTextField regexField;
	private JTextField replacementField;
	private DefaultTableModel previewData;
	private JButton renameButton;
	private JButton openButton;
	private JButton expandFoldersButton;
	private JButton rollbackButton;
	private JFileChooser fileChooser = new JFileChooser();

	private RegexRenameFile regexRenameFile = null;


	public RegexRenameFilePanel() {
		super(new BorderLayout());

		this.previewData = new DefaultTableModel(new String[] { "Current", "After" }, 0);

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

		this.expandFoldersButton = new JButton("Expand Folder");
		this.expandFoldersButton.setEnabled(false);
		this.expandFoldersButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				expandFolders();
			}
		});

		this.rollbackButton = new JButton("Rollback");
		this.rollbackButton.setEnabled(false);
		this.rollbackButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				performRollback();
			}
		});

		this.renameButton = new JButton("Rename");
		this.renameButton.setEnabled(false);
		this.renameButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				performRename();
			}
		});

		JPanel panel = new JPanel(new GridLayout(2, 4));
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

		this.fileChooser = new JFileChooser();
		this.fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		this.fileChooser.setMultiSelectionEnabled(true);

		panel.add(new JLabel("Pattern: ", SwingConstants.RIGHT));
		panel.add(this.regexField);
		panel.add(this.replacementField);
		panel.add(this.renameButton);
		panel.add(this.openButton);
		panel.add(this.expandFoldersButton);
		panel.add(this.rollbackButton);
		panel.setPreferredSize(new Dimension(0, 50));
		this.add(panel, BorderLayout.PAGE_END);

		this.updatePreview();
	}

	/**
	 * Expande as pastas na lista recursivamente.
	 */
	private void expandFolders() {
		File[] files = this.regexRenameFile.getFiles();
		List<File> expandFolders = this.expandFolders(files);
		this.regexRenameFile = new RegexRenameFile(expandFolders.toArray(new File[0]));
		updatePreview();
	}

	private List<File> expandFolders(File[] files) {
		List<File> fileList = new ArrayList<File>();
		for (File currentFile : files) {
			if (currentFile.isFile()) {
				fileList.add(currentFile);
			} else {
				fileList.addAll(this.expandFolders(currentFile.listFiles()));
			}
		}

		return fileList;
	}

	/**
	 * Abre a caixa de dialog de escolha de arquivo.
	 */
	private void chooseFiles() {
		int returnVal = fileChooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] fileSelected = fileChooser.getSelectedFiles();
			this.regexRenameFile = new RegexRenameFile(fileSelected);
			updatePreview();
			this.rollbackButton.setEnabled(false);
			this.renameButton.setEnabled(true);
			this.expandFoldersButton.setEnabled(true);
		}
	}

	/**
	 * Realiza o rename.
	 */
	private void performRename() {
		if (this.regexRenameFile == null) {
			JOptionPane.showMessageDialog(this, "Os arquivos não foram selecionados.", "Files not selected", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String regex = this.regexField.getText();
		String replacement = this.replacementField.getText();
		if (regex.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Os padrões de rename não foram informados.", "Regex not informed", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String fileErros = this.regexRenameFile.performRename(regex, replacement);

		if (!fileErros.isEmpty()) {
			JOptionPane.showMessageDialog(this, fileErros.substring(0, fileErros.length() - 1), "Arquivos não renomeados", JOptionPane.ERROR_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this, "Arquivos renomeados com sucesso", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
		}
		this.updatePreview();
		this.rollbackButton.setEnabled(true);
	}

	/**
	 * Realiza o rollback dos renames realizados.
	 */
	private void performRollback() {
		if (this.regexRenameFile == null || !this.rollbackButton.isEnabled()) {
			JOptionPane.showMessageDialog(this, "O rollback não disponível.", "O rollback não disponível", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String fileErros = this.regexRenameFile.performRollback();
		if (!fileErros.isEmpty()) {
			JOptionPane.showMessageDialog(this, fileErros.substring(0, fileErros.length() - 1), "Arquivos não renomeados", JOptionPane.ERROR_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this, "Arquivos renomeados com sucesso", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
		}
		this.updatePreview();
	}

	/**
	 * Atualiza a lista de preview.
	 */
	private void updatePreview() {
		if (this.regexRenameFile == null) {
			return;
		}
		this.previewData.setRowCount(0);
		String regex = this.regexField.getText();
		String replecement = this.replacementField.getText();
		for (File file : this.regexRenameFile.getFiles()) {
			String fileName = file.getName();
			String fileNameUpdated;
			if (regex.isEmpty()) {
				fileNameUpdated = "";
			} else {
				fileNameUpdated = fileName.replaceAll(regex, replecement);
			}
			this.previewData.addRow(new String[] { fileName, fileNameUpdated });
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("Regex Rename File");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		RegexRenameFilePanel newContentPane = new RegexRenameFilePanel();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
