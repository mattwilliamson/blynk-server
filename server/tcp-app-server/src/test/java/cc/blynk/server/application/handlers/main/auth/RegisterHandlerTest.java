package cc.blynk.server.application.handlers.main.auth;

import cc.blynk.server.core.dao.UserDao;
import cc.blynk.server.core.protocol.model.messages.ResponseMessage;
import cc.blynk.server.core.protocol.model.messages.appllication.RegisterMessage;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static cc.blynk.server.core.protocol.enums.Response.*;
import static org.mockito.Mockito.*;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 10.08.15.
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterHandlerTest {

    @Mock
    private ChannelHandlerContext ctx;

    @Mock
    private UserDao userDao;


    @Test
    public void testRegisterOk() throws Exception {
        RegisterHandler registerHandler = new RegisterHandler(userDao, null);

        String userName = "test@gmail.com";

        when(userDao.isUserExists(userName)).thenReturn(false);
        registerHandler.channelRead0(ctx, new RegisterMessage(1, userName + " 1"));

        verify(userDao).add(eq(userName), eq("1"));
    }

    @Test
    public void testRegisterOk2() throws Exception {
        RegisterHandler registerHandler = new RegisterHandler(userDao, null);

        String userName = "test@gmail.com";

        when(userDao.isUserExists(userName)).thenReturn(false);
        registerHandler.channelRead0(ctx, new RegisterMessage(1, userName + " 1"));

        verify(userDao).add(eq(userName), eq("1"));
    }

    @Test
    public void testAllowedUsersSingleUserWork() throws Exception {
        RegisterHandler registerHandler = new RegisterHandler(userDao, new String[] {"test@gmail.com"});

        String userName = "test@gmail.com";

        when(userDao.isUserExists(userName)).thenReturn(false);
        registerHandler.channelRead0(ctx, new RegisterMessage(1, userName + " 1"));

        verify(userDao).add(eq(userName), eq("1"));
    }

    @Test
    public void testAllowedUsersSingleUserNotWork() throws Exception {
        RegisterHandler registerHandler = new RegisterHandler(userDao, new String[] {"test@gmail.com"});

        String userName = "test2@gmail.com";

        when(userDao.isUserExists(userName)).thenReturn(false);
        registerHandler.channelRead0(ctx, new RegisterMessage(1, userName + " 1"));

        verify(userDao, times(0)).add(eq(userName), eq("1"));
        verify(ctx).writeAndFlush(eq(new ResponseMessage(1, NOT_ALLOWED)), any());
    }

    @Test
    public void testAllowedUsersSingleUserWork2() throws Exception {
        RegisterHandler registerHandler = new RegisterHandler(userDao, new String[] {"test@gmail.com", "test2@gmail.com"});

        String userName = "test2@gmail.com";

        when(userDao.isUserExists(userName)).thenReturn(false);
        registerHandler.channelRead0(ctx, new RegisterMessage(1, userName + " 1"));

        verify(userDao).add(eq(userName), eq("1"));
    }

}
