package com.hob.hobsystem.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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

    public void addLast(T item) {
        Node<T> n = new Node<>(item);
        if (tail == null) head = n;      
        else tail.next = n;
        tail = n;
        size++;
    }

    public void addFirst(T item) {
        Node<T> n = new Node<>(item);
        n.next = head;
        head = n;
        if (tail == null) tail = n;
        size++;
    }

    public T removeFirst() {
        if (head == null) return null;
        T item = head.data;
        head = head.next;
        if (head == null) tail = null;   // the list became empty
        size--;
        return item;
    }

    public T peekFirst() { return head == null ? null : head.data; }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public List<T> toList() {
        List<T> out = new ArrayList<>(size);
        for (Node<T> n = head; n != null; n = n.next) out.add(n.data);
        return out;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Node<T> prevOfLast; 
            private Node<T> last;       
            private Node<T> next = head;  

            @Override public boolean hasNext() { return next != null; }

            @Override public T next() {
                if (next == null) throw new NoSuchElementException();
                if (last != null) prevOfLast = last;   
                last = next;
                next = next.next;
                return last.data;
            }

            @Override public void remove() {
                if (last == null) throw new IllegalStateException();
                if (prevOfLast == null) head = last.next;   
                else prevOfLast.next = last.next;           
                if (last == tail) tail = prevOfLast;     
                size--;
                last = null;                               
            }
        };
    }
}
