/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *
 *******************************************************************************/
package com.ttp.and_jacoco.report;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.MultiSourceFileLocator;
import org.jacoco.report.html.HTMLFormatter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This example creates a HTML report for eclipse like projects based on a
 * single execution data store called jacoco.exec. The report contains no
 * grouping information.
 * <p>
 * The class files under test must be compiled with debug information, otherwise
 * source highlighting will not work.
 */
public class ReportGenerator {

    private final String title;

    private final String executionDataDir;
    private final List<File> classesDirectories;
    private final List<File> sourceDirectories;
    private final File reportDirectory;

    private ExecFileLoader execFileLoader;

    /**
     * Create a new generator based for the given project.
     */
    public ReportGenerator(final String execDir, List<File> classesDir, List<File> sourceDir, File reportDir) {
        this.title = "CodeCoverageReport";
        this.executionDataDir = execDir;
        this.classesDirectories = classesDir;
        this.sourceDirectories = sourceDir;
        this.reportDirectory = reportDir;
    }

    /**
     * Create the report.
     *
     * @throws IOException
     */
    public void create() throws IOException {

        // Read the jacoco.exec file. Multiple data files could be merged
        // at this point
       try{
           loadExecutionData();
           // Run the structure analyzer on a single class folder to build up
           // the coverage model. The process would be similar if your classes
           // were in a jar file. Typically you would create a bundle for each
           // class folder and each jar you want in your report. If you have
           // more than one bundle you will need to add a grouping node to your
           // report
           final IBundleCoverage bundleCoverage = analyzeStructure();

           createReport(bundleCoverage);

       }catch (Exception e){//不中断流程
           e.printStackTrace();
           System.err.println(e.getMessage());
       }

    }

    private void createReport(final IBundleCoverage bundleCoverage)
            throws IOException {

        // Create a concrete report visitor based on some supplied
        // configuration. In this case we use the defaults
        final HTMLFormatter htmlFormatter = new HTMLFormatter();
        final IReportVisitor visitor = htmlFormatter
                .createVisitor(new FileMultiReportOutput(reportDirectory));

        // Initialize the report with all of the execution and session
        // information. At this point the report doesn't know about the
        // structure of the report being created
        visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
                execFileLoader.getExecutionDataStore().getContents());

        // Populate the report structure with the bundle coverage information.
        // Call visitGroup if you need groups in your report.
        MultiSourceFileLocator sourceFileLocator = new MultiSourceFileLocator(4);
        if (sourceDirectories != null || !sourceDirectories.isEmpty()) {
            for (File source : sourceDirectories) {
                sourceFileLocator.add(new DirectorySourceFileLocator(source, "utf-8", 4));
            }
        }
        visitor.visitBundle(bundleCoverage, sourceFileLocator);
        // Signal end of structure information to allow report to write all
        // information out
        visitor.visitEnd();

    }

    private void loadExecutionData() {
        execFileLoader = new ExecFileLoader();
        load(execFileLoader,executionDataDir);

    }


    /**
     * 加载dump文件
     *
     * @param loader
     * @throws RuntimeException
     */
    public void load(final ExecFileLoader loader,String dir) throws RuntimeException {
        for (final File fileSet : fileSets(dir)) {
            final File inputFile = new File(dir, fileSet.getName());
            if (inputFile.isDirectory()) {
                continue;
            }
            try {
                System.out.println("Loading execution data file " + inputFile.getAbsolutePath());
                loader.load(inputFile);
            } catch (final IOException e) {
                throw new RuntimeException("Unable to read "
                        + inputFile.getAbsolutePath(), e);
            }
        }
    }

    private List<File> fileSets(String dir) {
        System.out.println(dir);
        List<File> fileSetList = new ArrayList<File>();
        File path = new File(dir);
        if (!path.exists()) {
            throw new NullPointerException("No path name is :" + dir);
        }else if(path.isFile() && (path.getName().endsWith(".exec") || path.getName().endsWith(".ec"))){
            fileSetList.add(path);
            return fileSetList;
        }
        File[] files = path.listFiles();
        if (files == null || files.length == 0) {
            throw new NullPointerException(path.getAbsolutePath() + " files is empty");
        }

        for (File file : files) {
            if (file.getName().endsWith(".exec") || file.getName().endsWith(".ec")) {
                fileSetList.add(file);
            }
        }
        return fileSetList;
    }


    private IBundleCoverage analyzeStructure() throws IOException {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(
                execFileLoader.getExecutionDataStore(), coverageBuilder);

        if (classesDirectories != null && !classesDirectories.isEmpty()) {
            for (File classDir : classesDirectories) {
                analyzer.analyzeAll(classDir);
            }
        }

        return coverageBuilder.getBundle(title);
    }



    /**
     * Starts the report generation process
     *
     * @param args Arguments to the application. This will be the location of the
     *             eclipse projects that will be used to generate reports for
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {
        File exec = new File("/Users/wzh/ttpc/gitlab/Dealer-Android-Rebuild/app/build/outputs/coverage");

        List<File> sourceDirs = new ArrayList<>();
        sourceDirs.add(new File("/Users/wzh/ttpc/gitlab/Dealer-Android-Rebuild/app/src/main/java"));

        List<File> classDirs = new ArrayList<>();
        classDirs.add(new File("/Users/wzh/ttpc/gitlab/Dealer-Android-Rebuild/app/classes"));

        File reportDir = new File("/Users/wzh/ttpc/gitlab/Dealer-Android-Rebuild/app/build/report");
        ReportGenerator generator = new ReportGenerator(exec.getAbsolutePath(), classDirs, sourceDirs, reportDir);
        generator.create();
    }

}
