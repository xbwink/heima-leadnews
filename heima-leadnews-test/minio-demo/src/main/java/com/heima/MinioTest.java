package com.heima;

import com.heima.file.service.FileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author xb
 * @description TODO
 * @create 2024-04-19 14:23
 * @vesion 1.0
 */

@SpringBootTest(classes = App.class)
@RunWith(SpringRunner.class)
public class MinioTest {

    @Autowired
    FileStorageService fileStorageService;

    @Test
    public void testUpdateImgFile() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("D:\\wm.jpg");
        String path = fileStorageService.uploadImgFile("", "wamei.jpg", fileInputStream);
        System.out.println(path);
    }

}
