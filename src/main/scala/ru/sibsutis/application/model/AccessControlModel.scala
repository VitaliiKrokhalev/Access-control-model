package ru.sibsutis.application.model

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.Random
import Function.tupled

object AccessControlModel {

  private val Num = 100
  private val Readers = 2

  private def read(values: List[Int]): List[Int] = {
    val counts = Random shuffle values.zipWithIndex
    counts.find(_._1 < 26) match {
      case Some((value, index)) => values.updated(index, value + 1)
      case None => values
    }
  }

  def createModel(implicit ec: ExecutionContext): Future[List[List[Double]]] = Future {
    val buffer = ListBuffer.empty[List[Int]]
    val priorities = 1 to 100 iterator
    @tailrec
    def start(priority: Int, i: Int = 1, counts: List[Int] = List.fill(Readers)(0)): List[List[Int]] = {
      if (Random.nextInt(100) < priority) {
        if (i > 1) {
          val merged = buffer.last zip counts map tupled {_ + _}
          buffer.update(buffer.length - 1, merged)
        } else buffer += counts
        i match {
          case Num if priorities.isEmpty => buffer.toList
          case Num => start(priorities.next)
          case i => start(priority, i + 1)

        }
      } else start(priority, i, read(counts))
    }
    start(priorities.next).map {
      counts => counts.map(_ / Num.toDouble)
    }
  }
}
