package com.opticdev.core.languages

import better.files.File
import com.opticdev.core.Fixture.TestBase
import com.opticdev.core.Fixture.compilerUtils.GearUtils
import com.opticdev.core.sourcegear.project.ProjectBase
import com.opticdev.core.sourcegear.{Render, SGConstructor}
import com.opticdev.opm.packages.{OpticMDPackage, OpticPackage}
import com.opticdev.parsers.SourceParserManager
import org.scalatest.{BeforeAndAfterAll, FunSpec}
import play.api.libs.json.{JsObject, JsString}

class ScalaSpec extends TestBase with GearUtils with BeforeAndAfterAll {

  implicit val languageName: String = "scala"
  implicit val project: ProjectBase = null

  override def beforeAll: Unit = {
    SourceParserManager.enableParser(new com.opticdev.parsers.scala.OpticParser)
    super.beforeAll
  }

  it("can find the scala parser") {
    assert(SourceParserManager.parserByLanguageName("scala").isDefined)
  }

  lazy val scalaPackage = OpticPackage.fromString("{\"description\":{\"info\":{\"author\":\"optic\",\"package\":\"scala-test\",\"version\":\"0.1.0\",\"range\":{\"start\":0,\"end\":81}},\"schemas\":[],\"lenses\":[{\"name\":\"example-scala\",\"id\":\"example-scala\",\"snippet\":{\"block\":\"val hello = world\\n\",\"language\":\"es7\"},\"value\":{\"value\":{\"type\":\"token\",\"at\":{\"astType\":\"Term.Name\",\"range\":{\"start\":12,\"end\":17}}},\"definedAs\":{\"type\":\"token\",\"at\":{\"astType\":\"Term.Name\",\"range\":{\"start\":4,\"end\":9}}}},\"variables\":{},\"containers\":{},\"schema\":{\"type\":\"object\",\"properties\":{\"value\":{\"type\":\"string\"},\"definedAs\":{\"type\":\"string\"}}},\"initialValue\":{},\"internal\":false,\"range\":{\"start\":141,\"end\":577}}],\"transformations\":[]},\"errors\":[]}").get.resolved(Map())
  lazy val sg = sourceGearFromPackage(scalaPackage)

  it("can parse scala code into model") {
    val found = sg.parseString("val name = me").get.modelNodes.head
    assert(found.value == JsObject(Seq("definedAs" -> JsString("name"), "value" -> JsString("me"))))
  }

  it("can generate scala code") {
    val generated = Render.simpleNode(sg.lensSet.listLenses.head.schemaRef,
      JsObject(Seq("definedAs" -> JsString("abcdefg"), "value" -> JsString("hijklmnop"))))(sg)
      .get

    assert(generated._2 == "val abcdefg = hijklmnop")
  }



}
