//package com.kentivo.mdm.commons
//
//import java.net.URLEncoder
//import scala.sys.process.Process
//import scala.sys.process.ProcessIO
//import scala.sys.process.ProcessIO
//import com.typesafe.config.ConfigFactory
//import scala.concurrent.Future
//import scala.concurrent.future
//import scala.concurrent.ExecutionContext
//import scala.concurrent.duration._
//import scala.concurrent.Await
//
//object ProcessUtils extends Logging {
//
//  lazy val config = ConfigFactory.load
//  lazy val isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0
//  lazy val timeout = config.getInt("com.kentivo.mdm.phantom.timeoutSec") seconds
//  lazy val phantomExePath = config.getString("com.kentivo.mdm.phantom.path") match {
//    case s if isWindows && !s.endsWith(".exe") => s + ".exe"
//    case s => s
//  }
//  lazy val phantomJsPath = config.getString("com.kentivo.mdm.phantom.js.path")
//  lazy val phantomTempFilesPath = config.getString("com.kentivo.mdm.phantom.tempFiles.path") match {
//    case s if s.endsWith("/") => s
//    case s => s + "/"
//  }
//
//  def runCsvPhantomProcess(url: String)(implicit context : ExecutionContext): (List[String], List[String], Int, String) = {
//    val csvOutputFile = phantomTempFilesPath + StringUtils.randomAlpha(10) + ".csv"
//    val (out, err, exit) = runProcess(s"${phantomExePath}  --local-storage-quota=0 --disk-cache=no --local-to-remote-url-access=true ${phantomJsPath} csv ${URLEncoder.encode(url, "UTF-8")} ${csvOutputFile}")
//
//    (out, err, exit, csvOutputFile)
//  }
//  
//  def runPdfPhantomProcess(url: String)(implicit context : ExecutionContext): (List[String], List[String], Int, String) = {
//    val pdfOutputFile = phantomTempFilesPath + StringUtils.randomAlpha(10) + ".pdf"
//    val (out, err, exit) = runProcess(s"${phantomExePath} --local-storage-quota=0 --disk-cache=no --local-to-remote-url-access=true ${phantomJsPath} pdf ${URLEncoder.encode(url, "UTF-8")} ${pdfOutputFile}") 
//    (out, err, exit, pdfOutputFile)
//  }
//
//  def runProcess(in: String)(implicit context : ExecutionContext): (List[String], List[String], Int) = {
//    debug("--START process: " + in)
//    val qb = Process(in)
//    var out = List[String]()
//    var err = List[String]()
//
//    val pio = new ProcessIO(_ => (),
//                        stdout => scala.io.Source.fromInputStream(stdout)
//                          .mkString("").foreach(print),
//                        _ => ())
//   
//    // Future is needed, because on some platforms, the caller is blocked.
//    val exit = future {
//      qb.run(pio).exitValue
//    }
//
//    debug("--EXIT process output: " + out)
//    debug("--EXIT process error: " + err)
//    (out.reverse, err.reverse, Await.result(exit, timeout))
//  }
//}