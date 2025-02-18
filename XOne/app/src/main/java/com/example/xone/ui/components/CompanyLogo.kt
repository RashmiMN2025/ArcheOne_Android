package com.example.xone.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xone.R

@Composable
fun CompanyLogo(modifier: Modifier = Modifier) {
    // Load your logo image
    Image(
        painter = painterResource(id = R.drawable.arche), // Changed from netcon to arche
        contentDescription = "Company Logo",
        modifier = Modifier
            .height(40.dp)  // Reduced from 30.dp to 20.dp
            .then(modifier)  // Apply any other modifiers after setting height
    )
}

@Preview
@Composable
fun PreviewCompanyLogo() {
    CompanyLogo()  // No need to pass height modifier anymore
}
