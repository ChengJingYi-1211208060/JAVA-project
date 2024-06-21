import javax.swing.*; // JComponent
import java.awt.*; // Layout Manager
import java.awt.event.*; // Event Handling
import java.io.*; // Reading and Writing to a binary file
import java.util.ArrayList;
import java.util.Random;

public class DinnerApp extends JFrame {
    private ArrayList<String> dinners;
    private JTextField dinnerInput;
    private DefaultListModel<String> listModel;
    private JList<String> dinnerList;

    public DinnerApp() {
        dinners = new ArrayList<>();
        loadDinners();

        setTitle("Dinner Manager");
        setLayout(new BorderLayout());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        dinnerInput = new JTextField(10);
        JButton addButton = createButton("add.png");
        JButton deleteButton = createButton("delete.png");
        JButton editButton = createButton("edit.png");
        JButton randomButton = createButton("random.png");

        addButton.addActionListener(new AddDinnerListener());
        deleteButton.addActionListener(new DeleteDinnerListener());
        editButton.addActionListener(new EditDinnerListener());
        randomButton.addActionListener(new RandomDinnerListener());

        listModel = new DefaultListModel<>(); // Helper class
        dinnerList = new JList<>(listModel); // JComponent
        dinnerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Dinner:"));
        inputPanel.add(dinnerInput);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(editButton);
        buttonPanel.add(randomButton);

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
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("dinners.dat"))) {
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
                JOptionPane.showMessageDialog(null, "Random Dinner: " + dinners.get(randomIndex));
            }
        }
    }

    public static void main(String[] args) {
        new DinnerApp();
    }
}
