package com.jeesite.modules.alg;

import java.util.Scanner;

public class AlgSimple7_9 {


    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        String s = in.nextLine();
        int total = Integer.valueOf(s);

        if (total <= 0) {
            System.out.println(0);
            return;
        }
        int cal = cal(total);
        System.out.println(cal);
    }

    public static int cal(int total) {
        int count = 0;
        while (total > 0) {
            if (total >= 5) {
                int five = total / 5;
                count += five;
                total = total % 5;
            } else if (total >= 2) {
                int sec = total / 2;
                count += sec;
                total = total % 2;
            } else {
                count += total;
                total = 0;
            }
        }

        return count;
    }
}
