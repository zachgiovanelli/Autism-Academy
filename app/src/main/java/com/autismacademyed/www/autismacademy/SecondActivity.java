package com.autismacademyed.www.autismacademy;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.prof.youtubeparser.Parser;
import com.prof.youtubeparser.models.videos.Video;

import java.util.ArrayList;

/**
 * Created by Owner on 9/26/2017.
 */

public class SecondActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private VideoAdapter vAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar progressBar;
    private FloatingActionButton fab;
    private int totalElement;
    private String nextToken;
    private final String CHANNEL_ID = "UCUwPShLvnCOTeQILvAtneOw";
    //TODO: delete
    private final String API_KEY = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.container);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        mSwipeRefreshLayout.canChildScrollUp();
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                vAdapter.clearData();
                vAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(true);
                loadVideo();
            }
        });

        if (!isNetworkAvailable()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.alert_message)
                    .setTitle(R.string.alert_title)
                    .setCancelable(false)
                    .setPositiveButton(R.string.alert_positive,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    finish();
                                }
                            });

            AlertDialog alert = builder.create();
            alert.show();

        } else if (isNetworkAvailable())
            loadVideo();

        //show the fab on the bottom of recycler view
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                LinearLayoutManager layoutManager = LinearLayoutManager.class.cast(recyclerView.getLayoutManager());
                int lastVisible = layoutManager.findLastVisibleItemPosition();

                if (lastVisible == totalElement - 1)
                    fab.setVisibility(View.VISIBLE);
                else
                    fab.setVisibility(View.GONE);

                super.onScrolled(recyclerView, dx, dy);
            }
        });

        //load more data
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Parser parser = new Parser();
                if (nextToken != null) {
                    String url = parser.generateMoreDataRequest(CHANNEL_ID, 20, Parser.ORDER_DATE, API_KEY, nextToken);
                    parser.execute(url);
                    parser.onFinish(new Parser.OnTaskCompleted() {
                        @Override
                        public void onTaskCompleted(ArrayList<Video> list, String nextPageToken) {

                            //update the adapter with the new data
                            vAdapter.getList().addAll(list);
                            totalElement = vAdapter.getItemCount();
                            nextToken = nextPageToken;
                            vAdapter.notifyDataSetChanged();
                            Toast.makeText(SecondActivity.this, "New video added!", Toast.LENGTH_SHORT).show();
                            fab.setVisibility(View.GONE);
                            mRecyclerView.scrollBy(0, 1000);
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(SecondActivity.this, "Error while loading data. Please retry", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(SecondActivity.this, "Unable to load data. Please retry", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void loadVideo() {

        if (!mSwipeRefreshLayout.isRefreshing())
            progressBar.setVisibility(View.VISIBLE);

        Parser parser = new Parser();
        String url = parser.generateRequest(CHANNEL_ID, 20, Parser.ORDER_DATE, API_KEY);

        parser.execute(url);
        parser.onFinish(new Parser.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(ArrayList<com.prof.youtubeparser.models.videos.Video> list, String nextPageToken) {
                //list is an ArrayList with all video's item
                //set the adapter to recycler view
                vAdapter = new VideoAdapter(list, R.layout.yt_row, SecondActivity.this);
                mRecyclerView.setAdapter(vAdapter);
                totalElement = vAdapter.getItemCount();
                nextToken = nextPageToken;
                vAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError() {
                Toast.makeText(SecondActivity.this, "Error while loading data. Please retry", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {

        super.onResume();
        if (vAdapter != null)
            vAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (vAdapter != null)
            vAdapter.clearData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {

            android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(SecondActivity.this).create();
            alertDialog.setTitle(R.string.app_name);
            alertDialog.setMessage(Html.fromHtml(SecondActivity.this.getString(R.string.info_text) +
                    " <a href='http://github.com/prof18/YoutubeParser'>GitHub.</a>" +
                    SecondActivity.this.getString(R.string.author)));
            alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            ((TextView) alertDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        }

        return super.onOptionsItemSelected(item);
    }
}
