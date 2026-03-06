package pharmacie.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import pharmacie.service.ApprovisionnementService;

import static org.mockito.Mockito.doNothing;

@WebMvcTest(ApprovisionnementController.class)
class ApprovisionnementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApprovisionnementService approService;

    @Test
    void endpointReapproEnvoieMail() throws Exception {
        doNothing().when(approService).notifieFournisseurs();

        mockMvc.perform(post("/rest/reapprovisionnement"))
                .andExpect(status().isOk())
                .andExpect(content().string("Processus de réapprovisionnement lancé"));
    }
}
