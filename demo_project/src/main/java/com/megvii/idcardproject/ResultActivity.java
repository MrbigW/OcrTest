package com.megvii.idcardproject;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.megvii.awesomedemo.idcard.R;
import com.megvii.idcardlib.util.ConUtil;
import com.megvii.idcardlib.util.Util;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by binghezhouke on 15-8-12.
 */
public class ResultActivity extends Activity {
	private Bitmap bitmap;
	private ProgressBar mBar;
	private TextView contentText, debugText;
	private float clear;
	float is_idcard;
	float in_bound;
	private boolean isTextDectde;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_resutl);

		init();
	}

	void init() {
		isTextDectde = getIntent().getBooleanExtra("isTextDectde", true);
		clear = getIntent().getFloatExtra("clear", 0.9f);
		is_idcard = getIntent().getFloatExtra("is_idcard", 0.9f);
		in_bound = getIntent().getFloatExtra("in_bound", 0.9f);
		String path = getIntent().getStringExtra("path");
		bitmap = BitmapFactory.decodeFile(path);

		mBar = (ProgressBar) findViewById(R.id.result_bar);
		contentText = (TextView) findViewById(R.id.result_idcard_contentText);
		debugText = (TextView) findViewById(R.id.result_idcard_debugText);
		DecimalFormat fnum = new DecimalFormat("##0.000");
		debugText.setText("clear: " + fnum.format(clear) + "\nis_idcard: " + fnum.format(is_idcard) + "\nin_bound: "
				+ fnum.format(in_bound));
		ImageView image = (ImageView) findViewById(R.id.result_myimage);
		image.setImageBitmap(bitmap);

		findViewById(R.id.result_returnBtn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ResultActivity.this.finish();
			}
		});

		if (isTextDectde) {
			doOCR(path);
		}
	}

	/**
	 * 对身份证照片做ocr，然后发现是正面照片，那么利用 face/extract 接口进行人脸检测，如果是背面，直接弹出对话框
	 */
	public void doOCR(final String path) {
		mBar.setVisibility(View.VISIBLE);
		try {
			String url = "https://api.megvii.com/cardpp/v1/ocridcard";
			RequestParams rParams = new RequestParams();
			Log.w("ceshi", "Util.API_KEY===" + Util.API_KEY + ", Util.API_SECRET===" + Util.API_SECRET);
			rParams.put("api_key", Util.API_KEY);
			rParams.put("api_secret", Util.API_SECRET);
			rParams.put("image_file", new File(path));
			rParams.put("legality", 1 + "");
			AsyncHttpClient asyncHttpclient = new AsyncHttpClient();
			asyncHttpclient.post(url, rParams, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseByte) {
					mBar.setVisibility(View.GONE);
					String successStr = new String(responseByte);
					try {
						String info = "";
						JSONObject jsonObject = new JSONObject(successStr);
						JSONArray jsonArray = jsonObject.getJSONArray("cards");
						if (jsonArray.length() == 0) {
							ConUtil.showToast(ResultActivity.this, "没有检测到卡，请重新识别！");
							return;
						}
						JSONObject jObject = jsonArray.getJSONObject(0);

						if ("back".equals(jObject.getString("side"))) {
							String officeAdress = jObject.getString("issued_by");
							String useful_life = jObject.getString("valid_date");
							info = info + "签发机关:  " + officeAdress + "\n有效期限:  " + useful_life;
						} else {
							String address = jObject.getString("address");
							String birthday = jObject.getString("birthday");
							String gender = jObject.getString("gender");
							String id_card_number = jObject.getString("id_card_number");
							String name = jObject.getString("name");
							Log.w("ceshi", "doOCR+++idCardBean.id_card_number===" + id_card_number
									+ ", idCardBean.name===" + name);
							String race = jObject.getString("race");
							String side = jObject.getString("side");

							info = info + "姓　　名:  " + name + "\n身份证号:  " + id_card_number + "\n民　　族:  " + gender
									+ "\n性　　别:  " + race + "\n出　　生:  " + birthday + "\n地　　址:  " + address;
						}
						contentText.setText(contentText.getText().toString() + info);
					} catch (Exception e) {
						e.printStackTrace();
						mBar.setVisibility(View.GONE);
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
					if (responseBody != null) {
						Log.w("ceshi", "responseBody===" + new String(responseBody));
					}
					mBar.setVisibility(View.GONE);
					ConUtil.showToast(ResultActivity.this, "识别失败，请重新识别！");
				}
			});
		} catch (FileNotFoundException e1) {
			mBar.setVisibility(View.GONE);
			e1.printStackTrace();
			ConUtil.showToast(ResultActivity.this, "识别失败，请重新识别！");
		}
	}
}