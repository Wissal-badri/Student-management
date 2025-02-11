import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StudentManagement extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField nomField, prenomField, emailField, adresseField, filiereField;
    private JComboBox<String> genreBox;
    private JTextField dateNaissanceField;

    public StudentManagement() {
        setTitle("Gestion des Étudiants");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Tableau
        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new Object[]{"ID", "Nom", "Prénom", "Email", "Adresse", "Date Naissance", "Filière", "Genre"});
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Boutons
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Ajouter");
        JButton updateButton = new JButton("Modifier");
        JButton deleteButton = new JButton("Supprimer");
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Actions des boutons
        addButton.addActionListener(e -> showForm(null));
        updateButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                Object[] studentData = new Object[tableModel.getColumnCount()];
                for (int i = 0; i < studentData.length; i++) {
                    studentData[i] = tableModel.getValueAt(selectedRow, i);
                }
                showForm(studentData);
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une ligne à modifier.");
            }
        });

        deleteButton.addActionListener(e -> deleteStudent());

        add(mainPanel);
        loadStudents();
        setVisible(true);
    }

    // Méthode pour afficher la fenêtre principale
    public void showManagement() {
        this.setVisible(true);
    }

    private void showForm(Object[] studentData) {
        JDialog dialog = new JDialog(this, "Formulaire", true);
        dialog.setSize(400, 600);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Champs du formulaire
        JLabel nomLabel = new JLabel("Nom :");
        nomField = new JTextField(20);
        JLabel prenomLabel = new JLabel("Prénom :");
        prenomField = new JTextField(20);
        JLabel emailLabel = new JLabel("Email :");
        emailField = new JTextField(20);
        JLabel adresseLabel = new JLabel("Adresse :");
        adresseField = new JTextField(20);
        JLabel dateNaissanceLabel = new JLabel("Date de Naissance (YYYY-MM-DD) :");
        dateNaissanceField = new JTextField(20);
        JLabel filiereLabel = new JLabel("Filière :");
        filiereField = new JTextField(20);
        JLabel genreLabel = new JLabel("Genre :");
        genreBox = new JComboBox<>(new String[]{"Masculin", "Féminin"});

        if (studentData != null) {
            nomField.setText((String) studentData[1]);
            prenomField.setText((String) studentData[2]);
            emailField.setText((String) studentData[3]);
            adresseField.setText((String) studentData[4]);
            dateNaissanceField.setText((String) studentData[5]);
            filiereField.setText((String) studentData[6]);
            genreBox.setSelectedItem(studentData[7]);
        }

        // Ajouter les champs au formulaire
        addFormField(formPanel, nomLabel, nomField, gbc, 0);
        addFormField(formPanel, prenomLabel, prenomField, gbc, 1);
        addFormField(formPanel, emailLabel, emailField, gbc, 2);
        addFormField(formPanel, adresseLabel, adresseField, gbc, 3);
        addFormField(formPanel, dateNaissanceLabel, dateNaissanceField, gbc, 4);
        addFormField(formPanel, filiereLabel, filiereField, gbc, 5);
        addFormField(formPanel, genreLabel, genreBox, gbc, 6);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> {
            if (saveStudent(studentData == null ? -1 : (int) studentData[0])) {
                dialog.dispose();
            }
        });
        dialog.add(saveButton, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addFormField(JPanel panel, JLabel label, JComponent field, GridBagConstraints gbc, int gridy) {
        gbc.gridx = 0;
        gbc.gridy = gridy;
        panel.add(label, gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private boolean saveStudent(int id) {
        if (!validateForm()) {
            return false;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user_management", "root", "")) {
            PreparedStatement stmt;
            String sql = id == -1 ?
                    "INSERT INTO student (name, first_name, email, address, datenaissance, filiere, genre) VALUES (?, ?, ?, ?, ?, ?, ?)" :
                    "UPDATE student SET name = ?, first_name = ?, email = ?, address = ?, datenaissance = ?, filiere = ?, genre = ? WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            if (id != -1) {
                stmt.setInt(8, id);
            }

            stmt.setString(1, nomField.getText());
            stmt.setString(2, prenomField.getText());
            stmt.setString(3, emailField.getText());
            stmt.setString(4, adresseField.getText());
            stmt.setString(5, dateNaissanceField.getText());
            stmt.setString(6, filiereField.getText());
            stmt.setString(7, (String) genreBox.getSelectedItem());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Étudiant enregistré avec succès !");
                loadStudents();
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Échec de l'enregistrement.");
                return false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'accès à la base de données : " + e.getMessage());
            return false;
        }
    }

    private boolean validateForm() {
        if (nomField.getText().trim().isEmpty() ||
                prenomField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                adresseField.getText().trim().isEmpty() ||
                dateNaissanceField.getText().trim().isEmpty() ||
                filiereField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tous les champs sont obligatoires.");
            return false;
        }

        // Validation du format de la date
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            sdf.parse(dateNaissanceField.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Le format de la date doit être YYYY-MM-DD");
            return false;
        }

        return true;
    }

    private void deleteStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un étudiant à supprimer.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer cet étudiant ?",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user_management", "root", "")) {
                String sql = "DELETE FROM student WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, id);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Étudiant supprimé avec succès !");
                    loadStudents();
                } else {
                    JOptionPane.showMessageDialog(this, "Échec de la suppression.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'accès à la base de données : " + e.getMessage());
            }
        }
    }

    private void loadStudents() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user_management", "root", "")) {
            String sql = "SELECT id, name, first_name, email, address, datenaissance, filiere, genre FROM student";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            tableModel.setRowCount(0);

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("first_name"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("datenaissance"),
                        rs.getString("filiere"),
                        rs.getString("genre")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des données : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            // Set cross-platform look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new StudentManagement();
        });
    }
}