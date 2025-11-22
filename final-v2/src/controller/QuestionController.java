package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import model.Question;
import util.CurrentUser;
import util.QuestionDataUtil;

public class QuestionController {

    @FXML
    private TextArea inputField;

    @FXML
    private Label statusLabel;

    @FXML
    private void onSubmit() {

        String content = inputField.getText().trim();

        if (content.isEmpty()) {
            statusLabel.setText("Please enter your question.");
            return;
        }

        if (CurrentUser.get() == null) {
            statusLabel.setText("CurrentUser is NULL! You must login first.");
            return;
        }

        String username = CurrentUser.get().getUsername();

        Question q = new Question(username, content);

        QuestionDataUtil.addQuestion(q);

        statusLabel.setText("Question submitted!");
        inputField.clear();
    }
}
