package com.shimmerresearch.multishimmer;

import java.io.File;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.shimmerresearch.multishimmerplay.R;

public class ShimmerSetSound extends Activity {

	public static String EXTRA_SOUND_ADDRESS = "sound_address";
	String[] mFileNames;
	ListView listView1 ;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_sound);
        
     

        File file = new File(Environment.getExternalStorageDirectory(), "multishimmerplay");
        listFiles(file);
        
        listView1.setOnItemClickListener(new OnItemClickListener() {
         			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub

         				Intent intent = new Intent();
         				intent.putExtra(EXTRA_SOUND_ADDRESS, mFileNames[position]);
         		        setResult(Activity.RESULT_OK, intent);    
         				finish();
         				// return this values to the main page
			}
        });
    }

    
    void listFiles(File f){
        File[] files = f.listFiles();
        mFileNames = new String [files.length];
        int i=0;
        for (File file : files){
         Log.d("Shimmer",file.getPath());
         mFileNames[i] = file.getPath();
         i++;
        }
        
        
        listView1 = (ListView) findViewById(R.id.listViewFiles);
        
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, mFileNames);
        
        listView1.setAdapter(adapter);

    }
    
    

    

    
}
