package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Question;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;

public class AdminQuestionController {

    @FXML
    private TableView<Question> questionTable;

    @FXML
    private TableColumn<Question, String> colUser;

    @FXML
    private TableColumn<Question, String> colContent;

    private final ObservableList<Question> questionList = FXCollections.observableArrayList();

    private static final String FILE = "data/question.csv";

    @FXML
    public void initialize() {
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colContent.setCellValueFactory(new PropertyValueFactory<>("content"));

        loadQuestions();
    }

    private void loadQuestions() {
        questionList.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 只解析出 username 和 content
                String[] parts = line.split(",", 4);
                if (parts.length == 4) {
                    String username = parts[1];
                    String content = parts[2];

                    // ID 和时间都不需要，但构造函数必须提供（按你的 Question 类要求）
                    Question q = new Question(parts[0], username, content, LocalDateTime.parse(parts[3]));

                    questionList.add(q);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        questionTable.setItems(questionList);
    }
}
