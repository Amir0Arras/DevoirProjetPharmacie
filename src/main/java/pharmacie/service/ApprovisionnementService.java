package pharmacie.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import pharmacie.dao.MedicamentRepository;
import pharmacie.entity.Categorie;
import pharmacie.entity.Fournisseur;
import pharmacie.entity.Medicament;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ApprovisionnementService {

    private final MedicamentRepository medicamentDao;
    private final RestTemplate restTemplate;

    /**
     * jeton d'API Postmark (X-Postmark-Server-Token header)
     */
    @Value("${postmark.token}")
    private String postmarkToken;

    /**
     * adresse d'expéditeur utilisée pour les messages Postmark
     */
    @Value("${postmark.from}")
    private String fromAddress;

    @Autowired
    public ApprovisionnementService(MedicamentRepository medicamentDao, RestTemplate restTemplate) {
        this.medicamentDao = medicamentDao;
        this.restTemplate = restTemplate;
    }

    /**
     * Exécute le processus d'identification des médicaments à réapprovisionner et
     * notifie chaque fournisseur par mail via l'API Postmark.
     */
@Transactional(readOnly = true)
public void notifieFournisseurs() {

    List<Medicament> toOrder = medicamentDao.findAll()
            .stream()
            .filter(m -> m.getUnitesEnStock() < m.getNiveauDeReappro())
            .toList();

    log.info("{} médicaments à reapprovisionner", toOrder.size());

    Map<Fournisseur, Map<Categorie, List<Medicament>>> grouping = new HashMap<>();

    for (Medicament m : toOrder) {

        Categorie cat = m.getCategorie();

        for (Fournisseur f : cat.getFournisseurs()) {

            grouping
                .computeIfAbsent(f, foo -> new HashMap<>())
                .computeIfAbsent(cat, c -> new ArrayList<>())
                .add(m);
        }
    }

    grouping.forEach((supplier, catMap) -> {

        String body = buildMessage(supplier, catMap);

        sendMail(
            supplier.getEmail(),
            "Demande de devis de réapprovisionnement",
            body
        );

    });
}

    private String buildMessage(Fournisseur supplier, Map<Categorie, List<Medicament>> catMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bonjour ").append(supplier.getNom()).append(",\n\n");
        sb.append("Merci de nous adresser un devis pour les médicaments suivants, classés par catégorie :\n\n");
        for (var entry : catMap.entrySet()) {
            sb.append("Catégorie : ").append(entry.getKey().getLibelle()).append("\n");
            for (Medicament m : entry.getValue()) {
                sb.append("  - ").append(m.getNom())
                  .append(" (stock="+m.getUnitesEnStock()+", niveau="+m.getNiveauDeReappro()+")")
                  .append("\n");
            }
            sb.append("\n");
        }
        sb.append("Cordialement,\nVotre pharmacie");
        return sb.toString();
    }

    private void sendMail(String to, String subject, String body) {
        String url = "https://api.postmarkapp.com/email";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Postmark-Server-Token", postmarkToken);

        Map<String, Object> payload = new HashMap<>();
        payload.put("From", fromAddress);
        payload.put("To", to);
        payload.put("Subject", subject);
        payload.put("TextBody", body);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity(url, request, String.class);
    }
}
