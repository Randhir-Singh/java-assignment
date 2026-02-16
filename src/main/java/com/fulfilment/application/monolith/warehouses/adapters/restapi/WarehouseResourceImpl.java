package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import java.util.List;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject WarehouseRepository warehouseRepository;
  @Inject CreateWarehouseOperation createWarehouseOperation;
  @Inject ArchiveWarehouseOperation archiveWarehouseOperation;
  @Inject ReplaceWarehouseOperation replaceWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {

    if (data == null) {
      throw new IllegalArgumentException("Warehouse payload cannot be null");
    }
    var domainWarehouse =
            new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();

    domainWarehouse.businessUnitCode = data.getBusinessUnitCode();
    domainWarehouse.location = data.getLocation();
    domainWarehouse.capacity = data.getCapacity();
    domainWarehouse.stock = data.getStock();
    domainWarehouse.createdAt = java.time.LocalDateTime.now();

    // Execute business logic
    createWarehouseOperation.create(domainWarehouse);
    return  data;
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {

    if(id == null || id.isBlank()){
      throw new IllegalArgumentException("Warehouse ID cannot be null or blank");
    }
    try {
      Long warehouseId = Long.parseLong(id);
      var dbWarehouse = warehouseRepository.findById(warehouseId);
      if (dbWarehouse == null) {
        throw new IllegalArgumentException("Warehouse not found for ID: " + id);
      }
      return toWarehouseResponse(dbWarehouse.toWarehouse());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid warehouse ID format: " + id);
    }
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {

    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("Warehouse ID cannot be null or blank");
    }

    try {
      Long warehouseId = Long.parseLong(id);
      var dbWarehouse = warehouseRepository.findById(warehouseId);
      if (dbWarehouse == null) {
        throw new IllegalArgumentException("Warehouse not found for ID: " + id);
      }
      archiveWarehouseOperation.archive(dbWarehouse.toWarehouse());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid warehouse ID format: " + id);
    }
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {

    if(businessUnitCode == null || businessUnitCode.isBlank()){
      throw new IllegalArgumentException("Business unit code cannot be null or blank");
    }
    if (data == null) {
      throw new IllegalArgumentException("Warehouse payload cannot be null");
    }

    var existingWarehouse = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
    if (existingWarehouse == null) {
      throw new IllegalArgumentException("Warehouse not found for business unit code: " + businessUnitCode);
    }
    // Capacity Accommodation: new warehouse capacity must accommodate existing stock
    if (data.getCapacity() < existingWarehouse.stock) {
      throw new IllegalArgumentException(
              "New warehouse capacity (" + data.getCapacity() +
                      ") cannot be less than existing stock (" + existingWarehouse.stock + ")");
    }

    // Stock Matching: stock must be preserved during replacement
    if (!existingWarehouse.stock.equals(data.getStock())) {
      throw new IllegalArgumentException(
              "Stock mismatch: existing stock (" + existingWarehouse.stock +
                      ") must match new warehouse stock (" + data.getStock() + ")");
    }

    archiveWarehouseOperation.archive(existingWarehouse);

    var domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.businessUnitCode = data.getBusinessUnitCode();
    domainWarehouse.location = data.getLocation();
    domainWarehouse.capacity = data.getCapacity();
    domainWarehouse.stock = data.getStock();
    domainWarehouse.createdAt = java.time.LocalDateTime.now();

    // Execute the replace business logic
    replaceWarehouseOperation.replace(domainWarehouse);

    return toWarehouseResponse(domainWarehouse);
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
}
