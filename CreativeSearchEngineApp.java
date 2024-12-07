import javax.swing.*;

import java.awt.*;

import java.awt.event.*;

import java.io.*;

import java.util.*;

import java.util.regex.*;



public class CreativeSearchEngineApp extends JFrame {

    private final JTextField searchField;

    private final JTextArea resultArea;

    private final Map<String, Map<String, Integer>> index; // Index to store word count in files

    private final DefaultListModel<String> searchHistoryModel;

    private final JList<String> searchHistoryList;



    public CreativeSearchEngineApp() {

        // Initialize the search engine index

        index = new HashMap<>();

        searchHistoryModel = new DefaultListModel<>();

        searchHistoryList = new JList<>(searchHistoryModel);



        // Set up the frame

        setTitle("Creative Dynamic Search Engine");

        setSize(1000, 700);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());



        // Set background color and font

        getContentPane().setBackground(new Color(245, 245, 255));

        Font customFont = new Font("Arial", Font.PLAIN, 14);

        UIManager.put("Label.font", customFont);



        // Create the top panel with buttons and search input

        JPanel topPanel = new JPanel();

        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        topPanel.setBackground(new Color(70, 130, 180));



        JButton addFileButton = new JButton("Add File");

        addFileButton.setBackground(new Color(102, 204, 255));

        addFileButton.setForeground(Color.WHITE);

        addFileButton.addActionListener(e -> addFileToIndex());



        JButton addFolderButton = new JButton("Add Folder");

        addFolderButton.setBackground(new Color(102, 204, 255));

        addFolderButton.setForeground(Color.WHITE);

        addFolderButton.addActionListener(e -> addFolderToIndex());



        JLabel searchLabel = new JLabel("Search:");

        searchLabel.setForeground(new Color(255, 255, 255));

        searchField = new JTextField(25);

        JButton searchButton = new JButton("Search");

        searchButton.setBackground(new Color(255, 140, 0));

        searchButton.setForeground(Color.WHITE);

        searchButton.addActionListener(e -> performSearch());



        // History panel

        JPanel historyPanel = new JPanel();

        historyPanel.setLayout(new BorderLayout());

        historyPanel.setBackground(new Color(240, 240, 255));

        JLabel historyLabel = new JLabel("Search History");

        historyLabel.setForeground(new Color(70, 130, 180));

        historyPanel.add(historyLabel, BorderLayout.NORTH);

        historyPanel.add(new JScrollPane(searchHistoryList), BorderLayout.CENTER);



        // Customize top panel layout

        topPanel.add(addFileButton);

        topPanel.add(addFolderButton);

        topPanel.add(searchLabel);

        topPanel.add(searchField);

        topPanel.add(searchButton);



        // Create the result area

        resultArea = new JTextArea();

        resultArea.setEditable(false);

        resultArea.setLineWrap(true);

        resultArea.setBackground(new Color(255, 255, 255));

        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        resultArea.setForeground(new Color(50, 50, 50));

        JScrollPane resultScrollPane = new JScrollPane(resultArea);



        // Add components to the frame

        JPanel mainPanel = new JPanel();

        mainPanel.setLayout(new GridLayout(1, 2, 10, 10));

        mainPanel.add(historyPanel);

        mainPanel.add(resultScrollPane);



        add(topPanel, BorderLayout.NORTH);

        add(mainPanel, BorderLayout.CENTER);



        // Add a status bar at the bottom

        JLabel statusBar = new JLabel("Welcome to the Creative Search Engine!", JLabel.CENTER);

        statusBar.setFont(new Font("Arial", Font.ITALIC, 12));

        statusBar.setBackground(new Color(70, 130, 180));

        statusBar.setForeground(Color.WHITE);

        statusBar.setOpaque(true);

        add(statusBar, BorderLayout.SOUTH);



        // Make the frame visible

        setVisible(true);

    }



    private void addFileToIndex() {

        JFileChooser fileChooser = new JFileChooser();

        int returnValue = fileChooser.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {

            File selectedFile = fileChooser.getSelectedFile();

            try {

                indexFile(selectedFile);

            } catch (IOException e) {

                resultArea.setText("Error indexing file: " + e.getMessage());

            }

        }

    }



    private void addFolderToIndex() {

        JFileChooser folderChooser = new JFileChooser();

        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnValue = folderChooser.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {

            File selectedFolder = folderChooser.getSelectedFile();

            indexFolder(selectedFolder);

        }

    }



    private void indexFile(File file) throws IOException {

        if (file == null || !file.exists()) {

            throw new FileNotFoundException("File not found: " + file);

        }



        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            StringBuilder content = new StringBuilder();

            String line;

            while ((line = br.readLine()) != null) {

                content.append(line).append(" ");

            }

            indexDocument(file.getName(), content.toString());

            resultArea.setText("File indexed: " + file.getName());

        }

    }



    private void indexFolder(File folder) {

        if (folder == null || !folder.exists()) {

            resultArea.setText("Folder not found: " + folder);

            return;

        }



        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));

        if (files != null) {

            for (File file : files) {

                try {

                    indexFile(file);

                } catch (IOException e) {

                    resultArea.setText("Error indexing file: " + file.getName() + " - " + e.getMessage());

                }

            }

            resultArea.setText("Folder indexed: " + folder.getName());

        } else {

            resultArea.setText("No text files found in folder.");

        }

    }



    private void indexDocument(String docId, String content) {

        String[] words = content.split("\\W+");

        for (String word : words) {

            word = word.toLowerCase();

            index.putIfAbsent(word, new HashMap<>());

            Map<String, Integer> postings = index.get(word);

            postings.put(docId, postings.getOrDefault(docId, 0) + 1);

        }

    }



    private void performSearch() {

        String query = searchField.getText().toLowerCase().trim();

        if (query.isEmpty()) {

            resultArea.setText("Please enter a search term.");

            return;

        }



        Map<String, Integer> results = index.getOrDefault(query, Collections.emptyMap());

        if (results.isEmpty()) {

            resultArea.setText("No results found for: " + query);

            return;

        }



        // Sort and display results

        ArrayList<Map.Entry<String, Integer>> sortedResults = new ArrayList<>(results.entrySet());

        sortedResults.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));



        StringBuilder sb = new StringBuilder();

        sb.append("Results for '").append(query).append("':\n\n");

        for (Map.Entry<String, Integer> entry : sortedResults) {

            sb.append(entry.getKey()).append(" (").append(entry.getValue()).append(" occurrences)\n");

        }

        resultArea.setText(sb.toString());



        // Save the search query to history

        searchHistoryModel.addElement(query);

    }



    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            new CreativeSearchEngineApp(); // Run the app

        });

    }

}