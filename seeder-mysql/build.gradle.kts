plugins {
    id("buildlogic.java-library-conventions")
}

dependencies {
    implementation(project(":seeder-core"))
    implementation("com.mysql:mysql-connector-j:8.4.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.1")
}
