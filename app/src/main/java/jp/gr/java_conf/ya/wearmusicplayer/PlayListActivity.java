package jp.gr.java_conf.ya.wearmusicplayer; //  Copyright (c) 2014 YA<ya.androidapp@gmail.com> All rights reserved.

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayListActivity extends ListActivity {
    public ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    private final int REQUEST_PLAYLIST = 100;
    private final String mSongIndex = "songIndex";
    private final String mSongTitle = "songTitle";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist);

        ArrayList<HashMap<String, String>> songsListData = new ArrayList<HashMap<String, String>>();

        SongsManager plm = new SongsManager();
        this.songsList = plm.getPlayList();

        for (HashMap<String, String> song : songsList) {
            songsListData.add(song);
        }

        ListAdapter adapter = new SimpleAdapter(this, songsListData,
                R.layout.playlist_item, new String[]{mSongTitle}, new int[]{
                R.id.songTitle});
        setListAdapter(adapter);

        ListView lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                try {
                    Intent in = new Intent(getApplicationContext(),
                            MainActivity.class);
                    in.putExtra(mSongIndex, position);
                    setResult(REQUEST_PLAYLIST, in);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
