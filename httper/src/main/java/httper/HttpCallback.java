package httper;

public interface HttpCallback<T> {

    void onResult(HttpResponse<T> httpResponse);

}
