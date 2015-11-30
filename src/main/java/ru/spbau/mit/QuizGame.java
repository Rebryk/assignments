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

    private void nextQuestion() throws FileNotFoundException {
        if (questions.size() == 0) {
            try {
                loadQuestions();
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException();
            }
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
            while (!isStopped) {
                try {
                    nextQuestion();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException();
                }

                Question question = getCurrentQuestion();
                server.broadcast("New round started: " + question.question + " (" + question.answer.length() + " letters)");

                Boolean stopped = false;
                for (int index = 0; index < maxLettersToOpen; ++index) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(delayUntilNextLetter);
                    } catch (InterruptedException e) {
                        stopped = true;
                        break;
                    }
                    if (!Thread.interrupted()) {
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
                    continue;
                }

                if (!Thread.interrupted()) {
                    server.broadcast("Nobody guessed, the word was " + question.answer);
                }
            }
        }
    }

    private void loadQuestions() throws FileNotFoundException {
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
            throw new FileNotFoundException();
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
