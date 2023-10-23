package com.sakethh.linkora.localDB.dao._import

import androidx.room.Dao
import androidx.room.Insert
import com.sakethh.linkora.localDB.dto.ArchivedFolders
import com.sakethh.linkora.localDB.dto.ArchivedLinks
import com.sakethh.linkora.localDB.dto.FoldersTable
import com.sakethh.linkora.localDB.dto.ImportantLinks
import com.sakethh.linkora.localDB.dto.LinksTable
import com.sakethh.linkora.localDB.dto.RecentlyVisited

@Dao
interface ImportDao {
    @Insert
    @JvmSuppressWildcards
    suspend fun addAllLinks(linksTable: List<LinksTable>)

    @Insert
    @JvmSuppressWildcards
    suspend fun addAllImportantLinks(importantLinks: List<ImportantLinks>)

    @Insert
    @JvmSuppressWildcards
    suspend fun addAllArchivedLinks(archivedLinks: List<ArchivedLinks>)

    @Insert
    @JvmSuppressWildcards
    suspend fun addAllHistoryLinks(historyLinks: List<RecentlyVisited>)

    @Insert
    @JvmSuppressWildcards
    suspend fun addAllRegularFolders(foldersData: List<FoldersTable>)

    @Insert
    @JvmSuppressWildcards
    suspend fun addAllArchivedFolders(foldersData: List<ArchivedFolders>)
}