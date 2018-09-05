package com.opticdev.server.http

import java.io.{BufferedReader, InputStreamReader}

import better.files.File
import com.opticdev.cli.binder.BindCLI
import com.opticdev.common.storage.{DataDirectory, DataDirectoryConfig}
import com.opticdev.core.sourcegear.project.Project
import com.opticdev.core.sourcegear.project.config.ProjectFile
import com.opticdev.opm.PackageManager
import com.opticdev.opm.providers.LocalProvider
import com.opticdev.opm.storage.{PackageStorage, ParserStorage}
import com.opticdev.parsers
import com.opticdev.parsers.SourceParserManager
import com.opticdev.server.state.ProjectsManager
import java.io.{File => JFile}
import scala.io.Source
import scala.util.Try

object Lifecycle extends App {

  //init the data directory if missing
  DataDirectory.init

  SourceParserManager.enableParser(new parsers.es7.OpticParser)
  SourceParserManager.enableParser(new parsers.scala.OpticParser)

  implicit val projectsManager: ProjectsManager = new ProjectsManager()
  implicit val actorCluster = projectsManager.actorCluster

  startup
  def startup = {

    Try(BindCLI.toNativeBash(
      new JFile(this.getClass.getProtectionDomain.getCodeSource.getLocation.toURI).getPath))

    Server.start()

    DataDirectoryConfig.triggerMigration

  }


}
