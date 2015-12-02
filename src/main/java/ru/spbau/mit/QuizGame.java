package ru.spbau.mit;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


public class QuizGame implements Game {
    private int delayUntilNextLetter;
    private int maxLettersToOpen;
    private String dictionaryFilename;

    private final GameServer server;
    private Thread gameThread;

    private ArrayList<Question> questions;
    private int currentQuestionIndex;

    private boolean isStopped;

    public QuizGame(GameServer server) {
        this.server = server;
        this.isStopped = true;
        questions = new ArrayList<Question>();
    }

    public void setDelayUntilNextLetter(int delay) {
        delayUntilNextLetter = delay;
    }

    public void setMaxLettersToOpen(int count) {
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
            loadQuestions();
        } else {
            currentQuestionIndex = (currentQuestionIndex + 1) % questions.size();
        }
    }

    @Override
    public void onPlayerConnected(String id) {
    }

    @Override
    synchronized public void onPlayerSentMsg(String id, String msg) {
        if (msg.equals("!start")) {
                if (gameThread != null) {
                    try {
                        gameThread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                }
                isStopped = false;
                gameThread = new Thread(new QuizRunnable());
                gameThread.start();
        } else if (msg.equals("!stop")) {
            isStopped = true;
            gameThread.interrupt();
            server.broadcast("Game has been stopped by " + id);
        } else if (msg.equals(getCurrentQuestion().answer)) {
            server.broadcast("The winner is " + id);
            gameThread.interrupt();
        } else {
            server.sendTo(id, "Wrong try");
        }
    }

    private class QuizRunnable implements Runnable {
        @Override
        public void run() {
            beforeWhile:
            while (!isStopped) {
                nextQuestion();
                Question question = getCurrentQuestion();
                server.broadcast("New round started: " + question.question + " (" + question.answer.length() + " letters)");

                for (int index = 0; index < maxLettersToOpen; ++index) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(delayUntilNextLetter);
                    } catch (InterruptedException e) {
                        continue beforeWhile;
                    }
                    server.broadcast("Current prefix is " + question.answer.substring(0, index + 1));
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(delayUntilNextLetter);
                } catch (InterruptedException e) {
                    continue;
                }

                if (!Thread.interrupted()) {
                    server.broadcast("Nobody guessed, the word was " + question.answer);
                }
            }
        }
    }

    private void loadQuestions() {
        try {
            synchronized (questions) {
                currentQuestionIndex = 0;
                Scanner scanner = new Scanner(new File(dictionaryFilename));
                while (scanner.hasNext()) {
                    Question question = new Question(scanner.nextLine());
                    questions.add(question);
                }
            }
        } catch (FileNotFoundException e) {
            throw new AssertionError("File not found!");
        }
    }

    private class Question {
        private final String question;
        private final String answer;

        private Question(String string) {
            String[] data = string.split(";");
            this.question = data[0];
            this.answer = data[1];
        }
    }
}
