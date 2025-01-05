package com.jeesite.modules.alg.hard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
            String addr = nodeSpilt[0];
            Integer value = Integer.parseInt(nodeSpilt[1]);
            String next = nodeSpilt[2];
            Node node = new Node();
            node.setAddr(addr);
            node.setValue(value);
            node.setNext(next);
            map.put(addr, node);
        }

        List<Node> list1 = buildList(header1, map);
        List<Node> list2 = buildList(header2, map);

        List<Node> longList = list1;
        List<Node> shortList = list2;
        if (list1.size() < list2.size()) {
            longList = list2;
            shortList = list1;
        }

        int shortSize = shortList.size();
        for (int i = 1; i < longList.size(); i++) {
            Node node = longList.get(i);
            if ( i % 2 == 1) {
                if (shortSize > 0) {
                    // 获取短链接的节点
                    Node shortNode = shortList.get(shortSize - 1);
                    node.setChildNode(shortNode);
                    shortSize--;
                }
            }
        }


        for (Node node : longList) {
            Node childNode = node.getChildNode();
            if (childNode == null) {
                System.out.println(node.getAddr() + " " + node.getValue() + " " + node.getNext());
            } else {

                String content = node.getAddr() + " " + node.getValue() + " " + childNode.getAddr();
                System.out.println(content);

                String content2 = childNode.getAddr() + " " + childNode.getValue() + " " + node.getNext();
                System.out.println(content2);
            }
        }
    }

    public static List<Node> buildList(String header, Map<String, Node> nodeMap) {
        if (nodeMap == null || nodeMap.size() == 0) {
            return new ArrayList<>();
        }
        List<Node> nodes = new ArrayList<>();
        Node node = nodeMap.get(header);
        if (node == null || node.getNext() == null) {
            return new ArrayList<>();
        }

        if (node.getNext().equals("-1")) {
            nodes.add(node);
            return nodes;
        }

        String h = header;
        while (true) {
            Node item = nodeMap.get(h);
            if (item == null) {
                break;
            }

            nodes.add(item);
            h = item.getNext();
            if (h.equals("-1")) {
                break;
            }
        }

        return nodes;
    }


    public static class Node {

        private String addr;

        private String next;

        private Integer value;

        private Node childNode;

        public String getAddr() {
            return addr;
        }

        public void setAddr(String addr) {
            this.addr = addr;
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

        public Node getChildNode() {
            return childNode;
        }

        public void setChildNode(Node childNode) {
            this.childNode = childNode;
        }
    }





}
