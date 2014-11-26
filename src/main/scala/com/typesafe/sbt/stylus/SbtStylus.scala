package com.typesafe.sbt.stylus

import sbt._
import sbt.Keys._
import com.typesafe.sbt.web._
import com.typesafe.sbt.jse.SbtJsTask
import spray.json._

object Import {

  object StylusKeys {
    val stylus = TaskKey[Seq[File]]("stylus", "Invoke the stylus compiler.")

    val compress = SettingKey[Boolean]("stylus-compress", "Compress output by removing some whitespaces.")
    val useNib = SettingKey[Boolean]("stylus-nib", "Use stylus nib.")
    val includeCSS = SettingKey[Boolean]("stylus-include-css", "Includes css on import.")
    val inlineImages = SettingKey[Boolean]("stylus-inline-images", "Encodes inline images.")
    val inlineFunction = SettingKey[String]("stylus-inline-function", "Sets the stylus function name for inline image encoding.")
    val inlineThreshold = SettingKey[Int]("stylus-inline-threshold", "Sets the byte threshold for inline image encoding.")
    val sourceMap = SettingKey[Boolean]("stylus-source-map", "Flag for using sourcemaps.")
    val sourceMapInline = SettingKey[Boolean]("stylus-source-map-inline", "Sets the sourcemap type to inline.")
    val sourceMapRoot = SettingKey[String]("stylus-source-map-root", "Sets the sourcemap root path.")
    val sourceMapBase = SettingKey[String]("stylus-source-map-base", "Sets the sourcemap base path.")
  }

}

object SbtStylus extends AutoPlugin {

  override def requires = SbtJsTask

  override def trigger = AllRequirements

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import SbtJsTask.autoImport.JsTaskKeys._
  import autoImport.StylusKeys._

  val stylusUnscopedSettings = Seq(

    includeFilter := GlobFilter("main.styl"),

    jsOptions := JsObject(
      "compress" -> JsBoolean(compress.value),
      "useNib" -> JsBoolean(useNib.value),
      "includeCSS" -> JsBoolean(includeCSS.value),
      "inlineImages" -> JsBoolean(inlineImages.value),
      "inlineFunction" -> JsString(inlineFunction.value),
      "inlineThreshold" -> JsNumber(inlineThreshold.value),
      "sourceMap" -> JsBoolean(sourceMap.value),
      "sourceMapInline" -> JsBoolean(sourceMapInline.value),
      "sourceMapRoot" -> JsString(sourceMapRoot.value),
      "sourceMapBase" -> JsString(sourceMapBase.value)
    ).toString()
  )

  override def projectSettings = Seq(
    compress := false,
    useNib := false,
    includeCSS := false,
    inlineImages := false,
    inlineFunction := "url",
    inlineThreshold := 10000,
    sourceMap := false,
    sourceMapInline := false,
    sourceMapRoot := "",
    sourceMapBase := ""

  ) ++ inTask(stylus)(
    SbtJsTask.jsTaskSpecificUnscopedSettings ++
      inConfig(Assets)(stylusUnscopedSettings) ++
      inConfig(TestAssets)(stylusUnscopedSettings) ++
      Seq(
        moduleName := "stylus",
        shellFile := getClass.getClassLoader.getResource("stylus-shell.js"),

        taskMessage in Assets := "Stylus compiling",
        taskMessage in TestAssets := "Stylus test compiling"
      )
  ) ++ SbtJsTask.addJsSourceFileTasks(stylus) ++ Seq(
    stylus in Assets := (stylus in Assets).dependsOn(webModules in Assets).value,
    stylus in TestAssets := (stylus in TestAssets).dependsOn(webModules in TestAssets).value
  )

}