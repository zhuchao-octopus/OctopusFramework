package com.common.util;

/**
 * 计算两个字符串的编辑距离。
 */
// 算法思想：
// 用数组b[i][j]记录s1[1..i]和s2[1..j]之间的编辑距离。
// 若已经b[i-1][j-1]，b[i][j-1]及b[i-1][j]，计算b[i][j]可能有以下几种情况：
//
// 1) s1[i] == s2[i]，则b[i][j] = b[i-1][j-1]，即不需要字符操作
//
// 2) s1[i] != s2[i]，则可能有三种操作：
//
// 　a) b[i][j] = b[i][j-1] + 1，即插入字符s2[j]；
//
// 　b) b[i][j] = b[i-1][j] + 1，即删除字符s1[i]；
//
// c) b[i][j] = b[i-1][j-1] + 1，即将s1[i]改为s2[j]。
//
// 故递归公式可总结为：
// b[i][j]=min(b[i-1]][j] + 1,b[i][j-1] + 1,b[i-1][j-1]+(s1[i]==s2[i]?0:1));

public class EditDistance {
    /*
     * private String s; private String aim;
     */

    /*
     * public EditDistance(){
     *
     * }
     */

    /*
     * public EditDistance(String s, String aim) { this.s = s; this.aim = aim; }
     */

    /*
     * public void setStrings(String s, String aim){ this.s = s; this.aim = aim;
     * }
     */

    public static double getSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return Double.MAX_VALUE;
        }
        int distance = calculteEditDistance(str1, str2);
        int maxLen = str1.length() > str2.length() ? str1.length() : str2.length();
        double similarity = (double) (maxLen - distance) / maxLen;
        return similarity;
    }

    public static int calculteEditDistance(String str1, String str2) {
        int sLength = str1.length() + 1;
        int aLength = str2.length() + 1;
        int[][] b = new int[aLength][sLength];
        for (int i = 1; i < sLength; i++) {
            b[0][i] = i;
        }
        for (int k = 1; k < aLength; k++) {
            b[k][0] = k;
        }
        for (int j = 1; j < sLength; j++) {
            for (int i = 1; i < aLength; i++) {
                int x = b[i - 1][j] + 1;
                int y = b[i][j - 1] + 1;
                int z = b[i - 1][j - 1] + ((str1.charAt(j - 1) == str2.charAt(i - 1)) ? 0 : 1);
                b[i][j] = min(x, y, z);
            }
        }
        return b[aLength - 1][sLength - 1];
    }

    public static int min(int x, int y, int z) {
        int temp = x;
        if (temp > y) temp = y;
        if (temp > z) temp = z;
        return temp;
    }

    /*
     * public static void main(String[] args) { String str1 = "abc"; String str2
     * = "bbbdc"; EditDistance ed = new EditDistance(str1, str2); int distance =
     * ed.calculteEditDistance(); System.out.println("distance = " + distance);
     * }
     */
}
