package pharmacie.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pharmacie.service.ApprovisionnementService;

@RestController
@RequestMapping("/rest")
public class ApprovisionnementController {

    private final ApprovisionnementService approService;

    @Autowired
    public ApprovisionnementController(ApprovisionnementService approService) {
        this.approService = approService;
    }

    /**
     * Déclenchement manuel du processus de demande de devis vers les fournisseurs.
     */
    @PostMapping("/reapprovisionnement")
    public ResponseEntity<String> lancerReappro() {
        approService.notifieFournisseurs();
        return ResponseEntity.ok("Processus de réapprovisionnement lancé");
    }

    /**
     * Endpoint GET pour tester rapidement l'envoi des mails
     */
    @GetMapping("/test-mail")
    public ResponseEntity<String> testMail() {
        try {
            approService.notifieFournisseurs();
            return ResponseEntity.ok("Test mail envoyé ! Vérifie Postmark et les logs Koyeb.");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Erreur lors de l'envoi du mail : " + e.getMessage());
        }
    }
}