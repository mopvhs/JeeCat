package com.jeesite.modules.alg.hard;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AlgHard7_7 {


    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        //3 1 4 2
        //3 1 2 4
        String[] n1 = in.nextLine().split(" ");
        String[] n2 = in.nextLine().split(" ");

        int i = Integer.parseInt(n1[0]);
        int j = Integer.parseInt(n2[0]);

        int[] num1 = new int[i];
        int[] num2 = new int[j];

        for (int k = 1; k <= i; k++) {
            num1[k-1] = Integer.parseInt(n1[k]);
        }
        for (int k = 1; k <= j; k++) {
            num2[k-1] = Integer.parseInt(n2[k]);
        }

        // lcs
        int[][] dp = new int[i + 1][j + 1];

        // dp[0][j] = 0; dp[i][0] = 0
        for (int k = 1; k <= i; k++) {
            for (int l = 1; l <= j; l++) {
                // 如果两个数相等
                if (n1[k].equals(n2[l])) {
                    dp[k][l] = dp[k-1][l-1] + 1;
                } else {
                    dp[k][l] = Math.max(dp[k-1][l], dp[k][l-1]);
                }
            }
        }

        System.out.println(dp[i][j]);
    }


}
