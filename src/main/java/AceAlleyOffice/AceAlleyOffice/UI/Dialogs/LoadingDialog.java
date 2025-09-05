// File: AceAlleyOffice/AceAlleyOffice/UI/Dialogs/LoadingDialog.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class LoadingDialog extends JDialog {

    private final JLabel messageLabel;

    public LoadingDialog(Frame owner) {
        super(owner, "Loading...", false);

        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        panel.setBackground(new Color(15, 15, 25));

        AnimatedSpinner spinner = new AnimatedSpinner();
        spinner.setPreferredSize(new Dimension(300, 220)); // Made it slightly taller

        messageLabel = new JLabel("Initializing...", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 12));
        messageLabel.setForeground(Color.WHITE);

        panel.add(spinner, BorderLayout.CENTER);
        panel.add(messageLabel, BorderLayout.SOUTH);

        setUndecorated(true);
        setContentPane(panel);
        pack();
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    public void setStatusMessage(String message) {
        messageLabel.setText(message);
    }

    // --- FINAL, "GOOGLE-LEVEL" VERSION WITH LOGO INTEGRATION ---
    private static class AnimatedSpinner extends JComponent {
        private final Timer timer;
        private final Particle[] particles;
        private final int PARTICLE_COUNT = 120; // Slightly fewer for performance
        private final double MAX_CONNECTION_DISTANCE = 70.0;
        private Point mousePos;
        
        // --- NEW: Logo and physics properties ---
        private Image logo;
        private final int LOGO_SIZE = 80;
        private final static double REPULSION_RADIUS = 60.0; // The "force field" radius around the logo
        private double boundaryRadius;

        private static class Particle {
            Point2D.Double pos;
            Point2D.Double vel;
            double radius;
            Color color;

            Particle(Random r, int width, int height) {
                this.pos = new Point2D.Double(r.nextInt(width), r.nextInt(height));
                this.vel = new Point2D.Double(r.nextDouble() - 0.5, r.nextDouble() - 0.5);
                this.radius = r.nextDouble() * 1.5 + 1;
                this.color = new Color(0, 150 + r.nextInt(106), 255, 200);
            }

            void update(int width, int height, double boundaryRadius, Point mousePos) {
                int centerX = width / 2;
                int centerY = height / 2;

                // Repulsion from mouse
                if (mousePos != null) {
                    double dx = pos.x - mousePos.x;
                    double dy = pos.y - mousePos.y;
                    double distSq = dx * dx + dy * dy;
                    if (distSq < 80 * 80 && distSq > 0) {
                        double dist = Math.sqrt(distSq);
                        double force = 50 / distSq;
                        vel.x += force * (dx / dist);
                        vel.y += force * (dy / dist);
                    }
                }
                
                // --- NEW: Repulsion from the central logo ---
                double dx_center = pos.x - centerX;
                double dy_center = pos.y - centerY;
                double distSq_center = dx_center * dx_center + dy_center * dy_center;
                if (distSq_center < REPULSION_RADIUS * REPULSION_RADIUS && distSq_center > 0) {
                    double dist_center = Math.sqrt(distSq_center);
                    double force = 100 / distSq_center;
                    vel.x += force * (dx_center / dist_center);
                    vel.y += force * (dy_center / dist_center);
                }

                vel.x *= 0.97; // Drag
                vel.y *= 0.97;
                pos.x += vel.x;
                pos.y += vel.y;

                // --- NEW: Circular boundary bouncing logic ---
                double dist_from_center = Math.sqrt(dx_center * dx_center + dy_center * dy_center);
                if (dist_from_center > boundaryRadius) {
                    // Reflect the velocity vector
                    double nx = dx_center / dist_from_center;
                    double ny = dy_center / dist_from_center;
                    double dot = vel.x * nx + vel.y * ny;
                    vel.x -= 2 * dot * nx;
                    vel.y -= 2 * dot * ny;
                }
            }
        }

        public AnimatedSpinner() {
            // --- NEW: Load the logo image from resources ---
            try {
                // IMPORTANT: Make sure you have a 'logo.png' in your resources folder!
                URL logoUrl = getClass().getResource("/media/AceAlleyLogoCircle.png");
                if (logoUrl != null) {
                    logo = new ImageIcon(logoUrl).getImage();
                } else {
                    System.err.println("Logo not found! Please place 'logo.png' in the resources folder.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            particles = new Particle[PARTICLE_COUNT];
            
            // Mouse listeners remain the same
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    if (mousePos == null) mousePos = new Point();
                    mousePos.setLocation(e.getPoint());
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseExited(MouseEvent e) { mousePos = null; }
            });

            this.timer = new Timer(16, e -> {
                if (getWidth() > 0 && particles[0] == null) {
                    Random r = new Random();
                    this.boundaryRadius = Math.min(getWidth(), getHeight()) / 2.0 - 10;
                    for (int i = 0; i < PARTICLE_COUNT; i++) {
                        particles[i] = new Particle(r, getWidth(), getHeight());
                    }
                }
                if (particles[0] != null) {
                    for (Particle p : particles) {
                        p.update(getWidth(), getHeight(), boundaryRadius, mousePos);
                    }
                }
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (particles[0] == null) return;

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            // Draw connections
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                for (int j = i + 1; j < PARTICLE_COUNT; j++) {
                    double distance = particles[i].pos.distance(particles[j].pos);
                    if (distance < MAX_CONNECTION_DISTANCE) {
                        int alpha = (int) (120 * (1 - (distance / MAX_CONNECTION_DISTANCE)));
                        g2d.setColor(new Color(100, 150, 255, alpha));
                        g2d.setStroke(new BasicStroke(0.5f));
                        g2d.drawLine((int) particles[i].pos.x, (int) particles[i].pos.y, (int) particles[j].pos.x, (int) particles[j].pos.y);
                    }
                }
            }
            
            // Draw particles
            for (Particle p : particles) {
                g2d.setColor(p.color);
                g2d.fillOval((int) (p.pos.x - p.radius), (int) (p.pos.y - p.radius), (int) (p.radius * 2), (int) (p.radius * 2));
            }
            
            // --- NEW: Draw the logo and its glow on top of everything ---
            if (logo != null) {
                // Draw a soft glow behind the logo
                Graphics2D g2dGlow = (Graphics2D) g2d.create();
                g2dGlow.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2dGlow.setColor(new Color(0, 180, 255));
                for(int i = LOGO_SIZE + 10; i > LOGO_SIZE; i-=2) {
                     g2dGlow.fillOval(centerX - i/2, centerY - i/2, i, i);
                }
                g2dGlow.dispose();

                // Draw the actual logo
                g2d.drawImage(logo, centerX - LOGO_SIZE / 2, centerY - LOGO_SIZE / 2, LOGO_SIZE, LOGO_SIZE, this);
            }
        }
    }
}