package com.andjacoco.demo;

import org.jacoco.core.diff.DiffAnalyzer;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        //生成差异方法
        DiffAnalyzer.readClasses("/Users/wzh/ttpc/gitlab/Android-Jacoco/app/build/tmp/a", DiffAnalyzer.CURRENT);
        DiffAnalyzer.readClasses("/Users/wzh/ttpc/gitlab/Android-Jacoco/app/build/tmp/b", DiffAnalyzer.BRANCH);
        DiffAnalyzer.getInstance().diff();
        System.out.println(DiffAnalyzer.getInstance().toString());

        String hex=String.format("0x%8s",Integer.toHexString(100)).replace(' ','0');
        System.out.println(hex);
/*
        Process process = Runtime.getRuntime().exec("where git");
        InputStream inputStream = process.getInputStream();

        StringBuilder sb = new StringBuilder();
        String line;

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        String str = sb.toString();
        System.out.println(str);*/
    }
}
