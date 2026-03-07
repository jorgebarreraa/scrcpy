package cl.powerbox.gateway.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import cl.powerbox.gateway.data.dao.CachedResponseDao;
import cl.powerbox.gateway.data.dao.CachedResponseDao_Impl;
import cl.powerbox.gateway.data.dao.MachineConfigDao;
import cl.powerbox.gateway.data.dao.MachineConfigDao_Impl;
import cl.powerbox.gateway.data.dao.OfflineTransactionDao;
import cl.powerbox.gateway.data.dao.OfflineTransactionDao_Impl;
import cl.powerbox.gateway.data.dao.PendingRequestDao;
import cl.powerbox.gateway.data.dao.PendingRequestDao_Impl;
import cl.powerbox.gateway.data.dao.ProductDao;
import cl.powerbox.gateway.data.dao.ProductDao_Impl;
import cl.powerbox.gateway.data.dao.ReplenishmentEventDao;
import cl.powerbox.gateway.data.dao.ReplenishmentEventDao_Impl;
import cl.powerbox.gateway.data.dao.SaleEventDao;
import cl.powerbox.gateway.data.dao.SaleEventDao_Impl;
import cl.powerbox.gateway.data.dao.StockMasterDao;
import cl.powerbox.gateway.data.dao.StockMasterDao_Impl;
import cl.powerbox.gateway.data.dao.StockStateDao;
import cl.powerbox.gateway.data.dao.StockStateDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile CachedResponseDao _cachedResponseDao;

  private volatile PendingRequestDao _pendingRequestDao;

  private volatile ProductDao _productDao;

  private volatile StockMasterDao _stockMasterDao;

  private volatile SaleEventDao _saleEventDao;

  private volatile ReplenishmentEventDao _replenishmentEventDao;

  private volatile MachineConfigDao _machineConfigDao;

  private volatile StockStateDao _stockStateDao;

  private volatile OfflineTransactionDao _offlineTransactionDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_response` (`key` TEXT NOT NULL, `path` TEXT NOT NULL, `method` TEXT NOT NULL, `bodyHash` TEXT NOT NULL, `contentType` TEXT NOT NULL, `bytes` BLOB NOT NULL, `cachedAt` INTEGER NOT NULL, `lastHitAt` INTEGER NOT NULL, PRIMARY KEY(`key`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `pending_requests` (`id` TEXT NOT NULL, `path` TEXT NOT NULL, `method` TEXT NOT NULL, `headersJson` TEXT NOT NULL, `body` BLOB NOT NULL, `clientOrderId` TEXT, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `product` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `price` INTEGER NOT NULL, `recipeJson` TEXT, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `stock_master` (`productId` TEXT NOT NULL, `qty` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`productId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sale_event` (`id` TEXT NOT NULL, `serverOrderId` TEXT, `clientOrderId` TEXT, `productId` TEXT NOT NULL, `qty` INTEGER NOT NULL, `price` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `sent` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `replenishment_events` (`id` TEXT NOT NULL, `productId` TEXT NOT NULL, `deltaQty` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `sent` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `machine_config` (`key` TEXT NOT NULL, `value` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`key`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `stock_state` (`productId` TEXT NOT NULL, `serverQty` INTEGER NOT NULL, `localDelta` INTEGER NOT NULL, `lastSync` INTEGER NOT NULL, PRIMARY KEY(`productId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `offline_transactions` (`id` TEXT NOT NULL, `deviceId` TEXT NOT NULL, `materialId` TEXT NOT NULL, `replenishQt` INTEGER NOT NULL, `operationType` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL, `syncedAt` INTEGER, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'ded5c002200bc62ffd2823bad366810c')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `cached_response`");
        db.execSQL("DROP TABLE IF EXISTS `pending_requests`");
        db.execSQL("DROP TABLE IF EXISTS `product`");
        db.execSQL("DROP TABLE IF EXISTS `stock_master`");
        db.execSQL("DROP TABLE IF EXISTS `sale_event`");
        db.execSQL("DROP TABLE IF EXISTS `replenishment_events`");
        db.execSQL("DROP TABLE IF EXISTS `machine_config`");
        db.execSQL("DROP TABLE IF EXISTS `stock_state`");
        db.execSQL("DROP TABLE IF EXISTS `offline_transactions`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCachedResponse = new HashMap<String, TableInfo.Column>(8);
        _columnsCachedResponse.put("key", new TableInfo.Column("key", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedResponse.put("path", new TableInfo.Column("path", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedResponse.put("method", new TableInfo.Column("method", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedResponse.put("bodyHash", new TableInfo.Column("bodyHash", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedResponse.put("contentType", new TableInfo.Column("contentType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedResponse.put("bytes", new TableInfo.Column("bytes", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedResponse.put("cachedAt", new TableInfo.Column("cachedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedResponse.put("lastHitAt", new TableInfo.Column("lastHitAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedResponse = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedResponse = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCachedResponse = new TableInfo("cached_response", _columnsCachedResponse, _foreignKeysCachedResponse, _indicesCachedResponse);
        final TableInfo _existingCachedResponse = TableInfo.read(db, "cached_response");
        if (!_infoCachedResponse.equals(_existingCachedResponse)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_response(cl.powerbox.gateway.data.entity.CachedResponse).\n"
                  + " Expected:\n" + _infoCachedResponse + "\n"
                  + " Found:\n" + _existingCachedResponse);
        }
        final HashMap<String, TableInfo.Column> _columnsPendingRequests = new HashMap<String, TableInfo.Column>(7);
        _columnsPendingRequests.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingRequests.put("path", new TableInfo.Column("path", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingRequests.put("method", new TableInfo.Column("method", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingRequests.put("headersJson", new TableInfo.Column("headersJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingRequests.put("body", new TableInfo.Column("body", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingRequests.put("clientOrderId", new TableInfo.Column("clientOrderId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingRequests.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPendingRequests = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPendingRequests = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPendingRequests = new TableInfo("pending_requests", _columnsPendingRequests, _foreignKeysPendingRequests, _indicesPendingRequests);
        final TableInfo _existingPendingRequests = TableInfo.read(db, "pending_requests");
        if (!_infoPendingRequests.equals(_existingPendingRequests)) {
          return new RoomOpenHelper.ValidationResult(false, "pending_requests(cl.powerbox.gateway.data.entity.PendingRequest).\n"
                  + " Expected:\n" + _infoPendingRequests + "\n"
                  + " Found:\n" + _existingPendingRequests);
        }
        final HashMap<String, TableInfo.Column> _columnsProduct = new HashMap<String, TableInfo.Column>(5);
        _columnsProduct.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProduct.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProduct.put("price", new TableInfo.Column("price", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProduct.put("recipeJson", new TableInfo.Column("recipeJson", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProduct.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProduct = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesProduct = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoProduct = new TableInfo("product", _columnsProduct, _foreignKeysProduct, _indicesProduct);
        final TableInfo _existingProduct = TableInfo.read(db, "product");
        if (!_infoProduct.equals(_existingProduct)) {
          return new RoomOpenHelper.ValidationResult(false, "product(cl.powerbox.gateway.data.entity.Product).\n"
                  + " Expected:\n" + _infoProduct + "\n"
                  + " Found:\n" + _existingProduct);
        }
        final HashMap<String, TableInfo.Column> _columnsStockMaster = new HashMap<String, TableInfo.Column>(3);
        _columnsStockMaster.put("productId", new TableInfo.Column("productId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockMaster.put("qty", new TableInfo.Column("qty", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockMaster.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStockMaster = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStockMaster = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoStockMaster = new TableInfo("stock_master", _columnsStockMaster, _foreignKeysStockMaster, _indicesStockMaster);
        final TableInfo _existingStockMaster = TableInfo.read(db, "stock_master");
        if (!_infoStockMaster.equals(_existingStockMaster)) {
          return new RoomOpenHelper.ValidationResult(false, "stock_master(cl.powerbox.gateway.data.entity.StockMaster).\n"
                  + " Expected:\n" + _infoStockMaster + "\n"
                  + " Found:\n" + _existingStockMaster);
        }
        final HashMap<String, TableInfo.Column> _columnsSaleEvent = new HashMap<String, TableInfo.Column>(8);
        _columnsSaleEvent.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaleEvent.put("serverOrderId", new TableInfo.Column("serverOrderId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaleEvent.put("clientOrderId", new TableInfo.Column("clientOrderId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaleEvent.put("productId", new TableInfo.Column("productId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaleEvent.put("qty", new TableInfo.Column("qty", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaleEvent.put("price", new TableInfo.Column("price", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaleEvent.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaleEvent.put("sent", new TableInfo.Column("sent", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSaleEvent = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSaleEvent = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSaleEvent = new TableInfo("sale_event", _columnsSaleEvent, _foreignKeysSaleEvent, _indicesSaleEvent);
        final TableInfo _existingSaleEvent = TableInfo.read(db, "sale_event");
        if (!_infoSaleEvent.equals(_existingSaleEvent)) {
          return new RoomOpenHelper.ValidationResult(false, "sale_event(cl.powerbox.gateway.data.entity.SaleEvent).\n"
                  + " Expected:\n" + _infoSaleEvent + "\n"
                  + " Found:\n" + _existingSaleEvent);
        }
        final HashMap<String, TableInfo.Column> _columnsReplenishmentEvents = new HashMap<String, TableInfo.Column>(5);
        _columnsReplenishmentEvents.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReplenishmentEvents.put("productId", new TableInfo.Column("productId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReplenishmentEvents.put("deltaQty", new TableInfo.Column("deltaQty", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReplenishmentEvents.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReplenishmentEvents.put("sent", new TableInfo.Column("sent", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReplenishmentEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesReplenishmentEvents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoReplenishmentEvents = new TableInfo("replenishment_events", _columnsReplenishmentEvents, _foreignKeysReplenishmentEvents, _indicesReplenishmentEvents);
        final TableInfo _existingReplenishmentEvents = TableInfo.read(db, "replenishment_events");
        if (!_infoReplenishmentEvents.equals(_existingReplenishmentEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "replenishment_events(cl.powerbox.gateway.data.entity.ReplenishmentEvent).\n"
                  + " Expected:\n" + _infoReplenishmentEvents + "\n"
                  + " Found:\n" + _existingReplenishmentEvents);
        }
        final HashMap<String, TableInfo.Column> _columnsMachineConfig = new HashMap<String, TableInfo.Column>(3);
        _columnsMachineConfig.put("key", new TableInfo.Column("key", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMachineConfig.put("value", new TableInfo.Column("value", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMachineConfig.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMachineConfig = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMachineConfig = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMachineConfig = new TableInfo("machine_config", _columnsMachineConfig, _foreignKeysMachineConfig, _indicesMachineConfig);
        final TableInfo _existingMachineConfig = TableInfo.read(db, "machine_config");
        if (!_infoMachineConfig.equals(_existingMachineConfig)) {
          return new RoomOpenHelper.ValidationResult(false, "machine_config(cl.powerbox.gateway.data.entity.MachineConfig).\n"
                  + " Expected:\n" + _infoMachineConfig + "\n"
                  + " Found:\n" + _existingMachineConfig);
        }
        final HashMap<String, TableInfo.Column> _columnsStockState = new HashMap<String, TableInfo.Column>(4);
        _columnsStockState.put("productId", new TableInfo.Column("productId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockState.put("serverQty", new TableInfo.Column("serverQty", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockState.put("localDelta", new TableInfo.Column("localDelta", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockState.put("lastSync", new TableInfo.Column("lastSync", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStockState = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStockState = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoStockState = new TableInfo("stock_state", _columnsStockState, _foreignKeysStockState, _indicesStockState);
        final TableInfo _existingStockState = TableInfo.read(db, "stock_state");
        if (!_infoStockState.equals(_existingStockState)) {
          return new RoomOpenHelper.ValidationResult(false, "stock_state(cl.powerbox.gateway.data.entity.StockState).\n"
                  + " Expected:\n" + _infoStockState + "\n"
                  + " Found:\n" + _existingStockState);
        }
        final HashMap<String, TableInfo.Column> _columnsOfflineTransactions = new HashMap<String, TableInfo.Column>(8);
        _columnsOfflineTransactions.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOfflineTransactions.put("deviceId", new TableInfo.Column("deviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOfflineTransactions.put("materialId", new TableInfo.Column("materialId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOfflineTransactions.put("replenishQt", new TableInfo.Column("replenishQt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOfflineTransactions.put("operationType", new TableInfo.Column("operationType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOfflineTransactions.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOfflineTransactions.put("synced", new TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOfflineTransactions.put("syncedAt", new TableInfo.Column("syncedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysOfflineTransactions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesOfflineTransactions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoOfflineTransactions = new TableInfo("offline_transactions", _columnsOfflineTransactions, _foreignKeysOfflineTransactions, _indicesOfflineTransactions);
        final TableInfo _existingOfflineTransactions = TableInfo.read(db, "offline_transactions");
        if (!_infoOfflineTransactions.equals(_existingOfflineTransactions)) {
          return new RoomOpenHelper.ValidationResult(false, "offline_transactions(cl.powerbox.gateway.data.entity.OfflineTransaction).\n"
                  + " Expected:\n" + _infoOfflineTransactions + "\n"
                  + " Found:\n" + _existingOfflineTransactions);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "ded5c002200bc62ffd2823bad366810c", "cb044f2892c6d46d8cd1fb1640a49e3e");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "cached_response","pending_requests","product","stock_master","sale_event","replenishment_events","machine_config","stock_state","offline_transactions");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `cached_response`");
      _db.execSQL("DELETE FROM `pending_requests`");
      _db.execSQL("DELETE FROM `product`");
      _db.execSQL("DELETE FROM `stock_master`");
      _db.execSQL("DELETE FROM `sale_event`");
      _db.execSQL("DELETE FROM `replenishment_events`");
      _db.execSQL("DELETE FROM `machine_config`");
      _db.execSQL("DELETE FROM `stock_state`");
      _db.execSQL("DELETE FROM `offline_transactions`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(CachedResponseDao.class, CachedResponseDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PendingRequestDao.class, PendingRequestDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ProductDao.class, ProductDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StockMasterDao.class, StockMasterDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SaleEventDao.class, SaleEventDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReplenishmentEventDao.class, ReplenishmentEventDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MachineConfigDao.class, MachineConfigDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StockStateDao.class, StockStateDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(OfflineTransactionDao.class, OfflineTransactionDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public CachedResponseDao cachedDao() {
    if (_cachedResponseDao != null) {
      return _cachedResponseDao;
    } else {
      synchronized(this) {
        if(_cachedResponseDao == null) {
          _cachedResponseDao = new CachedResponseDao_Impl(this);
        }
        return _cachedResponseDao;
      }
    }
  }

  @Override
  public PendingRequestDao pendingDao() {
    if (_pendingRequestDao != null) {
      return _pendingRequestDao;
    } else {
      synchronized(this) {
        if(_pendingRequestDao == null) {
          _pendingRequestDao = new PendingRequestDao_Impl(this);
        }
        return _pendingRequestDao;
      }
    }
  }

  @Override
  public ProductDao productDao() {
    if (_productDao != null) {
      return _productDao;
    } else {
      synchronized(this) {
        if(_productDao == null) {
          _productDao = new ProductDao_Impl(this);
        }
        return _productDao;
      }
    }
  }

  @Override
  public StockMasterDao stockMasterDao() {
    if (_stockMasterDao != null) {
      return _stockMasterDao;
    } else {
      synchronized(this) {
        if(_stockMasterDao == null) {
          _stockMasterDao = new StockMasterDao_Impl(this);
        }
        return _stockMasterDao;
      }
    }
  }

  @Override
  public SaleEventDao saleDao() {
    if (_saleEventDao != null) {
      return _saleEventDao;
    } else {
      synchronized(this) {
        if(_saleEventDao == null) {
          _saleEventDao = new SaleEventDao_Impl(this);
        }
        return _saleEventDao;
      }
    }
  }

  @Override
  public ReplenishmentEventDao replenishmentEventDao() {
    if (_replenishmentEventDao != null) {
      return _replenishmentEventDao;
    } else {
      synchronized(this) {
        if(_replenishmentEventDao == null) {
          _replenishmentEventDao = new ReplenishmentEventDao_Impl(this);
        }
        return _replenishmentEventDao;
      }
    }
  }

  @Override
  public MachineConfigDao machineConfigDao() {
    if (_machineConfigDao != null) {
      return _machineConfigDao;
    } else {
      synchronized(this) {
        if(_machineConfigDao == null) {
          _machineConfigDao = new MachineConfigDao_Impl(this);
        }
        return _machineConfigDao;
      }
    }
  }

  @Override
  public StockStateDao stockStateDao() {
    if (_stockStateDao != null) {
      return _stockStateDao;
    } else {
      synchronized(this) {
        if(_stockStateDao == null) {
          _stockStateDao = new StockStateDao_Impl(this);
        }
        return _stockStateDao;
      }
    }
  }

  @Override
  public OfflineTransactionDao offlineTransactionDao() {
    if (_offlineTransactionDao != null) {
      return _offlineTransactionDao;
    } else {
      synchronized(this) {
        if(_offlineTransactionDao == null) {
          _offlineTransactionDao = new OfflineTransactionDao_Impl(this);
        }
        return _offlineTransactionDao;
      }
    }
  }
}
