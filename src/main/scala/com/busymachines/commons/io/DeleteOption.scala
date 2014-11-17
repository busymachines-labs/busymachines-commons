package com.busymachines.commons.io

/**
 * Created by Lorand Szakacs, lorand.szakacs@busymachines.com, on 11/17/14.
 *
 */
//TODO: add more options like Silent, and mini DSL that allows composing them
sealed trait DeleteOption

case object DeleteRecursively extends DeleteOption

/**
 * If you are trying to delete:
 * - a directory: it will not delete the directory recursively
 * - a file: nothing, at the moment
 */
case object DefaultDelete extends DeleteOption