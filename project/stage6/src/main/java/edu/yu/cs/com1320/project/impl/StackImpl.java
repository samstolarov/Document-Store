package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T> {

    public T[] stack;
    private int size;
    private int top;
    public StackImpl(){
        this.stack = (T[]) new Object[5];
        this.top = -1;
        this.size = 0;
    }
    @Override
    public void push(T element) {
        if(element == null){
            throw new IllegalArgumentException("no pushing nulls onto the stack");
        }
        if(top == stack.length - 1){
            resize();
        }
        top++;
        stack[top] = element;
        size++;
    }

    @Override
    public T pop() {
        if(top == -1){
            return null;
        }
        T poppedItem = stack[top];
        stack[top] = null;
        top--;
        size--;
        return poppedItem;
    }

    @Override
    public T peek() {
        if(size() > 0){
            return stack[top];
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    private void resize(){
        T[] newStack = (T[]) new Object[stack.length * 2];
        System.arraycopy(stack, 0, newStack, 0, stack.length);
        stack = newStack;
    }
}
