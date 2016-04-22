package com.example.how_old;

import java.io.ByteArrayOutputStream;

import org.apache.http.HttpRequest;
import org.json.JSONObject;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import android.graphics.Bitmap;
import android.util.Log;

public class FaceppDetect {
	public interface Callback{
		void success(JSONObject result);
		
		void error(FaceppParseException exception);
	}
	public static void detect(final Bitmap bm,final Callback callBack){
		new Thread(new Runnable() {			
			@Override
			public void run() {
				
				try {
					HttpRequests requests = new HttpRequests(Const.key,Const.keysecret,true,true);
					Bitmap bmSmall = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight());
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					bmSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);
					byte[] array = stream.toByteArray();
					
					PostParameters params = new PostParameters();
					params.setImg(array);
					JSONObject jsonObject = requests.detectionDetect(params);
					//Log.e("TAG",jsonObject.toString());
					//Log.e("TAG","++++++++++++++++++++++++++++++++++");
					if(callBack!= null){
						callBack.success(jsonObject);
					}
				} catch (FaceppParseException e) {
					e.printStackTrace();
					if(callBack!= null){
						callBack.error(e);
					}
				}				
			}
		}).start();
	}
}
