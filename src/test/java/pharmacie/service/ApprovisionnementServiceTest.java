package pharmacie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import pharmacie.dao.CategorieRepository;
import pharmacie.dao.FournisseurRepository;
import pharmacie.dao.MedicamentRepository;
import pharmacie.entity.Categorie;
import pharmacie.entity.Fournisseur;
import pharmacie.entity.Medicament;

@SpringBootTest
@Import(ApprovisionnementService.class)
class ApprovisionnementServiceTest {

    @Autowired
    private ApprovisionnementService approService;

    @Autowired
    private MedicamentRepository medicamentRepository;
    @Autowired
    private CategorieRepository categorieRepository;
    @Autowired
    private FournisseurRepository fournisseurRepository;

    @MockBean
    private RestTemplate restTemplate; // intercept network calls

    @BeforeEach
    void setUp() {
        // clear in-memory DB (spring boot test with create-drop so should start clean automatically)
    }

 @Test
void emailsEnvoyesAuxFournisseursAvecMedsALivrer() {

    // 1️⃣ Créer catégorie
    Categorie cat = new Categorie();
    cat.setLibelle("TestCat");
    cat = categorieRepository.saveAndFlush(cat);

    // 2️⃣ Créer fournisseurs
    Fournisseur f1 = new Fournisseur();
    f1.setNom("F1");
    f1.setEmail("aarras@etud.univ-jfc.fr");
    f1.getCategories().add(cat); // côté owner
    fournisseurRepository.saveAndFlush(f1);

    Fournisseur f2 = new Fournisseur();
    f2.setNom("F2");
    f2.setEmail("arrasamir2024@gmail.com");
    f2.getCategories().add(cat);
    fournisseurRepository.saveAndFlush(f2);

    // 3️⃣ Créer médicament
    Medicament m = new Medicament();
    m.setNom("LowStockMed");
    m.setCategorie(cat);
    m.setUnitesEnStock(5);
    m.setNiveauDeReappro(10);
    medicamentRepository.saveAndFlush(m);

    // 4️⃣ Mock RestTemplate
    when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
            .thenReturn(ResponseEntity.ok("ok"));

    // 5️⃣ Exécuter le service
    approService.notifieFournisseurs();

    // 6️⃣ Vérifications
    verify(restTemplate, times(2))
            .postForEntity(any(String.class), any(), eq(String.class));

    org.mockito.ArgumentCaptor<org.springframework.http.HttpEntity> captor =
            org.mockito.ArgumentCaptor.forClass(org.springframework.http.HttpEntity.class);

    verify(restTemplate, times(2))
            .postForEntity(any(String.class), captor.capture(), eq(String.class));

    List<org.springframework.http.HttpEntity> all = captor.getAllValues();

    for (org.springframework.http.HttpEntity entity : all) {
        Object payload = entity.getBody();
        assertThat(payload.toString()).contains("LowStockMed");
        assertThat(payload.toString()).contains("TestCat");
    }
}
}
