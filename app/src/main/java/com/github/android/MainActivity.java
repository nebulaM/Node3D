package com.github.android;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.github.android.GameDrawing.OpenGLES20Activity;
import com.github.android.nodeEdge3D.R;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void startNewGame(View view){
        Intent intent = new Intent(this, OpenGLES20Activity.class);

        startActivity(intent);
    }

}
