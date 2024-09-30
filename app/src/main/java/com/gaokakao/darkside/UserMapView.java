package com.gaokakao.darkside;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

public class UserMapView extends View {
    private Paint currentUserPaint;
    private Paint otherUserPaint;
    private double currentLatitude;
    private double currentLongitude;
    private JSONArray users;

    public UserMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        currentUserPaint = new Paint();
        currentUserPaint.setColor(Color.BLUE);
        currentUserPaint.setStyle(Paint.Style.FILL);

        otherUserPaint = new Paint();
        otherUserPaint.setColor(Color.WHITE);
        otherUserPaint.setStyle(Paint.Style.FILL);
    }

    public void updateUserLocation(double latitude, double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        invalidate();
    }

    public void updateUserLocations(JSONArray users) {
        this.users = users;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (users != null) {
            drawUsers(canvas);
        }
    }

    private void drawUsers(Canvas canvas) {
        float minX = (float) currentLongitude;
        float minY = (float) currentLatitude;
        float maxX = (float) currentLongitude;
        float maxY = (float) currentLatitude;

        try {
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                double userLat = user.getDouble("latitude");
                double userLong = user.getDouble("longitude");

                minX = (float) Math.min(minX, userLong);
                minY = (float) Math.min(minY, userLat);
                maxX = (float) Math.max(maxX, userLong);
                maxY = (float) Math.max(maxY, userLat);
            }

            float scaleFactor = Math.min(getWidth() / (Math.abs(maxX - minX) * 100000), getHeight() / (Math.abs(maxY - minY) * 100000));
            float offsetX = (float) ((maxX + minX) / 2);
            float offsetY = (float) ((maxY + minY) / 2);

            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                double userLat = user.getDouble("latitude");
                double userLong = user.getDouble("longitude");

                float x = (float) ((userLong - offsetX) * scaleFactor + getWidth() / 2);
                float y = (float) ((userLat - offsetY) * scaleFactor + getHeight() / 2);
                canvas.drawCircle(x, y, 10, otherUserPaint);
            }

            float currentX = getWidth() / 2;
            float currentY = getHeight() / 2;
            canvas.drawCircle(currentX, currentY, 15, currentUserPaint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
