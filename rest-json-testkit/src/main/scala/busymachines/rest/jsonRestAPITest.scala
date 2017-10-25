package busymachines.rest

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Assertions, Suite}
import busymachines.json._
import busymachines.json.syntax._


/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 19 Oct 2017
  *
  */
trait JsonRestAPITest extends
  RestAPITest with
  JsonRequestRunners with
  JsonRestAPIRequestBuildingSugar with
  jsonrest.JsonSupport {
  this: Suite with Assertions =>
  /**
    * Seriously, we get this ridiculous error:
    * https://github.com/sbt/zinc/issues/292
    * {{{
    * [error] ## Exception when compiling 13 sources to /Users/lorandszakacs/workspace/bm/busymachines-commons/rest-json-testkit/target/scala-2.12/test-classes
    * [error] assertion failed: Modified names for busymachines.rest.JsonRestAPITest is empty
    * [error] scala.Predef$.assert(Predef.scala:219)
    * [error] sbt.internal.inc.NamesChange.<init>(Changes.scala:54)
    * [error] sbt.internal.inc.IncrementalNameHashing.sameAPI(IncrementalNameHashing.scala:46)
    * [error] sbt.internal.inc.IncrementalCommon.sameClass(IncrementalCommon.scala:205)
    * [error] sbt.internal.inc.IncrementalCommon.$anonfun$changedIncremental$1(IncrementalCommon.scala:187)
    * [error] scala.collection.TraversableLike.$anonfun$flatMap$1(TraversableLike.scala:241)
    * [error] scala.collection.Iterator.foreach(Iterator.scala:929)
    * [error] scala.collection.Iterator.foreach$(Iterator.scala:929)
    * [error] scala.collection.AbstractIterator.foreach(Iterator.scala:1417)
    * [error] scala.collection.MapLike$DefaultKeySet.foreach(MapLike.scala:178)
    * [error] scala.collection.TraversableLike.flatMap(TraversableLike.scala:241)
    * [error] scala.collection.TraversableLike.flatMap$(TraversableLike.scala:238)
    * [error] scala.collection.AbstractTraversable.flatMap(Traversable.scala:104)
    * [error] sbt.internal.inc.IncrementalCommon.changedIncremental(IncrementalCommon.scala:186)
    * [error] sbt.internal.inc.IncrementalCommon.changedInitial(IncrementalCommon.scala:243)
    * [error] sbt.internal.inc.Incremental$.compile(Incremental.scala:61)
    * [error] sbt.internal.inc.IncrementalCompile$.apply(Compile.scala:70)
    * [error] sbt.internal.inc.IncrementalCompilerImpl.compileInternal(IncrementalCompilerImpl.scala:309)
    * [error] sbt.internal.inc.IncrementalCompilerImpl.$anonfun$compileIncrementally$1(IncrementalCompilerImpl.scala:267)
    * [error] sbt.internal.inc.IncrementalCompilerImpl.handleCompilationError(IncrementalCompilerImpl.scala:158)
    * [error] sbt.internal.inc.IncrementalCompilerImpl.compileIncrementally(IncrementalCompilerImpl.scala:237)
    * [error] sbt.internal.inc.IncrementalCompilerImpl.compile(IncrementalCompilerImpl.scala:68)
    * [error] sbt.Defaults$.compileIncrementalTaskImpl(Defaults.scala:1403)
    * [error] sbt.Defaults$.$anonfun$compileIncrementalTask$1(Defaults.scala:1385)
    * [error] scala.Function1.$anonfun$compose$1(Function1.scala:44)
    * [error] sbt.internal.util.$tilde$greater.$anonfun$$u2219$1(TypeFunctions.scala:42)
    * [error] sbt.std.Transform$$anon$4.work(System.scala:64)
    * [error] sbt.Execute.$anonfun$submit$2(Execute.scala:257)
    * [error] sbt.internal.util.ErrorHandling$.wideConvert(ErrorHandling.scala:16)
    * [error] sbt.Execute.work(Execute.scala:266)
    * [error] sbt.Execute.$anonfun$submit$1(Execute.scala:257)
    * [error] sbt.ConcurrentRestrictions$$anon$4.$anonfun$submitValid$1(ConcurrentRestrictions.scala:167)
    * [error] sbt.CompletionService$$anon$2.call(CompletionService.scala:32)
    * [error] java.util.concurrent.FutureTask.run(FutureTask.java:266)
    * [error] java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
    * [error] java.util.concurrent.FutureTask.run(FutureTask.java:266)
    * [error] java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
    * [error] java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
    * [error] java.lang.Thread.run(Thread.java:748)
    * [error]
    * [info] Done compiling.
    * }}}
    */
  @scala.deprecated("I keep this here, because the sbt incremental compiler dies if it's not here", "0.2.0-RC")
  protected val `thisIsUtterlyUselessPleaseDontUse`: Int = 42
}

private[rest] trait JsonRequestRunners extends DefaultRequestRunners {
  this: ScalatestRouteTest =>

  import busymachines.json._

  override protected def transformEntityString(entityString: String): String = {
    JsonParsing.parseString(entityString) match {
      case Left(_) => entityString
      case Right(value) => value.spaces2NoNulls
    }
  }
}

private[rest] trait JsonRestAPIRequestBuildingSugar extends RestAPIRequestBuildingSugar {
  this: ScalatestRouteTest =>

  protected def postJson[R](uri: String)(raw: Json)(thunk: => R)
    (implicit cc: CallerContext): R = {
    val g = Post(uri).withEntity(ContentTypes.`application/json`, raw.spaces2NoNulls)
    requestRunner.runRequest(g)(thunk)
  }

  protected def patchJson[R](uri: String)(raw: Json)(thunk: => R)
    (implicit cc: CallerContext): R = {
    val g = Patch(uri).withEntity(ContentTypes.`application/json`, raw.spaces2NoNulls)
    requestRunner.runRequest(g)(thunk)
  }

  protected def putJson[R](uri: String)(raw: Json)(thunk: => R)
    (implicit cc: CallerContext): R = {
    val g = Put(uri).withEntity(ContentTypes.`application/json`, raw.spaces2NoNulls)
    requestRunner.runRequest(g)(thunk)
  }
}
