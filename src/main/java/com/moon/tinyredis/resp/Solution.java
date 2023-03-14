package com.moon.tinyredis.resp;

import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Solution su = new Solution();
        String[] nums = new String[]{"are","amy","u"};
        su.minFee();
    }

    public void minFee() {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt(); //参加活动的人数
        int[][] vote = new int[n][3];
        boolean[] isFenpei = new boolean[n];
        sc.nextLine();  //吸收enter
        for(int i =0;i<n;i++){
            String line = sc.nextLine();
            for (char ch : line.toCharArray()) {
                vote[i][ch-'A'] = 1;
            }
        }
        int[][] countAndFee = new int[3][3];
        for (int i = 0; i < 3; i++) {
            countAndFee[i][0] = i;
            countAndFee[i][1] = sc.nextInt();
            countAndFee[i][2] = sc.nextInt();
        }
        Arrays.sort(countAndFee, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return o1[2] - o2[2];
            }
        });

        int minFee = 0;

        for (int i = 0; i < countAndFee.length; i++) { // 遍历项目数据
            int count = countAndFee[i][1];
            int fee = countAndFee[i][2];
            for (int k = 0;k< vote.length;k++) {
                if (vote[k][i] == 1 && count > 0 && !isFenpei[k]){
                    count--;
                    minFee += fee;
                    isFenpei[k] = true;
                }
            }
        }
        int count = 0;
        for (int i = 0; i < isFenpei.length; i++) {
            if (isFenpei[i]){
                count++;
            }
        }
        if (count < n) {
            System.out.println("NO");
            System.out.println(count);
        } else {
            System.out.println("YES");
            System.out.println(minFee);
        }
    }
}
