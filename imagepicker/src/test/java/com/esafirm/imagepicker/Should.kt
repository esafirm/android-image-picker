package com.esafirm.imagepicker

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo

infix fun <T> T.shouldBe(value: Any?) = should(equalTo(value))
infix fun <T> T.shouldNot(value: Any?) = should(equalTo(value).not())
infix fun <T> T.shouldBe(matcher: Matcher<T>) = should(matcher)
infix fun <T> T.shouldNot(matcher: Matcher<T>) = should(matcher.not())

private infix fun <T> T.should(matcher: Matcher<T>) = assertThat(this, matcher)