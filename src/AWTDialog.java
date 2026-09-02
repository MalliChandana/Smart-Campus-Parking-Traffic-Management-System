import java.awt.*;
import java.awt.event.*;

/**
 * AWTDialog - Pure Java AWT Modal Notification and Confirmation Dialog
 *
 * Replaces JOptionPane and JDialog completely using standard java.awt.Dialog.
 * Contains ZERO javax.swing dependencies.
 */
public class AWTDialog extends Dialog {

    private boolean confirmed = false;

    public AWTDialog(Frame parent, String title, String message, boolean isConfirm) {
        super(parent, title, true); // Modal dialog
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(248, 250, 252));
        setSize(420, 240);
        setLocationRelativeTo(parent);

        // Header Banner
        Panel headerPanel = new Panel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        headerPanel.setBackground(new Color(15, 23, 42));
        Label lblTitle = new Label(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        // Message Area
        Panel messagePanel = new Panel(new BorderLayout(8, 8)) {
            @Override
            public Insets getInsets() {
                return new Insets(12, 16, 12, 16);
            }
        };
        messagePanel.setBackground(Color.WHITE);

        TextArea txtMsg = new TextArea(message, 5, 40, TextArea.SCROLLBARS_VERTICAL_ONLY);
        txtMsg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMsg.setEditable(false);
        txtMsg.setBackground(Color.WHITE);
        messagePanel.add(txtMsg, BorderLayout.CENTER);
        add(messagePanel, BorderLayout.CENTER);

        // Buttons Footer
        Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        buttonPanel.setBackground(new Color(241, 245, 249));

        if (isConfirm) {
            Button btnYes = new Button("  Yes, Proceed  ");
            btnYes.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnYes.setBackground(new Color(37, 99, 235));
            btnYes.setForeground(Color.WHITE);
            btnYes.addActionListener(e -> {
                confirmed = true;
                dispose();
            });

            Button btnNo = new Button("  Cancel  ");
            btnNo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            btnNo.setBackground(Color.WHITE);
            btnNo.addActionListener(e -> {
                confirmed = false;
                dispose();
            });

            buttonPanel.add(btnYes);
            buttonPanel.add(btnNo);
        } else {
            Button btnOk = new Button("     OK     ");
            btnOk.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnOk.setBackground(new Color(37, 99, 235));
            btnOk.setForeground(Color.WHITE);
            btnOk.addActionListener(e -> dispose());
            buttonPanel.add(btnOk);
        }

        add(buttonPanel, BorderLayout.SOUTH);

        // Window Closing Listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmed = false;
                dispose();
            }
        });
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public static void showMessage(Component parent, String title, String message) {
        Frame frame = getParentFrame(parent);
        AWTDialog dialog = new AWTDialog(frame, title, message, false);
        dialog.setVisible(true);
    }

    public static boolean showConfirm(Component parent, String title, String question) {
        Frame frame = getParentFrame(parent);
        AWTDialog dialog = new AWTDialog(frame, title, question, true);
        dialog.setVisible(true);
        return dialog.isConfirmed();
    }

    private static Frame getParentFrame(Component c) {
        if (c instanceof Frame) {
            return (Frame) c;
        }
        while (c != null && !(c instanceof Frame)) {
            c = c.getParent();
        }
        return (Frame) c;
    }
}
