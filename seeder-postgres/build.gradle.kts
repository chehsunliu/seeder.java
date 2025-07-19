plugins {
    id("buildlogic.java-library-conventions")
}

dependencies {
    implementation(project(":seeder-core"))
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.1")

    testImplementation("org.mybatis:mybatis:3.5.19")
}
