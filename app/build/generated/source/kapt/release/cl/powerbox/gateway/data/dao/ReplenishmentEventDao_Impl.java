package cl.powerbox.gateway.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import cl.powerbox.gateway.data.entity.ReplenishmentEvent;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ReplenishmentEventDao_Impl implements ReplenishmentEventDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReplenishmentEvent> __insertionAdapterOfReplenishmentEvent;

  private final SharedSQLiteStatement __preparedStmtOfMarkAsSentSuspend;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldSent;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public ReplenishmentEventDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReplenishmentEvent = new EntityInsertionAdapter<ReplenishmentEvent>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `replenishment_events` (`id`,`productId`,`deltaQty`,`createdAt`,`sent`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReplenishmentEvent entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getProductId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getProductId());
        }
        statement.bindLong(3, entity.getDeltaQty());
        statement.bindLong(4, entity.getCreatedAt());
        final int _tmp = entity.getSent() ? 1 : 0;
        statement.bindLong(5, _tmp);
      }
    };
    this.__preparedStmtOfMarkAsSentSuspend = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE replenishment_events SET sent = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOldSent = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM replenishment_events WHERE sent = 1 AND createdAt < ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM replenishment_events WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public void upsert(final ReplenishmentEvent event) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfReplenishmentEvent.insert(event);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public Object upsertSuspend(final ReplenishmentEvent event,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfReplenishmentEvent.insert(event);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public void upsertAll(final List<ReplenishmentEvent> events) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfReplenishmentEvent.insert(events);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public Object upsertAllSuspend(final List<ReplenishmentEvent> events,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfReplenishmentEvent.insert(events);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insert(final ReplenishmentEvent e, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfReplenishmentEvent.insert(e);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markAsSentSuspend(final String eventId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAsSentSuspend.acquire();
        int _argIndex = 1;
        if (eventId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, eventId);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkAsSentSuspend.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public void markAsSent(final String eventId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAsSentSuspend.acquire();
    int _argIndex = 1;
    if (eventId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, eventId);
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
      __preparedStmtOfMarkAsSentSuspend.release(_stmt);
    }
  }

  @Override
  public int deleteOldSent(final long timestamp) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldSent.acquire();
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
      __preparedStmtOfDeleteOldSent.release(_stmt);
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
  public List<ReplenishmentEvent> all() {
    final String _sql = "SELECT * FROM replenishment_events ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
      final int _cursorIndexOfDeltaQty = CursorUtil.getColumnIndexOrThrow(_cursor, "deltaQty");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final int _cursorIndexOfSent = CursorUtil.getColumnIndexOrThrow(_cursor, "sent");
      final List<ReplenishmentEvent> _result = new ArrayList<ReplenishmentEvent>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ReplenishmentEvent _item;
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        final String _tmpProductId;
        if (_cursor.isNull(_cursorIndexOfProductId)) {
          _tmpProductId = null;
        } else {
          _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
        }
        final int _tmpDeltaQty;
        _tmpDeltaQty = _cursor.getInt(_cursorIndexOfDeltaQty);
        final long _tmpCreatedAt;
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
        final boolean _tmpSent;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfSent);
        _tmpSent = _tmp != 0;
        _item = new ReplenishmentEvent(_tmpId,_tmpProductId,_tmpDeltaQty,_tmpCreatedAt,_tmpSent);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<ReplenishmentEvent> getAll() {
    final String _sql = "SELECT * FROM replenishment_events ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
      final int _cursorIndexOfDeltaQty = CursorUtil.getColumnIndexOrThrow(_cursor, "deltaQty");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final int _cursorIndexOfSent = CursorUtil.getColumnIndexOrThrow(_cursor, "sent");
      final List<ReplenishmentEvent> _result = new ArrayList<ReplenishmentEvent>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ReplenishmentEvent _item;
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        final String _tmpProductId;
        if (_cursor.isNull(_cursorIndexOfProductId)) {
          _tmpProductId = null;
        } else {
          _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
        }
        final int _tmpDeltaQty;
        _tmpDeltaQty = _cursor.getInt(_cursorIndexOfDeltaQty);
        final long _tmpCreatedAt;
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
        final boolean _tmpSent;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfSent);
        _tmpSent = _tmp != 0;
        _item = new ReplenishmentEvent(_tmpId,_tmpProductId,_tmpDeltaQty,_tmpCreatedAt,_tmpSent);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Object allUnsent(final Continuation<? super List<ReplenishmentEvent>> $completion) {
    final String _sql = "SELECT * FROM replenishment_events WHERE sent = 0 ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReplenishmentEvent>>() {
      @Override
      @NonNull
      public List<ReplenishmentEvent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfDeltaQty = CursorUtil.getColumnIndexOrThrow(_cursor, "deltaQty");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfSent = CursorUtil.getColumnIndexOrThrow(_cursor, "sent");
          final List<ReplenishmentEvent> _result = new ArrayList<ReplenishmentEvent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReplenishmentEvent _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpProductId;
            if (_cursor.isNull(_cursorIndexOfProductId)) {
              _tmpProductId = null;
            } else {
              _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            }
            final int _tmpDeltaQty;
            _tmpDeltaQty = _cursor.getInt(_cursorIndexOfDeltaQty);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpSent;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSent);
            _tmpSent = _tmp != 0;
            _item = new ReplenishmentEvent(_tmpId,_tmpProductId,_tmpDeltaQty,_tmpCreatedAt,_tmpSent);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object pending(final Continuation<? super List<ReplenishmentEvent>> $completion) {
    final String _sql = "SELECT * FROM replenishment_events WHERE sent = 0 ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReplenishmentEvent>>() {
      @Override
      @NonNull
      public List<ReplenishmentEvent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfDeltaQty = CursorUtil.getColumnIndexOrThrow(_cursor, "deltaQty");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfSent = CursorUtil.getColumnIndexOrThrow(_cursor, "sent");
          final List<ReplenishmentEvent> _result = new ArrayList<ReplenishmentEvent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReplenishmentEvent _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpProductId;
            if (_cursor.isNull(_cursorIndexOfProductId)) {
              _tmpProductId = null;
            } else {
              _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            }
            final int _tmpDeltaQty;
            _tmpDeltaQty = _cursor.getInt(_cursorIndexOfDeltaQty);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpSent;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSent);
            _tmpSent = _tmp != 0;
            _item = new ReplenishmentEvent(_tmpId,_tmpProductId,_tmpDeltaQty,_tmpCreatedAt,_tmpSent);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object allSentSuspend(final Continuation<? super List<ReplenishmentEvent>> $completion) {
    final String _sql = "SELECT * FROM replenishment_events WHERE sent = 1 ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReplenishmentEvent>>() {
      @Override
      @NonNull
      public List<ReplenishmentEvent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfDeltaQty = CursorUtil.getColumnIndexOrThrow(_cursor, "deltaQty");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfSent = CursorUtil.getColumnIndexOrThrow(_cursor, "sent");
          final List<ReplenishmentEvent> _result = new ArrayList<ReplenishmentEvent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReplenishmentEvent _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpProductId;
            if (_cursor.isNull(_cursorIndexOfProductId)) {
              _tmpProductId = null;
            } else {
              _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            }
            final int _tmpDeltaQty;
            _tmpDeltaQty = _cursor.getInt(_cursorIndexOfDeltaQty);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpSent;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSent);
            _tmpSent = _tmp != 0;
            _item = new ReplenishmentEvent(_tmpId,_tmpProductId,_tmpDeltaQty,_tmpCreatedAt,_tmpSent);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public List<ReplenishmentEvent> allSent() {
    final String _sql = "SELECT * FROM replenishment_events WHERE sent = 1 ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
      final int _cursorIndexOfDeltaQty = CursorUtil.getColumnIndexOrThrow(_cursor, "deltaQty");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final int _cursorIndexOfSent = CursorUtil.getColumnIndexOrThrow(_cursor, "sent");
      final List<ReplenishmentEvent> _result = new ArrayList<ReplenishmentEvent>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ReplenishmentEvent _item;
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        final String _tmpProductId;
        if (_cursor.isNull(_cursorIndexOfProductId)) {
          _tmpProductId = null;
        } else {
          _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
        }
        final int _tmpDeltaQty;
        _tmpDeltaQty = _cursor.getInt(_cursorIndexOfDeltaQty);
        final long _tmpCreatedAt;
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
        final boolean _tmpSent;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfSent);
        _tmpSent = _tmp != 0;
        _item = new ReplenishmentEvent(_tmpId,_tmpProductId,_tmpDeltaQty,_tmpCreatedAt,_tmpSent);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Object markSentSuspend(final List<String> ids,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE replenishment_events SET sent = 1 WHERE id IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : ids) {
          if (_item == null) {
            _stmt.bindNull(_argIndex);
          } else {
            _stmt.bindString(_argIndex, _item);
          }
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public void markSent(final List<String> ids) {
    __db.assertNotSuspendingTransaction();
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("UPDATE replenishment_events SET sent = 1 WHERE id IN (");
    final int _inputSize = ids.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
    int _argIndex = 1;
    for (String _item : ids) {
      if (_item == null) {
        _stmt.bindNull(_argIndex);
      } else {
        _stmt.bindString(_argIndex, _item);
      }
      _argIndex++;
    }
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
