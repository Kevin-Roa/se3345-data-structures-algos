/**
 * Project 2, SkipList Implementation
 * Written on 3/28/21
 * Written by Kevin Roa
 * Made for 
 *  Sridhar Alagar
 *  SE 3345, Algorythms and Data Structures
 */

package kar180005;

import java.util.Iterator;

public class SkipList<T extends Comparable<? super T>> {
    static final int PossibleLevels = 33; // Max levels Entry can occupy
    Entry<T>[] pred = new Entry[PossibleLevels]; // Path to a given Entry
    Entry<T> head, tail; // Start and End of the list
    int size; // Number of Entries in the list

    /*
     * Generic entry for the list Contains references to: Value stored in the entry
     * Arrays for next and previous entries given a level
     */
    static class Entry<E> {
        E element; // Value of Entry
        Entry<E>[] next, prev; // Arrays containing references to Entries at different levels
                               // Prev is hardly necessary but it was here so I implemented it

        // Construct new Entry
        public Entry(E x, int lev) {
            element = x;
            next = new Entry[lev];
            prev = new Entry[lev];
        }

        // Get the value of an Entry
        public E getElement() {
            return element;
        }

        // Get the level of an Entry
        public int level() {
            return next.length;
        }
    }

    /**
     * Constructor Initialize head and tail Entries to be start and end points of
     * list
     */
    public SkipList() {
        size = 0;

        // Create new Head and Tail Entries
        head = new Entry<T>(null, PossibleLevels);
        tail = new Entry<T>(null, PossibleLevels);

        // Set references to all indexes of Head and Tail next and prev values
        // At the start, all indexes point to Head and Tail
        for (int i = 0; i < PossibleLevels; i++) {
            head.next[i] = tail;
            tail.prev[i] = head;
        }
    }

    /**
     * Add x to list. If x already exists, reject it. Returns true if new node is
     * added to the list
     * 
     * @param x Value to be added to the lists
     * @return true if x added successfully, false if the list already contained x
     */
    public boolean add(T x) {
        // If list already contains x then return false, change nothing
        if (contains(x))
            return false;

        // Create a new Entry for the list
        int lvl = (int) (Math.random() * PossibleLevels) + 1;
        Entry<T> entry = new Entry<T>(x, lvl);

        // Add the entry to the list
        for (int i = 0; i < lvl; i++) {
            // Insert new Entry after predecessor
            entry.next[i] = pred[i].next[i];
            pred[i].next[i] = entry;

            // Set prev node values
            entry.prev[i] = pred[i];
            entry.next[i].prev[i] = entry;
        }

        size++;
        return true;
    }

    /**
     * Find smallest element that is greater or equal to x
     * 
     * @param x Value to compare against
     * @return The value of the smallest entry greater than or equal to x
     */
    public T ceiling(T x) {
        findPred(x);

        // Assuming x < max of list
        // No info was given on this check nor a return value therefore not implemented
        return pred[0].next[0].getElement();
    }

    /**
     * Test if the list contains x
     * 
     * @param x Check if x is on the list
     * @return True if x is on the list, false if not
     */
    public boolean contains(T x) {
        findPred(x);

        if (pred[0].next[0].equals(tail))
            return false;
        return pred[0].next[0].element.compareTo(x) == 0;
    }

    /**
     * Get the first element in the list
     * 
     * @return The value of the first element in the list
     */
    public T first() {
        return head.next[0].getElement();
    }

    /**
     * Find largest element that is less than or equal to x
     * 
     * @param x Value to compare against
     * @return The value of the smallest entry less than or equal to x
     */
    public T floor(T x) {
        findPred(x);

        if (!pred[0].next[0].equals(tail) && pred[0].next[0].getElement().compareTo(x) <= 0)
            return pred[0].next[0].getElement();
        return pred[0].getElement();
    }

    /**
     * Return element at index n of list. First element is at index 0.
     * 
     * @param n Index of the element to get
     * @return The value of the element at index n
     */
    public T get(int n) {
        // Throw exception if n is out of bounds
        if (n > size - 1 || n < 0)
            throw new IndexOutOfBoundsException();

        // Go to nth index of the list
        Entry<T> p = head.next[0];
        for (int i = 0; i < n; i++) {
            p = p.next[0];
        }

        return p.getElement();
    }

    /**
     * Is the list empty or not
     * 
     * @return True if list is empty, false if not
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Iterate through the elements of list in sorted order
     * 
     * @return A new iterator for the list
     */
    public Iterator<T> iterator() {
        return new SkipListIterator();
    }

    /**
     * Get the last element in the list
     * 
     * @return The value of the last element in the list
     */
    public T last() {
        return tail.prev[0].getElement();
    }

    /**
     * Remove x from list. Removed element is returned. Return null if x not in list
     * 
     * @param x Entry of element to remove from the list
     * @return The value of the removed entry, null if entry wasn't on the list
     */
    public T remove(T x) {
        // If list doesn't contain x then return null, change nothing
        if (!contains(x))
            return null;

        // Reference to the Entry to remove
        Entry<T> entry = pred[0].next[0];
        int lvl = entry.level();

        // Remove all references to x Entry
        for (int i = 0; i < lvl; i++) {
            pred[i].next[i] = entry.next[i];
            entry.next[i].prev[i] = pred[i];
        }

        size--;
        return entry.getElement();
    }

    /**
     * Get the size of the list
     * 
     * @return The number of elements in the list
     */
    public int size() {
        return size;
    }

    /**
     * Find the path to x
     * 
     * @param x Value to find a path towards
     */
    private void findPred(T x) {
        // Clear pred array
        pred = new Entry[PossibleLevels];

        // Get the path to the element
        Entry<T> p = head;
        for (int i = PossibleLevels - 1; i >= 0; i--) {
            while (!p.next[i].equals(tail) && p.next[i].getElement().compareTo(x) < 0) {
                p = p.next[i];
            }
            pred[i] = p;
        }
    }

    /*
     * Iterator for SkipList class
     */
    class SkipListIterator implements Iterator<T> {
        Entry<T> current;

        // Initialize iterator to Entry after head
        public SkipListIterator() {
            current = head.next[0];
        }

        /**
         * Return true if the next element isn't the tail
         * 
         * @return True if there is a next element after curent
         */
        public boolean hasNext() {
            return !current.equals(tail);
        }

        /**
         * Get the next element in the SkipList
         * 
         * @return The value of the next element in the list
         */
        public T next() {
            T element = current.getElement();
            current = current.next[0];
            return element;
        }

    }
}