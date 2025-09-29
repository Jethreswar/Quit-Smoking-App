package com.example.quitesmoking.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.quitesmoking.model.DailySmokingStatus
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar

@Composable
fun SmokingCalendarView(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    smokingStatuses: List<DailySmokingStatus>,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance().apply { time = selectedDate }) }
    var showYearPicker by remember { mutableStateOf(false) }
    
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayNames = remember { listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat") }
    
    // Memoized month navigation functions for zero latency
    val navigateToPreviousMonth = remember {
        {
            // Create new calendar instance to avoid mutation issues and ensure immediate response
            val newCalendar = Calendar.getInstance().apply {
                time = currentMonth.time
                add(Calendar.MONTH, -1)
            }
            currentMonth = newCalendar
        }
    }
    
    val navigateToNextMonth = remember {
        {
            // Create new calendar instance to avoid mutation issues and ensure immediate response
            val newCalendar = Calendar.getInstance().apply {
                time = currentMonth.time
                add(Calendar.MONTH, 1)
            }
            currentMonth = newCalendar
        }
    }
    
    // Update current month when selectedDate changes (only when necessary)
    LaunchedEffect(selectedDate) {
        val newCalendar = Calendar.getInstance().apply { time = selectedDate }
        if (newCalendar.get(Calendar.YEAR) != currentMonth.get(Calendar.YEAR) || 
            newCalendar.get(Calendar.MONTH) != currentMonth.get(Calendar.MONTH)) {
            currentMonth = newCalendar
        }
    }
    
    Column(modifier = modifier) {
        // Enhanced Month/Year navigation header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Month/Year display with click to change year
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous month button - optimized for zero latency
                    IconButton(
                        onClick = navigateToPreviousMonth,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            ),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft, 
                            contentDescription = "Previous month",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    // Month/Year display with year picker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showYearPicker = true }
                    ) {
                        Text(
                            text = monthFormat.format(currentMonth.time),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Tap to change year",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Change year",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Next month button - optimized for zero latency
                    IconButton(
                        onClick = navigateToNextMonth,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            ),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowRight, 
                            contentDescription = "Next month",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Quick navigation row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Jump to today button
                    OutlinedButton(
                        onClick = {
                            val today = Calendar.getInstance()
                            currentMonth = today
                            onDateSelected(today.time)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Today,
                            contentDescription = "Today",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Today")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Jump to selected date button
                    OutlinedButton(
                        onClick = {
                            val selectedCal = Calendar.getInstance().apply { time = selectedDate }
                            currentMonth = selectedCal
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Selected")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Day names header with better styling
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dayNames.forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calendar grid - optimized for performance with memoized month changes
        CalendarGrid(
            currentMonth = currentMonth,
            smokingStatuses = smokingStatuses,
            onDateSelected = onDateSelected,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Legend
        Spacer(modifier = Modifier.height(16.dp))
        CalendarLegend()
    }
    
    // Year picker dialog
    if (showYearPicker) {
        YearPickerDialog(
            currentYear = currentMonth.get(Calendar.YEAR),
            onYearSelected = { year ->
                currentMonth = Calendar.getInstance().apply {
                    time = currentMonth.time
                    set(Calendar.YEAR, year)
                }
                showYearPicker = false
            },
            onDismiss = { showYearPicker = false }
        )
    }
}

@Composable
private fun YearPickerDialog(
    currentYear: Int,
    onYearSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableStateOf(currentYear) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Year",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Year selection with arrows
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = { selectedYear -= 1 }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null)
                    }
                    
                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(
                        onClick = { selectedYear += 1 }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next year")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Quick year buttons
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(120.dp)
                ) {
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val years = (currentYear - 10..currentYear + 10).toList()
                    
                    items(years) { year ->
                        OutlinedButton(
                            onClick = { selectedYear = year },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (year == selectedYear) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    Color.Transparent
                            )
                        ) {
                            Text(year.toString())
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { onYearSelected(selectedYear) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Select")
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: Calendar,
    smokingStatuses: List<DailySmokingStatus>,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
    val calendar = currentMonth.clone() as Calendar
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    
    // Get the first day of the month and adjust to start of week
    val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)
    val startOffset = if (firstDayOfMonth == Calendar.SUNDAY) 0 else firstDayOfMonth - 1
    
    // Calculate total days needed (including empty cells for padding)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val calendarDays = mutableListOf<CalendarDay>()
    
    // Add empty cells for days before the month starts
    repeat(startOffset) {
        calendarDays.add(CalendarDay.Empty)
    }
    
    // Add all days of the month
    for (day in 1..daysInMonth) {
        calendar.set(Calendar.DAY_OF_MONTH, day)
        val date = calendar.time
        
        // Find smoking status for this date
        val status = smokingStatuses.find { status ->
            val statusCal = Calendar.getInstance().apply { time = status.date }
            val dayCal = Calendar.getInstance().apply { time = date }
            statusCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
            statusCal.get(Calendar.MONTH) == dayCal.get(Calendar.MONTH) &&
            statusCal.get(Calendar.DAY_OF_MONTH) == dayCal.get(Calendar.DAY_OF_MONTH)
        }
        
        calendarDays.add(
            CalendarDay.CalendarDate(
                date = date,
                dayNumber = day,
                smokingStatus = status
            )
        )
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
    ) {
        items(calendarDays) { calendarDay ->
            when (calendarDay) {
                is CalendarDay.Empty -> {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                    )
                }
                is CalendarDay.CalendarDate -> {
                    CalendarDayCell(
                        calendarDay = calendarDay,
                        onDateSelected = onDateSelected,
                        modifier = Modifier.aspectRatio(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    calendarDay: CalendarDay.CalendarDate,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = remember(calendarDay.date) {
        val today = Calendar.getInstance()
        val dayCal = Calendar.getInstance().apply { time = calendarDay.date }
        today.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
        today.get(Calendar.MONTH) == dayCal.get(Calendar.MONTH) &&
        today.get(Calendar.DAY_OF_MONTH) == dayCal.get(Calendar.DAY_OF_MONTH)
    }
    
    val backgroundColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        calendarDay.smokingStatus?.isSmokeFree == true -> Color(0xFF4CAF50)
        calendarDay.smokingStatus?.isSmokeFree == false -> Color(0xFFFF5722)
        else -> Color.Transparent
    }
    
    val borderColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    
    val textColor = when {
        isToday -> MaterialTheme.colorScheme.onPrimary
        calendarDay.smokingStatus != null -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val borderWidth = if (isToday) 3.dp else 1.dp
    
    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = CircleShape
            )
            .clickable { onDateSelected(calendarDay.date) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = calendarDay.dayNumber.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            // Show smoking status indicator with better spacing
            calendarDay.smokingStatus?.let { status ->
                Spacer(modifier = Modifier.height(2.dp))
                if (status.isSmokeFree) {
                    Text(
                        text = "✓",
                        fontSize = 12.sp,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "✗",
                        fontSize = 12.sp,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarLegend() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(
                color = Color(0xFF4CAF50),
                label = "Smoke-free",
                modifier = Modifier.weight(1f)
            )
            LegendItem(
                color = Color(0xFFFF5722),
                label = "Smoked",
                modifier = Modifier.weight(1f)
            )
            LegendItem(
                color = MaterialTheme.colorScheme.primary,
                label = "Today",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

sealed class CalendarDay {
    object Empty : CalendarDay()
    data class CalendarDate(
        val date: Date,
        val dayNumber: Int,
        val smokingStatus: DailySmokingStatus?
    ) : CalendarDay()
}
