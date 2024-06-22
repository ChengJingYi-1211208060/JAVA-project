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
    private JLabel welcomeLabel;
    private static final Dinner[] defaultDinners = {
        new Dinner("McDonalds", "Fast food chain"),
        new Dinner("KFC", "Fast food chain specializing in fried chicken"),
        new Dinner("Pizza Hut", "Pizza restaurant chain")
    };

    public DinnerApp() {
        users = new HashMap<>();
        loadUsers();
        dinners = new ArrayList<>();
        setTitle("Diner Manager");
        setLayout(new BorderLayout());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
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

        welcomeLabel = new JLabel();
        updateWelcomeLabel();

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Diner:"));
        inputPanel.add(dinnerInput);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descriptionInput);

        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.add(welcomeLabel);

        JPanel combinedPanel = new JPanel(new BorderLayout());
        combinedPanel.add(inputPanel, BorderLayout.NORTH);
        combinedPanel.add(welcomePanel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(editButton);
        buttonPanel.add(randomButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(combinedPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(dinnerList), BorderLayout.CENTER);

        add(buttonPanel, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        updateButtonsState();
        loadDefaultDinners();
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

    private void loadDefaultDinners() {
        if (currentUser == null) {
            dinners.clear();
            for (Dinner dinner : defaultDinners) {
                dinners.add(dinner);
            }
            refreshDinnerList();
        }
    }

    private void loadDinners() {
        if (currentUser != null) {
            File userFile = new File(currentUser + "_dinners.dat");
            if (userFile.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(userFile))) {
                    dinners = (ArrayList<Dinner>) ois.readObject();
                } catch (Exception e) {
                    e.printStackTrace();
                    dinners = new ArrayList<>();
                }
            } else {
                dinners = new ArrayList<>();
            }
        } else {
            loadDefaultDinners();
        }
    }

    private void saveDinners() {
        if (currentUser != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(currentUser + "_dinners.dat"))) {
                oos.writeObject(dinners);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        updateWelcomeLabel();
        if (!loggedIn) {
            loadDefaultDinners();
        }
    }

    private void updateWelcomeLabel() {
        if (currentUser == null) {
            welcomeLabel.setText("Welcome! Please log in to manage your diners.");
        } else {
            welcomeLabel.setText("Welcome, " + currentUser + "! What dinner would you like to eat today?");
        }
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
                int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this dinner?", "Delete Confirmation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dinners.remove(selectedIndex);
                    refreshDinnerList();
                    saveDinners();
                }
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
                Random random = new Random();
                int randomIndex = random.nextInt(dinners.size());
                Dinner randomDinner = dinners.get(randomIndex);
                JOptionPane.showMessageDialog(null, "Random Diner: " + randomDinner.getName() + "\nDescription: " + randomDinner.getDescription());
            }
        }
    }

    private class LoginListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (currentUser != null) {
                int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to log out?", "Logout Confirmation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    currentUser = null;
                    loadDefaultDinners();
                    updateButtonsState();
                    JOptionPane.showMessageDialog(null, "Logged out successfully.");
                }
            } else {
                JTextField usernameField = new JTextField(10);
                JPasswordField passwordField = new JPasswordField(10);

                JPanel panel = new JPanel(new GridLayout(2, 2));
                panel.add(new JLabel("Username:"));
                panel.add(usernameField);
                panel.add(new JLabel("Password:"));
                panel.add(passwordField);

                int result = JOptionPane.showConfirmDialog(null, panel, "Login", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String username = usernameField.getText();
                    String password = new String(passwordField.getPassword());
                    if (users.containsKey(username) && users.get(username).equals(password)) {
                        currentUser = username;
                        loadDinners();
                        updateButtonsState();
                        refreshDinnerList();
                        JOptionPane.showMessageDialog(null, "Login successful.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid username or password.", "Login Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private class RegisterListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JTextField usernameField = new JTextField(10);
            JPasswordField passwordField = new JPasswordField(10);

            JPanel panel = new JPanel(new GridLayout(2, 2));
            panel.add(new JLabel("Username:"));
            panel.add(usernameField);
            panel.add(new JLabel("Password:"));
            panel.add(passwordField);

            int result = JOptionPane.showConfirmDialog(null, panel, "Register", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                if (!username.isEmpty() && !password.isEmpty()) {
                    if (!users.containsKey(username)) {
                        users.put(username, password);
                        saveUsers();
                        JOptionPane.showMessageDialog(null, "Registration successful.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Username already exists.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Username or password cannot be empty.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class DinnerMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                int selectedIndex = dinnerList.getSelectedIndex();
                if (selectedIndex != -1) {
                    Dinner dinner = listModel.getElementAt(selectedIndex);
                    JOptionPane.showMessageDialog(null, "Diner: " + dinner.getName() + "\nDescription: " + dinner.getDescription());
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DinnerApp::new);
    }
}
