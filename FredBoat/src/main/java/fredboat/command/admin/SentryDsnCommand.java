/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Frederik Ar. Mikkelsen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fredboat.command.admin;

import ch.qos.logback.classic.LoggerContext;
import fredboat.command.util.HelpCommand;
import fredboat.commandmeta.abs.Command;
import fredboat.commandmeta.abs.CommandContext;
import fredboat.commandmeta.abs.ICommandRestricted;
import fredboat.perms.PermissionLevel;
import fredboat.util.GitRepoState;
import io.sentry.Sentry;
import io.sentry.logback.SentryAppender;
import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by napster on 07.09.17.
 * <p>
 * Override the DSN for sentry. Pass stop or clear to turn it off.
 */
public class SentryDsnCommand extends Command implements ICommandRestricted {

    private static final Logger log = LoggerFactory.getLogger(SentryDsnCommand.class);

    @Override
    public void onInvoke(CommandContext context) {
        if (context.args.length < 2) {
            HelpCommand.sendFormattedCommandHelp(context);
            return;
        }
        String dsn = context.args[1];

        if (dsn.equals("stop") || dsn.equals("clear")) {
            turnOff();
            context.replyWithName("Sentry service has been stopped");
        } else {
            turnOn(dsn);
            context.replyWithName("New Sentry DSN has been set!");
        }
    }

    public static void turnOn(String dsn) {
        log.info("Turning on sentry");
        Sentry.init(dsn).setRelease(GitRepoState.getGitRepositoryState().commitId);
        getSentryLogbackAppender().start();
    }

    public static void turnOff() {
        log.info("Turning off sentry");
        Sentry.close();
        getSentryLogbackAppender().stop();
    }

    private static SentryAppender getSentryLogbackAppender() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        return (SentryAppender) lc.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("SENTRY");
    }

    @Override
    public String help(Guild guild) {
        return "{0}{1} <sentry DSN> OR {0}{1} stop\n#Set a temporary sentry DSN overriding the one from the config until" +
                " the next restart, or stop the sentry service.";
    }

    @Override
    public PermissionLevel getMinimumPerms() {
        return PermissionLevel.BOT_ADMIN;
    }
}
