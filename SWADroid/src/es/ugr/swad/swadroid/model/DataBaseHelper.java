/*
 *  This file is part of SWADroid.
 *
 *  Copyright (C) 2010 Juan Miguel Boyero Corral <juanmi1982@gmail.com>
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
package es.ugr.swad.swadroid.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import es.ugr.swad.swadroid.Global;
import es.ugr.swad.swadroid.R;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper {
	//The Android's default system path of your application database.
	private static String DB_PATH = "/data/data/es.ugr.swad.swadroid/databases/";
	private static String DB_NAME = "swadroid";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "SWADroidDB";
	private SQLiteDatabase swadroidDataBase;
	private final Context context;
	
	/**
	 * Constructor
	 * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
	 * @param context
	 */
	public DataBaseHelper(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
		this.context = context;
	}
	
	/**
	 * Creates a empty database on the system and rewrites it with your own database.
	 * @throws IOException
	 */
	public void createDataBase() throws IOException{
		boolean dbExist = checkDataBase();
		if(dbExist){
			//do nothing - database already exist
			Log.d("createDataBase", "Database already exists.");
		}else{
			//By calling this method and empty database will be created into the default system path
			//of your application so we are gonna be able to overwrite that database with our database.
			this.getReadableDatabase();
			try {
				Log.d("createDataBase", "Creating database...");
				copyDataBase();
				Log.d("createDataBase", "Database created successfully.");
			} catch (IOException e) {
				throw new Error(context.getString(R.string.errorCopyMsg_DB));
			}
		}
	}
	
	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * @return true if it exists, false if it doesn't
	 * @throws IOException 
	 */
	private boolean checkDataBase() throws IOException{
		SQLiteDatabase checkDB = null;
		try{
			Log.d("checkDataBase", "Checking database...");
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
			Log.d("checkDataBase", "Database already exists.");
		}catch(SQLiteException e){
			//database doesn't exist yet.
			Log.d("checkDataBase", "Database doesn't exist yet.");
			createDataBase();
		}

		if(checkDB != null){
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}
	
	/**
	 * Copies your database from your local assets-folder to the just created empty database in the
	 * system folder, from where it can be accessed and handled.
	 * This is done by transferring bytestream.
	 * @throws IOException
	 **/
	private void copyDataBase() throws IOException{
		Log.d("copyDataBase", "Copying database to destination...");
		
		//Open your local db as the input stream
		InputStream myInput = context.getAssets().open(DB_NAME);
		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;
		//Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);
		
		//transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer))>0){
			myOutput.write(buffer, 0, length);
		}
		
		//Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
		
		Log.d("copyDataBase", "Database successfully copied.");
	}
	
	public void openDataBase() throws SQLException, IOException {
		Log.d("openDataBase", "Opening database...");
		
		checkDataBase();
		
		//Open the database
		String myPath = DB_PATH + DB_NAME;
		swadroidDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
		
		Log.d("openDataBase", "Database successfully opened.");
	}
	
	@Override
	public synchronized void close() {
		if(swadroidDataBase != null)
			swadroidDataBase.close();
		super.close();
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		Log.w(TAG, context.getString(R.string.upgradeMsg_DB));
        //db.execSQL("");
        try {
			copyDataBase();
		} catch (IOException e) {
			Log.e(TAG, context.getString(R.string.errorCopyMsg_DB));
			e.printStackTrace();
		}
	}
	
	// Add your public helper methods to access and get content from the database.
	// You could return cursors by doing "return swadroidDataBase.query(....)" so it'd be easy
	// to you to create adapters for your views.
	
	/**
	 * Gets all rows of specified table
	 * @param table Table containing the rows
	 * @return A cursor pointing to the rows
	 */
	public Cursor getCursorAllRows(String table)
    {
        String query = "SELECT * FROM " + table;
        return swadroidDataBase.rawQuery(query, null);
    }
	
	/**
	 * Function to parse from Integer to Boolean
	 * @param n Integer to be parsed
	 * @return true if n==0, false in other case
	 */
	private boolean parseIntBool(int n) {
		return n==0 ? true : false;
	}
	
	/**
	 * Function to parse from String to Boolean
	 * @param s String to be parsed
	 * @return true if s equals "Y", false in other case
	 */
	private boolean parseStringBool(String s) {
		return s.equals("Y") ? true : false;
	}
	
	/**
	 * Function to parse from Boolean to Integer
	 * @param b Boolean to be parsed
	 * @return 1 if b==true, 0 in other case
	 */
	private int parseBoolInt(boolean b) {
		return b ? 1 : 0;
	}
	
	/**
	 * Function to parse from Boolean to String
	 * @param b Boolean to be parsed
	 * @return "Y" if b==true, "N" in other case
	 */
	private String parseBoolString(boolean b) {
		return b ? "Y" : "N";
	}
	
	/**
	 * Creates a Model's subclass object looking at the table selected
	 * @param table Table selected
	 * @param rows Cursor to the table rows
	 * @return A Model's subclass object
	 */
	private Model createObjectByTable(String table, Cursor rows) {
		Model o = null;
		
		if(table.equals(Global.DB_TABLE_COURSES)) {
			o = new Course((Integer) rows.getInt(0),
							(String) rows.getString(1));
		} else if(table.equals(Global.DB_TABLE_NOTICES)) {
			o = new Notice((Integer) rows.getInt(0),
							(Integer) rows.getInt(1),
							(String) rows.getString(2));
		} else if(table.equals(Global.DB_TABLE_STUDENTS)) {
			o = new Student((Integer) rows.getInt(0),
							(String) rows.getString(1),
							(String) rows.getString(2),
							(String) rows.getString(3),
							(String) rows.getString(4));
		} else if(table.equals(Global.DB_TABLE_TEST_ANSWERS)) {
			o = new TestAnswer((Integer) rows.getInt(0),
					(Boolean) parseIntBool(rows.getInt(1)),
					(String) rows.getString(2));
		} else if(table.equals(Global.DB_TABLE_TEST_QUESTIONS)) {
			o = new TestQuestion((Integer) rows.getInt(0),
					(String) rows.getString(1),
					(String) rows.getString(2),
					(Integer) rows.getInt(3),
					(Boolean) parseStringBool(rows.getString(4)),
					(Float) rows.getFloat(5));
		} else if(table.equals(Global.DB_TABLE_MSG_CONTENT)) {
			o = new MessageContent((Integer) rows.getInt(0),
					(String) rows.getString(1),
					(String) rows.getString(2),
					(Boolean) parseStringBool(rows.getString(3)),
					(Integer) rows.getInt(4));
		} else if(table.equals(Global.DB_TABLE_MSG_RCV)) {
			o = new MessageReceived((Integer) rows.getInt(0),
					(String) rows.getString(1),
					(String) rows.getString(2),
					(Integer) rows.getInt(3),
					(Boolean) parseStringBool(rows.getString(4)),
					(Boolean) parseStringBool(rows.getString(5)),
					(Boolean) parseStringBool(rows.getString(6)),
					(Boolean) parseStringBool(rows.getString(7)));
		} else if(table.equals(Global.DB_TABLE_MSG_SNT)) {
			o = new MessageSent((Integer) rows.getInt(0),
					(String) rows.getString(1),
					(String) rows.getString(2),
					(Boolean) parseStringBool(rows.getString(3)),
					(Integer) rows.getInt(4),
					(Integer) rows.getInt(5),
					(String) rows.getString(6));
		} else if(table.equals(Global.DB_TABLE_MARKS)) {
			o = new Mark((Integer) rows.getInt(0),
					(Integer) rows.getInt(1),
					(Integer) rows.getInt(2),
					(String) rows.getString(3),
					(Integer) rows.getInt(4),
					(Integer) rows.getInt(5));
		} else if(table.equals(Global.DB_TABLE_NOTICES_COURSES) ||
				table.equals(Global.DB_TABLE_STUDENTS_COURSES) ||
				table.equals(Global.DB_TABLE_TEST_QUESTIONS_COURSES)) {
			
			o = new PairTable<Integer, Integer>(table,
					(Integer) rows.getInt(0),
					(Integer) rows.getInt(1));
		}
		
		return o;
	}
	
	/**
	 * Gets all rows of specified table
	 * @param table Table containing the rows
	 * @return A list of Model's subclass objects
	 */
	public List<Model> getAllRows(String table)
    {
		List<Model> result = new ArrayList<Model>();		
        Cursor rows = getCursorAllRows(table);
        Model row = null;
        
        if(rows.moveToFirst()) {        	
        	do {
        		row = createObjectByTable(table, rows);
        		result.add(row);
        	} while (rows.moveToNext());
        }
        
        return result;
    }
	
	/**
	 * Inserts a course in database
	 * @param c Course to be inserted
	 */
	public void insertCourse(Course c)
    {
		String command = "INSERT INTO "
						+ Global.DB_TABLE_COURSES
						+ " (_id, name) VALUES ("
						+ c.getId() + ", \'"
						+ c.getName()
									+ "\')";
        Log.d("DB", command);
		swadroidDataBase.execSQL(command, null);
    }
	
	/**
	 * Inserts a notice in database
	 * @param n Notice to be inserted
	 */
	public void insertNotice(Notice n)
    {
		String command = "INSERT INTO "
						+ Global.DB_TABLE_NOTICES
						+ " (_id, timestamp, description) VALUES ("
						+ n.getId() + ", "
						+ n.getTimestamp() + ", \'"
						+ n.getDescription()
											+ "\')";
        
		swadroidDataBase.execSQL(command, null);
    }
	
	/**
	 * Inserts a student in database
	 * @param s Student to be inserted
	 */
	public void insertStudent(Student s)
    {
		String command = "INSERT INTO "
						+ Global.DB_TABLE_STUDENTS
						+ " (_id, dni, firstname, surname1, surname2) VALUES ("
						+ s.getId() + ", "
						+ s.getDni() + ", \'"
						+ s.getFirstName() + "\', \'"
						+ s.getSurname1() + "\', \'"
						+ s.getSurname2()
											+ "\')";
        
		swadroidDataBase.execSQL(command, null);
    }
	
	/**
	 * Inserts a test answer in database
	 * @param a test answer to be inserted
	 */
	public void insertTestAnswer(TestAnswer a)
    {
		String command = "INSERT INTO "
						+ Global.DB_TABLE_TEST_ANSWERS
						+ " (_id, answer, correct) VALUES ("
						+ a.getId() + ", \'"
						+ a.getAnswer() + "\', "
						+ parseBoolInt(a.getCorrect())
													+ ")";
        
		swadroidDataBase.execSQL(command, null);
    }
	
	/**
	 * Inserts a test question in database
	 * @param q test question to be inserted
	 */
	public void insertTestQuestion(TestQuestion q)
    {
		String command = "INSERT INTO "
						+ Global.DB_TABLE_TEST_QUESTIONS
						+ " (_id, anstype, numhits, question, score, shuffle) VALUES ("
						+ q.getId() + ", "
						+ q.getAnstype() + ", "
						+ q.getNumhits() + ", \'"
						+ q.getQuestion() + "\', "
						+ q.getScore() + ", "
						+ parseBoolString(q.getShuffle())
														+ ")";
        
		swadroidDataBase.execSQL(command, null);
    }
	
	/**
	 * Inserts a message in database
	 * @param m Message to be inserted
	 */
	public void insertMessageContent(MessageContent m)
    {
		String command = "INSERT INTO "
						+ Global.DB_TABLE_MSG_CONTENT
						+ " (msgcod, subject, content) VALUES ("
						+ m.getId() + ", \'"
						+ m.getSubject() + "\', \'"
						+ m.getContent()
										+ "\')";
        
		swadroidDataBase.execSQL(command, null);
    }
	
	/**
	 * Inserts a received message in database
	 * @param m Message to be inserted
	 */
	public void insertMessageReceived(MessageReceived m)
    {
		String command = "INSERT INTO "
						+ Global.DB_TABLE_MSG_RCV
						+ " (msgcod, usrcod, notified, open, replied, expanded) VALUES ("
						+ m.getId() + ", "
						+ m.getUsrcod() + ", "
						+ parseBoolString(m.isNotified()) + ", "
						+ parseBoolString(m.isOpen()) + ", "
						+ parseBoolString(m.isReplied()) + ", "
						+ parseBoolString(m.isExpanded())
														+ ")";
        
		swadroidDataBase.execSQL(command, null);
    }
	
	/**
	 * Inserts a sent message in database
	 * @param m Message to be inserted
	 */
	public void insertMessageSent(MessageSent m)
    {
		String command = "INSERT INTO "
						+ Global.DB_TABLE_MSG_SNT
						+ " (msgcod, crscod, usrcod, creattime, expanded) VALUES ("
						+ m.getId() + ", "
						+ m.getCrscod() + ", "
						+ m.getUsrcod() + ", "
						+ m.getCreattime() + ", "
						+ parseBoolString(m.isExpanded())
														+ ")";
        
		swadroidDataBase.execSQL(command, null);
    }
	
	/**
	 * Inserts a mark in database
	 * @param m Mark to be inserted
	 */
	public void insertMark(Mark m)
    {
		String command = "INSERT INTO "
						+ Global.DB_TABLE_MARKS
						+ " (crscod, grpcod, path, header, footer) VALUES ("
						+ m.getCrscod() + ", "
						+ m.getGrpcod() + ", "
						+ m.getPath() + ", "
						+ m.getHeader() + ", "
						+ m.getFooter()
											+ ")";
        
		swadroidDataBase.execSQL(command, null);
    }
	
	/**
	 * Inserts a relation in database
	 * @param p Relation to be inserted
	 */
	public void insertPairTable(PairTable<?, ?> p)
    {
		String command = "INSERT INTO ";
		String table = p.getTable();
    	command += table + " ";
    	
    	if(table.equals(Global.DB_TABLE_NOTICES_COURSES)) {
    		command +=  " (idcourse, idnotice) VALUES (";
    	} else if(table.equals(Global.DB_TABLE_STUDENTS_COURSES)) {
    		command +=  " (idcourse, idstudent) VALUES (";
    	} else if(table.equals(Global.DB_TABLE_TEST_QUESTIONS_COURSES)) {
    		command +=  " (crscod, qstcod) VALUES (";
    	}
    	
    	command += p.getFirst() + ", "
    			+ p.getSecond()
    							+ ")";
        
		swadroidDataBase.execSQL(command, null);
    }
}
