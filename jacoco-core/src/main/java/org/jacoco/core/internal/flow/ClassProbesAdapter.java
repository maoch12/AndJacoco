/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.flow;

import org.jacoco.core.diff.DiffAnalyzer;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AnalyzerAdapter;

/**
 * A {@link org.objectweb.asm.ClassVisitor} that calculates probes for every
 * method.
 */
public class ClassProbesAdapter extends ClassVisitor
        implements IProbeIdGenerator {

    private static final MethodProbesVisitor EMPTY_METHOD_PROBES_VISITOR = new MethodProbesVisitor() {
    };

    protected final ClassProbesVisitor cv;

    private final boolean trackFrames;

    private int counter = 0;

    private String className;

    /**
     * Creates a new adapter that delegates to the given visitor.
     *
     * @param cv          instance to delegate to
     * @param trackFrames if <code>true</code> stackmap frames are tracked and provided
     */
    public ClassProbesAdapter(final ClassProbesVisitor cv,
                              final boolean trackFrames) {
        super(InstrSupport.ASM_API_VERSION, cv);
        this.cv = cv;
        this.trackFrames = trackFrames;
    }

    @Override
    public void visit(final int version, final int access, final String name,
                      final String signature, final String superName,
                      final String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature,
                                     final String[] exceptions) {
        if (DiffAnalyzer.getInstance().containsMethod(className, name, desc)) {
//        if ("com/example/jacoco_plugin/Hello".equals(className) && ("()V".equals(desc) && "lambda$hello$0".equals(name)) || ("()V".equals(desc) && "hello".equals(name))) {
            final MethodProbesVisitor methodProbes;
            final MethodProbesVisitor mv = (MethodProbesVisitor) cv.visitMethod(access, name, desc,
                    signature, exceptions);
            if (mv == null) {
                // We need to visit the method in any case, otherwise probe ids
                // are not reproducible
                methodProbes = EMPTY_METHOD_PROBES_VISITOR;
            } else {
                methodProbes = mv;
            }
            return new MethodSanitizer(null, access, name, desc, signature,
                    exceptions) {

                @Override
                public void visitEnd() {
                    super.visitEnd();
                    LabelFlowAnalyzer.markLabels(this);
                    final MethodProbesAdapter probesAdapter = new MethodProbesAdapter(
                            methodProbes, ClassProbesAdapter.this);
                    if (trackFrames) {
                        final AnalyzerAdapter analyzer = new AnalyzerAdapter(
                                ClassProbesAdapter.this.className, access, name, desc,
                                probesAdapter);
                        probesAdapter.setAnalyzer(analyzer);
                        methodProbes.accept(this, analyzer);
                    } else {
                        methodProbes.accept(this, probesAdapter);
                    }
                }
            };
        } else {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }

    @Override
    public void visitEnd() {
        cv.visitTotalProbeCount(counter);
        super.visitEnd();
    }

    // === IProbeIdGenerator ===

    public int nextId() {
        return counter++;
    }

}
