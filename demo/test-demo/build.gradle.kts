import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.0"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"

	id("io.github.lognet.grpc-spring-boot") version "4.7.0"
	id("com.google.protobuf") version "0.8.18"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

extra["testcontainersVersion"] = "1.17.2"

val grpcKotlinVersion = "1.3.0"
val grpcVersion = "1.47.0"
val protobufVersion = "3.21.1"

protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc:$protobufVersion"
	}
	plugins {
		id("grpc") {
			artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
		}
		id("grpckt") {
			artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk8@jar"
		}
	}
	generateProtoTasks {
		all().forEach { task ->
			task.plugins {
				id("grpc") { }
				id("grpckt") { }
			}
		}
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	implementation("io.github.lognet:grpc-spring-boot-starter:4.7.0")
	implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
	implementation("io.grpc:grpc-protobuf:$grpcVersion")
	implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mongodb")

	testImplementation(platform("io.kotest:kotest-bom:5.3.1"))
	testImplementation("io.kotest:kotest-runner-junit5")

	testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.1")

	testImplementation("io.mockk:mockk:1.12.4")

}

dependencyManagement {
	imports {
		mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
