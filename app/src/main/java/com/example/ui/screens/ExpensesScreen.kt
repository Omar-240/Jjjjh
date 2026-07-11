package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Expense
import com.example.viewmodel.MainViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: MainViewModel) {
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val accentColor = viewModel.getAccentColor()

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("All") } // All, Expense, Income

    val filteredExpenses = expenses.filter {
        when (selectedTab) {
            "Expense" -> it.isExpense
            "Income" -> !it.isExpense
            else -> true
        }
    }

    // Financial calculations
    val totalIncome = expenses.filter { !it.isExpense }.sumOf { it.amount }
    val totalExpense = expenses.filter { it.isExpense }.sumOf { it.amount }
    val netBalance = totalIncome - totalExpense

    val df = DecimalFormat("$#,##0.00")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracker", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = "dashboard" }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add Transaction", color = Color.White) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White) },
                onClick = {
                    viewModel.selectedExpense = null
                    viewModel.expenseTitleInput = ""
                    viewModel.expenseAmountInput = ""
                    viewModel.expenseIsExpenseInput = true
                    viewModel.expenseCategoryInput = "Food"
                    viewModel.expenseWalletInput = "Cash"
                    showAddExpenseDialog = true
                },
                containerColor = accentColor,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_expense_fab")
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // FINANCIAL SUMMARY CARD (BALANCE)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Net Account Balance",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = df.format(netBalance),
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = if (netBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Income", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Income", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(df.format(totalIncome), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF10B981))
                        }

                        Box(modifier = Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))

                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Expense", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Expenses", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(df.format(totalExpense), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFEF4444))
                        }
                    }
                }
            }

            // BUDGET LIMITS PROGRESS BAR
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Monthly Spending Budget", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "${df.format(totalExpense)} of ${df.format(viewModel.expenseBudgetLimit)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalExpense > viewModel.expenseBudgetLimit) Color(0xFFEF4444) else accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val limitProgress = if (viewModel.expenseBudgetLimit > 0) (totalExpense / viewModel.expenseBudgetLimit).toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { limitProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (totalExpense > viewModel.expenseBudgetLimit) Color(0xFFEF4444) else accentColor,
                        trackColor = accentColor.copy(alpha = 0.1f)
                    )
                }
            }

            // SPENDING ANALYSIS CUSTOM CANVAS CHART
            Text(
                "Spending Analysis Chart",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                // Custom drawn bar chart for spending category breakdown
                val categories = listOf("Food", "Office", "Leisure", "Travel", "Utility")
                val categorySpending = categories.associateWith { cat ->
                    expenses.filter { it.isExpense && it.category == cat }.sumOf { it.amount }
                }
                val maxSpend = categorySpending.values.maxOrNull()?.coerceAtLeast(10.0) ?: 100.0

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    val width = size.width
                    val height = size.height

                    // Draw baseline
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(0f, height - 30f),
                        end = Offset(width, height - 30f),
                        strokeWidth = 2f
                    )

                    val barWidth = (width / categories.size) - 20f
                    categories.forEachIndexed { idx, cat ->
                        val spend = categorySpending[cat] ?: 0.0
                        val barHeight = ((spend / maxSpend) * (height - 60f)).toFloat()

                        val startX = idx * (width / categories.size) + 10f
                        val startY = height - 30f

                        // Draw bar
                        drawRect(
                            color = if (spend > 0) accentColor else accentColor.copy(alpha = 0.1f),
                            topLeft = Offset(startX, startY - barHeight),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                        )
                    }
                }
            }

            // TRANSACTIONS TABS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Expense", "Income").forEach { tab ->
                    val isSelected = selectedTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        label = { Text(tab) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.2f),
                            selectedLabelColor = accentColor
                        )
                    )
                }
            }

            // TRANSACTIONS LIST
            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No transactions found.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredExpenses.forEach { exp ->
                        ExpenseRowItem(
                            expense = exp,
                            accentColor = accentColor,
                            onEdit = {
                                viewModel.selectedExpense = exp
                                viewModel.expenseTitleInput = exp.title
                                viewModel.expenseAmountInput = exp.amount.toString()
                                viewModel.expenseIsExpenseInput = exp.isExpense
                                viewModel.expenseCategoryInput = exp.category
                                viewModel.expenseWalletInput = exp.wallet
                                showAddExpenseDialog = true
                            },
                            onDelete = { viewModel.deleteExpense(exp.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // ADD / EDIT TRANSACTIONS DIALOG MODAL
    if (showAddExpenseDialog) {
        val categories = listOf("Food", "Office", "Leisure", "Travel", "Utility", "Income")
        val wallets = listOf("Cash", "Bank Card", "PayPal", "Apple Pay")

        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = {
                Text(
                    text = if (viewModel.selectedExpense == null) "Add Transaction" else "Edit Transaction",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // TRANSACTION TYPE SELECTOR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.expenseIsExpenseInput = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.expenseIsExpenseInput) Color(0xFFEF4444) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Expense", color = if (viewModel.expenseIsExpenseInput) Color.White else MaterialTheme.colorScheme.onSurface)
                        }

                        Button(
                            onClick = { viewModel.expenseIsExpenseInput = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!viewModel.expenseIsExpenseInput) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Income", color = if (!viewModel.expenseIsExpenseInput) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    OutlinedTextField(
                        value = viewModel.expenseTitleInput,
                        onValueChange = { viewModel.expenseTitleInput = it },
                        label = { Text("Title / Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_title_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor)
                    )

                    OutlinedTextField(
                        value = viewModel.expenseAmountInput,
                        onValueChange = { viewModel.expenseAmountInput = it },
                        label = { Text("Amount ($)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_amount_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
                        singleLine = true
                    )

                    // CATEGORIES
                    Text("Category", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = viewModel.expenseCategoryInput == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.expenseCategoryInput = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor.copy(alpha = 0.2f),
                                    selectedLabelColor = accentColor
                                )
                            )
                        }
                    }

                    // WALLET
                    Text("Wallet / Source", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        wallets.forEach { wal ->
                            val isSelected = viewModel.expenseWalletInput == wal
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.expenseWalletInput = wal },
                                label = { Text(wal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor.copy(alpha = 0.2f),
                                    selectedLabelColor = accentColor
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveExpense()
                        showAddExpenseDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier.testTag("save_expense_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun ExpenseRowItem(
    expense: Expense,
    accentColor: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val df = DecimalFormat("$#,##0.00")
    val color = if (expense.isExpense) Color(0xFFEF4444) else Color(0xFF10B981)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_item_${expense.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (expense.isExpense) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = if (expense.isExpense) "Expense" else "Income",
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = expense.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = expense.category,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = expense.wallet,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (expense.isExpense) "-" else "+"}${df.format(expense.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = color,
                    modifier = Modifier.padding(end = 6.dp)
                )

                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
