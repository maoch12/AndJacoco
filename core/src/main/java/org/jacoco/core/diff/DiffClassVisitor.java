package org.jacoco.core.diff;


import org.jacoco.core.data.MethodInfo;
import org.jacoco.core.tools.Util;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * <li>Package:com.ttp.gnirts_plugin</li>
 * <li>Author: Administrator  </li>
 * <li>Date: 2020/9/15</li>
 * <li>Description:   </li>
 */
public class DiffClassVisitor extends ClassVisitor {
    private String className;
    //CURRENT = 0X10;
    //BRANCH = 0X11;
    private int type;

    public DiffClassVisitor(int api, int type) {
        super(api);
        this.type = type;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return super.visitField(access, name, desc, signature, value);
    }

    /**
     * @param access
     * @param name
     * @param desc
     * @param signature
     * @param exceptions
     * @return
     */

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
//        System.out.println("className:" + className + "  methodName:" + name + "  desc:" + desc + "  signature:" + signature);
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        final MethodInfo methodInfo = new MethodInfo();
        methodInfo.className = className;
        methodInfo.methodName = name;
        methodInfo.desc = desc;
        methodInfo.signature = signature;
        methodInfo.exceptions = exceptions;
        mv = new MethodVisitor(Opcodes.ASM5, mv) {
            StringBuilder builder = new StringBuilder();

            //开始访问方法体
            @Override
            public void visitCode() {
//                System.out.println("visitCode");
                super.visitCode();
            }

            //访问方法一个参数
            //todo 需要验证哪些代码会调用这个方法
            @Override
            public void visitParameter(String name, int access) {
                builder.append(name);
                builder.append(access);
//                System.out.println("visitParameter--name:" + name + "  access" + access);
                super.visitParameter(name, access);
            }

            //访问方法的一个注解
            //@TargetApi(21)
            //public static void Toast(Context context, String s) {
            //       ...
            //}
            //todo 注解中的参数21不知道在哪里获取
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                builder.append(desc);
                builder.append(visible);
//                System.out.println("visitAnnotation--desc:" + desc + "  visible:" + visible);
                return super.visitAnnotation(desc, visible);
            }

            //访问方法签名上的一个类型的注解
            //不知道怎么访问这个，但是需要参与md5
            //todo 需要验证哪些代码会调用这个方法
            @Override
            public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
                builder.append(typeRef);
                builder.append(typePath.toString());
                builder.append(desc);
                builder.append(visible);
//                System.out.println("visitTypeAnnotation--typeRef:" + typeRef + "  TypePath:" + typePath.toString() + "  desc:" + desc + "  visible" + visible);
                return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
            }

            //访问参数的注解，返回一个AnnotationVisitor可以访问该注解值;
            //public static void Toast(@Nullable Context context, @Nullable String s) {
            //     ...
            // }
            @Override
            public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
                builder.append(parameter);
                builder.append(desc);
                builder.append(visible);
//                System.out.println("visitParameterAnnotation--parameter:" + parameter + "  desc:" + desc + "  visible" + visible);
                return super.visitParameterAnnotation(parameter, desc, visible);
            }

            //访问此方法的非标准属性
            //不知道干嘛的，暂不参与md5
            @Override
            public void visitAttribute(Attribute attr) {
//                System.out.println("visitAttribute--attr:" + attr.toString());
                super.visitAttribute(attr);
            }

            //访问方法局部变量的当前状态以及操作栈成员信息，方法栈必须是expanded 格式或者compressed格式,该方法必须在visitInsn方法前调用
            //visitFrame--type:3  nLocal:0  local:[Ljava.lang.Object;@b071554  nStack:0  stack:[Ljava.lang.Object;@702b9ffd
            //与方法体有关，需要参与MD5，排除local和stack
            @Override
            public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
                builder.append(type);
                builder.append(nLocal);
                builder.append(nStack);
//                System.out.println("visitFrame--type:" + type + "  nLocal:" + nLocal + "  local:" + local.toString() + "  nStack:" + nStack + "  stack:" + stack);
                super.visitFrame(type, nLocal, local, nStack, stack);
            }

            //访问零操作数指令
            //与方法体有关，需要参与md5
            @Override
            public void visitInsn(int opcode) {
                builder.append(opcode);
//                System.out.println("visitInsn--opcode:" + opcode);
                super.visitInsn(opcode);
            }

            //访问数值类型指令
            //与方法体有关，需要参与md5
            @Override
            public void visitIntInsn(int opcode, int operand) {
                builder.append(opcode);
                builder.append(operand);
//                System.out.println("visitIntInsn--opcode:" + opcode + "  operand:" + operand);
                super.visitIntInsn(opcode, operand);
            }

            //访问本地变量类型指令
            //与方法体有关，需要参与md5
            @Override
            public void visitVarInsn(int opcode, int var) {
                builder.append(opcode);
                builder.append(var);
//                System.out.println("visitVarInsn--opcode:" + opcode + "  var:" + var);
                super.visitVarInsn(opcode, var);
            }

            //访问类型指令，类型指令会把类的内部名称当成参数Type
            //与方法体有关，需要参与md5
            @Override
            public void visitTypeInsn(int opcode, String type) {
                builder.append(opcode);
                builder.append(type);
//                System.out.println("visitTypeInsn--opcode:" + opcode + "  type:" + type);
                super.visitTypeInsn(opcode, type);
            }

            //域操作指令，用来加载或者存储对象的Field；
            //与方法体有关，需要参与md5
            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                builder.append(opcode);
                builder.append(owner);
                builder.append(name);
                builder.append(desc);
//                System.out.println("visitFieldInsn--opcode:" + opcode + "  owner:" + owner + "  name:" + name + "  desc:" + desc);
                super.visitFieldInsn(opcode, owner, name, desc);
            }

            //问方法操作指令
            //与方法体有关，需要参与md5
            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                builder.append(opcode);
                builder.append(owner);
                builder.append(name);
                builder.append(desc);
                builder.append(itf);
//                System.out.println("visitMethodInsn--opcode:" + opcode + "  owner:" + owner + "  name:" + name + "  desc:" + desc + "  itf:" + itf);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }

            //访问一个invokedynamic指令
            //与方法体有关，需要参与md5
            @Override
            public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
                builder.append(name);
                builder.append(desc);
                builder.append(bsm.toString());
//                System.out.println("visitInvokeDynamicInsn--name:" + name + "  desc:" + desc + "  bsm:" + bsm.toString() + "  bsmArgs:" + bsmArgs.toString());
                super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
            }

            //访问比较跳转指令
            //与方法体有关，需要参与md5
            @Override
            public void visitJumpInsn(int opcode, Label label) {
                builder.append(opcode);
//                System.out.println("visitJumpInsn--opcode:" + opcode + "  label:" + label.toString());
                super.visitJumpInsn(opcode, label);
            }

            //访问label，当会在调用该方法后访问该label标记一个指令
            //不作为方法md5的条件
            @Override
            public void visitLabel(Label label) {
                super.visitLabel(label);
            }

            //访问ldc指令，也就是访问常量池索引
            //与方法体有关，需要参与md5
            @Override
            public void visitLdcInsn(Object cst) {
//                System.out.println("visitLdcInsn--cst:" + cst.toString() + " " + cst.getClass());
                //资源id 每次编译都会变，所以不参与 0x7f010008
                if (!(cst instanceof Integer) || !isResourceId((Integer)cst)) {
                    builder.append(cst.toString());
                }
                super.visitLdcInsn(cst);
            }

            //访问本地变量索引增加指令
            //与方法体有关，需要参与md5
            @Override
            public void visitIincInsn(int var, int increment) {
                builder.append(var);
                builder.append(increment);
//                System.out.println("visitIincInsn--var:" + var + "  increment:" + increment);
                super.visitIincInsn(var, increment);
            }

            //访问switch跳转指令
            //与方法体有关，需要参与md5
            @Override
            public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
                builder.append(min);
                builder.append(max);
//                System.out.println("visitTableSwitchInsn--min:" + min + "  max:" + max + "  dflt:" + dflt.toString() + "  labels:" + labels.toString());
                super.visitTableSwitchInsn(min, max, dflt, labels);
            }

            //访问查询跳转指令
            //与方法体有关，需要参与md5
            @Override
            public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
                if (keys != null && keys.length > 0) {
                    for (int key : keys) {
                        if(!isResourceId(key)){
                            builder.append(key);
                        }
                    }
                }
//                System.out.println("visitLookupSwitchInsn--dflt:" + dflt.toString() + "  keys:" + keys.toString() + "  labels:" + labels.toString());
                super.visitLookupSwitchInsn(dflt, keys, labels);
            }

            //访问多维数组指令
            //与方法体有关，需要参与md5
            @Override
            public void visitMultiANewArrayInsn(String desc, int dims) {
                builder.append(desc);
                builder.append(dims);
//                System.out.println("visitMultiANewArrayInsn--desc:" + desc + "  dims:" + dims);
                super.visitMultiANewArrayInsn(desc, dims);
            }

            //访问指令注解，必须在访问注解之后调用
            //与方法体有关，需要参与md5
            @Override
            public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
                builder.append(typeRef);
                builder.append(typePath.toString());
                builder.append(desc);
                builder.append(visible);
//                System.out.println("visitInsnAnnotation--typeRef:" + typeRef + "  TypePath:" + typePath.toString() + "  desc:" + desc + "  visible:" + visible);
                return super.visitInsnAnnotation(typeRef, typePath, desc, visible);
            }

            //方法try--catch块
            //与方法体有关，需要参与md5,只需将type加入即可
            @Override
            public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                builder.append(type);
//                System.out.println("visitTryCatchBlock--start:" + start.toString() + "  end:" + end.toString() + "  handler:" + handler.toString() + "  type:" + type);
                super.visitTryCatchBlock(start, end, handler, type);
            }

            //访问try...catch块上异常处理的类型注解，必须在调用visitTryCatchBlock之后调用;
            //与方法体有关，需要参与md5
            @Override
            public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
                builder.append(typeRef);
                builder.append(typePath.toString());
                builder.append(desc);
                builder.append(visible);
//                System.out.println("visitTryCatchAnnotation--typeRef:" + typeRef + "  typePath:" + typePath.toString() + "  desc:" + desc + "  visible:" + visible);
                return super.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
            }

            //访问局部变量描述
            //与方法体有关，需要参与md5
            @Override
            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                builder.append(name);
                builder.append(desc);
                builder.append(signature);
                builder.append(index);
//                System.out.println("visitLocalVariable--name:" + name + "  desc:" + desc + "  signature:" + signature + "  start:" + start.toString() + "  end:" + end.toString() + "  index:" + index);
                super.visitLocalVariable(name, desc, signature, start, end, index);
            }

            //访问局部变量类型的注释
            //与方法体有关，需要参与md5，注意Lable相关的都不需要加入
            @Override
            public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
                builder.append(typeRef);
                builder.append(typePath.toString());
                if (index != null && index.length > 0) {
                    for (int i : index) {
                        builder.append(i);
                    }
                }
                builder.append(desc);
                builder.append(visible);
//                System.out.println("visitLocalVariableAnnotation--typeRef:" + typeRef + "  TypePath:" + typePath.toString() + "  desc:" + desc + "  visible" + visible);
                return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
            }

            //访问行号描述
            //不作为方法md5的条件
            @Override
            public void visitLineNumber(int line, Label start) {
                super.visitLineNumber(line, start);
            }

            //最大的操作数栈与本地变量表个数
            //需要参与md5
            @Override
            public void visitMaxs(int maxStack, int maxLocals) {
                builder.append(maxStack);
                builder.append(maxLocals);
//                System.out.println("visitMaxs--maxStack:" + maxStack + "  maxLocals:" + maxLocals);
                super.visitMaxs(maxStack, maxLocals);
            }

            //方法访问结束
            @Override
            public void visitEnd() {
                String md5 = "";
                try {
                    md5 = Util.MD5(builder.toString());
                } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                methodInfo.md5 = md5;
                DiffAnalyzer.getInstance().addMethodInfo(methodInfo, type);
//                System.out.println("visitEnd>>>md5:" + md5);
                super.visitEnd();
            }
        };
        return mv;
    }

    private boolean isResourceId(Integer id) {
        String hex=String.format("0x%8s",Integer.toHexString(id)).replace(' ','0');
        return hex.startsWith("0x7f");//0x7f 一定为id，不参与diff
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
