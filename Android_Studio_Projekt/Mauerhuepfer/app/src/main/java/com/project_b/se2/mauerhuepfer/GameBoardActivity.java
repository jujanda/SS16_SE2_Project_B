package com.project_b.se2.mauerhuepfer;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.project_b.se2.mauerhuepfer.interfaces.INetworkManager;
import com.project_b.se2.mauerhuepfer.interfaces.IReceiveMessage;
import com.project_b.se2.mauerhuepfer.listener.ShakeDetector;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GameBoardActivity extends AppCompatActivity implements IReceiveMessage {

    private Game game;
    private Dice dice;
    private int numberOfPlayers;
    private int playerID;
    private String playerName;
    private INetworkManager mNetworkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_game_board);
        mNetworkManager = NetworkActivity.getmNetworkManager();

        if (mNetworkManager != null) {
            mNetworkManager.addMessageReceiverListener(this);
            Bundle b = getIntent().getExtras();
            if (b != null) {
                playerID = b.getInt("playerID");
                playerName = b.getString("playerName");
                numberOfPlayers = b.getInt("numberOfPlayers");
                //dice.infoText.setText(playerName + " du bist Spieler " + playerID);
            }

            // start a new game
            this.game = new Game(this, numberOfPlayers, mNetworkManager, playerID);
            dice = game.getDice();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        dice.getmSensorManager().registerListener(dice.getmSensorListener(),
                dice.getmSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        dice.getmSensorManager().unregisterListener(dice.getmSensorListener());
        super.onPause();
    }

    @Override
    public void receiveMessage(UpdateState status) {
        if (status != null) {
            game.handleUpdate(status);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d("GAME", "onBackPressed");
        // Do nothing
    }

    @Override
    public void onDestroy() {
        mNetworkManager.disconnect();
        super.onDestroy();
    }
}