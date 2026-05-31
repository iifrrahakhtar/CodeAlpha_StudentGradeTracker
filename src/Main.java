import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;

public class Main extends JFrame {

    private ArrayList<Double> grades = new ArrayList<>();
    private DefaultListModel<String> listModel = new DefaultListModel<>();

    // UI Elements for Live Updates
    private JLabel lblTotalStudents, lblAverage, lblHighest, lblLowest;
    private JTextField txtStudentName, txtGrade;
    private JList<String> gradeLogList;
    private ChartPanel chartPanel;

    // Placeholders
    private final String NAME_PROMPT = "Student Name (e.g., Alex)";
    private final String GRADE_PROMPT = "Grade (0 - 100)";

    private final Color COLOR_BG_MAIN = new Color(23, 15, 38);        // Deep Dark Purple Background
    private final Color COLOR_BG_CARD = new Color(36, 25, 56);        // Slightly Lighter Purple for Cards/Sidebar
    private final Color COLOR_ACCENT = new Color(187, 134, 252);     // Bright Neon Purple / Pastel Orchid
    private final Color COLOR_TEXT_MAIN = Color.WHITE;
    private final Color COLOR_TEXT_MUTED = new Color(190, 175, 210); // Soft Light Lavender for Placeholders
    private final Color COLOR_BORDER = new Color(74, 52, 107);        // Muted Purple Border

    public Main() {
        setTitle("CodeAlpha | Premium Grade Dashboard");
        setSize(1050, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG_MAIN);
        setLayout(new BorderLayout());

        // 1. --- SIDEBAR ---
        JPanel sidebar = new JPanel();
        sidebar.setBackground(COLOR_BG_CARD);
        sidebar.setPreferredSize(new Dimension(280, 650));
        sidebar.setBorder(new EmptyBorder(25, 20, 25, 20));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel sidebarTitle = new JLabel("Grade Control Panel");
        sidebarTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sidebarTitle.setForeground(COLOR_TEXT_MAIN);
        sidebarTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtStudentName = createCustomTextField(NAME_PROMPT);
        txtGrade = createCustomTextField(GRADE_PROMPT);

        JButton btnAdd = new JButton("Add Grade");
        styleButton(btnAdd, COLOR_ACCENT, new Color(23, 15, 38), true);
        btnAdd.addActionListener(e -> handleAddGrade());

        JButton btnReset = new JButton("Reset Data");
        styleButton(btnReset, COLOR_BG_CARD, new Color(255, 107, 107), false);
        btnReset.setBorder(new LineBorder(new Color(255, 107, 107), 1, true));
        btnReset.addActionListener(e -> handleReset());

        JLabel lblLogTitle = new JLabel("Grade Log");
        lblLogTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLogTitle.setForeground(new Color(150, 135, 170));
        lblLogTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        gradeLogList = new JList<>(listModel);
        gradeLogList.setBackground(new Color(51, 37, 77));
        gradeLogList.setForeground(COLOR_TEXT_MAIN);
        gradeLogList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(gradeLogList);
        scrollPane.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(scrollPane);

        add(sidebar, BorderLayout.WEST);

        // 2. --- MAIN DASHBOARD AREA ---
        JPanel mainContent = new JPanel(new BorderLayout(0, 20));
        mainContent.setBackground(COLOR_BG_MAIN);
        mainContent.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel mainTitle = new JLabel("STUDENT PERFORMANCE DASHBOARD");
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        mainTitle.setForeground(COLOR_TEXT_MAIN);
        mainContent.add(mainTitle, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 25));
        centerWrapper.setBackground(COLOR_BG_MAIN);

        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        kpiPanel.setBackground(COLOR_BG_MAIN);

        // Styled KPI Cards with unique modern bottom accent lines
        JPanel cardTotal = createKPICard("TOTAL STUDENTS", "0", new Color(106, 90, 205));
        JPanel cardAvg = createKPICard("AVERAGE SCORE", "0.0%", new Color(0, 201, 167));
        JPanel cardHigh = createKPICard("HIGHEST SCORE", "0.0", new Color(255, 165, 0));
        JPanel cardLow = createKPICard("LOWEST SCORE", "0.0", new Color(255, 95, 86));

        lblTotalStudents = (JLabel) cardTotal.getClientProperty("valueLabel");
        lblAverage = (JLabel) cardAvg.getClientProperty("valueLabel");
        lblHighest = (JLabel) cardHigh.getClientProperty("valueLabel");
        lblLowest = (JLabel) cardLow.getClientProperty("valueLabel");

        kpiPanel.add(cardTotal);
        kpiPanel.add(cardAvg);
        kpiPanel.add(cardHigh);
        kpiPanel.add(cardLow);
        centerWrapper.add(kpiPanel, BorderLayout.NORTH);

        chartPanel = new ChartPanel();
        centerWrapper.add(chartPanel, BorderLayout.CENTER);

        mainContent.add(centerWrapper, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);
    }

    private JTextField createCustomTextField(String prompt) {
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setPreferredSize(new Dimension(240, 40));
        field.setBackground(new Color(51, 37, 77));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(0, 10, 0, 10)
        ));

        field.setText(prompt);
        field.setForeground(COLOR_TEXT_MUTED);
        field.setCaretColor(COLOR_TEXT_MAIN);

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
                g.fillRect(0, getHeight() - 5, getWidth(), 5);
            }
        };
        card.setBackground(COLOR_BG_CARD);
        card.setLayout(new GridLayout(2, 1, 0, 0));
        card.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(150, 135, 170));

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblVal.setForeground(COLOR_TEXT_MAIN);

        card.add(lblTitle);
        card.add(lblVal);
        card.putClientProperty("valueLabel", lblVal);
        return card;
    }

    private void handleAddGrade() {
        String name = txtStudentName.getText().trim();
        String gradeStr = txtGrade.getText().trim();

        if (name.isEmpty() || name.equals(NAME_PROMPT)) {
            name = "Student #" + (grades.size() + 1);
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

            grades.add(grade);
            listModel.addElement(name + " — " + grade + "%");
            chartPanel.addBarData(name, grade);
            updateMetrics();

            resetFieldToPrompt(txtStudentName, NAME_PROMPT);
            resetFieldToPrompt(txtGrade, GRADE_PROMPT);
            chartPanel.requestFocusInWindow();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please input a valid numerical grade value.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetFieldToPrompt(JTextField field, String prompt) {
        field.setText(prompt);
        field.setForeground(COLOR_TEXT_MUTED);
    }

    private void updateMetrics() {
        if (grades.isEmpty()) return;

        double total = 0;
        double highest = grades.get(0);
        double lowest = grades.get(0);

        for (double grade : grades) {
            total += grade;
            if (grade > highest) highest = grade;
            if (grade < lowest) lowest = grade;
        }
        double average = total / grades.size();

        lblTotalStudents.setText(String.valueOf(grades.size()));
        lblAverage.setText(String.format("%.1f%%", average));
        lblHighest.setText(String.format("%.1f", highest));
        lblLowest.setText(String.format("%.1f", lowest));
    }

    private void handleReset() {
        grades.clear();
        listModel.clear();
        chartPanel.clearChart();
        lblTotalStudents.setText("0");
        lblAverage.setText("0.0%");
        lblHighest.setText("0.0");
        lblLowest.setText("0.0");
        resetFieldToPrompt(txtStudentName, NAME_PROMPT);
        resetFieldToPrompt(txtGrade, GRADE_PROMPT);
    }

    // Custom Chart with matching Purple Glowing Watermark Engine
    private class ChartPanel extends JPanel {
        private ArrayList<String> names = new ArrayList<>();
        private ArrayList<Double> values = new ArrayList<>();

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
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString("Grade Distribution Visualizer", 20, 30);

            // Empty State: Draws a stunning neon purple vector logo watermark
            if (values.isEmpty()) {
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2 - 20;

                // Outer glowing circle
                g2.setColor(new Color(187, 134, 252, 25));
                g2.fillOval(centerX - 40, centerY - 40, 80, 80);

                g2.setColor(new Color(187, 134, 252, 70));
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(centerX - 40, centerY - 40, 80, 80);

                // Vector Bar Chart Graphics
                g2.setColor(COLOR_ACCENT);
                g2.fillRect(centerX - 15, centerY - 5, 8, 20);
                g2.fillRect(centerX - 3, centerY - 15, 8, 30);
                g2.fillRect(centerX + 9, centerY - 25, 8, 40);

                // Information prompts
                g2.setColor(COLOR_TEXT_MAIN);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                String msg1 = "No Data Captured Yet";
                int stringWidth1 = g2.getFontMetrics().stringWidth(msg1);
                g2.drawString(msg1, centerX - (stringWidth1 / 2), centerY + 70);

                g2.setColor(new Color(150, 135, 170));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                String msg2 = "Please input student details on the left panel to begin.";
                int stringWidth2 = g2.getFontMetrics().stringWidth(msg2);
                g2.drawString(msg2, centerX - (stringWidth2 / 2), centerY + 95);
                return;
            }

            int chartWidth = getWidth() - 80;
            int chartHeight = getHeight() - 100;
            int startX = 50;
            int startY = getHeight() - 40;

            int barWidth = Math.max(15, (chartWidth / values.size()) - 15);

            for (int i = 0; i < values.size(); i++) {
                double score = values.get(i);
                int barHeight = (int) ((score / 100.0) * chartHeight);
                int x = startX + i * (barWidth + 15);
                int y = startY - barHeight;

                // Beautiful Purple-to-Dark Purple Gradient bars
                GradientPaint barGradient = new GradientPaint(x, y, COLOR_ACCENT, x, startY, new Color(74, 34, 117));
                g2.setPaint(barGradient);
                g2.fillRect(x, y, barWidth, barHeight);

                g2.setColor(COLOR_TEXT_MAIN);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.drawString(String.format("%.0f%%", score), x, y - 5);

                g2.setColor(new Color(150, 135, 170));
                String truncatedName = names.get(i).length() > 8 ? names.get(i).substring(0, 6) + ".." : names.get(i);
                g2.drawString(truncatedName, x, startY + 18);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}