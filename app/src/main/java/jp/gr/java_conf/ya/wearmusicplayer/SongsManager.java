package jp.gr.java_conf.ya.wearmusicplayer; //  Copyright (c) 2014 YA<ya.androidapp@gmail.com> All rights reserved.

import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class SongsManager {
    final String MEDIA_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/";
    private final String mSongPath = "songPath";
    private final String mSongTitle = "songTitle";
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

    public SongsManager() {}

    public ArrayList<HashMap<String, String>> getPlayList() {
        try {
            File home = new File(MEDIA_PATH);
            getPlayList(home);
            return songsList;
        } catch (Exception e) {
            Log.d("",e.getLocalizedMessage());
        }
        return new ArrayList<HashMap<String, String>>();
    }

    private void getPlayList(File home) {
        try {
            if (!(home == null)) {
                File[] files = home.listFiles(new FileExtensionFilter());
                if (!(files == null)) {
                    if (files.length > 0) {
                        for (File file : files) {
                            if (file.isDirectory()) {
                                getPlayList(file);
                            } else if (file.isFile()) {
                                addSong(file);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d("",e.getLocalizedMessage());
        }
    }

    private void addSong(File file) {
        try {
            HashMap<String, String> song = new HashMap<String, String>();
            song.put(mSongTitle, file.getName().substring(0, (file.getName().lastIndexOf("."))));
            song.put(mSongPath, file.getPath());
            songsList.add(song);
        } catch (Exception e) {
            Log.d("",e.getLocalizedMessage());
        }
    }

    /**
     * Class to filter files which are having .mp3 extension
     */
    class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            try {
                File file = new File(name);
                name = name.toLowerCase(Locale.getDefault());
                if (file.isDirectory()) {
                    return true;
                } else if (name.endsWith(".3gp")) {
                    return true;
                } else if (name.endsWith(".flac")) {
                    return true;
                } else if (name.endsWith(".mp3")) {
                    return true;
                } else if (name.endsWith(".mid")) {
                    return true;
                } else if (name.endsWith(".ogg")) {
                    return true;
                } else if (name.endsWith(".wav")) {
                    return true;
                }
            } catch (Exception e) {
                Log.d("",e.getLocalizedMessage());
            }
            return false;
        }
    }
}
