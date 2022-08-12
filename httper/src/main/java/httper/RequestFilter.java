package httper;

import java.util.Map;

/**
 * 用于接口签名生成
 */
public interface RequestFilter {

    /**
     * 过滤操作
     *
     * @param url    url
     * @param params 接口参数，可以直接修改
     */
    void filter(String url, Map<String, String> params);
}
