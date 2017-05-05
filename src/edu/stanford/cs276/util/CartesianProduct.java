package edu.stanford.cs276.util;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import static edu.stanford.cs276.RunCorrector.DIAMOND;

public class CartesianProduct {

    private CartesianProduct(){}

    // compares list
    private static Comparator<List<String>> SIZE_COMPARATOR = new Comparator<List<String>>() {
        @Override
        public int compare(List<String> list, List<String> otherList) {

            int sizeDiff = list.size() - otherList.size();

            if      (sizeDiff == 0)   return 0;
            else if (sizeDiff >  0)   return +1;
            else                      return -1;

        }
    };

    /**
     * Computes the cartesian product of the passed lists. Uses a priority queue that orders its elements
     * by increasing size, to help make the calculations more efficient.
     *
     * @param lists lists on which the cartesian product is to be performed
     * @return      cartesian product
     */
    public static List<String> compute(List<List<String>> lists) {

        Logger.print(false, "Calculating cartesian product of " + lists.size() + " lists.");

        PriorityQueue queue = new PriorityQueue<>(SIZE_COMPARATOR);

        for (List<String> list : lists) {
            queue.add(list);
        }

        while (true) {
            if (queue.size() <= 1) {
                break;
            }

            List<String> temp = new ArrayList<>();
            List<String> list1 = (List<String>) queue.remove();
            List<String> list2 = (List<String>) queue.remove();

            for (String s1 : list1) {
                for (String s2 : list2) {
                    temp.add(s1 + DIAMOND + s2);
                }
            }

            queue.add(temp);
        }

        List<String> result = (List<String>) queue.remove();

        return result;
    }

}