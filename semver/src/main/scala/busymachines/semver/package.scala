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

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 13 Nov 2017
  *
  */
package object semver {

  /**
    * convenient aliases to [[SemanticVersion]]. A lot of people use these names
    * because of lazyness, might as well support some trivial type aliases
    * out of the box.
    */
  type SemVer = SemanticVersion
  val SemVer: SemanticVersion.type = SemanticVersion

  /**
    * In this particular case this is by no means a "dangerous" implicit,
    * since we just want to do automatic lifting into Option, because
    * it is the only way labels are used
    */
  implicit def labelToOptLabel(label: Label): Option[Label] = Option[Label](label)
}
