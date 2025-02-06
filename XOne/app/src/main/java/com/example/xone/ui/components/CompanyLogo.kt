package com.example.xone.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.xone.R

@Composable
fun CompanyLogo(modifier: Modifier = Modifier) {
    // Load your logo image
    Image(
        painter = painterResource(id = R.drawable.netcon), // Replace 'logo' with your actual logo resource
        contentDescription = "Company Logo",
        modifier = modifier
    )
}

@Preview
@Composable
fun PreviewCompanyLogo() {
    CompanyLogo(modifier = Modifier.height(120.dp)) // Adjust the size of the logo
}
