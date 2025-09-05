// File: AceAlleyOffice/AceAlleyOffice/UI/Dialogs/SearchResultDialog.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import AceAlleyOffice.AceAlleyOffice.UI.BookingDetails;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class SearchResultDialog extends JDialog {

    // --- ADDED: A listener interface to communicate clicks back to the main window ---
    public interface OnSearchResultClickedListener {
        void onSearchResultClicked(BookingDetails clickedBooking);
    }

    // --- MODIFIED: The constructor now accepts the listener ---
    public SearchResultDialog(Frame owner, List<BookingDetails> results, OnSearchResultClickedListener listener) {
        super(owner, "Search Results", true);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(owner);
        getContentPane().setBackground(new Color(45, 45, 45));

        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(new Color(45, 45, 45));
        resultsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        if (results.isEmpty()) {
            JLabel noResultsLabel = new JLabel("No bookings found for the search query.");
            noResultsLabel.setForeground(Color.WHITE);
            noResultsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            resultsPanel.add(noResultsLabel);
        } else {
            for (BookingDetails result : results) {
                SearchResultPanel card = new SearchResultPanel(result);
                card.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        card.setBackground(new Color(80, 80, 80));
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        card.setBackground(new Color(60, 60, 60));
                    }
                    
                    // --- ADDED: Handle the click event ---
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Notify the main window about which booking was clicked
                        if (listener != null) {
                            listener.onSearchResultClicked(result);
                        }
                        // Close the search dialog
                        dispose(); 
                    }
                });
                resultsPanel.add(card);
                resultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);
    }
}