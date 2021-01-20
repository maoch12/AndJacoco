package org.jacoco.core.diff;


import org.jacoco.core.data.MethodInfo;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiffAnalyzer {
    public static final int CURRENT = 0X10;
    public static final int BRANCH = 0X11;

    Set<MethodInfo> currentList = new HashSet<>();
    Set<MethodInfo> branchList = new HashSet<>();

    Set<MethodInfo> diffList = new HashSet<>();

    //com/ttp/newcore/network/CommonDataLoader$4
    Set<String> diffClass = new HashSet<>();

    //int anim abc_tooltip_enter 0x7f01000a
    List<String> resIdLines = new ArrayList<>();//资源id集

    private static DiffAnalyzer instance;

    public static DiffAnalyzer getInstance() {
        if (instance == null) {
            synchronized (DiffAnalyzer.class) {
                if (instance == null)
                    instance = new DiffAnalyzer();
            }
        }
        return instance;
    }

    public void addMethodInfo(MethodInfo methodInfo, int type) {
        if (type == CURRENT) {
            currentList.add(methodInfo);
        } else {
            branchList.add(methodInfo);
        }
    }

    public void diff() {
        if (!currentList.isEmpty() && !branchList.isEmpty()) {
            for (MethodInfo cMethodInfo : currentList) {
                boolean findInBranch = false;
                for (MethodInfo bMethodInfo : branchList) {
                    if (cMethodInfo.className.equals(bMethodInfo.className)
                            && cMethodInfo.methodName.equals(bMethodInfo.methodName)
                            && cMethodInfo.desc.equals(bMethodInfo.desc)) {
                        if (!cMethodInfo.md5.equals(bMethodInfo.md5)) {
                            diffList.add(cMethodInfo);
                        }
                        findInBranch = true;
                        break;
                    }
                }
                if (!findInBranch) {
                    diffList.add(cMethodInfo);
                }
                diffClass.add(cMethodInfo.className);
            }
        }
    }

    public boolean containsMethod(String className, String methodName, String desc) {
        for (MethodInfo methodInfo : diffList) {
            if (className.equals(methodInfo.className) && methodName.equals(methodInfo.methodName) && desc.equals(methodInfo.desc)) {
                return true;
            }
        }
        return false;
    }


    public boolean containsClass(String className) {
        return diffClass.contains(className);
    }

    public void reset() {
        currentList.clear();
        branchList.clear();
        diffList.clear();
    }

    public Set<MethodInfo> getDiffList() {
        return diffList;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (MethodInfo mi : diffList) {
            builder.append(mi.toString());
            builder.append("\n");
        }
        return builder.toString();
    }

    public static void readClasses(String dirPath, int type) {
        File file = new File(dirPath);
        if (!file.exists() || file.getName().equals(".git")) {
            return;
        }
        File[] files = file.listFiles();
        for (File classFile : files) {
            if (classFile.isDirectory()) {
                readClasses(classFile.getAbsolutePath(), type);
            } else {
                if (classFile.getName().endsWith(".class")) {
                    try {
                        doClass(classFile, type);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private static void doClass(File fileIn, int type) throws IOException {
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(fileIn));
            processClass(is, type);
        } finally {
            closeQuietly(is);
        }
    }


    private static void processClass(InputStream classIn, int type) throws IOException {
        ClassReader cr = new ClassReader(classIn);
        ClassVisitor cv = new DiffClassVisitor(Opcodes.ASM5, type);
        cr.accept(cv, 0);
    }

    private static void closeQuietly(Closeable target) {
        if (target != null) {
            try {
                target.close();
            } catch (Exception e) {
                // Ignored.
            }
        }
    }

    public List<String> getResIdLines() {
        return resIdLines;
    }

    public void setResIdLines(List<String> resIdLines) {
        this.resIdLines = resIdLines;
    }
}
