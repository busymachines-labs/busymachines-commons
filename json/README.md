# busymachines-commons-json

Current version is `0.2.0-RC1`. SBT module id:
`"com.busymachines" %% "busymachines-commons-json" % "0.2.0-RC1"`

## How it works
This module is a thin layer over [circe](https://circe.github.io/circe/), additionally, it depends on [shapeless](https://github.com/milessabin/shapeless). The latter being the mechanism through which `auto` and `semiauto` derivation can be made to work.

You can glean 99% of what's going on here by first understanding `circe`. This module provides just convenience, and a principled way of using it. The only "real" contribution of this is that provides a `Codec` type class, which is lacking from `circe`.

### Transitive dependencies
- circe 0.9.0-M1 (with all its modules)
- shapeless 2.3.2
- cats 1.0.0-MF

## Common usage

Most likely the best way to make use of the library is to have the following imports:
```scala
import busymachines.json._
import busymachines.json.syntax._
```

`json._` brings in common type definitions, `auto` derivation of `Encoder`/`Decoder`, and an object `derive` which is the rough equivalent of circe's `semiauto`. When importing `json._`, the compiler will try to automatically generate the aforementioned type-classes whenever one is required. So, when a method like `def decodeAs[A](json: String)(implicit decoder: Decoder[A])` is called the compiler will attempt to derive a `Decoder` for a type `A`.  

`syntax._` brings in handy syntactic ops to parse strings to `Json` and/or to decode them to a specified type—these are just syntactically convenient ways of doing what the objects in `utilsJson.scala` can do.

## Decoding/encoding simple case class

### semiauto derivation (`derive`)
```scala
case class AnarchistMelon(
  noGods: Boolean,
  noMasters: Boolean,
  noSuperTypes: Boolean)

object CommonsJson extends App {

  import busymachines.json._
  import busymachines.json.syntax._

  val anarchistMelon = AnarchistMelon(true, true, true)

  //we need an encoder, so using the functions from semiauto we can explicitly create it
  implicit val encoder: Encoder[AnarchistMelon] = derive.encoder[AnarchistMelon]
  implicit val decoder: Decoder[AnarchistMelon] = derive.decoder[AnarchistMelon]

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

### auto derivation (default)
It's more or less the same, but with less boilerplate. But one should be wary of using this
except in fairly trivial cases because it takes considerably longer to compile your code.
```scala
object CommonsJson extends App {

  import busymachines.json._
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

### semiauto derivation of hierarchies

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
  import busymachines.json.syntax._

  //note that we did not import everything from ``derive``, and there's a
  //good reason why, check out the next section.
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

### combining auto (default), and semiauto derivation

Unfortunately there is a small caveat when combining these two methods of deriving json codecs. And we have to delve a bit into the internals of this library. The reason is that an implicit `Configuration` is required to derive all sealed traits, and we use the non-default way of adding in `_type` discriminator. This configuration can be found in `busymachines.json.DefaultTypeDiscriminatorConfig` and is mixed into both the root `json` package object and the `derive` object.
```scala
trait DefaultTypeDiscriminatorConfig {

  private[this] final val JsonTypeString: String = "_type"

  final implicit val defaultDerivationConfiguration: io.circe.generic.extras.Configuration =
    io.circe.generic.extras.Configuration.default
      .withDiscriminator(JsonTypeString)

}
```
Therefore, if you do a wildcard import of both, you have two conflicting implicits—that are needed to derive _other_ implicits, that's why your compilation fails with `"Could not find implicit Encoder[A]"` instead of `"Conflicting Configuration implicit.`. Therefore, the recommended pattern of combining the two modes is to wildcard import `json._`, and refer to `derive` by name—the latter having explicit methods like in the example in the previous section: `auto derivation of hierarchies`.

## provided decoders

The object/trait `busymachines.json.FailureMessageJsonCodec` contains all encoders necessary for dealing with the exceptions from core.

## tests

Check out all the tests for runnable usage examples.

## if everything else goes wrong

In case you do not like the default automatic derivation you import from package `jsonbare` in which case you simply have to add the additional import `import busymachines.jsonbare.auto._` to make automatic derivation work. `jsonbare` contains its own `syntax` and `derive` imports, so you never have to depend on `busymachines.json`.
