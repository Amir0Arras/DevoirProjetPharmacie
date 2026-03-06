package pharmacie.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter @Setter @NoArgsConstructor @RequiredArgsConstructor @ToString
public class Fournisseur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @NonNull
    @NotBlank
    @Size(max = 255)
    private String nom;

    @NonNull
    @NotBlank
    @Email
    @Size(max = 255)
    @Column(unique = true)
    private String email;

    @ToString.Exclude
    @JsonIgnoreProperties("fournisseurs")
    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(
        name = "FOURNISSEUR_CATEGORIE",
        joinColumns = @JoinColumn(name = "FOURNISSEUR_ID"),
        inverseJoinColumns = @JoinColumn(name = "CATEGORIE_CODE")
    )
    private Set<Categorie> categories = new HashSet<>();
}
