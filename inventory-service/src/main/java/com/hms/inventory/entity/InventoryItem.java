package com.hms.inventory.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
@Entity @Table(name="inventory_items")
public class InventoryItem {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; private String itemName; private String category; private BigDecimal quantity; private String unit; private BigDecimal reorderThreshold; private BigDecimal unitCost;
 public UUID getId(){return id;} public void setId(UUID v){id=v;} public String getItemName(){return itemName;} public void setItemName(String v){itemName=v;} public String getCategory(){return category;} public void setCategory(String v){category=v;} public BigDecimal getQuantity(){return quantity;} public void setQuantity(BigDecimal v){quantity=v;} public String getUnit(){return unit;} public void setUnit(String v){unit=v;} public BigDecimal getReorderThreshold(){return reorderThreshold;} public void setReorderThreshold(BigDecimal v){reorderThreshold=v;} public BigDecimal getUnitCost(){return unitCost;} public void setUnitCost(BigDecimal v){unitCost=v;}
}
