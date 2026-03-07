package cl.powerbox.gateway.data.dao

import androidx.room.*
import cl.powerbox.gateway.data.entity.CachedResponse

@Dao
interface CachedResponseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(e: CachedResponse)  // ✅ SIN suspend para llamadas síncronas

    @Query("SELECT * FROM cached_response WHERE key = :key LIMIT 1")
    fun byKey(key: String): CachedResponse?  // ✅ SIN suspend

    @Query("UPDATE cached_response SET lastHitAt = :ts WHERE key = :key")
    fun touch(key: String, ts: Long)

    @Query("DELETE FROM cached_response WHERE cachedAt < :olderThanMs")
    fun deleteOlderThan(olderThanMs: Long): Int

    @Query("SELECT key FROM cached_response ORDER BY lastHitAt ASC LIMIT :limit")
    fun lruKeys(limit: Int): List<String>

    @Query("DELETE FROM cached_response WHERE key IN (:keys)")
    fun deleteByKeys(keys: List<String>): Int

    @Query("SELECT IFNULL(SUM(LENGTH(bytes)), 0) FROM cached_response")
    fun totalBytes(): Long

    @Query("DELETE FROM cached_response WHERE key = :key")
    fun deleteByKey(key: String)

    @Query("SELECT COUNT(*) FROM cached_response")
    fun count(): Int
}
