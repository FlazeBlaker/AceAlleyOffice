// File: AceAlleyOffice/AceAlleyOffice/UI/Dialogs/VenueSelectionDialog.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import AceAlleyOffice.AceAlleyOffice.Core.VenueConfig;

public class VenueSelectionDialog extends JDialog {
    private JComboBox<VenueConfig> venueComboBox;
    private String selectedVenueId = null;

    public VenueSelectionDialog(Frame owner, List<VenueConfig> venues) {
        super(owner, "Select Venue to Manage", true);

        // Populate the combo box
        venueComboBox = new JComboBox<>(venues.toArray(new VenueConfig[0]));
        venueComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof VenueConfig) {
                    setText(((VenueConfig) value).getVenueName());
                }
                return this;
            }
        });

        JButton selectButton = new JButton("Select");
        selectButton.addActionListener(e -> {
            VenueConfig selected = (VenueConfig) venueComboBox.getSelectedItem();
            if (selected != null) {
                // You will need to add a getVenueId() method to your VenueConfig class
                // that gets the ID from the document. For now, we assume it's there.
                // selectedVenueId = selected.getVenueId(); 
                // As a temporary measure if you don't have the above, we can parse it from the name
                String venueName = selected.getVenueName().toLowerCase();
                if (venueName.contains("viman")) {
                    selectedVenueId = "viman_nagar";
                } else if (venueName.contains("aundh")) {
                    selectedVenueId = "aundh";
                }
            }
            dispose();
        });

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        panel.add(new JLabel("Choose a venue:"));
        panel.add(venueComboBox);
        panel.add(selectButton);

        setContentPane(panel);
        pack();
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    public String showDialogAndGetVenueId() {
        setVisible(true);
        return this.selectedVenueId;
    }
}