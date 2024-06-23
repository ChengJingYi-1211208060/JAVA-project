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

class DinnerApp extends JFrame {
    private ArrayList<Dinner> dinners;
    private JTextField dinnerInput;
    private JTextField descriptionInput;
    private DefaultListModel<Dinner> listModel;
    private JList<Dinner> dinnerList;
    private String currentUser;
    private Map<String, String> users;
    private JButton addButton, deleteButton, editButton, randomButton, logoutButton;
    private JLabel welcomeLabel;
    private static final Dinner[] defaultDinners = {
        new Dinner("McDonalds", "Fast food restaurant"),
        new Dinner("KFC", "Fast food restaurant that specializing in fried chicken"),
        new Dinner("Pizza Hut", "Pizza restaurant ")
    };

    public DinnerApp() {
        users = new HashMap<>();
        loadUsers();
        dinners = new ArrayList<>();
        setTitle("Dinner Manager");
        setLayout(new BorderLayout());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize UI Components
        initUI();

        // Show login dialog at startup
        showLoginDialog();
    }

    private void initUI() {
        dinnerInput = new JTextField(10);
        descriptionInput = new JTextField(20);
        addButton = createButton("add.png");
        deleteButton = createButton("delete.png");
        editButton = createButton("edit.png");
        randomButton = createButton("random.png");
        logoutButton = new JButton("Logout");

        addButton.addActionListener(new AddDinnerListener());
        deleteButton.addActionListener(new DeleteDinnerListener());
        editButton.addActionListener(new EditDinnerListener());
        randomButton.addActionListener(new RandomDinnerListener());
        logoutButton.addActionListener(new LogoutListener());

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

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(editButton);
        buttonPanel.add(randomButton);
        buttonPanel.add(logoutButton);

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
        randomButton.setEnabled(loggedIn);
        logoutButton.setEnabled(loggedIn);
        updateWelcomeLabel();
        if (!loggedIn) {
            loadDefaultDinners();
        }
    }

    private void updateWelcomeLabel() {
        if (currentUser == null) {
            welcomeLabel.setText("Welcome! Please log in to manage your dinners.");
        } else {
            welcomeLabel.setText("Welcome, " + currentUser + "! What dinner would you like to eat today?");
        }
    }

    //login
    private void showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "Login", true);
        loginDialog.setLayout(new GridLayout(3, 2));
        loginDialog.setSize(300, 150);

        JTextField usernameField = new JTextField(10);
        JPasswordField passwordField = new JPasswordField(10);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        loginDialog.add(new JLabel("Username:"));
        loginDialog.add(usernameField);
        loginDialog.add(new JLabel("Password:"));
        loginDialog.add(passwordField);
        loginDialog.add(loginButton);
        loginDialog.add(registerButton);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (users.containsKey(username) && users.get(username).equals(password)) {
                currentUser = username;
                loadDinners();
                updateButtonsState();
                refreshDinnerList();
                loginDialog.dispose();
                JOptionPane.showMessageDialog(this, "Login successful.");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            loginDialog.dispose();
            showRegisterDialog();
        });

        loginDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setVisible(true);
    }

    //register
    private void showRegisterDialog() {
        JDialog registerDialog = new JDialog(this, "Register", true);
        registerDialog.setLayout(new GridLayout(3, 2));
        registerDialog.setSize(300, 150);

        JTextField usernameField = new JTextField(10);
        JPasswordField passwordField = new JPasswordField(10);

        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");

        registerDialog.add(new JLabel("Username:"));
        registerDialog.add(usernameField);
        registerDialog.add(new JLabel("Password:"));
        registerDialog.add(passwordField);
        registerDialog.add(registerButton);
        registerDialog.add(cancelButton);

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (!username.isEmpty() && !password.isEmpty()) {
                if (!users.containsKey(username)) {
                    users.put(username, password);
                    saveUsers();
                    registerDialog.dispose();
                    JOptionPane.showMessageDialog(this, "Registration successful. Please log in.");
                    showLoginDialog();
                } else {
                    JOptionPane.showMessageDialog(this, "Username already exists.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> {
            registerDialog.dispose();
            showLoginDialog();
        });

        registerDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        registerDialog.setLocationRelativeTo(this);
        registerDialog.setVisible(true);
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
                Dinner selectedDinner = dinners.get(selectedIndex);
                String newName = JOptionPane.showInputDialog(null, "Enter new name:", selectedDinner.getName());
                String newDescription = JOptionPane.showInputDialog(null, "Enter new description:", selectedDinner.getDescription());
                if (newName != null && newDescription != null) {
                    selectedDinner = new Dinner(newName, newDescription);
                    dinners.set(selectedIndex, selectedDinner);
                    refreshDinnerList();
                    saveDinners();
                }
            }
        }
    }

    private class RandomDinnerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!dinners.isEmpty()) {
                Random random = new Random();
                int index = random.nextInt(dinners.size());
                Dinner randomDinner = dinners.get(index);
                JOptionPane.showMessageDialog(null, "Random dinner: " + randomDinner.getName() + "\nDescription: " + randomDinner.getDescription());
            }
        }
    }

    //logout
    private class LogoutListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            currentUser = null;
            getContentPane().removeAll(); // Clear the main frame content
            repaint(); // Refresh the frame
            revalidate(); // Revalidate the layout
            showLoginDialog(); // Show the login dialog
        }
    }

    private class DinnerMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                int index = dinnerList.locationToIndex(e.getPoint());
                Dinner selectedDinner = dinnerList.getModel().getElementAt(index);
                JOptionPane.showMessageDialog(null, "Dinner: " + selectedDinner.getName() + "\nDescription: " + selectedDinner.getDescription());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DinnerApp().setVisible(true));
    }
}
