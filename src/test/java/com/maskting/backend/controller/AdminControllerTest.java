package com.maskting.backend.controller;

import com.maskting.backend.domain.RoleType;
import com.maskting.backend.domain.User;
import com.maskting.backend.factory.TokenFactory;
import com.maskting.backend.factory.UserFactory;
import com.maskting.backend.repository.ProfileRepository;
import com.maskting.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension.class)
class AdminControllerTest {

    private final String pre = "/admin";
    private String accessToken;

    @Autowired private MockMvc mockMvc;
    @Autowired private UserFactory userFactory;
    @Autowired private TokenFactory tokenFactory;
    @Autowired private UserRepository userRepository;
    @Autowired private ProfileRepository profileRepository;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        accessToken = tokenFactory.createAdminAccessToken();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @AfterEach
    void tearDown() {
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Admin Page 화면")
    void home() throws Exception {
        mockMvc.perform(get(pre)
                .header("accessToken", accessToken))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/home"))
                .andExpect(model().attributeExists("name"))
                .andDo(document("admin"));
    }

    @Test
    @DisplayName("심사받는 페이징 유저들 반환")
    void returnGuests() throws Exception {
        userFactory.createGuests();
        String draw = "1";
        String start = "0";
        String length = "10";

        mockMvc.perform(get(pre + "/guests")
                .header("accessToken", accessToken)
                .param("search[value]", "")
                .param("draw", draw)
                .param("start", start)
                .param("length", length))
                .andExpect(jsonPath("recordsTotal", is(16)))
                .andExpect(jsonPath("data.length()", is(10)))
                .andDo(document("admin/guests",
                        requestParameters(
                                parameterWithName("draw").description("몇번째 draw"),
                                parameterWithName("start").description("시작(offset)"),
                                parameterWithName("length").description("길이(limit)"),
                                parameterWithName("search[value]").description("검색값")
                        )));
    }

    @Test
    @DisplayName("심사받는 검색 페이징 유저들 반환")
    void returnGuestsWithSearch() throws Exception {
        userFactory.createGuests();
        String draw = "1";
        String start = "0";
        String length = "10";

        mockMvc.perform(get(pre + "/guests")
                .header("accessToken", accessToken)
                .param("search[value]", "Second")
                .param("draw", draw)
                .param("start", start)
                .param("length", length))
                .andExpect(jsonPath("recordsTotal", is(5)))
                .andExpect(jsonPath("data.length()", is(5)))
                .andDo(document("admin/guestsWithSearch",
                        requestParameters(
                                parameterWithName("draw").description("몇번째 draw"),
                                parameterWithName("start").description("시작(offset)"),
                                parameterWithName("length").description("길이(limit)"),
                                parameterWithName("search[value]").description("검색값")
                        )));
    }

    @Test
    @Transactional
    @DisplayName("유저 승인")
    void approval() throws Exception {
        User user = userFactory.createGuest("test", "testNickname");
        userRepository.save(user);

        assertTrue(user.isSort());
        assertEquals(RoleType.GUEST, user.getRoleType());
        mockMvc.perform(post(pre + "/approval/"  + user.getName())
                .header("accessToken", accessToken))
                .andDo(document("admin/approval"));

        assertFalse(user.isSort());
        assertEquals(RoleType.USER, user.getRoleType());
    }
}