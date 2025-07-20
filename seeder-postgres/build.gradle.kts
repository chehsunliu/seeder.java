plugins {
    id("buildlogic.java-library-conventions")
}

dependencies {
    api(project(":seeder-core"))
    compileOnly("org.postgresql:postgresql:42.7.7")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.19.1")

    testImplementation("org.mybatis:mybatis:3.5.19")
    testImplementation("org.postgresql:postgresql:42.7.7")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.19.1")
}
