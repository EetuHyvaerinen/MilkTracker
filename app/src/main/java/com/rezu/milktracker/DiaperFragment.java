package com.rezu.milktracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DiaperFragment extends Fragment implements MainActivity.Refreshable {

    private DatabaseHelper dbHelper;
    private TextView tvDiaperStats, tvDiaperTimeSince;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> historyStrings = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_diaper, container, false);
        dbHelper = new DatabaseHelper(getContext());

        tvDiaperStats = v.findViewById(R.id.tvDiaperStats);
        tvDiaperTimeSince = v.findViewById(R.id.tvDiaperTimeSince);
        ListView lvHistory = v.findViewById(R.id.lvDiaperHistory);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, historyStrings);
        lvHistory.setAdapter(adapter);

        v.findViewById(R.id.btnLogDiaper).setOnClickListener(view -> {
            dbHelper.addDiaperEntry(System.currentTimeMillis());
            refresh();
        });

        v.findViewById(R.id.btnClearDiapers).setOnClickListener(view -> {
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Clear Diaper History?")
                    .setMessage("Are you sure you want to delete all diaper logs?")
                    .setPositiveButton("Yes", (d, w) -> {
                        dbHelper.clearDiaperData();
                        refresh();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        refresh();
        return v;
    }

    @Override
    public void refresh() {
        if (!isAdded()) return;

        ArrayList<DatabaseHelper.Entry> entries = dbHelper.getAllDiaperEntries();
        historyStrings.clear();
        int todayCount = 0;
        long now = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault());

        for (int i = entries.size() - 1; i >= 0; i--) {
            DatabaseHelper.Entry e = entries.get(i);
            if (isToday(e.timestamp)) todayCount++;
            historyStrings.add("Changed at " + sdf.format(new Date(e.timestamp)));
        }

        tvDiaperStats.setText("Total changes today: " + todayCount);

        if (entries.isEmpty()) {
            tvDiaperTimeSince.setText("Last change: --");
        } else {
            long lastTime = entries.get(entries.size() - 1).timestamp;
            tvDiaperTimeSince.setText("Last change: " + formatDuration(now - lastTime));
        }

        adapter.notifyDataSetChanged();
    }

    private String formatDuration(long ms) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(ms);
        if (minutes >= 60) {
            long hours = minutes / 60;
            long remainMin = minutes % 60;
            return hours + "h " + remainMin + "m ago";
        } else {
            return minutes + "m ago";
        }
    }

    private boolean isToday(long timestamp) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(timestamp);
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }
}