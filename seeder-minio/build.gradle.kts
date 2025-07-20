plugins {
    id("buildlogic.java-library-conventions")
}

dependencies {
    api(project(":seeder-core"))
    compileOnly("software.amazon.awssdk:s3")

    testImplementation("software.amazon.awssdk:s3")
}
