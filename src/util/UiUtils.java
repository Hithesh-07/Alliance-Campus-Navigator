package util;

import model.Category;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * UI Utilities for custom painting, modern rounded controls, and consistent Swing aesthetics.
 */
public final class UiUtils {

    private UiUtils() {}

    /**
     * Applies standard high-quality anti-aliasing hints to graphics context.
     */
    public static void applyQualityHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    /**
     * Creates a styled modern button with rounded corners and hover effect.
     */
    public static JButton createButton(String text, Color bgColor, Color fgColor, int radius) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            private boolean pressed = false;

            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setFont(Theme.FONT_BODY_BOLD);
                setForeground(fgColor);
                setCursor(new Cursor(Cursor.HAND_CURSOR));

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        pressed = true;
                        repaint();
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        pressed = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyQualityHints(g2);

                Color currentBg = bgColor;
                if (!isEnabled()) {
                    currentBg = new Color(203, 213, 225);
                } else if (pressed) {
                    currentBg = darken(bgColor, 0.15f);
                } else if (hovered) {
                    currentBg = brighten(bgColor, 0.12f);
                }

                g2.setColor(currentBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
                g2.dispose();

                super.paintComponent(g);
            }
        };
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, Theme.PRIMARY, Color.WHITE, 8);
    }

    public static JButton createSecondaryButton(String text) {
        return createButton(text, new Color(241, 245, 249), Theme.TEXT_MAIN, 8);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, Theme.DANGER, Color.WHITE, 8);
    }

    /**
     * Creates a modern rounded text field.
     */
    public static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyQualityHints(g2);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyQualityHints(g2);
                g2.setColor(hasFocus() ? Theme.PRIMARY : Theme.CARD_BORDER);
                g2.setStroke(new BasicStroke(hasFocus() ? 1.5f : 1f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 8, 8));
                g2.dispose();
            }
        };
        field.setOpaque(false);
        field.setFont(Theme.FONT_BODY);
        field.setBorder(new EmptyBorder(8, 12, 8, 12));
        return field;
    }

    /**
     * Creates a category badge pill component.
     */
    public static JPanel createCategoryBadge(Category category) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyQualityHints(g2);
                g2.setColor(category.getBadgeBgColor());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(new Color(category.getPrimaryColor().getRed(), category.getPrimaryColor().getGreen(), category.getPrimaryColor().getBlue(), 70));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        JLabel lbl = new JLabel(category.getDisplayName());
        lbl.setFont(Theme.FONT_SMALL_BOLD);
        lbl.setForeground(category.getPrimaryColor());
        badge.add(lbl);
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));
        return badge;
    }

    /**
     * Creates a rounded card container.
     */
    public static JPanel createCardPanel(int radius) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyQualityHints(g2);
                // Card background
                g2.setColor(Theme.CARD_BG);
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, radius, radius));
                // Card border
                g2.setColor(Theme.CARD_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, radius, radius));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        return card;
    }

    public static Color brighten(Color color, float fraction) {
        int r = Math.min(255, (int) (color.getRed() + (255 - color.getRed()) * fraction));
        int g = Math.min(255, (int) (color.getGreen() + (255 - color.getGreen()) * fraction));
        int b = Math.min(255, (int) (color.getBlue() + (255 - color.getBlue()) * fraction));
        return new Color(r, g, b, color.getAlpha());
    }

    public static Color darken(Color color, float fraction) {
        int r = Math.max(0, (int) (color.getRed() * (1 - fraction)));
        int g = Math.max(0, (int) (color.getGreen() * (1 - fraction)));
        int b = Math.max(0, (int) (color.getBlue() * (1 - fraction)));
        return new Color(r, g, b, color.getAlpha());
    }
}
