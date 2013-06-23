
//  }
//  
//  /**
//   * The returned Media might have a different id.
//   */
//  def store(mimeType : String, name : Option[String], data : Array[Byte]) : Future[Media] = {
//    def hash = hasher.hashBytes(data).toString
//    val stringData = encoding.encode(data)
//    super.find(termFilter("mimeType", mimeType), termFilter("hash", hash)) flatMap {
//      _.find(m => m.data == stringData && m.name == name) match {
//        case Some(HashedMedia(id, mimeType, name, hash, data)) =>
//          Future.successful(Media(Id(id.toString), mimeType, name, encoding.decode(data)))
//        case None =>
//          val id = Id.generate[Media]
//          super.create(HashedMedia(Id(id.toString), mimeType, name, hash, stringData)).map(_ => Media(id, mimeType, name, data))
//      }
//    }
//  }
//  
//  def importUrl(url : String) : Future[Option[Media]] = {
//    Future(readUrl(url)) flatMap {
//      case Some(bytes) => 
//        val name = url.substring(url.lastIndexOf('/') + 1)
//        store(MimeType.fromResourceName(name), name, bytes).map(Option(_))
//      case None => Future.successful(None)
//    }
//  }
//  
//  def readUrl(url : String) : Option[Array[Byte]] = {
//    try {
//      if (url.toString.isEmpty()) None
//      else {
//        val bis = new BufferedInputStream(new URL(url.toString).openStream())
//        val bytes = Stream.continually(bis.read).takeWhile(-1 != _).map(_.toByte).toArray
//        bis.close
//        Some(bytes)
//      }
//    }
//    catch {
//      case t : Throwable => println(t); None
//    }
//  }
//}