package ru.spbau.mit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Created by rebryk on 21/09/15.
 */

public class StringSetImpl implements StringSet, StreamSerializable {

    private static int getCode(char c) {
        if (c >= 'a' && c <= 'z') {
            return c - 'a';
        } else {
            return (c - 'A') + ('z' - 'a');
        }
    }

    private static char getChar(int code) {
        if (code <= ('z' - 'a')) {
            return (char)(code + 'a');
        } else {
            return (char)((code + 'A') - ('z' - 'a'));
        }
    }

    private class Node {
        private static final int ALPHABET_SIZE = 52;

        private boolean isTerminal = false;
        private int wordsCount = 0;
        private Node[] nodeByCharCode = new Node[ALPHABET_SIZE];

        private StringBuilder serialize() {
            StringBuilder data = new StringBuilder();
            if (isTerminal) {
                data.append('!');
            }
            for (int i = 0; i < ALPHABET_SIZE; i++) {
                if (nodeByCharCode[i] != null) {
                    data.append(getChar(i));
                    data.append(nodeByCharCode[i].serialize());
                }
            }
            return data.append('#');
        }
    }

    private Node root = new Node();

    @Override
    public boolean add(String element) {
        if (contains(element)) {
            return false;
        }

        Node curr = root;
        for (int i = 0; i < element.length(); ++i) {
            curr.wordsCount++;
            int index = getCode(element.charAt(i));
            if (curr.nodeByCharCode[index] == null) {
                curr.nodeByCharCode[index] = new Node();
            }
            curr = curr.nodeByCharCode[index];
        }
        curr.wordsCount++;
        curr.isTerminal = true;
        return true;
    }

    private Node goDown(String element) {
        Node curr = root;
        for (int i = 0; i < element.length(); i++) {
            int index = getCode(element.charAt(i));
            if (curr.nodeByCharCode[index] == null) {
                return null;
            }
            curr = curr.nodeByCharCode[index];
        }
        return curr;
    }

    @Override
    public boolean contains(String element) {
        Node node = goDown(element);
        return node != null && node.isTerminal;
    }

    @Override
    public boolean remove(String element) {
        if (contains(element)) {
            Node curr = root;
            for (int i = 0; i < element.length(); i++) {
                curr.wordsCount--;
                int index = getCode(element.charAt(i));
                if (curr.nodeByCharCode[index].wordsCount == 1) {
                    curr.nodeByCharCode[index] = null;
                    return true;
                }
                curr = curr.nodeByCharCode[index];
            }
            curr.wordsCount--;
            curr.isTerminal = false;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int size() {
        return root.wordsCount;
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        Node node = goDown(prefix);
        return node == null ? 0 : node.wordsCount;
    }

    @Override
    public void serialize(OutputStream out) throws SerializationException {
        StringBuilder data = root.serialize();
        try {
            out.write(data.toString().getBytes());
        } catch (IOException e) {
            throw new SerializationException();
        }
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private char readChar(InputStream in) throws IOException {
        return (char) in.read();
    }

    private Node deserializeNode(InputStream in) throws IOException {
        Node node = new Node();

        char c = readChar(in);
        if (c == '!') {
            node.wordsCount++;
            node.isTerminal = true;
            c = readChar(in);
        }

        while (isAlpha(c)) {
            node.nodeByCharCode[getCode(c)] = deserializeNode(in);
            node.wordsCount += node.nodeByCharCode[getCode(c)].wordsCount;
            c = readChar(in);
        }
        return node;
    }

    @Override
    public void deserialize(InputStream in) throws SerializationException {
        try {
            root = deserializeNode(in);
        } catch (IOException e) {
            throw new SerializationException();
        }
    }
}
