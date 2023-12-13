package io.kenji.courier.codec;

import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.common.utils.SerializationUtils;
import io.kenji.courier.constants.RpcConstants;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.enumeration.RpcType;
import io.kenji.courier.protocol.header.RpcHeader;
import io.kenji.courier.protocol.request.RpcRequest;
import io.kenji.courier.protocol.response.RpcResponse;
import io.kenji.courier.serialization.api.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Optional;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/26
 **/
public class RpcDecoder extends ByteToMessageDecoder implements RpcCodec {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < RpcConstants.HEADER_TOTAL_LEN) {
            return;
        }
        in.markReaderIndex();
        short magic = in.readShort();
        if (magic != RpcConstants.MAGIC) {
            throw new IllegalArgumentException("magic number is illegal, magic number = " + magic);
        }
        byte msgType = in.readByte();
        byte status = in.readByte();
        long requestId = in.readLong();

        ByteBuf serializationTypeByteBuf = in.readBytes(SerializationUtils.MAX_SERIALIZATION_TYPE_LEN);
        String serializationType = SerializationUtils.subString(serializationTypeByteBuf.toString(CharsetUtil.UTF_8));
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        Optional<RpcType> msgTypeEnum = RpcType.findByType(msgType);
        if (msgTypeEnum.isEmpty()) return;
        RpcHeader header = RpcHeader.builder()
                .magic(magic)
                .status(status)
                .requestId(requestId)
                .msgType(msgType)
                .serializationType(SerializationType.valueOf(serializationType))
                .msgLen(dataLength)
                .build();
        //TODO serialization
        Serialization serialization = gerJdkSerialization();
        switch (msgTypeEnum.get()) {
            case REQUEST -> {
                RpcRequest request = serialization.deserialize(data, RpcRequest.class);
                if (request != null) {
                    out.add(RpcProtocol.builder().header(header).body(request).build());
                }
            }
            case RESPONSE -> {
                RpcResponse response = serialization.deserialize(data, RpcResponse.class);
                if (response != null) {
                    out.add(RpcProtocol.builder().header(header).body(response).build());
                }
            }
            //TODO case HEARTBEAT
        }
    }

}
