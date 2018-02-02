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
package busymachines.rest_json_test.routes_to_test

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
private[rest_json_test] case class SomeTestDTOGet(
  int:    Int,
  string: String,
  option: Option[Int]
)

private[rest_json_test] case class SomeTestDTOPost(
  string: String,
  option: Option[Int]
)

private[rest_json_test] case class SomeTestDTOPut(
  string: String,
  option: Option[Int]
)

private[rest_json_test] case class SomeTestDTOPatch(
  string: String
)
