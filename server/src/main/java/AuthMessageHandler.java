import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import messages.AuthMsg;

public class AuthMessageHandler extends ChannelInboundHandlerAdapter {

    private boolean isAuthorized;
    private String nick;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        System.out.println("AuthMessageHandler получил сообщение!");
        if (msg == null)
            return;
        else {
            if (!isAuthorized) {

                if (msg instanceof AuthMsg) {
                    AuthMsg am = (AuthMsg) msg;
                    nick = DbConnector.getNickname(am.getLogin(), am.getPassword());
                    if (nick != null) {
                        isAuthorized = true;
                        ctx.fireChannelRead(new AuthMsg(nick));

                        ReferenceCountUtil.release(msg);
                    }
                } else {
                    ReferenceCountUtil.release(msg);
                }
            } else {
                ctx.fireChannelRead(msg);
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
