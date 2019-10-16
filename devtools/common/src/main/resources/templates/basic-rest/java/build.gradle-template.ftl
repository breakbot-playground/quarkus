// this block is necessary to make enforcedPlatform work for Quarkus plugin available
// only locally (snapshot) that is also importing the Quarkus BOM
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath "io.quarkus:quarkus-gradle-plugin:${quarkusVersion}"
    }
}

plugins {
    id 'java'
}

apply plugin: 'io.quarkus'

repositories {
     mavenLocal()
     mavenCentral()
}

sourceSets {
    nativeTest {
        compileClasspath += sourceSets.main.output
        compileClasspath += sourceSets.test.output
        runtimeClasspath += sourceSets.main.output
        runtimeClasspath += sourceSets.test.output
    }
}

configurations {
    nativeTestImplementation.extendsFrom implementation
    nativeTestRuntimeOnly.extendsFrom runtimeOnly
}

dependencies {
    implementation enforcedPlatform("io.quarkus:quarkus-bom:${quarkusVersion}")
    implementation 'io.quarkus:quarkus-resteasy'

    testImplementation 'io.quarkus:quarkus-junit5'
    testImplementation 'io.rest-assured:rest-assured'

    nativeTestImplementation 'io.quarkus:quarkus-junit5'
    nativeTestImplementation 'io.rest-assured:rest-assured'
}

group '${project_groupId}'
version '${project_version}'

compileJava {
    options.compilerArgs << '-parameters'
}

test {
    useJUnitPlatform()
}

task testNative(type: Test) {
    useJUnitPlatform()
    description = 'Runs native image tests'
    group = 'verification'

    testClassesDirs = sourceSets.nativeTest.output.classesDirs
    classpath = sourceSets.nativeTest.runtimeClasspath
    shouldRunAfter test
}

testNative.dependsOn buildNative
check.dependsOn testNative



