package io.kenji.courier.proxy.asm.factory;

import io.kenji.courier.proxy.asm.proxy.ASMProxy;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-23
 **/
public class ASMGenerateFactory {

    private static final Integer DEFAULT_NUM = 1;

    public static byte[] generateClass(Class<?>[] interfaces, String proxyClassName) {
        //Create ClassWriter object, compute stack frame and local variable table size
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        //Create Java version, accessFlags, class name, superclass, interface
        String internalName = Type.getInternalName(ASMProxy.class);
        classWriter.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, proxyClassName, null, internalName, getInterfacesName(interfaces));
        //Create<init>
        createInit(classWriter);
        //Create static
        addStatic(classWriter, interfaces);
        //Create<clinit>
        addClinit(classWriter, interfaces, proxyClassName);
        //Implement method of interface
        addInterfaceImpl(classWriter, interfaces, proxyClassName);
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private static void addInterfaceImpl(ClassWriter classWriter, Class<?>[] interfaces, String proxyClassName) {
        for (Class<?> anInterface : interfaces) {
            for (int i = 0; i < anInterface.getMethods().length; i++) {
                Method method = anInterface.getMethods()[i];
                String methodName = "_" + anInterface.getSimpleName() + "_" + i;
                MethodVisitor methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method), null, new String[]{Type.getInternalName(Exception.class)});
                methodVisitor.visitCode();
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                methodVisitor.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(ASMProxy.class), "handler", "Ljava/lang/reflect/InvocationHandler;");
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, proxyClassName, methodName, Type.getDescriptor(Method.class));
                switch (method.getParameterCount()) {
                    case 1 -> methodVisitor.visitInsn(Opcodes.ICONST_1);
                    case 2 -> methodVisitor.visitInsn(Opcodes.ICONST_2);
                    case 3 -> methodVisitor.visitInsn(Opcodes.ICONST_3);
                    default -> methodVisitor.visitVarInsn(Opcodes.BIPUSH, method.getParameterCount());
                }
                methodVisitor.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(Object.class));
                for (int paramIndex = 0; paramIndex < method.getParameterCount(); paramIndex++) {
                    methodVisitor.visitInsn(Opcodes.DUP);
                    switch (paramIndex) {
                        case 0 -> methodVisitor.visitInsn(Opcodes.ICONST_0);
                        case 1 -> methodVisitor.visitInsn(Opcodes.ICONST_1);
                        case 2 -> methodVisitor.visitInsn(Opcodes.ICONST_2);
                        case 3 -> methodVisitor.visitInsn(Opcodes.ICONST_3);
                        default -> methodVisitor.visitVarInsn(Opcodes.BIPUSH, paramIndex);
                    }
                    methodVisitor.visitVarInsn(Opcodes.ALOAD, paramIndex + 1);
                    methodVisitor.visitInsn(Opcodes.AASTORE);
                }
                methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, Type.getInternalName(InvocationHandler.class), "invoke",
                        "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", true);
                addReturn(methodVisitor, method.getReturnType());
                methodVisitor.visitMaxs(DEFAULT_NUM, DEFAULT_NUM);
                methodVisitor.visitEnd();
            }
        }
    }

    private static void addReturn(MethodVisitor methodVisitor, Class<?> returnType) {
        if (returnType.isAssignableFrom(Void.class)) {
            methodVisitor.visitInsn(Opcodes.RETURN);
            return;
        }
        if (returnType.isAssignableFrom(boolean.class)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Boolean.class));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Boolean.class), "booleanValue", "()Z", false);
            methodVisitor.visitInsn(Opcodes.IRETURN);
        } else if (returnType.isAssignableFrom(int.class)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Integer.class));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Integer.class), "intValue", "()I", false);
            methodVisitor.visitInsn(Opcodes.IRETURN);
        } else if (returnType.isAssignableFrom(long.class)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Long.class));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Long.class), "longValue", "()J", false);
            methodVisitor.visitInsn(Opcodes.LRETURN);
        } else if (returnType.isAssignableFrom(short.class)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Short.class));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Short.class), "shortValue", "()S", false);
            methodVisitor.visitInsn(Opcodes.IRETURN);
        } else if (returnType.isAssignableFrom(byte.class)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Byte.class));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Byte.class), "byteValue", "()B", false);
            methodVisitor.visitInsn(Opcodes.IRETURN);
        } else if (returnType.isAssignableFrom(char.class)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Character.class));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Character.class), "charValue", "()C", false);
            methodVisitor.visitInsn(Opcodes.IRETURN);
        } else if (returnType.isAssignableFrom(float.class)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Float.class));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Float.class), "floatValue", "()F", false);
            methodVisitor.visitInsn(Opcodes.FRETURN);
        } else if (returnType.isAssignableFrom(double.class)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Double.class));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Double.class), "doubleValue", "()D", false);
            methodVisitor.visitInsn(Opcodes.DRETURN);
        } else {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(returnType));
            methodVisitor.visitInsn(Opcodes.ARETURN);
        }
    }

    private static void addClinit(ClassWriter classWriter, Class<?>[] interfaces, String proxyClassName) {
        MethodVisitor methodVisitor = classWriter.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        methodVisitor.visitCode();
        for (Class<?> anInterface : interfaces) {
            for (int i = 0; i < anInterface.getMethods().length; i++) {
                Method method = anInterface.getMethods()[i];
                String methodName = "_" + anInterface.getSimpleName() + "_" + i;
                methodVisitor.visitLdcInsn(anInterface.getName());
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Class.class), "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false);
                methodVisitor.visitLdcInsn(method.getName());
                if (method.getParameterCount() == 0) {
                    methodVisitor.visitInsn(Opcodes.ACONST_NULL);
                } else {
                    switch (method.getParameterCount()) {
                        case 1 -> methodVisitor.visitInsn(Opcodes.ICONST_1);
                        case 2 -> methodVisitor.visitInsn(Opcodes.ICONST_2);
                        case 3 -> methodVisitor.visitInsn(Opcodes.ICONST_3);
                        default -> methodVisitor.visitVarInsn(Opcodes.BIPUSH, method.getParameterCount());
                    }
                    methodVisitor.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(Class.class));
                    for (int paramIndex = 0; paramIndex < method.getParameterTypes().length; paramIndex++) {
                        Class<?> parameterType = method.getParameterTypes()[paramIndex];
                        methodVisitor.visitInsn(Opcodes.DUP);
                        switch (paramIndex) {
                            case 0 -> methodVisitor.visitInsn(Opcodes.ICONST_0);
                            case 1 -> methodVisitor.visitInsn(Opcodes.ICONST_1);
                            case 2 -> methodVisitor.visitInsn(Opcodes.ICONST_2);
                            case 3 -> methodVisitor.visitInsn(Opcodes.ICONST_3);
                            default -> methodVisitor.visitVarInsn(Opcodes.BIPUSH, paramIndex);
                        }
                        methodVisitor.visitLdcInsn(parameterType.getName());
                        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Class.class),
                                "forName",
                                "(Ljava/lang/String;)Ljava/lang/Class;",
                                false
                        );
                        methodVisitor.visitInsn(Opcodes.AASTORE);
                    }
                }
                methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Class.class),
                        "getMethod",
                        "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
                        false
                );
                methodVisitor.visitFieldInsn(Opcodes.PUTSTATIC, proxyClassName, methodName, Type.getDescriptor(Method.class));
            }
            methodVisitor.visitInsn(Opcodes.RETURN);
        }
        methodVisitor.visitMaxs(DEFAULT_NUM, DEFAULT_NUM);
        methodVisitor.visitEnd();
    }

    /**
     * Create static field
     *
     * @param classWriter
     * @param interfaces
     */
    private static void addStatic(ClassWriter classWriter, Class<?>[] interfaces) {
        for (Class<?> anInterface : interfaces) {
            for (int i = 0; i < anInterface.getMethods().length; i++) {
                String methodName = "_" + anInterface.getSimpleName() + "_" + i;
                classWriter.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, methodName, Type.getDescriptor(Method.class), null, null);
            }
        }
    }

    /**
     * Create<init> method
     * invoke construct method of superclass
     */
    private static void createInit(ClassWriter classWriter) {
        MethodVisitor methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/reflect/InvocationHandler;)V", null, null);
        methodVisitor.visitCode();
        //Put this into stack
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        //Put parameters into stack
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(ASMProxy.class), "<init>", "(Ljava/lang/reflect/InvocationHandler;)V", false);
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
    }

    private static String[] getInterfacesName(Class<?>[] interfaces) {
        return Arrays.stream(interfaces).map(Type::getInternalName).toArray(String[]::new);
    }
}
