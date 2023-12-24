package io.kenji.courier.reflect.asm.visitor;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-24
 **/
@Slf4j
public class TargetClassVisitor extends ClassVisitor {

    private boolean isFinal;
    private List<MethodBean> methods = new ArrayList<>();

    private List<MethodBean> declaredMethods = new ArrayList<>();

    private List<MethodBean> constructors = new ArrayList<>();

    public TargetClassVisitor() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        if ((access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL) {
            isFinal = true;
        }
        if (superName != null) {
            List<MethodBean> beans = initMethodBeanByParent(superName);
            if (beans != null && !beans.isEmpty()) {
                for (MethodBean bean : beans) {
                    if (!methods.contains(bean)) {
                        methods.add(bean);
                    }
                }
            }
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (name.equals("<init>")) {
            MethodBean constructor = new MethodBean(access, name, descriptor);
            constructors.add(constructor);
        } else if (!name.equals("<clinit>")) {
            if ((access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL
                    || (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
            MethodBean methodBean = new MethodBean(access, name, descriptor);
            declaredMethods.add(methodBean);
            if ((access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) {
                methods.add(methodBean);
            }
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    private List<MethodBean> initMethodBeanByParent(String superName) {
        try {
            if (superName != null && !superName.isEmpty()) {
                ClassReader classReader = new ClassReader(superName);
                TargetClassVisitor visitor = new TargetClassVisitor();
                classReader.accept(visitor, ClassReader.SKIP_DEBUG);
                List<MethodBean> methodBeans = new ArrayList<>();
                for (MethodBean methodBean : visitor.methods) {
                    //skip final and static
                    if ((methodBean.access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL
                            || ((methodBean.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC)) {
                        continue;
                    }
                    // Only use public
                    if ((methodBean.access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) {
                        methodBeans.add(methodBean);
                    }
                }
                return methodBeans;
            }
        } catch (IOException e) {
            log.error("Hit exception during initializing method bean by parent", e);
        }
        return null;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public List<MethodBean> getConstructors() {
        return constructors;
    }

    public List<MethodBean> getMethods() {
        return methods;
    }

    public List<MethodBean> getDeclaredMethods() {
        return declaredMethods;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    public class MethodBean {
        public int access;

        public String methodName;

        public String methodDesc;

//        public MethodBean(int access, String methodName, String methodDesc) {
//            this.access = access;
//            this.methodName = methodName;
//            this.methodDesc = methodDesc;
//        }

        public MethodBean() {
        }
    }
}
