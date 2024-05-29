package com.jeesite.modules.alg;

import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class AlgSimple7_1 {


    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        String[] operationList = in.nextLine().split(",");
        String[] dataList = in.nextLine().split(",");

        MyQueue queue = new MyQueue();
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < operationList.length; i++) {
            String op = operationList[i];
            String val = dataList[i];
            switch (op) {
                case "push":
                    queue.push(Integer.valueOf(val));
                    str.append("null,");
                    break;
                case "pop":
                    str.append(String.valueOf(queue.pop())).append(",");
                    break;
                case "peek":
                    str.append(String.valueOf(queue.peek())).append(",");
                    break;
                case "empty":
                    boolean empty = queue.empty();
                    str.append(String.valueOf(empty)).append(",");
                    break;
            }
        }

        System.out.println(str.substring(0, str.length() - 1));
    }


    static class MyQueue {

        public Stack<Integer> stack1 = new Stack<>();
        public Stack<Integer> stack2 = new Stack<>();

        // 将元素 x 推到队列的末尾
        public void push(int x) {
            // 如果栈2不为空，需要将栈2的元素放到栈1
            if (!stack2.isEmpty()) {
                while (!stack2.isEmpty()) {
                    stack1.push(stack2.pop());
                }
            }

            // 放到栈1
            stack1.push(x);
        }

        // 从队列的开头移除并返回元素
        public int pop() {
            // 返回栈的最底下元素
            if (stack1.isEmpty() && stack2.isEmpty()) {
                return 0;
            }
            // 站2不为空，直接返回
            if (!stack2.isEmpty()) {
                return stack2.pop();
            }
            // 如果栈1数量等于1，直接返回
            if (stack1.size() == 1) {
                return stack1.pop();
            }
            // 站2为空，将站1的元素放到站2
            while(!stack1.isEmpty()) {
                stack2.push(stack1.pop());
            }
            // 返回站2的元素
            return stack2.pop();
        }

        // 返回队列开头的元素，不出栈
        public int peek() {
            // 返回栈的最底下元素
            if (stack1.isEmpty() && stack2.isEmpty()) {
                return 0;
            }
            // 站2不为空，直接返回
            if (!stack2.isEmpty()) {
                return stack2.peek();
            }
            // 如果栈1数量等于1，直接返回
            if (stack1.size() == 1) {
                return stack1.peek();
            }
            // 站2为空，将站1的元素放到站2
            while(!stack1.isEmpty()) {
                stack2.push(stack1.pop());
            }
            // 返回站2的元素
            return stack2.peek();
        }

        // 如果队列为空，返回 true ；否则，返回 false
        public boolean empty() {
            return stack1.isEmpty() && stack2.isEmpty();
        }
    }


}
