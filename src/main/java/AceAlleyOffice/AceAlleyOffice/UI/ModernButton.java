package AceAlleyOffice.AceAlleyOffice.UI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;

public class ModernButton extends JButton{
    private Shape shape;
    private Color baseColor;
    private Color hoverColor;
    private Color pressedColor;

    public ModernButton(String text, Color color) {
        super(text);
        this.baseColor = color;
        this.hoverColor = color.brighter();
        this.pressedColor = color.darker();
        
        // Font size for left panel buttons is smaller
        if (text.equals("Save Changes") || text.equals("Cancel Booking")) {
            setFont(new Font("Roboto", Font.BOLD, 18));
        } else {
            setFont(new Font("Roboto", Font.BOLD, 14));
        }

        setForeground(Color.WHITE);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (getModel().isEnabled()) {
            if (getModel().isPressed()) {
                g2.setColor(pressedColor);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverColor);
            } else {
                g2.setColor(baseColor);
            }
        } else {
            g2.setColor(Color.GRAY);
        }
        
        shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        g2.fill(shape);
        
        // --- ADDED: Draw a black border ---
        g2.setColor(Color.BLACK);
        g2.draw(shape);
        
        g2.dispose();
        
        super.paintComponent(g);
    }

    @Override
    public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())) {
            shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        }
        return shape.contains(x, y);
    }
}
