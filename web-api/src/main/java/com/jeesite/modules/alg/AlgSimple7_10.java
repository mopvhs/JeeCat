package com.jeesite.modules.alg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class AlgSimple7_10 {

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        String s = in.nextLine();
        // 80,null,40,34,70,21,null,55,78
        List<String> list = new ArrayList<>();
        for (String s1 : s.split(",")) {
            list.add(s1);
        }


        if (list.size() == 1) {
            System.out.println(true);
            return;
        }

        // 实际的节点数量
        int realNode = list.size();
        // 先判断一次根节点
        int root = Integer.parseInt(list.get(0));

        if (realNode > 1 && !list.get(1).equals("null")) {
            Integer integer = Integer.valueOf(list.get(1));
            if (root <= integer) {
                System.out.println(false);
                return;
            }
        }

        if (realNode > 2 && !list.get(2).equals("null")) {
            Integer integer = Integer.valueOf(list.get(2));
            if (root >= integer) {
                System.out.println(false);
                return;
            }
        }


        boolean match = true;
        boolean[] visited = new boolean[100000];
        int startIndex = 1;
        while (true) {
            if (realNode <= 1) {
                break;
            }
            if (startIndex > list.size() - 1) {
                break;
            }

            int left = startIndex * 2 + 1;
            int right = left + 1;
            // 虚拟节点
            if (visited[startIndex]) {
                // 说明没有左右子树，需要补全
                list.add(left, "null");
                list.add(right, "null");
                visited[left] = true;
                visited[right] = true;
                startIndex++;
                continue;
            }
            // 实际节点
            String s1 = list.get(startIndex);
            if (s1.equals("null")) {
                // 说明没有左右子树，需要补全
                list.add(left, "null");
                list.add(right, "null");
                visited[left] = true;
                visited[right] = true;
                startIndex++;
                realNode--;
                continue;
            }

            // 左右节点补全
            if (left > list.size() - 1) {
                list.add(left, "null");
                visited[left] = true;
            }
            // 左右节点补全
            if (right > list.size() - 1) {
                list.add(right, "null");
                visited[right] = true;
            }

            // 实际节点处理
            realNode--;
            // 左
            Integer sv = Integer.valueOf(s1);
            String lv = list.get(left);
            if (!Objects.equals(lv, "null")) {
                match = isMatch(sv, lv, true);
                if (!match) {
                    break;
                }
            }

            // 右
            String rv = list.get(right);
            if (!Objects.equals(rv, "null")) {
                match = isMatch(sv, rv, false);
                if (!match) {
                    break;
                }
            }
        }
        System.out.println(match);
    }

    public static Boolean isMatch(Integer root, String child, boolean left) {
        if (child.equals("null")) {
            return true;
        }
        Integer cv = Integer.valueOf(child);
        if (left) {
            return root > cv;
        }

        return root < cv;
    }

}
