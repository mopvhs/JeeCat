package com.jeesite.modules.alg.hard;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class AlgHard7_10 {


    public static void main(String[] args) {

        // 4,1000
        Scanner in = new Scanner(System.in);
        String line = in.nextLine();

        String[] split = line.split(",");
        long left = Long.parseLong(split[0]);
        long right = Long.parseLong(split[1]);

        // 1 <= magic < 100000
        int magic = 100000;
        List<Long> ans = new ArrayList<>();

        // 计算奇数长度回文
        for (int i = 1; i < magic; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(i);
            // 构建出另外一半的数
            // i = 22 -> 构建结果为 222
            // i = 12 -> 构建结果为 121
            for (int j = sb.length() - 2; j >= 0 ; j--) {
                sb.append(sb.charAt(j));
            }
            // v一定是一个回文
            long v = Long.parseLong(sb.toString());

            // 判断v * v 是否是回文
            v *= v;

            if (v < left || v > right) {
                continue;
            }

            if (isPalindrome(v)) {
                ans.add(v);
            }
        }

        for (int i = 1; i < magic; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(i);
            // 构建出另外一半的数
            // i = 22 -> 构建结果为 222
            // i = 12 -> 构建结果为 121
            for (int j = sb.length() - 1; j >= 0 ; j--) {
                sb.append(sb.charAt(j));
            }
            // v一定是一个回文
            long v = Long.parseLong(sb.toString());

            // 判断v * v 是否是回文
            v *= v;

            if (v < left || v > right) {
                continue;
            }

            if (isPalindrome(v)) {
                ans.add(v);
            }
        }

        Collections.sort(ans);

        String sb = "";
        System.out.print("[");
        for (int i = 0; i < ans.size(); i++) {
            if (i != ans.size()- 1) {
                sb += ans.get(i) + ", ";
            } else {
                sb += ans.get(i) + "";
            }
        }

        System.out.println(sb + "]");
    }

    public static boolean isPalindrome(long v) {
        // 判断是否是回文，只需要把数据反过来就行
        long ans = 0;
        long temp = v;
        while (temp > 0) {
            // 结果扩大 10倍 加上 原本的个位数
            ans = ans * 10 + temp % 10;
            // 把个位数移除
            temp = temp / 10;
        }

        return ans == v;
    }


}
