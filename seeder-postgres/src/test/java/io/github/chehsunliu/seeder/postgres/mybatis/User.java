package io.github.chehsunliu.seeder.postgres.mybatis;

public record User(int id, String username, int createdAt, Auth auth) {
    public record Auth(String type) {}
}
