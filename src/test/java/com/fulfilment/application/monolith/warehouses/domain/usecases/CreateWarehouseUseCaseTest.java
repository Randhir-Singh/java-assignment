package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;

public class CreateWarehouseUseCaseTest {

    private TestWarehouseStore warehouseStore;
    private TestLocationGateway locationGateway;
    private CreateWarehouseUseCase createWarehouseUseCase;

    @BeforeEach
    void setUp() {
        warehouseStore = new TestWarehouseStore();
        locationGateway = new TestLocationGateway();
        // CreateWarehouseUseCase expects LocationGateway, so we need to cast or use it differently
        createWarehouseUseCase = new CreateWarehouseUseCase(warehouseStore, new LocationGateway() {
            @Override
            public Location resolveByIdentifier(String identifier) {
                return locationGateway.resolveByIdentifier(identifier);
            }
        });
    }

    @Test
    void shouldThrowExceptionWhenLocationNotFound() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "BU123";
        warehouse.location = "INVALID-LOCATION";
        warehouse.capacity = 1000;
        warehouse.stock = 0;

        locationGateway.setLocation(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createWarehouseUseCase.create(warehouse)
        );

        assertTrue(exception.getMessage().contains("Invalid location"));
        assertEquals(0, warehouseStore.createCount);
    }

    @Test
    void shouldThrowExceptionWhenCapacityIsNegative() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "BU123";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = -100;
        warehouse.stock = 0;

        Location location = new Location("ZWOLLE-001", 1, 40);
        locationGateway.setLocation(location);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createWarehouseUseCase.create(warehouse)
        );

        assertTrue(exception.getMessage().contains("capacity"));
        assertEquals(0, warehouseStore.createCount);
    }

    @Test
    void shouldThrowExceptionWhenCurrentStockExceedsCapacity() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "BU123";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 1000;
        warehouse.stock = 1500;

        Location location = new Location("ZWOLLE-001", 1, 5000);
        locationGateway.setLocation(location);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createWarehouseUseCase.create(warehouse)
        );

        assertTrue(exception.getMessage().contains("Stock") && exception.getMessage().contains("capacity"));
        assertEquals(0, warehouseStore.createCount);
    }

    @Test
    void shouldCreateWarehouseWithZeroStock() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "BU123";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 1000;
        warehouse.stock = 0;

        Location location = new Location("ZWOLLE-001", 1, 5000);
        locationGateway.setLocation(location);

        createWarehouseUseCase.create(warehouse);

        assertEquals(1, warehouseStore.createCount);
        assertEquals(0, warehouseStore.lastCreatedWarehouse.stock);
    }

    @Test
    void shouldCreateWarehouseWithFullCapacity() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "BU123";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 1000;
        warehouse.stock = 1000;

        Location location = new Location("ZWOLLE-001", 1, 5000);
        locationGateway.setLocation(location);

        createWarehouseUseCase.create(warehouse);

        assertEquals(1, warehouseStore.createCount);
        assertEquals(1000, warehouseStore.lastCreatedWarehouse.stock);
    }

    @Test
    void shouldCreateMultipleWarehouses() {
        Location location1 = new Location("ZWOLLE-001", 5, 5000);
        locationGateway.setLocation(location1);

        Warehouse warehouse1 = new Warehouse();
        warehouse1.businessUnitCode = "BU001";
        warehouse1.location = "ZWOLLE-001";
        warehouse1.capacity = 1000;
        warehouse1.stock = 100;

        Warehouse warehouse2 = new Warehouse();
        warehouse2.businessUnitCode = "BU002";
        warehouse2.location = "AMSTERDAM-001";
        warehouse2.capacity = 2000;
        warehouse2.stock = 200;

        createWarehouseUseCase.create(warehouse1);

        Location location2 = new Location("AMSTERDAM-001", 5, 5000);
        locationGateway.setLocation(location2);
        createWarehouseUseCase.create(warehouse2);

        assertEquals(2, warehouseStore.createCount);
        assertEquals(2, warehouseStore.warehouses.size());
    }

    // Test implementation of WarehouseStore
    private static class TestWarehouseStore implements WarehouseStore {
        int createCount = 0;
        int updateCount = 0;
        Warehouse lastCreatedWarehouse;
        List<Warehouse> warehouses = new ArrayList<>();

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
        public Warehouse findByBusinessUnitCode(String businessUnitCode) {
            return warehouses.stream()
                    .filter(w -> w.businessUnitCode != null && w.businessUnitCode.equals(businessUnitCode))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Warehouse> getAll() {
            return new ArrayList<>(warehouses);
        }
    }

    // Test implementation of LocationResolver
    private static class TestLocationGateway implements com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver {
        private Location location;

        void setLocation(Location location) {
            this.location = location;
        }

        @Override
        public Location resolveByIdentifier(String identifier) {
            return location;
        }
    }
}