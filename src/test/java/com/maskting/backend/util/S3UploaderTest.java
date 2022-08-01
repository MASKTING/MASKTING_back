package com.maskting.backend.util;

import com.maskting.backend.dto.response.S3Response;
import io.findify.s3mock.S3Mock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(S3MockConfig.class)
class S3UploaderTest {

    @Autowired
    S3Mock s3Mock;

    @Autowired
    S3Uploader s3Uploader;

    @AfterEach
    public void tearDown() {
        s3Mock.stop();
    }

    @Test
    @DisplayName("파일 업로드")
    void upload() throws IOException {
        String name = "test";
        String dirname = "static";
        MockMultipartFile mockMultipartFile = new MockMultipartFile(name, "test.png",
                "image/png", "test data".getBytes());

        S3Response s3Response = s3Uploader.upload(mockMultipartFile, dirname);

        assertThat(s3Response.getName()).contains(name);
        assertThat(s3Response.getPath()).contains(dirname);
    }
}