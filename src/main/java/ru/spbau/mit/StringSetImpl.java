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
    private class Node {
        public static final int MAXA = 60;

        public Node[] to;
        public boolean term;
        public int count;

        public Node() {
            this.term = false;
            this.count = 0;
            this.to = new Node[MAXA];
        }
    }

    private class Pos {
        public int i = 0;
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private int getCode(char c) {
        if (c >= 'a' && c <= 'z') {
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

    private Node root = new Node();

    @Override
    public boolean add(String element) {
        if (contains(element))
            return false;

        Node curr = root;
        for (int i = 0; i < element.length(); i++) {
            int x = getCode(element.charAt(i));
            curr.count++;
            if (curr.to[x] == null) {
                curr.to[x] = new Node();
            }
            curr = curr.to[x];
        }
        curr.count++;
        curr.term = true;
        return true;
    }

    @Override
    public boolean contains(String element) {
        Node curr = root;
        for (int i = 0; i < element.length(); i++) {
            int x = getCode(element.charAt(i));
            if (curr.to[x] == null) {
                return false;
            }
            curr = curr.to[x];
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
            int x = getCode(element.charAt(i));
            if (curr.to[x].count == 1) {
                curr.to[x] = null;
                return true;
            } else {
                curr = curr.to[x];
            }
        }

        if (curr != null) {
            curr.count--;
            curr.term = false;
        }

        return true;
    }

    @Override
    public int size() {
        return root.count;
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        if (!contains(prefix)) {
            return 0;
        }
        Node curr = root;
        for (int i = 0; i < prefix.length(); i++) {
            curr = curr.to[getCode(prefix.charAt(i))];
        }
        return curr.count;
    }

    private String serializeNode(Node node) {
        String data = "";
        if (node.term) {
            data += "!";
        }
        for (int i = 0; i < Node.MAXA; ++i) {
            if (node.to[i] == null) {
                continue;
            }
            data += getChar(i) + serializeNode(node.to[i]) + "#";
        }
        return data;
    }

    @Override
    public void serialize(OutputStream out) throws SerializationException {
        String data = serializeNode(root);
        try {
            out.write(data.getBytes());
        } catch (IOException e) {
            throw new SerializationException();
        }
    }

    private Node deserializeNode(String data, Pos p) {
        Node node = new Node();
        if (data.charAt(p.i) == '!') {
            p.i++;
            node.term = true;
            node.count = 1;
        }
        while (p.i < data.length() && isAlpha(data.charAt(p.i))) {
            int x = getCode(data.charAt(p.i));
            p.i++;
            node.to[x] = deserializeNode(data, p);
            node.count += node.to[x].count;
        }
        p.i++;
        return node;
    }

    @Override
    public void deserialize(InputStream in) throws SerializationException {
        try {
            int c = 0;
            String data = "";
            while ((c = in.read()) != -1) {
                data += (char)c;
            }
            root = deserializeNode(data, new Pos());
        } catch (IOException e) {
            throw new SerializationException();
        }
    }
}
