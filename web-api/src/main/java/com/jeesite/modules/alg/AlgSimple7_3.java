package com.jeesite.modules.alg;

import java.util.Scanner;

public class AlgSimple7_3 {

    public static int n;
    public static int m;
    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        // 4 5
        // 1 1 0 0 0
        // 1 1 0 0 0
        // 0 0 1 0 0
        // 0 0 0 1 1
        String header = in.nextLine();

        String[] split = header.split(" ");
        n = Integer.parseInt(split[0]);
        m = Integer.parseInt(split[1]);

        String[][] res = new String[n][m];
        for (int i = 0; i < n; i++) {
            String s = in.nextLine();
            String[] val = s.split(" ");
            res[i] = val;
        }

        boolean[][] visited = new boolean[n][m];

        int count = 0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                // 如果这座山已经访问过，就不再访问
                if (visited[i][j]) {
                    continue;
                }
                String now = res[i][j];
                if ("1".equals(now)) {
                    visited[i][j] = true;
                    // 右 下的去找是否有1，如果不是1则返回
                    int a = dfs(res, i + 1, j, visited);
                    int b = dfs(res, i, j+1, visited);
                    if (a + b == 0) {
                        count++;
                    }
                }
            }
        }

        System.out.println(count);
    }

    // 深度优先算法遍历
    public static int dfs(String[][] res, int i, int j, boolean[][] visited) {
        if (i >= n || j >= m) {
            return 0;
        }
        // 这个条件是不可能存在
        if (visited[i][j]) {
            return 0;
        }

        // 判断右边是否为1
        String val = res[i][j];
        if ("0".equals(val)) {
            return 0;
        }

        visited[i][j] = true;
        // 右边
        int right = dfs(res, i + 1, j, visited);
        // 下边
        int down = dfs(res, i, j + 1, visited);

        return right + down;
    }
}
