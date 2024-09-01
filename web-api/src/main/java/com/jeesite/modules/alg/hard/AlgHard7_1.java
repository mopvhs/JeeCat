package com.jeesite.modules.alg.hard;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

public class AlgHard7_1 {


    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        String firstLine = in.nextLine();

        String[] split = firstLine.split(" ");
        String header1 = split[0];
        String header2 = split[1];
        int num = Integer.parseInt(split[2]);


        Map<String, Node> map = new HashMap<>();
        for (int i = 0; i < num ; i++ ) {
            String line = in.nextLine();
            String[] nodeSpilt = line.split(" ");
            String prev = nodeSpilt[0];
            Integer value = Integer.parseInt(nodeSpilt[1]);
            String next = nodeSpilt[2];
            Node node = new Node();
            node.setPrev(prev);
            node.setValue(value);
            node.setNext(next);
            map.put(prev, node);
        }
    }


    public static class Node {

        private String prev;

        private String next;

        private Integer value;

        public String getPrev() {
            return prev;
        }

        public void setPrev(String prev) {
            this.prev = prev;
        }

        public String getNext() {
            return next;
        }

        public void setNext(String next) {
            this.next = next;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }





}
