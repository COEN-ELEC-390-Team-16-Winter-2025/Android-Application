package com.drinkwise.app.ui.soberup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class GameView extends View {

    private float platformX;
    private float platformWidth = 400;
    private float platformHeight = 50;
    private float platformY;
    private float screenWidth;
    private float screenHeight;

    private Paint platformPaint;
    private Paint ballPaint;
    private Paint textPaint;

    private List<Ball> balls = new ArrayList<>();
    private List<Ball> settledBalls = new ArrayList<>();
    private Handler handler = new Handler();

    private final float gravity = 5f;
    private boolean isGameOver = false;

    public GameView(Context context) {
        super(context);
        init();
    }

    private void init() {
        platformPaint = new Paint();
        platformPaint.setColor(Color.DKGRAY);

        ballPaint = new Paint();
        ballPaint.setColor(Color.RED);

        textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(100);
        textPaint.setTextAlign(Paint.Align.CENTER);

        startGame();
    }

    private void startGame() {
        isGameOver = false;
        balls.clear();
        settledBalls.clear();

        handler.removeCallbacks(addBallRunnable);
        balls.add(new Ball(randomX(), 100));

        handler.postDelayed(addBallRunnable, 2000);
    }

    private Runnable addBallRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isGameOver) {
                balls.add(new Ball(randomX(), 100));
                handler.postDelayed(this, 2000);
            }
        }
    };

    private float randomX() {
        return (float) (Math.random() * (screenWidth - Ball.RADIUS * 2)) + Ball.RADIUS;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        screenWidth = w;
        screenHeight = h;
        platformX = w / 2f;
        platformY = h - 200f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isGameOver) {
            canvas.drawColor(Color.WHITE);
            canvas.drawText("Game Over!", screenWidth / 2, screenHeight / 2, textPaint);
            canvas.drawText("Tap to restart", screenWidth / 2, screenHeight / 2 + 120, textPaint);
            return;
        }

        float left = platformX - platformWidth / 2;
        float right = platformX + platformWidth / 2;
        float bottom = platformY + platformHeight;

        // Draw bucket walls
        platformPaint.setColor(Color.DKGRAY);
        canvas.drawRect(left, platformY, right, bottom, platformPaint);
        canvas.drawRect(left - 10, platformY - 100, left, bottom, platformPaint);
        canvas.drawRect(right, platformY - 100, right + 10, bottom, platformPaint);

        // Update falling balls
        List<Ball> caughtBalls = new ArrayList<>();

        for (Ball ball : balls) {
            ball.update();

            boolean isCaught = ball.y + Ball.RADIUS >= platformY &&
                    ball.x >= left && ball.x <= right;

            if (isCaught) {
                ball.y = platformY - Ball.RADIUS; // Place on top of bucket
                settledBalls.add(ball);
                caughtBalls.add(ball);
            }

            if (ball.y - Ball.RADIUS > screenHeight) {
                gameOver();
                return;
            }

            canvas.drawCircle(ball.x, ball.y, Ball.RADIUS, ballPaint);
        }

        balls.removeAll(caughtBalls);

        // Update settled balls (fluid-like behavior)
        updateSettledBalls(left, right, bottom);
        for (Ball ball : settledBalls) {
            canvas.drawCircle(ball.x, ball.y, Ball.RADIUS, ballPaint);
        }

        postInvalidateDelayed(16);
    }

    private void updateSettledBalls(float left, float right, float bottom) {
        float bucketTop = platformY - 100;

        for (Ball b1 : settledBalls) {
            b1.vy += 0.5f; // gravity effect
            b1.y += b1.vy;

            // Constrain to bucket
            if (b1.x < left + Ball.RADIUS) {
                b1.x = left + Ball.RADIUS;
            }
            if (b1.x > right - Ball.RADIUS) {
                b1.x = right - Ball.RADIUS;
            }

            if (b1.y > bottom - Ball.RADIUS) {
                b1.y = bottom - Ball.RADIUS;
                b1.vy *= -0.3f; // bounce (damping)
            }
            if (b1.y < bucketTop + Ball.RADIUS) {
                b1.y = bucketTop + Ball.RADIUS;
                b1.vy = 0;
            }
        }

        // Simple collision between settled balls
        for (int i = 0; i < settledBalls.size(); i++) {
            Ball b1 = settledBalls.get(i);
            for (int j = i + 1; j < settledBalls.size(); j++) {
                Ball b2 = settledBalls.get(j);

                float dx = b2.x - b1.x;
                float dy = b2.y - b1.y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float minDist = Ball.RADIUS * 2;

                if (dist < minDist && dist > 0) {
                    float overlap = minDist - dist;

                    // Normalize
                    float nx = dx / dist;
                    float ny = dy / dist;

                    // Push them apart
                    b1.x -= nx * overlap / 2;
                    b1.y -= ny * overlap / 2;
                    b2.x += nx * overlap / 2;
                    b2.y += ny * overlap / 2;

                    // Add damping to velocities for more fluid behavior
                    b1.vx -= nx * 0.1f;
                    b1.vy -= ny * 0.1f;
                    b2.vx += nx * 0.1f;
                    b2.vy += ny * 0.1f;
                }
            }
        }

        // Apply velocities (optional smoothing)
        for (Ball ball : settledBalls) {
            ball.x += ball.vx;
            ball.y += ball.vy;

            // Dampen velocities
            ball.vx *= 0.9f;
            ball.vy *= 0.9f;
        }
    }

    private void gameOver() {
        isGameOver = true;
        handler.removeCallbacks(addBallRunnable);
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isGameOver && event.getAction() == MotionEvent.ACTION_DOWN) {
            startGame();
            invalidate();
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                platformX = event.getX();

                if (platformX < platformWidth / 2) {
                    platformX = platformWidth / 2;
                }
                if (platformX > screenWidth - platformWidth / 2) {
                    platformX = screenWidth - platformWidth / 2;
                }

                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    // Inner Ball class
    private static class Ball {
        float x, y;
        float vx, vy;
        static final float RADIUS = 30f;

        Ball(float startX, float startY) {
            this.x = startX;
            this.y = startY;
            this.vx = 0;
            this.vy = 0;
        }

        void update() {
            y += 8f; // falling speed
        }
    }
}
