package cc.blynk.server.hardware.handlers;

import cc.blynk.server.core.BlockingIOProcessor;
import cc.blynk.server.core.dao.SessionDao;
import cc.blynk.server.core.dao.UserDao;
import cc.blynk.server.core.model.DashBoard;
import cc.blynk.server.core.model.Profile;
import cc.blynk.server.core.model.auth.User;
import cc.blynk.server.core.model.widgets.notifications.Twitter;
import cc.blynk.server.core.protocol.enums.Command;
import cc.blynk.server.core.protocol.exceptions.NotificationBodyInvalidException;
import cc.blynk.server.core.protocol.exceptions.QuotaLimitException;
import cc.blynk.server.core.protocol.model.messages.MessageFactory;
import cc.blynk.server.core.protocol.model.messages.hardware.TweetMessage;
import cc.blynk.server.core.session.HardwareStateHolder;
import cc.blynk.server.hardware.exceptions.NotifNotAuthorizedException;
import cc.blynk.server.hardware.handlers.hardware.logic.TweetLogic;
import cc.blynk.server.notifications.twitter.TwitterWrapper;
import cc.blynk.utils.ServerProperties;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

/**
 * The Blynk Project.
 * Created by Andrew Zakordonets.
 * Created on 26.04.15.
 */
@RunWith(MockitoJUnitRunner.class)
public class TweetHandlerTest {

	@Mock
	private static ServerProperties props;

	@Mock
	private BlockingIOProcessor blockingIOProcessor;

	@Mock
	private TwitterWrapper twitterWrapper;

	@Mock
	private ChannelHandlerContext ctx;

	@Mock
	private UserDao userDao;

	@Mock
	private SessionDao sessionDao;

	@Mock
	private TweetLogic tweetHandler;

	@Mock
	private User user;

	@Mock
	private Profile profile;

	@Mock
	private DashBoard dash;


    private HardwareStateHolder state;

    @Before
    public void setup() {
        state = new HardwareStateHolder(1, user, "x");
    }

	@Test(expected = NotificationBodyInvalidException.class)
	public void testTweetMessageWithEmptyBody() {
		TweetMessage tweetMessage = (TweetMessage) MessageFactory.produce(1, Command.TWEET, "");
        state.user.profile = profile;
		TweetLogic tweetHandler = new TweetLogic(blockingIOProcessor, twitterWrapper, 60);
		tweetHandler.messageReceived(ctx, state, tweetMessage);
	}

	@Test(expected = NotificationBodyInvalidException.class)
	public void testTweetMessageWithBodyMoreThen140Symbols() {
		final String longBody = RandomStringUtils.random(150);
		TweetMessage tweetMessage = (TweetMessage) MessageFactory.produce(1, Command.TWEET, longBody);
        state.user.profile = profile;
		TweetLogic tweetHandler = new TweetLogic(blockingIOProcessor, twitterWrapper, 60);
		tweetHandler.messageReceived(ctx, state, tweetMessage);
	}

	@Test(expected = NotifNotAuthorizedException.class)
	public void testTweetMessageWithNoTwitterAccessToken() {
		TweetMessage tweetMessage = (TweetMessage) MessageFactory.produce(1, Command.TWEET, "test tweet");
		TweetLogic tweetHandler = new TweetLogic(blockingIOProcessor, twitterWrapper, 60);
        state.user.profile = profile;
        when(state.user.profile.getDashById(1, 1)).thenReturn(dash);
        when(dash.getWidgetByType(Twitter.class)).thenReturn(null);
		tweetHandler.messageReceived(ctx, state, tweetMessage);
	}

	@Test(expected = NotifNotAuthorizedException.class)
	public void testTweetMessageWithTwitterTokenNull() {
		TweetMessage tweetMessage = (TweetMessage) MessageFactory.produce(1, Command.TWEET, "test tweet");
		TweetLogic tweetHandler = new TweetLogic(blockingIOProcessor, twitterWrapper, 60);
        state.user.profile = profile;
		Twitter twitter = new Twitter();
		twitter.token = null;
		twitter.secret = "secret_token";
		when(state.user.profile.getDashById(1, 1)).thenReturn(dash);
		when(dash.getWidgetByType(Twitter.class)).thenReturn(twitter);
		tweetHandler.messageReceived(ctx, state, tweetMessage);
	}

	@Test(expected = NotifNotAuthorizedException.class)
	public void testTweetMessageWithTwitterTokenEmpty() {
		TweetMessage tweetMessage = (TweetMessage) MessageFactory.produce(1, Command.TWEET, "test tweet");
		TweetLogic tweetHandler = new TweetLogic(blockingIOProcessor, twitterWrapper, 60);
        state.user.profile = profile;
		Twitter twitter = new Twitter();
		twitter.token = null;
		twitter.secret = "secret_token";
		when(state.user.profile.getDashById(1, 1)).thenReturn(dash);
		when(dash.getWidgetByType(Twitter.class)).thenReturn(twitter);
		tweetHandler.messageReceived(ctx, state, tweetMessage);
	}

	@Test(expected = NotifNotAuthorizedException.class)
	public void testTweetMessageWithTwitterSecretTokenNull() {
		TweetMessage tweetMessage = (TweetMessage) MessageFactory.produce(1, Command.TWEET, "test tweet");
		TweetLogic tweetHandler = new TweetLogic(blockingIOProcessor, twitterWrapper, 60);
        state.user.profile = profile;
		Twitter twitter = new Twitter();
		twitter.token = "token";
		twitter.secret = null;
		when(state.user.profile.getDashById(1, 1)).thenReturn(dash);
		when(dash.getWidgetByType(Twitter.class)).thenReturn(twitter);
		tweetHandler.messageReceived(ctx, state, tweetMessage);
	}

	@Test(expected = NotifNotAuthorizedException.class)
	public void testTweetMessageWithTwitterSecretTokenEmpty() {
		TweetMessage tweetMessage = (TweetMessage) MessageFactory.produce(1, Command.TWEET, "test tweet");
		TweetLogic tweetHandler = new TweetLogic(blockingIOProcessor, twitterWrapper, 60);
        state.user.profile = profile;
		Twitter twitter = new Twitter();
		twitter.token = "token";
		twitter.secret = null;
		when(state.user.profile.getDashById(1, 1)).thenReturn(dash);
		when(dash.getWidgetByType(Twitter.class)).thenReturn(twitter);
		tweetHandler.messageReceived(ctx, state, tweetMessage);
	}

	@Test(expected = QuotaLimitException.class)
	public void testSendQuotaLimitationException() throws InterruptedException {
		TweetMessage tweetMessage = (TweetMessage) MessageFactory.produce(1, Command.TWEET, "this is a test tweet");
		TweetLogic tweetHandler = spy(new TweetLogic(blockingIOProcessor, twitterWrapper, 60));
        state.user.profile = profile;
		Twitter twitter = new Twitter();
		twitter.token = "token";
		twitter.secret = "secret_token";
		when(state.user.profile.getDashById(1, 1)).thenReturn(dash);
		when(dash.getWidgetByType(Twitter.class)).thenReturn(twitter);
		dash.isActive = true;

		tweetHandler.messageReceived(ctx, state, tweetMessage);
		tweetHandler.messageReceived(ctx, state, tweetMessage);
	}

	@Test
	public void testSendQuotaLimitationIsWorking() throws InterruptedException {
		TweetMessage tweetMessage = (TweetMessage) MessageFactory.produce(1, Command.TWEET, "this is a test tweet");
		ServerProperties props = new ServerProperties();
		props.setProperty("notifications.frequency.user.quota.limit", "1");
		final long defaultQuotaTime = props.getLongProperty("notifications.frequency.user.quota.limit") * 1000;
		TweetLogic tweetHandler = spy(new TweetLogic(blockingIOProcessor, twitterWrapper, 60));
		state.user.profile = profile;
		Twitter twitter = new Twitter();
		twitter.token = "token";
		twitter.secret = "secret_token";
		when(state.user.profile.getDashById(1, 1)).thenReturn(dash);
		when(dash.getWidgetByType(Twitter.class)).thenReturn(twitter);
		dash.isActive = true;

		tweetHandler.messageReceived(ctx, state, tweetMessage);
		TimeUnit.MILLISECONDS.sleep(defaultQuotaTime);
		tweetHandler.messageReceived(ctx, state, tweetMessage);
	}

}
