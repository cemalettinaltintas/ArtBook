package com.cemalettinaltintas.artbook;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cemalettinaltintas.artbook.databinding.ActivityDetailBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;
    ActivityResultLauncher<String> permissionResultLauncher;//izin için
    ActivityResultLauncher<Intent> activityResultLauncher;//galeriye gitmek için
    public static Uri secilenGorsel;
    Bitmap secilenBitmap;
    SQLiteDatabase  database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityDetailBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        registerLauncher();
        database=this.openOrCreateDatabase("ArtDb",MODE_PRIVATE,null);

        Intent intent=getIntent();
        String info = intent.getStringExtra("info");
        if (info.matches("new")){
            binding.artNameText.setText("");
            binding.painterNameText.setText("");
            binding.yearText.setText("");
            binding.button.setVisibility(View.VISIBLE);

            Bitmap selectImage= BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.addimage);
            binding.imageView.setImageBitmap(selectImage);
        }else{
            binding.button.setVisibility(View.INVISIBLE);
        }
    }

    private void registerLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == AppCompatActivity.RESULT_OK) {
                    Intent intentFromResult = o.getData();
                    if (intentFromResult != null) {
                        secilenGorsel = intentFromResult.getData();
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                //Yeni yöntem
                                ImageDecoder.Source source = ImageDecoder.createSource(DetailActivity.this.getContentResolver(), secilenGorsel);
                                secilenBitmap = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(secilenBitmap);
                            } else {
                                //Eski yöntem
                                secilenBitmap = MediaStore.Images.Media.getBitmap(DetailActivity.this.getContentResolver(), secilenGorsel);
                                binding.imageView.setImageBitmap(secilenBitmap);
                            }
                        } catch (IOException e) {
                            e.getLocalizedMessage();
                        }
                    }
                }
            }
        });
        permissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {
                if (o) {
                    //izin verildi
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                } else {
                    //izin verilmedi
                    Toast.makeText(DetailActivity.this, "İzin verilmedi!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void selectImage(View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(DetailActivity.this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                //izin istenecek
                if (ActivityCompat.shouldShowRequestPermissionRationale(DetailActivity.this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, "Galeriye ulaşıp görsel seçmemiz lazım!", Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //izin istenecek
                            permissionResultLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();
                } else {
                    //izin istenecek
                    permissionResultLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            } else {
                //izin verilmiş, galeriye gidilecek
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        } else {
            if (ContextCompat.checkSelfPermission(DetailActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //İzin verilmemiş, izin istememiz gerekiyor.
                if (ActivityCompat.shouldShowRequestPermissionRationale(DetailActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //Snacbar gösterebiliriz. Kullanıcıdan neden izin istediğimizi bir kez daha söyleyerek izin istememiz lazım.
                    Snackbar.make(view, "Galeriye ulaşıp görsel seçmemiz lazım!", Snackbar.LENGTH_INDEFINITE).setAction("İzin ver", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //İzin isteyeceğiz.
                            permissionResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                } else {
                    //İzin isteyeceğiz
                    permissionResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            } else {
                //İzin verilmiş, galeriye gidebilirim.
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }

    }


    public Bitmap kucukBitmapOlustur(Bitmap image, int maximumSize) {

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image,width,height,true);
    }

    public void save(View view) {
        String artName= binding.artNameText.getText().toString();
        String painterName= binding.painterNameText.getText().toString();
        String year= binding.yearText.getText().toString();
        Bitmap smallImage= kucukBitmapOlustur(secilenBitmap,300);

        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray= outputStream.toByteArray();

        try {
            database=this.openOrCreateDatabase("ArtDb",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY,artname VARCHAR,paintername VARCHAR,year VARCHAR,image BLOB)");
            String sqlString="INSERT INTO arts(artname,paintername,year,image) VALUES(?,?,?,?)";
            SQLiteStatement sqLiteStatement=database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,artName);
            sqLiteStatement.bindString(2,painterName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();
        }catch(Exception e){
            e.getLocalizedMessage();
        }
        Intent intent=new Intent(DetailActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}