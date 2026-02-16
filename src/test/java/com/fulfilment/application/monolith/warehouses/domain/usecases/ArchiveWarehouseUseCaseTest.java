package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;

public class ArchiveWarehouseUseCaseTest {

  private TestWarehouseStore warehouseStore;
  private ArchiveWarehouseUseCase archiveWarehouseUseCase;

  @BeforeEach
  void setUp() {
    warehouseStore = new TestWarehouseStore();
    archiveWarehouseUseCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Test
  void shouldArchiveWarehouseSuccessfully() {
    Warehouse warehouse = createTestWarehouse("ZWOLLE-001", "MWH.001");

    archiveWarehouseUseCase.archive(warehouse);

    assertNotNull(warehouse.archivedAt);
    assertEquals(1, warehouseStore.updateCount);
    assertEquals(warehouse, warehouseStore.lastUpdatedWarehouse);
  }

  @Test
  void shouldSetArchivedAtTimestamp() {
    Warehouse warehouse = createTestWarehouse("AMSTERDAM-001", "MWH.002");
    LocalDateTime beforeArchive = LocalDateTime.now();

    archiveWarehouseUseCase.archive(warehouse);

    LocalDateTime afterArchive = LocalDateTime.now();
    assertNotNull(warehouse.archivedAt);
    assertTrue(warehouse.archivedAt.isAfter(beforeArchive) || warehouse.archivedAt.equals(beforeArchive));
    assertTrue(warehouse.archivedAt.isBefore(afterArchive) || warehouse.archivedAt.equals(afterArchive));
  }

  @Test
  void shouldThrowExceptionWhenWarehouseIsNull() {
    assertThrows(IllegalArgumentException.class,
        () -> archiveWarehouseUseCase.archive(null));
    assertEquals(0, warehouseStore.updateCount);
  }

  @Test
  void shouldUpdateWarehouseInStore() {
    Warehouse warehouse = createTestWarehouse("TILBURG-001", "MWH.003");

    archiveWarehouseUseCase.archive(warehouse);

    assertEquals(1, warehouseStore.updateCount);
    assertEquals(warehouse, warehouseStore.lastUpdatedWarehouse);
  }

  @Test
  void shouldNotModifyOtherWarehouseFields() {
    Warehouse warehouse = createTestWarehouse("ROTTERDAM-001", "MWH.004");
    String originalBusinessUnitCode = warehouse.businessUnitCode;
    String originalLocation = warehouse.location;
    Integer originalCapacity = warehouse.capacity;
    Integer originalStock = warehouse.stock;
    LocalDateTime originalCreatedAt = warehouse.createdAt;

    archiveWarehouseUseCase.archive(warehouse);

    assertEquals(originalBusinessUnitCode, warehouse.businessUnitCode);
    assertEquals(originalLocation, warehouse.location);
    assertEquals(originalCapacity, warehouse.capacity);
    assertEquals(originalStock, warehouse.stock);
    assertEquals(originalCreatedAt, warehouse.createdAt);
    assertNotNull(warehouse.archivedAt);
  }

  @Test
  void shouldArchiveMultipleWarehouses() {
    Warehouse warehouse1 = createTestWarehouse("ZWOLLE-001", "MWH.001");
    Warehouse warehouse2 = createTestWarehouse("AMSTERDAM-001", "MWH.002");

    archiveWarehouseUseCase.archive(warehouse1);
    archiveWarehouseUseCase.archive(warehouse2);

    assertNotNull(warehouse1.archivedAt);
    assertNotNull(warehouse2.archivedAt);
    assertEquals(2, warehouseStore.updateCount);
  }

  @Test
  void shouldArchiveWarehouseWithZeroStock() {
    Warehouse warehouse = createTestWarehouse("EINDHOVEN-001", "MWH.005");
    warehouse.stock = 0;

    archiveWarehouseUseCase.archive(warehouse);

    assertNotNull(warehouse.archivedAt);
    assertEquals(1, warehouseStore.updateCount);
  }

  @Test
  void shouldArchiveWarehouseWithFullCapacity() {
    Warehouse warehouse = createTestWarehouse("UTRECHT-001", "MWH.006");
    warehouse.capacity = 100;
    warehouse.stock = 100;

    archiveWarehouseUseCase.archive(warehouse);

    assertNotNull(warehouse.archivedAt);
    assertEquals(1, warehouseStore.updateCount);
  }

  private Warehouse createTestWarehouse(String location, String businessUnitCode) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = location;
    warehouse.capacity = 100;
    warehouse.stock = 50;
    warehouse.createdAt = LocalDateTime.now();
    return warehouse;
  }

  // Test implementation of WarehouseStore
  private static class TestWarehouseStore implements WarehouseStore {
    int updateCount = 0;
    Warehouse lastUpdatedWarehouse = null;
    List<Warehouse> warehouses = new ArrayList<>();

    @Override
    public List<Warehouse> getAll() {
      return warehouses;
    }

    @Override
    public void create(Warehouse warehouse) {
      warehouses.add(warehouse);
    }

    @Override
    public void update(Warehouse warehouse) {
      updateCount++;
      lastUpdatedWarehouse = warehouse;
    }

    @Override
    public void remove(Warehouse warehouse) {
      warehouses.remove(warehouse);
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return warehouses.stream()
          .filter(w -> w.businessUnitCode.equals(buCode))
          .findFirst()
          .orElse(null);
    }
  }
}