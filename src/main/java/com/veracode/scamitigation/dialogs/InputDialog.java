package com.veracode.scamitigation.dialogs;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.veracode.scamitigation.ui.components.StringModalWrapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;

public class InputDialog  extends JDialog {
    private final StringModalWrapper stringModalWrapper;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea inputTextArea;

    public InputDialog(String title, StringModalWrapper stringModalWrapper) {
        this.$$$setupUI$$$();
        this.setContentPane(this.contentPane);
        this.setTitle(title);
        this.setModal(true);
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.getRootPane().setDefaultButton(this.buttonOK);
        this.stringModalWrapper = stringModalWrapper;
        this.buttonOK.addActionListener((e) -> {
            this.onOK();
        });
        this.buttonCancel.addActionListener((e) -> {
            this.onCancel();
        });
        this.setDefaultCloseOperation(0);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                InputDialog.this.onCancel();
            }
        });
        this.contentPane.registerKeyboardAction((e) -> {
            this.onCancel();
        }, KeyStroke.getKeyStroke(27, 0), 1);
    }

    private void onOK() {
        this.stringModalWrapper.setTheString(this.inputTextArea.getText());
        this.dispose();
    }

    private void onCancel() {
        this.stringModalWrapper.setCancel(true);
        this.dispose();
    }

    public static Optional<StringModalWrapper> getModalResult(String title) {
        StringModalWrapper stringModalWrapper = new StringModalWrapper();
        InputDialog dialog = new InputDialog(title, stringModalWrapper);
        dialog.pack();
        dialog.setVisible(true);
        return Optional.of(stringModalWrapper).filter(StringModalWrapper::isSet);
    }

    private void $$$setupUI$$$() {
        this.contentPane = new JPanel();
        this.contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        this.contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, 0, 3, 3, 1, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, 0, 1, 4, 1, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.buttonOK = new JButton();
        this.buttonOK.setText("OK");
        panel2.add(this.buttonOK, new GridConstraints(0, 0, 1, 1, 0, 1, 3, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.buttonCancel = new JButton();
        this.buttonCancel.setText("Cancel");
        panel2.add(this.buttonCancel, new GridConstraints(0, 1, 1, 1, 0, 1, 3, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        this.contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.inputTextArea = new JTextArea();
        panel3.add(this.inputTextArea, new GridConstraints(0, 0, 1, 1, 0, 3, 4, 4, (Dimension)null, new Dimension(150, 50), (Dimension)null, 0, false));
    }

    public JComponent $$$getRootComponent$$$() {
        return this.contentPane;
    }
}
