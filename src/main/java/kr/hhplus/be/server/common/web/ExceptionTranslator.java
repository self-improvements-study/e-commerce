package kr.hhplus.be.server.common.web;

public interface ExceptionTranslator<T extends Exception> {

    T translate(Throwable t);

}
