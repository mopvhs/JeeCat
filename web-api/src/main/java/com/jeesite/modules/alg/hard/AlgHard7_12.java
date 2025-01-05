package com.jeesite.modules.alg.hard;


import java.util.Scanner;
import java.util.Stack;

public class AlgHard7_12 {


    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        // )()(()))
        String line = scanner.nextLine();

        Stack<Integer> stack = new Stack<>();
        stack.push(-1);

        int max = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            // 如果是合理的，就把当前值放进去，一边后续判断，同时等同于记录不满足的起始位置
            if (c == '(') {
                stack.push(i);
            } else {
                // 出栈，把-1移除，或者)移除,并且如果为空需要重新记录最新的起始位置
                Integer pop = stack.pop();
                if (stack.empty()) {
                    stack.push(i);
                } else {
                    // 不为空，说明是(符号，计算最大长度 减去栈顶
                    max = Math.max(max, i - stack.peek());
                }
            }
        }
        System.out.println(max);

    }



}
