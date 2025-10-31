package com.space.ship.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.Random;

// سفینه فضایی
class SpaceShip {
    private float x, y;
    private float velocityX, velocityY;
    private float maxSpeed = 8f;
    private float acceleration = 0.4f;
    private float friction = 0.92f;
    private int health = 100;
    private int screenX, screenY;
    private Random random = new Random();
    
    public SpaceShip(float startX, float startY, int screenX, int screenY) {
        this.x = startX;
        this.y = startY;
        this.screenX = screenX;
        this.screenY = screenY;
    }
    
    public void update(VirtualJoystick joystick) {
        // اعمال نیروی جویستیک
        if (joystick.isActive()) {
            velocityX += joystick.getForceX() * acceleration;
            velocityY += joystick.getForceY() * acceleration;
            
            // محدود کردن سرعت
            float speed = (float)Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            if (speed > maxSpeed) {
                velocityX = (velocityX / speed) * maxSpeed;
                velocityY = (velocityY / speed) * maxSpeed;
            }
        }
        
        // اعمال اصطکاک
        velocityX *= friction;
        velocityY *= friction;
        
        // بروزرسانی موقعیت
        x += velocityX;
        y += velocityY;
        
        // محدود کردن به صفحه
        x = Math.max(30, Math.min(screenX - 30, x));
        y = Math.max(30, Math.min(screenY - 30, y));
    }
    
    public void draw(Canvas canvas, Paint paint) {
        // بدنه اصلی سفینه
        paint.setColor(Color.argb(255, 0, 200, 255));
        canvas.drawCircle(x, y, 25, paint);
        
        // کابین خلبان
        paint.setColor(Color.argb(255, 200, 230, 255));
        canvas.drawCircle(x, y, 15, paint);
        
        // باله‌ها
        paint.setColor(Color.argb(255, 0, 150, 220));
        canvas.drawRect(x - 35, y - 8, x - 25, y + 8, paint);
        canvas.drawRect(x + 25, y - 8, x + 35, y + 8, paint);
        
        // موتورها (با توجه به حرکت)
        drawEngines(canvas, paint);
    }
    
    private void drawEngines(Canvas canvas, Paint paint) {
        float enginePower = (Math.abs(velocityX) + Math.abs(velocityY)) / maxSpeed;
        
        // موتور چپ
        paint.setColor(Color.argb(255, 255, (int)(150 + enginePower * 105), 0));
        canvas.drawRect(x - 35, y - 5, x - 40 - enginePower * 10, y + 5, paint);
        
        // موتور راست
        paint.setColor(Color.argb(255, 255, (int)(150 + enginePower * 105), 0));
        canvas.drawRect(x + 35, y - 5, x + 40 + enginePower * 10, y + 5, paint);
    }
    
    public boolean checkCollision(GameObject other) {
        float dx = x - other.getX();
        float dy = y - other.getY();
        float distance = (float)Math.sqrt(dx * dx + dy * dy);
        return distance < (25 + other.getRadius());
    }
    
    public void takeDamage(int damage) {
        health = Math.max(0, health - damage);
    }
    
    public void reset(float newX, float newY) {
        x = newX;
        y = newY;
        velocityX = 0;
        velocityY = 0;
        health = 100;
    }
    
    // گترها
    public float getX() { return x; }
    public float getY() { return y; }
    public int getHealth() { return health; }
}

// سیاره
class Planet extends GameObject {
    private int health;
    private int maxHealth;
    private int screenX, screenY;
    private Random random = new Random();
    private float rotation = 0;
    
    public Planet(float x, float y, int health, int screenX, int screenY) {
        super(x, y, 60);
        this.health = health;
        this.maxHealth = health;
        this.screenX = screenX;
        this.screenY = screenY;
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) {
        rotation += 0.5f;
        
        // محاسبه رنگ بر اساس سلامت
        float healthRatio = (float)health / maxHealth;
        int red = (int)(255 * (1 - healthRatio));
        int green = (int)(255 * healthRatio);
        int blue = (int)(100 * healthRatio);
        
        // بدنه اصلی سیاره
        paint.setColor(Color.argb(255, red, green, blue));
        canvas.drawCircle(x, y, radius, paint);
        
        // جزئیات سطح سیاره
        drawPlanetDetails(canvas, paint, healthRatio);
        
        // نمایش سلامت
        paint.setColor(Color.WHITE);
        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(health), x, y + 8, paint);
    }
    
    private void drawPlanetDetails(Canvas canvas, Paint paint, float healthRatio) {
        paint.setColor(Color.argb(150, 50, 50, 50));
        
        // حلقه‌های سیاره
        for (int i = 0; i < 3; i++) {
            float angle = rotation + i * 120;
            float ringX = x + (float)Math.cos(Math.toRadians(angle)) * radius * 0.7f;
            float ringY = y + (float)Math.sin(Math.toRadians(angle)) * radius * 0.7f;
            canvas.drawCircle(ringX, ringY, radius * 0.3f, paint);
        }
        
        // ابرهای سیاره
        if (healthRatio > 0.3f) {
            paint.setColor(Color.argb(100, 255, 255, 255));
            for (int i = 0; i < 4; i++) {
                float cloudX = x + (float)Math.cos(Math.toRadians(rotation * 2 + i * 90)) * radius * 0.5f;
                float cloudY = y + (float)Math.sin(Math.toRadians(rotation * 2 + i * 90)) * radius * 0.5f;
                canvas.drawCircle(cloudX, cloudY, radius * 0.2f, paint);
            }
        }
    }
    
    public void takeDamage(int damage) {
        health = Math.max(0, health - damage);
    }
    
    public boolean isDestroyed() {
        return health <= 0;
    }
}

// دشمن
class Enemy extends GameObject {
    private float velocityX, velocityY;
    private int screenX, screenY;
    private int level;
    private Random random = new Random();
    private float rotation = 0;
    
    public Enemy(int screenX, int screenY, int level) {
        super(0, 0, 35);
        this.screenX = screenX;
        this.screenY = screenY;
        this.level = level;
        initializePosition();
    }
    
    private void initializePosition() {
        int side = random.nextInt(4);
        float speed = 2 + level * 0.5f;
        
        switch (side) {
            case 0: // بالا
                x = random.nextFloat() * screenX;
                y = -radius;
                velocityX = (random.nextFloat() - 0.5f) * speed;
                velocityY = speed;
                break;
            case 1: // راست
                x = screenX + radius;
                y = random.nextFloat() * screenY;
                velocityX = -speed;
                velocityY = (random.nextFloat() - 0.5f) * speed;
                break;
            case 2: // پایین
                x = random.nextFloat() * screenX;
                y = screenY + radius;
                velocityX = (random.nextFloat() - 0.5f) * speed;
                velocityY = -speed;
                break;
            case 3: // چپ
                x = -radius;
                y = random.nextFloat() * screenY;
                velocityX = speed;
                velocityY = (random.nextFloat() - 0.5f) * speed;
                break;
        }
    }
    
    public void update(SpaceShip ship) {
        // حرکت به سمت سفینه با هوش مصنوعی ساده
        float dx = ship.getX() - x;
        float dy = ship.getY() - y;
        float distance = (float)Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0) {
            float baseSpeed = 2 + level * 0.3f;
            velocityX += (dx / distance) * 0.1f * baseSpeed;
            velocityY += (dy / distance) * 0.1f * baseSpeed;
            
            // محدود کردن سرعت
            float currentSpeed = (float)Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            float maxSpeed = 3 + level * 0.4f;
            if (currentSpeed > maxSpeed) {
                velocityX = (velocityX / currentSpeed) * maxSpeed;
                velocityY = (velocityY / currentSpeed) * maxSpeed;
            }
        }
        
        x += velocityX;
        y += velocityY;
        rotation += 5;
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) {
        // بدنه اصلی دشمن
        paint.setColor(Color.argb(255, 255, 50, 50));
        canvas.drawCircle(x, y, radius, paint);
        
        // جزئیات دشمن
        paint.setColor(Color.argb(255, 255, 150, 150));
        canvas.drawCircle(x, y, radius - 8, paint);
        
        // خارهای دشمن
        paint.setColor(Color.argb(255, 200, 0, 0));
        for (int i = 0; i < 8; i++) {
            float angle = rotation + i * 45;
            float spikeX = x + (float)Math.cos(Math.toRadians(angle)) * radius;
            float spikeY = y + (float)Math.sin(Math.toRadians(angle)) * radius;
            canvas.drawCircle(spikeX, spikeY, 5, paint);
        }
    }
    
    public boolean isOutOfScreen() {
        return x < -100 || x > screenX + 100 || y < -100 || y > screenY + 100;
    }
}

// جویستیک مجازی
class VirtualJoystick {
    private float centerX, centerY;
    private float baseRadius, handleRadius;
    private float handleX, handleY;
    private boolean isActive = false;
    
    public VirtualJoystick(float centerX, float centerY, float baseRadius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.baseRadius = baseRadius;
        this.handleRadius = baseRadius * 0.4f;
        resetHandle();
    }
    
    public void setActive(boolean active, float touchX, float touchY) {
        isActive = active;
        if (active) {
            // محاسبه موقعیت دسته با محدودیت دایره
            float dx = touchX - centerX;
            float dy = touchY - centerY;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);
            
            if (distance <= baseRadius) {
                handleX = touchX;
                handleY = touchY;
            } else {
                handleX = centerX + (dx / distance) * baseRadius;
                handleY = centerY + (dy / distance) * baseRadius;
            }
        } else {
            resetHandle();
        }
    }
    
    private void resetHandle() {
        handleX = centerX;
        handleY = centerY;
    }
    
    public void draw(Canvas canvas, Paint paint) {
        // پایه جویستیک
        paint.setColor(Color.argb(150, 100, 100, 100));
        canvas.drawCircle(centerX, centerY, baseRadius, paint);
        
        // دسته جویستیک
        paint.setColor(Color.argb(200, 200, 200, 200));
        canvas.drawCircle(handleX, handleY, handleRadius, paint);
        
        // مرکز دسته
        paint.setColor(Color.argb(255, 100, 100, 100));
        canvas.drawCircle(handleX, handleY, handleRadius * 0.5f, paint);
    }
    
    public float getForceX() {
        return (handleX - centerX) / baseRadius;
    }
    
    public float getForceY() {
        return (handleY - centerY) / baseRadius;
    }
    
    public boolean isActive() {
        return isActive;
    }
}

// پایه کلاس برای اشیاء بازی
abstract class GameObject {
    protected float x, y;
    protected int radius;
    
    public GameObject(float x, float y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
    
    public abstract void draw(Canvas canvas, Paint paint);
    
    public float getX() { return x; }
    public float getY() { return y; }
    public int getRadius() { return radius; }
    }
