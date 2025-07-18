plugins {
    id("buildlogic.java-library-conventions")
}

dependencies {
    implementation(project(":seeder-core"))
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.1")
}
