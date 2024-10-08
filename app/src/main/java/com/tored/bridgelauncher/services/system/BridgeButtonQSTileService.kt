package com.tored.bridgelauncher.services.system

import android.content.ComponentName
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.datastore.preferences.core.edit
import com.tored.bridgelauncher.services.settings.SettingsState
import com.tored.bridgelauncher.services.settings.settingsDataStore
import com.tored.bridgelauncher.utils.CurrentAndroidVersion
import com.tored.bridgelauncher.utils.readBool
import com.tored.bridgelauncher.utils.writeBool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BridgeButtonQSTileService : TileService()
{
    private var _job = SupervisorJob()
    private var _scope = CoroutineScope(Dispatchers.Main + _job)
    private var _isListening = false;
    private var _shouldBeActive = false;

    override fun onCreate()
    {
        _scope.launch {
            applicationContext.settingsDataStore.data.collectLatest { prefs ->
                _shouldBeActive = prefs.readBool(SettingsState::showBridgeButton, true)
                if (_isListening)
                {
                    updateTileIfListening()
                }
                else
                {
                    try
                    {
                        requestListeningState(applicationContext, ComponentName(applicationContext, BridgeButtonQSTileService::class.java))
                    }
                    catch (_: java.lang.Exception)
                    {
                        { }()
                    }
                }
            }
        }
    }

    override fun onTileAdded()
    {
        updateTileIsAdded(true)
    }

    fun updateTileIsAdded(isAdded: Boolean)
    {
        runBlocking {
            applicationContext.settingsDataStore.edit { prefs ->
                prefs.writeBool(SettingsState::isQSTileAdded, isAdded)
            }
        }
    }

    override fun onStartListening()
    {
        _isListening = true;
        updateTileIfListening();
    }

    fun updateTileIfListening()
    {
        if (!_isListening) return

        if (_shouldBeActive)
        {
            qsTile.state = Tile.STATE_ACTIVE

            if (CurrentAndroidVersion.supportsQSTileSubtitle())
                qsTile.subtitle = "Shown"
        }
        else
        {
            qsTile.state = Tile.STATE_INACTIVE

            if (CurrentAndroidVersion.supportsQSTileSubtitle())
                qsTile.subtitle = "Hidden"
        }

        qsTile.updateTile()
    }

    override fun onClick()
    {
        val showButton = qsTile.state != Tile.STATE_ACTIVE

        runBlocking {
            applicationContext.settingsDataStore.edit { prefs ->
                prefs.writeBool(SettingsState::showBridgeButton, showButton)
            }
        }
    }

    override fun onStopListening()
    {
        _isListening = false
    }

    override fun onDestroy()
    {
        _job.cancel()
    }

    override fun onTileRemoved()
    {
        updateTileIsAdded(false)
    }
}