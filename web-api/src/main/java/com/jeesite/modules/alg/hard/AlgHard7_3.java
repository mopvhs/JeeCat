package com.jeesite.modules.alg.hard;

import java.util.Scanner;

public class AlgHard7_3 {

    public static void main(String[] args) {


        Scanner in = new Scanner(System.in);

        String l1 = in.nextLine();
        String l2 = in.nextLine();
        int k = Integer.parseInt(in.nextLine());

        String[] split = l1.split(",");
        String[] split2 = l2.split(",");
        int[] nums1 = new int[split.length];
        int[] nums2 = new int[split2.length];

        for (int i = 0; i < split.length; i++) {
            nums1[i] = Integer.parseInt(split[i]);
        }
        for (int i = 0; i < split2.length; i++) {
            nums2[i] = Integer.parseInt(split2[i]);
        }

        // 最大的数组
        int[] maxNums = new int[k];

        // 注意 i是元素个数，不是偏移量
        for (int i = 0; i <= k && i <= nums1.length; i++) {

            if (k - i >= 0 && k-i <= nums2.length) {
                // num2需要的数量不足，可以直接返回

                // 假的从数组 1 中获取i个元素，那么剩余的元素个数就需要从另外一个数组获取(k - i)
                int[] maxNums1 = getMaxNums(nums1, i);
                int[] maxNums2 = getMaxNums(nums2, k - i);

                // 数组合并
                int[] merge = merge(maxNums1, maxNums2);
                // 判断是否比之前的大
                if (!compare(maxNums, merge, 0, 0)) {
                    maxNums = merge;
                }
            }

        }

        for (int i = 0; i < maxNums.length; i++) {
            if (i == maxNums.length - 1) {
                System.out.print(maxNums[i]);
                break;
            }
            System.out.print(maxNums[i] + ",");
        }
    }


    public static int[] merge(int[] nums1, int[] nums2) {
        int[] res = new int[nums2.length + nums1.length];

        // 双指针，哪个大获取哪个，相同的情况下，对比后面的元素大小
        int p1 = 0; int p2 = 0;

        for (int i = 0; i < res.length; i++) {

            // 对比大小
            if (compare(nums1, nums2, p1, p2)) {
                res[i] = nums1[p1];
                p1++;
            } else {
                res[i] = nums2[p2];
                p2++;
            }
        }
        return res;
    }

    public static boolean compare(int[] nums1, int[] nums2, int p1, int p2) {

        // 判断p1 p2 是否已经越界
        // p1越界用nums2
        if (p1 >= nums1.length) {
            return false;
        }
        // p2越界用nums1
        if (p2 >= nums2.length) {
            return true;
        }

        // 判断值是否相同，相同则对比后续的值
        if (nums1[p1] == nums2[p2]) {
            return compare(nums1, nums2, p1 + 1, p2 + 1);
        }

        // 如果p1大于 p2，用p1，否则p2
        return nums1[p1] > nums2[p2];
    }

    public static int[] getMaxNums(int[] nums, int len) {
        if (len <= 0 || nums.length < len) {
            return new int[0];
        }
        
        int[] maxNums = new int[len];
        // 可以丢弃的元素个数
        int dropCount = nums.length - len;

        // 表示已经占据了n个位置，并且也是当前元素的位置
        int cur = 1;

        maxNums[0] = nums[0];
        // 从第一位开始找判断最大的，如果最大的元素在第一位,那么就直接赋值
        for (int i = 1; i < nums.length; i++) {
            // 新的元素，向前对比，是不是比他们都大，并且需要保证还有可以丢弃的元素
            while (cur > 0 && maxNums[cur - 1] < nums[i] && dropCount > 0) {
                // 丢弃一个元素
                dropCount--;
                // 位置前移
                cur--;
            }

            // 位置还未占满len的长度
            if (cur < len) {
                // 赋值
                maxNums[cur] = nums[i];
                cur++;
            } else {
                // 避免超过边界，少删元素
                dropCount--;
            }
        }

        return maxNums;
    }
}
