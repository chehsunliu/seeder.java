plugins {
    id("buildlogic.java-library-conventions")
}

dependencies {
    implementation(project(":seeder-core"))
    implementation("software.amazon.awssdk:s3")
}
