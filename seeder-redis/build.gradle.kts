plugins {
    id("buildlogic.java-library-conventions")
}

dependencies {
    implementation(project(":seeder-core"))
    implementation("redis.clients:jedis:6.0.0")
}
