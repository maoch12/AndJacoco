package com.ttp.and_jacoco.merge;

import org.jacoco.core.tools.ExecFileLoader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MergeExec {
    private final String path;
    private final File destFile;

    public MergeExec(String path) {
        this.path = path;
        this.destFile = new File(path + "/jacoco-" + getDate() + ".ec");
    }

    private List<File> fileSets(String dir) {
        System.out.println(dir);
        List<File> fileSetList = new ArrayList<File>();
        File path = new File(dir);
        if (!path.exists()) {
            throw new NullPointerException("No path name is :" + dir);
        }
        File[] files = path.listFiles();
        if (files == null || files.length == 0) {
            throw new NullPointerException(path.getAbsolutePath() + " files is empty");
        }

        for (File file : files) {
            if (file.getName().endsWith(".exec") || file.getName().endsWith(".ec")) {
                System.out.println("文件:" + file.getAbsolutePath());
                fileSetList.add(file);
            } else {
                System.out.println("非exec文件:" + file.getAbsolutePath());
            }
        }
        return fileSetList;
    }

    public File getDestFile() {
        return destFile;
    }

    private String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date(System.currentTimeMillis());
        return sdf.format(d);
    }

    public void executeMerge() throws RuntimeException {

        final ExecFileLoader loader = new ExecFileLoader();
        load(loader);
        save(loader);

    }

    /**
     * 加载dump文件
     *
     * @param loader
     * @throws RuntimeException
     */
    public void load(final ExecFileLoader loader) throws RuntimeException {
        for (final File fileSet : fileSets(this.path)) {
            System.out.println(fileSet.getAbsoluteFile());
            final File inputFile = new File(this.path, fileSet.getName());
            if (inputFile.isDirectory()) {
                continue;
            }
            try {
                System.out.println("Loading execution data file " + inputFile.getAbsolutePath());
                loader.load(inputFile);
                System.out.println(loader.getExecutionDataStore().getContents());
            } catch (final IOException e) {
                throw new RuntimeException("Unable to read "
                        + inputFile.getAbsolutePath(), e);
            }
        }
    }

    /**
     * 执行合并文件
     *
     * @param loader
     * @throws RuntimeException
     */
    public void save(final ExecFileLoader loader) {
        if (loader.getExecutionDataStore().getContents().isEmpty()) {
            System.out.println("Skipping JaCoCo merge execution due to missing execution data files");
            return;
        }
        System.out.println("Writing merged execution data to " + this.destFile.getAbsolutePath());
        try {
            loader.save(this.destFile, false);
        } catch (final IOException e) {
            throw new RuntimeException("Unable to write merged file "
                    + this.destFile.getAbsolutePath(), e);
        }
    }
}
