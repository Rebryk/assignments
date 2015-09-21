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

    private int getCode(char c) {
        if (c <= 'z') {
            return c - 'a';
        } else {
            return c - 'A' + 'z';
        }
    }

    private char getChar(int code) {
        if (code <= 'z' - 'a') {
            return (char)(code + 'a');
        } else {
            return (char)(code + 'A' - 'z');
        }
    }

    private class Node {
        public static final int MAXA = 52;

        public boolean term = false;
        public int count = 0;
        public Node[] to = new Node[MAXA];

        public String serialize() {
            String data = "";
            if (this.term) {
                data += "!";
            }
            for (int i = 0; i < MAXA; i++) {
                if (to[i] != null) {
                    data += getChar(i) + to[i].serialize();
                }
            }
            return data + "#";
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
            curr.count++;
            int index = getCode(element.charAt(i));
            if (curr.to[index] == null) {
                curr.to[index] = new Node();
            }
            curr = curr.to[index];
        }
        curr.count++;
        curr.term = true;
        return true;
    }

    @Override
    public boolean contains(String element) {
        Node curr = root;
        for (int i = 0; i < element.length(); i++) {
            int index = getCode(element.charAt(i));
            if (curr.to[index] == null) {
                return false;
            }
            curr = curr.to[index];
        }
        return curr.term;
    }

    @Override
    public boolean remove(String element) {
        if (!contains(element)) {
            return false;
        }
        Node curr = root;
        for (int i = 0; i < element.length(); i++) {
            curr.count--;
            int index = getCode(element.charAt(i));
            curr = curr.to[index];
        }
        curr.count--;
        curr.term = false;

        // clear memory
        curr = root;
        for (int i = 0; i < element.length(); i++) {
            int index = getCode(element.charAt(i));
            if (curr.to[index].count == 0) {
                curr.to[index] = null;
                break;
            }
            curr = curr.to[index];
        }

        return true;
    }

    @Override
    public int size() {
        return root.count;
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        Node curr = root;
        for (int i = 0; i < prefix.length(); i++) {
            int index = getCode(prefix.charAt(i));
            if (curr.to[index] == null) {
                return 0;
            }
            curr = curr.to[index];
        }
        return curr.count;
    }

    @Override
    public void serialize(OutputStream out) throws SerializationException {
        String data = root.serialize();
        try {
            out.write(data.getBytes());
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
            node.count++;
            node.term = true;
            c = readChar(in);
        }

        while (isAlpha(c)) {
            node.to[getCode(c)] = deserializeNode(in);
            node.count += node.to[getCode(c)].count;
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
