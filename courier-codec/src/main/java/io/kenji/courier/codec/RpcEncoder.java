package io.kenji.courier.codec;

import io.kenji.courier.common.utils.SerializationUtils;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.header.RpcHeader;
import io.kenji.courier.serialization.api.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/26
 **/
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol<Object>> implements RpcCodec {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol<Object> msg, ByteBuf byteBuf) throws Exception {
        RpcHeader header = msg.getHeader();
        byteBuf.writeShort(header.getMagic());
        byteBuf.writeByte(header.getMsgType());
        byteBuf.writeByte(header.getStatus());
        byteBuf.writeLong(header.getRequestId());
        String serializationType = header.getSerializationType().toString();
        //TODO Serialization
        Serialization serialization = getSerialization(serializationType);

        byteBuf.writeBytes(SerializationUtils.paddingString(serializationType).getBytes(StandardCharsets.UTF_8));
        byte[] data = serialization.serialize(msg.getBody());
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }


}
