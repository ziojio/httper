package httper;

import okhttp3.Response;

/**
 * 接口结果解析器
 */
public interface ResponseParser<T> {

    /**
     * 结果解析
     *
     * @param response 请求的响应
     * @return T 解析的结果对象，交给Callback处理
     */
    T parse(Response response);

}

