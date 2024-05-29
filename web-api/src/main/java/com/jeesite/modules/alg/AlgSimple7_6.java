package com.jeesite.modules.alg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class AlgSimple7_6 {


    /**
     * https://leetcode.cn/problems/XltzEq/description/
     * @param args
     */
    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        String s = in.nextLine();

        String[] split = s.split(",");
        String s1 = split[0];
        String s2 = split[1];

        if (s1.length() != s2.length()) {
            System.out.println(0);
            return;
        }

        // s1 映射到 s2
        Map<String, String> map = new HashMap<>();
        // s2 是否已经被映射过
        Set<String> set = new HashSet<>();

        for (int i = 0; i < s1.length(); i++) {
            String c = String.valueOf(s1.charAt(i));
            String aChar = map.get(String.valueOf(c));
            if (aChar == null) {
                if (!set.add(String.valueOf(s2.charAt(i)))) {
                    System.out.println(0);
                    return;
                }
                map.put(c, String.valueOf(s2.charAt(i)));
            } else {
                if (aChar.equals(String.valueOf(s2.charAt(i)))) {
                    continue;
                } else {
                    System.out.println(0);
                    return;
                }
            }
        }

        System.out.println(1);
    }

}
