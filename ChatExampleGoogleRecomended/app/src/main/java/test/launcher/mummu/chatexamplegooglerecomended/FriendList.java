package test.launcher.mummu.chatexamplegooglerecomended;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import test.launcher.mummu.chatexamplegooglerecomended.interfaces.IAppManager;
import test.launcher.mummu.chatexamplegooglerecomended.services.IMService;
import test.launcher.mummu.chatexamplegooglerecomended.tools.FriendController;
import test.launcher.mummu.chatexamplegooglerecomended.tools.NotificationBuilderIM;
import test.launcher.mummu.chatexamplegooglerecomended.types.FriendInfo;
import test.launcher.mummu.chatexamplegooglerecomended.types.STATUS;

public class FriendList extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final int ADD_NEW_FRIEND_ID = Menu.FIRST;
    private static final int EXIT_APP_ID = Menu.FIRST + 1;
    private IAppManager imService = null;
    private FriendListAdapter friendAdapter;
    private ListView listView;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(this, Messaging.class);
        FriendInfo friend = friendAdapter.getItem(position);
        if (friend.status == STATUS.ONLINE) {
            i.putExtra(FriendInfo.USERNAME, friend.userName);
            i.putExtra(FriendInfo.PORT, friend.port);
            i.putExtra(FriendInfo.IP, friend.ip);
            startActivity(i);
        } else {
            Toast.makeText(FriendList.this, R.string.user_offline, Toast.LENGTH_SHORT).show();
        }
    }

    private class FriendListAdapter extends BaseAdapter {
        class ViewHolder {
            TextView text;
            ImageView icon;
        }

        private LayoutInflater mInflater;
        private Bitmap mOnlineIcon;
        private Bitmap mOfflineIcon;

        private FriendInfo[] friends = null;


        public FriendListAdapter(Context context) {
            super();

            mInflater = LayoutInflater.from(context);

            mOnlineIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.greenstar);
            mOfflineIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.redstar);

        }

        public void setFriendList(FriendInfo[] friends) {
            this.friends = friends;
        }


        public int getCount() {

            return friends.length;
        }

        public FriendInfo getItem(int position) {

            return friends[position];
        }

        public long getItemId(int position) {

            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.friend_list_screen, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            holder.text.setText(friends[position].userName);
            holder.icon.setImageBitmap(friends[position].status == STATUS.ONLINE ? mOnlineIcon : mOfflineIcon);

            return convertView;
        }

    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i("Broadcast receiver ", "received a message");
            Bundle extra = intent.getExtras();
            if (extra != null) {
                String action = intent.getAction();
                if (action.equals(IMService.FRIEND_LIST_UPDATED)) {
                    // taking friend List from broadcast
                    //String rawFriendList = extra.getString(FriendInfo.FRIEND_LIST);
                    //FriendList.this.parseFriendInfo(rawFriendList);
                    FriendList.this.updateData(FriendController.getFriendsInfo(),
                            FriendController.getUnapprovedFriendsInfo());

                }
            }
        }

    }

    ;
    public MessageReceiver messageReceiver = new MessageReceiver();

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            imService = ((IMService.IMBinder) service).getService();

            FriendInfo[] friends = FriendController.getFriendsInfo(); //imService.getLastRawFriendList();
            if (friends != null) {
                FriendList.this.updateData(friends, null); // parseFriendInfo(friendList);
            }

            setTitle(imService.getUsername() + "'s friend list");
        }

        public void onServiceDisconnected(ComponentName className) {
            imService = null;
            Toast.makeText(FriendList.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list_screen);
        listView = (ListView) findViewById(R.id.list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);


        friendAdapter = new FriendListAdapter(this);
        listView.setOnItemClickListener(this);


    }

    public void updateData(FriendInfo[] friends, FriendInfo[] unApprovedFriends) {
        if (friends != null) {
            friendAdapter.setFriendList(friends);
            listView.setAdapter(friendAdapter);
        }

        if (unApprovedFriends != null) {
            NotificationManager NM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (unApprovedFriends.length > 0) {
                String tmp = new String();
                for (int j = 0; j < unApprovedFriends.length; j++) {
                    tmp = tmp.concat(unApprovedFriends[j].userName).concat(",");

                }
                Log.d("TAG", "updateData: " + tmp);
                Intent i = new Intent(this, UnApprovedFriendList.class);
                i.putExtra(FriendInfo.FRIEND_LIST, tmp);
                NotificationBuilderIM instance = NotificationBuilderIM.getInstance(this);
                instance.CreateNotification("You have new friend request(s)", "Please open", i, 0);
//                Notification notification = new Notification(R.drawable.stat_sample,
//                        getText(R.string.new_friend_request_exist),
//                        System.currentTimeMillis());
//
//
//
//                PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                        i, 0);
//
//
//                notification.contentIntent = contentIntent;
//                notification.tickerText = "You have new friend request(s)";
//
//
////				NM.notify(R.string.new_friend_request_exist, notification);
//                Intent i = new Intent(this, UnApprovedFriendList.class);
//                i.putExtra(FriendInfo.FRIEND_LIST, tmp);
//                startActivity(i);
            } else {
                // if any request exists, then cancel it
                NM.cancel(R.string.new_friend_request_exist);
            }
        }

    }


    @Override
    protected void onPause() {
        unregisterReceiver(messageReceiver);
        unbindService(mConnection);
        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();
        bindService(new Intent(FriendList.this, IMService.class), mConnection, Context.BIND_AUTO_CREATE);

        IntentFilter i = new IntentFilter();
        //i.addAction(IMService.TAKE_MESSAGE);
        i.addAction(IMService.FRIEND_LIST_UPDATED);

        registerReceiver(messageReceiver, i);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.edit_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addMenu: {
                Intent i = new Intent(FriendList.this, AddFriend.class);
                startActivity(i);
                return true;
            }
            case R.id.exitMenu: {
                imService.exit();
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);


    }
}
