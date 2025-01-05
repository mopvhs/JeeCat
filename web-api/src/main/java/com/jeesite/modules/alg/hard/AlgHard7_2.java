package com.jeesite.modules.alg.hard;

import java.util.Scanner;

public class AlgHard7_2 {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        String line = in.nextLine();

        String[] split = line.split(" ");


        // 获取数量，创建一个二维数组
        int[][] arr = new int[split.length][split.length];
        for (int i = 0; i < split.length; i++) {
            int iNum = Integer.parseInt(split[i]);
            for (int j = i + 1; j < split.length; j++) {
                if (arr[i][j] == 1) {
                    continue;
                }
                int jNum = Integer.parseInt(split[j]);
                boolean hasCommon = hasCommon(iNum, jNum);
                if (hasCommon) {
                    arr[i][i] = 1;
                    arr[j][j] = 1;
                    arr[i][j] = 1;
                    arr[j][i] = 1;
                }
            }
        }

        // 找到 1 最多的一组
        int maxIndex = 0;
        int maxOne = 0;
        for (int i = 0; i < split.length; i++) {
            int one = 0;
            for (int j = 0; j < split.length; j++) {
                one += arr[i][j];
            }
            if (one > maxOne) {
                maxIndex = i;
                maxOne = one;
            }
        }

        for (int i = 0; i < split.length; i++) {
            for (int j = 0; j < split.length; j++) {
                System.out.print(arr[i][j] + " ");
            }
            System.out.print("\n");
        }

        // 获取最长的一行
        for (int i = 0; i < split.length; i++) {
            // 获取到 0 的位置
            int val = arr[maxIndex][i];
            if (val == 0) {
                for (int j = 0; j < split.length; j++) {
                    // 找这一列，不为 0 的行
                    int i1 = arr[j][i];
                    if (i1 == 0) {
                        continue;
                    }
                    int maxNum = 0;
                    // 判断其他的位置是否和其重合，需要大于 2 才算
                    for (int k = 0; k < split.length; k++) {
                        int k1 = arr[maxIndex][k];
                        int k2 = arr[j][k];
                        if (k1 == 1 && k2 == 1) {
                            maxNum++;
                        }
                    }
                    // 不重复加
                    if (maxNum >= 2 && arr[maxIndex][i] == 0) {
                        arr[maxIndex][i] = 1;
                        maxOne++;
                    }
                }
            }
        }

        System.out.println(maxOne);

    }

    private static boolean hasCommon(int a, int b) {

        // 计算最大公约数
        int res = gcd(a, b);

        // 如果最大公约数大于1，则有大于1的公因数
        return res > 1;
    }

    // 使用辗转相除法求最大公约数
    private static int gcd(int a, int b) {
        if (b == 0) {
            return a;
        }

        return gcd(b, a % b);
    }
}
