package com.snaplab.cameraft.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snaplab.cameraft.models.WatermarkCategory
import com.snaplab.cameraft.models.WatermarkTemplate
import com.snaplab.cameraft.services.LocalizationManager
import com.snaplab.cameraft.ui.theme.SnapCardDark
import com.snaplab.cameraft.ui.theme.SnapSafetyOrange

@Composable
fun TemplateSelectorDrawer(
    selectedTemplate: WatermarkTemplate,
    templates: List<WatermarkTemplate>,
    onSelectTemplate: (WatermarkTemplate) -> Unit,
    onCustomizeTemplate: (WatermarkTemplate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loc = LocalizationManager.shared
    val isVi = loc.isVietnamese()

    var selectedCategory by remember { mutableStateOf<WatermarkCategory?>(null) }

    val filteredTemplates = remember(selectedCategory, templates) {
        if (selectedCategory == null) templates
        else templates.filter { it.category == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(Color(0xF5181A20))
            .padding(vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = loc.t("templates"),
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CategoryPill(
                    title = loc.t("all"),
                    isSelected = selectedCategory == null,
                    onClick = { selectedCategory = null }
                )
            }
            items(WatermarkCategory.values()) { cat ->
                CategoryPill(
                    title = cat.getDisplayName(isVi),
                    isSelected = selectedCategory == cat,
                    onClick = { selectedCategory = cat }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Templates Cards Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredTemplates) { tmpl ->
                val isSelected = tmpl.id == selectedTemplate.id
                TemplateCard(
                    template = tmpl,
                    isSelected = isSelected,
                    onSelect = { onSelectTemplate(tmpl) },
                    onCustomize = { onCustomizeTemplate(tmpl) }
                )
            }
        }
    }
}

@Composable
private fun CategoryPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) SnapSafetyOrange else Color.White.copy(alpha = 0.12f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun TemplateCard(
    template: WatermarkTemplate,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onCustomize: () -> Unit
) {
    val accentColor = template.colorTheme.composeColor

    Column(
        modifier = Modifier
            .width(190.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF22252E))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onSelect() }
            .padding(12.dp)
    ) {
        // Top row: Template name + Selected Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = template.name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Badge Preview Miniature
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF14161B))
                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(6.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (template.titleText.isNotBlank()) template.titleText else template.name,
                    color = accentColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "📍 " + (template.projectName.ifBlank { "GPS Coordinates" }),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 8.sp,
                    maxLines = 1
                )
                Text(
                    text = "🕒 2026-09-30 08:48:13",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 7.5.sp,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action row: Customize button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .clickable { onCustomize() }
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = LocalizationManager.shared.t("customizeTemplate"),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
