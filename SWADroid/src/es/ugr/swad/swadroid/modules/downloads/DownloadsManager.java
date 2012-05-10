/*
 *  This file is part of SWADroid.
 *
 *  Copyright (C) 2012 Helena Rodriguez Gijon <hrgijon@gmail.com>
 *
 *  SWADroid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  SWADroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with SWADroid.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.ugr.swad.swadroid.modules.downloads;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import es.ugr.swad.swadroid.Global;
import es.ugr.swad.swadroid.MenuActivity;
import es.ugr.swad.swadroid.R;
import es.ugr.swad.swadroid.model.Course;


/**
 * Activity to navigate through the directory tree of documents and to manage the downloads of documents
 * @author Helena Rodríguez Gijon <hrgijon@gmail.com>
 * */
public class DownloadsManager extends MenuActivity {
	/**
	 * Class that contains the directory tree and gives information of each level
	 * */
	private DirectoryNavigator	navigator;
	
	/**
	 * Specifies whether to display the documents or the shared area of the subject
	 * 1 specifies documents area
	 * 2 specifies shared area
	 * */
	private int downloadsCode = 0;
	/**
	 * String that contains the xml files recevied from the web service 
	 * */
	private String tree;
	
    /**
     * Downloads tag name for Logcat
     */
    public static final String TAG = Global.APP_TAG + " Downloads";
    
    /**
     * Course name
     * */
    private String courseName;

	@Override
	protected void onStart() {
		super.onStart();
		
		Course courseSelected = ((Course) dbHelper.getRow(Global.DB_TABLE_COURSES, "id",String.valueOf( Global.getSelectedCourseCode())));
		
		courseName =courseSelected.getName();
		
		Intent activity;
		activity = new Intent(getBaseContext(),DirectoryTreeDownload.class);
		activity.putExtra("treeCode",downloadsCode);
		startActivityForResult(activity,Global.DIRECTORY_TREE_REQUEST_CODE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		downloadsCode = getIntent().getIntExtra("downloadsCode", Global.DOCUMENTS_AREA_CODE);

		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			switch(requestCode){
			//After get the list of courses, a dialog is launched to choice the course
			case Global.DIRECTORY_TREE_REQUEST_CODE:
				tree = data.getStringExtra("tree");
				setMainView();
				break;
			}
		}
	}
	
	private void setMainView(){
		ImageView image;
		TextView text;
		setContentView(R.layout.navigation);
		
		if(downloadsCode == Global.DOCUMENTS_AREA_CODE){
	        image = (ImageView)this.findViewById(R.id.moduleIcon);
	        image.setBackgroundResource(R.drawable.folder);
	        
	        text = (TextView)this.findViewById(R.id.moduleName);
	        text.setText(R.string.documentsDownloadModuleLabel);		
		}else{ //SHARE_AREA_CODE
	        image = (ImageView)this.findViewById(R.id.moduleIcon);
	        image.setBackgroundResource(R.drawable.folderusers);
	        
	        text = (TextView)this.findViewById(R.id.moduleName);
	        text.setText(R.string.sharedsDownloadModuleLabel);				
		}
		
		TextView text2;
		text2= (TextView) this.findViewById(R.id.path);
		text2.setText(R.string.blogTitle);
		
		navigator = new DirectoryNavigator(tree);
		GridView grid = (GridView) this.findViewById(R.id.gridview);
		ArrayList<DirectoryItem> r = (ArrayList<DirectoryItem>) navigator.goToRoot();
		text2.setText(courseName + " "+navigator.getPath());
		grid.setAdapter(new NodeAdapter(this,r));
	}


}