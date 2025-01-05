package com.jeesite.modules.alg.hard;


import java.util.Scanner;

public class AlgHard7_8 {


    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        // 6,5,4,6
        String[] n1 = in.nextLine().split(",");
        int[] encoded = new int[n1.length];
        int[] p = new int[n1.length + 1];
        for (int i = 0; i < n1.length; i++) {
            encoded[i] = Integer.parseInt(n1[i]);
        }

        int pAll = 1;
        // 全部的
        for (int i = 2; i <= n1.length + 1; i++) {
            pAll = pAll ^ i;
        }

        int e1 = encoded[1];
        // 个位数,除了p[0]的其他异或结果
        for (int i = 3; i < n1.length; i++) {
            if (i % 2 == 1) {
                //
                e1 = e1 ^ encoded[i];
            }
        }
        // 计算出p[0]
        int p0 = e1 ^ pAll;
        p[0] = p0;

        for (int i = 0; i < encoded.length; i++) {
            p[i + 1] = encoded[i] ^ p[i];
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < encoded.length + 1; i++) {
            sb.append(p[i]);
            if (i != encoded.length) {
                sb.append(",");
            }
        }

        System.out.println(sb);
    }


}
