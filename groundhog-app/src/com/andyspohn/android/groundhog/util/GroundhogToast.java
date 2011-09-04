package com.andyspohn.android.groundhog.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.andyspohn.android.groundhog.R;


public class GroundhogToast {
    public static void showServiceToast(Context context, int messageId) {
        showToast(context, messageId, Toast.LENGTH_SHORT);
    }

    private static void showToast(Context context, int messageId, int duration) {
        final View view = LayoutInflater.from(context).inflate(R.layout.service_toast, null);
        ((TextView) view.findViewById(R.id.message)).setText(messageId);

        Toast toast = new Toast(context);
        toast.setDuration(duration);
        toast.setView(view);

        toast.show();
    }

    public static void showQuickToast(Context context, int messageId) {
        showToast(context, messageId, Toast.LENGTH_SHORT);
    }
}
