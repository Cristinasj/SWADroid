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
package es.ugr.swad.swadroid.modules.notifications;

import android.accounts.Account;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.bugsense.trace.BugSenseHandler;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import es.ugr.swad.swadroid.Constants;
import es.ugr.swad.swadroid.R;
import es.ugr.swad.swadroid.gui.AlertNotification;
import es.ugr.swad.swadroid.model.SWADNotification;
import es.ugr.swad.swadroid.modules.Module;

import org.ksoap2.serialization.SoapObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Notifications module for get user's notifications
 *
 * @author Juan Miguel Boyero Corral <juanmi1982@gmail.com>
 * @author Antonio Aguilera Malagon <aguilerin@gmail.com>
 * @author Helena Rodriguez Gijon <hrgijon@gmail.com>
 */
public class Notifications extends Module {
    /**
     * Max size to store notifications
     */
    private int SIZE_LIMIT;
    /**
     * Notifications adapter for showing the data
     */
    private NotificationsCursorAdapter adapter;
    /**
     * Cursor for database access
     */
    private Cursor dbCursor;
    /**
     * Cursor selection parameter
     */
    private final String selection = null;
    /**
     * Cursor orderby parameter
     */
    private final String orderby = "eventTime DESC";
    /**
     * Notifications counter
     */
    private int notifCount;
    /**
     * Error message returned by the synchronization service
     */
    private String errorMessage;
    /**
     * Unique identifier for notification alerts
     */
    private final int NOTIF_ALERT_ID = 1982;
    /**
     * Notifications tag name for Logcat
     */
    private static final String TAG = Constants.APP_TAG + " Notifications";
    /**
     * Account type
     */
    private static final String accountType = "es.ugr.swad.swadroid";
    /**
     * Synchronization authority
     */
    private static final String authority = "es.ugr.swad.swadroid.content";
    /**
     * Synchronization receiver
     */
    private static SyncReceiver receiver;
    /**
     * Synchronization account
     */
    private static Account account;
    /**
     * ListView click listener
     */
    private OnItemClickListener clickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long rowId) {
            //adapter.toggleContentVisibility(position);
            TextView code = (TextView) v.findViewById(R.id.eventCode);
            TextView type = (TextView) v.findViewById(R.id.eventType);
            TextView userPhoto = (TextView) v.findViewById(R.id.eventUserPhoto);
            TextView sender = (TextView) v.findViewById(R.id.eventSender);
            TextView course = (TextView) v.findViewById(R.id.eventLocation);
            TextView summary = (TextView) v.findViewById(R.id.eventSummary);
            TextView content = (TextView) v.findViewById(R.id.eventText);
            TextView date = (TextView) v.findViewById(R.id.eventDate);
            TextView time = (TextView) v.findViewById(R.id.eventTime);

            Intent activity = new Intent(getApplicationContext(), NotificationItem.class);
            activity.putExtra("notificationCode", code.getText().toString());
            activity.putExtra("notificationType", type.getText().toString());
            activity.putExtra("userPhoto", userPhoto.getText().toString());
            activity.putExtra("sender", sender.getText().toString());
            activity.putExtra("course", course.getText().toString());
            activity.putExtra("summary", summary.getText().toString());
            activity.putExtra("content", content.getText().toString());
            activity.putExtra("date", date.getText().toString());
            activity.putExtra("time", time.getText().toString());
            startActivity(activity);
        }
    };

    /**
     * Refreshes data on screen
     */
    private void refreshScreen() {
        //Refresh data on screen
        stopManagingCursor(dbCursor);
        dbCursor = dbHelper.getDb().getCursor(Constants.DB_TABLE_NOTIFICATIONS, selection, orderby);
        startManagingCursor(dbCursor);
        adapter.changeCursor(dbCursor);

        //TextView text = (TextView) this.findViewById(R.id.listText);
        //ListView list = (ListView) this.findViewById(R.id.listItems);
        PullToRefreshListView list = (PullToRefreshListView) this.findViewById(R.id.listItemsPullToRefresh);

        //If there are notifications to show, hide the empty notifications message and show the notifications list
        if (dbCursor.getCount() > 0) {
            //text.setVisibility(View.GONE);
            //list.setVisibility(View.VISIBLE);
            list.setAdapter(adapter);
            list.setOnItemClickListener(clickListener);
        	//list.setDividerHeight(1);
        }        

    	list.onRefreshComplete();
    }

    /* (non-Javadoc)
     * @see es.ugr.swad.swadroid.modules.Module#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //ImageButton updateButton;
        ImageView image;
        TextView text;
        //ListView list;
        final PullToRefreshListView list;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_items_pulltorefresh);

        this.findViewById(R.id.courseSelectedText).setVisibility(View.GONE);
        this.findViewById(R.id.groupSpinner).setVisibility(View.GONE);

        image = (ImageView) this.findViewById(R.id.moduleIcon);
        image.setBackgroundResource(R.drawable.bell);

        text = (TextView) this.findViewById(R.id.moduleName);
        text.setText(R.string.notificationsModuleLabel);

        //image = (ImageView)this.findViewById(R.id.title_sep_1);
        //image.setVisibility(View.VISIBLE);

        //updateButton = (ImageButton) this.findViewById(R.id.refresh);
        //updateButton.setVisibility(View.VISIBLE);

        dbCursor = dbHelper.getDb().getCursor(Constants.DB_TABLE_NOTIFICATIONS, selection, orderby);
        startManagingCursor(dbCursor);
        adapter = new NotificationsCursorAdapter(this, dbCursor, prefs.getDBKey());

        //list = (ListView) this.findViewById(R.id.listItems);
        list = (PullToRefreshListView) this.findViewById(R.id.listItemsPullToRefresh);
        list.setAdapter(adapter);
        list.setOnItemClickListener(clickListener);
        list.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                runConnection();
            }
        });

        text = (TextView) this.findViewById(R.id.listText);

		/*
         * If there aren't notifications to show, hide the notifications list and show the empty notifications
		 * message
		 */
        if (dbCursor.getCount() == 0) {
            //list.setVisibility(View.GONE);
            //text.setVisibility(View.VISIBLE);
        	String emptyMsgArray[]={getString(R.string.notificationsEmptyListMsg)};
        	ArrayAdapter<String> adapter =new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, emptyMsgArray);
        	list.setAdapter(adapter);
        	list.setOnItemClickListener(null);
        	//list.setDividerHeight(0);
        }

        setMETHOD_NAME("getNotifications");
        receiver = new SyncReceiver(this);
        account = new Account(getString(R.string.app_name), accountType);
        SIZE_LIMIT = prefs.getNotifLimit();
    }

    /**
     * Launches an action when refresh button is pushed
     *
     * @param v Actual view
     */
    public void onRefreshClick(View v) {
        ImageButton updateButton = (ImageButton) this.findViewById(R.id.refresh);
        ProgressBar pb = (ProgressBar) this.findViewById(R.id.progress_refresh);

        updateButton.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);

        runConnection();
        if (!isConnected)
            onError();
    }

    /* (non-Javadoc)
     * @see es.ugr.swad.swadroid.modules.Module#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NotificationsSyncAdapterService.START_SYNC);
        intentFilter.addAction(NotificationsSyncAdapterService.STOP_SYNC);
        registerReceiver(receiver, intentFilter);

        refreshScreen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    /* (non-Javadoc)
     * @see es.ugr.swad.swadroid.modules.Module#requestService()
     */
    @Override
    protected void requestService() throws NoSuchAlgorithmException,
            IOException, XmlPullParserException {
        SIZE_LIMIT = prefs.getNotifLimit();

        account = new Account(getString(R.string.app_name), accountType);
        if (ContentResolver.getSyncAutomatically(account, authority)) {
            //Call synchronization service
            ContentResolver.requestSync(account, authority, new Bundle());
        } else {
            //Calculates next timestamp to be requested
            Long timestamp = Long.valueOf(dbHelper.getFieldOfLastNotification("eventTime"));
            timestamp++;

            //Creates webservice request, adds required params and sends request to webservice
            createRequest();
            addParam("wsKey", Constants.getLoggedUser().getWsKey());
            addParam("beginTime", timestamp);
            sendRequest(SWADNotification.class, false);

            if (result != null) {
                dbHelper.beginTransaction();

                //Stores notifications data returned by webservice response
                ArrayList<?> res = new ArrayList<Object>((Vector<?>) result);
                SoapObject soap = (SoapObject) res.get(1);
                notifCount = soap.getPropertyCount();
                for (int i = 0; i < notifCount; i++) {
                    SoapObject pii = (SoapObject) soap.getProperty(i);
                    Long notificationCode = Long.valueOf(pii.getProperty("notificationCode").toString());
                    String eventType = pii.getProperty("eventType").toString();
                    Long eventTime = Long.valueOf(pii.getProperty("eventTime").toString());
                    String userSurname1 = pii.getProperty("userSurname1").toString();
                    String userSurname2 = pii.getProperty("userSurname2").toString();
                    String userFirstName = pii.getProperty("userFirstname").toString();
                    String userPhoto = pii.getProperty("userPhoto").toString();
                    String location = pii.getProperty("location").toString();
                    String summary = pii.getProperty("summary").toString();
                    Integer status = Integer.valueOf(pii.getProperty("status").toString());
                    String content = pii.getProperty("content").toString();
                    
                    //TODO Add "notification seen" info from SWAD
                    SWADNotification n = new SWADNotification(notificationCode, eventType, eventTime, userSurname1, userSurname2, userFirstName, userPhoto, location, summary, status, content, false, false);
                    dbHelper.insertNotification(n);

                    if(isDebuggable)
                    	Log.d(TAG, n.toString());
                }

                //Request finalized without errors
                Log.i(TAG, "Retrieved " + notifCount + " notifications");

                //Clear old notifications to control database size
                dbHelper.clearOldNotifications(SIZE_LIMIT);

                dbHelper.endTransaction();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see es.ugr.swad.swadroid.modules.Module#connect()
     */
    @Override
    protected void connect() {
        String progressDescription = getString(R.string.notificationsProgressDescription);
        int progressTitle = R.string.notificationsProgressTitle;

        startConnection(false, progressDescription, progressTitle);
    }

    /* (non-Javadoc)
     * @see es.ugr.swad.swadroid.modules.Module#postConnect()
     */
    @Override
    protected void postConnect() {
        refreshScreen();
        //Toast.makeText(this, R.string.notificationsDownloadedMsg, Toast.LENGTH_SHORT).show();

        if (!ContentResolver.getSyncAutomatically(account, authority)) {
        	if (notifCount > 0) {
	            //If the notifications counter exceeds the limit, set it to the max allowed
	            if (notifCount > SIZE_LIMIT) {
	                notifCount = SIZE_LIMIT;
	            }
	            
	            AlertNotification.alertNotif(getApplicationContext(),
	            		NOTIF_ALERT_ID,
	            		getString(R.string.app_name),
	            		notifCount + " " + getString(R.string.notificationsAlertMsg),
	            		getString(R.string.app_name));
        	}

            ProgressBar pb = (ProgressBar) this.findViewById(R.id.progress_refresh);
            ImageButton updateButton = (ImageButton) this.findViewById(R.id.refresh);

            pb.setVisibility(View.GONE);
            updateButton.setVisibility(View.VISIBLE);
        }
    }

    /* (non-Javadoc)
     * @see es.ugr.swad.swadroid.modules.Module#onError()
     */
    @Override
    protected void onError() {
        ProgressBar pb = (ProgressBar) this.findViewById(R.id.progress_refresh);
        ImageButton updateButton = (ImageButton) this.findViewById(R.id.refresh);

        pb.setVisibility(View.GONE);
        updateButton.setVisibility(View.VISIBLE);
    }

    /**
     * Removes all notifications from database
     *
     * @param context Database context
     */
    public void clearNotifications(Context context) {
        try {
            dbHelper.emptyTable(Constants.DB_TABLE_NOTIFICATIONS);
        } catch (Exception e) {
            e.printStackTrace();

            //Send exception details to Bugsense
            if (!isDebuggable) {
                BugSenseHandler.sendException(e);
            }
        }
    }

    /**
     * Synchronization callback. Is called when synchronization starts and stops
     *
     * @author Juan Miguel Boyero Corral <juanmi1982@gmail.com>
     */
    private class SyncReceiver extends BroadcastReceiver {
        //private final Notifications mActivity;

        public SyncReceiver(Notifications activity) {
            //mActivity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(NotificationsSyncAdapterService.START_SYNC)) {
                Log.i(TAG, "Started sync");
            } else if (intent.getAction().equals(NotificationsSyncAdapterService.STOP_SYNC)) {
                Log.i(TAG, "Stopped sync");

                notifCount = intent.getIntExtra("notifCount", 0);
                errorMessage = intent.getStringExtra("errorMessage");
                if((errorMessage != null) && !errorMessage.equals("")) {
                	error(TAG, errorMessage, null, true);
                } else if (notifCount == 0) {
                    Toast.makeText(context, R.string.NoNotificationsMsg, Toast.LENGTH_LONG).show();
                }

                //ProgressBar pb = (ProgressBar) mActivity.findViewById(R.id.progress_refresh);
                //ImageButton updateButton = (ImageButton) mActivity.findViewById(R.id.refresh);

                //pb.setVisibility(View.GONE);
                //updateButton.setVisibility(View.VISIBLE);

                refreshScreen();
            }
        }
    }
}
