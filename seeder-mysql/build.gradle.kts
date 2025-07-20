plugins {
    id("buildlogic.java-library-conventions")
}

dependencies {
    api(project(":seeder-core"))
    compileOnly("com.mysql:mysql-connector-j:8.4.0")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.19.1")

    testImplementation("com.mysql:mysql-connector-j:8.4.0")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.19.1")
}
