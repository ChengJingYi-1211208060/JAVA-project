import javax.swing.*; // JComponent
import java.awt.*; // Layout Manager
import java.awt.event.*; // Event Handling
import java.io.*; // Reading and Writing to a binary file
import java.util.ArrayList;
import java.util.Random;


public class DinnerApp extends JFrame {
    private ArrayList<String> dinners;
    private JTextField dinnerInput; // JComponent
    private DefaultListModel<String> listModel; // Helper class for List data management
    private JList<String> dinnerList; // JComponent

    public DinnerApp() {
        dinners = new ArrayList<>();
        loadDinners(); // Reading from binary file

        setTitle("What's Dinner Tonight");
        setLayout(new BorderLayout()); // Layout Manager 1
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        dinnerInput = new JTextField(20); // JComponent
        JButton addButton = createButton("add.png"); // JComponent, ImageIcon 1
        JButton deleteButton = createButton("delete.png"); // JComponent, ImageIcon 2
        JButton editButton = createButton("edit.png"); // JComponent, ImageIcon 3
        JButton randomButton = createButton("random.png"); // JComponent

        // Event Handling
        addButton.addActionListener(new AddDinnerListener()); // Event class 1
        deleteButton.addActionListener(new DeleteDinnerListener()); // Event class 1
        editButton.addActionListener(new EditDinnerListener()); // Event class 1
        randomButton.addActionListener(new RandomDinnerListener()); // Event class 1

        listModel = new DefaultListModel<>(); // Helper class
        dinnerList = new JList<>(listModel); // JComponent
        dinnerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Layout Manager 2
        inputPanel.add(new JLabel("Dinner:")); // JComponent
        inputPanel.add(dinnerInput); // JComponent

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Layout Manager 2
        buttonPanel.add(addButton); // JComponent
        buttonPanel.add(deleteButton); // JComponent
        buttonPanel.add(editButton); // JComponent
        buttonPanel.add(randomButton); // JComponent

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(dinnerList), BorderLayout.CENTER); // JComponent

        refreshDinnerList();
        setVisible(true);
    }

    private JButton createButton(String imagePath) { // Helper method for creating buttons with icons
        ImageIcon icon = new ImageIcon(imagePath); // ImageIcon
        Image image = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        JButton button = new JButton(new ImageIcon(image)); // JComponent
        button.setPreferredSize(new Dimension(50, 50));
        button.setMaximumSize(new Dimension(50, 50));
        button.setMinimumSize(new Dimension(50, 50));
        button.setText(null);
        return button;
    }

    @SuppressWarnings("unchecked")
    private void loadDinners() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("dinners.txt"))) { //binary file
            dinners = (ArrayList<String>) ois.readObject();
        } catch (Exception e) {
            dinners = new ArrayList<>();
        }
    }

    private void saveDinners() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("dinners.txt"))) { //binary file
            oos.writeObject(dinners);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshDinnerList() {
        listModel.clear();
        for (String dinner : dinners) {
            listModel.addElement(dinner);
        }
    }

    // Event Handlers (Event class 2)
    private class AddDinnerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String dinner = dinnerInput.getText();
            if (!dinner.isEmpty()) {
                dinners.add(dinner);
                refreshDinnerList();
                saveDinners();
                dinnerInput.setText("");
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
                String newDinner = JOptionPane.showInputDialog("Edit Dinner", dinners.get(selectedIndex));
                if (newDinner != null && !newDinner.isEmpty()) {
                    dinners.set(selectedIndex, newDinner);
                    refreshDinnerList();
                    saveDinners();
                }
            }
        }
    }

    private class RandomDinnerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!dinners.isEmpty()) {
                Random rand = new Random();
                int randomIndex = rand.nextInt(dinners.size());
                JOptionPane.showMessageDialog(null, "Dinner Tonight: " + dinners.get(randomIndex));
            }
        }
    }

    public static void main(String[] args) {
        new DinnerApp();
    }
}
