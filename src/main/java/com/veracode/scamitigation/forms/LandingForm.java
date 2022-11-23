package com.veracode.scamitigation.forms;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.veracode.scamitigation.api.VeracodeApi;
import com.veracode.scamitigation.dialogs.MessageDialog;
import com.veracode.scamitigation.ui.components.CursorHandler;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class LandingForm  extends BaseForm {
    private JButton startWorkButton;
    private JButton validateCredentialsButton;
    private JPanel rootPanel;
    private static JFrame form;

    public LandingForm(JFrame frame) {
        super(frame);
        this.$$$setupUI$$$();
        this.startWorkButton.addActionListener(actionEvent -> {
            ViewScaFindingsForm.openPage();
            form.setVisible(false);
        });
        this.validateCredentialsButton.addActionListener(actionEvent -> {
            if (CursorHandler.runWithWaitCursor(form, VeracodeApi::validateCredentials)) {
                MessageDialog.showSuccessDialog("Credentials are valid");
            } else {
                MessageDialog.showErrorDialog("Invalid credentials!");
            }

        });
    }

    protected Container getRootPanel() {
        return this.rootPanel;
    }

    public static void openPage() {
        form = FormHandler.handleInitialize(LandingForm.class, "Landing Page", form);
    }

    public static void main(String[] args) {
        openPage();
    }

    private void $$$setupUI$$$() {
        this.rootPanel = new JPanel();
        this.rootPanel.setLayout(new GridLayoutManager(3, 5, new Insets(0, 0, 0, 0), -1, -1));
        this.startWorkButton = new JButton();
        this.startWorkButton.setText("Start Work");
        this.rootPanel.add(this.startWorkButton, new GridConstraints(0, 0, 3, 2, 9, 0, 3, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.validateCredentialsButton = new JButton();
        this.validateCredentialsButton.setText("Validate Credentials");
        this.rootPanel.add(this.validateCredentialsButton, new GridConstraints(0, 2, 1, 1, 5, 0, 3, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    }

    public JComponent $$$getRootComponent$$$() {
        return this.rootPanel;
    }

}
