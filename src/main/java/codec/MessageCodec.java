package codec;

import io.scalecube.cluster.metadata.MetadataCodec;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static utils.ExceptionHandler.unhandled;

/**
 * @author ashan on 2020-05-04
 */
public class MessageCodec implements MetadataCodec {
    @Override
    public Object deserialize(ByteBuffer byteBuffer) {
//        unhandled(() -> {
//            String converted = new String(byteBuffer.array(), StandardCharsets.UTF_8);
//            System.out.println("Converted Data :: " + converted);
//            return converted;
//        });
        return byteBuffer;
    }

    @Override
    public ByteBuffer serialize(Object o) {
//        unhandled(() -> {
//            ByteBuffer buffer = ByteBuffer.wrap(o.toString().getBytes(StandardCharsets.UTF_8));
//            return buffer;
//        });
//        return null;
        return ByteBuffer.allocate(0);
    }
}
