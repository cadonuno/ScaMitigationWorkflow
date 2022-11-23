package com.veracode.scamitigation.ui.actions;

import com.veracode.scamitigation.ExceptionHandler;
import com.veracode.scamitigation.dialogs.InputDialog;
import com.veracode.scamitigation.dialogs.MessageDialog;
import com.veracode.scamitigation.forms.CommentsForm;
import com.veracode.scamitigation.models.ScaMitigation;
import com.veracode.scamitigation.selenium.SeleniumWrapper;
import com.veracode.scamitigation.ui.components.CursorHandler;
import com.veracode.scamitigation.ui.components.StringModalWrapper;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.Consumer;
import javax.swing.JFrame;

public final class ButtonActionHandler {
    private static final int PLATFORM_BUTTON_INDEX = 8;
    private static final int APPROVE_BUTTON_INDEX = 9;
    private static final int REJECT_BUTTON_INDEX = 10;
    private static final int COMMENTS_BUTTON_INDEX = 11;

    private ButtonActionHandler() {
    }

    public static void runButtonAction(int clickedColumn, ScaMitigation scaMitigation, JFrame frame) {
        switch (clickedColumn) {
            case 8:
                openOnPlatform(scaMitigation.getIssueLink());
                break;
            case 9:
                tryApproveMitigation(scaMitigation, frame);
                break;
            case 10:
                tryRejectMitigation(scaMitigation, frame);
                break;
            case 11:
                openCommentsForm(scaMitigation, frame);
        }

    }

    private static void tryApproveMitigation(ScaMitigation scaMitigation, JFrame frame) {
        executeOnModal(frame, "Please enter an approval message:", (modalResult) -> {
            if (SeleniumWrapper.approveMitigation(modalResult.getTheString(), scaMitigation.getIssueLink())) {
                MessageDialog.showSuccessDialog("Mitigation approved!");
            } else {
                MessageDialog.showErrorDialog("Unable to approve mitigation!");
            }

        }, "Please enter a mitigation approval message");
    }

    private static void tryRejectMitigation(ScaMitigation scaMitigation, JFrame frame) {
        executeOnModal(frame, "Please enter a rejection message:", (modalResult) -> {
            if (SeleniumWrapper.rejectMitigation(modalResult.getTheString(), scaMitigation.getIssueLink())) {
                MessageDialog.showErrorDialog("Mitigation rejected!");
            } else {
                MessageDialog.showErrorDialog("Unable to reject mitigation!");
            }

        }, "Please enter a mitigation rejection message");
    }

    private static void executeOnModal(JFrame frame, String modalTitle, Consumer<StringModalWrapper> modalResultConsumer, String failMessage) {
        Optional<StringModalWrapper> modalResult = InputDialog.getModalResult(modalTitle);
        if (modalResult.isPresent()) {
            if (((StringModalWrapper)modalResult.get()).isCancel()) {
                return;
            }

            CursorHandler.runWithWaitCursor(frame, () -> {
                modalResultConsumer.accept((StringModalWrapper)modalResult.get());
            });
        } else {
            MessageDialog.showErrorDialog(failMessage);
        }

    }

    private static void openCommentsForm(ScaMitigation scaMitigation, JFrame frame) {
        CursorHandler.runWithWaitCursor(frame, () -> {
            CommentsForm.openPage();
            CommentsForm.setScaMitigation(scaMitigation);
        });
    }

    private static void openOnPlatform(String issueLink) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(issueLink));
            } catch (URISyntaxException | IOException e) {
                ExceptionHandler.logException(e);
            }
        }

    }
}