package com.gaokakao.darkside;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import org.json.JSONArray;
import org.json.JSONException;

public class UserMapView extends View {
    private Paint userPaint;
    private Paint otherUserPaint;
    private double currentLatitude;
    private double currentLongitude;
    private JSONArray users;

    public UserMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        userPaint = new Paint();
        userPaint.setColor(Color.BLUE);
        userPaint.setStyle(Paint.Style.FILL);

        otherUserPaint = new Paint();
        otherUserPaint.setColor(Color.WHITE);
        otherUserPaint.setStyle(Paint.Style.FILL);
    }

    public void updateUserLocations(JSONArray users, double currentLat, double currentLon) {
        this.users = users;
        this.currentLatitude = currentLat;
        this.currentLongitude = currentLon;
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();

        // Draw current user at center
        float radius = 20;
        canvas.drawCircle(width / 2, height / 2, radius, userPaint);

        // Draw other users
        if (users != null) {
            for (int i = 0; i < users.length(); i++) {
                try {
                    double lat = users.getJSONObject(i).getDouble("latitude");
                    double lon = users.getJSONObject(i).getDouble("longitude");
                    float userX = (float) ((lon - currentLongitude) * 100000) + (width / 2);
                    float userY = (float) ((lat - currentLatitude) * 100000) + (height / 2);
                    canvas.drawCircle(userX, userY, radius / 2, otherUserPaint);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
