package ch.epfl.data
package dblab.legobase
package optimization

import scala.language.implicitConversions
import sc.pardis.ir._
import reflect.runtime.universe.{ TypeTag, Type }
import sc.pardis.optimization._
import deep._
import sc.pardis.types._
import sc.pardis.types.PardisTypeImplicits._
// import sc.pardis.deep.scalalib.collection._

/**
 * Transforms HashMaps which have no collision in the hash function computation and also
 * in the case that the key has a continuous value into a one dimensional Array.
 *
 * @param LB the polymorphic embedding trait which contains the reified program.
 * @param queryNumber specifies the TPCH query number (TODO should be removed)
 */
class HashMapNoCollisionTransformation(val LB: LoweringLegoBase, val queryNumber: Int) extends sc.pardis.deep.scalalib.collection.HashMapOptimalNoCollisionTransformation(LB) with StructCollector[ch.epfl.data.sc.pardis.deep.scalalib.collection.HashMapOps with ch.epfl.data.sc.pardis.deep.scalalib.collection.RangeOps with ch.epfl.data.sc.pardis.deep.scalalib.ArrayOps with ch.epfl.data.sc.pardis.deep.scalalib.OptionOps with ch.epfl.data.sc.pardis.deep.scalalib.IntOps with ch.epfl.data.sc.pardis.deep.scalalib.Tuple2Ops] {
  import LB._
  override def hashMapExtractKey[A, B](self: Rep[HashMap[A, B]], value: Rep[B])(implicit typeA: TypeRep[A], typeB: TypeRep[B]): Rep[A] = typeB match {
    case t if t.isRecord && t.name.startsWith("AGGRecord") => {
      // class C
      // implicit val typeC = t.asInstanceOf[TypeRep[C]]
      // System.out.println(s"typeRepA: $typeA, record type $t")
      val structDef = getStructDef(t)
      val fieldType = structDef.map(sd => sd.fields.find(_.name == "key").get.tpe.asInstanceOf[TypeRep[A]]).getOrElse(typeA)
      // System.out.println(s"structDef $structDef fieldType $fieldType")
      field[A](value, "key")(fieldType)
      // field[A](value, "key")(value.tp.typeArguments(0).asInstanceOf[TypeRep[A]])
    }
    case t => throw new Exception(s"does not know how to extract key for type $t")
  }

  override def hashMapHash[A, B](self: Rep[HashMap[A, B]], k: Rep[A])(implicit typeA: TypeRep[A], typeB: TypeRep[B]): Rep[Int] = infix_hashCode(k) //.$amp(self.table.length.$minus(unit(1)))

  // FIXME should not be computed based on the query number
  override def hashMapBuckets[A, B](self: Rep[HashMap[A, B]])(implicit typeA: TypeRep[A], typeB: TypeRep[B]): Rep[Int] = queryNumber match {
    case 12      => unit(16)
    case 10 | 13 => unit(1 << 21)
    case 8       => unit(2000)
    // case 18 => unit(49000000)
    case _       => unit(1 << 20)
  }
}
