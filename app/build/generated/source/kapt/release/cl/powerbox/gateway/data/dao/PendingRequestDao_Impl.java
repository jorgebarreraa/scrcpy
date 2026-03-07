package cl.powerbox.gateway.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import cl.powerbox.gateway.data.entity.PendingRequest;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PendingRequestDao_Impl implements PendingRequestDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PendingRequest> __insertionAdapterOfPendingRequest;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOlderThan;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSynced;

  public PendingRequestDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPendingRequest = new EntityInsertionAdapter<PendingRequest>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `pending_requests` (`id`,`path`,`method`,`headersJson`,`body`,`clientOrderId`,`createdAt`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PendingRequest entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
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
        if (entity.getHeadersJson() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getHeadersJson());
        }
        if (entity.getBody() == null) {
          statement.bindNull(5);
        } else {
          statement.bindBlob(5, entity.getBody());
        }
        if (entity.getClientOrderId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getClientOrderId());
        }
        statement.bindLong(7, entity.getCreatedAt());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM pending_requests WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOlderThan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM pending_requests WHERE createdAt < ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM pending_requests";
        return _query;
      }
    };
  }

  @Override
  public void insert(final PendingRequest request) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfPendingRequest.insert(request);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteById(final String id) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
    int _argIndex = 1;
    if (id == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, id);
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
      __preparedStmtOfDeleteById.release(_stmt);
    }
  }

  @Override
  public int deleteOlderThan(final long timestamp) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOlderThan.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, timestamp);
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
  public int deleteSynced() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSynced.acquire();
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
      __preparedStmtOfDeleteSynced.release(_stmt);
    }
  }

  @Override
  public List<PendingRequest> allPending() {
    final String _sql = "SELECT * FROM pending_requests ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfPath = CursorUtil.getColumnIndexOrThrow(_cursor, "path");
      final int _cursorIndexOfMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "method");
      final int _cursorIndexOfHeadersJson = CursorUtil.getColumnIndexOrThrow(_cursor, "headersJson");
      final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
      final int _cursorIndexOfClientOrderId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientOrderId");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final List<PendingRequest> _result = new ArrayList<PendingRequest>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PendingRequest _item;
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
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
        final String _tmpHeadersJson;
        if (_cursor.isNull(_cursorIndexOfHeadersJson)) {
          _tmpHeadersJson = null;
        } else {
          _tmpHeadersJson = _cursor.getString(_cursorIndexOfHeadersJson);
        }
        final byte[] _tmpBody;
        if (_cursor.isNull(_cursorIndexOfBody)) {
          _tmpBody = null;
        } else {
          _tmpBody = _cursor.getBlob(_cursorIndexOfBody);
        }
        final String _tmpClientOrderId;
        if (_cursor.isNull(_cursorIndexOfClientOrderId)) {
          _tmpClientOrderId = null;
        } else {
          _tmpClientOrderId = _cursor.getString(_cursorIndexOfClientOrderId);
        }
        final long _tmpCreatedAt;
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
        _item = new PendingRequest(_tmpId,_tmpPath,_tmpMethod,_tmpHeadersJson,_tmpBody,_tmpClientOrderId,_tmpCreatedAt);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<PendingRequest> getAll() {
    final String _sql = "SELECT * FROM pending_requests ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfPath = CursorUtil.getColumnIndexOrThrow(_cursor, "path");
      final int _cursorIndexOfMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "method");
      final int _cursorIndexOfHeadersJson = CursorUtil.getColumnIndexOrThrow(_cursor, "headersJson");
      final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
      final int _cursorIndexOfClientOrderId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientOrderId");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final List<PendingRequest> _result = new ArrayList<PendingRequest>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PendingRequest _item;
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
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
        final String _tmpHeadersJson;
        if (_cursor.isNull(_cursorIndexOfHeadersJson)) {
          _tmpHeadersJson = null;
        } else {
          _tmpHeadersJson = _cursor.getString(_cursorIndexOfHeadersJson);
        }
        final byte[] _tmpBody;
        if (_cursor.isNull(_cursorIndexOfBody)) {
          _tmpBody = null;
        } else {
          _tmpBody = _cursor.getBlob(_cursorIndexOfBody);
        }
        final String _tmpClientOrderId;
        if (_cursor.isNull(_cursorIndexOfClientOrderId)) {
          _tmpClientOrderId = null;
        } else {
          _tmpClientOrderId = _cursor.getString(_cursorIndexOfClientOrderId);
        }
        final long _tmpCreatedAt;
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
        _item = new PendingRequest(_tmpId,_tmpPath,_tmpMethod,_tmpHeadersJson,_tmpBody,_tmpClientOrderId,_tmpCreatedAt);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public PendingRequest byId(final String id) {
    final String _sql = "SELECT * FROM pending_requests WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (id == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, id);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfPath = CursorUtil.getColumnIndexOrThrow(_cursor, "path");
      final int _cursorIndexOfMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "method");
      final int _cursorIndexOfHeadersJson = CursorUtil.getColumnIndexOrThrow(_cursor, "headersJson");
      final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
      final int _cursorIndexOfClientOrderId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientOrderId");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final PendingRequest _result;
      if (_cursor.moveToFirst()) {
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
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
        final String _tmpHeadersJson;
        if (_cursor.isNull(_cursorIndexOfHeadersJson)) {
          _tmpHeadersJson = null;
        } else {
          _tmpHeadersJson = _cursor.getString(_cursorIndexOfHeadersJson);
        }
        final byte[] _tmpBody;
        if (_cursor.isNull(_cursorIndexOfBody)) {
          _tmpBody = null;
        } else {
          _tmpBody = _cursor.getBlob(_cursorIndexOfBody);
        }
        final String _tmpClientOrderId;
        if (_cursor.isNull(_cursorIndexOfClientOrderId)) {
          _tmpClientOrderId = null;
        } else {
          _tmpClientOrderId = _cursor.getString(_cursorIndexOfClientOrderId);
        }
        final long _tmpCreatedAt;
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
        _result = new PendingRequest(_tmpId,_tmpPath,_tmpMethod,_tmpHeadersJson,_tmpBody,_tmpClientOrderId,_tmpCreatedAt);
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
  public int count() {
    final String _sql = "SELECT COUNT(*) FROM pending_requests";
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
