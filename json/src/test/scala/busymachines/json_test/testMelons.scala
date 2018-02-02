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
package busymachines.json_test

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 09 Aug 2017
  *
  */
private[json_test] case class AnarchistMelon(
  noGods:       Boolean,
  noMasters:    Boolean,
  noSuperTypes: Boolean
)

private[json_test] sealed trait Melon {
  def weight: Int
}

private[json_test] case class WinterMelon(
  fuzzy:  Boolean,
  weight: Int
) extends Melon

private[json_test] case class WaterMelon(
  seeds:  Boolean,
  weight: Int
) extends Melon

private[json_test] case object SmallMelon extends Melon {
  override val weight: Int = 0
}

private[json_test] sealed trait Taste

private[json_test] case object SweetTaste extends Taste

//I ran out of ideas, ok? I'll think of better test data.
private[json_test] case object SourTaste extends Taste

private[json_test] sealed trait TastyMelon extends Melon {
  def tastes: Seq[Taste]
}

private[json_test] case class SquareMelon(
  weight: Int,
  tastes: Seq[Taste]
) extends TastyMelon
