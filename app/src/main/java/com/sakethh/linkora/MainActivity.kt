package com.sakethh.linkora

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.sakethh.linkora.localDB.LocalDataBase
import com.sakethh.linkora.localDB.commonVMs.UpdateVM
import com.sakethh.linkora.navigation.BottomNavigationBar
import com.sakethh.linkora.navigation.MainNavigation
import com.sakethh.linkora.navigation.NavigationRoutes
import com.sakethh.linkora.navigation.NavigationVM
import com.sakethh.linkora.screens.settings.SettingsScreenVM
import com.sakethh.linkora.screens.settings.SettingsScreenVM.Settings.dataStore
import com.sakethh.linkora.ui.theme.LinkoraTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            SettingsScreenVM.Settings.readAllPreferencesValues(this@MainActivity)
        }
        if (SettingsScreenVM.Settings.isSendCrashReportsEnabled.value) {
            val firebaseCrashlytics = FirebaseCrashlytics.getInstance()
            firebaseCrashlytics.setCrashlyticsCollectionEnabled(true)
            firebaseCrashlytics.log("logged in :- v${SettingsScreenVM.currentAppVersion}")
        }
        setContent {
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            LinkoraTheme {
                val navController = rememberNavController()
                val bottomBarSheetState =
                    androidx.compose.material.rememberBottomSheetScaffoldState()
                val currentBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = rememberSaveable(
                    inputs = arrayOf(currentBackStackEntry.value?.destination?.route)
                ) {
                    currentBackStackEntry.value?.destination?.route.toString()
                }
                val systemUIController = rememberSystemUiController()
                systemUIController.setNavigationBarColor(NavigationVM.btmNavBarContainerColor.value)
                systemUIController.setStatusBarColor(MaterialTheme.colorScheme.surface)
                LaunchedEffect(key1 = currentRoute) {
                    if (NavigationRoutes.values().any {
                            it.name != NavigationRoutes.SPECIFIC_SCREEN.name && it.name == currentRoute && it.name != NavigationRoutes.ARCHIVE_SCREEN.name
                        }) {

                        if (bottomBarSheetState.bottomSheetState.isCollapsed) {
                            coroutineScope.launch {
                                bottomBarSheetState.bottomSheetState.expand()
                            }
                        }
                    } else {
                        if (bottomBarSheetState.bottomSheetState.isExpanded) {
                            coroutineScope.launch {
                                bottomBarSheetState.bottomSheetState.collapse()
                            }
                        }
                    }
                }
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.material.BottomSheetScaffold(sheetPeekHeight = 0.dp,
                        sheetGesturesEnabled = false,
                        scaffoldState = bottomBarSheetState,
                        sheetContent = {
                            BottomNavigationBar(navController = navController)
                        }) {
                        Scaffold {
                            MainNavigation(navController = navController)
                        }
                    }
                }
            }
            LocalDataBase.localDB = LocalDataBase.getLocalDB(context = context)
            LaunchedEffect(key1 = Unit) {
                if (SettingsScreenVM.Settings.readSettingPreferenceValue(
                        preferenceKey = booleanPreferencesKey(
                            SettingsScreenVM.SettingsPreferences.IS_DATA_MIGRATION_COMPLETED_FROM_V9.name
                        ), dataStore = context.dataStore
                    ) == null
                ) {
                    LocalDataBase.localDB.readDao().getAllArchiveFoldersV9().collect {
                        if (it.isNotEmpty()) {
                            UpdateVM().migrateArchiveFoldersV9toV10()
                        }
                    }
                    LocalDataBase.localDB.readDao().getAllRootFolders().collect {
                        if (it.isNotEmpty()) {
                            UpdateVM().migrateRegularFoldersLinksDataFromV9toV10()
                        }
                    }
                    async {
                        SettingsScreenVM.Settings.changeSettingPreferenceValue(
                            preferenceKey = booleanPreferencesKey(
                                SettingsScreenVM.SettingsPreferences.IS_DATA_MIGRATION_COMPLETED_FROM_V9.name
                            ), dataStore = context.dataStore, newValue = true
                        )
                    }.await()
                    Toast.makeText(context, "Data Migrated successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}