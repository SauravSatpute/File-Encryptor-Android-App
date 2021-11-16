package com.example.fileencryptor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.fileencryptor.R.id.pick3;

public class MainActivity extends AppCompatActivity {
    Button button;
    TextView textView;
    static String filePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,new String[]{ READ_EXTERNAL_STORAGE , WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED );

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 1001);
        }


        button = (Button) findViewById(R.id.pick3);
        textView = (TextView) findViewById(R.id.textView2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialFilePicker()
                        .withActivity(MainActivity.this)
                        .withRequestCode(1000)
//                    .withFilter(Pattern.compile(".*\\.(jpg|jpeg)$"))
//                        .withFilterDirectories(true)
                        .withHiddenFiles(true)
                        .start();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK) {
            filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            // Do anything with file
            String[] path = filePath.split("/");
//            Log.d("on click :", path[path.length-1]);
            textView.setText(path[path.length-1]);
            //the name of file to be encrypted



        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1001 : {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this,"Permission Granted", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this,"Permission Not Granted" , Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    public void buttonShareFile(View view) {
        File file = new File(MainActivity.filePath);

        if(filePath.isEmpty()) {
            Toast.makeText(MainActivity.this, "File is not selected.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intentShare = new Intent(Intent.ACTION_SEND);

        intentShare.setType("application/pdf");
        intentShare.putExtra(Intent.EXTRA_STREAM, Uri.parse(String.valueOf(file)));

        startActivity(Intent.createChooser(intentShare,"Share the file....."));
    }

    public void encryptFile(View view){
        if(filePath.isEmpty()){
            Toast.makeText(MainActivity.this, "File is not selected...", Toast.LENGTH_LONG).show();
            return;
        }
        File inputfile = new File(filePath);
        //the name of file to be decrypted   we have added .enc extension to that encrypted file
        String[] path = filePath.split("/");

        String filename = path[path.length-1];

        File outputfile = new File("/storage/emulated/0/Android/"+filename+".enc");
        String key = "secrete key_____";
        Toast.makeText(MainActivity.this, "File is Encrypted...check your Android Folder", Toast.LENGTH_LONG).show();

        try {
            CryptoUtils.encrypt(key, inputfile, outputfile);
            //    CryptoUtils.decrypt(key, encryptedFile, outputfile);
        } catch (CryptoException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void decryptFile(View view) {
        File inputfile = new File(filePath);
        if(filePath.isEmpty()){
            Toast.makeText(MainActivity.this, "File is not selected...", Toast.LENGTH_LONG).show();
            return;
        }

        //the name of file to be decrypted   we have added .enc extension to that encrypted file
        String[] filep = filePath.split("/");
        String f = filep[filep.length-1];

        char arr[] = f.toCharArray();
        if((arr[arr.length-1] != 'c') || (arr[arr.length-2]!= 'n') || (arr[arr.length-3] !='e') || (arr[arr.length-4] != '.')){
            Toast.makeText(MainActivity.this, "Please select encrypted file.", Toast.LENGTH_LONG).show();

            return;
        }

        Toast.makeText(MainActivity.this, "File is Decrypted...check your Android Folder", Toast.LENGTH_LONG).show();
        String finalName = f.replace(".enc","");

        File encryptedFile = new File(filePath);

        File outputfile = new File("/storage/emulated/0/Android/"+finalName);
//         outputfile = new File(filePath+".enc");
        String key = "secrete key_____";
        try {
//             CryptoUtils.encrypt(key, inputfile, outputfile);
            CryptoUtils.decrypt(key, encryptedFile, outputfile);
        } catch (CryptoException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }


    public void Logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext() , Login.class));
        finish();
    }
}

