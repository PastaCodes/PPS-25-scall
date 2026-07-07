package it.unibo.scall

extension [A](self: Set[Seq[A]])
  infix def eachAppend(e: A): Set[Seq[A]] =
    self.map(_ appended e)
  infix def productConcat(other: Set[Seq[A]]): Set[Seq[A]] =
    for
      x <- self
      y <- other
    yield x concat y

extension [K](self: Set[K])
  def associateWith[V](valueSelector: K => V): Map[K, V] =
    self.map(k => k -> valueSelector(k)).toMap

extension [K, V](self: Map[K, V])
  def mapValues1[W](transform: V => W): Map[K, W] =
    self.map((k, v) => (k, transform(v)))

type MultiMap[K, V] = Map[K, Set[V]]

extension [K, V](self: MultiMap[K, V])
  def getOrEmpty(key: K): Set[V] =
    self.getOrElse(key, Set.empty)
  infix def unionAll(other: MultiMap[K, V]): MultiMap[K, V] =
    val keys = self.keySet union other.keySet
    keys.associateWith(k => self.getOrEmpty(k) union other.getOrEmpty(k))
