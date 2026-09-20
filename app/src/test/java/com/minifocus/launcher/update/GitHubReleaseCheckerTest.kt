package com.minifocus.launcher.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubReleaseCheckerTest {

    @Test
    fun newerTagIsDetected() {
        assertTrue(GitHubReleaseChecker.isNewer("v2.7.1", "2.7.0"))
        assertTrue(GitHubReleaseChecker.isNewer("v2.10.0", "2.9.9"))
        assertTrue(GitHubReleaseChecker.isNewer("v3", "2.7.0"))
    }

    @Test
    fun sameOrOlderTagIsNotAnUpdate() {
        assertFalse(GitHubReleaseChecker.isNewer("v2.7.0", "2.7.0"))
        assertFalse(GitHubReleaseChecker.isNewer("v2.7", "2.7.0"))
        assertFalse(GitHubReleaseChecker.isNewer("v2.6.9", "2.7.0"))
    }

    @Test
    fun unparseableTagIsIgnored() {
        assertFalse(GitHubReleaseChecker.isNewer("", "2.7.0"))
        assertFalse(GitHubReleaseChecker.isNewer("latest", "2.7.0"))
    }
}
