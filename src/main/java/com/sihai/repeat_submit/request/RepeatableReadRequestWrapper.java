package com.sihai.repeat_submit.request;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * 可重复读取的请求
 */

/**
 * 装饰者模式
 */
public class RepeatableReadRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] bytes;

    public RepeatableReadRequestWrapper(HttpServletRequest request, HttpServletResponse response) throws IOException {
        super(request);
        // 设置字符编码
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        // 请求获取一行bytes
        bytes = request.getReader().readLine().getBytes();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        // 从输入流中读取数据， 返回到BufferedReader
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public int available() throws IOException {
                 return bytes.length; // 返回流的长度
            }
        };
    }
}
