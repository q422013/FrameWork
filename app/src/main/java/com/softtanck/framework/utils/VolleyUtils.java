package com.softtanck.framework.utils;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.softtanck.framework.ConValue;
import com.softtanck.framework.net.NetWorkRespListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : Tanck
 * @version : 1.0
 * @Description :
 * @date : 2015/1/22 10:41
 * @name : lb-enduser-android
 */
public class VolleyUtils {


    /**
     * 请求队列
     */
    private RequestQueue requestQueue;
    private Context context;
    /**
     * 参数标志
     */
    private String mParamsTag;
    /**
     * 超时
     */
    private int TIMEOUT_POST = 60000;

    private VolleyUtils() {
    }


    /**
     * 初始化配置
     *
     * @param requestQueue 网络请求队列
     * @param context      context
     * @return
     */
    public static VolleyUtils init(RequestQueue requestQueue,
                                   Context context) {
        VolleyUtils v = new VolleyUtils();
        v.requestQueue = requestQueue;
        v.context = context;
        return v;
    }

    /**
     * @param url      服务器地址
     * @param params   Post参数
     * @param listener 回调监听
     * @param <T>
     */
    public <T> void post(String url, final Map<String, Object> params, final NetWorkRespListener<String> listener) {

        if (null == listener)
            return;
        // TODO 不管有没有网络都直接去掉上一次的请求
        cancelLastRequest(url);

        if (NetworkUtils.isDisconnected(context)) {
            listener.onErrorResponse("当前没有网络...");
            return;
        }

        mParamsTag = null;
        if (null != params.get(ConValue.FUNCTION_TYPE_TAG)) {
            mParamsTag = params.get(ConValue.FUNCTION_TYPE_TAG).toString();
            params.remove(ConValue.FUNCTION_TYPE_TAG);
        }


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (null != mParamsTag) {
                    listener.onSuccessResponse(jsonObject.toString(), mParamsTag);
                } else {
                    listener.onSuccessResponse(jsonObject.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtils.d(error.getMessage());
                if (null != error.networkResponse) {
                    byte[] htmlBodyBytes = error.networkResponse.data;
                    LogUtils.d(new String(htmlBodyBytes));
                }
                listener.onErrorResponse(error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = new HashMap<String, String>();

                headers.put("Accept", "application/json");

                headers.put("Content-Type", "application/json; charset=UTF-8");

                return headers;
            }
        };
        request.setRetryPolicy(getRetryPolicy());
        request.setTag(url);
        requestQueue.add(request);
    }


    /**
     * 重试策略
     */

    private RetryPolicy getRetryPolicy() {
        RetryPolicy retryPolicy = new DefaultRetryPolicy(TIMEOUT_POST,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        return retryPolicy;
    }

    /**
     * 取消上一次的网络请求[<b>注意:取消的相同的</b>]
     *
     * @param Tag
     */
    public void cancelLastRequest(String Tag) {
        // return request.getTag() == tag;
        requestQueue.cancelAll(Tag);
    }


}
