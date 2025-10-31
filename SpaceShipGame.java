package com.space.ship.game;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.graphics.Point;
import android.view.Display;

public class SpaceShipGame extends Activity {
    private GameEngine gameEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // حالت تمام صفحه و نگه داشتن صفحه روشن
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                           WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // دریافت ابعاد صفحه
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        
        // ایجاد موتور بازی
        gameEngine = new GameEngine(this, size.x, size.y);
        setContentView(gameEngine);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameEngine.pauseGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameEngine.resumeGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameEngine.destroyGame();
    }
}
