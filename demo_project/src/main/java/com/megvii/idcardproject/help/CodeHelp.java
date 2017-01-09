package com.megvii.idcardproject.help;

import java.io.File;
import java.io.FileNotFoundException;
import org.apache.http.Header;
import org.json.JSONObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * 代码参考
 * 
 * 这里写了一些代码帮助（仅供参考）
 */
public class CodeHelp {

	/**
	 * ocridcard接口调用
	 */
	public void imageOCR() {
		RequestParams rParams = new RequestParams();
		rParams.put("api_key", "API_KEY");
		rParams.put("api_secret", "API_SECRET");
		try {
			rParams.put("image", new File("imagePath"));//身份证照片图片地址
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		rParams.put("legality", 1 + "");//传入1可以判断身份证是否  被编辑/是真实身份证/是复印件/是屏幕翻拍/是临时身份证
		AsyncHttpClient asyncHttpclient = new AsyncHttpClient();
		String url = "https://api.faceid.com/faceid/v1/ocridcard";
		asyncHttpclient.post(url, rParams, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseByte) {
				String successStr = new String(responseByte);
				try {
					JSONObject jObject = new JSONObject(successStr);
					if ("back".equals(jObject.getString("side"))) {
						// 身份证背后信息
					} else {
						// 身份证正面信息
					}
				} catch (Exception e) {
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				// 上传失败
			}
		});
	}
}