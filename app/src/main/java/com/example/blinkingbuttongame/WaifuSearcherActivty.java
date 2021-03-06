package com.example.blinkingbuttongame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class WaifuSearcherActivty extends AppCompatActivity {

    Button searchButton, saveButton, goToSavedImagesButton;
    ImageButton backButton;
    ImageView display;

    AnimeClass anime;

    List<String> urlList;
    List<String> nameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waifu_searcher_activty);

        wireWidgets();
        saveButton.setEnabled(false);
        setListeners();

        loadLists();
        checkForPastPicture();
    }

    public void wireWidgets()
    {
        searchButton = findViewById(R.id.button_waifulayout_search);
        display = findViewById(R.id.imageView_waifulayout_display);
        backButton = (ImageButton)findViewById(R.id.Imagebutton_waifulayout_backbutton);
        saveButton = findViewById(R.id.button_waifulayout_savepicture);
        goToSavedImagesButton = findViewById(R.id.button_waifulayout_savedimages);
    }

    public void setListeners()
    {
        goToSavedImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent targetIntent = new Intent(WaifuSearcherActivty.this, AnimeListView.class);
                targetIntent.putStringArrayListExtra("first_url_list",(ArrayList<String>)urlList);
                targetIntent.putStringArrayListExtra("first_name_list",(ArrayList<String>)nameList);
                startActivity(targetIntent);
                finish();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                callLink(randomNumber());
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent targetIntent = new Intent(WaifuSearcherActivty.this, MainActivity2.class);
                targetIntent.putStringArrayListExtra("first_url_list",(ArrayList<String>)urlList);
                targetIntent.putStringArrayListExtra("first_name_list",(ArrayList<String>)nameList);
                startActivity(targetIntent);
                finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    urlList.add(anime.returnUrlLink());
                    saveButton.setEnabled(false);
                goToSavedImagesButton.setEnabled(true);

                saveLists();
            }
        });
    }

    private void saveLists()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("Url List", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(urlList);
        editor.putString("Url List", json);
        editor.apply();
    }

    private void loadLists()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("Url List", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("Url List", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        urlList = gson.fromJson(json, type);

        if(urlList == null)
        {
            urlList = new ArrayList<>();

            goToSavedImagesButton.setEnabled(false);
        }
    }

    public void callLink(String number)
    {
        DisableButton disableButton = new DisableButton();
        disableButton.execute();

        Log.d("LOOK HERE","This is the id: " + number);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(WaifuApiService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WaifuApiService waifuService = retrofit.create(WaifuApiService.class);
        Call<AnimeClass> animeCall = waifuService.searchInfo(number);

        animeCall.enqueue(new Callback<AnimeClass>() {
            @Override
            public void onResponse(Call<AnimeClass> call, Response<AnimeClass> response) {

                    Log.e("THIS", "SOMETHING HAPPENED");

                if (!response.isSuccessful()) {
                    Log.e("API Error", "BAD STUFF HAPPENED, probably character doesnt exist");
                    Toast.makeText(WaifuSearcherActivty.this, "hi", Toast.LENGTH_SHORT).show();
                    return;
                }

               anime = response.body();

              //  search.setText(anime.returnURL(1));
                loadImage(anime.returnRandomURL(),display);

                if(!checkForExisitingUrl(anime.returnUrlLink())) {
                    saveButton.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<AnimeClass> call, Throwable t) {
                Toast.makeText(WaifuSearcherActivty.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Error", "BIG ERROR");
                Log.e("Error", t.getMessage());
            }
        });
    }

    public void checkForPastPicture()
    {
        Intent listIntent = getIntent();
        int position = listIntent.getIntExtra("position", -1);

       if((position != -1) && (urlList != null))
        {
            loadImage(urlList.get(position),display);
        }
    }

    public boolean checkForExisitingUrl(String url)
    {
        if(urlList != null) {
            for (int i = 0; i < urlList.size(); i++) {
                if (urlList.get(i).equals(url)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void loadImage(String url, ImageView image)
    {
        Picasso.get().load(url).into(image);
    }

    public String randomNumber() // jikan goes up to at least 2000.
    {
        String id = "" + (int)(Math.random() * 2000 + 1);
        return id;
    }

    public void timer(long milli)
    {
        try {
            Thread.sleep(milli);
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
    }



    private class DisableButton extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            searchButton.setEnabled(false);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            timer(5000);
            publishProgress(voids);
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            searchButton.setEnabled(true);
            super.onProgressUpdate(values);
        }
    }
}
