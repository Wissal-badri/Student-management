import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class Note extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField ds1Field, ds2Field, ds3Field;
    private JLabel moyenneLabel, mentionLabel;
    private int currentStudentId = -1;

    public Note() {
        setTitle("Gestion des Notes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Main panel with modern background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(245, 245, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Table Panel
        createTablePanel(mainPanel);

        // Buttons Panel
        createButtonPanel(mainPanel);

        add(mainPanel);
        loadNotes();
    }

    private void createTablePanel(JPanel mainPanel) {
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "ID Étudiant", "Nom Complet", "DS1", "DS2", "DS3", "Moyenne", "Mention"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);

        // Make headers more visible
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 12));
        ((DefaultTableCellRenderer)header.getDefaultRenderer())
                .setHorizontalAlignment(JLabel.CENTER);

        // Center align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void createButtonPanel(JPanel mainPanel) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(245, 245, 250));

        JButton addButton = createStyledButton("Ajouter", new Color(70, 130, 180));
        JButton updateButton = createStyledButton("Modifier", new Color(60, 179, 113));
        JButton deleteButton = createStyledButton("Supprimer", new Color(220, 20, 60));

        addButton.addActionListener(e -> showNoteForm(null));
        updateButton.addActionListener(e -> updateSelectedNote());
        deleteButton.addActionListener(e -> deleteSelectedNote());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showNoteForm(Object[] noteData) {
        JDialog dialog = new JDialog(this, "Ajouter/Modifier Note", true);
        dialog.setSize(400, 400); // Made taller to accommodate student selection
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 245, 250));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create all components first
        JComboBox<StudentItem> studentComboBox = new JComboBox<>();
        loadStudentsIntoComboBox(studentComboBox);

        ds1Field = createStyledTextField();
        ds2Field = createStyledTextField();
        ds3Field = createStyledTextField();
        moyenneLabel = createStyledLabel("Moyenne: --");
        mentionLabel = createStyledLabel("Mention: --");

        JLabel etudiantLabel = new JLabel("Étudiant:");
        JLabel ds1Label = new JLabel("DS1:");
        JLabel ds2Label = new JLabel("DS2:");
        JLabel ds3Label = new JLabel("DS3:");

        // Set colors for labels
        etudiantLabel.setForeground(Color.BLACK);
        ds1Label.setForeground(Color.BLACK);
        ds2Label.setForeground(Color.BLACK);
        ds3Label.setForeground(Color.BLACK);

        // Now add components to panel
        addFormComponent(formPanel, etudiantLabel, studentComboBox, gbc, 0);
        addFormComponent(formPanel, ds1Label, ds1Field, gbc, 1);
        addFormComponent(formPanel, ds2Label, ds2Field, gbc, 2);
        addFormComponent(formPanel, ds3Label, ds3Field, gbc, 3);
        addFormComponent(formPanel, moyenneLabel, null, gbc, 4);
        addFormComponent(formPanel, mentionLabel, null, gbc, 5);

        // Add save button
        JButton saveButton = createStyledButton("Enregistrer", new Color(70, 130, 180));
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        formPanel.add(saveButton, gbc);

        // Set selected student if updating
        if (noteData != null) {
            int studentId = (int) noteData[1];
            for (int i = 0; i < studentComboBox.getItemCount(); i++) {
                if (studentComboBox.getItemAt(i).getId() == studentId) {
                    studentComboBox.setSelectedIndex(i);
                    break;
                }
            }
            ds1Field.setText(String.valueOf(noteData[3]));
            ds2Field.setText(String.valueOf(noteData[4]));
            ds3Field.setText(String.valueOf(noteData[5]));
            calculateMoyenneAndMention();
        }

        saveButton.addActionListener(e -> {
            StudentItem selectedStudent = (StudentItem) studentComboBox.getSelectedItem();
            if (selectedStudent != null) {
                currentStudentId = selectedStudent.getId();
                if (noteData == null) {
                    if (saveNote()) {
                        dialog.dispose();
                    }
                } else {
                    if (updateNote((int) noteData[0])) {
                        dialog.dispose();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Veuillez sélectionner un étudiant");
            }
        });

        dialog.add(formPanel);
        dialog.setVisible(true);
    }

    // Add this new class inside Note class
    private static class StudentItem {
        private int id;
        private String name;

        public StudentItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }

        @Override
        public String toString() { return name; }
    }

    private void loadStudentsIntoComboBox(JComboBox<StudentItem> comboBox) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user_management", "root", "");
            String sql = "SELECT id, CONCAT(name, ' ', first_name) as full_name FROM student";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            comboBox.removeAllItems();
            while (rs.next()) {
                comboBox.addItem(new StudentItem(
                        rs.getInt("id"),
                        rs.getString("full_name")
                ));
            }

            conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des étudiants: " + e.getMessage());
        }
    }

    private boolean updateNote(int noteId) {
        try {
            if (!validateForm()) return false;

            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user_management", "root", "");
            String sql = "UPDATE Note SET Ds1=?, Ds2=?, Ds3=? WHERE Id_n=?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setFloat(1, Float.parseFloat(ds1Field.getText()));
            stmt.setFloat(2, Float.parseFloat(ds2Field.getText()));
            stmt.setFloat(3, Float.parseFloat(ds3Field.getText()));
            stmt.setInt(4, noteId);

            int result = stmt.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Note modifiée avec succès!");
                loadNotes();
                return true;
            }
            return false;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
            return false;
        }
    }

    private void updateSelectedNote() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une note à modifier");
            return;
        }

        Object[] noteData = new Object[tableModel.getColumnCount()];
        for (int i = 0; i < noteData.length; i++) {
            noteData[i] = tableModel.getValueAt(selectedRow, i);
        }
        showNoteForm(noteData);
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField(10);
        field.setPreferredSize(new Dimension(100, 30));
        return field;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.BLACK);  // Set label color to black
        return label;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(100, 35));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }

    private void addFormComponent(JPanel panel, Component comp1, Component comp2, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.gridx = 0;
        panel.add(comp1, gbc);
        if (comp2 != null) {
            gbc.gridx = 1;
            panel.add(comp2, gbc);
        }
    }

    private void calculateMoyenneAndMention() {
        try {
            double ds1 = Double.parseDouble(ds1Field.getText());
            double ds2 = Double.parseDouble(ds2Field.getText());
            double ds3 = Double.parseDouble(ds3Field.getText());

            double moyenne = (ds1 + ds2 + ds3) / 3;
            String mention;

            if (moyenne >= 16) mention = "Très Bien";
            else if (moyenne >= 14) mention = "Bien";
            else if (moyenne >= 12) mention = "Assez Bien";
            else if (moyenne >= 10) mention = "Passable";
            else mention = "Insuffisant";

            moyenneLabel.setText(String.format("Moyenne: %.2f", moyenne));
            mentionLabel.setText("Mention: " + mention);
        } catch (NumberFormatException e) {
            moyenneLabel.setText("Moyenne: --");
            mentionLabel.setText("Mention: --");
        }
    }

    private boolean saveNote() {
        try {
            if (!validateForm()) return false;

            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user_management", "root", "");
            String sql = "INSERT INTO Note (id_s, Ds1, Ds2, Ds3) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, currentStudentId);
            stmt.setFloat(2, Float.parseFloat(ds1Field.getText()));
            stmt.setFloat(3, Float.parseFloat(ds2Field.getText()));
            stmt.setFloat(4, Float.parseFloat(ds3Field.getText()));

            int result = stmt.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Note enregistrée avec succès!");
                loadNotes();
                clearFields();
                return true;
            }
            return false;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
            return false;
        }
    }

    private void deleteSelectedNote() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une note à supprimer");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer cette note?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user_management", "root", "");
                String sql = "DELETE FROM Note WHERE Id_n=?";
                PreparedStatement stmt = conn.prepareStatement(sql);

                int noteId = (int) tableModel.getValueAt(selectedRow, 0);
                stmt.setInt(1, noteId);

                int result = stmt.executeUpdate();
                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Note supprimée avec succès!");
                    loadNotes();
                    clearFields();
                }

                conn.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
            }
        }
    }

    private void loadNotes() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user_management", "root", "");
            String sql = "SELECT n.*, s.name, s.first_name FROM Note n " +
                    "JOIN student s ON n.id_s = s.id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("Id_n"),
                        rs.getInt("id_s"),
                        rs.getString("name") + " " + rs.getString("first_name"),
                        rs.getFloat("Ds1"),
                        rs.getFloat("Ds2"),
                        rs.getFloat("Ds3"),
                        rs.getFloat("Moyenne"),
                        rs.getString("Mention")
                });
            }

            conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des notes: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        try {
            if (currentStudentId == -1) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un étudiant");
                return false;
            }
            Float.parseFloat(ds1Field.getText());
            Float.parseFloat(ds2Field.getText());
            Float.parseFloat(ds3Field.getText());
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer des notes valides");
            return false;
        }
    }

    private void clearFields() {
        ds1Field.setText("");
        ds2Field.setText("");
        ds3Field.setText("");
        currentStudentId = -1;
        moyenneLabel.setText("Moyenne: --");
        mentionLabel.setText("Mention: --");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Note().setVisible(true);
        });
    }
}
