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

import java.util.ArrayList;

public class MainActivity extends Activity {
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

    }
}