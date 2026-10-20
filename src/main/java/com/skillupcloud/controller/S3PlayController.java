package com.skillupcloud.controller;

import com.skillupcloud.service.S3PlayService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class S3PlayController {

    private final S3PlayService s3PlayService;

    public S3PlayController(S3PlayService s3PlayService) {
        this.s3PlayService = s3PlayService;
    }

    @PostMapping(value = "/addFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> addFile(@RequestParam("file") MultipartFile file) {
        String fileName = s3PlayService.uploadJsonFile(file);
        return ResponseEntity.status(201).body(new FileUploadResponse(fileName, "File uploaded"));
    }

    @GetMapping(value = "/readFile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> readFile(@RequestParam("fileName") String fileName) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(s3PlayService.readJsonFile(fileName));
    }

    public record FileUploadResponse(String fileName, String message) {
    }
}
