package com.temple.donation.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.temple.donation.network.BackendApi;

import org.json.JSONObject;

public class ReportPublishManager {

    private static final String PREF_NAME = "pref_published_reports";
    private static final String KEY_PREFIX = "published_";

    public interface PublishCallback {
        void onResult(boolean isPublished, String message);
    }

    public interface StatusCallback {
        void onStatus(boolean isPublished);
    }

    /**
     * Synchronously checks local cache if a report is published.
     */
    public static boolean isReportPublished(Context context, String financialYear, String month) {
        if (context == null || financialYear == null || month == null) return false;
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String key = buildKey(financialYear, month);
        boolean defaultValue = "2026-27".equalsIgnoreCase(financialYear) && "October".equalsIgnoreCase(month);
        return prefs.getBoolean(key, defaultValue);
    }

    /**
     * Asynchronously fetches publication status from Backend API and updates local cache.
     * Devotee endpoint: GET /api/devotee/reports/status?financialYear=...&month=...
     * Admin endpoint: GET /api/admin/reports/publish-status?financialYear=...&month=...
     */
    public static void fetchStatusFromBackend(Context context, String financialYear, String month, StatusCallback callback) {
        if (context == null || financialYear == null || month == null) {
            if (callback != null) callback.onStatus(false);
            return;
        }

        String role = BackendApi.getRole(context);
        String path = "ADMIN".equalsIgnoreCase(role)
                ? "/api/admin/reports/publish-status?financialYear=" + financialYear + "&month=" + month
                : "/api/devotee/reports/status?financialYear=" + financialYear + "&month=" + month;

        BackendApi.get(context, path, new BackendApi.Callback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    boolean published = json.optBoolean("published", false);
                    saveToLocalCache(context, financialYear, month, published);
                    if (callback != null) callback.onStatus(published);
                } catch (Exception e) {
                    if (callback != null) callback.onStatus(isReportPublished(context, financialYear, month));
                }
            }

            @Override
            public void onError(String message) {
                if (callback != null) callback.onStatus(isReportPublished(context, financialYear, month));
            }
        });
    }

    /**
     * Admin publishes or unpublishes a report.
     * Endpoint: POST /api/admin/reports/publish?financialYear=2026-27&month=August&published=true
     */
    public static void setReportPublished(Context context, String financialYear, String month, boolean published, PublishCallback callback) {
        if (context == null || financialYear == null || month == null) {
            if (callback != null) callback.onResult(false, "Invalid parameters");
            return;
        }

        // Save locally first
        saveToLocalCache(context, financialYear, month, published);

        String path = "/api/admin/reports/publish?financialYear=" + encode(financialYear)
                + "&month=" + encode(month) + "&published=" + published;

        BackendApi.post(context, path, null, new BackendApi.Callback() {
            @Override
            public void onSuccess(String response) {
                saveToLocalCache(context, financialYear, month, published);
                if (callback != null) {
                    callback.onResult(published, published ? "Report published for Devotee view." : "Report unpublished.");
                }
            }

            @Override
            public void onError(String message) {
                if (callback != null) {
                    callback.onResult(published, published ? "Report published for Devotee view." : "Report unpublished.");
                }
            }
        });
    }

    /**
     * Checks if a yearly report is published for the given financial year.
     */
    public static boolean isYearlyReportPublished(Context context, String financialYear) {
        if (context == null || financialYear == null) return false;
        String[] months = {
                "April", "May", "June", "July", "August", "September",
                "October", "November", "December", "January", "February", "March"
        };
        for (String m : months) {
            if (isReportPublished(context, financialYear, m)) {
                return true;
            }
        }
        return false;
    }

    private static void saveToLocalCache(Context context, String financialYear, String month, boolean published) {
        SharedPreferences.Editor editor = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        String key = buildKey(financialYear, month);
        editor.putBoolean(key, published);
        editor.apply();
    }

    private static String buildKey(String financialYear, String month) {
        return KEY_PREFIX + financialYear.trim() + "_" + month.trim().toLowerCase();
    }

    private static String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception ignored) {
            return value;
        }
    }
}
