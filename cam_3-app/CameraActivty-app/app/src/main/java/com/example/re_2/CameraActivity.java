package com.example.re_2;

import static com.example.re_2.functions.BitmapConverter.BitmapToString;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";

    public static final int REQUEST_TAKE_PHOTO = 10;
    public static final int REQUEST_PERMISSION = 11;
    private static final Logger log = LoggerFactory.getLogger(CameraActivity.class);

    private Button btnCamera, btnSave;
    private ImageView ivCapture;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        checkPermission(); //권한체크 이거 완전 느림
        captureCamera();//카메라 바로 실행


        ivCapture = findViewById(R.id.ivCapture); //ImageView 선언
        btnCamera = findViewById(R.id.btnCapture); //Button 선언
        btnSave = findViewById(R.id.btnSave); //Button 선언

        loadImgArr();

        //촬영
        btnCamera.setOnClickListener(v -> captureCamera());

        //저장
        btnSave.setOnClickListener(v -> {

            try {

                BitmapDrawable drawable = (BitmapDrawable) ivCapture.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                //찍은 사진이 없으면
                if (bitmap == null) {
                    Toast.makeText(this, "저장할 사진이 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    //저장
                    String bitmap_string=saveImg();
                    String a="ifd";
                    //Log.v("bitmap:","bitmap_string"+bitmap_string);
                    Log.v("bitmap length:",""+bitmap_string.length());
                    //Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
                    mCurrentPhotoPath = ""; //initialize
                    Intent intent = new Intent(getApplicationContext(), MenuActivity.class);


                    startActivity(intent);
                    finish();//카메라 액티비티 종료
                }

            } catch (Exception e) {
                Log.w(TAG, "SAVE ERROR!", e);
            }
        });
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void captureCamera() //void -> boolean으로 변경
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 인텐트를 처리 할 카메라 액티비티가 있는지 확인
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            // 촬영한 사진을 저장할 파일 생성
            File photoFile = null;

            try {
                //임시로 사용할 파일이므로 경로는 캐시폴더로
                File tempDir = getCacheDir();

                //임시촬영파일 세팅
                String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
                String imageFileName = "Capture_" + timeStamp + "_"; //ex) Capture_20201206_

                File tempImage = File.createTempFile(
                        imageFileName,  /* 파일이름 */
                        ".jpg",         /* 파일형식 */
                        tempDir      /* 경로 */
                );

                // ACTION_VIEW 인텐트를 사용할 경로 (임시파일의 경로)
                mCurrentPhotoPath = tempImage.getAbsolutePath();

                photoFile = tempImage;

            } catch (IOException e) {
                //에러 로그는 이렇게 관리하는 편이 좋다.
                Log.w(TAG, "파일 생성 에러!", e);
            }

            //파일이 정상적으로 생성되었다면 계속 진행
            if (photoFile != null) {
                //바로사진저장으로 건너뛰려고 넣어놓음

                //

                //Uri 가져오기
                Uri photoURI = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider",
                        photoFile);

                //인텐트에 Uri담기
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                //인텐트 실행
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);


            }
        }

    }

    //이미지저장 메소드(내가 String을 반환하게 함)
    private String saveImg() {
        //이미지 저장위치 변수
       String image_location=getFilesDir() + "/capture/pic.jpg";
       Bitmap bitmap = null;
       Bitmap compressedBitmap = null;

        //

        try {
            //저장할 파일 경로
            File storageDir = new File(getFilesDir() + "/capture");
            if (!storageDir.exists()) //폴더가 없으면 생성.
                storageDir.mkdirs();

            String filename = "pic" + ".jpg";

            // 기존에 있다면 삭제
            File file = new File(storageDir, filename);
            boolean deleted = file.delete();
            Log.w(TAG, "Delete Dup Check : " + deleted);
            FileOutputStream output = null;

            try {
                output = new FileOutputStream(file);
                BitmapDrawable drawable = (BitmapDrawable) ivCapture.getDrawable();
                bitmap = drawable.getBitmap();//Bitmap bitmap = drawable.getBitmap()이였음

                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, output); //해상도에 맞추어 Compress

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    assert output != null;
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

           // Log.e(TAG, "Captured Saved");
            Toast.makeText(this, "이미지 처리 중입니다", Toast.LENGTH_SHORT).show();
           // Toast.makeText(this, "Capture Saved ", Toast.LENGTH_SHORT).show();
            //이미지 위치 출력
            //Toast.makeText(this, image_location, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "잠시만 기다려주세요..!", Toast.LENGTH_SHORT).show();
            //
        } catch (Exception e) {
            Log.w(TAG, "Capture Saving Error!", e);
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();

        }
        //이미지 경로 반환
       //return image_location;
        //Log.i("비트맵은:",BitmapToString(bitmap));//이거 내가 추가해봄 로그
        return BitmapToString(bitmap);
        //
    }

    private void loadImgArr() {
        File storageDir = null;
        try {

            storageDir = new File(getFilesDir() + "/capture");
            String filename = "pic" + ".jpg";

            File file = new File(storageDir, filename);
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            ivCapture.setImageBitmap(bitmap);

        } catch (Exception e) {
            Log.w(TAG, "Capture loading Error!", e);
            Toast.makeText(this, "load failed", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            //after capture
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO: {
                    if (resultCode == RESULT_OK) {

                        File file = new File(mCurrentPhotoPath);
                        Bitmap bitmap = MediaStore.Images.Media
                                .getBitmap(getContentResolver(), Uri.fromFile(file));

                        if (bitmap != null) {
                            ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_UNDEFINED);

//                            //사진해상도가 너무 높으면 비트맵으로 로딩
//                            BitmapFactory.Options options = new BitmapFactory.Options();
//                            options.inSampleSize = 8; //8분의 1크기로 비트맵 객체 생성
//                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                            Bitmap rotatedBitmap = null;
                            switch (orientation) {

                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    rotatedBitmap = rotateImage(bitmap, 90);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    rotatedBitmap = rotateImage(bitmap, 180);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    rotatedBitmap = rotateImage(bitmap, 270);
                                    break;

                                case ExifInterface.ORIENTATION_NORMAL:
                                default:
                                    rotatedBitmap = bitmap;
                            }

                            //Rotate한 bitmap을 ImageView에 저장
                            ivCapture.setImageBitmap(rotatedBitmap);

                        }
                    }
                    break;
                }
            }

        } catch (Exception e) {
            Log.w(TAG, "onActivityResult Error !", e);
        }
    }

    //카메라에 맞게 이미지 로테이션
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    @Override
    public void onResume() {
        super.onResume();
      //  checkPermission(); //권한체크
    }

    //권한 확인
    public void checkPermission() {
        int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //권한이 없으면 권한 요청
        if (permissionCamera != PackageManager.PERMISSION_GRANTED
                || permissionRead != PackageManager.PERMISSION_GRANTED
                || permissionWrite != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toast.makeText(this, "이 앱을 실행하기 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                // 권한이 취소되면 result 배열은 비어있다.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "권한 확인", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();
                    //finish(); //권한이 없으면 앱 종료
                }
            }
        }
    }


    //aws rekognition 이지지 처리 총괄 함수


    //
//aws rekognition용 detectFacesinImage 메소드

//

}