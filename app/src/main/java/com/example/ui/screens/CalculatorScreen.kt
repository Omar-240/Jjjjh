package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(viewModel: MainViewModel) {
    val accentColor = viewModel.getAccentColor()
    var isScientificMode by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf("calc") } // calc, converter

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Calculator", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = "dashboard" }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (activeTab == "calc") {
                        IconButton(onClick = { isScientificMode = !isScientificMode }) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = "Toggle Scientific Mode",
                                tint = if (isScientificMode) accentColor else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 66.dp)
            ) {
                // TABS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { activeTab = "calc" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == "calc") accentColor else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Calculator", color = if (activeTab == "calc") Color.White else MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = { activeTab = "converter" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == "converter") accentColor else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Unit Converter", color = if (activeTab == "converter") Color.White else MaterialTheme.colorScheme.onSurface)
                }
            }

            if (activeTab == "calc") {
                CalculatorTab(viewModel = viewModel, accentColor = accentColor, isScientific = isScientificMode)
            } else {
                UnitConverterTab(viewModel = viewModel, accentColor = accentColor)
            }
        }

        // Docked professional AdMob Banner at the bottom of Calculator Screen
        com.example.ads.AdBannerView(
            adUnitId = com.example.ads.AdManager.BANNER_GENERIC,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}
}

@Composable
fun CalculatorTab(viewModel: MainViewModel, accentColor: Color, isScientific: Boolean) {
    Column(modifier = Modifier.fillMaxSize()) {
        // DISPLAY SCREEN CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = viewModel.calcExpression,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = viewModel.calcDisplay,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    lineHeight = 52.sp,
                    modifier = Modifier.testTag("calc_display")
                )
            }
        }

        // BUTTONS GRID LAYOUT
        val basicKeys = listOf(
            listOf("C", "DEL", "%", "/"),
            listOf("7", "8", "9", "*"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=", "")
        )

        val scientificKeys = listOf("sin", "cos", "tan", "sqrt", "log")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isScientific) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    scientificKeys.forEach { key ->
                        Button(
                            onClick = { viewModel.handleCalcAction(key) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor.copy(alpha = 0.15f),
                                contentColor = accentColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(key, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            basicKeys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val isOperator = key in listOf("/", "*", "-", "+", "=")
                            val isAction = key in listOf("C", "DEL", "%")

                            val btnBg = when {
                                key == "=" -> accentColor
                                isOperator -> accentColor.copy(alpha = 0.15f)
                                isAction -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }

                            val btnText = when {
                                key == "=" -> Color.White
                                isOperator -> accentColor
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            Button(
                                onClick = { viewModel.handleCalcAction(key) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = btnBg,
                                    contentColor = btnText
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(if (key == "0") 2f else 1f)
                                    .height(64.dp)
                                    .testTag("calc_key_$key")
                            ) {
                                Text(
                                    text = key,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnitConverterTab(viewModel: MainViewModel, accentColor: Color) {
    val types = listOf("Length", "Weight", "Temp")
    val unitsMap = mapOf(
        "Length" to listOf("Meters", "Feet", "Kilometers", "Miles"),
        "Weight" to listOf("Kilograms", "Pounds", "Grams", "Ounces"),
        "Temp" to listOf("Celsius", "Fahrenheit")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // CONVERSION TYPE SELECTOR
        Text("Conversion Category", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.forEach { t ->
                val isSelected = viewModel.unitConvType == t
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.unitConvType = t
                        val list = unitsMap[t] ?: emptyList()
                        viewModel.unitConvFrom = list.getOrNull(0) ?: ""
                        viewModel.unitConvTo = list.getOrNull(1) ?: ""
                        viewModel.performUnitConversion()
                    },
                    label = { Text(t) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor.copy(alpha = 0.2f),
                        selectedLabelColor = accentColor
                    )
                )
            }
        }

        // INPUT FIELD
        OutlinedTextField(
            value = viewModel.unitConvValue,
            onValueChange = {
                viewModel.unitConvValue = it
                viewModel.performUnitConversion()
            },
            label = { Text("Input Value") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("unit_input"),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
            singleLine = true
        )

        // FROM UNIT SELECTOR
        Text("From Unit", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val list = unitsMap[viewModel.unitConvType] ?: emptyList()
            list.forEach { u ->
                val isSelected = viewModel.unitConvFrom == u
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.unitConvFrom = u
                        viewModel.performUnitConversion()
                    },
                    label = { Text(u) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor.copy(alpha = 0.2f),
                        selectedLabelColor = accentColor
                    )
                )
            }
        }

        // TO UNIT SELECTOR
        Text("To Unit", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val list = unitsMap[viewModel.unitConvType] ?: emptyList()
            list.forEach { u ->
                val isSelected = viewModel.unitConvTo == u
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.unitConvTo = u
                        viewModel.performUnitConversion()
                    },
                    label = { Text(u) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor.copy(alpha = 0.2f),
                        selectedLabelColor = accentColor
                    )
                )
            }
        }

        // RESULT DISPLAY
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Converted Result", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${viewModel.unitConvResult} ${viewModel.unitConvTo}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("unit_result")
                )
            }
        }
    }
}
