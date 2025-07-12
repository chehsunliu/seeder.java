package io.github.chehsunliu.seeder.core;

import java.util.List;
import lombok.Builder;
import lombok.Singular;

@Builder
public class SeederManager {
    @Singular
    private List<Seeder> seeders;

    public void truncate() {
        this.seeders.forEach(Seeder::truncate);
    }

    public void seedResource(String name) {
        this.seeders.forEach(seeder -> seeder.seedResource(name));
    }
}
