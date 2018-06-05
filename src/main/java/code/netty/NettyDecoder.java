package code.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty解码器 <p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/9/27
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class NettyDecoder extends LengthFieldBasedFrameDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyDecoder.class);

    private static final int FRAME_MAX_LENGTH = Integer.parseInt(System.getProperty("netty.frameMaxLength", "8388608"));

    public NettyDecoder(){
        super(FRAME_MAX_LENGTH, 0, Protocol.LENGTH_FILED_LENGTH, 0, 4);
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            frame = (ByteBuf) super.decode(ctx, in);
            if (null == frame) {
                return null;
            }
            return RpcCommand.decode(frame.nioBuffer());
        } catch (Exception e) {
            LOGGER.error("decode exception, " + ctx.channel(), e);
            throw new RuntimeException("Decode exception: "+e.getMessage());
        } finally {
            if (null != frame) {
                frame.release();
            }
        }
    }
}
