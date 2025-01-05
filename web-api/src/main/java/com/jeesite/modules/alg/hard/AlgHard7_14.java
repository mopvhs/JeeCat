package com.jeesite.modules.alg.hard;


import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class AlgHard7_14 {


    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        // 2,1,3
        String line = scanner.nextLine();
        // 构建出数组
        String[] split = line.split(",");
        int[] nums = new int[split.length];

        for (int i = 0; i < split.length; i++) {
            nums[i] = Integer.parseInt(split[i]);
        }

        int[] xjNums = new int[1 << 16];
        // 先遍历出一个x & j的
        // 1<<16 & 1<< 16 最大值依旧是 2^16
        for (int i = 0; i < nums.length; i++) {
            int iv = nums[i];
            for (int j = 0; j < nums.length; j++) {
                int jv = nums[j];
                // i & j 的数量
                ++xjNums[iv & jv];
            }
        }

        int ans = 0;
        for (int i = 0; i < nums.length; i++) {
            int num = nums[i];
            for (int j = 0; j < (1<<16); j++) {
                if ((num & j) == 0) {
                    ans += xjNums[j];
                }
            }
        }

        System.out.println(ans);

    }



}
