package com.tored.bridgelauncher.ui.settings.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tored.bridgelauncher.R
import com.tored.bridgelauncher.composables.Btn
import com.tored.bridgelauncher.services.settings.SettingsState
import com.tored.bridgelauncher.ui.settings.SettingsCheckboxFieldFor
import com.tored.bridgelauncher.ui.settings.SettingsSection
import com.tored.bridgelauncher.utils.tryStartWallpaperPickerActivity

@Composable
fun SettingsSystemWallpaperSection()
{
    val context = LocalContext.current

    SettingsSection(label = "System wallpaper", iconResId = R.drawable.ic_image)
    {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        )
        {
            Btn(
                text = "Change system wallpaper",
                modifier = Modifier
                    .fillMaxWidth(),
                outlined = true,
                onClick = { context.tryStartWallpaperPickerActivity() },
            )

            SettingsCheckboxFieldFor(SettingsState::drawSystemWallpaperBehindWebView)
        }
    }
}