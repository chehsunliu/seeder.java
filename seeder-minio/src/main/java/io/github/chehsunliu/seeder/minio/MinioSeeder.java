package io.github.chehsunliu.seeder.minio;

import io.github.chehsunliu.seeder.core.Seeder;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

public class MinioSeeder implements Seeder {
    private final S3Client s3Client;
    private final List<String> buckets;
    private Function<String, Path> getFolderPath;

    public MinioSeeder(String endpoint, String username, String password) {
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(username, password)))
                .region(Region.AP_EAST_2)
                .forcePathStyle(true)
                .build();
        this.buckets =
                this.s3Client.listBuckets().buckets().stream().map(Bucket::name).collect(Collectors.toList());
        this.getFolderPath = Path::of;
    }

    @Override
    public void truncate() {
        this.buckets.forEach(this::deleteObjects);
    }

    @Override
    @SneakyThrows
    public void seedResource(String name) {
        var url = Objects.requireNonNull(this.getClass().getResource(name));

        for (String bucket : this.buckets) {
            var basePath = Path.of(url.getPath()).resolve(this.getFolderPath.apply(bucket));
            if (!basePath.toFile().exists()) {
                continue;
            }

            try (var stream = Files.walk(basePath)) {
                stream.filter(Files::isRegularFile).forEach(p -> {
                    var key = basePath.relativize(p).toString();
                    this.s3Client.putObject(
                            PutObjectRequest.builder().bucket(bucket).key(key).build(), p);
                });
            }
        }
    }

    public MinioSeeder withGetFolderPath(Function<String, Path> getFolderPath) {
        this.getFolderPath = getFolderPath;
        return this;
    }

    private void deleteObjects(String bucket) {
        var objectIdentifiers =
                this.s3Client
                        .listObjectsV2Paginator(
                                ListObjectsV2Request.builder().bucket(bucket).build())
                        .stream()
                        .flatMap(r -> r.contents().stream())
                        .map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
                        .toList();
        if (objectIdentifiers.isEmpty()) {
            return;
        }

        var r = this.s3Client.deleteObjects(DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(Delete.builder().objects(objectIdentifiers).build())
                .build());
        assert !r.hasErrors();
    }
}
