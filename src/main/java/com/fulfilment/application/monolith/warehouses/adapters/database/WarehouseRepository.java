package com.fulfilment.application.monolith.warehouses.adapters.database;

import java.util.List;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {

    if (warehouse == null){
      throw new IllegalArgumentException("Warehouse cannot be null");
    }
    var entity = new DbWarehouse();
    entity.businessUnitCode = warehouse.businessUnitCode;
    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.createdAt = warehouse.createdAt != null
            ? warehouse.createdAt
            : java.time.LocalDateTime.now();
    entity.archivedAt = warehouse.archivedAt;

    persist(entity);

  }

  @Override
  public void update(Warehouse warehouse) {

    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse cannot be null");
    }

    var entity = this.find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (entity == null) {
      throw new IllegalArgumentException("Warehouse not found with business unit code: " + warehouse.businessUnitCode);
    }

    entity.businessUnitCode = warehouse.businessUnitCode;
    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.archivedAt = warehouse.archivedAt;

    persist(entity);
  }

  @Override
  public void remove(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse cannot be null");
    }

    var entity = this.find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (entity == null) {
      throw new IllegalArgumentException("Warehouse not found with business unit code: " + warehouse.businessUnitCode);
    }

    delete(entity);
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {

    if (buCode == null || buCode.isBlank()) {
      throw new IllegalArgumentException("Business unit code cannot be null or blank");
    }

    var entity = this.find("businessUnitCode", buCode).firstResult();

    if (entity == null) {
      return null;
    }

    return entity.toWarehouse();
  }
}
