package com.example.salesforce.core.cache

object AppCache {
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long,
        val ttlMillis: Long,
    ) {
        fun isExpired(): Boolean {
            return System.currentTimeMillis() - timestamp > ttlMillis
        }
    }

    private val cache = mutableMapOf<String, CacheEntry<*>>()

    fun <T : Any> put(key: String, data: T, ttlMillis: Long = 5 * 60 * 1000L) {
        cache[key] = CacheEntry(data, System.currentTimeMillis(), ttlMillis)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: String): T? {
        val entry = cache[key] ?: return null
        if (entry.isExpired()) {
            cache.remove(key)
            return null
        }
        return entry.data as? T
    }

    fun invalidate(key: String) {
        cache.remove(key)
    }

    fun clear() {
        cache.clear()
    }
}
