/**
 * Copyright 2019 Matthew Jones
 *
 * File: MainActivity.java
 * Author: Matt Jones
 * Date: 2019.09.04
 * Desc: Main activity for the console controller phone app.
 */

package zone.mattjones.consolepad;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;

public class MainActivity extends Activity implements UdpNetworkTask.ResponseHandler {
    /** Information representing a button in the app. */
    public static class ConsoleButtonInfo {
        /** The resource ID of the image to use for the button icon. */
        public final int imageId;
        /** The ID of the text to place next to the icon. */
        public final int labelId;
        /** The ID of the action to perform on the host device. */
        public final String actionId;

        public ConsoleButtonInfo(int imageId, int labelId, String actionId) {
            this.imageId = imageId;
            this.labelId = labelId;
            this.actionId = actionId;
        }
    }

    /** The current network task. */
    private UdpNetworkTask mCurrentNetTask;

    /** The IP of the console. */
    private String mConsoleIp;

    /** The name of the console we are currently connected to. */
    private String mConnectedConsoleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ArrayList<ConsoleButtonInfo> actionItems = new ArrayList<>();
        actionItems.add(new ConsoleButtonInfo(
                R.drawable.house, R.string.button_home, UdpNetworkTask.ACTION_HOME));
        actionItems.add(new ConsoleButtonInfo(
                R.drawable.emulationstation, R.string.button_emulationstation,
                UdpNetworkTask.ACTION_EMULATION_STATION));
        actionItems.add(new ConsoleButtonInfo(
                R.drawable.steam, R.string.button_steam, UdpNetworkTask.ACTION_STEAM));
        actionItems.add(new ConsoleButtonInfo(
                R.drawable.zzz, R.string.button_power_off, UdpNetworkTask.ACTION_POWER_OFF));

        // Allow tapping on the status to make an info request and "connect".
        findViewById(R.id.connection_status).setOnClickListener((v) -> {
            handleButtonClick(new ConsoleButtonInfo(0, 0, UdpNetworkTask.ACTION_INFO));
        });

        ListView actionList = (ListView) findViewById(R.id.action_list);
        actionList.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return actionItems.size();
            }

            @Override
            public Object getItem(int i) {
                return actionItems.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                View button = getLayoutInflater().inflate(R.layout.button, null);
                ConsoleButtonInfo item = actionItems.get(i);

                ((LongPressButton) button).setButtonText(item.labelId);
                ((LongPressButton) button).setButtonIcon(item.imageId);
                button.setOnClickListener((v) -> handleButtonClick(item));

                return button;
            }
        });
    }

    /**
     * Handle button presses from the list of actions.
     * @param clickedItem The item that was clicked.
     */
    private void handleButtonClick(ConsoleButtonInfo clickedItem) {
        String ip = mConsoleIp == null ? UdpNetworkTask.BROADCAST_IP : mConsoleIp;
        UdpNetworkTask task = new UdpNetworkTask(this, ip, clickedItem.actionId);
        task.execute();
    }

    @Override
    public void handleResponse(
            ArrayList<String> messageParts, boolean error, boolean timedOut, String remoteIp) {
        mCurrentNetTask = null;

        // If there was an error, reset the connection info.
        if (error) {
            mConnectedConsoleName = null;
            mConsoleIp = null;
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.generic_console_error, Toast.LENGTH_LONG).show();
                updateConnectionStatusUi();
            });
            return;
        }

        // Decode the message. The message is always the last component of the data.
        byte[] decodedMessage =
                Base64.getDecoder().decode(messageParts.get(messageParts.size() - 1));
        String decodedMessageString = new String(decodedMessage, Charset.forName("UTF8"));

        if (mConsoleIp == null) {
            mConsoleIp = remoteIp;
            mConnectedConsoleName = messageParts.get(2);
            runOnUiThread(() -> {
                updateConnectionStatusUi();
            });
        }
    }

    /**
     * Update the piece of UI that shows the status of the connection between the app and the
     * console.
     */
    private void updateConnectionStatusUi() {
        TextView status = (TextView) findViewById(R.id.connection_status);
        if (mConnectedConsoleName == null) {
            status.setText(R.string.connection_status_none);
            status.setTextColor(
                    getResources().getColor(R.color.connection_status_none_color, null));
        } else {
            String base = getResources().getString(R.string.connection_status_connected);
            status.setText(base + " " + mConnectedConsoleName);
            status.setTextColor(getResources().getColor(R.color.purple_primary, null));
        }
    }
}
