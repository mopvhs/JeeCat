package com.jeesite.modules.alg.hard;

import java.util.Scanner;

public class AlgHard7_6 {

    // 行
    private static int row = 0;
    // 列
    private static int column = 0;

    private static int curLand = 0;

    public static void main(String[] args) {

        // [[1,1,0,0,0];[1,1,0,0,0];[0,0,1,0,0];[0,0,0,1,1]]
        // [[0,0,1,0,0,0,0,1,0,0,0,0,0];[0,0,0,0,0,0,0,1,1,1,0,0,0];[0,1,1,0,1,0,0,0,0,0,0,0,0];[0,1,0,0,1,1,0,0,1,0,1,0,0];[0,1,0,0,1,1,0,0,1,1,1,0,0];[0,0,0,0,0,0,0,0,0,0,1,0,0];[0,0,0,0,0,0,0,1,1,1,0,0,0];[0,0,0,0,0,0,0,1,1,0,0,0,0]]
        Scanner in = new Scanner(System.in);

        // 先把[ 和 ] 全部吃掉，然后再按照;分隔，最后再按照，分隔
        String s = in.nextLine();
        String s1 = s.replaceAll("\\[|\\]", "");


        String[] rowVal = s1.split(";");
        String[] columnVal = rowVal[0].split(",");

        row = rowVal.length;
        column = columnVal.length;

        int[][] gird = new int[row][column];
        // 是否反问过
        boolean[][] landed = new boolean[row][column];

        // 构建岛屿
        for (int i = 0; i < row; i++) {
            String[] lie = rowVal[i].split(",");
            for (int j = 0; j < lie.length; j++) {
                gird[i][j] = Integer.parseInt(lie[j]);
            }
        }

        // 最大岛屿
        int maxLand = 0;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                // 已经登录过的岛屿，不在登录，说明已经被统计过，无论 0,1
                if (landed[i][j]) {
                    continue;
                }

                int val = gird[i][j];
                // 是岛屿
                if (val == 1) {
                    // 新岛屿
                    curLand = 0;
                    dfs(gird, i, j, landed);

                    maxLand = Math.max(maxLand, curLand);
                }
            }
        }

        System.out.println(maxLand);
    }

    public static int dfs(int[][] gird, int i, int j, boolean[][] landed) {

        // 超出边界，返回 0
        if (i < 0 || j < 0 || i >= row || j >= column) {
            return 0;
        }
        if (gird[i][j] == 0) {
            return 0;
        }

        if (landed[i][j]) {
            return 0;
        }

        curLand++;
        // 登录
        landed[i][j] = true;
        // 右边
        int a = dfs(gird, i, j + 1, landed);
        // 下边
        int b = dfs(gird, i + 1, j, landed);
        // 上边
        int c = dfs(gird, i - 1, j, landed);
        // 左边
        int d = dfs(gird, i , j - 1, landed);

        // 和为 0，表示 4 边都是海洋
        return a + b + c + d;
    }
}
