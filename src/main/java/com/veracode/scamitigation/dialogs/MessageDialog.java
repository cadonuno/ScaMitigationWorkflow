package com.veracode.scamitigation.dialogs;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.veracode.scamitigation.MessageDialogIconEnum;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MessageDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea messageTextArea;
    private JPanel topPanel;
    private JPanel bottomPanel;
    private JLabel iconLabel;

    public MessageDialog(String title, String message) {
        this.$$$setupUI$$$();
        this.setContentPane(this.contentPane);
        this.setModal(true);
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.setTitle(title);
        this.messageTextArea.setText(message);
        this.getRootPane().setDefaultButton(this.buttonOK);
        this.buttonOK.addActionListener((e) -> {
            this.onOK();
        });
        this.buttonCancel.addActionListener((e) -> {
            this.onCancel();
        });
        this.setDefaultCloseOperation(0);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                MessageDialog.this.onCancel();
            }
        });
        this.contentPane.registerKeyboardAction((e) -> {
            this.onCancel();
        }, KeyStroke.getKeyStroke(27, 0), 1);
    }

    private void setIcon(MessageDialogIconEnum messageDialogIconEnum) {
        this.iconLabel.setIcon(messageDialogIconEnum.getImageIcon());
    }

    private void onOK() {
        this.dispose();
    }

    private void onCancel() {
        this.dispose();
    }

    public static void showSuccessDialog(String message) {
        showMessageDialog("Success!", message, MessageDialogIconEnum.SUCCESS_ICON);
    }

    public static void showWarningDialog(String message) {
        showMessageDialog("Warning!", message, MessageDialogIconEnum.WARNING_ICON);
    }

    public static void showErrorDialog(String message) {
        showMessageDialog("Error!", message, MessageDialogIconEnum.ERROR_ICON);
    }

    private static void showMessageDialog(String title, String message, MessageDialogIconEnum messageDialogIconEnum) {
        MessageDialog dialog = new MessageDialog(title, message);
        dialog.setIcon(messageDialogIconEnum);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void $$$setupUI$$$() {
        this.contentPane = new JPanel();
        this.contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        this.bottomPanel = new JPanel();
        this.bottomPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        this.contentPane.add(this.bottomPanel, new GridConstraints(1, 0, 1, 1, 0, 3, 3, 1, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        Spacer spacer1 = new Spacer();
        this.bottomPanel.add(spacer1, new GridConstraints(0, 0, 1, 1, 0, 1, 4, 1, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        this.bottomPanel.add(panel1, new GridConstraints(0, 1, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.buttonOK = new JButton();
        this.buttonOK.setText("OK");
        panel1.add(this.buttonOK, new GridConstraints(0, 0, 1, 1, 0, 1, 3, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.buttonCancel = new JButton();
        this.buttonCancel.setText("Cancel");
        panel1.add(this.buttonCancel, new GridConstraints(0, 1, 1, 1, 0, 1, 3, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.topPanel = new JPanel();
        this.topPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        this.contentPane.add(this.topPanel, new GridConstraints(0, 0, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.iconLabel = new JLabel();
        this.iconLabel.setText("");
        this.topPanel.add(this.iconLabel, new GridConstraints(0, 0, 1, 1, 8, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.messageTextArea = new JTextArea();
        this.messageTextArea.setEditable(false);
        this.topPanel.add(this.messageTextArea, new GridConstraints(0, 1, 1, 1, 0, 3, 4, 4, (Dimension)null, new Dimension(150, 50), (Dimension)null, 0, false));
    }

    public JComponent $$$getRootComponent$$$() {
        return this.contentPane;
    }
}
