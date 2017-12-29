# busymachines-commons-json

[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-json_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-json_2.12)

Current version is `0.2.0-RC8`. SBT module id:
`"com.busymachines" %% "busymachines-commons-json" % "0.2.0-RC8"`

## How it works
This module is a thin layer over [circe](https://circe.github.io/circe/), additionally, it depends on [shapeless](https://github.com/milessabin/shapeless). The latter being the mechanism through which `autoderive` and `derive` derivation can be made to work.

You can glean 99% of what's going on here by first understanding `circe`. This module provides just convenience, and a principled way of using it. The only "real" contribution of this is that provides a `Codec` type class, which is lacking from `circe`.

### Transitive dependencies
- circe 0.9.0-M1 (with all its modules)
- shapeless 2.3.2
- cats 1.0.0-MF

## Recommended usage

If you are writing a full-fledged web-server with literally hundreds of REST API endpoints, then it is a good idea to minimize the usage of `import busymachines.json.autoderive._` because this can seriously increase your compilation times, and instead use the following pattern:

```scala
object DomainSpecificJsonCodecs extends DomainSpecificJsonCodecs

trait DomainSpecificJsonCodecs {
  import busymachines.json._

  implicit val postDTOCodec: Codec[PostDTO] = derive.codec[PostDTO]
  implicit val getDTOCodec : Codec[GetDTO]  = derive.codec[GetDTO]
}
//...

object WhereINeedMyCodecUsuallyInMyEndpointDefinitions extends DomainSpecificJsonCodecs {
  //....
}
//or:
object OtherPlaceINeedMyJson {
  import DomainSpecificJsonCodecs._

}
```

But for small-scale stuff, sure, go crazy with `autoderive._`.

## Description

Most likely the best way to make use of the library is to have the following imports:
```scala
import busymachines.json._
import busymachines.json.syntax._
```

`json._` brings in common type definitions, and an object `derive` which is the rough equivalent of circe's `semiauto`, and an object `autoderive` which is a rough equivalent of circe's `auto`. When importing `autoderive._`, the compiler will try to automatically generate `Encoder[T]`, `Decoder[T]` type-classes whenever one is required. So, when a method like `def decodeAs[A](json: String)(implicit decoder: Decoder[A])` is called the compiler will attempt to derive a `Decoder` for a type `A`.  

`syntax._` brings in handy syntactic ops to parse strings to `Json` and/or to decode them to a specified type—these are just syntactically convenient ways of doing what the objects in `utilsJson.scala` can do.

## Decoding/encoding simple case classes

### semi-auto derivation (`busymachines.json.derive`)
```scala
case class AnarchistMelon(
  noGods: Boolean,
  noMasters: Boolean,
  noSuperTypes: Boolean)

object CommonsJson extends App {

  import busymachines.json._
  import busymachines.json.syntax._

  val anarchistMelon = AnarchistMelon(true, true, true)

  //we need an encoder, so using the functions from derive we can explicitly create it
  implicit val encoder: Encoder[AnarchistMelon] = derive.encoder[AnarchistMelon]
  implicit val decoder: Decoder[AnarchistMelon] = derive.decoder[AnarchistMelon]
  //Alternatively, if we always need both encoders/decoders then we just:
  //implicit val decoder: Codec[AnarchistMelon] = derive.codec[AnarchistMelon]

  val jsonString: String = anarchistMelon.asJson.spaces2
  println(jsonString)
  println(jsonString.unsafeDecodeAs[AnarchistMelon])
}
```
Will print:
```json
{
  "noGods" : true,
  "noMasters" : true,
  "noSuperTypes" : true
}
```
```
AnarchistMelon(true,true,true)
```

### automatic derivation (`busymachines.json.autoderive`)
It's more or less the same, but with less boilerplate. But one should be wary of using this
except in fairly trivial cases because it takes considerably longer to compile your code.
```scala
object CommonsJson extends App {

  import busymachines.json._
  import busymachines.json.autoderive._
  import busymachines.json.syntax._

  val anarchistMelon = AnarchistMelon(true, true, true)

  val jsonString: String = anarchistMelon.asJson.spaces2
  println(jsonString)
  println(jsonString.unsafeDecodeAs[AnarchistMelon])
}
```

### Codec
If you know you need both an `Encoder` and `Decoder` then you can just use `Codec` which is both of those things. So in the `derive` case we can simplify the code more by deriving only:
```scala
implicit val codec: Codec[AnarchistMelon] = deriveCodec[AnarchistMelon]
```

## Dealing with hierarchies

Unlike the `AnarchistMelon` in our previous example, regular melons accept hierarchy. And, like in real life, we have many hierarchies in code. But `circe` helps to wrangle them in—for dealing with real life hierarchies you should google Murray Bookchin.

Take the following hierarchy:  

```scala
sealed trait Melon { def weight: Int}
case class WinterMelon(fuzzy: Boolean, weight: Int) extends Melon
case class WaterMelon(seeds: Boolean, weight: Int) extends Melon
case object SmallMelon extends Melon { override val weight: Int = 0 }

sealed trait Taste
case object SweetTaste extends Taste
case object SourTaste extends Taste

sealed trait TastyMelon extends Melon { def tastes: Seq[Taste]}
case class SquareMelon(weight: Int, tastes: Seq[Taste]) extends TastyMelon
```
As you can see we have two parallel hierarchies. `Melon`, and `Taste` (this one being a vanilla way of doing "enumerations"). And a sub-hierarchy `TastyMelon`.
Now, as per good practice we'll put the Codecs separately from our application code.

### semi-auto derivation of hierarchies

```scala
object MelonsDefaultJsonCodecs {
  import busymachines.json._

  //this is a special method that encodes/decodes sealed trait hierarchies
  //composed of case objects only as plain strings, as opposed to the derivedCodec
  implicit val tasteEncoder: Codec[Taste] = derive.enumerationCodec[Taste]
  implicit val melonEncoder: Codec[Melon] = derive.codec[Melon]
}
```

Variants of an abstract datatype are distinguished by inserting a field in the json object called `"_type"` whose value is the name of the variant. As can be seen from the example bellow:
```scala
object CommonsJson extends App {
  import busymachines.json.syntax._
  import MelonsDefaultJsonCodecs._

  val winterMelon = WinterMelon(fuzzy = true, weight = 45)
  val waterMelon = WaterMelon(seeds = true, weight = 90)
  val smallMelon = SmallMelon
  val squareMelon = SquareMelon(weight = 10, tastes = Seq(SourTaste, SweetTaste))
  val melons = List[Melon](winterMelon, waterMelon, smallMelon, squareMelon)

  val rawJson = melons.asJson.spaces2
  println(rawJson)
  rawJson.unsafeDecodeAs[List[Melon]]
}
```
Notice that the tastes array contains simple strings, but the `SmallMelon`, even though it was a case object is still represented as a json object.
```json
[
  {
    "fuzzy" : true,
    "weight" : 45,
    "_type" : "WinterMelon"
  },
  {
    "seeds" : true,
    "weight" : 90,
    "_type" : "WaterMelon"
  },
  {
    "_type" : "SmallMelon"
  },
  {
    "weight" : 10,
    "tastes" : [
      "SourTaste",
      "SweetTaste"
    ],
    "_type" : "SquareMelon"
  }
]
```

### auto derivation of hierarchies

It should be clear by now what we have to do. Essentially nothing, but if we want to maintain the same behavior when converting `Taste` case objects, then we do have to explicitly create a codec for it. Otherwise it would represent this enumeration as json objects with only the `_type` field in them.

So, our code in this case looks like the following—and the result is the same:
```scala
object CommonsJson extends App {

  import busymachines.json._
  import busymachines.json.autoderive._
  import busymachines.json.syntax._

  //The explicitly defined enumeration codec
  implicit val tasteEncoder: Codec[Taste] = derive.enumerationCodec[Taste]

  val winterMelon = WinterMelon(fuzzy = true, weight = 45)
  val waterMelon = WaterMelon(seeds = true, weight = 90)
  val smallMelon = SmallMelon
  val squareMelon = SquareMelon(weight = 10, tastes = Seq(SourTaste, SweetTaste))
  val melons = List[Melon](winterMelon, waterMelon, smallMelon, squareMelon)

  val rawJson = melons.asJson.spaces2
  println(rawJson)
  rawJson.unsafeDecodeAs[List[Melon]]
}
```

### On `_type` discrimination

Here we have to delve a bit into the internals of this library. We depend on the `circe.generic.extras` packages, where derivation requires an implicit an implicit `Configuration` to derive all sealed traits. This configuration can be found in `busymachines.json.DefaultTypeDiscriminatorConfig` and is mixed into both the root `json` package object, so that it can be made available to us in both use cases:

semi-auto:
```scala
import busymachines.json._
implicit val e = derive.encoder[T]
```
auto:
```scala
import busymachines.json._
import busymachines.json.autoderive._
val m: Melon = ???
m.asJson
```
In either case if you remove the `busymachines.json._` import, it will fail to compile. But importing:
`import busymachines.json.defaultDerivationConfiguration` is also enough. You can also define your own rules — just look at the implementation of this default config. But, you have to be very careful not to import both, or have both in scope. If you do that then have two conflicting implicits—that are needed to derive _other_ implicits, that's why your compilation fails with `"Could not find implicit Encoder[A]"` instead of `"Conflicting Configuration implicit.`. Therefore, it is recommended that you do the following:

```scala
/**
  * unfortunately this exclusion is absolutely necessary if you want to use the non-default _type
  * discriminator for sealed hierarchies of classes AND auto-derivation at the same time
  */
import busymachines.json.{defaultDerivationConfiguration => _, _}
import busymachines.json.autoderive._

final implicit val _melonManiaDiscriminatorConfig: Configuration =
  Configuration.default.withDiscriminator("_melonMania")
```

As illustrated in the `JsonAutoDerivationWithSpecialConfigurationTest` test.

The definition of the magic configuration trait:
```scala
trait DefaultTypeDiscriminatorConfig {

  private[this] final val JsonTypeString: String = "_type"

  final implicit val defaultDerivationConfiguration: io.circe.generic.extras.Configuration =
    io.circe.generic.extras.Configuration.default
      .withDiscriminator(JsonTypeString)

}
```

## provided decoders

The object/trait `busymachines.json.AnomalyJsonCodec` contains all encoders necessary for dealing with the exceptions from core.

## tests

Check out all the tests for runnable usage examples.
