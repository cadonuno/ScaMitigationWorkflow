package com.veracode.scamitigation.forms;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.veracode.scamitigation.DateHandler;
import com.veracode.scamitigation.TypeOfSearchEnum;
import com.veracode.scamitigation.api.VeracodeApi;
import com.veracode.scamitigation.dialogs.MessageDialog;
import com.veracode.scamitigation.models.ScaMitigation;
import com.veracode.scamitigation.selenium.ExecutionParameters;
import com.veracode.scamitigation.selenium.SeleniumWrapper;
import com.veracode.scamitigation.ui.actions.ButtonActionHandler;
import com.veracode.scamitigation.ui.components.CursorHandler;
import com.veracode.scamitigation.ui.components.ScaFindingsTableModel;
import com.veracode.scamitigation.ui.components.TableColumnAdjuster;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.jdatepicker.impl.DateComponentFormatter;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

public class ViewScaFindingsForm extends BaseForm {
    private static JFrame form;
    private JDatePanelImpl issuesSinceDatePanel;
    private JDatePanelImpl issuesUntilDatePanel;
    private JButton getOpenIssuesButton;
    private JPanel headerPanel;
    private JPanel rootPanel;
    private JTable mitigationsTable;
    private JTextField workspaceTextField;
    private JTextField projectTextField;
    private JLabel workspaceLabel;
    private JLabel projectLabel;
    private JComboBox typesOfIssuesComboBox;
    private final Map<Integer, ScaMitigation> rowToScaMitigation = new HashMap();

    public ViewScaFindingsForm(JFrame frame) {
        super(frame);
        this.$$$setupUI$$$();
        this.initializeTable();
        this.setFilterFieldSize(this.workspaceTextField);
        this.setFilterFieldSize(this.projectTextField);
        this.getOpenIssuesButton.addActionListener((e) -> {
            this.getOpenIssues();
        });
    }

    private void setFilterFieldSize(JTextField textField) {
        textField.setSize(100, (int) this.getOpenIssuesButton.getPreferredSize().getHeight());
        textField.setPreferredSize(new Dimension(100, (int) this.getOpenIssuesButton.getPreferredSize().getHeight()));
    }

    private void getOpenIssues() {
        CursorHandler.runWithWaitCursor(this.frame, () -> {
            TypeOfSearchEnum typeOfSearchEnum = TypeOfSearchEnum.fromIndex(this.typesOfIssuesComboBox.getSelectedIndex());
            List<ScaMitigation> allMitigations = VeracodeApi.getAllIgnoredFindings(
                    this.workspaceTextField.getText(), this.projectTextField.getText(),
                    typeOfSearchEnum == TypeOfSearchEnum.REJECTED_ISSUES);
            if (allMitigations.isEmpty()) {
                this.handleNoIssuesFound();
            } else {
                SeleniumWrapper.tryLoginWithNewDriver().ifPresent((webDriver) -> {
                    Date startDateFilter = (Date) this.issuesSinceDatePanel.getModel().getValue();
                    Date endDateFilter = (Date) this.issuesUntilDatePanel.getModel().getValue();

                    try {
                        allMitigations.forEach((mitigation) -> {
                            mitigation.readCommentsFromPlatform(webDriver);
                            mitigation.populateDateLastIgnore(typeOfSearchEnum);
                        });
                        this.populateTable(allMitigations.stream()
                                .filter(scaMitigation ->
                                        scaMitigation.isMitigationOpenBetween(startDateFilter, endDateFilter))
                                .collect(Collectors.toList()));
                    } finally {
                        webDriver.close();
                    }
                });
            }
        });
    }

    private void populateTable(List<ScaMitigation> allMitigationsSince) {
        if (allMitigationsSince.isEmpty()) {
            this.handleNoIssuesFound();
        } else {
            CursorHandler.runWithWaitCursor(this.frame, () -> {
                ScaFindingsTableModel baseModel = new ScaFindingsTableModel(this.mitigationsTable);
                this.rowToScaMitigation.clear();

                for (int currentScaMitigationIndex = 0; currentScaMitigationIndex < allMitigationsSince.size(); ++currentScaMitigationIndex) {
                    this.populateRow(baseModel, allMitigationsSince.get(currentScaMitigationIndex), currentScaMitigationIndex);
                }

                this.mitigationsTable.setModel(baseModel);
                this.mitigationsTable.addMouseListener(this.getMouseAdapterForTable());
                baseModel.initializeRenderers();
                this.adjustSize(false);
            });
        }
    }

    private void handleNoIssuesFound() {
        MessageDialog.showWarningDialog("No issues found");
    }

    private MouseAdapter getMouseAdapterForTable() {
        return new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                int clickedRow = ViewScaFindingsForm.this.mitigationsTable.rowAtPoint(mouseEvent.getPoint());
                int clickedColumn = ViewScaFindingsForm.this.mitigationsTable.columnAtPoint(mouseEvent.getPoint());
                if (clickedRow >= 0 && clickedColumn >= 8) {
                    Optional.ofNullable(ViewScaFindingsForm.this.rowToScaMitigation.get(clickedRow)).ifPresent((scaMitigations) -> {
                        ButtonActionHandler.runButtonAction(clickedColumn, scaMitigations, ViewScaFindingsForm.this.frame);
                    });
                }

            }
        };
    }

    private void adjustSize(boolean isInitial) {
        Dimension dimensions = TableColumnAdjuster.adjustTable(this.mitigationsTable);
        this.rootPanel.revalidate();
        this.rootPanel.repaint();
        int width;
        int height;
        if (isInitial) {
            width = (int) this.mitigationsTable.getPreferredSize().getWidth();
            height = this.getPageHeight();
        } else {
            width = (int) dimensions.getWidth();
            height = (int) dimensions.getHeight() + this.headerPanel.getHeight();
        }

        height = Math.min(height, 768);
        this.rootPanel.setPreferredSize(new Dimension(width, height));
        this.rootPanel.setSize(width, height);
        this.rootPanel.revalidate();
        this.rootPanel.repaint();
        if (!isInitial) {
            form.pack();
        }

    }

    private int getPageHeight() {
        return (int) (this.mitigationsTable.getPreferredSize().getHeight() + this.headerPanel.getPreferredSize().getHeight());
    }

    private void populateRow(ScaFindingsTableModel baseModel, ScaMitigation mitigation, int currentRow) {
        JButton seeOnPlatformButton = new JButton("Open in Platform");
        JButton approveButton = new JButton("Approve");
        JButton rejectButton = new JButton("Reject");
        JButton commentsButton = new JButton("Comments");
        Arrays.asList(seeOnPlatformButton, approveButton, rejectButton, commentsButton).forEach(this::adjustButtonSize);
        this.rowToScaMitigation.put(currentRow, mitigation);
        baseModel.addRow(new Object[]{DateHandler.getDateAsString(mitigation.getDateLastIgnore()), mitigation.getWorkspace(), mitigation.getProject(), mitigation.getIssueId(), mitigation.getCveId(), mitigation.getDescription(), mitigation.getLibrary(), mitigation.getVersion(), seeOnPlatformButton, approveButton, rejectButton, commentsButton});
    }

    private void adjustButtonSize(JButton button) {
        int width = button.getText().length() * button.getFont().getSize() + (int) this.mitigationsTable.getIntercellSpacing().getWidth();
        button.setSize(new Dimension(width, button.getHeight()));
        button.setPreferredSize(new Dimension(width, (int) button.getPreferredSize().getHeight()));
    }

    private void initializeTable() {
        this.mitigationsTable.setModel(new ScaFindingsTableModel(this.mitigationsTable, 25));
        this.adjustSize(true);
    }

    protected Container getRootPanel() {
        return this.rootPanel;
    }

    public static void openPage() {
        form = FormHandler.handleInitialize(ViewScaFindingsForm.class, "SCA Proposed Mitigations", form);
    }

    public void afterLoad() {
        Properties properties = new Properties();
        properties.put("text.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");
        this.issuesSinceDatePanel = new JDatePanelImpl(new UtilDateModel(), properties);
        JDatePickerImpl datePickerSince = new JDatePickerImpl(this.issuesSinceDatePanel, new DateComponentFormatter());
        Dimension preferredSize = new Dimension(120, (int) datePickerSince.getPreferredSize().getHeight());
        datePickerSince.setSize(preferredSize);
        datePickerSince.setPreferredSize(preferredSize);
        this.issuesUntilDatePanel = new JDatePanelImpl(new UtilDateModel(), properties);
        JDatePickerImpl datePickerUntil = new JDatePickerImpl(this.issuesUntilDatePanel, new DateComponentFormatter());
        datePickerUntil.setSize(preferredSize);
        datePickerUntil.setPreferredSize(preferredSize);
        this.headerPanel.add(datePickerSince, 1);
        this.headerPanel.add(datePickerUntil, 3);
        this.headerPanel.revalidate();
        this.headerPanel.repaint();
    }

    private void $$$setupUI$$$() {
        this.rootPanel = new JPanel();
        this.rootPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        this.headerPanel = new JPanel();
        this.headerPanel.setLayout(new FlowLayout(1, 5, 5));
        this.rootPanel.add(this.headerPanel, new GridConstraints(0, 0, 1, 1, 0, 3, 3, 3, (Dimension) null, (Dimension) null, (Dimension) null, 0, false));
        JLabel label1 = new JLabel();
        label1.setText("Get Issues Since:");
        this.headerPanel.add(label1);
        JLabel label2 = new JLabel();
        label2.setText("Get Issues Until:");
        this.headerPanel.add(label2);
        this.workspaceLabel = new JLabel();
        this.workspaceLabel.setText("Workspace:");
        this.headerPanel.add(this.workspaceLabel);
        this.workspaceTextField = new JTextField();
        this.headerPanel.add(this.workspaceTextField);
        this.projectLabel = new JLabel();
        this.projectLabel.setText("Project:");
        this.headerPanel.add(this.projectLabel);
        this.projectTextField = new JTextField();
        this.headerPanel.add(this.projectTextField);
        this.typesOfIssuesComboBox = new JComboBox();
        DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Find All Open Issues");
        defaultComboBoxModel1.addElement("Find All Rejected Issues");
        defaultComboBoxModel1.addElement("Find All Approved Issues");
        this.typesOfIssuesComboBox.setModel(defaultComboBoxModel1);
        this.headerPanel.add(this.typesOfIssuesComboBox);
        this.getOpenIssuesButton = new JButton();
        this.getOpenIssuesButton.setText("Get Open Issues");
        this.headerPanel.add(this.getOpenIssuesButton);
        JScrollPane scrollPane1 = new JScrollPane();
        this.rootPanel.add(scrollPane1, new GridConstraints(1, 0, 1, 1, 0, 3, 5, 5, (Dimension) null, (Dimension) null, (Dimension) null, 0, false));
        this.mitigationsTable = new JTable();
        scrollPane1.setViewportView(this.mitigationsTable);
    }

    public JComponent $$$getRootComponent$$$() {
        return this.rootPanel;
    }
}
