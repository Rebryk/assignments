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
        public static final int MAXA = 255;

        public Node[] to;
        public boolean term;
        public int count;

        public Node() {
            this.term = false;
            this.count = 0;
            this.to = new Node[MAXA];
            for (int i = 0; i < Node.MAXA; ++i)
                this.to[i] = null;
        }
    }

    /*
    private class Pos {
        public int i = 0;
    }*/

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /*
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
    }*/

    private Node root = new Node();

    @Override
    public boolean add(String element) {
        if (contains(element))
            return false;

        Node curr = root;
        for (int i = 0; i < element.length(); i++) {
            int x = (int)element.charAt(i);
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
            int x = (int)element.charAt(i);
            if (curr.to[x] == null || curr.to[x].count == 0) {
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
            int x = (int)element.charAt(i);
            curr = curr.to[x];
        }
        curr.count--;
        curr.term = false;
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
            curr = curr.to[(int)prefix.charAt(i)];
        }
        return curr.count;
    }

    /*
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
    }*/

    private class SString {
        public String data = "";
    }

    private void dfs(Node node, String s, SString data) {
        if (node.term) {
            data.data += s + "#";
        }
        for (int i = 0; i < Node.MAXA; ++i) {
            if (node.to[i] != null && node.to[i].count != 0) {
                dfs(node.to[i], s + (char)(i), data);
            }
        }
    }

    @Override
    public void serialize(OutputStream out) throws SerializationException {
        SString data = new SString();
        dfs(root, "", data);
        try {
            out.write(data.data.getBytes());
        } catch (IOException e) {
            throw new SerializationException();
        }
    }

    /*
    private Node deserializeNode(String data, Pos p) throws SerializationException  {
        Node node = new Node();
        if (p.i < data.length() && data.charAt(p.i) == '!') {
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
        if (p.i < data.length())
            p.i++;
        return node;
    }*/


    @Override
    public void deserialize(InputStream in) throws SerializationException {
        try {
            root = new Node();

            int c = 0;
            String word = "";
            while ((c = in.read()) != -1) {
                char ch = (char)c;
                if (isAlpha(ch)) {
                    word += ch;
                } else {
                    add(word);
                    word = "";
                }
            }
            //root = deserializeNode(data, new Pos());
        } catch (IOException e) {
            throw new SerializationException();
        }
    }
}
