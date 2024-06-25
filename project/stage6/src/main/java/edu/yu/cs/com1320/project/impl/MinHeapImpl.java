package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {

    public MinHeapImpl(){
        super();
        super.elements = (E[]) new Comparable[5];
    }
    @Override
    public void reHeapify(E element) {
        int index = getArrayIndex(element);
        if(index != -1){
            upHeap(index);
            downHeap(index);
        }
    }

    @Override
    protected int getArrayIndex(E element) {
        for(int i = 1; i <= this.count; i++){
            if(elements[i].equals(element)){
                return i;
            }
        }
        throw new NoSuchElementException("element does not exist in the heap");
    }

    @Override
    protected void doubleArraySize() {
        int newSize = elements.length * 2;
        E[] newArray = (E[]) new Comparable[newSize];
        System.arraycopy(this.elements, 0, newArray, 0, elements.length);
        this.elements = newArray;
    }
}
