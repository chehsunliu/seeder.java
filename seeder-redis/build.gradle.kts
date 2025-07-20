plugins {
    id("buildlogic.java-library-conventions")
}

dependencies {
    api(project(":seeder-core"))
    compileOnly("redis.clients:jedis:6.0.0")

    testImplementation("redis.clients:jedis:6.0.0")
}
