package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;

public class ReplaceWarehouseUseCaseTest {

  private TestWarehouseStore warehouseStore;
  private ReplaceWarehouseUseCase replaceWarehouseUseCase;

  @BeforeEach
  void setUp() {
    warehouseStore = new TestWarehouseStore();
    replaceWarehouseUseCase = new ReplaceWarehouseUseCase(warehouseStore);
  }

  @Test
  void shouldReplaceWarehouseSuccessfully() {
    Warehouse newWarehouse = createTestWarehouse("ZWOLLE-001", "MWH.001", 1500, 100);

    replaceWarehouseUseCase.replace(newWarehouse);

    assertEquals(1, warehouseStore.createCount);
    assertNotNull(warehouseStore.lastCreatedWarehouse);
    assertEquals("MWH.001", warehouseStore.lastCreatedWarehouse.businessUnitCode);
    assertEquals("ZWOLLE-001", warehouseStore.lastCreatedWarehouse.location);
    assertEquals(1500, warehouseStore.lastCreatedWarehouse.capacity);
  }

  @Test
  void shouldThrowExceptionWhenWarehouseIsNull() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> replaceWarehouseUseCase.replace(null)
    );

    assertEquals("Warehouse cannot be null", exception.getMessage());
    assertEquals(0, warehouseStore.createCount);
  }

  @Test
  void shouldCreateNewWarehouseInStore() {
    Warehouse newWarehouse = createTestWarehouse("AMSTERDAM-001", "MWH.002", 2000, 500);

    replaceWarehouseUseCase.replace(newWarehouse);

    assertEquals(1, warehouseStore.createCount);
    assertEquals(newWarehouse, warehouseStore.lastCreatedWarehouse);
    assertTrue(warehouseStore.warehouses.contains(newWarehouse));
  }

  @Test
  void shouldReplaceWarehouseWithDifferentCapacity() {
    Warehouse oldWarehouse = createTestWarehouse("ZWOLLE-001", "MWH.001", 1000, 500);
    warehouseStore.warehouses.add(oldWarehouse);

    Warehouse newWarehouse = createTestWarehouse("ZWOLLE-001", "MWH.001", 1500, 500);
    replaceWarehouseUseCase.replace(newWarehouse);

    assertEquals(1, warehouseStore.createCount);
    assertEquals(1500, warehouseStore.lastCreatedWarehouse.capacity);
  }

  @Test
  void shouldReplaceWarehouseWithDifferentLocation() {
    Warehouse oldWarehouse = createTestWarehouse("ZWOLLE-001", "MWH.001", 1000, 500);
    warehouseStore.warehouses.add(oldWarehouse);

    Warehouse newWarehouse = createTestWarehouse("AMSTERDAM-001", "MWH.001", 1000, 500);
    replaceWarehouseUseCase.replace(newWarehouse);

    assertEquals(1, warehouseStore.createCount);
    assertEquals("AMSTERDAM-001", warehouseStore.lastCreatedWarehouse.location);
  }

  @Test
  void shouldReplaceWarehouseWithZeroStock() {
    Warehouse newWarehouse = createTestWarehouse("TILBURG-001", "MWH.003", 1000, 0);

    replaceWarehouseUseCase.replace(newWarehouse);

    assertEquals(1, warehouseStore.createCount);
    assertEquals(0, warehouseStore.lastCreatedWarehouse.stock);
  }

  @Test
  void shouldReplaceWarehouseWithFullCapacity() {
    Warehouse newWarehouse = createTestWarehouse("EINDHOVEN-001", "MWH.004", 1000, 1000);

    replaceWarehouseUseCase.replace(newWarehouse);

    assertEquals(1, warehouseStore.createCount);
    assertEquals(1000, warehouseStore.lastCreatedWarehouse.stock);
    assertEquals(1000, warehouseStore.lastCreatedWarehouse.capacity);
  }

  @Test
  void shouldReplaceMultipleWarehouses() {
    Warehouse warehouse1 = createTestWarehouse("ZWOLLE-001", "MWH.001", 1000, 100);
    Warehouse warehouse2 = createTestWarehouse("AMSTERDAM-001", "MWH.002", 2000, 200);

    replaceWarehouseUseCase.replace(warehouse1);
    replaceWarehouseUseCase.replace(warehouse2);

    assertEquals(2, warehouseStore.createCount);
    assertEquals(2, warehouseStore.warehouses.size());
  }

  @Test
  void shouldPreserveCreatedAtTimestamp() {
    LocalDateTime specificTime = LocalDateTime.of(2025, 1, 15, 10, 30);
    Warehouse newWarehouse = createTestWarehouse("ROTTERDAM-001", "MWH.005", 1500, 300);
    newWarehouse.createdAt = specificTime;

    replaceWarehouseUseCase.replace(newWarehouse);

    assertEquals(1, warehouseStore.createCount);
    assertEquals(specificTime, warehouseStore.lastCreatedWarehouse.createdAt);
  }

  @Test
  void shouldReplaceWarehouseWithAllFieldsSet() {
    Warehouse newWarehouse = createTestWarehouse("HELMOND-001", "MWH.006", 1800, 600);
    newWarehouse.archivedAt = null;

    replaceWarehouseUseCase.replace(newWarehouse);

    assertEquals(1, warehouseStore.createCount);
    assertEquals("HELMOND-001", warehouseStore.lastCreatedWarehouse.location);
    assertEquals("MWH.006", warehouseStore.lastCreatedWarehouse.businessUnitCode);
    assertEquals(1800, warehouseStore.lastCreatedWarehouse.capacity);
    assertEquals(600, warehouseStore.lastCreatedWarehouse.stock);
    assertNull(warehouseStore.lastCreatedWarehouse.archivedAt);
  }

  @Test
  void shouldAllowReplacementWithSameBusinessUnitCode() {
    Warehouse warehouse1 = createTestWarehouse("ZWOLLE-001", "MWH.001", 1000, 100);
    Warehouse warehouse2 = createTestWarehouse("AMSTERDAM-001", "MWH.001", 1500, 100);

    replaceWarehouseUseCase.replace(warehouse1);
    replaceWarehouseUseCase.replace(warehouse2);

    assertEquals(2, warehouseStore.createCount);
    assertEquals("MWH.001", warehouseStore.lastCreatedWarehouse.businessUnitCode);
    assertEquals("AMSTERDAM-001", warehouseStore.lastCreatedWarehouse.location);
  }

  @Test
  void shouldReplaceWarehouseWithLargeCapacity() {
    Warehouse newWarehouse = createTestWarehouse("VETSBY-001", "MWH.007", 10000, 5000);

    replaceWarehouseUseCase.replace(newWarehouse);

    assertEquals(1, warehouseStore.createCount);
    assertEquals(10000, warehouseStore.lastCreatedWarehouse.capacity);
    assertEquals(5000, warehouseStore.lastCreatedWarehouse.stock);
  }

  @Test
  void shouldHandleWarehouseWithNoArchivedDate() {
    Warehouse newWarehouse = createTestWarehouse("UTRECHT-001", "MWH.008", 1200, 300);
    assertNull(newWarehouse.archivedAt);

    replaceWarehouseUseCase.replace(newWarehouse);

    assertEquals(1, warehouseStore.createCount);
    assertNull(warehouseStore.lastCreatedWarehouse.archivedAt);
  }

  private Warehouse createTestWarehouse(String location, String businessUnitCode,
                                         Integer capacity, Integer stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;
    return warehouse;
  }

  // Test implementation of WarehouseStore
  private static class TestWarehouseStore implements WarehouseStore {
    int createCount = 0;
    int updateCount = 0;
    Warehouse lastCreatedWarehouse;
    List<Warehouse> warehouses = new ArrayList<>();

    @Override
    public List<Warehouse> getAll() {
      return warehouses;
    }

    @Override
    public void create(Warehouse warehouse) {
      createCount++;
      lastCreatedWarehouse = warehouse;
      warehouses.add(warehouse);
    }

    @Override
    public void update(Warehouse warehouse) {
      updateCount++;
    }

    @Override
    public void remove(Warehouse warehouse) {
      warehouses.remove(warehouse);
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return warehouses.stream()
          .filter(w -> w.businessUnitCode != null && w.businessUnitCode.equals(buCode))
          .findFirst()
          .orElse(null);
    }
  }
}
