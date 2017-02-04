package com.azavea.annotation

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

class GenParser(name: String, requiredFields: String*) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro GenParserImpl.impl
}

@macrocompat.bundle
object GenParserImpl {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def isOptional(fieldType: Type): Boolean = fieldType <:< typeOf[Option[_]]
    def isLiftedOptEq[T: TypeTag](fieldType: Type): Boolean = fieldType =:= typeOf[T] || fieldType =:= typeOf[Option[T]]
    def isPrimitive(fieldType: Type) =
      isLiftedOptEq[Int](fieldType) ||
      isLiftedOptEq[Long](fieldType) ||
      isLiftedOptEq[Boolean](fieldType) ||
      isLiftedOptEq[Byte](fieldType) ||
      isLiftedOptEq[Char](fieldType) ||
      isLiftedOptEq[Short](fieldType) ||
      isLiftedOptEq[Float](fieldType) ||
      isLiftedOptEq[Double](fieldType) ||
      isLiftedOptEq[Symbol](fieldType)

    def extractAnnotationParameters(tree: Tree): (Tree, Seq[String]) = tree match {
      case q"new $name( ..$params )" if params.nonEmpty => {
        val l = params.length
        if(l == 1) params.head -> Seq()
        else if(l > 1) {
          val req = params(1).toString
          if(req.contains("requiredFields =")) {
            val fst = Seq(req.split("requiredFields =").mkString("").trim)
            (params.head, (if (l == 2) fst else fst.toList ::: params.seq.slice(2, l).toList) map (s => s"--$s".replaceAll("\"", "")))
          } else (params.head, Seq())
        } else
          throw new Exception("GenParser annotation must have only one parameter or additional annotated required fields.")
      }
      case _ => throw new Exception("GenParser annotation must have only one parameter or additional annotated required fields.")
    }

    def extractCaseClassesParts(classDecl: ClassDef) = classDecl match {
      case classDef @ q"case class $className(..$fields) extends ..$parents { ..$body }" =>
        (classDef, className, fields, parents, body)
    }

    def extractTree(field: Tree) = {
      val f = field.asInstanceOf[ValDef]
      val fieldName = f.name
      val fieldType = c.typecheck(q"type T = ${f.tpt}") match {
        case TypeDef(_, _, _, rhs) => rhs.tpe
      }
      val defaultValue = f.rhs

      (fieldName, fieldType, defaultValue)
    }

    val (name, reqFields) = extractAnnotationParameters(c.prefix.tree)

    def generateCliHelp(fields: Seq[Tree]): String = {
      val options = fields.map { field =>
        val (fieldName, fieldType, defaultValue) = extractTree(field)

        if(defaultValue.isEmpty)
          throw new Exception("Annotated case class should be with default values only.")

        def typeToString(fieldType: Type) = {
          if(isPrimitive(fieldType)) {
            if (isOptional(fieldType)) s"${fieldType.typeArgs.head}"
            else s"non-empty $fieldType"
          } else "non-empty String"
        }

        def defaultValueToString(fieldType: Type, defaultValue: Tree) = {
          if(defaultValue.isEmpty || isOptional(fieldType) && defaultValue != q"None") ""
          else if(!isPrimitive(fieldType) && defaultValue.children.length > 1) s"[default: ${defaultValue.children.tail.mkString(",")}]"
          else s"[default: $defaultValue]"
        }

        s"""
            |  --$fieldName <value>
            |      $fieldName is a ${typeToString(fieldType)} property ${defaultValueToString(fieldType, defaultValue)}
        """
      } mkString ""

      s"""
         |$name
         |
         |Usage: $name [options]
         |
         $options
         |  --help
         |      prints this usage text
       """ replaceAll("\"", "") replaceAll("(?m)^[ \t]*\r?\n", "") stripMargin
    }

    def modifiedDeclaration(classDecl: ClassDef) = {
      val (_, className, fields: Seq[Tree], _, _) = extractCaseClassesParts(classDecl)
      val helpMessage = generateCliHelp(fields)

      val default = cq"""Nil => opts"""
      val help = cq""""--help" :: tail => { println(help); sys.exit(1) }"""
      val noopt = cq"""option :: tail => { println(s"Unknown option $${option}"); println(help); sys.exit(1) }"""

      val cases = default +: fields.map { field =>
        val (fieldName, fieldType, _) = extractTree(field)
        val fn = s"--$fieldName"

        val additionalCast =
          if(isLiftedOptEq[Int](fieldType)) q"value.toInt"
          else if(isLiftedOptEq[Long](fieldType)) q"value.toLong"
          else if (isLiftedOptEq[Boolean](fieldType)) q"value.toBoolean"
          else if (isLiftedOptEq[Byte](fieldType)) q"value.toByte"
          else if (isLiftedOptEq[Char](fieldType)) q"value.toChar"
          else if (isLiftedOptEq[Short](fieldType)) q"value.toShort"
          else if (isLiftedOptEq[Float](fieldType)) q"value.toFloat"
          else if (isLiftedOptEq[Double](fieldType)) q"value.toDouble"
          else if (isLiftedOptEq[Symbol](fieldType)) q"value.toSymbol"
          else q"value"

        if(isOptional(fieldType)) cq"""$fn :: value :: tail => nextOption(opts.copy($fieldName = Some(${additionalCast})), tail)"""
        else cq"""$fn :: value :: tail => nextOption(opts.copy($fieldName = ${additionalCast}), tail)"""
      } :+ help :+ noopt

     q"""
         val help = $helpMessage
         def nextOption(opts: $className, list: Seq[String]): $className = list.toList match {
           case ..$cases
         }
         def parse(args: Seq[String]): $className = {
           val reqFields = Seq(..$reqFields)
           if(reqFields.nonEmpty && reqFields.diff(args).nonEmpty) {
             println(s"Required fields not passed: $${reqFields.diff(args)}")
             sys.exit(1)
           }

           nextOption(new $className(), args)
         }
         def apply(args: Seq[String]): $className = parse(args)
      """
    }

    val result = annottees map (_.tree) match {
      case (classDef @ q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }")
        :: Nil if mods.hasFlag(Flag.CASE) =>
        val name = tpname.toTermName
        val objGen = modifiedDeclaration(classDef.asInstanceOf[ClassDef])

        q"""
         $classDef
         object $name {
           ..$objGen
         }
         """
      case (classDef @ q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }")
        :: q"object $objName extends { ..$objEarlyDefs } with ..$objParents { $objSelf => ..$objDefs }"
        :: Nil if mods.hasFlag(Flag.CASE) =>
        val objGen = modifiedDeclaration(classDef.asInstanceOf[ClassDef])

        q"""
         $classDef
         object $objName extends { ..$objEarlyDefs} with ..$objParents { $objSelf =>
           ..$objGen
           ..$objDefs
         }
         """
      case _ => c.abort(c.enclosingPosition, "Invalid annotation target: must be a case class.")
    }

    c.Expr[Any](result)
  }
}
