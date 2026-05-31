import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class Main extends JFrame {

    // --- Data Models ---
    private final ArrayList<StudentEntry> studentList = new ArrayList<>();
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private DefaultTableModel summaryTableModel;

    // --- UI Controls ---
    private JLabel lblTotalStudents, lblAverage, lblHighest, lblLowest;
    private JTextField txtStudentName, txtGrade;
    private JList<String> gradeLogList;
    private ChartPanel chartPanel;

    private final String NAME_PROMPT = "Student Name (e.g., Alex)";
    private final String GRADE_PROMPT = "Grade (0 - 100)";

    // ✨ Palette: Pastel Cherry Blossom & White
    private final Color COLOR_BG_MAIN = new Color(255, 240, 242);     // Soft Pastel Pink
    private final Color COLOR_BG_CARD = Color.WHITE;                  // Crisp Snow White Cards
    private final Color COLOR_ACCENT = new Color(216, 27, 96);        // Deep Berry Pink / Rose Accent
    private final Color COLOR_TEXT_MAIN = new Color(66, 50, 54);      // Dark Cocoa/Charcoal for High Contrast
    private final Color COLOR_TEXT_MUTED = new Color(160, 140, 145);  // Soft Muted Rose Gray for Placeholders
    private final Color COLOR_BORDER = new Color(242, 215, 219);      // Gentle Pinkish-Gray Border

    public Main() {
        setTitle("CodeAlpha | Premium Grade Dashboard");
        setSize(1100, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG_MAIN);
        setLayout(new BorderLayout());

        // 1. --- SIDEBAR (CONTROL PANEL) ---
        JPanel sidebar = new JPanel();
        sidebar.setBackground(COLOR_BG_CARD);
        sidebar.setPreferredSize(new Dimension(290, 720));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, COLOR_BORDER));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(25, 20, 25, 20));

        JLabel sidebarTitle = new JLabel("Grade Control Panel");
        sidebarTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sidebarTitle.setForeground(COLOR_TEXT_MAIN);
        sidebarTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtStudentName = createCustomTextField(NAME_PROMPT);
        txtGrade = createCustomTextField(GRADE_PROMPT);

        JButton btnAdd = new JButton("Add Entry");
        styleButton(btnAdd, COLOR_ACCENT, Color.WHITE, true);
        btnAdd.addActionListener(e -> handleAddGrade());

        JButton btnReset = new JButton("Reset All Data");
        styleButton(btnReset, COLOR_BG_CARD, new Color(217, 48, 37), false);
        btnReset.setBorder(new LineBorder(new Color(217, 48, 37), 1, true));
        btnReset.addActionListener(e -> handleReset());

        JLabel lblLogTitle = new JLabel("Audit Activity Log");
        lblLogTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLogTitle.setForeground(COLOR_TEXT_MUTED);
        lblLogTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        gradeLogList = new JList<>(listModel);
        gradeLogList.setBackground(new Color(253, 250, 251));
        gradeLogList.setForeground(COLOR_TEXT_MAIN);
        gradeLogList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane logScrollPane = new JScrollPane(gradeLogList);
        logScrollPane.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        logScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(sidebarTitle);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(txtStudentName);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(txtGrade);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebar.add(btnAdd);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnReset);
        sidebar.add(Box.createRigidArea(new Dimension(0, 25)));
        sidebar.add(lblLogTitle);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(logScrollPane);

        add(sidebar, BorderLayout.WEST);

        // 2. --- MAIN CONTAINER AREA ---
        JPanel mainContent = new JPanel(new BorderLayout(0, 20));
        mainContent.setBackground(COLOR_BG_MAIN);
        mainContent.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel mainTitle = new JLabel("STUDENT PERFORMANCE DASHBOARD");
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        mainTitle.setForeground(COLOR_TEXT_MAIN);
        mainContent.add(mainTitle, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 20));
        centerWrapper.setBackground(COLOR_BG_MAIN);

        // --- KPI TOP CARDS ROW ---
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        kpiPanel.setBackground(COLOR_BG_MAIN);

        JPanel cardTotal = createKPICard("TOTAL STUDENTS", "0", new Color(136, 14, 79));  // Deep Magenta
        JPanel cardAvg = createKPICard("CLASS AVERAGE", "0.0%", new Color(46, 125, 50)); // Emerald Green
        JPanel cardHigh = createKPICard("HIGHEST MARK", "0.0%", new Color(230, 81, 0));  // Dark Amber
        JPanel cardLow = createKPICard("LOWEST MARK", "0.0%", new Color(198, 40, 40));   // Deep Red

        lblTotalStudents = (JLabel) cardTotal.getClientProperty("valueLabel");
        lblAverage = (JLabel) cardAvg.getClientProperty("valueLabel");
        lblHighest = (JLabel) cardHigh.getClientProperty("valueLabel");
        lblLowest = (JLabel) cardLow.getClientProperty("valueLabel");

        kpiPanel.add(cardTotal);
        kpiPanel.add(cardAvg);
        kpiPanel.add(cardHigh);
        kpiPanel.add(cardLow);
        centerWrapper.add(kpiPanel, BorderLayout.NORTH);

        // --- DATA INTERFACE SPLIT (Table Top, Compact Graph Bottom) ---
        JPanel splitWorkspace = new JPanel(new GridLayout(2, 1, 0, 20));
        splitWorkspace.setBackground(COLOR_BG_MAIN);

        // A. Table Grid View Panel
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(COLOR_BG_CARD);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(15, 15, 15, 15)));

        JLabel tableLabel = new JLabel("Ranked Performance Summary Statement");
        tableLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableLabel.setForeground(COLOR_TEXT_MAIN);
        tableLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        tableContainer.add(tableLabel, BorderLayout.NORTH);

     
        String[] headers = {"S.No.", "Student Name", "Marks Obtained", "Class Position"};
        summaryTableModel = new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(summaryTableModel);
        styleSummaryTable(table);
        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);
        splitWorkspace.add(tableContainer);


        chartPanel = new ChartPanel();
        splitWorkspace.add(chartPanel);

        centerWrapper.add(splitWorkspace, BorderLayout.CENTER);
        mainContent.add(centerWrapper, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);
    }

    private JTextField createCustomTextField(String prompt) {
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setPreferredSize(new Dimension(240, 40));
        field.setBackground(new Color(253, 245, 246));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(0, 12, 0, 12)
        ));

        field.setText(prompt);
        field.setForeground(COLOR_TEXT_MUTED);
        field.setCaretColor(COLOR_ACCENT);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (field.getText().equals(prompt)) {
                    field.setText("");
                    field.setForeground(COLOR_TEXT_MAIN);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getText().isEmpty()) {
                    field.setText(prompt);
                    field.setForeground(COLOR_TEXT_MUTED);
                }
            }
        });
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private void styleButton(JButton btn, Color bg, Color fg, boolean opaque) {
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(240, 40));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setOpaque(opaque);
        btn.setContentAreaFilled(opaque);
        btn.setBorderPainted(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JPanel createKPICard(String title, String value, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(accentColor);
                g.fillRect(0, 0, 4, getHeight());
            }
        };
        card.setBackground(COLOR_BG_CARD);
        card.setLayout(new GridLayout(2, 1, 0, 2));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(12, 18, 12, 15)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(COLOR_TEXT_MUTED);

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblVal.setForeground(COLOR_TEXT_MAIN);

        card.add(lblTitle);
        card.add(lblVal);
        card.putClientProperty("valueLabel", lblVal);
        return card;
    }

    private void styleSummaryTable(JTable table) {
        table.setRowHeight(30);
        table.setBackground(COLOR_BG_CARD);
        table.setForeground(COLOR_TEXT_MAIN);
        table.setGridColor(COLOR_BORDER);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setBackground(new Color(253, 242, 244));
        table.getTableHeader().setForeground(COLOR_TEXT_MAIN);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBorder(new LineBorder(COLOR_BORDER, 1));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Custom width for serial number column
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(70);

        // Center alignment applied to S.No. (Col 0), Marks Obtained (Col 2), and Class Position (Col 3)
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
    }

    private void handleAddGrade() {
        String name = txtStudentName.getText().trim();
        String gradeStr = txtGrade.getText().trim();

        if (name.isEmpty() || name.equals(NAME_PROMPT)) {
            name = "Student #" + (studentList.size() + 1);
        }

        if (gradeStr.isEmpty() || gradeStr.equals(GRADE_PROMPT)) {
            JOptionPane.showMessageDialog(this, "Please enter a grade numerical value.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double grade = Double.parseDouble(gradeStr);
            if (grade < 0 || grade > 100) {
                JOptionPane.showMessageDialog(this, "Grades must strictly be between 0 and 100.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            studentList.add(new StudentEntry(name, grade));
            listModel.insertElementAt("Added: " + name + " (" + grade + "%)", 0);

            recalculateMetricsAndRanks();

            resetFieldToPrompt(txtStudentName, NAME_PROMPT);
            resetFieldToPrompt(txtGrade, GRADE_PROMPT);
            chartPanel.requestFocusInWindow();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please input a valid numerical grade value.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recalculateMetricsAndRanks() {
        if (studentList.isEmpty()) return;


        Collections.sort(studentList, (a, b) -> Double.compare(b.getGrade(), a.getGrade()));

        summaryTableModel.setRowCount(0);
        chartPanel.clearChart();

        double total = 0;

        StudentEntry highestStudent = studentList.get(0);
        StudentEntry lowestStudent = studentList.get(0);

        int rank = 1;
        for (int i = 0; i < studentList.size(); i++) {
            StudentEntry student = studentList.get(i);
            double score = student.getGrade();
            total += score;

            if (score > highestStudent.getGrade()) highestStudent = student;
            if (score < lowestStudent.getGrade()) lowestStudent = student;

            if (i > 0 && score < studentList.get(i - 1).getGrade()) {
                rank = i + 1;
            }

            String positionSuffix = getOrdinalSuffix(rank);

           
            summaryTableModel.addRow(new Object[]{
                    String.valueOf(i + 1), student.getName(), String.format("%.1f%%", score), rank + positionSuffix
            });

            chartPanel.addBarData(student.getName(), score);
        }

        double average = total / studentList.size();

        lblTotalStudents.setText(String.valueOf(studentList.size()));
        lblAverage.setText(String.format("%.1f%%", average));

        lblHighest.setText(String.format("%.1f%% (%s)", highestStudent.getGrade(), highestStudent.getName()));
        lblLowest.setText(String.format("%.1f%% (%s)", lowestStudent.getGrade(), lowestStudent.getName()));
    }

    private String getOrdinalSuffix(int value) {
        if (value >= 11 && value <= 13) return "th";
        switch (value % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }

    private void resetFieldToPrompt(JTextField field, String prompt) {
        field.setText(prompt);
        field.setForeground(COLOR_TEXT_MUTED);
    }

    private void handleReset() {
        studentList.clear();
        listModel.clear();
        summaryTableModel.setRowCount(0);
        chartPanel.clearChart();
        lblTotalStudents.setText("0");
        lblAverage.setText("0.0%");
        lblHighest.setText("0.0%");
        lblLowest.setText("0.0%");
        resetFieldToPrompt(txtStudentName, NAME_PROMPT);
        resetFieldToPrompt(txtGrade, GRADE_PROMPT);
    }
    
    private class ChartPanel extends JPanel {
        private final ArrayList<String> names = new ArrayList<>();
        private final ArrayList<Double> values = new ArrayList<>();

        public ChartPanel() {
            setBackground(COLOR_BG_CARD);
            setBorder(new LineBorder(COLOR_BORDER, 1, true));
        }

        public void addBarData(String name, double value) {
            names.add(name);
            values.add(value);
            repaint();
        }

        public void clearChart() {
            names.clear();
            values.clear();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(COLOR_TEXT_MAIN);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString("Visual Frequency Distribution Ticker", 15, 22);

            if (values.isEmpty()) {
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                g2.setColor(COLOR_TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                String emptyMsg = "No data available to plot visualization scales.";
                int strW = g2.getFontMetrics().stringWidth(emptyMsg);
                g2.drawString(emptyMsg, centerX - (strW / 2), centerY + 4);
                return;
            }

            int chartHeight = getHeight() - 65;
            int startX = 45;
            int startY = getHeight() - 25;

            int fixedBarWidth = 45;
            int customGap = 24;

            for (int i = 0; i < values.size(); i++) {
                double score = values.get(i);
                int barHeight = (int) ((score / 100.0) * chartHeight);
                int x = startX + i * (fixedBarWidth + customGap);
                int y = startY - barHeight;

                if (x + fixedBarWidth > getWidth()) break;

                // ✨ Dynamic Color Gradient: Soft light rose on top fading down into dark accent berry pink at the base
                GradientPaint barGradient = new GradientPaint(
                        x, y, new Color(244, 143, 177),          // Light pink at the top
                        x, startY, COLOR_ACCENT                  // Dark accent pink at the bottom
                );
                g2.setPaint(barGradient);
                g2.fillRect(x, y, fixedBarWidth, barHeight);

                g2.setColor(COLOR_TEXT_MAIN);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.drawString(String.format("%.0f%%", score), x + (fixedBarWidth / 2) - 10, y - 5);

                g2.setColor(COLOR_TEXT_MUTED);
                String label = names.get(i).length() > 7 ? names.get(i).substring(0, 6) + ".." : names.get(i);
                g2.drawString(label, x + 2, startY + 14);
            }
        }
    }

    private static class StudentEntry {
        private final String name;
        private final double grade;
        public StudentEntry(String name, double grade) { this.name = name; this.grade = grade; }
        public String getName() { return name; }
        public double getGrade() { return grade; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
