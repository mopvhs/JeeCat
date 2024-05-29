package com.jeesite.modules.alg;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class AlgSimple7_11 {

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        String arrStr = in.nextLine();
        int index = Integer.parseInt(in.nextLine());

        String[] arrStrs = arrStr.split(" ");
        int[] tickets = new int[arrStrs.length];
        for (int i = 0; i < arrStrs.length; i++) {
            tickets[i] = Integer.parseInt(arrStrs[i]);
        }

        int num = tickets[index];
        int second = 0;
        // 需要购买的自身的n轮票数
        while (true) {
            if (tickets[index] == 0) {
                break;
            }

            for (int j = 0; j < arrStrs.length; j++) {
                Integer needNum = tickets[j];
                // 不需要买票的人忽略
                if (needNum == 0) {
                    continue;
                }
                if (tickets[index] == 0) {
                    break;
                }
                // 买了票的人，票数-1，时间加1
                tickets[j] = needNum - 1;
                // 每个人用一秒
                second++;
            }
        }

        System.out.println(second);
    }
}
