plugins {
    id("buildlogic.java-library-conventions")
}

dependencies {
    api(project(":seeder-core"))
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.5.4")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.19.1")

    testImplementation("org.mariadb.jdbc:mariadb-java-client:3.5.4")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.19.1")
}
