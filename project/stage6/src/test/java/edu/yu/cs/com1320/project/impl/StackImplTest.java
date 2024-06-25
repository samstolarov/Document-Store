package edu.yu.cs.com1320.project.impl;


import edu.yu.cs.com1320.project.Stack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StackImplTest {
    private StackImpl<String> stack;
    @BeforeEach
    public void setUp() {
        stack = new StackImpl<>();
    }

    @Test
    public void pushAndPeekTest() {
        assertNull(stack.pop());
        stack.push("hey");
        stack.push("hi");
        stack.push("greetings");
        assertEquals("greetings", stack.pop());
        assertEquals(2, stack.size());
        assertEquals("hi", stack.pop());
        assertEquals("hey", stack.pop());
        assertEquals(0, stack.size());
    }

    @Test
    public void peekTest() {
        assertNull(stack.peek());
        stack.push("hey");
        stack.push("hello");
        assertEquals("hello", stack.peek());
        assertEquals("hello", stack.peek());
    }

    @Test
    public void sizeTest() {
        stack.push("hello");
        stack.push("hi hi");
        assertEquals(2, stack.size());
        stack.push("hey there");
        stack.push("hola");
        stack.pop();
        assertEquals(3, stack.size());
    }
}