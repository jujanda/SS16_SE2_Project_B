package com.project_b.se2.mauerhuepfer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener{

    Button newGameButton;
    private Button mLaunchButton;
    private Button mOptionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        newGameButton = (Button) findViewById(R.id.neuesSpiel);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), GameBoardActivity.class);
                startActivity(intent);
               // setContentView(R.layout.activity_game_board);
            }
        });
        setContentView(R.layout.activity_main);
        mLaunchButton = (Button) findViewById(R.id.buttonLaunch);
        mOptionButton = (Button) findViewById(R.id.buttonOption);
        findViewById(R.id.buttonLaunch).setOnClickListener(this);
        findViewById(R.id.buttonOption).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Intent myIntent;
        switch (v.getId()) {
            case R.id.buttonLaunch:
                myIntent = new Intent(MenuActivity.this, NetworkActivity.class);
                //myIntent.putExtra("key", value); //Optional parameters
                MenuActivity.this.startActivity(myIntent);
                break;
            case R.id.buttonOption:
                myIntent = new Intent(MenuActivity.this, OptionsActivity.class);
                //myIntent.putExtra("key", value); //Optional parameters
                MenuActivity.this.startActivity(myIntent);
                break;
        }
    }
}
