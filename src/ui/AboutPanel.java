package ui;

import util.Theme;
import util.UiUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * About Project presentation view detailing objectives, technology stack,
 * features, schematic map notes, and academic viva credentials.
 */
public class AboutPanel extends JPanel {

    public AboutPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_LIGHT);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(28, 36, 36, 36));

        // 1. Hero Card
        content.add(createHeroCard());
        content.add(Box.createVerticalStrut(24));

        // 2. Project Details Grid (Objective & Tech Stack)
        content.add(createDetailsGrid());
        content.add(Box.createVerticalStrut(24));

        // 3. Features & Disclaimer Card
        content.add(createFeaturesCard());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Theme.BG_LIGHT);
        scroll.getViewport().setBackground(Theme.BG_LIGHT);

        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createHeroCard() {
        JPanel hero = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.applyQualityHints(g2);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(15, 23, 42),
                        getWidth(), getHeight(), new Color(14, 116, 144)
                );
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        hero.setLayout(new BorderLayout());
        hero.setOpaque(false);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setMaximumSize(new Dimension(2000, 150));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel tag = new JLabel("B.TECH COMPUTER SCIENCE MICRO PROJECT");
        tag.setFont(Theme.FONT_SMALL_BOLD);
        tag.setForeground(Theme.SIDEBAR_ACCENT);

        JLabel title = new JLabel("CampusNav — Alliance University Campus Navigation System");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel inst = new JLabel("Alliance University, Chikkahagade Cross, Chandapura - Anekal Main Road, Bengaluru, Karnataka 562106");
        inst.setFont(Theme.FONT_BODY);
        inst.setForeground(new Color(226, 232, 240));

        left.add(tag);
        left.add(Box.createVerticalStrut(8));
        left.add(title);
        left.add(Box.createVerticalStrut(8));
        left.add(inst);

        hero.add(left, BorderLayout.CENTER);
        return hero;
    }

    private JPanel createDetailsGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(2000, 240));

        // Card 1: Objectives
        JPanel cardObj = UiUtils.createCardPanel(14);
        cardObj.setLayout(new BorderLayout());
        cardObj.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel titleObj = new JLabel("🎯 Project Objective");
        titleObj.setFont(Theme.FONT_HEADER);
        titleObj.setForeground(Theme.TEXT_MAIN);

        JTextArea textObj = new JTextArea(
                "To deliver a lightweight, offline-first desktop navigation and discovery solution " +
                "for Alliance University students, faculty, and campus visitors. " +
                "The system provides an intuitive, interactive schematic overview of academic departments, " +
                "dormitory halls, dining hubs, and athletic grounds with zero external API or database dependencies."
        );
        textObj.setFont(Theme.FONT_BODY);
        textObj.setForeground(Theme.TEXT_MUTED);
        textObj.setLineWrap(true);
        textObj.setWrapStyleWord(true);
        textObj.setEditable(false);
        textObj.setOpaque(false);
        textObj.setBorder(new EmptyBorder(12, 0, 0, 0));

        cardObj.add(titleObj, BorderLayout.NORTH);
        cardObj.add(textObj, BorderLayout.CENTER);

        // Card 2: Technology Stack
        JPanel cardTech = UiUtils.createCardPanel(14);
        cardTech.setLayout(new BorderLayout());
        cardTech.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel titleTech = new JLabel("💻 Technology Stack");
        titleTech.setFont(Theme.FONT_HEADER);
        titleTech.setForeground(Theme.TEXT_MAIN);

        JPanel techList = new JPanel();
        techList.setLayout(new BoxLayout(techList, BoxLayout.Y_AXIS));
        techList.setOpaque(false);
        techList.setBorder(new EmptyBorder(12, 0, 0, 0));

        addTechItem(techList, "Core Platform", "Java 21 / Modern Java SE Development Kit");
        addTechItem(techList, "GUI Framework", "Java Swing (Custom painted components, EDT model)");
        addTechItem(techList, "Rendering Engine", "Java2D & AWT (Anti-aliased vector map graphics)");
        addTechItem(techList, "Persistence Layer", "Standard RFC 4180 CSV File I/O (locations.csv & map_layout.csv)");
        addTechItem(techList, "Development Environment", "Visual Studio Code & Windows PowerShell");

        cardTech.add(titleTech, BorderLayout.NORTH);
        cardTech.add(techList, BorderLayout.CENTER);

        grid.add(cardObj);
        grid.add(cardTech);
        return grid;
    }

    private void addTechItem(JPanel container, String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        row.setOpaque(false);

        JLabel lbl = new JLabel("•  " + label + ": ");
        lbl.setFont(Theme.FONT_BODY_BOLD);
        lbl.setForeground(Theme.TEXT_MAIN);

        JLabel val = new JLabel(value);
        val.setFont(Theme.FONT_BODY);
        val.setForeground(Theme.TEXT_MUTED);

        row.add(lbl);
        row.add(val);
        container.add(row);
    }

    private JPanel createFeaturesCard() {
        JPanel card = UiUtils.createCardPanel(14);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 20, 22));
        card.setMaximumSize(new Dimension(2000, 320));

        JLabel title = new JLabel("⚡ Core Architectural Features");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_MAIN);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(12, 0, 0, 0));

        String[] features = {
                "Dynamic Java2D Schematic Map: Custom vector rendering of iconic Alliance landmarks including the central School of Business circular rotunda, University Library dome, quadrangle residence halls, football turf, cricket grounds, and road networks.",
                "Real-Time Map Editor: Drag-and-drop building layout modification with coordinate preservation, zoom controls (0.35x - 2.5x), smooth canvas panning, and single-click 'Save Layout' or 'Reset Layout' functionality.",
                "Multi-Criterion Search Engine: Case-insensitive search across building titles, unique IDs, category taxonomies, and functional descriptions.",
                "Full Lifecycle Location Management: Add, edit, and delete campus points with validation, duplicate ID prevention, and instant cross-view synchronization.",
                "Resilient CSV Persistence: Automatic generation of initial seed files upon first launch with robust quotation and comma handling."
        };

        for (String feat : features) {
            JLabel lbl = new JLabel("<html><body style='width: 780px;'>✔ " + feat + "</body></html>");
            lbl.setFont(Theme.FONT_BODY);
            lbl.setForeground(Theme.TEXT_MAIN);
            lbl.setBorder(new EmptyBorder(3, 0, 6, 0));
            body.add(lbl);
        }

        // Disclaimer Note Banner
        JPanel notice = new JPanel(new BorderLayout());
        notice.setBackground(new Color(254, 243, 199));
        notice.setBorder(new EmptyBorder(10, 14, 10, 14));
        JLabel noticeText = new JLabel("<html><b>Notice:</b> The campus map in this application is an illustrated schematic model created for academic discovery and visual orientation. It does not calculate turn-by-turn routing or GPS distances.</html>");
        noticeText.setFont(Theme.FONT_SMALL);
        noticeText.setForeground(new Color(146, 64, 14));
        notice.add(noticeText, BorderLayout.CENTER);

        body.add(Box.createVerticalStrut(10));
        body.add(notice);

        card.add(title, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

        return card;
    }
}
