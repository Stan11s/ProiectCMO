package com.example.poriectcmocheltuieli;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class CalendarActivity extends BaseActivity {
    private String selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private List<Expense> allExpenses;
    private String selectedMode = "day";
    private FirebaseFirestore db;
    private String userId="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);
        //initializam firebase
        db = FirebaseFirestore.getInstance();
        allExpenses = new ArrayList<>();
        // Configurare Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        userId = getIntent().getStringExtra("userId");

        if (userId != null) {
            fetchDataFromFirestore(userId); // Preia datele din Firestore utilizând userId
        } else {
            Toast.makeText(this, "Eroare: Nu s-a primit userId!", Toast.LENGTH_SHORT).show();
        }

        // Configurare RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Generăm cheltuieli exemplu
        //allExpenses = generateMockExpenses();

        // Configurare butoane
        Button openCalendarButton = findViewById(R.id.btn_open_calendar);
        Button dayButton = findViewById(R.id.btn_day);
        Button weekButton = findViewById(R.id.btn_week);
        Button monthButton = findViewById(R.id.btn_month);
        Button openChartButton = findViewById(R.id.btn_open_chart);
        Button openBarChartButton = findViewById(R.id.btn_open_barchart);

        dayButton.setOnClickListener(v -> {
            selectedMode = "day";
            updateViewBasedOnMode();
        });

        weekButton.setOnClickListener(v -> {
            selectedMode = "week";
            updateViewBasedOnMode();
        });

        monthButton.setOnClickListener(v -> {
            selectedMode = "month";
            updateViewBasedOnMode();
        });
        openCalendarButton.setOnClickListener(v -> openDatePicker());
        dayButton.setOnClickListener(v -> filterExpensesByDay());
        weekButton.setOnClickListener(v -> filterExpensesByWeek());
        monthButton.setOnClickListener(v -> filterExpensesByMonth());
        openChartButton.setOnClickListener(v -> openChart());
        openBarChartButton.setOnClickListener(v -> openBarChart());

    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Formatează data selectată în formatul "dd/MM/yyyy"
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(Calendar.YEAR, selectedYear);
                    selectedCalendar.set(Calendar.MONTH, selectedMonth);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    selectedDate = sdf.format(selectedCalendar.getTime()); // Formatează data selectată

                    Toast.makeText(this, "Data selectată: " + selectedDate, Toast.LENGTH_SHORT).show();

                    // Actualizează vizualizarea pentru modul curent
                    updateViewBasedOnMode();
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
    }

    private void filterExpensesByDay() {
        selectedMode = "day";
        if (selectedDate == null || selectedDate.isEmpty()) {
            Toast.makeText(this, "Selectați o dată din calendar!", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Expense> filteredExpenses = new ArrayList<>();
        for (Expense expense : allExpenses) {
            if (expense.getDate().equals(selectedDate)) {
                filteredExpenses.add(expense);
            }
        }
        updateRecyclerView(filteredExpenses);
    }

    private void filterExpensesByWeek() {
        selectedMode = "week";
        if (selectedDate == null || selectedDate.isEmpty()) {
            Toast.makeText(this, "Selectați o dată din calendar!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Spargem `selectedDate` în zi, lună și an
        String[] dateParts = selectedDate.split("/");
        int selectedDay = Integer.parseInt(dateParts[0]);
        int selectedMonth = Integer.parseInt(dateParts[1]) - 1; // Calendar folosește lunile de la 0
        int selectedYear = Integer.parseInt(dateParts[2]);

        // Configurăm Calendar pentru data selectată
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth);
        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

        // Calculăm începutul săptămânii (luni)
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        Calendar startOfWeek = (Calendar) calendar.clone(); // Luni

        // Calculăm sfârșitul săptămânii (duminică)
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        Calendar endOfWeek = (Calendar) calendar.clone(); // Duminică

        // Filtrăm cheltuielile pentru săptămână
        List<Expense> filteredExpenses = new ArrayList<>();
        for (Expense expense : allExpenses) {
            String[] expenseDateParts = expense.getDate().split("/");
            int expenseDay = Integer.parseInt(expenseDateParts[0]);
            int expenseMonth = Integer.parseInt(expenseDateParts[1]) - 1;
            int expenseYear = Integer.parseInt(expenseDateParts[2]);

            Calendar expenseCalendar = Calendar.getInstance();
            expenseCalendar.set(Calendar.YEAR, expenseYear);
            expenseCalendar.set(Calendar.MONTH, expenseMonth);
            expenseCalendar.set(Calendar.DAY_OF_MONTH, expenseDay);

            // Adăugăm cheltuiala dacă se află între începutul și sfârșitul săptămânii
            if (!expenseCalendar.before(startOfWeek) && !expenseCalendar.after(endOfWeek)) {
                filteredExpenses.add(expense);
            }
        }

        // Actualizăm RecyclerView cu cheltuielile filtrate
        updateRecyclerView(filteredExpenses);

        // Afișăm perioada săptămânii pentru debug
        String weekRange = "Perioada săptămânii: " +
                startOfWeek.get(Calendar.DAY_OF_MONTH) + "/" +
                (startOfWeek.get(Calendar.MONTH) + 1) + "/" +
                startOfWeek.get(Calendar.YEAR) + " - " +
                endOfWeek.get(Calendar.DAY_OF_MONTH) + "/" +
                (endOfWeek.get(Calendar.MONTH) + 1) + "/" +
                endOfWeek.get(Calendar.YEAR);
        Log.d("CalendarActivity", weekRange);
    }

    private void filterExpensesByMonth() {
        selectedMode = "month";
        if (selectedDate == null || selectedDate.isEmpty()) {
            Toast.makeText(this, "Selectați o dată din calendar!", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Expense> filteredExpenses = new ArrayList<>();
        String[] selectedParts = selectedDate.split("/");
        String selectedMonth = selectedParts[1];
        String selectedYear = selectedParts[2];

        for (Expense expense : allExpenses) {
            String[] expenseParts = expense.getDate().split("/");
            if (expenseParts[1].equals(selectedMonth) && expenseParts[2].equals(selectedYear)) {
                filteredExpenses.add(expense);
            }
        }
        updateRecyclerView(filteredExpenses);
    }

    private void updateRecyclerView(List<Expense> filteredExpenses) {
        adapter = new ExpenseAdapter(filteredExpenses);
        recyclerView.setAdapter(adapter);
    }
    private void openChart() {
        // Verificăm dacă există o dată selectată
        if (selectedDate == null || selectedDate.isEmpty()) {
            Toast.makeText(this, "Selectați o dată din calendar!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Expense> filteredExpenses = new ArrayList<>();

        switch (selectedMode) {
            case "day":
                for (Expense expense : allExpenses) {
                    if (expense.getDate().equals(selectedDate)) {
                        filteredExpenses.add(expense);
                    }
                }
                break;

            case "week":
                filteredExpenses = getExpensesForWeek(selectedDate);
                break;

            case "month":
                filteredExpenses = getExpensesForMonth(selectedDate);
                break;
            default:
                Toast.makeText(this, "Selectați o un mode:Day/Week/Moth!", Toast.LENGTH_SHORT).show();
                break;
        }

        if (filteredExpenses.isEmpty()) {
            Toast.makeText(this, "Nu există cheltuieli pentru modul selectat!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Agregăm cheltuielile pe baza datei
        Map<String, List<Expense>> expensesByDate = new HashMap<>();
        for (Expense expense : filteredExpenses) {
            String date = expense.getDate();
            expensesByDate.putIfAbsent(date, new ArrayList<>());
            expensesByDate.get(date).add(expense);
        }

        setContentView(R.layout.chart);

        // Referințe pentru PieChart și CardView
        PieChart pieChart = findViewById(R.id.pie_chart);
        CardView cardDetails = findViewById(R.id.card_details);
        TextView tvDate = findViewById(R.id.tv_date);
        TextView tvDescription = findViewById(R.id.tv_description);
        TextView tvAmount = findViewById(R.id.tv_amount);

        List<PieEntry> pieEntries = new ArrayList<>();
        Map<String, Float> totalAmountsByDate = new HashMap<>();

        for (Map.Entry<String, List<Expense>> entry : expensesByDate.entrySet()) {
            String date = entry.getKey();
            float totalAmount = 0f;

            for (Expense expense : entry.getValue()) {
                totalAmount += expense.getAmount();
            }
            totalAmountsByDate.put(date, totalAmount);
            pieEntries.add(new PieEntry(totalAmount, date));
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "Cheltuieli");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(android.graphics.Color.BLACK);
        Button backButton = findViewById(R.id.btn_back);

        // Configurăm butonul Back
        backButton.setOnClickListener(v -> {
            // Ne întoarcem la layout-ul anterior
            setupMainLayout();
        });

        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.2f RON", value);
            }
        });

        pieChart.setData(pieData);
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Cheltuieli");
        pieChart.setCenterTextSize(18f);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.invalidate();

        // Listener pentru selectarea unui element din grafic
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String selectedDate = ((PieEntry) e).getLabel();
                List<Expense> selectedExpenses = expensesByDate.get(selectedDate);

                // Populăm CardView cu detalii
                StringBuilder descriptionBuilder = new StringBuilder();
                double totalAmount = 0.0;

                for (Expense expense : selectedExpenses) {
                    descriptionBuilder.append("• ").append(expense.getDescription()).append("\n");
                    totalAmount += expense.getAmount();
                }

                tvDate.setText("Data: " + selectedDate);
                tvDescription.setText("Descriere:\n" + descriptionBuilder.toString());
                tvAmount.setText("Total: " + String.format("%.2f RON", totalAmount));

                // Afișăm CardView
                cardDetails.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected() {
                // Ascundem CardView dacă nu este selectat nimic
                cardDetails.setVisibility(View.GONE);
            }
        });
    }

    private void setupMainLayout() {
        // Setăm layout-ul principal
        setContentView(R.layout.calendar);

        // Reconfigurăm Toolbar-ul
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Reconfigurăm butoanele
        Button openCalendarButton = findViewById(R.id.btn_open_calendar);
        Button dayButton = findViewById(R.id.btn_day);
        Button weekButton = findViewById(R.id.btn_week);
        Button monthButton = findViewById(R.id.btn_month);
        Button openChartButton = findViewById(R.id.btn_open_chart);
        Button openBarChartButton = findViewById(R.id.btn_open_barchart);

        // Configurăm RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Refacem listener-ele pentru butoane
        openCalendarButton.setOnClickListener(v -> openDatePicker());
        dayButton.setOnClickListener(v -> {
            selectedMode = "day";
            filterExpensesByDay();
        });
        weekButton.setOnClickListener(v -> {
            selectedMode = "week";
            filterExpensesByWeek();
        });
        monthButton.setOnClickListener(v -> {
            selectedMode = "month";
            filterExpensesByMonth();
        });
        openChartButton.setOnClickListener(v -> openChart());
        openBarChartButton.setOnClickListener(v -> openBarChart());

        // Refacem vizualizarea în RecyclerView pe baza modului selectat
        switch (selectedMode) {
            case "day":
                filterExpensesByDay();
                break;
            case "week":
                filterExpensesByWeek();
                break;
            case "month":
                filterExpensesByMonth();
                break;
            default:
                // Dacă nu există mod selectat, nu facem nimic
                break;
        }
    }

    private List<Expense> getExpensesForWeek(String selectedDate) {
        List<Expense> filteredExpenses = new ArrayList<>();

        String[] dateParts = selectedDate.split("/");
        int selectedDay = Integer.parseInt(dateParts[0]);
        int selectedMonth = Integer.parseInt(dateParts[1]) - 1;
        int selectedYear = Integer.parseInt(dateParts[2]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth);
        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

        // Determinăm începutul săptămânii (luni)
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        Calendar startOfWeek = (Calendar) calendar.clone();

        // Determinăm sfârșitul săptămânii (duminică)
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        Calendar endOfWeek = (Calendar) calendar.clone();

        for (Expense expense : allExpenses) {
            String[] expenseDateParts = expense.getDate().split("/");
            int expenseDay = Integer.parseInt(expenseDateParts[0]);
            int expenseMonth = Integer.parseInt(expenseDateParts[1]) - 1;
            int expenseYear = Integer.parseInt(expenseDateParts[2]);

            Calendar expenseCalendar = Calendar.getInstance();
            expenseCalendar.set(Calendar.YEAR, expenseYear);
            expenseCalendar.set(Calendar.MONTH, expenseMonth);
            expenseCalendar.set(Calendar.DAY_OF_MONTH, expenseDay);

            if (!expenseCalendar.before(startOfWeek) && !expenseCalendar.after(endOfWeek)) {
                filteredExpenses.add(expense);
            }
        }

        return filteredExpenses;
    }

    private List<Expense> getExpensesForMonth(String selectedDate) {
        List<Expense> filteredExpenses = new ArrayList<>();
        String[] selectedParts = selectedDate.split("/");
        String selectedMonth = selectedParts[1];
        String selectedYear = selectedParts[2];

        for (Expense expense : allExpenses) {
            String[] expenseParts = expense.getDate().split("/");
            if (expenseParts[1].equals(selectedMonth) && expenseParts[2].equals(selectedYear)) {
                filteredExpenses.add(expense);
            }
        }

        return filteredExpenses;
    }
    private void openBarChart() {
        // Verificăm dacă există o dată selectată
        if (selectedDate == null || selectedDate.isEmpty()) {
            Toast.makeText(this, "Selectați o dată din calendar!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Expense> filteredExpenses = new ArrayList<>();

        switch (selectedMode) {
            case "day":
                // Filtrare pentru ziua selectată
                for (Expense expense : allExpenses) {
                    if (expense.getDate().equals(selectedDate)) {
                        filteredExpenses.add(expense);
                    }
                }
                break;

            case "week":
                // Filtrare pentru săptămâna selectată
                filteredExpenses = getExpensesForWeek(selectedDate);
                break;

            case "month":
                // Filtrare pentru luna selectată
                filteredExpenses = getExpensesForMonth(selectedDate);
                break;
        }

        if (filteredExpenses.isEmpty()) {
            Toast.makeText(this, "Nu există cheltuieli pentru modul selectat!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Agregăm cheltuielile pe zile
        Map<String, Double> aggregatedByDate = new TreeMap<>(); // TreeMap pentru sortare automată după dată
        for (Expense expense : filteredExpenses) {
            aggregatedByDate.put(expense.getDate(), aggregatedByDate.getOrDefault(expense.getDate(), 0.0) + expense.getAmount());
        }

        // Pregătim datele pentru BarChart
        List<BarEntry> barEntries = new ArrayList<>();
        List<String> xAxisLabels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Double> entry : aggregatedByDate.entrySet()) {
            barEntries.add(new BarEntry(index, entry.getValue().floatValue()));
            xAxisLabels.add(entry.getKey()); // Adăugăm data în lista de etichete
            index++;
        }

        // Afișăm graficul
        setContentView(R.layout.bar_chart);

        BarChart barChart = findViewById(R.id.bar_chart);
        BarDataSet barDataSet = new BarDataSet(barEntries, "Cheltuieli pe zile");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        Button backButton = findViewById(R.id.btn_backBarChart);

        // Configurăm butonul Back
        backButton.setOnClickListener(v -> {
            // Ne întoarcem la layout-ul anterior
            setupMainLayout();
        });


        BarData barData = new BarData(barDataSet);
        barData.setValueTextSize(12f);
        barData.setBarWidth(0.9f); // Lățimea barurilor

        // Configurăm BarChart
        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
        // Configurăm axa X pentru a afișa datele sortate
        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value); // Conversie float la int prin rotunjire
                if (index >= 0 && index < xAxisLabels.size()) {
                    return xAxisLabels.get(index); // Returnăm eticheta corespunzătoare
                } else {
                    return ""; // Returnăm un șir gol pentru valori invalide
                }
            }
        });
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setGranularityEnabled(true);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        barChart.invalidate(); // Reîmprospătăm graficul
    }



    private List<Expense> generateMockExpenses() {
        List<Expense> expenses = new ArrayList<>();
        expenses.add(new Expense("6/1/2025", "Cumparaturi", 100.0));
        expenses.add(new Expense("6/1/2025", "Carburant", 100.0));
        expenses.add(new Expense("6/1/2025", "Cadouri", 100.0));
        expenses.add(new Expense("7/1/2025", "Cumparaturi", 100.0));
        expenses.add(new Expense("8/1/2025", "Cumparaturi", 100.0));
        expenses.add(new Expense("9/1/2025", "Cumparaturi", 100.0));
        expenses.add(new Expense("10/1/2025", "Cumparaturi", 100.0));
        expenses.add(new Expense("11/1/2025", "Carburant", 50.0));
        expenses.add(new Expense("12/1/2025", "Restaurant", 75.0));
        expenses.add(new Expense("13/1/2025", "Restaurant", 75.0));
        expenses.add(new Expense("14/1/2025", "Restaurant", 75.0));
        expenses.add(new Expense("17/1/2025", "Restaurant", 75.0));
        expenses.add(new Expense("15/2/2025", "Cadouri", 200.0));
        expenses.add(new Expense("16/1/2025", "Cumparaturi", 100.0));
        expenses.add(new Expense("17/1/2025", "Carburant", 100.0));
        expenses.add(new Expense("18/1/2025", "Cadouri", 100.0));
        expenses.add(new Expense("19/1/2025", "Cumparaturi", 100.0));
        expenses.add(new Expense("20/1/2025", "Cumparaturi", 100.0));
        expenses.add(new Expense("21/1/2025", "Cumparaturi", 100.0));
        expenses.add(new Expense("22/1/2025", "Cumparaturi", 100.0));
        expenses.add(new Expense("23/1/2025", "Carburant", 50.0));
        expenses.add(new Expense("24/1/2025", "Restaurant", 75.0));
        expenses.add(new Expense("25/1/2025", "Restaurant", 75.0));
        expenses.add(new Expense("26/1/2025", "Restaurant", 75.0));
        expenses.add(new Expense("27/1/2025", "Restaurant", 75.0));
        expenses.add(new Expense("28/2/2025", "Cadouri", 200.0));


        // Harta pentru agregare (cheie: "dată + descriere", valoare: sumă totală)
        Map<String, Expense> aggregatedExpenses = new HashMap<>();

        for (Expense expense : expenses) {
            String key = expense.getDate() + "-" + expense.getDescription();
            if (aggregatedExpenses.containsKey(key)) {
                // Dacă există deja o intrare, adunăm suma
                Expense existingExpense = aggregatedExpenses.get(key);
                existingExpense.setAmount(existingExpense.getAmount() + expense.getAmount());
            } else {
                // Dacă nu există, o adăugăm
                aggregatedExpenses.put(key, new Expense(expense.getDate(), expense.getDescription(), expense.getAmount()));
            }
        }

        // Convertim harta în listă
        List<Expense> aggregatedList = new ArrayList<>(aggregatedExpenses.values());

        // Sortăm lista după dată
        aggregatedList.sort((e1, e2) -> {
            String[] date1Parts = e1.getDate().split("/");
            String[] date2Parts = e2.getDate().split("/");

            Calendar calendar1 = Calendar.getInstance();
            calendar1.set(Integer.parseInt(date1Parts[2]), Integer.parseInt(date1Parts[1]) - 1, Integer.parseInt(date1Parts[0]));

            Calendar calendar2 = Calendar.getInstance();
            calendar2.set(Integer.parseInt(date2Parts[2]), Integer.parseInt(date2Parts[1]) - 1, Integer.parseInt(date2Parts[0]));

            return calendar1.compareTo(calendar2);
        });

        return aggregatedList;
    }
    private void fetchDataFromFirestore(String userId) {
        db.collection("Users")
                .document(userId) // Accesăm documentul corespunzător utilizatorului
                .collection("Cumparaturi")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Expense> expenses = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String description = document.getString("description");
                            Double amount = document.getDouble("amount");
                            Date date = document.getDate("date");

                            String formattedDate = "";
                            if (date != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                formattedDate = sdf.format(date);
                                Log.d("formattedDate", formattedDate);
                            }

                            if (description != null && amount != null && !formattedDate.isEmpty()) {
                                expenses.add(new Expense(formattedDate, description, amount));
                            }
                        }

                        // Salvează toate cheltuielile fără să actualizezi RecyclerView
                        allExpenses = aggregateAndSortExpenses(expenses);

                        // Poți să adaugi un log aici pentru a verifica că datele sunt preluate corect.
                        Log.d("fetchDataFromFirestore", "Datele au fost preluate: " + allExpenses.size());
                    } else {
                        Log.e("Firestore", "Eroare la preluare: " + task.getException().getMessage());
                    }
                });
    }
    private List<Expense> aggregateAndSortExpenses(List<Expense> expenses) {
        // Harta pentru agregare (cheie: "dată + descriere", valoare: sumă totală)
        Map<String, Expense> aggregatedExpenses = new HashMap<>();

        for (Expense expense : expenses) {
            String key = expense.getDate() + "-" + expense.getDescription();
            if (aggregatedExpenses.containsKey(key)) {
                // Dacă există deja o intrare, adunăm suma
                Expense existingExpense = aggregatedExpenses.get(key);
                existingExpense.setAmount(existingExpense.getAmount() + expense.getAmount());
            } else {
                // Dacă nu există, o adăugăm
                aggregatedExpenses.put(key, new Expense(expense.getDate(), expense.getDescription(), expense.getAmount()));
            }
        }

        // Convertim harta în listă
        List<Expense> aggregatedList = new ArrayList<>(aggregatedExpenses.values());

        // Sortăm lista după dată
        aggregatedList.sort((e1, e2) -> {
            String[] date1Parts = e1.getDate().split("/");
            String[] date2Parts = e2.getDate().split("/");

            Calendar calendar1 = Calendar.getInstance();
            calendar1.set(Integer.parseInt(date1Parts[2]), Integer.parseInt(date1Parts[1]) - 1, Integer.parseInt(date1Parts[0]));

            Calendar calendar2 = Calendar.getInstance();
            calendar2.set(Integer.parseInt(date2Parts[2]), Integer.parseInt(date2Parts[1]) - 1, Integer.parseInt(date2Parts[0]));

            return calendar1.compareTo(calendar2);
        });

        return aggregatedList;
    }
    private void updateViewBasedOnMode() {
        if (allExpenses == null || allExpenses.isEmpty()) {
            Toast.makeText(this, "Nu există cheltuieli disponibile!", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (selectedMode) {
            case "day":
                filterExpensesByDay();
                break;
            case "week":
                filterExpensesByWeek();
                break;
            case "month":
                filterExpensesByMonth();
                break;
            default:
                // Dacă nu există mod selectat, nu facem nimic
                break;
        }
    }

    // Actualizare RecyclerView
}
