package com.skillupcloud.controller;

import com.skillupcloud.service.S3PlayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(S3PlayController.class)
class S3PlayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private S3PlayService s3PlayService;

    @Test
    void addFileUploadsMultipartJsonFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "payload.json",
                "application/json",
                "{\"message\":\"hello\"}".getBytes());
        when(s3PlayService.uploadJsonFile(any())).thenReturn("payload.json");

        mockMvc.perform(multipart("/addFile").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("payload.json"))
                .andExpect(jsonPath("$.message").value("File uploaded"));

        verify(s3PlayService).uploadJsonFile(any());
    }

    @Test
    void readFileReturnsJsonFromService() throws Exception {
        when(s3PlayService.readJsonFile("payload.json"))
                .thenReturn("{\"message\":\"hello\"}".getBytes());

        mockMvc.perform(get("/readFile").param("fileName", "payload.json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json("{\"message\":\"hello\"}"));

        verify(s3PlayService).readJsonFile("payload.json");
    }
}
