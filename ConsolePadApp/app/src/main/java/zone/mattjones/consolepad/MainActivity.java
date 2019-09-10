/**
 * File: MainActivity.js
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
import android.widget.ImageView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ArrayList<ConsoleButtonInfo> actionItems = new ArrayList<>();
        actionItems.add(new ConsoleButtonInfo(
                R.drawable.house, R.string.button_home, "KillEmulators"));
        actionItems.add(new ConsoleButtonInfo(
                R.drawable.emulationstation, R.string.button_emulationstation,
                "RestartEmulationStation"));
        actionItems.add(new ConsoleButtonInfo(
                R.drawable.steam, R.string.button_steam, "RestartSteamBP"));
        actionItems.add(new ConsoleButtonInfo(
                R.drawable.zzz, R.string.button_power_off, "PowerOff"));


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

                ((ImageView) button.findViewById(R.id.button_icon)).setImageDrawable(
                        getDrawable(item.imageId));
                ((TextView) button.findViewById(R.id.button_label)).setText(item.labelId);
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
        UdpNetworkTask task =
                new UdpNetworkTask(this, UdpNetworkTask.BROADCAST_IP, UdpNetworkTask.ACTION_INFO);
        task.execute();
    }

    @Override
    public void handleResponse(ArrayList<String> messageParts, boolean error, boolean timedOut) {
        mCurrentNetTask = null;
        if (error) {
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.generic_console_error, Toast.LENGTH_LONG).show();
            });
            return;
        }

        // Decode the message. The message is always the last component of the data.
        byte[] decodedMessage =
                Base64.getDecoder().decode(messageParts.get(messageParts.size() - 1));
        String decodedMessageString = new String(decodedMessage, Charset.forName("UTF8"));

        runOnUiThread(() -> {

            Toast.makeText(this, decodedMessageString, Toast.LENGTH_LONG).show();
        });
    }
}
