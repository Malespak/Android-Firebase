package com.example.karyaseni;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {

    Button tambah, logout, simpan;
    ImageView inputgambar;

    private static final int SELECT_FILE = 1;
    private StorageReference mStorage;
    //uri to store file
    private Uri filePath;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tambah = findViewById(R.id.btnphoto);
        simpan = findViewById(R.id.btnsee);
        logout = findViewById(R.id.btnlogout);
        inputgambar = findViewById(R.id.addphoto);


        simpan.setVisibility(View.INVISIBLE);

        mStorage = FirebaseStorage.getInstance().getReference("photos");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("dataGambar");

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, Login.class));
                    finish();
                }
            }
        };
        tambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent g = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                g.setType("image/*");
                startActivityForResult(g.createChooser(g,"Select File"), SELECT_FILE);

                simpan.setVisibility(View.VISIBLE);

            }
        });

        simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePath != null) {
                    final StorageReference storageReference = mStorage.child("photos" + System.currentTimeMillis() + "." + getFileExtension(filePath));
                    storageReference.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(getApplicationContext(), "Add Resep is Success", Toast.LENGTH_SHORT).show();
                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful());
                            Uri uris = urlTask.getResult();
                            myRef.child("photos").setValue(uris).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent success = new Intent(MainActivity.this,Result.class);
                                    startActivity(success);
                                    inputgambar.setImageResource(R.drawable.ic_launcher_background);

                                }
                            });

                        }
                    });
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "No File Selected", Toast.LENGTH_SHORT).show();

                }

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"You have Logout",Toast.LENGTH_SHORT).show();
                mAuth.signOut();
            }
        });
    }

    private String getFileExtension(Uri filePath) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(filePath));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK ) {
            if (requestCode==SELECT_FILE && data != null && data.getData() != null)
            {

                filePath=data.getData();

                inputgambar.setImageURI(filePath);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            mAuth.removeAuthStateListener(authListener);
        }
    }
}
