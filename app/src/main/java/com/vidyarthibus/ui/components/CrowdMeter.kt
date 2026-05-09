package com.vidyarthibus.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vidyarthibus.data.model.CrowdLevel
import com.vidyarthibus.data.model.CrowdStatus
import com.vidyarthibus.ui.theme.*

@Composable
fun CrowdMeter(
    crowdStatus: CrowdStatus,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = crowdStatus.percentage,
        animationSpec = tween(durationMillis = 600, easing = EaseOutCubic),
        label = "crowd_progress"
    )

    val (barColor, bgColor, levelText) = when (crowdStatus.level) {
        CrowdLevel.GREEN  -> Triple(CrowdGreen, CrowdGreenBg, "Seats Available")
        CrowdLevel.YELLOW -> Triple(CrowdYellow, CrowdYellowBg, "Filling Up")
        CrowdLevel.RED    -> Triple(CrowdRed, CrowdRedBg, "Bus Full")
    }

    val percentText = "${(crowdStatus.percentage * 100).toInt()}%"
    val a11yLabel  = "Crowd meter: $levelText, $percentText occupied. " +
            "${crowdStatus.activeReporters} reporters active."

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = a11yLabel },
        colors   = CardDefaults.cardColors(containerColor = bgColor),
        shape    = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text  = "Crowd Meter",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // Coloured chip
                Surface(
                    color = barColor,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text     = levelText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color    = Color.White,
                        style    = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Progress bar — NFR-08: minimum contrast ratio WCAG AA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .clip(RoundedCornerShape(11.dp))
            ) {
                // Background track
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = barColor.copy(alpha = 0.2f)
                ) {}
                // Filled portion
                LinearProgressIndicator(
                    progress          = animatedProgress,
                    modifier          = Modifier.fillMaxSize(),
                    color             = barColor,
                    trackColor        = Color.Transparent
                )
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = "$percentText full",
                    style = MaterialTheme.typography.bodyMedium,
                    color = barColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = "${crowdStatus.activeReporters} reporting",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Zone legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = CrowdGreen,  label = "0-40%  Free")
                LegendItem(color = CrowdYellow, label = "41-70% Filling")
                LegendItem(color = CrowdRed,    label = "71-100% Full")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            modifier = Modifier.size(10.dp),
            shape    = RoundedCornerShape(2.dp),
            color    = color
        ) {}
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}