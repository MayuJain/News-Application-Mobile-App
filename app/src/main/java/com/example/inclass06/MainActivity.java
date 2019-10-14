package com.example.inclass06;

/*
* Group 29
* Inclass 06
* MainActivity.java
* Mayuri Jain, Narendra Pahuja
*
* */
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

public class MainActivity extends AppCompatActivity {

    Button bt_select;
    TextView tv_select;
    TextView tv_title;
    TextView tv_time;
    ImageView im_view;
    TextView tv_scroll;
    TextView tv_count;
    ImageView im_next;
    ImageView im_prev;
    ProgressBar pg_progress;
    ArrayList<News> result = new ArrayList<>();
    int currentIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt_select = findViewById(R.id.bt_select);
        tv_select = findViewById(R.id.tv_select);
        tv_title = findViewById(R.id.tv_title);
        tv_time = findViewById(R.id.tv_time);
        tv_scroll = findViewById(R.id.tv_scroll);
        tv_count = findViewById(R.id.tv_count);
        im_view = findViewById(R.id.iv_photo);
        im_prev = findViewById(R.id.im_prev);
        im_next = findViewById(R.id.im_next);
        pg_progress = findViewById(R.id.pg_progress);

        //declaring invisible
        pg_progress.setVisibility(View.INVISIBLE);
        im_view.setVisibility(View.INVISIBLE);
        im_next.setEnabled(false);
        im_prev.setEnabled(false);


        bt_select.setOnClickListener(new View.OnClickListener() {

            AlertDialog dialog;
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose Category");
                final String[] categories = getResources().getStringArray(R.array.categories);
                builder.setItems(categories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String category = categories[i];
                        tv_select.setText(category);
                        result = null;
                        String url="https://newsapi.org/v2/top-headlines?category="+category+"&apiKey=c9059ac0c4cd4fe0989090f935fa7f2f";

                        if(isConnected()){
                            new getAsyncTask().execute(url);
                        }else{
                            Toast.makeText(MainActivity.this, "No Internet", Toast.LENGTH_SHORT).show();
                        }

                    }
            });
            dialog = builder.create();
            dialog.show();
            }
        });

        im_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentIndex = getindex("right",currentIndex, result.size());
                displayData();
            }
        });

        im_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentIndex = getindex("left",currentIndex, result.size());
                displayData();
            }
        });

    }

    public void displayData(){
        tv_title.setText(result.get(currentIndex).Title);
        tv_time.setText(result.get(currentIndex).publishedAt);
        tv_scroll.setText(result.get(currentIndex).description);
        Picasso.get().load(result.get(currentIndex).urlToImage).into(im_view);
        tv_count.setText(currentIndex+1+" out of "+ result.size());
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            return false;
        }
        return true;
    }

    private int getindex(String direction,int currentIndex,int size)
    {
        if(direction=="left" && currentIndex !=0)
            return currentIndex-1;
        else if( direction=="left" && currentIndex==0)
            return size-1;

        else if( direction=="right" && currentIndex!=size-1)
            return currentIndex+1;
        else
            return 0;

    }

    public class getAsyncTask extends AsyncTask<String, Void, ArrayList<News>>{

        @Override
        protected void onPreExecute() {
            pg_progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<News> doInBackground(String... params) {
            HttpURLConnection connection = null;
            result = new ArrayList<News>();

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String json = IOUtils.toString(connection.getInputStream(), "UTF8");

                    JSONObject root = new JSONObject(json);
                    JSONArray articles = root.getJSONArray("articles");
                    for (int i=0;i<articles.length();i++) {
                        JSONObject articleJson = articles.getJSONObject(i);
                        News news = new News();
                        news.Title = articleJson.getString("title");
                        if(articleJson.getString("description") == "null"){
                            news.description = "No Description available";
                        }else{
                            news.description = articleJson.getString("description");
                        }
                        news.publishedAt = articleJson.getString("publishedAt");
                        news.urlToImage = articleJson.getString("urlToImage");
                        result.add(news);
                    }
                }
                result = new ArrayList<>(result.subList(0,20));
            } catch (Exception e) {
                //Handle Exceptions
            } finally {
                //Close the connections
            }
            return result;

        }

        @Override
        protected void onPostExecute(ArrayList<News> news) {
            
            if(news.size() > 0 ){

                if(news.size() == 1){
                    im_next.setEnabled(false);
                    im_prev.setEnabled(false);
                }else{
                    im_next.setEnabled(true);
                    im_prev.setEnabled(true);
                }
                currentIndex = 0;
                pg_progress.setVisibility(View.INVISIBLE);
                im_view.setVisibility(View.VISIBLE);
                Log.d("demo", news.get(0).urlToImage);
                tv_title.setText(news.get(0).Title);
                tv_time.setText(news.get(0).publishedAt);
                tv_scroll.setText(news.get(0).description);
                Picasso.get().load(news.get(0).urlToImage).into(im_view);
                tv_count.setText(currentIndex+1+" out of "+ news.size());
            }else{
                Toast.makeText(MainActivity.this, "No News Found", Toast.LENGTH_SHORT).show();
                tv_title.setText("");
                tv_time.setText("");
                tv_scroll.setText("");
                im_view.setVisibility(View.INVISIBLE);
                tv_count.setText("");
                im_next.setEnabled(false);
                im_prev.setEnabled(false);
            }
            


        }
    }
}
