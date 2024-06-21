import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class Dinner implements Serializable {
    private String name;
    private String description;

    public Dinner(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name;
    }
}

public class DinnerApp extends JFrame {
    private ArrayList<Dinner> dinners;
    private JTextField dinnerInput;
    private JTextField descriptionInput;
    private DefaultListModel<Dinner> listModel;
    private JList<Dinner> dinnerList;
    private String currentUser;
    private Map<String, String> users;
    private JButton addButton, deleteButton, editButton, loginButton, registerButton;

    public DinnerApp() {
        users = new HashMap<>();
        loadUsers();
        dinners = new ArrayList<>();
        loadDinners();  // Load dinners from the centralized file
        setTitle("Diner Manager");
        setLayout(new BorderLayout());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
        refreshDinnerList();
        setVisible(true);
    }

    private void initUI() {
        dinnerInput = new JTextField(10);
        descriptionInput = new JTextField(20);
        addButton = createButton("add.png");
        deleteButton = createButton("delete.png");
        editButton = createButton("edit.png");
        JButton randomButton = createButton("random.png");
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        addButton.addActionListener(new AddDinnerListener());
        deleteButton.addActionListener(new DeleteDinnerListener());
        editButton.addActionListener(new EditDinnerListener());
        randomButton.addActionListener(new RandomDinnerListener());
        loginButton.addActionListener(new LoginListener());
        registerButton.addActionListener(new RegisterListener());

        listModel = new DefaultListModel<>();
        dinnerList = new JList<>(listModel);
        dinnerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dinnerList.addMouseListener(new DinnerMouseListener());

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Diner:"));
        inputPanel.add(dinnerInput);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descriptionInput);

        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(editButton);
        buttonPanel.add(randomButton);

        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.WEST);
        add(new JScrollPane(dinnerList), BorderLayout.CENTER);

        updateButtonsState();
    }

    private JButton createButton(String imagePath) {
        ImageIcon icon = new ImageIcon(imagePath);
        Image image = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        JButton button = new JButton(new ImageIcon(image));
        button.setPreferredSize(new Dimension(50, 50));
        button.setMaximumSize(new Dimension(50, 50));
        button.setMinimumSize(new Dimension(50, 50));
        button.setText(null);
        return button;
    }

    private void loadDinners() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("dinners.dat"))) {
            dinners = (ArrayList<Dinner>) ois.readObject();
        } catch (Exception e) {
            dinners = new ArrayList<>();
        }
    }

    private void saveDinners() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("dinners.dat"))) {
            oos.writeObject(dinners);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUsers() {
        try (BufferedReader br = new BufferedReader(new FileReader("admin.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUsers() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("admin.txt"))) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                bw.write(entry.getKey() + ":" + entry.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshDinnerList() {
        listModel.clear();
        for (Dinner dinner : dinners) {
            listModel.addElement(dinner);
        }
    }

    private void updateButtonsState() {
        boolean loggedIn = currentUser != null;
        addButton.setEnabled(loggedIn);
        deleteButton.setEnabled(loggedIn);
        editButton.setEnabled(loggedIn);
        loginButton.setText(loggedIn ? "Logout" : "Login");
        registerButton.setVisible(!loggedIn); // Hide register button if logged in
    }

    private class AddDinnerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String dinnerName = dinnerInput.getText();
            String description = descriptionInput.getText();
            if (!dinnerName.isEmpty()) {
                Dinner dinner = new Dinner(dinnerName, description);
                dinners.add(dinner);
                refreshDinnerList();
                saveDinners();
                dinnerInput.setText("");
                descriptionInput.setText("");
            }
        }
    }

    private class DeleteDinnerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int selectedIndex = dinnerList.getSelectedIndex();
            if (selectedIndex != -1) {
                dinners.remove(selectedIndex);
                refreshDinnerList();
                saveDinners();
            }
        }
    }

    private class EditDinnerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int selectedIndex = dinnerList.getSelectedIndex();
            if (selectedIndex != -1) {
                Dinner dinner = dinners.get(selectedIndex);
                JTextField dinnerField = new JTextField(dinner.getName(), 10);
                JTextField descriptionField = new JTextField(dinner.getDescription(), 20);

                JPanel panel = new JPanel(new GridLayout(2, 2));
                panel.add(new JLabel("Diner:"));
                panel.add(dinnerField);
                panel.add(new JLabel("Description:"));
                panel.add(descriptionField);

                int result = JOptionPane.showConfirmDialog(null, panel, "Edit Dinner", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String newDinnerName = dinnerField.getText();
                    String newDescription = descriptionField.getText();
                    if (!newDinnerName.isEmpty()) {
                        dinners.set(selectedIndex, new Dinner(newDinnerName, newDescription));
                        refreshDinnerList();
                        saveDinners();
                    }
                }
            }
        }
    }

    private class RandomDinnerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!dinners.isEmpty()) {
                Random rand = new Random();
                int randomIndex = rand.nextInt(dinners.size());
                Dinner dinner = dinners.get(randomIndex);
                JOptionPane.showMessageDialog(null, "Random Dinner: " + dinner.getName() + "\nDescription: " + dinner.getDescription());
            }
        }
    }

    private class LoginListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (currentUser != null) {
                int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to logout ? ");
                if (confirm == JOptionPane.YES_OPTION) {
                currentUser = null;
                updateButtonsState();
                JOptionPane.showMessageDialog(null, "Logged out successfully.");
                }
            } else {
                JTextField usernameField = new JTextField(10);
                JPasswordField passwordField = new JPasswordField(10);

                JPanel panel = new JPanel(new GridLayout(3, 2));
                panel.add(new JLabel("Username:"));
                panel.add(usernameField);
                panel.add(new JLabel("Password:"));
                panel.add(passwordField);

                int result = JOptionPane.showConfirmDialog(null, panel, "Login", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String username = usernameField.getText();
                    String password = new String(passwordField.getPassword());
                    if (users.containsKey(username)) {
                        if (users.get(username).equals(password)) {
                            currentUser = username;
                        } else {
                            JOptionPane.showMessageDialog(null, "Incorrect password. Please try again.");
                            actionPerformed(e); // Reopen login dialog
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Username not found. Please register first.");
                    }
                    updateButtonsState();
                }
            }
        }
    }

    private class RegisterListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JTextField usernameField = new JTextField(10);
            JPasswordField passwordField = new JPasswordField(10);

            JPanel panel = new JPanel(new GridLayout(3, 2));
            panel.add(new JLabel("New Username:"));
            panel.add(usernameField);
            panel.add(new JLabel("New Password:"));
            panel.add(passwordField);

            int result = JOptionPane.showConfirmDialog(null, panel, "Register", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Username and password cannot be empty.");
                    return;
                }
                if (users.containsKey(username)) {
                    JOptionPane.showMessageDialog(null, "Username already exists. Please choose a different one.");
                } else {
                    users.put(username, password);
                    saveUsers();
                    JOptionPane.showMessageDialog(null, "User registered successfully.");
                }
            }
        }
    }

    private class DinnerMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                int index = dinnerList.locationToIndex(e.getPoint());
                if (index != -1) {
                    Dinner dinner = dinners.get(index);
                    JOptionPane.showMessageDialog(null, "Dinner Information: " + dinner.getName() + "\nDescription: " + dinner.getDescription());
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DinnerApp());
    }
}

