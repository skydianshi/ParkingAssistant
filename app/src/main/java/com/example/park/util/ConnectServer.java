package com.example.park.util;

import android.os.AsyncTask;

import com.example.park.listeners.HandleResponse;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by 张海逢 on 2017/5/9.
 */

public class ConnectServer extends AsyncTask<String, Void, String> {
    List<BasicNameValuePair> paramsList = new ArrayList<BasicNameValuePair>();
    private String URL;
    private HandleResponse handleResponse;
    private String parameter;

    //doPost构造函数
    public ConnectServer(List<BasicNameValuePair> paramsList, String url, HandleResponse handleResponse){
        this.paramsList = paramsList;
        this.URL = url;
        this.handleResponse = handleResponse;
    }
    //doGet构造函数
    public ConnectServer(String parameter, String url){
        this.parameter = parameter;
        this.URL = url;
    }

    //params代表可以填写任意多参数，下面取出来即可，对应excute方法的参数，比如下面的doPost需要5个参数，这边就可以放5个参数，下面再添加一个String five = params【4】即可
    @Override
    protected String doInBackground(String... params) {      // TODO Auto-generated method stub
        //通过post方式通信，如果通过get，则doGet即可
        return doPost();
    }

    @Override
    protected void onPostExecute(String result) {
        handleResponse.getResponse(result);
        super.onPostExecute(result);
    }


    //用get方式和服务器端通信
    private String doGet(String parameter){
        String responseStr = "";
        try {
            String getUrl = URL + "?PARAMETER=" + parameter;

            HttpGet httpRequest = new HttpGet(getUrl);
            HttpParams params = new BasicHttpParams();
            ConnManagerParams.setTimeout(params, 1000);
            HttpConnectionParams.setConnectionTimeout(params, 3000);
            HttpConnectionParams.setSoTimeout(params, 5000);
            httpRequest.setParams(params);

            HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
            final int ret = httpResponse.getStatusLine().getStatusCode();
            if(ret == HttpStatus.SC_OK){
                responseStr = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
            }else{
                responseStr = "-1";
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return responseStr;
    }

    /**
     * 用Post方式跟服务器传递数据
     * @param url
     * @return
     */
    private String doPost(){
        String responseStr = "";
        try {
            HttpPost httpRequest = new HttpPost(URL);
            //应该用requestConfig来代替HttpParams，下面的这个方法过时了，但要引入最新的包应该
            HttpParams params = new BasicHttpParams();
            ConnManagerParams.setTimeout(params, 10000); //从连接池中获取连接的超时时间
            HttpConnectionParams.setConnectionTimeout(params, 10000);//通过网络与服务器建立连接的超时时间
            HttpConnectionParams.setSoTimeout(params, 10000);//读响应数据的超时时间
            httpRequest.setParams(params);
            UrlEncodedFormEntity mUrlEncodeFormEntity = new UrlEncodedFormEntity(paramsList, HTTP.UTF_8);
            httpRequest.setEntity(mUrlEncodeFormEntity);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            final int ret = httpResponse.getStatusLine().getStatusCode();
            if(ret == HttpStatus.SC_OK){
                responseStr = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
            }else{
                responseStr = "-1";
            }

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return responseStr;
    }

}