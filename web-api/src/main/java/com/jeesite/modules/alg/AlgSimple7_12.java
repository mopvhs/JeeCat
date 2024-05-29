package com.jeesite.modules.alg;

import java.util.Scanner;

public class AlgSimple7_12 {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        String[] students = params(in.nextLine());
        String[] sandwiches = params(in.nextLine());
        boolean[] visited = new boolean[students.length];
        int oneSum = 0;
        int zeroSum = 0;
        for (int i = 0; i < students.length; i++) {
            if (students[i].equals("0")) {
                zeroSum++;
            } else {
                oneSum++;
            }
        }

        for (int i = 0; i < sandwiches.length; i++) {
            String sandwich = sandwiches[i];
            if (sandwich.equals("0") && zeroSum > 0) {
                zeroSum--;
            } else if (sandwich.equals("1") && oneSum > 0) {
                oneSum--;
            } else {
                break;
            }
        }

        System.out.println(zeroSum + oneSum);
    }

//    public static void main(String[] args) {
//
//        Scanner in = new Scanner(System.in);
//
//        String[] students = params(in.nextLine());
//        String[] sandwiches = params(in.nextLine());
//        boolean[] visited = new boolean[students.length];
//        int second = 0;
//        int matchNum = 0;
//        // 需要购买的自身的n轮票数
//        for (int i = 0; i < sandwiches.length; i++) {
//            String sandwich = sandwiches[i];
//            boolean match = false;
//            while (true) {
//                for (int j = 0; j < sandwiches.length; j++) {
//                    if (visited[j]) {
//                        continue;
//                    }
//                    String student = students[j];
//                    if (student.equals(sandwich)) {
//                        visited[j] = true;
//                        match = true;
//                        matchNum++;
//                        break;
//                    }
//                }
//                if (match) {
//                    break;
//                }
//            }
//            // 如果一轮后，还是没匹配到，说明无法匹配
//            if (!match) {
//                break;
//            }
//        }
//        System.out.println(students.length - matchNum);
//    }

    private static String[] params(String str) {
        String[] split = str.split(" ");

        return split;
    }
}
