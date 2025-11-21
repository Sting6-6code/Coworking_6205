package util;

import datastructure.ResizableArrayBag;
import model.Question;

import java.io.*;
import java.time.LocalDateTime;

public class QuestionDataUtil {

    private static final String FILE = "data/question.csv";

    private static ResizableArrayBag<Question> questionBag = new ResizableArrayBag<>();

    // ----- 在类加载时读取 CSV -----
    static {
        loadQuestions();
    }

    /**
     * 添加问题（内存 + 追加写 CSV）
     */
    public static void addQuestion(Question q) {
        questionBag.add(q);
        appendQuestionToFile(q);  // ⚠ 不再覆盖整个文件
    }

    /**
     * 删除问题（内存 + 重写 CSV）
     */
    public static void removeQuestion(Question q) {
        questionBag.remove(q);
        saveAllQuestions(); // 删除只能重写
    }

    /**
     * 获取所有问题
     */
    public static Question[] getAllQuestions() {
        return questionBag.toArray(new Question[0]);
    }


    // =========================================================
    //               private 读写 CSV 方法
    // =========================================================

    /**
     * 追加写入最新一条数据（不会覆盖原文件）
     */
    private static void appendQuestionToFile(Question q) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE, true))) {

            pw.println(String.join(",",
                    q.getQuestionId(),
                    q.getUsername(),
                    q.getContent().replace(",", " "),   // 避免 CSV 列错位
                    q.getCreatedAt().toString()
            ));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除时需要重写整个 CSV
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
     * 启动时读取 CSV 数据
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
                if (parts.length != 4) continue; // 跳过损坏行

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
