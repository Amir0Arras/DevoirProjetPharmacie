package pharmacie.entity;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import pharmacie.entity.Fournisseur;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @RequiredArgsConstructor @ToString
public class Categorie {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(AccessLevel.NONE) // la clé est auto-générée par la BD, On ne veut pas de "setter"
	private Integer code;

	@NonNull
	@Size(min = 1, max = 255)
	@Column(unique=true, length = 255)
	@NotBlank // pour éviter les libellés vides
	private String libelle;

	@Size(max = 255)
	@Column(length = 255)
	private String description;

	@ToString.Exclude
	// CascadeType.ALL signifie que toutes les opérations CRUD sur la catégorie sont également appliquées à ses médicaments
	@OneToMany(cascade = {CascadeType.ALL}, mappedBy = "categorie")
	// pour éviter la boucle infinie si on convertit la catégorie en JSON
	@JsonIgnoreProperties({"categorie", "lignes"})
	private List<Medicament> medicaments = new LinkedList<>();

	/*
	 * Relation plusieurs-à-plusieurs avec les fournisseurs. Un fournisseur peut
	 * proposer plusieurs catégories et une catégorie peut être fournie par
	 * plusieurs fournisseurs. La jointure est stockée dans la table
	 * FOURNISSEUR_CATEGORIE.
	 */
	@ToString.Exclude
	@ManyToMany(mappedBy = "categories")
	@JsonIgnoreProperties("categories")
	private java.util.Set<Fournisseur> fournisseurs = new java.util.HashSet<>();

}
