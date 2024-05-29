package com.jeesite.modules.alg;

import java.util.Scanner;
import java.util.Stack;

public class AlgSimple7_2 {


    public static boolean[] visited = new boolean[10000];

    public static int count = 0;

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        String s = in.nextLine();
        String[][] res = processLine(s);

        for (int i = 0; i < res.length; i++) {
            // 如果是访问过的节点，直接返回
            if (visited[i]) {
                continue;
            }
            // 没访问过的人，直接加一
            count++;

            // 继续递归
            dfs(res, i);

            System.out.println(count);
        }



    }

    // 深度优先搜索，对于所有能找到的节点（人），进行标记已经访问过
    public static void dfs(String[][] res, int i) {
        // 如果反问过，直接返回
        if (visited[i]) {
            return;
        }
        // 标记关系已经访问过
        visited[i] = true;

        // 继续遍历下面的节点
        for (int j = 0; j < res.length; j++) {
            if (i == j || visited[j]) {
                continue;
            }
            // 如果是1，继续找，否者继续迭代
            if (res[i][j].equals("1")) {
                dfs(res, j);
            }
        }
    }

    public static String[][] processLine(String line) {
        String[] split = line.split("\\|");

        // 1 1 0|1 1 0|0 0 1 ->  [[1,1,0], [1,1,0], [0,0,1]]
        String[][] res = new String[split.length][split.length];
        for (int i = 0; i < split.length; i++) {
            String[] val = split[i].split(" ");
            res[i] = val;
        }

        return res;
    }


}
