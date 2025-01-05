package com.jeesite.modules.alg.hard;


import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class AlgHard7_13 {


    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        // abcabcbb
        String line = scanner.nextLine();

        int max = 0;
        Set<String> set =new HashSet<>();

        // 左右指针移动
        int right = -1;

        for (int left = 0 ; left < line.length(); ++left) {
            // 不是第一位，需要移除相同字符
            if (left != 0) {
                // 从左到右移除
                set.remove(String.valueOf(line.charAt(left - 1)));
            }

            // 右指针，一直向右遍历，直到出现相同字符串
            while (right + 1 < line.length() && !set.contains(String.valueOf(line.charAt(right + 1)))) {
                set.add(String.valueOf(line.charAt(right + 1)));
                right++;
            }

            // 获取最长长度
            max = Math.max(max, right - left + 1);
        }

        System.out.println(max);

    }



}
