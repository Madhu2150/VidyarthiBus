package com.vidyarthibus.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.vidyarthibus.ui.theme.CrowdGreen
import com.vidyarthibus.ui.theme.CrowdRed

/**
 * FR-04: One-tap crowd report button.
 * NFR-07: Discoverability — ≤ 3 taps from app open.
 */
@Composable
fun ReportButton(
    hasActiveReport: Boolean,
    isReporting: Boolean,
    onReport: () -> Unit,
    onRemoveReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (label, icon, containerColor, a11y) = if (hasActiveReport) {
        arrayOf(
            "I'm off the bus",
            Icons.Default.ExitToApp,
            CrowdRed,
            "Tap to remove your crowd report"
        )
    } else {
        arrayOf(
            "I'm on the bus",
            Icons.Default.DirectionsBus,
            CrowdGreen,
            "Tap to report you are on the bus"
        )
    }

    Button(
        onClick   = if (hasActiveReport) onRemoveReport else onReport,
        modifier  = modifier
            .fillMaxWidth()
            .height(56.dp)
            .semantics { contentDescription = a11y as String },
        enabled   = !isReporting,
        shape     = RoundedCornerShape(16.dp),
        colors    = ButtonDefaults.buttonColors(
            containerColor = containerColor as Color,
            contentColor   = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        AnimatedContent(
            targetState = isReporting,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "report_btn_content"
        ) { loading ->
            if (loading) {
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier  = Modifier.size(20.dp),
                        color     = Color.White,
                        strokeWidth = 2.dp
                    )
                    Text("Verifying location…")
                }
            } else {
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(icon as androidx.compose.ui.graphics.vector.ImageVector, null)
                    Text(
                        text  = label as String,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}