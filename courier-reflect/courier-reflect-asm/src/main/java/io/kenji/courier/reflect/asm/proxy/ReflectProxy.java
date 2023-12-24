package io.kenji.courier.reflect.asm.proxy;

import io.kenji.courier.reflect.asm.visitor.TargetClassVisitor;
import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-24
 **/
@Slf4j
public class ReflectProxy {

    private static final String METHOD_SETTER = "setInvocationHandler";

    private static final String METHOD_INVOKE = "invokeInvocationHandler";

    private static final String METHOD_INVOKE_DESC = "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;";

    public static final String PROXY_CLASSNAME_PREFIX = "$Proxy_";

    private static final String FIELD_INVOCATION_HANDLER = "invocationHandler";

    private static final String METHOD_FIELD_PREFIX = "method";

    private static final Map<String, Class<?>> proxyClassCache = new ConcurrentHashMap<>();

    /**
     * Create proxy instance which is extended targetClass
     *
     * @param classLoader
     * @param invocationHandler
     * @param targetClass
     * @param targetConstructor
     * @param targetParam
     * @return
     */
    public static Object newProxyInstance(ClassLoader classLoader,
                                          InvocationHandler invocationHandler,
                                          Class<?> targetClass,
                                          Constructor<?> targetConstructor,
                                          Object... targetParam) {
        if (classLoader == null || targetClass == null || invocationHandler == null) {
            throw new IllegalArgumentException("argument is null, classLoader = " + classLoader + ", targetClass = " + targetClass + ", invocationHandler = " + invocationHandler);
        }
        try {
            // Check cache
            Class<?> proxyClass = getProxyClassFromCache(classLoader, targetClass);
            if (proxyClass != null) {
                // create proxy instance
                return newInstance(proxyClass, invocationHandler, targetConstructor, targetParam);
            }
            //Get data for target class
            ClassReader classReader = new ClassReader(targetClass.getName());
            TargetClassVisitor targetClassVisitor = new TargetClassVisitor();
            classReader.accept(targetClassVisitor, ClassReader.SKIP_DEBUG);
            //Judge whether it is Final
            if (targetClassVisitor.isFinal()) {
                throw new IllegalArgumentException("target class is final");
            }
            //Start to create proxy instance
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            String newClassName = generateProxyClassName(targetClass);
            String newClassInternalName = newClassName.replace(".", "/");
            String targetClassName = targetClass.getName();
            String targetClassInternalName = Type.getInternalName(targetClass);
            //Create class
            newClass(classWriter, newClassInternalName, targetClassInternalName);
            //Add InvocationHandler field
            addField(classWriter);
            //Add InvocationHandler setter
            addSetterMethod(classWriter, newClassInternalName);
            //Add constructor, invoke super directly in constructor
            List<TargetClassVisitor.MethodBean> constructors = targetClassVisitor.getConstructors();
            addConstructor(classWriter, constructors, targetClassInternalName);
            //Add invoke InvocationHandler method
            addInvokeMethod(classWriter, newClassInternalName);
            //Add inherited public / protected / default method
            List<TargetClassVisitor.MethodBean> methods = targetClassVisitor.getMethods();
            List<TargetClassVisitor.MethodBean> declaredMethods = targetClassVisitor.getDeclaredMethods();
            Map<Integer, Integer> methodsMap = new HashMap<>();
            Map<Integer, Integer> declaredMethodsMap = new HashMap<>();
            int methodNameIndex = 0;
            methodNameIndex = addMethod(classWriter, newClassInternalName, targetClass.getMethods(), methods, true, methodNameIndex, methodsMap);
            addMethod(classWriter, newClassInternalName, targetClass.getDeclaredMethods(), declaredMethods, false, methodNameIndex, declaredMethodsMap);
            //Add static init block
            addStaticInitBlock(classWriter, targetClassName, newClassInternalName, methodsMap, declaredMethodsMap);
            //To byte
            byte[] bytes = classWriter.toByteArray();
            //load Class from classLoader
            proxyClass = transferToClass(classLoader, bytes);
            //Cache
            saveProxyClassCache(classLoader, targetClass, proxyClass);
            return newInstance(proxyClass, invocationHandler, targetConstructor, targetParam);
        } catch (Exception e) {
            log.error("Hit exception whilst creating proxy instance", e);
        }
        return null;
    }

    private static void saveProxyClassCache(ClassLoader classLoader, Class<?> targetClass, Class<?> proxyClass) {
        String key = classLoader.getName() + "_" + targetClass.getName();
        proxyClassCache.put(key,proxyClass);
    }

    private static Class<?> transferToClass(ClassLoader classLoader, byte[] bytes) {
        try {
            Class<?> clazz = Class.forName("java.lang.ClassLoader");
            Method declaredMethod = clazz.getDeclaredMethod("defineClass", new Class[]{String.class, byte[].class, int.class, int.class});
            declaredMethod.setAccessible(true);
            return  (Class<?>) declaredMethod.invoke(classLoader, new Object[]{null, bytes, 0, bytes.length});
        } catch (Exception e) {
            log.error("Hit exception during class transformation",e);
        }
        return null;
    }
    /**
     * Add static init block
     */
    private static void addStaticInitBlock(ClassWriter classWriter, String targetClassName, String newClassInternalName, Map<Integer, Integer> methodsMap, Map<Integer, Integer> declaredMethodsMap) {
        String exceptionClassName = Type.getInternalName(ClassNotFoundException.class);
        MethodVisitor methodVisitor = classWriter.visitMethod(Opcodes.ACC_STATIC, "<clinit>",
                "()V", null, null);
        methodVisitor.visitCode();
        //Exception handle
        Label label0 = new Label();
        Label label1 = new Label();
        Label label2 = new Label();
        methodVisitor.visitTryCatchBlock(label0, label1, label2, exceptionClassName);
        methodVisitor.visitLabel(label0);
        //Initialize filed in the inherited method
        for (Map.Entry<Integer, Integer> entry : methodsMap.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            methodVisitor.visitLdcInsn(targetClassName);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Class.class),
                    "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false);
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Class.class),
                    "getMethods", "()[Ljava/lang/reflect/Method;", false);
            methodVisitor.visitIntInsn(Opcodes.BIPUSH, value);
            methodVisitor.visitInsn(Opcodes.AALOAD);
            methodVisitor.visitFieldInsn(Opcodes.PUTSTATIC, newClassInternalName,
                    METHOD_FIELD_PREFIX + key, Type.getDescriptor(Method.class));
        }
        //Initialize field in the target class
        for (Map.Entry<Integer, Integer> entry : declaredMethodsMap.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            methodVisitor.visitLdcInsn(targetClassName);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Class.class),
                    "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false);
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Class.class),
                    "getDeclaredMethods", "()[Ljava/lang/reflect/Method;", false);
            methodVisitor.visitIntInsn(Opcodes.BIPUSH, value);
            methodVisitor.visitInsn(Opcodes.AALOAD);
            methodVisitor.visitFieldInsn(Opcodes.PUTSTATIC, newClassInternalName,
                    METHOD_FIELD_PREFIX + key, Type.getDescriptor(Method.class));
        }
        methodVisitor.visitLabel(label1);
        Label label3 = new Label();
        methodVisitor.visitJumpInsn(Opcodes.GOTO, label3);
        methodVisitor.visitLabel(label2);
        methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1,
                new Object[]{exceptionClassName});
        methodVisitor.visitVarInsn(Opcodes.ASTORE, 0);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, exceptionClassName,
                "printStackTrace", "()V", false);
        methodVisitor.visitLabel(label3);
        methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitMaxs(2, 1);
        methodVisitor.visitEnd();

    }

    /**
     * Add inherited method or target class itself method
     *
     * @param classWriter
     * @param newClassInternalName
     * @param methods
     * @param methodsBeans
     * @param isPublic
     * @param methodNameIndex
     * @param methodsMap
     * @return
     */
    private static int addMethod(ClassWriter classWriter, String newClassInternalName, Method[] methods, List<TargetClassVisitor.MethodBean> methodsBeans, boolean isPublic, int methodNameIndex, Map<Integer, Integer> methodsMap) {
        for (TargetClassVisitor.MethodBean methodsBean : methodsBeans) {
            //Skip final and static method
            if ((methodsBean.access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL
                    || (methodsBean.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STRICT) {
                continue;
            }
            //Cater specific modifier
            int access = -1;
            if (isPublic) {
                //public method
                if ((methodsBean.access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) {
                    access = Opcodes.ACC_PUBLIC;
                }
            } else {
                //protected method
                if ((methodsBean.access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED) {
                    access = Opcodes.ACC_PROTECTED;
                } else if ((methodsBean.access & Opcodes.ACC_PUBLIC) == 0 &&
                        (methodsBean.access & Opcodes.ACC_PROTECTED) == 0 &&
                        (methodsBean.access & Opcodes.ACC_PRIVATE) == 0) {
                    access = 0;
                }
            }
            if (access == -1) {
                continue;
            }
            // Match corresponding method
            int methodIndex = findSomeMethod(methods, methodsBean);
            if (methodIndex == -1) {
                continue;
            }
            methodsMap.put(methodNameIndex, methodIndex);
            //Add corresponding filed to method
            String fieldName = METHOD_FIELD_PREFIX + methodNameIndex;
            FieldVisitor fieldVisitor = classWriter.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                    fieldName, Type.getDescriptor(Method.class), null, null);
            fieldVisitor.visitEnd();
            // Add method invoke
            addMethod(classWriter, newClassInternalName, methodsBean, access, methodNameIndex);
            methodNameIndex++;
        }
        return methodNameIndex;
    }

    private static void addMethod(ClassWriter classWriter, String newClassInternalName, TargetClassVisitor.MethodBean methodsBean, int access, int methodNameIndex) {
        MethodVisitor methodVisitor = classWriter.visitMethod(access, methodsBean.methodName, methodsBean.methodDesc, null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        //Identify invoked method is static or non-static
        if ((methodsBean.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
            methodVisitor.visitInsn(Opcodes.ACONST_NULL);
        } else {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        }
        //Get filed from created method
        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, newClassInternalName,
                METHOD_FIELD_PREFIX + methodNameIndex, Type.getDescriptor(Method.class));
        Type[] argumentTypes = Type.getArgumentTypes(methodsBean.methodDesc);
        // Instantiated array, capacity is parameter size of the corresponding method
        methodVisitor.visitIntInsn(Opcodes.BIPUSH, argumentTypes.length);
        methodVisitor.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(Object.class));
        //Compute local variable table location, double and long occupy two slot, others occupy one slot
        int start = 1;
        int stop = start;
        for (int i = 0; i < argumentTypes.length; i++) {
            Type type = argumentTypes[i];
            if (type.equals(Type.BYTE_TYPE)) {
                stop = start + 1;
                methodVisitor.visitInsn(Opcodes.DUP);
                //Put array index
                methodVisitor.visitIntInsn(Opcodes.BIPUSH, i);
                // local variable table index
                methodVisitor.visitVarInsn(Opcodes.ILOAD, start);
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Byte.class),
                        "valueOf", "(B)Ljava/lang/Byte;", false);
                methodVisitor.visitInsn(Opcodes.AASTORE);
            } else if (type.equals(Type.SHORT_TYPE)) {
                stop = start + 1;
                methodVisitor.visitInsn(Opcodes.DUP);
                //Put array index
                methodVisitor.visitIntInsn(Opcodes.BIPUSH, i);
                // local variable table index
                methodVisitor.visitVarInsn(Opcodes.ILOAD, start);
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Byte.class),
                        "valueOf", "(S)Ljava/lang/Short;", false);
                methodVisitor.visitInsn(Opcodes.AASTORE);
            } else if (type.equals(Type.CHAR_TYPE)) {
                stop = start + 1;
                methodVisitor.visitInsn(Opcodes.DUP);
                //Put array index
                methodVisitor.visitIntInsn(Opcodes.BIPUSH, i);
                // local variable table index
                methodVisitor.visitVarInsn(Opcodes.ILOAD, start);
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Byte.class),
                        "valueOf", "(C)Ljava/lang/Character;", false);
                methodVisitor.visitInsn(Opcodes.AASTORE);
            } else if (type.equals(Type.INT_TYPE)) {
                stop = start + 1;
                methodVisitor.visitInsn(Opcodes.DUP);
                //Put array index
                methodVisitor.visitIntInsn(Opcodes.BIPUSH, i);
                // local variable table index
                methodVisitor.visitVarInsn(Opcodes.ILOAD, start);
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Byte.class),
                        "valueOf", "(I)Ljava/lang/Integer;", false);
                methodVisitor.visitInsn(Opcodes.AASTORE);
            } else if (type.equals(Type.FLOAT_TYPE)) {
                stop = start + 1;
                methodVisitor.visitInsn(Opcodes.DUP);
                //Put array index
                methodVisitor.visitIntInsn(Opcodes.BIPUSH, i);
                // local variable table index
                methodVisitor.visitVarInsn(Opcodes.FLOAD, start);
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Byte.class),
                        "valueOf", "(F)Ljava/lang/Float;", false);
                methodVisitor.visitInsn(Opcodes.AASTORE);
            } else if (type.equals(Type.DOUBLE_TYPE)) {
                stop = start + 2;
                methodVisitor.visitInsn(Opcodes.DUP);
                //Put array index
                methodVisitor.visitIntInsn(Opcodes.BIPUSH, i);
                // local variable table index
                methodVisitor.visitVarInsn(Opcodes.DLOAD, start);
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Byte.class),
                        "valueOf", "(D)Ljava/lang/Double;", false);
                methodVisitor.visitInsn(Opcodes.AASTORE);
            } else if (type.equals(Type.LONG_TYPE)) {
                stop = start + 2;
                methodVisitor.visitInsn(Opcodes.DUP);
                //Put array index
                methodVisitor.visitIntInsn(Opcodes.BIPUSH, i);
                // local variable table index
                methodVisitor.visitVarInsn(Opcodes.LLOAD, start);
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Byte.class),
                        "valueOf", "(J)Ljava/lang/Long;", false);
                methodVisitor.visitInsn(Opcodes.AASTORE);
            } else if (type.equals(Type.BOOLEAN_TYPE)) {
                stop = start + 1;
                methodVisitor.visitInsn(Opcodes.DUP);
                //Put array index
                methodVisitor.visitIntInsn(Opcodes.BIPUSH, i);
                // local variable table index
                methodVisitor.visitVarInsn(Opcodes.ILOAD, start);
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Byte.class),
                        "valueOf", "(Z)Ljava/lang/Boolean;", false);
                methodVisitor.visitInsn(Opcodes.AASTORE);
            } else {
                stop = start + 1;
                methodVisitor.visitInsn(Opcodes.DUP);
                //Put array index
                methodVisitor.visitIntInsn(Opcodes.BIPUSH, i);
                // local variable table index
                methodVisitor.visitVarInsn(Opcodes.ALOAD, start);
                methodVisitor.visitInsn(Opcodes.AASTORE);
            }
            start = stop;
        }
        //Invoke invokeInvocationHandler method
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, newClassInternalName,
                METHOD_INVOKE, METHOD_INVOKE_DESC, false);
        // Handle return case
        Type returnType = Type.getReturnType(methodsBean.methodDesc);
        if (returnType.equals(Type.BYTE_TYPE)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Byte.class));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Byte.class),
                    "byteValue", "()B", false);
            methodVisitor.visitInsn(Opcodes.IRETURN);
        } else if (returnType.equals(Type.BOOLEAN_TYPE)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Boolean.class));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Boolean.class),
                    "booleanValue", "()Z", false);
            methodVisitor.visitInsn(Opcodes.IRETURN);
        } else if (returnType.equals(Type.CHAR_TYPE)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Character.class));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Character.class),
                    "charValue", "()C", false);
            methodVisitor.visitInsn(Opcodes.IRETURN);
        } else if (returnType.equals(Type.SHORT_TYPE)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Short.class));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Short.class),
                    "shortValue", "()S", false);
            methodVisitor.visitInsn(Opcodes.IRETURN);
        } else if (returnType.equals(Type.INT_TYPE)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Integer.class));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Integer.class),
                    "intValue", "()I", false);
            methodVisitor.visitInsn(Opcodes.IRETURN);
        } else if (returnType.equals(Type.LONG_TYPE)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Long.class));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Long.class),
                    "longValue", "()J", false);
            methodVisitor.visitInsn(Opcodes.LRETURN);
        } else if (returnType.equals(Type.FLOAT_TYPE)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Float.class));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Float.class),
                    "floatValue", "()F", false);
            methodVisitor.visitInsn(Opcodes.FRETURN);
        } else if (returnType.equals(Type.DOUBLE_TYPE)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Double.class));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Double.class),
                    "doubleValue", "()D", false);
            methodVisitor.visitInsn(Opcodes.DRETURN);
        } else if (returnType.equals(Type.VOID_TYPE)) {
            methodVisitor.visitInsn(Opcodes.RETURN);
        } else {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, returnType.getInternalName());
            methodVisitor.visitInsn(Opcodes.ARETURN);
        }
        methodVisitor.visitMaxs(8, 37);
        methodVisitor.visitEnd();
    }

    /**
     * Find equal method index
     *
     * @param methods
     * @param methodsBean
     * @return
     */
    private static int findSomeMethod(Method[] methods, TargetClassVisitor.MethodBean methodsBean) {
        for (int i = 0; i < methods.length; i++) {
            if (equalMethod(methods[i], methodsBean)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Judge wether {@link Method} and {@link io.kenji.courier.reflect.asm.visitor.TargetClassVisitor.MethodBean} is equal
     *
     * @param method
     * @param methodBean
     * @return
     */
    private static boolean equalMethod(Method method, TargetClassVisitor.MethodBean methodBean) {
        if (method == null && methodBean == null) {
            return true;
        }
        if (method == null || methodBean == null) {
            return false;
        }
        try {
            if (!method.getName().equals(methodBean.methodName)) {
                return false;
            }
            if (!Type.getReturnType(method).equals(Type.getReturnType(methodBean.methodDesc))) {
                return false;
            }
            Type[] argumentTypes1 = Type.getArgumentTypes(method);
            Type[] argumentTypes2 = Type.getArgumentTypes(methodBean.methodDesc);
            if (argumentTypes1.length != argumentTypes2.length) return false;
            for (int i = 0; i < argumentTypes1.length; i++) {
                if (!argumentTypes1[i].equals(argumentTypes2[i])) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Hit exception in Method and MethodBean judgement", e);
        }
        return false;
    }

    /**
     * Add method to invoke the invoke method of InvocationHandler
     *
     * @param classWriter
     * @param owner
     */
    private static void addInvokeMethod(ClassWriter classWriter, String owner) {
        MethodVisitor methodVisitor = classWriter.visitMethod(Opcodes.ACC_PRIVATE | Opcodes.ACC_VARARGS, METHOD_INVOKE, METHOD_INVOKE_DESC, null, null);
        methodVisitor.visitCode();
        //Exception handler
        Label label0 = new Label();
        Label label1 = new Label();
        Label label2 = new Label();
        methodVisitor.visitTryCatchBlock(label0, label1, label2, Type.getInternalName(Throwable.class));
        methodVisitor.visitLabel(label0);
        //Put InvocationHandler field into stack
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitFieldInsn(Opcodes.GETFIELD, owner, FIELD_INVOCATION_HANDLER,
                Type.getDescriptor(InvocationHandler.class));
        //Put local variable table location of three parameters into stack
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 3);
        String handlerName = Type.getInternalName(InvocationHandler.class);
        String handlerMethodName = "invoke";
        String handlerDesc = "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;";
        //Invoke invocationHandler.invoke method
        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, handlerName, handlerMethodName, handlerDesc, true);
        //Return successfully
        methodVisitor.visitLabel(label1);
        methodVisitor.visitInsn(Opcodes.ARETURN);
        //Exception handle
        methodVisitor.visitLabel(label2);
        methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{Type.getInternalName(Throwable.class)});
        methodVisitor.visitVarInsn(Opcodes.ASTORE, 4);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 4);
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Throwable.class), "printStackTrace", "()V", false);
        methodVisitor.visitInsn(Opcodes.ACONST_NULL);
        methodVisitor.visitInsn(Opcodes.ARETURN);
        methodVisitor.visitMaxs(4, 5);
        methodVisitor.visitEnd();
    }

    private static void addConstructor(ClassWriter
                                               classWriter, List<TargetClassVisitor.MethodBean> constructors, String targetClassInternalName) {
        for (TargetClassVisitor.MethodBean constructor : constructors) {
            Type[] argumentTypes = Type.getArgumentTypes(constructor.methodDesc);
            MethodVisitor methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
                    constructor.methodDesc, null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            //put parameter into local variable table
            for (int i = 0; i < argumentTypes.length; i++) {

                Type argumentType = argumentTypes[i];
                if (argumentType.equals(Type.BYTE_TYPE)
                        || argumentType.equals(Type.BOOLEAN_TYPE)
                        || argumentType.equals(Type.CHAR_TYPE)
                        || argumentType.equals(Type.SHORT_TYPE)
                        || argumentType.equals(Type.INT_TYPE)) {
                    methodVisitor.visitVarInsn(Opcodes.ILOAD, i + 1);
                } else if (argumentType.equals(Type.LONG_TYPE)) {
                    methodVisitor.visitVarInsn(Opcodes.LLOAD, i + 1);
                } else if (argumentType.equals(Type.FLOAT_TYPE)) {
                    methodVisitor.visitVarInsn(Opcodes.FLOAD, i + 1);
                } else if (argumentType.equals(Type.DOUBLE_TYPE)) {
                    methodVisitor.visitVarInsn(Opcodes.DLOAD, i + 1);
                } else {
                    methodVisitor.visitVarInsn(Opcodes.ALOAD, i + 1);
                }
            }
            //Invoke super() constructor
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, targetClassInternalName, "<init>", constructor.methodDesc, false);
            methodVisitor.visitInsn(Opcodes.RETURN);
            methodVisitor.visitMaxs(argumentTypes.length + 1, argumentTypes.length + 1);
            methodVisitor.visitEnd();
        }
    }

    /**
     * Add setter method for InvocationHandler
     *
     * @param classWriter
     * @param owner
     */
    private static void addSetterMethod(ClassWriter classWriter, String owner) {
        String methodDesc = "(" + Type.getDescriptor(InvocationHandler.class) + ")V";
        MethodVisitor methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, METHOD_SETTER, methodDesc, null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, owner, FIELD_INVOCATION_HANDLER,
                Type.getDescriptor(InvocationHandler.class));
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
    }

    private static void addField(ClassWriter classWriter) {
        FieldVisitor fieldVisitor = classWriter.visitField(Opcodes.ACC_PRIVATE, FIELD_INVOCATION_HANDLER, Type.getDescriptor(InvocationHandler.class), null, null);
        fieldVisitor.visitEnd();
    }

    private static void newClass(ClassWriter classWriter, String newClassInternalName, String
            targetClassInternalName) {
        int access = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL;
        classWriter.visit(Opcodes.V17, access, newClassInternalName, null, targetClassInternalName, null);
    }

    private static String generateProxyClassName(Class<?> targetClass) {
        return targetClass.getPackage().getName() + "." + PROXY_CLASSNAME_PREFIX + targetClass.getSimpleName();
    }

    /**
     * Create proxy instance by target constructor. Invoke setter method during create proxy instance
     *
     * @param proxyClass
     * @param invocationHandler
     * @param targetConstructor
     * @param targetParam
     * @return
     * @throws Exception
     */
    private static Object newInstance(Class<?> proxyClass, InvocationHandler invocationHandler, Constructor<?>
            targetConstructor, Object[] targetParam) throws Exception {
        Class<?>[] parameterTypes = targetConstructor.getParameterTypes();
        Constructor<?> constructor = proxyClass.getConstructor(parameterTypes);
        Object instance = constructor.newInstance(targetParam);
        Method setterMethod = proxyClass.getDeclaredMethod(METHOD_SETTER, InvocationHandler.class);
        setterMethod.setAccessible(true);
        setterMethod.invoke(instance, invocationHandler);
        return instance;
    }

    private static Class<?> getProxyClassFromCache(ClassLoader classLoader, Class<?> targetClass) {
        String key = classLoader.getName() + "_" + targetClass.getName();
        return proxyClassCache.get(key);
    }
}
