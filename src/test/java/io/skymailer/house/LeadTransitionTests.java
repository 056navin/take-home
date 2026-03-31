package io.skymailer.house;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.skymailer.house.dto.CreateLeadRequest;
import io.skymailer.house.dto.LoginRequest;
import io.skymailer.house.dto.TransitionRequest;
import io.skymailer.house.model.LeadPriority;
import io.skymailer.house.model.LeadStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class LeadTransitionTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        // Login to get JWT token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@house.com");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(responseBody).get("token").asText();
    }

    @Test
    @DisplayName("Test 1: Valid transition NEW → CONTACTED succeeds and updates the lead")
    void validTransition_NewToContacted_Succeeds() throws Exception {
        // Lead ID 1 is seeded with status NEW
        TransitionRequest request = new TransitionRequest();
        request.setStatus(LeadStatus.CONTACTED);

        mockMvc.perform(post("/leads/1/transition")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONTACTED"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Test 2: Invalid transition NEW → VISITED returns correct error message")
    void invalidTransition_NewToVisited_ReturnsError() throws Exception {
        // Lead ID 1 is seeded with status NEW
        TransitionRequest request = new TransitionRequest();
        request.setStatus(LeadStatus.VISITED);

        mockMvc.perform(post("/leads/1/transition")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        containsString("Cannot move from 'NEW' to 'VISITED'")))
                .andExpect(jsonPath("$.message",
                        containsString("Next valid status is 'CONTACTED'")));
    }

    @Test
    @DisplayName("Test 3: Creating a duplicate lead (same phone + same property) is rejected")
    void duplicateLead_SamePhoneAndProperty_IsRejected() throws Exception {
        // Lead with phone +919876543210 and property 1 already exists (seeded)
        CreateLeadRequest request = new CreateLeadRequest();
        request.setBuyerName("Duplicate Buyer");
        request.setPhone("+919876543210");
        request.setEmail("duplicate@email.com");
        request.setPropertyId(1L);
        request.setPriority(LeadPriority.WARM);

        mockMvc.perform(post("/leads")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message",
                        containsString("already exists")));
    }

    @Test
    @DisplayName("Test 4: LOST can be set from any non-terminal status")
    void lostTransition_FromContacted_Succeeds() throws Exception {
        // Lead ID 4 is seeded with status CONTACTED
        TransitionRequest request = new TransitionRequest();
        request.setStatus(LeadStatus.LOST);

        mockMvc.perform(post("/leads/4/transition")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LOST"));
    }

    @Test
    @DisplayName("Test 5: Transition from terminal status BOOKED is rejected")
    void terminalStatus_Booked_RejectsTransition() throws Exception {
        // Lead ID 10 is seeded with status BOOKED
        TransitionRequest request = new TransitionRequest();
        request.setStatus(LeadStatus.LOST);

        mockMvc.perform(post("/leads/10/transition")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        containsString("terminal status")));
    }

    @Test
    @DisplayName("Test 6: Full pipeline transition NEW → CONTACTED → VISITED → BOOKED sets property to BOOKED")
    void fullPipeline_BookedSetsPropertyStatus() throws Exception {
        // Create a fresh lead on property 2 (Available)
        CreateLeadRequest createReq = new CreateLeadRequest();
        createReq.setBuyerName("Full Pipeline Buyer");
        createReq.setPhone("+919999999999");
        createReq.setEmail("pipeline@email.com");
        createReq.setPropertyId(2L);
        createReq.setPriority(LeadPriority.HOT);

        MvcResult createResult = mockMvc.perform(post("/leads")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long leadId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // NEW → CONTACTED
        transition(leadId, LeadStatus.CONTACTED);
        // CONTACTED → VISITED
        transition(leadId, LeadStatus.VISITED);
        // VISITED → BOOKED
        mockMvc.perform(post("/leads/" + leadId + "/transition")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transitionReq(LeadStatus.BOOKED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BOOKED"));

        // Verify property is now BOOKED
        mockMvc.perform(get("/properties/2")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BOOKED"));
    }

    private void transition(Long leadId, LeadStatus status) throws Exception {
        mockMvc.perform(post("/leads/" + leadId + "/transition")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transitionReq(status))))
                .andExpect(status().isOk());
    }

    private TransitionRequest transitionReq(LeadStatus status) {
        TransitionRequest req = new TransitionRequest();
        req.setStatus(status);
        return req;
    }
}

