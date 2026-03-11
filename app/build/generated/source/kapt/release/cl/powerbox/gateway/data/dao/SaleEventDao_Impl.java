package cl.powerbox.gateway.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import cl.powerbox.gateway.data.entity.SaleEvent;
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
public final class SaleEventDao_Impl implements SaleEventDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SaleEvent> __insertionAdapterOfSaleEvent;

  public SaleEventDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSaleEvent = new EntityInsertionAdapter<SaleEvent>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `sale_event` (`id`,`serverOrderId`,`clientOrderId`,`productId`,`qty`,`price`,`createdAt`,`sent`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SaleEvent entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getServerOrderId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getServerOrderId());
        }
        if (entity.getClientOrderId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getClientOrderId());
        }
        if (entity.getProductId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getProductId());
        }
        statement.bindLong(5, entity.getQty());
        statement.bindLong(6, entity.getPrice());
        statement.bindLong(7, entity.getCreatedAt());
        final int _tmp = entity.getSent() ? 1 : 0;
        statement.bindLong(8, _tmp);
      }
    };
  }

  @Override
  public Object insert(final SaleEvent e, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSaleEvent.insert(e);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object pending(final Continuation<? super List<SaleEvent>> $completion) {
    final String _sql = "SELECT * FROM sale_event WHERE sent = 0 ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SaleEvent>>() {
      @Override
      @NonNull
      public List<SaleEvent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfServerOrderId = CursorUtil.getColumnIndexOrThrow(_cursor, "serverOrderId");
          final int _cursorIndexOfClientOrderId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientOrderId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfQty = CursorUtil.getColumnIndexOrThrow(_cursor, "qty");
          final int _cursorIndexOfPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "price");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfSent = CursorUtil.getColumnIndexOrThrow(_cursor, "sent");
          final List<SaleEvent> _result = new ArrayList<SaleEvent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SaleEvent _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpServerOrderId;
            if (_cursor.isNull(_cursorIndexOfServerOrderId)) {
              _tmpServerOrderId = null;
            } else {
              _tmpServerOrderId = _cursor.getString(_cursorIndexOfServerOrderId);
            }
            final String _tmpClientOrderId;
            if (_cursor.isNull(_cursorIndexOfClientOrderId)) {
              _tmpClientOrderId = null;
            } else {
              _tmpClientOrderId = _cursor.getString(_cursorIndexOfClientOrderId);
            }
            final String _tmpProductId;
            if (_cursor.isNull(_cursorIndexOfProductId)) {
              _tmpProductId = null;
            } else {
              _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            }
            final int _tmpQty;
            _tmpQty = _cursor.getInt(_cursorIndexOfQty);
            final long _tmpPrice;
            _tmpPrice = _cursor.getLong(_cursorIndexOfPrice);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpSent;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSent);
            _tmpSent = _tmp != 0;
            _item = new SaleEvent(_tmpId,_tmpServerOrderId,_tmpClientOrderId,_tmpProductId,_tmpQty,_tmpPrice,_tmpCreatedAt,_tmpSent);
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
  public Object markSent(final List<String> ids, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE sale_event SET sent = 1 WHERE id IN (");
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
