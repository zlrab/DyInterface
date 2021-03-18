package com.zlrab;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException {
        Object attach = DyInterfaceAttach.attach(new CallBackAttacher() {
            @AttachMethodName("success")
            @AttachMethodReflectParams(reflectParams = {"java.lang.String"})
            @Override
            public void successa(String msg) {
                System.out.println("msg = " + msg);
            }

            @Override
            public void failed(String msg) {
                System.out.println("msg = " + msg);
            }
        }, CallBack.class.getName());
        Reflect.on(Main.class).call("test", attach);
    }

    public static void test(CallBack callBack) {
        callBack.success("success");
        callBack.failed("failed");
    }

    public interface CallBackAttacher {
        void successa(String msg);

        void failed(String msg);
    }

    public interface CallBack {
        void success(String msg);

        void failed(String msg);
    }
}
