package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BTreeImplTest {

    private BTree<Integer, String> bTree;

    @BeforeEach
    public void setUp() {
        bTree = new BTreeImpl<>();
    }


    @Test
    public void getAndPutTest() {
        for (int i = 0; i < 100; i++) {
            bTree.put(i, "hey" + i);
        }
        for (int i = 0; i < 100; i++) {
            assertEquals("hey" + i, bTree.get(i));
        }
        for (int i = 99; i >= 0; i--) {
            assertEquals("hey" + i, bTree.get(i));
        }
    }
}
    //CHANGE THESE TESTS!!!!!