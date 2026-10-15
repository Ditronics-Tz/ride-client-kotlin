package com.example.ridepassenger2.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridepassenger2.ui.components.RidaIcon
import com.example.ridepassenger2.ui.components.RidaIcons
import com.example.ridepassenger2.ui.theme.RidaMintDark

/** Shared low-end friendly components. No heavy animation. 48dp touch targets. */

@Composable
fun MapPlaceholder(modifier: Modifier = Modifier, label: String = "Ramani") {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE8ECE9))
            .border(1.dp, Color(0xFFD5DBD7), RoundedCornerShape(16.dp))
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            RidaIcon(RidaIcons.Map, contentDescription = null, modifier = Modifier.size(32.dp), tint = RidaMintDark)
            Spacer(Modifier.height(4.dp))
            Text(text = label, fontSize = 12.sp, color = Color(0xFF687771))
        }
    }
}

@Composable
fun StatusChip(text: String, tone: ChipTone = ChipTone.NEUTRAL) {
    val (bg, fg) = when (tone) {
        ChipTone.OK -> Color(0xFFE6F6EE) to Color(0xFF15803D)
        ChipTone.WARN -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        ChipTone.BAD -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
        ChipTone.INFO -> Color(0xFFE0F2FE) to Color(0xFF075985)
        ChipTone.NEUTRAL -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .semantics { contentDescription = text }
    ) { Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = fg) }
}

enum class ChipTone { OK, WARN, BAD, INFO, NEUTRAL }

@Composable
fun VerifyChip(state: VerifyState, modifier: Modifier = Modifier) {
    val (t, tone) = when (state) {
        VerifyState.VERIFIED -> "Imethibitishwa" to ChipTone.OK
        VerifyState.PENDING -> "Inasubiri" to ChipTone.WARN
        VerifyState.UNVERIFIED -> "Haijathibitishwa" to ChipTone.NEUTRAL
        VerifyState.FAILED -> "Imeshindikana" to ChipTone.BAD
    }
    Box(modifier = modifier) { StatusChip(text = t, tone = tone) }
}

enum class VerifyState { VERIFIED, UNVERIFIED, PENDING, FAILED }

@Composable
fun VerifyBanner(text: String, tone: ChipTone = ChipTone.WARN, modifier: Modifier = Modifier) {
    val bg = when (tone) {
        ChipTone.OK -> Color(0xFFE6F6EE); ChipTone.WARN -> Color(0xFFFEF3C7)
        ChipTone.BAD -> Color(0xFFFEE2E2); ChipTone.INFO -> Color(0xFFE0F2FE)
        ChipTone.NEUTRAL -> Color(0xFFF1F5F9)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(12.dp)
    ) { Text(text = text, fontSize = 12.5.sp, color = Color(0xFF111827)) }
}

@Composable
fun RidaField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    error: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder.ifBlank { label }) },
            singleLine = true,
            isError = error != null,
            supportingText = error?.let { { Text(text = it) } },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .semantics { contentDescription = label }
        )
    }
}

@Composable
fun RidaPrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true, loading: Boolean = false) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RidaMintDark, contentColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        if (loading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
        else Text(text = text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RidaGhostButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) { Text(text = text) }
}

@Composable
fun FlowRow(title: String, subtitle: String = "", onClick: () -> Unit, trailing: @Composable (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE8ECE9), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotBlank()) Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF687771))
        }
        trailing?.invoke() ?: RidaIcon(RidaIcons.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF9AA6A1))
    }
}

@Composable
fun FlowScaffold(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onBack)
                    .semantics { contentDescription = "Rudi nyuma" },
                contentAlignment = Alignment.Center
            ) { RidaIcon(RidaIcons.Back, contentDescription = null, modifier = Modifier.size(24.dp)) }
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        content()
        Spacer(Modifier.height(24.dp))
    }
}
