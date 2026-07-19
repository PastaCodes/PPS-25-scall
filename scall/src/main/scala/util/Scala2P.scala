package it.unibo.scall
package util

import alice.tuprolog.{Prolog, Theory}

import java.io.InputStream

def engineWithTheoryFile(source: InputStream): Prolog =
  val engine = Prolog()
  val theory = Theory.parseLazilyWithStandardOperators(source)
  engine.setTheory(theory)
  engine
