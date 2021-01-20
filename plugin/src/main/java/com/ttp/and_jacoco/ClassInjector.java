/*
 * Copyright (C) 2017, Megatron King
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ttp.and_jacoco;


import com.android.utils.FileUtils;

import org.jacoco.core.diff.DiffAnalyzer;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ClassInjector extends ClassProcessor {

    public ClassInjector(List<String> includes) {
        super(includes);
    }

    @Override
    void processJar(ZipFile zipFile, ZipInputStream zis, ZipOutputStream zos, ZipEntry entryIn, ZipEntry entryOut) throws IOException {
        String entryName = entryIn.getName();
        //com/ttp/newcore/network/CommonDataLoader$4
        //entryName = androidx/collection/MapCollections.class
        if (DiffAnalyzer.getInstance().containsClass(entryName.replace(".class",""))
                && shouldIncludeClass(entryName)) {
//            InputStream inputStream = zipFile.getInputStream(entryIn);
            final Instrumenter instr = new Instrumenter(new OfflineInstrumentationAccessGenerator());
            final byte[] instrumented = instr.instrument(zis, entryName);
            zos.write(instrumented);
        } else {
            copy(zis, zos);
        }
    }

    @Override
    void processClass(File fileIn, File fileOut) throws IOException {
        if (shouldIncludeClass(fileIn)) {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = new BufferedInputStream(new FileInputStream(fileIn));
                os = new BufferedOutputStream(new FileOutputStream(fileOut));
                // For instrumentation and runtime we need a IRuntime instance
                // to collect execution data:
                // The Instrumenter creates a modified version of our test target class
                // that contains additional probes for execution data recording:
                final Instrumenter instr = new Instrumenter(new OfflineInstrumentationAccessGenerator());

                final byte[] instrumented = instr.instrument(is, fileIn.getName());
                os.write(instrumented);
            } finally {
                closeQuietly(os);
                closeQuietly(is);
            }
        } else {
            FileUtils.copyFile(fileIn, fileOut);
        }
    }

}
