package com.palletflow.presentation.screens.reports

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val AccentTeal = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val PlaceholderColor = Color(0xFF2C2C2E)
private val NegativeRed = Color(0xFFFF453A)
private val NeutralBlue = Color(0xFF4A90E2)
private val WarningAmber = Color(0xFFFF9500)

@Composable
fun ReportsScreen() {
    var selectedDateIndex by remember { mutableIntStateOf(1) } // Default "This Week"
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val dateFilters = listOf("Today", "This Week", "This Month", "This Year", "Custom")
    val tabs = listOf("Overview", "By Lot", "By Platform")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // 1. Date range filter
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dateFilters.size) { index ->
                FilterChip(
                    selected = selectedDateIndex == index,
                    onClick = { selectedDateIndex = index },
                    label = { Text(dateFilters[index]) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentTeal,
                        selectedLabelColor = DarkBackground,
                        containerColor = PlaceholderColor,
                        labelColor = TextSecondary
                    ),
                    border = null,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // 2. TabRow
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkBackground,
            contentColor = AccentTeal,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AccentTeal
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) AccentTeal else TextSecondary,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> OverviewTab()
                1 -> ByLotTab()
                2 -> ByPlatformTab()
            }
        }
    }
}

@Composable
fun OverviewTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        // Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Summary", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                SummaryRow("Revenue", "+$8,500", AccentTeal)
                SummaryRow("Lot Costs", "-$5,200", NegativeRed)
                SummaryRow("Expenses", "-$980", NegativeRed)
                HorizontalDivider(color = PlaceholderColor, modifier = Modifier.padding(vertical = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Net Profit", color = TextSecondary, fontSize = 14.sp)
                    Text("+$2,320", color = AccentTeal, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        // Bar Chart Placeholder
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Revenue by Day", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    val heights = listOf(0.3f, 0.5f, 0.4f, 0.8f, 0.6f, 0.9f, 0.45f)
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")
                    heights.forEachIndexed { index, height ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(height)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(AccentTeal)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(days[index], color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Top Selling Items Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Top Selling Items", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                val topItems = listOf(
                    "Nike Air Max 270" to ("+$1,200" to "x12"),
                    "Samsung Galaxy S21" to ("+$950" to "x3"),
                    "Mechanical Keyboard" to ("+$480" to "x8"),
                    "Wireless Headphones" to ("+$320" to "x10")
                )
                topItems.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${index + 1}.", color = TextSecondary, modifier = Modifier.width(24.dp))
                        Text(item.first, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(item.second.first, color = AccentTeal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(item.second.second, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ByLotTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(Modifier.weight(1f), "Total Lots", "8", TextPrimary)
            MetricCard(Modifier.weight(1f), "Avg Profit/Lot", "+$290", AccentTeal)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val lots = listOf(
            LotData("#1042", "Amazon Returns", "Jun 15", "+$520", "42%", "Profitable", AccentTeal),
            LotData("#1041", "BULQ", "Jun 10", "+$180", "22%", "Profitable", AccentTeal),
            LotData("#1040", "Direct Liquidation", "Jun 5", "-$80", "-12%", "Loss", NegativeRed),
            LotData("#1039", "B-Stock", "May 28", "+$340", "35%", "Profitable", AccentTeal),
            LotData("#1038", "Amazon Returns", "May 20", "+$0", "0%", "Break Even", WarningAmber)
        )
        
        lots.forEach { lot ->
            LotProfitabilityCard(lot)
        }
    }
}

@Composable
fun ByPlatformTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val platforms = listOf(
            PlatformData("eBay", "42 items sold", "+$4,200", "49%", "E"),
            PlatformData("Facebook Marketplace", "28 items sold", "+$2,800", "33%", "F"),
            PlatformData("Cash", "12 items sold", "+$960", "11%", "C"),
            PlatformData("Bin Store", "8 items sold", "+$540", "6%", "B"),
            PlatformData("Other", "2 items sold", "+$0", "1%", "O")
        )
        
        platforms.forEach { platform ->
            PlatformBreakdownCard(platform)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Sales Distribution",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                    Canvas(modifier = Modifier.size(200.dp)) {
                        val stroke = 40.dp.toPx()
                        drawArc(Color(0xFF00C2A0), -90f, 176f, false, style = Stroke(stroke))
                        drawArc(Color(0xFF4A90E2), 86f, 118f, false, style = Stroke(stroke))
                        drawArc(Color(0xFFFF9500), 204f, 40f, false, style = Stroke(stroke))
                        drawArc(Color(0xFFFF453A), 244f, 22f, false, style = Stroke(stroke))
                        drawArc(Color(0xFF2C2C2E), 266f, 4f, false, style = Stroke(stroke))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total", color = TextSecondary, fontSize = 12.sp)
                        Text("+$8,500", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LegendItem(AccentTeal, "eBay")
                    LegendItem(NeutralBlue, "Facebook")
                    LegendItem(WarningAmber, "Cash")
                    LegendItem(NegativeRed, "Bin Store")
                    LegendItem(PlaceholderColor, "Other")
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun MetricCard(modifier: Modifier, label: String, value: String, valueColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

data class LotData(val id: String, val supplier: String, val date: String, val profit: String, val roi: String, val status: String, val color: Color)

@Composable
fun LotProfitabilityCard(lot: LotData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Lot ${lot.id}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(lot.supplier, color = TextSecondary, fontSize = 12.sp)
                Text(lot.date, color = TextSecondary, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(lot.profit, color = if(lot.profit.startsWith("+")) AccentTeal else NegativeRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("ROI: ${lot.roi}", color = TextSecondary, fontSize = 12.sp)
                Surface(
                    color = lot.color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        lot.status,
                        color = lot.color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

data class PlatformData(val name: String, val sold: String, val revenue: String, val percentage: String, val initial: String)

@Composable
fun PlatformBreakdownCard(platform: PlatformData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PlaceholderColor),
                contentAlignment = Alignment.Center
            ) {
                Text(platform.initial, color = AccentTeal, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(platform.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(platform.sold, color = TextSecondary, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(platform.revenue, color = AccentTeal, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(platform.percentage, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LegendFlow(content: @Composable () -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun ReportsScreenPreview() {
    ReportsScreen()
}
