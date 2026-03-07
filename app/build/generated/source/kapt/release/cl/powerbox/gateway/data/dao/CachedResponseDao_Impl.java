package cl.powerbox.gateway.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import cl.powerbox.gateway.data.entity.CachedResponse;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CachedResponseDao_Impl implements CachedResponseDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CachedResponse> __insertionAdapterOfCachedResponse;

  private final SharedSQLiteStatement __preparedStmtOfTouch;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOlderThan;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByKey;

  public CachedResponseDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCachedResponse = new EntityInsertionAdapter<CachedResponse>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cached_response` (`key`,`path`,`method`,`bodyHash`,`contentType`,`bytes`,`cachedAt`,`lastHitAt`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CachedResponse entity) {
        if (entity.getKey() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getKey());
        }
        if (entity.getPath() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPath());
        }
        if (entity.getMethod() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getMethod());
        }
        if (entity.getBodyHash() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getBodyHash());
        }
        if (entity.getContentType() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getContentType());
        }
        if (entity.getBytes() == null) {
          statement.bindNull(6);
        } else {
          statement.bindBlob(6, entity.getBytes());
        }
        statement.bindLong(7, entity.getCachedAt());
        statement.bindLong(8, entity.getLastHitAt());
      }
    };
    this.__preparedStmtOfTouch = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE cached_response SET lastHitAt = ? WHERE key = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOlderThan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_response WHERE cachedAt < ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteByKey = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_response WHERE key = ?";
        return _query;
      }
    };
  }

  @Override
  public void upsert(final CachedResponse e) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfCachedResponse.insert(e);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void touch(final String key, final long ts) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfTouch.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, ts);
    _argIndex = 2;
    if (key == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, key);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfTouch.release(_stmt);
    }
  }

  @Override
  public int deleteOlderThan(final long olderThanMs) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOlderThan.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, olderThanMs);
    try {
      __db.beginTransaction();
      try {
        final int _result = _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
        return _result;
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteOlderThan.release(_stmt);
    }
  }

  @Override
  public void deleteByKey(final String key) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByKey.acquire();
    int _argIndex = 1;
    if (key == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, key);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteByKey.release(_stmt);
    }
  }

  @Override
  public CachedResponse byKey(final String key) {
    final String _sql = "SELECT * FROM cached_response WHERE key = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (key == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, key);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfKey = CursorUtil.getColumnIndexOrThrow(_cursor, "key");
      final int _cursorIndexOfPath = CursorUtil.getColumnIndexOrThrow(_cursor, "path");
      final int _cursorIndexOfMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "method");
      final int _cursorIndexOfBodyHash = CursorUtil.getColumnIndexOrThrow(_cursor, "bodyHash");
      final int _cursorIndexOfContentType = CursorUtil.getColumnIndexOrThrow(_cursor, "contentType");
      final int _cursorIndexOfBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "bytes");
      final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
      final int _cursorIndexOfLastHitAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastHitAt");
      final CachedResponse _result;
      if (_cursor.moveToFirst()) {
        final String _tmpKey;
        if (_cursor.isNull(_cursorIndexOfKey)) {
          _tmpKey = null;
        } else {
          _tmpKey = _cursor.getString(_cursorIndexOfKey);
        }
        final String _tmpPath;
        if (_cursor.isNull(_cursorIndexOfPath)) {
          _tmpPath = null;
        } else {
          _tmpPath = _cursor.getString(_cursorIndexOfPath);
        }
        final String _tmpMethod;
        if (_cursor.isNull(_cursorIndexOfMethod)) {
          _tmpMethod = null;
        } else {
          _tmpMethod = _cursor.getString(_cursorIndexOfMethod);
        }
        final String _tmpBodyHash;
        if (_cursor.isNull(_cursorIndexOfBodyHash)) {
          _tmpBodyHash = null;
        } else {
          _tmpBodyHash = _cursor.getString(_cursorIndexOfBodyHash);
        }
        final String _tmpContentType;
        if (_cursor.isNull(_cursorIndexOfContentType)) {
          _tmpContentType = null;
        } else {
          _tmpContentType = _cursor.getString(_cursorIndexOfContentType);
        }
        final byte[] _tmpBytes;
        if (_cursor.isNull(_cursorIndexOfBytes)) {
          _tmpBytes = null;
        } else {
          _tmpBytes = _cursor.getBlob(_cursorIndexOfBytes);
        }
        final long _tmpCachedAt;
        _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
        final long _tmpLastHitAt;
        _tmpLastHitAt = _cursor.getLong(_cursorIndexOfLastHitAt);
        _result = new CachedResponse(_tmpKey,_tmpPath,_tmpMethod,_tmpBodyHash,_tmpContentType,_tmpBytes,_tmpCachedAt,_tmpLastHitAt);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<String> lruKeys(final int limit) {
    final String _sql = "SELECT key FROM cached_response ORDER BY lastHitAt ASC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final List<String> _result = new ArrayList<String>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final String _item;
        if (_cursor.isNull(0)) {
          _item = null;
        } else {
          _item = _cursor.getString(0);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public long totalBytes() {
    final String _sql = "SELECT IFNULL(SUM(LENGTH(bytes)), 0) FROM cached_response";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final long _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getLong(0);
      } else {
        _result = 0L;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int count() {
    final String _sql = "SELECT COUNT(*) FROM cached_response";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int deleteByKeys(final List<String> keys) {
    __db.assertNotSuspendingTransaction();
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("DELETE FROM cached_response WHERE key IN (");
    final int _inputSize = keys.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
    int _argIndex = 1;
    for (String _item : keys) {
      if (_item == null) {
        _stmt.bindNull(_argIndex);
      } else {
        _stmt.bindString(_argIndex, _item);
      }
      _argIndex++;
    }
    __db.beginTransaction();
    try {
      final int _result = _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
