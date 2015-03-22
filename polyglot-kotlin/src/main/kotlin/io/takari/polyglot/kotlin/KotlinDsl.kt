package io.takari.polyglot.kotlin;

import org.apache.maven.model.Model
import java.util.ArrayList
import java.util.HashMap

fun main(args: Array<String>) {
  val result =
    model {
      head {
        title { +"XML encoding with Kotlin" }
      }
      body {
        h1 { +"XML encoding with Kotlin" }
        p { +"this format can be used as an alternative markup to XML" }

        // an element with attributes and text content
        a(href = "http://jetbrains.com/kotlin") { +"Kotlin" }

        // mixed content
        p {
          +"This is some"
          b { +"mixed" }
          +"text. For more see the"
          a(href = "http://jetbrains.com/kotlin") { +"Kotlin" }
          +"project"
        }
        p { +"some text" }

        // content generated from command-line arguments
        p {
          +"Command line arguments were:"
          ul {
            for (arg in args)
              li { +arg }
          }
        }
      }
    }
  println(result)
}

fun model(parse: MavenModel.() -> Unit): MavenModel {
  val model = MavenModel()
  model.parse()
  return model
}

trait Element {
  fun parse(model: Model, indent: String)

  override fun toString(): String {
    val builder = Model()
    parse(builder, "")
    return builder.toString()
  }
}

class TextElement(val text: String) : Element {
  override fun parse(builder: Model, indent: String) {
    //builder.append("$indent$text\n")
  }
}

abstract class Tag(val name: String) : Element {
  val children: ArrayList<Element> = ArrayList<Element>()
  val attributes = HashMap<String, String>()

  protected fun initTag<T : Element>(tag: T, init: T.() -> Unit): T {
    tag.init()
    children.add(tag)
    return tag
  }

  override fun parse(builder: Model, indent: String) {
    //builder.append("$indent<$name${renderAttributes()}>\n")
    for (c in children) {
      c.parse(builder, indent + "  ")
    }
    //builder.append("$indent</$name>\n")
  }

  private fun renderAttributes(): String? {
    val builder = StringBuilder()
    for (a in attributes.keySet()) {
      builder.append(" $a=\"${attributes[a]}\"")
    }
    return builder.toString()
  }
}

abstract class TagWithText(name: String) : Tag(name) {
  fun String.plus() {
    children.add(TextElement(this))
  }
}

class MavenModel() : TagWithText("html") {
  fun head(init: Head.() -> Unit) = initTag(Head(), init)
  fun body(init: Body.() -> Unit) = initTag(Body(), init)
}

class Head() : TagWithText("head") {
  fun title(init: Title.() -> Unit) = initTag(Title(), init)
}

class Title() : TagWithText("title")

abstract class BodyTag(name: String) : TagWithText(name) {
  fun b(init: B.() -> Unit) = initTag(B(), init)
  fun p(init: P.() -> Unit) = initTag(P(), init)
  fun h1(init: H1.() -> Unit) = initTag(H1(), init)
  fun ul(init: UL.() -> Unit) = initTag(UL(), init)
  fun a(href: String, init: A.() -> Unit) {
    val a = initTag(A(), init)
    a.href = href
  }
}

class Body() : BodyTag("body")
class UL() : BodyTag("ul") {
  fun li(init: LI.() -> Unit) = initTag(LI(), init)
}

class B() : BodyTag("b")
class LI() : BodyTag("li")
class P() : BodyTag("p")
class H1() : BodyTag("h1")
class A() : BodyTag("a") {
  public var href: String
    get() = attributes["href"]!!
    set(value) {
      attributes["href"] = value
    }
}



