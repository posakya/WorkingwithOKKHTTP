package com.example.roshan.imageupload;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private Button btn_load,btn_upload;
    private EditText edit_image;
    private ImageView imageView;
    final int RQS_IMAGE1 = 1;
    Uri source1;
    Bitmap bm1;
    private String filePath = null;
    public static final String FILE_UPLOAD_URL = "http://192.168.10.4/EasyPetrol/update.php";
    long totalSize = 0;
    String type;
    File file;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_load=(Button)findViewById(R.id.btn_load);
        btn_upload=(Button)findViewById(R.id.btn_upload);
        edit_image=(EditText)findViewById(R.id.image_name);
        imageView=(ImageView)findViewById(R.id.imageUpload);

        btn_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RQS_IMAGE1);
            }
        });

//        // Checking camera availability
//        if (!isDeviceSupportCamera()) {
//            Toast.makeText(getApplicationContext(),
//                    "Sorry! Your device doesn't support camera",
//                    Toast.LENGTH_LONG).show();
//            // will close the app if the device does't have camera
//            finish();
//        }
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                type = edit_image.getText().toString();
                //new UploadFileToServer().execute);
                UploadFileToServer upd = new UploadFileToServer(file);
                upd.execute();

            }
        });
    }
    /**
     * Checking device has camera hardware or not
     //     * */
//    private boolean isDeviceSupportCamera() {
//        if (getApplicationContext().getPackageManager().hasSystemFeature(
//                PackageManager.FEATURE_CAMERA)) {
//            // this device has a camera
//            return true;
//        } else {
//            // no camera on this device
//            return false;
//        }
//    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case RQS_IMAGE1:
                    source1 = data.getData();
                    try {
                        System.out.println("Bitmap path = "+source1.getPath());
                        bm1 = BitmapFactory.decodeStream(
                               getContentResolver().openInputStream(source1));
                        imageView.setImageBitmap(bm1);

                        String[] filePathColumn = { MediaStore.Images.Media.DATA };
                        Cursor cursor = getContentResolver().query(source1, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String filePath = cursor.getString(columnIndex);
                        cursor.close();

                        file = new File(filePath);

                        System.out.println("Image :"+bm1);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }
    }

    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        File type1;

        UploadFileToServer(File type)
        {
            this.type1 = type;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

//        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return uploadFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() throws IOException {
            String responseString = null;

            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("Image", file.getName(),
                            RequestBody.create(MediaType.parse("image/png"), file))
                    .addFormDataPart("Type", type)
                    .build();
            Request request = new Request.Builder().url(FILE_UPLOAD_URL).post(formBody).build();
            Response response = client.newCall(request).execute();

            Log.e("message", response.body().string());
            return "OK";



        }

        @Override
        protected void onPostExecute(String result) {
            // Log.e(TAG, "Response from server: " + result);

            // showing the server response in an alert dialog
            //showAlert(result);

            super.onPostExecute(result);
        }

    }

    /**
     * Method to show alert dialog
     * */
    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
