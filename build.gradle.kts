// Top-level build file where you can add configuration options common to all sub-projects/modules.

allprojects {
    dependencyLocking {
        lockAllConfigurations()
    }
}

plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}
