/**
  * Copyright (c) 2017-2018 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package busymachines

import busymachines.effects.async._
import busymachines.effects.sync._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 02 Feb 2018
  *
  */
package object effects
    extends AnyRef with OptionSyntax.Implicits with OptionSyntaxAsync.Implcits with TryTypeDefinitons
    with TrySyntax.Implicits with TrySyntaxAsync.Implcits with EitherSyntax.Implicits with EitherSyntaxAsync.Implcits
    with ResultTypeDefinitions with ResultCompanionAliases with ResultSyntax.Implicits with ResultSyntaxAsync.Implcits
    with FutureTypeDefinitions with FutureSyntax.Implicits with IOTypeDefinitions with IOSyntax.Implicits
    with TaskTypeDefinitions with TaskSyntax.Implicits {

  object option extends OptionSyntax.Implicits with OptionSyntaxAsync.Implcits

  object tr extends TryTypeDefinitons with TrySyntax.Implicits with TrySyntaxAsync.Implcits

  object either extends EitherSyntax.Implicits with EitherSyntaxAsync.Implcits

  object result
      extends ResultTypeDefinitions with ResultCompanionAliases with ResultSyntax.Implicits
      with ResultSyntaxAsync.Implcits

  object io extends IOTypeDefinitions with IOSyntax.Implicits

  object future extends FutureTypeDefinitions with FutureSyntax.Implicits

  object task extends TaskTypeDefinitions with TaskSyntax.Implicits

}
