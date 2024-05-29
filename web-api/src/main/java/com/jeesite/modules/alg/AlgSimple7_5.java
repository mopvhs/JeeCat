package com.jeesite.modules.alg;

import java.util.Scanner;
import java.util.Stack;

public class AlgSimple7_5 {


    /**
     * https://leetcode.cn/problems/XltzEq/description/
     * @param args
     */
    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        String s = in.nextLine();
        s = s.toLowerCase();
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9') {
                str += c;
            }
        }

        if (str.equals("")) {
            System.out.println(0);
            return;
        }

        int length = str.length();

        boolean stopPush = true;
        int stop = length / 2;
        int a = length % 2;
        if (a != 0) {
            stop++;
            stopPush = false;
        }

        Stack<String> stack = new Stack<>();
        for (int i = 0; i < length; i++) {
            String s1 = String.valueOf(str.charAt(i));
            if (!stopPush && stop - 1 == i) {
                continue;
            }
            if (i < stop) {
                stack.push(s1);
                continue;
            }
            String pop = stack.pop();
            if (!pop.equals(s1)) {
                System.out.println(0);
                return;
            }
        }

        System.out.println(1);
    }

}
