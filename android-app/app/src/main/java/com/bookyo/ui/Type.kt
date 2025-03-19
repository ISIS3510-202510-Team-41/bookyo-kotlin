package com.bookyo.ui

import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.bookyo.R


private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)



val ParkinsansFont = GoogleFont(name = "Parkinsans")

val RobotoFont = GoogleFont(name = "Roboto")

val RobotoFontFamily = FontFamily(
    Font(googleFont = RobotoFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = RobotoFont, fontProvider = provider, weight = FontWeight.Normal)
)

val ParkinsansFontFamily = FontFamily(
    Font(googleFont = ParkinsansFont, fontProvider = provider, weight = FontWeight.SemiBold)
)

val typography = Typography(
    displayLarge = TextStyle(
        fontFamily = ParkinsansFontFamily,
        fontSize = 64.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = RobotoFontFamily,
        fontSize = 26.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = RobotoFontFamily,
        fontSize = 16.sp
    )
)