package com.jeesite.modules.alg.hard;

import java.util.Scanner;

public class AlgHard7_4 {

    public static void main(String[] args) {


        Scanner in = new Scanner(System.in);

        String[] split = in.nextLine().split(" ");

        int[] nums = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            nums[i] = Integer.parseInt(split[i]);
        }

        // dp[i] = max(db[j]) + 1 并且 nums[j] < nums[i]

        int[] dp = new int[split.length];
        dp[0] = 1;
        int maxans = 1;

        for (int i = 1; i < split.length; i++) {
            // num[j] < num[i]
            dp[i] = 1;
            int ival = nums[i];
            for (int j = 0; j < split.length; j++) {
                int jval = nums[j];
                // 如果后面的大于前面的，获取max(dp[j]) + 1 覆盖 dp[i]，说明前面的链路最长 + 1
                if (ival > jval) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            maxans = Math.max(maxans, dp[i]);
        }

        System.out.println(maxans);

    }


}
