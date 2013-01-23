package com.thenewmotion.scynapse.test

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

import org.axonframework.test.matchers.Matchers._


trait EventMatchers {
  def isEqualTo[T](o: T) = org.hamcrest.CoreMatchers.equalTo(o)

  def isLike(pf: =>PartialFunction[Any, Boolean]) = new BaseMatcher[Any] {

    def matches(item: Any) = pf.isDefinedAt(item) && pf.apply(item)

    def describeTo(description: Description) {
      description.appendText("is like partially described expectation")
    }
  }

  def allPayloadsOf(items: Any*) = {
    payloadsMatching(listWithAllOf(
      (items map isEqualTo): _*
    ))
  }

  def withPayloads(matchers: Matcher[_]*) =
    payloadsMatching(listWithAllOf(matchers: _*))

}
