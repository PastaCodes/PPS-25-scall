package it.unibo.scall

import util.engineWithTheoryFile

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.*

class FirstFollowTest extends AnyFunSuite:

  test("should load theory from file"):
    noException should be thrownBy
      engineWithTheoryFile(getClass.getResourceAsStream("/prolog/first_follow.pl"))
