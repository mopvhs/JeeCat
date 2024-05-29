package com.jeesite.modules.alg;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AlgSimple7_7 {


    /**
     * https://leetcode.cn/problems/XltzEq/description/
     * @param args
     */
    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        int total = Integer.valueOf(in.nextLine());
        List<String> res = new ArrayList<>();

        // 相邻元素从1开始
        for (int i = 1; i <= total; i++) {
            // 从i开始，判断是否可以满足
            String cal = cal(i, total);
            if (cal == null) {
                continue;
            }
            res.add(cal);
        }
        if (res.size() == 0) {
            return;
        }
        for (String re : res) {
            System.out.println(re);
        }
    }

    public static String cal(int start, int total) {
        if (start >= total) {
            return null;
        }

        String res = "";
        int sum = 0;
        for (int i = 0; i < total; i++) {
            start = start + i;
            sum = sum + start;
            if (sum < total) {
                res += start + ",";
                continue;
            }
            if (sum == total) {
                res += start;
                return res;
            }
            if (sum > total) {
                return null;
            }
        }

        return null;
    }

}
