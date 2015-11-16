package ru.spbau.mit;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


public class QuizGame implements Game {
    private Integer delayUntilNextLetter;
    private Integer maxLettersToOpen;
    private String dictionaryFilename;

    private final GameServer server;
    private Thread gameThread;

    private ArrayList<Question> questions;
    private int currentQuestionIndex;

    private Boolean isStopped;

    public QuizGame(GameServer server) {
        this.server = server;
        this.isStopped = true;
        questions = new ArrayList<Question>();
    }

    public void setDelayUntilNextLetter(Integer delay) {
        delayUntilNextLetter = delay;
    }

    public void setMaxLettersToOpen(Integer count) {
        maxLettersToOpen = count;
    }

    public void setDictionaryFilename(String fileName) {
        dictionaryFilename = fileName;
    }

    private Question getCurrentQuestion() {
        return questions.get(currentQuestionIndex);
    }

    private void nextQuestion() {
        if (questions.size() == 0) {
            try {
                loadQuestions();
            } catch (FileNotFoundException e) {
                // do nothing)
            }
        }
        currentQuestionIndex = (currentQuestionIndex + 1) % questions.size();
    }

    @Override
    public void onPlayerConnected(String id) {
    }

    @Override
    public void onPlayerSentMsg(String id, String msg) {
        synchronized (this) {
            if (msg.equals("!start")) {
                if (isStopped) {
                    System.out.println("Game: message from " + id + ": " + msg);
                    if (gameThread != null) {
                        try {
                            gameThread.join();
                        } catch (InterruptedException e) {
                           // do nothing)
                        }
                    }
                    isStopped = false;
                    gameThread = new Thread(new QuizRunnable());
                    gameThread.start();
                }
            } else if (msg.equals("!stop")) {
                System.out.println("Game: message from " + id + ": " + msg);
                isStopped = true;
                gameThread.interrupt();
                server.broadcast("Game has been stopped by " + id);
            } else if (msg.equals(getCurrentQuestion().answer)) {
                System.out.println("Game: message from " + id + ": " + msg);
                server.broadcast("The winner is " + id);
                gameThread.interrupt();
            } else {
                System.out.println("Game: message from " + id + ": " + msg);
                server.sendTo(id, "Wrong try");
            }
        }
    }

    private class QuizRunnable implements Runnable {
        @Override
        public void run() {
            System.out.println("Quiz: started " + Thread.currentThread().getName().toString());

            while (!isStopped) {
                System.out.println("Quiz: new question");
                nextQuestion();
                Question question = getCurrentQuestion();

                System.out.println("Quiz: round started: " + question.question + " (" + question.answer.length() + " letters)");
                server.broadcast("New round started: " + question.question + " (" + question.answer.length() + " letters)");

                Boolean stopped = false;
                for (int index = 0; index < maxLettersToOpen; ++index) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(delayUntilNextLetter);
                    } catch (InterruptedException e) {
                        stopped = true;
                        System.out.println("Quiz: interrupted, letter");
                        break;
                    }
                    if (!Thread.interrupted()) {
                        System.out.println("Quiz: " + "Current prefix is " + question.answer.substring(0, index + 1));
                        server.broadcast("Current prefix is " + question.answer.substring(0, index + 1));
                    } else {
                        stopped = true;
                        break;
                    }
                }

                if (stopped) {
                    continue;
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(delayUntilNextLetter);
                } catch (InterruptedException e) {
                    System.out.println("Quiz: interrupted, end");
                    continue;
                }

                if (!Thread.interrupted()) {
                    System.out.println("Quiz: " + "Nobody guessed, the word was " + question.answer);
                    server.broadcast("Nobody guessed, the word was " + question.answer);
                }
            }

            System.out.println("Quiz: stopped");
        }
    }

    private void loadQuestions() throws FileNotFoundException {
        try {
            synchronized (questions) {
                currentQuestionIndex = -1;
                Scanner scanner = new Scanner(new File(dictionaryFilename));
                while (scanner.hasNext()) {
                    Question question = new Question(scanner.nextLine());
                    questions.add(question);
                    System.out.println("Game: added (" + question.question + ", " + question.answer + ")");
                }
                System.out.println("Game: Read questions.");
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException();
        }
    }

    private class Question {
        private final String question;
        private final String answer;

        public Question(String string) {
            String[] data = string.split(";");
            this.question = data[0];
            this.answer = data[1];
        }
    }
}
