package com.hob.hobsystem.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Our own SINGLY LINKED LIST. Every node holds one item and a link to the next node, and the list
 * remembers its head (first node) and tail (last node).
 * Adding at the end and removing the first node take constant time, and a node in the middle can be
 * unlinked without shifting the other items. That is why the waitlist uses it.
 * ENCAPSULATION: the nodes are private, so other classes can only use the methods below.
 */
public class SinglyLinkedList<T> implements Iterable<T> {

    /** One node: the data and the link to the next node. */
    private static class Node<T> {
        final T data;
        Node<T> next;
        Node(T data) { this.data = data; }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    /** Adds an item after the last node (enqueue). */
    public void addLast(T item) {
        Node<T> n = new Node<>(item);
        if (tail == null) head = n;      // empty list: the new node is also the head
        else tail.next = n;
        tail = n;
        size++;
    }

    /** Adds an item before the first node. */
    public void addFirst(T item) {
        Node<T> n = new Node<>(item);
        n.next = head;
        head = n;
        if (tail == null) tail = n;
        size++;
    }

    /** Removes and returns the first item (dequeue), or null when the list is empty. */
    public T removeFirst() {
        if (head == null) return null;
        T item = head.data;
        head = head.next;
        if (head == null) tail = null;   // the list became empty
        size--;
        return item;
    }

    /** Returns the first item without removing it, or null when the list is empty. */
    public T peekFirst() { return head == null ? null : head.data; }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    /** Copies the items, from first to last, into a normal list. */
    public List<T> toList() {
        List<T> out = new ArrayList<>(size);
        for (Node<T> n = head; n != null; n = n.next) out.add(n.data);
        return out;
    }

    /** Walks from the head to the tail. The iterator can also unlink the node it just returned. */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Node<T> prevOfLast;   // node before the one returned last
            private Node<T> last;         // node returned by the last next()
            private Node<T> next = head;  // node that next() will return

            @Override public boolean hasNext() { return next != null; }

            @Override public T next() {
                if (next == null) throw new NoSuchElementException();
                if (last != null) prevOfLast = last;   // step forward (only if last was not removed)
                last = next;
                next = next.next;
                return last.data;
            }

            @Override public void remove() {
                if (last == null) throw new IllegalStateException();
                if (prevOfLast == null) head = last.next;   // removing the head
                else prevOfLast.next = last.next;           // skip over the node
                if (last == tail) tail = prevOfLast;        // removing the tail
                size--;
                last = null;                                // prevOfLast stays the same
            }
        };
    }
}
