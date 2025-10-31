package com.space.ship.game;

import android.content.Context;
import android.content.SharedPreferences;

public class GameState {
    private static final String PREFS_NAME = "SpaceShipGame";
    private static final String KEY_COINS = "coins";
    private static final String KEY_SCORE = "score";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_HIGH_SCORE = "high_score";
    
    private long coins;
    private int score;
    private int highScore;
    private int currentLevel;
    
    public GameState() {
        // شروع با 1 میلیون سکه
        coins = 1000000;
        score = 0;
        highScore = 0;
        currentLevel = 1;
    }
    
    public void planetDestroyed(int level) {
        score += level * 100;
        coins += level * 50000;
        
        if (score > highScore) {
            highScore = score;
        }
    }
    
    public void shipDestroyed() {
        score = Math.max(0, score - 50);
        coins = Math.max(1000000, coins - 100000);
    }
    
    public void levelCompleted(int newLevel) {
        coins += newLevel * 1000000; // 1 میلیون سکه به ازای هر سطح
        score += newLevel * 1000;
    }
    
    public void addCoins(long amount) {
        coins += amount;
    }
    
    public boolean spendCoins(long amount) {
        if (coins >= amount) {
            coins -= amount;
            return true;
        }
        return false;
    }
    
    // ذخیره و بازیابی بازی
    public void saveGame(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_COINS, coins);
        editor.putInt(KEY_SCORE, score);
        editor.putInt(KEY_HIGH_SCORE, highScore);
        editor.putInt(KEY_LEVEL, currentLevel);
        editor.apply();
    }
    
    public void loadGame(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        coins = prefs.getLong(KEY_COINS, 1000000);
        score = prefs.getInt(KEY_SCORE, 0);
        highScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        currentLevel = prefs.getInt(KEY_LEVEL, 1);
    }
    
    // گترها
    public long getCoins() { return coins; }
    public int getScore() { return score; }
    public int getHighScore() { return highScore; }
    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int level) { currentLevel = level; }
}
