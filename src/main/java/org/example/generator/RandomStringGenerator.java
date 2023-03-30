package org.example.generator;

import java.util.*;

public class RandomStringGenerator {

    private int length;
    private int size;
    public String[] getRandomStringArray(){
        String[] strs = new String[size];
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        HashSet<String> set = new HashSet<>();
        while (set.size() < size){//生成随机字符串到set里面
            sb.setLength(0);
            for (int i = 0; i < length; i++)
                sb.append(chars[random.nextInt(62)]);
            set.add(sb.toString());
        }
        int i= 0;
        for (String s : set)//将set里面的数据存放到数组
            strs[i++] = s;
        return strs;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public RandomStringGenerator(int length, int size) {
        this.length = length;
        this.size = size;
    }
}
