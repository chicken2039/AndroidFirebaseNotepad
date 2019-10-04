package com.example.firebase_sojeon_example;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;

    private EditText editContent;

    private TextView userEmail,userName;

    private String selectedPostKey;


    //private ImageView userImage;

    private NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        editContent = (EditText) findViewById(R.id.content);




        if (mFirebaseUser == null)
        {
            startActivity(new Intent(MainActivity.this,Main2Activity.class));
            finish();
            return;
        }




        setSupportActionBar(toolbar);
        FloatingActionButton new_post = findViewById(R.id.new_post);
        FloatingActionButton save_post = findViewById(R.id.save_post);

        new_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               initPost();
            }
        });
        save_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedPostKey == null) {
                    savePost();
                }
                else {updatePost();}
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        userEmail = (TextView) headerView.findViewById(R.id.userEmail);
        userName = (TextView) headerView.findViewById(R.id.userName);
        //userImage = (ImageView) headerView.findViewById(R.id.userImage);

        profileUpdate();
        displayPosts();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            deletePost();
        }
        else if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout()
    {
        Snackbar.make(editContent,"로그아웃하시겠습니까?",Snackbar.LENGTH_LONG)
                .setAction("로그아웃", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mFirebaseAuth.signOut();
                        startActivity(new Intent(MainActivity.this,Main2Activity.class));
                        finish();
                    }
                }).show();
    }

    private void initPost()
    {
        selectedPostKey=null;
        editContent.setText("");
    }
    private void savePost()
    {
        String txt = editContent.getText().toString();
        if (txt.isEmpty()){
            return;
        }

        Post post = new Post();
        post.setTxt(editContent.getText().toString());
        post.setCreateDate(new Date().getTime());



        mFirebaseDatabase
                .getReference("posts/"+mFirebaseUser.getUid())
                .push()
                .setValue(post)
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(editContent,"포스트가 저장됨",Snackbar.LENGTH_LONG).show();
                        initPost();

                    }
                })
                .addOnFailureListener(MainActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(editContent,"포스트 저장 실패",Snackbar.LENGTH_LONG).show();
                    }
                });

        //posts/%uid% 아래에  메모가 저장되게 합니다.
    }
    private void updatePost()
    {
        String txt = editContent.getText().toString();
        if (txt.isEmpty()){
            return;
        }
        Post post = new Post();
        post.setTxt(editContent.getText().toString());
        post.setCreateDate(new Date().getTime());
        mFirebaseDatabase
                .getReference("posts/"+mFirebaseUser.getUid()+"/"+selectedPostKey)
                .setValue(post)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(editContent,"메모가 저장되었습니다",Snackbar.LENGTH_LONG).show();
                    }
                });
    }
    private void deletePost()
    {
        if(selectedPostKey == null){ return; }
        Snackbar.make(editContent,"메모를 삭제하시겠습니까",Snackbar.LENGTH_LONG)
                .setAction("삭제", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mFirebaseDatabase
                                .getReference("posts/"+mFirebaseUser.getUid()+"/"+selectedPostKey)
                                .removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        Snackbar.make(editContent,"삭제가 완료되었습니다.",Snackbar.LENGTH_LONG).show();
                                        initPost();
                                    }
                                });

                    }
                }).show();


    }



    private void profileUpdate()
    {
        userEmail.setText(mFirebaseUser.getEmail());
        userName.setText(mFirebaseUser.getDisplayName());
        //userImage.setImageURI(mFirebaseUser.getPhotoUrl());

    }
    private void displayPosts()
    {
         mFirebaseDatabase.getReference("posts/"+mFirebaseUser.getUid())
                 .addChildEventListener(new ChildEventListener() {
                     @Override
                     public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                         //데이터가 추가된경우
                         Post post = dataSnapshot.getValue(Post.class);
                         post.setKey(dataSnapshot.getKey());

                         Boolean isDuplecated = false;

                         for (int i  = 0 ; i < navigationView.getMenu().size();i++) {
                             MenuItem menuItem = navigationView.getMenu().getItem(i);
                             if (post.getKey().equals(((Post)menuItem.getActionView().getTag()).getKey())) {
                                 //리스트를 돌면서 key가 같다면
                                isDuplecated = true;
                             }

                         }

                         Log.d("MisakaMoe","ChildAdd");
                         if(!isDuplecated)
                            displayPostList(post);
                     }

                     @Override
                     public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        //데이터가 변경된경우
                         Post post = dataSnapshot.getValue(Post.class);
                         post.setKey(dataSnapshot.getKey());

                         for (int i  = 0 ; i < navigationView.getMenu().size();i++) {
                             MenuItem menuItem = navigationView.getMenu().getItem(i);
                             if (post.getKey().equals(((Post)menuItem.getActionView().getTag()).getKey())) {
                                //리스트를 돌면서 key가 같다면
                                 menuItem.getActionView().setTag(post);
                                 menuItem.setTitle(post.getTitle());
                             }

                         }

                         Log.d("MisakaMoe","ChildChanged");

                     }

                     @Override
                     public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                         //데이터가 삭제된 경우
                         navigationView.getMenu().clear();
                         displayPosts();
                         Log.d("MisakaMoe","ChildRemoved");
                     }

                     @Override
                     public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                         //데이터가 이동한 경우
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {
                        //데이터가 로드중 캔슬된 경우
                     }
                 });
    }

    private void displayPostList(Post post){
        Menu navMenu = navigationView.getMenu();
        MenuItem item = navMenu.add(post.getTitle());
        View view = new View(getApplication());
        view.setTag(post);
        item.setActionView(view);
        Log.d("MisakaMoe","displayPostList");

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Post selectedPost = (Post) item.getActionView().getTag();
        editContent.setText(selectedPost.getTxt());
        selectedPostKey = selectedPost.getKey();
        DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
