package io.github.chehsunliu.seeder.core;

public interface Seeder {
    void truncate();

    void seedResource(String name);
}
