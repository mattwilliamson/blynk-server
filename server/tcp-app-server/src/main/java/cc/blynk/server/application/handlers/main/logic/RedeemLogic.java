package cc.blynk.server.application.handlers.main.logic;

import cc.blynk.server.core.BlockingIOProcessor;
import cc.blynk.server.core.model.auth.User;
import cc.blynk.server.core.protocol.model.messages.ResponseMessage;
import cc.blynk.server.core.protocol.model.messages.StringMessage;
import cc.blynk.server.db.DBManager;
import cc.blynk.server.db.Redeem;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static cc.blynk.server.core.protocol.enums.Response.*;

/**
 * Handler responsible for handling redeem logic. Unlocks premium content for predefined tokens.
 * Used for kickstarter backers and other companies that paid for redeeming.
 *
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 02.03.16.
 */
public class RedeemLogic {

    private static final Logger log = LogManager.getLogger(RedeemLogic.class);

    private final BlockingIOProcessor blockingIOProcessor;
    private final DBManager dbManager;

    public RedeemLogic(DBManager dbManager, BlockingIOProcessor blockingIOProcessor) {
        this.blockingIOProcessor = blockingIOProcessor;
        this.dbManager = dbManager;
    }

    private static boolean isAlreadyUnlocked(User user) {
        return false;
    }

    public void messageReceived(ChannelHandlerContext ctx, User user, StringMessage message) {
        String redeemToken = message.body;

        if (isAlreadyUnlocked(user)) {
            ctx.writeAndFlush(new ResponseMessage(message.id, OK), ctx.voidPromise());
            return;
        }

        blockingIOProcessor.execute(() -> ctx.writeAndFlush(verifyToken(message, redeemToken, user.name), ctx.voidPromise()));
    }

    private ResponseMessage verifyToken(StringMessage message, String redeemToken, String username) {
        try {
            Redeem redeem = dbManager.selectRedeemByToken(redeemToken);
            if (redeem != null) {
                if (redeem.isRedeemed && redeem.username.equals(username)) {
                    return new ResponseMessage(message.id, OK);
                } else if (!redeem.isRedeemed && dbManager.updateRedeem(username, redeemToken)) {
                    unlockContent();
                    return new ResponseMessage(message.id, OK);
                }
            }
        } catch (Exception e) {
            log.debug("Error redeeming token.", e);
        }

        return new ResponseMessage(message.id, NOT_ALLOWED);
    }

    private void unlockContent() {
        log.info("Unlocking content...");
    }

}
