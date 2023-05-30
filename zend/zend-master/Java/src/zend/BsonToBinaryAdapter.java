//Edited from https://gist.github.com/Koboo/ebd7c6802101e1a941ef31baca04113d

package zend;

import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;
import java.nio.ByteBuffer;

public class BsonToBinaryAdapter {

  private static final Codec<Document> DOCUMENT_CODEC = new DocumentCodec();

  public static byte[] toBytes(Document document) {
    BasicOutputBuffer buffer = new BasicOutputBuffer();
    BsonBinaryWriter writer = new BsonBinaryWriter(buffer);
    DOCUMENT_CODEC.encode(writer, document, EncoderContext.builder().isEncodingCollectibleDocument(true).build());
    byte[] res = buffer.toByteArray();
    buffer.close();
    return res;
  }

  public static Document toDocument(byte[] bytes) {
    BsonBinaryReader reader = new BsonBinaryReader(ByteBuffer.wrap(bytes));
    Document document = DOCUMENT_CODEC.decode(reader, DecoderContext.builder().build());
    return document;
  }
}
