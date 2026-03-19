# Ezya - Finance Calculator

A personal finance tracking application for Android. Track income and expenses by category, analyze spending with charts, and review history across periods.

## Features

- Period-based budgeting (week, month, year)
- Income and expense categories with custom emoji
- Bar charts for visual spending analysis
- Transaction calendar with daily breakdown
- Period history with archived results
- Light and dark theme
- Russian and English localization

## How to Use

### 1. First Launch

On the welcome screen tap "Start" to begin setup. You will be taken through a two-step budget configuration.

### 2. Setting Up Income Categories

Choose a period length — week, month, or year. Then add your income sources. For each category enter a name, select an emoji icon, and enter the planned amount. Add as many categories as needed.

### 3. Setting Up Expense Categories

After confirming your income, add expense categories. The total across all expense categories cannot exceed your planned income. Remaining budget is shown in real time as you add categories. Tap any existing category to edit it.

### 4. Main Dashboard

The dashboard shows two tabs — Income and Expenses — which you can switch between by swiping left or right. Each tab contains a bar chart of transactions by category and a transaction history list. Tap any bar in the chart to see the category name and amount.

### 5. Adding Transactions

Tap "Add transaction" to record an actual income or expense. The type is pre-selected based on the currently active tab. Choose a category from the list, enter the amount, and optionally add a comment.

### 6. Calendar

Tap the calendar button in the top left corner of the dashboard. The calendar shows the current month. Days with transactions are marked with green dots for income and red dots for expenses. Tap any marked day to see a detailed breakdown of all transactions for that day, including totals and balance.

### 7. Ending a Period

When the period expires, the summary screen opens automatically. You can also end a period early using the "End period early" button. The summary shows total income, total expenses, and savings or overspending per category.

### 8. Starting a New Period

After reviewing the summary, tap "Start new period". Your categories are preserved so you do not need to set them up again. You can add, remove, or edit categories before confirming the new period.

### 9. History

Tap "History" on the dashboard to browse all completed periods. Each period shows its start and end dates. Swipe between periods using the tabs at the top. Inside each period you can view income and expense charts and tap "Results" to see the full summary.

### 10. Settings

Open settings via the gear icon on the dashboard. Available options:

- Toggle between dark and light theme
- Switch interface language between Russian and English
- Clear all archived history while keeping the current period intact
- Rate the app or report a bug

## Installation

Clone the repository and open in Android Studio. Build and run on a device or emulator with Android 7.0 (API 24) or higher.
```bash
git clone https://github.com/Toster1012/EzyaFinancialController.git
```

Open in Android Studio, wait for Gradle sync to complete, then run the project.

## Requirements

- Android 7.0 (API level 24) or higher
- Android Studio Hedgehog or newer

## Tech Stack

- Java
- Room (SQLite)
- LiveData
- ViewPager2
- Material Components
- Custom BarChartView (canvas-based)

## License

MIT License

Copyright (c) 2026 Vafelny Toster

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
