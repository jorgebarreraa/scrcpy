package cl.powerbox.gateway.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import cl.powerbox.gateway.data.entity.StockState;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
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
public final class StockStateDao_Impl implements StockStateDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<StockState> __insertionAdapterOfStockState;

  private final SharedSQLiteStatement __preparedStmtOfUpdateServerQty;

  private final SharedSQLiteStatement __preparedStmtOfResetLocalDelta;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public StockStateDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStockState = new EntityInsertionAdapter<StockState>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `stock_state` (`productId`,`serverQty`,`localDelta`,`lastSync`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StockState entity) {
        if (entity.getProductId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getProductId());
        }
        statement.bindLong(2, entity.getServerQty());
        statement.bindLong(3, entity.getLocalDelta());
        statement.bindLong(4, entity.getLastSync());
      }
    };
    this.__preparedStmtOfUpdateServerQty = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE stock_state SET serverQty = ? WHERE productId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfResetLocalDelta = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE stock_state SET localDelta = 0 WHERE productId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM stock_state WHERE productId = ?";
        return _query;
      }
    };
  }

  @Override
  public void upsert(final StockState state) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfStockState.insert(state);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public Object upsertSuspend(final StockState state,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStockState.insert(state);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object ensureAndDeltaSuspend(final String productId, final int delta, final long timestamp,
      final Continuation<? super StockState> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> StockStateDao.DefaultImpls.ensureAndDeltaSuspend(StockStateDao_Impl.this, productId, delta, timestamp, __cont), $completion);
  }

  @Override
  public void updateServerQty(final String id, final int qty) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateServerQty.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, qty);
    _argIndex = 2;
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
      __preparedStmtOfUpdateServerQty.release(_stmt);
    }
  }

  @Override
  public void resetLocalDelta(final String id) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfResetLocalDelta.acquire();
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
      __preparedStmtOfResetLocalDelta.release(_stmt);
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
  public List<StockState> all() {
    final String _sql = "SELECT * FROM stock_state";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
      final int _cursorIndexOfServerQty = CursorUtil.getColumnIndexOrThrow(_cursor, "serverQty");
      final int _cursorIndexOfLocalDelta = CursorUtil.getColumnIndexOrThrow(_cursor, "localDelta");
      final int _cursorIndexOfLastSync = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSync");
      final List<StockState> _result = new ArrayList<StockState>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final StockState _item;
        final String _tmpProductId;
        if (_cursor.isNull(_cursorIndexOfProductId)) {
          _tmpProductId = null;
        } else {
          _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
        }
        final int _tmpServerQty;
        _tmpServerQty = _cursor.getInt(_cursorIndexOfServerQty);
        final int _tmpLocalDelta;
        _tmpLocalDelta = _cursor.getInt(_cursorIndexOfLocalDelta);
        final long _tmpLastSync;
        _tmpLastSync = _cursor.getLong(_cursorIndexOfLastSync);
        _item = new StockState(_tmpProductId,_tmpServerQty,_tmpLocalDelta,_tmpLastSync);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public StockState byId(final String id) {
    final String _sql = "SELECT * FROM stock_state WHERE productId = ? LIMIT 1";
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
      final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
      final int _cursorIndexOfServerQty = CursorUtil.getColumnIndexOrThrow(_cursor, "serverQty");
      final int _cursorIndexOfLocalDelta = CursorUtil.getColumnIndexOrThrow(_cursor, "localDelta");
      final int _cursorIndexOfLastSync = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSync");
      final StockState _result;
      if (_cursor.moveToFirst()) {
        final String _tmpProductId;
        if (_cursor.isNull(_cursorIndexOfProductId)) {
          _tmpProductId = null;
        } else {
          _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
        }
        final int _tmpServerQty;
        _tmpServerQty = _cursor.getInt(_cursorIndexOfServerQty);
        final int _tmpLocalDelta;
        _tmpLocalDelta = _cursor.getInt(_cursorIndexOfLocalDelta);
        final long _tmpLastSync;
        _tmpLastSync = _cursor.getLong(_cursorIndexOfLastSync);
        _result = new StockState(_tmpProductId,_tmpServerQty,_tmpLocalDelta,_tmpLastSync);
      } else {
        _result = null;
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
