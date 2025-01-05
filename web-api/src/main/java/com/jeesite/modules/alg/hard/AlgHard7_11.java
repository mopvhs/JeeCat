package com.jeesite.modules.alg.hard;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class AlgHard7_11 {


    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        // 5 4 3 2 1
        String line = scanner.nextLine();
        String[] split = line.split(" ");
        int[] arr = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            arr[i] = Integer.parseInt(split[i]);
        }

        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < arr.length; i++) {

            int val = arr[i];
            int blockMaxVal = val;
            // 获取栈顶元素：peek 不出栈, pop 出栈
            while (stack.size() > 0 && stack.peek() > val) {
                // 循环从栈中找，
                blockMaxVal = Math.max(blockMaxVal, stack.pop());
            }
            stack.push(blockMaxVal);
        }

        System.out.println(stack.size());

    }



}
