package com.jeesite.modules.alg;

import java.util.Map;
import java.util.Scanner;

public class AlgSimple7_8 {


    /**
     * https://leetcode.cn/problems/XltzEq/description/
     * @param args
     */
    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        String s = in.nextLine();
        if (s == "" || s.length() < 2) {
            return;
        }

        int start = -1;
        int len = 0;
        for (int f = 0; f < s.length(); f++) {
            int i1 = 0;
            int i2 = 0;

            for (int se = f; se < s.length(); se++) {
                char c = s.charAt(se);
                if (c >= 'a' && c <= 'z') {
                    // 需要去重，不能每次都加，| 1｜0  1｜1 0｜1 都为1 0|0为0
                    i1 = i1 | (1 << (c - 'a'));
                } else {
                    i2 = i2 | (1 << (c - 'A'));
                }
                if (i1 == i2 && se - f + 1 > len) {
                    //
                    start = f;
                    len = se - f + 1;
                }
            }
        }
        if (start == -1) {
            System.out.println("");
            return;
        }

        System.out.println(s.substring(start, start + len));
    }
}
