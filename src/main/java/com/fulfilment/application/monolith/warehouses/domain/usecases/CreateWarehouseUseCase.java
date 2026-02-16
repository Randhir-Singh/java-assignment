package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationGateway locationGateway;

  @Inject
  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationGateway locationGateway) {
    this.warehouseStore = warehouseStore;
    this.locationGateway = locationGateway;
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    // Validate business unit code doesn't already exist
    var existingWarehouse = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existingWarehouse != null) {
      throw new IllegalArgumentException("Business unit code already exists: " + warehouse.businessUnitCode);
    }

    // Validate location exists and is valid
    var location = locationGateway.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Invalid location: " + warehouse.location);
    }

    // Check warehouse creation feasibility (max warehouses per location)
    var warehousesAtLocation = warehouseStore.getAll().stream()
            .filter(w -> w.location.equals(warehouse.location))
            .count();
    if (warehousesAtLocation >= location.maxNumberOfWarehouses) {
      throw new IllegalArgumentException("Maximum number of warehouses reached for location: " + warehouse.location);
    }

    // Validate capacity doesn't exceed location's max capacity
    if (warehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException("Warehouse capacity exceeds location maximum: " + location.maxCapacity);
    }

    // Validate warehouse can handle the stock
    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Stock cannot exceed warehouse capacity");
    }

    // if all went well, create the warehouse
    warehouseStore.create(warehouse);
  }
}
