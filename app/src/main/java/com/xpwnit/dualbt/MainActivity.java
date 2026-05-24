package com.xpwnit.dualbt;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.xpwnit.dualbt.bt.AndroidBluetoothScanner;
import com.xpwnit.dualbt.logging.AppLogger;
import com.xpwnit.dualbt.logging.LogStore;
import com.xpwnit.dualbt.service.DualBTService;
import com.xpwnit.dualbt.state.StreamDevice;
import com.xpwnit.dualbt.state.StreamRoutePlan;
import com.xpwnit.dualbt.state.StreamSessionController;

import java.util.ArrayList;
import java.util.List;

public final class MainActivity extends Activity implements AppLogger.Listener {
    private static final int REQUIRED_SPEAKERS = 2;
    private static final int REQUEST_MEDIA_PROJECTION = 41;
    private final ArrayList<StreamDevice> devices = new ArrayList<>();
    private final StreamSessionController streamSession = new StreamSessionController(REQUIRED_SPEAKERS);
    private final AndroidBluetoothScanner bluetoothScanner = new AndroidBluetoothScanner();

    private FrameLayout root;
    private LinearLayout deviceList;
    private TextView statusText;
    private TextView countBadge;
    private TextView streamButton;
    private TextView modeBadge;
    private OrbBackgroundView backgroundView;
    private FrameLayout logOverlay;
    private LinearLayout logList;
    private String logFilter = "ALL";
    private MediaProjectionManager projectionManager;
    private boolean emulatorMode = true;
    private boolean dark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        configureWindow();
        AppLogger.i("MainActivity", "Activity created");
        AppLogger.i("MainViewModel", "ViewModel initialized");
        loadDevices("initial");
        buildUi();
        requestRuntimePermissions();
        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppLogger.addListener(this);
        AppLogger.i("MainActivity", "Activity resumed");
    }

    @Override
    protected void onPause() {
        AppLogger.removeListener(this);
        AppLogger.i("MainActivity", "Activity paused");
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            handleCapturePermissionResult(resultCode, data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 7) {
            AppLogger.i("MainActivity", "Runtime permissions result received: " + grantResults.length);
            loadDevices("permissions");
            if (deviceList != null) {
                render();
            }
        }
    }

    @Override
    public void onLogsChanged() {
        if (logOverlay != null) {
            renderLogs();
        }
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(dark ? Color.rgb(8, 10, 16) : Color.rgb(238, 244, 250));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null && !dark) {
                controller.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        }
    }

    private void loadDevices(String reason) {
        devices.clear();
        AndroidBluetoothScanner.ScanResult scanResult = bluetoothScanner.scan(this, true);
        devices.addAll(scanResult.devices);
        emulatorMode = scanResult.mockFallback;
        AppLogger.d("MainViewModel", "Devices updated: " + devices.size() + ", emulator=" + emulatorMode + ", reason=" + reason);
    }

    private void buildUi() {
        root = new FrameLayout(this);
        root.setBackgroundColor(bg());
        backgroundView = new OrbBackgroundView(this);
        root.addView(backgroundView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(28), dp(20), dp(24));
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        content.addView(header());
        content.addView(space(16));
        content.addView(statusCard());
        content.addView(space(16));

        TextView section = label("Available Devices", 14, muted(), Typeface.BOLD);
        content.addView(section);
        content.addView(space(8));

        deviceList = new LinearLayout(this);
        deviceList.setOrientation(LinearLayout.VERTICAL);
        content.addView(deviceList);
        content.addView(space(16));

        streamButton = button("Start Mock Stream", true);
        streamButton.setOnClickListener(v -> {
            if (streamSession.isStreaming()) {
                stopStreaming();
            } else {
                startStreaming();
            }
        });
        content.addView(streamButton);

        root.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        setContentView(root);
    }

    private View header() {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        GradientTextView title = new GradientTextView(this);
        title.setText("DualBT");
        title.setTextSize(34);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        titleBlock.addView(title);

        modeBadge = pill("Emulator Mode", warning(), tint(warning(), 0.12f));
        titleBlock.addView(modeBadge);
        row.addView(titleBlock, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView logs = iconButton("Logs");
        logs.setOnClickListener(v -> showLogs());
        row.addView(logs);
        row.addView(spaceHorizontal(8));

        TextView refresh = iconButton("Refresh");
        refresh.setOnClickListener(v -> refreshDevices());
        row.addView(refresh);
        return row;
    }

    private View statusCard() {
        LinearLayout card = glassCard();
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);

        statusText = label("Select 2 speakers", 16, fg(), Typeface.BOLD);
        card.addView(statusText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        countBadge = pill("0/2", accent(), tint(accent(), 0.14f));
        card.addView(countBadge);
        return card;
    }

    private void render() {
        deviceList.removeAllViews();
        for (StreamDevice device : devices) {
            deviceList.addView(deviceCard(device));
            deviceList.addView(space(10));
        }
        int count = streamSession.selectedCount();
        boolean streaming = streamSession.isStreaming();
        boolean waiting = streamSession.isAwaitingCapturePermission();
        boolean canStart = streamSession.canStart();
        countBadge.setText(count + "/" + REQUIRED_SPEAKERS);
        statusText.animate().alpha(0f).setDuration(90).withEndAction(() -> {
            statusText.setText(streamSession.statusMessage());
            statusText.animate().alpha(1f).setDuration(140).start();
        }).start();
        streamButton.setText(streaming ? "Stop Streaming" : waiting ? "Waiting for Permission" : "Start Mock Stream");
        streamButton.setBackground(streaming ? rounded(error(), errorDark()) : waiting ? rounded(warning(), warning()) : gradient(accent(), accent2(), dp(8)));
        streamButton.setEnabled(canStart || streaming);
        streamButton.setAlpha((canStart || streaming) ? 1f : 0.55f);
        backgroundView.setStreaming(streaming);
        modeBadge.setText(emulatorMode ? "Emulator Mode" : "Bluetooth Ready");
        modeBadge.setVisibility(View.VISIBLE);
    }

    private View deviceCard(StreamDevice device) {
        boolean isSelected = streamSession.isSelected(device);
        boolean streaming = streamSession.isStreaming();
        LinearLayout card = glassCard();
        card.setOrientation(LinearLayout.VERTICAL);
        card.setOnClickListener(v -> {
            toggle(device);
        });
        card.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                view.animate().scaleX(0.985f).scaleY(0.985f).setDuration(80).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                view.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
            }
            return false;
        });
        if (isSelected) {
            card.setBackground(glassBackground(tint(success(), 0.16f), success()));
        }
        if (streaming && isSelected) {
            AlphaAnimation pulse = new AlphaAnimation(0.72f, 1f);
            pulse.setDuration(760);
            pulse.setRepeatMode(Animation.REVERSE);
            pulse.setRepeatCount(Animation.INFINITE);
            card.startAnimation(pulse);
        }

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setOrientation(LinearLayout.HORIZONTAL);
        TextView name = label(device.name, 18, fg(), Typeface.BOLD);
        top.addView(name, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView state = pill(isSelected ? "Selected" : "Ready", isSelected ? success() : accent(), tint(isSelected ? success() : accent(), 0.12f));
        top.addView(state);
        card.addView(top);

        card.addView(space(6));
        card.addView(label(device.subtitle, 13, muted(), Typeface.NORMAL));
        card.addView(space(8));
        card.addView(label(device.address, 12, subtle(), Typeface.NORMAL));
        return card;
    }

    private void toggle(StreamDevice device) {
        boolean wasSelected = streamSession.isSelected(device);
        boolean changed = streamSession.toggle(device);
        if (changed && wasSelected) {
            AppLogger.i("MainViewModel", "Device deselected: " + device.name);
        } else if (changed) {
            AppLogger.i("MainViewModel", "Device selected: " + device.name);
        } else if (streamSession.isAwaitingCapturePermission()) {
            AppLogger.w("MainViewModel", "Selection locked while capture permission is pending");
        } else if (streamSession.isStreaming()) {
            AppLogger.w("MainViewModel", "Selection locked while streaming");
        } else {
            AppLogger.w("MainViewModel", "Selection limit reached: exactly 2 speakers supported");
        }
        render();
    }

    private void refreshDevices() {
        AppLogger.i("MainViewModel", "Refresh requested");
        loadDevices("refresh");
        render();
    }

    private void startStreaming() {
        StreamSessionController.StartResult result = streamSession.start();
        if (result == StreamSessionController.StartResult.CAPTURE_PERMISSION_REQUIRED) {
            AppLogger.i("MainViewModel", "Capture permission required before streaming to " + streamSession.selectedNames());
            render();
            requestCapturePermission();
        } else if (result == StreamSessionController.StartResult.NEED_TWO_DEVICES) {
            AppLogger.w("MainViewModel", "Start blocked: select exactly 2 speakers, selected " + streamSession.selectedCount());
            render();
        } else if (result == StreamSessionController.StartResult.ALREADY_WAITING_FOR_PERMISSION) {
            AppLogger.w("MainViewModel", "Start ignored because capture permission is already pending");
            render();
        } else {
            AppLogger.w("MainViewModel", "Start ignored because streaming is already active");
            render();
        }
    }

    private void stopStreaming() {
        streamSession.stop();
        stopService(new Intent(this, DualBTService.class));
        AppLogger.i("MainViewModel", "Stopping streaming");
        render();
    }

    private void requestCapturePermission() {
        if (projectionManager == null) {
            streamSession.cancelCapturePermission();
            AppLogger.e("MainActivity", "MediaProjectionManager unavailable", new IllegalStateException("MediaProjection service missing"));
            render();
            return;
        }
        try {
            AppLogger.i("MainActivity", "Requesting MediaProjection permission");
            startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
        } catch (RuntimeException exception) {
            streamSession.cancelCapturePermission();
            AppLogger.e("MainActivity", "Unable to request MediaProjection permission", exception);
            render();
        }
    }

    private void handleCapturePermissionResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (streamSession.confirmCapturePermission()) {
                StreamRoutePlan routePlan = StreamRoutePlan.fromSelected(streamSession.selectedDevices(), REQUIRED_SPEAKERS);
                AppLogger.i("MainViewModel", "Capture permission granted; starting streaming to " + routePlan.displayNames());
                startDualBTService(resultCode, data, routePlan);
            } else {
                AppLogger.w("MainViewModel", "Capture permission result ignored because no stream start is pending");
            }
        } else if (streamSession.cancelCapturePermission()) {
            AppLogger.w("MainViewModel", "Capture permission denied; streaming not started");
        } else {
            AppLogger.w("MainViewModel", "Capture permission denial ignored because no stream start is pending");
        }
        render();
    }

    private void startDualBTService(int resultCode, Intent data, StreamRoutePlan routePlan) {
        Intent serviceIntent = new Intent(this, DualBTService.class);
        serviceIntent.putExtra(DualBTService.EXTRA_RESULT_CODE, resultCode);
        serviceIntent.putExtra(DualBTService.EXTRA_RESULT_DATA, data);
        serviceIntent.putExtra(DualBTService.EXTRA_ROUTE_PLAN, routePlan.toPayload());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        AppLogger.i("MainActivity", "Foreground service start requested after capture consent");
    }

    private void showLogs() {
        if (logOverlay != null) {
            return;
        }
        AppLogger.i("MainActivity", "Log screen opened");
        logOverlay = new FrameLayout(this);
        logOverlay.setBackgroundColor(tint(bg(), dark ? 0.90f : 0.82f));

        LinearLayout panel = glassCard();
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = label("Logs", 22, fg(), Typeface.BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView clear = iconButton("Clear");
        clear.setOnClickListener(v -> AppLogger.clear());
        header.addView(clear);
        header.addView(spaceHorizontal(8));
        TextView close = iconButton("Close");
        close.setOnClickListener(v -> hideLogs());
        header.addView(close);
        panel.addView(header);
        panel.addView(space(12));
        panel.addView(filterBar());
        panel.addView(space(12));

        ScrollView scroll = new ScrollView(this);
        logList = new LinearLayout(this);
        logList.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(logList);
        panel.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        params.setMargins(dp(12), dp(44), dp(12), dp(12));
        logOverlay.addView(panel, params);
        root.addView(logOverlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        logOverlay.setTranslationY(root.getHeight() == 0 ? dp(600) : root.getHeight());
        logOverlay.animate().translationY(0f).setDuration(220).start();
        renderLogs();
    }

    private View filterBar() {
        HorizontalScrollView scroller = new HorizontalScrollView(this);
        scroller.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        String[] levels = {"ALL", "DEBUG", "INFO", "WARN", "ERROR"};
        for (String level : levels) {
            TextView chip = pill(level, level.equals(logFilter) ? accent() : muted(), level.equals(logFilter) ? tint(accent(), 0.18f) : glassFill());
            chip.setOnClickListener(v -> {
                logFilter = ((TextView) v).getText().toString();
                showLogsRefreshPanel();
            });
            row.addView(chip);
            row.addView(spaceHorizontal(8));
        }
        scroller.addView(row);
        return scroller;
    }

    private void showLogsRefreshPanel() {
        if (logOverlay != null) {
            root.removeView(logOverlay);
            logOverlay = null;
            showLogs();
        }
    }

    private void hideLogs() {
        if (logOverlay == null) {
            return;
        }
        View overlay = logOverlay;
        logOverlay = null;
        AppLogger.i("MainActivity", "Log screen closed");
        overlay.animate().translationY(root.getHeight()).setDuration(180).withEndAction(() -> root.removeView(overlay)).start();
    }

    private void renderLogs() {
        if (logList == null) {
            return;
        }
        logList.removeAllViews();
        List<LogStore.Entry> logs = AppLogger.logs(logFilter);
        if (logs.isEmpty()) {
            logList.addView(label("No log entries", 14, muted(), Typeface.NORMAL));
            return;
        }
        for (LogStore.Entry entry : logs) {
            LinearLayout row = glassCard();
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(12), dp(10), dp(12), dp(10));
            TextView meta = label(entry.level + "  " + entry.tag + "  " + entry.formattedTime(), 11, levelColor(entry.level), Typeface.BOLD);
            TextView message = label(entry.message, 13, fg(), Typeface.NORMAL);
            row.addView(meta);
            row.addView(space(4));
            row.addView(message);
            logList.addView(row);
            logList.addView(space(8));
        }
    }

    private void requestRuntimePermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        ArrayList<String> permissions = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (!permissions.isEmpty()) {
            AppLogger.i("MainActivity", "Requesting runtime permissions: " + permissions.size());
            requestPermissions(permissions.toArray(new String[0]), 7);
        }
    }

    private LinearLayout glassCard() {
        LinearLayout card = new LinearLayout(this);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(glassBackground(glassFill(), glassStroke()));
        return card;
    }

    private GradientDrawable glassBackground(int fill, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(8));
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private TextView button(String text, boolean enabled) {
        TextView view = label(text, 16, Color.WHITE, Typeface.BOLD);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(16), dp(14), dp(16), dp(14));
        view.setBackground(gradient(accent(), accent2(), dp(8)));
        view.setEnabled(enabled);
        return view;
    }

    private TextView iconButton(String text) {
        TextView view = label(text, 13, fg(), Typeface.BOLD);
        view.setGravity(Gravity.CENTER);
        view.setMinWidth(dp(64));
        view.setPadding(dp(10), dp(9), dp(10), dp(9));
        view.setBackground(glassBackground(glassFill(), glassStroke()));
        return view;
    }

    private TextView pill(String text, int textColor, int fill) {
        TextView view = label(text, 12, textColor, Typeface.BOLD);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(10), dp(6), dp(10), dp(6));
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(8));
        drawable.setStroke(dp(1), tint(textColor, 0.34f));
        view.setBackground(drawable);
        return view;
    }

    private TextView label(String text, int sp, int color, int style) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, style);
        view.setIncludeFontPadding(true);
        return view;
    }

    private View space(int dp) {
        View view = new View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(1, dp(dp)));
        return view;
    }

    private View spaceHorizontal(int dp) {
        View view = new View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(dp(dp), 1));
        return view;
    }

    private GradientDrawable gradient(int start, int end, int radius) {
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{start, end});
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private GradientDrawable rounded(int fill, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(8));
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int bg() {
        return dark ? Color.rgb(8, 10, 16) : Color.rgb(238, 244, 250);
    }

    private int fg() {
        return dark ? Color.rgb(245, 248, 255) : Color.rgb(23, 31, 43);
    }

    private int muted() {
        return dark ? Color.rgb(177, 188, 205) : Color.rgb(82, 96, 116);
    }

    private int subtle() {
        return dark ? Color.rgb(124, 136, 158) : Color.rgb(109, 123, 145);
    }

    private int glassFill() {
        return dark ? Color.argb(92, 32, 38, 52) : Color.argb(156, 255, 255, 255);
    }

    private int glassStroke() {
        return dark ? Color.argb(108, 164, 185, 214) : Color.argb(170, 145, 162, 188);
    }

    private int accent() {
        return Color.rgb(64, 169, 255);
    }

    private int accent2() {
        return Color.rgb(33, 214, 176);
    }

    private int success() {
        return Color.rgb(66, 220, 139);
    }

    private int warning() {
        return Color.rgb(248, 185, 73);
    }

    private int error() {
        return Color.rgb(224, 76, 93);
    }

    private int errorDark() {
        return Color.rgb(151, 39, 54);
    }

    private int levelColor(String level) {
        if ("ERROR".equals(level)) return error();
        if ("WARN".equals(level)) return warning();
        if ("INFO".equals(level)) return accent2();
        return accent();
    }

    private int tint(int color, float alpha) {
        return Color.argb(Math.round(255 * alpha), Color.red(color), Color.green(color), Color.blue(color));
    }

    public final class GradientTextView extends TextView {
        private final Paint paintRef;

        public GradientTextView(Activity activity) {
            super(activity);
            paintRef = getPaint();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            paintRef.setShader(new LinearGradient(
                    0,
                    0,
                    Math.max(1, w),
                    0,
                    new int[]{accent(), accent2(), warning()},
                    null,
                    Shader.TileMode.CLAMP
            ));
        }
    }

    public final class OrbBackgroundView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float phase;
        private boolean isStreaming;

        public OrbBackgroundView(Activity activity) {
            super(activity);
            post(animator);
        }

        public void setStreaming(boolean streaming) {
            isStreaming = streaming;
        }

        private final Runnable animator = new Runnable() {
            @Override
            public void run() {
                phase += isStreaming ? 0.024f : 0.012f;
                if (phase > 1f) {
                    phase -= 1f;
                }
                invalidate();
                postDelayed(this, 16);
            }
        };

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int width = getWidth();
            int height = getHeight();
            drawOrb(canvas, width * (0.18f + 0.07f * wave(phase)), height * 0.20f, width * 0.30f, accent(), 0.20f);
            drawOrb(canvas, width * 0.82f, height * (0.38f + 0.08f * wave(phase + 0.33f)), width * 0.24f, accent2(), 0.18f);
            drawOrb(canvas, width * (0.42f + 0.06f * wave(phase + 0.66f)), height * 0.82f, width * 0.28f, warning(), 0.15f);
        }

        private void drawOrb(Canvas canvas, float x, float y, float radius, int color, float alpha) {
            paint.setColor(tint(color, alpha));
            canvas.drawCircle(x, y, radius, paint);
        }

        private float wave(float value) {
            return (float) Math.sin(value * Math.PI * 2);
        }
    }
}
