package com.isep.appli.dbModels;

import com.isep.appli.models.enums.ItemCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "item")
public class Item implements Serializable {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Lob
	@Column(columnDefinition = "MEDIUMBLOB")
	private String urlImage;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ItemCategory category;

	@Column()
	private boolean canUse;

	private String description;
	private String useDescription;
	private Instant createdAt;

	@UpdateTimestamp
	@Column()
	private Instant updatedAt;


	@OneToMany(fetch = FetchType.LAZY, mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Inventory> inventories;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Shop> shops;


	public int nbTotalElement() {
		return inventories.stream().mapToInt(Inventory::getQuantity).sum() + shops.stream().mapToInt(Shop::getQuantity).sum();
	}

	public boolean itemInShop(Long idUser) {
		return shops.stream().anyMatch(shop -> shop.getSeller().getId().equals(idUser) );
	}


	@Override
	public String toString() {
		return "Item{" +
				"id=" + id +
				", name='" + name + '\'' +
				", urlImage='" + urlImage + '\'' +
				", category=" + category +
				", canUse=" + canUse +
				", description='" + description + '\'' +
				", useDescription='" + useDescription + '\'' +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				", inventoriesSize='" + (inventories != null ? inventories.size() : "null") + '\'' +
				", shopsSize='" + (shops != null ? shops.size() : "null") + '\'' +
				'}';
	}


}
