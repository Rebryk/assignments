package ru.spbau.mit;

import java.util.*;
import java.util.NoSuchElementException;

public class TreeSetImpl<E> extends AbstractSet<E> {
    private Comparator<E> comparator;
    private Node<E> root = null;

    public TreeSetImpl(Comparator<E> comparator) {
        this.comparator = comparator;
    }

    public int size() {
        return root == null ? 0 : root.size;
    }

    public boolean add(E e) {
        if (find(root, e) != null)
            return false;
        root = insert(root, new Node<E>(e));
        return true;
    }

    public boolean remove(Object o) {
        E key = (E)o;
        Node<E> node = find(root, key);
        if (node == null)
            return false;
        root = erase(root, node);
        return true;
    }

    public boolean contains(Object o) {
        return find(root, (E)o) != null;
    }

    public treeIterator iterator() {
        return new treeIterator();
    }

    private class treeIterator implements Iterator<E> {
        private Node<E> currNode;
        private Node<E> last;

        public treeIterator() {
            last = null;
            currNode = root;
            if (currNode != null) {
                while (currNode.leftNode != null)
                    currNode = currNode.leftNode;
            }
        }

        public void remove() throws IllegalStateException {
            if (last == null)
                throw new IllegalStateException();
            root = erase(root, last);
            last = null;
        }

        public E next() throws NoSuchElementException {
            if (currNode == null)
                throw new NoSuchElementException();
            last = currNode;
            E key = currNode.key;
            currNode = nextNode();
            return key;
        }

        private Node<E> nextNode() {
            Node<E> next = currNode;
            if (next == null)
                return next;
            if (next.rightNode != null) {
                next = next.rightNode;
                while (next.leftNode != null)
                    next = next.leftNode;
            } else {
                while (next.parent != null && next.parent.rightNode == next)
                    next = next.parent;
                if (next != null)
                    next = next.parent;
            }
            return next;
        }

        public boolean hasNext() {
            return currNode != null ? true : false;
        }
    };

    private static class Node<T> {
        private static final Random rand = new Random();
        private static final int MAX_PRIORITY = 1000000000;

        private final int priority;
        private T key;
        private Node<T> leftNode;
        private Node<T> rightNode;
        private Node<T> parent;

        private int size;

        private Node(T key) {
            this.key = key;
            this.priority = rand.nextInt(MAX_PRIORITY);
            leftNode = null;
            rightNode = null;
            parent = null;
            size = 1;
        }

        private void update() {
            size = 1;
            // ???
            parent = null;
            if (leftNode != null) {
                size += leftNode.size;
                leftNode.parent = this;
            }
            if (rightNode != null) {
                size += rightNode.size;
                rightNode.parent = this;
            }
        }
    };

    private Pair<Node<E>> split(Node<E> root, E key) {
        if (root == null)
            return new Pair<Node<E>>(null, null);
        if (comparator.compare(key, root.key) == -1) {
            Pair<Node<E>> p = split(root.leftNode, key);
            root.leftNode = p.second;
            root.update();
            return new Pair<Node<E>>(p.first, root);
        } else {
            Pair<Node<E>> p = split(root.rightNode, key);
            root.rightNode = p.first;
            root.update();
            return new Pair<Node<E>>(root, p.second);
        }
    }

    private Node<E> merge(Node<E> left, Node<E> right) {
        if (left == null || right == null)
            return left == null ? right : left;
        if (left.priority > right.priority) {
            left.rightNode = merge(left.rightNode, right);
            left.update();
            return left;
        } else {
            right.leftNode = merge(left, right.leftNode);
            right.update();
            return right;
        }
    }

    private Node<E> insert(Node<E> root, Node<E> node) {
        if (root == null)
            return node;
        if (node.priority < root.priority) {
            if (comparator.compare(node.key, root.key) == -1)
                root.leftNode = insert(root.leftNode, node);
            else
                root.rightNode = insert(root.rightNode, node);
            root.update();
            return root;
        } else {
            Pair<Node<E>> p = split(root, node.key);
            node.leftNode = p.first;
            node.rightNode = p.second;
            node.update();
            return node;
        }
    }

    // tree must contains element
    private Node<E> erase(Node<E> root, Node<E> node) {
        if (comparator.compare(node.key, root.key) == 0)
            return merge(root.leftNode, root.rightNode);
        if (comparator.compare(node.key, root.key) == -1)
            root.leftNode = erase(root.leftNode, node);
        if (comparator.compare(node.key, root.key) == 1)
            root.rightNode = erase(root.rightNode, node);
        root.update();
        return root;
    }

    private Node<E> find(Node<E> root, E key) {
        if (root == null)
            return null;
        if (comparator.compare(key, root.key) == -1)
            return find(root.leftNode, key);
        if (comparator.compare(key, root.key) == 1)
            return find(root.rightNode, key);
        return root;
    }

}
