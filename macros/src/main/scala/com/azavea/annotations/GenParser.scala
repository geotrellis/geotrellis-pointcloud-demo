package com.azavea.annotations

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

class GenParser(cliName: String) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro GenParserImpl.impl
}

object GenParserImpl {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def isOptional(fieldType: Type): Boolean = fieldType <:< typeOf[Option[_]]
    def isLiftedEqal[T: TypeTag](fieldType: Type): Boolean = fieldType =:= typeOf[T] || fieldType =:= typeOf[Option[T]]

    def extractAnnotationParameters(tree: Tree): Tree = tree match {
      case q"new $name( ..$params )" if params.length == 1 => params.head
      case _ => throw new Exception("GenParser annotation must have only least one parameter.")
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

    val cliName = extractAnnotationParameters(c.prefix.tree)

    def generateCliHelp(fields: Seq[Tree]): String = {
      val options = fields.map { field =>
        val (fieldName, fieldType, defaultValue) = extractTree(field)

        if(defaultValue.isEmpty)
          throw new Exception("case class to generate patter should be with default values only.")

        def typeToString(fieldType: Type) = {
          if(isOptional(fieldType)) s"$fieldType"
          else s"non-empty $fieldType"
        }

        def defaultValueToString(defaultValue: Tree) = {
          if(defaultValue.isEmpty || isOptional(fieldType) && defaultValue != TypeName("None")) ""
          else s"[default: $defaultValue]"
        }

        s"""
            |  --$fieldName <value>
            |      $fieldName is a ${typeToString(fieldType)} property ${defaultValueToString(defaultValue)}
        """
      } mkString ""

      println(cliName)

      s"""
         |$cliName
         |
         |Usage: $cliName [options]
         |
         $options
         |  --help
         |      prints this usage text
       """ replaceAll("\"", "") replaceAll("(?m)^[ \t]*\r?\n", "") stripMargin
    }

    def modifiedDeclaration(classDecl: ClassDef) = {
      val (_, className, fields: Seq[Tree], _, _) = extractCaseClassesParts(classDecl)
      val helpMessage = generateCliHelp(fields)

      println(helpMessage)

      val default = cq"""Nil => opts"""
      val help = cq""""--help" :: tail => { println(help); sys.exit(1) }"""
      val noopt = cq"""option :: tail => { println(s"Unknown option $${option}"); println(help); sys.exit(1) }"""

      val cases = default +: fields.map { field =>
        val (fieldName, fieldType, defaultValue) = extractTree(field)
        val fn = s"--$fieldName"

        val additionalCast =
          if(isLiftedEqal[Int](fieldType)) q"value.toInt"
          else if(isLiftedEqal[Long](fieldType)) q"value.toLong"
          else if (isLiftedEqal[Boolean](fieldType)) q"value.toBoolean"
          else if (isLiftedEqal[Byte](fieldType)) q"value.toByte"
          else if (isLiftedEqal[Char](fieldType)) q"value.toChar"
          else if (isLiftedEqal[Short](fieldType)) q"value.toShort"
          else if (isLiftedEqal[Float](fieldType)) q"value.toFloat"
          else if (isLiftedEqal[Double](fieldType)) q"value.toDouble"
          else if (isLiftedEqal[Symbol](fieldType)) q"value.toSymbol"
          else q"value"

        if(isOptional(fieldType)) cq"""$fn :: value :: tail => nextOption(opts.copy($fieldName = Some(${additionalCast})), tail)"""
        else cq"""$fn :: value :: tail => nextOption(opts.copy($fieldName = ${additionalCast}), tail)"""
      } :+ help :+ noopt

     q"""
         val help = $helpMessage
         def nextOption(opts: $className, list: Seq[String]): $className = list.toList match {
           case ..$cases
         }
         def parse(args: Seq[String]): $className = nextOption(new $className(), args)
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
      case _ => c.abort(c.enclosingPosition, "Invalid annotation target: must be a case class")
    }

    c.Expr[Any](result)
  }
}
