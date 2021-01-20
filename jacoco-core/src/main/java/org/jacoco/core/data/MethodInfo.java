package org.jacoco.core.data;

import java.util.Arrays;
import java.util.Objects;

public class MethodInfo {
    public String className;
    public String methodName;
    public String desc;
    public String signature;
    public String[] exceptions;
    public String md5;//有方法本体，注解构成的md5

    @Override
    public int hashCode() {
        int result = Objects.hash(className, methodName, desc, signature, md5);
        result = 31 * result + Arrays.hashCode(exceptions);
        return result;
    }

    @Override
    public String toString() {
        return "MethodInfo{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", desc='" + desc + '\'' +
                ", signature='" + signature + '\'' +
                ", exceptions=" + Arrays.toString(exceptions) +
                ", md5='" + md5 + '\'' +
                '}';
    }
}
