package com.opticdev.core.sourcegear.graph

import better.files.File
import com.opticdev.core.debug.DebugAstNode
import com.opticdev.core.sourcegear.graph.edges.{InFile, YieldsModel}
import com.opticdev.core.sourcegear.graph.model.BaseModelNode
import com.opticdev.core.sourcegear.project.{OpticProject, ProjectBase}
import com.opticdev.core.sourcegear.sync.{SyncGraph, SyncStatusManager}
import com.opticdev.parsers.AstGraph
import com.opticdev.parsers.graph.{CommonAstNode, CustomEdge, WithinFile}
import com.opticdev.parsers.utils.Crypto
import com.opticdev.sdk.descriptions.PackageExportable

import scala.util.Try
import scalax.collection.GraphPredef.Param
import scalax.collection.edge.Implicits._
import scalax.collection.edge.LkDiEdge
import scalax.collection.mutable.Graph

import scala.collection.mutable

object ProjectGraphWrapper {
  def empty()(implicit project: ProjectBase) = new ProjectGraphWrapper(Graph[AstProjection, LkDiEdge]())
}

class ProjectGraphWrapper(val projectGraph: ProjectGraph)(implicit val project: ProjectBase) {

  import GraphImplicits._

  def addFile(astGraph: AstGraph, forFile: File) = {
    if (forFile.exists) {
      projectGraph ++= astGraphToProjectGraph(astGraph, forFile)
    }
  }

  def updateFile(astGraph: AstGraph, forFile: File) = {
    if (forFile.exists) {
      removeFile(forFile, ignoreExceptions = true)
      addFile(astGraph, forFile)
    }
  }

  def removeFile(file: File, ignoreExceptions: Boolean = false) = {
    val fileSubgraphOption = subgraphForFile(file)

    val removeFileAttempt = Try {
      val fileSubgraph = fileSubgraphOption.get
      projectGraph --= fileSubgraph
    }

    if (!ignoreExceptions && removeFileAttempt.isFailure) throw removeFileAttempt.failed.get
  }

  def nodeForId(id: String) = projectGraph.nodes.toVector.find(_.value.id == id)

  private def astGraphToProjectGraph(astGraph: AstGraph, forFile: File): ProjectGraph = {
    val newProjectGraph = Graph[AstProjection, LkDiEdge]()
    val fileNode = FileNode(forFile.pathAsString, Crypto.createSha1(forFile.contentAsString))
    astGraph.edges.toVector.foreach(edge => {
      val fromNode = edge._1.value
      val toNode = edge._2.value

      if (fromNode.isModel && toNode.isModel) {
        newProjectGraph add (fromNode.asInstanceOf[BaseModelNode] ~+#> toNode.asInstanceOf[BaseModelNode]) (edge.label)
      } else if ( (fromNode.isAstNode() || fromNode.isInstanceOf[DebugAstNode[PackageExportable]]) && toNode.isModel && edge.label.isInstanceOf[YieldsModel]) {
        newProjectGraph add (fileNode ~+#> toNode.asInstanceOf[BaseModelNode]) (InFile(fromNode.asInstanceOf[WithinFile].range))
      }
    })
    newProjectGraph
  }

  def subgraphForFile(file: File): Option[ProjectGraph] = {
    val fileNodeOption = projectGraph.fileNode(file)
    if (fileNodeOption.isDefined) {
      val fileNode = fileNodeOption.get
      val subgraphNodes = projectGraph.allSuccessorsOf(fileNode) + fileNode

      val subgraph = projectGraph.filter(projectGraph.having(
        node = (n)=> {
          subgraphNodes.contains(n)
        },
        edge = (e)=> {
          subgraphNodes.contains(e.from.value) && subgraphNodes.contains(e.to.value)
        }
      ))

      Option(subgraph)

    } else None
  }

  def query(nodeFilter: (projectGraph.NodeT) => Boolean): Set[AstProjection] =
    projectGraph.nodes.collect {
      case n: projectGraph.NodeT if nodeFilter(n) => n.value
    }.toSet

  def prettyPrint = {
    //clear it a bit println("\n\n\n")
    import GraphImplicits._
    val files = projectGraph.nodes.filter(_.value.isInstanceOf[FileNode]).toVector.sortBy(_.asInstanceOf[FileNode].filePath)
    files.foreach(file=> {
      val asFileNode = file.value.asInstanceOf[FileNode]
      println(asFileNode+":")
      projectGraph.allSuccessorsOf(asFileNode).foreach(println)
      println()
    })
  }

}