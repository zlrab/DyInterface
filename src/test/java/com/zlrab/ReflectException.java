package com.zlrab;

/**
 * @author Lody
 * @link https://github.com/asLody/VirtualApp
 */
public class ReflectException extends RuntimeException {

    public ReflectException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReflectException(Throwable cause) {
        super(cause);
    }
}
