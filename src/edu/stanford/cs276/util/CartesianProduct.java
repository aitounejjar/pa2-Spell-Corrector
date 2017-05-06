package edu.stanford.cs276.util;


import java.util.*;

import static edu.stanford.cs276.Config.DIAMOND;

public class CartesianProduct {

    private CartesianProduct(){}

    /**
     * Computes the cartesian product of the passed lists. Uses a priority queue that orders its elements
     * by increasing size, to help make the calculations more efficient.
     *
     * @param lists lists on which the cartesian product is to be performed
     * @return      cartesian product
     */
    public static List<String> compute(List<List<String>> lists) {

        LinkedList queue = new LinkedList();

        queue.addAll(lists);

        while (true) {
            if (queue.size() <= 1) {
                break;
            }

            List<String> result = new ArrayList<>();
            List<String> list1 = (List<String>) queue.removeFirst();
            List<String> list2 = (List<String>) queue.removeFirst();

            for (String s1 : list1) {
                for (String s2 : list2) {
                    result.add(s1 + DIAMOND + s2);
                }
            }

            queue.addFirst(result);
        }
        return (List<String>)queue.get(0);


    }

}
