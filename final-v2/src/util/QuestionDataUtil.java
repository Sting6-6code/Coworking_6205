package util;

import datastructure.ResizableArrayBag;
import model.Question;

import java.io.*;
import java.time.LocalDateTime;

public class QuestionDataUtil {

    private static final String FILE = "data/question.csv";

    private static ResizableArrayBag<Question> questionBag = new ResizableArrayBag<>();

    // ----- Read CSV on class load -----
    static {
        loadQuestions();
    }

    /**
     * Add question (in-memory + append to CSV)
     */
    public static void addQuestion(Question q) {
        questionBag.add(q);
        appendQuestionToFile(q);  // ⚠ No longer overwrites the entire file
    }

    /**
     * Remove question (in-memory + rewrite CSV)
     */
    public static void removeQuestion(Question q) {
        questionBag.remove(q);
        saveAllQuestions(); // Remove requires rewrite
    }

    /**
     * Get all questions
     */
    public static Question[] getAllQuestions() {
        return questionBag.toArray(new Question[0]);
    }


    // =========================================================
    //               private Read/Write CSV Methods
    // =========================================================

    /**
     * Append the latest data (does not overwrite original file)
     */
    private static void appendQuestionToFile(Question q) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE, true))) {

            pw.println(String.join(",",
                    q.getQuestionId(),
                    q.getUsername(),
                    q.getContent().replace(",", " "),   // Avoid CSV column misalignment
                    q.getCreatedAt().toString()
            ));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Rewrite entire CSV when deleting
     */
    private static void saveAllQuestions() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {

            Question[] all = questionBag.toArray(new Question[0]);

            for (Question q : all) {
                pw.println(String.join(",",
                        q.getQuestionId(),
                        q.getUsername(),
                        q.getContent().replace(",", " "),
                        q.getCreatedAt().toString()
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Read CSV data on startup
     */
    private static void loadQuestions() {
        File file = new File(FILE);
        if (!file.exists()) {
            System.out.println("question.csv not found, skip loading.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {

                String[] parts = line.split(",");
                if (parts.length != 4) continue; // Skip broken lines

                Question q = new Question(
                        parts[0],                   // questionId
                        parts[1],                   // username
                        parts[2],                   // content
                        LocalDateTime.parse(parts[3]) // createdAt
                );

                questionBag.add(q);
            }

            System.out.println("Loaded " + questionBag.toArray(new Question[0]).length + " questions.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
