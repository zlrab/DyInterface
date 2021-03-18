package com.zlrab;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author ZLRab
 * @date 2021/03/18
 */
public class DyInterfaceAttach {
    /**
     * @param attacher    Interface forwarding object , The program's call to the interface will be forwarded here
     * @param dyInterface The name of the interface class that needs to be forwarded example : com.zlrab.demo
     * @return Interface instance object being proxied
     * @throws ClassNotFoundException class not found
     */
    public static Object attach(Object attacher, String dyInterface) throws ClassNotFoundException {
        return attach(attacher, dyInterface, attacher.getClass().getClassLoader());
    }

    /**
     * @param attacher               Interface forwarding object , The program's call to the interface will be forwarded here
     * @param dyInterface            The name of the interface class that needs to be forwarded example : com.zlrab.demo
     * @param dyInterfaceClassLoader The classloader where the proxy interface is located
     * @return Interface instance object being proxied
     * @throws ClassNotFoundException class not found
     */
    public static Object attach(Object attacher, String dyInterface, ClassLoader dyInterfaceClassLoader) throws ClassNotFoundException {
        return attach(attacher, dyInterface, true, dyInterfaceClassLoader);
    }

    /**
     * @param attacher               Interface forwarding object , The program's call to the interface will be forwarded here
     * @param dyInterface            The name of the interface class that needs to be forwarded example : com.zlrab.demo
     * @param initDyInterfaceClass   Specify whether to initialize the proxy interface
     * @param dyInterfaceClassLoader The classloader where the proxy interface is located
     * @return Interface instance object being proxied
     * @throws ClassNotFoundException class not found
     */
    public static Object attach(Object attacher, String dyInterface, boolean initDyInterfaceClass, ClassLoader dyInterfaceClassLoader) throws ClassNotFoundException {
        if (attacher == null) return null;

        Class<?> dyInterfaceClass = Class.forName(dyInterface, initDyInterfaceClass, dyInterfaceClassLoader);

        if (!dyInterfaceClass.isInterface()) return null;

        List<AttacherMethod> attacherMethods = parsingAttacherMethods(attacher.getClass());

        if (attacherMethods.size() == 0) return null;

        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                AttacherMethod attacherMethod = new AttacherMethod(method, method.getName());
                if (attacherMethods.contains(attacherMethod)) {
                    AttacherMethod attacherMe = attacherMethods.get(attacherMethods.indexOf(attacherMethod));
                    if (attacherMe != null) {
                        attacherMe.method.invoke(attacher, args);
                    }
                }
                return null;
            }
        };
        return Proxy.newProxyInstance(dyInterfaceClassLoader, new Class[]{dyInterfaceClass}, invocationHandler);
    }

    private static List<AttacherMethod> parsingAttacherMethods(Class<?> attacherClass) {
        Method[] methods = attacherClass.getMethods();
        final List<AttacherMethod> attacherMethodList = new ArrayList<>();
        for (Method method : methods) {
            attacherMethodList.add(new AttacherMethod(method));
        }
        return attacherMethodList;
    }

    private static class AttacherMethod {
        private Method method;
        private String attachMethodName;
        private String[] attachMethodParamType;

        public AttacherMethod(Method method) {
            this.method = method;
            AttachMethodName annotation = method.getAnnotation(AttachMethodName.class);
            attachMethodName = annotation == null ? method.getName() : annotation.value();

            AttachMethodParams attachMethodParams = method.getAnnotation(AttachMethodParams.class);
            AttachMethodReflectParams attachMethodReflectParams = method.getAnnotation(AttachMethodReflectParams.class);
            //TODO Check the return type
            if (attachMethodParams == null) {
                if (attachMethodReflectParams == null) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    classTypeToStrings(parameterTypes);
                } else {
                    attachMethodParamType = attachMethodReflectParams.reflectParams();
                }
            } else {
                Class<?>[] params = attachMethodParams.params();
                classTypeToStrings(params);
            }
        }

        public AttacherMethod(Method method, String attachMethodName) {
            this.method = method;
            this.attachMethodName = attachMethodName;
            classTypeToStrings(method.getParameterTypes());
        }

        public Method getMethod() {
            return method;
        }

        public String getAttachMethodName() {
            return attachMethodName;
        }

        public String[] getAttachMethodParamType() {
            return attachMethodParamType;
        }

        private void classTypeToStrings(Class<?>[] paramsType) {
            attachMethodParamType = new String[paramsType.length];
            for (int index = 0; index < paramsType.length; index++) {
                attachMethodParamType[index] = paramsType[index].getName();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AttacherMethod that = (AttacherMethod) o;
            return attachMethodName.equals(that.attachMethodName) &&
                    Arrays.equals(attachMethodParamType, that.attachMethodParamType);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(attachMethodName);
            result = 31 * result + Arrays.hashCode(attachMethodParamType);
            return result;
        }
    }
}
