package com.example.how_old;

import java.sql.PreparedStatement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facepp.error.FaceppParseException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends Activity implements OnClickListener {

	private static final int PICK_CODE = 0x110;
	private Button btn_get;
	private Button btn_detect;
	private TextView tv_tip;
	private ImageView mphoto;
	private View mwaiting;
	private String mCurrentPhotoStr;
	private Bitmap mPhotoImg;
	private Paint mpaint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        initEvent();
        mpaint = new Paint();
    }


    private void initEvent() {
    	btn_get.setOnClickListener(this);
    	btn_detect.setOnClickListener(this);
	}


	private void initViews() {
		
		btn_get = (Button)findViewById(R.id.btn_get);
		btn_detect = (Button)findViewById(R.id.btn_detect);
		tv_tip = (TextView)findViewById(R.id.tv_tip);
		mphoto = (ImageView)findViewById(R.id.photo);
		mwaiting = findViewById(R.id.waiting);
	}



    private static final int msg_success = 0x111;
    private static final int msg_error = 0x112;
    private Handler mhandler = new Handler(){
    	public void handleMessage(Message msg){
    		super.handleMessage(msg);
    		switch (msg.what) {
			case msg_success:
				mwaiting.setVisibility(View.GONE);
				JSONObject rs = (JSONObject) msg.obj;
				PrepareRsBitmap(rs);
				mphoto.setImageBitmap(mPhotoImg);
				break;
			case msg_error:
				mwaiting.setVisibility(View.GONE);
				String errormsg = (String) msg.obj;
				if(TextUtils.isEmpty(errormsg)){
					tv_tip.setText("error");
				}
				else{
					tv_tip.setText(errormsg);
				}
				break;
			default:
				break;
			}
    	}		
    };

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn_get: 
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");
			startActivityForResult(intent, PICK_CODE);
			break;
			
		case R.id.btn_detect:
			mwaiting.setVisibility(View.VISIBLE);
			
			if(mCurrentPhotoStr != null && !mCurrentPhotoStr.trim().equals("")){
				resizePhoto();
			}else{
				mPhotoImg = BitmapFactory.decodeResource(getResources(), R.drawable.test);
			}
			FaceppDetect.detect(mPhotoImg, new FaceppDetect.Callback() {
				
				@Override
				public void success(JSONObject result) {
					Message msg = Message.obtain();
					msg.what = msg_success;
					msg.obj = result;
					mhandler.sendMessage(msg);
				}
				
				@Override
				public void error(FaceppParseException exception) {
					// TODO Auto-generated method stub
					Message msg = Message.obtain();
					msg.what = msg_error;
					msg.obj = exception.getErrorMessage();
					mhandler.sendMessage(msg);
				}
			});
			break;
		}
		
	}


	protected void PrepareRsBitmap(JSONObject rs) {
				
		Bitmap bitmap = Bitmap.createBitmap(mPhotoImg.getWidth(), mPhotoImg.getHeight(),mPhotoImg.getConfig());
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(mPhotoImg,0,0, null);
		try {
			JSONArray faces = rs.getJSONArray("face");
			
			int facecount = faces.length();
			
			tv_tip.setText("face"+facecount);
			for(int i=0;i <= facecount;i++){
				JSONObject face = faces.getJSONObject(i);

				JSONObject posobj = face.getJSONObject("position");
				float x = (float) posobj.getJSONObject("center").getDouble("x");
				float y = (float) posobj.getJSONObject("center").getDouble("y");
				float w = (float) posobj.getDouble("width");
				float h = (float) posobj.getDouble("height");
				
				x = x /100 * bitmap.getWidth();
				y = y /100 * bitmap.getHeight();
				w = w /100 * bitmap.getWidth();
				h = h /100 * bitmap.getHeight();

				mpaint.setColor(Color.WHITE);
				mpaint.setStrokeWidth(3);
			
				canvas.drawLine(x-w/2, y-h/2, x-w/2, y+h/2, mpaint);
				canvas.drawLine(x-w/2, y-h/2, x+w/2, y-h/2, mpaint);
				canvas.drawLine(x+w/2, y-h/2, x+w/2, y+h/2, mpaint);
				canvas.drawLine(x-w/2, y+h/2, x+w/2, y+h/2, mpaint);
				int age = face.getJSONObject("attribute").getJSONObject("age").getInt("value");
				String gander = face.getJSONObject("attribute").getJSONObject("gender").getString("value");
				
				Bitmap ageBitmap = buildAgeBitmap(age,"Male".equals(gander));
				int ageWidth = ageBitmap.getWidth();
				int ageHeight = ageBitmap.getHeight();
				
				if(bitmap.getWidth() < mphoto.getWidth() &&bitmap.getHeight()< mphoto.getHeight()){
					float ratio = Math.max(bitmap.getWidth()*1.0f / mphoto.getWidth(),bitmap.getHeight()*1.0f / mphoto.getHeight());;
					ageBitmap = Bitmap.createScaledBitmap(ageBitmap,(int) (ageWidth*ratio),
							(int)(ageHeight * ratio), false);
				}
				canvas.drawBitmap(ageBitmap, x-ageBitmap.getWidth()/2,y-h/2 - ageBitmap.getHeight(),null ); 
				mPhotoImg = bitmap;	
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private Bitmap buildAgeBitmap(int age, boolean isMale) {
		TextView tv = (TextView) mwaiting.findViewById(R.id.id_age_and_gender);
		tv.setText(age+""+"  ");
		if(isMale){
			tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.male), null, null, null);
		}else{
			tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.female), null, null, null);
		}
		tv.setDrawingCacheEnabled(true);
		Bitmap bitmap = Bitmap.createBitmap(tv.getDrawingCache());
		tv.destroyDrawingCache();
		
		return bitmap;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if(requestCode == PICK_CODE){
			if(intent != null){
				Uri uri = intent.getData();
				Cursor cursor = getContentResolver().query(uri, null, null, null, null);
				cursor.moveToFirst();
				
				int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
				mCurrentPhotoStr = cursor.getString(idx);
				cursor.close();
				
				resizePhoto();
				mphoto.setImageBitmap(mPhotoImg);

			}
		}
	}


	private void resizePhoto() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoStr, options);
		double ratio = Math.max(options.outWidth * 1.0d/1024f, options.outHeight * 1.0d/1024f);
		options.inSampleSize = (int)Math.ceil(ratio);
		options.inJustDecodeBounds = false;
		mPhotoImg = BitmapFactory.decodeFile(mCurrentPhotoStr,options);
	}
}
