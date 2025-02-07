package rxhttp.wrapper.param;


import java.io.IOException;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.Headers.Builder;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import rxhttp.wrapper.callback.IConverter;
import rxhttp.wrapper.utils.BuildUtil;

/**
 * 此类是唯一直接实现Param接口的类
 * User: ljx
 * Date: 2019/1/19
 * Time: 14:35
 */
@SuppressWarnings("unchecked")
public abstract class AbstractParam<P extends Param> implements Param<P> {

    private String mUrl;    //链接地址
    private Method mMethod;  //请求方法
    private Builder mHBuilder; //请求头构造器

    private Request.Builder requestBuilder = new Request.Builder(); //请求构造器

    private boolean mIsAssemblyEnabled = true;//是否添加公共参数

    /**
     * @param url    请求路径
     * @param method {@link Method#GET,Method#HEAD,Method#POST,Method#PUT,Method#DELETE,Method#PATCH}
     */
    public AbstractParam(@NonNull String url, Method method) {
        this.mUrl = url;
        this.mMethod = method;
    }

    public P setUrl(@NonNull String url) {
        mUrl = url;
        return (P) this;
    }

    @Override
    public HttpUrl getHttpUrl() {
        return HttpUrl.get(mUrl);
    }

    /**
     * @return 不带参数的url
     */
    @Override
    public final String getSimpleUrl() {
        return mUrl;
    }

    @Override
    public Method getMethod() {
        return mMethod;
    }

    @Nullable
    @Override
    public final Headers getHeaders() {
        return mHBuilder == null ? null : mHBuilder.build();
    }

    @Override
    public final Builder getHeadersBuilder() {
        if (mHBuilder == null)
            mHBuilder = new Builder();
        return mHBuilder;
    }

    @Override
    public P setHeadersBuilder(Builder builder) {
        mHBuilder = builder;
        return (P) this;
    }

    @Override
    public final P addHeader(String key, String value) {
        getHeadersBuilder().add(key, value);
        return (P) this;
    }

    @Override
    public final P addHeader(String line) {
        getHeadersBuilder().add(line);
        return (P) this;
    }

    @Override
    public final P setHeader(String key, String value) {
        getHeadersBuilder().set(key, value);
        return (P) this;
    }

    @Override
    public final String getHeader(String key) {
        return getHeadersBuilder().get(key);
    }

    @Override
    public final P removeAllHeader(String key) {
        getHeadersBuilder().removeAll(key);
        return (P) this;
    }

    @Override
    public P cacheControl(CacheControl cacheControl) {
        requestBuilder.cacheControl(cacheControl);
        return (P) this;
    }

    @Override
    public <T> P tag(Class<? super T> type, T tag) {
        requestBuilder.tag(type, tag);
        return (P) this;
    }

    @Override
    public final P setAssemblyEnabled(boolean enabled) {
        mIsAssemblyEnabled = enabled;
        return (P) this;
    }

    @Override
    public final boolean isAssemblyEnabled() {
        return mIsAssemblyEnabled;
    }

    public Request.Builder getRequestBuilder() {
        return requestBuilder;
    }

    @Override
    public Request buildRequest() {
        return BuildUtil.buildRequest(this, requestBuilder);
    }

    protected IConverter getConverter() {
        Request request = getRequestBuilder().build();
        return request.tag(IConverter.class);
    }

    protected final RequestBody convert(Object object) {
        IConverter converter = getConverter();
        if (converter == null)
            throw new NullPointerException("converter can not be null");
        try {
            return converter.convert(object);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to convert " + object + " to RequestBody", e);
        }
    }
}
