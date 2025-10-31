package com.space.ship.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class GameEngine extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Thread gameThread;
    private volatile boolean playing;
    private SurfaceHolder surfaceHolder;
    private Canvas canvas;
    private Paint paint;
    
    // ابعاد صفحه
    private int screenX, screenY;
    
    // بازی‌ابزار
    private SpaceShip spaceShip;
    private VirtualJoystick joystick;
    private List<Planet> planets;
    private List<Enemy> enemies;
    private List<Particle> particles;
    private GameState gameState;
    private LevelManager levelManager;
    private ParticleSystem particleSystem;
    private Random random;
    
    // زمان‌سنج
    private long lastTime;
    private int fps;

    public GameEngine(Context context, int screenX, int screenY) {
        super(context);
        this.screenX = screenX;
        this.screenY = screenY;
        
        initializeEngine();
    }

    private void initializeEngine() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        
        paint = new Paint();
        paint.setAntiAlias(true);
        random = new Random();
        
        // ایجاد اجزای بازی
        spaceShip = new SpaceShip(screenX / 2, screenY / 2, screenX, screenY);
        joystick = new VirtualJoystick(screenX / 2, screenY - 200, 120);
        planets = new ArrayList<>();
        enemies = new ArrayList<>();
        particles = new ArrayList<>();
        gameState = new GameState();
        levelManager = new LevelManager();
        particleSystem = new ParticleSystem();
        
        startNewLevel();
        
        setFocusable(true);
    }

    private void startNewLevel() {
        planets.clear();
        enemies.clear();
        particles.clear();
        
        // ایجاد 20 سیاره
        for (int i = 0; i < 20; i++) {
            float x = random.nextFloat() * (screenX - 200) + 100;
            float y = random.nextFloat() * (screenHeight - 400) + 100;
            int health = levelManager.getCurrentLevel() * 10 + 50;
            planets.add(new Planet(x, y, health, screenX, screenY));
        }
        
        // ایجاد 10 دشمن
        for (int i = 0; i < 10; i++) {
            enemies.add(new Enemy(screenX, screenY, levelManager.getCurrentLevel()));
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
        lastTime = System.currentTimeMillis();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (playing) {
            update();
            draw();
            controlFPS();
        }
    }

    private void update() {
        // بروزرسانی سفینه
        spaceShip.update(joystick);
        
        // بروزرسانی دشمنان
        for (Enemy enemy : enemies) {
            enemy.update(spaceShip);
            
            // بررسی برخورد با سفینه
            if (spaceShip.checkCollision(enemy)) {
                particleSystem.createExplosion(spaceShip.getX(), spaceShip.getY(), 50);
                gameState.shipDestroyed();
                resetGame();
                return;
            }
        }
        
        // بررسی برخورد با سیارات
        for (int i = planets.size() - 1; i >= 0; i--) {
            Planet planet = planets.get(i);
            if (spaceShip.checkCollision(planet)) {
                planet.takeDamage(25);
                particleSystem.createImpact(planet.getX(), planet.getY(), 20);
                
                if (planet.isDestroyed()) {
                    planets.remove(i);
                    gameState.planetDestroyed(levelManager.getCurrentLevel());
                    particleSystem.createExplosion(planet.getX(), planet.getY(), 80);
                }
            }
        }
        
        // بروزرسانی ذرات
        particleSystem.update();
        
        // بررسی پایان مرحله
        if (planets.isEmpty()) {
            levelManager.nextLevel();
            gameState.levelCompleted(levelManager.getCurrentLevel());
            startNewLevel();
        }
        
        // حذف دشمنان خارج از صفحه
        enemies.removeIf(enemy -> enemy.isOutOfScreen());
        
        // اضافه کردن دشمنان جدید
        if (enemies.size() < 10 && random.nextInt(100) < 2) {
            enemies.add(new Enemy(screenX, screenY, levelManager.getCurrentLevel()));
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            
            // رسم پس زمینه کهکشان
            drawGalaxyBackground(canvas);
            
            // رسم ذرات
            particleSystem.draw(canvas, paint);
            
            // رسم سیارات
            for (Planet planet : planets) {
                planet.draw(canvas, paint);
            }
            
            // رسم دشمنان
            for (Enemy enemy : enemies) {
                enemy.draw(canvas, paint);
            }
            
            // رسم سفینه
            spaceShip.draw(canvas, paint);
            
            // رسم جویستیک
            joystick.draw(canvas, paint);
            
            // رسم رابط کاربری
            drawHUD(canvas);
            
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawGalaxyBackground(Canvas canvas) {
        // پس زمینه فضای عمیق
        canvas.drawColor(Color.argb(255, 5, 5, 20));
        
        // ستاره‌های درخشان
        drawStars(canvas);
        
        // سحابی‌های رنگی
        drawNebulas(canvas);
        
        // کهکشان‌های دوردست
        drawDistantGalaxies(canvas);
    }

    private void drawStars(Canvas canvas) {
        paint.setColor(Color.WHITE);
        for (int i = 0; i < 300; i++) {
            float x = random.nextFloat() * screenX;
            float y = random.nextFloat() * screenY;
            float radius = random.nextFloat() * 2.5f;
            float alpha = 150 + random.nextInt(105);
            paint.setAlpha((int)alpha);
            canvas.drawCircle(x, y, radius, paint);
        }
        
        // ستاره‌های درخشان بزرگ
        for (int i = 0; i < 20; i++) {
            float x = random.nextFloat() * screenX;
            float y = random.nextFloat() * screenY;
            paint.setColor(Color.argb(200, 255, 255, 200));
            canvas.drawCircle(x, y, 1.5f, paint);
            paint.setColor(Color.argb(100, 255, 255, 255));
            canvas.drawCircle(x, y, 3.5f, paint);
        }
    }

    private void drawNebulas(Canvas canvas) {
        // سحابی آبی-بنفش
        paint.setColor(Color.argb(30, 100, 80, 255));
        for (int i = 0; i < 5; i++) {
            float x = random.nextFloat() * screenX;
            float y = random.nextFloat() * screenY * 0.3f;
            canvas.drawCircle(x, y, 200 + random.nextInt(200), paint);
        }
        
        // سحابی قرمز-نارنجی
        paint.setColor(Color.argb(25, 255, 100, 50));
        for (int i = 0; i < 3; i++) {
            float x = screenX * 0.7f + random.nextFloat() * screenX * 0.3f;
            float y = screenY * 0.6f + random.nextFloat() * screenY * 0.4f;
            canvas.drawCircle(x, y, 150 + random.nextInt(150), paint);
        }
    }

    private void drawDistantGalaxies(Canvas canvas) {
        paint.setColor(Color.argb(40, 200, 200, 255));
        for (int i = 0; i < 8; i++) {
            float x = random.nextFloat() * screenX;
            float y = random.nextFloat() * screenY;
            float size = 20 + random.nextFloat() * 50;
            canvas.drawCircle(x, y, size, paint);
            paint.setColor(Color.argb(20, 255, 255, 255));
            canvas.drawCircle(x, y, size * 1.5f, paint);
        }
    }

    private void drawHUD(Canvas canvas) {
        paint.setColor(Color.WHITE);
        paint.setTextSize(36);
        paint.setShadowLayer(3, 2, 2, Color.BLACK);
        
        // اطلاعات سطح
        canvas.drawText("LEVEL: " + levelManager.getCurrentLevel(), 30, 50, paint);
        canvas.drawText("PLANETS: " + planets.size() + "/20", 30, 100, paint);
        
        // اطلاعات امتیاز
        paint.setTextSize(32);
        canvas.drawText("SCORE: " + gameState.getScore(), screenX - 300, 50, paint);
        canvas.drawText("COINS: " + formatCoins(gameState.getCoins()), screenX - 300, 100, paint);
        
        // سلامت سفینه
        drawHealthBar(canvas);
        
        paint.setShadowLayer(0, 0, 0, 0);
    }

    private void drawHealthBar(Canvas canvas) {
        float healthPercent = spaceShip.getHealth() / 100.0f;
        float barWidth = 200;
        float barHeight = 20;
        float x = screenX - barWidth - 30;
        float y = screenY - 50;
        
        // پس‌زمینه سلامت
        paint.setColor(Color.argb(180, 100, 100, 100));
        canvas.drawRect(x, y, x + barWidth, y + barHeight, paint);
        
        // سلامت فعلی
        if (healthPercent > 0.7f) {
            paint.setColor(Color.argb(220, 0, 255, 0));
        } else if (healthPercent > 0.3f) {
            paint.setColor(Color.argb(220, 255, 255, 0));
        } else {
            paint.setColor(Color.argb(220, 255, 0, 0));
        }
        canvas.drawRect(x, y, x + (barWidth * healthPercent), y + barHeight, paint);
        
        // کادر سلامت
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRect(x, y, x + barWidth, y + barHeight, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private String formatCoins(long coins) {
        if (coins >= 1000000) {
            return String.format("%.1fM", coins / 1000000.0);
        } else if (coins >= 1000) {
            return String.format("%.1fK", coins / 1000.0);
        }
        return String.valueOf(coins);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (y > screenY - 400) { // منطقه جویستیک
                    joystick.setActive(true, x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
                joystick.setActive(false, x, y);
                break;
        }
        return true;
    }

    private void controlFPS() {
        try {
            long currentTime = System.currentTimeMillis();
            long sleepTime = 16 - (currentTime - lastTime); // ~60 FPS
            if (sleepTime > 0) {
                Thread.sleep(sleepTime);
            }
            lastTime = currentTime;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void resetGame() {
        spaceShip.reset(screenX / 2, screenY / 2);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pauseGame() {
        playing = false;
    }

    public void resumeGame() {
        if (!playing) {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    public void destroyGame() {
        playing = false;
        gameState.saveGame(getContext());
    }
                                               }
