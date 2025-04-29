package com.cemalettinaltintas.artbook;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cemalettinaltintas.artbook.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    ArrayList<Art> artList;
    ArtAdapter artAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        artList=new ArrayList<>();
        artAdapter=new ArtAdapter(artList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(artAdapter);
        getData();
        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

    }

    private void getData(){
        try {
            SQLiteDatabase database=this.openOrCreateDatabase("ArtDb",MODE_PRIVATE,null);

            Cursor cursor=database.rawQuery("SELECT * FROM arts",null);

            int nameIx=cursor.getColumnIndex("artname");
            int idIx=cursor.getColumnIndex("id");

            while (cursor.moveToNext()){
                String artname=cursor.getString(nameIx);
                int id=cursor.getInt(idIx);

                Art art=new Art(id,artname);
                artList.add(art);
            }
            artAdapter.notifyDataSetChanged();
            cursor.close();

        }catch (Exception e){
            e.getLocalizedMessage();
        }
    }

    private void showPopupMenu(View view){
        //kodlar
        PopupMenu popupMenu= null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            popupMenu = new PopupMenu(MainActivity.this,view);
        }
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.option_menu,popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (R.id.addArt==item.getItemId()){
                    Intent intentToDetail=new Intent(MainActivity.this, DetailActivity.class);
                    intentToDetail.putExtra("info","new");
                    startActivity(intentToDetail);
                }
                return false;
            }
        });
        popupMenu.show();
    }
}