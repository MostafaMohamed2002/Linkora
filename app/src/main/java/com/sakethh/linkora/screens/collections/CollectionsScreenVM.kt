package com.sakethh.linkora.screens.collections

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.localDB.CustomFunctionsForLocalDB
import com.sakethh.linkora.localDB.dto.FoldersTable
import com.sakethh.linkora.screens.settings.SettingsScreenVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CollectionsScreenVM : ViewModel() {
    private val _foldersData = MutableStateFlow(emptyList<FoldersTable>())
    val foldersData = _foldersData.asStateFlow()

    companion object {
        val selectedFolderData =
            FoldersTable(
                folderName = "", infoForSaving = "", parentFolderID = 0,
                childFolderIDs = emptyList()
            )
    }

    init {
        changeRetrievedFoldersData(
            sortingPreferences = SettingsScreenVM.SortingPreferences.valueOf(
                SettingsScreenVM.Settings.selectedSortingType.value
            )
        )
    }

    fun changeRetrievedFoldersData(sortingPreferences: SettingsScreenVM.SortingPreferences) {
        when (sortingPreferences) {
            SettingsScreenVM.SortingPreferences.A_TO_Z -> {
                viewModelScope.launch {
                    CustomFunctionsForLocalDB.localDB.regularFolderSorting().sortByAToZ()
                        .collect {
                            _foldersData.emit(it)
                        }
                }
            }

            SettingsScreenVM.SortingPreferences.Z_TO_A -> {
                viewModelScope.launch {
                    CustomFunctionsForLocalDB.localDB.regularFolderSorting().sortByZToA()
                        .collect {
                            _foldersData.emit(it)
                        }
                }
            }

            SettingsScreenVM.SortingPreferences.NEW_TO_OLD -> {
                viewModelScope.launch {
                    CustomFunctionsForLocalDB.localDB.regularFolderSorting()
                        .sortByLatestToOldest()
                        .collect {
                            _foldersData.emit(it)
                        }
                }
            }

            SettingsScreenVM.SortingPreferences.OLD_TO_NEW -> {
                viewModelScope.launch {
                    CustomFunctionsForLocalDB.localDB.regularFolderSorting()
                        .sortByOldestToLatest()
                        .collect {
                            _foldersData.emit(it)
                        }
                }
            }
        }
    }

    fun onNoteDeleteClick(context: Context, clickedFolderID: Long) {
        viewModelScope.launch {
            CustomFunctionsForLocalDB.localDB.deleteDao()
                .deleteAFolderNote(folderID = clickedFolderID)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "deleted the note", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onDeleteClick(clickedFolderID: Long) {
        viewModelScope.launch {
            kotlinx.coroutines.awaitAll(async {
                CustomFunctionsForLocalDB.localDB.deleteDao()
                    .deleteAFolder(folderID = clickedFolderID)
            }, async {
                CustomFunctionsForLocalDB.localDB.deleteDao()
                    .deleteThisFolderData(folderID = clickedFolderID)
            })
        }

    }
}