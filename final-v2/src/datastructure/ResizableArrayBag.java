package datastructure;

import java.util.Arrays;

import adt.BagInterface;

public class ResizableArrayBag<T> implements BagInterface<T> {

    private T[] bag;
    private int numberOfEntries;
    private static final int DEFAULT_CAPACITY = 25;

    public ResizableArrayBag() {
        this(DEFAULT_CAPACITY);
    }

    public ResizableArrayBag(int capacity) {
        @SuppressWarnings("unchecked")
        T[] temp = (T[]) new Object[capacity];
        bag = temp;
        numberOfEntries = 0;
    }

    @Override
    public boolean add(T newEntry) {
        if (isArrayFull()) doubleCapacity();
        bag[numberOfEntries++] = newEntry;
        return true;
    }

    private boolean isArrayFull() {
        return numberOfEntries >= bag.length;
    }

    private void doubleCapacity() {
        bag = Arrays.copyOf(bag, bag.length * 2);
    }
    @Override
    public boolean remove(T anEntry) {
        for (int i = 0; i < numberOfEntries; i++) {
            if (bag[i].equals(anEntry)) {
                bag[i] = bag[numberOfEntries - 1]; // Overwrite with the last element
                bag[numberOfEntries - 1] = null;
                numberOfEntries--;
                return true;
            }
        }
        return false;
    }

    @Override
    public T[] toArray(T[] a) {
        if (a.length < numberOfEntries) {
            return Arrays.copyOf(bag, numberOfEntries, (Class<? extends T[]>) a.getClass());
        }
        System.arraycopy(bag, 0, a, 0, numberOfEntries);
        if (a.length > numberOfEntries) {
            a[numberOfEntries] = null;
        }
        return a;
    }
}
