package com.minifocus.launcher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minifocus.launcher.R

@Composable
fun AllSetScreen(onBegin: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0E14)) // Deep dark background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.weight(0.25f))
            
            // Checkmark with concentric rings
            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Outer rings
                    drawCircle(
                        color = Color.White.copy(alpha = 0.03f),
                        radius = size.width / 2.2f,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = size.width / 3.0f,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    
                    // Small "stars"
                    drawCircle(
                        color = Color.White.copy(alpha = 0.7f),
                        radius = 2.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.25f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        radius = 1.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.4f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = 1.5.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height * 0.8f)
                    )
                }
                
                // Dark circle for checkmark
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B1D22)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "All set",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "All set",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "You're good to go.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal
            )
            
            Spacer(modifier = Modifier.weight(0.35f))
            
            // Button at the bottom
            Button(
                onClick = onBegin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(32.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Start Focusing",
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
                    )
                }
            }
        }
    }
}
