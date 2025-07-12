package io.github.chehsunliu.seeder.minio;

import static org.junit.jupiter.api.Assertions.*;

import io.github.chehsunliu.seeder.core.SeederManager;
import java.net.URI;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

class MinioSeederTest {
    private static final String minioEndpoint = "http://localhost:9000";
    private static final String minioUsername = "admin";
    private static final String minioPassword = "admin-password";

    @Test
    void testIsUsable() {
        var manager = SeederManager.builder()
                .seeder(new MinioSeeder(minioEndpoint, minioUsername, minioPassword))
                .build();
        manager.truncate();
        manager.seedResource("/test-data/data-1");

        assertArrayEquals(new String[] {"a/b.txt", "c.txt"}, listAllKeys("demo-1"));
        assertArrayEquals(new String[] {"d.txt"}, listAllKeys("demo-2"));
        assertEquals(0, listAllKeys("demo-3").length);
    }

    @Test
    void testNestedFolder() {
        var manager = SeederManager.builder()
                .seeder(new MinioSeeder(minioEndpoint, minioUsername, minioPassword)
                        .withGetFolderPath((bucket) -> Path.of("minio").resolve(bucket)))
                .build();
        manager.truncate();
        manager.seedResource("/test-data/data-2");

        assertArrayEquals(new String[] {"a/b/z.txt", "c.txt"}, listAllKeys("demo-1"));
        assertArrayEquals(new String[] {"d.txt"}, listAllKeys("demo-2"));
        assertEquals(0, listAllKeys("demo-3").length);
    }

    private static final S3Client s3Client = S3Client.builder()
            .endpointOverride(URI.create(minioEndpoint))
            .credentialsProvider(
                    StaticCredentialsProvider.create(AwsBasicCredentials.create(minioUsername, minioPassword)))
            .region(Region.AP_EAST_2)
            .forcePathStyle(true)
            .build();

    private static String[] listAllKeys(String bucket) {
        return s3Client
                .listObjectsV2Paginator(
                        ListObjectsV2Request.builder().bucket(bucket).build())
                .stream()
                .flatMap(r -> r.contents().stream())
                .map(S3Object::key)
                .sorted()
                .toArray(String[]::new);
    }
}
