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

public class MilkFragment extends Fragment implements MainActivity.Refreshable {

    private int currentInput = 0;
    private DatabaseHelper dbHelper;
    private TextView tvCurrentInput, tvStats, tvTimeSince;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> historyStrings = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_milk, container, false);
        dbHelper = new DatabaseHelper(getContext());

        tvCurrentInput = v.findViewById(R.id.tvCurrentInput);
        tvStats = v.findViewById(R.id.tvStats);
        tvTimeSince = v.findViewById(R.id.tvTimeSince);
        ListView lvHistory = v.findViewById(R.id.lvHistory);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, historyStrings);
        lvHistory.setAdapter(adapter);

        v.findViewById(R.id.btnAdd30).setOnClickListener(view -> { currentInput += 30; updateInputText(); });
        v.findViewById(R.id.btnAdd60).setOnClickListener(view -> { currentInput += 60; updateInputText(); });
        v.findViewById(R.id.btnReset).setOnClickListener(view -> { currentInput = 0; updateInputText(); });

        v.findViewById(R.id.btnSubmit).setOnClickListener(view -> {
            if (currentInput > 0) {
                dbHelper.addMilkEntry(currentInput, System.currentTimeMillis());
                currentInput = 0; updateInputText(); refresh();
            }
        });

        v.findViewById(R.id.btnClear).setOnClickListener(view -> {
            new android.app.AlertDialog.Builder(getContext()).setTitle("Clear Milk History?")
                    .setPositiveButton("Yes", (d, w) -> { dbHelper.clearMilkData(); refresh(); })
                    .setNegativeButton("No", null).show();
        });

        refresh();
        return v;
    }

    private void updateInputText() { tvCurrentInput.setText("Amount to add: " + currentInput + "ml"); }

    public void refresh() {
        if (!isAdded()) return;
        ArrayList<DatabaseHelper.Entry> entries = dbHelper.getAllMilkEntries();
        historyStrings.clear();
        int total = 0, today = 0;
        long now = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        for (int i = entries.size() - 1; i >= 0; i--) {
            DatabaseHelper.Entry e = entries.get(i);
            total += e.value;
            if (isToday(e.timestamp)) today += e.value;
            historyStrings.add(e.value + "ml at " + sdf.format(new Date(e.timestamp)));
        }

        tvStats.setText("Total: " + total + "ml\nToday: " + today + "ml");
        if (!entries.isEmpty()) {
            tvTimeSince.setText("Previous feeding: " + formatDuration(now - entries.get(entries.size()-1).timestamp));
        }
        adapter.notifyDataSetChanged();
    }

    private String formatDuration(long ms) {
        long m = TimeUnit.MILLISECONDS.toMinutes(ms);
        return (m >= 60) ? (m/60) + "h " + (m%60) + "m ago" : m + "m ago";
    }

    private boolean isToday(long t) {
        Calendar c1 = Calendar.getInstance(); Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(t);
        return c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}