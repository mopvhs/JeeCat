package com.jeesite.modules.alg.hard;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class AlgHard7_5 {

    private static int maxSum = 0;

    public static void main(String[] args) {

        // -10,9,20,null,null,15,7
        // 20,null,40,34,70,21,null,55,78
        Scanner in = new Scanner(System.in);
        String[] split = in.nextLine().split(",");
        Queue<Integer> numQueue = new LinkedList<>();
        Queue<Node> nodeQueue = new LinkedList<>();
        // 创建 2 个队列，一个人数字的队列，一个是节点的队列
        for (int i = 1; i < split.length; i++) {
            String val = split[i];
            if (val.equals("null")) {
                numQueue.offer(null);
            } else {
                numQueue.offer(Integer.parseInt(val));
            }
        }

        // 开始处理，先创建root节点
        Node root = new Node();
        root.setVal(Integer.parseInt(split[0]));
        // 进入
        nodeQueue.offer(root);

        // 2个队列都不为空的时候，继续处理
        while (!nodeQueue.isEmpty() && !numQueue.isEmpty()) {
            // 从节点队列从出队列
            Node tmp = nodeQueue.poll();

            // 如果是空的，继续循环
            if (tmp == null) {
                continue;
            }
            // 逐层处理
            // 通过 2 个队列，nodeQueue的出入队列 和 numQueue的出队列，按照顺序一层一层处理，完成节点的赋值

            // 从数字队列获取，先是左节点，然后是右节点
            // 第一次出队列的，就是左节点的值
            Integer left = numQueue.poll();
            if (left != null) {
                // 不为空，创建节点
                Node leftNode = new Node();
                leftNode.setVal(left);
                // 设置到父节点
                tmp.setLeft(leftNode);
            } else {
                // 设置其左节点为空
                tmp.left = null;
            }
            // 把节点加入到队列中，因为下一层需要处理的就是他
            nodeQueue.offer(tmp.left);

            // 右节点同理，再出一次队列，就是右节点的值
            Integer right = numQueue.poll();
            if (right != null) {
                // 不为空,创建节点
                Node rightNode = new Node();
                rightNode.setVal(right);
                // 设置到父节点
                tmp.setRight(rightNode);
            }
            // 把节点加入到队列中，因为下一层需要处理的就是他
            nodeQueue.offer(tmp.right);
        }

        int i = maxGain(root);
        System.out.println(maxSum);

    }

    private static int maxGain(Node node) {
        if (node == null || node.getVal() == Integer.MIN_VALUE) {
            return 0;
        }

        // 递归计算左右子节点的最大贡献值
        // 只有在最大贡献值大于 0 时，才会选取对应子节点
        int leftGain = Math.max(maxGain(node.left), 0);
        int rightGain = Math.max(maxGain(node.right), 0);

        // 节点的最大路径和取决于该节点的值与该节点的左右子节点的最大贡献值
        int priceNewpath = node.val + leftGain + rightGain;

        // 更新答案
        maxSum = Math.max(maxSum, priceNewpath);

        // 返回节点的最大贡献值
        return node.val + Math.max(leftGain, rightGain);
    }

    private static class Node {

        private int val;

        private Node left;

        private Node right;

        public int getVal() {
            return val;
        }

        public void setVal(int val) {
            this.val = val;
        }

        public Node getLeft() {
            return left;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public Node getRight() {
            return right;
        }

        public void setRight(Node right) {
            this.right = right;
        }
    }

}
