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
package busymachines.effects

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 29 Jan 2018
  *
  */
package object sync
    extends TryTypeDefinitons with ResultTypeDefinitions with OptionSyntax.Implicits with EitherSyntax.Implicits
    with ResultSyntax.Implicits with TrySyntax.Implicits {

  object tr extends TryTypeDefinitons with TrySyntax.Implicits

  object option extends OptionSyntax.Implicits

  object either extends EitherSyntax.Implicits

  object result extends ResultTypeDefinitions with ResultSyntax.Implicits with ResultCompanionAliases

}
