package com.jeesite.modules.alg;

import java.util.Scanner;

public class AlgSimple7_4 {


    public static int num = 0;

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        // 7
        // 4 2 3 0 3 1 2
        // 5
        num = Integer.parseInt(in.nextLine());
        String[] split = in.nextLine().split(" ");
        String s = in.nextLine();
        int index = Integer.parseInt(s);

        int[] arr = new int[num];
        boolean[] visited = new boolean[num];

        for (int i = 0; i < split.length ; i++) {
            arr[i] = Integer.parseInt(split[i]);
        }

        // 获取跳跃的步数
        int step = arr[index];
        if (step == 0) {
            System.out.println("True");
            return;
        }
        if (num == 1) {
            System.out.println("False");
            return;
        }
        // 先向前跳，如果到头或者越界，再从index开始向后跳
        // 判断是否向前就越界
        // 向前跳
        boolean bfs = bfs(arr, visited, index);
        System.out.println(bfs ? "True" : "False");
    }

    public static boolean bfs(int[] arr, boolean[] visited, int index) {
        if (index < 0 || index >= num) {
            return false;
        }

        if (visited[index]) {
            return false;
        }

        visited[index] = true;
        int i = arr[index];
        if (i == 0) {
            return true;
        }

        return bfs(arr, visited, index - i) || bfs(arr, visited, index + i);
    }

//    public static int jump(int[] arr, int index) {
//        if (index < 0 || index >= num) {
//            return -1;
//        }
//
//        // 当前位置是不是出口，如果是出口返回0，不是出口继续
//        int nextStep = arr[index];
//        if (nextStep == 0) {
//            return 0;
//        }
//
//        // 向前
//        int jump1 = jump(arr, index - nextStep);
//        // 向后
//        int jump2 = jump(arr, index + nextStep);
//
//        return (jump1 == 0 || jump2 == 0) ? 0 : -1;
//    }


}
