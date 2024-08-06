package com.isep.appli.dbModels;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "inventory")
public class Inventory implements Serializable {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "item_id", nullable = false)
	@JsonBackReference
	private Item item;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "character_id", nullable = false)
	@JsonBackReference
	private Personnage character;

	private int quantity;

	@Override
	public String toString() {
		return "Inventory{" +
				"id=" + id +
				", item=" + (item != null ? item.getId() : "null") +
				", character=" + (character != null ? character.getId() : "null") +
				", quantity=" + quantity +
				'}';
	}
}
